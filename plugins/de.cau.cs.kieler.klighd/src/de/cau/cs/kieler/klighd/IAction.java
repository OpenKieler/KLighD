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
package de.cau.cs.kieler.klighd;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;

import org.eclipse.elk.core.LayoutConfigurator;
import org.eclipse.elk.core.math.KVector;

import de.cau.cs.kieler.klighd.kgraph.KGraphElement;
import de.cau.cs.kieler.klighd.kgraph.KGraphPackage;
import de.cau.cs.kieler.klighd.kgraph.KNode;
import de.cau.cs.kieler.klighd.krendering.KRendering;
import de.cau.cs.kieler.klighd.krendering.Trigger;
import de.cau.cs.kieler.klighd.util.ModelingUtil;
import de.cau.cs.kieler.klighd.viewers.ContextViewer;

/**
 * Common interface of classes providing implementations of
 * {@link de.cau.cs.kieler.klighd.krendering.KAction KActions}.<br>
 * <br>
 * {@link IAction IActions} are currently assumed to be stateless and, thus, a singleton instance is
 * created only.
 *
 * @author chsch
 *
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch
 */
public interface IAction {

    /**
     * This method performs the intended action on a diagram.
     *
     * @param context
     *            an {@link ActionContext} instance providing various useful data.
     * @return an {@link ActionResult} providing {@link ILayoutConfig layout config(s)} to be
     *         incorporated while updating the layout after the action has been performed, as well
     *         as configurations of 'animateLayout', 'zoomToFit', etc.<br>
     *         Use {@link ActionResult#createResult(boolean, ILayoutConfig...)} for creation.
     */
    ActionResult execute(ActionContext context);


    /**
     * This class comprises various useful data required for performing things in diagrams.
     *
     * @author chsch
     */
    public static class ActionContext {

        private IViewer viewer = null;
        private Trigger trigger = null;
        private KGraphElement kgraphElement = null;
        private KRendering rendering = null;

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
        public ActionContext(final IViewer v, final Trigger t, final KGraphElement kge,
                final KRendering r) {
            this.viewer = v;
            this.trigger = t;
            this.kgraphElement = kge;
            this.rendering = r;
        }

        /**
         * @return the current actual diagram viewer, usually the PiccoloViewer
         */
        public IViewer getActiveViewer() {
            return viewer;
        }

        /**
         * @return the current {@link ContextViewer}
         */
        public ContextViewer getContextViewer() {
            return viewer.getContextViewer();
        }

        /**
         * @return the view context related to the current diagram
         */
        public ViewContext getViewContext() {
            return viewer.getViewContext();
        }

        /**
         * @return the trigger
         */
        public Trigger getTrigger() {
            return trigger;
        }

        /**
         * @return the node
         */
        public KGraphElement getKGraphElement() {
            if (kgraphElement == null) {
                kgraphElement = (KGraphElement) ModelingUtil.eContainerOfType(rendering,
                        KGraphPackage.eINSTANCE.getKGraphElement());
            }
            return kgraphElement;
        }

        /**
         * @return the node
         */
        public KNode getKNode() {
            return (KNode) ModelingUtil.eContainerOrSelfOfType(getKGraphElement(),
                    KGraphPackage.eINSTANCE.getKNode());
        }

        /**
         * @return the rendering
         */
        public KRendering getKRendering() {
            return rendering;
        }


        /**
         * Reveals the source (domain) element associated with the given <code>node</code>
         * if any exists, or <code>null</code>.
         *
         * @param <T> the expected return type
         * @param viewElement the {@link KNode} to get the associated source element for
         * @return the related source (domain) element if any associated one exists, or <code>null</code>
         */
        @SuppressWarnings("unchecked")
        public <T> T getDomainElement(final KNode viewElement) {
            return (T) this.viewer.getViewContext().getSourceElement(viewElement);
        }


        /**
         * @return an AWT Geometry {@link Point2D} denoting the mouse cursor position while invoking
         *         the action in overall diagram coordinates, or <code>null</code> if the action was
         *         not triggered by clicking on a diagram element but, e.g., via a menu contribution.
         */
        public Point2D getDiagramRelativeMousePos() {
            return null;
        }

        /**
         * @return an AWT Geometry {@link Point2D} denoting the mouse cursor position while invoking
         *         the action in canvas coordinates, or <code>null</code> if the action was not
         *         triggered by clicking on a diagram element but, e.g., via a menu contribution; is
         *         similar to {@link #getControlRelativeMousePos()}.
         */
        public Point2D getCanvasRelativeMousePos() {
            return null;
        }
        // TODO what is the difference between SWT and AWT Point???

        /**
         * @return an SWT {@link Point} denoting the mouse cursor position while invoking the action
         *         in {@link org.eclipse.swt.widgets.Control Control} coordinates, or
         *         <code>null</code> if the action was not triggered by clicking on a diagram
         *         element but, e.g., via a menu contribution; is similar to
         *         {@link #getCanvasRelativeMousePos()}.
         */
        public Point getControlRelativeMousePos() {
            return null;
        }

        /**
         * @return an SWT {@link Point} denoting the mouse cursor position while invoking the action
         *         in {@link org.eclipse.swt.widgets.Display Display} coordinates, or
         *         <code>null</code> if the action was not triggered by clicking on a diagram
         *         element but, e.g., via a menu contribution.
         */
        public Point getDisplayRelativeMousePos() {
            return null;
        }
    }


    /**
     * Return type being expected by implementations of {@link IAction#execute(ActionContext)}.
     * Besides {@link ILayoutConfig ILayoutConfigs}
     *
     * @author chsch
     */
    public static final class ActionResult {

        private static final IKlighdPreferenceStore STORE = KlighdPreferences.getPreferenceStore();

        private List<LayoutConfigurator> layoutConfigs = null;

        private boolean needsSynthesis = false;
        private boolean actionPerformed = true;
        private Boolean animateLayout = null;
        private Boolean zoomToActualSize = null;
        private Boolean zoomToFit = null;
        private Boolean zoomToFitContent = null;
        private Boolean zoomToFocus = null;
        private Boolean zoomToStay = null;
        private KGraphElement focus = null;
        private KVector previousPosition = null;

        private ActionResult(final boolean theActionPerformed) {
            this.actionPerformed = theActionPerformed;
        }

        private ActionResult(final boolean theActionPerformed,
                final List<LayoutConfigurator> theConfigs) {
            
            this.actionPerformed = theActionPerformed;
            this.layoutConfigs = theConfigs;
        }

        /**
         * Creates a new {@link ActionResult} instance .
         *
         * @param actionRequiresLayout
         *            flag indicating whether the action performed changes on the diagram requiring
         *            a subsequent layout re-computation
         * @param config
         *            some additional {@link LayoutConfigurator LayoutConfigurators} to by incorporated,
         *            will be ignored if {@code actionRequiresLayout == false}
         * @return the requested {@link ActionResult}
         */
        public static ActionResult createResult(final boolean actionRequiresLayout,
                final LayoutConfigurator... config) {
            
            if (actionRequiresLayout) {
                return new ActionResult(actionRequiresLayout, Arrays.asList(config));
            } else {
                return new ActionResult(actionRequiresLayout).dontZoom();
            }
        }

        /**
         * Creates a new {@link ActionResult} instance .
         *
         * @param actionRequiresLayout
         *            flag indicating whether the action performed changes on the diagram requiring
         *            a subsequent layout re-computation
         * @return the requested {@link ActionResult}
         */
        public static ActionResult createResult(final boolean actionRequiresLayout) {
            if (actionRequiresLayout) {
                return new ActionResult(actionRequiresLayout);
            } else {
                return new ActionResult(actionRequiresLayout).dontZoom();
            }
        }
        
        /**
         * Do a new synthesis before the subsequent automatic layout run.
         * Overwrites that the action now definitely also needs an automatic layout run after the synthesis.
         * 
         * @return <code>this</code> {@link ActionResult}
         */
        public ActionResult doSynthesis() {
            this.needsSynthesis = true;
            this.actionPerformed = true;
            return this;
        }

        
        /**
         * Schedule animation during the subsequent automatic layout run.
         *
         * @return <code>this</code> {@link ActionResult}
         */
        public ActionResult doAnimateLayout() {
            this.animateLayout = true;
            return this;
        }

        /**
         * Suppress animation during the subsequent automatic layout run.
         *
         * @return <code>this</code> {@link ActionResult}
         */
        public ActionResult dontAnimateLayout() {
            this.animateLayout = false;
            return this;
        }

        /**
         * Schedule zoomToActualSize during the subsequent automatic layout run.
         *
         * @return <code>this</code> {@link ActionResult}
         */
        public ActionResult doZoomToActualSize() {
            this.zoomToActualSize = true;
            this.zoomToFit = null;
            this.zoomToFitContent = null;
            this.zoomToFocus = null;
            this.zoomToStay = null;
            return this;
        }

        /**
         * Suppress zoomToActualSize during the subsequent automatic layout run.
         *
         * @return <code>this</code> {@link ActionResult}
         */
        public ActionResult dontZoomToActualSize() {
            this.zoomToActualSize = false;
            return this;
        }

        /**
         * Schedule zoomToFit during the subsequent automatic layout run.
         *
         * @return <code>this</code> {@link ActionResult}
         */
        public ActionResult doZoomToFit() {
            this.zoomToActualSize = null;
            this.zoomToFit = true;
            this.zoomToFitContent = null;
            this.zoomToFocus = null;
            this.zoomToStay = null;
            return this;
        }

        /**
         * Schedule zoomToFitContent during the subsequent automatic layout run.
         *
         * @return <code>this</code> {@link ActionResult}
         */
        public ActionResult doZoomToFitContent() {
            this.zoomToActualSize = null;
            this.zoomToFit = null;
            this.zoomToFitContent = true;
            this.zoomToFocus = null;
            this.zoomToStay = null;
            return this;
        }

        /**
         * Suppress zoomToFit during the subsequent automatic layout run.
         *
         * @return <code>this</code> {@link ActionResult}
         */
        public ActionResult dontZoomToFit() {
            this.zoomToFit = false;
            return this;
        }

        /**
         * Schedule zoomToFocus during the subsequent automatic layout run.
         *
         * @return <code>this</code> {@link ActionResult}
         */
        public ActionResult doZoomToFocus() {
            this.zoomToActualSize = null;
            this.zoomToFit = null;
            this.zoomToFitContent = null;
            this.zoomToFocus = true;
            this.zoomToStay = null;
            return this;
        }

        /**
         * Schedule zoomToFocus during the subsequent automatic layout run.
         *
         * @param focusNode
         *            the {@link KNode} to focus
         * @return <code>this</code> {@link ActionResult}
         */
        public ActionResult doZoomToFocus(final KNode focusNode) {
            this.zoomToActualSize = null;
            this.zoomToFit = null;
            this.zoomToFitContent = null;
            this.zoomToFocus = true;
            this.zoomToStay = null;
            this.focus = focusNode;
            return this;
        }

        /**
         * Suppress zoomToFocus during the subsequent automatic layout run.
         *
         * @return <code>this</code> {@link ActionResult}
         */
        public ActionResult dontZoomToFocus() {
            this.zoomToFocus = false;
            return this;
        }
        
        /**
         * Schedule toomZoStay during the subsequent automatic layout run.
         * 
         * @return <code>this</code> {@link ActionResult}
         */
        public ActionResult doZoomToStay(final KVector previousPosition, final KGraphElement focusElement) {
            this.zoomToActualSize = null;
            this.zoomToFit = null;
            this.zoomToFitContent = null;
            this.zoomToFocus = null;
            this.zoomToStay = true;
            this.previousPosition = previousPosition;
            this.focus = focusElement;
            return this;
        }
        
        /**
         * Suppress zoomToStay during the subsequent automatic layout run.
         * 
         * @return <code>this</code> {@link ActionResult}
         */
        public ActionResult dontZoomZoStay() {
            this.zoomToStay = false;
            return this;
        }

        /**
         * Suppress any zooming during the subsequent automatic layout run.
         *
         * @return <code>this</code> {@link ActionResult}
         */
        public ActionResult dontZoom() {
            this.zoomToActualSize = false;
            this.zoomToFit = false;
            this.zoomToFitContent = false;
            this.zoomToFocus = false;
            this.zoomToStay = false;
            return this;
        }

        /**
         * Getter.
         *
         * @return the attached {@link LayoutConfigurator LayoutConfigurators}
         */
        public List<LayoutConfigurator> getLayoutConfigs() {
            return this.layoutConfigs;
        }
        
        /**
         * Getter. Denotes whether a subsequent synthesis is required.
         * @return The {@link #needsSynthesis} flag.
         */
        public boolean getNeedsSynthesis() {
            return this.needsSynthesis;
        }

        /**
         * Getter.
         *
         * Getter. Denotes whether a subsequent layout update run is required.
         * @return the {@link #actionPerformed} flag
         */
        public boolean getActionPerformed() {
            return this.actionPerformed;
        }

        /**
         * Getter.
         *
         * @return the {@link #animateLayout} flag
         */
        public boolean getAnimateLayout() {
            return this.animateLayout != null
                    ? this.animateLayout : STORE.getBoolean(KlighdPreferences.ANIMATE_LAYOUT);
        }

        /**
         * Getter. Returns a {@link Boolean} instead of the primitive <code>boolean</code>
         * in order distinguish the 'not configured' state. Returns <code>null</code> in this
         * case.
         *
         * @return the {@link #zoomToActualSize} state
         */
        public Boolean getZoomToActualSize() {
            return this.zoomToActualSize;
        }

        /**
         * Getter. Returns a {@link Boolean} instead of the primitive <code>boolean</code>
         * in order distinguish the 'not configured' state. Returns <code>null</code> in this
         * case.
         *
         * @return the {@link #zoomToFit} state
         */
        public Boolean getZoomToFit() {
            return this.zoomToFit;
        }

        /**
         * Getter. Returns a {@link Boolean} instead of the primitive <code>boolean</code>
         * in order distinguish the 'not configured' state. Returns <code>null</code> in this
         * case.
         *
         * @return the {@link #zoomToFitContent} state
         */
        public Boolean getZoomToFitContent() {
            return this.zoomToFitContent;
        }

        /**
         * Getter. Returns a {@link Boolean} instead of the primitive <code>boolean</code>
         * in order distinguish the 'not configured' state. Returns <code>null</code> in this
         * case.
         *
         * @return the {@link #zoomToFocus} state
         */
        public Boolean getZoomToFocus() {
            return this.zoomToFocus;
        }
        
        /**
         * Getter. Returns a {@link Boolean} instead of the primitive <code>boolean</code>
         * in order distinguish the 'not configured' state. Returns <code>null</code> in this
         * case.
         * 
         * @return the {@link #zoomToStay} state.
         */
        public Boolean getZoomToStay() {
            return this.zoomToStay;
        }

        /**
         * Getter.
         *
         * @return the {@link KNode} to focus on
         * @deprecated use {@link #getFocusElement()} instead.
         */
        public KNode getFocusNode() {
            if (focus instanceof KNode) {
                return (KNode) focus;
            } else {
                return null;
            }
        }

        /**
         * Getter.
         *
         * @return the {@link KGraphElement} to focus on
         */
        public KGraphElement getFocusElement() {
            return focus;
        }
        
        /**
         * Getter.
         * 
         * @return the previous position of the element the action was issued on
         */
        public KVector getPreviousPosition() {
            return previousPosition;
        }
    }
}
