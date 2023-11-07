package org.sqteam.ui.listeners;

import org.sqteam.network.BaseTransportAdapter;
import org.sqteam.network.GUIEvent;
import org.sqteam.network.ImageEventTransport;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyBoardListenerTransportAdapter extends BaseTransportAdapter implements KeyListener  {
    public KeyBoardListenerTransportAdapter(ImageEventTransport transport) {
        super(transport);
    }

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
}