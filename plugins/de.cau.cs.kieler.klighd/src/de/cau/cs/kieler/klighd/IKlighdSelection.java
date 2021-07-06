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
package de.cau.cs.kieler.klighd;

import java.util.Iterator;

import org.eclipse.emf.ecore.EObject;

/**
 * A common interface of KLighD-specific implementations of {@link ISelection}.
 * 
 * @author chsch
 * 
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch
 */
public interface IKlighdSelection {

    /**
     * Provides the {@link IViewer} of the corresponding diagram.
     *   
     * @return the {@link IViewer} of the corresponding diagram.
     */
    IViewer getViewer();

    /**
     * Provides the {@link ViewContext} of corresponding diagram.
     * 
     * @return the {@link ViewContext} of corresponding diagram
     */
    ViewContext getViewContext();

    /**
     * Provides an {@link Iterator} of the selected
     * {@link de.cau.cs.kieler.klighd.kgraph.KGraphElement KGraphElements} and
     * {@link de.cau.cs.kieler.klighd.krendering.KText KTexts}.
     * 
     * @return an {@link Iterator} of the selected
     *         {@link de.cau.cs.kieler.klighd.kgraph.KGraphElement KGraphElements} and
     *         {@link de.cau.cs.kieler.klighd.krendering.KText KTexts}
     */
    Iterator<EObject> diagramElementsIterator();
}
