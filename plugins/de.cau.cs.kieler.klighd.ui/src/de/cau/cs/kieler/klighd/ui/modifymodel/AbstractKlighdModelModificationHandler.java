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
package de.cau.cs.kieler.klighd.ui.modifymodel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.google.common.base.Function;

import de.cau.cs.kieler.klighd.IViewer;
import de.cau.cs.kieler.klighd.eclipse.IModelModificationHandler;
import de.cau.cs.kieler.klighd.ui.parts.DiagramEditorPart;
import de.cau.cs.kieler.klighd.ui.parts.DiagramViewPart;
import de.cau.cs.kieler.klighd.viewers.ContextViewer;

/**
 * Abstract implementation of the IModifyModelHandler for convenience purposes.
 * 
 * @author ckru
 */
public abstract class AbstractKlighdModelModificationHandler implements IModelModificationHandler {

    /**
     * Cache to get workbench part from different thread.
     */
    private IWorkbenchPart partCache = null;

    /**
     * {@inheritDoc}
     */
    public List<EObject> getSelection(final IWorkbenchPart workbenchPart, final List<?> selection) {
        ContextViewer activeContextViewer = this.getDiagramViewPart(workbenchPart);
        // Object defaultVar = selection.getSelectionElements();
        if (activeContextViewer != null) {
            List<EObject> eObjects = new ArrayList<EObject>(selection.size());
            try {
                for (Object o : selection) {
                    eObjects.add((EObject) activeContextViewer.getViewContext().getSourceElement(
                            ((EObject) o)));
                }
                return eObjects;
            } catch (ClassCastException e) {
                // ignore exception
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void execute(final IWorkbenchPart workbenchPart, final Method m,
            final Object classObject, final List<Object> params) {

        try {
            m.invoke(classObject, params);
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * {@inheritDoc}
     */
    public void execute(final IWorkbenchPart workbenchPart, final Function<String, Void> m,
            final String param) {
        m.apply(param);
    }

    /**
     * {@inheritDoc}
     */
    public void performPostProcessing() {

    }

    /**
     * {@inheritDoc}
     */
    public boolean isLayoutRequired() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public EObject getLayoutRoot() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void transformationExecuted(final String transformationName, final Object[] parameters,
            final Object result, final EObject select) {

    }

    /**
     * Gets a ContextViewer from currently active WorkbenchPart if possible. The ContextViewer is
     * later used to get the current selection.
     * 
     * @param activePart
     *            the workbench part the model to modify belongs to.
     * @return the ContextViewer linked to the active WorkbenchPart. Null if active part has no
     *         ContextViewer
     */
    public ContextViewer getDiagramViewPart(final IWorkbenchPart activePart) {
        IViewer viewer = null;
        // Object activePart = context.getVariable("org.eclipse.ui.active_activePart");
        if (activePart instanceof DiagramViewPart) {
            viewer = ((DiagramViewPart) activePart).getViewer();
            if (viewer instanceof ContextViewer) {
                return (ContextViewer) viewer;
            }
        }
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                IWorkbench wb = PlatformUI.getWorkbench();
                IWorkbenchWindow wbw = wb.getActiveWorkbenchWindow();
                if (wbw != null) {
                    IWorkbenchPage ap = wbw.getActivePage();
                    partCache = ap.getActivePart();
                }

            }
        });

        if (partCache != null && partCache instanceof DiagramViewPart) {
            viewer = ((DiagramViewPart) partCache).getViewer();
            if (viewer instanceof ContextViewer) {
                return (ContextViewer) viewer;
            }
        }

        if (partCache != null && partCache instanceof DiagramEditorPart) {
            viewer = ((DiagramEditorPart) partCache).getViewer();
            if (viewer instanceof ContextViewer) {
                return (ContextViewer) viewer;
            }
        }
        return null;
    }

}
