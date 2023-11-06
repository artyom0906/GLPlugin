package org.sqteam;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.uiDesigner.core.GridConstraints;
import org.sqteam.networkl.GUIEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.UUID;

public class CanvasToolWindow {
    private final ToolWindow toolWindow;
    private final Project project;
    private JPanel windowContent;

    public CanvasToolWindow(ToolWindow toolWindow, Project project) {

        //windowContent = new JPanel();

        this.toolWindow = toolWindow;
        this.project = project;
    }

    public void setWindowContent(){
        try {
            UDPCanvas udpCanvas = new UDPCanvas(project, windowContent);
            windowContent.add(udpCanvas, new GridConstraints());
            udpCanvas.start();
        }catch (Exception ignored) {
            throw new RuntimeException(ignored);
        }
    }



    public JPanel getContent(){
        return windowContent;
    }
}
