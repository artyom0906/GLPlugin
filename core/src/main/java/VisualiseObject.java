import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.impl.DebuggerSupport;
import com.intellij.xdebugger.impl.actions.DebuggerActionHandler;
import com.intellij.xdebugger.impl.actions.XDebuggerActionBase;
import com.intellij.xdebugger.impl.actions.handlers.XDebuggerActionHandler;
import com.intellij.xdebugger.impl.actions.handlers.XDebuggerEvaluateActionHandler;
import com.intellij.xdebugger.impl.breakpoints.XExpressionImpl;
import com.intellij.xdebugger.impl.ui.tree.XDebuggerTree;
import com.intellij.xdebugger.impl.ui.tree.actions.XDebuggerTreeActionBase;
import com.intellij.xdebugger.impl.ui.tree.nodes.XValueNodeImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.concurrency.Promise;
import org.jetbrains.concurrency.Promises;

import static com.intellij.xdebugger.impl.ui.tree.actions.XDebuggerTreeActionBase.getSelectedNodes;

public class VisualiseObject extends XDebuggerActionBase {
    private static class Holder {
        private static final XDebuggerTreeActionBase VISUALISE_ACTION = new XDebuggerTreeActionBase(){

            @Override
            protected boolean isEnabled(@NotNull final XValueNodeImpl node, @NotNull AnActionEvent e) {
                return super.isEnabled(node, e);
            }
            @Override
            protected void perform(XValueNodeImpl node, @NotNull String nodeName, AnActionEvent e) {
                System.err.println(nodeName + " -> " + node + " [" + e +"]");
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                for (XValueNodeImpl node : getSelectedNodes(e.getDataContext())) {
                    if (node != null) {
                        String nodeName = node.getName();
                        if (nodeName != null) {
                            perform(node, nodeName, e);
                        }
                    }
                }
            }


        };
    };
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        if (XDebuggerTreeActionBase.getSelectedNode(event.getDataContext()) != null) {
            VisualiseObject.Holder.VISUALISE_ACTION.actionPerformed(event);
        }
        else {
            super.actionPerformed(event);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        if (XDebuggerTreeActionBase.getSelectedNode(event.getDataContext()) != null) {
            VisualiseObject.Holder.VISUALISE_ACTION.update(event);
        }
        else {
            super.update(event);
        }
    }

    @Override
    protected @NotNull DebuggerActionHandler getHandler(@NotNull DebuggerSupport debuggerSupport) {
        //System.out.println(debuggerSupport);
        return new XDebuggerActionHandler(){

            @Override
            protected boolean isEnabled(@NotNull XDebugSession session, DataContext dataContext) {
                return true;
            }


            @Override
            protected void perform(@NotNull XDebugSession session, DataContext dataContext) {
                ApplicationManager.getApplication().invokeLater(()->{
                    getTextToEvaluate(dataContext, session).onError(System.err::println).then(s->{
                        System.out.println("s:" + s);
                        return s;
                    }).onSuccess(text->{
                        if(text == null) return;
                        session.getCurrentStackFrame().getEvaluator().evaluate(XExpressionImpl.fromText(text), new XDebuggerEvaluator.XEvaluationCallback() {
                            @Override
                            public void evaluated(@NotNull XValue result) {
                                System.err.println("result: " + result);

                                StringBuilder sb = new StringBuilder();
                                sb.append("(void)'plugin::renderer<");
                                sb.append(result.toString().substring(result.toString().indexOf(":")+1));
                                sb.append(" >::add'(");
                                sb.append(text);
                                sb.append(")");

                                session.getCurrentStackFrame().getEvaluator().evaluate(sb.toString(), new XDebuggerEvaluator.XEvaluationCallback() {
                                    @Override
                                    public void evaluated(@NotNull XValue result) {
                                        System.err.println(result);
                                    }

                                    @Override
                                    public void errorOccurred(@NotNull @NlsContexts.DialogMessage String errorMessage) {
                                        System.err.println(errorMessage);
                                    }
                                }, session.getCurrentPosition());

                            }

                            @Override
                            public void errorOccurred(@NotNull @NlsContexts.DialogMessage String errorMessage) {

                            }
                        }, session.getCurrentPosition());
                    });
                }, ModalityState.nonModal());

                var a = XDebuggerTree.getSelectedNodes(dataContext);
                XDebuggerTree tree1 = XDebuggerTree.getTree(dataContext);
                System.out.println(dataContext);
                System.out.println(a);
                System.out.println(tree1);
            }
            @NotNull
            protected static Promise<String> getTextToEvaluate(DataContext dataContext, XDebugSession session) {
                final Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
                if (editor == null) {
                    return Promises.resolvedPromise(null);
                }

                String text = editor.getSelectionModel().getSelectedText();
                if (text != null) {
                    return Promises.resolvedPromise(StringUtil.nullize(text, true));
                }
                XDebuggerEvaluator evaluator = session.getDebugProcess().getEvaluator();
                if (evaluator != null) {
                    return XDebuggerEvaluateActionHandler.getExpressionText(evaluator, editor.getProject(), editor).then(s -> StringUtil.nullize(s, true));
                }
                return Promises.resolvedPromise(null);
            }
        };

    }
}
