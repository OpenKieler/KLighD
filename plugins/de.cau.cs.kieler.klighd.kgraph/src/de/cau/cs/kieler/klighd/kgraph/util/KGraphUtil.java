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
package de.cau.cs.kieler.klighd.kgraph.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.elk.core.math.KVector;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.options.Direction;
import org.eclipse.elk.core.options.NodeLabelPlacement;
import org.eclipse.elk.core.options.PortSide;
import org.eclipse.elk.core.options.SizeConstraint;
import org.eclipse.elk.core.util.ElkUtil;
import org.eclipse.elk.graph.ElkEdge;
import org.eclipse.elk.graph.ElkGraphElement;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.ElkPort;
import org.eclipse.elk.graph.util.ElkGraphUtil;
import org.eclipse.emf.common.util.AbstractTreeIterator;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EContentsEList.FeatureFilter;
import org.eclipse.emf.ecore.util.EContentsEList.FeatureIteratorImpl;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.cau.cs.kieler.klighd.kgraph.EMapPropertyHolder;
import de.cau.cs.kieler.klighd.kgraph.KEdge;
import de.cau.cs.kieler.klighd.kgraph.KGraphElement;
import de.cau.cs.kieler.klighd.kgraph.KGraphFactory;
import de.cau.cs.kieler.klighd.kgraph.KGraphPackage;
import de.cau.cs.kieler.klighd.kgraph.KIdentifier;
import de.cau.cs.kieler.klighd.kgraph.KInsets;
import de.cau.cs.kieler.klighd.kgraph.KLabel;
import de.cau.cs.kieler.klighd.kgraph.KLabeledGraphElement;
import de.cau.cs.kieler.klighd.kgraph.KNode;
import de.cau.cs.kieler.klighd.kgraph.KPort;
import de.cau.cs.kieler.klighd.kgraph.KShapeLayout;

/**
 * Utility methods to operate on KGraphs.
 */
public final class KGraphUtil {
    
    /** We will need this. A lot. */
    private static final KGraphFactory FACTORY = KGraphFactory.eINSTANCE;
    /** Default minimal width for nodes. */
    public static final float DEFAULT_MIN_WIDTH = 20.0f;
    /** Default minimal height for nodes. */
    public static final float DEFAULT_MIN_HEIGHT = 20.0f;
    
    /** Utility class is not meant to be instantiated. */
    private KGraphUtil() {
        throw new IllegalAccessError();
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////////////////
    // CREATION

    /**
     * Creates an initialized node. Its insets are set to zero.
     * 
     * @return initialized node.
     */
    public static KNode createInitializedNode() {
        KNode node = FACTORY.createKNode();
        node.setInsets(FACTORY.createKInsets());
        return node;
    }

    /**
     * Creates an initialized port. Its insets are set to zero.
     * 
     * @return initialized port.
     */
    public static KPort createInitializedPort() {
        KPort port = FACTORY.createKPort();
        port.setInsets(FACTORY.createKInsets());
        return port;
    }

    /**
     * Creates an intialized label and adds it to the given element. Its insets are set to zero
     * and its text is set to the empty string.
     * 
     * @param element the element to add the label to.
     * @return the initialized label.
     */
    public static KLabel createInitializedLabel(final KLabeledGraphElement element) {
        KLabel label = FACTORY.createKLabel();
        label.setInsets(FACTORY.createKInsets());
        label.setText("");
        label.setParent(element);
        return label;
    }

    /**
     * Creates an initialized edge. Its source and target point are set to zero.
     * 
     * @return the initialized edge.
     */
    public static KEdge createInitializedEdge() {
        KEdge edge = FACTORY.createKEdge();
        edge.setSourcePoint(FACTORY.createKPoint());
        edge.setTargetPoint(FACTORY.createKPoint());
        return edge;
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////////////////
    // CONFIGURATION

    /**
     * Ensures that each element contained in the given graph is attributed correctly for
     * usage in KIML. {@link KGraphElement}
     * 
     * @param graph the parent node of a graph 
     */
    public static void validate(final KNode graph) {
        // construct an iterator that first returns the root node, i.e. 'graph',
        //  and all contained {@link KGraphElement KGraphElements} afterwards
        //  ({@link KGraphData} are omitted for performance reasons)
        Iterator<KGraphElement> contentIter = Iterators.concat(
                Lists.newArrayList(graph).iterator(),
                Iterators.filter(graph.eAllContents(), KGraphElement.class));
        
        // Note that using an iterator and adding elements works here
        //  as the eAllContents() iterator relies on the lists provided by eContents()
        //  of EObjects that, in turn, provides a mirrored list of all contained elements.
        while (contentIter.hasNext()) {
            EObject element = contentIter.next();
            // Make sure nodes are OK
            if (element instanceof KNode) {
                KNode node = (KNode) element;
                if (node.getInsets() == null) {
                    node.setInsets(FACTORY.createKInsets());
                }
            // Make sure edges are OK
            } else if (element instanceof KEdge) {
                KEdge edge = (KEdge) element;
                if (edge.getSourcePoint() == null) {
                    edge.setSourcePoint(FACTORY.createKPoint());
                }
                if (edge.getTargetPoint() == null) {
                    edge.setTargetPoint(FACTORY.createKPoint());
                }
                // ports and edges are not opposite, so check whether they are connected properly
                KPort sourcePort = edge.getSourcePort();
                if (sourcePort != null) {
                    if (!sourcePort.getEdges().contains(edge)) {
                        sourcePort.getEdges().add(edge);
                    }
                }
                KPort targetPort = edge.getTargetPort();
                if (targetPort != null) {
                    if (!targetPort.getEdges().contains(edge)) {
                        targetPort.getEdges().add(edge);
                    }
                }
            }
        }
    }
    
    /**
     * Adds a default label to the graph element based on the {@link KIdentifier}. Depending on the
     * elements type, hints on the placement of the label are set, e.g. a centered, inside label
     * placement for atomic nodes.
     * 
     * @param ele
     *            the element to configure.
     */
    public static void configureWithDefaultLabel(final KLabeledGraphElement ele) {
        ensureLabel(ele);

        if (ele instanceof KNode) {
            if (!ele.getProperties().containsKey(CoreOptions.NODE_LABELS_PLACEMENT)) {
                // If the node has children, we need to get the label out of the way a bit (we're
                // not setting it up such that padding is computed to reserve space for the label,
                // though)
                if (((KNode) ele).getChildren().isEmpty()) {
                    ele.setProperty(CoreOptions.NODE_LABELS_PLACEMENT,
                            NodeLabelPlacement.insideCenter());
                } else {
                    ele.setProperty(CoreOptions.NODE_LABELS_PLACEMENT,
                            NodeLabelPlacement.insideTopCenter());
                }
            }
        }
    }
    
    /**
     * Sets a default size to the passed element which is based on {@link #DEFAULT_MIN_WIDTH} and
     * {@link #DEFAULT_MIN_HEIGHT} in conjunction with a scaling factor based on the type of the
     * element.
     * 
     * @param ele
     *            the element to configure.
     */
    public static void configurWithDefaultSize(final KShapeLayout ele) {
        if (ele instanceof KNode) {
            KNode node = (KNode) ele;
            // Make sure the node has a size if the size constraints are fixed
            Set<SizeConstraint> sc = node.getProperty(CoreOptions.NODE_SIZE_CONSTRAINTS);

            if (sc.equals(SizeConstraint.fixed()) 
                    && node.getWidth() == 0f && node.getHeight() == 0f) {
                node.setWidth(DEFAULT_MIN_WIDTH * 2);
                node.setHeight(DEFAULT_MIN_HEIGHT * 2);
            }
        } else if (ele instanceof KPort) {
            KPort port = (KPort) ele;

            if (port != null && port.getWidth() == 0f && port.getHeight() == 0f) {
                port.setWidth(DEFAULT_MIN_WIDTH / 2 / 2);
                port.setHeight(DEFAULT_MIN_HEIGHT / 2 / 2);
            }
        }
    }

    /**
     * Adds some default values to the passed node. This includes a reasonable size, a label based
     * on the node's {@link KIdentifier} and a inside center node label placement.
     * 
     * Such default values are useful for fast test case generation.
     * 
     * @param node
     *            a node of a graph
     */
    public static void configureWithDefaultValues(final KNode node) {
        // Make sure the node has a size if the size constraints are fixed
        Set<SizeConstraint> sc = node.getProperty(CoreOptions.NODE_SIZE_CONSTRAINTS);
        
        if (sc.equals(SizeConstraint.fixed()) && node.getWidth() == 0f && node.getHeight() == 0f) {
            node.setWidth(DEFAULT_MIN_WIDTH * 2 * 2);
            node.setHeight(DEFAULT_MIN_HEIGHT * 2 * 2);
        }
        
        // Label
        ensureLabel(node);
        if (!node.getProperties().containsKey(CoreOptions.NODE_LABELS_PLACEMENT)) {
            // If the node has children, we need to get the label out of the way a bit (we're not
            // setting it up such that padding is computed to reserve space for the label, though)
            if (node.getChildren().isEmpty()) {
                node.setProperty(CoreOptions.NODE_LABELS_PLACEMENT,
                        NodeLabelPlacement.insideCenter());
            } else {
                node.setProperty(CoreOptions.NODE_LABELS_PLACEMENT,
                        NodeLabelPlacement.insideTopCenter());
            }
        }
    }

    /**
     * Adds some default values to the passed port. This includes a reasonable size 
     * and a label based on the port's {@link KIdentifier}.
     * 
     * Such default values are useful for fast test case generation.
     * 
     * @param port
     *            a port of a node of a graph
     */
    public static void configureWithDefaultValues(final KPort port) {
        if (port != null && port.getWidth() == 0f && port.getHeight() == 0f) {
            port.setWidth(DEFAULT_MIN_WIDTH / 2 / 2);
            port.setHeight(DEFAULT_MIN_HEIGHT / 2 / 2);
        }
        
        ensureLabel(port);
    }

    /**
     * If the element does not already own a label, a label is created based on the element's
     * {@link KIdentifier} (if it exists, that is).
     * 
     * @param element the element to create a label for.
     */
    private static void ensureLabel(final KLabeledGraphElement element) {
        if (element.getLabels().isEmpty()) {
            KIdentifier id = element.getData(KIdentifier.class);
            if (id != null && !Strings.isNullOrEmpty(id.getId())) {
                KLabel label = createInitializedLabel(element);
                label.setText(id.getId());
            }
        }
    }

    /**
     * A tree iterator that skips persistent entries of {@link EMapPropertyHolder}s. For an
     * explanation of why this is necessary, see the implementation of
     * {@link KGraphDataUtil#loadDataElements()}.
     */
    public static class PersistentEntriesSkippingTreeIterator
            extends AbstractTreeIterator<EObject> {
        /** Bogus serial version ID. */
        private static final long serialVersionUID = 1L;

        /**
         * Creates a tree iterator that skips persistent entries.
         *
         * @param object
         *            The object to start the iteration on. Is expected to be an EObject.
         * @param includeRoot
         *            Flag to indicate whether the passed object should be included in the iterator.
         */
        public PersistentEntriesSkippingTreeIterator(final Object object,
                final boolean includeRoot) {
            super(object, includeRoot);
        }

        @Override
        protected Iterator<? extends EObject> getChildren(final Object object) {
            // We know that the object is an EObject; get an iterator over its content
            Iterator<EObject> iterator = ((EObject) object).eContents().iterator();

            // The iterator will usually be a FeatureIteratorImpl
            // that we can set a feature filter on
            if (iterator instanceof FeatureIteratorImpl) {
                ((FeatureIteratorImpl<EObject>) iterator).filter(new FeatureFilter() {
                    public boolean isIncluded(final EStructuralFeature eStructuralFeature) {
                        // We include everything but persistent entries
                        if (eStructuralFeature.getContainerClass()
                                .equals(EMapPropertyHolder.class)) {
                            return eStructuralFeature.getFeatureID() 
                                    != KGraphPackage.EMAP_PROPERTY_HOLDER__PERSISTENT_ENTRIES;
                        } else {
                            return true;
                        }
                    }
                });
            }

            return iterator;
        }
    }

    /**
     * Persists all {@link EMapPropertyHolder}s of a KGraph by serializing the contained properties
     * into {@link de.cau.cs.kieler.klighd.kgraph.PersistentEntry} tuples.
     *
     * @param graph
     *            the root element of the graph to persist elements of.
     */
    public static void persistDataElements(final KNode graph) {
        TreeIterator<EObject> iterator = new PersistentEntriesSkippingTreeIterator(graph, true);
        while (iterator.hasNext()) {
            EObject eObject = iterator.next();
            if (eObject instanceof EMapPropertyHolder) {
                ((EMapPropertyHolder) eObject).makePersistent();
            }
        }
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////////////////
    // LAYOUT

    /**
     * Converts the given relative point to an absolute location. The insets of the parent node
     * are included in this calculation.
     * 
     * @param point a relative point
     * @param parent the parent node to which the point is relative to
     * @return {@code point} for convenience
     */
    public static KVector toAbsolute(final KVector point, final KNode parent) {
        KNode node = parent;
        while (node != null) {
            KInsets insets = node.getInsets();
            point.add(node.getXpos() + insets.getLeft(), node.getYpos() + insets.getTop());
            node = node.getParent();
        }
        return point;
    }

    /**
     * Converts the given absolute point to a relative location. The insets of the parent node
     * are included in this calculation.
     * 
     * @param point an absolute point
     * @param parent the parent node to which the point shall be made relative to
     * @return {@code point} for convenience
     */
    public static KVector toRelative(final KVector point, final KNode parent) {
        KNode node = parent;
        while (node != null) {
            KInsets insets = node.getInsets();
            point.add(-node.getXpos() - insets.getLeft(), -node.getYpos() - insets.getTop());
            node = node.getParent();
        }
        return point;
    }
    
    /**
     * Returns the absolute position of the given element from the positions of its {@link KShapeLayout} parents.
     * 
     * @param element The element to which the point should be calculated.
     * @return The absolute position of the element in the diagram.
     */
    public static KVector getAbsolute(final KGraphElement element) {
        KVector absolutePosition;
        EObject parent = element;
        while (!(element instanceof KShapeLayout) && parent != null) {
            parent = parent.eContainer();
        }
        if (parent == null) {
            return null;
        }
        absolutePosition = new KVector(((KShapeLayout) parent).getXpos(), ((KShapeLayout) parent).getYpos());
        
        parent = parent.eContainer();
        while (parent != null) {
            while (!(parent instanceof KShapeLayout) && parent != null) {
                    parent = parent.eContainer();
            }
            // We know that the parent is a KShapeLayout now, if its not null.
            if (parent != null) {
                
                KInsets insets = ((KShapeLayout) parent).getInsets();
                absolutePosition.add(insets.getLeft(),  insets.getTop());
                
                absolutePosition.add(((KShapeLayout) parent).getXpos(), ((KShapeLayout) parent).getYpos());
                parent = parent.eContainer();
            }
        }
        
        return absolutePosition;
    }

    /**
     * Determines the port side for the given port from its relative position at
     * its corresponding node.
     * 
     * @param port port to analyze
     * @param direction the overall layout direction
     * @return the port side relative to its containing node
     * @throws IllegalArgumentException if the containing node is {@code null}.
     */
    public static PortSide calcPortSide(final KPort port, final Direction direction) {
        KNode node = port.getNode();
        if (node == null) {
            throw new IllegalArgumentException("port must be assigned to a node");
        }
        
        // if the node has zero size, we cannot decide anything
        float nodeWidth = node.getWidth();
        float nodeHeight = node.getHeight();
        if (nodeWidth <= 0 && nodeHeight <= 0) {
            return PortSide.UNDEFINED;
        }

        // check direction-dependent criterion
        float xpos = port.getXpos();
        float ypos = port.getYpos();
        switch (direction) {
        case LEFT:
        case RIGHT:
            if (xpos < 0) {
                return PortSide.WEST;
            } else if (xpos + port.getWidth() > nodeWidth) {
                return PortSide.EAST;
            }
            break;
        case UP:
        case DOWN:
            if (ypos < 0) {
                return PortSide.NORTH;
            } else if (ypos + port.getHeight() > nodeHeight) {
                return PortSide.SOUTH;
            }
        }
        
        // check general criterion
        float widthPercent = (xpos + port.getWidth() / 2) / nodeWidth;
        float heightPercent = (ypos + port.getHeight() / 2) / nodeHeight;
        if (widthPercent + heightPercent <= 1
                && widthPercent - heightPercent <= 0) {
            // port is on the left
            return PortSide.WEST;
        } else if (widthPercent + heightPercent >= 1
                && widthPercent - heightPercent >= 0) {
            // port is on the right
            return PortSide.EAST;
        } else if (heightPercent < 1.0f / 2) {
            // port is on the top
            return PortSide.NORTH;
        } else {
            // port is on the bottom
            return PortSide.SOUTH;
        }
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////////////////
    // INTERPLAY WITH THE ELK GRAPH
    
    /**
     * Translates all coordinates of the given edge from ELK's to KLighD's coordinate system. ELK
     * uses an edge's containing node's top left corner as the point all edge coordinates will be
     * relative to. KLighD uses either the source node's parent, or the source node itself. This
     * method converts from ELK- to KLighD-compatible coordinates, including all routing
     * information as well as junction points and labels.
     * 
     * <p>If insets are passed to the method, those should be the insets of the {@link KNode} the
     * edge coordinates are relative to in the KLighD graph. In that case, the resulting edge
     * coordinates will not be relative to that node's top left corner, but to the top left corner
     * of its inset area, which is the way it works in KLighD.
     * 
     * @param elkedge the edge whose layout information to change.
     * @param insets optional insets of the node the edge coordinates are relative to in the
     *               KLighD graph.
     * @throws IllegalArgumentException if the edge does not have exactly one source and exactly
     *                                  one target.
     */
    public static void toKGraphCoordinateSystem(final ElkEdge elkedge, final KInsets insets) {
        // Find the edge's end points
        if (elkedge.getSources().size() != 1 || elkedge.getTargets().size() != 1) {
            throw new IllegalArgumentException("Edge must have exactly one source and one target");
        }
        
        ElkNode source = ElkGraphUtil.connectableShapeToNode(elkedge.getSources().get(0));
        ElkNode target = ElkGraphUtil.connectableShapeToNode(elkedge.getTargets().get(0));
        
        // First, find the node the edge coordinates would be relative to in KLighD. This is
        // usually the source's parent node, unless the edge connects a node to one of its
        // descendants
        ElkNode coordinateOrigin = ElkGraphUtil.isDescendant(target, source)
                ? source
                : source.getParent();
        
        // Calculate the offset from the edge's current coordinate origin to the new one (if the
        // two differ)
        KVector offset = new KVector();
        
        if (coordinateOrigin != elkedge.getContainingNode()) {
            ElkUtil.toAbsolute(offset, elkedge.getContainingNode());
            ElkUtil.toRelative(offset, coordinateOrigin);
        }
        
        // KLighD's coordinates are not relative to the top left border of a node, but to the top
        // left corner of its insets area
        if (insets != null) {
            offset.sub(insets.getLeft(), insets.getTop());
        }
        
        // Apply the offset to things
        ElkUtil.translate(elkedge, offset.x, offset.y);
    }
    
    /**
     * Translates all coordinates of the given edge from KLighD's to ELK's coordinate system. This
     * is the inverse of {@link #toKGraphCoordinateSystem(ElkEdge, KInsets)}. See that method for
     * details.
     * 
     * @param elkedge the edge whose layout information to change.
     * @param insets optional insets of the node the edge coordinates are relative to in the
     *               KLighD graph.
     * @throws IllegalArgumentException if the edge does not have exactly one source and exactly
     *                                  one target.
     */
    public static void toELKGraphCoordinateSystem(final ElkEdge elkedge, final KInsets insets) {
        // Find the edge's end points
        if (elkedge.getSources().size() != 1 || elkedge.getTargets().size() != 1) {
            throw new IllegalArgumentException("Edge must have exactly one source and one target");
        }
        
        ElkNode source = ElkGraphUtil.connectableShapeToNode(elkedge.getSources().get(0));
        ElkNode target = ElkGraphUtil.connectableShapeToNode(elkedge.getTargets().get(0));
        
        // First, find the node the edge coordinates would be relative to in KLighD. This is
        // usually the source's parent node, unless the edge connects a node to one of its
        // descendants
        ElkNode coordinateOrigin = ElkGraphUtil.isDescendant(target, source)
                ? source
                : source.getParent();
        
        // Calculate the offset from the edge's current coordinate origin to the new one (if the
        // two differ)
        KVector offset = new KVector();
        
        if (coordinateOrigin != elkedge.getContainingNode()) {
            ElkUtil.toAbsolute(offset, coordinateOrigin);
            ElkUtil.toRelative(offset, elkedge.getContainingNode());
        }
        
        // KLighD's coordinates are not relative to the top left border of a node, but to the top
        // left corner of its insets area
        if (insets != null) {
            offset.add(insets.getLeft(), insets.getTop());
        }
        
        // Apply the offset to things
        ElkUtil.translate(elkedge, offset.x, offset.y);
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////////////////
    // SELECTION ITERATION

    /**
     * Determines the edges that are (transitively) connected to the given edges across hierarchy
     * boundaries via common ports. See {@link #getConnectedEdges(ElkEdge)} for details.
     *
     * @see #getConnectedEdges(ElkEdge)
     * @param edges
     *            an {@link Iterable} of edges that shall be checked
     * @return an {@link Iterator} visiting the given edges and all (transitively) connected ones.
     * @deprecated Use {@link #getConnectedElements(ElkEdge, SelectionIterator, SelectionIterator)}
     *             in combination with {@link DefaultSelectionIterator}
     */
    public static Iterator<KEdge> getConnectedEdges(final Iterable<KEdge> edges) {
        return Iterators.concat(Iterators.transform(edges.iterator(),
                new Function<KEdge, Iterator<KEdge>>() {
                    public Iterator<KEdge> apply(final KEdge kedge) {
                        return getConnectedEdges(kedge);
                    }
        }));
    }

    /**
     * Determines the edges that are (transitively) connected to the given edge across
     * hierarchy boundaries via common ports. Rational: Multiple edges that are
     * pairwise connected by means of an {@link ElkPort} (target port of edge a == source port of
     * edge b or vice versa) may form one logical connection. This kind of splitting might be
     * already present in the view model, or is performed by the layout algorithm for decomposing
     * a nested layout input graph into flat sub graphs.
     *
     * @param edge
     *            the edge check for connected edges
     * @return an {@link Iterator} visiting the given edge and all connected edges in a(n
     *         almost) breadth first search fashion
     * @deprecated Use {@link #getConnectedElements(ElkEdge, SelectionIterator, SelectionIterator)}
     *             in combination with {@link DefaultSelectionIterator}
     */
    public static Iterator<KEdge> getConnectedEdges(final KEdge edge) {
        // Default behavior should be to not select the ports
        return Iterators.filter(getConnectedElements(edge, false), KEdge.class);
    }
    
    /**
     * Determines the {@link ElkGraphElement ElkGraphElements} that are (transitively) connected to
     * {@code kedge} across hierarchy boundaries via common ports. Rational: Multiple {@link ElkEdge
     * ElkEdges} that are pairwise connected by means of an {@link ElkPort} (target port of
     * edge a == source port of edge b or vice versa) may form one logical connection. This kind of
     * splitting might be already present in the view model, or is performed by the layout
     * algorithm for decomposing a nested layout input graph into flat sub graphs.
     * This version allows to also include ports in the selection.
     *
     * @param edge
     *            the edge to check for connected elements
     * @param addPorts
     *            flag to determine, whether ports should be added to the selection or not
     * @return an {@link Iterator} visiting the given {@code edge} and all connected edges in a(n
     *         almost) breadth first search fashion
     * @deprecated Use {@link #getConnectedElements(ElkEdge, SelectionIterator, SelectionIterator)}
     *             in combination with {@link DefaultSelectionIterator}
     */
    public static Iterator<KGraphElement> getConnectedElements(final KEdge edge,
            final boolean addPorts) {
        
        final SelectionIterator sourceSideIt = new DefaultSelectionIterator(edge, addPorts, false);
        final SelectionIterator targetSideIt = new DefaultSelectionIterator(edge, addPorts, true);

        return getConnectedElements(edge, sourceSideIt, targetSideIt);
    }
    
    /**
     * Determines the {@link ElkEdge ElkEdges} that are (transitively) connected to {@code edge}
     * across hierarchy boundaries via common ports. Rational: Multiple {@link ElkEdge ElkEdges}
     * that are pairwise connected by means of na {@link ElkPort} (target port of edge a == source
     * port of edge b or vice versa) may form one logical connection. This kind of splitting might
     * be already present in the view model, or is performed by the layout algorithm for
     * decomposing a nested layout input graph into flat sub graphs.
     *
     * @param kedge
     *            the {@link ElkEdge} check for connected elements
     * @param sourceIterator
     *            the {@link SelectionIterator} to be used for iterating towards the tail of the
     *            selected edge
     * @param targetIterator
     *            the {@link SelectionIterator} to be used for iterating towards the head of the
     *            selected edge
     * @return an {@link Iterator} visiting the given {@code edge} and all connected elements
     *         determined by the {@link SelectionIterator SelectionIterators}
     */
    public static Iterator<KGraphElement> getConnectedElements(final KEdge kedge,
            final SelectionIterator sourceIterator, final SelectionIterator targetIterator) {

     // get a singleton iterator offering 'kedge'
        final Iterator<KGraphElement> kedgeIt = Iterators.singletonIterator((KGraphElement) kedge);
        // Keep a set of visited elements for the tree iterators
        final Set<KPort> visited = Sets.newHashSet();

        // Grab source iterator if edge has a source
        final SelectionIterator sourceSideIt =
                kedge.getSourcePort() == null ? null : sourceIterator;
        if (sourceSideIt != null) {
            // Configure the iterator
            sourceSideIt.attachVisitedSet(visited);
        }

        // Grab target iterator if edge has a target
        final SelectionIterator targetSideIt =
                kedge.getTargetPort() == null ? null : targetIterator;
        if (targetSideIt != null) {
            // Configure the iterator
            targetSideIt.attachVisitedSet(visited);
        }

        // concatenate the source-sidewise and target-sidewise iterators if present ...
        final Iterator<KGraphElement> connectedEdges =
                sourceSideIt == null ? targetSideIt : targetSideIt == null ? sourceSideIt
                        : Iterators.concat(sourceSideIt, targetSideIt);

        // ... and attach them to the input 'kedge' offering iterator, or return just the
        // input 'kedge' iterator in case no ports are configured for 'kedge'
        return connectedEdges == null ? kedgeIt : Iterators.concat(kedgeIt, connectedEdges);
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////////////////
    // MISCELLANEOUS

    /**
     * Determines whether the given child node is a descendant of the parent node. This method does
     * not regard a node as its own descendant.
     * 
     * @param child a child node
     * @param parent a parent node
     * @return {@code true} if {@code child} is a direct or indirect child of {@code parent}.
     */
    public static boolean isDescendant(final KNode child, final KNode parent) {
        KNode current = child;
        if (current == null) return false;
        while (current.getParent() != null) {
            current = current.getParent();
            if (current == parent) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Determines whether the given child element is a descendant of the parent node. This method does
     * not regard a node as its own descendant.
     * 
     * @param child a child element
     * @param parent a parent node
     * @return {@code true} if {@code child} is contained directly or indirectly in {@code parent}.
     */
    public static boolean isDescendant(final KGraphElement child, final KNode parent) {
        EObject current = child;
        while (current.eContainer() != null) {
            current = current.eContainer();
            if (current == parent) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns the graph the given edge belongs to. If the target is a descendant of the source
     * node, the latter is the contained graph. Otherwise, the source's parent is the contained
     * node.
     * 
     * @param edge the edge.
     * @return the graph that contains the edge.
     */
    public static KNode containedGraph(final KEdge edge) {
        return isDescendant(edge.getTarget(), edge.getSource())
            ? edge.getSource()
            : edge.getSource().getParent();
    }
    
    /**
     * Determines whether the given two nodes are siblings, that is if they have the same parent
     * node. If they do not have a parent node they are not considered siblings.
     * 
     * @param node1 the first node.
     * @param node2 the second node.
     * @return {@code true} if the two nodes have the same non-{@code null} parent.
     */
    public static boolean isSibling(final KNode node1, final KNode node2) {
        return node1.getParent() == node2.getParent() && node1.getParent() != null;
    }

    /**
     * Determines the root of the given node.
     * 
     * @param node KNode, which root should be returned.
     * @return The root of the given node.
     */
    public static KNode getRootNodeOf(KNode node) {
        KNode parent = node;
        while (parent.getParent() != null) {
            parent = parent.getParent();
        }
        return parent;
    }

    /**
     * Collects the adjacent nodes of {@code node} in a list.
     * @param node The node of which you want to know the adjacent nodes.
     * @return The adjacent nodes of the given node
     */
    public static List<KNode> getAdjacentNodes(KNode node) {
        List<KEdge> inEdges = node.getIncomingEdges();
        List<KEdge> outEdges = node.getOutgoingEdges();
        List<KNode> adjacentNodes = new ArrayList<>();

        for (KEdge e : inEdges) {
            adjacentNodes.add(e.getSource());
        }
        for (KEdge e : outEdges) {
            adjacentNodes.add(e.getTarget());
        }
        return adjacentNodes;
    }
}
