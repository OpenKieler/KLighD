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
package de.cau.cs.kieler.klighd.piccolo.viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.function.Supplier;

import javax.swing.Timer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.elk.core.math.KVector;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Composite;

import de.cau.cs.kieler.klighd.IViewer;
import de.cau.cs.kieler.klighd.IViewerProvider;
import de.cau.cs.kieler.klighd.Klighd;
import de.cau.cs.kieler.klighd.ViewChangeType;
import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.ZoomStyle;
import de.cau.cs.kieler.klighd.eclipse.IEclipseViewerProvider;
import de.cau.cs.kieler.klighd.eclipse.internal.IDiagramOutlinePage;
import de.cau.cs.kieler.klighd.eclipse.viewers.AbstractEclipseViewer;
import de.cau.cs.kieler.klighd.internal.ILayoutRecorder;
import de.cau.cs.kieler.klighd.internal.util.KlighdInternalProperties;
import de.cau.cs.kieler.klighd.kgraph.KGraphElement;
import de.cau.cs.kieler.klighd.kgraph.KNode;
import de.cau.cs.kieler.klighd.kgraph.util.KGraphUtil;
import de.cau.cs.kieler.klighd.piccolo.KlighdPiccolo;
import de.cau.cs.kieler.klighd.piccolo.internal.KlighdCanvas;
import de.cau.cs.kieler.klighd.piccolo.internal.controller.DiagramController;
import de.cau.cs.kieler.klighd.piccolo.internal.events.KlighdActionEventHandler;
import de.cau.cs.kieler.klighd.piccolo.internal.events.KlighdBasicInputEventHandler;
import de.cau.cs.kieler.klighd.piccolo.internal.events.KlighdMagnificationLensEventHandler;
import de.cau.cs.kieler.klighd.piccolo.internal.events.KlighdMouseWheelZoomEventHandler;
import de.cau.cs.kieler.klighd.piccolo.internal.events.KlighdPanEventHandler;
import de.cau.cs.kieler.klighd.piccolo.internal.events.KlighdSelectionEventHandler;
import de.cau.cs.kieler.klighd.piccolo.internal.events.KlighdSelectiveZoomEventHandler;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KlighdMainCamera;
import de.cau.cs.kieler.klighd.piccolo.internal.util.NodeUtil;
import de.cau.cs.kieler.klighd.util.KlighdProperties;
import de.cau.cs.kieler.klighd.viewers.ContextViewer;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * A viewer for Piccolo2D diagram contexts.
 *
 * @author mri
 * @author chsch
 */
public class PiccoloViewer extends AbstractEclipseViewer implements ILayoutRecorder,
        IDiagramOutlinePage.Provider {

    /** The identifier of this viewer type as specified in the extension. */
    public static final String ID = "de.cau.cs.kieler.klighd.piccolo.viewer.PiccoloViewer";

    /**
     * The required corresponding provider class.<br>
     * <br>
     * This class is not registered via the corresponding extension point within this bundle, but
     * employed and registered in 'de.cau.cs.kieler.klighd.test'. Instead
     * <code>PiccoloViewerUI</code> is contributed via a related {@link IViewerProvider} in
     * <code>de.cau.cs.kieler.klighd.ui</code>.
     */
    public static class Provider implements IEclipseViewerProvider {

        /**
         * {@inheritDoc}
         */
        public IViewer createViewer(final ContextViewer parentViewer, final Composite parent) {
            return new PiccoloViewer(parentViewer, parent);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public IViewer createViewer(ContextViewer parentViewer) {
            // TODO Not needed
            return null;
        }
    }

    private static final int VIEW_PORT_CHANGE_NOTIFY_DELAY = 250; // ms

    /** the canvas used for drawing. */
    private final KlighdCanvas canvas;
    /** the content outline page. */
    private PiccoloOutlinePage outlinePage;
    /** a supplier contributing the magnifying lens visibility state, may be <code>null</code>. */
    private final Supplier<Boolean> magnificationLensVisibleSupplier;

    /** the parent viewer. */
    private ContextViewer parentViewer;
    /** the graph controller. */
    private DiagramController controller;

    /**
     * Creates a Piccolo2D viewer with default style.
     *
     * @param parentViewer
     *            the parent {@link ContextViewer}
     * @param parent
     *            the parent composite
     */
    public PiccoloViewer(final ContextViewer parentViewer, final Composite parent) {
        this(parentViewer, parent, SWT.NONE);
    }

    /**
     * Creates a Piccolo2D viewer with given style.
     *
     * @param theParentViewer
     *            the parent {@link ContextViewer}
     * @param parent
     *            the parent composite
     * @param style
     *            the style attributes
     */
    public PiccoloViewer(final ContextViewer theParentViewer, final Composite parent,
            final int style) {
        if (parent.isDisposed()) {
            final String msg =
                    "KLighD (piccolo): A 'PiccoloViewer' has been tried to attach to a"
                            + "disposed 'Composite' widget.";
            throw new IllegalArgumentException(msg);
        }
        this.parentViewer = theParentViewer;
        this.canvas = new KlighdCanvas(parent, style,
                theParentViewer.getViewContext().getProperty(KlighdProperties.CANVAS_COLOR));

        final KlighdMainCamera camera = canvas.getCamera();
        this.magnificationLensVisibleSupplier = installEventHanders(camera);

        // add a tooltip element
        new PiccoloTooltip(canvas);

        // A timer being in charge of buffering and thus aggregating a bunch of single
        // view transform changes occurring closely after each other to a single view
        // change notification. Once the time elapsed without restarting it in the
        // meantime the provided {@link ActionListener#actionPerformed(ActionEvent)}
        // method is called.
        final Timer timer =
                camera.getRoot().createTimer(VIEW_PORT_CHANGE_NOTIFY_DELAY, new ActionListener() {

                    public void actionPerformed(final ActionEvent e) {
                        PiccoloViewer.this.notifyViewChangeListeners(ViewChangeType.VIEW_PORT,
                                null, camera.getViewBounds(), camera.getViewScale());
                    }
                });
        timer.setRepeats(false);

        // Install a listener sensitive to canvas size changes, i.e. view port changes,
        // being in charge of notifying the registered view change listeners of new view port
        // bounds.
        this.canvas.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(final ControlEvent e) {
                timer.restart();
            }
        });

        // Install a listener sensitive to view transform changes, i.e. view port changes,
        // being in charge of notifying the registered view change listeners of new view port
        // bounds.
        camera.addPropertyChangeListener(PCamera.PROPERTY_VIEW_TRANSFORM,
                new PropertyChangeListener() {

                    /**
                     * Called after each particular change of the camera's view transform.
                     * (Re-)Starts the timer in order to aggregate subsequent notifications, which
                     * occur during animations or manual zooming and panning.
                     */
                    public void propertyChange(final PropertyChangeEvent evt) {
                        timer.restart();
                    }
                });
    }

    /**
     * Installs the event handlers enabling the user interaction on the diagram main camera. May be
     * overridden. Overriding methods may or may not call this (super) implementation.
     * 
     * @param camera
     *            the diagram browser's main camera to which the event handlers need to be installed
     * @return a {@link Supplier} allowing to query the visibility of the diagram browser's
     *         magnifying glass, if available, may return <code>null</code>
     */
    protected Supplier<Boolean> installEventHanders(KlighdMainCamera camera) {
        final KlighdMagnificationLensEventHandler magnificationLensHandler =
                new KlighdMagnificationLensEventHandler(camera);

        // install the required event handlers, they rely on SWT event type codes
        // the order of registering them DOES MATTER,
        //  the first has lowest priority, the last highest
        // make sure those handlers properly execute 'event.setHandled(true);'
        //  in order to skip invoking the less priority handlers
        camera.addInputEventListener(new KlighdActionEventHandler(this));
        camera.addInputEventListener(magnificationLensHandler);
        camera.addInputEventListener(new KlighdMouseWheelZoomEventHandler());

        // caution: the selection handler currently marks most of the 'mouse release'
        //  events as handled; thus all handlers registered above will not get those events!
        // make sure to let all sub classes of 'PDragSequenceEventHandler' know about
        //  those release events to get their employed drag activity get terminated.
        camera.addInputEventListener(new KlighdSelectionEventHandler(this));
        camera.addInputEventListener(new KlighdBasicInputEventHandler(
                new KlighdPanEventHandler(canvas)));
        camera.addInputEventListener(new KlighdBasicInputEventHandler(
                new KlighdSelectiveZoomEventHandler(this)));

        return () -> magnificationLensHandler.isLensVisible();
    }

    /**
     * Provides the visibility state of the magnifying lens.<br>
     * This is evaluated by particular Klighd...EventHandlers that are inactive in case the
     * lens is shown.
     *
     * @return the visibility state of the magnifying lens.
     */
    public boolean isMagnificationLensVisible() {
        final Boolean isVisible = magnificationLensVisibleSupplier == null ? Boolean.FALSE
                : magnificationLensVisibleSupplier.get();
        return isVisible == null ? false : isVisible.booleanValue();
    }

    /**
     * {@inheritDoc}
     */
    public IDiagramOutlinePage getDiagramOutlinePage() {
        if (outlinePage == null || outlinePage.isDisposed()) {
            outlinePage = createDiagramOutlinePage();
            outlinePage.setContent(this.controller.getNode());
        }
        return outlinePage;
    }

    /**
     * Factory method for creation of a corresponding outline page.<br>
     * To be overridden by subclasses in order to inject specialized outline pages, see e.g.
     * PiccoloViewerProvider in <code>de.cau.cs.kieler.klighd.ui.internal.PiccoloViewer</code>.
     *
     * @return a {@link PiccoloOutlinePage}
     */
    protected PiccoloOutlinePage createDiagramOutlinePage() {
        return new PiccoloOutlinePage();
    }

    /**
     * {@inheritDoc}
     */
    public KlighdCanvas getControl() {
        return canvas;
    }

    /**
     * {@inheritDoc}
     */
    public ContextViewer getContextViewer() {
        return this.parentViewer;
    }

    /**
     * {@inheritDoc}
     */
    public ViewContext getViewContext() {
        return this.parentViewer.getViewContext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setModel(final KNode model, final boolean sync) {
        final ViewContext context = getViewContext();

        // create a controller for the graph
        controller = new DiagramController(model, canvas.getCamera(), sync, context);

        // update the outline page
        if (outlinePage != null && !outlinePage.isDisposed()) {
            outlinePage.setContent(controller.getNode());
        } else {
            this.outlinePage = null;
        }

    }

    /**
     * {@inheritDoc}
     */
    public void startRecording() {
        controller.startRecording();
    }

    /**
     * {@inheritDoc}
     */
    public void stopRecording(final int animationTime) {
        final ViewContext viewContext = this.getViewContext();
        final ZoomStyle zoomStyle;
        KGraphElement focusElement;
        final KVector previousPosition;

        // get the zoomStyle
        if (viewContext != null) {
            final ZoomStyle nzs = viewContext.getProperty(KlighdInternalProperties.NEXT_ZOOM_STYLE);
            if (nzs != null) {
                zoomStyle = nzs;

                // in case 'nzs' is unequal to ZOOM_TO_FOCUS, the NEXT_FOCUS_NODE is likely to be null,
                //  otherwise it may be null, or may denote to a KNode
                //  - both cases have to be handled properly!
                focusElement = viewContext.getProperty(KlighdInternalProperties.NEXT_FOCUS_ELEMENT);
                // If the focus element is not contained in the view model, ignore it.
                if (focusElement != null && !KGraphUtil.isDescendant(focusElement, viewContext.getViewModel())) {
                    focusElement = null;
                }

                viewContext.setProperty(KlighdInternalProperties.NEXT_ZOOM_STYLE, null);
                viewContext.setProperty(KlighdInternalProperties.NEXT_FOCUS_ELEMENT, null);
            } else {
                zoomStyle = viewContext.getZoomStyle();
                focusElement = null;
            }
            previousPosition = viewContext.getProperty(KlighdInternalProperties.PREVIOUS_POSITION);
        } else {
            zoomStyle = ZoomStyle.NONE;
            focusElement = null;
            previousPosition = null;
        }

        stopRecording(zoomStyle, focusElement, previousPosition, animationTime);
    }
    
    /**
     * {@inheritDoc}
     */
    public void stopRecording(final ZoomStyle zoomStyle, final KGraphElement focusElement, final int animationTime) {
        stopRecording(zoomStyle, focusElement, null, animationTime);
    }

    /**
     * {@inheritDoc}
     */
    public void stopRecording(final ZoomStyle zoomStyle, final KGraphElement focusElement,
            KVector previousPosition, final int animationTime) {
        controller.stopRecording(zoomStyle, focusElement, previousPosition, animationTime);
    }

    /**
     * Convenience method simplifying the notification call.
     *
     * @see #notifyViewChangeListeners(ViewChangeType, KGraphElement, java.awt.geom.Rectangle2D,
     *      float)
     *
     * @param type
     *            the corresponding {@link ViewChangeType}
     * @param affectedElement
     *            a potentially affect few element, e.g. a collapsed or expanded
     *            {@link de.cau.cs.kieler.klighd.kgraph.KNode KNode}, may be <code>null</code>
     */
    protected void notifyViewChangeListeners(final ViewChangeType type,
            final KGraphElement affectedElement) {
        final PCamera camera = canvas.getCamera();
        super.notifyViewChangeListeners(type, affectedElement, camera.getViewBounds(),
                camera.getViewScale());

    }

    /**
     * {@inheritDoc}
     */
    public boolean isDisplayed(final KGraphElement diagramElement, final boolean checkParents) {
        return controller.isDisplayed(diagramElement, checkParents);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isVisible(final KGraphElement diagramElement, final boolean checkContainment) {
        return controller.isVisible(diagramElement, checkContainment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void zoomToLevel(final float zoomLevel, final int duration) {
        controller.getZoomController().zoomToLevel(zoomLevel, duration);
    }

    /**
     * {@inheritDoc}
     */
    public void zoomToFocus(final KNode diagramElement, final int duration) {
        controller.getZoomController().zoomToFocus(diagramElement, duration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void zoom(final ZoomStyle style, final int duration) {
        controller.getZoomController().zoom(style, null, duration);
    }

    /**
     * {@inheritDoc}
     */
    public float getZoomLevel() {
        return (float) canvas.getCamera().getViewScale();
    }


    private static final String NO_DIAGRAM_ELEMENT_REPRESENTATION_ERROR_MSG =
            "KLighD: There is no figure represtation (PNode) of diagramElement XX!";

    private static final String NOT_IN_CURRENT_CLIP_ERROR_MSG =
            "KLighD: The figure representation of diagram element XX is not"
                    + " displayed in the current diagram clipping!";

    private static final String NOT_IN_CURRENT_CLIP_REVEAL_ERROR_MSG =
            NOT_IN_CURRENT_CLIP_ERROR_MSG + " It thus cannot be revealed.";

    /**
     * {@inheritDoc}
     */
    public void reveal(final KGraphElement diagramElement, final int duration) {
        final PNode node = getRepresentation(diagramElement);
        if (node == null) {
            Klighd.handle(
                    new Status(IStatus.WARNING, KlighdPiccolo.PLUGIN_ID, new String(
                            NO_DIAGRAM_ELEMENT_REPRESENTATION_ERROR_MSG).replace("XX",
                            diagramElement.toString())));
        } else {
            final PCamera camera = canvas.getCamera();
            final PBounds destBounds =
                    NodeUtil.clipRelativeGlobalBoundsOf(node, this.canvas.getCamera()
                            .getDisplayedKNodeNode());

            if (destBounds != null) {
                // move the camera so it includes the bounds of the node
                camera.animateViewToPanToBounds(destBounds, duration);

            } else {
                Klighd.handle(
                        new Status(IStatus.WARNING, KlighdPiccolo.PLUGIN_ID, new String(
                                NOT_IN_CURRENT_CLIP_REVEAL_ERROR_MSG).replace("XX",
                                diagramElement.toString())));
            }
        }
    }

    private static final String NOT_IN_CURRENT_CLIP_CENTER_ON_ERROR_MSG =
            NOT_IN_CURRENT_CLIP_ERROR_MSG + " Thus the diagram cannot be centered on that element.";

    /**
     * {@inheritDoc}
     */
    public void centerOn(final KGraphElement diagramElement, final int duration) {
        final PNode node = getRepresentation(diagramElement);
        if (node == null) {
            Klighd.handle(
                    new Status(IStatus.WARNING, KlighdPiccolo.PLUGIN_ID, new String(
                            NO_DIAGRAM_ELEMENT_REPRESENTATION_ERROR_MSG).replace("XX",
                            diagramElement.toString())));
        } else {
            final PCamera camera = canvas.getCamera();
            final PBounds destBounds =
                    NodeUtil.clipRelativeGlobalBoundsOf(node, this.canvas.getCamera()
                            .getDisplayedKNodeNode());

            if (destBounds != null) {
                // center the camera on the node
                camera.animateViewToCenterBounds(destBounds, false, duration);

            } else {
                Klighd.handle(
                        new Status(IStatus.WARNING, KlighdPiccolo.PLUGIN_ID,
                            NOT_IN_CURRENT_CLIP_CENTER_ON_ERROR_MSG.replace("XX",
                                    diagramElement.toString())));
            }
        }
    }

    private static final String NOT_IN_CURRENT_CLIP_TO_TOP_LEFT_ERROR_MSG =
            NOT_IN_CURRENT_CLIP_ERROR_MSG + " Thus the diagram cannot be panned to align"
                    + " that node with the visible area's top left corner.";

    /**
     * {@inheritDoc}
     */
    public void panToTopLeftCorner(final KNode diagramElement, final int duration) {
        final PNode node = getRepresentation(diagramElement);
        if (node == null) {
            Klighd.handle(
                    new Status(IStatus.WARNING, KlighdPiccolo.PLUGIN_ID,
                            NO_DIAGRAM_ELEMENT_REPRESENTATION_ERROR_MSG.replace("XX",
                            diagramElement.toString())));
        } else {
            final KlighdMainCamera camera = canvas.getCamera();

            final PBounds destBounds =
                    NodeUtil.clipRelativeGlobalBoundsOf(node, this.canvas.getCamera()
                            .getDisplayedKNodeNode());

            if (destBounds != null) {
                final double scale = camera.getViewTransformReference().getScale();

                // compose a new affine transform describing the desired camera view ...
                final AffineTransform t = AffineTransform.getScaleInstance(scale, scale);
                t.translate(-destBounds.x, -destBounds.y);

                // ... and trigger the change
                camera.animateViewToTransform(t, duration);
            } else {
                Klighd.handle(
                        new Status(IStatus.WARNING, KlighdPiccolo.PLUGIN_ID,
                                NOT_IN_CURRENT_CLIP_TO_TOP_LEFT_ERROR_MSG.replace("XX",
                                diagramElement.toString())));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void panDiagramToTopLeftCorner(final int duration) {
        final KlighdMainCamera camera = canvas.getCamera();

        final double scale = camera.getViewTransformReference().getScale();
        final AffineTransform t = AffineTransform.getScaleInstance(scale, scale);

        final PBounds destBounds =
                NodeUtil.clipRelativeGlobalBoundsOf(camera.getDisplayedKNodeNode(),
                        camera.getDisplayedKNodeNode());
        t.translate(-destBounds.x, -destBounds.y);
        camera.animateViewToTransform(t, duration);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isExpanded(final KNode diagramElement) {
        return controller.isExpanded(diagramElement);
    }

    /**
     * {@inheritDoc}
     */
    public void collapse(final KNode diagramElement) {
        controller.collapse(diagramElement);
        this.notifyViewChangeListeners(ViewChangeType.COLLAPSE, diagramElement);
    }

    /**
     * {@inheritDoc}
     */
    public void expand(final KNode diagramElement) {
        controller.expand(diagramElement);
        this.notifyViewChangeListeners(ViewChangeType.EXPAND, diagramElement);
    }

    /**
     * {@inheritDoc}
     */
    public void toggleExpansion(final KNode diagramElement) {
        controller.toggleExpansion(diagramElement);

        if (isExpanded(diagramElement)) {
            this.notifyViewChangeListeners(ViewChangeType.EXPAND, diagramElement);
        } else {
            this.notifyViewChangeListeners(ViewChangeType.COLLAPSE, diagramElement);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void hide(final KGraphElement diagramElement) {
        controller.hide(diagramElement);
        this.notifyViewChangeListeners(ViewChangeType.HIDE, diagramElement);
    }

    /**
     * {@inheritDoc}
     */
    public void show(final KGraphElement diagramElement) {
        controller.show(diagramElement);
        this.notifyViewChangeListeners(ViewChangeType.SHOW, diagramElement);
    }

    /**
     * {@inheritDoc}
     */
    public void clip(final KNode diagramElement) {
        this.clip(diagramElement, null, null);
    }

    /**
     * {@inheritDoc}
     */
    public void clip(final KNode diagramElement, final Boolean hideClipNodePorts, final Boolean hideClipNodeLabels) {
        controller.clip(diagramElement, hideClipNodePorts, hideClipNodeLabels);
        this.notifyViewChangeListeners(ViewChangeType.CLIP, diagramElement);
    }

    /**
     * {@inheritDoc}
     */
    public KNode getClip() {
        return controller.getClip();
    }

    /**
     * {@inheritDoc}
     */
    public void scale(final KNode diagramElement, final double factor) {
        diagramElement.setProperty(CoreOptions.SCALE_FACTOR, factor);

        if (isExpanded(diagramElement)) {
            controller.getZoomController().setFocusNode(diagramElement);
        }

        this.notifyViewChangeListeners(ViewChangeType.SCALE, diagramElement);
    }

    /**
     * {@inheritDoc}
     */
    public double getScale(final KNode diagramElement) {
        return diagramElement.getProperty(CoreOptions.SCALE_FACTOR).floatValue();
    }

    /**
     * Returns the Piccolo2D representation for the given diagram element.
     *
     * @param diagramElement
     *            the diagram element
     * @return the Piccolo2D representation
     */
    protected PNode getRepresentation(final KGraphElement diagramElement) {
        final PNode node = (PNode) controller.getRepresentation(diagramElement);
        if (node != null && node.getRoot() == canvas.getRoot()) {
            return node;
        }
        return null;
    }
}
