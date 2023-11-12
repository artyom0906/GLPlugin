package org.sqteam.model;

import com.intellij.openapi.wm.ToolWindow;
import lombok.Getter;
import org.sqteam.network.ImageEventTransport;

@Getter
public class VisualDebugSession {
    private final ImageEventTransport transport;
    private final ToolWindow toolWindow;


    public VisualDebugSession(ImageEventTransport transport, ToolWindow toolWindow) {
        this.transport = transport;
        this.toolWindow = toolWindow;
    }
}
