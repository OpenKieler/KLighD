/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2014 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klighd.test;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.hamcrest.CoreMatchers;
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

import de.cau.cs.kieler.core.kgraph.KEdge;
import de.cau.cs.kieler.core.kgraph.KGraphElement;
import de.cau.cs.kieler.core.kgraph.KNode;
import de.cau.cs.kieler.kiml.klayoutdata.KLayoutData;
import de.cau.cs.kieler.kiml.util.KimlUtil;
import de.cau.cs.kieler.klighd.IDiagramWorkbenchPart;
import de.cau.cs.kieler.klighd.IUpdateStrategy;
import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.internal.util.KlighdInternalProperties;
import de.cau.cs.kieler.klighd.internal.util.SourceModelTrackingAdapter;
import de.cau.cs.kieler.klighd.krendering.SimpleUpdateStrategy;
import de.cau.cs.kieler.klighd.syntheses.DuplicatingDiagramSynthesis;

/**
 * @author chsch
 */
public class ViewContextSourceModelTrackingTest {
    
    // CHECKSTYLEOFF MagicNumber

    private static final IUpdateStrategy UPDATE_STRATEGY = new SimpleUpdateStrategy();
    
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

        Assert.assertThat(sourceTargetsMap.values(), IsCollectionWithSize.hasSize(expectedEntryNumber));
        Assert.assertThat(targetSourceMap.entrySet(), IsCollectionWithSize.hasSize(expectedEntryNumber));
    }


    /**
     * A.
     */
    @Test
    public void test1() {
        updateViewModelRootSourceElement(
                createViewContext());
    }
    
    /**
     * A.
     */
    @Test
    public void test2() {
        updateViewModelRootSourceElement(
                updateViewModelRootSourceElement(
                        createViewContext()));
    }

    private ViewContext updateViewModelRootSourceElement(final ViewContext viewContext) {
        final KNode viewModel = viewContext.getViewModel();

        final EObject sourceModel = new EObjectImpl() { };
        final KNode newModel = KimlUtil.createInitializedNode();        
        newModel.getData(KLayoutData.class).setProperty(KlighdInternalProperties.MODEL_ELEMEMT,
                sourceModel);

        UPDATE_STRATEGY.update(viewModel, newModel, viewContext);

        Assert.assertEquals(
                viewContext.getSourceElement(viewContext.getViewModel()),
                sourceModel);
        
        Assert.assertThat(viewContext.getTargetElements(sourceModel),
                IsIterableContainingInOrder.contains((Object) viewContext.getViewModel()));
        
        return viewContext;
    }
    
    
    /**
     * 
     */
    @Test
    public void test3() {
        updateWithSimpleViewModel(createViewContext());
    }
    
    private ViewContext updateWithSimpleViewModel(final ViewContext viewContext) {
        
        final KNode sourceRoot = createSimpleNetwork();
        
        final KNode targetRoot = new DuplicatingDiagramSynthesis().transform(sourceRoot, viewContext);

        UPDATE_STRATEGY.update(viewContext.getViewModel(), targetRoot, viewContext);
        
        Assert.assertEquals(sourceRoot,
                viewContext.getSourceElement(viewContext.getViewModel()));
        
        Assert.assertThat(viewContext.getTargetElements(sourceRoot),
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
        final KNode sourceNode1 = KimlUtil.createInitializedNode();
        final KNode sourceNode2 = KimlUtil.createInitializedNode();
        final KEdge sourceEdge = KimlUtil.createInitializedEdge();
        sourceEdge.setSource(sourceNode1);
        sourceEdge.setTarget(sourceNode2);
        
        final KNode sourceRoot = KimlUtil.createInitializedNode();
        sourceRoot.getChildren().add(sourceNode1);
        sourceRoot.getChildren().add(sourceNode2);
        return sourceRoot;
    }
    
    /**
     * A.
     */
    @Test
    public void test4() {
        final ViewContext viewContext = updateWithSimpleViewModel(createViewContext());
        
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
    public void test5() {
        final ViewContext viewContext = 
                addSubViewModels(updateWithSimpleViewModel(createViewContext()));
        
        final KNode child0 = viewContext.getViewModel().getChildren().get(0);
        
        final Collection<KGraphElement> child0kges =
                Lists.newArrayList(Iterators.filter(child0.eAllContents(), KGraphElement.class));
        
        // note that child0.eAllContents() includes child0's outgoing edge, too! 
        Assert.assertThat(child0kges, IsIterableWithSize.<KGraphElement>iterableWithSize(4));
        
        final Iterable<Object> child0kgesSources = Lists.newArrayList(
                Iterables.transform(child0kges, new Function<KGraphElement, Object>() {

                    public Object apply(final KGraphElement input) {
                        return viewContext.getSourceElement(input);
                    }
                }));
        
        Assert.assertThat(child0kgesSources, IsIterableWithSize.iterableWithSize(4));

        Assert.assertThat(child0kgesSources,
                IsCollectionContaining.hasItem(CoreMatchers.notNullValue()));

        Assert.assertThat(Iterables.filter(child0kgesSources, Predicates.<Object>in(child0kges)),
                IsEmptyIterable.emptyIterable());
        
        final Iterable<EObject> child0kgesSourcesTargets =
                Iterables.transform(child0kgesSources, new Function<Object, EObject>() {

                    public EObject apply(final Object input) {
                        return viewContext.getTargetElement(input, null);
                    }
                });

        Assert.assertThat(child0kgesSourcesTargets, IsIterableWithSize.<EObject>iterableWithSize(4));

        Assert.assertThat(child0kgesSourcesTargets,
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
    public void test6() {
        final ViewContext viewContext = 
                addSubViewModels(updateWithSimpleViewModel(createViewContext()));
        
        viewContext.getViewModel().getChildren().clear();
        
        checkTracerMaps(viewContext, 1);
    }
}