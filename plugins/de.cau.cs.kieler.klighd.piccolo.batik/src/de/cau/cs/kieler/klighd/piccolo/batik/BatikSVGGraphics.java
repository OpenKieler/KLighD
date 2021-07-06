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
package de.cau.cs.kieler.klighd.piccolo.batik;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGeneratorContext.GraphicContextDefaults;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.util.SVGConstants;
import org.eclipse.swt.graphics.LineAttributes;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import de.cau.cs.kieler.klighd.KlighdConstants;
import de.cau.cs.kieler.klighd.eclipse.EclipseKlighdConstants;
import de.cau.cs.kieler.klighd.piccolo.export.KlighdAbstractSVGGraphics;

/**
 * A wrapper for batik's {@link SVGGraphics2D} svg generator. Allows to render the contents of
 * KlighD views as SVGs.
 * 
 * After painting to the graphics object, the {@link #getSVG()} method can be used to retrieve the
 * SVG as String.
 * 
 * @author uru
 * 
 * @see <a href="http://xmlgraphics.apache.org/batik/"> http://xmlgraphics.apache.org/batik/</a>
 */
public class BatikSVGGraphics extends KlighdAbstractSVGGraphics {

    private SVGGraphics2D graphicsDelegate;

    /**
     * Constructor.<br>
     * Delegates to {@link #KlighdBatikSVGGraphics(boolean) #KlighdBatikSVGGraphics(false)}.
     * 
     * @param bounds
     *            the bounds will be set as viewport values for the resulting root <svg ..> tag.
     */
    public BatikSVGGraphics(final Rectangle2D bounds) {
        this(bounds, false, false);
    }

    /**
     * Constructor.
     * 
     * @param bounds
     *            the bounds will be set as viewport values for the resulting root <svg ..> tag.
     * @param textAsShapes
     *            whether text should be rendered as shapes
     * @param embedFonts
     *            whether the texts' fonts shall be embedded in the output
     */
    public BatikSVGGraphics(final Rectangle2D bounds, final Boolean textAsShapes,
            final Boolean embedFonts) {
        super(null);

        // Get a DOMImplementation.
        final DOMImplementation domImpl = SVGDOMImplementation.getDOMImplementation();


        // Create an instance of org.w3c.dom.Document.
        final Document document =
                domImpl.createDocument(SVGConstants.SVG_NAMESPACE_URI, "svg", null); //$NON-NLS-1$

        // assemble context
        final SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(document);
        ctx.setEmbeddedFontsOn(embedFonts);
        
        final GraphicContextDefaults defaults = new GraphicContextDefaults();
        ctx.setGraphicContextDefaults(defaults);
        
        defaults.setBackground(Color.WHITE);
        
        // this setting influences the default stroke color as well as the default paint (fill) color!!
        defaults.setPaint(Color.BLACK);
        
        final LineAttributes lineAttributes = EclipseKlighdConstants.DEFAULT_LINE_ATTRIBUTES;
        defaults.setStroke(new BasicStroke(lineAttributes.width, lineAttributes.cap - 1,
                lineAttributes.join - 1, lineAttributes.miterLimit, lineAttributes.dash,
                lineAttributes.dashOffset));
        
        defaults.setFont(new Font(KlighdConstants.DEFAULT_FONT_NAME,
                KlighdConstants.DEFAULT_FONT_STYLE, KlighdConstants.DEFAULT_FONT_SIZE));
        
        final RenderingHints hints = new RenderingHints(null);
        defaults.setRenderingHints(hints);

        // be careful while modifying these definitions
        //  they're evaluated in org.apache.batik.svggen.SVGRenderingHints.toSVG(...)

        // + RENDERING -> sets all other hints to initial value.
        hints.put(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        // + FRACTIONAL_METRICS -> sets initial values for text-rendering and shape-rendering.
        hints.put(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        // + ANTIALIASING -> shape-rendering and text-rendering
        hints.put(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        // + COLOR_RENDERING -> color-rendering
        hints.put(RenderingHints.KEY_COLOR_RENDERING,
                RenderingHints.VALUE_COLOR_RENDER_SPEED);
        // + INTERPOLATION -> image-rendering
        hints.put(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        // + TEXT_ANTIALIASING -> text-rendering
        hints.put(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // create and configure the graphics object
        graphicsDelegate = new SVGGraphics2D(ctx, textAsShapes);
        graphicsDelegate.setSVGCanvasSize(
                new Dimension((int) Math.ceil(bounds.getWidth()), (int) Math.ceil(bounds.getHeight())));
        
        // IMPORTANT
        super.setGraphicsDelegate(graphicsDelegate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSVG() {
        final StringWriter sw = new StringWriter();
        try {
// An alternative to uru's way below and SVGGraphics2D#setSVGCanvasSize (or may be a supplement)
// seen in GMF code:
            // Define the view box
//            Element svgRoot = graphicsDelegate.getRoot();
//            svgRoot.setAttributeNS(null, "viewBox", String.valueOf(0) + " " + String.valueOf(0)
//                    + " " + String.valueOf(viewBox.width) + " " + String.valueOf(viewBox.height));

            // Write the document to the stream
//            Transformer transformer = TransformerFactory.newInstance().newTransformer();
//            transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
//            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
//            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
//
//            DOMSource source = new DOMSource(svgRoot);
//            StreamResult result = new StreamResult(sw);
//            transformer.transform(source, result);
            graphicsDelegate.stream(sw, true);
        } catch (final Exception e) {
            e.printStackTrace();
        }

// chsch: deactivated the following as the viewport is set via setSVGCanvasSize(...) in constructor!
//        // batik does not create any viewport elements, so we add them here
//        StringBuffer sb = new StringBuffer(sw.toString());
//        sb.insert(
//                sb.indexOf("<svg") + "<svg".length() + 1,
//                " x=\"" + (int) bounds.getX() + "px\" y=\"" + (int) bounds.getY() + "px\" "
//                        + "width=\"" + (int) bounds.getWidth() + "px\" height=\""
//                        + (int) bounds.getHeight() + "px\" ");
//
//        return sb.toString();
        return sw.toString();
    }

    @Override
    public void stream(final OutputStream out) throws IOException {
        graphicsDelegate.stream(new OutputStreamWriter(out));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        // done implicitly when retrieving the svg
        // call it just to be sure
        getSVG();
    }
}
