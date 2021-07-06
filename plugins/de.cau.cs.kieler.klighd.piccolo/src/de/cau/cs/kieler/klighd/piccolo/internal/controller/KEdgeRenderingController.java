/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 *
 * Copyright 2012 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 *
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klighd.piccolo.internal.controller;

import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.google.common.collect.Lists;

import de.cau.cs.kieler.klighd.Klighd;
import de.cau.cs.kieler.klighd.kgraph.KEdge;
import de.cau.cs.kieler.klighd.krendering.KCustomRendering;
import de.cau.cs.kieler.klighd.krendering.KPolyline;
import de.cau.cs.kieler.klighd.krendering.KRendering;
import de.cau.cs.kieler.klighd.krendering.KRenderingPackage;
import de.cau.cs.kieler.klighd.krendering.KRenderingRef;
import de.cau.cs.kieler.klighd.krendering.KRoundedBendsPolyline;
import de.cau.cs.kieler.klighd.krendering.KSpline;
import de.cau.cs.kieler.klighd.krendering.KStyle;
import de.cau.cs.kieler.klighd.microlayout.Bounds;
import de.cau.cs.kieler.klighd.piccolo.IKlighdNode.IKlighdFigureNode;
import de.cau.cs.kieler.klighd.piccolo.KlighdPiccolo;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KCustomConnectionFigureNode;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KEdgeNode;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KlighdDisposingLayer;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KlighdPath;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.NodeDisposeListener;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PPickPath;

/**
 * An {@link AbstractKGERenderingController} for {@link KEdge KEdges} generating the rendering
 * PNodes according to the related KRendering rendering description.<br>
 * <br>
 * In addition it takes care about creating and moving the junction point rendering that may be
 * attached to the edge's {@link KPolyline} rendering. This is done by creating a single
 * {@link PNode} representation. The upscaling to multiple concrete junction points is realized by
 * {@link PCamera cameras} that are added to the edge representation, one for each junction point,
 * and positioned accordingly. Those cameras point to the junction point representation figure. This
 * way a consistent update of the junction point figures in case of style changes is enabled. <br>
 * <br>
 * The junction point rendering is currently not implemented for edges with {@link KCustomRendering
 * custom renderings}.
 *
 * @author mri
 * @author chsch
 */
public class KEdgeRenderingController extends AbstractKGERenderingController<KEdge, KEdgeNode> {

    /**
     * Constructs a rendering controller for an edge.
     *
     * @param edge
     *            the Piccolo2D node representing an edge
     */
    public KEdgeRenderingController(final KEdgeNode edge) {
        super(edge.getViewModelElement(), edge);
    }

    /**
     * Error message if an edge is created with an illegal rendering.
     */
    private String illegalEdgeRenderingText() {
        return "KLighD: Illegal KRendering is attached to graph edge: "
            + getGraphElement()
            + ", must be a KPolyline, KRoundedBendsPolyline, KSpline, or KCustomRendering!";
    }
    
    @Override
    protected PNodeController<?> internalUpdateRendering() {
        final KEdgeNode repNode = getRepresentation();

        // evaluate the rendering data
        final KRendering currentRendering = getCurrentRendering();

        if (currentRendering == null) {
            return handleEdgeRendering(createDefaultRendering(), repNode);
        }

        final PNodeController<?> renderingNodeController;

        // the rendering of an edge has to be a KPolyline or a sub type of KPolyline except KPolygon,
        //  or a KCustomRendering providing a KCustomConnectionFigureNode
        switch (currentRendering.eClass().getClassifierID()) {
        case KRenderingPackage.KPOLYLINE:
        case KRenderingPackage.KROUNDED_BENDS_POLYLINE:
        case KRenderingPackage.KSPLINE:
            renderingNodeController = handleEdgeRendering((KPolyline) currentRendering, repNode);
            break;

        case KRenderingPackage.KCUSTOM_RENDERING:
            final KCustomRendering customRendering = (KCustomRendering) currentRendering;
            if (customRendering.getFigureObject()  == null
                    || customRendering.getFigureObject() instanceof KCustomConnectionFigureNode) {
                renderingNodeController = handleEdgeRendering(customRendering, repNode);
                break;
            } else {
                // FIXME why is the status manager used here, while below a runtime exception is thrown?
                // very inconsistent handling of error states!
                final String msg = "KLighD: KCustomRendering attached to edge "
                        + currentRendering.eContainer()
                        + " must provide a figure object of type KCustomConnectionFigureNode"
                        + " allowing to set the start, end, and bend points, or 'null' letting KLighD"
                        + " trying to load the class provided by its name.";
                Klighd.log(new Status(IStatus.ERROR, KlighdPiccolo.PLUGIN_ID, msg));
                
                return null;
            }

        case KRenderingPackage.KRENDERING_REF:
            final KRenderingRef renderingRef = (KRenderingRef) currentRendering;
            if (renderingRef.getRendering() == null) {
                return handleEdgeRendering(createDefaultRendering(), repNode);
            } else if (renderingRef.getRendering() instanceof KPolyline) {
                renderingNodeController = handleEdgeRendering(renderingRef, repNode);
            } else {
                throw new RuntimeException("KLighD: llegal KRendering is attached to graph edge.");
            }
            break;

        default:
            // hence, throw an exception if something different is provided
            throw new RuntimeException(illegalEdgeRenderingText());
        }

        return renderingNodeController;
    }

    /**
     * Creates the Piccolo2D node for a rendering of a {@code KEdge} inside a parent Piccolo2D node.<br>
     * <br>
     * The rendering has to be a {@code KPolyline} or reference one or the method fails.
     *
     * @param renderingOrRef
     *            the rendering
     * @param parent
     *            the parent Piccolo2D edge node
     * @return the {@link PNodeController} managing the Piccolo2D node that represents
     *         <code>rendering</code>
     */
    private PNodeController<?> handleEdgeRendering(final KRendering renderingOrRef, final KEdgeNode parent) {
        // create the rendering
        @SuppressWarnings("unchecked")
        final PNodeController<KlighdPath> controller = (PNodeController<KlighdPath>) createRendering(
                renderingOrRef, parent, new Bounds(1, 1), true);
        
        // De-reference the rendering if it is a reference.
        KRendering kRendering = (renderingOrRef instanceof KRenderingRef)
            ? ((KRenderingRef) renderingOrRef).getRendering()
            : renderingOrRef;
        if (!(kRendering instanceof KPolyline)) {
            throw new IllegalArgumentException(illegalEdgeRenderingText());
        }
        KPolyline rendering = (KPolyline) kRendering;

        if (rendering instanceof KSpline) {
            controller.getNode().setPathToSpline(parent.getBendPoints());
        } else if (rendering instanceof KRoundedBendsPolyline) {
            controller.getNode().setPathToRoundedBendPolyline(parent.getBendPoints(),
                    ((KRoundedBendsPolyline) rendering).getBendRadius());
        } else {
            controller.getNode().setPathToPolyline(parent.getBendPoints());
        }

        // add a listener on the parent's bend points
        addListener(KEdgeNode.PROPERTY_BEND_POINTS, parent, controller.getNode(),
                new PropertyChangeListener() {
                    public void propertyChange(final PropertyChangeEvent e) {
                        // let the parent KEdgeNode send a repaint request to the canvas in order to
                        //  get the (now dirty) area covered by the edge by now properly cleared
                        parent.invalidateFullBounds();

                        if (rendering instanceof KSpline) {
                            controller.getNode().setPathToSpline(parent.getBendPoints());
                        } else if (rendering instanceof KRoundedBendsPolyline) {
                            controller.getNode().setPathToRoundedBendPolyline(parent.getBendPoints(),
                                    ((KRoundedBendsPolyline) rendering).getBendRadius());
                        } else {
                            controller.getNode().setPathToPolyline(parent.getBendPoints());
                        }

                        parent.setBounds(controller.getNode().getBoundsReference());
                    }
                });


        final KRendering jpR = rendering.getJunctionPointRendering();

        if (jpR == null) {
            // if there is no junction point rendering determined, we're done!
            return controller;
        }


        /* ----------------------- */
        /* junction point handling */
        /* ----------------------- */

        final List<KStyle> propagatedStyles = determinePropagationStyles(rendering.getStyles(),
                Lists.<KStyle>newLinkedList(), false);

        // As explained in the class doc, the representation of multiple junction points is realized
        //  by means of cameras. Since those support only PLayers as 'viewed' figures, create one
        //  serving as container for the junction point figure:
        final KlighdDisposingLayer junctionParent = new KlighdDisposingLayer();

        // Create the junction point figure the usual way
        final IKlighdFigureNode junctionFigure = handleAreaAndPointPlacementRendering(
                jpR, propagatedStyles, junctionParent).getNode();

        // Create a layer accommodating the concrete camera instances ...
        final PLayer displayedJunctions = new KlighdDisposingLayer();

        // ... and add it to the edge's rendering figure (I think it could be also added to the
        //  parent 'edgeNode', but this is cleaner regarding the replacement/deletion of the current
        //  KRendering):
        controller.getNode().addChild(displayedJunctions);

        // initially add the junction points (required if the initial layout run is suppressed)
        updateJunctionPoints(parent, junctionFigure, displayedJunctions);

        // and finally add the listener that is keeping them up to date
        addListener(KEdgeNode.PROPERTY_JUNCTION_POINTS, parent, controller.getNode(),
                new PropertyChangeListener() {

                    public void propertyChange(final PropertyChangeEvent e) {
                        updateJunctionPoints(parent, junctionFigure, displayedJunctions);
                    }
                });

        parent.addPropertyChangeListener(NodeDisposeListener.DISPOSE, new PropertyChangeListener() {

            public void propertyChange(final PropertyChangeEvent evt) {
                junctionParent.firePropertyChange(NodeDisposeListener.DISPOSE_CODE,
                        evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        });

        return controller;
    }

    /**
     * Updates the displayed junction point figures based on the junction point coordinates in
     * <code>parent</code>. Puts the required amount of {@link PCamera} instances 'viewing'
     * <code>junctionFigure</code> into the <code>displayedJunctions</code> layer.
     *
     * @param parent
     *            the {@link KEdgeNode} providing the junction point coordinates
     * @param junctionFigure
     *            the single {@link PNode} representing the junction point {@link KRendering}
     * @param displayedJunctions
     *            the container {@link PLayer layer} comprising the concrete {@link PCamera cameras}
     */
    private void updateJunctionPoints(final KEdgeNode parent, final IKlighdFigureNode junctionFigure,
            final PLayer displayedJunctions) {

        final PNode junctionFigureAsPNode = junctionFigure.asPNode();

        // get the points from the parent (structural) node
        final Point2D[] newJunctionPoints = parent.getJunctionPoints();

        // determine the number of cameras to be added (removed)
        final int missingJuncts = newJunctionPoints.length - displayedJunctions.getChildrenCount();

        // since a camera can be pointed to layers take the junctionFigures parent layer
        //  (see handleEdgeRendering(final KPolyline rendering, final KEdgeNode parent)
        final PLayer junctionParent = (PLayer) junctionFigureAsPNode.getParent();

        // remove superfluous cameras in case the number of required juncts has decreased
        for (int i = 0; i < -missingJuncts; i++) {
            ((PCamera) displayedJunctions.removeChild(displayedJunctions.getChildrenCount() - 1))
                    .removeLayer(junctionParent);
        }

        // add further ones respectively
        for (int i = 0; i < missingJuncts; i++) {
            final PCamera cam = new JunctionPointCamera();

            // set the camera pickable as junction points shall be selectable
            // keep in mind: this might enable local panning of the junctionFigure ('s parent layer)!

            // this however is avoided by the specialized 'KlighdInputEvent' that offers
            //  the diagram top camera (usually the employed 'KlighdMainCamera') to the employed
            //  event handlers by default, rather than the inner helper cameras ("bottom camera").
            // see 'KlighdInputManager' for details
            cam.setPickable(true);

            // add the layer to be shown by the camera
            cam.addLayer(junctionParent);

            // set the camera's bounds to the junctionFigure's bounds adjusted by the
            //  junctionFigure's transform (which is done by getFullBoundsReference)
            // in order to align the junction point figure as defined in attached
            //  KPointPlacement data based on the given junction point coordinates
            cam.setBounds(junctionFigureAsPNode.getFullBoundsReference());

            // put the camera into the "camera container" -> invalidates the parent and the polyline
            displayedJunctions.addChild(cam);
        }

        @SuppressWarnings("unchecked")
        final List<PNode> cams = displayedJunctions.getChildrenReference();

        // update the position of the cameras to the given coordinates by modifying their transform
        //  (their local bounds need not to be touched)
        for (int i = 0; i < cams.size(); i++) {
            final Point2D p = newJunctionPoints[i];
            cams.get(i).setOffset(p.getX(), p.getY());
        }
    }

    /**
     * A specialized {@link PCamera} behaving exactly as {@link PCamera}. This named subclass just
     * exists for simplifying debugging by introducing the "JunctionPointCamera" name.
     *
     * @author chsch
     */
    public static final class JunctionPointCamera extends PCamera {

        private static final long serialVersionUID = -1724430297849001050L;

        private JunctionPointCamera() {
        }

        /**
         * {@inheritDoc}<br>
         * <br>
         * This specialization enables the selectability of junction points, i.e. the diverging or
         * converging edges. See
         * {@link de.cau.cs.kieler.klighd.piccolo.internal.events.KlighdSelectionEventHandler
         * #performSelection(edu.umd.cs.piccolo.event.PInputEvent)
         * KlighdSelectionEventHandler#performSelection(PInputEvent)} for details.
         */
        @Override
        public boolean fullPick(final PPickPath pickPath) {
            if (fullIntersects(pickPath.getPickBounds())) {
                pickPath.pushNode(this);
                pickPath.pushTransform(getTransformReference(true));

                final boolean thisPickable = getPickable() && pickPath.acceptsNode(this);

                if (thisPickable) {
                    return true;
                }

                pickPath.popTransform(getTransformReference(true));
                pickPath.popNode(this);
            }

            return false;
        }
    }


    /**
     * Creates the Piccolo2D node for a rendering of a {@code KEdge} inside a parent Piccolo2D node.<br>
     * <br>
     * The rendering has to be a {@code KPolyline} or the method fails.
     *
     * @param rendering
     *            the rendering
     * @param parent
     *            the parent Piccolo2D edge node
     * @return the {@link PNodeController} managing the Piccolo2D node that represents
     *         <code>rendering</code>
     */
    private PNodeController<?> handleEdgeRendering(final KCustomRendering rendering,
            final KEdgeNode parent) {

        // create the rendering
        @SuppressWarnings("unchecked")
        final PNodeController<KCustomConnectionFigureNode> controller =
                (PNodeController<KCustomConnectionFigureNode>) createRendering(
                        rendering, parent, new Bounds(1, 1));
        controller.getNode().setPoints(parent.getBendPoints());

        // add a listener on the parent's bend points
        addListener(KEdgeNode.PROPERTY_BEND_POINTS, parent, controller.getNode(),
                new PropertyChangeListener() {
                    public void propertyChange(final PropertyChangeEvent e) {
                        // let the parent KEdgeNode send a repaint request to the canvas in order to
                        //  get the (now dirty) area covered by the edge by now properly cleared
                        parent.repaint();

                        controller.getNode().setPoints(parent.getBendPoints());

                        parent.setBounds(controller.getNode().getBoundsReference());
                    }
                });

        return controller;
    }

    @Override
    protected void moveToFront() {
        this.getRepresentation().moveToFront();
    }
}
