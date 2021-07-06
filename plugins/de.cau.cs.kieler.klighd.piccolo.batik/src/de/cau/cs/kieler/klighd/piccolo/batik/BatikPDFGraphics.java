/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2014 by
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
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGeneratorContext.GraphicContextDefaults;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.SVGConstants;
import org.apache.fop.svg.PDFTranscoder;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import de.cau.cs.kieler.klighd.KlighdConstants;
import de.cau.cs.kieler.klighd.eclipse.EclipseKlighdConstants;
import de.cau.cs.kieler.klighd.eclipse.IDiagramExporter;
import de.cau.cs.kieler.klighd.piccolo.export.KlighdAbstractSVGGraphics;
import de.cau.cs.kieler.klighd.piccolo.internal.KlighdCanvas;
import de.cau.cs.kieler.klighd.piccolo.internal.util.KlighdPaintContext;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * 
 * Save klighd diagram as pdf using batik.
 * 
 * @author ckru
 * 
 */
public class BatikPDFGraphics extends KlighdAbstractSVGGraphics implements IDiagramExporter {

    private SVGGraphics2D graphicsDelegate;

    /**
     * {@inheritDoc}
     */
    public BatikPDFGraphics() {
        super(null);
    }

    @Override
    public String getSVG() {
        final StringWriter sw = new StringWriter();
        try {
            graphicsDelegate.stream(sw, true);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return sw.toString();
    }

    @Override
    public void clear() {
        getSVG();
    }

    @Override
    public void stream(final OutputStream output) throws IOException {
        final PNGTranscoder t = new PNGTranscoder();
        final Reader r = new StringReader(getSVG());
        final TranscoderInput in = new TranscoderInput(r);
        final TranscoderOutput out = new TranscoderOutput(output);
        try {
            t.transcode(in, out);
        } catch (final TranscoderException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    public IStatus export(final Control control, final ExportData data) {

        final PCamera camera = ((KlighdCanvas) control).getCamera();

        Rectangle2D bounds = null;
        if (data.cameraViewport()) {
            bounds = camera.getBounds();
        } else {
            // we want the svg to contain all elements, not just the visible area
            bounds = camera.getRoot().getFullBounds();
        }

        // Get a DOMImplementation.
        final DOMImplementation domImpl = SVGDOMImplementation.getDOMImplementation();

        // Create an instance of org.w3c.dom.Document.
        final Document document = domImpl.createDocument(SVGConstants.SVG_NAMESPACE_URI,
                "svg", null); //$NON-NLS-1$

        // assemble context
        final SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(document);
        ctx.setEmbeddedFontsOn(data.embedFonts());

        final GraphicContextDefaults defaults = new GraphicContextDefaults();
        ctx.setGraphicContextDefaults(defaults);

        // Check if the background should be exported
        if (!data.transparentBackground()) {
            // Create a background with the configured color
            defaults.setBackground(new Color(data.backgroundColor().red,
                    data.backgroundColor().green, data.backgroundColor().blue));
        }

        // this setting influences the default stroke color as well as the default paint (fill)
        // color!!
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
        // they're evaluated in org.apache.batik.svggen.SVGRenderingHints.toSVG(...)

        // + RENDERING -> sets all other hints to initial value.
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        // + FRACTIONAL_METRICS -> sets initial values for text-rendering and shape-rendering.
        hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        // + ANTIALIASING -> shape-rendering and text-rendering
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        // + COLOR_RENDERING -> color-rendering
        hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
        // + INTERPOLATION -> image-rendering
        hints.put(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        // + TEXT_ANTIALIASING -> text-rendering
        hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // create and configure the graphics object
        graphicsDelegate = new SVGGraphics2D(ctx, data.textAsShapes());
        graphicsDelegate.setSVGCanvasSize(new Dimension((int) Math.ceil(bounds.getWidth()),
                (int) Math.ceil(bounds.getHeight())));

        // IMPORTANT
        super.setGraphicsDelegate(graphicsDelegate);

        final KlighdPaintContext paintContext =
                KlighdPaintContext.createExportDiagramPaintContext(this, false, true, false);
        paintContext.setRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);

        // perform the painting
        if (data.cameraViewport()) {
            // only render the current viewport
            camera.fullPaint(paintContext);
        } else {
            // render the whole diagram
            @SuppressWarnings("unchecked")
            final List<PLayer> layersReference = camera.getLayersReference();
            for (final PLayer layer : layersReference) {
                layer.fullPaint(paintContext);
            }
        }

        // Transcode the image to pdf
        final PDFTranscoder t = new PDFTranscoder();
        final String svg = getSVG();
        final Reader r = new StringReader(svg);
        final TranscoderInput in = new TranscoderInput(r);

        try {
            final OutputStream stream = data.createOutputStream();
            final TranscoderOutput out = new TranscoderOutput(stream);
            t.transcode(in, out);
            stream.flush();
            stream.close();
            return Status.OK_STATUS;

        } catch (final Exception e) {
            return new Status(IStatus.ERROR, "de.cau.cs.kieler.klighd.piccolo.batik",
                    "KLighD Batik-based PDF export failed with the following exception", e);
        }
    }

}
