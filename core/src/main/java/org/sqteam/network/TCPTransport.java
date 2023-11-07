package org.sqteam.network;

import com.intellij.util.ui.ImageUtil;
import lombok.extern.log4j.Log4j2;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import com.intellij.openapi.diagnostic.Logger;

public class TCPTransport implements ImageEventTransport {
    ServerSocketChannel serverSocket;
    private SocketChannel client;
    BufferedImage image;

    private static final Logger log = Logger.getInstance(TCPTransport.class);


    public TCPTransport() throws IOException {
        serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress("0.0.0.0", 7576));
    }


    @Override
    public void initialize() throws IOException {
        System.out.println("waiting for connection");
        client = serverSocket.accept();
        System.out.println("connected");
    }

    @Override
    public void sendEvent(GUIEvent event) {
        try {
            if (client != null && client.isConnected()) {
                client.write(event.serialize());
            }
        }catch (IOException e){
            log.error("error sending event: " + event + " -> ", e);
        }
    }

    private BufferedImage receiveImage(){
        try {
            byte[] header = client.socket().getInputStream().readNBytes(12);
            ByteBuffer buff = ByteBuffer.allocate(1024);
            buff.put(header);
            buff.flip();
            if (buff.getShort() == (short)0xDEAD) {

                int t_width = buff.getShort();
                int t_height = buff.getShort();
                int length = buff.getInt(8);
                byte[] body = client.socket().getInputStream().readNBytes(length);//new byte[786432];

                //windowContent.resize(width, height);
                BufferedImage raw_image = ImageUtil.createImage(t_width, t_height, BufferedImage.TYPE_3BYTE_BGR);//new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR)
                raw_image.setData(Raster.createRaster(raw_image.getSampleModel(), new DataBufferByte(body, body.length), new Point() ) );
                return raw_image;
            }

            return null;

        }catch (IOException e) {
            log.error( "error receiving image", e);
            //e.printStackTrace();
            // init();
        }
        return null;
    }

    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<BufferedImage> bufferedImageFuture;
    @Override
    public BufferedImage getImage() throws IOException {
            serverSocket.configureBlocking(false);
            SocketChannel clientTmp = serverSocket.accept();
            if(clientTmp != null) {
                client.close();
                client = clientTmp;
            }
            if(bufferedImageFuture == null){
                bufferedImageFuture = executor.submit(this::receiveImage);
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
                    bufferedImageFuture = executor.submit(this::receiveImage);
                }
            }
        return image;
    }
}
