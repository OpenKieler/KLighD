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
package de.cau.cs.kieler.klighd.eclipse.microlayout;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import org.eclipse.elk.core.math.KVector;
import org.eclipse.elk.core.util.Pair;
import org.eclipse.swt.graphics.Point;

import de.cau.cs.kieler.klighd.kgraph.KGraphFactory;
import de.cau.cs.kieler.klighd.kgraph.KInsets;
import de.cau.cs.kieler.klighd.microlayout.Bounds;

/**
 * A convenience class representing floating-point-precise bounds and lots of helpful operations on
 * them.
 *
 * @author chsch
 *
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch
 */
public class EclipseBounds extends Bounds {

    /**
     * Constructs bounds with the given dimensions and (x,y) coordinates of (0,0).
     *
     * @param width
     *            the width
     * @param height
     *            the height
     */
    public EclipseBounds(final float width, final float height) {
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
    public EclipseBounds(final double width, final double height) {
        super(width, height);
    }

    /**
     * Constructs bounds from the dimensions of the given Bounds.
     *
     * @param bounds
     *            the Bounds to take the data from
     */
    public EclipseBounds(final EclipseBounds bounds) {
        super(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
    }

    /**
     * Constructs bounds from the dimensions of the given SWT Point with coordinates (0,0).
     *
     * @param point
     *            the SWT point to take width and height from
     */
    public EclipseBounds(final Point point) {
        super(new java.awt.Point(point.x, point.y));
    }

    /**
     * Constructs bounds from the dimensions of the given AWT geometry {@link Rectangle2D.Float}.
     *
     * @param rect
     *            the {@link Rectangle2D.Float} to take x, y, width, and height from
     */
    public EclipseBounds(final Rectangle2D.Float rect) {
        super(rect);
    }

    /**
     * Constructs bounds from the dimensions of the given AWT geometry {@link Rectangle2D.Float}.
     *
     * @param rect
     *            the {@link Rectangle2D} to take x, y, width, and height from
     */
    public EclipseBounds(final Rectangle2D rect) {
        super(rect);
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
    public EclipseBounds(final float x, final float y, final float width, final float height) {
        super(x, y, width, height);
    }
}
