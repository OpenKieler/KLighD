// CHECKSTYLEOFF |
// Copyright 2000-2007 FreeHEP
package de.cau.cs.kieler.klighd.piccolo.freehep;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.AttributedCharacterIterator.Attribute;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Stack;
import java.util.zip.GZIPOutputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.PathData;
import org.eclipse.swt.widgets.Display;
import org.freehep.graphics2d.font.FontUtilities;
import org.freehep.graphicsbase.util.UserProperties;
import org.freehep.graphicsbase.util.Value;
import org.freehep.graphicsbase.xml.util.XMLWriter;
import org.freehep.graphicsio.AbstractVectorGraphicsIO;
import org.freehep.graphicsio.FontConstants;
import org.freehep.graphicsio.ImageConstants;
import org.freehep.graphicsio.ImageGraphics2D;
import org.freehep.graphicsio.InfoConstants;
import org.freehep.graphicsio.PageConstants;
import org.freehep.graphicsio.svg.SVGFontTable;
import org.freehep.util.io.Base64OutputStream;
import org.freehep.util.io.WriterOutputStream;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import de.cau.cs.kieler.klighd.Klighd;
import de.cau.cs.kieler.klighd.KlighdConstants;
import de.cau.cs.kieler.klighd.eclipse.EclipseKlighdConstants;
import de.cau.cs.kieler.klighd.eclipse.microlayout.EclipsePlacementUtil;
import de.cau.cs.kieler.klighd.piccolo.export.KlighdAbstractSVGGraphics.KlighdGradientPaint;
import de.cau.cs.kieler.klighd.util.KlighdSemanticDiagramData;

/**
 * This class implements the Scalable Vector Graphics output. SVG specifications
 * can be found at http://www.w3c.org/Graphics/SVG/
 *
 * The current implementation is based on REC-SVG11-20030114
 *
 * @author Mark Donszelmann
 * @version $Id: freehep-graphicsio-svg/src/main/java/org/freehep/graphicsio/svg/SVGGraphics2D.java 4c4708a97391 2007/06/12 22:32:31 duns $
 * 
 * - Added capabilities to add semantic information to the svg, ie key/value pairs within the 'klighd' namespace.
 * - Allow comments to be switched off.
 * - Corrected direction of color gradients.
 * - Added support for multi-line text and proper font sizing. 
 * 
 * @author uru
 */
public class SemanticSVGGraphics2D extends AbstractVectorGraphicsIO {

    public static final String VERSION_1_1 = "Version 1.1 (REC-SVG11-20030114)";

    private static final String rootKey = SemanticSVGGraphics2D.class.getName();

    public static final String TRANSPARENT = rootKey + "."
            + PageConstants.TRANSPARENT;

    public static final String BACKGROUND = rootKey + "."
            + PageConstants.BACKGROUND;

    public static final String BACKGROUND_COLOR = rootKey + "."
            + PageConstants.BACKGROUND_COLOR;

    public static final String VERSION = rootKey + ".Version";

    public static final String COMPRESS = rootKey + ".Binary";

    /**
     * use style="font-size:20" instaed of font-size="20"
     * see {@link #style(java.util.Properties)} for details
     */
    public static final String STYLABLE = rootKey + ".Stylable";

    public static final String IMAGE_SIZE = rootKey + "."
            + ImageConstants.IMAGE_SIZE;

    public static final String EXPORT_IMAGES = rootKey + ".ExportImages";

    public static final String EXPORT_SUFFIX = rootKey + ".ExportSuffix";

    public static final String WRITE_IMAGES_AS = rootKey + "."
            + ImageConstants.WRITE_IMAGES_AS;

    public static final String FOR = rootKey + "." + InfoConstants.FOR;

    public static final String TITLE = rootKey + "." + InfoConstants.TITLE;

    private BasicStroke defaultStroke =
            new BasicStroke(
                    EclipseKlighdConstants.DEFAULT_LINE_ATTRIBUTES.width,
                    EclipseKlighdConstants.DEFAULT_LINE_ATTRIBUTES.cap - 1,
                    EclipseKlighdConstants.DEFAULT_LINE_ATTRIBUTES.join - 1);

    public static final String EMBED_FONTS = rootKey + "."
            + FontConstants.EMBED_FONTS;
    
    public static final String DESCRIPTION = rootKey + ".Description";
    
    public static final String ADDITIONAL_ROOT_DATA = rootKey + ".AdditionalData";
    
    public static final String CSS = rootKey + ".css";
    
    private SVGFontTable fontTable;

    private static final UserProperties defaultProperties = new UserProperties();
    static {
        defaultProperties.setProperty(TRANSPARENT, true);
        defaultProperties.setProperty(BACKGROUND, false);
        defaultProperties.setProperty(BACKGROUND_COLOR, Color.GRAY);

        defaultProperties.setProperty(VERSION, VERSION_1_1);
        defaultProperties.setProperty(COMPRESS, false);

        defaultProperties.setProperty(STYLABLE, false);

        defaultProperties.setProperty(IMAGE_SIZE, new Dimension(0, 0)); // ImageSize

        defaultProperties.setProperty(EXPORT_IMAGES, false);
        defaultProperties.setProperty(EXPORT_SUFFIX, "image");

        defaultProperties.setProperty(WRITE_IMAGES_AS, ImageConstants.SMALLEST);

        defaultProperties.setProperty(FOR, "");
        defaultProperties.setProperty(TITLE, "");

        defaultProperties.setProperty(CLIP, true);

        defaultProperties.setProperty(EMBED_FONTS, false);
        defaultProperties.setProperty(TEXT_AS_SHAPES, true);
        defaultProperties.setProperty(DESCRIPTION, "");
        
        defaultProperties.setProperty(ADDITIONAL_ROOT_DATA, "");
        defaultProperties.setProperty(CSS, "");
    }

    public static Properties getDefaultProperties() {
        return defaultProperties;
    }

    public static void setDefaultProperties(Properties newProperties) {
        defaultProperties.setProperties(newProperties);
    }

    public static final String version = "$Revision$";

    // current filename including path
    private String filename;

    // The lowerleft and upper right points of the bounding box.
    private int bbx, bby, bbw, bbh;

    // The private writer used for this file.
    private OutputStream ros;

    private PrintWriter os;

    // table for gradients
    Hashtable<GradientPaint, String> gradients = new Hashtable<GradientPaint, String>();

    // table for textures
    Hashtable<?, ?> textures = new Hashtable<Object, Object>();

    private Stack<String> closeTags = new Stack<String>();

    private int imageNumber = 0;

    private Value clipNumber;

    private int width, height;
    
    private KlighdSemanticDiagramData semanticData = null;
    
    private boolean writeComments = false;
    
    /** The display used to render diagrams, if available, otherwhise {@code null}. */
    private Display display;
    /** If a display exists, this {@link GC} is used to perform font size calculations. */  
    private GC gc;
    /** If a display exists, this tracks the display zoom scale ratio to 100% display zoom. */  
    private float displayScaleX = 1f;
    /** If a display exists, this tracks the display zoom scale ratio to 100% display zoom. */  
    private float displayScaleY = 1f;
    
    private final float pointToPxFactor = KlighdConstants.DEFAULT_DISPLAY_DPI / 72f;
    /** A mapping of awt fonts to swt fonts. The latter are used for font size calculations. */
    private Map<Font, org.eclipse.swt.graphics.Font> awtSwtFontCache = Maps.newHashMap();

    /*
     * ================================================================================ |
     * 1. Constructors & Factory Methods
     * ================================================================================
     */
    public SemanticSVGGraphics2D(File file, Dimension size) throws IOException {
        this(new FileOutputStream(file), size);
        this.filename = file.getPath();
    }

    public SemanticSVGGraphics2D(File file, Component component) throws IOException {
        this(new FileOutputStream(file), component);
        this.filename = file.getPath();
    }

    public SemanticSVGGraphics2D(OutputStream os, Dimension size) {
        super(size, false);
        init(os);
        width = size.width;
        height = size.height;
    }

    public SemanticSVGGraphics2D(OutputStream os, Component component) {
        super(component, false);
        init(os);
        width = getSize().width;
        height = getSize().height;
    }

    private void init(OutputStream os) {
        this.ros = os;
        initProperties(getDefaultProperties());

        this.filename = null;

        this.clipNumber = new Value().set(0);
        
        // Dont use #getDefault() as we do not want to 
        //  create a display if none exists so far.
        display = Display.getCurrent();
        if (display != null && gc == null) {
            // remember to dispose the gc later
            gc = new GC(display);
            if (!Klighd.isSuppressDisplayScaleCompensationWhileHandlingText()) {
                org.eclipse.swt.graphics.Point dpi = display.getDPI();
                displayScaleX = KlighdConstants.DEFAULT_DISPLAY_DPI / dpi.x;
                displayScaleY = KlighdConstants.DEFAULT_DISPLAY_DPI / dpi.y;
            }
        }
    }

    protected float getAdjustedFontHeight() {
        final FontMetrics fm = getFontMetrics();
        return getAdjustedFontHeight(fm.getHeight(), fm.getAscent(), fm.getDescent(), false);
    }

    protected SemanticSVGGraphics2D(SemanticSVGGraphics2D graphics, boolean doRestoreOnDispose) {
        super(graphics, doRestoreOnDispose);
        // Now initialize the new object.
        filename = graphics.filename;
        os = graphics.os;
        bbx = graphics.bbx;
        bby = graphics.bby;
        bbw = graphics.bbw;
        bbh = graphics.bbh;
        gradients = graphics.gradients;
        textures = graphics.textures;
        clipNumber = graphics.clipNumber;
        fontTable = graphics.fontTable;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        // dispose cached swt fonts
        for (org.eclipse.swt.graphics.Font f : awtSwtFontCache.values()) {
            f.dispose();
        }
        awtSwtFontCache.clear();
        awtSwtFontCache = null;

        // dispose the gc
        if (gc != null) {
            gc.dispose();
        }
        gc = null;

        super.dispose();
    }

    /*
     * ================================================================================ |
     * 2. Document Settings
     * ================================================================================
     */

    /**
     * Get the bounding box for this image.
     */
    public void setBoundingBox() {
        bbx = 0;
        bby = 0;

        Dimension size = getSize();
        bbw = size.width;
        bbh = size.height;
    }

    /**
     * @param writeComments the writeComments to set
     */
    public void setWriteComments(boolean writeComments) {
        this.writeComments = writeComments;
    }
    
    /*
     * ================================================================================ |
     * 3. Header, Trailer, Multipage & Comments
     * ================================================================================
     */

    /*--------------------------------------------------------------------------------
     | 3.1 Header & Trailer
     *--------------------------------------------------------------------------------*/
    /**
     * Write out the header of this SVG file.
     */
    public void writeHeader() throws IOException {
        ros = new BufferedOutputStream(ros);
        if (isProperty(COMPRESS)) {
            ros = new GZIPOutputStream(ros);
        }

        os = new PrintWriter(ros, true);
        fontTable = new SVGFontTable();

        // Do the bounding box calculation.
        setBoundingBox();
        imageNumber = 0;

        os.println("<?xml version=\"1.0\" standalone=\"no\"?>");
        
        // Insert css document if path is set
        if (!getProperty(CSS).equals("")) {
            os.println("<?xml-stylesheet href=\""+ getProperty(CSS) +"\" type=\"text/css\"?>");
        }
        
        if (getProperty(VERSION).equals(VERSION_1_1)) {
            // no DTD anymore
        } else {
            // FIXME experimental version
        }
        os.println();

        int x = 0;
        int y = 0;
        Dimension size = getPropertyDimension(IMAGE_SIZE);
        int w = size.width;
        if (w <= 0)
            w = width;
        int h = size.height;
        if (h <= 0)
            h = height;

        os.println("<svg ");
        if (getProperty(VERSION).equals(VERSION_1_1)) {
            os.println("     version=\"1.1\"");
            os.println("     baseProfile=\"full\"");
            os.println("     xmlns=\"http://www.w3.org/2000/svg\"");
            os.println("     xmlns:xlink=\"http://www.w3.org/1999/xlink\"");
            os.println("     xmlns:ev=\"http://www.w3.org/2001/xml-events\"");
            os.println("     xmlns:klighd=\"http://de.cau.cs.kieler/klighd\"");
        }
        os.println("     x=\"" + x + "px\"");
        os.println("     y=\"" + y + "px\"");
        os.println("     width=\"" + w + "px\"");
        os.println("     height=\"" + h + "px\"");
        os.println("     viewBox=\"" + bbx + " " + bby + " " + bbw + " " + bbh
                + "\"");
        
        // KIPRA-1849 Allow adding additional data to the root element
        if (!getProperty(ADDITIONAL_ROOT_DATA).equals("")) {
            os.println("     " + getProperty(ADDITIONAL_ROOT_DATA));
        }
        
        os.println("     >");
        closeTags.push("</svg>");
        if (writeComments) {
            pushComment(closeTags, " <!-- bounding box -->");
        }

        os.print("<title>");
        os.print(XMLWriter.normalizeText(getProperty(TITLE)));
        os.println("</title>");

        String producer = getClass().getName();
        if (!isDeviceIndependent()) {
            producer += " " + version.substring(1, version.length() - 1);
        }

        os.print("<desc>");
        if (Strings.isNullOrEmpty(getProperty(DESCRIPTION))) {
            os.print("Creator: " + XMLWriter.normalizeText(getCreator()));
            os.print(" Producer: " + XMLWriter.normalizeText(producer));
            os.print(" Source: " + XMLWriter.normalizeText(getProperty(FOR)));
            if (!isDeviceIndependent()) {
                os.print(" Date: "
                        + DateFormat.getDateTimeInstance(DateFormat.FULL,
                                DateFormat.FULL, Locale.US).format(new Date()));
            }
        } else {
            os.print(getProperty(DESCRIPTION));
        }
        os.println("</desc>");

        // write default stroke
        os.print("<g ");
        Properties style = getStrokeProperties(defaultStroke,  true);
        os.print(style(style));
        os.println(">");

        // close default settings at the end
        closeTags.push("</g>");
        pushComment(closeTags, "<!-- default stroke -->");
    }

    public void writeBackground() throws IOException {
        if (isProperty(TRANSPARENT)) {
            setBackground(null);
        } else if (isProperty(BACKGROUND)) {
            setBackground(getPropertyColor(BACKGROUND_COLOR));
            clearRect(0.0, 0.0, getSize().width, getSize().height);
        } else {
            setBackground(getComponent() != null ? getComponent()
                    .getBackground() : Color.WHITE);
            clearRect(0.0, 0.0, getSize().width, getSize().height);
        }
    }

    /**
     * Writes the font definitions and calls {@link #writeGraphicsRestore()} to
     * close all open XML Tags
     *
     * @throws IOException
     */
    public void writeTrailer() throws IOException {
        // write font definition
        if (isProperty(EMBED_FONTS)) {
            os.println("<defs>");
            os.println(fontTable.toString());
            os.println("</defs>");
            writeComment("<!-- font definitions -->");
        }

        // restor graphic
        writeGraphicsRestore();
    }

    public void closeStream() throws IOException {
        os.close();
    }

    /*
     * ================================================================================ |
     * 4. Create
     * ================================================================================
     */

    public Graphics create() {
        try {
            writeGraphicsSave();
        } catch (IOException e) {
            handleException(e);
        }
        return new SemanticSVGGraphics2D(this, true);
    }

    public Graphics create(double x, double y, double width, double height) {
        try {
            writeGraphicsSave();
        } catch (IOException e) {
            handleException(e);
        }
        SemanticSVGGraphics2D graphics = new SemanticSVGGraphics2D(this, true);
        // FIXME: All other drivers have a translate(x,y), clip(0,0,w,h) here
        os.println("<svg x=\"" + fixedPrecision(x) + "\" " + "y=\""
                + fixedPrecision(y) + "\" " + "width=\""
                + fixedPrecision(width) + "\" " + "height=\""
                + fixedPrecision(height) + "\" " + ">");
        graphics.closeTags.push("</svg>");
        pushComment(graphics.closeTags, " <!-- graphics context -->");
        
        // write default stroke
        os.print("<g ");
        Properties style = getStrokeProperties(defaultStroke,  true);
        os.print(style(style));
        os.println(">");

        graphics.closeTags.push("</g>");
        pushComment(graphics.closeTags, " <!-- default stroke -->");

        return graphics;
    }

    protected void writeGraphicsSave() throws IOException {
        // not applicable
    }

    protected void writeGraphicsRestore() throws IOException {
        while (!closeTags.empty()) {
            os.println(closeTags.pop());
        }
    }

    /*
     * ================================================================================ |
     * 5. Drawing Methods
     * ================================================================================
     */
    /* 5.1 shapes */
    /* 5.1.4. shapes */

    /**
     * Draws the shape using the current paint as border
     *
     * @param shape Shape to draw
     */
    public void draw(Shape shape) {
        // others than BasicStrokes are written by its
        // {@link Stroke#createStrokedShape()}
        if (getStroke() instanceof BasicStroke) {
            PathIterator path = shape.getPathIterator(null);

            Properties style = new Properties();
            if (getPaint() != null) {
                style.put("stroke", hexColor(getPaint()));
                style.put("stroke-opacity", fixedPrecision(alphaColor(getPaint())));
            }

            // no filling
            style.put("fill", "none");
            style.putAll(getStrokeProperties(getStroke(), false));

            writePathIterator(path, style);
        } else if (getStroke() != null) {
            // fill the shape created by stroke
            fill(getStroke().createStrokedShape(shape));
        } else {
            // FIXME: do nothing or draw using defaultStroke?
            fill(defaultStroke.createStrokedShape(shape));
        }
    }

    /**
     * Fills the shape without a border using the current paint
     *
     * @param shape Shape to be filled with the current paint
     */
    public void fill(Shape shape) {
        // draw paint as image if needed
        if (!(getPaint() instanceof Color || getPaint() instanceof GradientPaint)) {
            // draw paint as image
            fill(shape, getPaint());
        } else {
            PathIterator path = shape.getPathIterator(null);

            Properties style = new Properties();

            if (path.getWindingRule() == PathIterator.WIND_EVEN_ODD) {
                style.put("fill-rule", "evenodd");
            } else {
                style.put("fill-rule", "nonzero");
            }

            // fill with paint
            if (getPaint() != null) {
                style.put("fill", hexColor(getPaint()));
                style.put("fill-opacity", fixedPrecision(alphaColor(getPaint())));
            }

            // no border
            style.put("stroke", "none");

            writePathIterator(path, style);
        }
    }

    /**
     * writes a path using {@link #getPath(java.awt.geom.PathIterator)}
     * and the given style
     *
     * @param pi PathIterator
     * @param style Properties for <g> tag
     */
    private void writePathIterator(PathIterator pi, Properties style) {
        StringBuffer result = new StringBuffer();

        // write style
        result.append("<g ");
        result.append(style(style));
        result.append(">\n  ");

        // draw shape
        result.append(getPath(pi));

        // close style
        result.append("\n</g>");
        if (writeComments) {
           result.append(" <!-- drawing style -->"); 
        }

        boolean drawClipped = false;

        // test if clip intersects pi
        if (getClip() != null) {
            GeneralPath gp = new GeneralPath();
            gp.append(pi, true);
            // create the stroked shape
            Stroke stroke = getStroke() == null? defaultStroke : getStroke();
            Rectangle2D bounds = stroke.createStrokedShape(gp).getBounds();
            // clip should intersect the path
            // if clip contains the bounds completely, clipping is not needed
            drawClipped = getClip().intersects(bounds) && !getClip().contains(bounds);
        }

        if (drawClipped) {
            // write in a transformed and clipped context
            os.println(
                getTransformedString(
                    getTransform(),
                    getClippedString(result.toString())));
        } else {
            // write in a transformed context
            os.println(
                getTransformedString(
                    getTransform(),
                    result.toString()));
        }
    }

    /* 5.2. Images */
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        writeWarning(getClass()
                + ": copyArea(int, int, int, int, int, int) not implemented.");
    }

    protected void writeImage(RenderedImage image, AffineTransform xform,
            Color bkg) throws IOException {

        StringBuffer result = new StringBuffer();

        result.append("<image x=\"0\" y=\"0\" " + "width=\"");
        result.append(image.getWidth());
        result.append("\" " + "height=\"");
        result.append(image.getHeight());
        result.append("\" " + attributes(true));
        result.append(" xlink:href=\"");

        String writeAs = getProperty(WRITE_IMAGES_AS);
        boolean isTransparent = image.getColorModel().hasAlpha()
                && (bkg == null);

        String encode;
        byte[] imageBytes;

        // write as PNG
        if (ImageConstants.PNG.equalsIgnoreCase(writeAs) || isTransparent) {
            encode = ImageConstants.PNG;
            imageBytes = ImageGraphics2D.toByteArray(
                image, ImageConstants.PNG, null, null);
        }

        // write as JPG
        else if (ImageConstants.JPG.equalsIgnoreCase(writeAs)) {
            encode = ImageConstants.JPG;
            imageBytes = ImageGraphics2D.toByteArray(
                image, ImageConstants.JPG, null, null);
        }

        // write as SMALLEST
        else {
            byte[] pngBytes = ImageGraphics2D.toByteArray(image, ImageConstants.PNG, null, null);
            byte[] jpgBytes = ImageGraphics2D.toByteArray(image, ImageConstants.JPG, null, null);

            // define encode and imageBytes
            if (jpgBytes.length < 0.5 * pngBytes.length) {
                encode = ImageConstants.JPG;
                imageBytes = jpgBytes;
            } else {
                encode = ImageConstants.PNG;
                imageBytes = pngBytes;
            }
        }

        if (isProperty(EXPORT_IMAGES)) {
            imageNumber++;

            // create filenames
            if (filename == null) {
                writeWarning("SVG: cannot write embedded images, since SVGGraphics2D");
                writeWarning("     was created from an OutputStream rather than a File.");
                return;
            }
            int pos = filename.lastIndexOf(File.separatorChar);
            String dirName = (pos < 0) ? "" : filename.substring(0, pos + 1);
            String imageName = (pos < 0) ? filename : filename
                    .substring(pos + 1);
            imageName += "." + getProperty(EXPORT_SUFFIX) + "-" + imageNumber
                    + "." + encode;

            result.append(imageName);

            // write the image separately
            FileOutputStream imageStream = new FileOutputStream(dirName
                    + imageName);

            imageStream.write(imageBytes);
            imageStream.close();
        } else {
            result.append("data:image/");
            result.append(encode);
            result.append(";base64,");

            StringWriter writer = new StringWriter();
            @SuppressWarnings("resource") // closing a StringWriter has no effect
            Base64OutputStream b64 = new Base64OutputStream(
                    new WriterOutputStream(writer));
            b64.write(imageBytes);
            b64.finish();

            result.append(writer.toString());
        }

        result.append("\"/>");

        os.println(getTransformedString(getTransform(),
            getClippedString(getTransformedString(xform, result
                .toString()))));
    }

    private Double nextTextLength = null;

    public void setNextTextLength(double textLength) {
        this.nextTextLength = Double.valueOf(textLength);
    }

    /**
     * Copied from {@link AbstractVectorGraphicsIO} to change the handling of text as shapes.
     * 
     * Draws the string at (x, y). If TEXT_AS_SHAPES is set
     * {@link #drawGlyphVector(java.awt.font.GlyphVector, float, float)} is used, otherwise
     * {@link #writeString(String, double, double)} for a more direct output of the string.
     *
     * @param string
     * @param x
     * @param y
     */
    public void drawString(String string, double x, double y) {
        // something to draw?
        if (string == null || string.equals("")) {
            return;
        }

        // Draw strings as shapes?
        // We reuse SWT's font size estimation here, as
        //  KLighD uses it to determine the sizes of nodes etc.
        if (isProperty(TEXT_AS_SHAPES)) {
            Path path = new Path(display);
            path.addString(string, 0, 0, getSWTFont());
            path.close();

            // convert to awt path
            Path2D p2d = createAWTPath(path.getPathData());
            path.dispose();

            // since the text shape is created display zoom scale dependent, compensate
            //  the display's scale now, and shift the path to its designated position
            final AffineTransform positionAndScale = new AffineTransform();
            positionAndScale.translate(x, y);
            positionAndScale.scale(displayScaleX, displayScaleY);
            p2d.transform(positionAndScale);

            fill(p2d);

        } else {
            // write string directly
            try {
                writeString(string, x, y);
            } catch (IOException e) {
                handleException(e);
            } finally {
                this.nextTextLength = null;
            }
        }
    }

    /* 5.3. Strings */
    protected void writeString(String str, double x, double y)
            throws IOException {
        // str = FontEncoder.getEncodedString(str, getFont().getName());

        if (isProperty(EMBED_FONTS)) {
            // FIXME not supported yet, the #addGlyphs method is protected
            // fontTable.addGlyphs(str, getFont());
            throw new UnsupportedOperationException("Embedding fonts is not supported.");
        }

        // font transformation should _not_ transform string position
        // so we draw at 0:0 and translate _before_ using getFont().getTransform()
        // we could not just translate before and reverse translation after
        // writing because the clipping area

        // create font properties
        Properties style = getFontProperties(getFont());

        // add stroke properties
        if (getPaint() != null) {
            style.put("fill", hexColor(getPaint()));
            style.put("fill-opacity", fixedPrecision(alphaColor(getPaint())));
        } else {
            style.put("fill", "none");
        }
        style.put("stroke", "none");

        // related to KIPRA-1637: preserve multiple spaces when displaying text strings
        if (isProperty(STYLABLE))
            style.put("white-space", "pre");
        else
            // avoid using the deprecated property 'xml:space' (https://docs.w3cub.com/svg/attribute/xml:space/)
            //  and sneak in the (css-based) replacement (https://www.w3schools.com/cssref/pr_text_white-space.asp)
            //  via a 'style' property definition
            style.put("style", "white-space: pre");

        // convert tags to string values
        str = XMLWriter.normalizeText(str);

        final AffineTransform textOffset = new AffineTransform(1, 0, 0, 1, x, y);
        textOffset.concatenate(getTransform());
        final boolean isSingleLine = str.indexOf('\n') == -1;
        os.println(
            isSingleLine
                ? getTransformedString(
                    // general transformation + text offset
                    textOffset,
                    getTransformedString(
                        // font transformation and text
                        getFont().getTransform(),
                        getTextsString(
                            str, style,
                            // indentation
                            textOffset.isIdentity() ? "" : "  "
                        )
                     )
                ) : getTransformedString(
                    // general transformation + text offset
                    textOffset,
                    // style
                    style(style)
                    // semantic data
                    + attributes(false),
                    getTransformedString(
                        // font transformation and text
                        getFont().getTransform(),
                        getTextsString(
                            str, null /* properties are added to the group */,
                            // indentation
                            "  "
                        )
                    )
                )
        );

        resetSemanticData();
    }

    private String getTextsString(String text, Properties style, String indentation) {
        final String[] lines = text.split("\\r?\\n|\\r");
        final boolean isSingleLine = lines.length <= 1;
        final StringBuffer content = new StringBuffer();
        
        int i = 0;
        if (display != null) {
            // Translate the font back to an swt font
            //  KLighD used SWT to determine font sizes and as SWT and AWT font metrics
            //  differ we have to use swt here
            // Note that the font style constants in SWT and AWT are identical

            final FontMetrics fm = this.getFontMetrics();
            final int height = fm.getHeight();
            final int ascent = fm.getAscent();
            final int descent = fm.getDescent();

            // to the 1st baseline 
            final float firstLineHeight = getAdjustedFontHeight(height, ascent, descent, true);
            // distance to each remaining line
            final float lineHeight = getAdjustedFontHeight(height, ascent, descent, false);

            final Double nextLength = this.nextTextLength;
            this.nextTextLength = null;
            
            // if we have a multi-line text and a configured nextTextLength
            //  we need to recalculate the designated textLength per line
            // otherwise we can stick to the given 'nextTextLength' value
            boolean noTextLengthPerLineCalcRequired = nextLength == null || isSingleLine;
            final FontData fontData = noTextLengthPerLineCalcRequired ? null : getFontData(getFont());
            
            float y = firstLineHeight;
            for (final String line : lines) {
                content.append("\n" + indentation);
                content.append("<text x=\"0\" y=\"").append(y).append("\"");
                // style
                content.append(isSingleLine ? " " + style(style) : "");
                
                final Double nextLineLength = noTextLengthPerLineCalcRequired ? nextLength :
                    // need to box the result here as the type of the ternary operation would be 'double' otherwise
                    //  yielding NPEs if 'nextLength' is 'null'
                    Double.valueOf(EclipsePlacementUtil.estimateTextSize(fontData, line).getWidth());
                
                // text length
                content.append(textLength(nextLineLength));
                // semantic data
                content.append(isSingleLine ? attributes(false) : "");
                content.append(textLineAttributes(line, i++));
                content.append(">");
                content.append(line);
                content.append("</text>");
                
                y += lineHeight;
            }
            
        } else if (Klighd.simulateSwtFontSizeInAwt()) {
            // If simulation of SWT font sizes in AWT is active we can use px size and set text length
            
            final int fontSize = getFont().getSize();
            final float y = fontSize * pointToPxFactor;
            final Double nextLength = this.nextTextLength;
            this.nextTextLength = null;
            
            // if we have a multi-line text and a configured nextTextLength
            //  we need to recalculate the designated textLength per line
            // otherwise we can stick to the given 'nextTextLength' value
            boolean noTextLengthPerLineCalcRequired = nextLength == null || isSingleLine;
            final FontData fontData = noTextLengthPerLineCalcRequired ? null : getFontData(getFont());
            
            for (final String line : lines) {
                content.append("\n" + indentation);
                content.append("<text x=\"0\" y=\"").append(y).append("\"");
                
                final Double nextLineLength = noTextLengthPerLineCalcRequired ? nextLength :
                    // need to box the result here as the type of the ternary operation would be 'double' otherwise
                    //  yielding NPEs if 'nextLength' is 'null'
                    Double.valueOf(EclipsePlacementUtil.estimateTextSize(fontData, line).getWidth());
                
                // text length
                content.append(textLength(nextLineLength));
                // semantic data
                content.append(isSingleLine ? " "
                    // style
                    + style(style)
                    // semantic data
                    + attributes(false) : ""
                );
                content.append(textLineAttributes(line, i++));
                content.append(">");
                content.append(line);
                content.append("</text>");
            }
        } else {
            // without a display just use the pt size as line height for multiline text

            final int fontSize = getFont().getSize();
            float y = fontSize;
            for (final String line : lines) {
                content.append("\n" + indentation);
                content.append("<text x=\"0\" y=\"").append(y).append("\"");
                content.append(isSingleLine ? " "
                    // style
                    + style(style)
                    // semantic data
                    + attributes(false) : ""
                );
                content.append(textLineAttributes(line, i++));
                content.append(">");
                content.append(line);
                content.append("</text>");
            }
        }

        if (content.length() == 0)
            return "";
        else
            return content.toString().substring(1); // trim the leading line break
    }

    protected float getAdjustedFontHeight(int height, int ascent, int descent, boolean firstLine) {
        // FIXME 
        // The following approximation has been determined experimentally
        //  as each of the browser/awt/swt seem to determine 
        //  slightly different values ...
        // fm.getLeading / 2 seems to be a good value at least under windows
        // for OSX 'leading' seems to be hard-wired to zero,
        //  hence 'height == ascent + descent' holds there
        
        final int leading = height - ascent - descent;
        return displayScaleY * (leading + ascent + (firstLine ? 0f : descent));
    }

    protected String textLength(Double nextLength) {
        if (nextLength == null)
            return "";

        else
            return " textLength=\"" + nextLength.floatValue() + "px\" lengthAdjust=\"spacingAndGlyphs\"";
    }

    /**
     * KLighD uses SWT to estimate font sizes, hence we do 
     * the same when exporting svgs.
     */
    @Override
    public FontMetrics getFontMetrics() {
        // use swt functionality if a display is available
        if (display != null) {
            gc.setFont(getSWTFont());
            return new PseudoAWTFontMetrics(getFont(), gc.getFontMetrics());
        } else {
            // fallback to the awt metrics
            return super.getFontMetrics();
        }
    }

    /**
     * Gets the height of the current font.
     * @return The current font height.
     */
    public float getFontHeight() {
        Font font = this.getFont();
        Map<?, ?> attributes = FontUtilities.getAttributes(font);
        return (Float) attributes.get(TextAttribute.SIZE);
    }
    
    /**
     * Creates the properties list for the given font.
     * Family, size, bold italic, underline and strikethrough are converted.
     * {@link java.awt.font.TextAttribute#SUPERSCRIPT}
     * is handled by {@link java.awt.Font#getTransform()}
     *
     * @return properties in svg style  for the font
     * @param font Font to
     */
    private Properties getFontProperties(Font font) {
        Properties result = new Properties();

        // attribute for font properties
        Map /*<TextAttribute, ?>*/<Attribute, Object> attributes = FontUtilities.getAttributes(font);

        // dialog.bold -> Helvetica with TextAttribute.WEIGHT_BOLD
        SVGFontTable.normalize(attributes);

        // family
        result.put("font-family", attributes.get(TextAttribute.FAMILY));

        // weight
        if (TextAttribute.WEIGHT_BOLD.equals(attributes.get(TextAttribute.WEIGHT))) {
            result.put("font-weight", "bold");
        } else {
            result.put("font-weight", "normal");
        }

        // posture
        if (TextAttribute.POSTURE_OBLIQUE.equals(attributes.get(TextAttribute.POSTURE))) {
            result.put("font-style", "italic");
        } else {
            result.put("font-style", "normal");
        }

        Object ul = attributes.get(TextAttribute.UNDERLINE);
        if (ul != null) {
            // underline style, only supported by CSS 3
            if (TextAttribute.UNDERLINE_LOW_DOTTED.equals(ul)) {
                result.put("text-underline-style", "dotted");
            } else if (TextAttribute.UNDERLINE_LOW_DASHED.equals(ul)) {
                result.put("text-underline-style", "dashed");
            } else if (TextAttribute.UNDERLINE_ON.equals(ul)) {
                result.put("text-underline-style", "solid");
            }

            // the underline itself, supported by CSS 2
            result.put("text-decoration", "underline");
        }

        if (attributes.get(TextAttribute.STRIKETHROUGH) != null) {
            // is the property allready witten?
            if  (ul == null) {
                result.put("text-decoration", "underline, line-through");
            } else {
                result.put("text-decoration", "line-through");
            }
        }

        /*
         * A font's size is specified in 'pt' which is a relative size where a height of 72pt
         * corresponds to 1 inch. When a font is rendered on a screen however, these pts are converted
         * to pixels and sizes of nodes and boxes are determined correspondingly. We thus use a px size
         * if we are able to determine the device with which the font was rendered (i.e. the device).
         */
        Float size = (Float) attributes.get(TextAttribute.SIZE);
        if (display != null || Klighd.simulateSwtFontSizeInAwt()) {
            result.put("font-size", fixedPrecision(size.floatValue() * pointToPxFactor) + "px");
        } else {
            result.put("font-size", fixedPrecision(size.floatValue()) + "pt");
        }

        return result;
    }

    /*
     * ================================================================================ |
     * 6. Transformations
     * ================================================================================
     */
    protected void writeTransform(AffineTransform transform) throws IOException {
        // written when needed
    }

    protected void writeSetTransform(AffineTransform transform)
            throws IOException {
        // written when needed
    }

    /*
     * ================================================================================ |
     * 7. Clipping
     * ================================================================================
     */
    protected void writeClip(Shape s) throws IOException {
        // written when needed
    }

    protected void writeSetClip(Shape s) throws IOException {
        // written when needed
    }

    /*
     * ================================================================================ |
     * 8. Graphics State
     * ================================================================================
     */
    /* 8.1. stroke/linewidth */
    protected void writeWidth(float width) throws IOException {
        // written when needed
    }

    protected void writeCap(int cap) throws IOException {
        // Written when needed
    }

    protected void writeJoin(int join) throws IOException {
        // written when needed
    }

    protected void writeMiterLimit(float limit) throws IOException {
        // written when needed
    }

    protected void writeDash(float[] dash, float phase) throws IOException {
        // written when needed
    }

    /**
     * return the style tag for the stroke
     *
     * @param s
     *            Stroke to convert
     * @param all
     *            all attributes (not only the differences to defaultStroke) are
     *            handled
     * @return corresponding style string
     */
    private Properties getStrokeProperties(Stroke s, boolean all) {
        Properties result = new Properties();

        // only BasisStrokes are written
        if (!(s instanceof BasicStroke)) {
            return result;
        }

        BasicStroke stroke = (BasicStroke) s;

        // append linecap
        if (all || (stroke.getEndCap() != defaultStroke.getEndCap())) {
            // append cap
            switch (stroke.getEndCap()) {
                default:
                case BasicStroke.CAP_BUTT:
                    result.put("stroke-linecap", "butt");
                    break;
                case BasicStroke.CAP_ROUND:
                    result.put("stroke-linecap", "round");
                    break;
                case BasicStroke.CAP_SQUARE:
                    result.put("stroke-linecap", "square");
                    break;
            }
        }

        // append dasharray
        if (all
                || !Arrays.equals(stroke.getDashArray(), defaultStroke
                        .getDashArray())) {
            if (stroke.getDashArray() != null
                    && stroke.getDashArray().length > 0) {
                StringBuffer array = new StringBuffer();
                for (int i = 0; i < stroke.getDashArray().length; i++) {
                    if (i > 0) {
                        array.append(",");
                    }
                    // SVG does not allow dash entry to be zero (Firefox 2.0).
                    float dash = stroke.getDashArray()[i];
                    array.append(fixedPrecision(dash > 0 ? dash : 0.1));
                }
                result.put("stroke-dasharray", array.toString());
            } else {
                result.put("stroke-dasharray", "none");
            }
        }

        if (all || (stroke.getDashPhase() != defaultStroke.getDashPhase())) {
            result.put("stroke-dashoffset", fixedPrecision(stroke.getDashPhase()));
        }

        // append meter limit
        if (all || (stroke.getMiterLimit() != defaultStroke.getMiterLimit())) {
            result.put("stroke-miterlimit", fixedPrecision(stroke.getMiterLimit()));
        }

        // append join
        if (all || (stroke.getLineJoin() != defaultStroke.getLineJoin())) {
            switch (stroke.getLineJoin()) {
                default:
                case BasicStroke.JOIN_MITER:
                    result.put("stroke-linejoin", "miter");
                    break;
                case BasicStroke.JOIN_ROUND:
                    result.put("stroke-linejoin", "round");
                    break;
                case BasicStroke.JOIN_BEVEL:
                    result.put("stroke-linejoin", "bevel");
                    break;
            }
        }

        // append linewidth
        if (all || (stroke.getLineWidth() != defaultStroke.getLineWidth())) {
            // width of 0 means thinnest line, which does not exist in SVG
            if (stroke.getLineWidth() == 0) {
                result.put("stroke-width", fixedPrecision(0.000001f));
            } else {
                result.put("stroke-width", fixedPrecision(stroke.getLineWidth()));
            }
        }

        return result;
    }

    /* 8.2. paint/color */
    public void setPaintMode() {
        writeWarning(getClass() + ": setPaintMode() not implemented.");
    }

    public void setXORMode(Color c1) {
        writeWarning(getClass() + ": setXORMode(Color) not implemented.");
    }

    protected void writePaint(Color c) throws IOException {
        // written with every draw
    }

    protected void writePaint(GradientPaint paint) throws IOException {
        if (gradients.get(paint) == null) {
            String name = "gradient-" + gradients.size();
            gradients.put(paint, name);
            Point2D p1 = paint.getPoint1();
            Point2D p2 = paint.getPoint2();
            os.println("<defs>");
            os.print("  <linearGradient id=\"" + name + "\" ");

            // special treatment of klighd's gradients, to support rotations
            if (paint instanceof KlighdGradientPaint) {
                // To make things easier and be able to reuse specified gradients, 
                //  we let the gradient always fill the whole bounding box. Thus we do not 
                //  need to specify explicit coordinates and can easily rotate 
                //  the gradient
                os.print("gradientUnits=\"objectBoundingBox\" ");
                os.print("gradientTransform=\"rotate("
                        + (((KlighdGradientPaint) paint).getRotation()) + ")\" ");
            } else {
                os.print("x1=\"" + fixedPrecision(p1.getX()) + "\" ");
                os.print("y1=\"" + fixedPrecision(p1.getY()) + "\" ");
                os.print("x2=\"" + fixedPrecision(p2.getX()) + "\" ");
                os.print("y2=\"" + fixedPrecision(p2.getY()) + "\" ");
                os.print("gradientUnits=\"userSpaceOnUse\" ");
                os.print("spreadMethod=\""
                        + ((paint.isCyclic()) ? "reflect" : "pad") + "\" ");
            }
            os.println(">");
            os.println("    <stop offset=\"0\" stop-color=\""
                    + hexColor(paint.getColor1()) + "\" " + "opacity-stop=\""
                    + alphaColor(paint.getColor1()) + "\" />");
            os.println("    <stop offset=\"1\" stop-color=\""
                    + hexColor(paint.getColor2()) + "\" " + "opacity-stop=\""
                    + alphaColor(paint.getColor2()) + "\" />");
            os.println("  </linearGradient>");
            os.println("</defs>");
        }

        // create style
        Properties style = new Properties();
        style.put("stroke", hexColor(getPaint()));

        // write style
        os.print("<g ");
        os.print(style(style));
        os.println(">");

        // close color later
        closeTags.push("</g>");
        pushComment(closeTags, " <!-- color -->");
    }

    protected void writePaint(TexturePaint paint) throws IOException {
        // written when needed
    }

    protected void writePaint(Paint p) throws IOException {
        // written when needed
    }

    /* 8.3. font */
    protected void writeFont(Font font) throws IOException {
        // written when needed
    }

    /*
     * ================================================================================ |
     * 9. Auxiliary
     * ================================================================================
     */
    public GraphicsConfiguration getDeviceConfiguration() {
        writeWarning(getClass() + ": getDeviceConfiguration() not implemented.");
        return null;
    }

    public void writeComment(String s) throws IOException {
        if (writeComments) {
            os.println("<!-- " + s + " -->");
        }
    }
    
    public void pushComment(final Stack<String> stack, String s) {
        if (writeComments) {
            // assure that the same number of elements is on the stack as before
            stack.push(stack.pop() + s);
        }
    }

    public String toString() {
        return "SVGGraphics2D";
    }

    /*
     * ================================================================================ |
     * 10. Private/Utility Methos
     * ================================================================================
     */

    /**
     * Encapsulates a SVG-Tag by the given transformation matrix
     *
     * @param t
     *            Transformation
     * @param s
     *            SVG-Tag
     */
    private String getTransformedString(AffineTransform t, String s) {
        return getTransformedString(t,  null, s);
    }
    
    /**
     * Encapsulates a SVG-Tag by the given transformation matrix
     *
     * @param t
     *            Transformation
     * @param additionalProperties
     *            additional properties to be set on the element
     * @param s
     *            SVG-Tag
     */
    private String getTransformedString(AffineTransform t, String additionalProperties, String s) {
        StringBuffer result = new StringBuffer();

        if (t != null && !t.isIdentity()) {
            result.append("<g transform=\"matrix(");
            result.append(fixedPrecision(t.getScaleX()));
            result.append(", ");
            result.append(fixedPrecision(t.getShearY()));
            result.append(", ");
            result.append(fixedPrecision(t.getShearX()));
            result.append(", ");
            result.append(fixedPrecision(t.getScaleY()));
            result.append(", ");
            result.append(fixedPrecision(t.getTranslateX()));
            result.append(", ");
            result.append(fixedPrecision(t.getTranslateY()));
            result.append(")\"");
            if (additionalProperties != null)
                result.append(" ").append(additionalProperties);
            result.append(">\n");
            
        } else if (additionalProperties != null) {
            result.append("<g ").append(additionalProperties).append(">\n");
        }

        result.append(s);

        if (t != null && !t.isIdentity() || additionalProperties != null) {
            result.append("\n</g>");
            if (writeComments) {
                result.append(" <!-- transform -->");
            }
        }

        return result.toString();
    }

    /**
     * Encapsulates a SVG-Tag by the current clipping area matrix
     *
     * @param s SVG-Tag
     * @return SVG Tag encapsulated by the current clip
     */
    private String getClippedString(String s) {
        StringBuffer result = new StringBuffer();

        // clipping
        if (isProperty(CLIP) && getClip() != null) {
            // SVG uses unique lip numbers, don't reset allways increment them
            clipNumber.set(clipNumber.getInt() + 1);

            // define clip
            result.append("<clipPath id=\"clip");
            result.append(clipNumber.getInt());
            result.append("\">\n  ");
            result.append(getPath(getClip().getPathIterator(null)));
            result.append("\n</clipPath>\n");

            // use clip
            result.append("<g clip-path=\"url(#clip");
            result.append(clipNumber.getInt());
            result.append(")\">\n");
        }

        // append the string
        result.append(s);

        // close clipping
        if (isProperty(CLIP) && getClip() != null) {
            result.append("\n</g>");
            if (writeComments) {
                result.append("<!-- clip");
                result.append(clipNumber.getInt());
                result.append(" -->");
            }
        }

        return result.toString();
    }

    private float alphaColor(Paint p) {
        if (p instanceof Color) {
            return (float) (getPrintColor((Color) p).getAlpha() / 255.0);
        } else if (p instanceof GradientPaint) {
            return 1.0f;
        } else if (p instanceof TexturePaint) {
            return 1.0f;
        }
        writeWarning(getClass() + ": alphaColor() not implemented for "
                + p.getClass() + ".");
        return 1.0f;
    }

    private String hexColor(Paint p) {
        if (p instanceof Color) {
            return hexColor(getPrintColor((Color) p));
        } else if (p instanceof GradientPaint) {
            return hexColor((GradientPaint) p);
        } else if (p instanceof TexturePaint) {
            return hexColor((TexturePaint) p);
        }
        writeWarning(getClass() + ": hexColor() not implemented for "
                + p.getClass() + ".");
        return "#000000";
    }

    private String hexColor(Color c) {
        String s1 = Integer.toHexString(c.getRed());
        s1 = (s1.length() != 2) ? "0" + s1 : s1;

        String s2 = Integer.toHexString(c.getGreen());
        s2 = (s2.length() != 2) ? "0" + s2 : s2;

        String s3 = Integer.toHexString(c.getBlue());
        s3 = (s3.length() != 2) ? "0" + s3 : s3;

        return "#" + s1 + s2 + s3;
    }

    private String hexColor(GradientPaint p) {
        return "url(#" + gradients.get(p) + ")";
    }

    private String hexColor(TexturePaint p) {
        return "url(#" + textures.get(p) + ")";
    }

    protected static String getPathContent(PathIterator path) {
        StringBuffer result = new StringBuffer();

        double[] coords = new double[6];
        result.append("d=\"");
        while (!path.isDone()) {
            int segType = path.currentSegment(coords);

            switch (segType) {
                case PathIterator.SEG_MOVETO:
                    result.append("M ");
                    result.append(fixedPrecision(coords[0]));
                    result.append(" ");
                    result.append(fixedPrecision(coords[1]));
                    break;
                case PathIterator.SEG_LINETO:
                    result.append("L ");
                    result.append(fixedPrecision(coords[0]));
                    result.append(" ");
                    result.append(fixedPrecision(coords[1]));
                    break;
                case PathIterator.SEG_CUBICTO:
                    result.append("C ");
                    result.append(fixedPrecision(coords[0]));
                    result.append(" ");
                    result.append(fixedPrecision(coords[1]));
                    result.append(" ");
                    result.append(fixedPrecision(coords[2]));
                    result.append(" ");
                    result.append(fixedPrecision(coords[3]));
                    result.append(" ");
                    result.append(fixedPrecision(coords[4]));
                    result.append(" ");
                    result.append(fixedPrecision(coords[5]));
                    break;
                case PathIterator.SEG_QUADTO:
                    result.append("Q ");
                    result.append(fixedPrecision(coords[0]));
                    result.append(" ");
                    result.append(fixedPrecision(coords[1]));
                    result.append(" ");
                    result.append(fixedPrecision(coords[2]));
                    result.append(" ");
                    result.append(fixedPrecision(coords[3]));
                    break;
                case PathIterator.SEG_CLOSE:
                    result.append("z");
                    break;
            }

            // Move to the next segment.
            path.next();

            // Not needed but makes the output readable
            if (!path.isDone()) {
                result.append(" ");
            }
        }
        result.append("\"");

        return result.toString();
    }

    protected String getPath(PathIterator path) {
        StringBuffer result = new StringBuffer();

        result.append("<path ");
        result.append(attributes(true));
        result.append(getPathContent(path));
        result.append("/>");

        return result.toString();
    }


    public void addSemanticData(final KlighdSemanticDiagramData nextSemanticData) {
        this.semanticData = nextSemanticData;
    }

    private String attributes(boolean resetSemData) {
        if (semanticData == null) {
            return "";
        }

        final StringBuffer sb = new StringBuffer();

        for (final Entry<String, String> e : semanticData) {

            // special tags
            if (e.getKey().equals(KlighdConstants.SEMANTIC_DATA_ID)) {
                sb.append(" id" + "=\"" + e.getValue() + "\"");

            } else if (e.getKey().equals(KlighdConstants.SEMANTIC_DATA_CLASS)) {
                sb.append(" class" + "=\"" + e.getValue() + "\"");

            } else if (e.getKey().equals(KlighdConstants.SEMANTIC_DATA_RAW)) {
                sb.append(" " + e.getValue());
                
            } else {
                sb.append(" klighd:" + e.getKey() + "=\"" + e.getValue() + "\"");
            }
        }

        if (resetSemData) {
            resetSemanticData();
        }
        return sb.toString();
    }

    private String textLineAttributes(final String textLine, final int noOfLine) {
        if (semanticData == null) {
            return "";
        }

        final StringBuffer sb = new StringBuffer();

        for (final Entry<String, String> e : semanticData.textLineIterable(textLine, noOfLine)) {

            // special tags
            if (e.getKey().equals(KlighdConstants.SEMANTIC_DATA_ID)) {
                sb.append(" id" + "=\"" + e.getValue() + "\"");

            } else if (e.getKey().equals(KlighdConstants.SEMANTIC_DATA_RAW)) {
                sb.append(" " + e.getValue());

            } else {
                sb.append(" klighd:" + e.getKey() + "=\"" + e.getValue() + "\"");
            }
        }

        return sb.toString();
    }

    private void resetSemanticData() {
        this.semanticData = null;
    }


    //If no semantic data is set these groups are unnecessary clutter. This
    // stack keeps track of them and prevents printing of those that are not needed.
    private Stack<Boolean> groupsStack = new Stack<Boolean>();
    public void startGroup(KlighdSemanticDiagramData nextSemanticData) {
        this.semanticData = nextSemanticData;
        if ((this.semanticData != null && this.semanticData.iterator().hasNext())) {
            os.write("<g");
            os.write(attributes(true));
            os.write(">\n");
            groupsStack.push(true);
        } else {
            groupsStack.push(false);
        }
    }

    public void endGroup() {
        if (groupsStack.peek()) {
            os.write("</g>\n");
        }
        groupsStack.pop();
    }
    

    /**
     * For a given "key -> value" property set the
     * method creates
     * style="key1:value1;key2:value2;" or
     * key2="value2" key2="value2" depending on
     * {@link #STYLABLE}.
     *
     * @param style properties to convert
     * @return String
     */
    private String style(Properties style) {
        if (style == null || style.isEmpty()) {
            return "";
        }

        StringBuffer result = new StringBuffer();
        boolean styleable = isProperty(STYLABLE);

        // embed everything in a "style" attribute
        if (styleable) {
            result.append("style=\"");
        }

        Enumeration<?> keys = style.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = style.getProperty(key);

            result.append(key);

            if (styleable) {
                result.append(":");
                result.append(value);
                result.append(";");
            } else {
                result.append("=\"");
                result.append(value);
                result.append("\"");
                if (keys.hasMoreElements()) {
                    result.append(" ");
                }
            }
        }

        // close the style attribute
        if (styleable) {
            result.append("\"");
        }

        return result.toString();
    }

    
    /**
     * Builds up a AWT {@link Path2D} according to a given SWT Geometry {@link PathData}.
     * 
     * @param pathData
     *            provides the segments the new path shall contain.
     *            the device to create the path on
     * @return the desired {@link Path2D} object.
     */
    public static Path2D createAWTPath(final PathData pathData) {
        final Path2D p2d = new Path2D.Float();
        final float[] pts = pathData.points;

        int i = 0;
        for (byte type : pathData.types) {
            switch (type) {
            case SWT.PATH_MOVE_TO:
                p2d.moveTo(pts[i++], pts[i++]);
                break;
            case SWT.PATH_LINE_TO:
                p2d.lineTo(pts[i++], pts[i++]);
                break;
            case SWT.PATH_CLOSE:
                p2d.closePath();
                break;
            case SWT.PATH_QUAD_TO:
                p2d.quadTo(pts[i++], pts[i++], pts[i++], pts[i++]);
                break;
            case SWT.PATH_CUBIC_TO:
                p2d.curveTo(pts[i++], pts[i++], pts[i++], pts[i++], pts[i++], pts[i++]);
                break;
            default:
            }
        }
        return p2d;
    }
    
    private org.eclipse.swt.graphics.Font getSWTFont() {
        return getSWTFont(getFont());
    }
    
    /**
     * @param font
     *            the awt font for which to assemble an swt font.
     * @return either the corresponding swt font, or {@code null} if no dispaly exists.
     */
    private org.eclipse.swt.graphics.Font getSWTFont(final Font font) {
        if (!awtSwtFontCache.containsKey(font) && display != null) {
            // note that the style constants for swt and awt are equal
            org.eclipse.swt.graphics.Font swtFont =
                    new org.eclipse.swt.graphics.Font(display, getFontData(font));
            awtSwtFontCache.put(font, swtFont);
        }
        return awtSwtFontCache.get(font);
    }
    
    protected FontData getFontData(Font font) {
        return new FontData(font.getName(), font.getSize(), getSWTFontStyle(font));
    }
    
    private int getSWTFontStyle(Font font) {
        switch (font.getStyle()) {
        case Font.PLAIN:
            return SWT.NORMAL;
        case Font.BOLD:
            return SWT.BOLD;
        case Font.ITALIC:
            return SWT.ITALIC;
        }
        // default
        return SWT.NORMAL;
    }
    
    /**
     * for fixedPrecision(double d), SVG does not understand "1E-7"
     * we have to use ".0000007" instead
     */
    private static DecimalFormat scientific = new DecimalFormat(
        "#.####################",
        new DecimalFormatSymbols(Locale.US));

    /**
     * converts the double value to a representing string
     *
     * @param d double value to convert
     * @return same as string
     */
    public static String fixedPrecision(double d) {
        return scientific.format(d);
    }

    protected PrintWriter getOutputStream() {
        return os;
    }
}
