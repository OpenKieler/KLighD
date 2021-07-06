/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 *
 * Copyright 2011 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 *
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klighd.eclipse;

import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.widgets.Control;

import de.cau.cs.kieler.klighd.IViewer;
import de.cau.cs.kieler.klighd.kgraph.KGraphElement;
import de.cau.cs.kieler.klighd.krendering.KText;

/**
 * The common interface for viewers displaying diagrams of provided models.<br>
 * <br>
 * This interface is supposed to be used in client's code for revealing elements in diagrams,
 * expanding & collapsing elements, clipping the diagram, or asking for the source elements
 * of representatives in diagrams by referring to {@link #getViewContext()}.
 *
 * @author mri
 * @author chsch
 *
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch
 */
public interface IEclipseViewer extends IViewer {

    /**
     * Returns the control used by this viewer.
     *
     * @return the control
     */
    Control getControl();
    


    /* ----------------------------- */
    /*   the selection setting API   */
    /* ----------------------------- */

    /**
     * Provides the current {@link KlighdTreeSelection} provided by the diagram viewer.
     *
     * @return the current {@link KlighdTreeSelection} or <code>null</code> if the current selection
     *         is not of type {@link KlighdTreeSelection}
     */
    KlighdTreeSelection getDiagramSelection();

    /**
     * Provides the current {@link IEclipseKlighdSelection} maintained by the diagram viewer.<br>
     * In earlier versions the return type was {@link org.eclipse.jface.viewers.ISelection}, which is
     * the super interface of {@link IEclipseKlighdSelection}.
     *
     * @return the current {@link IEclipseKlighdSelection}
     */
    IEclipseKlighdSelection getSelection();
}
