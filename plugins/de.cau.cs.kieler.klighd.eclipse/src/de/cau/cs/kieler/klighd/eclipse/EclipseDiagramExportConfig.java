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
package de.cau.cs.kieler.klighd.eclipse;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

import org.eclipse.swt.widgets.Display;

import de.cau.cs.kieler.klighd.DiagramExportConfig;
import de.cau.cs.kieler.klighd.IExportBranding.Trim;
import de.cau.cs.kieler.klighd.ViewContext;

/**
 * A record comprising all information required for diagram export supporting all the features like
 * exporting a clipped diagram, providing a preview, applying export customizers, etc.<br>
 * This class is shall not be instantiated by application code but will be by KLighD internally.
 *
 * @author chsch
 */
public class EclipseDiagramExportConfig extends DiagramExportConfig {

    /**
     * Constructor.<br>
     * {@link #dotsPerInch} is set to {@link Display#getDPI()} of the default Display.
     *
     * @param viewContext
     *            the {@link ViewContext} belonging to the diagram being exported
     * @param diagramBounds
     *            the bounds of the diagram area to be exported
     * @param tileBounds
     *            the bounds of the particular diagram tiles
     * @param diagramScale
     *            the scale factor to be applied to the diagram (e.g. chosen by the user while
     *            exporting raster images or during printout), just affects the image quality for
     *            raster images, has no effect on the visibility of diagram elements or diagram
     *            figure parts; see {@link #setApplyCameraZoomLevel(boolean)} for the latter
     * @param pages
     *            the number of pages the diagram is to be printed, at least 1.
     */
    public EclipseDiagramExportConfig(final ViewContext viewContext, final Rectangle2D diagramBounds,
            final Dimension tileBounds, final double diagramScale, final int pages) {
        super(viewContext, diagramBounds, tileBounds, diagramScale, new Point(), Trim.EMPTY_TRIM, pages);

        final org.eclipse.swt.graphics.Point dpi = Display.getCurrent().getDPI();
        this.dotsPerInch.setLocation(dpi.x, dpi.y);
    }
}