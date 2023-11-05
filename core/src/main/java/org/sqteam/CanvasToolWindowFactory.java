package org.sqteam;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.ContentFactory;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XDebuggerManagerListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CanvasToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        CanvasToolWindow myToolWindow = new CanvasToolWindow(toolWindow, project);
        ContentFactory contentFactory = ContentFactory.getInstance();
    project.getMessageBus().connect().subscribe(XDebuggerManager.TOPIC, new XDebuggerManagerListener(){
        @Override
        public void processStarted(@NotNull XDebugProcess debugProcess) {
            //XDebuggerManagerListener.super.processStarted(debugProcess);

        }

        @Override
        public void processStopped(@NotNull XDebugProcess debugProcess) {
            XDebuggerManagerListener.super.processStopped(debugProcess);
        }

        @Override
        public void currentSessionChanged(@Nullable XDebugSession previousSession, @Nullable XDebugSession currentSession) {
            XDebuggerManagerListener.super.currentSessionChanged(previousSession, currentSession);
        }
    });
    }
}
