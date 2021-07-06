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
package de.cau.cs.kieler.klighd.eclipse;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.reverse;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.elk.core.util.Pair;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import de.cau.cs.kieler.klighd.IViewer;
import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.util.ModelingUtil;
import de.cau.cs.kieler.klighd.viewers.ContextViewer;

/**
 * A specialized {@link TreeSelection} providing the selected view elements as well as
 * {@link ContextViewer} that contributed <code>this</code> selection and an {@link Iterator}
 * providing {@link Pair Pairs} of the selected view elements and their corresponding source
 * model elements.
 * 
 * @author chsch
 */
@SuppressWarnings("rawtypes")
public class KlighdTreeSelection extends TreeSelection implements IEclipseKlighdSelection, Iterable {
    
    /**
     * Empty singleton instance of {@link KlighdTreeSelection}.
     */
    public static final KlighdTreeSelection EMPTY = new KlighdTreeSelection(null);
    
    private ViewContext viewContext;
    
    /**
     * Constructor.
     * 
     * @param theViewContext
     *            the current view's {@link ViewContext}
     * @param path
     *            a single {@link TreePath}.
     */
    public KlighdTreeSelection(final ViewContext theViewContext, final TreePath... path) {
        super(path);
        this.viewContext = theViewContext;
    }

    /**
     * Constructor.
     * 
     * @param viewContext
     *            the current view's {@link ViewContext}
     * @param selectedElements
     *            the view elements being currently selected
     */
    public KlighdTreeSelection(final ViewContext viewContext,
            final Collection<? extends EObject> selectedElements) {
        this(viewContext, Iterables.toArray(
                Collections2.transform(selectedElements, new Function<EObject, TreePath>() {
                    public TreePath apply(final EObject eObject) {
                        return new TreePath(Iterables.toArray(reverse(
                                newArrayList(ModelingUtil.selfAndEAllContainers(eObject))),
                                Object.class));
                    }
                }), TreePath.class));
    }

    /**
     * {@inheritDoc}
     */
    public IViewer getViewer() {
        return viewContext.getViewer().getContextViewer();
    }

    /**
     * Getter.
     * 
     * @deprecated use {@link #getViewer()}
     * 
     * @return the {@link ContextViewer} providing this selection.
     */
    public ContextViewer getContextViewer() {
        return viewContext.getViewer().getContextViewer();
    }

    /**
     * {@inheritDoc}
     */
    public ViewContext getViewContext() {
        return this.viewContext;
    }

    @Override
    public Iterator<EObject> iterator() {
        // the aim of this method is only to apply the cast and avoid the warning on class level
        
        @SuppressWarnings("unchecked")
        final Iterator<EObject> iterator = super.iterator();
        return iterator;
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<EObject> diagramElementsIterator() {
        return this.iterator();
    }

    /**
     * Analogously to {@link #iterator()} this methods returns an {@link Iterator} providing the
     * source model elements associated with the selected view elements.
     * 
     * @return an {@link Iterator} providing the requested source model elements
     */
    public Iterator<Object> sourceElementIterator() {
        return Iterators.transform(KlighdTreeSelection.this.iterator(),
                new Function<EObject, Object>() {
                    public Object apply(final EObject object) {
                        return KlighdTreeSelection.this.viewContext.getSourceElement(object);
                    }
                });
    }

    /**
     * Analogously to {@link #iterator()} this methods returns an {@link Iterator} providing the
     * source model elements associated with the selected view elements.
     * 
     * @return an {@link Iterator} providing the requested source model elements
     */
    public Iterator<Pair<EObject, Object>> sourceViewPairIterator() {
        return Iterators.transform(KlighdTreeSelection.this.iterator(),
                new Function<EObject, Pair<EObject, Object>>() {
                    public Pair<EObject, Object> apply(final EObject eObject) {
                        return Pair.of(eObject,
                                KlighdTreeSelection.this.viewContext.getSourceElement(eObject));
                    }
                });
    }
}