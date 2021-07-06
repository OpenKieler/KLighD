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
package de.cau.cs.kieler.klighd.piccolo.export;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.LineAttributes;

import de.cau.cs.kieler.klighd.DiagramExportConfig;
import de.cau.cs.kieler.klighd.KlighdConstants;
import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.eclipse.EclipseKlighdConstants;
import de.cau.cs.kieler.klighd.eclipse.microlayout.EclipsePlacementUtil;
import de.cau.cs.kieler.klighd.piccolo.KlighdSWTGraphics;

/**
 * Example export hook. Draws a border around the diagram, prints the author and a "Confidential"
 * watermark.
 *
 * Registration is to be done in plugin.xml like
 * <pre>
 * {@code
 * <extension
 *       point="de.cau.cs.kieler.klighd.piccolo.exportHooks">
 *    <exportHook
 *          class="de.cau.cs.kieler.klighd.piccolo.export.AuthorConfidentialExportHook"
 *          id="de.cau.cs.kieler.klighd.piccolo.export.AuthorConfidentialExportHook"
 *          supportedFormats="bmp, jpeg, png, pdf, printout">
 *    </exportHook>
 * </extension>}
 * </pre>
 *
 * @author csp
 * @author chsch
 */
public class AuthorConfidentialExportBranding extends AbstractExportBranding {

    private static final int BORDER_LINE_WIDTH = 3;
    private static final int BOTTOM_MARGIN = 30;
    private static final int TOP_LEFT_RIGHT_MARGIN = 20;
    private static final int BORDER_PADDING = 5;
    private static final int WATERMARK_ALPHA = 100;
    private static final double WATERMARK_PADDING_FACTOR = 0.8;

    // CHECKSTYLEOFF MagicNumber

    /**
     * {@inheritDoc}
     */
    @Override
    public void setViewContext(final ViewContext viewContext) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Trim getDiagramTrim(final Rectangle2D bounds) {
        return new Trim(100, 100, 200, 200);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Trim getDiagramTileTrimm(final Rectangle2D bounds, final Point dotsPerInch,
            final Trim deviceTrim) {
        if (bounds.getWidth() > 1000 && bounds.getHeight() > 1000) {
            return new Trim(500, 500, 200, 200);
        } else {
            return new Trim(50, 50, 20, 20);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void drawDiagramBackground(final KlighdSWTGraphics graphics,
            final DiagramExportConfig config) {
        graphics.setFillColor(EclipseKlighdConstants.RED);

        final Rectangle2D bounds = config.diagramBounds.getBounds2D();
        bounds.setRect(0, 0, 200 + bounds.getWidth(), 400 + bounds.getHeight());

        graphics.fill(new Rectangle2D.Double(0, 0, 100, bounds.getHeight()));
        graphics.fill(new Rectangle2D.Double(bounds.getWidth() - 100, 0, 100, bounds.getHeight()));

        final Path2D p = new Path2D.Float();
        p.moveTo(0, 0);
        p.lineTo(bounds.getWidth(), 0);
        p.lineTo(bounds.getWidth() - 100, 200);
        p.lineTo(100, 200);
        p.closePath();

        graphics.setFillColor(EclipseKlighdConstants.BLUE);

        graphics.fill(p);

        graphics.transform(AffineTransform.getRotateInstance(
              Math.toRadians(180), bounds.getCenterX(), bounds.getCenterY()));

        graphics.fill(p);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void drawDiagramTileBackground(final KlighdSWTGraphics graphics,
            final DiagramExportConfig config) {
        graphics.setFillColor(EclipseKlighdConstants.YELLOW);
        if (config.tileBounds.getWidth() > 1000 && config.tileBounds.getHeight() > 1000) {
            graphics.fill(new Rectangle2D.Double(500, 200,
                    config.tileBounds.getWidth() - 1000,
                    config.tileBounds.getHeight() - 400));
        } else {
            graphics.fill(new Rectangle2D.Double(50, 20,
                    config.tileBounds.getWidth() - 100,
                    config.tileBounds.getHeight() - 40));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void drawDiagramOverlay(final KlighdSWTGraphics graphics, final DiagramExportConfig config) {

        final Rectangle2D bounds = config.getDiagramBoundsIncludingTrim();

        final double innerWidth = bounds.getWidth() - (2 * TOP_LEFT_RIGHT_MARGIN);
        final double innerHeight = bounds.getHeight() - (TOP_LEFT_RIGHT_MARGIN + BOTTOM_MARGIN);

        // make border
        graphics.setLineAttributes(new LineAttributes(BORDER_LINE_WIDTH));
        graphics.setStrokeColor(EclipseKlighdConstants.WHITE);
        graphics.draw(new Rectangle2D.Double(
                TOP_LEFT_RIGHT_MARGIN - BORDER_PADDING, TOP_LEFT_RIGHT_MARGIN - BORDER_PADDING,
                innerWidth + 2 * BORDER_PADDING, innerHeight + 2 * BORDER_PADDING));

        // draw author
        graphics.transform(AffineTransform.getTranslateInstance(TOP_LEFT_RIGHT_MARGIN,
                bounds.getHeight() - TOP_LEFT_RIGHT_MARGIN));
        graphics.setFont(new FontData("Arial", KlighdConstants.DEFAULT_FONT_SIZE,
                EclipseKlighdConstants.DEFAULT_FONT_STYLE_SWT));
        graphics.drawText("Author: Max Mustermann");

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void drawDiagramTileOverlay(final KlighdSWTGraphics graphics,
            final DiagramExportConfig config) {
        // draw confidential

        final Dimension bounds = config.tileBounds;

        final double innerWidth = bounds.getWidth(); // - (2 * TOP_LEFT_RIGHT_MARGIN);
        final double innerHeight = bounds.getHeight(); // - (TOP_LEFT_RIGHT_MARGIN + BOTTOM_MARGIN);

        // font
        final FontData font = new FontData("Arial", KlighdConstants.DEFAULT_FONT_SIZE, SWT.BOLD);
        graphics.setFont(font);
        graphics.setAlpha(WATERMARK_ALPHA);

        // size & scale
        final String confidential = "Confidential";
        final Rectangle size =
                EclipsePlacementUtil.estimateTextSize(font, confidential).setBoundsOf(new Rectangle());

        final double scale = Math.sqrt(Math.pow(innerWidth, 2) + Math.pow(innerHeight, 2))
                / size.width * WATERMARK_PADDING_FACTOR;

        size.width *= scale;
        size.height *= scale;

        // transformations
        graphics.transform(AffineTransform.getTranslateInstance(
                bounds.getWidth() / 2d - size.width / 2d, bounds.getHeight() / 2d - size.height / 2d));
        graphics.transform(AffineTransform.getRotateInstance(
                innerWidth, -innerHeight, size.width / 2d, size.height / 2d));

        graphics.setStrokeColor(EclipseKlighdConstants.BLACK);
        graphics.draw(size);

        graphics.transform(AffineTransform.getScaleInstance(scale, scale));
        graphics.drawText(confidential);
    }
}
