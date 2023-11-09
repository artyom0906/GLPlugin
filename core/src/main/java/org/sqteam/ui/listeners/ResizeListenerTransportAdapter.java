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
        sendEvent(e.getComponent().getWidth(), e.getComponent().getHeight(), GUIEvent.EventType.RESIZE);
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {
        sendEvent(e.getComponent().getWidth(), e.getComponent().getHeight(), GUIEvent.EventType.RESIZE);
    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }
}
