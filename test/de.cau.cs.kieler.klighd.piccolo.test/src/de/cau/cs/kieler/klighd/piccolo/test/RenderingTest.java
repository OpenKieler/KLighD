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
package de.cau.cs.kieler.klighd.piccolo.test;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

import org.eclipse.elk.graph.properties.IProperty;
import org.eclipse.elk.graph.properties.Property;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import de.cau.cs.kieler.kgraph.text.KGraphStandaloneSetup;
import de.cau.cs.kieler.klighd.eclipse.EclipseKlighdConstants;
import de.cau.cs.kieler.klighd.kgraph.KGraphElement;
import de.cau.cs.kieler.klighd.kgraph.KIdentifier;
import de.cau.cs.kieler.klighd.kgraph.KNode;
import de.cau.cs.kieler.klighd.krendering.KBackground;
import de.cau.cs.kieler.klighd.krendering.KEllipse;
import de.cau.cs.kieler.klighd.krendering.KForeground;
import de.cau.cs.kieler.klighd.krendering.KPolygon;
import de.cau.cs.kieler.klighd.krendering.KRectangle;
import de.cau.cs.kieler.klighd.krendering.KRendering;
import de.cau.cs.kieler.klighd.krendering.KRoundedRectangle;
import de.cau.cs.kieler.klighd.krendering.KShadow;
import de.cau.cs.kieler.klighd.krendering.KStyle;
import de.cau.cs.kieler.klighd.piccolo.internal.controller.DiagramController;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KNodeAbstractNode;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KlighdMainCamera;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KlighdPath;
import de.cau.cs.kieler.klighd.piccolo.internal.util.RGBGradient;
import de.cau.cs.kieler.klighd.test.runners.ModelCollectionTestRunner;
import de.cau.cs.kieler.klighd.test.runners.ModelCollectionTestRunner.BundleId;
import de.cau.cs.kieler.klighd.test.runners.ModelCollectionTestRunner.ModelFilter;
import de.cau.cs.kieler.klighd.test.runners.ModelCollectionTestRunner.ModelPath;
import de.cau.cs.kieler.klighd.util.Iterables2;
import de.cau.cs.kieler.klighd.util.ModelingUtil;
import de.cau.cs.kieler.klighd.util.RenderingContextData;
import edu.umd.cs.piccolo.PRoot;

/**
 *
 * Tests that check whether styles and shapes that are defined in the KGraph also show up
 * equivalently in the underlying piccolo graph.
 *
 * @author ckru
 *
 */
@RunWith(ModelCollectionTestRunner.class)
@BundleId("de.cau.cs.kieler.klighd.piccolo.test")
@ModelPath("testModels/")
@ModelFilter("KlighdRenderingTestModel.kgt")
public class RenderingTest {

    private final KNode graph;

    /**
     * Provides a {@link ResourceSet} in order to load the models properly.
     *
     * @return the required {@link ResourceSet}
     */
    @ModelCollectionTestRunner.ResourceSet
    public static ResourceSet getResourceSet() {
        return new KGraphStandaloneSetup().createInjectorAndDoEMFRegistration().getInstance(
                XtextResourceSet.class);
    }

    /**
     * Constructor. Is enabled in that form by the Model {@link ModelCollectionTestRunner}.
     */
    public RenderingTest(final KNode input) {
        graph = input;
    }

    /**
     * Some initialization to insert input KGraph into some Klighd structures.
     *
     * @param n
     *            KNode representing the input KGraph
     */
    @Before
    public void initialize() {
        final KlighdMainCamera camera = new KlighdMainCamera(new PRoot());
        new DiagramController(graph, camera, true, null);
    }

    /**
     * This method just removes the auxiliary information attach by the {@link DiagramController}
     * from 'graph's {@link KGraphElement KGraphElements}, i.e. cleans 'graph' up for the next test.
     */
    @After
    public void dispose() {
        final Predicate<Object> isRenderingContextData = Predicates.instanceOf(RenderingContextData.class);
        for (final KGraphElement e : Iterables2.toIterable(
                ModelingUtil.<KGraphElement>selfAndEAllContentsOfType2(graph, KGraphElement.class))) {
            Iterables.removeIf(e.getData(), isRenderingContextData);
        }
    }


    /** Just copied the property definition from {@link DiagramController}. */
    private static final IProperty<KNodeAbstractNode> REP = new Property<KNodeAbstractNode>(
            "klighd.piccolo.representation");

    /**
     * Test whether the test graphs styles are the same in KGraph notation as well as in the piccolo
     * results.
     *
     * @param node
     *            test kgraph
     */
    @Test
    public void renderingStyleTest(final KNode node) {

        final KNodeAbstractNode targetNode = RenderingContextData.get(node).getProperty(REP);
        final KlighdPath path = Iterables.getFirst(
                Iterables.filter(targetNode.getChildrenReference(), KlighdPath.class), null);

        final KRendering ren = node.getData(KRendering.class);

        if (path != null && ren != null) {
            compareStyle(path, ren, node);
        }
        for (final KNode child: node.getChildren()) {
            renderingStyleTest(child);
        }
    }

    /**
     * Test whether the test graphs shapes are the same in KGraph notation as well as in the piccolo
     * results.
     *
     * @param node
     *            test kgraph
     */
    @Test
    public void renderingShapeTest(final KNode node) {

        final KNodeAbstractNode targetNode = RenderingContextData.get(node).getProperty(REP);
        final KlighdPath path = Iterables.getFirst(
                Iterables.filter(targetNode.getChildrenReference(), KlighdPath.class), null);

        final KRendering ren = node.getData(KRendering.class);

        if (path != null && ren != null) {
            compareShape(path, ren, node);
        }
        for (final KNode child: node.getChildren()) {
            renderingShapeTest(child);
        }
    }


    private String getKNodeId(final KNode node) {
        final KIdentifier ki = node.getData(KIdentifier.class);
        final String id;
        if (ki != null) {
            id = ki.getId();
        } else {
            id = node.toString();
        }
        return id;
    }

    /**
     * Compare the shape of given renderings from a KNode and a PNode.
     *
     * @param path
     *            piccolo object
     * @param ren
     *            KGraph object
     */
    private void compareShape(final KlighdPath path, final KRendering ren, final KNode node) {
        final String id = getKNodeId(node);
        Assert.assertTrue(
                "Shape mismatched on node " + id,
                (ren instanceof KRectangle && path.getShape() instanceof Rectangle2D)
                        || (ren instanceof KEllipse && path.getShape() instanceof Ellipse2D)
                        || (ren instanceof KRoundedRectangle
                                && path.getShape() instanceof RoundRectangle2D)
                        || (ren instanceof KPolygon && path.getShape() instanceof Path2D));
    }

    /**
     * Compare the styles of given renderings from a KNode and a PNode.
     *
     * @param path
     *            object holding styles for klighd piccolo
     * @param ren
     *            object holding styles for KGraph
     */
    private void compareStyle(final KlighdPath path, final KRendering ren, final KNode node) {
        final List<KStyle> styles = ren.getStyles();
        final KBackground bg = Iterables.getLast(Iterables.filter(styles, KBackground.class), null);
        final KForeground fg = Iterables.getLast(Iterables.filter(styles, KForeground.class), null);
        final KShadow sh = Iterables.getLast(Iterables.filter(styles, KShadow.class), null);

        final RGB shadow = path.getShadow();
        final RGB paint = path.getSWTPaint();
        final RGBGradient paintGradient = path.getSWTPaintGradient();
        final RGB strokepaint = path.getStrokePaint();
        final RGBGradient strokeGradient = path.getStrokePaintGradient();

        final String id = getKNodeId(node);
        if (!(bg != null && paint == null)) {
            Assert.assertTrue("Background color mismatched on node " + id,
                    (bg == null && paint == null)
                            || ((bg.getColor().getRed() == paint.red)
                                    && (bg.getColor().getBlue() == paint.blue) && (bg.getColor()
                                    .getGreen() == paint.green)));
        } else {
            Assert.assertTrue("Background gradient color missing on node " + id, (paintGradient != null));
            Assert.assertTrue("Gradient color mismatch on node " + id, ((bg.getColor().getRed() == paintGradient.getColor1().red)
                                    && (bg.getColor().getBlue() == paintGradient.getColor1().blue) && (bg.getColor()
                                    .getGreen() == paintGradient.getColor1().green)));
            Assert.assertTrue("Gradient target color mismatch on node " + id, ((bg.getTargetColor().getRed() == paintGradient.getColor2().red)
                    && (bg.getTargetColor().getBlue() == paintGradient.getColor2().blue) && (bg.getTargetColor()
                    .getGreen() == paintGradient.getColor2().green)));
        }
        if (!(fg != null && strokepaint == null)) {
            Assert.assertTrue("Foreground color mismatched on node " + id,
                    (fg == null && strokepaint == EclipseKlighdConstants.BLACK)
                            || ((fg.getColor().getRed() == strokepaint.red)
                                    && (fg.getColor().getBlue() == strokepaint.blue) && (fg.getColor()
                                    .getGreen() == strokepaint.green)));
        } else {
            Assert.assertTrue("Background gradient color missing on node " + id, (strokeGradient != null));
            Assert.assertTrue("Gradient stroke color mismatch on node " + id, ((fg.getColor().getRed() == strokeGradient.getColor1().red)
                    && (fg.getColor().getBlue() == strokeGradient.getColor1().blue) && (fg.getColor()
                    .getGreen() == strokeGradient.getColor1().green)));
            Assert.assertTrue("Gradient stroke target color mismatch on node " + id, ((fg.getTargetColor().getRed() == strokeGradient.getColor2().red)
                && (fg.getTargetColor().getBlue() == strokeGradient.getColor2().blue) && (fg.getTargetColor()
                        .getGreen() == strokeGradient.getColor2().green)));
        }
        Assert.assertTrue("Shadow mismatched on node " + id,
                (sh == null && shadow == null)
                        || ((sh.getColor().getRed() == shadow.red)
                                && (sh.getColor().getBlue() == shadow.blue) && (sh.getColor()
                                .getGreen() == shadow.green)));

    }
}