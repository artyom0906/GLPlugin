package org.sqteam.debug;

import com.intellij.openapi.util.NlsContexts;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XExpression;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XValue;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class VisualEvaluator{
    private final XDebugProcess debugProcess;
    private final Queue<XExpression> expressionQueue = new LinkedList<XExpression>();
    private final ExecutorService executor;

    private final List<Consumer<String>> errors = new ArrayList<>();
    public VisualEvaluator(XDebugProcess debugProcess, ExecutorService executor){

        this.debugProcess = debugProcess;
        this.executor = executor;
    }

    public void addNext(XExpression expression){
        expressionQueue.add(expression);
    }

    public void onError(Consumer<String> consumer){
        errors.add(consumer);
    }

    private Runnable nextTask(){

        System.out.println("submitting:" + expressionQueue.peek());
        return ()->{
            Objects.requireNonNull(debugProcess.getEvaluator()).evaluate(Objects.requireNonNull(expressionQueue.poll()), new XDebuggerEvaluator.XEvaluationCallback() {
                @Override
                public void evaluated(@NotNull XValue result) {
                    System.out.println("result: " + result );
                    if(expressionQueue.peek()==null) return;
                    executor.submit(nextTask());
                }

                @Override
                public void errorOccurred(@NotNull @NlsContexts.DialogMessage String errorMessage) {
                    errors.forEach(e->e.accept(errorMessage));
                }
            }, debugProcess.getSession().getCurrentPosition());
        };
    }

    public void run(){

        executor.submit(nextTask());
    }
}