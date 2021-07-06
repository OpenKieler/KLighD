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
package de.cau.cs.kieler.klighd.piccolo.export;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import de.cau.cs.kieler.klighd.DiagramExportConfig;
import de.cau.cs.kieler.klighd.IExportBranding;
import de.cau.cs.kieler.klighd.IExportBranding.Trim;
import de.cau.cs.kieler.klighd.Klighd;
import de.cau.cs.kieler.klighd.KlighdDataManager;
import de.cau.cs.kieler.klighd.eclipse.EclipseDiagramExportConfig;
import de.cau.cs.kieler.klighd.piccolo.KlighdPiccolo;
import de.cau.cs.kieler.klighd.piccolo.internal.KlighdCanvas;
import de.cau.cs.kieler.klighd.piccolo.internal.KlighdSWTGraphicsImpl;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KlighdMainCamera;

/**
 * An {@link de.cau.cs.kieler.klighd.eclipse.IDiagramExporter IDiagramExporter} contributing diagram
 * exports into the raster formats {@code .bmp}, {@code .jpeg}, and {@code .png}.
 *
 * @author chsch
 * @author csp
 */
public class BitmapExporter extends KlighdCanvasExporter {

    /** the bmp format. */
    public static final String SUB_FORMAT_BMP = "bmp";
    /** the jpeg format. */
    public static final String SUB_FORMAT_JPEG = "jpeg";
    /** the png format. */
    public static final String SUB_FORMAT_PNG = "png";

    private static final String ERROR_MSG_PREFIX = "KLighD bitmap export: ";

    /**
     * {@inheritDoc}
     */
    @Override
    public IStatus export(final KlighdCanvas canvas, final ExportData data) {

        // reveal the canvas' camera ...
        final KlighdMainCamera camera = canvas.getCamera();

        // ... and determine the bounds of the diagram to be exported
        final Rectangle2D bounds = getExportedBounds(camera, data.cameraViewport());

        final Iterable<IExportBranding> brandings = KlighdDataManager.getInstance()
                .getExportBrandingByFormat(data.format(), data.viewContext());

        final TilingData tilingInfo = data.tilingInfo();

        final Trim trim = getMaximumDiagramTrim(brandings, bounds);
        final Trim tileTrimScaled = getMaximumDiagramTileTrim(brandings);

        // determine the employed image's size
        final double width = data.scale() * (bounds.getWidth() + trim.getWidth());
        final double height = data.scale() * (bounds.getHeight() + trim.getHeight());

        // if export is tiled, compute resp. receive the needed number of rows and columns
        final int rows, columns;
        final int tileWidth, tileHeight;

        if (!tilingInfo.isTiled) {
            columns = 1;
            rows = 1;

            tileWidth = (int) Math.ceil(width + tileTrimScaled.getWidth());
            tileHeight = (int) Math.ceil(height + tileTrimScaled.getHeight());

        } else if (tilingInfo.isMaxsize) {
            columns = (int) Math.ceil(width / (tilingInfo.maxWidth - tileTrimScaled.getWidth()));
            rows = (int) Math.ceil(height / (tilingInfo.maxHeight - tileTrimScaled.getHeight()));

            tileWidth = (int) Math.ceil(width / columns + tileTrimScaled.getWidth());
            tileHeight = (int) Math.ceil(height / rows + tileTrimScaled.getHeight());

        } else {
            columns = tilingInfo.cols;
            rows = tilingInfo.rows;

            tileWidth = (int) Math.ceil(width / columns + tileTrimScaled.getWidth());
            tileHeight = (int) Math.ceil(height / rows + tileTrimScaled.getHeight());
        }

        final int pages = columns * rows;
        final Dimension tileBounds = new Dimension(tileWidth, tileHeight);
        final DiagramExportConfig exportConfig =
                new EclipseDiagramExportConfig(data.viewContext(), bounds, tileBounds, data.scale(), pages)
                .setBrandingsAndTrim(brandings, trim, tileTrimScaled)
                .setExportViewport(data.cameraViewport())
                .setApplyCameraZoomLevel(data.applyCameraZoomLevel());

        final Rectangle tileClip = getBasicTileClip(tileBounds, tileTrimScaled);

        int pageNo = 0;

        // for each row and columns draw and export the image
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {

                exportConfig.setPageAndTileNumbers(pageNo++, row, column, rows, columns);

                final IStatus res = exportTile(data, canvas, tileClip, exportConfig);

                // if any tile could not be create and saved stop here completely, as continuing
                //  is non-sense in case the file path is broken or write permissions are missing
                if (res != Status.OK_STATUS) {
                    return res;
                }
            }
        }

        return Status.OK_STATUS;
    }

    private IStatus exportTile(final ExportData data, final KlighdCanvas canvas,
            final Rectangle tileClip, final DiagramExportConfig exportConfig) {

        final Dimension tileBounds = exportConfig.tileBounds;

        // initialize an SWT Image that serves as the pixel 'canvas'
        //  since any potential scaling is (here) considered as diagram scaling, not tile scaling,
        //  the employed images will have the size of 'tilesBounds',
        //  and this size is used while calling #drawDiagramTile(...) below
        final Image image;
        try {
            image = new Image(canvas.getDisplay(), tileBounds.width, tileBounds.height);

        } catch (final OutOfMemoryError e) {
            System.gc();

            final String msg = ERROR_MSG_PREFIX + "Out of heap space memory!";
            // a more detailed message is be provided by the UI integration
            //  (SaveAsImageHandler in de.cau.cs.kieler.klighd.ui)
            return new Status(IStatus.ERROR, KlighdPiccolo.PLUGIN_ID, msg, e);

        } catch (final SWTError e) {
            final String hint = Klighd.LINE_SEPARATOR
                    + "This may be due to a too large image. Try a tiled export with smaller tiles.";

            final String msg = ERROR_MSG_PREFIX + "Export failed." + (
                    !Klighd.IS_WINDOWS && e.code == SWT.ERROR_NO_HANDLES ? "" : hint);

            final String msg2 = "Got an SWT Error while allocating the image buffer. "
                    + "Error code is" + e.code + ":";

            final IStatus[] detailStatus = new IStatus[] {
                    new Status(IStatus.ERROR, KlighdPiccolo.PLUGIN_ID, msg2, e)
            };

            // the multiStatus improves the error pop-up dialog: the detailStatus information
            //  will be hidden until the user hits the details button
            return new MultiStatus(KlighdPiccolo.PLUGIN_ID, 0, detailStatus, msg, null);
        }

        // initialize a GC and graphics object that 'collects' all the drawing instructions
        final GC gc = new GC(image);
        final KlighdSWTGraphicsImpl graphics = new KlighdSWTGraphicsImpl(gc);

        // now draw the diagram
        //  see comment above on the assignment of 'drawablesBounds' (4th parameter)
        drawDiagramTile(exportConfig, graphics, canvas.getCamera(), tileBounds, tileClip);

        // release the instruction recipients
        graphics.dispose();
        gc.dispose();

        // create an image loader to save the image
        // although the API differently suggests:
        //  the ImageData array below must contain exactly 1 element,
        //  see the implementations of FileFormat.unloadIntoByteStream(ImageLoader)
        final ImageLoader loader = new ImageLoader();
        
        try {
            loader.data = new ImageData[] { image.getImageData() };

        } catch (OutOfMemoryError e) {
            System.gc();

            final String msg = ERROR_MSG_PREFIX + "Out of heap space memory!";
            // a more detailed message is be provided by the UI integration
            //  (SaveAsImageHandler in de.cau.cs.kieler.klighd.ui)
            return new Status(IStatus.ERROR, KlighdPiccolo.PLUGIN_ID, msg, e);

        } finally {
            image.dispose();
        }

        // translate the requested format identifier
        final int format;
        if (data.format().equals(SUB_FORMAT_JPEG)) {
            format = SWT.IMAGE_JPEG;
        } else if (data.format().equals(SUB_FORMAT_PNG)) {
            format = SWT.IMAGE_PNG;
        } else {
            // default format is bmp
            format = SWT.IMAGE_BMP;
        }

        // dump out the binary image data via the provided output stream
        OutputStream stream = null;
        IStatus status;

        try {
            if (data.tilingInfo().isTiled) {
                stream = data.createOutputStream(exportConfig.row, exportConfig.column);
            } else {
                stream = data.createOutputStream();
            }
            loader.save(stream, format);
            stream.close();
            status = Status.OK_STATUS;

        } catch (final SWTError e) {
            String msg = ERROR_MSG_PREFIX + "Failed to write bitmap data";
            if (stream != null) {
                msg += " into the provided OutputStream of type "
                        + stream.getClass().getCanonicalName()
                        + Klighd.LINE_SEPARATOR + " the stream instance is "
                        + stream.toString();
            }
            status = new Status(IStatus.ERROR, KlighdPiccolo.PLUGIN_ID, msg, e);

        } catch (final IOException e) {
            final String msg;
            if (stream == null) {
                msg = ERROR_MSG_PREFIX + "Failed to create output stream to write the image to.";
            } else {
                msg = ERROR_MSG_PREFIX + "Closing the employed output stream failed.";
            }
            status = new Status(IStatus.ERROR, KlighdPiccolo.PLUGIN_ID, msg, e);

        } catch (final Throwable t) {
            status = new Status(IStatus.ERROR, KlighdPiccolo.PLUGIN_ID, t.getMessage(), t);
        }

        return status;
    }
}
