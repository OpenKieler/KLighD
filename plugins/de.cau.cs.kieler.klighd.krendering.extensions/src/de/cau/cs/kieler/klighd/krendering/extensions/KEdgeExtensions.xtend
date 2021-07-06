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
package de.cau.cs.kieler.klighd.krendering.extensions

import com.google.inject.Injector
import com.google.inject.Scope
import de.cau.cs.kieler.klighd.kgraph.KEdge
import de.cau.cs.kieler.klighd.kgraph.util.KGraphUtil
import de.cau.cs.kieler.klighd.krendering.KPolyline
import de.cau.cs.kieler.klighd.krendering.KRenderingFactory
import de.cau.cs.kieler.klighd.krendering.KRoundedBendsPolyline
import de.cau.cs.kieler.klighd.krendering.KSpline
import de.cau.cs.kieler.klighd.krendering.ViewSynthesisShared
import java.util.List
import javax.inject.Inject
import org.eclipse.elk.graph.properties.IProperty

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
 * @author ssm
 * @autnor nre
 * 
 * @containsExtensions
 */
@ViewSynthesisShared
class KEdgeExtensions {

    private static val KRenderingFactory renderingFactory = KRenderingFactory::eINSTANCE
        
    @Inject
    extension KRenderingExtensions;
    
        
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////                    KEdgeExtensions
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * A convenient getter preserving the element image relation by a create extension.
     */ 
    def private KEdge create port: KGraphUtil::createInitializedEdge internalCreateEdge(Object... oc) {
    }
    
    /**
     * The Xtend-generated internal create map for {@link #internalCreateEdge} with a more accessible name.
     */
    private def getInternalEdgeMap() {
        return this._createCache_internalCreateEdge
    }

    /**
     * A convenient test method to check whether or not a specific edge exists in the create extension
     */
    def boolean edgeExists(Object o1) {
        getInternalEdgeMap().containsKey(newArrayList(o1))
    }

    /**
     * A convenient test method to check whether or not a specific edge exists in the create extension
     */
    def boolean edgeExists(Object o1, Object o2) {
        getInternalEdgeMap().containsKey(newArrayList(o1, o2))
    }

    /**
     * A convenient test method to check whether or not a specific edge exists in the create extension
     */
    def boolean edgeExists(Object o1, Object o2, Object o3) {
        getInternalEdgeMap().containsKey(newArrayList(o1, o2, o3))
    }

    /**
     * A convenient test method to check whether or not a specific edge exists in the create extension
     */
    def boolean edgeExists(Object o1, Object o2, Object o3, Object o4) {
        getInternalEdgeMap().containsKey(newArrayList(o1, o2, o3, o4))
    }
    
    /**
     * A convenient test method to check whether or not a specific edge exists in the create extension
     */
    def boolean edgeExists(Object... os) {
        getInternalEdgeMap.containsKey(newArrayList(os))
    }

    /**
     * A convenient port getter based on a single business object preserving the
     * element image relation by a create extension.
     */
    def KEdge getEdge(Object o1) {
        return internalCreateEdge(o1)
    }
    
    /**
     * A convenient port getter based on a two business objects preserving the
     * element image relation by a create extension.
     */
    def KEdge getEdge(Object o1, Object o2) {
        return internalCreateEdge(o1, o2)
    }
    
    /**
     * A convenient port getter based on a three business objects preserving the
     * element image relation by a create extension.
     */
    def KEdge getEdge(Object o1, Object o2, Object o3) {
        return internalCreateEdge(o1, o2, o3)
    }
    
    /**
     * A convenient port getter based on a four business objects preserving the
     * element image relation by a create extension.
     */
    def KEdge getEdge(Object o1, Object o2, Object o3, Object o4) {
        return internalCreateEdge(o1, o2, o3, o4)
    }
    
    /**
     * A convenient port getter based on a single business object preserving the
     * element image relation by a create extension.
     */
    def KEdge getEdge(Object... os) {
        return internalCreateEdge(os)
    }
    
    /**
     * A convenience method to create a KEdge without relating it to a business object. 
     */
    def KEdge createEdge() {
        return KGraphUtil::createInitializedEdge()
    }
    
    /**
     * An alias of {@link #getEdge(Object o1)} allowing to express in business that the KEdge will
     * be created at this place. It is just syntactic sugar.  
     */
    def KEdge createEdge(Object o1) {
        return o1.getEdge()
    }
    
    /**
     * An alias of {@link #getEdge(Object o1, Object o2)} allowing to express in business that the
     * KEdge will be created at this place. It is just syntactic sugar.  
     */
    def KEdge createEdge(Object o1, Object o2) {
        return o1.getEdge(o2);
    }
    
    /**
     * An alias of {@link #getEdge(Object o1, Object o2, Object o3)} allowing to express in business
     * that the KEdge will be created at this place. It is just syntactic sugar.  
     */
    def KEdge createEdge(Object o1, Object o2, Object o3) {
        return o1.getEdge(o2, o3);
    }
    
    /**
     * An alias of {@link #getEdge(Object o1, Object o2, Object o3, Object o4)} allowing to express in
     * business that the KEdge will be created at this place. It is just syntactic sugar.  
     */
    def KEdge createEdge(Object o1, Object o2, Object o3, Object o4) {
        return o1.getEdge(o2, o3, o4);
    }
    
    /**
     * An alias of {@link #getEdge(Object o1)} allowing to express in business that the KEdge will
     * be created at this place. It is just syntactic sugar.  
     */
    def KEdge createEdge(Object... os) {
        return os.getEdge()
    }
    
    /**
     * Similar to createEdge, createNewEdge creates a new edge related to an object. Furthermore, the 
     * method makes sure a new edge is created and stored in the create extensions.
     */
    def KEdge createNewEdge(Object o1) {
        var int counter = 0
        while (o1.edgeExists(counter)) counter = counter + 1 
        return o1.createEdge(counter)
    }

    /**
     * Similar to createEdge, createNewEdge creates a new edge related to an object. Furthermore, the 
     * method makes sure a new edge is created and stored in the create extensions.
     */
    def KEdge createNewEdge(Object o1, Object o2) {
        var int counter = 0
        while (o1.edgeExists(o2, counter)) counter = counter + 1 
        return o1.createEdge(o2, counter)
    }

    /**
     * Similar to createEdge, createNewEdge creates a new edge related to an object. Furthermore, the 
     * method makes sure a new edge is created and stored in the create extensions.
     */
    def KEdge createNewEdge(Object o1, Object o2, Object o3) {
        var int counter = 0
        while (o1.edgeExists(o2, o3, counter)) counter = counter + 1 
        return o1.createEdge(o2, o3, counter)
    }
    
    /**
     * The method getAllEdges retrieves all edges linked to a specific (semantic) object.
     */
    def List<KEdge> getAllEdges(Object o1) {
        var int counter = 0
        val edges = <KEdge> newArrayList
        while (o1.edgeExists(counter)) { 
            edges.add(o1.getEdge(counter)); 
            counter = counter + 1
        }
        edges
    }

    /**
     * The method getAllEdges retrieves all edges linked to a specific (semantic) object.
     */
    def List<KEdge> getAllEdges(Object o1, Object o2) {
        var int counter = 0
        val edges = <KEdge> newArrayList
        while (o1.edgeExists(o2, counter)) { 
            edges.add(o1.getEdge(o2, counter)); 
            counter = counter + 1
        }
        edges
    }

    /**
     * The method getAllEdges retrieves all edges linked to a specific (semantic) object.
     */
    def List<KEdge> getAllEdges(Object o1, Object o2, Object o3) {
        var int counter = 0
        val edges = <KEdge> newArrayList
        while (o1.edgeExists(o2, o3, counter)) { 
            edges.add(o1.getEdge(o2, o3, counter)); 
            counter = counter + 1
        }
        edges
    }
    
    /**
     * getSemanticObject returns the primary (semantic) object of an edge.
     */
    def Object getSemanticObject(KEdge edge) {
        (getInternalEdgeMap.filter[p1, p2|p2 == edge].keySet.head as List<?>).head
    }
    
    
    def <T> KEdge addLayoutParam(KEdge edge, IProperty<? super T> property, T value) {
        edge.setProperty(property, value);
        return edge;
    }
    
    def KPolyline addPolyline(KEdge e) {
        return renderingFactory.createKPolyline() => [
            e.data += it;
        ];
    }
    
    def KPolyline addPolyline(KEdge e, float lineWidth) {
        return e.addPolyline.lineWidth = lineWidth;
    }
    
    def KRoundedBendsPolyline addRoundedBendsPolyline(KEdge e, float bendRadius) {
        return renderingFactory.createKRoundedBendsPolyline() => [
            e.data += it;
            it.bendRadius = bendRadius;
        ];
    }

    def KRoundedBendsPolyline addRoundedBendsPolyline(KEdge e, float bendRadius, float lineWidth) {
        return e.addRoundedBendsPolyline(bendRadius).lineWidth = lineWidth;
    }
    
    def KSpline addSpline(KEdge e) {
        return renderingFactory.createKSpline => [
            e.data += it;
        ];
    }
    
    def KSpline addSpline(KEdge e, float lineWidth) {
        return e.addSpline.lineWidth = lineWidth;
    }
    
}