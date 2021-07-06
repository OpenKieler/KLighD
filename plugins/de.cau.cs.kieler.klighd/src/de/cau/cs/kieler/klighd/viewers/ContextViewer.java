/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 *
 * Copyright 2011 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 *
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klighd.viewers;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static de.cau.cs.kieler.klighd.util.KlighdPredicates.isSelectable;
import static de.cau.cs.kieler.klighd.util.KlighdPredicates.notIn;
import static java.util.Collections.singleton;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.elk.core.math.KVector;
import org.eclipse.emf.common.util.AbstractTreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

import de.cau.cs.kieler.klighd.IKlighdSelection;
import de.cau.cs.kieler.klighd.IViewChangeListener;
import de.cau.cs.kieler.klighd.IViewer;
import de.cau.cs.kieler.klighd.Klighd;
import de.cau.cs.kieler.klighd.KlighdTreeSelection;
import de.cau.cs.kieler.klighd.ViewChangeType;
import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.ZoomStyle;
import de.cau.cs.kieler.klighd.internal.IDiagramOutlinePage;
import de.cau.cs.kieler.klighd.internal.ILayoutRecorder;
import de.cau.cs.kieler.klighd.internal.util.KlighdInternalProperties;
import de.cau.cs.kieler.klighd.kgraph.KGraphElement;
import de.cau.cs.kieler.klighd.kgraph.KNode;
import de.cau.cs.kieler.klighd.krendering.KRendering;
import de.cau.cs.kieler.klighd.krendering.KRenderingPackage;
import de.cau.cs.kieler.klighd.krendering.KText;

/**
 * A viewer for instances of type {@code ViewContext}. It is instantiated by
 * {@link de.cau.cs.kieler.klighd.views.DiagramViewPart DiagramViewPart} and
 * {@link de.cau.cs.kieler.klighd.views.DiagramEditorPart DiagramEditorPart}.<br>
 * <br>
 * This class acts as a wrapper for the viewer supplied by the current view context. The method
 * {@code getControl} returns the control of that viewer, all other methods are delegated to the
 * wrapped viewer.<br>
 * <br>
 * The motivation of this class is the intended multiformity of model viewers (although only the
 * Piccolo2D-based one has been realized). In addition, multiple diagram viewers, i.e. diagram
 * exhibiting controls, might be hosted and unified within a {@link ContextViewer}.<br>
 * <br>
 * During the initialization it is possible to set a message to be shown instead of a view context,
 * the wrapped viewer is then of type {@code StringViewer}.<br>
 * <br>
 * This viewer also implements the {@code ISelectionProvider} interface and acts as KLighD's
 * provider of selection events.
 *
 * @author mri
 * @author chsch
 * @author msp
 *
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch
 */
public class ContextViewer implements IViewer, ILayoutRecorder, ISelectionProvider,
        IDiagramOutlinePage.Provider {

    /** the parent composite for diagram viewers. */
    private Composite diagramComposite;

    /** the current viewer. */
    private IViewer currentViewer;

    /** the current view context. */
    private ViewContext currentViewContext = null;

    /** the {@link #currentViewer} if it is a {@link ILayoutRecorder}, <code>null</code> otherwise. */
    private ILayoutRecorder layoutRecorder;


    /**
     * Constructs a view context viewer.
     *
     * @param parent
     *            the parent composite
     */
    public ContextViewer(final Composite parent) {
        this.diagramComposite = parent;
    }

    /**
     * Employs the given <code>viewer</code>.
     *
     * @param viewer
     *            the {@link IViewer} to be employed
     */
    protected synchronized void setViewer(final IViewer viewer) {
        // remove the current viewer if someone exists
        if (currentViewer != null) {
            currentViewer.getControl().dispose();
            currentViewer = null;
        }

        final IViewer objViewer = viewer;
        currentViewer = objViewer;

        diagramComposite.layout();

        if (currentViewer instanceof ILayoutRecorder) {
            layoutRecorder = (ILayoutRecorder) currentViewer;
        }
    }


    /* -------------------------------------------------- */
    /*   implementation of IDiagramOutlinePage.Provider   */
    /* -------------------------------------------------- */

    /**
     * {@inheritDoc}
     */
    public IDiagramOutlinePage getDiagramOutlinePage() {
        if (currentViewer instanceof IDiagramOutlinePage.Provider) {
            return ((IDiagramOutlinePage.Provider) currentViewer).getDiagramOutlinePage();
        } else {
            return null;
        }
    }


    /* -------------------------------------------------- */
    /*   implementation of ILayoutRecorder                */
    /* -------------------------------------------------- */

    /**
     * {@inheritDoc}
     */
    public void startRecording() {
        if (layoutRecorder != null) {
            layoutRecorder.startRecording();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void stopRecording(final int animationTime) {
        if (layoutRecorder != null) {
            layoutRecorder.stopRecording(animationTime);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void stopRecording(final ZoomStyle zoomStyle, final KGraphElement focusElement, final int animationTime) {
        stopRecording(zoomStyle, focusElement, null, animationTime);
    }

    /**
     * {@inheritDoc}
     */
    public void stopRecording(final ZoomStyle zoomStyle, final KGraphElement focusElement,
            final KVector previousPosition, final int animationTime) {
        if (layoutRecorder != null) {
            layoutRecorder.stopRecording(zoomStyle, focusElement, previousPosition, animationTime);
        }
    }


    /* -------------------------------------------------- */
    /*   implementation of IViewer                        */
    /* -------------------------------------------------- */

    /**
     * {@inheritDoc}
     */
    public ContextViewer getContextViewer() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void setModel(final Object model) {
        setModel(model, false);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void setModel(final Object model, final boolean sync) {
        // if the model is a view context adapt the viewer to the given context if possible
        if (model instanceof ViewContext) {
            final ViewContext viewContext = (ViewContext) model;

            // set the new view context
            currentViewContext = viewContext;

            // create and set a corresponding new viewer
            setViewer(viewContext.createViewer(this, diagramComposite));

            // initialize the current selection
            notifySelectionListeners(new KlighdTreeSelection(currentViewContext));
        }
    }

    /**
     * {@inheritDoc}
     */
    public ViewContext getViewContext() {
        return currentViewContext;
    }

    /**
     * {@inheritDoc}
     */
    public Control getControl() {
        return currentViewer != null ? currentViewer.getControl() : null;
    }

    /**
     * Returns the currently active view context.
     *
     * @deprecated use {@link IViewer#getViewContext()}, which is implemented by this class
     *
     * @return the view context
     */
    public ViewContext getCurrentViewContext() {
        return currentViewContext;
    }

    /**
     * Returns the currently active viewer.
     *
     * @return the viewer
     */
    public IViewer getActiveViewer() {
        return currentViewer;
    }

    /**
     * {@inheritDoc}
     */
    public void addViewChangeListener(final IViewChangeListener listener,
            final ViewChangeType... eventTypes) {
        if (currentViewer != null) {
            if (listener != null) {
                currentViewer.addViewChangeListener(listener, eventTypes);
            }
        } else {
            throw new RuntimeException("KLighD: Registering the "
                    + listener.getClass().getCanonicalName()
                    + " is not possible, since the actual diagram viewer is not initialized yet.");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addViewChangeListener(final IViewChangeListener listener,
            final EnumSet<ViewChangeType> eventTypes) {
        if (currentViewer != null) {
            if (listener != null) {
                currentViewer.addViewChangeListener(listener, eventTypes);
            }
        } else {
            throw new RuntimeException("KLighD: Registering the "
                    + listener.getClass().getCanonicalName()
                    + " is not possible, since the actual diagram viewer is not initialized yet.");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeViewChangeListener(final IViewChangeListener listener) {
        if (listener != null && currentViewer != null) {
            currentViewer.removeViewChangeListener(listener);
        }
    }


    /* ----------------------------- */
    /*   the view manipulation API   */
    /* ----------------------------- */

    /**
     * {@inheritDoc}
     */
    public boolean isDisplayed(final Object semanticElement, final boolean checkParents) {
        final EObject diagramNode =
                getViewContext().getTargetElement(semanticElement, KNode.class);
        if (diagramNode instanceof KGraphElement) {
            return currentViewer.isDisplayed((KGraphElement) diagramNode, checkParents);
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDisplayed(final KGraphElement diagramElement, final boolean checkParents) {
        return this.currentViewer.isDisplayed(diagramElement, checkParents);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isVisible(final Object semanticElement, final boolean checkParents) {
        final EObject diagramNode =
                getViewContext().getTargetElement(semanticElement, KNode.class);
        if (diagramNode instanceof KGraphElement) {
            return currentViewer.isVisible((KGraphElement) diagramNode, checkParents);
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isVisible(final KGraphElement diagramElement, final boolean checkParents) {
        return this.currentViewer.isVisible(diagramElement, checkParents);
    }

    private static void checkValidThread(String msgReplacement) {
        if (Klighd.IS_PLATFORM_RUNNING && PlatformUI.isWorkbenchRunning() && Display.getCurrent() == null) {
            throw new RuntimeException(NON_DISPLAY_ERROR_MSG.replace("##", msgReplacement));
        }
    }

    private static final String NON_DISPLAY_ERROR_MSG =
            "KLighD: Application attempted to traverse an Iterator provided by "
            + "IViewer.##. Evaluations of those Iterators must be "
            + "performed by the display (UI) thread for integrity reasons.";

    /**
     * {@inheritDoc}
     */
    public Iterator<KNode> getVisibleDiagramNodes() {
        // SUPPRESS CHECKSTYLE PREVIOUS 4 Javadoc, see http://sourceforge.net/p/checkstyle/bugs/592/

        final IViewer activeViewer = getActiveViewer();
        final KNode clip = activeViewer.getClip();

        if (!activeViewer.isVisible(clip, false)) {
            return Collections.emptyIterator();

        } else {
            return new AbstractTreeIterator<KNode>(clip) {
                private static final long serialVersionUID = 1021356500841593549L;

                @Override
                protected Iterator<? extends KNode> getChildren(final Object object) {
                    checkValidThread("getVisibleDiagramNodes()");

                    return Iterators.filter(((KNode) object).getChildren().iterator(),
                            new Predicate<KNode>() {

                        public boolean apply(final KNode input) {
                            return activeViewer.isVisible(input, false);
                        }
                    });
                }
            };
        }
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<KGraphElement> getVisibleDiagramElements() {
        // SUPPRESS CHECKSTYLE PREVIOUS 4 Javadoc, see http://sourceforge.net/p/checkstyle/bugs/592/

        final IViewer activeViewer = getActiveViewer();
        final KNode clip = activeViewer.getClip();

        if (!activeViewer.isVisible(clip, false)) {
            return Collections.emptyIterator();

        } else {
            return new AbstractTreeIterator<KGraphElement>(clip) {
                private static final long serialVersionUID = 1021356500841593549L;

                @Override
                protected Iterator<? extends KGraphElement> getChildren(final Object object) {
                    checkValidThread("getVisibleDiagramElements()");

                    final Iterator<EObject> candidates;
                    if (object instanceof KNode) {
                        candidates =
                                Iterators.concat(((EObject) object).eContents().iterator(),
                                        ((KNode) object).getIncomingEdges().iterator());
                    } else {
                        candidates = ((EObject) object).eContents().iterator();
                    }

                    @SuppressWarnings("unchecked")
                    final
                    Iterator<? extends KGraphElement> res = (Iterator<KGraphElement>) (Iterator<?>)
                            Iterators.filter(candidates, filter);

                    return res;
                }

                private Predicate<EObject> filter = new Predicate<EObject>() {

                    public boolean apply(final EObject input) {
                        return input instanceof KGraphElement
                                && activeViewer.isVisible((KGraphElement) input, false);
                    }
                };
            };
        }
    }

    /**
     * {@inheritDoc}
     */
    public void zoomToFocus(final Object semanticElement, final int duration) {
        final EObject diagramNode =
                getViewContext().getTargetElement(semanticElement, KNode.class);
        if (diagramNode instanceof KNode) {
            currentViewer.zoomToFocus((KNode) diagramNode, duration);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void zoomToFocus(final KNode diagramElement, final int duration) {
        currentViewer.zoomToFocus(diagramElement, duration);
    }

    /**
     * {@inheritDoc}
     */
    public void zoomToLevel(final float zoomLevel, final int duration) {
        if (currentViewer != null) {
            currentViewer.zoomToLevel(zoomLevel, duration);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void zoom(final ZoomStyle style, final int duration) {
        if (currentViewer != null) {
            currentViewer.zoom(style, duration);
        }
    }

    /**
     * {@inheritDoc}
     */
    public float getZoomLevel() {
        if (currentViewer != null) {
            return currentViewer.getZoomLevel();
        } else {
            return 1f;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isExpanded(final Object semanticElement) {
        final EObject diagramNode =
                getViewContext().getTargetElement(semanticElement, KNode.class);
        if (diagramNode instanceof KNode) {
            return currentViewer.isExpanded((KNode) diagramNode);
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isExpanded(final KNode diagramElement) {
        return currentViewer.isExpanded(diagramElement);
    }

    /**
     * {@inheritDoc}
     */
    public void collapse(final Object semanticElement) {
        final EObject diagramNode =
                getViewContext().getTargetElement(semanticElement, KNode.class);
        if (diagramNode instanceof KNode) {
            currentViewer.collapse((KNode) diagramNode);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void collapse(final KNode diagramElement) {
        currentViewer.collapse(diagramElement);
    }

    /**
     * {@inheritDoc}
     */
    public void expand(final Object semanticElement) {
        final EObject diagramNode =
                getViewContext().getTargetElement(semanticElement, KNode.class);
        if (diagramNode instanceof KNode) {
            currentViewer.expand((KNode) diagramNode);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void expand(final KNode diagramElement) {
        currentViewer.expand(diagramElement);
    }

    /**
     * {@inheritDoc}
     */
    public void toggleExpansion(final Object semanticElement) {
        final EObject diagramNode =
                getViewContext().getTargetElement(semanticElement, KNode.class);
        if (diagramNode instanceof KNode) {
            currentViewer.toggleExpansion((KNode) diagramNode);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void toggleExpansion(final KNode diagramElement) {
        currentViewer.toggleExpansion(diagramElement);
    }

    /**
     * {@inheritDoc}
     */
    public void hide(final Object semanticElement) {
        final EObject diagramElement =
                getViewContext().getTargetElement(semanticElement, KGraphElement.class);
        if (diagramElement instanceof KGraphElement) {
            currentViewer.hide((KGraphElement) diagramElement);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void hide(final KGraphElement diagramElement) {
        currentViewer.hide(diagramElement);
    }

    /**
     * {@inheritDoc}
     */
    public void show(final Object semanticElement) {
        final EObject diagramElement =
                getViewContext().getTargetElement(semanticElement, KGraphElement.class);
        if (diagramElement instanceof KGraphElement) {
            currentViewer.show((KGraphElement) diagramElement);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void show(final KGraphElement diagramElement) {
        currentViewer.show(diagramElement);
    }

    /**
     * {@inheritDoc}
     */
    public void clip(final Object semanticElement) {
        if (semanticElement == null) {
            this.currentViewer.clip(this.getViewContext().getViewModel());
            return;
        }

        final EObject diagramElement =
                getViewContext().getTargetElement(semanticElement, KGraphElement.class);
        if (diagramElement instanceof KNode) {
            this.currentViewer.clip((KNode) diagramElement);
        }
    }

    /**
     * {@inheritDoc}
     */
        public void clip(Object semanticElement, Boolean hideClipNodePorts,
                Boolean hideClipNodeLabels) {
        if (semanticElement == null) {
            this.currentViewer.clip(this.getViewContext().getViewModel(), hideClipNodePorts, hideClipNodeLabels);
            return;
        }

        final EObject diagramElement =
                getViewContext().getTargetElement(semanticElement, KGraphElement.class);
        if (diagramElement instanceof KNode) {
            this.currentViewer.clip((KNode) diagramElement, hideClipNodePorts, hideClipNodeLabels);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void clip(final KNode diagramElement) {
        if (diagramElement == null) {
            this.currentViewer.clip(this.getViewContext().getViewModel());
        } else {
            this.currentViewer.clip(diagramElement);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void clip(KNode diagramElement, Boolean hideClipNodePorts, Boolean hideClipNodeLabels) {
        if (diagramElement == null) {
            this.currentViewer.clip(this.getViewContext().getViewModel(), hideClipNodePorts, hideClipNodeLabels);
        } else {
            this.currentViewer.clip(diagramElement, hideClipNodePorts, hideClipNodeLabels);
        }
    }

    /**
     * {@inheritDoc}
     */
    public KNode getClip() {
        return this.currentViewer.getClip();
    }

    /**
     * {@inheritDoc}
     */
    public void scale(final Object semanticElement, final double scale) {
        final EObject diagramElement =
                getViewContext().getTargetElement(semanticElement, KGraphElement.class);
        if (diagramElement instanceof KNode) {
            currentViewer.scale((KNode) diagramElement, scale);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void scale(final KNode diagramElement, final double scale) {
        currentViewer.scale(diagramElement, scale);
    }

    /**
     * {@inheritDoc}
     */
    public double getScale(final Object semanticElement) {
        final EObject diagramNode =
                getViewContext().getTargetElement(semanticElement, KNode.class);
        if (diagramNode instanceof KNode) {
            return currentViewer.getScale((KNode) diagramNode);
        } else {
            return 1f;
        }
    }

    /**
     * {@inheritDoc}
     */
    public double getScale(final KNode diagramElement) {
        return currentViewer.getScale(diagramElement);
    }

    /**
     * {@inheritDoc}
     */
    public void reveal(final Object semanticElement, final int duration) {
        final EObject diagramElement =
                getViewContext().getTargetElement(semanticElement, KGraphElement.class);
        if (diagramElement instanceof KGraphElement) {
            currentViewer.centerOn((KGraphElement) diagramElement, duration);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void reveal(final KGraphElement diagramElement, final int duration) {
        if (currentViewer != null) {
            currentViewer.reveal(diagramElement, duration);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void centerOn(final Object semanticElement, final int duration) {
        final EObject diagramElement =
                getViewContext().getTargetElement(semanticElement, KGraphElement.class);
        if (diagramElement instanceof KGraphElement) {
            currentViewer.centerOn((KGraphElement) diagramElement, duration);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void centerOn(final KGraphElement diagramElement, final int duration) {
        if (currentViewer != null) {
            currentViewer.centerOn(diagramElement, duration);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void panToTopLeftCorner(final Object semanticElement, final int duration) {
        final EObject diagramElement =
                getViewContext().getTargetElement(semanticElement, KGraphElement.class);
        if (diagramElement instanceof KNode) {
            currentViewer.panToTopLeftCorner((KNode) diagramElement, duration);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void panToTopLeftCorner(final KNode diagramElement, final int duration) {
        if (currentViewer != null) {
            currentViewer.panToTopLeftCorner(diagramElement, duration);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void panDiagramToTopLeftCorner(final int duration) {
        if (currentViewer != null) {
            currentViewer.panDiagramToTopLeftCorner(duration);
        }
    }


    /* ----------------------------- */
    /*   the selection setting API   */
    /* ----------------------------- */

    private final Function<Object, EObject> getViews = new Function<Object, EObject>() {
        public EObject apply(final Object semanticElement) {
            final EObject diagramElement = getViewContext().getTargetElement(semanticElement, null);
            return isSelectable().apply(diagramElement) ? diagramElement : null;
        }
    };


    /**
     * {@inheritDoc}
     */
    public void toggleSelectionOf(final Object semanticElement) {
        final EObject diagramElement = getViews.apply(semanticElement);
        if (diagramElement != null) {
            toggleSelectionOfDiagramElements(singleton(diagramElement));
        }
    };

    /**
     * {@inheritDoc}
     */
    public void toggleSelectionOf(final KGraphElement diagramElement) {
        toggleSelectionOfDiagramElements(singleton(diagramElement));
    }

    /**
     * {@inheritDoc}
     */
    public void toggleSelectionOf(final KText diagramElement) {
        toggleSelectionOfDiagramElements(singleton(diagramElement));
    }

    /**
     * {@inheritDoc}
     */
    public void toggleSelectionOfSemanticElements(final Set<Object> semanticElements) {
        toggleSelectionOfDiagramElements(
                Sets.newHashSet(transform(semanticElements, getViews)));

    }

    /**
     * {@inheritDoc}
     */
    public void toggleSelectionOfDiagramElements(final Set<? extends EObject> toBeToggled) {
        final KlighdTreeSelection diagSelection = this.getDiagramSelection();
        final List<EObject> theSelection = newArrayList(
                filter(diagSelection != null ? diagSelection : KlighdTreeSelection.EMPTY, EObject.class)
        );

        for (final EObject diagramElement : Sets.filter(toBeToggled, isSelectable())) {
            final boolean removed = theSelection.remove(diagramElement);
            if (!removed) {
                theSelection.add(diagramElement);
            }
        }
        updateSelection(theSelection);

    }

    /**
     * {@inheritDoc}
     */
    public void resetSelectionTo(final Object semanticElement) {
        final EObject diagramElement = getViews.apply(semanticElement);
        // Collections.singleton accepts 'null' values and 'updateSelection' does so, too!
        updateSelection(singleton(diagramElement));
    }

    /**
     * {@inheritDoc}
     */
    public void resetSelectionTo(final KGraphElement diagramElement) {
        updateSelection(singleton(diagramElement));
    }

    /**
     * {@inheritDoc}
     */
    public void resetSelectionTo(final KText diagramElement) {
        updateSelection(singleton(diagramElement));
    }

    /**
     * {@inheritDoc}
     */
    public void resetSelectionToSemanticElements(final Iterable<? extends Object> semanticElements) {
        updateSelection(transform(semanticElements, getViews));
    }

    /**
     * {@inheritDoc}
     */
    public void resetSelectionToDiagramElements(final Iterable<? extends EObject> diagramElements) {
        updateSelection(filter(diagramElements, isSelectable()));
    }


    /**
     * Function for determining the (root) {@link KRendering KRenderings} corresponding to a
     * selected diagram element. Returns the input value wrapped in a {@link java.util.Collection
     * Collection} if that is a {@link KRendering}, and all contained {@link KRendering KRenderings}
     * if the input is a {@link KGraphElement}. The function is used for determining the elements
     * whose value of {@link KlighdInternalProperties#SELECTED} is to be updated.
     */
    private static final Function<EObject, Iterable<KRendering>> AS_RENDERING
        = new Function<EObject, Iterable<KRendering>>() {

        public Iterable<KRendering> apply(final EObject eo) {
            if (KRenderingPackage.eINSTANCE.getKRendering().isInstance(eo)) {
                return singleton((KRendering) eo);
            } else {
                return newArrayList(filter(eo.eContents(), KRendering.class));
            }
        }
    };

    /**
     * Updates the selection provided by this {@link IViewer}.
     *
     * @param diagramElements an {@link Iterable} of view model elements being selected
     */
    private void updateSelection(final Iterable<? extends EObject> diagramElements) {
        // here the selected elements are assumed to be diagram elements, i.e. KGraph elements or KTexts

        final KlighdTreeSelection diagSelection = getDiagramSelection();

        final List<EObject> currentlySelected = diagSelection != null
                ? newArrayList(filter(diagSelection, EObject.class)) : Collections.<EObject>emptyList();
        final List<EObject> toBeSelected = newArrayList(filter(diagramElements, Predicates.notNull()));

        for (final KRendering r : concat(transform(filter(currentlySelected, notIn(toBeSelected)),
                AS_RENDERING))) {
            r.setProperty(KlighdInternalProperties.SELECTED, false);
        }

        // in the following loop 'filter(toBeSelected, notIn(currentlySelected))' is skipped by intention
        //  leading to repeated property settings to 'true'
        // this has the positive effect that unselected edges will automatically go to background since
        //  the still selected ones will be brought to front again
        // that should be the only side effect, except some addition performance waste ;-)
        for (final KRendering r : concat(transform(toBeSelected, AS_RENDERING))) {
            r.setProperty(KlighdInternalProperties.SELECTED, true);
        }

        createSelection(toBeSelected);
    }

    /**
     * Updates the selection provided by <code>this</code> {@link ContextViewer} and notifies the
     * registered {@link ISelectionChangedListener ISelectionChangedListeners}.
     *
     * @param elements
     *            the elements contained in the updated selection
     */
    protected void createSelection(final Collection<EObject> elements) {
        // update the selection status for the ISelectionProvider interface
        notifySelectionListeners(new KlighdTreeSelection(getViewContext(), elements));
    }


    /* -------------------------------------------------- */
    /*   The selection provider related stuff             */
    /* -------------------------------------------------- */

    /** the current selection. */
    private KlighdTreeSelection diagramSelection = KlighdTreeSelection.EMPTY;

    /** alternative generic selection, required for providing next selections by KLighD's UI parts. */
    private IKlighdSelection selection = diagramSelection;

    /** the selection listeners registered on this view. */
    // don't change the type to a collection type violating the Set property
    //  see doc of 'ISelectionProvider.addSelectionChangedListener(...)'
    private Set<ISelectionChangedListener> selectionListeners = Sets.newLinkedHashSet();

    /**
     * Notifies the registered {@link ISelectionChangedListener ISelectionChangedListeners}. Such
     * listeners are registered e.g. by the platform in order to broadcast changes in the selection
     * across change listeners registered in the {@link org.eclipse.ui.ISelectionService
     * ISelectionService}.
     *
     * @param theSelection
     *            the selection to be broadcasted
     */
    void notifySelectionListeners(final IKlighdSelection theSelection) {
        // method is package protected as it is called in AbstractViewer, too

        synchronized (selectionListeners) {
            this.selection = theSelection;

            if (theSelection instanceof KlighdTreeSelection) {
                this.diagramSelection = (KlighdTreeSelection) theSelection;
            } else {
                resetSelectionHighlighting();
                this.diagramSelection = null;
            }

            if (!selectionListeners.isEmpty()) {
                final SelectionChangedEvent event = new SelectionChangedEvent(this, theSelection);

                for (final ISelectionChangedListener listener : selectionListeners) {
                    listener.selectionChanged(event);
                }
            }
        }
    }

    /**
     * Resets the highlighting of the currently selected diagram elements.
     */
    private void resetSelectionHighlighting() {
        final Iterable<EObject> currentSelection = filter(getDiagramSelection(), EObject.class);
        if (currentSelection != null) {
            for (final KRendering r : concat(transform(currentSelection, AS_RENDERING))) {
                r.setProperty(KlighdInternalProperties.SELECTED, false);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public KlighdTreeSelection getDiagramSelection() {
        return this.diagramSelection;
    }


    /* -------------------------------------------------- */
    /*   implementation of ISelectionProvider             */
    /* -------------------------------------------------- */

    /**
     * {@inheritDoc}
     */
    public IKlighdSelection getSelection() {
        return this.selection;
    }

    /**
     * {@inheritDoc}
     */
    public void setSelection(final ISelection selection) {
        // not supported yet
        final String msg = "KLighD: Setting the selection "
                + "in KLighD viewers via ISelectionProvider.setSelection(...) is not supported.";
        throw new UnsupportedOperationException(msg);
    }

    /**
     * {@inheritDoc}
     */
    public void addSelectionChangedListener(final ISelectionChangedListener listener) {
        synchronized (selectionListeners) {
            selectionListeners.add(listener);
            listener.selectionChanged(new SelectionChangedEvent(this, selection));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeSelectionChangedListener(final ISelectionChangedListener listener) {
        synchronized (selectionListeners) {
            selectionListeners.remove(listener);
        }
    }
}
