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
package de.cau.cs.kieler.klighd.piccolo.viewer;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;

import javax.swing.Timer;

import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.util.Pair;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;

import de.cau.cs.kieler.klighd.eclipse.internal.IDiagramOutlinePage;
import de.cau.cs.kieler.klighd.kgraph.KGraphPackage;
import de.cau.cs.kieler.klighd.kgraph.KNode;
import de.cau.cs.kieler.klighd.kgraph.KShapeLayout;
import de.cau.cs.kieler.klighd.krendering.Colors;
import de.cau.cs.kieler.klighd.piccolo.KlighdSWTGraphics;
import de.cau.cs.kieler.klighd.piccolo.internal.KlighdCanvas;
import de.cau.cs.kieler.klighd.piccolo.internal.KlighdSWTGraphicsEx;
import de.cau.cs.kieler.klighd.piccolo.internal.events.KlighdBasicInputEventHandler;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KNodeTopNode;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KlighdMainCamera;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KlighdPath;
import de.cau.cs.kieler.klighd.piccolo.internal.util.KlighdPaintContext;
import de.cau.cs.kieler.klighd.piccolo.internal.util.NodeUtil;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PDragSequenceEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PAffineTransform;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDimension;

/**
 * A content outline page for the Piccolo2D viewer.
 *
 * @author msp
 * @author uru
 * @author chsch
 */
public class PiccoloOutlinePage implements IDiagramOutlinePage {

    // Properties for the appearance of the outline rectangle.
    private static final float VIEWPORT_OUTLINE_LINEWIDTH = 1.5f;
    private static final int VIEWPORT_OUTLINE_CORNER_WIDTH = 5;
    private static final Colors VIEWPORT_OUTLINE_COLOR = Colors.BLUE_3;
    private static final int VIEWPORT_OUTLINE_ALPHA = 25;

    private static final Colors CLIP_OUTLINE_COLOR = Colors.WHITE;
    private static final int CLIP_OVERLAY_ALPHA = 200;

    // Delay of the timed outline rect update.
    private static final int OUTLINE_RECT_UPDATE_DELAY = 200;

    // Delay of the timed camera repaint.
    private static final int CAMERA_UPDATE_DELAY = 800;

    /** the canvas used for drawing. */
    private KlighdCanvas outlineCanvas;

    /** the graph layer to display. */
    private KNodeTopNode topNode;

    /** the observed {@link KNode}. */
    private KNode rootNode;

    /** the adapter listening to layout changes. */
    private Adapter nodeLayoutAdapter;

    /** the viewport outline rectangle. */
    private KlighdPath viewportOutlineRect;

    /** the clip outline overlay. */
    private KlighdPath clipOutlineOverlay;

    /** Indicates whether a drag action on the outline canvas is in progress. */
    private boolean isDragging = false;

    /** A timer to reduce the load during original diagram's size changes, e.g. due to node expansion. */
    private Timer cameraTimer;

    /** A timer to reduce the load during panning of the original canvas. */
    private Timer outlineRectTimer;

    /**
     * Configuration hook for customizing the viewport outline (rounded) rectangle's corner width &
     * height.
     * 
     * @return the desired value taken as corner width & corner height
     */
    protected int getViewportOutlineRectCornerWidth() {
        return VIEWPORT_OUTLINE_CORNER_WIDTH;
    }

    /**
     * Configuration hook for customizing the viewport outline rectangle's line width.
     *
     * @return the desired line width as <code>float<code>
     */
    protected float getViewportOutlineRectLineWidth() {
        return VIEWPORT_OUTLINE_LINEWIDTH;
    }

    /**
     * Configuration hook for customizing the viewport outline rectangle's fill color.
     *
     * @return a {@link Pair} of an {@link RGB} record determining the color and an {@link Integer}
     *         denoting the opacity (alpha value).
     */
    protected Pair<RGB, Integer> getViewportOutlineRectPaint() {
        return Pair.of(toRGB(VIEWPORT_OUTLINE_COLOR), VIEWPORT_OUTLINE_ALPHA);
    }

    /**
     * Configuration hook for customizing the diagram clip overlay's fill color.<br>
     * This overlay cover's the whole diagram except the {@link KNode} the diagram is clipped to.
     *
     * @return a {@link Pair} of an {@link RGB} record determining the color and an {@link Integer}
     *         denoting the opacity (alpha value).
     */
    protected Pair<RGB, Integer> getClipOutlineOverlayPaint() {
        return Pair.of(toRGB(CLIP_OUTLINE_COLOR), CLIP_OVERLAY_ALPHA);
    }

    /**
     * Convenience converter of {@link Colors} into {@link RGB} objects.
     *
     * @param color
     *            the {@link Colors} element to convert
     * @return the desired {@link RGB}
     */
    protected RGB toRGB(final Colors color) {
        if (color == null) {
            return null;
        } else {
            return new RGB(color.getRed(), color.getGreen(), color.getBlue());
        }
    }


    /**
     * Property Listener that listens to changes on the original canvas and triggers a redraw of the
     * outline rectangle.
     */
    private PropertyChangeListener mainCamListener = new PropertyChangeListener() {

        public void propertyChange(final PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(PCamera.PROPERTY_VIEW_TRANSFORM)
                    || evt.getPropertyName().equals(PNode.PROPERTY_FULL_BOUNDS)) {

                // only use the timer if we are not dragging
                if (isDragging) {
                    adjustOutlineRect();
                } else {
                    if (outlineRectTimer != null) {
                        outlineRectTimer.restart();
                    }
                }
            } else if (evt.getPropertyName().equals(PCamera.PROPERTY_LAYERS)
                    && evt.getNewValue() != null) {
                // listening to the layers is required for enabling/disabling the clip outline overlay
                //  immediately without any flickering

                if (Collections.emptyList().equals(evt.getNewValue())) {
                    // in this case the previous depicted IKNodeNode has been detached
                    //  a notification on the addition of the new one will follow shortly
                    return;
                }

                // set the clip overlay invisible if the current clip node is the KNodeTopNode,
                //  and visible otherwise, the clip node is a KNodeNode in those cases
                // switched from 'setVisible(...)' to 'setOccluded(...)' in order to prevent the
                //  calls of 'repaint()' / 'invalidatePaint()' that are hard-coded in 'setVisible(...)'
                clipOutlineOverlay.setOccluded(((KlighdMainCamera) evt.getSource())
                        .getDisplayedKNodeNode() instanceof KNodeTopNode);
            }
        }
    };

    /**
     * Named subclass of {@link KlighdCanvas} providing outline paint contexts.
     */
    private final class KlighdOutlineCanvas extends KlighdCanvas {

        KlighdOutlineCanvas(final Composite parent, final int style) {
            super(parent, style);
        }

        @Override
        public KlighdMainCamera createBasicSceneGraph() {
            return new KlighdMainCamera(new KlighdRoot()) {

                private static final long serialVersionUID = -3551541550083498908L;

                @Override
                public void repaintFromLayer(final PBounds viewBounds, final PLayer repaintedLayer) {
                    // by overriding the super implementation the propagation of repaint requests
                    //  from the main diagram is blocked, and the load due to updating the outline
                    //  is avoided
                    // in order to reflect diagram changes, e.g., due to style or rendering updates
                    //  an update of the outline camera is scheduled
                    PiccoloOutlinePage.this.cameraTimer.restart();
                }
            };
        }

        @Override
        protected KlighdPaintContext getPaintContext(final Graphics2D g2) {
            return KlighdPaintContext.createOutlinePaintContext((KlighdSWTGraphics) g2);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void createControl(final Composite parent) {
        final Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new FillLayout());

        outlineCanvas = new KlighdOutlineCanvas(container, SWT.NONE);
        outlineCanvas.setVisible(false);

        // since we don't rely on the picked node in OutlineDragHandler below
        //  we just set the camera non-pickable in order reduce performance waste
        outlineCanvas.getCamera().setPickable(false);


        // add a handler to the outline canvas to allow dragging
        outlineCanvas.addInputEventListener(new KlighdBasicInputEventHandler(new OutlineDragHandler()));


        // initialize the timer triggering the update of the outline camera's view transform
        cameraTimer = outlineCanvas.getRoot().createTimer(CAMERA_UPDATE_DELAY, new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                adjustCamera();
                // adjustCamera() invokes adjustOutlineRect() when it is done
                // adjustOutlineRect();
            }
        });
        cameraTimer.setRepeats(false);

        // initialize the timer triggering the request to update the outline rect
        outlineRectTimer = outlineCanvas.getRoot().createTimer(OUTLINE_RECT_UPDATE_DELAY,
                new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                // The SWTTimerQueue is holding a lock on itself during operation but for 
                // scheduling new events it needs the Display(Device) lock.
                // This Thread has the Display(Device) lock during creation but tries to access the
                // SWTTimer here, causing a wait on the SWTTimerQueue.

                // if (cameraTimer.isRunning()) {
                //     return;
                // }
                
                adjustOutlineRect();
            }
        });
        outlineRectTimer.setRepeats(false);
        outlineRectTimer.start();

        // create the actual outline view elements
        setContent(topNode);
    }

    /**
     * {@inheritDoc}
     */
    public Control getControl() {
        if (outlineCanvas == null || outlineCanvas.isDisposed()) {
            return null;
        } else {
            return outlineCanvas.getParent();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setFocus() {
        outlineCanvas.setFocus();
    }

    /** A flag for tracking the visibility state until the {@link #outlineCanvas} is created. */
    private boolean visible = false;

    /**
     * Sets the visibility property of the employed {@link KlighdCanvas}.
     *
     * @param isVisible the visibility state
     */
    public void setVisible(final boolean isVisible) {
        visible = isVisible;
        if (outlineCanvas != null) {
            adjustOutlineRect();
            outlineCanvas.setVisible(isVisible);
        }
    }

    /**
     * Update the content of the outline page.
     *
     * @param newTopNode
     *            the {@link KNodeTopNode} to be displayed
     */
    public void setContent(final KNodeTopNode newTopNode) {
        if (outlineCanvas == null) {
            this.topNode = newTopNode;
            return;
        }

        if (topNode != null && topNode != newTopNode) {
            // detach the propertyListener from the previously observed top node
            //  this' outlineCanvas' camera from the former topNode
            if (topNode.getCameraCount() == 0) {
                throw new IllegalStateException(
                        "The PLayer passed to the PiccoloOutlineView has "
                                + "to contain at least one camera.");
            }

            this.topNode.getDiagramMainCamera().removePropertyChangeListener(mainCamListener);
            this.outlineCanvas.getCamera().removeChild(this.topNode);

            if (nodeLayoutAdapter != null) {
                this.topNode.getViewModelElement().eAdapters().remove(nodeLayoutAdapter);
            }
        }

        this.topNode = newTopNode;

        final KlighdMainCamera diagramMainCamera = topNode.getDiagramMainCamera();

        // listen to view transformations
        diagramMainCamera.addPropertyChangeListener(mainCamListener);

        final KlighdMainCamera outlineCamera = outlineCanvas.getCamera();

        // install the outlineCanvas' camera into the given (new) topNode
        outlineCamera.addLayer(topNode);

        // initialize & configure the clip outline overlay rectangle
        //  its size is updated by the PropertyChangeListener on the BOUNDS of 'camera'
        clipOutlineOverlay = new KlighdPath() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paint(final KlighdPaintContext kpc) {

                final KlighdSWTGraphicsEx graphics = ((KlighdSWTGraphicsEx) kpc.getKlighdGraphics());

                final Rectangle2D globalClipBounds =
                        topNode.getDiagramMainCamera().getDisplayedKNodeNode().getGlobalFullBounds();

                final Region clip = new Region(graphics.getDevice());
                clip.add(toSWTRectangle(clipOutlineOverlay.getBoundsReference().getBounds()));

                final Rectangle diagramClip = toSWTRectangle(
                        outlineCanvas.getCamera().getViewTransformReference()
                                .transform(globalClipBounds, null).getBounds());

                clip.subtract(diagramClip);

                graphics.getGC().setTransform(null);
                graphics.getGC().setClipping(clip);

                super.paint(kpc);

                graphics.getGC().setClipping((Rectangle) null);
                clip.dispose();
            }

            private Rectangle toSWTRectangle(final java.awt.Rectangle rect) {
                return new Rectangle(rect.x, rect.y, rect.width, rect.height);
            }
        };

        final Pair<RGB, Integer> clipOutlineOverlayColoring = getClipOutlineOverlayPaint();
        clipOutlineOverlay.setStrokeColor((RGB) null);
        clipOutlineOverlay.setPaint(clipOutlineOverlayColoring.getFirst());
        clipOutlineOverlay.setPaintAlpha(clipOutlineOverlayColoring.getSecond());

        // switched from 'setVisible(...)' to 'setOccluded(...)' in order to prevent the
        //  calls of 'repaint()' / 'invalidatePaint()' that are hard-coded in 'setVisible(...)'
        clipOutlineOverlay.setOccluded(
                // implies to switch it off if displayedKNodeNode is a KNodeTopNode
                (diagramMainCamera.getDisplayedKNodeNode() instanceof KNodeTopNode));

        outlineCamera.addChild(clipOutlineOverlay);
        
        outlineCamera.addPropertyChangeListener(PNode.PROPERTY_BOUNDS, new PropertyChangeListener() {

            public void propertyChange(final PropertyChangeEvent evt) {
                clipOutlineOverlay.setPathToRectangle((Rectangle2D) evt.getNewValue());
            }
        });

        // initialize & configure the outline rectangle
        //  the concrete path data are set later on in #adjustOutlineRect()
        //  when the timer expires the first time (it's started in constructor)
        viewportOutlineRect = new KlighdPath();

        final Pair<RGB, Integer> viewportOutlineRectColoring = getViewportOutlineRectPaint();
        viewportOutlineRect.setLineWidth(getViewportOutlineRectLineWidth());
        viewportOutlineRect.setPaint(viewportOutlineRectColoring.getFirst());
        viewportOutlineRect.setPaintAlpha(viewportOutlineRectColoring.getSecond());

        // add the viewport outline rectangle after the clip outline overlay in order to let the
        //  viewport rectangle be always clearly visible without any opacity reduction
        //  cause by the clip outline overlay
        outlineCamera.addChild(viewportOutlineRect);

        // add listeners to layout changes and canvas resizing
        rootNode = topNode.getViewModelElement();
        nodeLayoutAdapter = new AdapterImpl() {

            @Override
            public void notifyChanged(final Notification notification) {
                super.notifyChanged(notification);

                if (notification.getNotifier() == rootNode) {
                    // in case anything is changed on the node, e.g. the node's shape layout
                    //  is removed or a new one is added by the simple update strategy
                    //  don't do anything!!
                    return;

                } else if (!(notification.getNotifier() instanceof KShapeLayout)) {
                    return;
                }

                final int featureId = notification.getFeatureID(KShapeLayout.class);
                if (featureId == KGraphPackage.KSHAPE_LAYOUT__WIDTH
                        || featureId == KGraphPackage.KSHAPE_LAYOUT__HEIGHT
                        || featureId == KGraphPackage.KSHAPE_LAYOUT__XPOS
                        || featureId == KGraphPackage.KSHAPE_LAYOUT__YPOS) {

                    if (cameraTimer != null) {
                        cameraTimer.restart();
                    }
                }
            }
        };

        rootNode.eAdapters().add(nodeLayoutAdapter);
        outlineCanvas.addControlListener(canvasResizeListener);

        adjustCamera();

        outlineCanvas.setVisible(visible);
    }

    /** the control listener reacting to canvas resizing. */
    private ControlListener canvasResizeListener = new ControlListener() {
        public void controlMoved(final ControlEvent e) {
            adjustCamera();
        }

        public void controlResized(final ControlEvent e) {
            adjustCamera();
        }
    };


    /** the minimal size of the view. */
    private static final float MIN_SIZE = 10.0f;

    /**
     * Adjust the view bounds of this' outlineCanvas' camera to the bounds of the currently tracked
     * graph layout. Note, that the bounds of the canvas are automatically updated of the size of
     * the outline page change, since the outlineCanvas is provided by this class via
     * {@link #getControl()}. The canvas in turn immediately updates the bounds of its camera, see
     * {@link edu.umd.cs.piccolox.swt.PSWTCanvas#setBounds(int, int, int, int)
     * PSWTCanvas#.setBounds(int, int, int, int)}. Thus we don't need to care about the canvas' and
     * camera's (full) bounds and can limit the modifications to the camera's view bounds (/view
     * transform).
     */
    private void adjustCamera() {
        if (rootNode == null) {
            // 'rootNode' is null if 'dispose()' was called while 'cameraTimer' is running
            return;
        }

        // always reveal the current shape layout - it may be exchanged over the diagram's life time
        final float scale = rootNode.getProperty(CoreOptions.SCALE_FACTOR).floatValue();

        final float width = Math.max(rootNode.getWidth() * scale, MIN_SIZE);
        final float height = Math.max(rootNode.getHeight() * scale, MIN_SIZE);
        outlineCanvas.getCamera().setViewBounds(
                new Rectangle2D.Double(rootNode.getXpos(), rootNode.getYpos(), width, height));

        adjustOutlineRect();
    }


    /**
     * Adjusts the displayed outline rectangle to the current view snippet.
     */
    private void adjustOutlineRect() {
        if (topNode == null) {
            // 'topNode' is null if 'dispose()' was called while 'cameraTimer' or 'outlineRectTimer'
            //  are running
            return;
        }

        final KlighdMainCamera originalCamera = topNode.getDiagramMainCamera();
        if (originalCamera == null) {
            return;
        }

        final PNode displayedNode = (PNode) originalCamera.getDisplayedKNodeNode();

        // get the new bounds
        final PBounds bounds = originalCamera.getViewBounds();
        final PAffineTransform localToParent = 
                NodeUtil.localToParent(displayedNode.getParent(), topNode);
        localToParent.preConcatenate(outlineCanvas.getCamera().getViewTransformReference());
        localToParent.transform(bounds, bounds);

        final int viewportOutlineRectCornerWidth = getViewportOutlineRectCornerWidth();

        viewportOutlineRect.setPathToRoundRectangle(
                (float) bounds.x, (float) bounds.y, (float) bounds.width, (float) bounds.height,
                viewportOutlineRectCornerWidth, viewportOutlineRectCornerWidth);

        // schedule a repaint
        outlineCanvas.getCamera().invalidatePaint();
    }


    private boolean disposed;

    /**
     * Getter.
     *
     * @return <code>true</code> if {@link #dispose()} has been called on this outline page
     */
    public boolean isDisposed() {
        return disposed;
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
        if (topNode != null) {
            this.outlineCanvas.getCamera().removeLayer(this.topNode);

            final PCamera originalCamera = topNode.getDiagramMainCamera();
            if (originalCamera != null) {
                originalCamera.removePropertyChangeListener(mainCamListener);
            }
        }
        topNode = null;
        mainCamListener = null;

        if (rootNode != null) {
            rootNode.eAdapters().remove(nodeLayoutAdapter);
        }
        rootNode = null;
        nodeLayoutAdapter = null;


        // the canvas, which is accessible by the platform via #getControl()
        //  is disposed separately by the platform, ...
        outlineCanvas = null;
        outlineRectTimer = null;

        // afterwards the canvasResizeListener can't be removed anymore (SWT exception)
        canvasResizeListener = null;

        disposed = true;
    }


    /**
     * {@inheritDoc}
     */
    public void setActionBars(final IActionBars actionBars) {
        // no action to register
    }


    /**
     * A drag handler that allows the user to drag the outline rectangle within the outline view and
     * propagates the movement to the actual editor part.<br>
     * <br>
     * <b>Note:</b> The {@link #outlineCanvas}' camera is set non-pickable in order to reduce
     * performance waste. However, due to {@link edu.umd.cs.piccolo.PInputManager#processInput()
     * PInputManager#processInput()} and {@link PCamera#pick(double, double, double)}
     * <code>event.getPickedNode()</code> is supposed to return the camera, which is absolutely fine :-).
     *
     * @author uru
     * @author chsch
     */
    private class OutlineDragHandler extends PDragSequenceEventHandler {

        /**
         * {@inheritDoc}
         */
        @Override
        protected void startDrag(final PInputEvent event) {
            isDragging = true;
            super.startDrag(event);
            final Point2D pos = event.getPosition();

            final KlighdMainCamera originalCamera = topNode.getDiagramMainCamera();

            // In clipped diagrams the accumulated 'translate' offset ((x,y) positions)
            //  of the displayed inode's parent pnodes (!) must be applied the determined
            //  view bounds in order to get the "actual" click position.
            // Note however that the displayed inode's translate (x,y) must not be taken
            //  into account because it is not contained in 'originalCamera.getViewBounds()'!
            // Since PCamera.paintCameraView() calls 'fullPaint(...)' on each displayed PLayer
            //  the transforms of those layers are applied on top of the camera's view transform!
            // For that reason the global translation of
            //  'originalCamera.getDisplayedLayer().getParent()' is calculated. This way an
            //  optional translation of the parent inode's child area is also respected!
            final Point2D clipOffset =
                    originalCamera.getDisplayedKNodeNode().getParent().getGlobalTranslation();

            final PBounds outlineRectBounds =
                    originalCamera.getViewBounds().moveBy(clipOffset.getX(), clipOffset.getY());

            // if the user clicks outside the outline rect,
            // center it on this point before dragging starts
            final boolean withinRect = outlineRectBounds.contains(pos);
            if (!withinRect) {
                // translate the camera by the delta between click
                // and current center point of the bounds
                final Point2D center = outlineRectBounds.getCenter2D();
                originalCamera.translateView(center.getX() - pos.getX(), center.getY() - pos.getY());
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void drag(final PInputEvent event) {
            super.drag(event);
            final PDimension delta = event.getDelta();
            topNode.getDiagramMainCamera().translateView(-delta.getWidth(), -delta.getHeight());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void endDrag(final PInputEvent event) {
            super.endDrag(event);
            isDragging = false;
        }
    }
}
