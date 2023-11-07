package org.sqteam.ui.listeners;

import org.sqteam.network.BaseTransportAdapter;
import org.sqteam.network.GUIEvent;
import org.sqteam.network.ImageEventTransport;

import java.awt.event.*;

public class MouseListenerTransportAdapter extends BaseTransportAdapter implements java.awt.event.MouseListener, MouseWheelListener, MouseMotionListener {
    public MouseListenerTransportAdapter(ImageEventTransport transport) {
        super(transport);
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
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
