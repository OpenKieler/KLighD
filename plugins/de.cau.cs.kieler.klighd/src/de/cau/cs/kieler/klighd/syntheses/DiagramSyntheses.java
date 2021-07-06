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
package de.cau.cs.kieler.klighd.syntheses;

import java.util.List;
import java.util.Map;

import org.eclipse.elk.core.data.LayoutMetaDataService;
import org.eclipse.elk.core.data.LayoutOptionData;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.util.Pair;
import org.eclipse.elk.graph.properties.IProperty;

import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.kgraph.KGraphElement;
import de.cau.cs.kieler.klighd.kgraph.KNode;
import de.cau.cs.kieler.klighd.kgraph.KPort;
import de.cau.cs.kieler.klighd.krendering.KAreaPlacementData;
import de.cau.cs.kieler.klighd.krendering.KBackground;
import de.cau.cs.kieler.klighd.krendering.KBottomPosition;
import de.cau.cs.kieler.klighd.krendering.KContainerRendering;
import de.cau.cs.kieler.klighd.krendering.KDecoratorPlacementData;
import de.cau.cs.kieler.klighd.krendering.KInvisibility;
import de.cau.cs.kieler.klighd.krendering.KLeftPosition;
import de.cau.cs.kieler.klighd.krendering.KLineStyle;
import de.cau.cs.kieler.klighd.krendering.KLineWidth;
import de.cau.cs.kieler.klighd.krendering.KPosition;
import de.cau.cs.kieler.klighd.krendering.KRectangle;
import de.cau.cs.kieler.klighd.krendering.KRendering;
import de.cau.cs.kieler.klighd.krendering.KRenderingFactory;
import de.cau.cs.kieler.klighd.krendering.KRightPosition;
import de.cau.cs.kieler.klighd.krendering.KRoundedRectangle;
import de.cau.cs.kieler.klighd.krendering.KText;
import de.cau.cs.kieler.klighd.krendering.KTopPosition;
import de.cau.cs.kieler.klighd.krendering.LineStyle;
import de.cau.cs.kieler.klighd.util.ExpansionAwareLayoutOption;
import de.cau.cs.kieler.klighd.util.KlighdProperties;
import de.cau.cs.kieler.klighd.util.KlighdSemanticDiagramData;

/**
 * Collection of KGraph/KRendering view model configuration methods.
 *
 * @author chsch
 *
 * @kieler.design proposed by chsch
 */
public final class DiagramSyntheses {

    /**
     * Hidden standard constructor.
     */
    private DiagramSyntheses() {
    }

    /**
     * Convenience method for defining layout options for {@link KGraphElement KGraphElements}. A
     * list of typically configured options is given in {@link DiagramLayoutOptions}.
     *
     * @param <S>
     *            the concrete type of <code>element</code>
     * @param <T>
     *            the property value type
     * @param element
     *            the element to set the layout option on
     * @param option
     *            the particular layout option, e.g. one of
     *            {@link de.cau.cs.kieler.kiml.options.LayoutOptions LayoutOptions}
     * @param value
     *            the option value
     * @return <code>element</code> allowing to perform multiple operations on it in one statement
     */
    public static <S extends KGraphElement, T> S setLayoutOption(final S element,
            final IProperty<T> option, final T value) {
        element.setProperty(option, value);
        return element;
    }

    /**
     * Convenience method for defining layout options for {@link KGraphElement KGraphElements} based
     * on ids and string representations of property values. A list of typically configured options
     * is given in {@link DiagramLayoutOptions}.
     *
     * @param <S>
     *            the concrete type of <code>element</code>
     * @param element
     *            the element to set the layout option on
     * @param optionId
     *            the particular layout option's id, e.g. one of
     *            {@link de.cau.cs.kieler.kiml.options.LayoutOptions LayoutOptions}
     * @param value
     *            the option value. It is possible to pass string representations of EnumSets as
     *            well as any non-string property.
     * @return <code>element</code> allowing to perform multiple operations on it in one statement
     */
    public static <S extends KGraphElement> S setLayoutOption(final S element,
            final String optionId, final Object value) {
        final LayoutOptionData option = LayoutMetaDataService.getInstance().getOptionData(optionId);
        if (option != null) {
            Object realValue = value;
            if (value instanceof String) {
                realValue = option.parseValue((String) value);
            }
            element.setProperty(option, realValue);
        }
        return element;
    }
    
    /**
     * Convenience method for defining multiple layout options for {@link KGraphElement
     * KGraphElements}. A list of typically configured options is given in
     * {@link DiagramLayoutOptions}.<br>
     * The required <code>optionValueMap</code> can be easily created via<br>
     * {@link com.google.common.collect.ImmutableMap#of(Object, Object, Object, Object)
     * ImmutableMap.&lt;IProperty&lt;?&gt;, Object&gt;of(option, value, option, value, ...)}, for
     * example.
     *
     * @param <S>
     *            the concrete type of <code>element</code>
     * @param element
     *            the element to set the layout option on
     * @param optionValueMap
     *            a {@link Map} containing valid pairs of layout options, e.g. some of
     *            {@link de.cau.cs.kieler.kiml.options.LayoutOptions LayoutOptions}, and
     *            corresponding option values.
     * @return <code>element</code> allowing to perform multiple operations on it in one statement
     */
    public static <S extends KGraphElement> S setLayoutOptions(final S element,
            final Map<IProperty<?>, ?> optionValueMap) {
        
        if (optionValueMap != null) {
            for (final Map.Entry<IProperty<?>, ?> entry : optionValueMap.entrySet()) {
                @SuppressWarnings("unchecked")
                final IProperty<Object> key = (IProperty<Object>) entry.getKey();
                element.setProperty(key, entry.getValue());
            }
        }
        return element;
    }

    /**
     * Convenience function to assemble a pair of layout option and allowed values. This method
     * should be used in conjunction with {@link AbstractDiagramSynthesis#getDisplayedLayoutOptions()}.
     * <br>
     * The type of the elements within the 'values' parameter depends on the type if the specified
     * property. For instance, for an <code>IProperty&lt;Integer&gt;</code> one might pass a
     * <code>List&lt;Integer&gt;</code> of size 2 to specify the lower and upper bound for the values.
     *
     * @param prop
     *            the desired property.
     * @param values
     *            the allowed values.
     * @return a pair with the property and the possible values.
     */
    public static Pair<IProperty<?>, List<?>> specifyLayoutOption(final IProperty<?> prop,
            final List<?> values) {
        return Pair.<IProperty<? extends Object>, List<? extends Object>>of(prop, values);
    }

    /**
     * Convenience function to assemble a pair of layout option and allowed values. This method
     * should be used in conjunction with {@link AbstractDiagramSynthesis#getDisplayedLayoutOptions()}.
     * <br>
     * The type of the elements within the 'values' parameter depends on the type if the specified
     * property. For instance, for an <code>IProperty&lt;Integer&gt;</code> one might pass a
     * <code>List&lt;Integer&gt;</code> of size 2 to specify the lower and upper bound for the values.
     *
     * @param layoutOptionId
     *            the desired layout option's id.
     * @param values
     *            the allowed values.
     * @return a pair with the property and the possible values.
     */
    public static Pair<IProperty<?>, List<?>> specifyLayoutOption(final String layoutOptionId, 
            final List<?> values) {
        final LayoutOptionData option =
                LayoutMetaDataService.getInstance().getOptionData(layoutOptionId);
        if (option != null) {
            return specifyLayoutOption(option, values);
        }
        throw new IllegalArgumentException("Could not resolve the layout option '" + layoutOptionId 
                + "' using KIML's layout option service. "
                + "Make sure the layout option exists and is properly registered with KIML.");
    }
    
    /**
     * Convenience method for defining collapse/expand state dependent layout options for
     * {@link KNode KNodes}. A list of typically configured options is given in
     * {@link DiagramLayoutOptions}.
     *
     * @param <T>
     *            the property value type
     * @param node
     *            the node to set the layout option on
     * @param option
     *            the particular layout option, e.g. one of
     *            {@link de.cau.cs.kieler.kiml.options.LayoutOptions LayoutOptions}
     * @param collapsedValue
     *            the value in case <code>node</code> is collapsed
     * @param expandedValue
     *            the value in case <code>node</code> is expanded
     * @return <code>node</code> allowing to perform multiple operations on it in one statement
     */
    public static <T> KNode setExpansionAwareLayoutOption(final KNode node, final IProperty<T> option,
            final T collapsedValue, final T expandedValue) {
        
        ExpansionAwareLayoutOption.setProperty(node, option, collapsedValue, expandedValue);
        return node;
    }

    /**
     * Convenience method for defining collapse/expand state dependent layout options for
     * {@link KPort KPorts}. The collapse/expand state refers to that of the {@link KNode}
     * containing the {@link KPort}. A list of typically configured options is given in
     * {@link DiagramLayoutOptions}.
     *
     * @param <T>
     *            the property value type
     * @param port
     *            the port to set the layout option on
     * @param option
     *            the particular layout option, e.g. one of
     *            {@link de.cau.cs.kieler.kiml.options.LayoutOptions LayoutOptions}
     * @param collapsedValue
     *            the value in case <code>port</code>'s container node is collapsed
     * @param expandedValue
     *            the value in case <code>port</code>'s container node is expanded
     * @return <code>port</code> allowing to perform multiple operations on it in one statement
     */
    public static <T> KPort setExpansionAwareLayoutOption(final KPort port,
            final IProperty<T> option, final T collapsedValue, final T expandedValue) {
        
        ExpansionAwareLayoutOption.setProperty(port, option, collapsedValue, expandedValue);
        return port;
    }


    /**
     * Configures the provided {@link KRendering} to represent the parent {@link KNode} if it is in
     * <b>collapsed</b> state. This information is evaluated for those {@link KRendering
     * KRenderings} only that are directly attached to {@link KNode KNodes}. For other
     * {@link KRendering KRenderings} this configuration has no effect.
     *
     * @param <T>
     *            the concrete type of <code>krendering</code>
     * @param krendering
     *            the {@link KRendering} to be configured
     * @return <code>rendering</code> for convenience
     */
    public static <T extends KRendering> T setAsCollapsedView(final T krendering) {
        krendering.getProperties().removeKey(KlighdProperties.EXPANDED_RENDERING);
        krendering.setProperty(KlighdProperties.COLLAPSED_RENDERING, true);
        return krendering;
    }

    /**
     * Configures the provided {@link KRendering} to represent the parent {@link KNode} if it is in
     * <b>expanded</b> state. This information is evaluated for those {@link KRendering KRenderings}
     * only that are directly attached to {@link KNode KNodes}. For other {@link KRendering
     * KRenderings} this configuration has no effect.
     *
     * @param <T>
     *            the concrete type of <code>krendering</code>
     * @param krendering
     *            the {@link KRendering} to be configured
     * @return <code>rendering</code> for convenience
     */
    public static <T extends KRendering> T setAsExpandedView(final T krendering) {
        krendering.getProperties().removeKey(KlighdProperties.COLLAPSED_RENDERING);
        krendering.setProperty(KlighdProperties.EXPANDED_RENDERING, true);
        return krendering;
    }

    /**
     * Configures the provided {@link KGraphElement} to be visible only if the diagram zoom scale is
     * strictly less then the given <code>upperBounds</code>.
     *
     * @param <S>
     *            the concrete type of <code>kgraphElement</code>
     * @param kgraphElement
     *            the {@link KGraphElement} to be configured
     * @param upperBound
     *            the desired upper bound
     * @return <code>kgraphElement</code> for convenience
     */
    public static <S extends KGraphElement> S setUpperVisibilityScaleBound(final S kgraphElement,
            final Number upperBound) {
        kgraphElement.setProperty(KlighdProperties.VISIBILITY_SCALE_UPPER_BOUND, upperBound);
        return kgraphElement;
    }

    /**
     * Configures the provided {@link KGraphElement} to be visible only if the diagram zoom scale is
     * higher or equal then the given <code>lowerBound</code>.
     *
     * @param <S>
     *            the concrete type of <code>kgraphElement</code>
     * @param kgraphElement
     *            the {@link KGraphElement} to be configured
     * @param lowerBound
     *            the desired lower bound
     * @return <code>kgraphElement</code> for convenience
     */
    public static <S extends KGraphElement> S setLowerVisibilityScaleBound(final S kgraphElement,
            final Number lowerBound) {
        kgraphElement.setProperty(KlighdProperties.VISIBILITY_SCALE_LOWER_BOUND, lowerBound);
        return kgraphElement;
    }

    /**
     * Configures the provided {@link KRendering} to be visible only if the diagram zoom scale is
     * strictly less then the given <code>upperBounds</code>.
     *
     * @param <T>
     *            the concrete type of <code>krendering</code>
     * @param krendering
     *            the {@link KRendering} to be configured
     * @param upperBound
     *            the desired upper bound
     * @return <code>rendering</code> for convenience
     */
    public static <T extends KRendering> T setUpperVisibilityScaleBound(final T krendering,
            final Number upperBound) {
        krendering.setProperty(KlighdProperties.VISIBILITY_SCALE_UPPER_BOUND, upperBound);
        return krendering;
    }

    /**
     * Configures the provided {@link KRendering} to be visible only if the diagram zoom scale is
     * higher or equal then the given <code>lowerBound</code>.
     *
     * @param <T>
     *            the concrete type of <code>krendering</code>
     * @param krendering
     *            the {@link KRendering} to be configured
     * @param lowerBound
     *            the desired lower bound
     * @return <code>rendering</code> for convenience
     */
    public static <T extends KRendering> T setLowerVisibilityScaleBound(final T krendering,
            final Number lowerBound) {
        krendering.setProperty(KlighdProperties.VISIBILITY_SCALE_LOWER_BOUND, lowerBound);
        return krendering;
    }

    /**
     * Configures the provided {@link KGraphElement} to be visible only if the diagram zoom scale is
     * strictly less then the level causing <code>kgraphElement</code> to be drawn with absolute
     * <em>height</em> of <code>upperBound</code>.
     *
     * @param <S>
     *            the concrete type of <code>kgraphElement</code>
     * @param kgraphElement
     *            the {@link KGraphElement} to be configured
     * @param upperBound
     *            the desired upper (absolute) height bound
     * @return <code>kgraphElement</code> for convenience
     */
    public static <S extends KGraphElement> S setUpperVisibilityHeightBound(final S kgraphElement,
            final Number upperBound) {
        kgraphElement.setProperty(KlighdProperties.VISIBILITY_HEIGHT_UPPER_BOUND, upperBound);
        return kgraphElement;
    }

    /**
     * Configures the provided {@link KGraphElement} to be visible only if the diagram zoom scale is
     * higher or equal to the level causing <code>kgraphElement</code> to be drawn with absolute
     * <em>height</em> of <code>lowerBound</code>.
     *
     * @param <S>
     *            the concrete type of <code>kgraphElement</code>
     * @param kgraphElement
     *            the {@link KGraphElement} to be configured
     * @param lowerBound
     *            the desired lower (absolute) height bound
     * @return <code>kgraphElement</code> for convenience
     */
    public static <S extends KGraphElement> S setLowerVisibilityHeightBound(final S kgraphElement,
            final Number lowerBound) {
        kgraphElement.setProperty(KlighdProperties.VISIBILITY_HEIGHT_LOWER_BOUND, lowerBound);
        return kgraphElement;
    }

    /**
     * Configures the provided {@link KRendering} to be visible only if the diagram zoom scale is
     * strictly less then the level causing <code>krendering</code> to be drawn with absolute
     * <em>height</em> of <code>upperBound</code>.
     *
     * @param <T>
     *            the concrete type of <code>krendering</code>
     * @param krendering
     *            the {@link KRendering} to be configured
     * @param upperBound
     *            the desired upper (absolute) height bound
     * @return <code>krendering</code> for convenience
     */
    public static <T extends KRendering> T setUpperVisibilityHeightBound(final T krendering,
            final Number upperBound) {
        krendering.setProperty(KlighdProperties.VISIBILITY_HEIGHT_UPPER_BOUND, upperBound);
        return krendering;
    }

    /**
     * Configures the provided {@link KRendering} to be visible only if the diagram zoom scale is
     * higher or equal to the level causing <code>krendering</code> to be drawn with absolute
     * <em>height</em> of <code>lowerBound</code>.
     *
     * @param <T>
     *            the concrete type of <code>krendering</code>
     * @param krendering
     *            the {@link KRendering} to be configured
     * @param lowerBound
     *            the desired lower (absolute) height bound
     * @return <code>krendering</code> for convenience
     */
    public static <T extends KRendering> T setLowerVisibilityHeightBound(final T krendering,
            final Number lowerBound) {
        krendering.setProperty(KlighdProperties.VISIBILITY_HEIGHT_LOWER_BOUND, lowerBound);
        return krendering;
    }

    /**
     * Configures the provided {@link KGraphElement} to be visible only if the diagram zoom scale is
     * strictly less then the level causing <code>kgraphElement</code> to be drawn with absolute
     * <em>width</em> of <code>upperBound</code>.
     *
     * @param <S>
     *            the concrete type of <code>kgraphElement</code>
     * @param kgraphElement
     *            the {@link KGraphElement} to be configured
     * @param upperBound
     *            the desired upper (absolute) width bound
     * @return <code>kgraphElement</code> for convenience
     */
    public static <S extends KGraphElement> S setUpperVisibilityWidthBound(final S kgraphElement,
            final Number upperBound) {
        kgraphElement.setProperty(KlighdProperties.VISIBILITY_WIDTH_UPPER_BOUND, upperBound);
        return kgraphElement;
    }

    /**
     * Configures the provided {@link KGraphElement} to be visible only if the diagram zoom scale is
     * higher or equal to the level causing <code>kgraphElement</code> to be drawn with absolute
     * <em>width</em> of <code>lowerBound</code>.
     *
     * @param <S>
     *            the concrete type of <code>kgraphElement</code>
     * @param kgraphElement
     *            the {@link KGraphElement} to be configured
     * @param lowerBound
     *            the desired lower (absolute) width bound
     * @return <code>kgraphElement</code> for convenience
     */
    public static <S extends KGraphElement> S setLowerVisibilityWidthBound(final S kgraphElement,
            final Number lowerBound) {
        kgraphElement.setProperty(KlighdProperties.VISIBILITY_WIDTH_LOWER_BOUND, lowerBound);
        return kgraphElement;
    }

    /**
     * Configures the provided {@link KRendering} to be visible only if the diagram zoom scale is
     * strictly less then the level causing <code>krendering</code> to be drawn with absolute
     * <em>width</em> of <code>upperBound</code>.
     *
     * @param <T>
     *            the concrete type of <code>krendering</code>
     * @param krendering
     *            the {@link KRendering} to be configured
     * @param upperBound
     *            the desired upper (absolute) height bound
     * @return <code>krendering</code> for convenience
     */
    public static <T extends KRendering> T setUpperVisibilityWidthBound(final T krendering,
            final Number upperBound) {
        krendering.setProperty(KlighdProperties.VISIBILITY_WIDTH_UPPER_BOUND, upperBound);
        return krendering;
    }

    /**
     * Configures the provided {@link KRendering} to be visible only if the diagram zoom scale is
     * higher or equal to the level causing <code>krendering</code> to be drawn with absolute
     * <em>width</em> of <code>lowerBound</code>.
     *
     * @param <T>
     *            the concrete type of <code>krendering</code>
     * @param krendering
     *            the {@link KRendering} to be configured
     * @param lowerBound
     *            the desired lower (absolute) height bound
     * @return <code>krendering</code> for convenience
     */
    public static <T extends KRendering> T setLowerVisibilityWidthBound(final T krendering,
            final Number lowerBound) {
        krendering.setProperty(KlighdProperties.VISIBILITY_WIDTH_LOWER_BOUND, lowerBound);
        return krendering;
    }

    /**
     * Configures the provided {@link KRendering} to propagate its diagram zoom scale-based
     * visibility expressions to its children. In other words: if set to <code>true</code>
     * drawing the whole figure (sub) tree is skipped, instead of solely the figure itself.
     *
     * @param <T>
     *            the concrete type of <code>krendering</code>
     * @param krendering
     *            the {@link KRendering} to be configured
     * @param propagateToChildren
     *            <code>true</code> if the visibility configuration shall apply to children,
     *            <code>false</code> otherwise.
     * @return <code>krendering</code> for convenience
     */
    public static <T extends KRendering> T setPropagateVisibilityBoundsToChildren(final T krendering,
            final boolean propagateToChildren) {
        krendering.setProperty(
                KlighdProperties.VISIBILITY_PROPAGATE_TO_CHILDREN, propagateToChildren);
        return krendering;
    }

    /**
     * Configures the provided {@link KRendering} to propagate its diagram zoom scale-based
     * visibility expressions to its children. In other words: if set drawing the whole figure (sub)
     * tree is skipped, instead of solely the figure itself.
     *
     * @param <T>
     *            the concrete type of <code>krendering</code>
     * @param krendering
     *            the {@link KRendering} to be configured
     * @return <code>krendering</code> for convenience
     */
    public static <T extends KRendering> T propagateVisibilityBoundsToChildren(final T krendering) {
        return setPropagateVisibilityBoundsToChildren(krendering, true);
    }

    /**
     * Configures the provided {@link KRendering} to be excluded from the outline diagram view.
     *
     * @param <T>
     *            the concrete type of <code>krendering</code>
     * @param krendering
     *            the {@link KRendering} to be configured
     * @return <code>rendering</code> for convenience
     */
    public static <T extends KRendering> T excludedFromOutline(final T krendering) {
        krendering.setProperty(KlighdProperties.OUTLINE_INVISIBLE, true);
        return krendering;
    }

    /**
     * Configures the provided {@link KRendering} to be excluded from exported diagram images.
     *
     * @param <T>
     *            the concrete type of <code>krendering</code>
     * @param krendering
     *            the {@link KRendering} to be configured
     * @return <code>rendering</code> for convenience
     */
    public static <T extends KRendering> T excludedFromExportedImages(final T krendering) {
        krendering.setProperty(KlighdProperties.EXPORTED_IMAGE_INVISIBLE, true);
        return krendering;
    }

    /**
     * Configures the provided {@link KRendering} to be excluded from diagram printouts.
     *
     * @param <T>
     *            the concrete type of <code>krendering</code>
     * @param krendering
     *            the {@link KRendering} to be configured
     * @return <code>rendering</code> for convenience
     */
    public static <T extends KRendering> T excludedFromPrintouts(final T krendering) {
        krendering.setProperty(KlighdProperties.PRINTOUT_INVISIBLE, true);
        return krendering;
    }

    /**
     * Configures the provided {@link KRendering} to be excluded from exported diagram images and
     * printouts.
     *
     * @param <T>
     *            the concrete type of <code>krendering</code>
     * @param krendering
     *            the {@link KRendering} to be configured
     * @return <code>rendering</code> for convenience
     */
    public static <T extends KRendering> T excludedFromExportedImagesAndPrintouts(final T krendering) {
        krendering.setProperty(KlighdProperties.EXPORTED_IMAGE_INVISIBLE, true);
        krendering.setProperty(KlighdProperties.PRINTOUT_INVISIBLE, true);
        return krendering;
    }

    /**
     * Configures the provided {@link KNode} to be shown in the diagram in collapsed fashion.
     *
     * @param node
     *            the {@link KNode} to be configured
     * @return <code>node</code> for convenience
     */
    public static KNode initiallyCollapse(final KNode node) {
        node.setProperty(KlighdProperties.EXPAND, false);
        return node;
    }

    /**
     * Configures the provided {@link KNode} to be shown in the diagram in expanded fashion.<br>
     * Note that this is the default configuration, so calling this method is not required in common
     * case.
     *
     * @param node
     *            the {@link KNode} to be configured
     * @return <code>node</code> for convenience
     */
    public static KNode initiallyExpand(final KNode node) {
        node.setProperty(KlighdProperties.EXPAND, true);
        return node;
    }

    /**
     * Configures the provided {@link KGraphElement} to be initially hidden from the diagram.<br>
     *
     * @param <S>
     *            the concrete type of the {@link KGraphElement} to be configured
     * @param kge
     *            the {@link KGraphElement} to be configured
     * @return <code>kge</code> for convenience
     */
    public static <S extends KGraphElement> S initiallyHide(final S kge) {
        kge.setProperty(KlighdProperties.SHOW, false);
        return kge;
    }

    /**
     * Configures the provided {@link KGraphElement} to be initially shown in the diagram.<br>
     * Note that this is the default configuration, so calling this method is not required in common
     * case.
     *
     * @param <S>
     *            the concrete type of the {@link KGraphElement} to be configured
     * @param kge
     *            the {@link KGraphElement} to be configured
     * @return <code>kge</code> for convenience
     */
    public static <S extends KGraphElement> S initiallyShow(final S kge) {
        kge.setProperty(KlighdProperties.SHOW, true);
        return kge;
    }

    /**
     * Configures the initial scale of the provided {@link KNode}.
     *
     * @param node
     *            the {@link KNode} to be configured
     * @param scale
     *            the initial scale factor of <code>node</code>'s representation in the diagram
     * @return <code>node</code> for convenience
     */
    public static KNode initiallyScale(final KNode node, final double scale) {
        node.setProperty(CoreOptions.SCALE_FACTOR, scale);
        return node;
    }

    private static final String NO_VIEWCONTEXT_ERROR_MSG =
            "KLighD: Failed to set the initial diagram clip in XX: No ViewContext is available.";

    /**
     * Configures the {@link KNode} the diagram is initially clipped to.<br>
     * Note that this method has to be called on the {@link KNode} to be the initial clip node
     * rather than, e.g., the view model's root node.<br>
     * <br>
     * This method is just an alias of {@link #initiallyClipTo(KNode, ViewContext)} allowing a more
     * intuitive formulation in Xtend's infix notation, like
     *
     * <pre>
     * viewContext.initiallyClipTo(node);
     * </pre>
     *
     * @param viewContext
     *            the {@link ViewContext} to perform this definition in
     * @param node
     *            the {@link KNode} to be the initial diagram clip node
     * @return <code>node</code> for convenience
     */
    public static KNode initiallyClipTo(final ViewContext viewContext, final KNode node) {
        return initiallyClipTo(node, viewContext);
    }

    /**
     * Configures the {@link KNode} the diagram is initially clipped to.<br>
     * Note that this method has to be called on the {@link KNode} to be the initial clip node
     * rather than, e.g., the view model's root node.
     *
     * @param node
     *            the {@link KNode} to be the initial diagram clip node
     * @param viewContext
     *            the {@link ViewContext} to perform this definition in
     * @return <code>node</code> for convenience
     */
    public static KNode initiallyClipTo(final KNode node, final ViewContext viewContext) {
        if (viewContext != null) {
            viewContext.setProperty(KlighdProperties.CLIP, node);
        } else {
            throw new RuntimeException(NO_VIEWCONTEXT_ERROR_MSG);
        }
        return node;
    }
    
    private static final String NO_DECORATOR_ERROR_MSG = 
            "KLighD: Only decorator renderings and KTexts can be made not selectable.";

    /**
     * Deactivates the selectability of given {@link KGraphElement}.<br>
     * If done the {@link KGraphElement} can't be selected anymore, other event handling like
     * associated action evaluation will not be affected.
     * 
     * @param <S>
     *            the concrete type of <code>kge</code>
     * @param kge
     *            the {@link KGraphElement} to configure
     * @return the <code>kge</code> for convenience
     */
    public static <S extends KGraphElement> S suppressSelectability(final S kge) {
        kge.setProperty(KlighdProperties.NOT_SELECTABLE, true);
        return kge;
    }

    /**
     * Deactivates the selectability of given {@link KText}.<br>
     * If done the {@link KText} can't be selected anymore, other event handling like associated
     * action evaluation will not be affected.
     *
     * @param kText
     *            the {@link KText} to configure
     * @return the <code>kText</code> for convenience
     */
    public static KText suppressSelectability(final KText kText) {
        kText.setProperty(KlighdProperties.NOT_SELECTABLE, true);
        return kText;
    }
    
    /**
     * Deactivates the selectability of given decorator {@link KRendering}.<br>
     * If done the {@link KRendering} can't be selected anymore and other event handling like associated
     * action evaluation will be caused on the element behind it instead.
     *
     * @param kRendering
     *            the decorator {@link KRendering} to configure
     * @return the <code>kRendering</code> for convenience
     * @throws IllegalArgumentException if the given rendering is not a decorator.
     */
    public static KRendering suppressSelectablility(final KRendering kRendering) {
        if (!(kRendering.getPlacementData() instanceof KDecoratorPlacementData)) {
            throw new IllegalArgumentException(NO_DECORATOR_ERROR_MSG);
        }
        kRendering.setProperty(KlighdProperties.NOT_SELECTABLE, true);
        return kRendering;
    }

    /**
     * Configures {@link KlighdSemanticDiagramData semantic data} on the provided {@link KGraphElement}.
     *
     * @param <S>
     *            the concrete type of <code>kge</code>
     * @param kge
     *            the {@link KGraphElement} to be configured
     * @param data
     *            the {@link KlighdSemanticDiagramData} record to be evaluated while exporting
     *            vector graphic images
     * @return <code>kge</code> for convenience
     */
    public static <S extends KGraphElement> S setSemanticData(final S kge,
            final KlighdSemanticDiagramData data) {
        kge.setProperty(KlighdProperties.SEMANTIC_DATA, data);
        return kge;
    }

    /**
     * Configures {@link KlighdSemanticDiagramData semantic data} on the provided {@link KRendering}.
     *
     * @param <T>
     *            the concrete type of <code>krendering</code>
     * @param krendering
     *            the {@link KRendering} to be configured
     * @param data
     *            the {@link KlighdSemanticDiagramData} record to be evaluated while exporting
     *            vector graphic images
     * @return <code>krendering</code> for convenience
     */
    public static <T extends KRendering> T setSemanticData(final T krendering,
            final KlighdSemanticDiagramData data) {
        krendering.setProperty(KlighdProperties.SEMANTIC_DATA, data);
        return krendering;
    }    

    /**
     * Configures a tooltip on the provided {@link KGraphElement KGraphElement}. This method has no
     * effect if another tooltip is defined on any {@link KRendering} attached to this {@link KGraphElement}
     * (except of blowing up the view model :-P).
     *
     * @param <S>
     *            the concrete type of {@code kge}
     * @param kge
     *            the {@link KGraphElement} to be configured
     * @param tooltip
     *            the tooltip text to be shown while hovering over the corresponding diagram figure
     * @return <code>kge</code> for convenience
     */
    public static <S extends KGraphElement> S setTooltip(final S kge, final String tooltip) {
        kge.setProperty(KlighdProperties.TOOLTIP, tooltip);
        return kge;
    }

    /**
     * Configures a tooltip on the provided {@link KRendering} of a {@link KGraphElement}.
     *
     * @param <T>
     *            the concrete type of <code>krendering</code>
     * @param krendering
     *            the {@link KRendering} to be configured
     * @param tooltip
     *            the tooltip text to be shown while hovering over the corresponding diagram figure
     * @return <code>rendering</code> for convenience
     */
    public static <T extends KRendering> T setTooltip(final T krendering, final String tooltip) {
        krendering.setProperty(KlighdProperties.TOOLTIP, tooltip);
        return krendering;
    }

    /** Corner size of the rounded rectangle used to display selections. */
    private static final float SELECTION_RECTANGLE_CORNER_SIZE = 3.0f;
    /** How much larger the selection rectangle should be than the original rendering. */
    private static final float SELECTION_RECTANGLE_ENLARGEMENT = 3.0f;
    /** Red component of the standard selection color. */
    private static final int SELECTION_COLOR_R = 180;
    /** Green component of the standard selection color. */
    private static final int SELECTION_COLOR_G = 213;
    /** Blue component of the standard selection color. */
    private static final int SELECTION_COLOR_B = 255;
}
