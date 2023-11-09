package org.sqteam.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.util.ui.ImageUtil;
import org.sqteam.network.GUIEvent;
import org.sqteam.network.ImageEventTransport;
import org.sqteam.network.TCPTransport;
import org.sqteam.ui.listeners.InputListener;

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

    private Thread thread;
    private boolean running = false;
    private final ImageEventTransport transport;
        private final InputListener inputListener;

    private BufferedImage image;
   private final Project project;
    private final JPanel windowContent;
    public UDPCanvas(Project project, JPanel windowContent, ImageEventTransport transport) throws IOException {
        this.project = project;
        this.windowContent = windowContent;
        this.transport = transport;
        this.transport.onConnect((t)->{
            t.sendEvent(new GUIEvent(windowContent.getWidth(), windowContent.getHeight(), GUIEvent.EventType.RESIZE));
        });
        inputListener = new InputListener(transport);
    }

    @Override
    public Dimension getPreferredSize() {
        return this.windowContent.getSize();
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
            //serverSocket.close();
            //client.close();
        } catch (Exception e) {
            e.printStackTrace();
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
            } catch (InterruptedException ignored) {
            }
        }

    }

    private void init() throws IOException {

        transport.initialize();
        //transport.sendEvent(new GUIEvent(width, height-100, GUIEvent.EventType.RESIZE));
        //sendEvent(width-100, height-100, GUIEvent.EventType.RESIZE);
    }

    private void registerListeners() {
        inputListener.registerKeyboardMouseListeners(this);
        inputListener.registerResizeListener(windowContent);
    }



    public void update(){
        try {
            var img = transport.getImage();
            if(img != null) {
                this.image = img;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void render() {
        ToolWindowManager.getInstance(project).invokeLater(()-> {

            //if(repaint.get()){
            //    this.repaint();
            //    windowContent.repaint();
            //    repaint.set(false);
            //}
            BufferStrategy bs = getBufferStrategy();
            if (bs == null) {
                createBufferStrategy(3);
                return;
            }

            do {
                do {
                    Graphics2D g = (Graphics2D) bs.getDrawGraphics();
                    //g.clearRect(0, 0, windowContent.getWidth(), windowContent.getHeight());
                    // You MUST clear the page before painting, bad things
                    // happen otherwise

                    //g.setColor(Color.WHITE);
                    //g.fillRect(0, 0, getWidth(), getHeight());
                    //g.setColor(Color.red);
                    //g.fillRect(10, 50, 50, 70);
                    if(this.image != null) {
                        g.drawImage(image,
                                (windowContent.getWidth() - image.getWidth()) / 2,
                                (windowContent.getHeight() - image.getHeight()) / 2,
                                image.getWidth(),
                                image.getHeight(),
                                null);
                    }
                    g.dispose();
                } while (bs.contentsRestored());
                bs.show();
            } while (bs.contentsLost());
        });
    }
}