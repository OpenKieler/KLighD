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

import org.eclipse.core.runtime.Plugin;
import org.eclipse.elk.core.data.LayoutMetaDataService;
import org.eclipse.elk.graph.util.ElkReflect;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.framework.BundleContext;

import de.cau.cs.kieler.klighd.Klighd;
import de.cau.cs.kieler.klighd.util.ExpansionAwareLayoutOption;

/**
 * The activator class controls the plug-in life cycle.
 * 
 * @author mri
 * @author chsch
 * 
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch
 */
public class EclipseKlighdPlugin extends Plugin {

    /** the plug-in ID. */
    public static final String PLUGIN_ID = Klighd.PLUGIN_ID;

    /** A definition place of the platform-specific line separator. */
    public static final String LINE_SEPARATOR = Klighd.LINE_SEPARATOR;

    /** A Boolean flag indicating that the tool is running on a linux system. */
    public static final boolean IS_LINUX = Klighd.IS_LINUX;

    /** A Boolean flag indicating that the tool is running on a MacOSX system. */
    public static final boolean IS_MACOSX = Klighd.IS_MACOSX;

    /** A Boolean flag indicating that the tool is running on a Windows system. */
    public static final boolean IS_WINDOWS = Klighd.IS_WINDOWS;

    /** the shared instance. */
    private static EclipseKlighdPlugin plugin;

    /**
     * The constructor.
     */
    public EclipseKlighdPlugin() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        // install an alternative status manager delegating to the default Eclipse Status Manager
        Klighd.setStatusManager((status, style) -> {
            StatusManager.getManager().handle(status, style);
        });

        // make sure that the layout meta data service has been initialized, 
        //  in particular that the ElkReflect registry has been filled 
        LayoutMetaDataService.getInstance();

        ElkReflect.register(
                ExpansionAwareLayoutOption.ExpansionAwareLayoutOptionData.class,
                () -> new ExpansionAwareLayoutOption.ExpansionAwareLayoutOptionData(), 
                (v) -> new ExpansionAwareLayoutOption.ExpansionAwareLayoutOptionData((ExpansionAwareLayoutOption.ExpansionAwareLayoutOptionData) v)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop(final BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance.
     * 
     * @return the shared instance
     */
    public static EclipseKlighdPlugin getDefault() {
        return plugin;
    }
}
