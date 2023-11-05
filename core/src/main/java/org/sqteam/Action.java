package org.sqteam;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XTestCompositeNode;
import com.intellij.xdebugger.XTestEvaluationCallback;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class Action extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var position = XDebuggerManager.getInstance(e.getProject()).getCurrentSession().getCurrentStackFrame().getSourcePosition();
        XCompositeNode compositeNode = new XCompositeNode() {
            @Override
            public void addChildren(@NotNull XValueChildrenList children, boolean last) {
                System.out.println(children + " -> " + children.size());
                for (int i = 0; i < children.size(); i++){
                    System.out.println(children.getName(i) + " : " + children.getValue(i));
                    children.getValue(i).computeChildren(this);
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
        };
        XDebuggerManager.getInstance(e.getProject()).getCurrentSession().getCurrentStackFrame().computeChildren(compositeNode);
        XDebuggerManager.getInstance(e.getProject()).getCurrentSession().getCurrentStackFrame().getEvaluator().evaluate("a->val1", new XDebuggerEvaluator.XEvaluationCallback(){

            @Override
            public void errorOccurred(@NotNull @NlsContexts.DialogMessage String errorMessage) {
                System.out.println("error:"+errorMessage);
            }

            @Override
            public void evaluated(@NotNull XValue result) {
                System.out.println("ok:");
                System.out.println(result);
            }
        },position);
        XDebuggerManager.getInstance(e.getProject()).getCurrentSession().getCurrentStackFrame();
    }
}
