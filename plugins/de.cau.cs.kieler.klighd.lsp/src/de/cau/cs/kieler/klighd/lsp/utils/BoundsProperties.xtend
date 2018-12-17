/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://rtsys.informatik.uni-kiel.de/kieler
 * 
 * Copyright ${year} by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 */
package de.cau.cs.kieler.klighd.lsp.utils

import de.cau.cs.kieler.klighd.krendering.KRendering
import de.cau.cs.kieler.klighd.krendering.KRenderingRef
import de.cau.cs.kieler.klighd.microlayout.Bounds
import de.cau.cs.kieler.klighd.piccolo.internal.util.PiccoloPlacementUtil.Decoration
import java.util.Map
import org.eclipse.elk.graph.properties.IProperty
import org.eclipse.elk.graph.properties.Property

/**
 * A collection of {@link IProperty IProperties} that may be used to persist
 * {@link Bounds} and {@link Decoration} data to {@link KRendering}s and {@link KRenderingRef}s.
 * 
 * @author stu114054
 */
class BoundsProperties {
    /**
     * Property holding the bounds of any {@link KRendering}. This property can be set if estimation
     * of rendering sizes should not be calculated in time when needed, but up front for all 
     * renderings.
     */
    public static final IProperty<Bounds> CALCULATED_BOUNDS = 
            new Property<Bounds>("klighd.calculated.bounds", null);
    
    /**
     * Property holding the decoration of any {@link KRendering}. This property can be set if estimation
     * of rendering sizes should not be calculated in time when needed, but up front for all 
     * renderings.
     * This only applies for renderings, that are decorations of edge renderings.
     */
    public static final IProperty<Decoration> CALCULATED_DECORATION =
            new Property<Decoration>("klighd.calculated.decoration", null);
    
    /**
     * Property holding the bounds for all child {@link KRendering}s in a {@link KRenderingRef}.
     * This property can be set if estimation of rendering sizes should not be calculated in time 
     * when needed, but up front for all renderings.
     */
    public static final IProperty<Map<String, Bounds>> CALCULATED_BOUNDS_MAP = 
            new Property<Map<String, Bounds>>("klighd.calculated.bounds.map", null);
    
    /**
     * Property holding the decorations for all child {@link KRendering}s in a {@link KRenderingRef}.
     * This only applies for renderings, that are decorations of edge renderings.
     * This property can be set if estimation of rendering sizes should not be calculated in time 
     * when needed, but up front for all renderings.
     */
    public static final IProperty<Map<String, Decoration>> CALCULATED_DECORATION_MAP = 
            new Property<Map<String, Decoration>>("klighd.calculated.decoration.map", null);
}