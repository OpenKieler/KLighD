/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2013 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klighd.viewers;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.reverse;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import de.cau.cs.kieler.core.util.Pair;
import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.util.ModelingUtil;

/**
 * A specialized {@link TreeSelection} providing the selected view elements as well as
 * {@link ContextViewer} that contributed <code>this</code> selection and an {@link Iterator}
 * providing {@link Pair Pairs} of the selected view elements and their corresponding source
 * model elements.
 * 
 * @author chsch
 */
public class KlighdTreeSelection extends TreeSelection implements Iterable<Object> {
    
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
     * Getter.
     * 
     * @return the {@link ContextViewer} providing this selection.
     */
    public ContextViewer getContextViewer() {
        return viewContext.getViewer().getContextViewer();
    }

    /**
     * Getter.
     * 
     * @return the {@link ViewContext} of the diagram the selection has been performed in.
     */
    public ViewContext getViewContext() {
        return this.viewContext;
    }

    @Override
    public Iterator<Object> iterator() {
        // the aim of this method is only to apply the cast and avoid the warning on class level
        
        @SuppressWarnings("unchecked")
        final Iterator<Object> iterator = (Iterator<Object>) super.iterator();
        return iterator;
    }
    
    Iterator<? extends EObject> eIterator() {
        @SuppressWarnings("unchecked")
        Iterator<? extends EObject> iterator = (Iterator<? extends EObject>) this.iterator();
        return iterator;
    }
    
    /**
     * Analogously to {@link #iterator()} this methods returns an {@link Iterator} providing the
     * source model elements associated with the selected view elements.
     * 
     * @return an {@link Iterator} providing the requested source model elements
     */
    public Iterator<Object> sourceElementIterator() {
        return Iterators.transform(KlighdTreeSelection.this.iterator(),
                new Function<Object, Object>() {
                    public Object apply(final Object object) {
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
                new Function<Object, Pair<EObject, Object>>() {
                    public Pair<EObject, Object> apply(final Object object) {
                        return Pair.of((EObject) object,
                                KlighdTreeSelection.this.viewContext.getSourceElement(object));
                    }
                });
    }
}