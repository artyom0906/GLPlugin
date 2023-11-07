package org.sqteam.network;

import com.intellij.openapi.diagnostic.Logger;
import org.sqteam.network.GUIEvent;
import org.sqteam.network.ImageEventTransport;
import org.sqteam.network.TCPTransport;

import java.io.IOException;

public class BaseTransportAdapter {
    private final ImageEventTransport transport;
    private static final Logger log = Logger.getInstance(TCPTransport.class);

    public BaseTransportAdapter(ImageEventTransport transport) {
        this.transport = transport;
    }
    protected void sendEvent(int x, int y, GUIEvent.EventType eventType){
        sendEvent(x, y, 0, eventType);
    }
    protected void sendEvent(int x, int y, int z, GUIEvent.EventType eventType) {
        transport.sendEvent(new GUIEvent(x, y, z, eventType));
    }
    private void sendEvent(double z, GUIEvent.EventType eventType) {
        transport.sendEvent(new GUIEvent(z, eventType));
    }
}
