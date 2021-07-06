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
package de.cau.cs.kieler.klighd.piccolo.internal.nodes;

import static de.cau.cs.kieler.klighd.piccolo.internal.nodes.KlighdPath.PolylinePath.POLYGON;
import static de.cau.cs.kieler.klighd.piccolo.internal.nodes.KlighdPath.PolylinePath.POLYLINE;
import static de.cau.cs.kieler.klighd.piccolo.internal.nodes.KlighdPath.PolylinePath.ROUNDED_BENDS_POLYLINE;
import static de.cau.cs.kieler.klighd.piccolo.internal.nodes.KlighdPath.PolylinePath.SPLINE;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.RGB;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import de.cau.cs.kieler.klighd.Klighd;
import de.cau.cs.kieler.klighd.KlighdConstants;
import de.cau.cs.kieler.klighd.eclipse.EclipseKlighdConstants;
import de.cau.cs.kieler.klighd.krendering.KRendering;
import de.cau.cs.kieler.klighd.piccolo.KlighdNode;
import de.cau.cs.kieler.klighd.piccolo.KlighdSWTGraphics;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.NodeDisposeListener.IResourceEmployer;
import de.cau.cs.kieler.klighd.piccolo.internal.util.KlighdPaintContext;
import de.cau.cs.kieler.klighd.piccolo.internal.util.PolylineUtil;
import de.cau.cs.kieler.klighd.piccolo.internal.util.RGBGradient;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * The KLighD-specific {@link edu.umd.cs.piccolo.PNode PNode} implementation for displaying
 * primitive figures.<br>
 * It is inspired by the Piccolo2D {@link edu.umd.cs.piccolox.swt.PSWTPath PSWTPath} and is
 * tailored/extended to those features required by KLighD.<br>
 * <br>
 * {@link KlighdPath} instances require a {@link KlighdSWTGraphics} while drawing (i.e. in
 * {@link #paint(KlighdPaintContext)}). In case the available implementation provides an SWT
 * {@link Device} SWT {@link Path} objects are created and drawn, and disposed if they got
 * out-dated. Otherwise the internally used AWT {@link Shape Shapes} are used for drawing.<br>
 * <br>
 * The stroke lines of closed figures like rectangles, ellipses, and arcs (although arcs are usually
 * not closed), i.e. those whose size is determined by a rectangular bounding box, do not violate
 * the bounds of those figures. Instead they get wider towards the center of the figure when the
 * line width increases. This behavior is realized in {@link #updateShape()}.<br>
 * <br>
 * This, however, does not hold for polylines and polygons as their covered area is determined by
 * the particular line points.<br>
 * <br>
 * <b>Note:</b> All the implementation of {@link edu.umd.cs.piccolo.PNode#invalidatePaint()
 * PNode#invalidatePaint()} is significantly changed in the super class
 * {@link KlighdNode.KlighdFigureNode}. Pay attention in case this figure implementation is used
 * beyond the representation of {@link KRendering}-based figure specifications.
 *
 * @author chsch, mri
 */
public class KlighdPath extends KlighdNode.KlighdFigureNode<KRendering> implements IResourceEmployer {

    private static final long serialVersionUID = 8034306769936734586L;

    private static final Map<Color, RGB> RGB_CACHE = Maps.newConcurrentMap();

    // in order to fix the windows specific issue with the monitor zoom related line width scaling
    //  (KIPRA-1925) but avoid additional effort on other platforms,
    // do the workaround only if the following condition is *not* satisfied
    private static final boolean skipCopyingLineAttributes = !Klighd.IS_WINDOWS;

    /**
     * Types of supported polyline paths, for internal use only.
     */
    enum PolylinePath {
        POLYGON, POLYLINE, ROUNDED_BENDS_POLYLINE, SPLINE;
    }

    private LineAttributes lineAttributes = new LineAttributes(1f);
    private Stroke stroke = new BasicStroke();

    private int strokeAlpha = KlighdConstants.ALPHA_FULL_OPAQUE;
    private RGB strokePaint = EclipseKlighdConstants.BLACK;
    private RGBGradient strokePaintGradient = null;

    private int paintAlpha = KlighdConstants.ALPHA_FULL_OPAQUE;
    // at most one of the following is allowed to have a non-null value at any time!
    private RGB paint = null;
    private RGBGradient paintGradient = null;

    // the amount of pixels the shadow will cover in horizontal and vertical direction
    private static final float DEFAULT_SHADOW_EXTEND = 4;
    private float shadowExtendX = DEFAULT_SHADOW_EXTEND;
    private float shadowExtendY = DEFAULT_SHADOW_EXTEND;
    private RGB shadow = null;

    // default initialization that avoids null pointer faults in case of failing setPathTo... calls
    private Shape origShape = new Rectangle2D.Float();
    private Shape shape = null;

    private PolylinePath lineType = null;

    private Path shapePath = null;

    // A field keeping the bounds being originally assigned to the figure;
    // the assigned bounds are mostly the bounds of 'origShape'; in case of polylines or
    //  polygons the bounding box of origShape will mostly differ from the assignedBounds
    // imagine for example the polyline describing the body of a UML actor stick man figure
    //  that may cover only the bottom-most 70% percent of the assigned (knode) bounds
    // the polyline child circle figure denoting the actor's head shall be placed based on bounds
    //  assigned to the polyline, rather the minimal bounds encompassing the polylines points
    private Rectangle2D assignedBounds = null;

    // A field to keep the list of points line points in mind. They are required while determining
    //  the rotation of edge decorators. By remembering them a re-extraction from the {@link Path2D
    //  Path2D} is avoided.
    private Point2D[] linePoints;

    // These deactivated internal affine transforms are kept here and later on in the code
    //  for the potential case they are be required in future for realizing a certain feature.
    // The deactivated statements illustrate how such transforms are employed correctly in
    //  PNode implementations.
    //
    // private PAffineTransform internalXForm;
    // private AffineTransform inverseXForm;

    /**
     * Standard constructor.
     */
    public KlighdPath() {
        super();
    }

    /**
     * Constructor.
     *
     * @param rendering
     *            the {@link KRendering} element being represented by this {@link KlighdPath}
     */
    public KlighdPath(final KRendering rendering) {
        super(rendering);
    }


    /**
     * Changes the underlying shape of this {@link KlighdPath}.
     *
     * @param newShape
     *            new associated shape of this {@link KlighdPath}
     */
    private boolean setShape(final Shape newShape) {
        origShape = newShape;
        updateShape();
        return updateBoundsFromPath();
    }

    /**
     * Gets the underlying shape of this {@link KlighdPath}.
     * @return the underlying shape
     */
    public Shape getShape() {
        return shape;
    }

    /**
     * Returns the points of the shape.
     *
     * @return the points or <code>null</code> if path is not a line or is not initialized properly.
     */
    public Point2D[] getShapePoints() {
        return linePoints;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Rectangle2D getAssignedBounds() {
        if (assignedBounds == null || !isLineOrPolygon()) {
            return super.getAssignedBounds();
        } else {
            return assignedBounds;
        }
    }

    /**
     * Provides the information whether is path is a simple line or a shape with a closed periphery.
     *
     * @return <code>true</code> if the path is a polyline, rounded bend point polyline, or a spline;
     *         <code>false</code> otherwise.
     */
    private boolean isLine() {
        return this.lineType != null && this.lineType != POLYGON;
    }

    /**
     * Provides the information whether is path is a line defined by particular points.
     *
     * @return <code>true</code> if the path is a polygon, polyline, rounded bend point polyline,
     *         or a spline; <code>false</code> otherwise.
     */
    private boolean isLineOrPolygon() {
        return this.lineType != null;
    }

    /* --------------------- */
    /*  style related stuff  */
    /* --------------------- */

    /**
     * Returns the attached line attributes record denoting stroke configuration w.r.t. line cap,
     * join, style, and width.
     *
     * @return the line related {@link LineAttributes}.
     */
    public LineAttributes getLineAttributes() {
        return lineAttributes;
    }

    /**
     * Sets a new line attributes record.
     *
     * @param theLineAttributes
     *            the desired {@link LineAttributes} record.
     */
    public void setLineAttributes(final LineAttributes theLineAttributes) {
        if (theLineAttributes == null || theLineAttributes.equals(this.lineAttributes)) {
            return;
        }
        this.lineAttributes = theLineAttributes;
        flushAttributes();
    }

    /**
     * Triggers a re-evaluation of the attached {@link LineAttributes}, must be called after
     * manipulating the {@link LineAttributes} obtained via {@link #getLineAttributes()}.
     */
    public void flushAttributes() {
        final Stroke newStroke = new BasicStroke(lineAttributes.width, lineAttributes.cap - 1,
                lineAttributes.join - 1, lineAttributes.miterLimit);
                //, lineAttributes.dash, lineAttributes.dashOffset);

        if (this.stroke.equals(newStroke)) {
            return;
        }

        this.stroke = newStroke;

        updateBoundsFromPath();
        updateShape();
    }

    /**
     * Returns the line width of the path.
     *
     * @return the line width
     */
    public double getLineWidth() {
        return this.lineAttributes.width;
    }

    /**
     * Sets the line width of the path.
     *
     * @param width
     *            the line width
     */
    public void setLineWidth(final float width) {
        if (width == this.lineAttributes.width) {
            return;
        }
        this.lineAttributes.width = width;
        this.stroke = new BasicStroke(lineAttributes.width, lineAttributes.cap - 1,
                lineAttributes.join - 1, lineAttributes.miterLimit);
                // , lineAttributes.dash, lineAttributes.dashOffset);
        updateBoundsFromPath();
        updateShape();
    }

    /**
     * Returns the paint to use when drawing the stroke of the shape.
     *
     * @return path's stroke paint
     */
    public RGB getStrokePaint() {
        return strokePaint;
    }

    /**
     * Returns the stroke paint gradient used while painting this node. This value may be null.
     *
     * @return the stroke paint gradient used while painting this node.
     */
    public RGBGradient getStrokePaintGradient() {
        return this.paintGradient;
    }

    /**
     * Sets the paint to use when drawing the stroke of the shape.
     *
     * @param strokeColor
     *            new stroke color
     */
    public void setStrokeColor(final RGB strokeColor) {
        if (strokePaint != null && strokePaint.equals(strokeColor)) {
            return;
        }

        Object oldPaint = null;
        if (strokePaintGradient != null) {
            oldPaint = strokePaintGradient;
            strokePaintGradient = null;
        } else if (strokePaint != null) {
            oldPaint = strokePaint;
        }
        strokePaint = strokeColor;

        invalidatePaint();
        firePropertyChange(
                PPath.PROPERTY_CODE_STROKE_PAINT, PPath.PROPERTY_STROKE_PAINT, oldPaint, strokePaint);
    }

    /**
     * Set the alpha used to draw the stroke of the shape.
     *
     * @param alpha
     *            the alpha value
     */
    public void setStrokeAlpha(final int alpha) {
        this.strokeAlpha = alpha;
    }

    /**
     * Sets the paint to use when drawing the stroke of the shape.
     *
     * @param strokeGradient
     *            new stroke color
     */
    public void setStrokeColor(final RGBGradient strokeGradient) {
        if (strokePaintGradient != null && strokePaintGradient.equals(strokeGradient)) {
            return;
        }
        Object oldPaint = null;
        if (strokePaintGradient != null) {
            oldPaint = strokePaintGradient;
        } else if (strokePaint != null) {
            oldPaint = strokePaint;
            strokePaint = null;
        }
        strokePaintGradient = strokeGradient;

        invalidatePaint();
        firePropertyChange(PPath.PROPERTY_CODE_STROKE_PAINT, PPath.PROPERTY_STROKE_PAINT, oldPaint,
                strokePaintGradient);
    }

    /**
     * Returns the paint used while painting this node. This value may be null.
     *
     * @return the paint used while painting this node.
     */
    public RGB getSWTPaint() {
        return this.paint;
    }

    /**
     * Returns the paint gradient used while painting this node. This value may be null.
     *
     * @return the paint gradient used while painting this node.
     */
    public RGBGradient getSWTPaintGradient() {
        return this.paintGradient;
    }

    /**
     * Set the paint used to paint this node, which may be null.
     *
     * @param newPaint
     *            paint that this node should use when painting itself.
     */
    public void setPaint(final RGB newPaint) {
        if (paint != null && paint.equals(newPaint)) {
            return;
        }
        Object oldPaint = null;
        if (paintGradient != null) {
            oldPaint = paintGradient;
            paintGradient = null;
        } else if (paint != null) {
            oldPaint = paint;
        }
        paint = newPaint;

        invalidatePaint();
        firePropertyChange(PROPERTY_CODE_PAINT, PROPERTY_PAINT, oldPaint, paint);
    }

    /**
     * Set the alpha used to paint this node.
     *
     * @param alpha
     *            the alpha value
     */
    public void setPaintAlpha(final int alpha) {
        this.paintAlpha = alpha;
    }

    /**
     * Set the paint used to paint this node, which may be null.
     *
     * @param newPaint
     *            paint that this node should use when painting itself.
     */
    public void setPaint(final RGBGradient newPaint) {
        if (paintGradient != null && paintGradient.equals(newPaint)) {
            return;
        }
        Object oldPaint = null;
        if (paintGradient != null) {
            oldPaint = paintGradient;
        } else if (paint != null) {
            oldPaint = paint;
            paint = null;
        }
        paintGradient = newPaint;

        invalidatePaint();
        firePropertyChange(PROPERTY_CODE_PAINT, PROPERTY_PAINT, oldPaint, paint);
    }

    /**
     * Set the paint used to paint this node, which may be null.
     *
     * @param newPaint
     *            paint that this node should use when painting itself.
     */
    public void setPaint(final Color newPaint) {
        RGB rgb = RGB_CACHE.get(newPaint);
        if (rgb == null) {
            rgb = new RGB(newPaint.getRed(), newPaint.getGreen(), newPaint.getBlue());
            RGB_CACHE.put(newPaint, rgb);
        }
        this.setPaint(rgb);
    }

    /**
     * Sets the shadow color for this path. Note that this definition will be ignored for non-closed
     * shapes, i.e. polylines and curves.
     *
     * @param color
     *            the color of the attached shadow
     * @param xOffset
     *            the x offset of the shadow
     * @param yOffset
     *            the y offset of the shadow
     */
    public void setShadow(final RGB color, final float xOffset, final float yOffset) {
        if (!isLine() && color != null) {
            this.shadow = color;
        } else {
            this.shadow = null;
        }
        this.shadowExtendX = xOffset;
        this.shadowExtendY = yOffset;
    }

    /**
     * Gets the shadow color for this path.
     *
     * @return the current shadow
     */
    public RGB getShadow() {
        return this.shadow;
    }

    /* ---------------------- */
    /*  bounds related stuff  */
    /* ---------------------- */

    /**
     * Returns true if path crosses the provided bounds. Takes visibility of path into account.
     *
     * @param localBounds
     *            bounds being tested for intersection
     * @return true if path visibly crosses bounds
     */
    @Override
    public boolean intersects(final Rectangle2D localBounds) {
        if (super.intersects(localBounds)) {
            final Rectangle2D srcBounds = localBounds;
            // see remark on the deactivated 'transform' code above
            //
            // if (internalXForm == null) {
            //     srcBounds = localBounds;
            // } else {
            //     srcBounds = new PBounds(localBounds);
            //     internalXForm.inverseTransform(srcBounds, srcBounds);
            // }

            if (!isLine()) {
                // We changed the condition to treat "frame figures", i.e. non-filled closed shapes
                //  without a given background color, like filled ones with a background color.
                // The distinction w.r.t. to the line attribute abolishes b.t.w. the
                //  picking issue of non-closed lines, which was a problem in the early days of
                //  KLighD.
                return shape.intersects(srcBounds);
            } else if (strokePaint != null || strokePaintGradient != null) {
                return stroke.createStrokedShape(shape).intersects(srcBounds);
            }
        }
        return false;
    }


    /**
     * This method realizes the adjustment of the shape bounds according to stroke line width.
     * To this end, the initial shape definition, which is kept in {@link #origShape} is replicated
     * with adjusted bounds. This replicate is stored in {@link #shape} and drawn on the canvas
     * in {@link #paint(KlighdPaintContext)}.<br>
     * <br>
     * In case of lines and polygons the {@link #origShape} is put into {@link #shape}, too, and
     * thus used while drawing.
     *
     * @author chsch
     */
    private void updateShape() {
        disposeSWTResource();

        if (isLineOrPolygon()) {
            shape = origShape;

        } else {
            final float lW = lineAttributes.width;
            final float halfLW = lineAttributes.width / 2;

            if (origShape instanceof Arc2D) {
                final Arc2D.Float arc = (Arc2D.Float) origShape;
                shape = new Arc2D.Float(arc.x + halfLW, arc.y + halfLW, arc.width - lW, arc.height - lW,
                        arc.start, arc.extent, arc.getArcType());
            } else {
                // in the above case the usage of the bound computed below is not valid in case the
                //  arc's extent is significantly smaller than 360, as 'getBounds2D()' returns the
                //  actually covered area's bounds; since, in addition, this call is rather expensive
                //  for arcs it is only performed for ellipses, rectangles, and rounded rectangles
                final Rectangle2D.Float b = (Rectangle2D.Float) origShape.getBounds2D();

                if (origShape instanceof Ellipse2D) {
                    shape = new Ellipse2D.Float(b.x + halfLW, b.y + halfLW, b.width - lW, b.height - lW);
                } else if (origShape instanceof Rectangle2D) {
                    shape = new Rectangle2D.Float(b.x + halfLW, b.y + halfLW, b.width - lW, b.height
                            - lW);
                } else if (origShape instanceof RoundRectangle2D) {
                    final RoundRectangle2D.Float rect = (RoundRectangle2D.Float) origShape;
                    shape = new RoundRectangle2D.Float(b.x + halfLW, b.y + halfLW, b.width - lW,
                            b.height - lW, rect.arcwidth, rect.archeight);
                }
            }
        }

        invalidatePaint();
        firePropertyChange(PPath.PROPERTY_CODE_PATH, PPath.PROPERTY_PATH, null, shape);
    }


    /**
     * Recalculates the path's bounds by examining it's associated shape.
     */
    private boolean updateBoundsFromPath() {
        if (isLineOrPolygon()) {
            final Rectangle2D b = shape.getBounds2D();
            final float halfLW = lineAttributes.width / 2;

            // By means of the following setting it's guaranteed that the line width is incorporated
            //  in determining the bounding box of horizontal and vertical lines.
            // This is important for being able to pick them, for instance.

            return super.setBounds(b.getX() - halfLW, b.getY() - halfLW,
                    b.getWidth() + lineAttributes.width,
                    b.getHeight() + lineAttributes.width);
        } else {
            return super.setBounds(origShape.getBounds2D());
        }
    }

    /**
     * {@inheritDoc}<br>
     * <br>
     * This customization locally extends the fullBounds by the size of the shadow, if present.<br>
     * Makes sure the shadow is cleared out if this path figure is moved leftward/upward or removed.
     */
    @Override
    public PBounds computeFullBounds(final PBounds dstBounds) {

        final PBounds fullBounds = super.computeFullBounds(dstBounds);
        // the above produced result is already adjusted by 'this.transform';
        // since particular figures are not scaled but only moved (transformed)
        //  we can easily increase the size

        if (shadow != null) {
            fullBounds.width += shadowExtendX;
            fullBounds.height += shadowExtendY;
        }
        return fullBounds;
    }

    /* ----------------------- */
    /*  drawing related stuff  */
    /* ----------------------- */

    /**
     * {@inheritDoc}
     */
    @Override
    protected void paint(final KlighdPaintContext kpc) {

        final KlighdSWTGraphics graphics = kpc.getKlighdGraphics();
        final Device device = graphics.getDevice();

        // flag indicating whether we can construct SWT Paths and rely on
        //  KlighdSWTGraphics#draw(Path) & KlighdSWTGraphics#fill(Path)
        final boolean swt = device != null;

        // take care about the shadow
        if (shadow != null) {
            if (swt && shapePath == null) {
                shapePath = KlighdPaths.createSWTPath(shape.getPathIterator(null), device);
            }
            drawShadow(graphics, swt);
        }
        final int currentAlpha = graphics.getAlpha();
        final float currentAlphaFloat = currentAlpha;


        // We attach semantic data favored to the foreground element
        // however, if no foreground exists, we attach it to the background
        // FIXME not working for rendering refs
        final boolean drawForeground = (lineAttributes.width != 0f)
                        && (strokePaint != null || strokePaintGradient != null);
        final boolean drawBackground = !isLine() && (paint != null || paintGradient != null);

        // if not even a background is painted, don't attach the semantic data at all
        if (drawBackground) {
            addSemanticData(kpc);
        }

        // draw the background if possible and required
        if (!isLine()) {
            if (swt && shapePath == null && (paint != null || paintGradient != null)) {
                shapePath = KlighdPaths.createSWTPath(shape.getPathIterator(null), device);
            }

            if (paint != null) {
                graphics.setAlpha(
                        (int) (paintAlpha * (currentAlphaFloat / KlighdConstants.ALPHA_FULL_OPAQUE)));
                graphics.setFillColor(paint);
                if (swt) {
                    graphics.fill(shapePath);
                } else {
                    graphics.fill(shape);
                }
            }

            if (paintGradient != null) {
                graphics.setFillPattern(paintGradient, getBoundsReference());
                if (swt) {
                    graphics.fill(shapePath);
                } else {
                    graphics.fill(shape);
                }
            }
        }

        if (!drawBackground && drawForeground) {
            addSemanticData(kpc);
        }

        // draw the foreground if required
        //  in case of a line width of zero we can skip this
        if (lineAttributes.width != 0f) {
            final LineAttributes attr = lineAttributes;
            graphics.setLineAttributes(
                    skipCopyingLineAttributes ? attr : new LineAttributes(
                            // workaround for KIPRA-1925
                            attr.width, attr.cap, attr.join, attr.style,
                            attr.dash != null ? attr.dash.clone() : null, attr.dashOffset,
                            attr.miterLimit));

            if (swt && shapePath == null && (strokePaint != null || strokePaintGradient != null)) {
                shapePath = KlighdPaths.createSWTPath(shape.getPathIterator(null), graphics.getDevice());
            }

            if (strokePaint != null) {
                graphics.setAlpha(
                        (int) (strokeAlpha * (currentAlphaFloat / KlighdConstants.ALPHA_FULL_OPAQUE)));
                graphics.setStrokeColor(strokePaint);
                if (swt) {
                    graphics.draw(shapePath);
                } else {
                    graphics.draw(shape);
                }
            }

            if (strokePaintGradient != null) {
                graphics.setStrokePattern(strokePaintGradient, getBoundsReference());
                if (swt) {
                    graphics.draw(shapePath);
                } else {
                    graphics.draw(shape);
                }
            }
        }

        graphics.setAlpha(currentAlpha);
        final LineAttributes defaults = EclipseKlighdConstants.DEFAULT_LINE_ATTRIBUTES; 
        graphics.setLineAttributes(skipCopyingLineAttributes ? defaults
                : new LineAttributes(
                        // workaround for KIPRA-1925
                        defaults.width, defaults.cap, defaults.join, defaults.style,
                        defaults.dash != null ? defaults.dash.clone() : null, defaults.dashOffset,
                        defaults.miterLimit));
    }

    /**
     * Draws the shadow of the current shape.
     *
     * @param graphics
     *            the {@link KlighdSWTGraphics} to draw on.
     * @param swt
     *            flag indicating whether we can construct SWT Paths and rely on
     *            {@link KlighdSWTGraphics#draw(Path)} & {@link KlighdSWTGraphics#fill(Path)}
     */
    private void drawShadow(final KlighdSWTGraphics graphics, final boolean swt) {

        final int currentAlpha = graphics.getAlpha();

        // the alpha value of the particular shadow shapes
        // note that the alpha values of stacked shapes will kind of accumulate
        final float shadowAlpha = 25f;


        // determine the movement of the shape coordinates by means of an affine transform
        final AffineTransform t = graphics.getTransform();
        final AffineTransform tc = new AffineTransform(t);
        tc.translate(shadowExtendX, shadowExtendY);

        // configure the graphics layer
        graphics.setFillColor(shadow);

        // a sufficiently small number unequal to 0
        graphics.setLineAttributes(new LineAttributes(0.0001f)); // SUPPRESS CHECKSTYLE MagicNumber
        graphics.setAlpha(
                (int) (currentAlpha * shadowAlpha / KlighdConstants.ALPHA_FULL_OPAQUE));

        // use the maximal value for the loop
        final int maxShadowExtend = (int) Math.ceil(Math.max(shadowExtendX, shadowExtendY));
        final boolean xIsBigger = shadowExtendX > shadowExtendY;
        final double ratio =
                xIsBigger ? (shadowExtendY / shadowExtendX) : (shadowExtendX / shadowExtendY);
        // draw a bunch of shape copies, each of them is moved a bit towards the original position
        for (int i = 0; i < maxShadowExtend; i++) {
            graphics.setTransform(tc);
            if (swt) {
                graphics.fill(shapePath);
            } else {
                graphics.fill(shape);
            }
            if (xIsBigger) {
                tc.translate(-1, -ratio);
            } else {
                tc.translate(-ratio, -1);
            }
        }

        // re-set the original transform to the graphics layer
        // and draw a fresh white background on the shadow shape stack at the original position
        // Note, that we accept the incompatibility of transparent shapes and shadow, and thus
        // dismiss the transparency.
        graphics.setTransform(t);
        // graphics.setLineWidth(1);
        graphics.setAlpha(KlighdConstants.ALPHA_FULL_OPAQUE);
        graphics.setFillColor(EclipseKlighdConstants.WHITE);
        if (swt) {
            graphics.fill(shapePath);
        } else {
            graphics.fill(shape);
        }

        // reset the manipulated settings
        graphics.setAlpha(currentAlpha);
        // graphics.setLineWidth(lineAttributes.width);
    }


    /**
     * Disposes the current {@link #shapePath} if any one is available. This is suggested in order
     * to tidy up the native drawing objects, which have been created while constructing
     * {@link #shapePath}. (see {@link Path#Path(Device)} for details)
     */
    public void disposeSWTResource() {
        if (this.shapePath != null) {
            this.shapePath.dispose();
            this.shapePath = null;
        }
    }


    /* --------------------- */
    /*  initializer methods  */
    /* --------------------- */

    /**
     * Resets <code>this</code> path to <code>rect</code>.<br>
     * In doing to a copy of the given {@link Rectangle2D} is created.
     *
     * @param rect
     *            the rectangle determining the new bounds
     */
    public void setPathToRectangle(final Rectangle2D rect) {
        this.assignedBounds = null;
        this.lineType = null;
        final Rectangle2D rectFloat = new Rectangle2D.Float();
        rectFloat.setFrame(rect);
        this.setShape(rectFloat);
    }

    /**
     * Resets <code>this</code> path to <code>rect</code>.<br>
     * <b>Be careful:</b>The given {@link Rectangle2D.Float} instance is used further on so don't
     * modify it externally!
     *
     * @param rect
     *            the rectangle determining the new bounds
     */
    public void setPathToRectangle(final Rectangle2D.Float rect) {
        this.assignedBounds = null;
        this.lineType = null;
        this.setShape(rect);
    }

    /**
     * Resets <code>this</code> path to a rectangle with the dimensions and position provided.
     *
     * @param x
     *            left of the rectangle
     * @param y
     *            top of the rectangle
     * @param width
     *            width of the rectangle
     * @param height
     *            height of the rectangle
     */
    public void setPathToRectangle(final float x, final float y, final float width,
            final float height) {
        this.assignedBounds = null;
        this.lineType = null;
        this.setShape(new Rectangle2D.Float(x, y, width, height));
    }
    /**
     * Resets <code>this</code> path to a rectangle with the dimensions and position provided.
     *
     * @param x
     *            left of the rectangle
     * @param y
     *            top of the rectangle
     * @param width
     *            width of the rectangle
     * @param height
     *            height of the rectangle
     * @param arcWidth
     *            width of arc in the corners of the rectangle
     * @param arcHeight
     *            height of arc in the corners of the rectangle
     */
    public void setPathToRoundRectangle(final float x, final float y, final float width,
            final float height, final float arcWidth, final float arcHeight) {
        this.assignedBounds = null;
        this.lineType = null;
        this.setShape(new RoundRectangle2D.Float(x, y, width, height, arcWidth, arcHeight));
    }

    /**
     * Resets <code>this</code> path to an ellipse positioned at the coordinate provided with the
     * dimensions provided.
     *
     * @param x
     *            left of the ellipse
     * @param y
     *            top of the ellipse
     * @param width
     *            width of the ellipse
     * @param height
     *            height of the ellipse
     */
    public void setPathToEllipse(final float x, final float y, final float width, final float height) {
        this.assignedBounds = null;
        this.lineType = null;
        this.setShape(new Ellipse2D.Float(x, y, width, height));
    }


    /**
     * Resets <code>this</code> path to an arc positioned at the coordinate provided with the
     * dimensions, angular start and angular extent provided.
     *
     * @param x
     *            left of the arc
     * @param y
     *            top of the arc
     * @param width
     *            width of the arc
     * @param height
     *            height of the arc
     * @param angStart
     *            angular start of the arc
     * @param angExtend
     *            angular extent of the arc
     * @param type
     *            one of the constants {@link Arc2D#OPEN}, {@link Arc2D#CHORD}, and
     *            {@link Arc2D#PIE}
     */
    public void setPathToArc(final float x, final float y, final float width, final float height,
            final float angStart, final float angExtend, final int type) {
        this.assignedBounds = null;
        this.lineType = null;
        this.setShape(new Arc2D.Float(x, y, width, height, angStart, angExtend, type));
    }


    /**
     * Sets <code>this</code> path to a sequence of segments described by the points.
     *
     * @param points
     *            points to that lie along the generated path
     */
    public void setPathToSpline(final Point2D[] points) {
        this.setPathToSpline(null, points);
    }


    /**
     * Sets <code>this</code> path to a sequence of segments described by the points.
     *
     * @param theAssignedBounds
     *            the bounds being assigned to this figure based on which the
     *            child figures' bounds are determined
     * @param points
     *            points to that lie along the generated path
     */
    public void setPathToSpline(final Rectangle2D theAssignedBounds, final Point2D[] points) {
        this.updateAssignedBoundsAndSetPathToLine(theAssignedBounds, points,
                () -> this.doSetPathToSpline(points));
    }

    private boolean doSetPathToSpline(final Point2D[] points) {
        if (points.length == 0) {
            return false;
        }

        this.lineType = SPLINE;
        final Path2D spline = PolylineUtil.createSplinePath(new Path2D.Float(), points);
        this.linePoints = PolylineUtil.createSplineApproximationPath(spline);
        return this.setShape(spline);
    }


    /**
     * Sets <code>this</code> path to a sequence of segments described by the points.
     *
     * @param points
     *            points to that lie along the generated path
     * @param bendRadius
     *            the radius of the bend points
     */
    public void setPathToRoundedBendPolyline(final Point2D[] points, final float bendRadius) {
        this.setPathToRoundedBendPolyline(null, points, bendRadius);
    }


    /**
     * Sets <code>this</code> path to a sequence of segments described by the points.
     *
     * @param theAssignedBounds
     *            the bounds being assigned to this figure based on which the
     *            child figures' bounds are determined
     * @param points
     *            points to that lie along the generated path
     * @param bendRadius
     *            the radius of the bend points
     */
    public void setPathToRoundedBendPolyline(final Rectangle2D theAssignedBounds,
            final Point2D[] points, final float bendRadius) {
        this.updateAssignedBoundsAndSetPathToLine(theAssignedBounds, points,
                () -> this.doSetPathToRoundedBendPolyline(points, bendRadius));
    }

    private boolean doSetPathToRoundedBendPolyline(final Point2D[] points, final float bendRadius) {
        if (points.length == 0) {
            return false;
        }

        this.lineType = ROUNDED_BENDS_POLYLINE;
        this.linePoints = points;
        return this.setShape(
            PolylineUtil.createRoundedBendsPolylinePath(new Path2D.Float(), points, bendRadius, this));
    }


    /**
     * Sets <code>this</code> path to a sequence of segments described by the points.
     *
     * @param points
     *            points to that lie along the generated path
     */
    public void setPathToPolyline(final Point2D[] points) {
        this.setPathToPolyline(null, points);
    }

    /**
     * Sets <code>this</code> path to a sequence of segments described by the points.
     *
     * @param theAssignedBounds
     *            the bounds being assigned to this figure based on which the
     *            child figures' bounds are determined
     * @param points
     *            points to that lie along the generated path
     */
    public void setPathToPolyline(final Rectangle2D theAssignedBounds, final Point2D[] points) {
        this.updateAssignedBoundsAndSetPathToLine(theAssignedBounds, points,
                () -> this.doSetPathToPolyline(points));
    }

    private boolean doSetPathToPolyline(final Point2D[] points) {
        if (points.length == 0) {
            return false;
        }

        this.lineType = POLYLINE;
        this.linePoints = points;
        return this.setShape(PolylineUtil.createPolylinePath(new Path2D.Float(), points));
    }


    /**
     * Sets <code>this</code> path to a sequence of segments described by the points.
     *
     * @param points
     *            points to that lie along the generated path
     */
    public void setPathToPolygon(final Point2D[] points) {
        this.setPathToPolygon(null, points);
    }

    /**
     * Sets <code>this</code> path to a sequence of segments described by the points.
     * @param theAssignedBounds
     *            the bounds being assigned to this figure based on which the
     *            child figures' bounds are determined
     * @param points
     *            points to that lie along the generated path
     */
    public void setPathToPolygon(final Rectangle2D theAssignedBounds, final Point2D[] points) {
        this.updateAssignedBoundsAndSetPathToLine(theAssignedBounds, points,
                () -> this.doSetPathToPolygon(points));
    }

    private boolean doSetPathToPolygon(final Point2D[] points) {
        if (points.length == 0) {
            return false;
        }

        this.lineType = POLYGON;
        this.linePoints = points;
        return this.setShape(PolylineUtil.createPolygonPath(new Path2D.Float(), points));
    }


    private void updateAssignedBoundsAndSetPathToLine(final Rectangle2D newAssignedBounds,
            final Point2D[] points, final Supplier<Boolean> setPathToLineSupplier) {

        final Rectangle2D oldAssignedBounds = this.assignedBounds;
        this.assignedBounds = newAssignedBounds;

        if (!setPathToLineSupplier.get().booleanValue()
                && !Objects.equal(oldAssignedBounds, newAssignedBounds)) {
            signalBoundsChanged();
        }
    }
}
