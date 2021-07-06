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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.eclipse.elk.core.util.Pair;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import de.cau.cs.kieler.klighd.DiagramExportConfig;
import de.cau.cs.kieler.klighd.IExportBranding;
import de.cau.cs.kieler.klighd.IExportBranding.Trim;
import de.cau.cs.kieler.klighd.piccolo.KlighdSWTGraphics;
import de.cau.cs.kieler.klighd.piccolo.internal.KlighdSWTGraphicsEx;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KlighdMainCamera;
import de.cau.cs.kieler.klighd.piccolo.internal.util.KlighdPaintContext;
import de.cau.cs.kieler.klighd.util.Iterables2;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * Abstract diagram exporter providing the common methods
 * {@link #getExportedBounds(KlighdMainCamera, boolean)} and
 * {@link #drawDiagram(KlighdMainCamera, boolean, KlighdSWTGraphics, PBounds)} to be re-used in
 * concrete implementation of {@link de.cau.cs.kieler.klighd.eclipse.IDiagramExporter IDiagramExporter} and
 * {@link de.cau.cs.kieler.klighd.IOffscreenRenderer IOffscreenRenderer}, in order to achieve
 * consistent behavior amongst all those implementations.
 *
 * @author chsch
 * @author csp
 */
public abstract class AbstractDiagramExporter {

    /**
     * Singleton {@link AffineTransform} instance denoting the identical transform. Note:
     * Application code must not manipulate it, overriding all the setters and throwing exceptions
     * is just to lengthy for all those operations...
     */
    protected static final AffineTransform IDENTITY = new AffineTransform();


    /**
     * Factory method hook for injecting a {@link KlighdPaintContext} tailored to the type of
     * diagram rendering/export.<br>
     * <br>
     * May be overriden by concrete implementations, base implementation returns a
     * {@link KlighdPaintContext} tailored to diagram image exports.
     *
     * @param graphics
     *            the {@link KlighdSWTGraphics} to draw on
     * @param exportConfig
     *            an {@link DiagramExportConfig} providing more details on the requested export,
     *            must not be <code>null</code>
     * @return a {@link KlighdPaintContext} tailored to the type of diagram rendering/export.
     */
    protected KlighdPaintContext createPaintContext(final KlighdSWTGraphics graphics,
            final DiagramExportConfig exportConfig) {
        return KlighdPaintContext.createExportDiagramPaintContext(graphics,
                exportConfig.applyCameraZoomLevel, exportConfig.exportSemanticData,
                exportConfig.setTextLengths);
    }


    /**
     * Helper method computing the actual unadjusted bounds of the content of the diagram to be
     * exported.
     *
     * @param camera
     *            the {@link KlighdMainCamera} whose depicted content is to be exported
     * @param exportViewport
     *            if <code>true</code> the camera's view port is considered,
     *            otherwise the closure bounds of the camera's displayed layer are returned
     * @return a new {@link PBounds} instance containing the requested bounds data
     */
    protected final PBounds getExportedBounds(final KlighdMainCamera camera,
            final boolean exportViewport) {
        final PBounds bounds;

        if (exportViewport) {
            bounds = camera.getBounds();
        } else {
            bounds = new PBounds(camera.getDisplayedKNodeNode().getExportedBounds());
        }

        return bounds;
    }


    /**
     * Calculates the cumulated (maximal) diagram {@link Trim} that is defined to require for each
     * side the maximum of those trims required by the given {@code exportBrandings} on that
     * particular side.
     *
     * @param exportBrandings
     *            the {@link IExportBranding IExportBrandings} to be applied
     * @param bounds
     *            the printed diagram's (or diagram clip's) bounds
     * @return the desired cumulated {@link Trim}
     */
    protected final Trim getMaximumDiagramTrim(
            final Iterable<IExportBranding> exportBrandings, final Rectangle2D bounds) {
        return getCumulatedTrim(false, exportBrandings, bounds, null, null);
    }

    /**
     * Calculates the cumulated (maximal) diagram tile {@link Trim} that is defined to require for
     * each side the maximum of those trims required by the given {@code exportBrandings} on that
     * particular side.
     *
     * @param exportBrandings
     *            the {@link IExportBranding IExportBrandings} to be applied
     * @return the desired cumulated {@link Trim}
     */
    protected final Trim getMaximumDiagramTileTrim(final Iterable<IExportBranding> exportBrandings) {
        return getMaximumDiagramTileTrim(exportBrandings, null, null, null);
    }

    /**
     * Calculates the cumulated (maximal) diagram tile {@link Trim} that is defined to require for
     * each side the maximum of those trims required by the given {@code exportBrandings} on that
     * particular side.
     *
     * @param exportBrandings
     *            the {@link IExportBranding IExportBrandings} to be applied
     * @param bounds
     *            depending on {@code fixSizedTiles} the absolute size of either the diagram tile
     *            itself, or the diagram excerpt drawn on the tile respectively
     * @param deviceTrim
     *            in case the diagram is printed this object represents the printers technically
     *            required trim, which can be incorporated, e.g., for facilitating margins of
     *            exactly a particular value, is <code>null</code> otherwise
     * @param dotsPerInch
     *            the image resolution applied by the employed drawing
     *            {@link org.eclipse.swt.graphics.Device Device}, maybe <code>null</code> if not valid
     * @return the desired cumulated {@link Trim}
     */
    protected final Trim getMaximumDiagramTileTrim(final Iterable<IExportBranding> exportBrandings,
            final Rectangle2D bounds, final Trim deviceTrim, final Point dotsPerInch) {
        return getCumulatedTrim(true, exportBrandings, bounds, deviceTrim, dotsPerInch);
    }

    private Trim getCumulatedTrim(final boolean tileTrim, final Iterable<IExportBranding> brandings,
            final Rectangle2D bounds, final Trim deviceTrim, final Point dotsPerInch) {

        final Trim res = Iterables2.fold(brandings, new Function<Pair<Trim, IExportBranding>, Trim>() {

            public Trim apply(final Pair<Trim, IExportBranding> input) {
                final Trim previousResult = input.getFirst();
                final IExportBranding branding = input.getSecond();
                final Trim trim;

                if (tileTrim) {
                    trim = branding.getDiagramTileTrimm(bounds, dotsPerInch, deviceTrim);
                } else {
                    trim = branding.getDiagramTrim(bounds);
                }

                if (previousResult == null) {
                    return trim;

                } else if (trim == null) {
                    return previousResult;

                } else {
                    final float maxLeft = Math.max(previousResult.left, trim.left);
                    final float maxRight = Math.max(previousResult.right, trim.right);
                    final float maxTop = Math.max(previousResult.top, trim.top);
                    final float maxBottom = Math.max(previousResult.bottom, trim.bottom);

                    if (maxLeft != previousResult.left || maxRight != previousResult.right
                            || maxTop != previousResult.top || maxBottom != previousResult.bottom) {
                        return new Trim(maxLeft, maxRight, maxTop, maxBottom);
                    } else {
                        return previousResult;
                    }
                }
            }
        });

        return res != null ? res : new Trim(0, 0, 0, 0);
    }


    /**
     * Provides a {@link Rectangle} describing the basic clip of diagram tiles based on the actual
     * bounds of the employed {@link org.eclipse.swt.graphics.Drawable Drawable} (
     * {@link org.eclipse.swt.graphics.Image Image} or {@link org.eclipse.swt.graphics.Printer
     * Printer} and the cumulated required tile {@link Trim} being scaled to the size of the
     * employed drawable.
     *
     * @param drawablesBounds
     *            the actual bounds of the employed {@link org.eclipse.swt.graphics.Drawable
     *            Drawable}
     * @param tileTrimScaled
     *            the cumulated required {@link Trim} scaled to {@code drawablesBounds} if necessary
     * @return a {@link Rectangle} describing the unadjusted clip rectangle that is to be applied to
     *         the employed {@link KlighdSWTGraphics} without having configured any
     *         {@link AffineTransform transform} on that graphics
     */
    protected final Rectangle getBasicTileClip(final Dimension drawablesBounds,
            final Trim tileTrimScaled) {
        final Rectangle2D clip = new Rectangle2D.Double(
                Math.ceil(tileTrimScaled.left),
                Math.ceil(tileTrimScaled.top),
                Math.floor(drawablesBounds.width - tileTrimScaled.getWidth()),
                Math.floor(drawablesBounds.height - tileTrimScaled.getHeight()));
        return clip.getBounds();
    }


    /**
     *
     * @param exportConfig
     *            an {@link DiagramExportConfig} record comprising all information required for
     *            proper export, must not be {@code null}
     * @param graphics
     *            the {@link KlighdSWTGraphicsEx} to 'draw' the diagram on
     * @param camera
     *            the {@link KlighdMainCamera} showing the diagram to be exported
     * @param drawablesBounds
     *            the actual (unadjusted) pure bounds, which may differ from
     *            {@code exportConfig.tileBounds} in case the tiles are scaled (e.g. for print previews)
     * @param baseTileClip
     *            the basic tile clip as provided by {@link #getBasicTileClip(Dimension, Trim)}, can
     *            be provided here in order to avoid re-computation (of the same values) for each
     *            diagram tile, maybe {@code null}
     */
    protected final void drawDiagramTile(final DiagramExportConfig exportConfig,
            final KlighdSWTGraphicsEx graphics, final KlighdMainCamera camera,
            final Dimension drawablesBounds, final Rectangle baseTileClip) {
        drawDiagramTile(exportConfig, graphics, camera, drawablesBounds, baseTileClip, 1d, null);
    }

    /**
     * Performs the diagram rendering work by means of the employed {@link KlighdSWTGraphicsEx}.<br>
     * <br>
     * This method is supposed to be used by all registered
     * {@link de.cau.cs.kieler.klighd.eclipse.IDiagramExporter IDiagramExporters} in order to achieve
     * consistent exporting behavior.
     *
     * @param exportConfig
     *            an {@link DiagramExportConfig} record comprising all information required for
     *            proper export, must not be {@code null}
     * @param graphics
     *            the {@link KlighdSWTGraphicsEx} to 'draw' the diagram on
     * @param camera
     *            the {@link KlighdMainCamera} showing the diagram to be exported
     * @param drawablesBounds
     *            the actual (unadjusted) pure bounds, which may differ from
     *            {@code exportConfig.tileBounds} in case the tiles are scaled (e.g. for print previews)
     * @param baseTileClip
     *            the basic tile clip as provided by {@link #getBasicTileClip(Dimension, Trim)}, can
     *            be provided here in order to avoid re-computation (of the same values) for each
     *            diagram tile, maybe {@code null}
     * @param tileScale
     *            the tile scale being required for creating preview images, should be {@code 1d}
     *            for eventual exports
     * @param centeringOffset
     *            a (n non-scaled) {@link Point2D} describing the translation for centrally aligning
     *            the diagram
     */
    protected final void drawDiagramTile(final DiagramExportConfig exportConfig,
            final KlighdSWTGraphicsEx graphics, final KlighdMainCamera camera,
            final Dimension drawablesBounds, final Rectangle baseTileClip,
            final double tileScale, final Point2D centeringOffset) {

        final Trim tileTrimScaled = exportConfig.tileTrim.getScaled((float) tileScale);

        // initial clip definition
        final Rectangle tileClip = baseTileClip != null
                ? baseTileClip : getBasicTileClip(drawablesBounds, tileTrimScaled);
        graphics.setClip(tileClip);

        // apply translation for tiled export if necessary
        final AffineTransform tileTransform = AffineTransform.getTranslateInstance(
                -exportConfig.column * drawablesBounds.getWidth(),
                -exportConfig.row * drawablesBounds.getHeight());

        tileTransform.translate(
                (exportConfig.column + 1) * tileTrimScaled.left
                        + exportConfig.column * tileTrimScaled.right,
                (exportConfig.row + 1) * tileTrimScaled.top
                        + exportConfig.row * tileTrimScaled.bottom);

        if (centeringOffset != null) {
            tileTransform.translate(centeringOffset.getX(), centeringOffset.getY());
        }

        final AffineTransform tileScaling = AffineTransform.getScaleInstance(tileScale, tileScale);
        tileTransform.concatenate(tileScaling);

        drawDiagram(exportConfig, graphics, camera, tileTransform, tileScaling, tileClip);
    }


    /**
     * Performs the actual diagram rendering work by means of the employed
     * {@link KlighdSWTGraphicsEx}.<br>
     * <br>
     * This method is supposed to be used by all registered
     * {@link de.cau.cs.kieler.klighd.eclipse.IDiagramExporter IDiagramExporters} in order to achieve
     * consistent exporting behavior.
     * 
     * @param exportConfig
     *            an {@link DiagramExportConfig} record comprising all information required for
     *            proper export, must not be {@code null}
     * @param graphics
     *            the {@link KlighdSWTGraphicsEx} to 'draw' the diagram on
     * @param camera
     *            the {@link KlighdMainCamera} showing the diagram to be exported
     * @param diagramTransform
     *            the {@link AffineTransform} to be applied before drawing the diagram (tile), must
     *            include the translate for obtained the correct tile, the translation caused by the
     *            tile trim, an optional centering offset, and the {@code tileBaseTransform}
     * @param tileBaseTransform
     *            the basic transform of a the current diagram tile/page, is only required for
     *            properly drawing tile background and, more importantly, drawing tile overlays;
     *            must contain the tile scaling if required (e.g. for print previews), should be
     *            {@link #IDENTITY} in the default case
     * @param tileClip
     *            a {@link Rectangle} describing the basic unadjusted (except for tile scaling) tile
     *            clip to prevent the diagram being drawn into the tile trim
     */
    protected final void drawDiagram(final DiagramExportConfig exportConfig,
            final KlighdSWTGraphicsEx graphics, final KlighdMainCamera camera,
            final AffineTransform diagramTransform, final AffineTransform tileBaseTransform,
            final Rectangle tileClip) {

        final Rectangle2D theBounds;

        if (exportConfig.diagramBounds != null) {
            theBounds = exportConfig.diagramBounds;
        } else {
            theBounds = getExportedBounds(camera, exportConfig.exportViewport);
        }

        final boolean brandingsAvailable = !Iterables.isEmpty(exportConfig.exportBrandings);

        // create a copy of the provided diagram exportConfig in order to prevent
        //  vulnerability because of changes in the exportConfig by the export exportBrandings
        final DiagramExportConfig configCopy =
                brandingsAvailable ? new DiagramExportConfig(exportConfig) : null;

        // create a rectangle describing the bounds of the current diagram tile required for
        //  reseting the clip before drawing the tile background and overlay,
        //  'exportConfig' provides only a Dimension of (width|height))
        final Rectangle tileBounds =
                brandingsAvailable ? new Rectangle(exportConfig.tileBounds) : null;

        if (brandingsAvailable) {
            graphics.setTransform(tileBaseTransform);

            // reset the clip to the tile/page bounds (which are not necessary the actual bounds
            //  of the underlying image in case this exporter is drawing on an image,
            //  the required scaling must be part of 'tileBaseTransform' in that case)
            graphics.setClip(tileBounds);

            // instruct the graphics to stop using the cached (SWT) fonts and create new fonts in
            //  context of the configured (font creation specific) device, e.g. the configured printer
            graphics.stopFontCaching();

            // perform the tile background drawing (1.)
            for (final IExportBranding branding : exportConfig.exportBrandings) {
                branding.drawDiagramTileBackground(graphics, configCopy);
                graphics.setTransform(tileBaseTransform);
            }

            // graphics.resumeFontCaching() is not missing here, it's called below!

            // reset the transform to a neutral one and restore the tile clip for drawing the
            //  drawing with respecting the tile trim properly
            graphics.setTransform(IDENTITY);
            graphics.setClip(tileClip);
        }

        // apply the diagram scale factor to the diagram transform ...
        diagramTransform.scale(exportConfig.diagramScale, exportConfig.diagramScale);

        // ... and apply the resulting transform
        graphics.setTransform(diagramTransform);

        if (brandingsAvailable) {

            // perform the overall diagram background drawing (2.)
            for (final IExportBranding branding : exportConfig.exportBrandings) {
                branding.drawDiagramBackground(graphics, configCopy);

                // Restore the transform in case 'branding' changed something.
                graphics.setTransform(diagramTransform);
            }

            // instruct the graphics to resume using the cached (display-based fonts)
            //  and dispose those (SWT) fonts created in the meantime
            graphics.resumeFontCaching();
        }

        final Trim diagramTrim = exportConfig.diagramTrim;

        // apply the translation required by the diagram trim,
        //  it will also be scaled by exportConfig.diagramScale since that is already part of
        //  'diagramTransform' and, hence, already applied
        graphics.transform(
                AffineTransform.getTranslateInstance(diagramTrim.left, diagramTrim.top));

        // adjust the zero reference point corresponding to the exported bounds' reference point
        graphics.transform(
                AffineTransform.getTranslateInstance(-theBounds.getX(), -theBounds.getY()));

        // create the required paint context containing, e.g., information whether a printout or
        //  an image export is to be done
        final KlighdPaintContext paintContext = createPaintContext(graphics, exportConfig);

        final PLayer exportedLayer = camera.getDisplayedKNodeNode();

        // perform the diagram drawing (3.)
        if (exportConfig.exportViewport) {
            // only render the current viewport
            camera.fullPaint(paintContext);

        } else if (exportedLayer != null) {
            paintContext.pushCamera(camera);
            paintContext.pushTransform(exportedLayer.getInverseTransform());

            exportedLayer.fullPaint(paintContext);

        } else {
            // the fallback case, should not happen in context of KLighD
            @SuppressWarnings("unchecked") // Piccolo2D classes do not use type parameters :-(
            final Iterable<PLayer> layersReference = camera.getLayersReference();
            for (final PLayer layer : layersReference) {
                layer.fullPaint(paintContext);
            }
        }

        if (brandingsAvailable) {

            // instruct the graphics to stop using the cached (SWT) fonts and create new fonts in
            //  context of the configured (font creation specific) device, e.g. the configured printer
            graphics.stopFontCaching();

            // perform the overall diagram overly drawing (4.)
            for (final IExportBranding branding : exportConfig.exportBrandings) {
                // reset the zero point, or restore the transform in case previous 'branding'
                //  changed something, respectively
                graphics.setTransform(diagramTransform);
                branding.drawDiagramOverlay(graphics, configCopy);
            }

            // perform the tile background drawing (5.)
            graphics.setTransform(tileBaseTransform);
            graphics.setClip(tileBounds);

            for (final IExportBranding branding : exportConfig.exportBrandings) {
                branding.drawDiagramTileOverlay(graphics, configCopy);

                // Reset the transform in case 'branding' changed something.
                graphics.setTransform(tileBaseTransform);
            }

            // instruct the graphics to resume using the cached (display-based fonts)
            //  and dispose those (SWT) fonts created in the meantime
            graphics.resumeFontCaching();
        }
    }
}
