/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://rtsys.informatik.uni-kiel.de/kieler
 * 
 * Copyright 2019, 2020 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 */
package de.cau.cs.kieler.klighd.lsp

import org.eclipse.sprotty.Action

/**
 * Service Interface for ActionHandler.
 * 
 * @author sdo
 */
interface IActionHandler {
    /**
     * Checks and returns true if this ActionHandler can handle this action.
     * @param kind String identifier of action
     */
    def boolean canHandleAction(String kind)
    
    /**
     * Handles an action if of the kind specified by canHandleAction.
     * @param action Action to handle.
     * @param clientId The id of the client.
     * @param server The diagram server.
     */
    def void handle(Action action, String clientId, KGraphDiagramServer server)
}