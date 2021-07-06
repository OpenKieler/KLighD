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
package de.cau.cs.kieler.klighd.eclipse;

import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.ui.IWorkbenchPart;

import com.google.common.base.Function;

/**
 * An interface that is used by KSBasE and KLighD to retrieve selections and perform
 * transformations. The {@link KSBasECombination} uses the {@link ModifyModelHandlerProvider} to
 * find the best fitting handler for the current context (via the
 * {@link ModifyModelHandlerProvider#getFittingKSBasEHandler(IWorkbenchPart)} method).
 * 
 * All handlers have to be registered via the de.cau.cs.kieler.ksbase.ui.handlers extension point.
 * 
 * @author uru
 * @author ckru
 * 
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch
 */
public interface IModelModificationHandler {

    /**
     * Test whether this handler is able to execute methods and functions in the context of the
     * given the given IWorkbenchPart.
     * 
     * @param workbenchPart
     *            the workbench part to be tested, eg, a {@link org.eclipse.ui.IEditorPart IEditorPart}
     * @return true if this handler is able to handle the editor and selection
     */
    boolean canHandle(final IWorkbenchPart workbenchPart);

    /**
     * Gets the selected EObjects out of a selection of undefined objects.
     * 
     * @param workbenchPart
     *            the workbench part for which to retrieve the selection, eg, a
     *            {@link org.eclipse.ui.IEditorPart IEditorPart}
     * @param selection
     *            the selection within the passed workbenchPart
     * @return the currently selected eobjects
     */
    List<EObject> getSelection(final IWorkbenchPart workbenchPart, List<?> selection);

    /**
     * Execute the given method in a way thats required to modify the data source i.e.: using a
     * TransactionalEditingDomain for GMF models.
     * 
     * @param workbenchPart
     *            the IWorkbenchPart of the source for this view.
     * @param method
     *            the Java method to be executed via reflection
     * @param classObject
     *            the class the given method belongs to
     * @param params
     *            the parameters to be given to the method
     */
    void execute(final IWorkbenchPart workbenchPart, final Method method, final Object classObject,
            final List<Object> params);

    /**
     * Execute the given method in a way thats required to modify the data source i.e.: using a
     * TransactionalEditingDomain for GMF models.
     * 
     * @param workbenchPart
     *            the IWorkbenchPart of the source for this view.
     * @param function
     *            the function to be executed
     * @param param
     *            the parameter to be given to the function
     */
    void execute(final IWorkbenchPart workbenchPart, final Function<String, Void> function,
            final String param);

    /**
     * Perform any post processing _after_ the model transformation was applied. For instance, GMF
     * editors might require their update policies to be refreshed.
     */
    void performPostProcessing();

    /**
     * Determines whether the diagram should be layouted after method/function has been executed.
     * 
     * @return true if a automatic layout should be performed after the ksbase action.
     */
    boolean isLayoutRequired();

    /**
     * Gets the element on which to execute the layout thats executed after the method has been
     * executed.
     * 
     * @return the root element for which layout should be applied if the {@link #isPerformLayout()}
     *         method returns true.
     */
    EObject getLayoutRoot();

    /**
     * This method will be called after a transformation was executed.
     * 
     * @param transformationName
     *            the name of the executed transformation
     * @param parameters
     *            the parameters with which the transformation was executed
     * @param result
     *            the result of the transformation
     * @param select
     *            this element should be selected within the active editor
     */
    void transformationExecuted(String transformationName, Object[] parameters, Object result,
            EObject select);
}
