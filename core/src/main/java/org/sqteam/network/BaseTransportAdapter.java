package org.sqteam.network;

public class BaseTransportAdapter {
    private final ImageEventTransport transport;

    public BaseTransportAdapter(ImageEventTransport transport) {
        this.transport = transport;
    }
    protected void sendEvent(int x, int y, GUIEvent.EventType eventType){
        sendEvent(x, y, 0, eventType);
    }
    protected void sendEvent(int x, int y, int z, GUIEvent.EventType eventType) {
        transport.sendEvent(new GUIEvent(x, y, z, eventType));
    }
    protected void sendEvent(double z, GUIEvent.EventType eventType) {
        transport.sendEvent(new GUIEvent(z, eventType));
    }
}
