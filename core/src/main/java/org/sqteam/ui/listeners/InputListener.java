package org.sqteam.ui.listeners;

import org.sqteam.network.GUIEvent;
import org.sqteam.network.ImageEventTransport;

import java.awt.*;
import java.awt.event.*;

public class InputListener {
    private final ImageEventTransport transport;
    private final MouseListenerTransportAdapter mouseListenerTransportAdapter;
    private final KeyBoardListenerTransportAdapter keyBoardListenerTransportAdapter;
    private final ResizeListenerTransportAdapter resizeListenerTransportAdapter;

    public InputListener(ImageEventTransport transport){
        this.transport = transport;
        mouseListenerTransportAdapter = new MouseListenerTransportAdapter(transport);
        keyBoardListenerTransportAdapter = new KeyBoardListenerTransportAdapter(transport);
        resizeListenerTransportAdapter = new ResizeListenerTransportAdapter(transport);
    }

    public void registerKeyboardMouseListeners(Component component) {

        component.addMouseMotionListener(mouseListenerTransportAdapter);
        component.addMouseListener(mouseListenerTransportAdapter);
        component.addKeyListener(keyBoardListenerTransportAdapter);
        component.addMouseWheelListener(mouseListenerTransportAdapter);
    }
    public void registerResizeListener(Component component) {
        component.addComponentListener(resizeListenerTransportAdapter);
        //new ComponentAdapter() {
        //
        //            @Override
        //            public void componentShown(ComponentEvent e) {
        //                Component c = (Component)e.getSource();
        //                try {
        //                    sendEvent(c.getWidth(), c.getHeight()-100, GUIEvent.EventType.RESIZE);
        //                } catch (Exception ex) {
        //                    throw new RuntimeException(ex);
        //                }
        //            }
        //
        //            @Override
        //            public void componentResized(ComponentEvent e) {
        //                Component c = (Component)e.getSource();
        //                try {
        //                    sendEvent(c.getWidth(), c.getHeight()-100, GUIEvent.EventType.RESIZE);
        //                    width = c.getWidth();
        //                    height = c.getHeight()-100;
        //                } catch (Exception ex) {
        //                    throw new RuntimeException(ex);
        //                }
        //            }
        //        }
    }
}
