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

import java.util.List;

import org.eclipse.elk.core.math.KVector;
import org.eclipse.elk.core.util.Pair;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.hamcrest.MatcherAssert;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import de.cau.cs.kieler.klighd.IDiagramWorkbenchPart;
import de.cau.cs.kieler.klighd.IViewer;
import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.kgraph.KNode;
import de.cau.cs.kieler.klighd.kgraph.KShapeLayout;
import de.cau.cs.kieler.klighd.kgraph.util.KGraphUtil;
import de.cau.cs.kieler.klighd.krendering.Colors;
import de.cau.cs.kieler.klighd.piccolo.viewer.PiccoloViewer;
import de.cau.cs.kieler.klighd.test.KlighdTestPlugin;
import de.cau.cs.kieler.klighd.util.KlighdSynthesisProperties;
import de.cau.cs.kieler.klighd.viewers.ContextViewer;

/**
 * Tests KLighD's clipping feature by means of the circuit example available in {@code klighd.test},
 * especially the modification of the main camera's view transform that is required in order let the
 * visible part remain at exactly the position it would be drawn without any clipping.<br>
 * <br>
 * This test is configured to be executed by the {@link Parameterized} test
 * {@link org.junit.runners.Suite Suite}. {@link #getParameters()} is called first prepares a list
 * of diagram nodes to clip the diagram to. Subsequently, {@link #prepareStatic()} is called that
 * initializes a diagram view consisting of a simple {@link Shell}, attaches {@link IViewer
 * IViewers}, and creates diagram of circuit diagram {@link #testModel} (which already is a KGraph).<br>
 * <br>
 * Then, for each clip {@link #test()} is called. It clip the displayed diagram to the particular
 * node without changing the layout and zooming. The correct execution of the test case is checked
 * by evaluating the colors on canvas at the positions of the clip nodes first and last ports (if
 * there are any, those positions are taken from the input KGraph). In order to be able to follow
 * the test routine the mouse pointer is moved to the currently evaluated canvas position.
 *
 * @author chsch
 */
@RunWith(Parameterized.class)
public class DiagramClipTest {

    private static final int EXPECTED_DURATION = 20000; //ms, i.e. 20 seconds
    private static final int A_MOMENT = 500;

    private static final Object FINISH = Boolean.TRUE;

    private static final Object testModel = KlighdTestPlugin.loadTestModel();
    private static final ColorMatcher<Pair<Control, KVector>> IS_BLACK =
            ColorMatcher.acceptingPairsOfControlAndKVectorExpecting(Colors.BLACK);
    private static final ColorMatcher<Pair<Control, KVector>> IS_WHITE =
            ColorMatcher.acceptingPairsOfControlAndKVectorExpecting(Colors.WHITE);

    private static DiagramClipTest sharedInstance;

    private IViewer viewer;
    private Shell shell = null;

    private Point zeroPoint;
    private boolean finished = true;
    private long deadline = 0;
    private boolean respectDeadline = true;
    private int heightDelta = 0;

    // CHECKSTYLEOFF MagicNumber

    @Parameters
    public static Iterable<Object[]> getParameters() {
        final KNode circuitGraph = (KNode) testModel;
        final KNode andOp = circuitGraph.getChildren().get(0).getChildren().get(0).getChildren().get(0);
        final KNode orOp = circuitGraph.getChildren().get(0).getChildren().get(0).getChildren().get(1);
        final KNode notOp = circuitGraph.getChildren().get(0).getChildren().get(1);

        final List<Object> clips =
                Lists.newArrayList(andOp, orOp, notOp, circuitGraph, notOp, andOp, orOp, notOp,
                        circuitGraph, notOp, orOp, andOp, circuitGraph, FINISH);

        final Iterable<Object[]> result = Iterables.transform(clips, new Function<Object, Object[]>() {
            public Object[] apply(final Object input) {
                return new Object[] { input };
            }
        });

        return result;
    }


    @BeforeClass
    public static void prepareStatic() {
        sharedInstance = new DiagramClipTest();
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

        final ViewContext viewContext = new ViewContext((IDiagramWorkbenchPart) null, testModel)
                .configure(new KlighdSynthesisProperties().useViewer(PiccoloViewer.ID));

        new ContextViewer(shell).setModel(viewContext, true);

        heightDelta = 200 - viewContext.getViewer().getControl().getSize().y;
        shell.setSize(1100, 800 + heightDelta);

        viewContext.update(null);
        viewer = viewContext.getViewer();

        deadline = System.currentTimeMillis() + EXPECTED_DURATION;

        shell.layout(true, true);
        viewer.zoomToLevel(4, 0);
        shell.open();

        zeroPoint = viewer.getControl().toDisplay(0, 0);

        // make sure the color recognition works by checking the background color in the top left corner
        // in case this check gives, e.g., RGB {0, 0, 0} the color identification doesn't work and
        //  this whole test class shall be skipped
        Assume.assumeThat(Pair.of(viewer.getControl(), new KVector(2, 2)), IS_WHITE);
        
        finished = false;
    }


    @Parameter(0)
    public Object inputParam;

    @Test
    public void test() throws InterruptedException {
        final boolean aborted = waitAmoment();

        if (aborted) {
            Assert.fail("Test could not be executed!");
            return;
        }

        if (FINISH == inputParam) {
            sharedInstance.finished = true;

        } else {
            final IViewer viewer = sharedInstance.viewer;

            viewer.clip(inputParam);

            final KNode clip = (KNode) inputParam;
            if (clip.getPorts().isEmpty()) {
                return;
            }

            final float zoom = viewer.getZoomLevel();

            final KShapeLayout port0Layout = clip.getPorts().get(0);
            final KVector port0pos = KGraphUtil.toAbsolute(port0Layout.createVector(), clip);
            port0pos.add(port0Layout.getWidth() / 2, port0Layout.getHeight() / 2);
            port0pos.scale(zoom);

            final KShapeLayout portXLayout = Iterables.getLast(clip.getPorts());
            final KVector portXpos = KGraphUtil.toAbsolute(portXLayout.createVector(), clip);
            portXpos.add(1, portXLayout.getHeight() / 2);
            portXpos.scale(zoom);

            waitAmoment();

            moveTo((int) (port0pos.x), (int) (port0pos.y));
            MatcherAssert.assertThat("",Pair.of(viewer.getControl(), port0pos), IS_BLACK);

            waitAmoment();

            moveTo((int) (portXpos.x), (int) (portXpos.y));
            MatcherAssert.assertThat("",Pair.of(viewer.getControl(), portXpos), IS_BLACK);
        }
    }


    @AfterClass
    public static void wrapUpStatic() {
        sharedInstance.wrapUp();
    }

    /**
     * Wait for all tests to finish (within the pre-defined time), direct the display to handling
     * pending events and close the employed shell if necessary.
     */
    private void wrapUp() {
        while (!finished && !shell.isDisposed()) {
            if (respectDeadline && System.currentTimeMillis() > deadline) {
                Assert.fail("Expected test time elapsed!");
                break;
            }
            try {
                if (!Display.getCurrent().readAndDispatch()) {
                    //  wait for some time before continuing
                    Thread.sleep(50);
                }

            } catch (final InterruptedException e) {
                // nothing
            }
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
