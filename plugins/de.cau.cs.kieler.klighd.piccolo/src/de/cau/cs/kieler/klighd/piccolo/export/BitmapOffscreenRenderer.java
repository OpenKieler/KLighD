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

import java.io.OutputStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.elk.graph.properties.IPropertyHolder;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.eclipse.IDiagramExporter.ExportDataBuilder;
import de.cau.cs.kieler.klighd.piccolo.KlighdPiccolo;
import de.cau.cs.kieler.klighd.piccolo.internal.KlighdCanvas;
import de.cau.cs.kieler.klighd.util.AbstractRunnableWithResult;
import de.cau.cs.kieler.klighd.util.KlighdProperties;
import de.cau.cs.kieler.klighd.util.RunnableWithResult;

/**
 * A concrete implementation of {@link de.cau.cs.kieler.klighd.IOffscreenRenderer
 * IOffscreenRenderer} providing diagram in BMP, JPEG, and PNG format.<br>
 * (see registration in plugin.xml)
 *
 * @author chsch
 */
public class BitmapOffscreenRenderer extends AbstractOffscreenRenderer {

    /** The id used at registration of the offscreen renderer in the plugin.xml. */
    public static final String ID = "de.cau.cs.kieler.klighd.piccolo.export.BitmapOffscreenRenderer";

    private static final String DISPLAY_ACCESS_FAILURE =
            "KLighD offscreen diagram export: "
                    + "A display is required for creating raster images (BMP, JPEG, PNG)!";

    /**
     * {@inheritDoc}
     */
    public IStatus render(final ViewContext viewContext, final OutputStream output,
            final IPropertyHolder properties) {

        final RunnableWithResult<IStatus> runnable = new AbstractRunnableWithResult<IStatus>() {
            public void run() {
                setResult(doRender(viewContext, output, properties));
            };
        };

        if (Display.getCurrent() != null) {
            runnable.run();
        } else {
            try {
                // Note that the proper termination of this statement requires an
                //  event loop calling "Display.getCurrent().readAndDispatch();" repeatedly
                Display.getDefault().syncExec(runnable);
            } catch (final Throwable e) {
                return new Status(IStatus.ERROR, KlighdPiccolo.PLUGIN_ID,
                        DISPLAY_ACCESS_FAILURE, e);
            }
        }

        return runnable.getResult();
    }

    private IStatus doRender(final ViewContext viewContext, final OutputStream output,
            final IPropertyHolder properties) {

        final int imageScale = properties != null
                ? properties.getProperty(IMAGE_SCALE) : IMAGE_SCALE.getDefault();

        final Shell shell = new Shell();
        final KlighdCanvas canvas = new KlighdCanvas(shell, 0,
                properties != null ? properties.getProperty(KlighdProperties.CANVAS_COLOR)
                        : KlighdProperties.CANVAS_COLOR.getDefault());

        try {
            // build up the diagram, i.e. apply the necessary diagram syntheses, etc.
            this.buildUpDiagram(viewContext, canvas.getCamera(), properties);

        } catch (final RuntimeException e) {
            return new Status(IStatus.ERROR, KlighdPiccolo.PLUGIN_ID,
                    BUILDING_UP_FIGURES_FAILURE_MSG, e);
        }

        try {
            final String format = properties != null
                    ? properties.getProperty(OUTPUT_FORMAT) : BitmapExporter.SUB_FORMAT_PNG;

            new BitmapExporter().export(canvas,
                    new ExportDataBuilder(viewContext, format, output).scale(imageScale).build());

        } catch (final RuntimeException e) {
            return new Status(IStatus.ERROR, KlighdPiccolo.PLUGIN_ID,
                    EXPORT_DIAGRAM_FAILURE_MSG, e);
        }

        shell.close();

        return Status.OK_STATUS;
    }
}
