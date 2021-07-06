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
package de.cau.cs.kieler.klighd;

import java.awt.Color;
import java.awt.Font;

import de.cau.cs.kieler.klighd.actions.CollapseExpandAction;
import de.cau.cs.kieler.klighd.krendering.KColor;
import de.cau.cs.kieler.klighd.krendering.KRenderingFactory;
import de.cau.cs.kieler.klighd.microlayout.Bounds;

/**
 * This class contains definitions of initial & default values.
 * 
 * @author chsch
 * 
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch
 */
public class KlighdConstants {

    /**
     * The background color used in default selection highlighting
     * {@link de.cau.cs.kieler.klighd.krendering.KBackground KBackground} style.
     */
    // the color values of 'DimGray'
    public static final KColor DEFAULT_SELECTION_HIGHLIGHTING_BACKGROUND_COLOR =
            KRenderingFactory.eINSTANCE.createKColor().setColor(190, 190, 190);
    
    /**
     * Constant definition of the black {@link RGB} color constant.
     */
    public static final Color BLACK = new Color(0, 0, 0);

    /**
     * Constant definition of the red {@link Color} color constant.
     */
    public static final Color BLUE = new Color(0, 0, 255);

    /**
     * Constant definition of the red {@link Color} color constant.
     */
    public static final Color GREEN = new Color(0, 255, 0);

    /**
     * Constant definition of the red {@link Color} color constant.
     */
    public static final Color RED = new Color(255, 0, 0);

    /**
     * Constant definition of the white {@link Color} color constant.
     */
    public static final Color WHITE = new Color(255, 255, 255);

    /**
     * Constant definition of the yellow {@link Color} color constant.
     */
    public static final Color YELLOW = new Color(255, 255, 0);

    /**
     * Constant definition of the <i>full transparent</i> SWT alpha value.
     * 
     * @see #ALPHA_FULL_OPAQUE
     */
    public static final int ALPHA_FULL_TRANSPARENT = 0;

    /**
     * Constant definition of the <i>full opaque</i> SWT alpha value.
     * 
     * @see #ALPHA_FULL_TRANSPARENT
     */
    public static final int ALPHA_FULL_OPAQUE = 255;

    /**
     * Default animation time in ms.
     */
    public static final int DEFAULT_ANIMATION_TIME = 500;


    /**
     * Default display dot density, basically copied from
     * {@link org.eclipse.swt.internal.DPIUtil#DPI_ZOOM_100()}.
     * For more details checkout https://en.wikipedia.org/wiki/Dots_per_inch
     */
    @SuppressWarnings("restriction")
    public static final float DEFAULT_DISPLAY_DPI = Klighd.IS_MACOSX ? 72f : 96f;

    /**
     * This font name is used for {@link de.cau.cs.kieler.klighd.krendering.KText KTexts}, if no
     * related {@link de.cau.cs.kieler.klighd.krendering.KFontName KFontName} style is attached.
     * 
     * Note: This is a AWT constant!
     */
    public static final String DEFAULT_FONT_NAME = Klighd.IS_WINDOWS ? "Arial" : Font.SANS_SERIF;

    /**
     * A platform independent font name identifier for a default monospaced font.
     */
    public static final String DEFAULT_MONOSPACE_FONT_NAME = Klighd.IS_WINDOWS ? "Consolas"
            : Klighd.IS_MACOSX ? "Monaco" : "Monospace";

    /**
     * This font size is used for {@link de.cau.cs.kieler.klighd.krendering.KText KTexts}, if no
     * related {@link de.cau.cs.kieler.klighd.krendering.KFontSize KFontSize} style is attached.
     */
    public static final int DEFAULT_FONT_SIZE = 10;

    /**
     * This font size is used for Tooltips.
     */
    public static final int DEFAULT_TOOL_TIP_FONT_SIZE = 9;

    /**
     * This font style used for {@link de.cau.cs.kieler.klighd.krendering.KText KTexts}, if no related
     * {@link de.cau.cs.kieler.klighd.krendering.KFontBold KFontBold} and/or
     * {@link de.cau.cs.kieler.klighd.krendering.KFontItalic KFontItalic} style is attached.
     * 
     * Note: This is a AWT constant!
     */
    public static final int DEFAULT_FONT_STYLE = Font.PLAIN;

    /**
     * This font configuration used for {@link de.cau.cs.kieler.klighd.krendering.KText KTexts}.
     */
    public static final Font DEFAULT_FONT = new Font(DEFAULT_FONT_NAME, DEFAULT_FONT_SIZE,
            Font.PLAIN);

    /**
     * This constant is used to express the non-underlining configuration as {@link SWT} doesn't
     * provide a constant for this case as {@link org.eclipse.swt.graphics.TextStyle TextStyle} uses
     * an extra boolean flag for that.
     */
    public static final int NO_FONT_UNDERLINING = -1;

    /**
     * Identifier of the built-in collapse expand action to be mentioned in instances of
     * {@link de.cau.cs.kieler.klighd.krendering.KAction KAction}.
     */
    public static final String ACTION_COLLAPSE_EXPAND = CollapseExpandAction.ID;

    /**
     * The minimal size of {@link de.cau.cs.kieler.klighd.kgraph.KNode KNodes} that is applied in case
     * no definition is given for a particular node.
     */
    public static final Bounds MINIMAL_NODE_BOUNDS = Bounds.immutableCopy(new Bounds(10, 10));
    
    /**
     * Constant definition of <i>printout</i> export format, required e.g. in the registration of
     * {@link IExportBranding IExportBrandings}.
     */
    public static final String EXPORT_PRINTOUT = "printout"; 

    /**
     * Special type of semantic data that represents an 'id'. A specified value should be mapped to
     * rendering specific id elements, i.e. for SVGs a {@code <tag id="xyz" />} attribute should be
     * generated.
     * 
     * @see de.cau.cs.kieler.klighd.util.KlighdProperties#SEMANTIC_DATA KlighdProperties#SEMANTIC_DATA
     */
    public static final String SEMANTIC_DATA_ID = "__id";
    
    /**
     * Special type of semantic data that represents a 'class'. A specified value should be mapped
     * to rendering specific class elements, i.e. for SVGs a {@code <tag class="xyz" />} attribute
     * should be generated.
     * 
     * @see de.cau.cs.kieler.klighd.util.KlighdProperties#SEMANTIC_DATA KlighdProperties#SEMANTIC_DATA
     */
    public static final String SEMANTIC_DATA_CLASS = "__class";

    /**
     * Special type of semantic data that is not handled by klighd but instead placed in the SVG as
     * raw data.
     * 
     * @see de.cau.cs.kieler.klighd.util.KlighdProperties#SEMANTIC_DATA
     *      KlighdProperties#SEMANTIC_DATA
     */
    public static final String SEMANTIC_DATA_RAW = "__raw";

    /**
     * Hidden default constructor.
     */
    private KlighdConstants() {
        
    }

}
