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

import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.elk.graph.properties.IProperty;
import org.eclipse.elk.graph.properties.IPropertyHolder;
import org.eclipse.elk.graph.properties.Property;

/**
 * Implementations of this interface are able to render diagram formats like PNG or SVG without
 * showing them on the screen.
 *
 * @author chsch
 *
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch
 */
public interface IOffscreenRenderer {

    /** Export format id denoting the Bitmap (.bmp) format. */
    String BMP = "bmp";

    /** Export format id denoting the JPEG compressed raster image (.jpeg) format. */
    String JPEG = "jpeg";

    /** Export format id denoting the PNG compressed raster image (.png) format. */
    String PNG = "png";

    /** Export format id denoting the Scalable Vector Graphics (.svg) format. */
    String SVG = "svg";

    /**
     * Property definition for declaring the desired rendering format.<br>
     * Its configuration is only required if multiple formats are supported by a concrete
     * {@link IOffscreenRenderer}.
     */
    IProperty<String> OUTPUT_FORMAT = new Property<String>(
            "de.cau.cs.kieler.klighd.offscreenRendering.outputFormat");

    /**
     * Property definition for suppressing the automatic layout run.
     */
    IProperty<Boolean> NO_LAYOUT = new Property<Boolean>(
            "de.cau.cs.kieler.klighd.offscreenRendering.noLayout", false);

    /**
     * Property definition for declaring elements whose diagram representation node shall be expanded.
     */
    IProperty<List<?>> EXPANDED_ELEMENTS = new Property<List<?>>(
            "de.cau.cs.kieler.klighd.offscreenRendering.expandedElements", Collections.emptyList());

    /**
     * Property definition for declaring elements whose diagram representation node shall be collapsed.
     */
    IProperty<List<?>> COLLAPSED_ELEMENTS = new Property<List<?>>(
            "de.cau.cs.kieler.klighd.offscreenRendering.collapsedElements", Collections.emptyList());

    /**
     * Property definition for instructing bitmap image exporters to render to apply the given scale
     * factor to the diagram before rendering it into an image.
     */
    IProperty<Integer> IMAGE_SCALE = new Property<Integer>(
            "de.cau.cs.kieler.klighd.offscreenRendering.imageScale", 1);

    /** Property definition for defining transparent diagram background. */
    IProperty<Boolean> TRANSPARENT_BACKGROUND = new Property<Boolean>(
            "de.cau.cs.kieler.klighd.offscreenRendering.transparentBackground", false);

    /**
     * Property definition for instructing vector image exporters to render text strings as polyline
     * shapes.
     */
    IProperty<Boolean> TEXT_AS_SHAPES = new Property<Boolean>(
            "de.cau.cs.kieler.klighd.offscreenRendering.textAsShapes", false);

    /**
     * Property definition for instructing vector image exporters to embed used fonts into the ouput.
     */
    IProperty<Boolean> EMBED_FONTS = new Property<Boolean>(
            "de.cau.cs.kieler.klighd.offscreenRendering.embedFonts", false);

    /**
     * Property definition for instructing vector image exporters to set the {@code textLength}
     * properties of text elements to the expected values. May reduce rendering mismatches among
     * browsers and compared to SWT's pixel drawings.
     */
    IProperty<Boolean> SET_TEXT_LENGTHS = new Property<Boolean>(
            "de.cau.cs.kieler.klighd.offscreenRendering.setTextLengths", false);
    
    /**
     * Property definition for embedding a predefined description in the output.
     */
    IProperty<String> DESCRIPTION = new Property<String>(
            "de.cau.cs.kieler.klighd.offscreenRendering.description", null);

    /**
     * Property definition for embedding css stylesheet in the svg.
     */
    IProperty<String> CSS = new Property<String>(
            "de.cau.cs.kieler.klighd.offscreenRendering.css", null);

    /**
     * Property definition for embedding user-defined data in the root svg tag.
     */
    IProperty<String> ADDITIONAL_ROOT_DATA = new Property<String>(
            "de.cau.cs.kieler.klighd.offscreenRendering.additionalRootData", null);

    /**
     * Renders the provided <code>viewModel</code> into the (standard) diagram format supported by
     * <code>this</code> {@link IOffscreenRenderer} and returns a {@link String}-based
     * representation.<br>
     * <br>
     * If multiple formats are support by this renderer use {@link #OUTPUT_FORMAT} to configure the
     * desired format.
     *
     * @param viewContext
     *            the view context describing the diagram to be rendered
     * @param output
     *            the OutputStream to write the rendered diagram to, e.g. a
     *            {@link java.io.FileOutputStream FileOutputStream}
     * @param properties
     *            an {@link IPropertyHolder}, e.g. a
     *            {@link de.cau.cs.kieler.klighd.util.KlighdSynthesisProperties
     *            KlighdSynthesisProperties} instance, declaring the {@link #OUTPUT_FORMAT}, for
     *            example.
     * @return an {@link IStatus} indicating success or failure of the diagram rendering, in case of
     *         a failure the return {@link IStatus} is supposed to contain detailed information
     */
    IStatus render(ViewContext viewContext, OutputStream output, IPropertyHolder properties);
}
