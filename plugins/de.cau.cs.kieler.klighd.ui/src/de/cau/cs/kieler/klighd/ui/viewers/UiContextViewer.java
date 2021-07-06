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
package de.cau.cs.kieler.klighd.ui.viewers;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.contexts.IContextService;

import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.eclipse.EclipseViewContext;
import de.cau.cs.kieler.klighd.eclipse.IDiagramWorkbenchPart;
import de.cau.cs.kieler.klighd.eclipse.internal.IDiagramOutlinePage;
import de.cau.cs.kieler.klighd.eclipse.viewers.EclipseContextViewer;
import de.cau.cs.kieler.klighd.viewers.ContextViewer;

/**
 * A specialized {@link ContextViewer} being in charge of providing UI contributions like context
 * menu and propagation of the selection into the platform.<br>
 * It is used by the {@link de.cau.cs.kieler.klighd.ui.parts.DiagramEditorPart DiagramEditorPart}
 * and {@link de.cau.cs.kieler.klighd.ui.parts.DiagramViewPart DiagramViewPart}.
 *
 * @author chsch
 */
public class UiContextViewer extends EclipseContextViewer implements ISelectionProvider,
        IDiagramOutlinePage.Provider {

    /**
     * Constant string defining the KLighD-specific Eclipse UI key binding context id.
     */
    public static final String KLIGHD_UI_CONTEXT_ID = "de.cau.cs.kieler.klighd.ui.context";

    private Composite diagramContainer;

    /**
     * Constructor.
     *
     * @param parent
     *            the parent {@link Composite} the diagram canvas is to be attached to
     * @param part
     *            the {@link IDiagramWorkbenchPart} this {@link ContextViewer} is attached to (is
     *            only required for setting the selection provider and ui context in time!)
     */
    public UiContextViewer(final Composite parent, final IDiagramWorkbenchPart part) {
        super(parent);
        diagramContainer = parent;

        // register this selection provider in the current work bench part site;
        // this must be done within 'createPartControl()' of 'part',
        //  which is why this registration is done here rather than in 'setModel(...)'
        part.getSite().setSelectionProvider(this);

        // the following activates the our (key binding) context, which is registered
        //  in the plugin.xml
        ((IContextService) part.getSite().getService(IContextService.class))
                .activateContext(KLIGHD_UI_CONTEXT_ID);

        // initialize with the display of an empty string
        showMessage("");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setModel(final Object model, final boolean sync) {
        // if the model is a view context adapt the viewer to the given context if possible
        if (model instanceof ViewContext) {

            super.setModel(model, sync);

            // since the control, i.e. the diagram canvas, is setup during the above call of
            //  'setModel(...)' we cannot register the context menu earlier; thus...
            final Control control = getControl();

            // this test is just for safety purposes, should never be null
            if (control != null) {
                // create the context menu
                final MenuManager menuManager = new MenuManager();

                // be careful: associating the menu with 'diagramContainer' does not work
                //  as it is completely covered by the active viewer's canvas!
                control.setMenu(menuManager.createContextMenu(control));

                // register the context menu in the current work bench part site
                //  this enables the population with entries contributed via extension points
                ((EclipseViewContext) model).getDiagramWorkbenchPart().getSite()
                        .registerContextMenu(menuManager, this);
            }

            // Note that the text widget employed for cursor-based text selection
            //  uses its own context menu, which must be registered at the part site, as well.
            // Since this feature is a service of the PiccoloViewerUI implementation
            //  that context menu is setup in PiccoloViewerUI!

        } else if (model instanceof String) {
            // if the model is a string show it
            showMessage((String) model);

            // provide no (custom) context menu in this case!
        }
    }

    /**
     * Shows the given message.
     *
     * @param message
     *            the message
     */
    private synchronized void showMessage(final String message) {
        final StringViewer stringViewer;

        if (!(getActiveViewer() instanceof StringViewer)) {
            stringViewer = new StringViewer(diagramContainer);
            setViewer(stringViewer);
        } else {
            stringViewer = (StringViewer) getActiveViewer();
        }

        stringViewer.setModel(message, false);
    }
}
