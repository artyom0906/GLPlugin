package org.sqteam.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.uiDesigner.core.GridConstraints;
import org.sqteam.network.ImageEventTransport;

import javax.swing.*;

public class CanvasToolWindow {
    private final Project project;
    private JPanel windowContent;
    private static final Logger log = Logger.getInstance(CanvasToolWindow.class);
    private final ImageEventTransport transport;

    public CanvasToolWindow(Project project, ImageEventTransport transport) {

        this.project = project;
        this.transport = transport;
    }

    public void setWindowContent(){
        try {
            UDPCanvas udpCanvas = new UDPCanvas(project, windowContent, transport);
            windowContent.add(udpCanvas, new GridConstraints());
            udpCanvas.start();
        }catch (Exception e) {
            log.error("CanvasToolWindow", e);
        }
    }

    public JPanel getContent(){
        return windowContent;
    }
}
