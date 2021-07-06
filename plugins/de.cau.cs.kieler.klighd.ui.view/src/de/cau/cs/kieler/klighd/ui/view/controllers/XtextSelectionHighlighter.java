/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://rtsys.informatik.uni-kiel.de/kieler
 * 
 * Copyright 2015 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 */
package de.cau.cs.kieler.klighd.ui.view.controllers;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;

import com.google.common.collect.Iterators;

import de.cau.cs.kieler.klighd.eclipse.KlighdTreeSelection;

/**
 * This class contains utility methods for highlighting selection in Xtext editors.
 * 
 * @author als
 * @kieler.design 2015-01-01 proposed
 * @kieler.rating 2015-01-01 proposed yellow
 */
public final class XtextSelectionHighlighter {
    /**
     * Prevent instantiation.
     */
    private XtextSelectionHighlighter() {
    }

    /**
     * Highlights the selection in the given editor.
     * 
     * @param editor
     *            the editor
     * @param selection
     *            the selection
     */
    public static void highlightSelection(final XtextEditor editor, final ISelection selection) {
        if (selection instanceof KlighdTreeSelection) {
            highlightSelection(editor, (KlighdTreeSelection) selection);
        }
    }

    /**
     * Highlights the selection in the given editor.
     * 
     * @param editor
     *            the editor
     * @param selection
     *            the selection
     */
    public static void highlightSelection(final XtextEditor editor,
            final KlighdTreeSelection selection) {
        if (selection != null) {
            final Iterator<EObject> it = Iterators.filter(
                    ((KlighdTreeSelection) selection).sourceElementIterator(), EObject.class);
            // The element to highlight
            final EObject element = Iterators.getNext(it, null);

            if (element != null && (element.eResource() instanceof XtextResource)) {
                // determine the workbench resource path of 'element's definition document
                // the boolean param 'true' forces the URI to properly convert space in file/folder
                // names
                final IPath path = new Path(element.eResource().getURI().toPlatformString(true));
                final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);

                PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
                    public void run() {
                        if (editor.getEditorInput().equals(new FileEditorInput(file))) {
                            // query the editor library for the position of the elements
                            // definition
                            Integer[] elementData = editor.getDocument()
                                    .readOnly(new IUnitOfWork<Integer[], XtextResource>() {
                                public Integer[] exec(final XtextResource state) throws Exception {
                                    INode node = NodeModelUtils.findActualNodeFor(element);
                                    return new Integer[] { node.getOffset(), node.getLength() };
                                }
                            });

                            // set the selection to that area
                            editor.getInternalSourceViewer().setSelectedRange(elementData[0],
                                    elementData[1]);
                            editor.getInternalSourceViewer().revealRange(elementData[0],
                                    elementData[1]);

                            // Maybe bring on top (this may disturb lined views)
                            // editor.getSite().getPage().bringToTop(editor);
                        }
                    }
                });
            }
        }
    }
}
