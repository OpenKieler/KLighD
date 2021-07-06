/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 *
 * Copyright 2013 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 *
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klighd.piccolo.export;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Control;

import de.cau.cs.kieler.klighd.eclipse.IDiagramExporter;
import de.cau.cs.kieler.klighd.piccolo.KlighdPiccolo;
import de.cau.cs.kieler.klighd.piccolo.internal.KlighdCanvas;

/**
 * Abstract implementation of {@link IDiagramExporter} supporting the export of diagrams depicted by
 * a {@link KlighdCanvas}.<br>
 * <br>
 * Thus, it treats the given {@link Control} as a {@link KlighdCanvas} and redirects to
 * {@link #export(KlighdExportInfo))}, which must be implemented by concrete subclasses.
 *
 * @author chsch
 */
public abstract class KlighdCanvasExporter extends AbstractDiagramExporter implements
        IDiagramExporter {

    private static final String INVALID_CONTROL_FAILURE =
            "KLighD diagram export: The SWT Control of type ## is not supported by this &&!";

    /**
     * {@inheritDoc}
     */
    public IStatus export(final Control control, final ExportData data) {

        if (control instanceof KlighdCanvas) {
            final KlighdCanvas canvas = (KlighdCanvas) control;
            return export(canvas, data);

        } else {
            final String msg = INVALID_CONTROL_FAILURE
                    .replace("##", control.getClass().getCanonicalName())
                    .replace("&&", this.getClass().getCanonicalName());

            return new Status(IStatus.WARNING, KlighdPiccolo.PLUGIN_ID, msg);
        }
    }

    /**
     * Exports the diagram depicted by the given <code>control</code>.
     *
     * @param canvas
     *            the canvas to export
     * @param data
     *            the specified export info
     *
     * @return {@link org.eclipse.core.runtime.Status#OK_STATUS Status#OK_STATUS} if the diagram
     *         export went successfully, an {@link IStatus} providing information on the failure
     *         otherwise.
     * @see IDiagramExporter#export(de.cau.cs.kieler.klighd.eclipse.IDiagramExporter.ExportData, Control)
     */
    public abstract IStatus export(KlighdCanvas canvas, ExportData data);
}
