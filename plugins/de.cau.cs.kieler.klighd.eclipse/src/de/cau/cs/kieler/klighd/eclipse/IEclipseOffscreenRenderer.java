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

import org.eclipse.elk.graph.properties.IProperty;
import org.eclipse.elk.graph.properties.Property;
import org.eclipse.swt.graphics.RGB;

import de.cau.cs.kieler.klighd.IOffscreenRenderer;

/**
 * Implementations of this interface are able to render diagram formats like PNG or SVG without
 * showing them on the screen.
 *
 * @author chsch
 *
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch
 */
public interface IEclipseOffscreenRenderer extends IOffscreenRenderer {

    /** Property definition for defining the diagram background color. */
    IProperty<RGB> BACKGROUND_COLOR = new Property<RGB>(
            "de.cau.cs.kieler.klighd.offscreenRendering.backgroundColor", EclipseKlighdConstants.WHITE);

}
