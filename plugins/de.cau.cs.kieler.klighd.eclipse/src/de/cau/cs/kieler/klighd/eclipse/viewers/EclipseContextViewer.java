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
package de.cau.cs.kieler.klighd.eclipse.viewers;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static de.cau.cs.kieler.klighd.util.KlighdPredicates.isSelectable;
import static de.cau.cs.kieler.klighd.util.KlighdPredicates.notIn;
import static java.util.Collections.singleton;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

import de.cau.cs.kieler.klighd.IViewer;
import de.cau.cs.kieler.klighd.Klighd;
import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.eclipse.EclipseViewContext;
import de.cau.cs.kieler.klighd.eclipse.IEclipseViewer;
import de.cau.cs.kieler.klighd.eclipse.IEclipseKlighdSelection;
import de.cau.cs.kieler.klighd.eclipse.KlighdTreeSelection;
import de.cau.cs.kieler.klighd.eclipse.internal.IDiagramOutlinePage;
import de.cau.cs.kieler.klighd.internal.ILayoutRecorder;
import de.cau.cs.kieler.klighd.internal.util.KlighdInternalProperties;
import de.cau.cs.kieler.klighd.kgraph.KGraphElement;
import de.cau.cs.kieler.klighd.kgraph.KNode;
import de.cau.cs.kieler.klighd.krendering.KRendering;
import de.cau.cs.kieler.klighd.krendering.KRenderingPackage;
import de.cau.cs.kieler.klighd.krendering.KText;
import de.cau.cs.kieler.klighd.viewers.ContextViewer;

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
 * exhibiting controls, might be hosted and unified within a {@link EclipseContextViewer}.<br>
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
public class EclipseContextViewer extends ContextViewer implements IEclipseViewer, ISelectionProvider,
        IDiagramOutlinePage.Provider {

    /** the parent composite for diagram viewers. */
    private Composite diagramComposite;

    /**
     * Constructs a view context viewer.
     *
     * @param parent
     *            the parent composite
     */
    public EclipseContextViewer(final Composite parent) {
        this.diagramComposite = parent;
    }

    /**
     * Employs the given <code>viewer</code>.
     *
     * @param viewer
     *            the {@link IViewer} to be employed
     */
    @Override
    protected synchronized void setViewer(final IViewer viewer) {
        // remove the current viewer if someone exists
        if (getCurrentViewer() != null) {
            ((IEclipseViewer) getCurrentViewer()).getControl().dispose();
            setCurrentViewer(null);
        }

        final IViewer objViewer = viewer;
        setCurrentViewer(objViewer);

        diagramComposite.layout();

        if (getCurrentViewer() instanceof ILayoutRecorder) {
            setLayoutRecorder((ILayoutRecorder) getCurrentViewer());
        }
    }


    /* -------------------------------------------------- */
    /*   implementation of IDiagramOutlinePage.Provider   */
    /* -------------------------------------------------- */

    /**
     * {@inheritDoc}
     */
    public IDiagramOutlinePage getDiagramOutlinePage() {
        if (getCurrentViewer() instanceof IDiagramOutlinePage.Provider) {
            return ((IDiagramOutlinePage.Provider) getCurrentViewer()).getDiagramOutlinePage();
        } else {
            return null;
        }
    }


    /* -------------------------------------------------- */
    /*   implementation of ILayoutRecorder                */
    /* -------------------------------------------------- */


    /* -------------------------------------------------- */
    /*   implementation of IViewer                        */
    /* -------------------------------------------------- */

    /**
     * {@inheritDoc}
     */
    public synchronized void setModel(final Object model, final boolean sync) {
        // if the model is a view context adapt the viewer to the given context if possible
        if (model instanceof ViewContext) {
            final ViewContext viewContext = (ViewContext) model;

            // set the new view context
            setCurrentViewContext(viewContext);

            // create and set a corresponding new viewer
            setViewer(((EclipseViewContext) viewContext).createViewer(this, diagramComposite));

            // initialize the current selection
            notifySelectionListeners(new KlighdTreeSelection(getCurrentViewContext()));
        }
    }

    /**
     * {@inheritDoc}
     */
    public Control getControl() {
        return getCurrentViewer() != null ? ((IEclipseViewer) getCurrentViewer()).getControl() : null;
    }

    /* ----------------------------- */
    /*   the view manipulation API   */
    /* ----------------------------- */

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
     * Updates the selection provided by <code>this</code> {@link EclipseContextViewer} and notifies the
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
    private IEclipseKlighdSelection selection = diagramSelection;

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
    void notifySelectionListeners(final IEclipseKlighdSelection theSelection) {
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
    public IEclipseKlighdSelection getSelection() {
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
