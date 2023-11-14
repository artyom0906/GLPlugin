package org.sqteam.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.uiDesigner.core.GridConstraints;
import org.sqteam.network.ImageEventTransport;
import org.sqteam.service.VisualDebugSessionManger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CanvasToolWindow {
    private final Project project;
    private JPanel windowContent;

    private static final Logger log = Logger.getInstance(CanvasToolWindow.class);

    List<Consumer<VirtualFile[]>> onSOChoose = new ArrayList<>();

    public CanvasToolWindow(Project project, ImageEventTransport transport) {

        this.project = project;
       JButton selectRendererButton = new JButton("Select Renderer");


        selectRendererButton.addActionListener(e -> {
            VirtualFile[] files = FileChooser.chooseFiles(
                    new FileChooserDescriptor(true, true, false, false, false, true)
                    , project, null);
            if(files.length != 0){
                onSOChoose.forEach(consumer ->consumer.accept(files));
            }
        });

        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.fill = GridBagConstraints.HORIZONTAL;
        buttonConstraints.gridx = 0;
        buttonConstraints.gridy = 0;
        buttonConstraints.gridwidth = 2; // Make the button span both columns
        windowContent.add(selectRendererButton, buttonConstraints);

        // Create panel that takes all free space
        JPanel canvasPanel = new JPanel();
       //freeSpacePanel.setBackground(Color.GREEN); // Just for visualization
        GridBagConstraints freeSpaceConstraints = new GridBagConstraints();
        freeSpaceConstraints.fill = GridBagConstraints.BOTH;
        freeSpaceConstraints.gridx = 0;
        freeSpaceConstraints.gridy = 1;
        freeSpaceConstraints.weightx = 1.0; // Make the panel take all available horizontal space
        freeSpaceConstraints.weighty = 1.0; // Make the panel take all available vertical space
        windowContent.add(canvasPanel, freeSpaceConstraints);
        try {

            UDPCanvas udpCanvas = new UDPCanvas(project, canvasPanel, transport);
            canvasPanel.add(udpCanvas);
            //canvasPanel.setLayout(new BoxLayout(canvasPanel, BoxLayout.Y_AXIS));
            //windowContent.setLayout(new BoxLayout(canvasPanel, BoxLayout.Y_AXIS));
            //windowContent.add(panel);
            udpCanvas.start();

        } catch (Exception e) {
            log.error("CanvasToolWindow", e);
        }
    }

    public void addOnRendererChoose(Consumer<VirtualFile[]> onChoose){
        this.onSOChoose.add(onChoose);
    }

    public JPanel getContent() {
        return windowContent;
    }
}
