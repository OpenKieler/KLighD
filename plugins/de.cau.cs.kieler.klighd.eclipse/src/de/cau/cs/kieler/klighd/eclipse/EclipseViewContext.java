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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;

import de.cau.cs.kieler.klighd.IViewer;
import de.cau.cs.kieler.klighd.IViewerProvider;
import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.internal.ILayoutRecorder;
import de.cau.cs.kieler.klighd.viewers.ContextViewer;

/**
 * A view context is a data record containing the required configuration information to translate a
 * model into a diagram and draw it on the screen, as well as provide user operations on it.<br>
 * <br>
 *
 * ViewContexts contain information on
 * <ul>
 * <li>the business model (input model) that is to be shown,</li>
 * <li>the source workbench part the business model stems from,</li>
 * <li>the diagram synthesis that is in charge of creating the view model,</li>
 * <li>the update strategy that is to be applied in case of diagram updates,</li>
 * <li>the resulting view model describing the diagram,</li>
 * <li>the {@link IViewerProvider} that wraps the instantiation of the viewer being used</li>
 * </ul>
 *
 * @author mri
 * @author chsch
 *
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch
 */
public class EclipseViewContext extends ViewContext {

    /** the serial version UID. */
    private static final long serialVersionUID = -431994394109554393L;

    /** the part the source model was selected from (if can reasonably be determined). */
    private transient IWorkbenchPart sourceWorkbenchPart = null;

    /** the workbench part for which the viewer is created. */
    private IDiagramWorkbenchPart diagramWorkbenchPart;

    /**
     * Standard constructor.
     *
     * @param diagramPart
     *            the {@link IDiagramWorkbenchPart} the diagram is shown in
     * @param inputModel
     *            the source model to be represented by a diagram
     */
    public EclipseViewContext(final IDiagramWorkbenchPart diagramPart, final Object inputModel) {
        super(inputModel);
        this.diagramWorkbenchPart = diagramPart;

    }

    // ---------------------------------------------------------------------------------- //
    //  initialization part


    /**
     * Creates the wrapped {@link IViewer} instance that is actually in charge of drawing the diagram
     * into the provided SWT {@link Composite} widget <code>parent</code>. Returns <code>null</code>
     * if no fitting {@link IViewerProvider} has been found during configuration.
     *
     * @param parentViewer
     *            the parent {@link ContextViewer}
     * @param parent
     *            the parent {@link Composite} widget
     * @return the created viewer or <code>null</code> on failure
     */
    public IViewer createViewer(final ContextViewer parentViewer, final Composite parent) {
        if (this.viewerProvider != null) {
            // create the new viewer
            this.viewer = ((IEclipseViewerProvider) this.viewerProvider).createViewer(parentViewer, parent);

            if (this.viewer instanceof ILayoutRecorder) {
                this.layoutRecorder = (ILayoutRecorder) viewer;
            }

            // set the base model if possible
            this.viewer.setModel(this.viewModel, true);

            return this.viewer;
        } else {
            return null;
        }
    }

    /**
     * Returns the diagram workbench part.
     *
     * @return the {@link IDiagramWorkbenchPart}
     */
    public IDiagramWorkbenchPart getDiagramWorkbenchPart() {
        return diagramWorkbenchPart;
    }


    /**
     * Sets the source workbench part (part the source model has been chosen in).
     *
     * @param theSourceWorkbenchPart
     *            the source workbench part (part the source model has been chosen in)
     */
    public void setSourceWorkbenchPart(final IWorkbenchPart theSourceWorkbenchPart) {
        this.sourceWorkbenchPart = theSourceWorkbenchPart;
    }

    /**
     * Returns the source workbench part.
     *
     * @return the source workbench part (part the source model has been chosen in)
     */
    public IWorkbenchPart getSourceWorkbenchPart() {
        return sourceWorkbenchPart;
    }


    // ---------------------------------------------------------------------------------- //
    //  Source target element handling


    // ---------------------------------------------------------------------------------- //
    //  Synthesis option handling


    // ---------------------------------------------------------------------------------- //
    //  Offered action handling


    // ---------------------------------------------------------------------------------- //
    //  Recommended layout option handling

    // ---------------------------------------------------------------------------------- //
    //  Additional layout configuration handling
    

    // ---------------------------------------------------------------------------------- //
    //  Child ViewContext handling
}
