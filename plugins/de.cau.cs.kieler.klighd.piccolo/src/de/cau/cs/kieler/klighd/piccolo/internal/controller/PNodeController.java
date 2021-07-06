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
package de.cau.cs.kieler.klighd.piccolo.internal.controller;

import java.awt.geom.Point2D;

import org.eclipse.swt.graphics.RGB;

import com.google.common.primitives.Floats;

import de.cau.cs.kieler.klighd.KlighdConstants;
import de.cau.cs.kieler.klighd.eclipse.EclipseKlighdConstants;
import de.cau.cs.kieler.klighd.krendering.HorizontalAlignment;
import de.cau.cs.kieler.klighd.krendering.KColor;
import de.cau.cs.kieler.klighd.krendering.KColoring;
import de.cau.cs.kieler.klighd.krendering.KPosition;
import de.cau.cs.kieler.klighd.krendering.KRenderingFactory;
import de.cau.cs.kieler.klighd.krendering.KRenderingPackage;
import de.cau.cs.kieler.klighd.krendering.KShadow;
import de.cau.cs.kieler.klighd.krendering.LineCap;
import de.cau.cs.kieler.klighd.krendering.LineJoin;
import de.cau.cs.kieler.klighd.krendering.LineStyle;
import de.cau.cs.kieler.klighd.krendering.Underline;
import de.cau.cs.kieler.klighd.krendering.VerticalAlignment;
import de.cau.cs.kieler.klighd.microlayout.Bounds;
import de.cau.cs.kieler.klighd.microlayout.PlacementUtil;
import de.cau.cs.kieler.klighd.piccolo.IKlighdNode.IKlighdFigureNode;
import de.cau.cs.kieler.klighd.piccolo.internal.util.RGBGradient;
import de.cau.cs.kieler.klighd.piccolo.internal.util.Styles;
import edu.umd.cs.piccolo.PNode;

/**
 * The abstract base class for Piccolo node controllers.<br />
 * <br />
 * A node controller is a facade on a specific Piccolo node, providing a generic interface to set
 * the bounds and other parameters of the node.
 *
 * @author mri, chsch
 *
 * @param <T>
 *            the type of the associated node
 */
public abstract class PNodeController<T extends IKlighdFigureNode> {

    /** the controller's node. */
    private T node;

    /**
     * Constructs a controller for a given Piccolo node.
     *
     * @param node
     *            the Piccolo node
     */
    public PNodeController(final T node) {
        this.node = node;
    }

    /**
     * Returns the associated node.
     *
     * @return the node
     */
    public final T getNode() {
        return node;
    }

    /**
     * Returns the associated node.
     *
     * @return the node
     */
    public final PNode getPNode() {
        return node.asPNode();
    }

    /**
     * Returns the {@link IKlighdFigureNode} being designated for applying rotations and translations.
     * <br>
     * Default implementation delegates to {@link #getNode()}.
     *
     * @return the {@link IKlighdFigureNode} being designated for applying rotations and translations.
     */
    public IKlighdFigureNode getTransformedNode() {
        return getNode();
    }

    /**
     * Returns the {@link PNode} being designated for applying rotations and translations.<br>
     * Default implementation delegates to {@link #getNode()}.
     *
     * @return the {@link PNode} being designated for applying rotations and translations.
     */
    public PNode getTransformedPNode() {
        return getTransformedNode().asPNode();
    }

    /**
     * Sets the bounds of the associated node.
     *
     * @param bounds
     *            the bounds
     */
    public abstract void setBounds(final Bounds bounds);

    /**
     * Sets the invisibility of the associated node.
     *
     * @param invisible
     *            the invisibility state
     */
    public void setInvisible(final boolean invisible) {
        final PNode figure = getNode().asPNode();
        if (invisible != figure.getOccluded()) {
            figure.setOccluded(invisible);

            // need the following call in order to get newly invisible figures to vanish
            // (PNode does not do it)
            figure.invalidatePaint();

            // question: is it correct to do the following when propagateToChildren is set?
            // figure.setVisible(!styles.invisibility.isInvisible());
        }
    }

    /**
     * Sets the foreground color of the associated node.
     *
     * @param color
     *            the foreground color
     * @param alpha
     *            the related alpha value
     */
    public void setForegroundColor(final RGB color, final int alpha) {
        // do nothing
    }

    /**
     * Sets the foreground gradient of the associated node.
     *
     * @param gradient
     *            the foreground gradient
     */
    public void setForegroundGradient(final RGBGradient gradient) {
        // do nothing
    }

    /**
     * Sets the background color of the associated node.
     *
     * @param color
     *            the background color
     * @param alpha
     *            the related alpha value
     */
    public void setBackgroundColor(final RGB color, final int alpha) {
        // do nothing
    }

    /**
     * Sets the background gradient of the associated node.
     *
     * @param gradient
     *            the background gradient
     */
    public void setBackgroundGradient(final RGBGradient gradient) {
        // do nothing
    }

    /**
     * Sets the line width of the associated node.
     *
     * @param lineWidth
     *            the line width
     */
    public void setLineWidth(final float lineWidth) {
        // do nothing
    }

    /**
     * Sets the line style of the associated node.
     *
     * @param lineStyle
     *            the line style
     * @param dashPattern
     *            the line dash pattern in case of {@link LineStyle#CUSTOM}
     * @param dashOffset
     *            the line dash offset in case of {@link LineStyle#CUSTOM}
     */
    public void setLineStyle(final LineStyle lineStyle, final float[] dashPattern,
            final float dashOffset) {
        // do nothing
    }

    /**
     * Sets the line cap style of the associated node.
     *
     * @param lineCap
     *            the line cap style
     */
    public void setLineCap(final LineCap lineCap) {
        // do nothing
    }

    /**
     * Sets the line join style of the associated node.
     *
     * @param lineJoin
     *            the line join style
     * @param miterLimit
     *            the miter limit in case of {@link LineJoin#JOIN_MITER}
     */
    public void setLineJoin(final LineJoin lineJoin, final float miterLimit) {
        // do nothing
    }


    private static final KPosition CENTER =
            KRenderingFactory.eINSTANCE.createKPosition().setPositions(
                KRenderingFactory.eINSTANCE.createKLeftPosition().setPosition(0, 1f / 2),
                KRenderingFactory.eINSTANCE.createKTopPosition().setPosition(0, 1f / 2)
            );

    private float prevRotation = 0f;
    private KPosition prevRotationAnchor = CENTER;
    private Point2D prevRotationPoint = new Point2D.Float();

    /**
     * Sets the rotation of the associated node.
     *
     * @param rotation
     *            the rotation
     * @param anchor
     *            the rotation anchor position
     */
    public void setRotation(final float rotation, final KPosition anchor) {
        if (rotation == 0f && prevRotation == 0f) {
            return;
        }

        final KPosition theAnchor = anchor != null ? anchor : CENTER;
        final PNode rotatedNode = getTransformedNode().asPNode();

        final Point2D point =
                PlacementUtil.evaluateKPosition(theAnchor, rotatedNode.getBoundsReference(), true);

        if (prevRotation == 0f || point.equals(prevRotationPoint)) {

            final float diff = rotation - prevRotation;
            if (diff != 0) {
                // with the following statement 'rotatedNode's transform is manipulated s.t. the rotation
                //  is applied first, and the existing afterwards, see javadoc of 'concatenate()'
                rotatedNode.getTransformReference(true).rotate(
                        Math.toRadians(diff), point.getX(), point.getY());
            }
        } else {
            // according to the above conditions 'prevRotation' is unequal to zero
            //  hence revert the existing rotation first ...
            rotatedNode.getTransformReference(true).rotate(
                    Math.toRadians(-prevRotation), prevRotationPoint.getX(), prevRotationPoint.getY());

            // ... and apply the new angle if unequal to zero
            if (rotation != 0) {
                rotatedNode.getTransformReference(true).rotate(
                        Math.toRadians(rotation), point.getX(), point.getY());
            }
        }

        // Remember the rotation in this memory since this rotation needs to be reverted
        //  (merged) with the subsequent rotation, since these rotations are absolute.
        prevRotation = rotation;
        prevRotationAnchor = theAnchor;
        prevRotationPoint = point;
    }

    /**
     * Re-applies the last rotation to the controlled {@link #getNode() node}.<br>
     * This method is just for internal use and not to be called by application code.
     */
    public void applyRotation() {
        final float rotation = prevRotation;
        prevRotation = 0;
        setRotation(rotation, prevRotationAnchor);
    }


    /**
     * Sets the shadow of the associated node.
     *
     * @param color
     *            the shadow color
     */
    public void setShadow(final KShadow color) {
        // do nothing
    }

    /**
     * Sets the horizontal alignment of the associated node.
     *
     * @param alignment
     *            the horizontal alignment
     */
    public void setHorizontalAlignment(final HorizontalAlignment alignment) {
        // do nothing
    }

    /**
     * Sets the vertical alignment of the associated node.
     *
     * @param alignment
     *            the vertical alignment
     */
    public void setVerticalAlignment(final VerticalAlignment alignment) {
        // do nothing
    }

    /**
     * Sets the font name of the associated node (most likely some kind of text).
     *
     * @param fontName
     *            the font name
     */
    public void setFontName(final String fontName) {
        // do nothing
    }

    /**
     * Sets the font size of the associated node (most likely some kind of text).
     *
     * @param fontSize
     *            the font size
     */
    public void setFontSize(final int fontSize) {
        // do nothing
    }

    /**
     * Sets the italic property for the associated node (most likely some kind of text).
     *
     * @param italic
     *            the italic property
     */
    public void setItalic(final boolean italic) {
        // do nothing
    }

    /**
     * Sets the bold property for the associated node (most likely some kind of text).
     *
     * @param bold
     *            the bold property
     */
    public void setBold(final boolean bold) {
        // do nothing
    }

    /**
     * Sets the underlining property for the associated node (most likely some kind of text).
     *
     * @param underline
     *            the underline property
     * @param color
     *            the underline color
     */
    public void setUnderline(final Underline underline, final RGB color) {
        // do nothing
    }

    /**
     * Sets the strikeout property for the associated node (most likely some kind of text).
     *
     * @param strikeout
     *            the underline property
     * @param color
     *            the strikeout color
     */
    public void setStrikeout(final boolean strikeout, final RGB color) {
        // do nothing
    }

    /**
     * Applies changes to the associated node.<br>
     * <br>
     * This can be used to apply several style changes at once to prevent costly redundancy.
     *
     * @param styles
     *            A compound {@link Styles} field to infer the data from.
     */
    // SUPPRESS CHECKSTYLE NEXT Length
    public void applyChanges(final Styles styles) {

        // apply rotation first since it should be applied even if the node is not visible
        //  (application of other styles will be skipped in that case - see below)
        if (styles.rotation != null) {
            this.setRotation(styles.rotation.getRotation(), styles.rotation.getRotationAnchor());
        } else {
            this.setRotation(0, null);
        }

        // apply invisibility
        if (styles.invisibility != null) {
            this.setInvisible(styles.invisibility.isInvisible());
            if (styles.invisibility.isInvisible()) {
                // in case the node is invisible, we can skip the remaining definitions
                return;
            }
        }

        // apply foreground coloring
        if (styles.foreground != null) {
            final KColor color = styles.foreground.getColor();
            final KColor targetColor = styles.foreground.getTargetColor();
            if (targetColor != null && color != null) {
                this.setForegroundGradient(toRGBGradient(styles.foreground));
            } else if (color != null) {
                this.setForegroundColor(toRGB(color), styles.foreground.getAlpha());
            } else {
                this.setForegroundColor((RGB) null, KlighdConstants.ALPHA_FULL_OPAQUE);
            }

        } else {
            this.setForegroundColor(EclipseKlighdConstants.BLACK, KlighdConstants.ALPHA_FULL_OPAQUE);
        }

        // apply background coloring
        if (styles.background != null) {
            final KColor color = styles.background.getColor();
            final KColor targetColor = styles.background.getTargetColor();

            if (targetColor != null && color != null) {
                this.setBackgroundGradient(toRGBGradient(styles.background));
            } else if (color != null) {
                this.setBackgroundColor(toRGB(color), styles.background.getAlpha());
            } else {
                this.setBackgroundColor((RGB) null, KlighdConstants.ALPHA_FULL_OPAQUE);
                this.setBackgroundGradient(null);
            }
        } else {
            this.setBackgroundColor((RGB) null, KlighdConstants.ALPHA_FULL_OPAQUE);
            this.setBackgroundGradient(null);
        }

        // apply line width
        if (styles.lineWidth != null) {
            this.setLineWidth(styles.lineWidth.getLineWidth());
        } else {
            this.setLineWidth(1);
        }

        // apply line style
        if (styles.lineStyle != null) {
            float[] pattern = styles.lineStyle.getLineStyle() == LineStyle.CUSTOM
                    ? Floats.toArray(styles.lineStyle.getDashPattern()) : null;

            this.setLineStyle(
                    styles.lineStyle.getLineStyle(), pattern, styles.lineStyle.getDashOffset());

        } else {
            this.setLineStyle(LineStyle.SOLID, null, 0f);
        }

        // apply line cap style
        if (styles.lineCap != null) {
            this.setLineCap(styles.lineCap.getLineCap());
        } else {
            this.setLineCap(LineCap.get(0));
        }

        // apply line join style
        if (styles.lineJoin != null) {
            if (styles.lineJoin.getLineJoin() == LineJoin.JOIN_MITER) {
                this.setLineJoin(styles.lineJoin.getLineJoin(), styles.lineJoin.getMiterLimit());
            } else {
                this.setLineJoin(styles.lineJoin.getLineJoin(), (Float) KRenderingPackage.eINSTANCE
                        .getKLineJoin_MiterLimit().getDefaultValue());
            }
        } else {
            this.setLineJoin(LineJoin.get(0), (Float) KRenderingPackage.eINSTANCE
                    .getKLineJoin_MiterLimit().getDefaultValue());
        }

        // apply shadow
        if (styles.shadow != null) {
            this.setShadow(styles.shadow);
        } else {
            this.setShadow(null);
        }

        // apply horizontal alignment
        if (styles.horizontalAlignment != null) {
            this.setHorizontalAlignment(styles.horizontalAlignment.getHorizontalAlignment());
        } else {
            this.setHorizontalAlignment(HorizontalAlignment.CENTER);
        }

        // apply vertical alignment
        if (styles.verticalAlignment != null) {
            this.setVerticalAlignment(styles.verticalAlignment.getVerticalAlignment());
        } else {
            this.setVerticalAlignment(VerticalAlignment.CENTER);
        }

        // apply font name
        if (styles.fontName != null) {
            this.setFontName(styles.fontName.getName());
        } else {
            this.setFontName(KlighdConstants.DEFAULT_FONT_NAME);
        }

        // apply font size
        if (styles.fontSize != null) {
            this.setFontSize(styles.fontSize.getSize());
        } else {
            this.setFontSize(KlighdConstants.DEFAULT_FONT_SIZE);
        }

        // apply the italic property
        if (styles.italic != null) {
            this.setItalic(styles.italic.isItalic());
        } else {
            this.setItalic(false);
        }

        // apply the bold property
        if (styles.bold != null) {
            this.setBold(styles.bold.isBold());
        } else {
            this.setBold(false);
        }

        // apply the underlined property
        if (styles.underline != null) {
            this.setUnderline(styles.underline.getUnderline(), toRGB(styles.underline.getColor()));
        } else {
            this.setUnderline(null, EclipseKlighdConstants.BLACK);
        }

        // apply the strikeout property
        if (styles.strikeout != null) {
            this.setStrikeout(styles.strikeout.getStruckOut(), toRGB(styles.strikeout.getColor()));
        } else {
            this.setStrikeout(false, EclipseKlighdConstants.BLACK);
        }
    }

    /**
     * Convenience transformation converting a {@link KColor} into an {@link RGB}.
     *
     * TODO Install same caching in order to avoid unnecessary object creation.
     *
     * @param color
     *            the {@link KColor} to be converted
     * @return null if<code>color = null<code>, the related {@link RGB} otherwise
     */
    public RGB toRGB(final KColor color) {
        final int maxValue = 255;

        if (color == null) {
            return null;
        }

        final int red = color.getRed() < maxValue ? color.getRed() : maxValue;
        final int green = color.getGreen() < maxValue ? color.getGreen() : maxValue;
        final int blue = color.getBlue() < maxValue ? color.getBlue() : maxValue;

        return new RGB(red, green, blue);
    }

    /**
     * Convenience transformation converting a {@link KColoring} with defined target color into an
     * {@link RGBGradient}.
     *
     * @param coloring
     *            the {@link KColoring} to be converted
     * @return null if<code>color = null<code>, the related {@link RGB} otherwise
     */
    public RGBGradient toRGBGradient(final KColoring<?> coloring) {
        if (coloring == null || coloring.getColor() == null || coloring.getTargetColor() == null) {
            return null;
        } else {
            return new RGBGradient(toRGB(coloring.getColor()), coloring.getAlpha(),
                    toRGB(coloring.getTargetColor()), coloring.getTargetAlpha(),
                    coloring.getGradientAngle());
        }
    }
}
