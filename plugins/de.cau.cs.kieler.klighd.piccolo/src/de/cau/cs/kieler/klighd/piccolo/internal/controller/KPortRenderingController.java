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

import de.cau.cs.kieler.klighd.kgraph.KPort;
import de.cau.cs.kieler.klighd.krendering.KRendering;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KPortNode;

/**
 * @author mri
 */
public class KPortRenderingController extends AbstractKGERenderingController<KPort, KPortNode> {

    /**
     * Constructs a rendering controller for a port.
     *
     * @param port
     *            the Piccolo node representing a port
     */
    public KPortRenderingController(final KPortNode port) {
        super(port.getViewModelElement(), port);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PNodeController<?> internalUpdateRendering() {
        final KPortNode repNode = getRepresentation();

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

        return renderingNodeController;
    }

}
