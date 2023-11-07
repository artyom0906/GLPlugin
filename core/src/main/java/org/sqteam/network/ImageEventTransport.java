package org.sqteam.network;

import java.awt.image.BufferedImage;
import java.io.IOException;

public interface ImageEventTransport {

    void initialize() throws IOException;
    void sendEvent(GUIEvent event);
    BufferedImage getImage() throws IOException;
}
