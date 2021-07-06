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
package de.cau.cs.kieler.klighd.piccolo.internal.nodes;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

import com.google.common.base.Strings;

import de.cau.cs.kieler.klighd.Klighd;
import de.cau.cs.kieler.klighd.KlighdConstants;
import de.cau.cs.kieler.klighd.eclipse.EclipseKlighdConstants;
import de.cau.cs.kieler.klighd.eclipse.microlayout.EclipsePlacementUtil;
import de.cau.cs.kieler.klighd.krendering.KText;
import de.cau.cs.kieler.klighd.microlayout.Bounds;
import de.cau.cs.kieler.klighd.microlayout.PlacementUtil;
import de.cau.cs.kieler.klighd.piccolo.KlighdNode;
import de.cau.cs.kieler.klighd.piccolo.KlighdPiccolo;
import de.cau.cs.kieler.klighd.piccolo.KlighdSWTGraphics;
import de.cau.cs.kieler.klighd.piccolo.internal.util.KlighdPaintContext;
import de.cau.cs.kieler.klighd.piccolo.internal.util.RGBGradient;
import de.cau.cs.kieler.klighd.util.KlighdProperties;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * The KLighD-specific {@link edu.umd.cs.piccolo.PNode PNode} implementation for displaying text
 * strings supporting {@link org.eclipse.swt.graphics.TextStyle TextStyles}.<br>
 * It is inspired by the Piccolo2D {@link edu.umd.cs.piccolox.swt.PSWTText PSWTText} and is
 * tailored/extended to those features required by KLighD.<br>
 * <br>
 * <b>Note:</b> All <code>invalidate...</code> and <code>repaint</code> calls are deactivated in
 * order to avoid superfluous repaint activities. The repaint events are fired by
 * {@link #addChild(edu.umd.cs.piccolo.PNode) addChild(PNode)}/
 * {@link #removeChild(edu.umd.cs.piccolo.PNode) removeChild(PNode)} in case of rendering changes,
 * by {@link #setBounds(double, double, double, double)} in case of layout changes, and in case of
 * pure style changes by the {@link de.cau.cs.kieler.klighd.kgraph.KGraphElement KGraphElement}
 * rendering controllers ({@link
 * de.cau.cs.kieler.klighd.piccolo.internal.controller.AbstractKGERenderingController #updateStyles()
 * AbstractKGERenderingController#updateStyles()}) after all rendering and style changes are
 * performed.
 *
 * @author chsch
 */
public class KlighdStyledText extends KlighdNode.KlighdFigureNode<KText> {

    private static final long serialVersionUID = -4463204146476543138L;

    private final Bounds testingSize;

    private String text = "";

    private FontData fontData = null;

    private int penAlpha = KlighdConstants.ALPHA_FULL_OPAQUE;
    private RGB penColor = EclipseKlighdConstants.BLACK;

    private int paintAlpha = KlighdConstants.ALPHA_FULL_OPAQUE;
    private RGB paint = null;
    private RGBGradient paintGradient = null;

    private int underlining = KlighdConstants.NO_FONT_UNDERLINING;
    private RGB underlineColor = EclipseKlighdConstants.BLACK;
    private boolean strikeout = false;
    private RGB strikeoutColor = EclipseKlighdConstants.BLACK;

    private boolean occludedOnMainDiagram = false;

    /**
     * Constructor taking the related {@link KText} view model element.
     *
     * @param theKText
     *            The KText view model element containing the string to be displayed.
     */
    public KlighdStyledText(final KText theKText) {
        this(theKText.getText(), EclipseKlighdConstants.DEFAULT_FONT,
                EclipsePlacementUtil.getTestingTextSize(theKText));
        this.setRendering(theKText);
    }

    /**
     * Constructor taking the initial text.
     *
     * @param theText
     *            The initial text.
     */
    public KlighdStyledText(final String theText) {
        this(theText, EclipseKlighdConstants.DEFAULT_FONT, null);
    }

    /**
     * Constructor taking the initial text and font configuration.
     *
     * @param theText
     *            The initial text.
     * @param theFont
     *            The SWT {@link FontData} configuration for this text component.
     */
    public KlighdStyledText(final String theText, final FontData theFont) {
        this(theText, theFont, null);
    }

    /**
     * Internal constructor.
     */
    private KlighdStyledText(final String theText, final FontData theFont, final Bounds testingSize) {
        super();
        this.text = theText;
        this.setFont(theFont != null ? theFont : EclipseKlighdConstants.DEFAULT_FONT);
        this.testingSize = testingSize;
    }

    /**
     * Configures the visibility of this {@link KlighdStyledText} on the main diagram, which need to
     * be suppressed, e.g., in case a text label widget covers this text node.
     *
     * @param occluded
     *          if <code>true</code>
     */
    public void setOccludedOnMainDiagram(final boolean occluded) {
        this.occludedOnMainDiagram = occluded;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSelectable() {
        return KlighdProperties.isSelectable(getViewModelElement());
    }

    /**
     * Get the text that is displayed by this node.
     * @return The text that is displayed.
     */
    public String getText() {
        return text;
    }

    /**
     * Updates the text string to be displayed by this node.<br>
     * <code>theText</code> may be empty or contain line breaks.
     *
     * @param theText
     *            The text string to be displayed.
     */
    public void setText(final String theText) {
        this.text = theText == null ? "" : theText;
        updateBounds();
    }

    /**
     * Provides the currently set pen color.<br>
     * It's currently used in order configure the text widget enabling the cursor-based text
     * selection, see {@link de.cau.cs.kieler.klighd.ui.internal.viewers.KlighdLabelWidgetHandler}.
     *
     * @return the current pen color {@link RGB}.
     */
    public RGB getPenColor() {
        return penColor;
    }

    /**
     * Provides the currently set pen color's alpha value.<br>
     * It's currently used in order configure the text widget enabling the cursor-based text
     * selection, see {@link de.cau.cs.kieler.klighd.ui.internal.viewers.KlighdLabelWidgetHandler}.
     *
     * @return the current pen color's alpha value
     */
    public int getPenAlpha() {
        return penAlpha;
    }

    /**
     * Provides the currently set background color or <code>null</code>, if no background coloring
     * or a background gradient is set.<br>
     * It's currently used in order configure the text widget enabling the cursor-based text
     * selection, see {@link de.cau.cs.kieler.klighd.ui.internal.viewers.KlighdLabelWidgetHandler}.
     *
     * @return the current background color {@link RGB}.
     */
    public RGB getBackgroundColor() {
        return paint;
    }

    /**
     * Provides the currently set background color's alpha value.<br>
     * It's currently used in order configure the text widget enabling the cursor-based text
     * selection, see {@link de.cau.cs.kieler.klighd.ui.internal.viewers.KlighdLabelWidgetHandler}.
     *
     * @return the current background color's alpha value
     */
    public int getBackgroundAlpha() {
        return paintAlpha;
    }

    /**
     * Provides the currently set background gradient configuration or <code>null</code>, if no
     * background coloring or a single background color is set.<br>
     * It's currently used in order configure the text widget enabling the cursor-based text
     * selection, see {@link de.cau.cs.kieler.klighd.ui.internal.viewers.KlighdLabelWidgetHandler}.
     *
     * @return the current background color's alpha value
     */
    public RGBGradient getBackgroundGradient() {
        return paintGradient;
    }

    /**
     * Sets the current pen color.
     *
     * @param color
     *            use this color.
     * @param alpha
     *            use this alpha.
     */
    public void setPenColor(final RGB color, final int alpha) {
        if (penColor.equals(color) && penAlpha == alpha) {
            return;
        }
        final Object oldPaint = penColor;
        penColor = color;
        penAlpha = alpha;

        invalidatePaint();
        firePropertyChange(PText.PROPERTY_CODE_TEXT_PAINT, PROPERTY_PAINT, oldPaint, penColor);
    }

    private static final String TEXT_GRADIENT_MESSAGE = "KLighD (Piccolo2D): A color gradient has been"
            + " tried to set as pen color of a text field. This currently not supported by SWT."
            + " Thus, this setting will have no effect.";

    /**
     * Sets the current pen color gradient.<br>
     * <b>This is currently not supported by SWT!</b>
     *
     * @param gradient gradient that this node should use drawing the text.
     */
    public void setPenColor(final RGBGradient gradient) {
        Klighd.log(new Status(IStatus.WARNING, KlighdPiccolo.PLUGIN_ID, TEXT_GRADIENT_MESSAGE));
    }

    /**
     * Sets the current pen paint.
     *
     * @param color
     *            use this color.
     * @param alpha
     *            use this alpha.
     */
    public void setPaint(final RGB color, final int alpha) {
        if (paint == null && color == null
                || paint != null && paint.equals(color) && paintAlpha == alpha) {
            return;
        }
        final Object oldPaint = paintGradient != null ? paintGradient : paint;
        paintGradient = null;
        paint = color;
        paintAlpha = alpha;

        invalidatePaint();
        firePropertyChange(PROPERTY_CODE_PAINT, PROPERTY_PAINT, oldPaint, color);
    }

    /**
     * Set the paint used to paint this node, which may be null.
     *
     * @param gradient paint that this node should use when painting itself.
     */
    public void setPaint(final RGBGradient gradient) {
        if (paintGradient != null && paintGradient.equals(gradient)) {
            return;
        }
        final Object oldPaint = paintGradient != null ? paintGradient : paint;
        paint = null;
        paintAlpha = KlighdConstants.ALPHA_FULL_OPAQUE;
        paintGradient = gradient;

        invalidatePaint();
        firePropertyChange(PROPERTY_CODE_PAINT, PROPERTY_PAINT, oldPaint, gradient);
    }

    /**
     * Provides the currently set {@link FontData}.<br>
     * It's currently used in order configure the text widget enabling the cursor-based text
     * selection, see
     * {@link de.cau.cs.kieler.klighd.piccolo.viewer.PiccoloViewer.KlighdTextInputHandler
     * PiccoloViewer.KlighdTextInputHandler}.
     *
     * @return the current {@link FontData}
     */
    public FontData getFontData() {
        return fontData;
    }

    /**
     * Configures the text node width a {@link FontData}.
     *
     * @param theFont
     *            the desired {@link FontData}, must not be <code>null<code>
     */
    public void setFont(final FontData theFont) {
        if (fontData != null && fontData.equals(theFont)) {
            return;
        }

        final Object oldFont = this.fontData;
        this.fontData = theFont;
        this.updateBounds();

        firePropertyChange(PText.PROPERTY_CODE_FONT, PText.PROPERTY_FONT, oldFont, theFont);
    }

    /**
     * Augments the text node with an underline of the given type and color.
     *
     * @param theUnderlining
     *            the related constant from {@link KlighdConstants} and {@link org.eclipse.swt.SWT SWT}
     * @param color
     *            the used color, maybe <code>null<code> (penColor is used in that case)
     */
    public void setUnderline(final int theUnderlining, final RGB color) {
        if (underlining == theUnderlining) {
            return;
        }

        this.underlining = theUnderlining;
        this.underlineColor = color != null ? color : this.penColor;

        invalidatePaint();
    }

    /**
     * Augments the text node with a strikeout of the given color.
     *
     * @param theStrikeout
     *            whether to strike out
     * @param color
     *            the used color, maybe <code>null<code> (penColor is used in that case)
     */
    public void setStrikeout(final boolean theStrikeout, final RGB color) {
        if (strikeout == theStrikeout && strikeoutColor == color) {
            return;
        }

        this.strikeout = theStrikeout;
        this.strikeoutColor = color != null ? color : this.penColor;

        invalidatePaint();
    }

    /**
     * Updates the bounds of <code>this</code> node based on the current {@link #text} and
     * {@link #fontData} by delegating to {@link PlacementUtil#estimateTextSize(FontData, String)}.
     */
    protected void updateBounds() {
        // do the (re-)computation of the figure's (local) bounds lazily during the next request,
        //  the indication to do so is done by setting the local bounds 'empty'!
        super.getBoundsReference().resetToZero();

        // CAUTION: I intentionally do no call 'invalidateFullBounds'
        //  as this will traverse up the parents and flag all as 'childBoundsInvalid',
        //  which I consider unnecessary performance waste.

        // Thus the 'fullBoundsInvalid' is set just locally. The subsequently called method
        //  'invalidatePaint()' is specialized in the super class 'KlighdNode.KlighdFigureNode';
        //  this modified method stops traversing upward if an 'IKGraphElementNode' is found

        // In the regular case this 'KlighdStyledText' is wrapped by a 'KlighdAlignmentNode',
        //  whose specialized method 'setChildPaintInvalid' sets its 'fullBoundsInvalid' and
        //  'childBoundsInvalid' flags to 'true' leading to the local (!) re-evaluation
        //  of the alignment node's as well as this node's full bounds on next access!
        // Thus, DON'T change the order of the method calls below.

        setFullBoundsInvalid(true);
        invalidatePaint();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PBounds getBoundsReference() {
        final PBounds bounds = super.getBoundsReference();

        if (bounds.isEmpty()) {
            // lazy (re-)computation of the figure's (local) bounds if the bounds are set 'empty'
            this.setBounds(
                testingSize != null ? testingSize.toRectangle2D()
                    : EclipsePlacementUtil.estimateTextSize(this.fontData, this.text).toRectangle2D());

            // update the scale-based visibility bounds (limits) according to specification defined
            //  on the corresponding KText element (either in absolute px or zoom scale fractions)
            this.updateScaleBasedVisibilityBounds(bounds);
        }

        return bounds;
    }

    /**
     * {@inheritDoc}<br>
     * <br>
     * The aim of this override is to enable to call this method from {@link KlighdAlignmentNode}
     * situated in the same package.
     */
    @Override
    protected boolean getFullBoundsInvalid() {
        return super.getFullBoundsInvalid();
    }

    /**
     * {@inheritDoc}<br>
     * <br>
     * The aim of this override is to enable to call this method from {@link KlighdAlignmentNode}
     * situated in the same package.
     */
    @Override
    protected void setFullBoundsInvalid(final boolean fullBoundsInvalid) {
        super.setFullBoundsInvalid(fullBoundsInvalid);
    }

    @Override
    public void fullPaint(final PPaintContext paintContext) {
        // since text figures are not supposed to contain children
        //  skip the 'fullPaint(...)' instead of just 'paint(...)'
        // skip if
        //  * no text is given, or
        //  * a text label widget is attached to this text
        //    and the text is to be drawn on the main diagram;
        //    (the text must be drawn as usual on the outline, printouts, and image exports)

        if (Strings.isNullOrEmpty(this.text)
                || occludedOnMainDiagram && ((KlighdPaintContext) paintContext).isMainDiagram()) {
            return;
        } else {
            super.fullPaint(paintContext);
        }
    }

    @Override
    protected void paint(final KlighdPaintContext kpc) {

        final KlighdSWTGraphics graphics = kpc.getKlighdGraphics();

        final int currentAlpha = graphics.getAlpha();
        final float currentAlphaFloat = currentAlpha;

        if (this.paintGradient != null) {
            graphics.setFillPattern(this.paintGradient, getBounds());
        } else if (this.paint != null) {
            graphics.setAlpha((int) (paintAlpha
                    * (currentAlphaFloat / KlighdConstants.ALPHA_FULL_OPAQUE)));
            graphics.setFillColor(this.paint);
        }

        if (this.paint != null || this.paintGradient != null) {
            graphics.setLineAttributes(EclipseKlighdConstants.DEFAULT_LINE_ATTRIBUTES);
            graphics.fill(getBoundsReference());
        }

        graphics.setUnderline(underlining, underlineColor);
        graphics.setStrikeout(strikeout, strikeoutColor);

        if (this.penColor != null) {
            graphics.setAlpha(
                    (int) (penAlpha * (currentAlphaFloat / KlighdConstants.ALPHA_FULL_OPAQUE)));
            graphics.setStrokeColor(this.penColor);

        } else {
            graphics.setStrokeColor(EclipseKlighdConstants.BLACK);
        }

        if (fontData != null) {
            graphics.setFont(fontData);
        } else {
            graphics.setFont(EclipseKlighdConstants.DEFAULT_FONT);
        }

        addSemanticData(kpc);

        if (kpc.isSetTextLengths()) {
            graphics.setNextTextLength(this.getWidth());
        }

        graphics.drawText(text);

        graphics.setAlpha(currentAlpha);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "KlighdStyledText '" + text + "'";
    }
}
