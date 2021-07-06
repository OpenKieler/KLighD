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

import static de.cau.cs.kieler.klighd.piccolo.test.DiagramClipGlobalPortLabelVisiblityConfigTest.Expectation.EXPECT_LABELS_INVISIBLE;
import static de.cau.cs.kieler.klighd.piccolo.test.DiagramClipGlobalPortLabelVisiblityConfigTest.Expectation.EXPECT_LABELS_VISIBLE;
import static de.cau.cs.kieler.klighd.piccolo.test.DiagramClipGlobalPortLabelVisiblityConfigTest.Expectation.EXPECT_PORTS_INVISIBLE;
import static de.cau.cs.kieler.klighd.piccolo.test.DiagramClipGlobalPortLabelVisiblityConfigTest.Expectation.EXPECT_PORTS_VISIBLE;
import static de.cau.cs.kieler.klighd.piccolo.test.DiagramClipGlobalPortLabelVisiblityConfigTest.Instruction.HIDE_LABELS;
import static de.cau.cs.kieler.klighd.piccolo.test.DiagramClipGlobalPortLabelVisiblityConfigTest.Instruction.HIDE_PORTS;
import static de.cau.cs.kieler.klighd.piccolo.test.DiagramClipGlobalPortLabelVisiblityConfigTest.Instruction.SHOW_LABELS;
import static de.cau.cs.kieler.klighd.piccolo.test.DiagramClipGlobalPortLabelVisiblityConfigTest.Instruction.SHOW_PORTS;
import static de.cau.cs.kieler.klighd.util.KlighdProperties.SHOW_CLIPPED_LABELS;
import static de.cau.cs.kieler.klighd.util.KlighdProperties.SHOW_CLIPPED_PORTS;

import java.util.Arrays;
import java.util.EnumSet;
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
import org.hamcrest.Matcher;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import de.cau.cs.kieler.klighd.IViewer;
import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.eclipse.IEclipseViewer;
import de.cau.cs.kieler.klighd.eclipse.viewers.EclipseContextViewer;
import de.cau.cs.kieler.klighd.kgraph.KNode;
import de.cau.cs.kieler.klighd.kgraph.KShapeLayout;
import de.cau.cs.kieler.klighd.kgraph.util.KGraphUtil;
import de.cau.cs.kieler.klighd.krendering.Colors;
import de.cau.cs.kieler.klighd.piccolo.viewer.PiccoloViewer;
import de.cau.cs.kieler.klighd.test.KlighdTestPlugin;
import de.cau.cs.kieler.klighd.util.KlighdProperties;
import de.cau.cs.kieler.klighd.util.KlighdSynthesisProperties;

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
 * This particular test class is supposed to test the functionality of the global port/label visibility
 *  settings in the view context.
 *
 * @author chsch
 */
@RunWith(Parameterized.class)
public class DiagramClipGlobalPortLabelVisiblityConfigTest {

    private static final int A_MOMENT = 500;

    private static final Object testModel = KlighdTestPlugin.loadTestModel();
    private static final ColorMatcher<Pair<Control, KVector>> IS_BLACK =
            ColorMatcher.acceptingPairsOfControlAndKVectorExpecting(Colors.BLACK);
    private static final ColorMatcher<Pair<Control, KVector>> IS_WHITE =
            ColorMatcher.acceptingPairsOfControlAndKVectorExpecting(Colors.WHITE);

    private static IViewer viewer;
    private static Shell shell;

    private static Point zeroPoint;
    private static int heightDelta = 0;

    // CHECKSTYLEOFF MagicNumber

    static enum Instruction {
        HIDE_PORTS,
        SHOW_PORTS,
        HIDE_LABELS,
        SHOW_LABELS;
        
        public EnumSet<Instruction> and(Instruction other) {
            return EnumSet.of(this, other);
        }
    }

    static enum Expectation {
        EXPECT_PORTS_VISIBLE,
        EXPECT_PORTS_INVISIBLE,
        EXPECT_LABELS_VISIBLE,
        EXPECT_LABELS_INVISIBLE;
    }

    @Parameters
    public static Iterable<Object[]> getParameters() {
        final KNode circuitGraph = (KNode) testModel;
        final KNode circuitContent = circuitGraph.getChildren().get(0);
        final KNode compoundOp = circuitGraph.getChildren().get(0).getChildren().get(0);
        final KNode andOp = circuitGraph.getChildren().get(0).getChildren().get(0).getChildren().get(0);
        final KNode orOp = circuitGraph.getChildren().get(0).getChildren().get(0).getChildren().get(1);
        final KNode notOp = circuitGraph.getChildren().get(0).getChildren().get(1);

        final List<Object[]> clips = Lists.newArrayList(
                /** in circuit content ports are set invisible by default in view model */
                clip(circuitContent, EXPECT_PORTS_INVISIBLE),
                clip(circuitContent, SHOW_PORTS, EXPECT_PORTS_VISIBLE),
                clip(circuitContent, HIDE_PORTS, EXPECT_PORTS_INVISIBLE),
                clip(circuitContent, SHOW_PORTS, EXPECT_PORTS_VISIBLE),

                /** in compound op labels are set invisible by default in view model;
                 * test the local memory of the port/label visibility if the clip node doesn't change */
                clip(compoundOp, EXPECT_PORTS_INVISIBLE, EXPECT_LABELS_INVISIBLE),
                clip(compoundOp, HIDE_PORTS, EXPECT_PORTS_INVISIBLE, EXPECT_LABELS_INVISIBLE),
                clip(compoundOp, SHOW_LABELS, EXPECT_PORTS_INVISIBLE, EXPECT_LABELS_VISIBLE),
                clip(compoundOp, SHOW_PORTS, EXPECT_PORTS_VISIBLE, EXPECT_LABELS_VISIBLE),
                clip(compoundOp, HIDE_LABELS, EXPECT_PORTS_VISIBLE, EXPECT_LABELS_INVISIBLE),
                clip(compoundOp, SHOW_LABELS, EXPECT_PORTS_VISIBLE, EXPECT_LABELS_VISIBLE),
                clip(compoundOp, EXPECT_PORTS_VISIBLE, EXPECT_LABELS_VISIBLE),

                clip(circuitContent, EXPECT_PORTS_INVISIBLE),
                clip(compoundOp, EXPECT_PORTS_INVISIBLE, EXPECT_LABELS_INVISIBLE),
                clip(andOp, EXPECT_PORTS_INVISIBLE),
                clip(orOp, EXPECT_PORTS_INVISIBLE),
                clip(notOp, EXPECT_PORTS_INVISIBLE),

                /** again test the local memory of the port/label visibility if the clip node doesn't change */
                clip(andOp, EXPECT_PORTS_INVISIBLE),
                clip(andOp, SHOW_PORTS.and(HIDE_LABELS)),
                clip(andOp, EXPECT_PORTS_VISIBLE),
                clip(andOp, SHOW_LABELS, EXPECT_PORTS_VISIBLE),
                clip(andOp, HIDE_LABELS, EXPECT_PORTS_VISIBLE),
                
                clip(null)
            );
        return clips;
    }
    
    private static Object[] clip(KNode node, Expectation... expectation) {
        if (expectation == null || expectation.length == 0)
            return clip(node, EnumSet.noneOf(Instruction.class), EnumSet.noneOf(Expectation.class));
        else
            return clip(node, EnumSet.noneOf(Instruction.class), EnumSet.of(
                    expectation[0], Arrays.copyOfRange(expectation, 1, expectation.length)));
    }
    
    private static Object[] clip(KNode node, Instruction instruction, Expectation... expectation) {
        if (expectation == null || expectation.length == 0)
            return clip(node, EnumSet.of(instruction), EnumSet.noneOf(Expectation.class));
        else
            return clip(node, EnumSet.of(instruction), EnumSet.of(
                    expectation[0], Arrays.copyOfRange(expectation, 1, expectation.length)));
    }
    
    private static Object[] clip(KNode node, EnumSet<Instruction> instructions) {
        return clip(node, instructions, EnumSet.noneOf(Expectation.class));
    }
    
    private static Object[] clip(KNode node,  EnumSet<Instruction> instructions, EnumSet<Expectation> expectations) {
        return new Object[] { node, instructions, expectations};
    }

    /**
     * Prepares the test: creates a fresh {@link Shell} and attaches KLighD's diagramming
     * infrastructure initialized with the model provided by {@link #loadTestModel()}.
     */
    @BeforeClass
    public static void prepare() {
        shell = new Shell(Display.getDefault());
        shell.setSize(1100, 200);
        shell.setLayout(new FillLayout());

        final ViewContext viewContext = new ViewContext(testModel)
                .configure(new KlighdSynthesisProperties().useViewer(PiccoloViewer.ID)
                        .setProperty(KlighdProperties.SHOW_CLIPPED_PORTS, false)
                        .setProperty(KlighdProperties.SHOW_CLIPPED_LABELS, false)
                );

        new EclipseContextViewer(shell).setModel(viewContext, true);

        heightDelta = 200 - ((IEclipseViewer) viewContext.getViewer()).getControl().getSize().y;
        shell.setSize(1100, 800 + heightDelta);

        viewContext.update(null);
        viewer = viewContext.getViewer();

        shell.layout(true, true);
        viewer.zoomToLevel(4, 0);

        shell.open();
        zeroPoint = ((IEclipseViewer) viewer).getControl().toDisplay(0, 0);

        // make sure the color recognition works by checking the background color in the top left corner
        // in case this check gives, e.g., RGB {0, 0, 0} the color identification doesn't work and
        //  this whole test class shall be skipped
        Assume.assumeThat(Pair.of(((IEclipseViewer) viewer).getControl(), new KVector(2, 2)), IS_WHITE);
    }

    @Parameter(0)
    public KNode node;
    
    @Parameter(1)
    public EnumSet<Instruction> instruction;
    
    @Parameter(2)
    public EnumSet<Expectation> expectation;
    
    @Test
    public void test() throws InterruptedException {
        final boolean aborted = waitAmoment();

        if (aborted) {
            Assert.fail("Test could not be executed!");
            return;

        } else {
            // use the semantic model-based (i.e. the Object-based) method here
            viewer.clip((Object) node,
                    instruction.contains(HIDE_PORTS) ? Boolean.TRUE
                            : instruction.contains(SHOW_PORTS) ? Boolean.FALSE : null,
                    instruction.contains(HIDE_LABELS) ? Boolean.TRUE
                            : instruction.contains(SHOW_LABELS) ? Boolean.FALSE : null);

            if (node == null || node.getPorts().isEmpty()) {
                return;
            }

            final float zoom = viewer.getZoomLevel();

            final KShapeLayout port0Layout = node.getPorts().get(0);
            final KVector port0pos = KGraphUtil.toAbsolute(port0Layout.createVector(), node)
                    .add(port0Layout.getWidth() / 2, port0Layout.getHeight() / 2).scale(zoom);

            final KShapeLayout portXLayout = Iterables.getLast(node.getPorts());
            final KVector portXpos = KGraphUtil.toAbsolute(portXLayout.createVector(), node)
                    // width -1 is used because of the non-filled output port of the 'not' gate
                    .add(portXLayout.getWidth() - 1, portXLayout.getHeight() / 2).scale(zoom);

            final Matcher<Pair<Control, KVector>> portMatcher =
                    expectation.contains(EXPECT_PORTS_INVISIBLE)
                            || !expectation.contains(EXPECT_PORTS_VISIBLE) && (
                                    instruction.contains(HIDE_PORTS)
                                    || !instruction.contains(SHOW_PORTS) &&
                                            !node.getProperty(SHOW_CLIPPED_PORTS).booleanValue())
                                                    ? IS_WHITE : IS_BLACK;

            if (portMatcher != null) {
                waitAmoment();

                moveTo((int) (port0pos.x), (int) (port0pos.y));
                Assert.assertThat(Pair.of(((IEclipseViewer) viewer).getControl(), port0pos), portMatcher);

                if (port0Layout != portXLayout) {
                    waitAmoment();

                    moveTo((int) (portXpos.x), (int) (portXpos.y));
                    Assert.assertThat(Pair.of(((IEclipseViewer) viewer).getControl(), portXpos), portMatcher);

                    if (node.getLabels().isEmpty()) {
                        return;
                    }
                }
            }

            final Matcher<Pair<Control, KVector>> labelMatcher =
                    expectation.contains(EXPECT_LABELS_INVISIBLE)
                            || !expectation.contains(EXPECT_LABELS_VISIBLE) && (
                                    instruction.contains(HIDE_LABELS)
                                    || !instruction.contains(SHOW_LABELS) &&
                                            !node.getProperty(SHOW_CLIPPED_LABELS).booleanValue())
                                                    ? IS_WHITE : IS_BLACK;

            if (labelMatcher != null && !node.getLabels().isEmpty()) {
                final KShapeLayout label0 = node.getLabels().get(0);
                final KVector label0pos =
                        KGraphUtil.toAbsolute(label0.createVector(), node).add(2, 2).scale(zoom);

                waitAmoment();

                moveTo((int) (label0pos.x), (int) (label0pos.y));
                Assert.assertThat(Pair.of(((IEclipseViewer) viewer).getControl(), label0pos), labelMatcher);
            }
        }
    }

    /**
     * Wait for all tests to finish (within the pre-defined time), direct the display to handling
     * pending events and close the employed shell if necessary.
     */
    @AfterClass
    public static void wrapUp() {
        waitAmoment();

        if (!shell.isDisposed()) {
            shell.close();
        }
    }

    /**
     * @return {@code true} if this 'wait' requested was aborted / impossible to be executed.
     *
     * @throws InterruptedException
     */
    private static boolean waitAmoment() {
        if (shell.isDisposed()) {
            return true;
        }

        final boolean[] wait = new boolean[]{ true };
        final Display d = shell.getDisplay();

        // make sure the display gets awoke after some time
        d.timerExec(A_MOMENT, new Runnable() {
            @Override
            public void run() {
                wait[0] = false;
            }
        });

        while (d.readAndDispatch());
        while (wait[0]) {
            d.sleep();
            while (d.readAndDispatch());
        }

        return false;
    }

    private void moveTo(final int x, final int y) {
        final Display d = shell.getDisplay();

        final Event moveTo = new Event();
        moveTo.type = SWT.MouseMove;
        moveTo.x = zeroPoint.x + x;
        moveTo.y = zeroPoint.y + y;
        d.post(moveTo);

        while (d.readAndDispatch());
    }
}
