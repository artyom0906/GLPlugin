package org.sqteam.service;

import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.ContentFactory;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSessionListener;
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl;
import org.sqteam.debug.VisualEvaluator;
import org.sqteam.model.VisualDebugSession;
import org.sqteam.network.ImageEventTransport;
import org.sqteam.network.TCPTransport;
import org.sqteam.ui.CanvasToolWindow;
import org.sqteam.ui.UDPCanvas;

import javax.swing.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VisualDebugSessionMangerImpl implements VisualDebugSessionManger {

    Map<XDebugProcess, VisualDebugSession> sessionMap = new HashMap<XDebugProcess, VisualDebugSession>();
    private final Project project;

    private final ExecutorService debugerExecutorService = Executors.newSingleThreadExecutor();
    private final ExecutorService renderExecutorService = Executors.newSingleThreadExecutor();

    public VisualDebugSessionMangerImpl(Project project) {
        this.project = project;
    }

    @Override
    public void startDebug(XDebugProcess process) {

        int port = 7576+sessionMap.size();

        ImageEventTransport transport = null;

        try {
            transport = new TCPTransport(port, renderExecutorService);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ImageEventTransport finalTransport = transport;
        process.getSession().addSessionListener(new XDebugSessionListener(){
            boolean visualizerStarted = false;
            @Override
            public void sessionPaused() {
                if (!visualizerStarted) {
                    VisualEvaluator e = new VisualEvaluator(process, debugerExecutorService);
                    e.onError(System.err::println);
                    e.addNext(new XExpressionImpl("((void*(*)(const char*, int))dlopen)(\"./build/libtest.so\", 1)", Language.ANY, ""));
                    e.addNext(new XExpressionImpl("(void)'plugin::open'( \"172.21.240.1\", "+ port +")", Language.ANY, ""));
                    e.run();
                    visualizerStarted = true;
                            /*Objects.requireNonNull(debugProcess.getEvaluator()).evaluate("((void*(*)(const char*, int))dlopen)(\"./libtest.so\", 1)", new XDebuggerEvaluator.XEvaluationCallback() {
                                @Override
                                public void evaluated(@NotNull XValue result) {
                                    XValueNode node = new XValueNode() {
                                        @Override
                                        public void setPresentation(@Nullable Icon icon, @NonNls @Nullable String type, @NonNls @NotNull String value, boolean hasChildren) {
                                            System.out.println("type=" + type + " value=" + value);
                                        }
                                        //XTestValueNode

                                        @Override
                                        public void setPresentation(@Nullable Icon icon, @NotNull XValuePresentation presentation, boolean hasChildren) {
                                            presentation.renderValue(new XValuePresentation.XValueTextRenderer() {
                                                @Override
                                                public void renderValue(@NotNull @NlsSafe String value) {
                                                    System.out.println("type=" + presentation.getType() + " value=" + value + " hasChildren=" + hasChildren);
                                                }

                                                @Override
                                                public void renderStringValue(@NotNull @NlsSafe String value) {

                                                }

                                                @Override
                                                public void renderNumericValue(@NotNull @NlsSafe String value) {

                                                }

                                                @Override
                                                public void renderKeywordValue(@NotNull @NlsSafe String value) {

                                                }

                                                @Override
                                                public void renderValue(@NotNull @NlsSafe String value, @NotNull TextAttributesKey key) {
                                                    System.out.println("type=" + presentation.getType() + " value=" + value + " hasChildren=" + hasChildren);
                                                }

                                                @Override
                                                public void renderStringValue(@NotNull @NlsSafe String value, @Nullable @NlsSafe String additionalSpecialCharsToHighlight, int maxLength) {
                                                    System.out.println("type=" + presentation.getType() + " value=" + value + " hasChildren=" + hasChildren);
                                                }

                                                @Override
                                                public void renderComment(@NotNull @NlsSafe String comment) {

                                                }

                                                @Override
                                                public void renderSpecialSymbol(@NotNull @NlsSafe String symbol) {

                                                }

                                                @Override
                                                public void renderError(@NotNull @NlsSafe String error) {

                                                }
                                            });

                                        }

                                        @Override
                                        public void setFullValueEvaluator(@NotNull XFullValueEvaluator fullValueEvaluator) {

                                        }
                                    };
                                    result.computePresentation(node,  XValuePlace.TOOLTIP);

                                    result.computeChildren(new XCompositeNode() {
                                        @Override
                                        public void addChildren(@NotNull XValueChildrenList children, boolean last) {
                                            System.err.println("result: " + result + " data: " + children.size());
                                            for (int i = 0; i < children.size(); i++) {
                                                System.out.println("name: " + children.getName(i) + " value: " + children.getValue(i));
                                            }
                                            visualizerStarted[0] = true;
                                        }

                                        @Override
                                        public void tooManyChildren(int remaining) {

                                        }

                                        @Override
                                        public void setAlreadySorted(boolean alreadySorted) {

                                        }

                                        @Override
                                        public void setErrorMessage(@NotNull String errorMessage) {

                                        }

                                        @Override
                                        public void setErrorMessage(@NotNull String errorMessage, @Nullable XDebuggerTreeNodeHyperlink link) {

                                        }

                                        @Override
                                        public void setMessage(@NotNull String message, @Nullable Icon icon, @NotNull SimpleTextAttributes attributes, @Nullable XDebuggerTreeNodeHyperlink link) {

                                        }
                                    });

                                }

                                @Override
                                public void errorOccurred(@NotNull @NlsContexts.DialogMessage String errorMessage) {
                                    System.err.println("error:" + errorMessage);
                                }
                            }, debugProcess.getSession().getCurrentPosition());*/
                }
            }
        });

        ToolWindowManager.getInstance(project).invokeLater(() -> {
            ToolWindow toolWindow = ToolWindowManager.getInstance(project).registerToolWindow("debug visual" + (sessionMap.isEmpty() ? "" : sessionMap.size()),
                    registerToolWindowTaskBuilder -> {
                        registerToolWindowTaskBuilder.anchor = ToolWindowAnchor.RIGHT;
                        return null;
                    }
            );
            CanvasToolWindow myToolWindow = new CanvasToolWindow(project, finalTransport);
            myToolWindow.setWindowContent();
            ContentFactory contentFactory = ContentFactory.getInstance();
            toolWindow.getContentManager().addContent(contentFactory.createContent(myToolWindow.getContent(), "", false));
            toolWindow.activate(() -> {
            });
            sessionMap.put(process, new VisualDebugSession(finalTransport, toolWindow));
        });
    }

    @Override
    public void endDebug(XDebugProcess process) {
        ToolWindowManager.getInstance(project).invokeLater(()->{
            if(sessionMap.containsKey(process)) {
                if (Objects.requireNonNull(sessionMap.get(process).getToolWindow().getContentManager().getContent(0)).getComponent() instanceof JPanel jPanel){
                    if(jPanel.getComponent(0) instanceof UDPCanvas udpCanvas)
                        udpCanvas.stop();
                }
                try {
                    sessionMap.get(process).getTransport().close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                sessionMap.get(process).getToolWindow().remove();
                sessionMap.remove(process);
            }
        });
    }
}
