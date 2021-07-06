/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://rtsys.informatik.uni-kiel.de/kieler
 *
 * Copyright 2016 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 *
 * This code is provided under the terms of the Eclipse Public License (EPL).
 */
package de.cau.cs.kieler.klighd;

import java.util.List;

import org.eclipse.elk.core.LayoutConfigurator;
import org.eclipse.elk.core.math.KVector;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.graph.properties.IPropertyHolder;

import de.cau.cs.kieler.klighd.kgraph.KGraphElement;
import de.cau.cs.kieler.klighd.kgraph.KNode;
import de.cau.cs.kieler.klighd.util.KlighdSynthesisProperties;

/**
 * Configuration object for {@link LightDiagramServices} methods
 * {@link LightDiagramServices#layoutDiagram(LightDiagramLayoutConfig)} and
 * {@link LightDiagramServices#updateDiagram(LightDiagramLayoutConfig). These configuration holders
 * should be disposed of after use. For this reason there are no public getters available.
 * 
 * Example:
 * <code>
 * new LightDiagramLayoutConfig(viewContext)
 *          .zoomStyle(zoomStyle)
 *          .focusNode(focusNode)
 *          .layout();
 * </code>
 * 
 * @author enbewe
 */
public class LightDiagramLayoutConfig {

    /**
     * The {@link ViewContext} for this layout.
     */
    private ViewContext viewContext;

    /**
     * Flag whether the layout should be animated.
     */
    private Boolean animate;

    /**
     * The minimal time for animations, in milliseconds.
     */
    private Integer minAnimationTime;
    
    /**
     * The maximal time for animations, in milliseconds.
     */
    private Integer maxAnimationTime;
    
    /**
     * Factor for computation of animation time.
     */
    private Integer animationTimeFactor;
    
    /**
     * The {@link ZoomStyle} to be employed.
     */
    private ZoomStyle zoomStyle;

    /**
     * The {@link KGraphElement} to focus on, if {@link ZoomStyle#ZOOM_TO_FOCUS} or
     * {@link ZoomStyle#ZOOM_TO_STAY_SELECTED} is active.
     */
    private KGraphElement focusElement;
    
    /**
     * The previous position of an element before the layout, if {@link ZoomStyle#ZOOM_TO_STAY_SELECTED} is active.
     */
    private KVector previousPosition;

    /**
     * List of {@link LayoutConfigurator LayoutConfigurators} to override default layout
     * configuration.
     */
    private List<LayoutConfigurator> options;

    /**
     * {@link IPropertyHolder} to influence the way the model update is handled.
     */
    private IPropertyHolder properties;

    /**
     * Model to be used to update the existing model in the {@link ViewContext}.
     */
    private Object model;
    
    /**
     * Default constructor to be called by sub implementations.
     */
    protected LightDiagramLayoutConfig() {
        
    }

    /**
     * Creates a configuration for a {@link ViewContext}.
     * 
     * @param viewContext
     *            the {@link ViewContext} to be used in the configuration.
     */
    public LightDiagramLayoutConfig(final ViewContext viewContext) {
        this.viewContext = viewContext;
    }

    ///////////////////////////////////////////////////////////
    // Modification of the configuration

    /**
     * Update the model in the configuration. Will be used when calling {@link #performUpdate()}.
     * 
     * @param theModel
     *            the new model to be used in the configuration.
     * @return the configuration
     */
    public LightDiagramLayoutConfig model(final Object theModel) {
        this.model = theModel;
        return this;
    }

    /**
     * Configure the animation of the layout.
     * 
     * @param doAnimate
     *            flag to determine if the layout should be animated
     * @return the configuration
     */
    public LightDiagramLayoutConfig animate(final Boolean doAnimate) {
        this.animate = doAnimate;
        return this;
    }

    /**
     * Configure the minimal animation time.
     * 
     * @param theMinAnimationTime
     *            the minimal time for animations, in milliseconds
     * @return the configuration
     */
    public LightDiagramLayoutConfig minAnimationTime(final Integer theMinAnimationTime) {
        this.minAnimationTime = theMinAnimationTime;
        return this;
    }

    /**
     * Configure the maximal animation time.
     * 
     * @param theMaxAnimationTime
     *            the maximal time for animations, in milliseconds
     * @return the configuration
     */
    public LightDiagramLayoutConfig maxAnimationTime(final Integer theMaxAnimationTime) {
        this.maxAnimationTime = theMaxAnimationTime;
        return this;
    }

    /**
     * Configure the animation time factor.
     * 
     * @param theAnimationTimeFactor
     *            Factor for computation of animation time. The higher the value, the longer the
     *            animation time. If the value is 0, the resulting time is always equal to the
     *            minimum defined by 'Minimal Animation Time'.
     * @return the configuration
     */
    public LightDiagramLayoutConfig animationTimeFactor(final Integer theAnimationTimeFactor) {
        this.animationTimeFactor = theAnimationTimeFactor;
        return this;
    }

    /**
     * Configuration for the {@link ZoomStyle} of the layout.
     * 
     * @param theZoomStyle
     *            the {@link ZoomStyle} to be used.
     * @return the configuration
     */
    public LightDiagramLayoutConfig zoomStyle(final ZoomStyle theZoomStyle) {
        this.zoomStyle = theZoomStyle;
        return this;
    }

    /**
     * Sets the node to focus on.
     * 
     * @param theFocusNode
     *            the {@link KNode} to focus on if {@link ZoomStyle#ZOOM_TO_FOCUS} or
     *            {@link ZoomStyle#ZOOM_TO_STAY_SELECTED} is set.
     * @return the configuration
     */
    public LightDiagramLayoutConfig focusNode(final KNode theFocusNode) {
        this.focusElement = theFocusNode;
        return this;
    }

    /**
     * Sets the element to focus on.
     * 
     * @param theFocusElement
     *            the {@link KGraphElement} to focus on if {@link ZoomStyle#ZOOM_TO_FOCUS} or
     *            {@link ZoomStyle#ZOOM_TO_STAY_SELECTED} is set.
     * @return the configuration
     */
    public LightDiagramLayoutConfig focusElement(final KGraphElement theFocusElement) {
        this.focusElement = theFocusElement;
        return this;
    }
    
    /**
     * Sets the previous position of an element before the layout run.
     * 
     * @param thePreviousPosition
     *            the previous position of an element if {@link ZoomStyle#ZOOM_TO_STAY_SELECTED} is set.
     * @return the configuration
     */
    public LightDiagramLayoutConfig previousPosition(final KVector thePreviousPosition) {
        this.previousPosition = thePreviousPosition;
        return this;
    }

    /**
     * List of {@link LayoutConfigurator LayoutConfigurators} that should override the default
     * configuration.
     * 
     * @param theOptions
     *            the list of {@link LayoutConfigurator LayoutConfigurators}
     * @return the configuration
     */
    public LightDiagramLayoutConfig options(final List<LayoutConfigurator> theOptions) {
        this.options = theOptions;
        return this;
    }

    /**
     * {@link IPropertyHolder} to modify the update behaviour of this layout.
     * 
     * @param theProperties
     *            the {@link IPropertyHolder} containing the settings.
     * @return the configuration
     */
    public LightDiagramLayoutConfig properties(final IPropertyHolder theProperties) {
        this.properties = theProperties;
        return this;
    }

    ///////////////////////////////////////////////////////////
    // Reading the configuration in LightDiagramServices

    /**
     * Returns the new model to be used during {@link #performUpdate()}.
     * 
     * @return the new model.
     */
    public Object model() {
        return this.model;
    }

    /**
     * Flag to determine the animation of the layout.
     * 
     * @return <code>true</code> if the layout should be animated.
     */
    public Boolean animate() {
        return this.animate!= null ? this.animate : (
                this.properties != null ? this.properties.getProperty(CoreOptions.ANIMATE) : null
        );
    }

    /**
     * The minimal time for animations.
     * 
     * @return the minimal time for animations, in milliseconds
     */
    public Integer minAnimationTime() {
        return this.minAnimationTime != null ? this.minAnimationTime : (
                this.properties != null ? this.properties.getProperty(CoreOptions.MIN_ANIM_TIME) : null
        );
    }

    /**
     * The maximal time for animations.
     * 
     * @return the maximal time for animations, in milliseconds
     */
    public Integer maxAnimationTime() {
        return this.maxAnimationTime != null ? this.maxAnimationTime : (
                this.properties != null ? this.properties.getProperty(CoreOptions.MAX_ANIM_TIME) : null
        );
    }

    /**
     * The animation time factor.
     * 
     * @return Factor for computation of animation time.
     */
    public Integer animationTimeFactor() {
        return this.animationTimeFactor != null ? this.animationTimeFactor : (
                this.properties != null ? this.properties.getProperty(CoreOptions.ANIM_TIME_FACTOR) : null
        );
    }

    /**
     * Returns the configured {@link ZoomStyle} of the configuration.
     * 
     * @return the {@link ZoomStyle}.
     */
    public ZoomStyle zoomStyle() {
        return this.zoomStyle;
    }

    /**
     * The node to focus on if {@link ZoomStyle#ZOOM_TO_FOCUS} or {@link ZoomStyle#ZOOM_TO_STAY_SELECTED} is
     * configured.
     * 
     * @return the focusNode
     */
    public KNode focusNode() {
        if (this.focusElement instanceof KNode) {
            return (KNode) this.focusElement;
        } else {
            return null;
        }
    }

    /**
     * The element to focus on if {@link ZoomStyle#ZOOM_TO_FOCUS} or {@link ZoomStyle#ZOOM_TO_STAY_SELECTED} is
     * configured.
     * 
     * @return the focusElement
     */
    public KGraphElement focusElement() {
        return this.focusElement;
    }
    
    /**
     * The previous position of an element if {@link ZoomStyle#ZOOM_TO_STAY_SELECTED} is configured.
     * @return
     */
    public KVector previousPosition() {
        return this.previousPosition;
    }

    /**
     * List of {@link LayoutConfigurator LayoutConfigurators} to override the default config with.
     * 
     * @return the list of {@link LayoutConfigurator LayoutConfigurators}.
     */
    public List<LayoutConfigurator> options() {
        return this.options;
    }

    /**
     * {@link IPropertyHolder} to configure the update strategy.
     * 
     * @return the {@link IPropertyHolder} containing properties like
     *         {@link KlighdSynthesisProperties#REQUESTED_UPDATE_STRATEGY}.
     */
    public IPropertyHolder properties() {
        return this.properties != null ? this.properties : KlighdSynthesisProperties.emptyConfig();
    }

    /**
     * The {@link ViewContext} this layout is related to.
     * 
     * @return the {@link ViewContext} or <code>null</code> if the configuration is based on a
     *         {@link IDiagramWorkbenchPart}.
     */
    public ViewContext viewContext() {
        return this.viewContext;
    }

    ///////////////////////////////////////////////////////////
    // Convenience aliases

    /**
     * Use this configuration and start a layout run. Convenient shorthand for
     * <code>LightDiagramServices.layoutDiagram(config)</code>.
     */
    public void performLayout() {
        LightDiagramServices.layoutDiagram(this);
    }

    /**
     * Use this configuration and update the layout. Convenient shorthand for
     * <code>LightDiagramServices.updateDiagram(config)</code>.
     * 
     * @return <code>true</code> if update could be performed successfully, <code>false</code>
     *         otherwise
     */
    public boolean performUpdate() {
        return LightDiagramServices.updateDiagram(this);
    }

}
