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
package de.cau.cs.kieler.klighd.eclipse.internal.marcrolayout;

import java.util.LinkedList;

import org.eclipse.elk.core.service.LayoutMapping;
import org.eclipse.elk.core.service.ui.EclipseLayoutMapping;
import org.eclipse.elk.graph.ElkGraphElement;
import org.eclipse.elk.graph.ElkLabel;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.properties.IProperty;
import org.eclipse.elk.graph.properties.IPropertyHolder;
import org.eclipse.elk.graph.properties.Property;
import org.eclipse.elk.graph.util.ElkGraphUtil;
import org.eclipse.ui.IWorkbenchPart;

import com.google.common.base.Strings;
import com.google.common.collect.Iterators;

import de.cau.cs.kieler.klighd.IViewer;
import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.eclipse.EclipseViewContext;
import de.cau.cs.kieler.klighd.eclipse.IDiagramWorkbenchPart;
import de.cau.cs.kieler.klighd.eclipse.IEclipseViewer;
import de.cau.cs.kieler.klighd.eclipse.microlayout.EclipsePlacementUtil;
import de.cau.cs.kieler.klighd.internal.ILayoutRecorder;
import de.cau.cs.kieler.klighd.internal.macrolayout.KlighdDiagramLayoutConnector;
import de.cau.cs.kieler.klighd.internal.util.KlighdInternalProperties;
import de.cau.cs.kieler.klighd.kgraph.KEdge;
import de.cau.cs.kieler.klighd.kgraph.KIdentifier;
import de.cau.cs.kieler.klighd.kgraph.KLabel;
import de.cau.cs.kieler.klighd.kgraph.KNode;
import de.cau.cs.kieler.klighd.krendering.KRendering;
import de.cau.cs.kieler.klighd.krendering.KRenderingFactory;
import de.cau.cs.kieler.klighd.krendering.KRenderingOptions;
import de.cau.cs.kieler.klighd.krendering.KRenderingRef;
import de.cau.cs.kieler.klighd.krendering.KRenderingUtil;
import de.cau.cs.kieler.klighd.krendering.KText;
import de.cau.cs.kieler.klighd.microlayout.Bounds;
import de.cau.cs.kieler.klighd.microlayout.PlacementUtil;
import de.cau.cs.kieler.klighd.util.KlighdSynthesisProperties;

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
public class EclipseKlighdDiagramLayoutConnector extends KlighdDiagramLayoutConnector {
    
    /**
     * A property that is used to tell KIML about the workbench part this layout manager is
     * responsible for. Note that this property is not referred to by KIML immediately, it rather
     * filters given property definitions by their value types and looks for one of
     * {@link IWorkbenchPart}.
     */
    private static final IProperty<IWorkbenchPart> WORKBENCH_PART = new Property<IWorkbenchPart>(
            "klighd.layout.workbenchPart");
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Layout Graph Building
    
    /**
     * {@inheritDoc}
     */
    public LayoutMapping buildLayoutGraph(final IWorkbenchPart workbenchPart, final Object diagramPart) {
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
        } else if (workbenchPart instanceof IDiagramWorkbenchPart) {
            viewContext = ((IDiagramWorkbenchPart) workbenchPart).getViewer().getViewContext();
            viewModel = viewContext.getViewModel();
        } else  {
            viewContext = null;
            viewModel = null;
        }

        // if no root node could be found
        if (viewModel == null) {
            throw new UnsupportedOperationException(
                    "Not supported by the KLighD KRendering layout manager: Workbench part "
                            + workbenchPart + ", diagram part " + diagramPart);
        }

        final boolean performSizeEstimation = viewContext == null
                ? true : !viewContext.getProperty(KlighdSynthesisProperties.SUPPRESS_SIZE_ESTIMATION);

        // create the mapping
        final LayoutMapping mapping = buildLayoutGraph(
                viewModel, performSizeEstimation, workbenchPart);

        if (viewContext != null) {
            mapping.setProperty(WORKBENCH_PART, ((EclipseViewContext) viewContext).getDiagramWorkbenchPart());
            
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
     * @param workbenchPart
     *            the workbenchPart in which the layout takes place, if any
     * @return the layout graph mapping
     */
    public LayoutMapping buildLayoutGraph(final KNode viewModel,
            final boolean performSizeEstimation, final Object workbenchPart) {
        
        final LayoutMapping mapping = new EclipseLayoutMapping(workbenchPart);
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
            
            if (setFontLayoutOptions) {
                EclipsePlacementUtil.fontDataFor(label, true);
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
            if (((IEclipseViewer) viewer).getControl() != null && ((IEclipseViewer) viewer).getControl().isDisposed()) {
                return;
            }
            recorder.startRecording();
            applyLayout(mapping, suppressEdgeAdjustment);
            recorder.stopRecording(calcAnimationTime(mapping, settings, false));
        } else {
            applyLayout(mapping, false);
        }
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Layout Information Transfer
    
    
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Animation Time

}
