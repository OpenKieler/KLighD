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
package de.cau.cs.kieler.klighd;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.Collections;

import com.google.common.collect.ImmutableList;

import de.cau.cs.kieler.klighd.IExportBranding.Trim;

/**
 * A record comprising all information required for diagram export supporting all the features like
 * exporting a clipped diagram, providing a preview, applying export customizers, etc.<br>
 * This class is shall not be instantiated by application code but will be by KLighD internally.
 *
 * @author chsch
 */
public class DiagramExportConfig {

    // this is just a data record class introduced for reducing method parameters, so
    // CHECKSTYLEOFF Visibility|Field

    /** The {@link ViewContext} belonging to the diagram being exported. */
    public final ViewContext viewContext;

    /**
     * The (unadjusted) bounds of the diagram to be exported (excluding the diagram trim).
     *
     * @see #getDiagramBoundsIncludingTrim()
     */
    public final Rectangle2D diagramBounds;

    /** The (unadjusted) size of the diagram tiles created on printouts or (raster) image exports. */
    public final Dimension tileBounds;

    /** The scale being chosen by the tool user while creating printouts or (raster) image exports. */
    public final double diagramScale;

    /**
     * The configured image resolution; during printouts this field is set to
     * {@link org.eclipse.swt.printing.Printer#getDPI() Printer#getDPI()} of the chosen printer,
     * otherwise to {@link Display#getDPI()} of the default Display.
     *
     * @see org.eclipse.swt.graphics.Device#getDPI()
     */
    public final Point dotsPerInch;

    /**
     * The {@link Trim} imposed by the used {@link org.eclipse.swt.printing.Printer Printer}
     * if the corresponding export is a print, is {@link Trim#EMPTY_TRIM} otherwise.
     */
    public final Trim deviceTrim;

    /**
     * Determines whether the diagram part being currently visible on the screen shall be exported
     * (<code>true</code>), or whether the whole currently displayed diagram (part/clip) shall be
     * exported (<code>false</code>).
     */
    public boolean exportViewport = false;

    /**
     * Determines whether the diagram zoom level determined by the diagram main camera's view
     * transform shall be used while evaluating the visibility of the particular diagram elements
     * and diagram element figure parts, if <code>false</code> a diagram zoom level of
     * <code>1.0<code> is assumed.
     */
    public boolean applyCameraZoomLevel = false;

    /**
     * Determines whether semantic information attached to diagram elements shall be exported.<br>
     * This available for SVG exports only.
     */
    public boolean exportSemanticData = false;

    /**
     * Determines whether the @{code textLength} information shall be added to text fields while
     * exporting.<br>
     * This available for SVG exports only.
     */
    public boolean setTextLengths = false;
    
    /** The list of {@link IExportBranding}s being enabled for the current diagram export. */
    public Iterable<IExportBranding> exportBrandings = Collections.emptyList();

    /** The cumulated overall diagram {@link Trim} required by the {@link #exportBrandings}. */
    public Trim diagramTrim = null;

    /** The cumulated diagram tile {@link Trim} required by the {@link #exportBrandings}. */
    public Trim tileTrim = null;

    /**
     * The number of pages the diagram is to be printed, at least 1. Is useful to add page infos
     * like 'page x/y' to printout pages. 
     */
    public final int pages;

    /**
     * The page number of the diagram tile to be printed or exported starting at 1, is updated after
     * each tile print or export.
     */
    public int pageNo = 0;

    /**
     * The row number of the diagram tile to be printed or exported starting at 0, is updated after
     * each tile print or export.
     */
    public int row = 0;

    /**
     * The column number of the diagram tile to be printed or exported starting at 0, is updated
     * after each tile print or export.
     */
    public int column = 0;

    /**
     * Indicates whether the currently handled tile is part of the first row, is updated after each
     * tile print or export.
     */
    public boolean firstRow = false;

    /**
     * Indicates whether the currently handled tile is part of the first column, is updated after
     * each tile print or export.
     */
    public boolean firstColumn = false;

    /**
     * Indicates whether the currently handled tile is part of the last row, is updated after each
     * tile print or export.
     */
    public boolean lastRow = false;

    /**
     * Indicates whether the currently handled tile is part of the last column, is updated after
     * each tile print or export.
     */
    public boolean lastColumn = false;


    /**
     * Constructor.<br>
     * {@link #diagramScale} is set equal to {@code 1d}, {@link #dotsPerInch} is set to
     * {@link Display#getDPI()} of the default Display.
     *
     * @param viewContext
     *            the {@link ViewContext} belonging to the diagram being exported
     * @param diagramBounds
     *            the bounds of the diagram area to be exported
     * @param tileBounds
     *            the bounds of the particular diagram tiles
     */
    public DiagramExportConfig(final ViewContext viewContext, final Rectangle2D diagramBounds,
            final Dimension tileBounds) {
        this(viewContext, diagramBounds, tileBounds, 1d, null, Trim.EMPTY_TRIM, 1);
    }

    /**
     * Constructor.
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
     * @param dotsPerInch
     *            the image resolution used by the employed drawing
     *            {@link org.eclipse.swt.graphics.Device Device}
     * @param deviceTrim
     *            the {@link Trim} imposed the used {@link org.eclipse.swt.printing.Printer Printer},
     *            if the corresponding export is a printout
     * @param pages
     *            the number of pages the diagram is to be printed, at least 1.
     */
    public DiagramExportConfig(final ViewContext viewContext, final Rectangle2D diagramBounds,
            final Dimension tileBounds, final double diagramScale, final Point dotsPerInch,
            final Trim deviceTrim, final int pages) {
        this.viewContext = viewContext;
        this.diagramBounds = diagramBounds;
        this.tileBounds = tileBounds;
        this.diagramScale = diagramScale;
        this.dotsPerInch = dotsPerInch;
        this.deviceTrim = deviceTrim;
        this.pages = pages;
    }

    /**
     * Copy constructor.
     *
     * @param original the {@link DiagramExportConfig} to take the values from
     */
    public DiagramExportConfig(final DiagramExportConfig original) {
        this.viewContext = original.viewContext;

        // also create copies of those records that can be changed, like rectangles, etc.
        this.diagramBounds = new Rectangle2D.Double();
        this.diagramBounds.setRect(original.diagramBounds);
        this.diagramScale = original.diagramScale;
        this.diagramTrim = original.diagramTrim;

        this.tileBounds = new Dimension(original.tileBounds);
        this.tileTrim = original.tileTrim;

        this.dotsPerInch = new Point(original.dotsPerInch);
        this.deviceTrim = original.deviceTrim;

        this.exportBrandings = ImmutableList.copyOf(original.exportBrandings);

        this.exportViewport = original.exportViewport;
        this.applyCameraZoomLevel = original.applyCameraZoomLevel;
        this.exportSemanticData = original.exportSemanticData;
        this.setTextLengths = original.setTextLengths;

        this.pages = original.pages;
        this.pageNo = original.pageNo;
        this.row = original.row;
        this.column = original.column;
        this.firstRow = original.firstRow;
        this.firstColumn = original.firstColumn;
        this.lastRow = original.lastRow;
        this.lastColumn = original.lastColumn;
    }

    /**
     * Provides a {@link Rectangle2D} describing the diagram's bounds including the
     * {@link #diagramTrim}.<br>
     * Implementation neglects position (x,y) of {@link #diagramBounds} since those don't matter for
     * the overall diagram size (they are neutralized before drawing the diagram).
     *
     * @return a {@link Rectangle2D} describing the diagram's bounds including the
     *         {@link #diagramTrim}
     */
    public Rectangle2D getDiagramBoundsIncludingTrim() {
        return new Rectangle2D.Double(diagramTrim.left, diagramTrim.top, diagramBounds.getWidth()
                + diagramTrim.getWidth(), diagramBounds.getHeight() + diagramTrim.getHeight());
    }

    /**
     * Configures the export of the current main diagram's visible area, by default the whole
     * diagram is exported.
     *
     * @param exportViewport
     *            {@code true} if just the view port shall be exported
     *
     * @return this {@link DiagramExportConfig} for convenience
     */
    public DiagramExportConfig setExportViewport(final boolean exportViewport) {
        this.exportViewport = exportViewport;

        return this;
    }

    /**
     * Instructs the exporter to rely on the diagram main camera's currently chosen zoom level while
     * determining the visibility of the particular diagram elements and diagram element figure
     * parts, by default the diagram zoom level of <code>1.0</code> is used. This is entirely
     * independent of the {@link #diagramScale} that is just about the scale of the exported image
     * (file). The latter can be increased to improve the image quality of raster images.
     *
     * @param applyCameraZoomLevel
     *            {@code true} if just the view port shall be exported
     *
     * @return this {@link DiagramExportConfig} for convenience
     */
    public DiagramExportConfig setApplyCameraZoomLevel(final boolean applyCameraZoomLevel) {
        this.applyCameraZoomLevel = applyCameraZoomLevel;

        return this;
    }

    /**
     * Instructs the exporter to add any semantic information attached to the view model elements by
     * means of {@link de.cau.cs.kieler.klighd.util.KlighdSemanticDiagramData
     * KlighdSemanticDiagramData} to the export, if supported by the exporter.
     *
     * @param exportSemanticData
     *            <code>true</code> if semantic data shall be added to the diagram while drawing,
     *            <code>false</code> otherwise.
     *
     * @return this {@link DiagramExportConfig} for convenience
     */
    public DiagramExportConfig setExportSemanticData(final boolean exportSemanticData) {
        this.exportSemanticData = exportSemanticData;

        return this;
    }

    /**
     * Instructs the exporter to add any semantic information attached to the view model elements by
     * means of {@link de.cau.cs.kieler.klighd.util.KlighdSemanticDiagramData
     * KlighdSemanticDiagramData} to the export, if supported by the exporter.
     *
     * @param setTextLength
     *            if the expected text length values shall be added to the diagram while exporting
     *            an SVG based image, <code>false</code> otherwise.
     *
     * @return this {@link DiagramExportConfig} for convenience
     */
    public DiagramExportConfig setTextLengths(final boolean setTextLengths) {
        this.setTextLengths = setTextLengths;

        return this;
    }

    /**
     * Combined setter of information concerning the application of {@link IExportBranding
     * IExportBrandings} and their cumulated diagram and tile trim requirements.
     *
     * @param exportBrandings
     *            the {@link IExportBranding IExportBranding} to be applied
     * @param diagramTrim
     *            the cumulated overall diagram trim required by the particular
     *            {@link IExportBranding IExportBranding}
     * @param tileTrim
     *            the cumulated diagram tile trim required by the particular
     *            {@link IExportBranding}s
     * @return this {@link DiagramExportConfig} for convenience
     */
    public DiagramExportConfig setBrandingsAndTrim(final Iterable<IExportBranding> exportBrandings,
            final Trim diagramTrim, final Trim tileTrim) {

        this.exportBrandings = exportBrandings != null
                ? exportBrandings : Collections.<IExportBranding>emptyList();
        this.diagramTrim = diagramTrim;
        this.tileTrim = tileTrim;

        return this;
    }

    /**
     * Combined setter of information concerning the page/tile numbering.<br>
     * {@link #firstRow} & {@link #firstColumn} are set if {@code rowNo} & {@code columnNo} are
     * equal to zero, {@link #lastRow} and {@link #lastColumn} are set {@code row == rows - 1} and
     * {@code column == columns - 1}, respectively.
     *
     * @param pageNo
     *            the current page number of the currently printed tile
     * @param row
     *            the row number of the currently printed tile (starting with zero)
     * @param column
     *            the column number of the currently printed tile (starting with zero)
     * @param rows
     *            number of tile rows
     * @param columns
     *            number of tile columns
     * @return this {@link DiagramExportConfig} for convenience
     */
    public DiagramExportConfig setPageAndTileNumbers(final int pageNo, final int row,
            final int column, final int rows, final int columns) {

        this.setPageAndTileNumbers(pageNo, row, column, row == rows - 1, column == columns - 1);

        return this;
    }

    /**
     * Combined setter of information concerning the page/tile numbering..<br>
     * {@link #firstRow} & {@link #firstColumn} are set if {@code rowNo} & {@code columnNo} are
     * equal to zero.
     *
     * @param pageNo
     *            the current page number of the currently printed tile
     * @param row
     *            the row number of the currently printed tile (starting with zero)
     * @param column
     *            the column number of the currently printed tile (starting with zero)
     * @param lastRow
     *            shall be {@code true} if the currently printed tile is part of the last row
     * @param lastColumn
     *            shall be {@code true} if the currently printed tile is part of the last column
     * @return this {@link DiagramExportConfig} for convenience
     */
    public DiagramExportConfig setPageAndTileNumbers(final int pageNo, final int row,
            final int column, final boolean lastRow, final boolean lastColumn) {

        this.pageNo = pageNo;
        this.row = row;
        this.column = column;

        this.firstRow = row == 0;
        this.firstColumn = column == 0;

        this.lastRow = lastRow;
        this.lastColumn = lastColumn;

        return this;
    }

    /**
     * Combined setter of information concerning the position of the tile to be printed within
     * the whole diagram.
     *
     * @param firstColumn
     *            {@code true} if and only if tile is located in the first column
     * @param firstRow
     *            {@code true} if and only if tile is located in the first row
     * @param lastColumn
     *            {@code true} if and only if tile is located in the last column
     * @param lastRow
     *            {@code true} if and only if tile is located in the last row
     *
     * @return this {@link DiagramExportConfig} for convenience
     */
    public DiagramExportConfig setTileInfo(final boolean firstColumn, final boolean firstRow,
            final boolean lastColumn, final boolean lastRow) {
        this.firstColumn = firstColumn;
        this.firstRow = firstRow;
        this.lastColumn = lastColumn;
        this.lastRow = lastRow;

        return this;
    }
}