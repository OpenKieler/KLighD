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
package de.cau.cs.kieler.klighd.microlayout;

import static de.cau.cs.kieler.klighd.krendering.KRenderingUtil.asAreaPlacementData;
import static de.cau.cs.kieler.klighd.krendering.KRenderingUtil.asPointPlacementData;
import static de.cau.cs.kieler.klighd.krendering.KRenderingUtil.getPlacementData;
import static de.cau.cs.kieler.klighd.krendering.KRenderingUtil.toNonNullBottomPosition;
import static de.cau.cs.kieler.klighd.krendering.KRenderingUtil.toNonNullLeftPosition;
import static de.cau.cs.kieler.klighd.krendering.KRenderingUtil.toNonNullRightPosition;
import static de.cau.cs.kieler.klighd.krendering.KRenderingUtil.toNonNullTopPosition;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.elk.core.math.KVector;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.util.Pair;
import org.eclipse.emf.ecore.EObject;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import de.cau.cs.kieler.klighd.Klighd;
import de.cau.cs.kieler.klighd.KlighdConstants;
import de.cau.cs.kieler.klighd.KlighdOptions;
import de.cau.cs.kieler.klighd.internal.util.KlighdInternalProperties;
import de.cau.cs.kieler.klighd.kgraph.KGraphElement;
import de.cau.cs.kieler.klighd.kgraph.KInsets;
import de.cau.cs.kieler.klighd.kgraph.KLabel;
import de.cau.cs.kieler.klighd.kgraph.KNode;
import de.cau.cs.kieler.klighd.kgraph.KShapeLayout;
import de.cau.cs.kieler.klighd.kgraph.PersistentEntry;
import de.cau.cs.kieler.klighd.krendering.KAreaPlacementData;
import de.cau.cs.kieler.klighd.krendering.KChildArea;
import de.cau.cs.kieler.klighd.krendering.KContainerRendering;
import de.cau.cs.kieler.klighd.krendering.KFontBold;
import de.cau.cs.kieler.klighd.krendering.KFontItalic;
import de.cau.cs.kieler.klighd.krendering.KFontName;
import de.cau.cs.kieler.klighd.krendering.KFontSize;
import de.cau.cs.kieler.klighd.krendering.KGridPlacement;
import de.cau.cs.kieler.klighd.krendering.KImage;
import de.cau.cs.kieler.klighd.krendering.KLeftPosition;
import de.cau.cs.kieler.klighd.krendering.KPlacement;
import de.cau.cs.kieler.klighd.krendering.KPlacementData;
import de.cau.cs.kieler.klighd.krendering.KPointPlacementData;
import de.cau.cs.kieler.klighd.krendering.KPolyline;
import de.cau.cs.kieler.klighd.krendering.KPosition;
import de.cau.cs.kieler.klighd.krendering.KRendering;
import de.cau.cs.kieler.klighd.krendering.KRenderingPackage;
import de.cau.cs.kieler.klighd.krendering.KRenderingRef;
import de.cau.cs.kieler.klighd.krendering.KRenderingUtil;
import de.cau.cs.kieler.klighd.krendering.KStyle;
import de.cau.cs.kieler.klighd.krendering.KText;
import de.cau.cs.kieler.klighd.krendering.KTextUtil;
import de.cau.cs.kieler.klighd.krendering.KTopPosition;
import de.cau.cs.kieler.klighd.krendering.KXPosition;
import de.cau.cs.kieler.klighd.krendering.KYPosition;
import de.cau.cs.kieler.klighd.krendering.util.KRenderingSwitch;
import de.cau.cs.kieler.klighd.util.Iterables2;
import de.cau.cs.kieler.klighd.util.KlighdProperties;
import de.cau.cs.kieler.klighd.util.ModelingUtil;

/**
 * A utility class for evaluating the micro layout of KRenderings.
 * 
 * @author chsch
 * @author sgu
 * @author akoc
 * 
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch 
 */
public class PlacementUtil {

    /**
     * Hidden default constructor.
     */
    protected PlacementUtil() {
    }

    // CHECKSTYLEOFF Visibility

    /**
     * A data holder class for points. This class and its fields are intentionally package
     * protected.
     * 
     * @author mri, chsch
     */
    public static class Point {

        /** the x-coordinate. */
        public float x;
        /** the y-coordinate. */
        public float y;

        /**
         * Constructs a point from the given coordinates.
         * 
         * @param x
         *            the x-coordinate
         * @param y
         *            the y-coordinate
         */
        public Point(final float x, final float y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Constructs a point according to the given SWT graphics point's coordinates.
         * 
         * @param point
         *            an AWT graphics point
         */
        public Point(final java.awt.Point point) {
            this.x = point.x;
            this.y = point.y;
        }

        /**
         * Sets x and y equal to x and y of the given point.
         * 
         * @param point
         *            the point to take the coordinates from
         */
        public void setLocation(final Point point) {
            this.x = point.x;
            this.y = point.y;
        }
        
        /**
         * Constructs a new {@link Point2D} with {@code x} and {@code y} equal to those of {@code this}
         * {@link Point}.
         * 
         * @return the {@link Point2D}
         */
        public Point2D.Float toPoint2D() {
            return new Point2D.Float(x, y);
        }
        
        /**
         * Constructs a new {@link KVector} with {@code x} and {@code y} equal to those of {@code this}
         * {@link Point}.
         * 
         * @return the new {@link KVector}.
         */
        public KVector toKVector() {
            return new KVector(x, y);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "(" + this.x + "," + this.y + ")";
        }
    }

    /**
     * Data container class that is convenient if a method shall return up to 3 results.
     * 
     * @author chsch
     * 
     * @param <A>
     *            The return type of result a;
     * @param <B>
     *            The return type of result b;
     * @param <C>
     *            The return type of result c;
     */
    public static class Triple<A, B, C> {

        private final A a;
        private final B b;
        private final C c;

        /**
         * Constructor.
         * 
         * @param theA
         *            The result a
         * @param theB
         *            The result b
         * @param theC
         *            The result c
         */
        public Triple(final A theA, final B theB, final C theC) {
            this.a = theA;
            this.b = theB;
            this.c = theC;
        }

        /**
         * @return the a
         */
        public A getA() {
            return this.a;
        }

        /**
         * @return the b
         */
        public B getB() {
            return this.b;
        }

        /**
         * @return the c
         */
        public C getC() {
            return this.c;
        }

    }

    // CHECKSTYLEON Visibility

    protected static final KRenderingPackage KRENDERING_PACKAGE = KRenderingPackage.eINSTANCE;
    protected static final float PT_TO_PX_FACTOR = KlighdConstants.DEFAULT_DISPLAY_DPI / 72f;

    /**
     * Evaluates a position inside given parent bounds.
     * 
     * @param position
     *            the position
     * @param parentBounds
     *            the parent bounds
     * @param topLeft
     *            in case position equals <code>null</code> assume a topLeft {@link KPosition},
     *            and a bottomRight {@link KPosition} otherwise
     * @return the evaluated position
     */
    public static Point2D.Float evaluateKPosition(final KPosition position,
            final Rectangle2D parentBounds, final boolean topLeft) {
        return evaluateKPosition(position, Bounds.of(parentBounds), topLeft).toPoint2D();
    }

    /**
     * Evaluates a position inside given parent bounds.
     * 
     * @param position
     *            the position
     * @param parentBounds
     *            the parent bounds
     * @param topLeft
     *            in case position equals <code>null</code> assume a topLeft {@link KPosition},
     *            and a bottomRight {@link KPosition} otherwise
     * @return the evaluated position
     */
    public static Point evaluateKPosition(final KPosition position,
            final Bounds parentBounds, final boolean topLeft) {
        final float width = parentBounds.getWidth();
        final float height = parentBounds.getHeight();
        
        final Point point = new Point(0, 0);
        final KXPosition<?> xPos = topLeft ? toNonNullLeftPosition(position.getX())
                : toNonNullRightPosition(position.getX());
        final KYPosition<?> yPos = topLeft ? toNonNullTopPosition(position.getY())
                : toNonNullBottomPosition(position.getY());
        
        if (xPos instanceof KLeftPosition) {
            point.x = xPos.getRelative() * width + xPos.getAbsolute();
        } else {
            point.x = width - xPos.getRelative() * width - xPos.getAbsolute();
        }
        
        if (yPos instanceof KTopPosition) {
            point.y = yPos.getRelative() * height + yPos.getAbsolute();
        } else {
            point.y = height - yPos.getRelative() * height - yPos.getAbsolute();
        }
        return point;
    }


    /**
     * Returns the bounds for a direct placement data in given parent bounds.
     * 
     * @param dpd
     *            the direct placement data
     * @param parentBounds
     *            the parent bounds
     * @return the bounds
     */
    public static Bounds evaluateAreaPlacement(final KAreaPlacementData dpd,
            final Rectangle2D parentBounds) {
        return evaluateAreaPlacement(dpd, Bounds.of(parentBounds));
    }


    /**
     * Returns the bounds for a direct placement data in given parent bounds.
     * 
     * @param dpd
     *            the direct placement data
     * @param parentBounds
     *            the parent bounds
     * @return the bounds
     */
    public static Bounds evaluateAreaPlacement(final KAreaPlacementData dpd,
            final Bounds parentBounds) {
        if (dpd == null) {
            return new Bounds(parentBounds);
        }
        return evaluateAreaPlacement(dpd.getTopLeft(), dpd.getBottomRight(), parentBounds);
    }
    
    /**
     * Returns the bounds for a direct placement data in given parent bounds.
     * 
     * @param topLeft
     *            the top left area position
     * @param topLeft
     *            the bottom right area position
     * @param parentBounds
     *            the parent bounds
     * @return the bounds
     */
    static Bounds evaluateAreaPlacement(final KPosition topLeft, final KPosition bottomRight,
            final Bounds parentBounds) {

        // determine the top-left
        final Point topLeftPoint;
        if (topLeft == null) {
            topLeftPoint = new Point(0, 0);
        } else {
            topLeftPoint = evaluateKPosition(topLeft, parentBounds, true);
        }

        // determine the bottom-right
        final Point bottomRightPoint;
        if (bottomRight == null) {
            bottomRightPoint = new Point(parentBounds.getWidth(), parentBounds.getHeight());
        } else {
            bottomRightPoint = evaluateKPosition(bottomRight, parentBounds, false);
        }

        return new Bounds(topLeftPoint.x, topLeftPoint.y, bottomRightPoint.x - topLeftPoint.x,
                bottomRightPoint.y - topLeftPoint.y);
    }

    /**
     * Returns the bounds for a point placement data in given parent bounds.
     * 
     * @param rendering
     *            the {@link KRendering} to be evaluated
     * @param ppd
     *            the {@link KPointPlacementData} to apply
     * @param parentBounds
     *            the parent bounds
     * @return the resulting bounds of the figure described by {@link KRendering} 
     */
    public static Bounds evaluatePointPlacement(final KRendering rendering,
            final KPointPlacementData ppd, final Rectangle2D parentBounds) {
        return evaluatePointPlacement(
                rendering, ppd, Bounds.of(parentBounds));
    }

    /**
     * Returns the bounds for a point placement data in given parent bounds.
     * 
     * @param rendering
     *            the {@link KRendering} to be evaluated
     * @param ppd
     *            the {@link KPointPlacementData} to apply
     * @param parentBounds
     *            the parent bounds
     * @return the resulting bounds of the figure described by {@link KRendering} 
     */
    public static Bounds evaluatePointPlacement(final KRendering rendering,
            final KPointPlacementData ppd, final Bounds parentBounds) {
        return evaluatePointPlacement(
                ppd, basicEstimateSize(rendering, Bounds.of(0, 0)), parentBounds);
    }
    
    /**
     * Returns the bounds for a point placement data in given parent bounds.
     * 
     * @param ppd
     *            the point placement data
     * @param ownBounds
     *            the size of the object to be placed
     * @param parentBounds
     *            the parent bounds
     * @return the bounds
     */
    public static Bounds evaluatePointPlacement(final KPointPlacementData ppd, final Bounds ownBounds,
            final Rectangle2D parentBounds) {
        return evaluatePointPlacement(ppd, ownBounds, Bounds.of(parentBounds));
    }

    /**
     * Returns the bounds for a point placement data in given parent bounds.
     * 
     * @param ppd
     *            the point placement data
     * @param ownBounds
     *            the size of the object to be placed
     * @param parentBounds
     *            the parent bounds
     * @return the bounds
     */
    public static Bounds evaluatePointPlacement(final KPointPlacementData ppd, final Bounds ownBounds,
            final Bounds parentBounds) {
        if (ppd == null) {
            return new Bounds(parentBounds.getWidth(), parentBounds.getHeight());
        }

        final float width = Math.max(ownBounds.getWidth(), ppd.getMinWidth());
        final float height = Math.max(ownBounds.getHeight(), ppd.getMinHeight());
        
        final KPosition ref = ppd.getReferencePoint();
        final Point refPoint;
        
        if (ref == null) {
            // if the reference point is missing, assume the top left corner as reference
            //  as requested by KIPRA-1785
            refPoint = new Point(0, 0);
        } else {
            refPoint = evaluateKPosition(ref, parentBounds, true);
        }

        final float x0, y0;

        switch (ppd.getHorizontalAlignment()) {
        case CENTER:
            x0 = refPoint.x - width / 2;
            break;
        case RIGHT:
            x0 = refPoint.x - width;
            break;
        default:
        case LEFT:
            x0 = refPoint.x;
        }
        
        switch (ppd.getVerticalAlignment()) {
        case BOTTOM:
            y0 = refPoint.y - height;
            break;
        case CENTER:
            y0 = refPoint.y - height / 2;
            break;
        default:
        case TOP:
            y0 = refPoint.y;
        }
        
        return Bounds.of(x0, y0, width, height);
    }

    /**
     * Returns the points for a polyline placement data in given parent bounds.
     * 
     * @param line
     *            the polyline with its points
     * @param parentBounds
     *            the parent bounds
     * @return the points
     */
    public static Point2D[] evaluatePolylinePlacement(final KPolyline line,
            final Bounds parentBounds) {
        if (line.getPoints() == null || line.getPoints().isEmpty()) {
            return new Point2D[] { new Point2D.Float(0, 0) };
        }

        // evaluate the points of the polyline inside the parent bounds
        Point2D[] points = new Point2D[line.getPoints().size()];
        int i = 0;
        for (KPosition point : line.getPoints()) {
            points[i++] = PlacementUtil.evaluateKPosition(point, parentBounds, true).toPoint2D();
        }

        return points;
    }


    /*--------------------------------------------------*/
    /*                                                  */
    /*  KPlacementData-based rendering size estimation  */
    /*                                                  */
    /*--------------------------------------------------*/


    /**
     * Returns the minimal size of a {@link KNode} based on the minimal size of contained
     * {@link KText KTexts} if present.
     * 
     * @param node
     *            the node to estimate the size for
     * @return the estimated size or (0, 0) if no text is contained.
     */
    public static Bounds estimateSize(final KNode node) {
        final KRendering nodeRendering = node.getData(KRendering.class);

        if (nodeRendering != null) {
            return estimateSize(nodeRendering, new Bounds(node.getWidth(), node.getHeight()));
        } else {
            return new Bounds(0, 0);
        }
    }

    /**
     * Returns the minimal size of a {@link KLabel} based on the minimal size of contained
     * {@link KText KTexts} if present.
     * 
     * @param label
     *            the label to estimate the size for
     * @return the estimated size or (0, 0) if no text is contained.
     */
    public static Bounds estimateSize(final KLabel label) {
        final KRendering labelRendering = label.getData(KRendering.class);

        if (labelRendering != null) {
            return estimateSize(labelRendering, new Bounds(label.getWidth(), label.getHeight()));
        } else {
            return estimateTextSize((KText) null, label.getText());
        }
    }

    /**
     * Estimates the minimal size of a KRendering.<br>
     * The the previous defined size is incorporated while resolving relative placement/size
     * constraints.
     * 
     * @param rendering
     *            the {@link KRendering} to be evaluated
     * @param givenBounds
     *            the size that is currently assigned to 'rendering's container or the related
     *            {@link KShapeLayout} respectively
     * @return the minimal size
     */
    public static Bounds estimateSize(final KRendering rendering, final Bounds givenBounds) {
        final KPlacementData placementData = getPlacementData(rendering);
        
        final Bounds bounds;
        final int placementDataType = placementData != null
                ? placementData.eClass().getClassifierID() : -1;                
        
        switch (placementDataType) {
        case KRenderingPackage.KAREA_PLACEMENT_DATA:
        // KGridPlacementData is a subclass of KAreaPlacementData, so accept them here, too
        case KRenderingPackage.KGRID_PLACEMENT_DATA:
            bounds = estimateAreaPlacedChildSize(rendering, (KAreaPlacementData) placementData,
                    givenBounds);
            break;

        case KRenderingPackage.KPOINT_PLACEMENT_DATA:
            bounds = estimatePointPlacedChildSize(rendering, (KPointPlacementData) placementData);
            break;

        default:
            bounds = basicEstimateSize(rendering, givenBounds);
        }

        if (rendering instanceof KImage) {
            final Bounds imageSize = estimateImageSize((KImage) rendering, bounds);
            return imageSize;
        } else {
            return bounds;
        }        
    }

    /**
     * Estimates the pure minimal size of a {@link KRendering} without evaluating its
     * {@link KPlacementData} w.r.t. minimal size constraints.<br>
     * The previous determined size is incorporated while resolving relative placement/size
     * constraints.
     * 
     * @param rendering
     *            the {@link KRendering} to be evaluated
     * @param givenBounds
     *            the size that is currently assigned to 'rendering's container or the related
     *            {@link KShapeLayout} respectively
     * @return the minimal size
     */
    public static Bounds basicEstimateSize(final KRendering rendering, final Bounds givenBounds) {
        // determine the type of the rendering
        final int id = KRENDERING_PACKAGE.getKText().isInstance(rendering) ? KRenderingPackage.KTEXT
            : KRENDERING_PACKAGE.getKContainerRendering().isInstance(rendering) 
                ? KRenderingPackage.KCONTAINER_RENDERING
                    : KRENDERING_PACKAGE.getKChildArea().isInstance(rendering) 
                        ? KRenderingPackage.KCHILD_AREA
                            : KRENDERING_PACKAGE.getKRenderingRef().isInstance(rendering)
                                ? KRenderingPackage.KRENDERING_REF
                                    : KRenderingPackage.KRENDERING;

        // calculate size based on type
        switch (id) {
        case KRenderingPackage.KTEXT:
            // for a text rendering just calculate the bounds of the text
            // and put the minimal size into 'minBounds'
            return estimateTextSize((KText) rendering);

        case KRenderingPackage.KCHILD_AREA:
            // KChildAreas do not cover any space that is not gathered by the (macro) layout
            return new Bounds(0, 0);

        case KRenderingPackage.KRENDERING_REF:
            // calculate the size of the referenced Rendering instead
            return basicEstimateSize(((KRenderingRef) rendering).getRendering(), givenBounds);

        case KRenderingPackage.KCONTAINER_RENDERING:

            final KContainerRendering container = (KContainerRendering) rendering;
            final int placementId = container.getChildPlacement() != null
                    ? container.getChildPlacement().eClass().getClassifierID() : -1;

            switch (placementId) {
            case KRenderingPackage.KGRID_PLACEMENT:
                // in case of a GridPlacement calculate the number of columns and rows of the grid
                // to the bounds of an inner rendering
                return GridPlacementUtil.estimateGridSize(container, givenBounds);

            default:
                // in case of no placement definition calculate the size of each child rendering and
                // find the biggest rendering in width and height
                final Bounds maxSize = new Bounds(givenBounds);

                for (final KRendering child : container.getChildren()) {
                    final Bounds childSize = estimateSize(child, givenBounds);
                    Bounds.max(maxSize, childSize);
                }

                if (container instanceof KPolyline) {
                    final Bounds pb = evaluatePolylineBounds((KPolyline) rendering, maxSize);
                    Bounds.max(maxSize, pb);
                }
                return maxSize;
            }
        default:
            // there is something else than a text box or a container, e.g. a KPolyline
            // just take the given 'defBounds' as minimal bounds as we consider only KTexts
            // as "atomic minimal bounds provider"
            return givenBounds;
        }
    }

    /**
     * Computes the minimal bounds of an {@link KImage}, esp. in case a clip shape is configured.<br>
     * This method basically applies the area/point placement data of the clip shape to the already
     * determined size of the KImage in order to avoid the extension of the, e.g., node to the
     * complete bounds of the image.<br>
     * <br>
     * <b>Note</b> that the general assumption of the size estimation assuming the bounds of a child
     * figure being completely encompassed by the bounds of the parent figure is not applied here!
     * Instead, the size estimation of images is supposed to cope with negative values of, at least,
     * the absolute positioning components.<br>
     * <br>
     * If no clip shape is defined it simply returns <code>imageSize</code>.
     * 
     * @param image
     *            the {@link KImage}
     * @param imageSize
     *            the pre-calculated size of the image itself
     * @return the minimal size
     */
    public static Bounds estimateImageSize(final KImage image, final Bounds imageSize) {
        final KRendering clipShape = image.getClipShape();

        if (clipShape == null) {
            return imageSize;

        } else {
            final KPlacementData pd = image.getPlacementData();
            final int pdType = pd == null ? 0 : pd.eClass().getClassifierID();
            
            if (pdType == KRenderingPackage.KPOINT_PLACEMENT_DATA) {
                // if the image is placed by means of point placement data
                
                final KPlacementData cpd = clipShape.getPlacementData();
                final int cpdType = cpd == null ? 0 : cpd.eClass().getClassifierID();
                
                if (cpdType == KRenderingPackage.KPOINT_PLACEMENT_DATA) {
                    return estimatePointPlacedChildSize(clipShape, (KPointPlacementData) cpd);
                    
                } else if (cpdType == KRenderingPackage.KAREA_PLACEMENT_DATA) {
                    // if the image clip is determined by means of point placement data
                    //  just take the (minimal) size specified in image's placement data  
                    //  to calculate the bounds of the clip shape
                    final KPointPlacementData ppd = (KPointPlacementData) pd;
                    return calculateBounds(null, Bounds.of(ppd.getMinWidth(), ppd.getMinHeight()),
                            null, clipShape);
                } else {
                    return imageSize;
                }

            } else if (pdType ==  KRenderingPackage.KAREA_PLACEMENT_DATA) {
                // if the image is placed by means of point placement data ...
                // NOTE that RELATIVE POSITION COMPONENTS ARE NOT CONSIDERED HERE (yet)
                //  as relative positioning of clipped images with unknown node size
                //  seems to be rather unlikely to me!

                final KAreaPlacementData apd = (KAreaPlacementData) pd;
                final KPosition tl = apd.getTopLeft();
                final KPosition br = apd.getBottomRight();

                // ... a reasonable size estimation can be performed if both
                //  top left and bottom right positions refer the same side,
                //  i.e. both refer to 'left'/'top' or both refer to 'right'/'bottom'.

                final boolean widthModEnabled =
                        tl != null && br != null && tl.getX().eClass() == br.getX().eClass();
                final boolean heightModEnabled =
                        tl != null && br != null && tl.getY().eClass() == br.getY().eClass();

                if (widthModEnabled) {
                    // if that is given adjust the assumed image width by subtracting the
                    //  potentially negative value of ...
                    if (tl.getX().eClass().getClassifierID() == KRenderingPackage.KLEFT_POSITION) {
                        // ... topLeft's absolute value ...
                        imageSize.setWidth(imageSize.getWidth() - tl.getX().getAbsolute());
                        imageSize.setX(0);
                    } else {
                        // ... bottomRight's absolute value ...
                        imageSize.setWidth(imageSize.getWidth() - br.getX().getAbsolute());
                        imageSize.setX(0);
                    }
                    // ... since in case of left/left (or right/right) positions just bottomRight's
                    //  absolute component (or topLeft's absolute component respectively)
                    //  is treated as the width of the image
                }

                if (heightModEnabled) {
                    // see horizontal case above
                    if (tl.getY().eClass().getClassifierID() == KRenderingPackage.KTOP_POSITION) {
                        imageSize.setHeight(imageSize.getHeight() - tl.getY().getAbsolute());
                        imageSize.setY(0);
                    } else {
                        imageSize.setHeight(imageSize.getHeight() - br.getY().getAbsolute());
                        imageSize.setY(0);
                    }
                }

                return calculateBounds(null, imageSize, null, clipShape);

            } else {
                return imageSize;
            }
        }
    }
    
    /**
     * Returns the font data used to render the given label. If the label has a {@link KText} rendering
     * attached, its font information are used. Otherwise, defaults defined in {@link KlighdConstants}
     * are used.
     * 
     * @param kLabel the label whose font information to retrieve.
     * @return font information for the given label.
     */
    public static Font fontFor(final KLabel kLabel) {
        return fontFor(kLabel, false);
    }

    /**
     * Returns the font data used to render the given label. If the label has a {@link KText}
     * rendering attached, its font information are used. Otherwise, defaults defined in
     * {@link KlighdConstants} are used.
     * 
     * @param kLabel
     *            the label whose font information to retrieve.
     * @param setFontLayoutOptions
     *            if <code>true</code> the layout options {@link LayoutOptions#FONT_NAME} and
     *            {@link LayoutOptions#FONT_SIZE} are set/updated on <code>kLabel</code>'s layout
     *            data as expected by Graphviz (dot) for properly sizing <i>edge</i> labels
     * @return font information for the given label.
     */
    public static Font fontFor(final KLabel kLabel, final boolean setFontLayoutOptions) {
        final KRendering rootRendering = Iterators.getNext(
                ModelingUtil.eAllContentsOfType2(kLabel, KRendering.class),
                null);
        
        final Iterator<KText> kTexts = Iterators.filter(
                KRenderingUtil.selfAndAllChildren(rootRendering), KText.class);
        final KText kText = Iterators.getNext(kTexts, null);
        
        // Check if we have found a KText thingy
        if (setFontLayoutOptions) {
            return fontFor(kText, kLabel);
        } else {
            return fontFor(kText, null);
        }
    }
    
    /**
     * Returns the font data defined by the given rendering. Missing font information are substituted by
     * defaults defined in {@link KlighdConstants}.
     * 
     * @param kText the rendering whose font information to retrieve.
     * @return font information for the given rendering.
     */
    public static Font fontFor(final KText kText) {
        return fontFor(kText, null);
    }
    
    /**
     * Returns the font data defined by the given rendering. Missing font information are
     * substituted by defaults defined in {@link KlighdConstants}.
     * 
     * @param kText
     *            the rendering whose font information to retrieve.
     * @param graphElement
     *            if unequal to <code>null</code> the layout options {@link LayoutOptions#FONT_NAME}
     *            and {@link LayoutOptions#FONT_SIZE} will be set/updated to the determined font
     *            name & size values
     * @return font information for the given rendering.
     */
    private static Font fontFor(final KText kText, final KGraphElement graphElement) {
        KFontName kFontName = null;
        KFontSize kFontSize = null;
        KFontBold kFontBold = null;
        KFontItalic kFontItalic = null;
        
        if (kText != null) {
            // the following lines look for font styles propagated from parents
            //  TODO also make allowance of styles propagated via KRenderingRefs
            final List<KStyle> styles = Lists.newLinkedList(kText.getStyles());            
            for (final KRendering k : Iterables2.toIterable(Iterators.filter(
                    ModelingUtil.eAllContainers(kText), KRendering.class))) {
                Iterables.addAll(styles, Iterables.filter(k.getStyles(), FILTER));
            }
            
            kFontName = Iterables.getLast(Iterables.filter(styles, KFontName.class), null);
            kFontSize = Iterables.getLast(Iterables.filter(styles, KFontSize.class), null);
            kFontBold = Iterables.getLast(Iterables.filter(styles, KFontBold.class), null);
            kFontItalic = Iterables.getLast(Iterables.filter(styles, KFontItalic.class), null);
        }

        final String fontName = kFontName != null
                ? kFontName.getName()
                : KlighdConstants.DEFAULT_FONT_NAME;

        final int fontSize = kFontSize != null
                ? kFontSize.getSize()
                : KlighdConstants.DEFAULT_FONT_SIZE;

        int fontStyle = kFontBold != null && kFontBold.isBold()
                ? KlighdConstants.DEFAULT_FONT_STYLE | Font.BOLD
                : KlighdConstants.DEFAULT_FONT_STYLE;

        fontStyle = kFontItalic != null && kFontItalic.isItalic()
                ? fontStyle | Font.ITALIC : fontStyle;

        if (graphElement != null) {
            // setting the font name and size layout options is expected by the Graphviz layouter
            //  as it does not rely on given sizes but computes it on its own based on the given
            //  label text and font configuration 
            graphElement.setProperty(CoreOptions.FONT_NAME, fontName);
            graphElement.setProperty(CoreOptions.FONT_SIZE, fontSize);
        }

        return new Font(fontName, fontSize, fontStyle);
    }

    private static final Predicate<KStyle> FILTER = new Predicate<KStyle>() {
        public boolean apply(final KStyle style) {
            return style.isPropagateToChildren();
        }
    };

    /**
     * Returns the minimal bounds for a KText.
     * 
     * @param kText
     *            the KText containing the text string whose size is to be estimated.
     * @return the minimal bounds for the {@link KText}
     */
    public static Bounds estimateTextSize(final KText kText) {
        if (kText.getText() == null) {
            // If the KText has a text override property set on it, use that
            if (kText.hasProperty(KlighdOptions.LABELS_TEXT_OVERRIDE)) {
                return estimateTextSize(kText,
                        kText.getProperty(KlighdOptions.LABELS_TEXT_OVERRIDE));
            }
            
            // We have nothing to go on here... Try to find the KText's parent label, if any
            EObject o = kText.eContainer();
            while (o instanceof KRendering) {
                o = o.eContainer();
            }
            
            if (o instanceof KLabel) {
                return estimateTextSize(kText, ((KLabel) o).getText());
            } else {
                // Give up
                return estimateTextSize(kText, null);
            }
        } else {
            return estimateTextSize(kText, kText.getText());
        }
    }
    
    /**
     * Returns the minimal bounds for a string based on configurations of a {@link KText}. The
     * string is handed over separately in order to allow the text size estimation for
     * {@link KLabel KLabels}, whose text string is given outside of the {@link KText} rendering.
     * 
     * @param kText
     *            the KText providing font configurations like font name, size, and style; maybe
     *            <code>null</code>
     * @param text
     *            the actual text string whose size is to be estimated; maybe <code>null</code>
     * @return the minimal bounds for the string
     */
    public static Bounds estimateTextSize(final KText kText, final String text) {
        final Bounds testSize = getTestingTextSize(kText);

        if (testSize != null) {
            return testSize;
        } else {
            // XXX
            return estimateTextSize(fontFor(kText, null), text);
        }
    }
    
    /**
     * Returns the testing size data if such are assigned to the given {@link KText}, or
     * <code>null</code>.
     * 
     * @param kText
     *            the KText potentially carrying testing width and height data, maybe
     *            <code>null</code>
     * @return the testing bounds assigned to {@code kText}, or <code>null</code> if {@code kText}
     *         is <code>null</code> or if no testing width or testing height is assigned.
     */
    public static Bounds getTestingTextSize(final KText kText) {
        if (kText != null) {
            // If the KText has already estimated bounds, use them.
            if (kText.hasProperty(KlighdProperties.CALCULATED_TEXT_BOUNDS)) {
                // The bounds need to be copied, because otherwise they would be changed by the caller of this method,
                // specifically the GridPlacementUtil.estimateGridSize::501 changes its cellSize (which is this object).
                return new Bounds(kText.getProperty(KlighdProperties.CALCULATED_TEXT_BOUNDS));
            }
            
            // special handling required for the regression tests
            // I don't trust in the different SWT implementations to
            //  provide the same size of a text on different platforms
            //  so given data are to be used
            final PersistentEntry testHeight = Iterables.find(
                    kText.getPersistentEntries(), KlighdInternalProperties.PRED_TESTING_HEIGHT, null);

            final PersistentEntry testWidth = Iterables.find(
                    kText.getPersistentEntries(), KlighdInternalProperties.PRED_TESTING_WIDTH, null);
            
            if (testHeight != null || testWidth != null) {
                final float height = testHeight != null ? Float.parseFloat(testHeight.getValue()) : 0f;
                final float width = testWidth != null ? Float.parseFloat(testWidth.getValue()) : 0f;

                if (height != 0f || width != 0f) {
                    return new Bounds(width, height);
                }
            }
        }
        return null;
    }
    
    protected static Point displayScale = null;

    private static BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    private static Graphics2D fmg = bi.createGraphics();

    /**
     * Returns the minimal bounds required by a drawing of the string <code>text</code> while
     * respecting the given <code>font</code>. While being in an Eclipse context and having a
     * {@link Display}, the method uses SWT's {@link GC} to perform estimations. Otherwise it falls
     * back to AWT's {@link FontMetrics}.
     * 
     * @param font
     *            an SWT {@link FontData} record describing font name, size, and style
     * @param text
     *            the text string whose size is to be estimated; maybe <code>null</code>
     * @return the minimal bounds for the string
     */
    public static Bounds estimateTextSize(final Font font, final String text) {
        // if no display is available fallback to awt metrics
        return estimateTextSizeAWT(font, text);
    }

    private static Bounds estimateTextSizeAWT(final Font font, final String text) {
        fmg.setFont(new java.awt.Font(font.getName(), 
                KTextUtil.swtFontStyle2Awt(font.getStyle()), 
                ((RenderedImage) font).getHeight()));
        final FontMetrics fm = fmg.getFontMetrics();

        final Bounds textBounds;
        if (Strings.isNullOrEmpty(text)) {
            // if no text string is given, take the bounds of a space character to get a proper
            // value for the height
            textBounds = new Bounds(fm.getStringBounds(" ", fmg));
            textBounds.setWidth(0f); // omit the width in this case
        } else {
            textBounds = new Bounds(fm.getStringBounds(text, fmg));
        }
        
        if (Klighd.simulateSwtFontSizeInAwt()) {
            textBounds.setWidth(textBounds.getWidth() * PT_TO_PX_FACTOR);
            textBounds.setHeight(textBounds.getHeight() * PT_TO_PX_FACTOR);
        }
        
        return textBounds;
    }
    
    /**
     * Returns the required minimal size of a {@link KRendering} width attached
     * {@link KPointPlacementData}.
     * 
     * @param rendering
     *            the {@link KRendering} to be evaluated
     * @param ppd the {@link KPointPlacementData} to be applied
     * 
     * @return the minimal required size
     */
    public static Bounds estimatePointPlacedChildSize(final KRendering rendering,
            final KPointPlacementData ppd) {
        // determine minimal needed size of the child
        // for point-based placement the parent size does not matter for the size!
        final Bounds minimalSize = Bounds.of(ppd.getMinWidth(), ppd.getMinHeight());
        final Bounds cSize = Bounds.max(minimalSize, basicEstimateSize(rendering, minimalSize));

        final float requiredWidth = getHorizontalSize(ppd, cSize.getWidth());
        final float requiredHeight = getVerticalSize(ppd, cSize.getHeight());

        return Bounds.of(requiredWidth, requiredHeight);
    }

    /**
     * Determines the horizontal size value for a point-based placed child.
     * 
     * @param ppd
     *            the {@link KPointPlacementData} containing the required declarations
     * @param minWidth
     *            the estimated minimal width of the child
     * @return a {@link Pair} of the absolute size offset and the relative size divisor
     */
    private static float getHorizontalSize(final KPointPlacementData ppd, final float minWidth) {
        if (ppd == null) {
            return minWidth;
        }

        final KPosition pos = ppd.getReferencePoint();
        final float abs = pos != null && pos.getX() != null ? pos.getX().getAbsolute() : 0f;
        float calculatedWidth = 0f;

        switch (ppd.getHorizontalAlignment()) {
        case LEFT:
        case RIGHT:
            // the child requires its minWidth and the absolute margin defined by pos.getX()
            calculatedWidth = abs + minWidth + ppd.getHorizontalMargin();
            break;
        case CENTER:
            final float halfWidth = minWidth / 2;
            if (abs > halfWidth) {
                // in this case the child requires, depending on type of pos.getX, on one side more
                // space than on the other, so:
                calculatedWidth = abs + halfWidth + ppd.getHorizontalMargin();
            } else {
                // in case one might argue the same way, but there's still the relative part
                // so I think potentially shrinking the width is not reasonable; thus:
                calculatedWidth = minWidth + 2 * ppd.getHorizontalMargin();
            }
        }
        return calculatedWidth;
    }

    /**
     * Determines the vertical size value for a point-based placed child.
     * 
     * @param ppd
     *            the {@link KPointPlacementData} containing the required declarations
     * @param minHeight
     *            the estimated minimal height of the child
     * @return a {@link Pair} of the absolute size offset and the relative size divisor
     */
    private static float getVerticalSize(final KPointPlacementData ppd, final float minHeight) {
        if (ppd == null) {
            return minHeight;
        }

        final KPosition pos = ppd.getReferencePoint();
        final float abs = pos != null && pos.getY() != null ? pos.getY().getAbsolute() : 0f;
        float calculatedHeight = 0f;

        switch (ppd.getVerticalAlignment()) {
        case TOP:
        case BOTTOM:
            // the child requires its minHeight and the absolute margin defined by pos.getY()
            calculatedHeight = abs + minHeight + ppd.getVerticalMargin();
            break;
        case CENTER:
            final float halfHeight = minHeight / 2;
            if (abs > halfHeight) {
                // in this case the child requires, depending on type of pos.getY, on one side more
                // space than on the other, so:
                calculatedHeight = abs + halfHeight + ppd.getVerticalMargin();
            } else {
                // in case one might argue the same way, but there's still the relative part
                // so I think potentially shrinking the width is not reasonable; thus:
                calculatedHeight = minHeight + 2 * ppd.getVerticalMargin();
            }
        }
        return calculatedHeight;
    }

    /**
     * Returns the required minimal size of a {@link KRendering} width attached
     * {@link KAreaPlacementData}.
     * 
     * @param container
     *            the {@link KRendering} to be evaluated
     * @param apd
     *            the {@link KAreaPlacementData} to be applied
     * @param givenBounds
     *            the size that is currently assigned to <code>rendering</code>'s container.
     * 
     * @return the minimal required size
     */
    protected static Bounds estimateAreaPlacedChildSize(final KRendering rendering,
            final KAreaPlacementData apd, final Bounds initialSize) {

        final Bounds cSize = evaluateAreaPlacement(apd, initialSize);
        // determine minimal needed size of the child
        final Bounds containerMinSize = basicEstimateSize(rendering, cSize);

        final KPosition tL = apd.getTopLeft();
        final KPosition bR = apd.getBottomRight();
        
        return inverselyApplyBoundingBoxKPositions(containerMinSize, tL, bR);
    }
    
    /**
     * Inversely applies the given "passe-partout" determined by <code>topLeft</code> and
     * <code>bottomRight</code> in order to calculate the outer bounds based on the given
     * <code>innerBounds</code>. Method is used in the area placement and grid placement handling.
     * 
     * @param innerBounds
     *            the inner bounds to inversely apply the "passe-partout" on
     * @param topLeft
     *            the top left {@link KPosition}
     * @param bottomRight
     *            the bottom right {@link KPosition}
     * @return the respective outer bounds
     */
    static Bounds inverselyApplyBoundingBoxKPositions(final Bounds innerBounds,
            final KPosition topLeft, final KPosition bottomRight) {
        
        final Pair<Float, Float> horSize = getHorizontalSize(topLeft, bottomRight);
        final Pair<Float, Float> verSize = getVerticalSize(topLeft, bottomRight);
        
        return inverselyApplySizeData(innerBounds, horSize, verSize);
    }
    

    /**
     * whether a position is measured in the same direction as the point it is defining e.g. a top
     * left position is measured from left
     */
    private static final int PIMARY = 0;

    /**
     * whether a position is measured contrary to the point it is defining e.g. a top right position
     * is measured from left
     */
    private static final int SECONDARY = 1;

    /**
     * offset to be used to calculate below defined constants to determine how positions interact
     * first positions are left or top.
     */
    private static final int FIRST_OFFSET = 100;

    /** both positions are measured direct. */
    private static final int PIMARY_PIMARY = PIMARY * FIRST_OFFSET + PIMARY;
    /** first position is measured directly, second position is measured indirectly. */
    private static final int PIMARY_SECONDARY = PIMARY * FIRST_OFFSET + SECONDARY;
    /** first position is measured indirectly, second position is measured directly. */
    private static final int SECONDARY_PIMARY = SECONDARY * FIRST_OFFSET + PIMARY;
    /** both positions are measured indirectly. */
    private static final int SECONDARY_SECONDARY = SECONDARY * FIRST_OFFSET + SECONDARY;

    /**
     * Determines the horizontal correction values for area-based placed child.
     * 
     * @param tL
     *            the topLeft position
     * @param bR
     *            the bottomRight position
     * @return a {@link Pair} of the absolute size offset (first component) and the relative size
     *         divisor (second component)
     */
    private static Pair<Float, Float> getHorizontalSize(final KPosition tL, final KPosition bR) {
        float abs0, abs1, rel0, rel1;
        int posId0, posId1;

        if (tL == null) {
            // emulate measuring from topLeft
            abs0 = 0;
            rel0 = 0;
            posId0 = PIMARY;
        } else {
            final KXPosition<?> lPos = toNonNullLeftPosition(tL.getX());
            abs0 = lPos.getAbsolute();
            rel0 = lPos.getRelative();
            posId0 = lPos.eClass().getClassifierID() == KRenderingPackage.KLEFT_POSITION ? PIMARY
                    : SECONDARY;
        }

        if (bR == null) {
            // emulate measuring from bottomRight
            abs1 = 0;
            rel1 = 0;
            posId1 = PIMARY;
        } else {
            final KXPosition<?> rPos = toNonNullRightPosition(bR.getX());
            abs1 = rPos.getAbsolute();
            rel1 = rPos.getRelative();
            posId1 = rPos.eClass().getClassifierID() == KRenderingPackage.KRIGHT_POSITION ? PIMARY
                    : SECONDARY;
        }

        return getSize(abs0, rel0, posId0, abs1, rel1, posId1);
    }

    /**
     * Determines the vertical correction values for area-based placed child.
     * 
     * @param tL
     *            the topLeft position
     * @param bR
     *            the bottomRight position
     * @return a {@link Pair} of the absolute size offset and the relative size divisor
     */
    private static Pair<Float, Float> getVerticalSize(final KPosition tL, final KPosition bR) {
        float abs0, abs1, rel0, rel1;
        int posId0, posId1;

        if (tL == null) {
            // emulate measuring from topLeft
            abs0 = 0;
            rel0 = 0;
            posId0 = PIMARY;
        } else {
            final KYPosition<?> rPos = toNonNullTopPosition(tL.getY());
            abs0 = rPos.getAbsolute();
            rel0 = rPos.getRelative();
            posId0 = rPos.eClass().getClassifierID() == KRenderingPackage.KTOP_POSITION ? PIMARY
                    : SECONDARY;
        }

        if (bR == null) {
            // emulate measuring from bottomRight
            abs1 = 0;
            rel1 = 0;
            posId1 = PIMARY;
        } else {
            final KYPosition<?> rPos = toNonNullBottomPosition(bR.getY());
            abs1 = rPos.getAbsolute();
            rel1 = rPos.getRelative();
            posId1 = rPos.eClass().getClassifierID() == KRenderingPackage.KBOTTOM_POSITION ? PIMARY
                    : SECONDARY;
        }

        return getSize(abs0, rel0, posId0, abs1, rel1, posId1);
    }

    /**
     * Normalize given insets to find out how big a child is in relation to it's parent and how much
     * space is needed to add absolute insets.
     * 
     * @param abs0
     *            the absolute value of the position defining first inset
     * @param rel0
     *            the relative value of the position defining first inset
     * @param positionId0
     *            the identifier constant informing about the measurement direction (DIRECT,
     *            INDIRECT)
     * @param abs1
     *            the absolute value of the position defining first inset
     * @param rel1
     *            the relative value of the position defining the first inset
     * @param positionId1
     *            the identifier constant informing about the measurement direction (DIRECT,
     *            INDIRECT)
     * @return a pair informing about the absolute value the child size is smaller than the parent
     *         and the relative size of a child w.r.t. to the size of the parent
     */
    private static Pair<Float, Float> getSize(final float abs0, final float rel0,
            final int positionId0, final float abs1, final float rel1, final int positionId1) {
        final float absOffset;
        final float relWidth;

        final int position = positionId0 * FIRST_OFFSET + positionId1;

        switch (position) {
        case PIMARY_PIMARY:
            // top left comes from left
            // bottom right comes from right
            relWidth = 1f - (rel1 + rel0); // this way the result is more precise!
            absOffset = abs0 + abs1;
            break;

        case PIMARY_SECONDARY:
            // top left comes from left
            // bottom right comes from left
            relWidth = rel1 - rel0;
            if (relWidth == 0) {
                absOffset = abs1;
            } else {
                absOffset = abs0 - abs1;
            }
            break;

        case SECONDARY_PIMARY:
            // top left comes from right
            // bottom right comes from right
            relWidth = rel0 - rel1;
            if (relWidth == 0) {
                absOffset = abs0;
            } else {
                absOffset = -abs0 + abs1;
            }
            break;

        case SECONDARY_SECONDARY:
            // top left comes from right
            // bottom right comes from left
            relWidth = rel1 + rel0 - 1f;
            absOffset = -abs0 - abs1;
            break;

        default:
            relWidth = 1f;
            absOffset = 0f;
            break;
        }
        return new Pair<Float, Float>(absOffset, relWidth);
    }


    /**
     * Applies the absolute offset and relative size factor inversely to the given bounds in order
     * to calculate the container's bounds.
     * 
     * @return the provided <code>bounds</code> object for convenience 
     */
    private static Bounds inverselyApplySizeData(final Bounds bounds,
            final Pair<Float, Float> horSize, final Pair<Float, Float> vertSize) {
        final float absXOffest = horSize.getFirst();
        final float relWidth = horSize.getSecond();

        final float absYOffest = vertSize.getFirst();
        final float relHeight = vertSize.getSecond();

        bounds.setWidth((relWidth == 0f ? absXOffest : (bounds.getWidth()  + absXOffest) / relWidth));
        bounds.setHeight((relHeight == 0f ? absYOffest : (bounds.getHeight()  + absYOffest) / relHeight));
        
        return bounds;
    }


    /**
     * Calculates the offset of the child area in the given <code>rootRendering</code>, which is
     * equal to the insets of the related node.<br>
     * <br>
     * To ensure correct layout, changes in the bounds of the node are not allowed to influence the
     * insets. If this is not given the insets may be incorrect.
     * 
     * @param rootRendering
     *            the related node's root KRendering element
     * @param insets
     *            the insets
     * @param minSize
     *            the minimal size of the related node (might not be reflected in the node's layout
     *            data)
     */
    public static void calculateInsets(final KRendering rootRendering, final KInsets insets,
            final Bounds minSize) {

        // no rendering so the whole node is the child area
        if (rootRendering == null) {
            return;
        }

        // find the path to the child area
        final LinkedList<KRendering> path = Lists.newLinkedList();

        if (!findChildArea(rootRendering, path)) {
            // no child area so the whole node is the child area
            return;
        }

        Bounds currentBounds = minSize;

        // the data of the reference parent
        KContainerRendering currentParent = null;

        // the calculated insets
        float leftInset = 0;
        float rightInset = 0;
        float topInset = 0;
        float bottomInset = 0;

        while (!path.isEmpty()) {
            KRendering currentRendering = path.pollFirst();

            // calculate the bounds of the current rendering
            Bounds bounds;
            if (currentParent == null) {
                bounds = calculateBounds(null, currentBounds, null, currentRendering);
            } else {
                bounds = calculateBounds(currentParent.getChildPlacement(), currentBounds,
                        currentParent.getChildren(), currentRendering);
            }

            // update the insets using the new bounds
            leftInset += bounds.getX();
            rightInset += currentBounds.getWidth() - bounds.getX() - bounds.getWidth();
            topInset += bounds.getY();
            bottomInset += currentBounds.getHeight() - bounds.getY() - bounds.getHeight();

            // dereference all rendering references
            while (currentRendering instanceof KRenderingRef) {
                final KRenderingRef renderingRef = (KRenderingRef) currentRendering;
                currentRendering = renderingRef.getRendering();
                path.removeFirst();
            }

            // if the rendering is a container rendering set the new current parent
            if (currentRendering instanceof KContainerRendering) {
                currentParent = (KContainerRendering) currentRendering;
                currentBounds = bounds;
            }
        }

        // set the insets
        insets.setLeft(leftInset);
        insets.setRight(rightInset);
        insets.setTop(topInset);
        insets.setBottom(bottomInset);
    }

    /**
     * Calculates the bounds of a child rendering inside a parent with given placement and bounds.
     * 
     * @param placement
     *            the placement
     * @param parentBounds
     *            the parent bounds
     * @param children
     *            the other children of the parent containing this rendering
     * @param child
     *            the child rendering
     * @return the bounds of the child rendering inside the parent rendering
     */
    private static Bounds calculateBounds(final KPlacement placement, final Bounds parentBounds,
            final List<KRendering> children, final KRendering child) {
        Bounds bounds = null;
        if (placement == null) {
            final KPlacementData pd = getPlacementData(child);
            final KPointPlacementData ppd = asPointPlacementData(pd);
            if (ppd != null) {
                bounds = evaluatePointPlacement(ppd,
                        PlacementUtil.estimateSize(child, new Bounds(0.0f, 0.0f)), parentBounds);
            } else {
                bounds = evaluateAreaPlacement(asAreaPlacementData(pd), parentBounds);
            }
        } else {
            bounds = new KRenderingSwitch<Bounds>() {
                @Override
                public Bounds caseKGridPlacement(final KGridPlacement gridPlacement) {
                    // evaluate grid based on the children, their placementData and size
                    // and get placement for current child
                    final Bounds[] childBounds = GridPlacementUtil.evaluateGridPlacement(
                            gridPlacement, children, parentBounds);
                    if (childBounds == null) {
                        return Bounds.of(0, 0);
                    } else {
                        final int index = children.lastIndexOf(child);
                        return childBounds[index];
                    }
                }
            }
            .doSwitch(placement);
        }

        if (child instanceof KPolyline) {
            return evaluatePolylineBounds((KPolyline) child, bounds);
        } else {
            return bounds;
        }
    }

    /**
     * Returns the bounds for a polyline based on given bounds.
     * 
     * @param line
     *            the polyline with its points
     * @param givenBounds
     *            the given bounds
     * @return the actual polyline's bounding box' bounds
     */
    protected static Bounds evaluatePolylineBounds(final KPolyline line, final Bounds givenBounds) {
        if (line == null || line.getPoints().isEmpty()) {
            return Bounds.of(givenBounds.getWidth(), givenBounds.getHeight());
        }

        // evaluate the points of the polyline inside the parent bounds to compute the bounding box

// chsch: considering the implementation of Bounds.max(...) determining the minimal x & y
//         doesn't make any sense, does it?

//        float minX = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
//        float minY = Float.MAX_VALUE;
        float maxY = Float.MIN_VALUE;
        for (final KPosition polylinePoint : line.getPoints()) {
            final Point point = evaluateKPosition(polylinePoint, givenBounds, true);
//            if (point.x < minX) {
//                minX = point.x;
//            }
            if (point.x > maxX) {
                maxX = point.x;
            }
//            if (point.y < minY) {
//                minY = point.y;
//            }
            if (point.y > maxY) {
                maxY = point.y;
            }
        }

        return Bounds.max(Bounds.of(maxX, maxY), givenBounds);
    }


    /**
     * Searches the rendering for a child area and records the path in the given list of renderings.
     * 
     * @param rendering
     *            the rendering
     * @param path
     *            the list of renderings which will contain the path of renderings to the child area
     * @return true if a child area has been found; false else
     */
    public static boolean findChildArea(final KRendering rendering,
            final LinkedList<KRendering> path) {
        path.addLast(rendering);
        if (rendering instanceof KChildArea) {
            return true;
        }
        if (rendering instanceof KContainerRendering) {
            final KContainerRendering containerRendering = (KContainerRendering) rendering;
            for (final KRendering childRendering : containerRendering.getChildren()) {
                if (findChildArea(childRendering, path)) {
                    return true;
                }
            }
        } else if (rendering instanceof KRenderingRef) {
            final KRenderingRef renderingReference = (KRenderingRef) rendering;
            final KRendering referencedRendering = renderingReference.getRendering();
            if (referencedRendering != null) {
                if (findChildArea(referencedRendering, path)) {
                    return true;
                }
            }
        }
        path.removeLast();
        return false;
    }
}
