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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CanvasToolWindow {
    private final Project project;
    private JPanel windowContent;
    private JButton selectRendererButton;
    private JPanel canvasPanel;
    private static final Logger log = Logger.getInstance(CanvasToolWindow.class);

    List<Consumer<VirtualFile[]>> onSOChoose = new ArrayList<>();

    public CanvasToolWindow(Project project, ImageEventTransport transport) {

        this.project = project;
        selectRendererButton.addActionListener(e -> {
            VirtualFile[] files = FileChooser.chooseFiles(
                    new FileChooserDescriptor(true, true, false, false, false, true)
                    , project, null);
            if(files.length != 0){
                onSOChoose.forEach(consumer ->consumer.accept(files));
            }
        });
        try {
            //canvasPanel.add(new JLabel("Canvas"), new GridConstraints());
            UDPCanvas udpCanvas = new UDPCanvas(project, canvasPanel, transport);
            canvasPanel.add(udpCanvas, new GridConstraints());
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
