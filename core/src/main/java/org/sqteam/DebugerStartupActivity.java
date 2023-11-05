package org.sqteam;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.wm.*;
import com.intellij.ui.content.ContentFactory;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XDebuggerManagerListener;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class DebugerStartupActivity implements ProjectActivity {
    private void attachDebugStartListener(Project project) {

        project.getMessageBus().connect().subscribe(XDebuggerManager.TOPIC, new XDebuggerManagerListener() {
            Map<XDebugProcess, ToolWindow> toolWindows = new HashMap<>();
            @Override
            public void processStarted(@NotNull XDebugProcess debugProcess) {
                RegisterToolWindowTaskBuilder taskBuilder = new RegisterToolWindowTaskBuilder("debug_visual"+ toolWindows.size());
                Function1<RegisterToolWindowTaskBuilder, Unit> f = registerToolWindowTaskBuilder -> {
                    registerToolWindowTaskBuilder.anchor = ToolWindowAnchor.RIGHT;

                    return null;
                };
                ToolWindowManager.getInstance(project).invokeLater(()-> {
                    ToolWindow toolWindow = ToolWindowManager.getInstance(project).registerToolWindow("debug visual" + (toolWindows.isEmpty() ? "" : toolWindows.size()) , f);
                    CanvasToolWindow myToolWindow = new CanvasToolWindow(toolWindow, project);
                    ContentFactory contentFactory = ContentFactory.getInstance();
                    toolWindow.getContentManager().addContent(contentFactory.createContent(myToolWindow.getContent(), "", false));
                    toolWindow.activate(() -> {});
                    toolWindows.put(debugProcess, toolWindow);
                });
            }

            @Override
            public void processStopped(@NotNull XDebugProcess debugProcess) {
                ToolWindowManager.getInstance(project).invokeLater(()->{
                    if(toolWindows.containsKey(debugProcess)) {
                        if (toolWindows.get(debugProcess).getContentManager().getContent(0).getComponent() instanceof JPanel jPanel){
                            if(jPanel.getComponent(0) instanceof UDPCanvas udpCanvas)
                                udpCanvas.stop();
                        }
                        toolWindows.get(debugProcess).remove();
                    }
                });
            }
        });
    }

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        attachDebugStartListener(project);
        return null;
    }
}
