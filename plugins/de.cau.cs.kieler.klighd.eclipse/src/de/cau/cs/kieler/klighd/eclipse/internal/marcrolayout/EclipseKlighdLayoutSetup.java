/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://rtsys.informatik.uni-kiel.de/kieler
 * 
 * Copyright ${year} by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 */
package de.cau.cs.kieler.klighd.eclipse.internal.marcrolayout;

import org.eclipse.elk.core.IGraphLayoutEngine;
import org.eclipse.elk.core.RecursiveGraphLayoutEngine;
import org.eclipse.elk.core.service.DiagramLayoutEngine;
import org.eclipse.elk.core.service.IDiagramLayoutConnector;
import org.eclipse.elk.core.service.ILayoutConfigurationStore;
import org.eclipse.elk.core.service.ILayoutSetup;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import de.cau.cs.kieler.klighd.eclipse.EclipseViewContext;
import de.cau.cs.kieler.klighd.eclipse.IDiagramWorkbenchPart;
import de.cau.cs.kieler.klighd.eclipse.IEclipseViewer;
import de.cau.cs.kieler.klighd.internal.macrolayout.KlighdDiagramLayoutConnector;
import de.cau.cs.kieler.klighd.internal.macrolayout.KlighdLayoutConfigurationStore;
import de.cau.cs.kieler.klighd.kgraph.KGraphElement;

/**
 * Defines how KLighD connects to the Eclipse Layout Kernel.
 * 
 * @author cds
 */
public final class EclipseKlighdLayoutSetup implements ILayoutSetup {

    /**
     * Some shortcut for requesting the {@link DiagramLayoutEngine} being called for computing the
     * diagram layout.
     * 
     * Enables the layout computation without relying on ELK extensions contributed via
     *  Eclipse extension points, which are unavailable in the no eclipse platform scenario.
     * 
     * @return the requested {@link DiagramLayoutEngine} instance
     */
    public DiagramLayoutEngine getDiagramLayoutEngine() {
        return createInjector(null).getInstance(DiagramLayoutEngine.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(final Object object) {
        // KGraph instances are supported
        //   Tests here for KGraphElement rather than KNode since this method is invoked while
        //   populating the layout view, which provides also port, edge, and label properties.
        if (object instanceof KGraphElement) {
            return true;
        } else if (object instanceof EclipseViewContext) {
            return true;
        } else if (object instanceof IEclipseViewer) {
            return true;
        } else if (object instanceof IDiagramWorkbenchPart) {
            return true;
        }
        
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Injector createInjector(final Module defaultModule) {
        return Guice.createInjector(new KlighdLayoutModule());
    }
    
    /**
     * Guice module for the generic GMF connector.
     */
    public static class KlighdLayoutModule implements Module {

        @Override
        public void configure(final Binder binder) {
            // TODO extend "org.eclipse.elk.core.service.internal.DefaultModule()"
            //  and replace the first line by "super.configure(binder);"
            binder.bind(IGraphLayoutEngine.class).to(RecursiveGraphLayoutEngine.class);
            binder.bind(IDiagramLayoutConnector.class).to(EclipseKlighdDiagramLayoutConnector.class);
            binder.bind(ILayoutConfigurationStore.Provider.class)
                    .to(EclipseKlighdLayoutConfigurationStore.Provider.class);
        }
    }

}
