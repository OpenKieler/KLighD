/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://rtsys.informatik.uni-kiel.de/kieler
 *
 * Copyright 2016 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 *
 * This code is provided under the terms of the Eclipse Public License (EPL).
 */
package de.cau.cs.kieler.klighd.eclipse;

import de.cau.cs.kieler.klighd.LightDiagramLayoutConfig;
import de.cau.cs.kieler.klighd.LightDiagramServices;
import de.cau.cs.kieler.klighd.ViewContext;

/**
 * Configuration object for {@link LightDiagramServices} methods
 * {@link LightDiagramServices#layoutDiagram(LightDiagramLayoutConfig)} and
 * {@link LightDiagramServices#updateDiagram(LightDiagramLayoutConfig). These configuration holders
 * should be disposed of after use. For this reason there are no public getters available.
 * 
 * Example:
 * <code>
 * new LightDiagramLayoutConfig(viewContext)
 *          .zoomStyle(zoomStyle)
 *          .focusNode(focusNode)
 *          .layout();
 * </code>
 * 
 * @author enbewe
 */
public class EclipseLightDiagramLayoutConfig extends LightDiagramLayoutConfig {

    /**
     * The {@link IDiagramWorkbenchPart} containing the view model.
     */
    private IDiagramWorkbenchPart workbenchPart;

    /**
     * Creates a configuration for a {@link IDiagramWorkbenchPart}.
     * 
     * @param workbenchPart
     *            the {@link IDiagramWorkbenchPart} to be used in the configuration.
     */
    public EclipseLightDiagramLayoutConfig(final IDiagramWorkbenchPart workbenchPart) {
        this.workbenchPart = workbenchPart;
    }

    ///////////////////////////////////////////////////////////
    // Modification of the configuration

    ///////////////////////////////////////////////////////////
    // Reading the configuration in LightDiagramServices

    /**
     * The {@link IDiagramWorkbenchPart} this layout is related to.
     * 
     * @return the {@link IDiagramWorkbenchPart} or <code>null</code> if a {@link ViewContext} was
     *         used to create this configuration.
     */
    IDiagramWorkbenchPart workbenchPart() {
        return this.workbenchPart;
    }

    ///////////////////////////////////////////////////////////
    // Convenience aliases

}
