/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://rtsys.informatik.uni-kiel.de/kieler
 * 
 * Copyright 2019 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 */
package de.cau.cs.kieler.klighd.piccolo.test;

import static de.cau.cs.kieler.klighd.piccolo.test.DiagramZoomControllerBoundsComputerTest.Instruction.HIDE_LABELS;
import static de.cau.cs.kieler.klighd.piccolo.test.DiagramZoomControllerBoundsComputerTest.Instruction.HIDE_PORTS;
import static de.cau.cs.kieler.klighd.piccolo.test.DiagramZoomControllerBoundsComputerTest.Instruction.HIDE_P_n_Ls;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import org.eclipse.elk.core.math.KVector;
import org.eclipse.elk.core.util.Maybe;
import org.eclipse.elk.core.util.Pair;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import de.cau.cs.kieler.klighd.IViewer;
import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.ZoomStyle;
import de.cau.cs.kieler.klighd.eclipse.IEclipseViewer;
import de.cau.cs.kieler.klighd.eclipse.viewers.EclipseContextViewer;
import de.cau.cs.kieler.klighd.kgraph.KEdge;
import de.cau.cs.kieler.klighd.kgraph.KGraphElement;
import de.cau.cs.kieler.klighd.kgraph.KLabel;
import de.cau.cs.kieler.klighd.kgraph.KNode;
import de.cau.cs.kieler.klighd.kgraph.KPort;
import de.cau.cs.kieler.klighd.krendering.Colors;
import de.cau.cs.kieler.klighd.piccolo.internal.controller.DiagramZoomControllerBoundsComputer;
import de.cau.cs.kieler.klighd.piccolo.viewer.PiccoloViewer;
import de.cau.cs.kieler.klighd.test.KlighdTestPlugin;
import de.cau.cs.kieler.klighd.util.KlighdSynthesisProperties;
import edu.umd.cs.piccolo.util.PAffineTransform;

/**
 * Test of the diagram bounding box computations implemented in {@link DiagramZoomControllerBoundsComputer}. 
 * 
 * @author chsch
 */
@RunWith(Parameterized.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SuppressWarnings("unused")
public class DiagramZoomControllerBoundsComputerTest {

    private static final int EXPECTED_DURATION = 40000; //ms, i.e. 20 seconds
    private static final int A_MOMENT = 500;

    private static final Object testModel = KlighdTestPlugin.loadTestModel();
    private static final ColorMatcher<Pair<Control, KVector>> IS_BLACK =
            ColorMatcher.acceptingPairsOfControlAndKVectorExpecting(Colors.BLACK);
    private static final ColorMatcher<Pair<Control, KVector>> IS_WHITE =
            ColorMatcher.acceptingPairsOfControlAndKVectorExpecting(Colors.WHITE);

    private static DiagramZoomControllerBoundsComputerTest sharedInstance;

    private IViewer viewer;
    private Shell shell = null;

    private Point zeroPoint;
    private long deadline = 0;
    private boolean respectDeadline = false;
    private int heightDelta = 0;

    // CHECKSTYLEOFF MagicNumber

    static enum Instruction {
        HIDE_LABELS,
        HIDE_PORTS,
        HIDE_P_n_Ls,
    }

    @Parameters
    public static Iterable<Object[]> getParameters() {
        final KNode circuitGraph = (KNode) testModel;
        final KNode circuitContent = circuitGraph.getChildren().get(0);     // x=12.0, y=12.0, width=229.0, height=171.0
        final KNode compoundOp = circuitGraph.getChildren().get(0).getChildren().get(0);                 // x=94.0,  y=22.0   width=111.0, height=122.0, label at y=127.0, height=10.0
        final KNode andOp = circuitGraph.getChildren().get(0).getChildren().get(0).getChildren().get(0); // x=46.0,  y=76.67, width=40.0,  height=30.0

        final Iterable<Object[]> clips = Lists.newArrayList(
         //            node           instruction  bb fit content                                 bb fit entirely  
                // the andOp has no children and and no node labels,
                //  so clipping with ports + labels yields the ports' bounding box for 'fit content', and an enlarged bb for 'fit entirely' 
         /* 0*/ zoomTo(andOp,                      "x=41.0, y=81.33, width= 49.0, height= 20.67", "x=41.0, y=76.66, width= 49.0, height= 30.01"),
                // ... same here
         /* 1*/ zoomTo(andOp,         HIDE_LABELS, "x=41.0, y=81.33, width= 49.0, height= 20.67", "x=41.0, y=76.66, width= 49.0, height= 30.01"),
                // however, with hidden ports, the 'fit content' bb would be empty, so fall back the node's bb, which is equal to the 'fit content' bb
         /* 2*/ zoomTo(andOp,         HIDE_PORTS,  "x=46.0, y=76.66, width= 40.0, height= 30.01", "x=46.0, y=76.66, width= 40.0, height= 30.01"),

                // the compoundOp has external ports (x=[-2, 98], width=5, y=[12,96], height=2)
                //  and an outside bottom label (y=127.0, height=10.0)
         /* 3*/ zoomTo(compoundOp,                 "x=92.0, y=34.0,  width=115.0, height=125.0",  "x=92.0, y=22.0,  width=115.0, height=137.0"),
                // when hiding the node label(s) the height is determined by the ports *and* their labels, i.e. h=96+(5+10)-12=99, in the fitContent case 
         /* 4*/ zoomTo(compoundOp,    HIDE_LABELS, "x=92.0, y=34.0,  width=115.0, height= 99.0",  "x=92.0, y=22.0,  width=115.0, height=122.0"),
                // when hiding the ports, all externally connected edges (here all edges) need to be excluded, too,
                //  only the child nodes and the node label determine the fitContent bounds
         /* 5*/ zoomTo(compoundOp,    HIDE_PORTS,  "x=121.5,y=48.66, width= 62.5, height=110.34", "x=94.0, y=22.0,  width=111.0, height=137.0"),
                // now consider the child nodes only (here all connecting edges need to be ignored
                //  for fitContent x results the andOp's input ports positions, width from andOps' input + output ports positions + widths
         /* 6*/ zoomTo(compoundOp,    HIDE_P_n_Ls, "x=135.0,y=48.66, width= 49.0, height=80.01",  "x=94.0, y=22.0,  width=111.0, height=122.0"),
                
                // the circuitContent node has an external port on the left,
                //  besides the fitContent bb is determined by a long edge and compoundOp's label
         /* 7*/ zoomTo(circuitContent,             "x=10.0, y=24.0,  width=219.0, height=147.0",  "x=10.0, y=12.0,  width=231.0, height=171.0"),
                // when hiding the ports, all externally connected edges (here all edges) need to be excluded, too,
                //  only the child nodes and the node label determine the fitContent bounds
         /* 8*/ zoomTo(circuitContent, HIDE_PORTS, "x=24.0, y=24.0,  width=205.0, height=147.0",  "x=12.0, y=12.0,  width=229.0, height=171.0"),
                
         /* 9*/ zoomTo(circuitGraph,               "x=10.0, y=12.0,  width=231.0, height=171.0",  "x= 0.0, y= 0.0,  width=253.0, height=195.0")
        );

        return clips;
    }
    
    private static Object[] zoomTo(KNode node, Object expectationFit, Object expectationFitContent) {
        return zoomTo(node, null, expectationFit, expectationFitContent);
    }
    
    private static Object[] zoomTo(KNode node, Instruction instruction, Object expectationFit, Object expectationFitContent) {
        return new Object[] { node, instruction, expectationFit, expectationFitContent };
    }
    

    @BeforeClass
    public static void prepareStatic() {
        sharedInstance = new DiagramZoomControllerBoundsComputerTest();
        sharedInstance.prepare();
    }

    /**
     * Prepares the test: creates a fresh {@link Shell} and attaches KLighD's diagramming
     * infrastructure initialized with the model provided by {@link #loadTestModel()}.
     */
    public void prepare() {
        shell = new Shell(Display.getDefault());
        shell.setSize(1100, 200);
        shell.setLayout(new FillLayout());

        final ViewContext viewContext = new ViewContext(testModel)
                .configure(new KlighdSynthesisProperties().useViewer(PiccoloViewer.ID));

        new EclipseContextViewer(shell).setModel(viewContext, true);

        heightDelta = 200 - ((IEclipseViewer) viewContext.getViewer()).getControl().getSize().y;
        shell.setSize(1100, 800 + heightDelta);

        viewContext.update(null);
        viewer = viewContext.getViewer();

        deadline = System.currentTimeMillis() + EXPECTED_DURATION;

        shell.layout(true, true);
        viewer.zoomToLevel(4, 0);
        shell.open();

        zeroPoint = ((IEclipseViewer) viewer).getControl().toDisplay(0, 0);
    }


    @Parameter(0)
    public Object inputParam;

    @Parameter(1)
    public Instruction instructions;

    @Parameter(2)
    public Object expectationFitContent;
    
    @Parameter(3)
    public Object expectationFitEntirely;


    @Test
    public void testLogicFitContent() {
        testLogic(true, expectationFitContent);
    }
    
    @Test
    public void testLogicFitEntirely() {
        testLogic(false, expectationFitEntirely);
    }

    public void testLogic(boolean narrowDownToContent, Object expected) {
        Assume.assumeTrue(inputParam instanceof KNode);
        
        // determine the view model node to use as diagram clip
        //  (inputParam is part of the input/source model for KLighD regardless that it's a KNode)
        final KNode node = (KNode) inputParam;

        // get a custom (test-specific) visibility filter 
        final Predicate<KGraphElement> visibilityFilter = getVisibilityFilter(node);

        // compute the required bounds
        final Rectangle2D result = new DiagramZoomControllerBoundsComputer(visibilityFilter)
                .toPBoundsIncludingPortsAndLabels(node, narrowDownToContent, null, true);

        // round 'result' to two decimals by
        //  * scaling up the result by factor of 100,
        //  * converting to Integer-based numbers (via the type 'Rectangle'), and
        //  * reverting the scaling while writing the numbers back to 'result' 
        final PAffineTransform t = new PAffineTransform();
        t.scale(100, 100);
        final Rectangle2D resultInt = t.transform(result, new Rectangle() /* integer-based implementation of Rectangle2D*/);
        t.inverseTransform(resultInt, result);

        // massage the expectation a bit
        final String expectation = expected instanceof String
                ? "PBounds[" + ((String) expected).replace(" ", "") + "]" : expected.toString();

        // do the comparison
        Assert.assertEquals(
            expectation, result == null ? "null" : result.toString()
        );
    }
    
    /**
     * Provides a test specific visibility filter that is independent of some displayed diagram (state).
     */
    private Predicate<KGraphElement> getVisibilityFilter(KNode clip) {
        final boolean portsHidden = instructions == HIDE_PORTS || instructions == HIDE_P_n_Ls;
        final boolean labelsHidden = instructions == HIDE_LABELS || instructions == HIDE_P_n_Ls;
        
        final Maybe<Predicate<KGraphElement>> isVisibleWrapper = new Maybe<Predicate<KGraphElement>>();
        isVisibleWrapper.set(e -> {
            if (e instanceof KNode) {
                return true;
                
            } else if (e instanceof KPort) {
                return !portsHidden || e.eContainer() != clip;
                
            } else if (e instanceof KEdge) {
                final KEdge edge = (KEdge) e;
                return (edge.getSourcePort() == null || isVisibleWrapper.get().apply(edge.getSourcePort())
                        && edge.getTargetPort() == null || isVisibleWrapper.get().apply(edge.getSourcePort()));
                
            } else if (e instanceof KLabel) {
                final KLabel label = (KLabel) e;
                if (label.getParent() instanceof KNode)
                    return !labelsHidden || label.getParent() != clip;
                else
                    return isVisibleWrapper.get().apply(label.getParent());
                
            } else {
                return false;
            }
        });
        
        return isVisibleWrapper.get(); 
    }


    @Test
    public void testUIportrait() throws InterruptedException {
        testUI(false);
    }

    @Test
    public void testUIlandscape() throws InterruptedException {
        testUI(true);
    }

    public void testUI(boolean landscape) throws InterruptedException {
        final boolean aborted = waitAmoment();

        if (aborted) {
            throw new InterruptedException("Test could not be executed!, SWT shell is disposed.");
        } else {
            // make sure the color recognition works by checking the background color in the top left corner
            // in case this check gives, e.g., RGB {0, 0, 0} the color identification doesn't work and
            //  this test shall be skipped
            Assume.assumeThat(Pair.of(((IEclipseViewer) sharedInstance.viewer).getControl(), new KVector(2, 2)), IS_WHITE);
            
            if (landscape) {
                sharedInstance.shell.setSize(1000, 500);
            } else {
                sharedInstance.shell.setSize(500, 1000);
            }
            sharedInstance.shell.getDisplay().readAndDispatch();
            
            final boolean portsHidden = instructions == HIDE_PORTS || instructions == HIDE_P_n_Ls;
            final boolean labelsHidden = instructions == HIDE_LABELS || instructions == HIDE_P_n_Ls;

            final IViewer viewer = sharedInstance.viewer;
            viewer.clip(inputParam, Boolean.valueOf(portsHidden), Boolean.valueOf(labelsHidden));
            viewer.zoom(ZoomStyle.ZOOM_TO_FIT_CONTENT, 0);

            waitAmoment();
            waitAmoment();
            
            
            // TODO: evaluating the resulting diagram images is not done yet,
            //  as it requires more effort to define reasonable validity criteria;
            // I however keep it, as it allows developers to "optically" verify the correctness
            //  of the zoom to fit applications by gazing at it ;-)
//
//            final KNode clip = (KNode) inputParam;
//            if (clip.getPorts().isEmpty()) {
//                return;
//            }
//
//            final float zoom = viewer.getZoomLevel();
//
//            final KShapeLayout port0Layout = clip.getPorts().get(0);
//            final KVector port0pos = KGraphUtil.toAbsolute(port0Layout.createVector(), clip);
//            port0pos.add(port0Layout.getWidth() / 2, port0Layout.getHeight() / 2);
//            port0pos.scale(zoom);
//
//            final KShapeLayout portXLayout = Iterables.getLast(clip.getPorts());
//            final KVector portXpos = KGraphUtil.toAbsolute(portXLayout.createVector(), clip);
//            portXpos.add(1, portXLayout.getHeight() / 2);
//            portXpos.scale(zoom);
//
//            waitAmoment();
//
//            moveTo((int) (port0pos.x), (int) (port0pos.y));
//            Assert.assertThat(Pair.of(viewer.getControl(), port0pos), IS_BLACK);
//
//            waitAmoment();
//
//            moveTo((int) (portXpos.x), (int) (portXpos.y));
//            Assert.assertThat(Pair.of(viewer.getControl(), portXpos), IS_BLACK);
        }
    }


    @AfterClass
    public static void wrapUpStatic() {
        sharedInstance.wrapUp();
    }


    /**
     * After all tests are done close the employed shell if necessary.
     */
    private void wrapUp() {
        if (respectDeadline && System.currentTimeMillis() > deadline) {
            Assert.fail("Expected test time elapsed!");
        }

        if (!shell.isDisposed()) {
            Display.getCurrent().readAndDispatch();
        }

        if (!shell.isDisposed()) {
            shell.close();
        }
    }


    /**
     * @return {@code true} if this 'wait' requested was aborted / impossible to be executed.
     *
     * @throws InterruptedException
     */
    private boolean waitAmoment() throws InterruptedException {

        if (sharedInstance.shell.isDisposed()) {
            return true;
        }

        final Display d = sharedInstance.shell.getDisplay();

        while (d.readAndDispatch());

        Thread.sleep(A_MOMENT);

        while (d.readAndDispatch());

        return false;
    }

    private void moveTo(final int x, final int y) {
        final Display d = sharedInstance.shell.getDisplay();

        final Event moveTo = new Event();
        moveTo.type = SWT.MouseMove;
        moveTo.x = sharedInstance.zeroPoint.x + x;
        moveTo.y = sharedInstance.zeroPoint.y + y;
        d.post(moveTo);

        while (d.readAndDispatch());
    }
}
