package org.sqteam;

import com.intellij.lang.Language;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.wm.*;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.content.ContentFactory;
import com.intellij.xdebugger.*;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.*;
import com.intellij.xdebugger.frame.presentation.XValuePresentation;
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl;
import com.jetbrains.cidr.cpp.execution.CMakeAppRunConfiguration;
import com.jetbrains.cidr.cpp.toolchains.CPPToolSet;
import com.jetbrains.cidr.cpp.toolchains.CPPToolchains;
import com.jetbrains.cidr.lang.workspace.OCWorkspace;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sqteam.debug.VisualEvaluator;
import org.sqteam.service.VisualDebugSessionManger;
import org.sqteam.ui.CanvasToolWindow;
import org.sqteam.ui.UDPCanvas;

import javax.swing.*;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class DebugerStartupActivity implements ProjectActivity {
    private void attachDebugStartListener(Project project) {

        project.getMessageBus().connect().subscribe(XDebuggerManager.TOPIC, new XDebuggerManagerListener() {
            Map<XDebugProcess, ToolWindow> toolWindows = new HashMap<>();


            @Override
            public void currentSessionChanged(@Nullable XDebugSession previousSession, @Nullable XDebugSession currentSession) {
                XDebuggerManagerListener.super.currentSessionChanged(previousSession, currentSession);
            }

            @Override
            public void processStarted(@NotNull XDebugProcess debugProcess) {

                //var a = CMakeAppRunConfiguration.getSelectedRunConfiguration(project);
                var a = CPPToolchains.getInstance().getDefaultToolchain();
                assert a != null;
                switch (a.getToolSetKind()){
                    case SYSTEM_UNIX_TOOLSET -> {}
                    case SYSTEM_WINDOWS_TOOLSET -> {}
                    case WSL -> {}
                    case SSH -> {}
                    case DOCKER -> {}
                    case MSVC -> {}
                    case MINGW -> {}
                    case CYGWIN -> {}
                    default -> {}
                }

                var configs = OCWorkspace.getInstance(project).getConfigurations();

                RegisterToolWindowTaskBuilder taskBuilder = new RegisterToolWindowTaskBuilder("debug_visual"+ toolWindows.size());
                //Function1<RegisterToolWindowTaskBuilder, Unit> f =

                final boolean[] visualizerStarted = {false};

                try {
                    Stream<NetworkInterface> interfaceList = Collections.list(NetworkInterface.getNetworkInterfaces()).stream();
                    interfaceList.filter(networkInterface -> {
                        try {
                            return networkInterface.isUp();
                        } catch (SocketException e) {
                            throw new RuntimeException(e);
                        }
                    })
                            .peek(System.err::println)
                            .forEach(i-> System.err.println(i.getInterfaceAddresses()));
                } catch (SocketException e) {
                    throw new RuntimeException(e);
                }
                //Evalator e = new Evalator();
                //e.addOnError(...)
                //e.evaluate("((void*(*)(const char*, int))dlopen)(\"./libtest.so\", 1)");
                //e.evaluate("...")
                //e.evaluate("...");
                //e.wait();

                project.getService(VisualDebugSessionManger.class).startDebug(debugProcess);
            }

            @Override
            public void processStopped(@NotNull XDebugProcess debugProcess) {
                project.getService(VisualDebugSessionManger.class).endDebug(debugProcess);
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
