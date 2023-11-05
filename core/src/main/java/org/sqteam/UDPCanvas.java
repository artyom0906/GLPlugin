package org.sqteam;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.util.ui.ImageUtil;
import org.sqteam.networkl.GUIEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.concurrent.*;

public class UDPCanvas extends Canvas implements Runnable {

    private static final long serialVersionUID = 1L;

    private int width = 1000;
    private int height = 1000;

    private Thread thread;
    private boolean running = false;
   //private final DatagramChannel server;
   ServerSocketChannel serverSocket;
   private SocketChannel client;
    BufferedImage image;
    private final Project project;
    private final JPanel windowContent;
    public UDPCanvas(Project project, JPanel windowContent) throws IOException {
        this.project = project;
        this.windowContent = windowContent;

        //ColorConvertOp xformOp = new ColorConvertOp(null);
        //xformOp.filter(this.image, image);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }

    public synchronized void start() {
        running = true;
        thread = new Thread(this, "Display");
        thread.start();

    }

    public synchronized void stop() {
        running = false;
        try {
            thread.join();
            serverSocket.close();
            client.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static String extractMessage(ByteBuffer buffer) {
        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        String msg = new String(bytes);

        return msg;
    }

    public void run() {
        try {
            init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while (running) {
            try {
                update();
            } catch (Exception e) {
                System.out.println(e);
            }
            render();

            // Control frame rate
            try {
                Thread.sleep(5);
            } catch (InterruptedException ex) {
            }
        }

    }

    private void init() throws IOException {
        serverSocket = ServerSocketChannel.open();

        serverSocket.socket().bind(new InetSocketAddress("0.0.0.0", 7576));
        System.out.println("waiting for connection");
        client = serverSocket.accept();
        System.out.println("connected");
        //this.server = DatagramChannel
        //        .open()
        //        .bind(new InetSocketAddress("0.0.0.0", 2345));
        /*
        ByteBuffer res = ByteBuffer.allocate(1024*1024*4);
        client.configureBlocking(false);
        while (true) {
            ByteBuffer tmp = ByteBuffer.allocate(1024*1024);

            int bytesRead = client.read(tmp);
            if (bytesRead == -1) {
                break;
            }

            // rewind ByteBuffer to get it back to start
            tmp.rewind();

            for (int i = 0; i < bytesRead; i++) {
                byte cur = tmp.get(i);
                res.put(cur);
            }

            // reached end of message, break loop
            if (bytesRead < 32*1024) {
                break;
            }
        }
        res.flip();
        width = res.getShort();
        height = res.getShort();
        byte[] data = Arrays.copyOfRange(res.array(), 4, res.array().length);
        //res.get(data);
        BufferedImage raw_image = ImageUtil.createImage(width, height, BufferedImage.TYPE_3BYTE_BGR);//new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR)
        raw_image.setData(Raster.createRaster(raw_image.getSampleModel(), new DataBufferByte(data, data.length), new Point() ) );
        this.image = raw_image;*/



        registerListeners();
    }

    private void registerListeners() {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                sendEvent(e.getX(), e.getY(), e.getButton(), GUIEvent.EventType.MOUSE_CLICK);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                sendEvent(e.getX(), e.getY(), e.getButton(), GUIEvent.EventType.MOUSE_PRESSED);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                sendEvent(e.getX(), e.getY(), e.getButton(), GUIEvent.EventType.MOUSE_RELEASED);
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                sendEvent(e.getX(), e.getY(), e.getScrollAmount(), GUIEvent.EventType.SCROLL);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                sendEvent(e.getX(), e.getY(), e.getButton(), GUIEvent.EventType.MOUSE_DRAGGED);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                sendEvent(e.getX(), e.getY(), GUIEvent.EventType.MOUSE_MOVED);
            }
        });
        windowContent.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Component c = (Component)e.getSource();
                try {
                    client.write(new GUIEvent(c.getWidth()-100, c.getHeight()-100, GUIEvent.EventType.RESIZE).serialize());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    private void sendEvent(int x, int y, GUIEvent.EventType eventType){
        sendEvent(x, y, 0, eventType);
    }
    private void sendEvent(int x, int y, int z, GUIEvent.EventType eventType) {
        try {
            client.write(new GUIEvent(x, y, z, eventType).serialize());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<BufferedImage> bufferedImageFuture;
    public void update() throws ExecutionException, InterruptedException, IOException {
        //BufferedInputStream bufferedInputStream =
        //        new BufferedInputStream(client.socket().getInputStream(), 1024*1024*8);
        Callable<BufferedImage> callable = ()->{

            try {
                // Make changes to the model which need to be painted
                //byte[] header = new byte[12];
                //int l = bufferedInputStream.read(header);//client.socket().getInputStream().readNBytes(10);
                byte[] header = client.socket().getInputStream().readNBytes(12);
                ByteBuffer buff = ByteBuffer.allocate(1024);
                buff.put(header);
                buff.flip();
                if (buff.getShort() == (short)0xDEAD) {
                    width = buff.getShort();
                    height = buff.getShort();
                    int length = buff.getInt(8);
                    //this.setSize(width, height);

                    byte[] body = client.socket().getInputStream().readNBytes(length);//new byte[786432];
                    //l = bufferedInputStream.read(body);
                    //System.out.println(header.length);
                    //System.out.println(body.length);

                    BufferedImage raw_image = ImageUtil.createImage(width, height, BufferedImage.TYPE_3BYTE_BGR);//new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR)
                    raw_image.setData(Raster.createRaster(raw_image.getSampleModel(), new DataBufferByte(body, body.length), new Point() ) );
                    //buff = ByteBuffer.allocate(1024);
                    //buff.put(("ok: " + width + "x" + height + ", data_l: " + body.length + ", length: " +length).getBytes());
                    //System.out.println("ok: " + width + "x" + height + ", data_l: " + body.length+ ", length: " +length);
                    //buff.flip();
                    //client.write(buff);
                    return raw_image;
                }

                //byte[] header = readFile(client, 10);

                //buff.flip();




                //    byte[] body = readFile(client, 786432);//fix

                //    BufferedImage raw_image = ImageUtil.createImage(width, height, BufferedImage.TYPE_3BYTE_BGR);//new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR)
                //    raw_image.setData(Raster.createRaster(raw_image.getSampleModel(), new DataBufferByte(body, body.length), new Point() ) );
                //    buff = ByteBuffer.allocate(1024);
                //    buff.put(("ok: " + width + "x" + height + ", data_l: " + body.length ).getBytes());
                //    System.out.println("ok: " + width + "x" + height + ", data_l: " + body.length);
                //    buff.flip();
                //    client.write(buff);
                //    return raw_image;
                //}

                /*ByteBuffer buff = ByteBuffer.allocate(1024);
                buff.put("next\n\r".getBytes());
                client.configureBlocking(true);
                buff.flip();
                client.write(buff);
                ByteBuffer tmp = ByteBuffer.allocate(0x1FFFF);
                int bytesRead;
                client.configureBlocking(true);
                do {
                    tmp.clear();
                    bytesRead = client.read(tmp);
                    tmp.flip();

                }while (tmp.getShort(0) != (short) 0xDEAD);
                client.configureBlocking(false);

                int length = tmp.getInt(6);
                ByteBuffer res = ByteBuffer.allocate(length);

                tmp.rewind();

                for (int i = 0; i < bytesRead; i++) {
                    byte cur = tmp.get(i);
                    res.put(cur);
                }

                while (res.hasRemaining()){
                    if (client.read(res) == -1){
                        throw new EOFException();
                    }
                }*/

                /*do {
                    // rewind ByteBuffer to get it back to start
                    tmp.rewind();

                    for (int i = 0; i < bytesRead; i++) {
                        byte cur = tmp.get(i);
                        res.put(cur);
                    }


                    tmp = ByteBuffer.allocate(1024 * 1024);
                    ByteBuffer[] buffers = new ByteBuffer[8];
                    for(int i = 0; i < 8; i++){
                        buffers[i] = ByteBuffer.allocate(1024*1024);
                    }

                    bytesRead = (int) client.read(buffers, 0, length-bytesRead);
                    if (bytesRead == -1 || bytesRead == 0) {
                        break;
                    }

                    // reached end of message, break loop
                    //if (bytesRead < 32 * 1024) {
                    //    break;
                    //}
                } while (true);*/

                //res.flip();
                //res.getShort();
                //width = res.getShort();
                //height = res.getShort();
                //int size = res.getInt(6);
                //res.getShort();
                //res.getShort();
                //byte[] data = Arrays.copyOfRange(res.array(), 4, res.array().length);
                //BufferedImage raw_image = ImageUtil.createImage(width, height, BufferedImage.TYPE_3BYTE_BGR);//new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR)
                //raw_image.setData(Raster.createRaster(raw_image.getSampleModel(), new DataBufferByte(data, data.length), new Point() ) );
                //buff = ByteBuffer.allocate(1024);
                //buff.put(("ok: " + width + "x" + height + ", data_l: " + data.length ).getBytes());
                //System.out.println("ok: " + width + "x" + height + ", data_l: " + data.length);
                //buff.flip();
                //client.write(buff);

                return null;

            }catch (IOException e) {}
            return null;
        };
        if(bufferedImageFuture == null){
            bufferedImageFuture = executor.submit(callable);
        }
        if(bufferedImageFuture.isDone()){
            try {
                if(bufferedImageFuture.get() != null){
                    this.image =bufferedImageFuture.get();
                }
            }catch (Exception e){
                System.out.println(e);
                this.bufferedImageFuture = null;
            }finally {
                bufferedImageFuture = executor.submit(callable);
            }
        }
    }

    public static byte[] readFile(SocketChannel channel, Integer length)
            throws IOException {
        ByteBuffer dataBuffer = ByteBuffer.allocate(length < 1024 ? length : 1024*1024);
        int contentLength = 0;
        int size = -1;
        byte[] bytes = null;
        channel.configureBlocking(false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while ((size = channel.read(dataBuffer)) >= 0) {
            contentLength += size;
            dataBuffer.flip();
            bytes = new byte[size];
            dataBuffer.get(bytes);
            byteArrayOutputStream.write(bytes);
            dataBuffer.clear();
            if (contentLength >= length) {
                break;
            }
        }
        //channel.configureBlocking(true);

        byte[] byteArray = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        return byteArray;
    }


    public void render() {
        ToolWindowManager.getInstance(project).invokeLater(()-> {
            BufferStrategy bs = getBufferStrategy();
            if (bs == null) {
                createBufferStrategy(3);
                return;
            }

            do {
                do {
                    Graphics2D g = (Graphics2D) bs.getDrawGraphics();
                    g.clearRect(0, 0, width, height);
                    // You MUST clear the page before painting, bad things
                    // happen otherwise

                    //g.setColor(Color.WHITE);
                    //g.fillRect(0, 0, getWidth(), getHeight());
                    //g.setColor(Color.red);
                    //g.fillRect(10, 50, 50, 70);
                    if(this.image != null) {
                        g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
                    }
                    g.dispose();
                } while (bs.contentsRestored());
                bs.show();
            } while (bs.contentsLost());
        });
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

}