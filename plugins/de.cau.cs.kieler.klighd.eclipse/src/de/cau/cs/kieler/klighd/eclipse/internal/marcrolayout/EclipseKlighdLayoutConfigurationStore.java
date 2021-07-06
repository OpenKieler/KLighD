/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 *
 * Copyright 2012 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 *
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klighd.eclipse.internal.marcrolayout;

import org.eclipse.elk.core.data.LayoutOptionData;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.service.ILayoutConfigurationStore;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;

import de.cau.cs.kieler.klighd.IViewer;
import de.cau.cs.kieler.klighd.eclipse.IDiagramWorkbenchPart;
import de.cau.cs.kieler.klighd.eclipse.IEclipseViewer;
import de.cau.cs.kieler.klighd.internal.macrolayout.KlighdLayoutConfigurationStore;
import de.cau.cs.kieler.klighd.internal.util.KlighdInternalProperties;
import de.cau.cs.kieler.klighd.kgraph.KGraphElement;
import de.cau.cs.kieler.klighd.util.AbstractRunnableWithResult;
import de.cau.cs.kieler.klighd.util.RunnableWithResult;

/**
 * A layout configuration which derives layout options from properties attached to layout data of
 * graph elements.
 *
 * @author mri
 * @author msp
 *
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch
 */
public class EclipseKlighdLayoutConfigurationStore extends KlighdLayoutConfigurationStore {

    /**
     * Provider for KLighD layout configuration stores.
     */
    public static final class Provider implements ILayoutConfigurationStore.Provider {

        @Override
        public ILayoutConfigurationStore get(final Object workbenchPart, final Object context) {
            if (context instanceof KGraphElement) {
                try {
                    return new EclipseKlighdLayoutConfigurationStore((IWorkbenchPart) workbenchPart, (KGraphElement) context);
                } catch (IllegalArgumentException e) {
                    // Fall back to null
                }
            }
            return null;
        }
    }

    /** The {@link IWorkbenchPart} attached to this context. */
    private final IWorkbenchPart workbenchPart;

    /**
     * Create a KLighD layout configuration store.
     * 
     * @param workbenchPart
     *            The {@link IWorkbenchPart} attached to this context.
     * @param context
     *            The {@link KGraphElement} of the view model this {@link ILayoutConfigurationStore}
     *            is attached to.
     */
    public EclipseKlighdLayoutConfigurationStore(final IWorkbenchPart workbenchPart,
            final KGraphElement context) {
        super(context);
        this.workbenchPart = workbenchPart;
    }
     
     
    /**
     * We support special layout options whose value we infer if they are not explicitly set on the
     * element. This method assumes that this is indeed the case and returns the special layout option
     * value, if any.
     * 
     * @param optionData the option whose value to return.
     * @return the value or {@code null} if it is not a special layout option.
     */
    private Object getSpecialLayoutOptionValue(final LayoutOptionData optionData) {
        if (optionData.equals(CoreOptions.ASPECT_RATIO) 
                && (getContainer() == null || isSingleNodeOnRootLevel())) {
            // Get aspect ratio for the current diagram
            final IViewer viewer = getViewer();
            if (viewer == null || ((IEclipseViewer) viewer).getControl() == null) {
                return null;
            }

            final Control control = ((IEclipseViewer) viewer).getControl();

            final RunnableWithResult<Double> runnable = new AbstractRunnableWithResult<Double>() {

                public void run() {
                    final Point size;

                    try {
                        size = control.getSize();
                    } catch (final SWTException exception) {
                        // ignore exception
                        return;
                    }

                    if (size.x == 0 || size.y == 0) {
                        return;
                    }

                    setResult(Math.round(ASPECT_RATIO_ROUND * size.x / size.y) / ASPECT_RATIO_ROUND);
                }
            };

            if (control.getDisplay() == Display.getCurrent()) {
                runnable.run();
            } else {
                control.getDisplay().syncExec(runnable);
            }

            return runnable.getResult();
        }

        return null;
    }

    /**
     * Reveals the KLighD (context) {@link IViewer} from the given layout context.
     *
     * @param context
     *            a layout context
     * @return the corresponding KLighD (context) {@link IViewer}, or {@code null}
     */
    private IViewer getViewer() {
        IViewer viewer = graphElement.getProperty(KlighdInternalProperties.VIEWER);

        if (viewer == null) {
            if (workbenchPart instanceof IDiagramWorkbenchPart) {
                viewer = ((IDiagramWorkbenchPart) workbenchPart).getViewer();
                graphElement.setProperty(KlighdInternalProperties.VIEWER, viewer);
            }
        }
        return viewer;
    }

    /**
     * {@inheritDoc}
     */
    public ILayoutConfigurationStore getParent() {
        KGraphElement container = getContainer();
        if (container != null) {
            return new EclipseKlighdLayoutConfigurationStore(workbenchPart, container);
        } else {
            return null;
        }
    }
}
