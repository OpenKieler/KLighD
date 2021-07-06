/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2013 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klighd.eclipse.actions;

import com.google.common.collect.Iterables;

import de.cau.cs.kieler.klighd.IAction;
import de.cau.cs.kieler.klighd.IViewer;
import de.cau.cs.kieler.klighd.eclipse.IEclipseViewer;
import de.cau.cs.kieler.klighd.kgraph.KNode;

/**
 * Sets the diagram clip to the first element of the current selection if it is a {@link KNode}.
 * Does nothing otherwise.
 * 
 * @author chsch
 * 
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch
 */
public class ClipSelectionAction implements IAction {

    /**
     * The extension id of this actions. This id is to be mentioned in instances of
     * {@link de.cau.cs.kieler.klighd.krendering.KAction KAction}.
     */
    public static final String ID = "de.cau.cs.kieler.klighd.actions.ClipSelectionAction";
    
    /**
     * {@inheritDoc}
     */
    public ActionResult execute(final ActionContext context) {
        final Iterable<?> selection = ((IEclipseViewer) context.getContextViewer()).getDiagramSelection();
        final IViewer contextViewer = context.getContextViewer();
        
        final Object first = Iterables.getFirst(selection, null);

        if (first instanceof KNode) {
            final KNode node = (KNode) first;
            
            context.getActiveViewer().clip(node);
            
            if (!contextViewer.isExpanded(node) && !node.getChildren().isEmpty()) {
                contextViewer.expand(node);
            }
            
            return ActionResult.createResult(true);
        } else {
            return ActionResult.createResult(false);
        }
    }
}
