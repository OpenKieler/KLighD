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

import org.eclipse.jface.viewers.TreePath;

import de.cau.cs.kieler.klighd.IAction;
import de.cau.cs.kieler.klighd.IViewer;
import de.cau.cs.kieler.klighd.eclipse.IEclipseViewer;
import de.cau.cs.kieler.klighd.eclipse.KlighdTreeSelection;
import de.cau.cs.kieler.klighd.kgraph.KNode;

/**
 * Set the diagram clip the child of the current clip that on the "hierarchy path" leading to
 * currently selected element, if that child is a {@link KNode}.
 * 
 * @author chsch
 * 
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch
 */
public class ClipTowardsSelectionAction implements IAction {

    /**
     * The extension id of this actions. This id is to be mentioned in instances of
     * {@link de.cau.cs.kieler.klighd.krendering.KAction KAction}.
     */
    public static final String ID = "de.cau.cs.kieler.klighd.actions.ClipTowardsSelectionAction";
    
    /**
     * {@inheritDoc}
     */
    public ActionResult execute(final ActionContext context) {
        final KlighdTreeSelection selection = ((IEclipseViewer) context.getContextViewer()).getDiagramSelection();
        final IViewer contextViewer = context.getContextViewer();
        
        final Object first = selection.getFirstElement();
        final TreePath path = selection.getPathsFor(first)[0];

        final KNode clip = context.getActiveViewer().getClip();
        final int max = path.getSegmentCount() - 1;

        int index = 0;
        for (; index < max;) {
            if (path.getSegment(index++) == clip) {
                break;
            }
        }
        
        final Object o;
        if (index != 0 && index < path.getSegmentCount()) {
            o = path.getSegment(index);
        } else {
            o = null;
        }
        
        if (!(o instanceof KNode)) {
            return ActionResult.createResult(false);
        }
  
        final KNode node = (KNode) o;
        context.getActiveViewer().clip(node);
        
        if (!contextViewer.isExpanded(node) && isExpandable(node)) {
            contextViewer.expand(node);
        }
        
        return ActionResult.createResult(true);
    }


    /**
     * Determines whether <code>node</code> is expandable.<br>
     * May be overridden by subclasses.
     * 
     * @param node
     *            the {@link KNode} to be tested.
     * @return <code>true</code> if <code>node</code> is expandable, <code>false</code> otherwise
     */
    public boolean isExpandable(final KNode node) {
        return !node.getChildren().isEmpty();
    }
}
