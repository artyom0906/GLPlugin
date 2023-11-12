package org.sqteam.service;

import com.intellij.xdebugger.XDebugProcess;
import org.sqteam.network.ImageEventTransport;

public interface VisualDebugSessionManger {
    void startDebug(XDebugProcess process);
    void endDebug(XDebugProcess process);
}
