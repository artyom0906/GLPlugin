package org.sqteam.network;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.function.Consumer;

public interface ImageEventTransport {

    void initialize() throws IOException;
    void sendEvent(GUIEvent event);
    public void onConnect(Consumer<ImageEventTransport> fn);
    BufferedImage getImage() throws IOException;
}
