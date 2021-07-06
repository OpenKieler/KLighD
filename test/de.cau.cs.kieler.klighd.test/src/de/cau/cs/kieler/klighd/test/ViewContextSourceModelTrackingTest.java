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
package de.cau.cs.kieler.klighd.test;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsCollectionWithSize;
import org.hamcrest.collection.IsEmptyIterable;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.hamcrest.collection.IsIterableWithSize;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import de.cau.cs.kieler.klighd.IDiagramWorkbenchPart;
import de.cau.cs.kieler.klighd.IUpdateStrategy;
import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.incremental.IncrementalUpdateStrategy;
import de.cau.cs.kieler.klighd.internal.util.KlighdInternalProperties;
import de.cau.cs.kieler.klighd.internal.util.SourceModelTrackingAdapter;
import de.cau.cs.kieler.klighd.kgraph.KEdge;
import de.cau.cs.kieler.klighd.kgraph.KGraphElement;
import de.cau.cs.kieler.klighd.kgraph.KNode;
import de.cau.cs.kieler.klighd.kgraph.util.KGraphUtil;
import de.cau.cs.kieler.klighd.krendering.KRectangle;
import de.cau.cs.kieler.klighd.krendering.KRenderingFactory;
import de.cau.cs.kieler.klighd.krendering.KText;
import de.cau.cs.kieler.klighd.krendering.SimpleUpdateStrategy;
import de.cau.cs.kieler.klighd.syntheses.DuplicatingDiagramSynthesis;

/**
 * @author chsch
 */
public class ViewContextSourceModelTrackingTest {
    
    // CHECKSTYLEOFF MagicNumber

    private static final IUpdateStrategy UPDATE_STRATEGY = new SimpleUpdateStrategy();
    
    private static final IUpdateStrategy INCREMENTAL_UPDATE_STRATEGY = new IncrementalUpdateStrategy();
    
    private ViewContext createViewContext() {
        return new ViewContext((IDiagramWorkbenchPart) null, null);
    }
    
    private void checkTracerMaps(final ViewContext viewContext, final int expectedEntryNumber) {
        final Multimap<?, ?> sourceTargetsMap;
        final Map<?, ?> targetSourceMap;

        try {
            final Field tracerField = ViewContext.class.getDeclaredField("tracer");
            tracerField.setAccessible(true);
            final SourceModelTrackingAdapter tracer =
                    (SourceModelTrackingAdapter) tracerField.get(viewContext);
            
            final Field sourceTargetsField = tracer.getClass().getDeclaredField("sourceTargetsMap");
            sourceTargetsField.setAccessible(true);
            sourceTargetsMap = (Multimap<?, ?>) sourceTargetsField.get(tracer);
            
            final Field targetSourceField = tracer.getClass().getDeclaredField("targetSourceMap");
            targetSourceField.setAccessible(true);
            targetSourceMap = (Map<?, ?>) targetSourceField.get(tracer);
            
        } catch (Exception e) {
            Assert.fail("Couldn't access the employed SourceModelTracingAdapter's maps.");
            return;
        }

        MatcherAssert.assertThat("", sourceTargetsMap.values(), IsCollectionWithSize.hasSize(expectedEntryNumber));
        MatcherAssert.assertThat("", targetSourceMap.entrySet(), IsCollectionWithSize.hasSize(expectedEntryNumber));
    }


    /**
     * Tests the initialization of the {@link SourceModelTrackingAdapter}'s maps if an instance is
     * attached to a given KGraph network.
     */
    @Test
    public void test00() {
        final Object elementB = new Object();
        final KNode nodeB = KGraphUtil.createInitializedNode();        
        nodeB.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, elementB);
        
        final Object elementA = new Object();
        final KNode nodeA = KGraphUtil.createInitializedNode();
        nodeA.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, elementA);
        nodeA.getChildren().add(nodeB);

        final KNode root = KGraphUtil.createInitializedNode();
        root.getChildren().add(nodeA);
        
        final SourceModelTrackingAdapter adapter = new SourceModelTrackingAdapter();
        root.eAdapters().add(adapter);
        
        Assert.assertSame(elementB, adapter.getSourceElement(nodeB));
        Assert.assertSame(nodeB, Iterables.getFirst(adapter.getTargetElements(elementB), null));
        
        Assert.assertSame(elementA, adapter.getSourceElement(nodeA));
        Assert.assertSame(nodeA, Iterables.getFirst(adapter.getTargetElements(elementA), null));
        
        root.getChildren().clear();
        
        Assert.assertNull(adapter.getSourceElement(nodeA));
        Assert.assertNull(adapter.getSourceElement(nodeB));
        
        root.eAdapters().remove(adapter);
    }


    /**
     * A.
     */
    @Test
    public void test01() {
        updateViewModelRootSourceElement(
                createViewContext());
    }
    
    /**
     * A.
     */
    @Test
    public void test02() {
        updateViewModelRootSourceElement(
                updateViewModelRootSourceElement(
                        createViewContext()));
    }

    private ViewContext updateViewModelRootSourceElement(final ViewContext viewContext) {
        final KNode viewModel = viewContext.getViewModel();

        final EObject sourceModel = new EObjectImpl() { };
        final KNode newModel = KGraphUtil.createInitializedNode();
        newModel.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, sourceModel);
        
        UPDATE_STRATEGY.update(viewModel, newModel, viewContext);
        
        Assert.assertEquals(
                viewContext.getSourceElement(viewContext.getViewModel()),
                sourceModel);
        
        MatcherAssert.assertThat("",viewContext.getTargetElements(sourceModel),
                IsIterableContainingInOrder.contains((Object) viewContext.getViewModel()));
        
        return viewContext;
    }
    
    
    /**
     * 
     */
    @Test
    public void test03() {
        updateWith(createViewContext(), createSimpleNetwork());
    }
    
    private ViewContext updateWith(final ViewContext viewContext, final KNode sourceRoot) {
        
        final KNode targetRoot = new DuplicatingDiagramSynthesis().transform(sourceRoot, viewContext);
        
        UPDATE_STRATEGY.update(viewContext.getViewModel(), targetRoot, viewContext);
        
        Assert.assertEquals(sourceRoot,
                viewContext.getSourceElement(viewContext.getViewModel()));
        
        MatcherAssert.assertThat("",viewContext.getTargetElements(sourceRoot),
                IsIterableContainingInOrder.contains((Object) viewContext.getViewModel()));
        
        Assert.assertEquals(sourceRoot.getChildren().get(0),
                viewContext.getSourceElement(viewContext.getViewModel().getChildren().get(0)));
        
        Assert.assertEquals(sourceRoot.getChildren().get(1),
                viewContext.getSourceElement(viewContext.getViewModel().getChildren().get(1)));
        
        Assert.assertEquals(sourceRoot.getChildren().get(0).getOutgoingEdges().get(0),
                viewContext.getSourceElement(viewContext.getViewModel().getChildren().get(0)
                        .getOutgoingEdges().get(0)));        
        
        return viewContext;
    }
    
    private KNode createSimpleNetwork() {
        final KNode sourceNode1 = KGraphUtil.createInitializedNode();
        final KNode sourceNode2 = KGraphUtil.createInitializedNode();
        final KEdge sourceEdge = KGraphUtil.createInitializedEdge();
        sourceEdge.setSource(sourceNode1);
        sourceEdge.setTarget(sourceNode2);
        
        final KNode sourceRoot = KGraphUtil.createInitializedNode();
        sourceRoot.getChildren().add(sourceNode1);
        sourceRoot.getChildren().add(sourceNode2);
        return sourceRoot;
    }
    
    /**
     * A.
     */
    @Test
    public void test04() {
        final ViewContext viewContext = updateWith(createViewContext(), createSimpleNetwork());
        
        final KNode child0 = viewContext.getViewModel().getChildren().get(0);
        final Object source0 = viewContext.getSourceElement(child0);
        final KNode child1 = viewContext.getViewModel().getChildren().get(1);
        final Object source1 = viewContext.getSourceElement(child1);
        
        final KEdge viewEdge = child0.getOutgoingEdges().get(0);
        final Object sourceEdge = viewContext.getSourceElement(viewEdge);
        
        viewContext.getViewModel().getChildren().clear();
        
        Assert.assertNull(viewContext.getSourceElement(child0));
        Assert.assertNull(viewContext.getSourceElement(child1));
        Assert.assertNull(viewContext.getSourceElement(viewEdge));
        
        Assert.assertNull(viewContext.getTargetElement(source0, null));
        Assert.assertNull(viewContext.getTargetElement(source1, null));
        Assert.assertNull(viewContext.getTargetElement(sourceEdge, null));
    }
    

    /**
     * 
     */
    @Test
    public void test05() {
        final ViewContext viewContext = 
                addSubViewModels(
                        updateWith(createViewContext(), createSimpleNetwork()));

        final KNode child0 = viewContext.getViewModel().getChildren().get(0);
        
        final Collection<KGraphElement> child0kges =
                Lists.newArrayList(Iterators.filter(child0.eAllContents(), KGraphElement.class));
        
        // note that child0.eAllContents() includes child0's outgoing edge, too! 
        MatcherAssert.assertThat("", child0kges, IsIterableWithSize.<KGraphElement>iterableWithSize(4));
        
        final Iterable<Object> child0kgesSources = Lists.newArrayList(
                Iterables.transform(child0kges, new Function<KGraphElement, Object>() {

                    public Object apply(final KGraphElement input) {
                        return viewContext.getSourceElement(input);
                    }
                }));
        
        MatcherAssert.assertThat("",child0kgesSources, IsIterableWithSize.iterableWithSize(4));

        MatcherAssert.assertThat("",child0kgesSources,
                IsCollectionContaining.hasItem(CoreMatchers.notNullValue()));

        MatcherAssert.assertThat("",Iterables.filter(child0kgesSources, Predicates.<Object>in(child0kges)),
                IsEmptyIterable.emptyIterable());
        
        final Iterable<EObject> child0kgesSourcesTargets =
                Iterables.transform(child0kgesSources, new Function<Object, EObject>() {

                    public EObject apply(final Object input) {
                        return viewContext.getTargetElement(input, null);
                    }
                });

        MatcherAssert.assertThat("",child0kgesSourcesTargets, IsIterableWithSize.<EObject>iterableWithSize(4));

        MatcherAssert.assertThat("",child0kgesSourcesTargets,
                IsIterableContainingInOrder.contains(child0kges.toArray()));
        
        checkTracerMaps(viewContext, 10);
    }
    
    private ViewContext addSubViewModels(final ViewContext viewContext) {
        
        final KNode child0 = viewContext.getViewModel().getChildren().get(0);
        final KNode targetRoot0 = new DuplicatingDiagramSynthesis().transform(
                createSimpleNetwork(), new ViewContext(viewContext, null));

        child0.getChildren().addAll(targetRoot0.getChildren());
        
        
        final KNode child1 = viewContext.getViewModel().getChildren().get(1);
        final KNode targetRoot1 = new DuplicatingDiagramSynthesis().transform(
                createSimpleNetwork(), new ViewContext(viewContext, null));

        child1.getChildren().addAll(targetRoot1.getChildren());
        
        return viewContext;
    }

    /**
     * 
     */
    @Test
    public void test06() {
        final ViewContext viewContext = 
                addSubViewModels(updateWith(createViewContext(), createSimpleNetwork()));
        
        viewContext.getViewModel().getChildren().clear();
        
        checkTracerMaps(viewContext, 1);
    }

    

    /**
     * 
     */
    @Test
    public void test07() {
        final ViewContext viewContext = 
                updateWith(createViewContext(), attachRenderings(createSimpleNetwork()));

        // 1 (root) + 2*3 (kge+rect+ktext) + 1 (edge)
        checkTracerMaps(viewContext, 8);

        final KRectangle rect =
                Iterators.getNext(Iterators.filter(viewContext.getViewModel().eAllContents(),
                        KRectangle.class), null);
        
        Assert.assertNotNull(rect);
        
       final Object source = viewContext.getSourceElement(rect);
        
       Assert.assertNotNull(source);
       
       final Iterable<EObject> targets = viewContext.getTargetElements(source);
       
       Assert.assertNotNull(targets);
       MatcherAssert.assertThat("",targets, IsIterableWithSize.<EObject>iterableWithSize(1));
       Assert.assertSame(rect, Iterables.getOnlyElement(targets));
   }
    
    private KNode attachRenderings(final KNode viewModel) {
        for (Iterator<KGraphElement> it =
                Iterators.filter(viewModel.eAllContents(), KGraphElement.class);
                it.hasNext();) {
            final KGraphElement kge = it.next();
            if (!(kge instanceof KEdge)) {
                final KText text = KRenderingFactory.eINSTANCE.createKText();
                final KRectangle rect = KRenderingFactory.eINSTANCE.createKRectangle();
                rect.getChildren().add(text);
                kge.getData().add(rect);
            }
        }

        return viewModel;
    }

    /**
     * 
     */
    @Test
    public void test08() {
        final ViewContext viewContext = 
                updateWith(createViewContext(), attachRenderings(createSimpleNetwork()));

        final KRectangle rect =
                Iterators.getNext(Iterators.filter(viewContext.getViewModel().eAllContents(),
                        KRectangle.class), null);

        final Object source = viewContext.getSourceElement(rect);

       EcoreUtil.remove(rect);

       Assert.assertNull(viewContext.getSourceElement(rect));
       Assert.assertNull(viewContext.getTargetElement(source, null));

        // 1 (root) + 1 (child node) 1*3 (node+rect+ktext) + 1 (edge)
        checkTracerMaps(viewContext, 6);
    }
    
    /**
     * 
     */
    @Test
    public void test09() {
        final ViewContext viewContext = 
                updateWith(createViewContext(), createSimpleNetwork());
        
        // 1 (root) + 2*1 (child nodes) + 1 (edge)
        checkTracerMaps(viewContext, 4);

        final KText text = KRenderingFactory.eINSTANCE.createKText();
        final KRectangle rect = KRenderingFactory.eINSTANCE.createKRectangle();
        rect.getChildren().add(text);
        viewContext.getViewModel().getChildren().get(0).getData().add(rect);

        // 1 (root) + 2*1 (child nodes) + 1 (edge)
       checkTracerMaps(viewContext, 4);
        
       final EObject source = new EObjectImpl() { };
       
       viewContext.associateSourceTargetPair(source, text);

       // 1 (root) + 2+1 (child nodes, first with text) + 1 (edge)
       checkTracerMaps(viewContext, 5);

       viewContext.associateSourceTargetPair(null, text);

       // 1 (root) + 2+1 (child nodes, first with text) + 1 (edge)
       checkTracerMaps(viewContext, 4);

       viewContext.associateSourceTargetPair(source, text);

       // 1 (root) + 2+1 (child nodes, first with text) + 1 (edge)
       checkTracerMaps(viewContext, 5);
    }
    
    /**
     * 
     */
    @Test
    public void test10() {
        final ViewContext viewContext = 
                updateWith(createViewContext(), createSimpleNetwork());
        
        // 1 (root) + 2*1 (child nodes) + 1 (edge)
        checkTracerMaps(viewContext, 4);

        final KText text = KRenderingFactory.eINSTANCE.createKText();
        final KRectangle rect = KRenderingFactory.eINSTANCE.createKRectangle();
        rect.getChildren().add(text);
        viewContext.getViewModel().getChildren().get(0).getData().add(rect);

        // 1 (root) + 2*1 (child nodes) + 1 (edge)
       checkTracerMaps(viewContext, 4);
        
       final EObject source = new EObjectImpl() { };
       
       viewContext.associateSourceTargetPair(source, text);

       // 1 (root) + 2+1 (child nodes, first with text) + 1 (edge)
       checkTracerMaps(viewContext, 5);
       
       final EObject source2 = new EObjectImpl() { };
       
       viewContext.associateSourceTargetPair(source2, text);
       
       // 1 (root) + 2+1 (child nodes, first with text) + 1 (edge)
       checkTracerMaps(viewContext, 5);
       
       Assert.assertSame(source2, viewContext.getSourceElement(text));

    }
    
    /**
     * Tests if the SourceModelTrackingAdapter works with the incremental update.
     * 
     * Tests issue #6.
     */
    @Test
    public void testIncrementalUpdate() {
        final KNode rootNode = createSimpleNetwork();
        final KNode childNode = rootNode.getChildren().get(1);

        final EObject rootSource = new EObjectImpl() { };
        final EObject childSource = new EObjectImpl() { };

        rootNode.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, rootSource);
        childNode.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, childSource);

        final ViewContext viewContext = createViewContext();
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), rootNode, viewContext);

        // The two elements we set the property for.
        checkTracerMaps(viewContext, 2);

        Assert.assertSame(childSource, viewContext.getSourceElement(childNode));

        final KNode newRoot = createSimpleNetwork();
        final KNode newChildNode = newRoot.getChildren().get(1);

        newRoot.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, rootSource);
        newChildNode.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, childSource);

        final ViewContext newViewContext = createViewContext();
        INCREMENTAL_UPDATE_STRATEGY.update(newViewContext.getViewModel(), newRoot, newViewContext);

        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), newViewContext.getViewModel(), viewContext);

        // The same two elements as above.
        checkTracerMaps(viewContext, 2);

        Assert.assertSame(childSource, viewContext.getSourceElement(childNode));
    }
}
