/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2010 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klighd.ui.modifymodel;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.ui.IWorkbenchPart;

import com.google.common.collect.Lists;

import de.cau.cs.kieler.klighd.eclipse.IModelModificationHandler;

/**
 * Class is used to get fitting IModifyModelHandlers that can handle the given IWorkbenchPart.
 * ExtensionPoint will be lazily read when its first called.
 * 
 * @author ckru
 */
public final class ModelModificationHandlerProvider {

    /**
     * singleton instance.
     */
    private static ModelModificationHandlerProvider instance =
            new ModelModificationHandlerProvider();

    /**
     * Cache of parsed handlers so that extension point only has to be evaluated once.
     */
    private List<IModelModificationHandler> handlers = null;

    /**
     * Private constructor, part of singleton pattern.
     */
    private ModelModificationHandlerProvider() {

    }

    /**
     * Get a handler that is able to execute methods on the data source of the given IWorkbenchPart.
     * 
     * @param part
     *            the IWorkbenchPart the handlers should be compatible with.
     * @return the _first_ matching {@link IModelModificationHandler} for the passed parameters.
     */
    public IModelModificationHandler getFittingHandler(final IWorkbenchPart part) {

        if (handlers == null) {
            readHandlerExtensionPoint();
        }

        for (IModelModificationHandler handler : handlers) {
            if (handler.canHandle(part)) {
                return handler;
            }
        }

        return null;
    }

    /**
     * Parse the extension point and generate handler instances.
     */
    private void readHandlerExtensionPoint() {
        handlers = Lists.newLinkedList();
        // read extension point
        IConfigurationElement[] elements =
                RegistryFactory.getRegistry().getConfigurationElementsFor(
                        "de.cau.cs.kieler.klighd.extensions");
        // get all ksbase handlers
        for (IConfigurationElement element : elements) {
            if (element.getName().equals("handler")) {
                try {
                    IModelModificationHandler handler =
                            (IModelModificationHandler) element.createExecutableExtension("class");
                    handlers.add(handler);
                } catch (CoreException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Gets the singleton instance of the ModelModificationHandlerProvider.
     * 
     * @return Singleton instance
     */
    public static ModelModificationHandlerProvider getInstance() {
        return instance;
    }
}
