/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 *
 * Copyright 2012 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 *
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klighd.piccolo.internal.controller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.cau.cs.kieler.klighd.kgraph.KNode;
import de.cau.cs.kieler.klighd.krendering.KChildArea;
import de.cau.cs.kieler.klighd.krendering.KRendering;
import de.cau.cs.kieler.klighd.microlayout.Bounds;
import de.cau.cs.kieler.klighd.microlayout.PlacementUtil;
import de.cau.cs.kieler.klighd.piccolo.IKlighdNode;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KChildAreaNode;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KNodeNode;
import de.cau.cs.kieler.klighd.piccolo.internal.util.NodeUtil;
import edu.umd.cs.piccolo.PNode;

/**
 * An {@link AbstractKGERenderingController} for KNodes generating the rendering PNodes according to
 * the related KRendering rendering description.
 *
 * @author mri
 * @author chsch
 */
public class KNodeRenderingController extends AbstractKGERenderingController<KNode, KNodeNode> {

    /**
     * Constructs a rendering controller for a node.
     *
     * @param node
     *            the Piccolo2D node representing a node
     */
    public KNodeRenderingController(final KNodeNode node) {
        super(node.getViewModelElement(), node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PNodeController<?> internalUpdateRendering() {
        final KNodeNode repNode = getRepresentation();
        final KChildAreaNode childAreaNode = repNode.getChildAreaNode();

        // detach the child area before updating the rendering
        childAreaNode.removeFromParent();

        // evaluate the rendering data
        final KRendering currentRendering = getCurrentRendering();

        final PNodeController<?> renderingNodeController;
        if (currentRendering != null) {
            renderingNodeController =
                    handleAreaAndPointPlacementRendering(currentRendering, repNode);
        } else {
            renderingNodeController =
                    handleAreaAndPointPlacementRendering(createDefaultRendering(), repNode);
        }

        // make sure the child area is attached to something
        if (childAreaNode.getParent() == null) {
            // if the childArea is not part of the above created PNode rendering tree
            // let the whole figure be the child area
            createDefaultChildArea(getRepresentation());
        }
        return renderingNodeController;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PNodeController<?> createChildArea(final IKlighdNode parent, final KChildArea childArea,
            final Bounds initialBounds) {

        final KChildAreaNode childAreaNode = getRepresentation().getChildAreaNode();

        // there can only be none or one child area
        if (childAreaNode.getParent() != null) {
            throw new RuntimeException("More then one child area found in graph element: "
                    + getGraphElement());
        }

        childAreaNode.setRendering(childArea);
        parent.addChild(childAreaNode);

        // configure the child area
        //  Caution: Changing the bounds of a childArea must not happen if the childArea is
        //  not contained in any other PNode as this will influence the positioning of KEdgeNodes,
        //  which synchronize on the container childAreas and their parents
        NodeUtil.applyBounds(childAreaNode, initialBounds);

        // create a controller for the child area and return it
        return new PNodeController<IKlighdNode.IKlighdFigureNode>(childAreaNode) {

            @Override
            public void setBounds(final Bounds bounds) {
                // apply the bounds
                NodeUtil.applyBounds(getPNode(), bounds);
            }
        };
    }

    /**
     * Creates the Piccolo2D node for the parent Piccolo2D node using direct placement.
     *
     * @param parent
     *            the parent Piccolo2D node
     */
    private void createDefaultChildArea(final KNodeNode parent) {
        // determine the initial bounds
        final Bounds bounds = PlacementUtil.evaluateAreaPlacement(null, parent.getBoundsReference());

        // configure the child area
        final PNodeController<?> controller = createChildArea(parent, null, bounds);

        // add a listener on the parent's bounds
        addListener(PNode.PROPERTY_BOUNDS, parent, controller.getNode(),
                new PropertyChangeListener() {
                    public void propertyChange(final PropertyChangeEvent e) {
                        // calculate the new bounds of the rendering
                        final Bounds bounds = PlacementUtil.evaluateAreaPlacement(null,
                                parent.getBoundsReference());
                        // use the controller to apply the new bounds
                        controller.setBounds(bounds);
                    }
                });
    }
}
