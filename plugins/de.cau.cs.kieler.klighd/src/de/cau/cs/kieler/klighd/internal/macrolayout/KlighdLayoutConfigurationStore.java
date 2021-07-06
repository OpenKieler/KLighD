/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 *
 * Copyright 2012 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 *
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klighd.internal.macrolayout;

import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.elk.core.data.LayoutMetaDataService;
import org.eclipse.elk.core.data.LayoutOptionData;
import org.eclipse.elk.core.data.LayoutOptionData.Target;
import org.eclipse.elk.core.math.ElkPadding;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.service.ILayoutConfigurationStore;
import org.eclipse.elk.graph.properties.IProperty;
import org.eclipse.elk.graph.properties.IPropertyValueProxy;
import org.eclipse.emf.edit.domain.EditingDomain;

import de.cau.cs.kieler.klighd.IViewer;
import de.cau.cs.kieler.klighd.Klighd;
import de.cau.cs.kieler.klighd.internal.util.KlighdInternalProperties;
import de.cau.cs.kieler.klighd.kgraph.KEdge;
import de.cau.cs.kieler.klighd.kgraph.KGraphElement;
import de.cau.cs.kieler.klighd.kgraph.KLabel;
import de.cau.cs.kieler.klighd.kgraph.KLabeledGraphElement;
import de.cau.cs.kieler.klighd.kgraph.KNode;
import de.cau.cs.kieler.klighd.kgraph.KPort;
import de.cau.cs.kieler.klighd.util.AbstractRunnableWithResult;
import de.cau.cs.kieler.klighd.util.ExpansionAwareLayoutOption;
import de.cau.cs.kieler.klighd.util.ExpansionAwareLayoutOption.ExpansionAwareLayoutOptionData;
import de.cau.cs.kieler.klighd.util.RenderingContextData;
import de.cau.cs.kieler.klighd.util.RunnableWithResult;

/**
 * A layout configuration which derives layout options from properties attached to layout data of
 * graph elements.
 *
 * @author mri
 * @author msp
 *
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch
 */
public class KlighdLayoutConfigurationStore implements ILayoutConfigurationStore {

    /**
     * Provider for KLighD layout configuration stores.
     */
    public static final class Provider implements ILayoutConfigurationStore.Provider {

        @Override
        public ILayoutConfigurationStore get(final Object workbenchPart, final Object context) {
            if (context instanceof KGraphElement) {
                try {
                    return new KlighdLayoutConfigurationStore((KGraphElement) context);
                } catch (IllegalArgumentException e) {
                    // Fall back to null
                }
            }
            return null;
        }
    }

    /** The view model part used as context for this configuration store. */
    protected final KGraphElement graphElement;

    /**
     * Create a KLighD layout configuration store.
     * 
     * @param workbenchPart
     *            The {@link IWorkbenchPart} attached to this context.
     * @param context
     *            The {@link KGraphElement} of the view model this {@link ILayoutConfigurationStore}
     *            is attached to.
     */
    public KlighdLayoutConfigurationStore(final KGraphElement context) {
        this.graphElement = context;
    }

    /**
     * {@inheritDoc}
     */
    public Object getOptionValue(final String optionId) {
        LayoutOptionData optionData = LayoutMetaDataService.getInstance().getOptionData(optionId);

        if (optionData == null || graphElement == null) {
            return null;
        }

        final Object value = graphElement.getProperties().get(optionData);
        if (value instanceof IPropertyValueProxy) {
            return ((IPropertyValueProxy) value).resolveValue(optionData);
        } else if (optionData.equals(CoreOptions.PADDING)) {
            return getPaddingLayoutOptionValue();
        } else if (value == null) {
            // check whether an expansion aware layout option set is present
            final ExpansionAwareLayoutOptionData ealo =
                    graphElement.getProperty(ExpansionAwareLayoutOption.OPTION);
            
            // XXX
//            if (ealo == null) {
//                // We provide special support for certain layout options
//                return getSpecialLayoutOptionValue(optionData);
//            } else {
            final KNode node;
            if (graphElement instanceof KNode) {
                node = (KNode) graphElement;
                
            } else if (graphElement instanceof KPort) {
                node = ((KPort) graphElement).getNode();
                
            } else {
                return null;
            }
            
            final RenderingContextData rcd = RenderingContextData.get(node);
            final boolean expanded = !node.getChildren().isEmpty()
                    && rcd.getProperty(KlighdInternalProperties.POPULATED);
            
            return ealo.getValue(optionData, expanded);
//            }
        } else {
            return value;
        }
    }

     /** The aspect ratio is rounded to two decimal places. */
     protected static final double ASPECT_RATIO_ROUND = 100;

    /**
     * When asked about the padding of the node we need to take the node insets into account.
     * 
     * @return The adjusted padding of the node, or {@code null} if graphElement is not a
     *         {@link KNode}
     */
     private Object getPaddingLayoutOptionValue() {
         if (graphElement instanceof KNode) {
             KNode node = (KNode) graphElement;
             ElkPadding padding = new ElkPadding(node.getProperty(CoreOptions.PADDING));
             padding.left += node.getInsets().getLeft();
             padding.right += node.getInsets().getRight();
             padding.top += node.getInsets().getTop();
             padding.bottom += node.getInsets().getBottom();
             return padding;
         }
         return null;
     }

    /**
     * {@inheritDoc}
     */
    public void setOptionValue(final String optionId, final String valueString) {
        LayoutOptionData optionData = LayoutMetaDataService.getInstance().getOptionData(optionId);
        Object value = optionData.parseValue(valueString);

        if (graphElement != null) {
            graphElement.setProperty(optionData, value);
        }
    }

    /**
     * Reveals the KLighD (context) {@link IViewer} from the given layout context.
     *
     * @param context
     *            a layout context
     * @return the corresponding KLighD (context) {@link IViewer}, or {@code null}
     */
    private IViewer getViewer() {
        IViewer viewer = graphElement.getProperty(KlighdInternalProperties.VIEWER);

//        if (viewer == null) {
//            if (workbenchPart instanceof IDiagramWorkbenchPart) {
//                viewer = ((IDiagramWorkbenchPart) workbenchPart).getViewer();
//                graphElement.setProperty(KlighdInternalProperties.VIEWER, viewer);
//            }
//        }
        return viewer;
    }

    /**
     * {@inheritDoc}
     */
    public Collection<String> getAffectedOptions() {
        if (graphElement == null) {
            return Collections.emptyList();
        }

        final List<String> options = new LinkedList<String>();

        Map<IProperty<?>, Object> allProperties = null;

        // chsch: we do have a concurrency issue here that leads to
        // 'elementLayout.getAllProperties() == null', and which I don't understand entirely
        // thus I added this retry routine that in addition catches
        // ConcurrentModificationExceptions, which IMO may occur as well due to the loop in
        // EMapPropertyHolder#getAllProperties()
        while (allProperties == null) {
            try {
                allProperties = graphElement.getAllProperties();

            } catch (final ConcurrentModificationException e) {
                // add an info to the log and retry...
                final String msg = "Concurrent modification in KGraphPropertyLayoutConfig:"
                        + Klighd.LINE_SEPARATOR + "  element == " + graphElement
                        + Klighd.LINE_SEPARATOR + "  sourceElement == "
                        + graphElement.getProperty(KlighdInternalProperties.MODEL_ELEMEMT);

                Klighd.log(new Status(IStatus.ERROR, Klighd.PLUGIN_ID, msg));
            }
        }

        final Set<Map.Entry<IProperty<?>, Object>> entrySet = allProperties.entrySet();

        final KNode node;
        if (graphElement instanceof KNode) {
            node = (KNode) graphElement;
        } else if (graphElement instanceof KPort) {
            node = ((KPort) graphElement).getNode();
        } else {
            node = null;
        }

        // first handle all expansion aware layout option sets
        if (node != null) {
            for (final Map.Entry<IProperty<?>, Object> entry : entrySet) {
                if (entry == null || entry.getKey() == null) {
                    // can happen when an input model is invalid (incorrect syntax in xtext, e.g.)
                    continue;
                }
                if (entry.getKey().equals(ExpansionAwareLayoutOption.OPTION)) {

                    final ExpansionAwareLayoutOptionData ealo =
                            (ExpansionAwareLayoutOptionData) entry.getValue();

                    final RenderingContextData rcd = RenderingContextData.get(node);
                    final boolean expanded = !node.getChildren().isEmpty()
                            && rcd.getProperty(KlighdInternalProperties.POPULATED);

                    final Set<IProperty<?>> keySet =
                            ealo.getValues(expanded).getAllProperties().keySet();
                    for (IProperty<?> iProperty : keySet) {
                        options.add(iProperty.getId());
                    }
                }
            }
        }

        // then handle all normal layout options
        for (final Map.Entry<IProperty<?>, Object> entry : entrySet) {
            if (entry == null || entry.getKey() == null) {
             // can happen when an input model is invalid (incorrect syntax in xtext, e.g.)
                continue;
            }
            if (!entry.getKey().equals(ExpansionAwareLayoutOption.OPTION)) {
                options.add(entry.getKey().getId());
            }
        }
        
        // handle special layout options
        if (getContainer() == null || isSingleNodeOnRootLevel()) {
            options.add(CoreOptions.ASPECT_RATIO.getId());
        }

        return options;
    }

    /**
     * {@inheritDoc}
     */
    public EditingDomain getEditingDomain() {
        // MIGRATE What is our editing domain here?
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Set<Target> getOptionTargets() {
        if (graphElement instanceof KEdge) {
            return EnumSet.of(Target.EDGES);
        } else if (graphElement instanceof KLabel) {
            return EnumSet.of(Target.LABELS);
        } else if (graphElement instanceof KPort) {
            return EnumSet.of(Target.PORTS);
        } else if (graphElement instanceof KNode) {
            if (((KNode) graphElement).getChildren().isEmpty()) {
                return EnumSet.of(Target.NODES);
            } else {
                return EnumSet.of(Target.NODES, Target.PARENTS);
            }
        }
        
        return EnumSet.noneOf(LayoutOptionData.Target.class);
    }

    /**
     * {@inheritDoc}
     */
    public ILayoutConfigurationStore getParent() {
        KGraphElement container = getContainer();
        if (container != null) {
            return new KlighdLayoutConfigurationStore(container);
        } else {
            return null;
        }
    }

    /**
     * Returns the parent node of the given graph element.
     *
     * @param graphElement
     *            a graph element
     * @return the corresponding parent node
     */
    protected KNode getContainer() {
        if (graphElement instanceof KNode) {
            return ((KNode) graphElement).getParent();

        } else if (graphElement instanceof KEdge) {
            return ((KEdge) graphElement).getSource().getParent();

        } else if (graphElement instanceof KPort) {
            return ((KPort) graphElement).getNode().getParent();

        } else if (graphElement instanceof KLabel) {
            final KLabeledGraphElement labeledGraphElement = ((KLabel) graphElement).getParent();

            if (labeledGraphElement instanceof KNode) {
                return ((KNode) labeledGraphElement).getParent();

            } else if (labeledGraphElement instanceof KEdge) {
                return ((KEdge) labeledGraphElement).getSource().getParent();

            } else if (labeledGraphElement instanceof KPort) {
                return ((KPort) labeledGraphElement).getNode().getParent();
            }
        }
        return null;
    }
    
    /**
     * @return {@code true} if and only if {@code graphElement} is a {@link KNode} and it node is
     *         the sole child of the root node.
     */
    protected boolean isSingleNodeOnRootLevel() {
        if (graphElement instanceof KNode) {
            KNode node = (KNode) graphElement;
            return node.getParent() != null 
                    && node.getParent().getParent() == null
                    && node.getParent().getChildren().size() == 1;
        }
        
        return false; 
    }
}
