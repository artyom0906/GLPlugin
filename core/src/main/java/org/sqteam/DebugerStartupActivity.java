package org.sqteam;

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
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sqteam.ui.CanvasToolWindow;
import org.sqteam.ui.UDPCanvas;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
                RegisterToolWindowTaskBuilder taskBuilder = new RegisterToolWindowTaskBuilder("debug_visual"+ toolWindows.size());
                Function1<RegisterToolWindowTaskBuilder, Unit> f = registerToolWindowTaskBuilder -> {
                    registerToolWindowTaskBuilder.anchor = ToolWindowAnchor.RIGHT;

                    return null;
                };

                final boolean[] visualizerStarted = {false};

                debugProcess.getSession().addSessionListener(new XDebugSessionListener(){
                    @Override
                    public void sessionPaused() {
                        if (!visualizerStarted[0]) {
                            Objects.requireNonNull(debugProcess.getEvaluator()).evaluate("((void*(*)(const char*, int))dlopen)(\"./libtest.so\", 1)", new XDebuggerEvaluator.XEvaluationCallback() {
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
                            }, debugProcess.getSession().getCurrentPosition());
                        }
                        Objects.requireNonNull(debugProcess.getSession().getCurrentStackFrame()).computeChildren(new XCompositeNode() {
                            @Override
                            public void addChildren(@NotNull XValueChildrenList children, boolean last) {
                                //System.err.println("result: " + result + " data: " + children.size());
                                for (int i = 0; i < children.size(); i++) {
                                    System.out.println("name: " + children.getName(i) + " value: " + children.getValue(i));
                                    if(!children.getName(i).equals("Registers"))
                                        Objects.requireNonNull(debugProcess.getEvaluator()).evaluate("ip = " + children.getName(i), new XDebuggerEvaluator.XEvaluationCallback() {
                                            @Override
                                            public void evaluated(@NotNull XValue result) {
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
                                        }, debugProcess.getSession().getCurrentPosition());
                                }
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
                });

                ToolWindowManager.getInstance(project).invokeLater(()-> {
                    ToolWindow toolWindow = ToolWindowManager.getInstance(project).registerToolWindow("debug visual" + (toolWindows.isEmpty() ? "" : toolWindows.size()) , f);
                    CanvasToolWindow myToolWindow = new CanvasToolWindow(toolWindow, project);
                    myToolWindow.setWindowContent();
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
