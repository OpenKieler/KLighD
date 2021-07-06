/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 *
 * Copyright 2011 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 *
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klighd;

import java.awt.Composite;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.elk.core.util.IGraphElementVisitor;
import org.eclipse.elk.core.util.Pair;
import org.eclipse.elk.core.util.WrappedException;
import org.eclipse.elk.graph.properties.IProperty;
import org.eclipse.elk.graph.properties.IPropertyHolder;
import org.eclipse.elk.graph.properties.MapPropertyHolder;
import org.eclipse.emf.ecore.EObject;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.cau.cs.kieler.klighd.internal.ILayoutRecorder;
import de.cau.cs.kieler.klighd.internal.ISynthesis;
import de.cau.cs.kieler.klighd.internal.util.KlighdInternalProperties;
import de.cau.cs.kieler.klighd.internal.util.SourceModelTrackingAdapter;
import de.cau.cs.kieler.klighd.kgraph.KGraphData;
import de.cau.cs.kieler.klighd.kgraph.KGraphElement;
import de.cau.cs.kieler.klighd.kgraph.KGraphPackage;
import de.cau.cs.kieler.klighd.kgraph.KNode;
import de.cau.cs.kieler.klighd.kgraph.util.KGraphUtil;
import de.cau.cs.kieler.klighd.syntheses.DuplicatingDiagramSynthesis;
import de.cau.cs.kieler.klighd.util.KlighdProperties;
import de.cau.cs.kieler.klighd.util.KlighdSynthesisProperties;
import de.cau.cs.kieler.klighd.viewers.ContextViewer;

/**
 * A view context is a data record containing the required configuration information to translate a
 * model into a diagram and draw it on the screen, as well as provide user operations on it.<br>
 * <br>
 *
 * ViewContexts contain information on
 * <ul>
 * <li>the business model (input model) that is to be shown,</li>
 * <li>the source workbench part the business model stems from,</li>
 * <li>the diagram synthesis that is in charge of creating the view model,</li>
 * <li>the update strategy that is to be applied in case of diagram updates,</li>
 * <li>the resulting view model describing the diagram,</li>
 * <li>the {@link IViewerProvider} that wraps the instantiation of the viewer being used</li>
 * </ul>
 *
 * @author mri
 * @author chsch
 *
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch
 */
public class ViewContext extends MapPropertyHolder {

    /** the serial version UID. */
    private static final long serialVersionUID = -431994394109554393L;

    /** the viewer provider. */
    protected transient IViewerProvider viewerProvider = null;

    /** the update strategy. */
    private transient IUpdateStrategy updateStrategy = null;

    /** the {@link ISynthesis} being applied. */
    protected transient ISynthesis diagramSynthesis = null;

    /** a fall-back instance of {@link DuplicatingDiagramSynthesis}, is instantiated if necessary. */
    private transient ISynthesis duplicator = null;

    /** the business model to be represented by means of this context. */
    private Object businessModel = null;

    private SourceModelTrackingAdapter tracer = new SourceModelTrackingAdapter();

    /** the view model is initiated while configuring the involved {@link IUpdateStrategy} and kept
     * for the whole life-cycle of the view context, in order to enable proper incremental update. */
    protected KNode viewModel = createViewModel();

    /** the {@link IViewer} being in charge of showing this {@link ViewContext}. */
    protected IViewer viewer = null;

    /** the {@link #viewer} if it is a {@link ILayoutRecorder}, <code>null</code> otherwise. */
    protected ILayoutRecorder layoutRecorder = null;

    /** the view-specific zoom style, initialized with the value defined in the preference store. */
    private ZoomStyle zoomStyle = KlighdPreferences.getPreferredZoomStyle();

    /**
     * Standard constructor.
     *
     * @param inputModel
     *            the source model to be represented by a diagram
     */
    public ViewContext(final Object inputModel) {
        super();
        this.businessModel = inputModel;
        this.viewModel.eAdapters().add(tracer);

    }

    /**
     * Partially copying constructor.
     *
     * @param otherContext
     *            the {@link ViewContext} to take {@link SynthesisOption} settings from, may be
     *            <code>null</code>
     * @param inputModel
     *            the source model to be represented by a diagram
     */
    public ViewContext(final ViewContext otherContext, final Object inputModel) {
        super();
        this.businessModel = inputModel;
        if (otherContext != null) {
            this.synthesisOptionConfig.putAll(otherContext.synthesisOptionConfig);
        }
    }

    // ---------------------------------------------------------------------------------- //
    //  initialization part

    /**
     * Helper method called while creating a new instance of this class.
     *
     * @return a new empty view model root node
     */
    private KNode createViewModel() {
        return KGraphUtil.createInitializedNode();
    }

    /**
     * This method performs the initial configuration of <code>this</code> view context.<br>
     * In case some custom configurations are to be applied {@link #configure(IPropertyHolder)}
     * might be called alternatively. (Currently) only one of these methods shall be called in life
     * of a {@link ViewContext} instance, and only once at that.<br>
     *
     * @return <code>this</code> {@link ViewContext} instance for convenience
     */
    public ViewContext configure() {
        return configure(KlighdSynthesisProperties.emptyConfig());
    }

    /**
     * This method performs the initial configuration of <code>this</code> view context, it
     * incorporates the properties given in <code>propertyHolder</code> during that.<br>
     * (Currently) only one of this method and {@link #configure()} shall be called in life of a
     * {@link ViewContext} instance, and only once at that.<br>
     *
     * @param propertyHolder
     *            an {@link IPropertyHolder} that contributes custom configuration instructions
     * @return <code>this</code> {@link ViewContext} instance for convenience
     */
    public ViewContext configure(final IPropertyHolder propertyHolder) {
        final KlighdDataManager data = KlighdDataManager.getInstance();

        // check for a requested diagram synthesis
        final String synthesisId =
                propertyHolder.getProperty(KlighdSynthesisProperties.REQUESTED_DIAGRAM_SYNTHESIS);
        diagramSynthesis = data.getDiagramSynthesisById(synthesisId);

        if (diagramSynthesis == null) {
            // if no concrete diagram synthesis is requested identify a matching one
            //  via the reflection-based mechanism; this involves
            // 1) get all registered diagram syntheses matching the business models type
            // 2) take the first one whose 'supports()' methods returns 'true'

            if (businessModel instanceof ISourceProxy) {
                // in case source model is not directly provided but accessible via a proxy
                // wrap the above described operation into a function and let it be
                //  executed by the proxy;
                // this approach is required in order to comply with the requirements of
                //  transaction-based model sources like Xtext editors

                final ISourceProxy proxy = (ISourceProxy) this.businessModel;
                diagramSynthesis = proxy.execute(new Function<Object, ISynthesis>() {

                    public ISynthesis apply(final Object input) {
                        return Iterables.getFirst(Iterables.filter(
                                data.getAvailableSyntheses(input.getClass()),
                                createSynthesisFilter(input)), null);
                    }
                });

            } else {
                // in the common case just execute the query directly (yes this is duplicate code)
                diagramSynthesis = Iterables.getFirst(Iterables.filter(
                                data.getAvailableSyntheses(this.businessModel.getClass()),
                                synthesisFilter), null);
            }
        }

        if (diagramSynthesis != null) {
            // if a diagram synthesis could be identified ...
            // ... get its offered synthesis options, ...
            synthesisOptions.addAll(diagramSynthesis.getDisplayedSynthesisOptions());

            // ... initialize their values according to the given 'synthesisOptionConfig', ...
            //  (those are configured if this ViewContext was created based on another one)
            for (final SynthesisOption option: synthesisOptions) {
                if (!this.synthesisOptionConfig.containsKey(option)) {
                    this.configureOption(option, option.getInitialValue());
                }
            }

            final Map<SynthesisOption, Object> config =
                    propertyHolder.getProperty(KlighdSynthesisProperties.SYNTHESIS_OPTION_CONFIG);

            // ... and evaluate synthesis option initialization settings provided with the
            //  diagram request, e.g. via "DiagramViewManager.createView(...)";
            if (config != null) {
                for (final Map.Entry<SynthesisOption, Object> entry : config.entrySet()) {
                    this.configureOption(entry.getKey(), entry.getValue());
                }
            }
        }

        final String updateStrategyId =
                propertyHolder.getProperty(KlighdSynthesisProperties.REQUESTED_UPDATE_STRATEGY);
        updateStrategy = data.getUpdateStrategyById(updateStrategyId);

        if (updateStrategy == null) {
            updateStrategy = data.getHighestPriorityUpdateStrategy();
            // updateStrategy is supposed to be non-null since this plug-in registers the
            //  SimpleUpdateStrategy
        }

        // get the viewer provider request
        final String viewerProviderId =
                propertyHolder.getProperty(KlighdSynthesisProperties.REQUESTED_VIEWER_PROVIDER);
        viewerProvider = data.getViewerProviderById(viewerProviderId);

        if (viewerProvider == null) {
            viewerProvider = Iterables.getFirst(data.getAvailableViewerProviders(), null);
        }

        // copy the properties from propertyHolder into 'this', e.g. in order to provide them to
        //  to the diagram synthesis
        this.copyProperties(propertyHolder);

        return this;
    }

    private final Predicate<ISynthesis> synthesisFilter = createSynthesisFilter(null);

    private Predicate<ISynthesis> createSynthesisFilter(final Object input) {
        return new Predicate<ISynthesis>() {

            public boolean apply(final ISynthesis synthesis) {
                final Object model = input != null ? input : ViewContext.this.businessModel;

                try {
                    return synthesis.supports(model, ViewContext.this);

                } catch (final WrappedException e) {
                    Klighd.log(
                            new Status(IStatus.ERROR, Klighd.PLUGIN_ID, e.getMessage(), e.getCause()));
                    return false;
                }
            }
        };
    }

    /**
     * Creates the wrapped {@link IViewer} instance that is actually in charge of drawing the diagram
     * into the provided SWT {@link Composite} widget <code>parent</code>. Returns <code>null</code>
     * if no fitting {@link IViewerProvider} has been found during configuration.
     *
     * @param parentViewer
     *            the parent {@link ContextViewer}
     * @param parent
     *            the parent {@link Composite} widget
     * @return the created viewer or <code>null</code> on failure
     */
    public IViewer createViewer(final ContextViewer parentViewer) {
        if (this.viewerProvider != null) {
            // create the new viewer
            this.viewer = this.viewerProvider.createViewer(parentViewer);

            if (this.viewer instanceof ILayoutRecorder) {
                this.layoutRecorder = (ILayoutRecorder) viewer;
            }

            // set the base model if possible
            this.viewer.setModel(this.viewModel, true);

            return this.viewer;
        } else {
            return null;
        }
    }


    /**
     * Executes the {@link #diagramSynthesis} attached to <code>this</code> view context involving
     * the formerly set input/source model and updates the view model by applying the configured
     * {@link IUpdateStrategy}.
     * 
     * @return <code>true</code> if view update succeeded, <code>false</code> otherwise
     */
    public boolean update() {
        return this.update(null);
    }


    /**
     * Executes the {@link #diagramSynthesis} attached to <code>this</code> view context and updates
     * the view model by applying the configured {@link IUpdateStrategy}. In case the formerly set
     * input/source model has been replaced by a new one of compatible type this new one must be
     * provided, otherwise <code>model</code> may by <code>null</code>.
     *
     * @param sourceModel
     *            the initial, updated, or replaced input model, may be <code>null</code>
     * @return <code>true</code> if view update succeeded, <code>false</code> otherwise
     */
    public boolean update(final Object sourceModel) {
        return this.update(sourceModel, this.updateStrategy);
    }

    /**
     * Executes the {@link #diagramSynthesis} attached to <code>this</code> view context and updates
     * the view model by applying the configured {@link IUpdateStrategy}. In case the formerly set
     * input/source model has been replaced by a new one of compatible type this new one must be
     * provided, otherwise <code>model</code> may by <code>null</code>.
     *
     * @param sourceModel
     *            the initial, updated, or replaced input model, may be <code>null</code>
     * @param properties
     *            a property holder that might influence the diagram update, e.g. via the
     *            {@link KlighdSynthesisProperties#REQUESTED_UPDATE_STRATEGY} property configuration
     * @return <code>true</code> if view update succeeded, <code>false</code> otherwise
     */
    public boolean update(final Object sourceModel, final IPropertyHolder properties) {
        final IUpdateStrategy strategy;
        if (properties != null) {
            final String usId =
                    properties.getProperty(KlighdSynthesisProperties.REQUESTED_UPDATE_STRATEGY);
            strategy = KlighdDataManager.getInstance().getUpdateStrategyById(usId);

        } else {
            strategy = null;
        }

        return this.update(sourceModel, strategy);
    }

    /**
     * Executes the {@link #diagramSynthesis} attached to <code>this</code> view context and updates
     * the view model by applying the configured {@link IUpdateStrategy}. In case the formerly set
     * input/source model has been replaced by a new one of compatible type this new one must be
     * provided, otherwise <code>model</code> may by <code>null</code>.
     *
     * @param model
     *            the initial, updated, or replaced input model, may be <code>null</code>
     * @param theUpdateStrategy
     *            the updateStrategy to use during this update, must not be <code>null</code>
     * @return <code>true</code> if view update succeeded, <code>false</code> otherwise
     */
    public boolean update(final Object model, final IUpdateStrategy theUpdateStrategy) {
        return update(model, theUpdateStrategy, KlighdSynthesisProperties.emptyConfig());
    }

    /**
     * Executes the {@link #diagramSynthesis} attached to <code>this</code> view context and updates
     * the view model by applying the configured {@link IUpdateStrategy}. In case the formerly set
     * input/source model has been replaced by a new one of compatible type this new one must be
     * provided, otherwise <code>model</code> may by <code>null</code>.
     *
     * @param model
     *            the initial, updated, or replaced input model, may be <code>null</code>
     * @param theUpdateStrategy
     *            the updateStrategy to use during this update, must not be <code>null</code>
     * @param properties
     *            a property holder that might influence the diagram update in case type of
     *            <code>model</code> differs from the current business model's type
     * @return <code>true</code> if view update succeeded, <code>false</code> otherwise
     */
    public boolean update(final Object model, final IUpdateStrategy theUpdateStrategy,
            final IPropertyHolder properties) {
        
        childViewContexts.clear();

        if (model != null && model != this.businessModel) {
            this.businessModel = model;

            if (this.diagramSynthesis == null
                    || !synthesisMatches(this.diagramSynthesis, this.businessModel)) {
                this.configure(properties);
            }
        }

        if (properties != null) {
            final Map<SynthesisOption, Object> config =
                    properties.getProperty(KlighdSynthesisProperties.SYNTHESIS_OPTION_CONFIG);

            if (config != null) {
                for (final Map.Entry<SynthesisOption, Object> entry : config.entrySet()) {
                    this.configureOption(entry.getKey(), entry.getValue());
                }
            }
        }

        final KNode newViewModel;
        final Object sourceModel = this.businessModel;

        final IUpdateStrategy chosenUpdateStrategy =
                theUpdateStrategy != null ? theUpdateStrategy : this.updateStrategy;

        if (!chosenUpdateStrategy.requiresDiagramSynthesisReRun(this)) {
            newViewModel = this.viewModel;
            
        } else if (this.diagramSynthesis != null) {
            final ISynthesis synthesis = this.diagramSynthesis;

            try {
                if (sourceModel instanceof ISourceProxy) {
                    newViewModel = ((ISourceProxy) sourceModel).execute(new Function<Object, KNode>() {

                        public KNode apply(final Object input) {
                            return synthesis.transform(input, ViewContext.this);
                        }
                    });

                } else {
                    newViewModel = synthesis.transform(sourceModel, ViewContext.this);
                }

            } catch (final Exception e) {
                final String msg = "KLighD: Execution of diagram synthesis "
                        + synthesis.getClass().getCanonicalName().toString()
                        + " failed for input model " + sourceModel.toString() + ".";

                Klighd.handle(
                        new Status(IStatus.ERROR, Klighd.PLUGIN_ID, msg, e));
                return false;
            }

        } else if (sourceModel instanceof KNode) {
            if (this.duplicator == null) {
                this.duplicator = new DuplicatingDiagramSynthesis();
            }

            newViewModel = duplicator.transform(sourceModel, this);

        } else {
            final String msg = "KLighD: Could not create a diagram of provided input model "
                    + sourceModel + ".";

            Klighd.handle(
                    new Status(IStatus.WARNING, Klighd.PLUGIN_ID, msg));
            return false;
        }

        chosenUpdateStrategy.update(this.viewModel, newViewModel, this);

        final KNode clipNode = this.getProperty(KlighdProperties.CLIP);
        if (clipNode != null && this.getViewer() != null) {
            this.getViewer().clip(clipNode);
        }

        return true;
    }

    /**
     * @param synthesis
     *            the {@link ISynthesis} to check, must not be <code>null</code>
     * @param model
     *            the source model to check <code>synthesis</code> for match
     * @return <code>true</code> if <code>synthesis</code> matches <code>model</code>
     */
    private boolean synthesisMatches(final ISynthesis synthesis, final Object model) {
        final Class<?> inputType = synthesis.getInputDataType();

        if (model instanceof ISourceProxy) {
            return ((ISourceProxy) model).execute(new Function<Object, Boolean>() {

                public Boolean apply(final Object theModel) {
                    return inputType.isAssignableFrom(theModel.getClass());
                }
            });
        } else {
            return inputType.isAssignableFrom(model.getClass());
        }
    }

    /**
     * Returns the synthesis being applied.
     *
     * @return the synthesis being applied
     */
    public ISynthesis getDiagramSynthesis() {
        return diagramSynthesis;
    }

    /**
     * Returns the current model to be represented, can be an {@link ISourceProxy},
     * see documentation of that interface for details.
     *
     * @return the current model to be represented.
     */
    public Object getInputModel() {
        return this.businessModel;
    }

    /**
     * Returns the update strategy used in this view context.
     *
     * @return the update strategy
     */
    public IUpdateStrategy getUpdateStrategy() {
        return updateStrategy;
    }

    /**
     * Returns the view model root, which is kept for the whole life-cycle of the view context, in
     * order to enable proper incremental update.
     *
     * @return the view model
     */
    public KNode getViewModel() {
        return viewModel;
    }

    /**
     *  @return the {@link IViewer} being in charge of showing this {@link ViewContext}.
     */
    public IViewer getViewer() {
        return viewer;
    }

    /**
     * @return the {@link ILayoutRecorder} being in charge of recording the layout for proper animation
     */
    public ILayoutRecorder getLayoutRecorder() {
        return layoutRecorder;
    }

    /**
     * @return the zoomStyle
     */
    public ZoomStyle getZoomStyle() {
        return zoomStyle;
    }

    /**
     * @return whether the zoom style is zoom to fit.
     */
    public boolean isZoomToFit() {
        return zoomStyle == ZoomStyle.ZOOM_TO_FIT;
    }

    /**
     * @return whether the zoom style is zoom to fit content.
     */
    public boolean isZoomToFitContent() {
        return zoomStyle == ZoomStyle.ZOOM_TO_FIT_CONTENT;
    }

    /**
     * Keep in mind that zoom to fit has a higher priority, thus
     * this can only return false if {@link #isZoomToFit()} returns false.
     *
     * @return whether the zoom style is zoom to focus.
     */
    public boolean isZoomToFocus() {
        return !isZoomToFit() && !isZoomToFitContent() && zoomStyle == ZoomStyle.getZoomToFocusStyle();
    }
    
    /**
     * @return whether the zoom style is zoom to stay.
     */
    public boolean isZoomToStay() {
        return zoomStyle == ZoomStyle.ZOOM_TO_STAY;
    }

    /**
     * @param zoomStyle the zoomStyle to set
     */
    public void setZoomStyle(final ZoomStyle zoomStyle) {
        this.zoomStyle = zoomStyle;
    }


    // ---------------------------------------------------------------------------------- //
    //  Source target element handling

    /**
     * Associates the given <code>source</code> element, which should be part of the input model,
     * and the given <code>target</code> element, which is assumed to be part of the view model.
     *
     * @param source
     *            a member of the input model, may be <code>null</code>
     * @param target
     *            a member of the view model, must not by <code>null</code>
     */
    public void associateSourceTargetPair(final Object source, final EObject target) {
        if (KGraphPackage.eINSTANCE.getKGraphData().isInstance(target)) {
            ((KGraphData) target).setProperty(KlighdInternalProperties.MODEL_ELEMEMT, source);

        } else if (KGraphPackage.eINSTANCE.getKGraphElement().isInstance(target)) {
            ((KGraphElement) target).setProperty(KlighdInternalProperties.MODEL_ELEMEMT, source);
        }
    }

    /**
     * Returns the element in the input model that is represented by the given <code>viewElement</code>
     * in the diagram.<br>
     * <b>Note:</b> This method does not check whether <code>viewElement</code> is currently contained
     * in the view model (accessible via {@link #getViewModel()}).
     *
     * @param viewElement
     *            the diagram element whose source element in the input (source, semantic, or
     *            business) model is requested
     * @return the element in the input model or <code>null</code> if no source element could be
     *         identified
     */
    public Object getSourceElement(final EObject viewElement) {
        if (viewElement == null) {
            return null;
        }

        return tracer.getSourceElement(viewElement);
    }


    /**
     * Returns the elements in the view model that represent the given <code>element</code> in the
     * diagram.<br>
     * <b>Note:</b> This method does not check whether <code>element</code> is currently contained
     * in the input model being represented (accessible via {@link #getInputModel()}).
     *
     * @param element
     *            the object in the input (source, semantic, or business) model
     * @return a {@link Collection} of diagram elements representing the given <code>element</code>
     *         or <code>{@link Collections#emptyList()}</code> if no corresponding view model
     *         elements could be identified
     */
    public Collection<EObject> getTargetElements(final Object element) {
        if (element == null) {
            return Collections.emptyList();
        }

        return tracer.getTargetElements(element);
    }


    /**
     * Returns the elements in the view model that represent the given <code>element</code> in the
     * diagram.<br>
     * Since multiple view elements can be associated with an input model element, there are most
     * likely multiple view model elements. Thus the method returns the first one, or the first one of
     * type <code>ofType</code> if that parameter is non-<code>null</code>, or <code>null</code> if
     * there isn't any corresponding element (of type <code>ofType</code>).<br>
     * <b>Note:</b> This method does not check whether <code>element</code> is currently contained
     * in the input model being represented (accessible via {@link #getInputModel()}).
     *
     * @param <T>
     *            the type of denoted by <code>type</code>
     * @param element
     *            the element in the source model
     * @param ofType
     *            the type of the requested view model element, maybe <code>null</code>
     * @return <code>element</code>'s representation in the context's view model or
     *         <code>null</code> if <code>element</code> is <code>null</code> of none exists
     */
    public <T extends EObject> T getTargetElement(final Object element, final Class<T> ofType) {
        if (element == null) {
            return null;
        }

        final Collection<EObject> targetCollection = getTargetElements(element);

        if (targetCollection == null || targetCollection.isEmpty()) {
            return null;
        } else {
            if (ofType == null) {
                @SuppressWarnings("unchecked")
                final T res = (T) Iterables.getFirst(targetCollection, null);
                return res;
            } else {
                return Iterables.getFirst(Iterables.filter(targetCollection, ofType), null);
            }
        }
    }


    // ---------------------------------------------------------------------------------- //
    //  Synthesis option handling

    private List<SynthesisOption> synthesisOptions = Lists.newLinkedList();

    /** Memory of the configured transformation options to be evaluated by the transformation. */
    private Map<SynthesisOption, Object> synthesisOptionConfig = Maps.newHashMap();


    /**
     * Returns the set of {@link SynthesisOption TransformationOptions} declared by the
     * transformation and forward to the users in the UI in order to allow them to influence the
     * transformation result.
     *
     * @return the set of {@link SynthesisOption TransformationOptions}
     *
     * @author chsch
     */
    public List<SynthesisOption> getDisplayedSynthesisOptions() {
        return this.synthesisOptions;
    }

    /**
     * Getter.
     *
     * @param option the option to evaluate the configuration state / the configured value.
     * @return the configured value of {@link SynthesisOption} option.
     */
    public Object getOptionValue(final SynthesisOption option) {

        if (option == null) {
            throw new IllegalArgumentException("KLighD transformation option handling: "
                    + "The transformation " + this.diagramSynthesis
                    + " attempted to evaluate the transformation option \"null\".");
        }
        if (this.synthesisOptionConfig.containsKey(option)) {
            return this.synthesisOptionConfig.get(option);
        } else {
            return option.getInitialValue();
        }
    }

    /**
     *
     * @param option
     *            the {@link SynthesisOption} to set
     * @param value
     *            the value of the {@link SynthesisOption}
     */
    public void configureOption(final SynthesisOption option, final Object value) {

        // Configuring separator "pseudo" options is senseless and
        //  since those options are not required to be singleton object
        //  their re-configuration will even fail!
        // Thus we skip them here.
        if (option != null && option.isSeparator()) {
            return;
        }

        // This check is weakened since the condition '|| !this.synthesisOptions.contains(option)'
        // was removed to support pre-configuration of synthesis options of child view contexts
        if (option == null) {
            throw new IllegalArgumentException("KLighD transformation option handling: "
                    + "Attempted to configure illegal option ("
                    + (option == null ? null : option.getName())
                    + "), which is not introduced by the transformation " + this.diagramSynthesis
                    + ".");
        }

        if (value == null) {
            synthesisOptionConfig.remove(option);
        } else {
            synthesisOptionConfig.put(option, value);
        }
    }

    // ---------------------------------------------------------------------------------- //
    //  Offered action handling
    
    /**
     * Passes the recommended layout options and related values provided by the employed diagram
     * synthesis.
     *
     * @return a map of options (map keys) and related values (map values)
     */
    public List<DisplayedActionData> getDisplayedActions() {
        if (this.diagramSynthesis != null) {
            return this.diagramSynthesis.getDisplayedActions();
        } else {
            return Collections.emptyList();
        }
    }

    // ---------------------------------------------------------------------------------- //
    //  Recommended layout option handling

    /**
     * Passes the recommended layout options and related values provided by the employed diagram
     * synthesis.
     *
     * @return a map of options (map keys) and related values (map values)
     */
    public List<Pair<IProperty<?>, List<?>>> getDisplayedLayoutOptions() {
        if (this.diagramSynthesis != null) {
            return this.diagramSynthesis.getDisplayedLayoutOptions();
        } else {
            return Collections.emptyList();
        }
    }

    // ---------------------------------------------------------------------------------- //
    //  Additional layout configuration handling

    /**
     * Passes the registered additional layout configurations provided by the employed diagram
     * synthesis.
     *
     * @return a map of options (map keys) and related values (map values)
     */
    public List<? extends IGraphElementVisitor> getAdditionalLayoutConfigs() {
        if (this.diagramSynthesis != null) {
            return this.diagramSynthesis.getAdditionalLayoutConfigs(viewModel, this);
        } else {
            return Collections.emptyList();
        }
    }
    

    // ---------------------------------------------------------------------------------- //
    //  Child ViewContext handling

    private final List<ViewContext> childViewContexts = Lists.newLinkedList();

    /**
     * Returns the list of registered child {@link ViewContext}.<br>
     * A child is a {@link ViewContext} which was created to translate a model with another
     * synthesis, which result is contained in the view model.
     * 
     * @param recursive
     *            if recursive is set true the list will also contain all nested child
     *            {@link ViewContext}s.
     * 
     * @return list of child ViewContext
     */
    public List<ViewContext> getChildViewContexts(final boolean recursive) {
        if (recursive) {
            List<ViewContext> recursiveVCs = Lists.newLinkedList();
            for (ViewContext vc : childViewContexts) {
                recursiveVCs.add(vc);
                recursiveVCs.addAll(vc.getChildViewContexts(true));
            }
            return recursiveVCs;
        } else {
            return childViewContexts;
        }
    }

    /**
     * Adds a {@link ViewContext} to the list of child {@link ViewContext}s.<br>
     * The list will be cleared on each update.
     * 
     * @param child
     *            the new child {@link ViewContext}.
     */
    public void addChildViewContext(final ViewContext child) {
        this.childViewContexts.add(child);
    }
}
