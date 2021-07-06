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
package de.cau.cs.kieler.klighd;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.widgets.Control;

import de.cau.cs.kieler.klighd.kgraph.KGraphElement;
import de.cau.cs.kieler.klighd.kgraph.KNode;
import de.cau.cs.kieler.klighd.krendering.KText;
import de.cau.cs.kieler.klighd.viewers.ContextViewer;

/**
 * The common interface for viewers displaying diagrams of provided models.<br>
 * <br>
 * This interface is supposed to be used in client's code for revealing elements in diagrams,
 * expanding & collapsing elements, clipping the diagram, or asking for the source elements
 * of representatives in diagrams by referring to {@link #getViewContext()}.
 *
 * @author mri
 * @author chsch
 *
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch
 */
public interface IViewer {

    /**
     * Returns the control used by this viewer.
     *
     * @return the control
     */
    Control getControl();

    /**
     * Returns the {@link ContextViewer} containing this viewer.
     *
     * @return the {@link ContextViewer}
     */
    ContextViewer getContextViewer();

    /**
     * Sets the input model for this viewer.
     *
     * @param model
     *            the input model
     */
    void setModel(Object model);

    /**
     * Sets the input model for this viewer.<br>
     * <br>
     * If synchronization is enabled his model is incrementally updated by KLighD. The
     * implementation of the viewer should utilize a listener mechanic on the model to keep the
     * visualization in sync.
     *
     * @param model
     *            the input model
     * @param sync
     *            true if the viewer should synchronize the visualization with the model; false else
     */
    void setModel(Object model, boolean sync);

    /**
     * Returns the {@link ViewContext} that is associated to <code>this</this> viewer.
     *
     * @return the associated {@link ViewContext} or <code>null</code> if no input model is set
     */
    ViewContext getViewContext();

    /**
     * Registers the given {@link IViewChangeListener} to be notified of events of the given
     * {@link ViewChangeType ViewChangeEventTypes}. The provided <code>listener</code> will be
     * notified after all possible changes if no {@link ViewChangeType} is specified.<br>
     * Subsequent (re-)registrations of the same listener for particular eventTypes won't have
     * any effect, so there's no need to conservatively remove a listener before (re-)adding it.
     *
     * @param listener
     *            the {@link IViewChangeListener} to be registered
     * @param eventTypes
     *            the {@link ViewChangeType} this listener is notified of, may be <code>null</code>
     *            or empty, all changes are notified in that case; throws an
     *            {@link IllegalArgumentException} if <code>eventTypes</code> contains
     *            <code>null</code> entries
     * @throws IllegalArgumentException if <code>eventTypes</code> contains
     *            <code>null</code> entries
     */
    void addViewChangeListener(IViewChangeListener listener, ViewChangeType... eventTypes);

    /**
     * Registers the given {@link IViewChangeListener} to be notified of events of the given
     * {@link ViewChangeType ViewChangeEventTypes}. The provided <code>listener</code> will be
     * notified after all possible changes if no {@link ViewChangeType} is specified.<br>
     * Subsequent (re-)registrations of the same listener for particular eventTypes won't have
     * any effect, so there's no need to conservatively remove a listener before (re-)adding it.
     *
     * @param listener
     *            the {@link IViewChangeListener} to be registered
     * @param eventTypes
     *            the {@link ViewChangeType} this listener is notified of, may be <code>null</code>
     *            or empty, all changes are notified in that case
     */
    void addViewChangeListener(IViewChangeListener listener, EnumSet<ViewChangeType> eventTypes);

    /**
     * Unregisters the given {@link IViewChangeListener}.
     *
     * @param listener
     *            the {@link IViewChangeListener} to be removed from the listeners queue
     */
    void removeViewChangeListener(IViewChangeListener listener);

    /**
     * @deprecated (inconsistent name) use
     *             {@link #addViewChangeListener(IViewChangeListener, ViewChangeType...)}
     *
     * @param listener
     *            the {@link IViewChangeListener} to be registered
     * @param eventTypes
     *            the {@link ViewChangeType} this listener is notified of
     */
    default void addViewChangedListener(IViewChangeListener listener,
            ViewChangeType... eventTypes) {
        this.addViewChangeListener(listener, eventTypes);
    }

    /**
     * @deprecated (inconsistent name) use {@link #removeViewChangeListener(IViewChangeListener)}
     *
     * @param listener
     *            the {@link IViewChangeListener} to be removed from the listeners queue
     */
    default void removeViewChangedEventListener(IViewChangeListener listener) {
        this.removeViewChangeListener(listener);
    }


    /* ----------------------------- */
    /*   the view modification API   */
    /* ----------------------------- */

    /**
     * Provides the displaying state of the given element's representing diagram element. A diagram
     * element is said to be displayed if it is part of the currently depicted diagram
     * <b>regardless</b> of the currently visible diagram excerpt (viewport).<br>
     * <br>
     * Note that a recursive displaying check along the containment hierarchy is done only if
     * <code>checkContainment</code> is <code>true</code>. Otherwise that is omitted for performance
     * reasons. Thus, given the nested diagram nodes A contains B contains C with A collapsed this
     * method may return <code>true</code> for C if <code>checkContainment</code> is
     * <code>false</code>.
     *
     * @param semanticElement
     *            the domain element being visualized by a {@link KGraphElement}
     * @param checkParents
     *            whether the parent (containment) hierarchy is to be checked, too
     * @return <code>true</code> if the {@link KGraphElement} related to
     *         <code>semanticElement</code> is visible, <code>false</code> otherwise.
     */
    boolean isDisplayed(Object semanticElement, boolean checkParents);

    /**
     * Provides the displaying state of the given diagram element. A diagram element is said to be
     * displayed if it is part of the currently depicted diagram <b>regardless</b> of the currently
     * visible diagram excerpt (viewport).<br>
     * <br>
     * Note that a recursive displaying check along the containment hierarchy is done only if
     * <code>checkContainment</code> is <code>true</code>. Otherwise that is omitted for performance
     * reasons. Thus, given the nested diagram nodes A contains B contains C with A collapsed this
     * method may return <code>true</code> for C if <code>checkContainment</code> is
     * <code>false</code>.
     *
     * @param diagramElement
     *            a {@link KGraphElement}
     * @param checkParents
     *            whether the parent (containment) hierarchy is to be checked, too
     * @return <code>true</code> if the {@link KGraphElement} <code>diagramElement</code> is
     *         visible, <code>false</code> otherwise.
     */
    boolean isDisplayed(KGraphElement diagramElement, boolean checkParents);

    /**
     * Provides the visibility state of the given element's representing diagram element. A diagram
     * element is said to be visible if it is drawn in the currently depicted diagram excerpt
     * (viewport).<br>
     * <br>
     * Note that a recursive visibility check along the containment hierarchy is done only if
     * <code>checkContainment</code> is <code>true</code>. Otherwise that is omitted for performance
     * reasons. Thus, given the nested diagram nodes A contains B contains C with A collapsed this
     * method may return <code>true</code> for C if <code>checkContainment</code> is
     * <code>false</code>.
     *
     * @param semanticElement
     *            the domain element being visualized by a {@link KGraphElement}
     * @param checkParents
     *            whether the parent (containment) hierarchy is to be checked, too
     * @return <code>true</code> if the {@link KGraphElement} related to
     *         <code>semanticElement</code> is visible, <code>false</code> otherwise.
     */
    boolean isVisible(Object semanticElement, boolean checkParents);

    /**
     * Provides the visibility state of the given diagram element. A diagram element is said to be
     * visible if it is drawn in the currently depicted diagram excerpt (viewport).<br>
     * <br>
     * Note that a recursive visibility check along the containment hierarchy is done only if
     * <code>checkContainment</code> is <code>true</code>. Otherwise that is omitted for performance
     * reasons. Thus, given the nested diagram nodes A contains B contains C with A collapsed this
     * method may return <code>true</code> for C if <code>checkContainment</code> is
     * <code>false</code>.
     *
     * @param diagramElement
     *            a {@link KGraphElement}
     * @param checkParents
     *            whether the parent (containment) hierarchy is to be checked, too
     * @return <code>true</code> if the {@link KGraphElement} <code>diagramElement</code> is
     *         visible, <code>false</code> otherwise.
     */
    boolean isVisible(KGraphElement diagramElement, boolean checkParents);

    /**
     * Creates an {@link org.eclipse.emf.common.util.TreeIterator TreeIterator} providing the
     * {@link KNode KNodes} visible at the moment of iterating (lazy evaluation)!<br>
     * <br>
     * <b>Caution:</b> Traversal must be performed by the display (UI) thread for integrity
     * reasons.
     *
     * @return the desired {@link org.eclipse.emf.common.util.TreeIterator TreeIterator}
     */
    Iterator<KNode> getVisibleDiagramNodes();

    /**
     * Creates an {@link org.eclipse.emf.common.util.TreeIterator TreeIterator} providing the
     * {@link KGraphElement KGraphElements} visible at the moment of iterating (lazy
     * evaluation)!<br>
     * <br>
     * <b>Caution:</b> Traversal must be performed by the display (UI) thread for integrity
     * reasons. {@link de.cau.cs.kieler.klighd.kgraph.KEdge KEdges} are likely to be returned
     * twice, as both outgoing as well as incoming edges of a {@link KNode} must be considered.
     *
     * @return the desired {@link org.eclipse.emf.common.util.TreeIterator TreeIterator}
     */
    Iterator<KGraphElement> getVisibleDiagramElements();

    /**
     * Reveals the representation of the given semantic element over the specified duration.
     *
     * @param semanticElement
     *            the semantic element to center on
     * @param duration
     *            the duration over which the panning animation is done
     */
    void reveal(Object semanticElement, int duration);

    /**
     * Reveals the given diagram element over the specified duration.
     *
     * @param diagramElement
     *            the diagram element
     * @param duration
     *            the duration
     */
    void reveal(KGraphElement diagramElement, int duration);

    /**
     * Centers on the representation of the given semantic element over the specified duration.
     *
     * @param semanticElement
     *            the semantic element to center on
     * @param duration
     *            the duration over which the panning animation is done
     */
    void centerOn(Object semanticElement, int duration);

    /**
     * Centers on the given diagram element over the specified duration.
     *
     * @param diagramElement
     *            the diagram element
     * @param duration
     *            the duration over which the panning animation is done
     */
    void centerOn(KGraphElement diagramElement, int duration);

    /**
     * Pans the diagram s.t. the representation of the given semantic element is depicted in the top
     * left corner of the visible area (over the specified duration).
     *
     * @param semanticElement
     *            the semantic element to refer to
     * @param duration
     *            the duration over which the panning animation is done
     */
    void panToTopLeftCorner(Object semanticElement, int duration);

    /**
     * Pans the diagram s.t. the given diagram element is depicted in the top left corner of the
     * visible area (over the specified duration).
     *
     * @param diagramElement
     *            the diagram element
     * @param duration
     *            the duration over which the panning animation is done
     */
    void panToTopLeftCorner(KNode diagramElement, int duration);

    /**
     * Pans the diagram s.t. the depicted content is aligned to the to left corner of diagram widget
     * (over the specified duration).
     *
     * @param duration
     *            the duration over which the panning animation is done
     */
    void panDiagramToTopLeftCorner(int duration);

    /**
     * Centers on the representation of the given semantic element over the specified duration.
     *
     * @param semanticElement
     *            the semantic element to center on
     * @param duration
     *            the duration over which the panning animation is done
     */
    void zoomToFocus(Object semanticElement, int duration);

    /**
     * Centers on the given diagram element over the specified duration.
     *
     * @param diagramElement
     *            the diagram element
     * @param duration
     *            the duration over which the panning animation is done
     */
    void zoomToFocus(KNode diagramElement, int duration);

    /**
     * Zooms to the given zoom level over the specified duration.
     *
     * @param zoomLevel
     *            the zoom level
     * @param duration
     *            the duration
     */
    void zoomToLevel(float zoomLevel, int duration);

    /**
     * Performs the specified zoom style over the specified duration.
     *
     * @param style
     *            the desired zoom style
     * @param duration
     *            the duration
     */
    void zoom(ZoomStyle style, int duration);

    /**
     * Provides the current diagram zoom level.
     *
     * @return the current diagram zoom level normalized to <code>1.0f</code>, or <code>0</code> if
     *         viewer doesn't support zooming.
     */
    float getZoomLevel();

    /**
     * Provides the expansion state of the given element's representation.
     *
     * @param semanticElement
     *            being visualized by a (hierarchic) {@link KNode}
     * @return <code>true</code> if the {@link KNode} related to <code>semanticElement</code> is
     *         expanded, <code>false</code> otherwise.
     */
    boolean isExpanded(Object semanticElement);

    /**
     * Provides the expansion state of the given diagram node.
     *
     * @param diagramElement
     *            a (hierarchic) {@link KNode}
     * @return <code>true</code> if the {@link KNode} <code>diagramElement</code> is expanded,
     *         <code>false</code> otherwise.
     */
    boolean isExpanded(KNode diagramElement);

    /**
     * Collapses the representation of the given element. Note that there must exist related element
     * <-> diagram element tracking information in the {@link TransformationContexts}.
     *
     * @param semanticElement
     *            the semantic element to be expanded
     */
    void collapse(Object semanticElement);

    /**
     * Expands the given representation element.
     *
     * @param diagramElement the diagram element to be expanded
     */
    void collapse(KNode diagramElement);

    /**
     * Expands the representation of the given element, i.e. populates it with children and changes
     * its rendering if necessary. Note that there must exist related element <-> diagram element
     * tracking information in the {@link TransformationContexts}.
     *
     * @param semanticElement
     *            the semantic element to be expanded
     */
    void expand(Object semanticElement);

    /**
     * Expands the given representation element.
     *
     * @param diagramElement the diagram element to be expanded
     */
    void expand(KNode diagramElement);

    /**
     * Expands the representation of the given element if collapsed and vice versa. Note that there
     * must exist related element <-> diagram element tracking information in the
     * {@link TransformationContexts}.
     *
     * @param semanticElement
     *            the semantic element to be expanded
     */
    void toggleExpansion(Object semanticElement);

    /**
     * Toggles the expansion state of the given representation element. If it is expanded, it gets
     * collapsed, and vice versa.
     *
     * @param diagramElement
     *            the diagram element to be expanded
     */
    void toggleExpansion(KNode diagramElement);

    /**
     * Hides the representation of the given {@link Object} from the diagram. In combination with
     * {@link #show(Object)} this method can be used for changing the diagram's amount of detail
     * without changing the view model.
     *
     * @param semanticElement
     *            the semantic element to be hidden
     */
    void hide(Object semanticElement);

    /**
     * Hides the given {@link KGraphElement} from the diagram. In combination with
     * {@link #show(KGraphElement)} this method can be used for changing the diagram's amount of
     * detail without changing the view model.
     *
     * @param diagramElement
     *            the diagram element to be hidden
     */
    void hide(KGraphElement diagramElement);

    /**
     * Shows the representation of the given {@link Object} in the diagram. In combination with
     * {@link #hide(Object)} this method can be used for changing the diagram's amount of detail
     * without changing the view model.
     *
     * @param semanticElement
     *            the semantic element to show up
     */
    void show(Object semanticElement);

    /**
     * Shows the given {@link KGraphElement} in the diagram. In combination with
     * {@link #hide(KGraphElement)} this method can be used for changing the diagram's amount of
     * detail without changing the view model.
     *
     * @param diagramElement
     *            the diagram element to show up
     */
    void show(KGraphElement diagramElement);

    /**
     * Limits the visible elements of the diagram to the content of the representation of the given
     * {@link Object} without causing any change on the view model. Hence, this method can be used
     * for changing the diagram's amount of detail without changing the view model.<br>
     * The clip can be reset to the whole diagram by calling <code>clip((Object) null)</code>.
     *
     * @param semanticElement
     *            the semantic element to whose representation the diagram view is to be limited,
     *            may be <code>null</code>
     */
    void clip(Object semanticElement);

    /**
     * Limits the visible elements of the diagram to the content of the representation of the given
     * {@link Object} without causing any change on the view model. Hence, this method can be used
     * for changing the diagram's amount of detail without changing the view model.<br>
     * The clip can be reset to the whole diagram by calling <code>clip((Object) null)</code>.
     *
     * @param semanticElement
     *            the semantic element to whose representation the diagram view is to be limited,
     *            may be <code>null</code>
     * @param hideClipNodePorts
     *            if {@link Boolean#TRUE} the new clip node's ports will be hidden and likewise
     *            their connected edges, if {@link Boolean#FALSE} the new clip node's ports will be
     *            displayed depending on their particular settings; if <code>null</code> and new
     *            clip node is equal to the current clip node the current port visibility setting is
     *            kept, otherwise the default setting defined for the clip node or the view context
     *            is applied
     * @param hideClipNodeLabels
     *            if {@link Boolean#TRUE} the new clip node's labels will be hidden, if
     *            {@link Boolean#FALSE} the new clip node's labels will be displayed depending on
     *            their particular settings; if <code>null</code> and new clip node is equal to the
     *            current clip node the current label visibility setting is kept, otherwise the
     *            default setting defined for the clip node or the view context is applied
     */
    void clip(Object semanticElement, Boolean hideClipNodePorts, Boolean hideClipNodeLabels);

    /**
     * Limits the visible elements of the diagram to the content of the given {@link KNode} without
     * causing any change on the view model. Hence, this method can be used for changing the
     * diagram's amount of detail without changing the view model.<br>
     * The clip can be reset to the whole diagram by calling <code>clip((KNode) null)</code>.
     *
     * @param diagramElement
     *            the diagram element to which the diagram view is to be limited, may be
     *            <code>null</code>
     */
    void clip(KNode diagramElement);

    /**
     * Limits the visible elements of the diagram to the content of the given {@link KNode} without
     * causing any change on the view model. Hence, this method can be used for changing the
     * diagram's amount of detail without changing the view model.<br>
     * The clip can be reset to the whole diagram by calling <code>clip((KNode) null)</code>.
     *
     * @param diagramElement
     *            the diagram element to which the diagram view is to be limited, may be
     *            <code>null</code>
     * @param hideClipNodePorts
     *            if {@link Boolean#TRUE} the new clip node's ports will be hidden and likewise
     *            their connected edges, if {@link Boolean#FALSE} the new clip node's ports will be
     *            displayed depending on their particular settings; if <code>null</code> and new
     *            clip node is equal to the current clip node the current port visibility setting is
     *            kept, otherwise the default setting defined for the clip node or the view context
     *            is applied
     * @param hideClipNodeLabels
     *            if {@link Boolean#TRUE} the new clip node's labels will be hidden, if
     *            {@link Boolean#FALSE} the new clip node's labels will be displayed depending on
     *            their particular settings; if <code>null</code> and new clip node is equal to the
     *            current clip node the current label visibility setting is kept, otherwise the
     *            default setting defined for the clip node or the view context is applied
     */
    void clip(KNode diagramElement, Boolean hideClipNodePorts, Boolean hideClipNodeLabels);

    // a getClipSemantic() is still absent, it's not clear whether that is really necessary

    /**
     * Provides the currently set diagram clip.
     *
     * @return the {@link KNode} that is currently clipped.
     */
    KNode getClip();

    /**
     * Scales the representation of the given {@link Object} in the diagram, i.e. increases or
     * decreases the size of the corresponding diagram node wrt. to its siblings without
     * re-arranging the children of that node.
     *
     * @param semanticElement
     *            the semantic element whose representing node is to be scaled
     * @param factor
     *            the absolute factor by which the representing node is to be scaled
     */
    void scale(Object semanticElement, double factor);

    /**
     * Scales the given {@link KGraphElement} in the diagram, i.e. increases or decreases its size
     * wrt. to its siblings without re-arranging the children of that node.
     *
     * @param diagramElement
     *            the diagram node to be scaled
     * @param factor
     *            the absolute factor by which the representing node is to be scaled
     */
    void scale(KNode diagramElement, double factor);

    /**
     * Provides the scale factor being currently applied to the diagram node representing the given
     * {@link Object}.
     *
     * @param semanticElement
     *            the semantic element being represented by a diagram node
     * @return the scale factor of the corresponding diagram node
     */
    double getScale(Object semanticElement);

    /**
     * Provides the scale factor being currently applied to the given diagram node.
     *
     * @param diagramElement
     *            the diagram node whose scale factory is requested
     * @return the scale factor of the given diagram node
     */
    double getScale(KNode diagramElement);


    /* ----------------------------- */
    /*   the selection setting API   */
    /* ----------------------------- */

    /**
     * Provides the current {@link IKlighdSelection} maintained by the diagram viewer.<br>
     * In earlier versions the return type was {@link org.eclipse.jface.viewers.ISelection}, which is
     * the super interface of {@link IKlighdSelection}.
     *
     * @return the current {@link IKlighdSelection}
     */
    IKlighdSelection getSelection();

    /**
     * Provides the current {@link KlighdTreeSelection} provided by the diagram viewer.
     *
     * @return the current {@link KlighdTreeSelection} or <code>null</code> if the current selection
     *         is not of type {@link KlighdTreeSelection}
     */
    KlighdTreeSelection getDiagramSelection();

    /**
     * Adds or removes the representative of the provided element to/from the current selection
     * depending on its presence in the current selection.
     *
     * @param semanticElement
     *            the element to add or remove
     */
    void toggleSelectionOf(Object semanticElement);

    /**
     * Adds or removes a diagram element to/from the current selection depending on its presence in
     * the current selection.
     *
     * @param diagramElement
     *            the element to add or remove
     */
    void toggleSelectionOf(KGraphElement diagramElement);

    /**
     * Adds or removes a diagram element to/from the current selection depending on its presence in
     * the current selection.
     *
     * @param diagramElement
     *            the element to add or remove
     */
    void toggleSelectionOf(KText diagramElement);

    /**
     * Adds or removes the representatives of the provided set of semantic elements to/from the
     * current selection depending on their particular presences in the current selection.
     *
     * @param semanticElements
     *            the elements to add or remove
     */
    void toggleSelectionOfSemanticElements(Set<Object> semanticElements);

    /**
     * Adds or removes a set of diagram element to/from the current selection depending on their
     * particular presences in the current selection.
     *
     * @param diagramElements
     *            the elements to add or remove
     */
    void toggleSelectionOfDiagramElements(Set<? extends EObject> diagramElements);

    /**
     * Dismisses the current selection and creates a new one containing the only the representative
     * of the provided element.
     *
     * @param diagramElement
     *            the element to be put into the new selection
     */
    void resetSelectionTo(Object diagramElement);

    /**
     * Dismisses the current selection and creates a new one containing the provided diagram
     * element.
     *
     * @param diagramElement
     *            the element to be put into the new selection
     */
    void resetSelectionTo(KGraphElement diagramElement);

    /**
     * Dismisses the current selection and creates a new one containing the provided diagram
     * element.
     *
     * @param diagramElement
     *            the element to be put into the new selection
     */
    void resetSelectionTo(KText diagramElement);

    /**
     * Dismisses the current selection and creates a new one containing the representatives of the
     * provided elements.
     *
     * @param semanticElements
     *            the elements to be put into the new selection
     */
    void resetSelectionToSemanticElements(Iterable<? extends Object> semanticElements);

    /**
     * Dismisses the current selection and creates a new one containing the provided diagram
     * elements.
     *
     * @param diagramElements
     *            the elements to be put into the new selection
     */
    void resetSelectionToDiagramElements(Iterable<? extends EObject> diagramElements);
}
