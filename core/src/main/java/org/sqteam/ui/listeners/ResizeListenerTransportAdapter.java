package org.sqteam.ui.listeners;

import org.sqteam.network.BaseTransportAdapter;
import org.sqteam.network.GUIEvent;
import org.sqteam.network.ImageEventTransport;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

public class ResizeListenerTransportAdapter extends BaseTransportAdapter implements ComponentListener {
    public ResizeListenerTransportAdapter(ImageEventTransport transport) {
        super(transport);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        if(e.getComponent().getWidth() > 0 && e.getComponent().getHeight() > 100)
            sendEvent(e.getComponent().getWidth(), e.getComponent().getHeight()-100, GUIEvent.EventType.RESIZE);
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {
        if(e.getComponent().getWidth() > 0 && e.getComponent().getHeight() > 100)
            sendEvent(e.getComponent().getWidth(), e.getComponent().getHeight()-100, GUIEvent.EventType.RESIZE);
    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }
}
