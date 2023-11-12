package org.sqteam.network;

import com.intellij.util.ui.ImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import com.intellij.openapi.diagnostic.Logger;

public class TCPTransport implements ImageEventTransport {
    ServerSocketChannel serverSocket;
    private SocketChannel client;
    BufferedImage image;
    List<Consumer<ImageEventTransport>> connectFn = new ArrayList<>();

    AtomicBoolean isConnect = new AtomicBoolean(true);

    private final ExecutorService executor;

    private static final Logger log = Logger.getInstance(TCPTransport.class);


    public TCPTransport(int port, ExecutorService executor) throws IOException {
        this.executor = executor;
        serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress("0.0.0.0", port));
    }


    @Override
    public void connect() throws IOException{
        if(isConnect.get()) {
            System.out.println("waiting for connection");
            client = serverSocket.accept();
            System.out.println("connected");
            connectFn.forEach(e -> e.accept(this));
        }
    }
    synchronized SocketChannel getConnection()throws IOException{
        while(client == null || !client.isConnected()) {
            connect();
        }
        return client;
    }
    @Override
    public void onConnect(Consumer<ImageEventTransport> f){
        connectFn.add(f);
    }

    @Override
    public void sendEvent(GUIEvent event) {
        executor.submit(()-> {
            try {
                try{
                    getConnection().write(event.serialize());
                } catch (BufferUnderflowException e) {
                    connect();
                }
            } catch (IOException e) {
                log.error("error sending event: " + event + " -> ", e);
            }
        });
    }

    private BufferedImage receiveImage(){
        while(true){
            try {
                try{
                    var sock = getConnection().socket();
                    byte[] header = sock.getInputStream().readNBytes(12);
                    ByteBuffer buff = ByteBuffer.allocate(1024);
                    buff.put(header);
                    buff.flip();
                    if (buff.getShort() == (short) 0xDEAD) {

                        int t_width = buff.getShort();
                        int t_height = buff.getShort();
                        int length = buff.getInt(8);
                        byte[] body = sock.getInputStream().readNBytes(length);//new byte[786432];

                        BufferedImage raw_image = ImageUtil.createImage(t_width, t_height, BufferedImage.TYPE_3BYTE_BGR);//new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR)
                        raw_image.setData(Raster.createRaster(raw_image.getSampleModel(), new DataBufferByte(body, body.length), new Point()));
                        return raw_image;
                    }

                    return null;

                } catch (BufferUnderflowException e) {
                    connect();
                }
            }
            catch (IOException e) {
                log.info( "error receiving image", e);
                return null;
            }
        }
    }


    Future<BufferedImage> bufferedImageFuture;
    @Override
    public BufferedImage getImage(){
            if(bufferedImageFuture == null){
                bufferedImageFuture = executor.submit(this::receiveImage);
            }
            if(bufferedImageFuture.isDone()){
                try {
                    if(bufferedImageFuture.get() != null){
                        this.image =bufferedImageFuture.get();
                    }
                }catch (Exception e){
                    log.error( "error getting image", e);
                }finally {
                    bufferedImageFuture = executor.submit(this::receiveImage);
                }
            }
        return image;
    }

    @Override
    public void close() throws IOException {
        isConnect.set(false);
        client.close();
        serverSocket.close();
    }
}
