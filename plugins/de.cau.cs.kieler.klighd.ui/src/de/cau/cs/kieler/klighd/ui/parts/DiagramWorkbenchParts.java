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
package de.cau.cs.kieler.klighd.ui.parts;

import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import de.cau.cs.kieler.klighd.IViewer;
import de.cau.cs.kieler.klighd.KlighdPreferences;
import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.eclipse.EclipseLightDiagramLayoutConfig;
import de.cau.cs.kieler.klighd.eclipse.EclipseLightDiagramServices;
import de.cau.cs.kieler.klighd.eclipse.IDiagramWorkbenchPart;
import de.cau.cs.kieler.klighd.eclipse.IEclipseViewer;

/**
 * A container class providing helpful things for {@link IDiagramWorkbenchPart IDiagramWorkbenchParts}.
 *
 * @author chsch
 */
public final class DiagramWorkbenchParts {

    private DiagramWorkbenchParts() {
    }

    /**
     * Provides a new {@link DiagramAreaChangeListener} to be installed on {@link IViewer diagram
     * viewer's} {@link IViewer#getControl() control} that is in charge of updating the diagram zoom
     * or even the diagram layout if the control's size (and aspect ration) changes.
     *
     * @param dwb
     *            the {@link IDiagramWorkbenchPart} this associated to
     * @return the expected {@link DiagramAreaChangeListener}
     */
    public static DiagramAreaChangeListener createDiagramAreaChangeListener(
            final IDiagramWorkbenchPart dwb) {
        return new DiagramAreaChangeListener(dwb);
    }

    /**
     * A {@link ControlAdapter} to be installed on {@link IViewer diagram viewer's}
     * {@link IViewer#getControl() control} that is in charge of updating the diagram zoom or even
     * the diagram layout if the control's size (and aspect ration) changes.
     */
    public static class DiagramAreaChangeListener extends ControlAdapter {

        /** The aspect ratio is rounded at two decimal places. */
        private static final double ASPECT_RATIO_ROUND = 100;

        private double oldAspectRatio = -1;

        private boolean isScheduled = false;

        private final IDiagramWorkbenchPart diagramWorkbenchPart;

        /**
         * Constructor.
         *
         * @param dwb
         *            the {@link IDiagramWorkbenchPart} this {@link DiagramAreaChangeListener} is
         *            attached to
         */
        public DiagramAreaChangeListener(final IDiagramWorkbenchPart dwb) {
            this.diagramWorkbenchPart = dwb;
        }

        @Override
        public void controlResized(final ControlEvent e) {
            final ViewContext context = diagramWorkbenchPart.getViewContext();

            if (KlighdPreferences.isZoomOnWorkbenchpartChange() && context != null) {
                final Control control = ((IEclipseViewer) context.getViewer()).getControl();

                if (control == null || control.isDisposed() || !control.isVisible()) {
                    return;
                }

                // this is a poor man barrier
                // as long as nobody abuses this listener this method will be always
                //  executed by the Display's thread
                // since 'isScheduled's reset statement below is also executed by the UI thread
                //  the world is fine :-)
                if (isScheduled) {
                    return;
                }

                isScheduled = true;
                Display.getCurrent().asyncExec(new Runnable() {

                    public void run() {
                        final IViewer viewer = diagramWorkbenchPart.getViewer();
                        final Control control = ((IEclipseViewer) viewer).getControl();

                        isScheduled = false;
                        if (!control.isDisposed() && control.isVisible()) {
                            zoomOrRelayout(viewer);
                        }
                    }
                });
            }
        }

        /**
         * Some layouters (eg KlayLayered) might change the layout based on the aspect ratio of the
         * canvas. Thus, when the aspect ratio passes 1 we re-layout the diagram instead of just
         * triggering a re-zoom.
         */
        private void zoomOrRelayout(final IViewer viewer) {
            // calculate the aspect ratio of the current canvas
            final Point size = ((IEclipseViewer) viewer).getControl().getSize();

            // assure that the composite's size is settled before we execute the layout
            if (size.x > 0 && size.y > 0) {
                final double aspectRatio =
                        Math.round(ASPECT_RATIO_ROUND * size.x / size.y) / ASPECT_RATIO_ROUND;

                if (oldAspectRatio == -1 || (oldAspectRatio > 1 && aspectRatio < 1)
                        || (oldAspectRatio < 1 && aspectRatio > 1)) {
                    new EclipseLightDiagramLayoutConfig(diagramWorkbenchPart).performLayout();
                    oldAspectRatio = aspectRatio;

                } else {
                    EclipseLightDiagramServices.zoomDiagram(diagramWorkbenchPart);
                }
            }
        }
    }
}
