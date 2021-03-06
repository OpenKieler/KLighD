/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://rtsys.informatik.uni-kiel.de/kieler
 * 
 * Copyright 2019 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 */
package de.cau.cs.kieler.klighd.lsp

import org.eclipse.sprotty.xtext.ls.DiagramServerModule
import org.eclipse.sprotty.xtext.ls.IDiagramServerManager

/**
 * Module binding the language server extension for KEITH diagrams.
 * 
 * @author nre
 */
class KGraphDiagramServerModule extends DiagramServerModule {
    override bindLanguageServerImpl() {
        KGraphLanguageServerExtension
    }
    
    override Class<? extends IDiagramServerManager> bindIDiagramServerManager() {
        KGraphDiagramServerManager
    }
    
    def Class<? extends INotificationHandler> bindINotificationHandler() {
        KGraphLanguageServerExtension
    }
}