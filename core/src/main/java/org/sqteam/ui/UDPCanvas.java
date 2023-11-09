package org.sqteam.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;
import org.sqteam.network.GUIEvent;
import org.sqteam.network.ImageEventTransport;
import org.sqteam.ui.listeners.InputListener;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serial;

public class UDPCanvas extends Canvas implements Runnable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Thread thread;
    private boolean running = false;

    private static final Logger log = Logger.getInstance(UDPCanvas.class);
    private final ImageEventTransport transport;
        private final InputListener inputListener;

    private BufferedImage image;
   private final Project project;
    private final JPanel windowContent;
    public UDPCanvas(Project project, JPanel windowContent, ImageEventTransport transport){
        this.project = project;
        this.windowContent = windowContent;
        this.transport = transport;
        this.transport.onConnect((t)-> t.sendEvent(new GUIEvent(windowContent.getWidth(), windowContent.getHeight(), GUIEvent.EventType.RESIZE)));
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
            thread.interrupt();
        } catch (Exception e) {
            log.error("error stopping: ", e);
            //e.printStackTrace();
        }
    }

    public void run() {

        registerListeners();

        while (running) {
            try {
                update();
            } catch (Exception e) {
                log.error("error update canvas: ", e);
            }
            render();

            // Control frame rate
            try {
                Thread.sleep(5);
            } catch (InterruptedException ignored) {
            }
        }

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