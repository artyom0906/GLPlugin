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
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

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
            registerListeners();
            serverSocket = ServerSocketChannel.open();
            serverSocket.socket().bind(new InetSocketAddress("0.0.0.0", 7576));
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
        System.out.println("waiting for connection");
        client = serverSocket.accept();
        System.out.println("connected");
        sendEvent(width, height-100, GUIEvent.EventType.RESIZE);
        //sendEvent(width-100, height-100, GUIEvent.EventType.RESIZE);
    }

    private void registerListeners() {

        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                sendEvent(e.getX(), e.getY(), e.getButton(), GUIEvent.EventType.MOUSE_DRAGGED);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                sendEvent(e.getX(), e.getY(), 0, GUIEvent.EventType.MOUSE_MOVED);
            }
        });
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

        this.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                sendEvent(e.getKeyCode(), 0, 0, GUIEvent.EventType.KEY_TYPED);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                sendEvent(e.getKeyCode(), 0, 0, GUIEvent.EventType.KEY_PRESSED);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                sendEvent(e.getKeyCode(), 0, 0, GUIEvent.EventType.KEY_RELEASED);
            }
        });

        this.addMouseWheelListener(e -> sendEvent(e.getX(), e.getY(), e.getScrollAmount(), GUIEvent.EventType.SCROLL));
        windowContent.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent e) {
                Component c = (Component)e.getSource();
                try {
                    sendEvent(c.getWidth(), c.getHeight()-100, GUIEvent.EventType.RESIZE);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public void componentResized(ComponentEvent e) {
                Component c = (Component)e.getSource();
                try {
                    sendEvent(c.getWidth(), c.getHeight()-100, GUIEvent.EventType.RESIZE);
                    width = c.getWidth();
                    height = c.getHeight()-100;
                } catch (Exception ex) {
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
            if (client != null)
                client.write(new GUIEvent(x, y, z, eventType).serialize());
        } catch (IOException ex) {
            //throw new RuntimeException(ex);
        }
    }

    ExecutorService executor = Executors.newSingleThreadExecutor();
    AtomicBoolean repaint = new AtomicBoolean(false);
    Future<BufferedImage> bufferedImageFuture;
    public void update() throws ExecutionException, InterruptedException, IOException {
        serverSocket.configureBlocking(false);
        SocketChannel clientTmp = serverSocket.accept();
        if(clientTmp != null) {
            client.close();
            client = clientTmp;
        }
        Callable<BufferedImage> callable = ()->{

            try {
                byte[] header = client.socket().getInputStream().readNBytes(12);
                ByteBuffer buff = ByteBuffer.allocate(1024);
                buff.put(header);
                buff.flip();
                if (buff.getShort() == (short)0xDEAD) {

                    int t_width = buff.getShort();
                    int t_height = buff.getShort();
                    int length = buff.getInt(8);
                    //if(t_width != width || t_height != height){
                    //    width = t_width;
                    //    height = t_height;
                    //    repaint.set(true);
                    //}

                    byte[] body = client.socket().getInputStream().readNBytes(length);//new byte[786432];

                    //windowContent.resize(width, height);
                    BufferedImage raw_image = ImageUtil.createImage(t_width, t_height, BufferedImage.TYPE_3BYTE_BGR);//new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR)
                    raw_image.setData(Raster.createRaster(raw_image.getSampleModel(), new DataBufferByte(body, body.length), new Point() ) );
                    return raw_image;
                }

                return null;

            }catch (IOException e) {
               // init();
            }
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
                System.err.println(e);
                //init();
            }finally {
                bufferedImageFuture = executor.submit(callable);
            }
        }
    }

    public void render() {
        ToolWindowManager.getInstance(project).invokeLater(()-> {

            if(repaint.get()){
                this.repaint();
                windowContent.repaint();
                repaint.set(false);
            }
            BufferStrategy bs = getBufferStrategy();
            if (bs == null) {
                createBufferStrategy(3);
                return;
            }

            do {
                do {
                    Graphics2D g = (Graphics2D) bs.getDrawGraphics();
                    g.clearRect(0, 0, windowContent.getWidth(), windowContent.getHeight());
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
}