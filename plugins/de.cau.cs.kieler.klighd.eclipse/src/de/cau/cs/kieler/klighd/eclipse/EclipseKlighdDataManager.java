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
package de.cau.cs.kieler.klighd.eclipse;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import de.cau.cs.kieler.klighd.KlighdDataManager;

/**
 * Singleton for accessing transformations, viewers, update strategies and layout post processors
 * registered with KLighD.
 *
 * @author mri, chsch, akoc, csp
 */
public final class EclipseKlighdDataManager extends KlighdDataManager {


    /**
     * A private constructor to prevent instantiation.
     */
    private EclipseKlighdDataManager() {
        super();
    }
    
    /**
     * @param id
     *            the id of the registered {@link IDiagramExporter}.
     * @return the registered exporter for the passed id.
     *
     * @throws IllegalArgumentException
     *             if the passed {@code id} is not registered.
     */
    public IDiagramExporter getExporter(final String id) {
        IDiagramExporter exporter = null;
        IConfigurationElement element = null;
        try {
            element = exportersMap.get(id);
            if (element == null) {
                throw new IllegalArgumentException("Id of " + IDiagramExporter.class + " not registered: "
                        + id + ".");
            }
            exporter = (IDiagramExporter) element.createExecutableExtension("class");
        } catch (final CoreException exception) {
            reportError(EXTP_ID_EXTENSIONS, element, ATTRIBUTE_ID, null, exception);
        }
        return exporter;
    }
}
