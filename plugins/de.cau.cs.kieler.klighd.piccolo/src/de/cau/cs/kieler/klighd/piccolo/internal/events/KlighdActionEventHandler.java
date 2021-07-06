/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 *
 * Copyright 2013 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 *
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klighd.piccolo.internal.events;

import java.awt.geom.Point2D;
import java.util.List;

import org.eclipse.elk.core.LayoutConfigurator;
import org.eclipse.elk.core.math.KVector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.PlatformUI;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import de.cau.cs.kieler.klighd.IAction;
import de.cau.cs.kieler.klighd.IAction.ActionResult;
import de.cau.cs.kieler.klighd.IViewer;
import de.cau.cs.kieler.klighd.KlighdDataManager;
import de.cau.cs.kieler.klighd.LightDiagramLayoutConfig;
import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.ZoomStyle;
import de.cau.cs.kieler.klighd.kgraph.KGraphElement;
import de.cau.cs.kieler.klighd.krendering.KAction;
import de.cau.cs.kieler.klighd.krendering.KRendering;
import de.cau.cs.kieler.klighd.krendering.ModifierState;
import de.cau.cs.kieler.klighd.krendering.Trigger;
import de.cau.cs.kieler.klighd.piccolo.IKlighdNode;
import de.cau.cs.kieler.klighd.piccolo.internal.controller.AbstractKGERenderingController;
import de.cau.cs.kieler.klighd.piccolo.internal.events.KlighdMouseEventListener.KlighdMouseEvent;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KNodeAbstractNode;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KNodeTopNode;
import de.cau.cs.kieler.klighd.piccolo.viewer.PiccoloViewer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;

/**
 * Event handler that invokes actions associated with KRenderings on corresponding mouse click events.
 *
 * @author chsch
 */
public class KlighdActionEventHandler implements PInputEventListener {

    private PiccoloViewer viewer = null;

    /**
     * Constructor.
     *
     * @param theViewer
     *            the {@link PiccoloViewer} it is attached to
     */
    public KlighdActionEventHandler(final PiccoloViewer theViewer) {
        this.viewer = theViewer;
    }

    /**
     * The well-formedness criterion of {@link KAction KActions} that is used to filter
     * the actions to be examined in {@link #processEvent(PInputEvent, int)}.
     */
    private static final Predicate<KAction> WELLFORMED = new Predicate<KAction>() {
        public boolean apply(final KAction action) {
            return action.getTrigger() != null && !Strings.isNullOrEmpty(action.getActionId());
        }
    };


    /**
     * {@inheritDoc}
     */
    public void processEvent(final PInputEvent inputEvent, final int eventType) {
        // SUPPRESS CHECKSTYLE PREVIOUS MethodLength -- don't bother me,
        //  there's lots of documentation!

        // CAUTION: parts of this method and parts of
        //  KlighdActionExecutionHandler.execute(...) (klighd.ui) are symmetric,
        // In case of changes make sure to update both!

        // caution: don't modify the evaluation of the 'handled' flag in an ad-hoc way,
        //  first make sure that the scenario described below is not enabled again.
        if (inputEvent.isHandled()) {
            return;
        }

        if (this.viewer.isMagnificationLensVisible()) {
            return;
        }

        if (!(inputEvent.getSourceSwingEvent() instanceof KlighdMouseEvent)) {
            return;
        }

        final KlighdMouseEvent me = (KlighdMouseEvent) inputEvent.getSourceSwingEvent();

        if (me.getEventType() == SWT.MouseMove) {
            return;
        }

        final PNode pickedNode = inputEvent.getPickedNode();

        KRendering rendering = pickedNode instanceof IKlighdNode.IKlighdFigureNode
                ? ((IKlighdNode.IKlighdFigureNode) pickedNode).getViewModelElement() : null;
//            (KRendering) pickedNode.getAttribute(AbstractKGERenderingController.ATTR_KRENDERING);


        if (rendering == null) {
            // in case no KRendering has been found ...

            // ... check whether a KNode's representative has been picked,
            //  which happens if a click or double click occurred on the canvas, for example
            if (pickedNode instanceof KNodeAbstractNode) {
                final KNodeAbstractNode iNode = (KNodeAbstractNode) pickedNode;

                // if so test whether the diagram's top node has been picked ...
                if (pickedNode instanceof KNodeTopNode) {
                    // and if so reveal the represented KNode and look for a dummy KRendering element
                    //  that might contain KActions
                    rendering = iNode.getViewModelElement().getData(KRendering.class);

                } else {
                    // Otherwise we assume that a nested KNode's representative has been picked,
                    //  which may happen if the diagram has been clipped to that particular KNode.

                    // in that case ask the associated KGE rendering controller ... 
                    final AbstractKGERenderingController<?, ?> controller =
                            iNode.getRenderingController();
                    if (controller == null) {
                        // the iNode will return 'null' in case 'iNode' has been removed from the
                        //  diagram and the corresponding controller has been released,
                        // a pickPack containing 'iNode' may however be hold by the canvas'
                        //  KlighdInputManager/PInputManager
                        return;
                    }
                    
                    // ... for the currently displayed KRendering
                    rendering = controller.getCurrentRenderingReference();
                }

                if (rendering == null) {
                    return;
                }
            } else {
                return;
            }
        }

        final Trigger trigger = me.getTrigger();

        ActionContext context = null; // construct the context lazily when it is required
        ActionResult resultOfLastAction = null;
        ActionResult resultOfLastActionRequiringLayout = null;

        // This flag is used to track the execution of actions requiring a layout update.
        boolean anyActionRequiresLayout = false;
        
        // This flag is used to track the execution of actions requiring a synthesis re-run.
        boolean anyActionRequiresSynthesis = false;

        for (final KAction action : Iterables.filter(rendering.getActions(), WELLFORMED)) {
            if (!action.getTrigger().equals(trigger) || !guardsMatch(action, me)) {
                continue;
            }

            final IAction actionImpl =
                    KlighdDataManager.getInstance().getActionById(action.getActionId());
            if (actionImpl == null) {
                continue;
            }

            if (context == null) {
                context = new ActionContext(viewer, trigger, null, rendering, inputEvent);
            }

            if (!anyActionRequiresLayout) {
                // in order to enable animated movements of diagram elements due to view model changes,
                //  the viewer must be informed to record view model changes before executing any action
                viewer.startRecording();

                // the related 'stopRecording(...)' will be performed below in case no layout update is
                //  required, and after the layout application, respectively
            }

            resultOfLastAction = actionImpl.execute(context);

            if (resultOfLastAction == null) {
                viewer.stopRecording(ZoomStyle.NONE, null, 0);

                final String msg = "KLighD action event handler: Execution of " + actionImpl.getClass()
                        + " returned 'null', expected an IAction.ActionResult.";
                throw new IllegalResultException(msg);
            }

            final boolean actionRequiresLayout = resultOfLastAction.getActionPerformed();
            if (actionRequiresLayout) {
                anyActionRequiresLayout = true;
                resultOfLastActionRequiringLayout = resultOfLastAction;
            }
            
            final boolean actionRequiresSynthesis = resultOfLastAction.getNeedsSynthesis();
            anyActionRequiresSynthesis |= actionRequiresSynthesis;
        }

        if (resultOfLastAction == null) {
            // ... indicating that no action has been executed at all
            // skip any layout and zoom update, do not tag 'inputEvent' to be handled, and ...
            return;
        }

        // otherwise 'resultOfLastAction' is a valid ActionResult determine the requested zoom style ...

        // caution: don't modify the evaluation of the 'handled' flag in an ad-hoc way,
        //  first make sure that the scenario described below is not enabled again.
        inputEvent.setHandled(true);

        final ViewContext vc = viewer.getViewContext();
        if (!anyActionRequiresLayout) {
            // ... i.e. no action requires layout (and 'resultOfLastActionRequiringLayout == null'),
            // skip the layout update and stop recording, and finish here

            // in case 'resultOfLastAction' was created via ActionResult.create(false) without any
            //  zooming requests the resulting zoomStyle will be ZoomStyle.NONE,
            //  see implementation of ActionResult.create(...)
            vc.getLayoutRecorder().stopRecording(ZoomStyle.create(resultOfLastAction, vc),
                    resultOfLastAction.getFocusElement(), 0);

            return;
        }

        final boolean doSynthesis = anyActionRequiresSynthesis;
        final boolean animate = resultOfLastAction.getAnimateLayout();
        final ZoomStyle zoomStyle = ZoomStyle.create(resultOfLastActionRequiringLayout, vc);
        final KGraphElement focusElement = resultOfLastActionRequiringLayout.getFocusElement();
        final KVector previousPosition = resultOfLastActionRequiringLayout.getPreviousPosition();
        final List<LayoutConfigurator> layoutConfigs = resultOfLastAction.getLayoutConfigs();

        // Execute the layout asynchronously in order to let the KLighdInputManager
        //  finish the processing of 'inputEvent' quickly.
        // Otherwise if the diagram layout engine interrupts its work by calling
        //  Display.readAndDispatch() and, with that, the control flow executing this method
        //  the processing of 'inputEvent' by the input manager might get triggered a
        //  second time by some timer event causing a kind of nested/recursive (!) evaluation
        //  of 'inputEvent' and, thereby, this method.
        // In addition, this scenario is tried to avoid by setting & evaluating the 'handled'
        //  flag of 'inputEvent' properly.
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
            public void run() {
                if (viewer.getControl() != null && viewer.getControl().isDisposed()) {
                    // This may happen if, e.g. the view oder editor is closed immediately after
                    //  triggering a potentially long-running action;
                    // in that case the subsequent executions may fail.
                    return;
                }
                
                // Update the view model first if a new synthesis is required.
                if (doSynthesis) {
                    vc.update();
                }
                
                new LightDiagramLayoutConfig(vc)
                        .animate(animate)
                        .zoomStyle(zoomStyle)
                        .focusElement(focusElement)
                        .previousPosition(previousPosition)
                        .options(layoutConfigs)
                        .performLayout();
            }
        });
    }

    private boolean guardsMatch(final KAction action, final KlighdMouseEvent event) {
        // Chain of implications. If the action requires / forbids some modifier, it has to be in that state, for all 
        // possible combinations.
        return (action.getAltPressed()     != ModifierState.PRESSED     ||  event.isAltDown())
            && (action.getAltPressed()     != ModifierState.NOT_PRESSED || !event.isAltDown())
            && (action.getCtrlCmdPressed() != ModifierState.PRESSED     ||  event.isControlDown())
            && (action.getCtrlCmdPressed() != ModifierState.NOT_PRESSED || !event.isControlDown())
            && (action.getShiftPressed()   != ModifierState.PRESSED     ||  event.isShiftDown())
            && (action.getShiftPressed()   != ModifierState.NOT_PRESSED || !event.isShiftDown());
    }

    /**
     * Specialized {@link IAction.ActionContext} providing the mouse click positions wrt. the
     * various coordinate systems.
     *
     * @author chsch
     */
    private static class ActionContext extends IAction.ActionContext {

        private final PInputEvent event;

        /**
         * Standard constructor.
         *
         * @param v
         *            the viewer the action was triggered in
         * @param t
         *            the trigger type that fired
         * @param kge
         *            the {@link KGraphElement} the action is invoked on
         * @param r
         *            the rendering the action is invoked on
         */
        ActionContext(final IViewer v, final Trigger t, final KGraphElement kge,
                final KRendering r, final PInputEvent event) {
            super(v, t, kge, r);
            this.event = event;
        }

        @Override
        public Point2D getDiagramRelativeMousePos() {
            return event.getPosition();
        }

        @Override
        public Point2D getCanvasRelativeMousePos() {
            return event.getCanvasPosition();
        }

        @Override
        public java.awt.Point getControlRelativeMousePos() {
            // the following cast doesn't need to be checked since the action event handler
            //  doesn't react on other kinds events, see above
            final MouseEvent me = ((KlighdMouseEvent) event.getSourceSwingEvent()).getEvent();
            return new java.awt.Point(me.x, me.y);
        }

        @Override
        public java.awt.Point getDisplayRelativeMousePos() {
            // the following cast doesn't need to be checked since the action event handler
            //  doesn't react on other kinds events, see above
            final MouseEvent me = ((KlighdMouseEvent) event.getSourceSwingEvent()).getEvent();
            Point p = ((PiccoloViewer) getActiveViewer()).getControl().toDisplay(me.x, me.y);
            return new java.awt.Point(p.x, p.y);
        }
    }


    /**
     * A dedicated exception indicating an illegal result of a method.<br>
     * It is currently thrown if implementations of {@link IAction#execute(ActionContext)} returns
     * <code>null</code>.
     *
     * @author chsch
     */
    public class IllegalResultException extends RuntimeException {

        private static final long serialVersionUID = -5838587904577606037L;

        /**
         * Constructor.
         *
         * @param msg
         *            the detail message. The detail message is saved for later retrieval by the
         *            {@link #getMessage()} method.
         */
        public IllegalResultException(final String msg) {
            super(msg);
        }
    }
}
