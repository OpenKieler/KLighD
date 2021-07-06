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
package de.cau.cs.kieler.klighd.eclipse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;

import de.cau.cs.kieler.klighd.ViewContext;

/**
 * Basic interface of diagram exporters creating images in <code>png</code> or </code>svg</code>
 * format, for instance.
 *
 * @author uru
 * @author chsch
 * @author csp
 *
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch
 *
 * @see IDiagramExporter#export(Control, ExportData)
 */
public interface IDiagramExporter {

    /**
     * Exports the diagram depicted by the given <code>control</code>.
     * 
     * @param control
     *            the control to export
     * @param data
     *            the specified export info
     *
     * @return {@link org.eclipse.core.runtime.Status#OK_STATUS Status#OK_STATUS} if the diagram
     *         export went successfully, an {@link IStatus} providing information on the failure
     *         otherwise.
     */
    IStatus export(Control control, ExportData data);

    /**
     * A data record encapsulating the information needed to export a diagram to the file
     * system.<br>
     *
     * @author csp
     */
    public class ExportData {

        private final ViewContext viewContext;
        private final String format;
        private final OutputStream stream;
        private final IPath path;
        private final boolean isWorkspacePath;
        private final boolean isCameraViewport;
        private final boolean applyCameraZoomLevel;
        private final int scale;
        private final boolean isTextAsShapes;
        private final boolean isEmbedFonts;
        private final boolean isSetTextLengths;
        private final String description;
        private final String css;
        private final String additionalRootData;
        private final TilingData tilingInfo;
        private final boolean transparentBackground;
        private final RGB backgroundColor;

        private ExportData(final ExportDataBuilder builder) {
            this.viewContext = builder.viewContext;
            this.format = builder.format;
            this.stream = builder.stream;
            this.path = builder.path;
            this.isWorkspacePath = builder.isWorkspacePath;
            this.isCameraViewport = builder.isCameraViewport;
            this.applyCameraZoomLevel = builder.applyCameraZoomLevel;
            this.scale = builder.scale;
            this.isTextAsShapes = builder.isTextAsShapes;
            this.isEmbedFonts = builder.isEmbedFonts;
            this.isSetTextLengths = builder.isSetTextLengths;
            this.description = builder.description;
            this.css = builder.css;
            this.additionalRootData = builder.additionalRootData;
            this.tilingInfo = builder.tilingInfo;
            this.transparentBackground = builder.transparentBackground;
            this.backgroundColor = builder.backgroundColor;
        }

        /**
         * @return The used {@code ViewContext}
         */
        public ViewContext viewContext() {
            return viewContext;
        }

        /**
         * @return A string representing the chosen export format
         */
        public String format() {
            return format;
        }

        /**
         * @return The path used for the export
         */
        public IPath path() {
            return path;
        }

        /**
         * @return Flag to indicate if the path is relative to the workspace
         */
        public boolean workspacePath() {
            return isWorkspacePath;
        }

        /**
         * @return Flag to indicate whether the current camera viewport is used
         */
        public boolean cameraViewport() {
            return isCameraViewport;
        }

        /**
         * @return Flag to indicate whether the diagram zoom level determined by the diagram main
         *         camera's view transform shall be used while evaluating the visibility of the
         *         particular diagram elements and diagram element figure parts, if
         *         <code>false</code> a diagram zoom level of <code>1.0<code> is assumed.
         */
        public boolean applyCameraZoomLevel() {
            return applyCameraZoomLevel;
        }

        /**
         * @return The scaling used in the export
         */
        public int scale() {
            return scale;
        }

        /**
         * @return Flag to export texts as shapes in SVG export
         */
        public boolean textAsShapes() {
            return isTextAsShapes;
        }

        /**
         * @return Flag to indicate whether fonts should be embedded in PDF export
         */
        public boolean embedFonts() {
            return isEmbedFonts;
        }

        /**
         * @return <code>true</code> if the expected text length values shall be added to the
         *         diagram while exporting an SVG based image, <code>false</code> otherwise.
         */
        public boolean setTextLengths() {
            return isSetTextLengths;
        }

        /**
         * @return The description to be placed in the exported data during SVG export
         */
        public String description() {
            return description;
        }

        /**
         * @return The css file to be referenced in SVG export
         */
        public String css() {
            return css;
        }

        /**
         * @return Additional data to be placed in the SVG tag during export
         */
        public String additionalRootData() {
            return additionalRootData;
        }

        /**
         * @return {@code TilingData} to control tiled bitmap export
         */
        public TilingData tilingInfo() {
            return tilingInfo;
        }

        /**
         * @return Flag whether the background should be drawn or left transparent
         */
        public boolean transparentBackground() {
            return transparentBackground;
        }

        /**
         * @return The {@code RGB} object to be used for background if not transparent
         */
        public RGB backgroundColor() {
            return backgroundColor;
        }

        /**
         * If an {@link OutputStream} has been configured, it is simply returned. Otherwise, a new
         * stream pointing to the given path is created.
         *
         * @return the {@link OutputStream} to write the image to.
         * @throws IOException
         *             if there is a problem obtaining an open output stream.
         */
        public OutputStream createOutputStream() throws IOException {
            if (stream != null) {
                return stream;
            } else {
                return createOutputStream(path);
            }
        }

        /**
         * If a path has been set, a new {@link OutputStream} is created. The name of the file is
         * appended by the given row and column.
         *
         * @param row
         *            number of current row
         * @param col
         *            number of current column
         * @return the {@link OutputStream} to write the image to.
         * @throws IOException
         *             if there is a problem obtaining an open output stream.
         * @throws IllegalArgumentException
         *             if no path has been set.
         */
        public OutputStream createOutputStream(final int row, final int col) throws IOException {
            if (path == null) {
                throw new IllegalStateException("Export not tileable.");
            }
            final String ext = path.getFileExtension();
            final String name = path.removeFileExtension().lastSegment() + "_" + row + "-" + col;
            final IPath aPath = path.removeLastSegments(1).append(name).addFileExtension(ext);
            return createOutputStream(aPath);
        }

        /**
         * Create a new output stream from a given path.
         * 
         * @param aPath
         *            The path to use for the output stream
         * @return The new output stream
         * @throws IOException
         *             If there is a problem obtaining an open output stream.
         */
        private OutputStream createOutputStream(final IPath aPath) throws IOException {
            if (isWorkspacePath) {
                // workspace path
                final URI fileURI = URI.createPlatformResourceURI(aPath.toOSString(), true);
                final OutputStream outputStream = URIConverter.INSTANCE.createOutputStream(fileURI);
                return outputStream;
            } else {
                // file system path
                final File file = new File(aPath.toString());
                final FileOutputStream outputStream = new FileOutputStream(file);
                return outputStream;
            }
        }
    }

    /**
     * Encapsulates the information needed to tile a diagram for export. Merely a record to hold the
     * information.
     *
     * @author csp
     */
    public static final class TilingData {

        /** Non tiled tiling information. */
        public static final TilingData NON_TILED = new TilingData();

        // SUPPRESS CHECKSTYLE NEXT 4 Visibility|Javadoc
        public final int maxWidth, maxHeight;
        public final int cols, rows;
        public final boolean isTiled;
        public final boolean isMaxsize;

        private TilingData(final int maxWidth, final int maxHeight, final int rows, final int cols,
                final boolean tiled, final boolean isMaxsize) {
            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;
            this.rows = rows;
            this.cols = cols;
            this.isTiled = tiled;
            this.isMaxsize = isMaxsize;
        }

        private TilingData() {
            this(-1, -1, 1, 1, false, false);
        }

        /**
         * @return non tiled information.
         */
        public static TilingData createNonTiledData() {
            return NON_TILED;
        }

        /**
         * Create tiled information with given numbers of rows and columns.
         *
         * @param rows
         *            number of rows.
         * @param cols
         *            number of columns.
         * @return the tiled information.
         */
        public static TilingData createTiledData(final int rows, final int cols) {
            return new TilingData(-1, -1, Math.max(1, rows), Math.max(1, cols),
                    rows > 1 || cols > 1, false);
        }

        /**
         * Create tiled information with given maximum size.
         *
         * @param maxWidth
         *            maximal width.
         * @param maxHeight
         *            maximal height.
         * @return the tiled information.
         */
        public static TilingData createMaxSizeTiledData(final int maxWidth, final int maxHeight) {
            return new TilingData(Math.max(1, maxWidth), Math.max(1, maxHeight), 1, 1, true, true);
        }
    }

    /**
     * Builder for {@code ExportData}.
     */
    public static class ExportDataBuilder {

        private final ViewContext viewContext;
        private final String format;
        private final OutputStream stream;
        private final IPath path;
        private final boolean isWorkspacePath;

        private boolean isCameraViewport = false;
        private boolean applyCameraZoomLevel = false;
        private int scale = 1;
        private boolean isTextAsShapes = false;
        private boolean isEmbedFonts = false;
        private boolean isSetTextLengths = false;
        private String description = null;
        private String css = null;
        private String additionalRootData = null;
        private TilingData tilingInfo = TilingData.createNonTiledData();
        private boolean transparentBackground = false;
        private RGB backgroundColor = EclipseKlighdConstants.WHITE;

        /**
         * Creates a builder for the given {@code ViewContext} using the specified
         * {@code OutputStream}. This constructor does not allow tiling the export.
         * 
         * @param viewContext
         *            The {@code ViewContext} that will be exported.
         * @param format
         *            The format to export in.
         * @param stream
         *            The {@code OutputStream} to write the export to.
         */
        public ExportDataBuilder(final ViewContext viewContext, final String format,
                final OutputStream stream) {
            this.viewContext = viewContext;
            this.format = format;
            this.stream = stream;
            this.path = null;
            this.isWorkspacePath = false;
        }

        /**
         * Creates a builder for the given {@code ViewContext} using the specified path. The path
         * can be absolute or relative to the workspace.
         * 
         * @param viewContext
         *            The {@code ViewContext} that will be exported.
         * @param format
         *            The format to export in.
         * @param path
         *            The path of the output file.
         * @param isWorkspacePath
         *            Flag to indicate the path is relative to the workspace.
         */
        public ExportDataBuilder(final ViewContext viewContext, final String format,
                final IPath path, final boolean isWorkspacePath) {
            this.viewContext = viewContext;
            this.format = format;
            this.stream = null;
            this.path = path;
            this.isWorkspacePath = isWorkspacePath;
        }

        /**
         * Configures the export to use the current camera viewport.
         * 
         * @param cameraViewport
         *            Flag whether the camera viewport should be used.
         * @return The current builder for comfortable usage
         */
        public ExportDataBuilder cameraViewport(final boolean cameraViewport) {
            this.isCameraViewport = cameraViewport;
            return this;
        }

        /**
         * Configures the export to use the diagram zoom level determined by the diagram main
         * camera's view transform while evaluating the visibility of the particular diagram
         * elements and diagram element figure parts, if <code>false</code> a diagram zoom level of
         * <code>1.0<code> is assumed. This is entirely independent of the {@link #scale(int)},
         * which is just about the scale of the exported image (file). The latter can be increased
         * to improve the image quality of raster images.
         * 
         * @param applyCameraZoomLevel
         *            the applyCameraZoomLevel to set
         */
        public ExportDataBuilder applyCameraZoomLevel(boolean applyCameraZoomLevel) {
            this.applyCameraZoomLevel = applyCameraZoomLevel;
            return this;
        }

        /**
         * Scaling factor to be applied during the diagram export. Just affects the image quality
         * for raster images, has no effect on the visibility of diagram elements or diagram figure
         * parts; see {@link #applyCameraZoomLevel(boolean)} for the latter.
         * 
         * @param theScale
         *            The scaling factor to be used.
         * @return The current builder for comfortable usage
         */
        public ExportDataBuilder scale(final int theScale) {
            this.scale = theScale;
            return this;
        }

        /**
         * Configures texts in SVG export to be embedded as shapes instead of texts.
         * 
         * @param textAsShapes
         *            Flag to indicate shapes
         * @return The current builder for comfortable usage
         */
        public ExportDataBuilder textAsShapes(final boolean textAsShapes) {
            this.isTextAsShapes = textAsShapes;
            return this;
        }

        /**
         * Configures the export to PDF to embed the used fonts in PDF.
         * 
         * @param embedFonts
         *            Whether fonts should be embedded
         * @return The current builder for comfortable usage
         */
        public ExportDataBuilder embedFonts(final boolean embedFonts) {
            this.isEmbedFonts = embedFonts;
            return this;
        }

        /**
         * Configures the export to PDF to embed the used fonts in PDF.
         * 
         * @param setTextLengths
         *            <code>true</code> if the expected text length values shall be added to the
         *            diagram while exporting an SVG based image, <code>false</code> otherwise.
         * @return The current builder for comfortable usage
         */
        public ExportDataBuilder setTextLengths(final boolean setTextLengths) {
            this.isSetTextLengths = setTextLengths;
            return this;
        }

        /**
         * Description to be placed in the exported file, e.g. during SVG export.
         * 
         * @param theDescription
         *            The image description
         * @return The current builder for comfortable usage
         */
        public ExportDataBuilder description(final String theDescription) {
            this.description = theDescription;
            return this;
        }

        /**
         * Path to a css file to be referenced in exported SVG.
         * 
         * @param theCss
         *            The path to the css file.
         * @return The current builder for comfortable usage
         */
        public ExportDataBuilder css(final String theCss) {
            this.css = theCss;
            return this;
        }

        /**
         * Data to be placed in the root SVG element. Can be used for i.e. namespace declarations.
         * 
         * @param theAdditionalRootData
         *            String to be placed in the root SVG tag.
         * @return The current builder for comfortable usage
         */
        public ExportDataBuilder additionalRootData(final String theAdditionalRootData) {
            this.additionalRootData = theAdditionalRootData;
            return this;
        }

        /**
         * Configuration for tiled bitmap export.
         * 
         * @param theTilingInfo
         *            The configuration to be used.
         * @return The current builder for comfortable usage
         */
        public ExportDataBuilder tilingInfo(final TilingData theTilingInfo) {
            if (path == null && !TilingData.NON_TILED.equals(theTilingInfo)) {
                // Tiling is only supported for exports using path specifications
                throw new IllegalStateException("Tiling not supported with stream export objects");
            }
            this.tilingInfo = theTilingInfo;
            return this;
        }

        /**
         * Flag to suppress drawing of the background and use transparent background instead.
         * 
         * @param theTransparentBackground
         *            {@code true} if the background should be transparent, {@code false} otherwise.
         * @return The current builder for comfortable usage
         */
        public ExportDataBuilder transparentBackground(final boolean theTransparentBackground) {
            this.transparentBackground = theTransparentBackground;
            return this;
        }

        /**
         * Configures the background color of the exported image, if not transparent.
         * 
         * @param theBackgroundColor
         *            {@code RGB} to be used as the background color.
         * @return The current builder for comfortable usage
         */
        public ExportDataBuilder backgroundColor(final RGB theBackgroundColor) {
            this.backgroundColor = theBackgroundColor;
            return this;
        }

        /**
         * Finalizes the build and creates the {@code ExportData}.
         * 
         * @return The finished {@code ExportData}.
         */
        public ExportData build() {
            // TODO validate?
            return new ExportData(this);
        }
    }
}
