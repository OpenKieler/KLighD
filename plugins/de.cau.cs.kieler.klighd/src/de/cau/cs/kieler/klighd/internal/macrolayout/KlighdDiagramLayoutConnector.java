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
package de.cau.cs.kieler.klighd.internal.macrolayout;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.elk.core.math.KVector;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.service.IDiagramLayoutConnector;
import org.eclipse.elk.core.service.LayoutMapping;
import org.eclipse.elk.core.util.ElkUtil;
import org.eclipse.elk.graph.ElkBendPoint;
import org.eclipse.elk.graph.ElkConnectableShape;
import org.eclipse.elk.graph.ElkEdge;
import org.eclipse.elk.graph.ElkEdgeSection;
import org.eclipse.elk.graph.ElkGraphElement;
import org.eclipse.elk.graph.ElkGraphFactory;
import org.eclipse.elk.graph.ElkLabel;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.ElkPort;
import org.eclipse.elk.graph.ElkShape;
import org.eclipse.elk.graph.properties.IProperty;
import org.eclipse.elk.graph.properties.IPropertyHolder;
import org.eclipse.elk.graph.properties.Property;
import org.eclipse.elk.graph.util.ElkGraphSwitch;
import org.eclipse.elk.graph.util.ElkGraphUtil;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import de.cau.cs.kieler.klighd.IViewer;
import de.cau.cs.kieler.klighd.KlighdConstants;
import de.cau.cs.kieler.klighd.KlighdDataManager;
import de.cau.cs.kieler.klighd.KlighdOptions;
import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.internal.ILayoutRecorder;
import de.cau.cs.kieler.klighd.internal.util.KlighdInternalProperties;
import de.cau.cs.kieler.klighd.kgraph.KEdge;
import de.cau.cs.kieler.klighd.kgraph.KGraphElement;
import de.cau.cs.kieler.klighd.kgraph.KGraphFactory;
import de.cau.cs.kieler.klighd.kgraph.KGraphPackage;
import de.cau.cs.kieler.klighd.kgraph.KIdentifier;
import de.cau.cs.kieler.klighd.kgraph.KInsets;
import de.cau.cs.kieler.klighd.kgraph.KLabel;
import de.cau.cs.kieler.klighd.kgraph.KLayoutData;
import de.cau.cs.kieler.klighd.kgraph.KNode;
import de.cau.cs.kieler.klighd.kgraph.KPoint;
import de.cau.cs.kieler.klighd.kgraph.KPort;
import de.cau.cs.kieler.klighd.kgraph.KShapeLayout;
import de.cau.cs.kieler.klighd.kgraph.util.KGraphUtil;
import de.cau.cs.kieler.klighd.krendering.KRendering;
import de.cau.cs.kieler.klighd.krendering.KRenderingFactory;
import de.cau.cs.kieler.klighd.krendering.KRenderingOptions;
import de.cau.cs.kieler.klighd.krendering.KRenderingRef;
import de.cau.cs.kieler.klighd.krendering.KRenderingUtil;
import de.cau.cs.kieler.klighd.krendering.KText;
import de.cau.cs.kieler.klighd.labels.management.LabelManagementResult;
import de.cau.cs.kieler.klighd.microlayout.Bounds;
import de.cau.cs.kieler.klighd.microlayout.PlacementUtil;
import de.cau.cs.kieler.klighd.util.KlighdPredicates;
import de.cau.cs.kieler.klighd.util.KlighdProperties;
import de.cau.cs.kieler.klighd.util.KlighdSynthesisProperties;
import de.cau.cs.kieler.klighd.util.ModelingUtil;
import de.cau.cs.kieler.klighd.util.RenderingContextData;

/**
 * A diagram layout connector for KLighD viewers that supports instances of {@link KNode}, as well as
 * the parts and viewers provided by KLighD.
 * 
 * <p>If the {@link KNode} instances have attached {@code KRendering} data the manager uses them to
 * compute the node insets as well as the minimal node size.</p>
 * 
 * <p><b>Note:</b> During the {@link #applyLayout(LayoutMapping)} phase layout data that have been
 * scaled according to a corresponding {@link LayoutOptions#SCALE_FACTOR} are normalized to scaling
 * <code>1.0f</code>, since the scaling is implemented on figure level by means of affine
 * transforms. In addition, the scale adjustment need not be reverted before the subsequent layout
 * run.</p>
 *
 * @author mri
 * @author chsch
 * @author msp
 * @author cds
 */
public class KlighdDiagramLayoutConnector implements IDiagramLayoutConnector {

    /**
     * A dummy value used in fired {@link Notification Notifications} indicating a completed update
     * of a {@link KLayoutData} instance.
     */
    public static final Object LAYOUT_DATA_CHANGED_VALUE = new Object();

    /**
     * A dummy value used in fired {@link Notification Notifications} indicating a completed update
     * of a {@link KLayoutData} instance.
     */
    public static final Object LAYOUT_DATA_UNCHANGED_VALUE = new Object();

    /** the list of edges found in the graph. */
    protected static final IProperty<List<KEdge>> EDGES =
            new Property<>("krendering.layout.edges");
    /** edges that have been excluded from the layout. */
    protected static final IProperty<List<KEdge>> EXCLUDED_EDGES =
            new Property<>("krendering.layout.excludedEdges");

    /**
     * Static predicate definition avoiding the recurring creation and disposal of instances of the
     * filter predicate.
     */
    protected static final Predicate<KNode> NODE_FILTER =
            Predicates.and(
                    RenderingContextData.IS_ACTIVE,
                    KlighdPredicates.kgePropertyPredicate(CoreOptions.NO_LAYOUT, false, true));
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Layout Graph Building
    
    /**
     * {@inheritDoc}
     */
    @Override
    public LayoutMapping buildLayoutGraph(final Object diagramPart) {
        final KNode viewModel;
        final ViewContext viewContext;

        // search for the root node
        if (diagramPart instanceof KNode) {
            viewContext = null;
            viewModel = (KNode) diagramPart;
        } else if (diagramPart instanceof ViewContext) {
            viewContext = (ViewContext) diagramPart;
            viewModel = viewContext.getViewModel();
        } else if (diagramPart instanceof IViewer) {
            viewContext = ((IViewer) diagramPart).getViewContext();
            viewModel = viewContext.getViewModel();
        } else  {
            viewContext = null;
            viewModel = null;
        }

        // if no root node could be found
        if (viewModel == null) {
            throw new UnsupportedOperationException(
                    "Not supported by the KLighD KRendering layout manager: Diagram part " + diagramPart);
        }

        final boolean performSizeEstimation = viewContext == null
                ? true : !viewContext.getProperty(KlighdSynthesisProperties.SUPPRESS_SIZE_ESTIMATION);

        // create the mapping
        final LayoutMapping mapping = buildLayoutGraph(
                viewModel, performSizeEstimation);

        if (viewContext != null) {            
            // remember the layout recorder if any
            mapping.setProperty(KlighdInternalProperties.RECORDER, viewContext.getLayoutRecorder());
        }
        
        return mapping;
    }

    /**
     * Builds a layout graph from the given graph.
     *
     * @param viewModel
     *            the graph to build the layout graph from
     * @param performSizeEstimation
     *            whether the size of nodes & labels should be automatically estimated.
     * @return the layout graph mapping
     */
    public LayoutMapping buildLayoutGraph(final KNode viewModel,
            final boolean performSizeEstimation) {
        
        final LayoutMapping mapping = new LayoutMapping();
        mapping.setProperty(EDGES, new LinkedList<KEdge>());

        // set the parent element
        mapping.setParentElement(viewModel);

        final ElkNode layoutGraph = ElkGraphUtil.createGraph();
        shapeLayoutToLayoutGraph(viewModel, layoutGraph);

        mapping.getGraphMap().put(layoutGraph, viewModel);
        mapping.setLayoutGraph(layoutGraph);

        // traverse the children of the layout root
        processNodes(mapping, viewModel, layoutGraph, performSizeEstimation);
        // transform all connections in the selected area
        processConnections(mapping, performSizeEstimation);

        return mapping;
    }
    
    /**
     * Processes all child nodes of the given parent node.
     *
     * @param mapping
     *            the layout mapping
     * @param viewModelParent
     *            the parent node in the view model
     * @param layoutParent
     *            the layout parent node
     * @param performSizeEstimation
     *            whether the size of nodes & labels should be automatically estimated.
     */
    protected void processNodes(final LayoutMapping mapping, final KNode viewModelParent,
            final ElkNode layoutParent, final boolean performSizeEstimation) {
        
        // iterate through the parent's active children and put copies in the layout graph;
        //  a child is active if it contains RenderingContextData and the 'true' value wrt.
        //  the property KlighdConstants.ACTIVE, see the predicate definition above
        // furthermore, all nodes that have the LAYOUT_IGNORE property set are ignored
        for (final KNode node : Iterables.filter(viewModelParent.getChildren(), NODE_FILTER)) {
            createNode(mapping, node, layoutParent, performSizeEstimation);
        }
    }

    /**
     * An internal property attached to a node's shape layout when the size of the node is affected
     * by ELK. It is used to decide which value to rely on for the minimal node size.
     */
    private static final IProperty<Boolean> INITIAL_NODE_SIZE = new Property<Boolean>(
            "klighd.initialNodeSize", true);

    /**
     * Creates a layout node for the node inside the given layout parent node.
     *
     * @param mapping
     *            the layout mapping
     * @param node
     *            the node
     * @param layoutParent
     *            the layout parent node
     * @param performSizeEstimation
     *            whether the size of the node, its labels, and its attached ports' and edges'
     *            labels should be automatically estimated.
     */
    private void createNode(final LayoutMapping mapping, final KNode node,
            final ElkNode layoutParent, final boolean performSizeEstimation) {
        
        final ElkNode layoutNode = ElkGraphUtil.createNode(layoutParent);
        
        KIdentifier id = node.getData(KIdentifier.class);
        if (id != null && !Strings.isNullOrEmpty(id.getId())) {
            layoutNode.setIdentifier(id.getId());
        }

        // first check whether children of 'node' shall be taken into account at all.
        // note that all KNodes of a view model are set 'POPULATED' except the
        //  explicitly (e.g. initially) collapsed ones.
        // note the special implementation of 'IS_POPULATED' in case no RenderingContextData
        //  are attached to 'node'; the predicate returns 'true' in that particular case.
        // this is required for applying layout to view models that aren't shown by a viewer
        //  and whose (compound) nodes are not tagged to be 'populated'. This may happen in
        //  batch tests, for example.
        final boolean isPopulated = RenderingContextData.IS_POPULATED.apply(node);

        // determine the corresponding rendering
        final Predicate<KRendering> filter = isPopulated ? KlighdPredicates.isExpandedRendering()
                : Predicates.not(KlighdPredicates.isExpandedRendering());
        // apply the class-based filter, than the above defined 'filter'
        //  if none is found, just take the first KRendering in the 'data' list
        final KRendering displayedRendering = Iterators.find(
                Iterators.filter(node.getData().iterator(), KRendering.class),
                filter, node.getData(KRendering.class));

        // consider 'node' a compound node if it is populated AND has active children
        //  will be false if all children are inactive and not added to the layout graph later on
        final boolean isCompoundNode = isPopulated
                && Iterables.any(node.getChildren(), RenderingContextData.IS_ACTIVE);
        
        // there is layoutData attached to the node,
        // so take that as node layout instead of the default-layout
        shapeLayoutToLayoutGraph(node, layoutNode);

        // In the following the minimal width and height of the node is determined, which
        //  is used as a basis for the size estimation (necessary for grid-based micro layouts).

        // We start with standard minimal bounds given in the related constant.
        Bounds minSize = Bounds.of(KlighdConstants.MINIMAL_NODE_BOUNDS);
        // check the definition of the minimal size property
        final boolean minNodeSizeIsSet = node.getProperties().containsKey(
                KlighdProperties.MINIMAL_NODE_SIZE);

        if (minNodeSizeIsSet) {
            // if the minimal node size is given in terms of the dedicated property, use its values
            minSize = Bounds.of(node.getProperty(KlighdProperties.MINIMAL_NODE_SIZE));
        } else if (!isCompoundNode || node.getProperty(INITIAL_NODE_SIZE)) {
            // otherwise, if the node is a non-compound one or the size is not yet modified by KIML
            //  take the component-wise maximum of the standard bounds and 'nodelayout's values
            minSize = Bounds.max(minSize, Bounds.of(node.getWidth(), node.getHeight()));
        }

        // explicitly store the determined minimal node size in the layout data of the node
        //  note that this information will be removed or overwritten by the update strategies
        final boolean deliver = node.eDeliver();
        node.eSetDeliver(false);
        node.setProperty(KlighdProperties.MINIMAL_NODE_SIZE,
                new KVector(minSize.getWidth(), minSize.getHeight()));
        node.eSetDeliver(deliver);

        // compute insets required by micro layout
        // remember these insets, we need to subtract them again when transferring the layout
        KInsets insets = KGraphFactory.eINSTANCE.createKInsets();

        // if a rendering definition is given ...
        if (displayedRendering != null) {
            // ... calculate the minimal required size based on the determined 'minSize' bounds
            final Bounds size;
            if (performSizeEstimation) {
                size = Bounds.max(minSize, PlacementUtil.estimateSize(displayedRendering, minSize));
            } else {
                size = minSize;
            }

            PlacementUtil.calculateInsets(displayedRendering, insets, size);

            // integrate the minimal estimated node size
            // in case of a compound node, the minimal node size to be preserved by KIML must be
            // handed over by means of the MIN_WIDTH/MIN_HEIGHT properties
            // in case of non-compound nodes with SizeConstraint::MINIMUM_SIZE set, the property
            // definitions are also relevant
            final KVector theMinSize = new KVector(size.getWidth(), size.getHeight());
            node.setProperty(CoreOptions.NODE_SIZE_MINIMUM, theMinSize);
            if (!isCompoundNode) {
                // in case of non-compound nodes the node size is usually taken from the
                // layoutLayout
                layoutNode.setDimensions(size.getWidth(), size.getHeight());
            }
        } else {
            PlacementUtil.calculateInsets(displayedRendering, insets, minSize);
        }

        // KLighD is somewhat mean and doesn't care about existing insets
        node.setInsets(insets);
        node.getProperty(CoreOptions.PADDING);
        // The Insets are used in {@link KlighdLayoutConfigurationStore} to retrieve the padding
        // of the node

        layoutParent.getChildren().add(layoutNode);
        mapping.getGraphMap().put(layoutNode, node);

        // process ports
        for (final KPort port
                : Iterables.filter(node.getPorts(), RenderingContextData.IS_ACTIVE)) {
            
            createPort(mapping, port, layoutNode, performSizeEstimation);
        }

        // process labels
        for (final KLabel label
                : Iterables.filter(node.getLabels(), RenderingContextData.IS_ACTIVE)) {
            
            createLabel(mapping, label, layoutNode, performSizeEstimation, false);
        }

        // process the child as new parent
        if (isCompoundNode) {
            processNodes(mapping, node, layoutNode, performSizeEstimation);
        }

        // store all the edges to process them later
        final List<KEdge> edges = mapping.getProperty(EDGES);
        Iterables.addAll(edges,
                Iterables.filter(node.getOutgoingEdges(), RenderingContextData.IS_ACTIVE));
    }

    /**
     * Creates a layout port for the port attached to the given layout node.
     *
     * @param mapping
     *            the layout mapping
     * @param port
     *            the port
     * @param layoutNode
     *            the layout node
     * @param estimateLabelSizes
     *            if <code>true</code> the minimal sizes of the attached {@link KLabel KLabels} will
     *            be estimated     */
    private void createPort(final LayoutMapping mapping, final KPort port,
            final ElkNode layoutNode, final boolean estimateLabelSizes) {
        
        final ElkPort layoutPort = ElkGraphUtil.createPort(layoutNode);
        KIdentifier id = port.getData(KIdentifier.class);
        if (id != null && !Strings.isNullOrEmpty(id.getId())) {
            layoutPort.setIdentifier(id.getId());
        }
        shapeLayoutToLayoutGraph(port, layoutPort);

        mapping.getGraphMap().put(layoutPort, port);

        // process labels
        for (final KLabel label : port.getLabels()) {
            createLabel(mapping, label, layoutPort, estimateLabelSizes, false);
        }
    }

    /**
     * Processes all edges collected while processing nodes.
     *
     * @param mapping
     *            the layout mapping
     * @param estimateLabelSizes
     *            if <code>true</code> the minimal sizes of the attached {@link KLabel KLabels} will
     *            be estimated
     */
    protected void processConnections(final LayoutMapping mapping,
            final boolean estimateLabelSizes) {
        
        final BiMap<Object, ElkGraphElement> graphMap = mapping.getGraphMap().inverse();

        // iterate through the list of collected edges
        final List<KEdge> edges = mapping.getProperty(EDGES);
        for (final KEdge edge : edges) {
            if (edge.getProperty(CoreOptions.NO_LAYOUT)) {
                List<KEdge> excludedEdges = mapping.getProperty(EXCLUDED_EDGES);
                
                if (excludedEdges == null) {
                    excludedEdges = new LinkedList<>();
                    mapping.setProperty(EXCLUDED_EDGES, excludedEdges);
                }
                
                excludedEdges.add(edge);
                continue;
            }
            
            // Find the edge's source and target shapes in the layout graph
            ElkConnectableShape layoutSource = null;
            if (edge.getSourcePort() != null) {
                layoutSource = (ElkConnectableShape) graphMap.get(edge.getSourcePort());
            } else {
                layoutSource = (ElkConnectableShape) graphMap.get(edge.getSource());
            }

            ElkConnectableShape layoutTarget = null;
            if (edge.getTargetPort() != null) {
                layoutTarget = (ElkConnectableShape) graphMap.get(edge.getTargetPort());
            } else {
                layoutTarget = (ElkConnectableShape) graphMap.get(edge.getTarget());
            }
            
            if (layoutSource != null && layoutTarget != null) {
                createEdge(mapping, edge, layoutSource, layoutTarget, estimateLabelSizes);
            }
        }
    }

    /**
     * Creates a layout edge for the edge between the given layout source node and layout target
     * node.
     *
     * @param mapping
     *            the layout mapping
     * @param edge
     *            the edge
     * @param layoutSource
     *            the layout source node
     * @param layoutTarget
     *            the layout target node
     * @param estimateLabelSizes
     *            if <code>true</code> the minimal sizes of the attached {@link KLabel KLabels} will
     *            be estimated
     */
    private void createEdge(final LayoutMapping mapping, final KEdge edge,
            final ElkConnectableShape layoutSource, final ElkConnectableShape layoutTarget,
            final boolean estimateLabelSizes) {
        
        final ElkEdge layoutEdge = ElkGraphUtil.createSimpleEdge(layoutSource, layoutTarget);

        KIdentifier id = edge.getData(KIdentifier.class);
        if (id != null && !Strings.isNullOrEmpty(id.getId())) {
            layoutEdge.setIdentifier(id.getId());
        }
        
        // set the edge layout
        edgeLayoutToLayoutGraph(edge, layoutEdge);

        // make sure to clear old junction points
        // the new layouter might not calculate any and we don't want
        // any floating junction points in the diagram
        edge.setProperty(CoreOptions.JUNCTION_POINTS, null);

        // delete the old EDGE_ROUTING return value
        // this is allowed as the EDGE_ROUTING directive to the layouter
        //  must be set on the parent of the KNode with the outgoing edge
        edge.setProperty(CoreOptions.EDGE_ROUTING, null);

        mapping.getGraphMap().put(layoutEdge, edge);

        // process labels
        for (final KLabel label
                : Iterables.filter(edge.getLabels(), RenderingContextData.IS_ACTIVE)) {
            
            createLabel(mapping, label, layoutEdge, estimateLabelSizes, true);
        }
    }

    /**
     * Creates a layout label for the label attached to the given labeled layout element.
     * 
     * @param mapping
     *            the layout mapping
     * @param label
     *            the label
     * @param layoutLabeledElement
     *            the labeled layout element
     * @param estimateSize
     *            if <code>true</code> the minimal size of the {@link KLabel} will be estimated
     * @param setFontLayoutOptions
     *            if <code>true</code> the layout options {@link LayoutOptions#FONT_NAME} and
     *            {@link LayoutOptions#FONT_SIZE} are set/updated on <code>kLabel</code>'s layout
     *            data as expected by Graphviz (dot) for properly sizing <i>edge</i> labels
     */
    private void createLabel(final LayoutMapping mapping, final KLabel label,
            final ElkGraphElement layoutLabeledElement, final boolean estimateSize,
            final boolean setFontLayoutOptions) {
        
        final KText kText = Iterators.getNext(
                Iterators.filter(
                        KRenderingUtil.selfAndAllChildren(label.getData(KRendering.class)),
                        KText.class),
                null);

        final String labelText =
                label.getText() != null ? label.getText()
                        : kText != null ? kText.getText() : "";

        final ElkLabel layoutLabel =
                ElkGraphUtil.createLabel(labelText, layoutLabeledElement);

        KIdentifier id = label.getData(KIdentifier.class);
        if (id != null && !Strings.isNullOrEmpty(id.getId())) {
            layoutLabel.setIdentifier(id.getId());
        }

        shapeLayoutToLayoutGraph(label, layoutLabel);

        // integrate the minimal estimated label size based on the updated layoutLayout
        // - manipulating the labelLayout may cause immediate glitches in the diagram
        // (through the listeners)
        final KRendering rootRendering = label.getData(KRendering.class);

        if (rootRendering != null) {
            if (estimateSize) {
                // calculate the minimal size need for the rendering ...
                final Bounds minSize = PlacementUtil.estimateSize(rootRendering, new Bounds(0, 0));
                
                final double minWidth = minSize.getWidth() > layoutLabel.getWidth()
                        ? minSize.getWidth()
                        : layoutLabel.getWidth();
                final double minHeight = minSize.getHeight() > layoutLabel.getHeight()
                        ? minSize.getHeight()
                        : layoutLabel.getHeight();
                
                // ... and update the label size if the calculated size is larger
                layoutLabel.setDimensions(minWidth, minHeight);
            }
            
            // XXX
            if (setFontLayoutOptions) {
                PlacementUtil.fontFor(label, true);
            }

            // attach a reference to the label's root rendering to the label so that our layout
            // algorithms know how to estimate text sizes.
            KRenderingRef rootRenderingRef = KRenderingFactory.eINSTANCE.createKRenderingRef();
            rootRenderingRef.setRendering(rootRendering);
            layoutLabel.setProperty(KRenderingOptions.K_RENDERING, rootRenderingRef);
        }

        mapping.getGraphMap().put(layoutLabel, label);
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Layout Application

    /**
     * {@inheritDoc}
     */
    @Override
    public void applyLayout(final LayoutMapping mapping, final IPropertyHolder settings) {
        // get the animation recorder if anyone has been attached above ...
        final ILayoutRecorder recorder = mapping.getProperty(KlighdInternalProperties.RECORDER);

        // ... and apply the layout
        if (recorder != null) {
            final IViewer viewer = (IViewer) recorder;
            final boolean suppressEdgeAdjustment = viewer.getViewContext().getProperty(
                    KlighdSynthesisProperties.SUPPRESS_EDGE_ADJUSTMENT);
            // XXX
//            if (viewer.getControl() != null && viewer.getControl().isDisposed()) {
//                return;
//            }
            recorder.startRecording();
            applyLayout(mapping, suppressEdgeAdjustment);
            recorder.stopRecording(calcAnimationTime(mapping, settings, false));
        } else {
            applyLayout(mapping, false);
        }
    }
    
    /**
     * Applies the computed layout back to the graph.
     *
     * @param mapping
     *            the layout mapping that was created by this manager
     * @param suppressEdgeAdjustment
     *            if true edge adjustment will be suppressed, if no
     *            edge adjustment will be done
     */
    protected void applyLayout(final LayoutMapping mapping, final boolean suppressEdgeAdjustment) {
        final Set<Entry<ElkGraphElement, Object>> elementMappings =
                mapping.getGraphMap().entrySet();
        
        // We need to process labels after the edges because during edge handling
        // the insets are handled and the source data adjusted accordingly.
        // We store the labels here to have them ready after the main switch.
        List<ElkLabel> graphLabels = Lists.newArrayList();
        
        // apply the layout of all mapped layout elements back to the associated element
        for (final Entry<ElkGraphElement, Object> elementMapping : elementMappings) {
            final ElkGraphElement layoutElement = elementMapping.getKey();
            
            // Since we built the layout graph, we know that this must be a KGraphElement
            final KGraphElement element = (KGraphElement) elementMapping.getValue();

            new ElkGraphSwitch<Boolean>() {
                @Override
                public Boolean caseElkNode(final ElkNode layoutNode) {
                    final KNode node = (KNode) element;
                    
                    // Get properties that shall be preserved from ElkGraph to KGraph
                    List<IProperty<?>> propertiesToPreserve = KlighdDataManager.getInstance().getPreservedProperties();
                    
                    // Preserve properties
                    for (IProperty<?> property : propertiesToPreserve) {
                        if (layoutNode.hasProperty(property)) {
                            node.setProperty((IProperty<Object>) property, (Object) layoutNode.getProperty(property));
                        }
                    }                  
                    
                    shapeToViewModel(mapping, layoutNode, node, true, true);
                    node.setProperty(INITIAL_NODE_SIZE, false);

                    // transfer the scale factor value since KIML might have reset it
                    //  to 1f in case scaling was not supported in the particular configuration
                    // and the figure scaling will be set according this property setting
                    node.setProperty(CoreOptions.SCALE_FACTOR,
                            layoutNode.getProperty(CoreOptions.SCALE_FACTOR));
                                        
                    return true;
                }

                @Override
                public Boolean caseElkEdge(final ElkEdge layoutEdge) {
                    KEdge edge = (KEdge) element;
                    edgeToViewModel(layoutEdge, edge, mapping, !suppressEdgeAdjustment);
                    
                    return true;
                }

                @Override
                public Boolean caseElkPort(final ElkPort layoutPort) {
                    final KPort port = (KPort) element;
                    
                    shapeToViewModel(mapping, layoutPort, port, false, true);
                    port.setProperty(KlighdProperties.LAYOUT_PORT_SIDE,
                            layoutPort.getProperty(CoreOptions.PORT_SIDE));
                    return true;
                }

                @Override
                public Boolean caseElkLabel(final ElkLabel layoutLabel) {
                    // Store the label for later use
                    graphLabels.add(layoutLabel);
                    return true;
                }
            } /**/.doSwitch(layoutElement);
        }

        // process the edges that have been excluded from layout
        final List<KEdge> excludedEdges = mapping.getProperty(EXCLUDED_EDGES);
        if (excludedEdges != null) {
            for (final KEdge edge : excludedEdges) {
                if (edge != null && edge.getTarget() != null)
                    handleExcludedEdge(edge);
            }
        }

        // Handle all the stored labels now, after edges have already been applied
        for (ElkLabel layoutLabel : graphLabels) {
            final KLabel label = (KLabel) mapping.getGraphMap().get(layoutLabel);
            
            shapeToViewModel(mapping, layoutLabel, label, false, true);
            
            // if the label's text was changed during layout, remember the new text in a
            // special property
            LabelManagementResult managementResult =
                    layoutLabel.getProperty(KlighdOptions.LABELS_MANAGEMENT_RESULT);
            if (managementResult != LabelManagementResult.UNMANAGED) {
                // TODO: This may in the future set the KText's text instead.
                // However, doing so now doesn't do anything yet...
                label.setProperty(KlighdOptions.LABELS_TEXT_OVERRIDE,
                        layoutLabel.getText());
                String origLabelText = label.getText();
                
                if (origLabelText.equals(layoutLabel.getText())) {
                    label.setProperty(KlighdProperties.TOOLTIP, null);
                } else {
                    label.setProperty(KlighdProperties.TOOLTIP, origLabelText);
                }
            }            
        }
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Layout Information Transfer

    /**
     * Transfers the source shape layout to the target shape layout.
     *
     * @param sourceShapeLayout
     *            the source shape layout
     * @param targetShape
     *            the target shape layout
     * @param copyInsets
     *            <code>true</code> if insets shall be copied
     */
    protected void shapeLayoutToLayoutGraph(
            final KShapeLayout sourceShapeLayout, final ElkShape targetShape) {

        // Attention: Layout options are transfered by the {@link KGraphPropertyLayoutConfig}
        targetShape.setLocation(sourceShapeLayout.getXpos(), sourceShapeLayout.getYpos());
        targetShape.setDimensions(sourceShapeLayout.getWidth(), sourceShapeLayout.getHeight());
    }

    /**
     * Transfers the source shape layout to the target shape layout.
     *
     * @param sourceShape
     *            the source shape layout
     * @param targetShapeLayout
     *            the target shape layout
     * @param copyPadding
     *            <code>true</code> if insets shall be copied
     * @param adjustScaling
     *            if <code>true</code> the <code>sourceShapeLayout</code>'s data will be adjusted
     *            s.t. the scaling of the corresponding node will be reverted to 100%, since the
     *            scaling is implemented by means of affine transforms on figure level.
     */
    private void shapeToViewModel(final LayoutMapping mapping,
            final ElkShape sourceShape, final KShapeLayout targetShapeLayout,
            final boolean copyPadding, final boolean adjustScaling) {

        // do not notify listeners about any change on the displayed KGraph in order
        //  to avoid unnecessary diagram refresh cycles
        final boolean deliver = targetShapeLayout.eDeliver();
        targetShapeLayout.eSetDeliver(false);
        targetShapeLayout.resetModificationFlag();

        // Parent insets need to be applied to coordinates
        KVector offset = new KVector();
        final ElkNode containingGraph = ElkGraphUtil.containingGraph(sourceShape);
        if (copyPadding && containingGraph != null) {
            KNode viewModelNode = (KNode) mapping.getGraphMap().get(containingGraph);
            KInsets parentInsets = viewModelNode.getInsets();
            if (parentInsets != null) {
                // offset 
                offset.add(-parentInsets.getLeft(), -parentInsets.getTop());
            }
        }
        
        if (adjustScaling && containingGraph != null) {
            
            if (sourceShape instanceof ElkNode) {
                final double scale = sourceShape.getProperty(CoreOptions.SCALE_FACTOR);
                targetShapeLayout.setPos(
                        (float) (sourceShape.getX() + offset.x),
                        (float) (sourceShape.getY() + offset.y));
                targetShapeLayout.setSize(
                        (float) (sourceShape.getWidth() / scale),
                        (float) (sourceShape.getHeight() / scale));
            } else if (sourceShape instanceof ElkPort || sourceShape instanceof ElkLabel) {
                final double scale = ModelingUtil.eContainerOfType(sourceShape, ElkNode.class).getProperty(CoreOptions.SCALE_FACTOR);
                targetShapeLayout.setPos(
                        (float) (sourceShape.getX() / scale),
                        (float) (sourceShape.getY() / scale));
                targetShapeLayout.setSize(
                        (float) (sourceShape.getWidth() / scale),
                        (float) (sourceShape.getHeight() / scale));

                final KVector anchor = targetShapeLayout.getProperty(CoreOptions.PORT_ANCHOR);
                if (anchor != null) {
                    anchor.x /= scale;
                    anchor.y /= scale;
                }
            }

        } else {
            targetShapeLayout.setPos(
                    (float) (sourceShape.getX() + offset.x),
                    (float) (sourceShape.getY() + offset.y));
            targetShapeLayout.setSize(
                    (float) sourceShape.getWidth(),
                    (float) sourceShape.getHeight());
        }

        // reactivate notifications & fire a notification
        //  bringing the observing diagram controller to update the displayed diagram
        targetShapeLayout.eSetDeliver(deliver);
        if (deliver) {
            // for efficiency reasons just fire a single notification with values indicating
            //  whether actually some change occurred in the shape layout
            // the information whether 'no change' happened is required for, e.g., updating the
            //  visibility of elements after the parent KNode has been expanded
            final Object newValue = targetShapeLayout.isModified()
                    ? LAYOUT_DATA_CHANGED_VALUE : LAYOUT_DATA_UNCHANGED_VALUE;

            targetShapeLayout.eNotify(new ENotificationImpl(
                    (InternalEObject) targetShapeLayout,
                    Notification.SET,
                    KGraphPackage.eINSTANCE.getKShapeLayout_Xpos(),
                    null,
                    newValue));
        }
    }

    /**
     * Transfers information from one edge layout to the other.
     *
     * @param viewModelEdgeLayout
     *            the view model edge layout
     * @param layoutEdge
     *            the layout edge layout
     */
    private void edgeLayoutToLayoutGraph(final KEdge viewModelEdge, final ElkEdge layoutEdge) {
        if (viewModelEdge.getSourcePoint() == null) {
            viewModelEdge.setSourcePoint(
                    KGraphFactory.eINSTANCE.createKPoint());
        }
        
        // We need to apply the effective parent padding that apply to the edge
        final KInsets parentPadding = effectivePaddingForEdge(viewModelEdge);
        
        // We need an edge section to work with (and only one)
        final ElkEdgeSection layoutEdgeSection = ElkGraphUtil.firstEdgeSection(
                layoutEdge, false, true);
        
        // transfer the source point without checking
        final KPoint sourcePoint = viewModelEdge.getSourcePoint();
        if (sourcePoint != null) {
            layoutEdgeSection.setStartLocation(
                    sourcePoint.getX(),
                    sourcePoint.getY());
        }

        // transfer the bend points, reusing any existing KPoint instances
        final ListIterator<KPoint> originBendIter = viewModelEdge.getBendPoints().listIterator();
        final ListIterator<ElkBendPoint> destBendIter =
                layoutEdgeSection.getBendPoints().listIterator();
        
        while (originBendIter.hasNext()) {
            final KPoint originPoint = originBendIter.next();
            ElkBendPoint destPoint;
            if (destBendIter.hasNext()) {
                destPoint = destBendIter.next();
            } else {
                destPoint = ElkGraphFactory.eINSTANCE.createElkBendPoint();
                destBendIter.add(destPoint);
            }
            
            destPoint.set(
                    originPoint.getX(),
                    originPoint.getY());
        }
        
        // remove any superfluous points
        while (destBendIter.hasNext()) {
            destBendIter.next();
            destBendIter.remove();
        }
        
        // transfer the target point without checking
        final KPoint targetPoint = viewModelEdge.getTargetPoint();
        if (targetPoint != null) {
            layoutEdgeSection.setEndLocation(
                    targetPoint.getX(),
                    targetPoint.getY());
        }
        
        // convert to the proper ELK coordinate system
        KGraphUtil.toELKGraphCoordinateSystem(layoutEdge, parentPadding);
    }

    /**
     * Returns the padding that apply to the coordinates of the given view model edge.
     * 
     * @param viewModelEdge the view model edge.
     * @return the effective padding.
     */
    private KInsets effectivePaddingForEdge(final KEdge viewModelEdge) {
        KNode relativeNode;
        if (KGraphUtil.isDescendant(viewModelEdge.getTarget(), viewModelEdge.getSource())) {
            relativeNode = viewModelEdge.getSource();
        } else {
            relativeNode = viewModelEdge.getSource().getParent();
        }
        
        return relativeNode.getInsets();
    }

    /**
     * Transfers information from one edge layout to the other.
     *
     * @param layoutEdgeLayout
     *            the layout edge layout
     * @param viewModelEdgeLayout
     *            the view model edge layout
     * @param adjustments
     */
    private void edgeToViewModel(final ElkEdge layoutEdge,
            final KEdge viewModelEdge, final LayoutMapping mapping, final boolean adjustments) {

        // Do not notify listeners about any change on the displayed KGraph in order
        // to avoid unnecessary diagram refresh cycles
        final boolean deliver = viewModelEdge.eDeliver();
        viewModelEdge.eSetDeliver(false);
        
        // adjust edge endpoints s.t. they touch the border of the node's/port's shape
        if (adjustments) {
            adjustEdgeEndpoints(layoutEdge, viewModelEdge);
        }
        
        // Find the KNode the view model edge coordinates are relative to
        KNode kgraphEdgeCoordinatesOrigin = KGraphUtil.containedGraph(viewModelEdge);
        
        // The coordinate systems of the layout and the view model edge can differ, so translate!
        KGraphUtil.toKGraphCoordinateSystem(layoutEdge, kgraphEdgeCoordinatesOrigin.getInsets());
        
        // we want to transfer the EDGE_ROUTING and JUNCTION_POINTS since those may contain
        // information produced by the layout algorithm
        viewModelEdge.setProperty(CoreOptions.EDGE_TYPE,
                layoutEdge.getProperty(CoreOptions.EDGE_TYPE));
        viewModelEdge.setProperty(CoreOptions.JUNCTION_POINTS,
                layoutEdge.getProperty(CoreOptions.JUNCTION_POINTS));
        
        final ElkEdgeSection layoutEdgeSection = layoutEdge.getSections().get(0);
        
        // - - - - - SOURCE POINT - - - - - 
        if (viewModelEdge.getSourcePoint() == null) {
            viewModelEdge.setSourcePoint(KGraphFactory.eINSTANCE.createKPoint());
        }
        final boolean sourcePointDeliver = viewModelEdge.getSourcePoint().eDeliver();
        viewModelEdge.getSourcePoint().eSetDeliver(false);
        viewModelEdge.getSourcePoint().setPos((float) layoutEdgeSection.getStartX(),
                (float) layoutEdgeSection.getStartY());
        viewModelEdge.getSourcePoint().eSetDeliver(sourcePointDeliver);
        
        // - - - - - BEND POINTS - - - - - 
        // transfer the bend points, reusing any existing KPoint instances
        final ListIterator<ElkBendPoint> originBendIter =
                layoutEdgeSection.getBendPoints().listIterator();
        final ListIterator<KPoint> destBendIter = viewModelEdge.getBendPoints().listIterator();
        while (originBendIter.hasNext()) {
            final ElkBendPoint originPoint = originBendIter.next();
            KPoint destPoint;
            if (destBendIter.hasNext()) {
                destPoint = destBendIter.next();
            } else {
                destPoint = KGraphFactory.eINSTANCE.createKPoint();
                destBendIter.add(destPoint);
            }
            destPoint.setPos((float) originPoint.getX(), (float) originPoint.getY());
        }
        
        // remove any superfluous points
        while (destBendIter.hasNext()) {
            destBendIter.next();
            destBendIter.remove();
        }
        
        // - - - - - TARGET POINT - - - - - 
        if (viewModelEdge.getTargetPoint() == null) {
            viewModelEdge.setTargetPoint(KGraphFactory.eINSTANCE.createKPoint());
        }
        final boolean targetPointDeliver = viewModelEdge.getTargetPoint().eDeliver();
        viewModelEdge.getTargetPoint().eSetDeliver(false);
        viewModelEdge.getTargetPoint().setPos((float) layoutEdgeSection.getEndX(),
                (float) layoutEdgeSection.getEndY());
        viewModelEdge.getTargetPoint().eSetDeliver(targetPointDeliver);

        // reactivate notifications & fire a notification
        //  bringing the observing diagram controller to update the displayed diagram
        viewModelEdge.eSetDeliver(deliver);
        ENotificationImpl notification = new ENotificationImpl(
                (InternalEObject) viewModelEdge, Notification.SET,
                KGraphPackage.KEDGE__BEND_POINTS, null, null);
        viewModelEdge.eNotify(notification);
    }
    
    /**
     * Adjust both endpoints of the passed edge s.t. they touch the border of the node's/port's
     * shape. The adjustment is done completely in elk's coordinate system. The passed
     * {@code viewModelEdge} is only used to retrieve the {@link KRendering}s of the source 
     * and target nodes and ports.
     */
    private void adjustEdgeEndpoints(final ElkEdge layoutEdge, final KEdge viewModelEdge) {

        ElkNode containingNode = layoutEdge.getContainingNode();
        ElkEdgeSection edgeSection = layoutEdge.getSections().get(0);

        ElkNode source = ElkGraphUtil.getSourceNode(layoutEdge);
        ElkPort sourcePort = ElkGraphUtil.getSourcePort(layoutEdge);
        ElkNode target = ElkGraphUtil.getTargetNode(layoutEdge);
        ElkPort targetPort = ElkGraphUtil.getTargetPort(layoutEdge);
        
        // - - - - - - - - - - - - SOURCE  - - - - - - - - - - - -
        KVector sourceVector = new KVector(edgeSection.getStartX(), edgeSection.getStartY());

        // compute source vector relative to source node
        ElkUtil.toAbsolute(sourceVector, containingNode);
        ElkUtil.toRelative(sourceVector, source);

        KRendering sourceRendering = viewModelEdge.getSource().getData(KRendering.class);
        KRendering sourcePortRendering = viewModelEdge.getSourcePort() == null 
                ? null
                : viewModelEdge.getSourcePort().getData(KRendering.class);

        boolean adjustSourcePortPosition = ElkGraphUtil.isDescendant(target, source);
        KVector adjustedSource = checkAndCopyPoint(sourceVector, source, 
                sourcePort,
                sourceRendering, sourcePortRendering, 
                adjustSourcePortPosition);

        // back to edge's coordinate system
        ElkUtil.toAbsolute(adjustedSource, source);
        ElkUtil.toRelative(adjustedSource, containingNode);

        edgeSection.setStartLocation(adjustedSource.x, adjustedSource.y);

        //  - - - - - - - - - - - - TARGET  - - - - - - - - - - - -
        KVector targetVector = new KVector(edgeSection.getEndX(), edgeSection.getEndY());

        // compute target vector relative to target node
        ElkUtil.toAbsolute(targetVector, containingNode);
        ElkUtil.toRelative(targetVector, target);

        KRendering targetRendering = viewModelEdge.getTarget().getData(KRendering.class);
        KRendering targetPortRendering = viewModelEdge.getTargetPort() == null 
                ? null
                : viewModelEdge.getTargetPort().getData(KRendering.class);

        boolean adjustTargetPortPosition = !adjustSourcePortPosition && !isSibling(source, target);
        KVector adjustedTarget = checkAndCopyPoint(targetVector, target, 
                targetPort,
                targetRendering, targetPortRendering, 
                adjustTargetPortPosition);

        // back to edge's coordinate system
        ElkUtil.toAbsolute(adjustedTarget, target);
        ElkUtil.toRelative(adjustedTarget, containingNode);

        edgeSection.setEndLocation(adjustedTarget.x, adjustedTarget.y);
    }
    
    /**
     * Determines whether the given two nodes are siblings, that is if they have the same parent
     * node. If they do not have a parent node they are not considered siblings.
     * 
     * @param node1 the first node.
     * @param node2 the second node.
     * @return {@code true} if the two nodes have the same non-{@code null} parent.
     */
    public static boolean isSibling(final ElkNode node1, final ElkNode node2) {
        return node1.getParent() == node2.getParent() && node1.getParent() != null;
    }

    /**
     * Check whether the given source point lies on the boundary of the corresponding {@code node}
     * or {@code port} and return a possibly altered point.
     *
     * @param originPoint
     *            the point from which to take the position
     * @param node
     *            the corresponding node
     * @param port
     *            the corresponding port, or {@code null}
     * @param nodeRendering
     *            the rendering of the corresponding node
     * @param portRendering
     *            the rendering of the corresponding port, or {@code null}
     * @param adjustPortPosAndSize
     *            if <code>true</code> the port's position and size will be adjusted 
     *            by the scale factor applied to its parent node
     * @return a new point that represents {@code originPoint}, possibly altered such that it lies
     *         on the boundary of {@code node} or {@code port}.
     */
    private KVector checkAndCopyPoint(final KVector originPoint,
            final ElkNode node, final ElkPort port, final KRendering nodeRendering,
            final KRendering portRendering, final boolean adjustPortPosAndSize) {

        // 'p' is relative to the 'node'
        KVector p = originPoint.clone();
        final double scale = node.getProperty(CoreOptions.SCALE_FACTOR);

        if (port == null) {
            p = AnchorUtil.nearestBorderPoint(p, node.getWidth(), node.getHeight(), nodeRendering, scale);
            return p;
        } else {
            KVector portPos = new KVector(port.getX(), port.getY());
            KVector portSize = new KVector(port.getWidth(), port.getHeight()); 
            if (adjustPortPosAndSize) {
                portPos.scale(1 / scale);
                portSize.scale(1 / scale);
            }
            // make 'p' relative to the port's top left corner
            p.sub(portPos);
             
            p = AnchorUtil.nearestBorderPoint(p, portSize.x, portSize.y, portRendering, scale);
            return p.add(portPos);
        }
    }

    /**
     * Handle the given edge that was excluded from layout. Set its source and
     * target points to appropriate positions on the border of the respective elements.
     *
     * @param edge an excluded edge
     */
    private void handleExcludedEdge(final KEdge edge) {
        boolean deliver = edge.eDeliver();
        edge.eSetDeliver(false);
        edge.getBendPoints().clear();

        final KNode sourceNode = edge.getSource();
        final KNode targetNode = edge.getTarget();
        final KPort sourcePort = edge.getSourcePort();
        final KPort targetPort = edge.getTargetPort();
        final boolean targetInSource = KGraphUtil.isDescendant(targetNode, sourceNode);

        // determine the source point
        final KVector sourcePoint = toElementBorder(sourceNode, sourcePort, targetNode, targetPort);
        if (targetInSource) {
            if (sourceNode.getInsets() != null) {
                sourcePoint.add(-sourceNode.getInsets().getLeft(),
                        -sourceNode.getInsets().getTop());
            }
        } else {
            sourcePoint.add(sourceNode.getXpos(), sourceNode.getYpos());
        }
        
        KPoint sourceKPoint = edge.getSourcePoint();
        if (sourceKPoint == null) {
            sourceKPoint = KGraphFactory.eINSTANCE.createKPoint();
            edge.setSourcePoint(sourceKPoint);
        }
        sourceKPoint.applyVector(sourcePoint);

        // determine the target point
        final KVector targetPoint = toElementBorder(targetNode, targetPort, sourceNode, sourcePort);
        targetPoint.add(targetNode.getXpos(), targetNode.getYpos());
        if (targetInSource) {
            KGraphUtil.toAbsolute(targetPoint, targetNode.getParent());
            KGraphUtil.toRelative(targetPoint, sourceNode);
        } else if (sourceNode.getParent() != targetNode.getParent()) {
            KGraphUtil.toAbsolute(targetPoint, targetNode.getParent());
            KGraphUtil.toRelative(targetPoint, sourceNode.getParent());
        }
        KPoint targetKPoint = edge.getTargetPoint();
        if (targetKPoint == null) {
            targetKPoint = KGraphFactory.eINSTANCE.createKPoint();
            edge.setTargetPoint(targetKPoint);
        }
        targetKPoint.applyVector(targetPoint);

        // notify the listeners
        edge.eSetDeliver(deliver);
        edge.eNotify(new ENotificationImpl((InternalEObject) edge, Notification.SET,
                KGraphPackage.KEDGE__BEND_POINTS, null, null));
    }

    /**
     * Find a point that lies close to the intersection of the direct line from the
     * given center element to the remote element with the center element's border.
     *
     * @param centerNode the center node
     * @param centerPort the center port, or {@code null}
     * @param remoteNode the remote node
     * @param remotePort the remote port, or {@code null}
     * @return the intersection with the center element's border
     */
    private KVector toElementBorder(final KNode centerNode,
            final KPort centerPort,
            final KNode remoteNode,
            final KPort remotePort) {

        KVector point = new KVector();
        if (remotePort == null) {
            point.x = remoteNode.getWidth() / 2;
            point.y = remoteNode.getHeight() / 2;
        } else {
            point.x = remotePort.getXpos() + remotePort.getWidth() / 2;
            point.y = remotePort.getYpos() + remotePort.getHeight() / 2;
        }
        point.add(remoteNode.getXpos(), remoteNode.getYpos());
        if (centerNode.getParent() != remoteNode.getParent()) {
            KGraphUtil.toAbsolute(point, remoteNode.getParent());
            KGraphUtil.toRelative(point, centerNode.getParent());
        }

        point.add(-centerNode.getXpos(), -centerNode.getYpos());
        if (centerPort == null) {
            point = AnchorUtil.collideTowardsCenter(
                    point,
                    centerNode.getWidth(),
                    centerNode.getHeight(),
                    centerNode.getData(KRendering.class));
        } else {
            point.add(-centerPort.getXpos(), -centerPort.getYpos());
            point = AnchorUtil.collideTowardsCenter(
                    point,
                    centerPort.getWidth(),
                    centerPort.getHeight(),
                    centerPort.getData(KRendering.class));
            point.add(centerPort.getXpos(), centerPort.getYpos());
        }

        return point;
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Animation Time

    /**
     * Calculates animation time for the given graph size. If the viewer is not visible,
     * the animation time is 0.
     * 
     * @param mapping a mapping of the layout graph
     * @param config the layout configurator from which to read animation settings, or {@code null}
     * @param viewerNotVisible whether the diagram viewer is currently not visible
     * @return number of milliseconds to animate, or 0 if no animation is desired
     */
    protected int calcAnimationTime(final LayoutMapping mapping, final IPropertyHolder settings,
            final boolean viewerNotVisible) {
        
        boolean animate = settings.getProperty(CoreOptions.ANIMATE);
        if (animate) {
            int minTime = settings.getProperty(CoreOptions.MIN_ANIM_TIME);
            if (minTime < 0) {
                minTime = 0;
            }
            int maxTime = settings.getProperty(CoreOptions.MAX_ANIM_TIME);
            if (maxTime < minTime) {
                maxTime = minTime;
            }
            int factor = settings.getProperty(CoreOptions.ANIM_TIME_FACTOR);
            if (factor > 0) {
                int graphSize = countNodes(mapping.getLayoutGraph());
                int time = minTime + (int) (factor * Math.sqrt(graphSize));
                return time <= maxTime ? time : maxTime;
            } else {
                return minTime;
            }
        }
        return 0;
    }

    /**
     * Counts the total number of children in the given node, including deep hierarchies.
     * 
     * @param node
     *            parent node
     * @return number of children and grandchildren in the given parent
     */
    private static int countNodes(final ElkNode node) {
        int count = 0;
        for (ElkNode child : node.getChildren()) {
            count += countNodes(child) + 1;
        }
        return count;
    }

}
