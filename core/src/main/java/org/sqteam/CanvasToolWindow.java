package org.sqteam;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import org.sqteam.networkl.GUIEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
        windowContent.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent e) {
                System.out.println(e);
            }

            @Override
            public void componentResized(ComponentEvent e) {
                System.out.println(e);
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                System.out.println(e);
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                System.out.println(e);
            }
        });

        windowContent.add(new JLabel("Hello: " + UUID.randomUUID()));

    }



    public JPanel getContent(){
        return windowContent;
    }
}
