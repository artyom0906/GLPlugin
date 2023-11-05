package org.sqteam;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;

import javax.swing.*;
import java.awt.*;
import java.util.UUID;

public class CanvasToolWindow {
    private final JPanel windowContent;

    public CanvasToolWindow(ToolWindow toolWindow, Project project) {

        windowContent = new JPanel(new FlowLayout(FlowLayout.LEFT));
        try {
            UDPCanvas udpCanvas = new UDPCanvas(project, windowContent);
            windowContent.add(udpCanvas);
            udpCanvas.start();
        }catch (Exception ignored) {}

        windowContent.add(new JLabel("Hello: " + UUID.randomUUID()));

    }



    public JPanel getContent(){
        return windowContent;
    }
}
