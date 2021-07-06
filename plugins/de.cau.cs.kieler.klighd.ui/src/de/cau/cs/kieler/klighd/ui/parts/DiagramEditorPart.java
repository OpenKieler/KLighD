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
package de.cau.cs.kieler.klighd.ui.parts;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.elk.core.LayoutConfigurator;
import org.eclipse.elk.core.util.WrappedException;
import org.eclipse.elk.graph.properties.IPropertyHolder;
import org.eclipse.elk.graph.properties.MapPropertyHolder;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import de.cau.cs.kieler.klighd.IDiagramWorkbenchPart;
import de.cau.cs.kieler.klighd.IViewer;
import de.cau.cs.kieler.klighd.Klighd;
import de.cau.cs.kieler.klighd.LightDiagramLayoutConfig;
import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.ZoomStyle;
import de.cau.cs.kieler.klighd.internal.IDiagramOutlinePage;
import de.cau.cs.kieler.klighd.internal.ILayoutConfigProvider;
import de.cau.cs.kieler.klighd.kgraph.KNode;
import de.cau.cs.kieler.klighd.krendering.SimpleUpdateStrategy;
import de.cau.cs.kieler.klighd.ui.KlighdUIPlugin;
import de.cau.cs.kieler.klighd.ui.internal.options.DiagramSideBar;
import de.cau.cs.kieler.klighd.ui.printing.PrintAction;
import de.cau.cs.kieler.klighd.ui.viewers.UiContextViewer;
import de.cau.cs.kieler.klighd.util.KlighdSynthesisProperties;
import de.cau.cs.kieler.klighd.viewers.ContextViewer;

/**
 * An editor which is able to display models in lightweight diagrams.
 *
 * @author msp
 * @author chsch
 * @author uru
 */
public class DiagramEditorPart extends EditorPart implements
        IDiagramWorkbenchPart.IDiagramEditorPart, ILayoutConfigProvider {

    /**
     * ActionBarContributor providing the print action for DiagramEditorParts.
     */
    public static class PrintActionContributor extends EditorActionBarContributor implements
            IPartListener {

        /** The print action. */
        private final PrintAction action = new PrintAction();
        private IDiagramWorkbenchPart currentEditor;

        /**
         * {@inheritDoc}
         */
        @Override
        public void init(final IActionBars bars) {
            super.init(bars);

            getPage().addPartListener(this);

            bars.setGlobalActionHandler(ActionFactory.PRINT.getId(), action);
            bars.updateActionBars();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void dispose() {
            this.getPage().removePartListener(this);

            super.dispose();
        }

        /**
         * Sets the active editor for the contributor.<br>
         * Updates the print action to reflect the the editor change.
         *
         * @param targetEditor
         *            the new active editor.
         */
        @Override
        public void setActiveEditor(final IEditorPart targetEditor) {
            super.setActiveEditor(targetEditor);

            currentEditor = (IDiagramWorkbenchPart) targetEditor;
            action.setViewer(currentEditor.getViewer());
        }

        /**
         * {@inheritDoc}
         */
        public void partClosed(final IWorkbenchPart part) {
            // the Eclipse runtime calls #setActiveEditor(<<newActiveEditor>>) before this method
            //  notifying the closing of the previous active editor
            // thus this code is only executed once the last instance of the editor part this
            //  contributor is attached to is closed, and other editor part is as the active one
            // however, this case is the most important one wrt. ensuring the proper garbage collection
            //  of all incorporated diagram resources
            if (part == currentEditor) {
                currentEditor = null;
                action.setViewer(null);
            }
        }

        /**
         * {@inheritDoc}
         */
        public void partOpened(final IWorkbenchPart part) {
        }

        /**
         * {@inheritDoc}
         */
        public void partDeactivated(final IWorkbenchPart part) {
        }

        /**
         * {@inheritDoc}
         */
        public void partBroughtToTop(final IWorkbenchPart part) {
        }

        /**
         * {@inheritDoc}
         */
        public void partActivated(final IWorkbenchPart part) {
        }
    }


    /** the resource set managed by this editor part. */
    private ResourceSet resourceSet;

    /** the model represented by this editor part. */
    private Object model;

    /** the viewer for this editor part. */
    private ContextViewer viewer;

    /** the dirty status of the editor. */
    private boolean dirty;

    /** the composite into which the sidebar is placed. */
    private Composite diagramComposite;

    /**
     * Listens to resize changes and triggers a re-layout of the diagram in case a zoom style is
     * defined.
     */
    private final ControlListener diagramAreaListener =
            DiagramWorkbenchParts.createDiagramAreaChangeListener(this);

    /**
     * Creates a diagram editor part.<br>
     * Besides, an {@link IResourceChangeListener} will be installed on the workspace, which is in
     * charge updating the diagram if the related editor input is changed.
     */
    public DiagramEditorPart() {
        this(true);
    }

    /**
     * Creates a diagram editor part.
     *
     * @param installResourceChangeListener
     *            if <code>true</code> an {@link IResourceChangeListener} will be installed on the
     *            workspace, which is in charge updating the diagram if the related editor input is
     *            changed.
     */
    protected DiagramEditorPart(final boolean installResourceChangeListener) {
        super();
        if (installResourceChangeListener) {
            resourceChangeListener = new KlighdResourceChangeListener();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);
        loadModel();
        registerResourceChangeListener();
    }

    private DiagramSideBar sideBar;

    /**
     * {@inheritDoc}
     */
    @Override
    public void createPartControl(final Composite parent) {
        // set the part name
        setPartName(getEditorInput().getName());

        // introduce a new Composite that accommodates the visualized content
        diagramComposite = new Composite(parent, SWT.NONE);
        diagramComposite.setLayout(new FillLayout());

        // create a context viewer
        viewer = new UiContextViewer(diagramComposite, this);

        // create a view context carrying all data required for building up the diagram
        final ViewContext viewContext =
                new ViewContext(this, model).configure(configureKlighdProperties());

        // create the options pane
        sideBar = DiagramSideBar.createSideBar(parent, diagramComposite, viewContext);

        if (viewContext != null) {
            viewer.setModel(viewContext);

            // do an initial update of the view context
            viewContext.update();

            if (requiresInitialLayout(viewContext)) {
                // In order to avoid flickering we set the viewer's control
                //  (the canvas) invisible, the canvas of a potentially created outline
                //  page is invisible after initialization, too.
                viewer.getControl().setVisible(false);

                // It is important to wait with the layout call until the #createPartControl
                //  method has finished and the widget toolkit has applied proper bounds
                //  to the parent composite via a Composite#layout call.
                // Otherwise a possible zoomToFit after the layout will fail since the
                //  view bounds are empty and no 'view area' to which to zoom can be
                //  determined. The async call here hopefully assures this.
                parent.getDisplay().asyncExec(new Runnable() {
                    public void run() {

                        // In case of the editor initialization at start of the tool (due
                        //  to foregoing tool exit without closing the editor)
                        // and some startup logic closing all "leftover" editor parts
                        //  the viewer's control (the canvas) may have been disposed in the
                        //  meantime of schedule this runnable and executing it.
                        // Thus check the disposition here, and for cautiousness after the
                        //  the layout request, too. (further Display activities may be scheduled
                        //  during the layout run while waiting for the layouters to finish).

                        if (viewer == null) {
                            // this is given if #dispose was called in the meantime
                            return;
                        }

                        final Control control = viewer.getControl();

                        if (control == null || control.isDisposed()) {
                            return;
                        }

                        new LightDiagramLayoutConfig(viewContext)
                            .animate(false)
                            .zoomStyle(getInitialZoomStyle())
                            .performLayout();

                        if (control.isDisposed()) {
                            return;
                        }

                        // now the editor's and outline page's canvas can be set visible
                        control.setVisible(true);
                        if (toBeFocussed) {
                            toBeFocussed = false;
                            control.setFocus();
                        }
                        if (currentOutlinePage != null) {
                            currentOutlinePage.setVisible(true);
                        }
                    }
                });
            } else if (currentOutlinePage != null) {
                currentOutlinePage.setVisible(true);
            }

            sideBar.updateOptions(diagramComposite, viewContext, false);

            // since no initial selection is set in the view context/context viewer implementation,
            // define some here by selection the root of the view model representing the diagram
            // canvas!
            viewer.resetSelectionTo(getViewer().getClip());
        } else {
            viewer.setModel("The selected file does not contain any supported model.", false);
        }

        // listen to any changes of the diagram area's size and re-zoom the diagram if
        // a zoom style is defined
        // note that it is enough to register the listener on the composite containing the sidebar
        // as this is resized simultaneously with the main window
        diagramComposite.addControlListener(diagramAreaListener);
    }

    /**
     * Tester that decides on the need for computing the diagram layout while opening the diagram.<br>
     * May be overridden by subclasses.
     *
     * @param viewContext
     *            provides context data that might be incorporated in the decision
     * @return true if the layout shall be (re-) computed while opening the diagram.
     */
    protected boolean requiresInitialLayout(final ViewContext viewContext) {
        final KNode viewModel = viewContext.getViewModel();
        return viewModel.getWidth() == 0 && viewModel.getHeight() == 0; 
    }

    /**
     * Provides the initial {@link ZoomStyle}, which is {@link ZoomStyle#NONE NONE} by default.
     * May be overridden by sub classes.
     *
     * @return the {@link ZoomStyle} being applied during initial layout application.
     */
    protected ZoomStyle getInitialZoomStyle() {
        return ZoomStyle.NONE;
    }

    /**
     * {@inheritDoc}
     */
    public LayoutConfigurator getLayoutConfig() {
        return this.sideBar != null ? this.sideBar.getLayoutConfig() : null;
    }

    /**
     * {@inheritDoc}
     */
    public void resetLayoutConfig() {
        resetLayoutConfig(true);
    }

    /**
     * {@inheritDoc}
     */
    public void resetLayoutConfig(final boolean doLayout) {
        if (this.sideBar != null) {
            this.sideBar.resetLayoutOptionsToDefaults(doLayout);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        unregisterResourceChangeListener();

        if (!diagramComposite.isDisposed()) {
            diagramComposite.removeControlListener(diagramAreaListener);
        }
        this.diagramComposite = null;

        if (this.sideBar != null) {
            this.sideBar.dispose();
        }
        this.sideBar = null;

        this.getSite().setSelectionProvider(null);
        this.viewer = null;

        this.currentOutlinePage = null;

        if (resourceSet != null) {
            for (final Resource r : resourceSet.getResources()) {
                r.unload();
            }
            resourceSet = null;
        }

        super.dispose();
    }

    private IDiagramOutlinePage currentOutlinePage = null;

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Object getAdapter(final Class type) {
        if (type == IContentOutlinePage.class && viewer instanceof IDiagramOutlinePage.Provider) {

            currentOutlinePage = ((IDiagramOutlinePage.Provider) viewer).getDiagramOutlinePage();
            if (currentOutlinePage != null) {
                // if the main canvas is visible we can assume the presence of a properly
                // initialized and arrange diagram (see #createPartControl() above),
                // otherwise leave the outline canvas invisible, thus...
                currentOutlinePage.setVisible(viewer.getControl().isVisible()
                        || !requiresInitialLayout(viewer.getViewContext()));
                return currentOutlinePage;
            }
        }
        return super.getAdapter(type);
    }

    /**
     * {@inheritDoc}
     */
    public String getPartId() {
        return "diagramEditor:" + getEditorInput().toString();
    }

    /**
     * {@inheritDoc}
     */
    public IViewer getViewer() {
        return viewer;
    }

    /**
     * {@inheritDoc}
     */
    public ViewContext getViewContext() {
        return viewer.getViewContext();
    }

    private boolean toBeFocussed = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFocus() {
        final Control c = viewer.getControl();
        if (c.isVisible()) {
            c.setFocus();
        } else {
            toBeFocussed = true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDirty() {
        return dirty;
    }

    /**
     * {@inheritDoc}
     */
    public void setDirty(final boolean dirty) {
        this.dirty = dirty;
        firePropertyChange(IEditorPart.PROP_DIRTY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doSave(final IProgressMonitor monitor) {
        try {

            // stop listening so the resource saving won't cause a model update
            unregisterResourceChangeListener();

            // save all opened resources
            for (final Resource resource : resourceSet.getResources()) {
                resource.save(Collections.emptyMap());
            }

            // restart listening to future resource updates
            registerResourceChangeListener();

            setDirty(false);
        } catch (final IOException exception) {
            throw new WrappedException(exception);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doSaveAs() {
        throw new UnsupportedOperationException();
    }

    /**
     * Load a model from the editor input. The result is put into {@link #model}.
     *
     * @throws PartInitException
     *             if loading the model fails
     *
     * @return the loaded model for convenience
     */
    protected Object loadModel() throws PartInitException {
        // get a URI or an input stream from the editor input
        URI uri = null;
        InputStream inputStream = null;
        final IEditorInput input = getEditorInput();
        if (input instanceof IFileEditorInput) {
            final IFile file = ((IFileEditorInput) input).getFile();
            uri = URI.createPlatformResourceURI(file.getFullPath().toString(), false);
        } else if (input instanceof IURIEditorInput) {
            final java.net.URI inputUri = ((IURIEditorInput) input).getURI();
            uri = URI.createURI(inputUri.toString());
        } else if (input instanceof IPathEditorInput) {
            final IPath path = ((IPathEditorInput) input).getPath();
            uri = URI.createFileURI(path.toString());
        } else if (input instanceof IStorageEditorInput) {
            try {
                final IStorage storage = ((IStorageEditorInput) input).getStorage();
                inputStream = storage.getContents();
            } catch (final CoreException exception) {
                throw new PartInitException("An error occurred while accessing the storage.",
                        exception);
            }
        } else {
            throw new PartInitException("The given editor input is not supported.");
        }

        final Resource resource;
        try {
            resourceSet = new ResourceSetImpl();
            configureResourceSet(resourceSet);
            if (inputStream != null) {
                // load a stream-based resource
                uri = URI.createFileURI("temp.xmi");
                resource = resourceSet.createResource(uri);
                resource.load(inputStream, Collections.EMPTY_MAP);
            } else {
                // load a URI-based resource
                resource = resourceSet.getResource(uri, true);
            }
        } catch (final IOException exception) {
            throw new PartInitException("An error occurred while loading the resource.", exception);
        } catch (final WrappedException exception) {
            throw new PartInitException("An error occurred while loading the resource.",
                    exception.getCause());
        }

        if (resource.getContents().isEmpty()) {
            throw new PartInitException("The resource is empty.");
        }
        // default behavior: get the first element in the resource
        model = resource.getContents().get(0);

        return model;
    }

    /**
     * Getter, provides access to the input model to subclasses.
     *
     * @return the model
     */
    protected Object getModel() {
        return model;
    }

    /**
     * Setter, allows to change the input model in subclass implementations.
     *
     * @param model
     *            the model to set
     */
    protected void setModel(final Object model) {
        this.model = model;
    }

    /**
     * Configures the given resource set. The default implementation does nothing.
     *
     * @param set
     *            the resource set to be configured.
     */
    protected void configureResourceSet(final ResourceSet set) {

    }

    /**
     * Returns a configuration for the KLighD view. Override this method to use a custom
     * configuration. The default implementation configures KLighD to use the simple update
     * strategy.
     *
     * @return KLighD configuration.
     */
    protected IPropertyHolder configureKlighdProperties() {
        final MapPropertyHolder props = new MapPropertyHolder();
        props.setProperty(KlighdSynthesisProperties.REQUESTED_UPDATE_STRATEGY,
                SimpleUpdateStrategy.ID);
        return props;
    }

    /* ---------------------------------- */
    /* resource change listener stuff */
    /* ---------------------------------- */

    /**
     * A resource change listener that updates the editor when resources are removed or changed.
     */
    private IResourceChangeListener resourceChangeListener;

    /**
     * Register the resource change listener.
     */
    private void registerResourceChangeListener() {
        if (resourceChangeListener != null) {
            ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener,
                    IResourceChangeEvent.POST_CHANGE);
        }
    }

    /**
     * Unregister the resource change listener.
     */
    private void unregisterResourceChangeListener() {
        if (resourceChangeListener != null) {
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
        }
    }

    /**
     * A resource change listener that updates the editor when resources are removed or changed.
     */
    private class KlighdResourceChangeListener implements IResourceChangeListener {

        public void resourceChanged(final IResourceChangeEvent event) {
            try {
                event.getDelta().accept(new DeltaVisitor());
            } catch (final CoreException exception) {
                Klighd.handle(exception, KlighdUIPlugin.PLUGIN_ID);
            }
        }

        /**
         * A dedicated {@link IResourceDeltaVisitor}, introduced as named class in order to clean up
         * the spaghetti code of anonymous interface implementations.
         */
        private class DeltaVisitor implements IResourceDeltaVisitor {

            public boolean visit(final IResourceDelta delta) {

                if (delta.getResource().getType() != IResource.FILE) {
                    // does that make sense - I'm not sure...
                    return true;
                }

                if (delta.getKind() == IResourceDelta.REMOVED) {
                    update(delta.getFullPath().toString(), true);

                } else if (delta.getKind() == IResourceDelta.CHANGED
                        && delta.getFlags() != IResourceDelta.MARKERS) {
                    update(delta.getFullPath().toString(), false);
                }
                // this the update is done on file level,
                // don't evaluate potential children of delta
                // thus ...
                return false;
            }

            private void update(final String fullPath, final boolean remove) {
                // checking whether the changed resource is used by
                // 'DiagramEditorPart.this' is implicitly done by the following
                // getResource(...) statement as the 'loadOnDemand' parameter is 'false'
                // thus only resources that are already loaded are returned
                // this won't work if the resource was loaded from an absolute path

                final Resource resource =
                        resourceSet.getResource(URI.createPlatformResourceURI(fullPath, true),
                                false);

                if (resource == null) {
                    return;
                }

                getSite().getShell().getDisplay().asyncExec(new Runnable() {
                    public void run() {
                        if (remove) {
                            // a required resource was removed, so close the editor
                            getSite().getPage().closeEditor(DiagramEditorPart.this, false);
                        } else {
                            reloadModel(resource);
                        }
                    }
                });
            }

            private void reloadModel(final Resource resource) {
                if (resource.isLoaded()) {
                    resource.unload();
                    try {
                        resource.load(Collections.EMPTY_MAP);

                        // default behavior: get the first element in the resource
                        model = resource.getContents().get(0);
                        viewer.getViewContext().update(model);

                    } catch (final IOException exception) {
                        final String msg =
                                this.getClass().getSimpleName() + " : Failed to reload "
                                        + resource.getURI().toString()
                                        + " after it has been changed.";

                        Klighd.handle(
                                new Status(IStatus.ERROR, KlighdUIPlugin.PLUGIN_ID, msg, exception));
                    }
                }
            }
        }
    }
}
