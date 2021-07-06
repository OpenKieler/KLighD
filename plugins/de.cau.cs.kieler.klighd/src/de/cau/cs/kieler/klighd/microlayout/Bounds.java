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
package de.cau.cs.kieler.klighd.microlayout;

import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import org.eclipse.elk.core.math.KVector;
import org.eclipse.elk.core.util.Pair;

import de.cau.cs.kieler.klighd.kgraph.KGraphFactory;
import de.cau.cs.kieler.klighd.kgraph.KInsets;

/**
 * A convenience class representing floating-point-precise bounds and lots of helpful operations on
 * them.
 *
 * @author chsch
 *
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch
 */
public class Bounds {

    // CHECKSTYLEOFF Visibility: values shall be directly accessible by the classes in the same package!

    /** the x-coordinate. */
    private float x;
    /** the y-coordinate. */
    private float y;
    /** the width. */
    private float width;
    /** the height. */
    private float height;
    /**
     * the insets used to transport the position of a ChildAreaCell from estimateGridSize to
     * calculateInsets method.
     */
    private KInsets insets = null;

    // CHECKSTYLEON Visibility

    /**
     * Constructs bounds with the given dimensions and (x,y) coordinates of (0,0).
     *
     * @param width
     *            the width
     * @param height
     *            the height
     */
    public Bounds(final float width, final float height) {
        this.setX(0);
        this.setY(0);
        this.setWidth(width);
        this.setHeight(height);
        this.setInsets(KGraphFactory.eINSTANCE.createKInsets());
    }

    /**
     * Constructs bounds with the given dimensions and (x,y) coordinates of (0,0).
     *
     * @param width
     *            the width
     * @param height
     *            the height
     */
    public Bounds(final double width, final double height) {
        this.setX(0);
        this.setY(0);
        this.setWidth((float) width);
        this.setHeight((float) height);
        this.setInsets(KGraphFactory.eINSTANCE.createKInsets());
    }

    /**
     * Constructs bounds from the dimensions of the given Bounds.
     *
     * @param bounds
     *            the Bounds to take the data from
     */
    public Bounds(final Bounds bounds) {
        this.setX(bounds.getX());
        this.setY(bounds.getY());
        this.setWidth(bounds.getWidth());
        this.setHeight(bounds.getHeight());
        this.setInsets(KGraphFactory.eINSTANCE.createKInsets());
    }

    /**
     * Constructs bounds from the dimensions of the given SWT Point with coordinates (0,0).
     *
     * @param point
     *            the SWT point to take width and height from
     */
    public Bounds(final Point point) {
        this.setWidth(point.x);
        this.setHeight(point.y);
        this.setInsets(KGraphFactory.eINSTANCE.createKInsets());
    }

    /**
     * Constructs bounds from the dimensions of the given AWT geometry {@link Rectangle2D.Float}.
     *
     * @param rect
     *            the {@link Rectangle2D.Float} to take x, y, width, and height from
     */
    public Bounds(final Rectangle2D.Float rect) {
        this.setX(rect.x);
        this.setY(rect.y);
        this.setWidth(rect.width);
        this.setHeight(rect.height);
        this.setInsets(KGraphFactory.eINSTANCE.createKInsets());
    }

    /**
     * Constructs bounds from the dimensions of the given AWT geometry {@link Rectangle2D.Float}.
     *
     * @param rect
     *            the {@link Rectangle2D} to take x, y, width, and height from
     */
    public Bounds(final Rectangle2D rect) {
        this.setX((float) rect.getX());
        this.setY((float) rect.getY());
        this.setWidth((float) rect.getWidth());
        this.setHeight((float) rect.getHeight());
        this.setInsets(KGraphFactory.eINSTANCE.createKInsets());
    }

    /**
     * Constructs bounds from the given coordinates and dimensions.
     *
     * @param x
     *            the x-coordinate
     * @param y
     *            the y-coordinate
     * @param width
     *            the width
     * @param height
     *            the height
     */
    public Bounds(final float x, final float y, final float width, final float height) {
        this.setX(x);
        this.setY(y);
        this.setWidth(width);
        this.setHeight(height);
        this.setInsets(KGraphFactory.eINSTANCE.createKInsets());
    }

    /**
     * Takes the data of the given bounds.
     *
     * @param bounds
     *            the bounds to take the data from
     */
    void setBounds(final Bounds bounds) {
        this.setX(bounds.getX());
        this.setY(bounds.getY());
        this.setWidth(bounds.getWidth());
        this.setHeight(bounds.getHeight());
        this.setInsets(KGraphFactory.eINSTANCE.createKInsets());
    }

    /**
     * Takes the data of the given bounds.
     *
     * @param bounds
     *            the bounds to take the data from
     */
    void setBounds(final Rectangle2D bounds) {
        this.setX((float) bounds.getX());
        this.setY((float) bounds.getY());
        this.setWidth((float) bounds.getWidth());
        this.setHeight((float) bounds.getHeight());
        this.setInsets(KGraphFactory.eINSTANCE.createKInsets());
    }

    /**
     * Getter for X coordinate.
     *
     * @return height
     */
    public float getX() {
        return this.x;
    }

    /**
     * Getter for Y coordinate.
     *
     * @return width
     */
    public float getY() {
        return this.y;
    }

    /**
     * Getter used in JUnit test of the placement logic.
     *
     * @return height
     */
    public float getHeight() {
        return this.height;
    }

    /**
     * Getter used in JUnit test of the placement logic.
     *
     * @return width
     */
    public float getWidth() {
        return this.width;
    }

    /**
     * @return true if <code>width</code> and <code>height</code> of <code>this</code> bounds equal
     *         zero.
     */
    public boolean isEmpty() {
        return this.getWidth() == 0f && this.getHeight() == 0f;
    }

    /**
     * Moves <code>this</code> {@link Bounds} object by adding the provided values to the
     * <code>horDir</code> and <code>vertDir</code> components.
     *
     * @param horDir
     *            the horizontal part
     * @param vertDir
     *            the vertical part
     * @return <code>this</code> {@link Bounds} for convenience
     */
    public Bounds move(final float horDir, final float vertDir) {
        this.setX(this.getX() + horDir);
        this.setY(this.getY() + vertDir);
        return this;
    }


    /**
     * Transforms the current {@link Bounds} object in an AWT geometry {@link Rectangle2D}.
     *
     * @return a related {@link Rectangle2D}
     */
    public Rectangle2D toRectangle2D() {
        return new Rectangle2D.Float(this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }

    /**
     * Transforms the current {@link Bounds} object in an AWT geometry {@link Ellipse2D}.
     *
     * @return a related {@link Ellipse2D}
     */
    public Ellipse2D toEllipse2D() {
        return new Ellipse2D.Float(this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }

    /**
     * Updates the coordinates of <code>shape</code> according to those of <code>this</code>.
     *
     * @param <S> the type of the concrete {@link RectangularShape} {@code shape}
     * @param shape
     *            the {@link RectangularShape} to be updated
     * @return <code>shape</code> for convenience
     */
    public <S extends RectangularShape> S setBoundsOf(final S shape) {
        shape.setFrame(this.getX(), this.getY(), this.getWidth(), this.getHeight());
        return shape;
    }

    /**
     * Getter to access the Insets of this Bounds.
     *
     * @return width
     */
    public KInsets getInsets() {
        return this.insets;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "(" + this.getX() + "," + this.getY() + "|" + this.getWidth() + "," + this.getHeight() + ")";
    }

    /**
     * Compares two {@link Bounds} objects wrt. equality of their widths and heights
     *
     * @param b1
     *            the first {@link Bounds}
     * @param b2
     *            the second {@link Bounds}
     *
     * @return a {@link Pair} of {@link Boolean} denoting the width and height equality
     */
    public static Pair<Boolean, Boolean> compare(final Bounds b1, final Bounds b2) {
        return compare(b1, b2, 0f);
    }

    /**
     * Compares two {@link Bounds} objects wrt. equality of their widths and heights whilst
     * tolerating the provided <code>delta</code>.
     *
     * @param b1
     *            the first {@link Bounds}
     * @param b2
     *            the second {@link Bounds}
     * @param delta
     *            the delta to accept while determining the equality of width and height
     *
     * @return a {@link Pair} of {@link Boolean} denoting the width and height equality
     */
    public static Pair<Boolean, Boolean> compare(final Bounds b1, final Bounds b2, final float delta) {
        final boolean widthEqual = Math.abs(b1.getWidth() - b2.getWidth()) <= delta;
        final boolean heightEqual = Math.abs(b1.getHeight() - b2.getHeight()) <= delta;

        return Pair.of(widthEqual, heightEqual);
    }

    /**
     * Determines component-wise the maximum of the given bounds <code>b1</code> and
     * <code>b2</code>. The function <b>modifies</b> <code>b1</code>.
     *
     * @param b1
     *            the first {@link Bounds} object
     * @param b2
     *            the second {@link Bounds} object
     * @return the object b1 with modified <code>width</code> and <code>height</code> data
     *         containing the particular maximal values.
     */
    public static Bounds max(final Bounds b1, final Bounds b2) {
        b1.setWidth(Math.max(b1.getWidth(), b2.getWidth()));
        b1.setHeight(Math.max(b1.getHeight(), b2.getHeight()));
        return b1;
    }

    /**
     * Determines component-wise the minimum of the given bounds <code>b1</code> and
     * <code>b2</code>. The function <b>modifies</b> <code>b1</code>.
     *
     * @param b1
     *            the first {@link Bounds} object
     * @param b2
     *            the second {@link Bounds} object
     * @return the object b1 with modified <code>width</code> and <code>height</code> data
     *         containing the particular minimal values.
     */
    public static Bounds min(final Bounds b1, final Bounds b2) {
        b1.setWidth(Math.min(b1.getWidth(), b2.getWidth()));
        b1.setHeight(Math.min(b1.getHeight(), b2.getHeight()));
        return b1;
    }

    /**
     * Constructs a new bounds object with the data of the given bounds.
     *
     * @param bounds
     *            bounds
     * @return the desired {@link Bounds} object
     */
    public static Bounds of(final Bounds bounds) {
        return new Bounds(bounds);
    }

    /**
     * Constructs bounds with the given dimensions and (width, height) coordinates (0,0).
     *
     * @param width
     *            the width
     * @param height
     *            the height
     * @return the desired {@link Bounds} object
     */
    public static Bounds of(final float width, final float height) {
        return new Bounds(width, height);
    }

    /**
     * Constructs bounds with the given coordinates and (x,y) dimensions (width, height).
     *
     * @param x
     *            the x-coordinate
     * @param y
     *            the y-coordinate
     * @param width
     *            the width
     * @param height
     *            the height
     * @return the desired {@link Bounds} object
     */
    public static Bounds of(final float x, final float y, final float width, final float height) {
        return new Bounds(x, y, width, height);
    }

    /**
     * Constructs bounds from the dimensions of the given AWT geometry {@link Rectangle2D}.
     *
     * @param rect
     *            the {@link Rectangle2D} to take x, y, width, and height from
     * @return the desired {@link Bounds} object
     */
    public static Bounds of(final Rectangle2D rect) {
        return new Bounds(rect);
    }

    /**
     * Constructs bounds from the given {@link KVector}.
     *
     * @param sizeVector
     *            the {@link KVector} to take x as width, and  y as height from
     * @return the desired {@link Bounds} object
     */
    public static Bounds of(final KVector sizeVector) {
        return new Bounds(sizeVector.x, sizeVector.y);
    }

    /**
     * Creates an immutable copy of the given <code>bounds</code>.
     *
     * @param bounds
     *            the {@link Bounds} object providing the atomic values
     * @return the desired immutable copy
     */
    public static Bounds immutableCopy(final Bounds bounds) {
        return new ImmutableBounds(bounds);
    }

    /**
     * @param width the width to set
     */
    public void setWidth(float width) {
        this.width = width;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(float height) {
        this.height = height;
    }

    /**
     * @param y the y to set
     */
    public void setY(float y) {
        this.y = y;
    }

    /**
     * @param x the x to set
     */
    public void setX(float x) {
        this.x = x;
    }

    /**
     * @param insets the insets to set
     */
    private void setInsets(KInsets insets) {
        this.insets = insets;
    }

    /**
     * An immutable variant of {@link Bounds} that can be used to make sure the bounds are not
     * changed. Note that this can be ensured for usages in different packages only. Since the fields in this class are
     * package private, any instance of this ImmutableBounds class that is fed into KLighD methods from this package may
     * still change the bounds, despite the naming of this class.
     *
     * @author chsch
     */
    public static class ImmutableBounds extends Bounds {

        /**
         * Constructs bounds with the given dimensions and (x,y) coordinates of (0,0).
         *
         * @param width
         *            the width
         * @param height
         *            the height
         */
        public ImmutableBounds(final float width, final float height) {
            super(width, height);
        }

        /**
         * Constructs bounds with the given dimensions and (x,y) coordinates of (0,0).
         *
         * @param width
         *            the width
         * @param height
         *            the height
         */
        public ImmutableBounds(final double width, final double height) {
            super(width, height);
        }

        /**
         * Constructs bounds from the dimensions of the given Bounds.
         *
         * @param bounds
         *            the Bounds to take the data from
         */
        public ImmutableBounds(final Bounds bounds) {
            super(bounds);
        }

        private static final String MSG = "This bounds object is immutable! Modifying is not allowed.";

        @Override
        void setBounds(final Bounds bounds) {
            throw new UnsupportedOperationException(MSG);
        }

        @Override
        void setBounds(final Rectangle2D rect) {
            throw new UnsupportedOperationException(MSG);
        }

        @Override
        public Bounds move(final float hor, final float vert) {
            throw new UnsupportedOperationException(MSG);
        }
    }
}
