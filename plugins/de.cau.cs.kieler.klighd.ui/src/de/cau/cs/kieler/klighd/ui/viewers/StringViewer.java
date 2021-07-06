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
package de.cau.cs.kieler.klighd.ui.viewers;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.cau.cs.kieler.klighd.IKlighdSelection;
import de.cau.cs.kieler.klighd.IViewChangeListener;
import de.cau.cs.kieler.klighd.IViewer;
import de.cau.cs.kieler.klighd.KlighdTreeSelection;
import de.cau.cs.kieler.klighd.ViewChangeType;
import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.ZoomStyle;
import de.cau.cs.kieler.klighd.kgraph.KGraphElement;
import de.cau.cs.kieler.klighd.kgraph.KNode;
import de.cau.cs.kieler.klighd.krendering.KText;
import de.cau.cs.kieler.klighd.viewers.ContextViewer;

/**
 * A viewer for string messages.
 *
 * chsch: TODO class and its interaction with the framework must be reviewed!!
 *
 * @author mri
 * @author chsch
 */
public class StringViewer implements IViewer {

    /** the canvas used to display the message. */
    private Canvas canvas = null;
    /** the currently displayed message. */
    private String message = "";

    /**
     * Constructs a string viewer.
     *
     * @param parent
     *            the parent composite
     */
    public StringViewer(final Composite parent) {
        // add a canvas for displaying the message
        canvas = new Canvas(parent, SWT.NONE);
        canvas.addPaintListener(new PaintListener() {
            public void paintControl(final PaintEvent e) {
                synchronized (message) {
                    e.gc.drawString(message, 0, 0, true);
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public Control getControl() {
        return canvas;
    }

    /**
     * {@inheritDoc}
     */
    public ViewContext getViewContext() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void setModel(final Object model) {
        this.setModel(model, false);
    }

    /**
     * {@inheritDoc}
     */
    public void setModel(final Object model, final boolean sync) {
        if (model instanceof String) {
            message = (String) model;
        }
    }


    // remaining methods of the IViewer interface
    //  their application is not reasonable for this viewer

    /**
     * {@inheritDoc}
     */
    public ContextViewer getContextViewer() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void addViewChangeListener(final IViewChangeListener listener,
            final ViewChangeType... eventTypes) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void addViewChangeListener(final IViewChangeListener listener,
            final EnumSet<ViewChangeType> eventTypes) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void removeViewChangeListener(final IViewChangeListener listener) {
        // do nothing
    }


    /**
     * {@inheritDoc}
     */
    public boolean isDisplayed(final Object semanticElement, final boolean checkParents) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDisplayed(final KGraphElement diagramElement, final boolean checkContainment) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isVisible(final Object semanticElement, final boolean checkParents) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isVisible(final KGraphElement diagramElement, final boolean checkContainment) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<KNode> getVisibleDiagramNodes() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<KGraphElement> getVisibleDiagramElements() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void reveal(final Object semanticElement, final int duration) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void reveal(final KGraphElement diagramElement, final int duration) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void centerOn(final Object semanticElement, final int duration) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void centerOn(final KGraphElement diagramElement, final int duration) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void panToTopLeftCorner(final Object semanticElement, final int duration) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void panToTopLeftCorner(final KNode diagramElement, final int duration) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void panDiagramToTopLeftCorner(final int duration) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void zoomToLevel(final float zoomLevel, final int duration) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void zoomToFocus(final Object semanticElement, final int duration) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void zoomToFocus(final KNode diagramElement, final int duration) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void zoom(final ZoomStyle style, final int duration) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public float getZoomLevel() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isExpanded(final Object semanticElement) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isExpanded(final KNode diagramElement) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void collapse(final Object semanticElement) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void collapse(final KNode diagramElement) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void expand(final Object semanticElement) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void expand(final KNode diagramElement) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void toggleExpansion(final Object semanticElement) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void toggleExpansion(final KNode diagramElement) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void hide(final Object semanticElement) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void hide(final KGraphElement diagramElement) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void show(final Object semanticElement) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void show(final KGraphElement diagramElement) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void clip(final Object semanticElement) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void clip(Object semanticElement, Boolean hideClipNodePorts,
            Boolean hideClipNodeLabels) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void clip(final KNode diagramElement) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void clip(KNode diagramElement, Boolean hideClipNodePorts, Boolean hideClipNodeLabels) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public KNode getClip() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void scale(final Object semanticElement, final double factor) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void scale(final KNode diagramElement, final double factor) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public double getScale(final Object semanticElement) {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public double getScale(final KNode diagramElement) {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public IKlighdSelection getSelection() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public KlighdTreeSelection getDiagramSelection() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void toggleSelectionOf(final Object semanticElement) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void toggleSelectionOf(final KGraphElement diagramElement) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void toggleSelectionOf(final KText diagramElement) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void toggleSelectionOfSemanticElements(final Set<Object> semanticElements) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void toggleSelectionOfDiagramElements(final Set<? extends EObject> diagramElements) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void resetSelectionTo(final Object diagramElement) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void resetSelectionTo(final KGraphElement diagramElement) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void resetSelectionTo(final KText diagramElement) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void resetSelectionToSemanticElements(final Iterable<? extends Object> semanticElements) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    public void resetSelectionToDiagramElements(final Iterable<? extends EObject> diagramElements) {
        // do nothing
    }
}

