/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2012-2019 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klighd.krendering.extensions

import com.google.inject.Injector
import com.google.inject.Scope
import de.cau.cs.kieler.klighd.kgraph.KGraphElement
import de.cau.cs.kieler.klighd.kgraph.KNode
import de.cau.cs.kieler.klighd.kgraph.util.KGraphUtil
import de.cau.cs.kieler.klighd.krendering.ViewSynthesisShared
import javax.inject.Inject
import org.eclipse.elk.core.math.KVector
import org.eclipse.elk.core.options.CoreOptions
import org.eclipse.elk.core.options.NodeLabelPlacement
import org.eclipse.elk.core.util.Pair
import org.eclipse.elk.graph.properties.IProperty
import org.eclipse.elk.graph.properties.Property

/**
 * Provides some helpful extension methods for simplifying the composition of KGraph/KRendering-based view models.<br>
 * <br>
 * In order to employ them beyond KLighD diagram syntheses you best declare a field of type
 * {@link KNodeExtensions} in your class and annotate it with {@link Inject Inject}.<br>
 * <br>
 * Make sure to bind the {@link ViewSynthesisShared} annotation in the employed
 * {@link Injector Injector} to a {@link Scope}, e.g. by calling
 * {@code Guice.createInjector(KRenderingExtensionsPlugin.createSingletonScopeBindingModule());} or 
 * {@code Guice.createInjector(KRenderingExtensionsPlugin.createNoScopeBindingModule());}.<br>
 * <br>
 * By means of that {@link Injector Injector} you may get a new instance of your class,
 * or you may inject the above mentioned attribute within instances of your class, e.g. by calling
 * {@code injector.injectMembers(this)} in the constructor of your class.
 * 
 * @author chsch
 * @author nre
 * 
 * @containsExtensions
 */
@ViewSynthesisShared
class KNodeExtensions {
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////                    KNodeExtensions
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * A convenient getter preserving the element image relation by a create extension.
     */ 
    def private KNode create node: KGraphUtil::createInitializedNode internalCreateNode(Object... oc) {
    }
    
    /**
     * The Xtend-generated internal create map for {@link #internalCreateNode} with a more accessible name.
     */
    private def getInternalNodeMap() {
        return this._createCache_internalCreateNode
    }
    
    /**
     * A convenient test method to check whether or not a specific node exists in the create extension
     */
    def boolean nodeExists(Object... os) {
        getInternalNodeMap.containsKey(newArrayList(os))
    }
    
    /**
     * A convenient method to register a node that was not created via the create extension.
     * @return the previous node associated with the given object(s), or {@code null} if there was no node.
     */
    def KNode registerExistingNode(KNode node, Object... os) {
        return getInternalNodeMap.put(newArrayList(os), node)
    }

    /**
     * A convenient getter preserving the element image relation by a create extension.
     */ 
    def KNode getNode(Object o) {
        internalCreateNode(o)
    }
    
    /**
     * A convenient getter preserving the element image relation.
     */ 
    def KNode getNode(Object o1, Object o2) {
        internalCreateNode(o1, o2)
    }
    
    /**
     * A convenient getter preserving the element image relation by a create extension.
     */ 
    def KNode getNode(Object... os) {
        internalCreateNode(os)
    }
    
    /**
     * A convenience method to create a KNode without relating it to a business object. 
     */
    def KNode createNode() {
        return KGraphUtil::createInitializedNode();
    }
    
    /**
     * An alias of {@link #getNode} allowing to express in business that the KNode will
     * be created at this place. It is just syntactic sugar.  
     */
    def KNode createNode(Object o) {
        return o.node
    }
    
    /**
     * An alias of {@link #getNode} allowing to express in business that the KNode will
     * be created at this place. It is just syntactic sugar.
     */
    def KNode createNode(Object o1, Object o2) {
        return o1.getNode(o2)
    }
    
    /**
     * An alias of {@link #getNode} allowing to express in business that the KNode will
     * be created at this place. It is just syntactic sugar.  
     */
    def KNode createNode(Object... os) {
        return os.node
    }
    
    def Pair<Float, Float> getNodeSize(KNode node) {
        return new Pair<Float, Float> => [
            it.first = node.height
            it.second = node.height
        ];
    }

    def float getHeight(KNode node) {
        node.height;
    }
    
    /**
     * Is used in KPortExtensions
     */    
    def float getWidth(KNode node) {
        node.width;
    }
    
    def KNode setNodeSize(KNode node, float width, float height) {
        return node => [
            node.setSize(width, height);
            setMinimalNodeSize(width, height);
        ];
    }
    
    def KNode setWidth(KNode node, float width) {
        val height = node.height;
        node.setNodeSize(width, height)
        return node
    }
    
    def KNode setHeight(KNode node, float height) {
        val width = node.width;
        node.setNodeSize(width, height)
        return node
    }
    
    private static val IProperty<KVector> MINIMAL_NODE_SIZE = new Property<KVector>(
            "de.cau.cs.kieler.klighd.minimalNodeSize", new KVector(10d, 10d));

    def KNode setMinimalNodeSize(KNode node, float width, float height) {
        return node.addLayoutParam(MINIMAL_NODE_SIZE, new KVector(width, height));
    }
    
    def <T> KNode addLayoutParam(KNode node, IProperty<? super T> property, T value) {
        node?.setProperty(property, value)
        return node
    }
    
    /////////////
    // Node label placement
    /////////////
    
    /**
     * Internal helper for setting layout options without the need to check for KNode, KEdge, ...
     */
    def private <S, T extends KGraphElement> T setLayoutOption(T kgraphElement, IProperty<S> option, S value) {
        kgraphElement?.setProperty(option, value)
        return kgraphElement
    }    
    
    /**
     * Configures labels on this node to be outside bottom left-aligned labels by default
     */
    def KNode configureOutsideBottomLeftNodeLabelPlacement(KNode node) {
        return node.setLayoutOption(CoreOptions::NODE_LABELS_PLACEMENT, NodeLabelPlacement::outsideBottomLeft)
    }
    
    /**
     * Configures labels on this node to be outside bottom centrally-aligned labels by default
     */
    def KNode configureOutsideBottomCenteredNodeLabelPlacement(KNode node) {
        return node.setLayoutOption(CoreOptions::NODE_LABELS_PLACEMENT, NodeLabelPlacement::outsideBottomCenter)
    }
    
    /**
     * Configures labels on this node to be outside bottom right-aligned labels by default
     */
    def KNode configureOutsideBottomRightNodeLabelPlacement(KNode node) {
        return node.setLayoutOption(CoreOptions::NODE_LABELS_PLACEMENT, NodeLabelPlacement::outsideBottomRight)
    }
    
    /**
     * Configures labels on this node to be outside top left-aligned labels by default
     */
    def KNode configureOutsideTopLeftNodeLabelPlacement(KNode node) {
        return node.setLayoutOption(CoreOptions::NODE_LABELS_PLACEMENT, NodeLabelPlacement::outsideTopLeft)
    }
    
    /**
     * Configures labels on this node to be outside top centrally-aligned labels by default
     */
    def KNode configureOutsideTopCenteredNodeLabelPlacement(KNode node) {
        return node.setLayoutOption(CoreOptions::NODE_LABELS_PLACEMENT, NodeLabelPlacement::outsideTopCenter)
    }
    
    /**
     * Configures labels on this node to be outside top right-aligned labels by default
     */
    def KNode configureOutsideTopRightNodeLabelPlacement(KNode node) {
        return node.setLayoutOption(CoreOptions::NODE_LABELS_PLACEMENT, NodeLabelPlacement::outsideTopRight)
    }
    
    /**
     * Configures labels on this node to be inside bottom left-aligned labels by default
     */
    def KNode configureInsideBottomLeftNodeLabelPlacement(KNode node) {
        return node.setLayoutOption(CoreOptions::NODE_LABELS_PLACEMENT, NodeLabelPlacement::insideBottomLeft)
    }
    
    /**
     * Configures labels on this node to be inside bottom centrally-aligned labels by default
     */
    def KNode configureInsideBottomCenteredNodeLabelPlacement(KNode node) {
        return node.setLayoutOption(CoreOptions::NODE_LABELS_PLACEMENT, NodeLabelPlacement::insideBottomCenter)
    }
    
    /**
     * Configures labels on this node to be inside bottom right-aligned labels by default
     */
    def KNode configureInsideBottomRightNodeLabelPlacement(KNode node) {
        return node.setLayoutOption(CoreOptions::NODE_LABELS_PLACEMENT, NodeLabelPlacement::insideBottomRight)
    }
    
    /**
     * Configures labels on this node to be inside centrally-aligned labels by default
     */
    def KNode configureInsideCenteredNodeLabelPlacement(KNode node) {
        return node.setLayoutOption(CoreOptions::NODE_LABELS_PLACEMENT, NodeLabelPlacement::insideCenter)
    }
    
    /**
     * Configures labels on this node to be inside top left-aligned labels by default
     */
    def KNode configureInsideTopLeftNodeLabelPlacement(KNode node) {
        return node.setLayoutOption(CoreOptions::NODE_LABELS_PLACEMENT, NodeLabelPlacement::insideTopLeft)
    }
    
    /**
     * Configures labels on this node to be inside top centrally-aligned labels by default
     */
    def KNode configureInsideTopCenteredNodeLabelPlacement(KNode node) {
        return node.setLayoutOption(CoreOptions::NODE_LABELS_PLACEMENT, NodeLabelPlacement::insideTopCenter)
    }
    
    /**
     * Configures labels on this node to be inside top right-aligned labels by default
     */
    def KNode configureInsideTopRightNodeLabelPlacement(KNode node) {
        return node.setLayoutOption(CoreOptions::NODE_LABELS_PLACEMENT, NodeLabelPlacement::insideTopRight)
    }
    
}