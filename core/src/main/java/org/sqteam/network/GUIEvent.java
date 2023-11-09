package org.sqteam.network;

import lombok.Getter;

import java.nio.ByteBuffer;

public class GUIEvent {



    public GUIEvent(int x, int y, EventType eventType) {
        this.x = x;
        this.y = y;
        this.z = 0;
        this.eventType = eventType;
    }

    public GUIEvent(int x, int y, int z, EventType eventType) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.eventType = eventType;
    }
    public GUIEvent(double z, EventType eventType) {
        this(0, 0, 0, eventType);
        this.dz = z;
    }

    public ByteBuffer serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);//1+4+4
        if(dz!=null) {
            buffer.putDouble(dz);
            buffer.putInt(0);
        }else {
            buffer.putInt(x);
            buffer.putInt(y);
            buffer.putInt(z);
        }
        buffer.putInt(eventType.id);
        if(eventType!=EventType.MOUSE_MOVED && eventType!=EventType.SCROLL)
            System.out.println(("{id:" +eventType.name()+", x:"+x+", y:"+y+", z:"+z+"}\n\r"));
        if(eventType==EventType.SCROLL)
            System.out.println("{id:" +eventType.name()+", x:"+dz+"}\n\r");
        buffer.flip();
        return buffer;
    }

    @Getter
    public enum EventType {
        RESIZE(0),
        MOUSE_CLICK(1),
        MOUSE_PRESSED(2),
        MOUSE_RELEASED(3),
        MOUSE_WHEEL(4),
        SCROLL(5),
        MOUSE_DRAGGED(6),
        MOUSE_MOVED(7),
        KEY_TYPED(8),
        KEY_PRESSED(9),
        KEY_RELEASED(10);
        private final int id;
        EventType(int id){this.id = id;}

    }

    private final int x;
    private final int y;
    private final int z;
    private Double dz = null;
    private final EventType eventType;





}
