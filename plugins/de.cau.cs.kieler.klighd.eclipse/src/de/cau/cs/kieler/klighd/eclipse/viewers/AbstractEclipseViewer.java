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
package de.cau.cs.kieler.klighd.eclipse.viewers;

import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import de.cau.cs.kieler.klighd.eclipse.IEclipseViewer;
import de.cau.cs.kieler.klighd.eclipse.IEclipseKlighdSelection;
import de.cau.cs.kieler.klighd.eclipse.KlighdTreeSelection;
import de.cau.cs.kieler.klighd.kgraph.KGraphElement;
import de.cau.cs.kieler.klighd.krendering.KText;
import de.cau.cs.kieler.klighd.viewers.AbstractViewer;
import de.cau.cs.kieler.klighd.viewers.ContextViewer;

/**
 * An abstract base class for concrete KGraph/KRendering viewers. It provides implementations of
 * those methods concrete viewers are not in charge of implementing, e.g. the source model related
 * ones ({@link Object}-based) and the selection related ones. The view model related methods (
 * {@link KGraphElement}/{@link de.cau.cs.kieler.klighd.kgraph.KNode KNode}/{@link KText}-based ones)
 * must be implemented by concrete viewers.
 *
 * @author mri
 * @author chsch
 *
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch
 */
public abstract class AbstractEclipseViewer extends AbstractViewer implements IEclipseViewer {

    /**
     * Forwards the given <code>selection</code> to the employed {@link ContextViewer} that is in
     * charge of broadcasting it into the platform and the registered selection listeners.
     *
     * @param selection
     *            the new {@link IEclipseKlighdSelection}
     */
    protected void updateSelection(final IEclipseKlighdSelection selection) {
        ((EclipseContextViewer) this.getContextViewer()).notifySelectionListeners(selection);
    }

    /* ---------------------------------------------------------- */
    /*   the selection setting API                                */
    /*    it is completely implemented by the ContextViewer,      */
    /*    no implementations of this class need to implement it!  */
    /* ---------------------------------------------------------- */

    /**
     * {@inheritDoc}
     */
    public IEclipseKlighdSelection getSelection() {
        return ((IEclipseViewer) getContextViewer()).getSelection();
    }

    /**
     * {@inheritDoc}
     */
    public KlighdTreeSelection getDiagramSelection() {
        return ((IEclipseViewer) getContextViewer()).getDiagramSelection();
    }
}

