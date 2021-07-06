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
package de.cau.cs.kieler.klighd.piccolo.test.highlightedEdgeToForeground;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.hamcrest.MatcherAssert;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.Scopes;

import de.cau.cs.kieler.klighd.IDiagramWorkbenchPart;
import de.cau.cs.kieler.klighd.LightDiagramLayoutConfig;
import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.ZoomStyle;
import de.cau.cs.kieler.klighd.kgraph.KShapeLayout;
import de.cau.cs.kieler.klighd.krendering.Colors;
import de.cau.cs.kieler.klighd.krendering.ViewSynthesisShared;
import de.cau.cs.kieler.klighd.piccolo.test.ColorMatcher;
import de.cau.cs.kieler.klighd.piccolo.viewer.PiccoloViewer;
import de.cau.cs.kieler.klighd.util.KlighdSynthesisProperties;
import de.cau.cs.kieler.klighd.viewers.ContextViewer;

/**
 * The aim of this class is to test the "bring the highlighted edge to foreground" feature.<br>
 * To this end a small diagram with ports and overlapping edges is build up (composition is out-
 * sourced to {@link HighlightedEdgeToForegroundTestModelGen}. In order to test not only single
 * methods but the whole tool a simple diagram viewer is launched and some mouse move and click
 * events are send to the employed {@link Display} instance. By means of those events edges are
 * selected and due to corresponding styles their color is switched from black to red.<br>
 * <br>
 * In order to assess the correctness of the tool's behavior the color of the diagram is evaluated
 * at certain positions of the diagram. Since the "measured" color will differ from the expected one
 * by some units due to anti-aliasing a similarity heuristic is applied.
 *
 * @author chsch
 */
public class HighlightedEdgeToForegroundTest {

    private static HighlightedEdgeToForegroundTestModelGen testModelGen;
    private static ViewContext viewContext;

    private static Shell shell;
    private static Control canvas;
    private static Point zeroPoint;

    /**
     * Prepares a {@link Shell} with a KLighD diagram canvas displaying a simple test model.
     */
    @BeforeClass
    public static void prepare() throws InterruptedException {

        testModelGen = Guice.createInjector(new Module() {
            public void configure(final Binder binder) {
                binder.bindScope(ViewSynthesisShared.class, Scopes.SINGLETON);
            }
        }).getInstance(HighlightedEdgeToForegroundTestModelGen.class);

        shell = new Shell(Display.getDefault());
        shell.setSize(650, 330);
        shell.setLayout(new FillLayout());

        viewContext = new ViewContext((IDiagramWorkbenchPart) null, testModelGen.getTestModel())
                .configure(new KlighdSynthesisProperties().useViewer(PiccoloViewer.ID));

        new ContextViewer(shell).setModel(viewContext, true);

        viewContext.update(null);
        new LightDiagramLayoutConfig(viewContext).animate(false)
                .zoomStyle(ZoomStyle.ZOOM_TO_ACTUAL_SIZE).performLayout();

        shell.layout(true, true);
        shell.open();

        canvas = viewContext.getViewer().getControl();
        zeroPoint = canvas.toDisplay(0, 0);

        // make sure the color recognition works by checking the background color in the top left
        // corner
        // in case this check gives, e.g., RGB {0, 0, 0} the color identification doesn't work and
        // this whole test class is skipped
        Assume.assumeThat(getColorAt(2, 2), IS_WHITE);
        //
        // // The following might be helpful when running/debugging via Eclipse:
        //
        // // The following is some mouse pointer "warm up" since I experienced some delayed
        // reactions of the
        // // UI on the mouse pointer movement commands executed in the tests below.
        // // Note, that these movements are not mandatory for the test execution,
        // // but very helpful for developers for verifying the test execution.
        // for (int i = 0; i < 3; i++) {
        // clickOn(4, 2);
        // waitAmoment();
        // clickOn(4, 4);
        // waitAmoment();
        // }
        // clickOn(4, 4);
        // waitAmoment();
        //
        // // The following is display a "warm up" test since the continuous build's (virtual)
        // display's palette
        // // seems to be very limited. The observed behavior is: If 'black' or 'white' is the first
        // observed
        // // color, only those colors will be obtained subsequently. I interpret this as follows:
        // // The image's color palette then contains only black and white. If the image also
        // contains 'red'
        // // parts before testing the first time for any color the palette than also contains
        // 'red'.
        // // Weird ...
        // final KShapeLayout firstChildNodeLayout =
        // viewContext.getViewModel().getChildren().get(0).getChildren().get(0);
        // final int firstChildNodeYPos = Math.round(firstChildNodeLayout.getYpos());
        // final int firstClickXPos = 5 + 100;
        //
        // clickOn(firstClickXPos, firstChildNodeYPos);
        // waitAmoment();
        //
        // clickOn(4, 4);
        // waitAmoment();
    }

    private static final ColorMatcher<RGB> IS_BLACK =
            ColorMatcher.acceptingRGBsExpecting(Colors.BLACK);
    private static final ColorMatcher<RGB> IS_RED = ColorMatcher.acceptingRGBsExpecting(Colors.RED);
    private static final ColorMatcher<RGB> IS_WHITE =
            ColorMatcher.acceptingRGBsExpecting(Colors.WHITE);

    /**
     * Tests the highlighting of overlapped interlevel edges connected to same source.
     */
    @Test
    public void test01() throws InterruptedException {
        final KShapeLayout firstChildNodeLayout =
                viewContext.getViewModel().getChildren().get(0).getChildren().get(0);
        final int firstChildNodeYPos = Math.round(firstChildNodeLayout.getYpos());

        final KShapeLayout firstWPortLayout =
                viewContext.getViewModel().getChildren().get(0).getPorts().get(2);
        final int firstWPortLayoutCenterYPos = Math.round(firstWPortLayout.getYpos()) + 3;

        final int sampleXPos = 50;
        final int firstClickXPos = 5 + 100; // port width + border spacing + edge spacing factor *
                                            // spacing
        final int secondClickXPos = 200;

        moveTo(firstClickXPos, firstChildNodeYPos);
        MatcherAssert.assertThat("", getColorAt(firstClickXPos, firstChildNodeYPos), IS_BLACK);

        clickOn(firstClickXPos, firstChildNodeYPos);
        waitAmoment();
        MatcherAssert.assertThat("", getColorAt(firstClickXPos, firstChildNodeYPos), IS_RED);

        moveTo(sampleXPos, firstWPortLayoutCenterYPos);
        waitAmoment();
        MatcherAssert.assertThat("", getColorAt(sampleXPos, firstWPortLayoutCenterYPos), IS_RED);

        moveTo(secondClickXPos, firstWPortLayoutCenterYPos);
        MatcherAssert.assertThat("", getColorAt(secondClickXPos, firstWPortLayoutCenterYPos), IS_BLACK);

        clickOn(secondClickXPos, firstWPortLayoutCenterYPos);
        waitAmoment();
        MatcherAssert.assertThat("", getColorAt(secondClickXPos, firstWPortLayoutCenterYPos), IS_RED);

        moveTo(sampleXPos, firstWPortLayoutCenterYPos);
        waitAmoment();
        MatcherAssert.assertThat("", getColorAt(sampleXPos, firstWPortLayoutCenterYPos), IS_RED);

        clickOn(4, 4);
        waitAmoment();
    }

    /**
     * Tests the highlighting of overlapped interlevel edges connected to same target.
     */
    @Test
    public void test02() throws InterruptedException {
        final KShapeLayout firstChildNodeLayout =
                viewContext.getViewModel().getChildren().get(0).getChildren().get(0);
        final int firstChildNodeYPos = Math.round(firstChildNodeLayout.getYpos());

        final KShapeLayout secondWPortLayout =
                viewContext.getViewModel().getChildren().get(0).getPorts().get(0);
        final int secondWPortLayoutCenterYPos = Math.round(secondWPortLayout.getYpos()) + 3;

        final int sampleXPos = 120;
        final int firstClickXPos = 5 + 100; // port width + border spacing + edge spacing factor *
                                            // spacing
        final int secondClickXPos = 50;

        moveTo(firstClickXPos, firstChildNodeYPos);
        MatcherAssert.assertThat("", getColorAt(firstClickXPos, firstChildNodeYPos), IS_BLACK);

        clickOn(firstClickXPos, firstChildNodeYPos);
        waitAmoment();
        MatcherAssert.assertThat("", getColorAt(firstClickXPos, firstChildNodeYPos), IS_RED);

        moveTo(sampleXPos, secondWPortLayoutCenterYPos);
        waitAmoment();
        MatcherAssert.assertThat("", getColorAt(sampleXPos, secondWPortLayoutCenterYPos), IS_RED);

        moveTo(secondClickXPos, secondWPortLayoutCenterYPos);
        MatcherAssert.assertThat("", getColorAt(secondClickXPos, secondWPortLayoutCenterYPos), IS_BLACK);

        clickOn(secondClickXPos, secondWPortLayoutCenterYPos);
        waitAmoment();
        MatcherAssert.assertThat("", getColorAt(secondClickXPos, secondWPortLayoutCenterYPos), IS_RED);

        moveTo(sampleXPos, secondWPortLayoutCenterYPos);
        waitAmoment();
        MatcherAssert.assertThat("", getColorAt(sampleXPos, secondWPortLayoutCenterYPos), IS_RED);

        clickOn(4, 4);
        waitAmoment();
    }

    /**
     * Tests the highlighting of overlapped equal-level edge and interlevel edge
     */
    @Test
    public void test03() throws InterruptedException {
        final KShapeLayout firstChildNodeLayout =
                viewContext.getViewModel().getChildren().get(0).getChildren().get(0);
        final int firstChildNodeYPos = Math.round(firstChildNodeLayout.getYpos());

        final KShapeLayout secondWPortLayout =
                viewContext.getViewModel().getChildren().get(0).getPorts().get(0);
        final int secondWPortLayoutCenterYPos = Math.round(secondWPortLayout.getYpos()) + 3;

        final int sampleXPos = 350;
        final int firstClickXPos = 5 + 50 + 100 + 5 + 100 + 5 + Math.round(1f / 2f * 100f);
        // port width + border spacing + spacing + port width + node with + port width
        // + edge spacing factor * spacing
        final int secondClickXPos = 5 + 50 + 100 + 5 + 100 + 5 + 20;
        // port width + border spacing + spacing + port width + node with + port width + 20

        moveTo(firstClickXPos, firstChildNodeYPos);
        MatcherAssert.assertThat("", getColorAt(firstClickXPos, firstChildNodeYPos), IS_BLACK);

        clickOn(firstClickXPos, firstChildNodeYPos);
        waitAmoment();
        MatcherAssert.assertThat("", getColorAt(firstClickXPos, firstChildNodeYPos), IS_RED);

        moveTo(sampleXPos, secondWPortLayoutCenterYPos);
        waitAmoment();
        MatcherAssert.assertThat("", getColorAt(sampleXPos, secondWPortLayoutCenterYPos), IS_RED);

        moveTo(secondClickXPos, secondWPortLayoutCenterYPos);
        MatcherAssert.assertThat("",getColorAt(secondClickXPos, secondWPortLayoutCenterYPos), IS_BLACK);

        clickOn(secondClickXPos, secondWPortLayoutCenterYPos);
        waitAmoment();
        MatcherAssert.assertThat("",getColorAt(secondClickXPos, secondWPortLayoutCenterYPos), IS_RED);

        moveTo(sampleXPos, secondWPortLayoutCenterYPos);
        waitAmoment();
        MatcherAssert.assertThat("",getColorAt(sampleXPos, secondWPortLayoutCenterYPos), IS_RED);

        clickOn(4, 4);
        waitAmoment();
    }

    private static final int A_MOMENT = 750;

    /**
     * Closes the employed shell.
     */
    @AfterClass
    public static void cleanup() {
        final Display display = shell.getDisplay();
        display.timerExec(A_MOMENT, new Runnable() {
            public void run() {
                shell.close();
            }
        });

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    private static void moveTo(final int x, final int y) {
        final Display d = shell.getDisplay();

        final Event moveTo = new Event();
        moveTo.type = SWT.MouseMove;
        moveTo.x = zeroPoint.x + x;
        moveTo.y = zeroPoint.y + y;
        d.post(moveTo);

        while (d.readAndDispatch())
            ;
    }

    private static void clickOn(final int x, final int y) {
        final Display d = shell.getDisplay();

        moveTo(x, y);

        final Event press = new Event();
        press.type = SWT.MouseDown;
        press.button = 1;
        d.post(press);

        while (d.readAndDispatch())
            ;

        final Event release = new Event();
        release.type = SWT.MouseUp;
        release.button = 1;
        d.post(release);

        while (d.readAndDispatch())
            ;
    }

    private static void waitAmoment() throws InterruptedException {
        final Display d = shell.getDisplay();
        final boolean[] sleep = new boolean[] { true };

        while (d.readAndDispatch())
            ;
        d.timerExec(A_MOMENT, new Runnable() {
            public void run() {
                sleep[0] = false;
            }
        });

        while (sleep[0]) {
            d.sleep();
            while (d.readAndDispatch())
                ;
        }
    }

    private static RGB getColorAt(final int x, final int y) {
        return ColorMatcher.getColorAt(canvas, x, y);
    }
}
