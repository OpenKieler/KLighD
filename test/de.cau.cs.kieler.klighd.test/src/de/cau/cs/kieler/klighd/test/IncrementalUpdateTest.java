/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://rtsys.informatik.uni-kiel.de/kieler
 * 
 * Copyright 2020 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klighd.test;

import java.util.ArrayList;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.junit.Assert;
import org.junit.Test;

import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.eclipse.IDiagramWorkbenchPart;
import de.cau.cs.kieler.klighd.incremental.IncrementalUpdateStrategy;
import de.cau.cs.kieler.klighd.internal.util.KlighdInternalProperties;
import de.cau.cs.kieler.klighd.kgraph.KEdge;
import de.cau.cs.kieler.klighd.kgraph.KGraphElement;
import de.cau.cs.kieler.klighd.kgraph.KGraphFactory;
import de.cau.cs.kieler.klighd.kgraph.KIdentifier;
import de.cau.cs.kieler.klighd.kgraph.KLabel;
import de.cau.cs.kieler.klighd.kgraph.KNode;
import de.cau.cs.kieler.klighd.kgraph.KPort;
import de.cau.cs.kieler.klighd.kgraph.util.KGraphUtil;

/**
 * Tests if the {@link IncrementalUpdateStrategy} correctly updates every aspect of the model
 * to match the old model. This includes correct adding, removing and merging of elements, as well
 * as preserving the order of elements.
 * 
 * @author nre
 */
public class IncrementalUpdateTest {
    
    private static final IncrementalUpdateStrategy INCREMENTAL_UPDATE_STRATEGY = new IncrementalUpdateStrategy();
    
    private ViewContext createViewContext() {
        return new ViewContext(null);
    }
    
    private KNode createTestGraph() {
        final KNode nodeRoot = KGraphUtil.createInitializedNode();
        addIdentifier(nodeRoot, "nodeRoot");
        
        final KNode nodeA = KGraphUtil.createInitializedNode();
        addIdentifier(nodeA, "nodeA");
        nodeA.setParent(nodeRoot);
        
        final KNode nodeB = KGraphUtil.createInitializedNode();
        addIdentifier(nodeB, "nodeB");
        nodeB.setParent(nodeRoot);
        
        final KNode nodeC = KGraphUtil.createInitializedNode();
        addIdentifier(nodeC, "nodeC");
        nodeC.setParent(nodeRoot);
        
        final KEdge edgeAB = KGraphUtil.createInitializedEdge();
        addIdentifier(edgeAB, "edgeAB");
        edgeAB.setSource(nodeA);
        edgeAB.setTarget(nodeB);
        
        final KEdge edgeAC = KGraphUtil.createInitializedEdge();
        addIdentifier(edgeAC, "edgeAC");
        edgeAC.setSource(nodeA);
        edgeAC.setTarget(nodeC);
        
        final KEdge edgeBC = KGraphUtil.createInitializedEdge();
        addIdentifier(edgeBC, "edgeBC");
        edgeBC.setSource(nodeB);
        edgeBC.setTarget(nodeC);
        
        final KEdge edgeCB = KGraphUtil.createInitializedEdge();
        addIdentifier(edgeCB, "edgeCB");
        edgeCB.setSource(nodeC);
        edgeCB.setTarget(nodeB);
        
        final KPort portA1 = KGraphUtil.createInitializedPort();
        addIdentifier(portA1, "portA1");
        portA1.setNode(nodeA);
        
        final KPort portA2 = KGraphUtil.createInitializedPort();
        addIdentifier(portA2, "portA2");
        portA2.setNode(nodeA);
        
        final KPort portB1 = KGraphUtil.createInitializedPort();
        addIdentifier(portB1, "portB1");
        portB1.setNode(nodeB);
        
        final KEdge edgeA1B1 = KGraphUtil.createInitializedEdge();
        addIdentifier(edgeA1B1, "edgeA1B1");
        edgeA1B1.setSource(nodeA);
        edgeA1B1.setSourcePort(portA1);
        edgeA1B1.setTarget(nodeB);
        edgeA1B1.setTargetPort(portB1);
        
        final KEdge edgeA1B1_2 = KGraphUtil.createInitializedEdge();
        addIdentifier(edgeA1B1_2, "edgeA1B1_2");
        edgeA1B1_2.setSource(nodeA);
        edgeA1B1_2.setSourcePort(portA1);
        edgeA1B1_2.setTarget(nodeB);
        edgeA1B1_2.setTargetPort(portB1);
        
        final KLabel labelA1 = KGraphUtil.createInitializedLabel(nodeA);
        addIdentifier(labelA1, "labelA1");
        labelA1.setText("labelA1");
        
        final KLabel labelA2 = KGraphUtil.createInitializedLabel(nodeA);
        addIdentifier(labelA2, "labelA2");
        labelA2.setText("labelA2");
        
        return nodeRoot;
    }
    
    private void addIdentifier(KGraphElement element, String id) {
        final KIdentifier identifier = KGraphFactory.eINSTANCE.createKIdentifier();
        identifier.setId(id);
        element.getData().add(identifier);
    }
    
    /**
     * Tests adding a node. Checks if the node is added and if it is added at the correct position.
     */
    @Test
    public void testAddNode() {
        final KNode baseGraph = createTestGraph();
        final KNode newGraph = createTestGraph();
        
        final KNode newNode = KGraphUtil.createInitializedNode();
        final EObject newNodeSource = new EObjectImpl() { };
        newNode.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, newNodeSource);
        final int newNodePosition = 0;
        
        newGraph.getChildren().add(newNodePosition, newNode);
        
        final ViewContext viewContext = createViewContext();
        // Initialize the view context with the base graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), baseGraph, viewContext);
        // Update with the new graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), newGraph, viewContext);
        
        // Assert the new node is in the updated base model.
        EObject baseNewNode = viewContext.getTargetElements(newNodeSource).stream().findFirst().orElse(null);
        Assert.assertNotNull(baseNewNode);
        Assert.assertTrue(baseNewNode instanceof KNode);
        // Assert that the new node is in the same position in the updated base graph as it is in the new graph.
        int baseNodePosition = viewContext.getViewModel().getChildren().indexOf(baseNewNode);
        Assert.assertSame(newNodePosition, baseNodePosition);
    }
    
    /**
     * Tests adding multiple nodes. Checks if the nodes are added and if they are added at the correct positions.
     */
    @Test
    public void testAddMultipleNodes() {
        final KNode baseGraph = KGraphUtil.createInitializedNode();
        final KNode nodeA0 = KGraphUtil.createInitializedNode();
        addIdentifier(nodeA0, "nodeA");
        nodeA0.setParent(baseGraph);
        final KNode newGraph = KGraphUtil.createInitializedNode();
        final KNode nodeA1 = KGraphUtil.createInitializedNode();
        addIdentifier(nodeA1, "nodeA");
        nodeA1.setParent(newGraph);

        final KNode newNode0 = KGraphUtil.createInitializedNode();
        final KNode newNode1 = KGraphUtil.createInitializedNode();
        final KNode newNode2 = KGraphUtil.createInitializedNode();
        final EObject newNodeSource0 = new EObjectImpl() { };
        final EObject newNodeSource1 = new EObjectImpl() { };
        final EObject newNodeSource2 = new EObjectImpl() { };
        newNode0.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, newNodeSource0);
        newNode1.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, newNodeSource1);
        newNode2.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, newNodeSource2);
        final int newNode0Position = 0;
        final int newNode1Position = 1;
        final int newNode2Position = 2;

        newGraph.getChildren().add(newNode0Position, newNode0);
        newGraph.getChildren().add(newNode1Position, newNode1);
        newGraph.getChildren().add(newNode2Position, newNode2);
        
        final ViewContext viewContext = createViewContext();
        // Initialize the view context with the base graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), baseGraph, viewContext);
        // Update with the new graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), newGraph, viewContext);
        
        // Assert the new nodes are in the updated base model.
        EObject baseNewNode0 = viewContext.getTargetElements(newNodeSource0).stream().findFirst().orElse(null);
        EObject baseNewNode1 = viewContext.getTargetElements(newNodeSource1).stream().findFirst().orElse(null);
        EObject baseNewNode2 = viewContext.getTargetElements(newNodeSource2).stream().findFirst().orElse(null);
        Assert.assertNotNull(baseNewNode0);
        Assert.assertNotNull(baseNewNode1);
        Assert.assertNotNull(baseNewNode2);
        Assert.assertTrue(baseNewNode0 instanceof KNode);
        Assert.assertTrue(baseNewNode1 instanceof KNode);
        Assert.assertTrue(baseNewNode2 instanceof KNode);
        // Assert that the new nodes are in the same position in the updated base graph as they are in the new graph.
        int baseNode0Position = viewContext.getViewModel().getChildren().indexOf(baseNewNode0);
        int baseNode1Position = viewContext.getViewModel().getChildren().indexOf(baseNewNode1);
        int baseNode2Position = viewContext.getViewModel().getChildren().indexOf(baseNewNode2);
        Assert.assertSame(newNode0Position, baseNode0Position);
        Assert.assertSame(newNode1Position, baseNode1Position);
        Assert.assertSame(newNode2Position, baseNode2Position);
    }
    
    /**
     * Tests removing a node.
     */
    @Test
    public void testRemoveNode() {
        final int toRemovePosition = 0;
        final KNode baseGraph = createTestGraph();
        final KNode baseNodeToRemove = baseGraph.getChildren().get(toRemovePosition);
        final EObject baseNodeToRemoveSource = new EObjectImpl() { };
        baseNodeToRemove.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, baseNodeToRemoveSource);
        
        final KNode newGraph = createTestGraph();
        
        removeNode(newGraph.getChildren().get(toRemovePosition));
        
        final ViewContext viewContext = createViewContext();
        // Initialize the view context with the base graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), baseGraph, viewContext);
        // Update with the new graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), newGraph, viewContext);
        
        // Assert the removed node is not in the updated base model.
        EObject baseRemovedNode = viewContext.getTargetElements(baseNodeToRemoveSource).stream().findFirst()
            .orElse(null);
        Assert.assertNull(baseRemovedNode);
    }
    
    /**
     * Tests updating a node's position.
     */
    @Test
    public void testUpdateNodePosition() {
        final int oldPosition = 0;
        final int newPosition = 1;
        final KNode baseGraph = createTestGraph();
        final KNode baseNodeToMove = baseGraph.getChildren().get(oldPosition);
        final EObject nodeToMoveSource = new EObjectImpl() { };
        baseNodeToMove.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, nodeToMoveSource);
        
        final KNode newGraph = createTestGraph();
        final KNode newNodeToMove = newGraph.getChildren().get(oldPosition);
        newNodeToMove.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, nodeToMoveSource);
        newGraph.getChildren().move(newPosition, newNodeToMove);
        
        
        final ViewContext viewContext = createViewContext();
        // Initialize the view context with the base graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), baseGraph, viewContext);
        // Update with the new graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), newGraph, viewContext);
        
        // Assert the moved node is in the new position in the updated base model.
        EObject baseMovedNode = viewContext.getTargetElements(nodeToMoveSource).stream().findFirst()
            .orElse(null);
        Assert.assertNotNull(baseMovedNode);
        Assert.assertTrue(baseMovedNode instanceof KNode);
        int baseMovedNodePosition = viewContext.getViewModel().getChildren().indexOf(baseMovedNode);
        Assert.assertSame(newPosition, baseMovedNodePosition);
    }
    
    /**
     * Tests adding a port. Checks if the port is added and if it is added at the correct position.
     */
    @Test
    public void testAddPort() {
        final KNode baseGraph = createTestGraph();
        final KNode newGraph = createTestGraph();
        
        final KPort newPort = KGraphUtil.createInitializedPort();
        final EObject newPortSource = new EObjectImpl() { };
        newPort.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, newPortSource);
        final int parentNodePosition = 0;
        final int newPortPosition = 0;
        
        newGraph.getChildren().get(parentNodePosition).getPorts().add(newPortPosition, newPort);
        
        final ViewContext viewContext = createViewContext();
        // Initialize the view context with the base graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), baseGraph, viewContext);
        // Update with the new graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), newGraph, viewContext);
        
        // Assert the new port is in the updated base model.
        EObject baseNewPort = viewContext.getTargetElements(newPortSource).stream().findFirst().orElse(null);
        Assert.assertNotNull(baseNewPort);
        Assert.assertTrue(baseNewPort instanceof KPort);
        // Assert that the new port is in the same position in the updated base graph as it is in the new graph.
        int basePortPosition = viewContext.getViewModel().getChildren().get(parentNodePosition).getPorts()
            .indexOf(baseNewPort);
        Assert.assertSame(newPortPosition, basePortPosition);
    }
    
    /**
     * Tests removing a port.
     */
    @Test
    public void testRemovePort() {
        final int parentNodePosition = 0;
        final int toRemovePosition = 0;
        final KNode baseGraph = createTestGraph();
        final KPort basePortToRemove = baseGraph.getChildren().get(parentNodePosition).getPorts().get(toRemovePosition);
        final EObject basePortToRemoveSource = new EObjectImpl() { };
        basePortToRemove.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, basePortToRemoveSource);
        
        final KNode newGraph = createTestGraph();
        
        newGraph.getChildren().get(parentNodePosition).getPorts().remove(toRemovePosition);
        
        final ViewContext viewContext = createViewContext();
        // Initialize the view context with the base graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), baseGraph, viewContext);
        // Update with the new graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), newGraph, viewContext);
        
        // Assert the removed port is not in the updated base model.
        EObject baseRemovedPort = viewContext.getTargetElements(basePortToRemoveSource).stream().findFirst()
            .orElse(null);
        Assert.assertNull(baseRemovedPort);
    }
    
    /**
     * Tests updating a port's position.
     */
    @Test
    public void testUpdatePortPosition() {
        final int oldPosition = 0;
        final int newPosition = 1;
        final int parentNodePosition = 0;
        final KNode baseGraph = createTestGraph();
        final KPort basePortToMove = baseGraph.getChildren().get(parentNodePosition).getPorts().get(oldPosition);
        final EObject portToMoveSource = new EObjectImpl() { };
        basePortToMove.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, portToMoveSource);
        
        final KNode newGraph = createTestGraph();
        final KPort newPortToMove = newGraph.getChildren().get(parentNodePosition).getPorts().get(oldPosition);
        newPortToMove.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, portToMoveSource);
        newGraph.getChildren().get(parentNodePosition).getPorts().move(newPosition, newPortToMove);
        
        
        final ViewContext viewContext = createViewContext();
        // Initialize the view context with the base graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), baseGraph, viewContext);
        // Update with the new graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), newGraph, viewContext);
        
        // Assert the moved port is in the new position in the updated base model.
        EObject baseMovedPort = viewContext.getTargetElements(portToMoveSource).stream().findFirst()
            .orElse(null);
        Assert.assertNotNull(baseMovedPort);
        Assert.assertTrue(baseMovedPort instanceof KPort);
        int baseMovedPortPosition = viewContext.getViewModel().getChildren().get(parentNodePosition).getPorts()
                .indexOf(baseMovedPort);
        Assert.assertSame(newPosition, baseMovedPortPosition);
    }
    
    /**
     * Tests adding a label. Checks if the label is added and if it is added at the correct position.
     */
    @Test
    public void testAddLabel() {
        final int parentNodePosition = 0;
        final int newLabelPosition = 0;
        final KNode baseGraph = createTestGraph();
        final KNode newGraph = createTestGraph();
        
        final KNode parentNode = newGraph.getChildren().get(parentNodePosition);
        final KLabel newLabel = KGraphUtil.createInitializedLabel(parentNode);
        final EObject newLabelSource = new EObjectImpl() { };
        newLabel.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, newLabelSource);
        parentNode.getLabels().move(newLabelPosition, newLabel);
        
        final ViewContext viewContext = createViewContext();
        // Initialize the view context with the base graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), baseGraph, viewContext);
        // Update with the new graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), newGraph, viewContext);
        
        // Assert the new label is in the updated base model.
        EObject baseNewLabel = viewContext.getTargetElements(newLabelSource).stream().findFirst().orElse(null);
        Assert.assertNotNull(baseNewLabel);
        Assert.assertTrue(baseNewLabel instanceof KLabel);
        // Assert that the new label is in the same position in the updated base graph as it is in the new graph.
        int baseLabelPosition = viewContext.getViewModel().getChildren().get(parentNodePosition).getLabels()
            .indexOf(baseNewLabel);
        Assert.assertSame(newLabelPosition, baseLabelPosition);
    }
    
    /**
     * Tests removing a label.
     */
    @Test
    public void testRemoveLabel() {
        final int parentNodePosition = 0;
        final int toRemovePosition = 0;
        final KNode baseGraph = createTestGraph();
        final KLabel baseLabelToRemove = baseGraph.getChildren().get(parentNodePosition).getLabels().get(toRemovePosition);
        final EObject baseLabelToRemoveSource = new EObjectImpl() { };
        baseLabelToRemove.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, baseLabelToRemoveSource);
        
        final KNode newGraph = createTestGraph();
        
        newGraph.getChildren().get(parentNodePosition).getLabels().remove(toRemovePosition);
        
        final ViewContext viewContext = createViewContext();
        // Initialize the view context with the base graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), baseGraph, viewContext);
        // Update with the new graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), newGraph, viewContext);
        
        // Assert the removed label is not in the updated base model.
        EObject baseRemovedLabel = viewContext.getTargetElements(baseLabelToRemoveSource).stream().findFirst()
            .orElse(null);
        Assert.assertNull(baseRemovedLabel);
    }
    
    /**
     * Tests updating a label's position.
     */
    @Test
    public void testUpdateLabelPosition() {
        final int oldPosition = 0;
        final int newPosition = 1;
        final int parentNodePosition = 0;
        final KNode baseGraph = createTestGraph();
        final KLabel baseLabelToMove = baseGraph.getChildren().get(parentNodePosition).getLabels().get(oldPosition);
        final EObject labelToMoveSource = new EObjectImpl() { };
        baseLabelToMove.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, labelToMoveSource);
        
        final KNode newGraph = createTestGraph();
        final KLabel newLabelToMove = newGraph.getChildren().get(parentNodePosition).getLabels().get(oldPosition);
        newLabelToMove.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, labelToMoveSource);
        newGraph.getChildren().get(parentNodePosition).getLabels().move(newPosition, newLabelToMove);
        
        
        final ViewContext viewContext = createViewContext();
        // Initialize the view context with the base graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), baseGraph, viewContext);
        // Update with the new graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), newGraph, viewContext);
        
        // Assert the moved label is in the new position in the updated base model.
        EObject baseMovedLabel = viewContext.getTargetElements(labelToMoveSource).stream().findFirst()
            .orElse(null);
        Assert.assertNotNull(baseMovedLabel);
        Assert.assertTrue(baseMovedLabel instanceof KLabel);
        int baseMovedLabelPosition = viewContext.getViewModel().getChildren().get(parentNodePosition).getLabels()
                .indexOf(baseMovedLabel);
        Assert.assertSame(newPosition, baseMovedLabelPosition);
    }
    
    /**
     * Tests adding an edge plainly on a node. Checks if the edge is added and if it is added at the correct position in
     * various reference lists.
     */
    @Test
    public void testAddEdgePlain() {
        final KNode baseGraph = createTestGraph();
        final KNode newGraph = createTestGraph();
        
        final KEdge newEdge = KGraphUtil.createInitializedEdge();
        final EObject newEdgeSource = new EObjectImpl() { };
        newEdge.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, newEdgeSource);
        
        final int sourceNodePosition = 0;
        final int targetNodePosition = 1;
        final KNode sourceNode = newGraph.getChildren().get(sourceNodePosition);
        final KNode targetNode = newGraph.getChildren().get(targetNodePosition);
        newEdge.setSource(sourceNode);
        newEdge.setTarget(targetNode);
        final int newEdgeSourcePosition = 0;
        final int newEdgeTargetPosition = 0;
        sourceNode.getOutgoingEdges().move(newEdgeSourcePosition, newEdge);
        targetNode.getIncomingEdges().move(newEdgeTargetPosition, newEdge);
        
        final ViewContext viewContext = createViewContext();
        // Initialize the view context with the base graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), baseGraph, viewContext);
        // Update with the new graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), newGraph, viewContext);
        
        // Assert the new edge is in the updated base model.
        EObject baseNewEdge = viewContext.getTargetElements(newEdgeSource).stream().findFirst().orElse(null);
        Assert.assertNotNull(baseNewEdge);
        Assert.assertTrue(baseNewEdge instanceof KEdge);
        // Assert that the new edge is in the same positions in the updated base graph as it is in the new graph.
        int baseEdgeSourcePosition = viewContext.getViewModel().getChildren().get(sourceNodePosition).getOutgoingEdges()
            .indexOf(baseNewEdge);
        Assert.assertSame(newEdgeSourcePosition, baseEdgeSourcePosition);
        int baseEdgeTargetPosition = viewContext.getViewModel().getChildren().get(targetNodePosition).getIncomingEdges()
            .indexOf(baseNewEdge);
        Assert.assertSame(newEdgeTargetPosition, baseEdgeTargetPosition);
    }
    
    /**
     * Tests adding an edge on a port of a node. Checks if the edge is added and if it is added at the correct position
     * in various reference lists.
     */
    @Test
    public void testAddEdgeOnPort() {
        final KNode baseGraph = createTestGraph();
        final KNode newGraph = createTestGraph();
        
        final KEdge newEdge = KGraphUtil.createInitializedEdge();
        final EObject newEdgeSource = new EObjectImpl() { };
        newEdge.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, newEdgeSource);
        
        final int sourceNodePosition = 0;
        final int targetNodePosition = 1;
        final int sourceNodePortPosition = 0;
        final int targetNodePortPosition = 0;
        final KNode sourceNode = newGraph.getChildren().get(sourceNodePosition);
        final KNode targetNode = newGraph.getChildren().get(targetNodePosition);
        final KPort sourcePort = sourceNode.getPorts().get(sourceNodePortPosition);
        final KPort targetPort = targetNode.getPorts().get(targetNodePortPosition);
        newEdge.setSourcePort(sourcePort);
        newEdge.setSource(sourceNode);
        newEdge.setTargetPort(targetPort);
        newEdge.setTarget(targetNode);
        final int newEdgeSourcePosition = 0;
        final int newEdgeTargetPosition = 0;
        sourcePort.getEdges().move(newEdgeSourcePosition, newEdge);
        targetPort.getEdges().move(newEdgeTargetPosition, newEdge);
        
        final ViewContext viewContext = createViewContext();
        // Initialize the view context with the base graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), baseGraph, viewContext);
        // Update with the new graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), newGraph, viewContext);
        
        // Assert the new edge is in the updated base model.
        EObject baseNewEdge = viewContext.getTargetElements(newEdgeSource).stream().findFirst().orElse(null);
        Assert.assertNotNull(baseNewEdge);
        Assert.assertTrue(baseNewEdge instanceof KEdge);
        // Assert that the new edge is in the same positions in the updated base graph as it is in the new graph.
        int baseEdgeSourcePosition = viewContext.getViewModel().getChildren().get(sourceNodePosition).getPorts()
                .get(sourceNodePortPosition).getEdges().indexOf(baseNewEdge);
        Assert.assertSame(newEdgeSourcePosition, baseEdgeSourcePosition);
        int baseEdgeTargetPosition = viewContext.getViewModel().getChildren().get(targetNodePosition).getPorts()
                .get(targetNodePortPosition).getEdges().indexOf(baseNewEdge);
        Assert.assertSame(newEdgeTargetPosition, baseEdgeTargetPosition);
    }
    
    /**
     * Tests removing an edge plainly on a node.
     */
    @Test
    public void testRemoveEdgePlain() {
        final int sourceNodePosition = 0;
        final int toRemovePosition = 0;
        final KNode baseGraph = createTestGraph();
        final KEdge baseEdgeToRemove = baseGraph.getChildren().get(sourceNodePosition).getOutgoingEdges()
            .get(toRemovePosition);
        final EObject baseEdgeToRemoveSource = new EObjectImpl() { };
        baseEdgeToRemove.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, baseEdgeToRemoveSource);
        
        final KNode newGraph = createTestGraph();
        
        removeEdge(newGraph.getChildren().get(sourceNodePosition).getOutgoingEdges().get(toRemovePosition));
        
        final ViewContext viewContext = createViewContext();
        // Initialize the view context with the base graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), baseGraph, viewContext);
        // Update with the new graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), newGraph, viewContext);
        
        // Assert the removed edge is not in the updated base model.
        EObject baseRemovedEdge = viewContext.getTargetElements(baseEdgeToRemoveSource).stream().findFirst()
            .orElse(null);
        Assert.assertNull(baseRemovedEdge);
    }
    
    /**
     * Tests removing an edge on a port of a node.
     */
    @Test
    public void testRemoveEdgeOnPort() {
        final int sourceNodePosition = 0;
        final int sourceNodePortPosition = 0;
        final int toRemovePosition = 0;
        final KNode baseGraph = createTestGraph();
        final KEdge baseEdgeToRemove = baseGraph.getChildren().get(sourceNodePosition).getPorts()
            .get(sourceNodePortPosition).getEdges().get(toRemovePosition);
        final EObject baseEdgeToRemoveSource = new EObjectImpl() { };
        baseEdgeToRemove.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, baseEdgeToRemoveSource);
        
        final KNode newGraph = createTestGraph();
        
        removeEdge(newGraph.getChildren().get(sourceNodePosition).getPorts().get(sourceNodePortPosition).getEdges()
            .get(toRemovePosition));
        
        final ViewContext viewContext = createViewContext();
        // Initialize the view context with the base graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), baseGraph, viewContext);
        // Update with the new graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), newGraph, viewContext);
        
        // Assert the removed edge is not in the updated base model.
        EObject baseRemovedEdge = viewContext.getTargetElements(baseEdgeToRemoveSource).stream().findFirst()
            .orElse(null);
        Assert.assertNull(baseRemovedEdge);
    }
    
    /**
     * Tests updating the position of an edge plainly on a node.
     */
    @Test
    public void testUpdateEdgePositionPlain() {
        final int sourceNodePosition = 0;
        final int oldSourcePosition = 0;
        final int newSourcePosition = 1;
        final int newTargetPosition = 1;
        final KNode baseGraph = createTestGraph();
        final KEdge baseEdgeToMove = baseGraph.getChildren().get(sourceNodePosition).getOutgoingEdges()
            .get(oldSourcePosition);
        final EObject edgeToMoveSource = new EObjectImpl() { };
        baseEdgeToMove.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, edgeToMoveSource);
        
        final KNode newGraph = createTestGraph();
        final KEdge newEdgeToMove = newGraph.getChildren().get(sourceNodePosition).getOutgoingEdges()
            .get(oldSourcePosition);
        newEdgeToMove.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, edgeToMoveSource);
        final KNode newTargetNode = newEdgeToMove.getTarget();
        newGraph.getChildren().get(sourceNodePosition).getOutgoingEdges().move(newSourcePosition, newEdgeToMove);
        newTargetNode.getIncomingEdges().move(newTargetPosition, newEdgeToMove);
        
        
        final ViewContext viewContext = createViewContext();
        // Initialize the view context with the base graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), baseGraph, viewContext);
        // Update with the new graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), newGraph, viewContext);
        
        // Assert the moved edge is in the new positions in the updated base model.
        EObject baseMovedEdge = viewContext.getTargetElements(edgeToMoveSource).stream().findFirst()
            .orElse(null);
        Assert.assertNotNull(baseMovedEdge);
        Assert.assertTrue(baseMovedEdge instanceof KEdge);
        int baseMovedEdgeSourcePosition = viewContext.getViewModel().getChildren().get(sourceNodePosition)
            .getOutgoingEdges().indexOf(baseMovedEdge);
        Assert.assertSame(newSourcePosition, baseMovedEdgeSourcePosition);
        int baseMovedEdgeTargetPosition = ((KEdge) baseMovedEdge).getTarget().getIncomingEdges().indexOf(baseMovedEdge);
        Assert.assertSame(newTargetPosition, baseMovedEdgeTargetPosition);
    }
    
    /**
     * Tests updating the position of an edge on a port of a node.
     */
    @Test
    public void testUpdateEdgePositionOnPort() {
        final int sourceNodePosition = 0;
        final int sourceNodePortPosition = 0;
        final int oldSourcePosition = 0;
        final int newSourcePosition = 1;
        final int newTargetPosition = 1;
        final KNode baseGraph = createTestGraph();
        final KEdge baseEdgeToMove = baseGraph.getChildren().get(sourceNodePosition).getPorts()
            .get(sourceNodePortPosition).getEdges().get(oldSourcePosition);
        final EObject edgeToMoveSource = new EObjectImpl() { };
        baseEdgeToMove.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, edgeToMoveSource);
        
        final KNode newGraph = createTestGraph();
        final KEdge newEdgeToMove = newGraph.getChildren().get(sourceNodePosition).getPorts()
            .get(sourceNodePortPosition).getEdges().get(oldSourcePosition);
        newEdgeToMove.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, edgeToMoveSource);
        final KPort newTargetPort = newEdgeToMove.getTargetPort();
        newGraph.getChildren().get(sourceNodePosition).getPorts().get(sourceNodePortPosition).getEdges()
            .move(newSourcePosition, newEdgeToMove);
        newTargetPort.getEdges().move(newTargetPosition, newEdgeToMove);
        
        
        final ViewContext viewContext = createViewContext();
        // Initialize the view context with the base graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), baseGraph, viewContext);
        // Update with the new graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), newGraph, viewContext);
        
        // Assert the moved edge is in the new positions in the updated base model.
        EObject baseMovedEdge = viewContext.getTargetElements(edgeToMoveSource).stream().findFirst()
            .orElse(null);
        Assert.assertNotNull(baseMovedEdge);
        Assert.assertTrue(baseMovedEdge instanceof KEdge);
        int baseMovedEdgeSourcePosition = viewContext.getViewModel().getChildren().get(sourceNodePosition).getPorts()
                .get(sourceNodePortPosition).getEdges().indexOf(baseMovedEdge);
        Assert.assertSame(newSourcePosition, baseMovedEdgeSourcePosition);
        int baseMovedEdgeTargetPosition = ((KEdge) baseMovedEdge).getTargetPort().getEdges().indexOf(baseMovedEdge);
        Assert.assertSame(newTargetPosition, baseMovedEdgeTargetPosition);
    }

    /**
     * Helper method to correctly remove the given node from a model again.
     * 
     * @param node The node to remove.
     */
    private void removeNode(KNode node) {
        for (KEdge edge : new ArrayList<KEdge>(node.getOutgoingEdges())) {
            edge.setSource(null);
            edge.setTarget(null);
            edge.setSourcePort(null);
            edge.setTargetPort(null);
        }
        for (KEdge edge : new ArrayList<KEdge>(node.getIncomingEdges())) {
            edge.setSource(null);
            edge.setTarget(null);
            edge.setSourcePort(null);
            edge.setTargetPort(null);
        }
        node.getParent().getChildren().remove(node);
        
    }
    
    /**
     * Helper method to correctly remove the given edge from a model again.
     * 
     * @param edge The edge to remove.
     */
    private void removeEdge(KEdge edge) {
        edge.setSource(null);
        edge.setTarget(null);
        edge.setSourcePort(null);
        edge.setTargetPort(null);
    }
    
    /**
     * Tests adding connected nodes without IDs as in KLighD issue 48.
     * See https://github.com/kieler/KLighD/issues/48
     */
    @Test
    public void testIssue48() {
        // Create the base graph with two connected nodes.
        final KNode baseGraph = KGraphUtil.createInitializedNode();
        final KNode baseNode3 = KGraphUtil.createInitializedNode();
        final KNode baseNode2 = KGraphUtil.createInitializedNode();
        baseNode3.setParent(baseGraph);
        baseNode2.setParent(baseGraph);
        
        final KPort basePort2In = KGraphUtil.createInitializedPort();
        basePort2In.setNode(baseNode2);
        final KPort basePort2Out = KGraphUtil.createInitializedPort();
        basePort2Out.setNode(baseNode2);

        final KPort basePort3In = KGraphUtil.createInitializedPort();
        basePort3In.setNode(baseNode3);
        final KPort basePort3Out = KGraphUtil.createInitializedPort();
        basePort3Out.setNode(baseNode3);
        
        final KEdge baseEdge23 = KGraphUtil.createInitializedEdge();
        baseEdge23.setSource(baseNode2);
        baseEdge23.setTarget(baseNode3);
        
        // Create the new graph with five connected nodes and ports.
        final KNode newGraph = KGraphUtil.createInitializedNode();
        final KNode newNodeC = KGraphUtil.createInitializedNode();
        final KNode newNode1 = KGraphUtil.createInitializedNode();
        final KNode newNode3 = KGraphUtil.createInitializedNode();
        final KNode newNodeL = KGraphUtil.createInitializedNode();
        final KNode newNode2 = KGraphUtil.createInitializedNode();
        newNodeC.setParent(newGraph);
        newNode1.setParent(newGraph);
        newNode3.setParent(newGraph);
        newNodeL.setParent(newGraph);
        newNode2.setParent(newGraph);
        final EObject newNodeSourceC = new EObjectImpl() { };
        final EObject newNodeSource1 = new EObjectImpl() { };
        final EObject newNodeSource3 = new EObjectImpl() { };
        final EObject newNodeSourceL = new EObjectImpl() { };
        final EObject newNodeSource2 = new EObjectImpl() { };
        newNodeC.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, newNodeSourceC);
        newNode1.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, newNodeSource1);
        newNode3.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, newNodeSource3);
        newNodeL.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, newNodeSourceL);
        newNode2.setProperty(KlighdInternalProperties.MODEL_ELEMEMT, newNodeSource2);

        final KPort newPortCOut = KGraphUtil.createInitializedPort();
        newPortCOut.setNode(newNodeC);
        final KPort newPort1In = KGraphUtil.createInitializedPort();
        newPort1In.setNode(newNode1);
        final KPort newPort1Out = KGraphUtil.createInitializedPort();
        newPort1Out.setNode(newNode1);
        final KPort newPort3In = KGraphUtil.createInitializedPort();
        newPort3In.setNode(newNode3);
        final KPort newPort3Out = KGraphUtil.createInitializedPort();
        newPort3Out.setNode(newNode3);
        final KPort newPortLIn = KGraphUtil.createInitializedPort();
        newPortLIn.setNode(newNodeL);
        final KPort newPortLOut = KGraphUtil.createInitializedPort();
        newPortLOut.setNode(newNodeL);
        final KPort newPort2In = KGraphUtil.createInitializedPort();
        newPort2In.setNode(newNode2);
        final KPort newPort2Out = KGraphUtil.createInitializedPort();
        newPort2Out.setNode(newNode2);
        
        final KEdge newEdgeC1 = KGraphUtil.createInitializedEdge();
        newEdgeC1.setSource(newNodeC);
        newEdgeC1.setSourcePort(newPortCOut);
        newEdgeC1.setTarget(newNode1);
        newEdgeC1.setTargetPort(newPort1In);
        final KEdge newEdge1L = KGraphUtil.createInitializedEdge();
        newEdge1L.setSource(newNode1);
        newEdge1L.setSourcePort(newPort1Out);
        newEdge1L.setTarget(newNodeL);
        newEdge1L.setTargetPort(newPortLIn);
        final KEdge newEdge3L = KGraphUtil.createInitializedEdge();
        newEdge3L.setSource(newNode3);
        newEdge3L.setSourcePort(newPort3Out);
        newEdge3L.setTarget(newNodeL);
        newEdge3L.setTargetPort(newPortLIn);
        final KEdge newEdgeL2 = KGraphUtil.createInitializedEdge();
        newEdgeL2.setSource(newNodeL);
        newEdgeL2.setSourcePort(newPortLOut);
        newEdgeL2.setTarget(newNode2);
        newEdgeL2.setTargetPort(newPort2In);
        
        // The test for these graphs.
        
        final ViewContext viewContext = createViewContext();
        // Initialize the view context with the base graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), baseGraph, viewContext);
        // Update with the new graph.
        INCREMENTAL_UPDATE_STRATEGY.update(viewContext.getViewModel(), newGraph, viewContext);
        
        // Assert the new nodes are in the updated base model.
        EObject baseNewNodeC = viewContext.getTargetElements(newNodeSourceC).stream().findFirst().orElse(null);
        EObject baseNewNode1 = viewContext.getTargetElements(newNodeSource1).stream().findFirst().orElse(null);
        EObject baseNewNode3 = viewContext.getTargetElements(newNodeSource3).stream().findFirst().orElse(null);
        EObject baseNewNodeL = viewContext.getTargetElements(newNodeSourceL).stream().findFirst().orElse(null);
        EObject baseNewNode2 = viewContext.getTargetElements(newNodeSource2).stream().findFirst().orElse(null);
        Assert.assertNotNull(baseNewNodeC);
        Assert.assertNotNull(baseNewNode1);
        Assert.assertNotNull(baseNewNode3); // This one failed before the fix because of the ordering.
        Assert.assertNotNull(baseNewNodeL);
        Assert.assertNotNull(baseNewNode2);
        Assert.assertTrue(baseNewNodeC instanceof KNode);
        Assert.assertTrue(baseNewNode1 instanceof KNode);
        Assert.assertTrue(baseNewNode3 instanceof KNode);
        Assert.assertTrue(baseNewNodeL instanceof KNode);
        Assert.assertTrue(baseNewNode2 instanceof KNode);
    }
    
}
