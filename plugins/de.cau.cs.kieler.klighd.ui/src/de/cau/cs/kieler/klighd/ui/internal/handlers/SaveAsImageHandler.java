/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse Rich Client
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
package de.cau.cs.kieler.klighd.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import de.cau.cs.kieler.klighd.IKlighdStatusManager;
import de.cau.cs.kieler.klighd.IViewer;
import de.cau.cs.kieler.klighd.Klighd;
import de.cau.cs.kieler.klighd.KlighdDataManager;
import de.cau.cs.kieler.klighd.eclipse.EclipseKlighdDataManager;
import de.cau.cs.kieler.klighd.eclipse.IDiagramExporter;
import de.cau.cs.kieler.klighd.eclipse.IDiagramWorkbenchPart;
import de.cau.cs.kieler.klighd.eclipse.IEclipseViewer;
import de.cau.cs.kieler.klighd.ui.KlighdUIPlugin;

/**
 * An action which invokes the 'save-as-image' dialog and performs the diagram export.
 *
 * @author mri
 * @author uru
 * @author chsch
 */
public class SaveAsImageHandler extends AbstractHandler {

    /**
     * {@inheritDoc}
     */
    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        final IWorkbenchPart part = HandlerUtil.getActivePart(event);

        final IViewer viewer;
        if (part instanceof IDiagramWorkbenchPart) {
            viewer = ((IDiagramWorkbenchPart) part).getViewer();
        } else {
            return null;
        }

        final Shell shell = ((IEclipseViewer) viewer).getControl().getShell();

        // open the dialog to receive the required user input
        final SaveAsImageDialog dialog = new SaveAsImageDialog(viewer.getViewContext(), shell);
        final int code = dialog.open();

        if (code != Dialog.OK
                || dialog.getCurrentExporter() == null /* may happen when closing the app before closing the dialog */) {
            return null;
        }

        // retrieve the exporter from the central registry
        final IDiagramExporter exporter =
                ((EclipseKlighdDataManager) KlighdDataManager.getInstance()).getExporter(dialog.getCurrentExporter().exporterId);

        IStatus res;
        try {
            // execute the export process
            res = exporter.export(((IEclipseViewer) viewer).getControl(), dialog.getExportData());

        } catch (final Throwable t) {
            final String msg = "The diagram export could not be completed.";
            res = new Status(IStatus.ERROR, KlighdUIPlugin.PLUGIN_ID, msg, t);
        }

        if (res == Status.OK_STATUS) {
            final String title = "Diagram export successful.";
            final String msg = "KLighD diagram export finished successfully.";

            MessageDialog.openInformation(shell, title, msg);
            return null;

        } else if (res == null) {
            final String msg = "KLighD diagram export: " + exporter.getClass().getCanonicalName()
                    + " must return an IStatus!";
            Klighd.show(
                    new Status(IStatus.WARNING, KlighdUIPlugin.PLUGIN_ID, msg));

        } else if (res.getException() instanceof OutOfMemoryError) {
            final String title = "Out of heap memory.";
            final String msg = "Diagram image could not be exported due to heap space overflow. "
                    + "Retry tiled export with more or smaller image tiles.";
            MessageDialog.openError(shell, title, msg);

        } else {
            Klighd.getStatusManager().handle(res,
                    // without logging the status, no stack trace will be available
                    IKlighdStatusManager.BLOCK | IKlighdStatusManager.SHOW | IKlighdStatusManager.LOG);
        }

        return null;
    }
}
