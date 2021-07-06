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
package de.cau.cs.kieler.klighd.ui;

import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.elk.graph.properties.IPropertyHolder;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.google.common.collect.Maps;

import de.cau.cs.kieler.klighd.Klighd;
import de.cau.cs.kieler.klighd.LightDiagramLayoutConfig;
import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.eclipse.EclipseViewContext;
import de.cau.cs.kieler.klighd.eclipse.IDiagramWorkbenchPart;
import de.cau.cs.kieler.klighd.ui.parts.DiagramEditorPart;
import de.cau.cs.kieler.klighd.ui.parts.DiagramViewPart;

/**
 * A singleton manager for creating, updating and closing diagram views. All methods in this class
 * have to be called in the UI thread.
 *
 * @author mri
 * @author chsch
 */
public final class DiagramViewManager implements IPartListener {

    /** the primary identifier for the diagram view as specified in the view extension. */
    private static final String PRIMARY_VIEW_ID = DiagramViewPart.VIEW_ID;

    /** the singleton instance. */
    private static DiagramViewManager singletonInstance = new DiagramViewManager();

    /** whether the manager is currently registered as a part listener. */
    private boolean registered = false;

    /** the mapping of diagram view identifiers on workbench parts. */
    private Map<String, IDiagramWorkbenchPart> idPartMapping = Maps.newHashMap();

    /** the mapping of diagram view identifiers on view contexts. */
    private Map<String, ViewContext> idContextMapping = Maps.newHashMap();

    /**
     * Returns the singleton instance.
     *
     * @return the singleton instance
     */
    public static DiagramViewManager getInstance() {
        return singletonInstance;
    }

    /**
     * A private constructor to prevent instantiation.
     */
    private DiagramViewManager() {
        // do nothing
        // chsch: does this the trick as well as at the other places? We'll see...
        registerPartListener();
    }

    /**
     * Returns the {@link IDiagramWorkbenchPart} associated with the given identifier if available.
     *
     * @param id
     *            the diagram view identifier (can be null for the default view)
     * @return the diagram view or null if no view with the given identifier exists
     */
    public static IDiagramWorkbenchPart getDiagramWorkbenchPart(final String id) {
        return getInstance().idPartMapping.get(id);
    }

    /**
     * Returns the diagram view with the given identifier if available.
     *
     * @param id
     *            the diagram view identifier (can be null for the default view)
     * @return the diagram view or null if no view with the given identifier exists
     */
    public static DiagramViewPart getView(final String id) {
        final IDiagramWorkbenchPart view = getInstance().idPartMapping.get(id);
        if (view != null && view instanceof DiagramViewPart) {
            if (((DiagramViewPart) view).isDisposed()) {
                // actually this branch should not be taken due to DiagramViewPart#dispose();
                // however, there're still those ugly exceptions after a view is closed and
                //  tried to be re-opened...
                getInstance().unregisterView(view);
                return null;
            } else {
                return (DiagramViewPart) view;
            }
        } else {
            return null;
        }
    }


    /**
     * Returns the diagram editor part with the given identifier if available.
     *
     * @param id
     *            the diagram view identifier (can be null for the default view)
     * @return the diagram editor part or null if no view with the given identifier exists
     */
    public static DiagramEditorPart getEditor(final String id) {
        final IDiagramWorkbenchPart part = getInstance().idPartMapping.get(id);
        if (part != null && part instanceof DiagramEditorPart) {
            return (DiagramEditorPart) part;
        } else {
            return null;
        }
    }


    /**
     * Updates the diagram view with the given identifier.<br>
     * <br>
     * The model has to be of the type of the old model, i.e. the view context associated with the
     * identifier must support the model.
     *
     * @param id
     *            the diagram identifier (can be null for the default view)
     * @return the view with the identifier or null on failure
     *
     * @author chsch
     */
    public static IDiagramWorkbenchPart updateView(final String id) {
        return updateView(id, null, null, null);
    }


    /**
     * Updates the diagram view with the given identifier.<br>
     * <br>
     * The model has to be of the type of the old model, i.e. the view context associated with the
     * identifier must support the model.
     *
     * @param id
     *            the diagram identifier (can be null for the default view)
     * @param properties
     *            the property holder containing properties configurations or <code>null</code>
     * @return the view with the identifier or null on failure
     *
     * @author chsch
     */
    public static IDiagramWorkbenchPart updateView(final String id,
            final IPropertyHolder properties) {
        return updateView(id, null, null, properties);
    }


    /**
     * Updates the diagram view with the given identifier with a specified name and model.<br>
     * <br>
     * The model has to be of the type of the old model, i.e. the view context associated with the
     * identifier must support the model.
     *
     * @param id
     *            the diagram identifier (can be null for the default view)
     * @param name
     *            the name (can be null if the name of the view should remain unchanged)
     * @param model
     *            the model (can be null if the displayed model should remain unchanged)
     * @param properties
     *            the property holder containing properties configurations or <code>null</code>
     * @return the view with the identifier or null on failure
     */
    public static IDiagramWorkbenchPart updateView(final String id, final String name,
            final Object model, final IPropertyHolder properties) {

        // get the view
        final IDiagramWorkbenchPart diagramView = getDiagramWorkbenchPart(id);
        if (diagramView == null) {
            return null;

        } else if (name != null && diagramView instanceof DiagramViewPart) {
            // set/update the view name
            ((DiagramViewPart) diagramView).setName(name);
        } 

        final ViewContext viewContext = diagramView.getViewContext();

        if (viewContext == null) {
            return null;
        }

        // update the view context
        if (!(new LightDiagramLayoutConfig(viewContext)
                    .model(model)
                    .properties(properties)
                    .performUpdate())) {
            return null;
        }

        return diagramView;
    }


    /**
     * Updates the diagram view corresponding to the given {@link ViewContext}.<br>
     * <br>
     * The <code>viewContext</code>'s current input model is taken as new input.
     *
     * @param viewContext
     *            the {@link ViewContext} of the diagram to be updated
     * @return the view with the identifier or null on failure
     */
    public static IDiagramWorkbenchPart updateView(final ViewContext viewContext) {
        return updateView(viewContext, null);
    }

    /**
     * Updates the diagram view corresponding to the given {@link ViewContext}.<br>
     * <br>
     * The model has to be of the type of the old model, i.e. the view context associated with the
     * identifier must support the model.
     *
     * @param viewContext
     *            the {@link ViewContext} of the diagram to be updated
     * @param model
     *            the new model, if <code>null</code> the current input model is taken
     * @return the view with the identifier or null on failure
     */
    public static IDiagramWorkbenchPart updateView(final ViewContext viewContext, final Object model) {
        boolean res = new LightDiagramLayoutConfig(viewContext)
                            .model(model)
                            .performUpdate();
        return res ? ((EclipseViewContext) viewContext).getDiagramWorkbenchPart() : null;
    }

    /**
     * Creates a {@link DiagramViewPart} showing the desired diagram of <code>model</code>. The
     * diagram view will exhibit the given name and can be addressed via the specified id.
     * 
     * @param id
     *            the diagram identifier (can be <code>null</code> for the default view)
     * @param name
     *            the name, may be <code>null</code> if the view should be created with the default
     *            name
     * @param model
     *            the model, may be <code>null</code> if the view should be created without an
     *            initial model
     * @return the view with the identifier (newly created or reused if it exists already), or
     *         <code>null</code> on failure
     */
    public static DiagramViewPart createView(final String id, final String name, final Object model) {
        return createView(id, name, model, null);
    }

    /**
     * Creates a {@link DiagramViewPart} showing the desired diagram of <code>model</code>. The
     * diagram view will exhibit the given name and can be addressed via the specified id.
     * 
     * @param id
     *            the diagram identifier (can be <code>null</code> for the default view)
     * @param name
     *            the name, may be <code>null</code> if the view should be created with the default
     *            name
     * @param model
     *            the model, may be <code>null</code> if the view should be created without an
     *            initial model
     * @param properties
     *            the property holder containing properties configurations or <code>null</code>
     * @return the view with the identifier (newly created or reused if it exists already), or
     *         <code>null</code> on failure
     */
    public static DiagramViewPart createView(final String id, final String name, final Object model,
            final IPropertyHolder properties) {
        // register the manager as part listener if necessary
        getInstance().registerPartListener();

        // create a view context for the model
        if (model == null) {
            return null;
        }

        final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        final IWorkbenchPage page = window.getActivePage();

        // look for an available view with the id 'id'
        DiagramViewPart diagramView = getView(id);

        if (diagramView == null) {
            // create the view and register it
            try {
                final IViewPart view =
                        page.showView(PRIMARY_VIEW_ID, id, IWorkbenchPage.VIEW_VISIBLE);
                if (view instanceof DiagramViewPart) {
                    diagramView = (DiagramViewPart) view;
                } else {
                    throw new PartInitException(
                            "KLighD: Creation of a KLighD DiagramViewPart failed.\n"
                                    + "This appears to be a heavy internal error!");
                }

            } catch (final PartInitException e) {
                Klighd.handle(new Status(IStatus.ERROR, KlighdUIPlugin.PLUGIN_ID, e.getMessage(), e));

                return null;
            } catch (final IllegalArgumentException e) {
                Klighd.handle(new Status(IStatus.ERROR, KlighdUIPlugin.PLUGIN_ID,
                        "Invalid KLighD view id: must not be empty or contain any colons."));

                return null;
            }
        } else {
            // in case there is already a view with the given id
            //  clean that one up by unregistering view contexts currently associated with it
            //  as a new one will be registered below
            getInstance().unregisterView(diagramView);
        }

        diagramView.initialize(model, name, properties);

        return diagramView;
    }

    /**
     * (Re-)Initializes the given {@link DiagramViewPart}.<br>
     * This involves the creation of a {@link ViewContext}, the execution the matching
     * implementation {@link de.cau.cs.kieler.klighd.syntheses.AbstractDiagramSynthesis
     * AbstractDiagramSynthesis}, the application of automatic layout. and the update of the diagram
     * side bar entries.
     *
     * @deprecated use {@link DiagramViewPart#initialize(Object, String, IPropertyHolder)}
     *
     * @param diagramView
     *            the {@link DiagramViewPart}, must not be <code>null</code>
     * @param name
     *            the name (can be null if the view should be created with the default name)
     * @param model
     *            the model (can be null if the view should be created without an initial model)
     * @param properties
     *            the property holder containing properties configurations or <code>null</code>
     * @return {@code diagramView} for convenience
     */
    public static DiagramViewPart initializeView(final DiagramViewPart diagramView,
            final Object model, final String name, final IPropertyHolder properties) {

        diagramView.initialize(model, name, properties);
        return diagramView;
    }

    /**
     * Allows {@link IDiagramWorkbenchPart}s that are not created via this manager to be registered,
     * e.g. {@link DiagramEditorPart DiagramEditorParts}. This is (currently) necessary in order to
     * let the side bar work correctly (diagram refresh), since those event listener call
     * {@link DiagramViewManager#updateView(String, IPropertyHolder)}.
     *
     * @param part
     *            the {@link IDiagramWorkbenchPart} to register
     */
    public void registerView(final IDiagramWorkbenchPart part) {
        final String id = part.getPartId();
        this.idPartMapping.put(id, part);
        this.idContextMapping.put(id, part.getViewContext());
    }

    /**
     * Is package protected in order to be callable from {@link DiagramViewPart#dispose()}.
     *
     * @param part
     *            the part being closed
     */
    public void unregisterView(final IDiagramWorkbenchPart part) {
        final String id = part.getPartId();
        idPartMapping.remove(id);
        idContextMapping.remove(id);
    }

    /**
     * Closes the diagram view associated with the given id.
     *
     * @param id
     *            the identifier of the view to be closed.
     * @return <code>true</code> if a view could be closed successfully, and <code>false</code>
     *         otherwise.
     *
     * @author chsch
     */
    public static boolean closeView(final String id) {
        if (id.equals("")) {
            return false;
        }
        try {
            final DiagramViewPart view;
            final IDiagramWorkbenchPart part = getInstance().idPartMapping.get(id);
            if (part instanceof DiagramEditorPart) {
                return false;
            } else if (part instanceof DiagramViewPart) {
                view = (DiagramViewPart) part;
            } else {
                view = null;
            }

            if (view != null) {
                getInstance().unregisterView(view);
                view.getSite().getPage().hideView(view);
                return true;
            } else {

                final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                if (window == null) {
                    return false;
                }

                final IWorkbenchPage page = window.getActivePage();
                final IViewReference viewRef = page.findViewReference(PRIMARY_VIEW_ID, id);
                if (viewRef != null) {
                    page.hideView(viewRef);
                    return true;
                }
            }

        } catch (final NullPointerException e) {
            /* do nothing */
        }
        return false;
    }

    /**
     * Registers the manager as a part listener on the active workbench window.
     */
    private void registerPartListener() {
        if (!registered) {
            registered = true;
            final IWorkbenchPage page =
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

            if (page == null) {
                return;
            }

            // find existing views
            final IViewReference[] viewReferences = page.getViewReferences();
            for (final IViewReference viewReference : viewReferences) {
                if (viewReference.getId().equals(PRIMARY_VIEW_ID)) {
                    // SUPPRESS CHECKSTYLE PREVIOUS block

                    // chsch: for the Moment, reset all existing views since there may
                    // occur errors during the (re-)initialization a further views

                    // Update: deactivated the closing of existing view skeletons as
                    // the behavior seems to be changed in e4
                    // page.hideView(viewReference);

                    // DiagramViewPart view = (DiagramViewPart) viewReference.getView(false);
                    // // TODO this does not take multi view contexts into account yet
                    // idPartMapping.put(viewReference.getSecondaryId(), view);
                }
            }
            // register as a part listener
            page.addPartListener(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void partClosed(final IWorkbenchPart part) {
        if (part instanceof DiagramViewPart) {
            final DiagramViewPart diagramView = (DiagramViewPart) part;
            // unregister all view contexts on the view
            unregisterView(diagramView);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void partOpened(final IWorkbenchPart part) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void partActivated(final IWorkbenchPart part) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void partDeactivated(final IWorkbenchPart part) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void partBroughtToTop(final IWorkbenchPart part) {
        // do nothing
    }

}
