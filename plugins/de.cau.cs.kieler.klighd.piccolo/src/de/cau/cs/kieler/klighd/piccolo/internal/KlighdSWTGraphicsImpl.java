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
package de.cau.cs.kieler.klighd.piccolo.internal;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Display;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.cau.cs.kieler.klighd.Klighd;
import de.cau.cs.kieler.klighd.KlighdConstants;
import de.cau.cs.kieler.klighd.eclipse.EclipseKlighdConstants;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KlighdPaths;
import de.cau.cs.kieler.klighd.piccolo.internal.util.RGBGradient;
import de.cau.cs.kieler.klighd.util.KlighdSemanticDiagramData;
import edu.umd.cs.piccolo.util.PAffineTransform;
import edu.umd.cs.piccolox.swt.SWTShapeManager;

/**
 * Standard implementation of {@link de.cau.cs.kieler.klighd.piccolo.KlighdSWTGraphics
 * KlighdSWTGraphics}. It's aim is to get independent from Piccolo2Ds
 * {@link edu.umd.cs.piccolox.swt.SWTGraphics2D SWTGraphics2D}.
 *
 * @author chsch
 */
public class KlighdSWTGraphicsImpl extends Graphics2D implements KlighdSWTGraphicsEx {

    private static float DEFAULT_DISPLAY_DPI = KlighdConstants.DEFAULT_DISPLAY_DPI;

    // SUPPRESS CHECKSTYLE NEXT 40 Visibility

    /** The {@link Device} to draw on. */
    protected Device device;

    /** The {@link Device} to use while instantiating {@link Font Fonts},
     *   in non-diagram parts like export brandings. */
    protected Device fontCreationDevice;

    /** The {@link GC} to draw on. */
    protected GC gc;

    /** The resolution of the drawing device, used for compensating the display scale when drawing text. */
    protected Point2D.Float dpiScale;

    /** Internal flag for easy determination whether text size compensation is needed. */
    protected boolean doDisplayScaleCompensation = false;

    /** An internal SWT {@link Rectangle} used for clip handling computations. */
    protected Transform swtTransform;

    // SUPPRESS CHECKSTYLE NEXT 2 MagicNumber
    /** The bit position of {@link GC#DRAW_OFFSET} required for avoiding Eclipse bugs 335769 & 253670.*/
    private static final int DRAW_OFFSET_BIT = Klighd.IS_WINDOWS ? 14 : 9;

    /** A {@link TextLayout} used to draw styled texts (e.g. those with underline and/or strikeout). */
    protected TextLayout textLayout;

    /** Indicates a self-created textLayout that is to be disposed while disposing this instance. */
    protected boolean disposeTextLayout = false;

    /** The current font to use when drawing text. */
    protected Font curFont;

    /** The current text style to use when drawing text. */
    protected TextStyle curTextStyle;

    /** The state w.r.t. using the text style when drawing text. */
    protected boolean useTextStyle = false;

    /** The max line width of text blocks, if exceeded text will be wrapped. */
    protected int textLineWidth = -1;

    /**
     * Constructor.
     * 
     * @param device
     *            {@link Device} onto which drawing operations are performed on, must not be
     *            <code>null</code>
     */
    public KlighdSWTGraphicsImpl(final Device device) {
        this(null, device, null);
    }

    /**
     * Constructor.
     * 
     * @param gc
     *            The {@link GC Eclipse Graphics Context} onto which all Graphics2D operations are
     *            delegating, must not be <code>null</code>
     */
    public KlighdSWTGraphicsImpl(final GC gc) {
        this(gc, gc.getDevice(), null);
    }

    /**
     * Constructor.
     * 
     * @param gc
     *            The {@link GC Eclipse Graphics Context} onto which all Graphics2D operations are
     *            delegating, must not be <code>null</code>
     * @param fontCreationDevice
     *            {@link Device} to be used for instantiation of {@link Font Fonts} while drawing
     *            non-diagram parts like export brandings (see {@link #stopFontCaching()}), may be
     *            <code>null</code>
     */
    public KlighdSWTGraphicsImpl(final GC gc, final Device fontCreationDevice) {
        this(gc, gc.getDevice(), fontCreationDevice);
    }

    /**
     * Constructor.
     * 
     * @param gc
     *            The Eclipse Graphics Context onto which all Graphics2D operations are delegating
     * @param device
     *            {@link Device} onto which ultimately all gc operations are drawn onto
     * @param fontCreationDevice
     *            {@link Device} to be used for instantiation of {@link Font Fonts} while drawing
     *            non-diagram parts like export brandings (see {@link #stopFontCaching()}), may be
     *            <code>null</code>
     */
    public KlighdSWTGraphicsImpl(final GC gc, final Device device, final Device fontCreationDevice) {
        this(device, gc, fontCreationDevice, new TextLayout(device));
        this.disposeTextLayout = true;
    }

    /**
     * Constructor for SWTGraphics2D.
     *
     * @param device
     *            Device onto which ultimately all {@link GC} operations are drawn onto
     * @param gc
     *            The Eclipse Graphics Context onto which all Graphics2D operations are
     *            delegating
     * @param tl
     *            The Eclipse Graphics Context onto which all Graphics2D operations are
     *            delegating
     */
    protected KlighdSWTGraphicsImpl(final Device device, final GC gc, final TextLayout tl) {
        this(device, gc, null, tl);
    }

    /**
     * Constructor for SWTGraphics2D.
     *
     * @param device
     *            Device onto which ultimately all {@link GC} operations are drawn onto
     * @param gc
     *            The Eclipse Graphics Context onto which all Graphics2D operations are
     *            delegating
     * @param fontCreationDevice
     *            {@link Device} to be used for instantiation of {@link Font Fonts} while drawing
     *            non-diagram parts like export brandings (see {@link #stopFontCaching()}), may be
     *            <code>null</code>
     * @param tl
     *            The Eclipse Graphics Context onto which all Graphics2D operations are
     *            delegating
     */
    protected KlighdSWTGraphicsImpl(final Device device, final GC gc,
            final Device fontCreationDevice, final TextLayout tl) {
        this.device = device;
        this.gc = gc;
        this.fontCreationDevice = fontCreationDevice;

        this.swtTransform = new Transform(device);
        this.textLayout = tl;
        this.transform = new AffineTransform();

        this.initializeDotDensityCompensation();
        this.initializeTransform();
    }

    /**
     * Initializes the {@link #transform(AffineTransform)} based on the current {@link #gc}'s
     * {@link Transform}.
     */
    private void initializeTransform() {
        if (gc != null) {
            gc.getTransform(swtTransform);
            final float[] elements = new float[6]; // SUPPRESS CHECKSTYLE MagicNumber
            this.swtTransform.getElements(elements);
            this.transform.setTransform(new AffineTransform(elements));
        }
    }

    /**
     * Initializes the cached display {@link #dpiScale} value and {@link #doDisplayScaleCompensation}
     * flag.
     */
    private void initializeDotDensityCompensation() {
        // cached fonts (those used in diagrams) shall always be created
        //  in context of the display, see #setFont(FontData, int);
        // hence always stick to the display for computing the compensation factor
        this.dpiScale = Display.getCurrent() == null ? new Point2D.Float(1, 1)
                : getDefaultDPIdevidedBy(Display.getCurrent().getDPI());

        // do only compensate the display scale when drawing on screen or diagram
        //  parts on some printer, don't compensate while drawing branding parts on printouts!
        this.doDisplayScaleCompensation =
                cacheFonts && !Klighd.isSuppressDisplayScaleCompensationWhileHandlingText()
                        && (this.dpiScale.x != 1f || this.dpiScale.y != 1f);
    }

    private Point2D.Float getDefaultDPIdevidedBy(final Point point) {
        // For some reason we don't need to care about 'DPIUtil#deviceZoom' here,
        //  which is applied to coordinates by the 'autoScale...' methods of 'DPIUtil'.
        // Experiments on windows showed that reverting the 'autoScale'-based adjustment
        //  in 'gc.getDevice().getDPI()' leads to wrong text drawings.
        // Hence, the 'autoScale' adjustment must be applied to the font size in native code,
        //  as I couldn't spot any place in the (win32 specific) Java implementation.
        return new Point2D.Float(DEFAULT_DISPLAY_DPI / point.x, DEFAULT_DISPLAY_DPI / point.y);
    }

    /**
     * {@inheritDoc}
     */
    public Device getDevice() {
        return this.device;
    }

    /**
     * {@inheritDoc}
     */
    public void setDevice(final Device theDevice) {
        this.device = theDevice;
    }

    /**
     * {@inheritDoc}
     */
    public GC getGC() {
        return this.gc;
    }

    /**
     * {@inheritDoc}
     */
    public void setGC(final GC theGc) {
        this.gc = theGc;
        this.gc.setAntialias(SWT.ON);

        this.initializeDotDensityCompensation();
        this.initializeTransform();
    }

    /**
     * {@inheritDoc}
     */
    public LineAttributes getLineAttributes() {
        return gc.getLineAttributes();
    }

    /**
     * {@inheritDoc}
     */
    public void setLineAttributes(final LineAttributes attributes) {
        // the following line keeps the lineWidth in mind in order to
        //  be able to adjust it according to the zoom factor
        this.setLineWidth(attributes.width);

        gc.setLineAttributes(attributes);
    }

    private float lineWidth = EclipseKlighdConstants.DEFAULT_LINE_ATTRIBUTES.width;

    /**
     * {@inheritDoc}
     */
    public float getLineWidth() {
        return this.lineWidth;
    }

    /**
     * {@inheritDoc}
     */
    public void setLineWidth(final float lineWidth) {
        this.lineWidth = lineWidth;
    }

    /**
     * {@inheritDoc}
     */
    public int getAlpha() {
        return gc.getAlpha();
    }

    /**
     * {@inheritDoc}
     */
    public void setAlpha(final int alpha) {
        gc.setAlpha(alpha);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    public RGB getStrokeColor() {
        return gc.getForeground().getRGB();
    }

    /**
     * {@inheritDoc}
     */
    public void setStrokeColor(final RGB foregroundColor) {
        gc.setForeground(getColor(foregroundColor));
    }

    /**
     * {@inheritDoc}
     */
    public RGB getFillColor() {
        return gc.getBackground().getRGB();
    }

    /**
     * {@inheritDoc}
     */
    public void setFillColor(final RGB backgroundColor) {
        gc.setBackground(getColor(backgroundColor));
    }

    private Pattern lastPattern = null;

    /**
     * {@inheritDoc}
     */
    public void setStrokePattern(final RGBGradient gradient, final Rectangle2D bounds) {
        final Point2D[] points = computePatternPoints(bounds, gradient.getAngle());

        final float curAlpha = this.getAlpha();
        final int alpha1 = (int) (gradient.getAlpha1() * (curAlpha / KlighdConstants.ALPHA_FULL_OPAQUE));
        final int alpha2 = (int) (gradient.getAlpha2() * (curAlpha / KlighdConstants.ALPHA_FULL_OPAQUE));

        if (this.lastPattern != null) {
            this.lastPattern.dispose();
        }

        this.lastPattern = new Pattern(this.device,
                (float) points[0].getX(), (float) points[0].getY(), (float) points[1].getX(),
                (float) points[1].getY(), this.getColor(gradient.getColor1()), alpha1,
                this.getColor(gradient.getColor2()), alpha2);
        gc.setForegroundPattern(this.lastPattern);
    }

    /**
     * {@inheritDoc}
     */
    public void setFillPattern(final RGBGradient gradient, final Rectangle2D bounds) {
        final Point2D[] points = computePatternPoints(bounds, gradient.getAngle());

        final float curAlpha = this.getAlpha();
        final int alpha1 = (int) (gradient.getAlpha1() * (curAlpha / KlighdConstants.ALPHA_FULL_OPAQUE));
        final int alpha2 = (int) (gradient.getAlpha2() * (curAlpha / KlighdConstants.ALPHA_FULL_OPAQUE));

        if (this.lastPattern != null) {
            this.lastPattern.dispose();
        }

        this.lastPattern = new Pattern(this.device,
                (float) points[0].getX(), (float) points[0].getY(), (float) points[1].getX(),
                (float) points[1].getY(), this.getColor(gradient.getColor1()), alpha1,
                this.getColor(gradient.getColor2()), alpha2);
        gc.setBackgroundPattern(this.lastPattern);
    }


    // font configuration
    
    /**
     * {@inheritDoc}
     */
    public FontData getFontData() {
        return gc.getFont().getFontData()[0];
    }

    /** Map from FontData to SWT Fonts. */
    protected static final HashMap<FontData, Font> FONT_CACHE = new HashMap<FontData, Font>();

    private boolean cacheFonts = true;
    private List<org.eclipse.swt.graphics.Font> temporaryFonts = null;


    /**
     * {@inheritDoc}
     */
    public void stopFontCaching() {
        cacheFonts = false;
        doDisplayScaleCompensation = false;
    }

    /**
     * {@inheritDoc}
     */
    public void resumeFontCaching() {
        cacheFonts = true;

        for (Font font : temporaryFonts) {
            font.dispose();
        }
        temporaryFonts.clear();
        
        initializeDotDensityCompensation();
    }
    
    /**
     * {@inheritDoc}
     */
    public void setFont(final FontData fontData) {
        this.setFont(fontData, -1);
    }

    /**
     * {@inheritDoc}
     */
    public void setFont(final FontData fontData, final int maxLineWidth) {
        if (cacheFonts) {
            org.eclipse.swt.graphics.Font font = FONT_CACHE.get(fontData);
            if (font == null) {
                // cached fonts (those used in diagrams) shall always be created
                //  in context of the display, in order to avoid trouble if one day
                //  a diagram is printed without drawing it on a display
                //  (because of resolution dependent actual font height)
                font = new org.eclipse.swt.graphics.Font(Display.getCurrent(), fontData);
                FONT_CACHE.put(fontData, font);
            }
            curFont = font;

        } else {
            if (temporaryFonts == null) {
                temporaryFonts = Lists.newArrayList();
            }
            curFont = new org.eclipse.swt.graphics.Font(
                        fontCreationDevice != null ? fontCreationDevice : device, fontData);
            temporaryFonts.add(curFont);
        }

        final boolean lineWidthLimited = maxLineWidth > 0;
        useTextStyle = underlining || strikeout || lineWidthLimited;

        if (!useTextStyle) {
            curTextStyle = null;
        } else {
            // unfortunately the TextStyle object can't be re-used in general,
            //  since there are object identity tests in the lower level code
            curTextStyle = new TextStyle();
            curTextStyle.font = curFont;
            curTextStyle.foreground = gc.getForeground();

            // since KlighdStyledText cares itself on the background
            //  setting the curTextStyle.background is left here

            if (strikeout) {
                curTextStyle.strikeout = true;
                curTextStyle.strikeoutColor = getColor(strikeoutColor);
            } else {
                curTextStyle.strikeout = false;
                curTextStyle.strikeoutColor = getColor(EclipseKlighdConstants.BLACK);
            }

            if (underlining) {
                curTextStyle.underline = true;
                curTextStyle.underlineStyle = underline;
                curTextStyle.underlineColor = getColor(underlineColor);
            } else {
                curTextStyle.underline = false;
                curTextStyle.underlineStyle = SWT.UNDERLINE_SINGLE;
                curTextStyle.underlineColor = getColor(EclipseKlighdConstants.BLACK);
            }

            if (lineWidthLimited) {
                textLineWidth = maxLineWidth;
            } else {
                textLineWidth = -1;
            }
        }
    }


    /** The state w.r.t. strikeout when drawing text. */
    private boolean strikeout = false;

    /** The current strikeout color to use when drawing struck out text. */
    private RGB strikeoutColor = null;

    /**
     * {@inheritDoc}
     */
    public void setStrikeout(final boolean theStrikeout, final RGB color) {
        strikeout = theStrikeout;
        strikeoutColor = color;
    }


    /** The state w.r.t. underlining when drawing text. */
    private boolean underlining = false;

    /** The current text underline style to use when drawing text; is also controlled by
     * {@link #underlining}. */
    private int underline = SWT.UNDERLINE_SINGLE;

    /** The current underline color to use when drawing underlined text. */
    private RGB underlineColor = null;

    /**
     * {@inheritDoc}
     */
    public void setUnderline(final int theUnderlining, final RGB color) {
        underlining = theUnderlining != KlighdConstants.NO_FONT_UNDERLINING;
        underline = theUnderlining;
        underlineColor = color;
    }


    /** Map from RGB to SWT Colors. */
    protected static final HashMap<RGB, Color> COLOR_CACHE = new HashMap<RGB, Color>();

    /**
     * A convenience method or obtaining the SWT {@link Color} related to the given {@link RGB}
     * instance.
     *
     * @param rgb
     *            the {@link RGB} descriptor
     * @return the related {@link Device} dependent {@link Color} instance.
     */
    protected Color getColor(final RGB rgb) {
        if (rgb == null) {
            return null;
        }
        Color color = COLOR_CACHE.get(rgb);
        if (color == null) {
            color = new Color(this.device, rgb);
            COLOR_CACHE.put(rgb, color);
        }
        return color;
    }


    /*-----------------*/
    /* drawing methods */
    /*-----------------*/

    /**
     * {@inheritDoc}
     */
    @Override
    public void draw(final Shape shape) {
        final int alpha = getAlpha();
        // antiAliase();

        final Path path = KlighdPaths.createSWTPath(shape.getPathIterator(null), this.device);

        gc.setTransform(this.swtTransform);
        gc.getGCData().state |= 1 << DRAW_OFFSET_BIT;

        gc.drawPath(path);

        path.dispose();

        setAlpha(alpha);
    }

    /**
     * {@inheritDoc}
     */
    public void draw(final Path path) {
        gc.setTransform(this.swtTransform);
        gc.getGCData().state |= 1 << DRAW_OFFSET_BIT;

        gc.drawPath(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fill(final Shape shape) {
        final Path path = KlighdPaths.createSWTPath(shape.getPathIterator(null), this.device);

        gc.setTransform(this.swtTransform);
        gc.getGCData().state |= 1 << DRAW_OFFSET_BIT;

        gc.fillPath(path);

        path.dispose();
    }

    /**
     * {@inheritDoc}
     */
    public void fill(final Path path) {
        gc.setTransform(this.swtTransform);
        gc.getGCData().state |= 1 << DRAW_OFFSET_BIT;

        gc.fillPath(path);
    }


    /**
     * {@inheritDoc}
     */
    public void drawImage(final Image image, final double width, final double height) {
        final Rectangle bounds = image.getBounds();
        gc.setTransform(swtTransform);
        gc.getGCData().state |= 1 << DRAW_OFFSET_BIT;

        gc.drawImage(image, 0, 0, bounds.width, bounds.height, 0, 0, (int) width, (int) height);
    }

    private Map<ImageData, Image> images = Maps.newHashMap();

    /**
     * {@inheritDoc}
     */
    public void drawImage(final ImageData imageData, final double width, final double height) {
        Image image = images.get(imageData);
        if (image == null) {
            image = new Image(this.device, imageData);
            images.put(imageData, image);
        }

        this.drawImage(image, width, height);
    }


    /**
     * {@inheritDoc}
     */
    public void drawText(final String text) {
        if (!useTextStyle) {
            gc.setFont(curFont);
            if (doDisplayScaleCompensation) {
                swtTransform.scale(dpiScale.x, dpiScale.y);
            }
            gc.setTransform(swtTransform);
            gc.getGCData().state |= 1 << DRAW_OFFSET_BIT;

            gc.drawText(text, 0, 0, SWT.DRAW_DELIMITER | SWT.DRAW_TRANSPARENT);
            gc.setTransform(null);
        } else {
            textLayout.setText(text);
            textLayout.setStyle(curTextStyle, 0, text.length() - 1);
            textLayout.setWidth(textLineWidth);
            if (doDisplayScaleCompensation) {
                swtTransform.scale(dpiScale.x, dpiScale.y);
            }
            gc.setTransform(swtTransform);
            gc.getGCData().state |= 1 << DRAW_OFFSET_BIT;

            this.textLayout.draw(gc, 0, 0);
            gc.setTransform(null);
        }
    }

    /*-------------------------*/
    /* internal helper methods */
    /*-------------------------*/

    // SUPPRESS CHECKSTYLE NEXT 10 MagicNumber
    private static final double[] MATRIX_BUFFER = new double[6];

    /** Updates the SWT transform instance such that it matches AWTs counterpart. */
    private void updateSWTTransform() {
        transform.getMatrix(MATRIX_BUFFER);
        swtTransform.setElements((float) MATRIX_BUFFER[0], (float) MATRIX_BUFFER[1],
                (float) MATRIX_BUFFER[2], (float) MATRIX_BUFFER[3], (float) MATRIX_BUFFER[4],
                (float) MATRIX_BUFFER[5]);
        // gc.setTransform(swtTransform);
    }

    private static final Rectangle2D TEMP_LINE_RECT = new Rectangle2D.Float();

    /**
     * Computes the width of the line after it passes through the current transform.
     *
     * @return resulting width of line after being transform
     */
    public float getTransformedLineWidthFloat() {
        // the following line does not work, as this method is called by
        //  fill AND draw methods, and thus the width would be adjusted twice
        // float lineWidth = gc.getGCData().lineWidth;
        TEMP_LINE_RECT.setRect(0, 0, lineWidth, lineWidth);
        SWTShapeManager.transform(TEMP_LINE_RECT, transform);

        return (float) TEMP_LINE_RECT.getWidth();
    }

    /**
     * This method realizes a poor man anti aliasing if the adjusted line width drawn on the screen
     * is less than two.
     */
    protected void antiAliase() {
        final int alpha = getAlpha();
        final float transformedLineWidth = this.getTransformedLineWidthFloat();
        final int factor = 100;

        if (transformedLineWidth < 2) {
            final double adjustedAlpha =
                    alpha * (KlighdConstants.ALPHA_FULL_OPAQUE - (2 - transformedLineWidth) * factor)
                            / KlighdConstants.ALPHA_FULL_OPAQUE;
            this.setAlpha((int) adjustedAlpha);
        }
    }

    /** A rectangle object used to adjust custom line dash configurations wrt. the current transform. */
    private static final Rectangle2D.Float TEMP_DASH_RECT = new Rectangle2D.Float();

    /**
     * A helper function that adjusts custom dash patterns and dash offset according the given transform,
     * i.e. the zoom factor
     *
     */
    @SuppressWarnings("unused")
    private void updateCustomLineStyle() {
        if (this.gc.getGCData().lineStyle == SWT.LINE_CUSTOM) {

            // adjust the pattern
            final float[] dashPattern = this.gc.getGCData().lineDashes;
            for (int i = 0; i < dashPattern.length; i++) {
                TEMP_DASH_RECT.setRect(0, 0, dashPattern[i], dashPattern[i]);
                SWTShapeManager.transform(TEMP_DASH_RECT, transform);
                dashPattern[i] = TEMP_DASH_RECT.width;
            }

            // adjust the offset
            final float dashOffset = this.gc.getGCData().lineDashesOffset;
            TEMP_DASH_RECT.setRect(0, 0, dashOffset, dashOffset);
            SWTShapeManager.transform(TEMP_DASH_RECT, transform);
            this.gc.getGCData().lineDashesOffset = TEMP_DASH_RECT.width;
        }
    }

    // the following fields with singleton values
    //  serve the purpose of avoiding unnecessary object creation and dismiss
    private final PAffineTransform rotation = new PAffineTransform();
    private final Rectangle2D transformedBounds = new Rectangle2D.Double();
    private final Point2D[] patternPoints = new Point2D[] {
            new Point2D.Double(), new Point2D.Double()
    };

    /**
     * Computes the pair of pattern points required by
     * {@link Pattern#Pattern(Device, float, float, float, float, Color, int, Color, int)}.
     *
     * @param bounds
     *            the bounds of the shape to be drawn with a gradient
     * @param angle
     *            the gradients angle in degree
     * @return the {@link #patternPoints} array providing the desired gradient points
     */
    private Point2D[] computePatternPoints(final Rectangle2D bounds, final double angle) {
        // the requirement to those pattern points is specify the color gradient s.t.
        //  (at least) one corner is colored with the source color, and (at least) one
        //  corner is colored with the target color, independent of 'angle'

        // The idea to achieve that is to rotate 'bounds' counterclockwise by 'angle', and
        //  determine the bounding box of the rotated rectangle. This way at least 2 corners
        //  will lie on the left and right side bound.
        // Now build a vector from the left side of the bounding box to its right side through
        //  the center, and rotate that vector clockwise by 'angle'.
        //  The new start and and end points obtained this way are our required pattern points.

        // An optimization: Since we're not interested in the actual rotated 'bounds' but just
        //  its bounding box, we can also rotate 'bounds' clockwise instead of counterclockwise
        //  so we can skip inverting or reconfiguring the 'rotation' affine transform.

        // store this value for avoiding superfluous re-computations
        final double centerY = bounds.getCenterY();

        if (angle != 0d) {
            // set 'rotation' to rotate its input counterclockwise around the center of 'bounds'
            this.rotation.rotate(Math.toRadians(angle), bounds.getCenterX(), centerY);

            // rotate 'bounds' and store the result in 'transformedBounds'
            // note that transforming a rectangle will lead to a new rectangle resembling
            //  the bounding box of the imaginary rotated original one;
            // there will be no Path2D created containing the rotated corner points!
            this.rotation.transform(bounds, this.transformedBounds);
        } else {
            // just set 'transformedBounds' to the values of 'bounds'
            this.transformedBounds.setRect(bounds);
        }

        // now determine two points forming a horizontal line through the center of 'bounds'
        //  (which is equal to the center of 'transformedBounds' as we rotated around the center)
        //  from 'transformedBounds's left most 'x' value to its right most one
        this.patternPoints[0].setLocation(this.transformedBounds.getMinX(), centerY);
        this.patternPoints[1].setLocation(this.transformedBounds.getMaxX(), centerY);

        // the gradient will be realized along that imaginary line,
        //  lines of points of equal color will run orthogonally to that line

        if (!this.rotation.isIdentity()) {
            // in order to let the imaginary line respect our desired angle,
            //  simply rotate the points wrt. the above configured rotation anchor
            this.rotation.transform(this.patternPoints, 0, this.patternPoints, 0, 2);
            this.rotation.setToIdentity();
        }

        return this.patternPoints;
    }


    /* ------------------------------------------------ */
    /*  legacy methods due to inheritance of Graphics2D */
    /*   that is required by KlighdCanvas -> PSWTCanvas */
    /*    and PPaintContext                             */
    /* ------------------------------------------------ */

    private Composite transparency;

    @Override
    public Composite getComposite() {
        return this.transparency;
    }

    @Override
    public void setComposite(final Composite comp) {
        if (comp instanceof AlphaComposite) {
            final AlphaComposite ac = (AlphaComposite) comp;
            this.gc.setAlpha((int) (KlighdConstants.ALPHA_FULL_OPAQUE * ac.getAlpha()));
            this.transparency = ac;
        } else {
            this.gc.setAlpha(KlighdConstants.ALPHA_FULL_OPAQUE);
            this.transparency = null;
        }
    }

    /** An {@link AffineTransform} instance denoting the currently set drawing transform. */
    private final AffineTransform transform;

    @Override
    public AffineTransform getTransform() {
        return new AffineTransform(this.transform);
    }

    @Override
    public void setTransform(final AffineTransform transform) {
        this.transform.setTransform(transform);
        this.updateSWTTransform();
    }

    @Override
    public void transform(final AffineTransform tx) {
        this.transform.concatenate(tx);
        this.updateSWTTransform();
    }

    @Override
    public Shape getClip() {
        // Since the results of this method are pushed on a stack it must not return
        //  an updated singleton element but distinct objects!!

        this.gc.setTransform(null);
        final Rectangle clipping = this.gc.getClipping();
        final Rectangle2D clip =
                new Rectangle2D.Double(clipping.x, clipping.y, clipping.width, clipping.height);

        // chsch: I would appreciate so much if we could get rid of the following 5 lines!
        //  (as well as 'this.gc.setTransform(null)' above)
        // however, due to the required coordinate roundings in #setClip(Shape) and #clip(Shape)
        //  their errors will also be scaled up leading to unacceptable results
        try {
            SWTShapeManager.transform(clip, transform.createInverse());
        } catch (final NoninvertibleTransformException e) {
            throw new RuntimeException(e);
        }
        return clip;
    }

    private final Rectangle swtClipRect = new Rectangle(0, 0, 0, 0);

    @Override
    public void setClip(final Shape clip) {
        // important: clip bounds must not be adjusted in any way, since in combination with
        //  usage of 'getClip()' those manipulations will accumulate and lead to unintended effects!!

        if (clip == null) {
            this.gc.setClipping((Rectangle) null);

        } else if (clip instanceof Rectangle2D) {

            // chsch: I would appreciate so much if we could get rid of the following 2(7) lines!
            //  (as well as the first line of the 'else' branch)
            // however, due to the required coordinate roundings their errors will also be scaled up
            //  leading to unacceptable results; see also #getClip() and #clip(Shape)
            this.gc.setTransform(null);
            final java.awt.Rectangle rect = clip.getBounds();
            SWTShapeManager.transform(rect, transform);

            this.swtClipRect.x = rect.x;
            this.swtClipRect.y = rect.y;
            this.swtClipRect.width = rect.width;
            this.swtClipRect.height = rect.height;
            this.gc.setClipping(this.swtClipRect);

        } else {
            this.gc.setTransform(null);
            final Path clipPath =
                    KlighdPaths.createSWTPath(clip.getPathIterator(this.transform), this.device);
            this.gc.setClipping(clipPath);
            clipPath.dispose();
        }
    }

    @Override
    public void clip(final Shape clip) {
        // important: clip bounds must not be adjusted in any way, since in combination with
        //  usage of 'getClip()' those manipulations will accumulate and lead to unintended effects!!
        //  (PCamera.fullPaint() calls 'clip(getBoundsReference()', for example);

        if (clip == null) {
            this.gc.setClipping((Rectangle) null);

        } else if (clip instanceof Rectangle2D) {
            final Rectangle2D rect = clip.getBounds2D();
            Rectangle2D.intersect(this.getClip().getBounds2D(), rect, rect);
            this.setClip(rect);

            // alternatively:
            //
            // // chsch: I would appreciate so much if we could get rid of the following 2(7) lines!
            // // however, due to the required coordinate roundings their errors will also be scaled up
            // //  leading to unacceptable results; see also #getClip() and #clip(Shape)
            // this.gc.setTransform(null);
            // SWTShapeManager.transform(rect, transform);
            //
            // this.swtClipRect.x = (int) rect.getX();
            // this.swtClipRect.y = (int) rect.getY();
            // this.swtClipRect.width = (int) (rect.getWidth() + 0.5);
            // this.swtClipRect.height = (int) (rect.getHeight() + 0.5);
            //
            // final Rectangle clipping = gc.getClipping();
            // this.swtClipRect.intersect(clipping);
            // this.gc.setClipping(this.swtClipRect);

        } else if (clip instanceof Ellipse2D) {
            // TODO the following is not correct wrt the specification of this method (see method doc)
            //  I however don't know an efficient way to intersect arbitrary shapes at the moment
            final Ellipse2D ellipse = (Ellipse2D) ((Ellipse2D) clip).clone();
            this.setClip(ellipse);

        } else if (clip instanceof Path2D) {
            // TODO the following is not correct wrt the specification of this method (see method doc)
            //  I however don't know an efficient way to intersect arbitrary shapes at the moment
            final Path2D polygon = (Path2D) ((Path2D) clip).clone();
            this.setClip(polygon);

        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Helper for debugging purposes.
     *
     * @param clip
     *            the clip SWT {@link Rectangle} to be visualized
     */
    @SuppressWarnings("unused")
    private void drawClip(final Rectangle clip) {
        final Color c = gc.getForeground();
        gc.setClipping((Rectangle) null);
        gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
        gc.drawRectangle(clip.x + 0, clip.y + 0, clip.width + 0, clip.height + 0);
        gc.setForeground(c);
    }

    @Override
    public void setColor(final java.awt.Color c) {
        gc.setForeground(getColor(new RGB(c.getRed(), c.getGreen(), c.getBlue())));
    }

    @Override
    public void setBackground(final java.awt.Color c) {
        gc.setBackground(getColor(new RGB(c.getRed(), c.getGreen(), c.getBlue())));
    }

    @Override
    public void fillRect(final int x, final int y, final int width, final int height) {
        fill(new Rectangle2D.Float(x, y, width, height));
    }

    @Override
    public void setRenderingHint(final Key hintKey, final Object hintValue) {
        /* do nothing */
    }

    @Override
    public void dispose() {
        if (disposeTextLayout && this.textLayout != null) {
            this.textLayout.dispose();
        }

        for (final Image image : images.values()) {
            image.dispose();
        }
        images.clear();
    }


    /* ------------------------------------------------ */
    /*  legacy methods due to inheritance of Graphics2D */
    /*   that are not supported by this implementation  */
    /* ------------------------------------------------ */

    // CHECKSTYLEOFF Parameter

    @Override
    public boolean drawImage(final java.awt.Image img, final AffineTransform xform,
            final ImageObserver obs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drawImage(final BufferedImage img, final BufferedImageOp op, final int x, final int y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drawRenderedImage(final RenderedImage img, final AffineTransform xform) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drawRenderableImage(final RenderableImage img, final AffineTransform xform) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drawString(final String str, final int x, final int y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drawString(final String str, final float x, final float y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drawString(final AttributedCharacterIterator iterator, final int x, final int y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drawString(final AttributedCharacterIterator iterator, final float x, final float y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drawGlyphVector(final GlyphVector g, final float x, final float y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hit(final java.awt.Rectangle rect, final Shape s, final boolean onStroke) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPaint(final Paint paint) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStroke(final Stroke s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getRenderingHint(final Key hintKey) {
         throw new UnsupportedOperationException();
    }

    @Override
    public void setRenderingHints(final Map<?, ?> hints) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addRenderingHints(final Map<?, ?> hints) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RenderingHints getRenderingHints() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void translate(final int x, final int y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void translate(final double tx, final double ty) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rotate(final double theta) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rotate(final double theta, final double x, final double y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void scale(final double sx, final double sy) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void shear(final double shx, final double shy) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Paint getPaint() {
        throw new UnsupportedOperationException();
    }

    @Override
    public java.awt.Color getBackground() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stroke getStroke() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FontRenderContext getFontRenderContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Graphics create() {
        throw new UnsupportedOperationException();
    }

    @Override
    public java.awt.Color getColor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPaintMode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setXORMode(final java.awt.Color c1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public java.awt.Font getFont() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFont(final java.awt.Font font) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FontMetrics getFontMetrics(final java.awt.Font f) {
        throw new UnsupportedOperationException();
    }

    @Override
    public java.awt.Rectangle getClipBounds() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clipRect(final int x, final int y, final int width, final int height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setClip(final int x, final int y, final int width, final int height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void copyArea(final int x, final int y, final int width, final int height, final int dx,
            final int dy) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drawLine(final int x1, final int y1, final int x2, final int y2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearRect(final int x, final int y, final int width, final int height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drawRoundRect(final int x, final int y, final int width, final int height,
            final int arcWidth, final int arcHeight) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillRoundRect(final int x, final int y, final int width, final int height,
            final int arcWidth, final int arcHeight) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drawOval(final int x, final int y, final int width, final int height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillOval(final int x, final int y, final int width, final int height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drawArc(final int x, final int y, final int width, final int height,
            final int startAngle, final int arcAngle) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillArc(final int x, final int y, final int width, final int height,
            final int startAngle, final int arcAngle) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drawPolyline(final int[] xPoints, final int[] yPoints, final int nPoints) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void drawPolygon(final int[] xPoints, final int[] yPoints, final int nPoints) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillPolygon(final int[] xPoints, final int[] yPoints, final int nPoints) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean drawImage(final java.awt.Image img, final int x, final int y,
            final ImageObserver observer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean drawImage(final java.awt.Image img, final int x, final int y, final int width,
            final int height, final ImageObserver observer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean drawImage(final java.awt.Image img, final int x, final int y,
            final java.awt.Color bgcolor,            final ImageObserver observer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean drawImage(final java.awt.Image img, final int x, final int y, final int width,
            final int height, final java.awt.Color bgcolor, final ImageObserver observer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean drawImage(final java.awt.Image img, final int dx1, final int dy1, final int dx2,
            final int dy2, final int sx1, final int sy1, final int sx2, final int sy2,
            final ImageObserver observer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean drawImage(final java.awt.Image img, final int dx1, final int dy1, final int dx2,
            final int dy2, final int sx1, final int sy1, final int sx2, final int sy2,
            final java.awt.Color bgcolor, final ImageObserver observer) {
        throw new UnsupportedOperationException();
    }

    /* --------------------- Semantic Data ---------------------*/

    // CHECKSTYLEOFF Javadoc
    // unsupported for the SWT graphics
    public void addSemanticData(final KlighdSemanticDiagramData semanticData) {
    }

    public void startGroup(final KlighdSemanticDiagramData semanticData) {
    }

    public void endGroup() {
    }
    /* --------------------- TextLength    ---------------------*/

    // unsupported for the SWT graphics
    public void setNextTextLength(double textLength) {
    }
}
