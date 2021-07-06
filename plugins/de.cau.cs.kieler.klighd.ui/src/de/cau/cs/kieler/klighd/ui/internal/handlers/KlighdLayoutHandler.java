/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2014 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klighd.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import de.cau.cs.kieler.klighd.eclipse.EclipseLightDiagramLayoutConfig;
import de.cau.cs.kieler.klighd.eclipse.IDiagramWorkbenchPart;

/**
 * Handler for execution of automatic layout in KLighD triggered by menu, toolbar, or keyboard
 * command.
 * 
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow 2014-01-25
 * 
 * @author chsch
 */
public class KlighdLayoutHandler extends AbstractHandler {
    
    /**
     * {@inheritDoc}
     */
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        
        // get the active editor, which is expected to contain the diagram for applying layout
        IEditorPart editorPart = HandlerUtil.getActiveEditor(event);
        if (editorPart instanceof IDiagramWorkbenchPart) {
            new EclipseLightDiagramLayoutConfig((IDiagramWorkbenchPart) editorPart).performLayout();
        }
        // the else case should not happen as this handler is only active for
        //  IDiagramWorkbenchPart.IDiagramEditorParts, see registration in plugin.xml
        
        return null;
    }
}
