package org.sqteam.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.uiDesigner.core.GridConstraints;
import org.sqteam.network.TCPTransport;

import javax.swing.*;

public class CanvasToolWindow {
    private final Project project;
    private JPanel windowContent;
    private static final Logger log = Logger.getInstance(CanvasToolWindow.class);

    public CanvasToolWindow(Project project) {

        this.project = project;
    }

    public void setWindowContent(){
        try {
            UDPCanvas udpCanvas = new UDPCanvas(project, windowContent, new TCPTransport());
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
