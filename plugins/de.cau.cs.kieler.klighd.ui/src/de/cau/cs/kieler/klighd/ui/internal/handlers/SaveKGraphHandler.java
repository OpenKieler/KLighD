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
package de.cau.cs.kieler.klighd.ui.internal.handlers;

import java.io.File;
import java.util.Arrays;
import java.util.Queue;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import com.google.common.collect.Lists;

import de.cau.cs.kieler.klighd.IViewer;
import de.cau.cs.kieler.klighd.Klighd;
import de.cau.cs.kieler.klighd.LightDiagramLayoutConfig;
import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.eclipse.EclipseViewContext;
import de.cau.cs.kieler.klighd.eclipse.IDiagramWorkbenchPart;
import de.cau.cs.kieler.klighd.eclipse.IEclipseViewer;
import de.cau.cs.kieler.klighd.eclipse.KlighdTreeSelection;
import de.cau.cs.kieler.klighd.kgraph.KNode;
import de.cau.cs.kieler.klighd.ui.KlighdUIPlugin;

/**
 * Handler to store the view model KGraph (or a subgraph) of a KlighD view to a file. This class is
 * mainly intended to support debugging purposes.
 * 
 * For "chunky", we export 4 types of diagrams:
 *      flat_           Every compound node with its children and none of the children being expanded
 *      expanded_       Every compound node with all of its children being expanded
 *      expanded_flat   Every compound node with its children of size as if they were expanded but all
 *                      further children removed 
 *      inchierarchy_   Incrementally expanded diagrams, starting with the top-level compound node and 
 *                      zero expanded children. Afterwards compound nodes are expanded one after another 
 *                      in a breath-first order.
 * 
 * @author uru, nbw
 */
public class SaveKGraphHandler extends AbstractHandler {

    // Parameter to differentiate normal export and "chunky" export
    private static final String EXPORT_TYPE_ID = "de.cau.cs.kieler.klighd.ui.saveKGraph.exportType";
    private static final String EXPORT_TYPE_SINGLE = "single";
    private static final String EXPORT_TYPE_CHUNKY = "chunky";

//    private static final String[] KGRAPH_FILE_EXTENSIONS = { ".kgt", ".kgx" };
    private static final String[] KGRAPH_FILE_EXTENSIONS = { ".kgx" };

    /**
     * Executes the save action with the map of parameter values by name.
     *
     * @param event
     *            An event containing all the information about the current state of the
     *            application; must not be <code>null</code>.
     * @return the result of the execution. Reserved for future use, must be <code>null</code>.
     * @throws ExecutionException
     *             if an exception occurred during execution.
     */
    public Object execute(final ExecutionEvent event) throws ExecutionException {

        // Phase 1: Find out what to export
        // Get the active diagram viewer.
        // We can get the currently active workbench part from the event
        final IWorkbenchPart part = HandlerUtil.getActivePart(event);

        // The workbench part should be out view part
        // and should be able to give us our active viewer.
        final IViewer viewer;
        if (part instanceof IDiagramWorkbenchPart) {
            viewer = ((IDiagramWorkbenchPart) part).getViewer();
        } else {
            // No view part means no export.
            // Something was not configured correct, but we are done here.
            return null;
        }

        // Now we want to find out whether the whole diagram should be exported
        // or if some child node has been selected
        final KlighdTreeSelection selection;
        // In case this handler is invoked via a context menu,
        // the activeMenuSelection (ISources#ACTIVE_MENU_SELECTION_NAME) is available
        ISelection s = HandlerUtil.getActiveMenuSelection(event);

        if (s instanceof KlighdTreeSelection) {
            // If it's a KLighD selection (it is supposed to be), we're fine :-)
            selection = (KlighdTreeSelection) s;
        } else if (s == null) {
            // If no activeMenuSelection is set, the handler may be called by the main menu,
            // toolbar, or a key binding; refer to the global selection in that case
            s = HandlerUtil.getCurrentSelectionChecked(event);

            if (s instanceof KlighdTreeSelection) {
                // Again if it's a KLighD selection, we're fine :-)
                selection = (KlighdTreeSelection) s;
            } else {
                // Something really strange must have happened
                return null;
            }
        } else {
            // Something really strange must have happened
            return null;
        }

        // We have some selection now, but we need to find out if it is valid
        // So we try to find the graph this node represents
        final ViewContext viewContext = selection.getViewContext();
        if (viewContext == null) {
            Klighd.show(new Status(IStatus.ERROR, KlighdUIPlugin.PLUGIN_ID,
                    "Could not determine ViewContext."));
        }

        final KNode subgraph;
        if (selection.isEmpty()) {
            // Nothing is selected, nothing to export
            Klighd.show(new Status(IStatus.ERROR, KlighdUIPlugin.PLUGIN_ID,
                    "Please select something to export."));
            return null;
        } else if (selection.size() == 1 && selection.getFirstElement() instanceof KNode) {
            // If exactly one element is selected and it is a knode
            // we suppose the user wanted to select that subgraph
            subgraph = (KNode) selection.getFirstElement();
        } else {
            // Otherwise we export the whole view model graph
            subgraph = viewContext.getViewModel();
        }

        // We grab the shell for message boxes later
        final Shell shell = Display.getCurrent().getActiveShell();

        // Now we decide whether to export just the normal model or lots of chunks for each model
        final String exportType = event.getParameter(EXPORT_TYPE_ID);
        if (exportType.equals(EXPORT_TYPE_SINGLE)) {
            // ---------------------------------------------------
            // Default export of the selected (sub-)graph
            // ---------------------------------------------------
            final SaveKGraphDialog fd = new SaveKGraphDialog(((IEclipseViewer) viewer).getControl().getShell());
            // show a KLighd save KGraph dialog
            int success = fd.open();

            if (success == Dialog.OK) {
                // retrieve selected
                IPath file = fd.getFilePath();

                // if no valid file extension provided, add it
                if (Arrays.stream(KGRAPH_FILE_EXTENSIONS)
                        .noneMatch(ext -> file.toOSString().endsWith(ext))) {
                    file.addFileExtension(KGRAPH_FILE_EXTENSIONS[0]);
                }

                // create uri to save file to the right place
                URI uri = null;
                if (fd.isWorkspacePath()) {
                    uri = URI.createPlatformResourceURI(file.toOSString(), true);
                } else {
                    uri = URI.createFileURI(file.toOSString());
                }

                ExportKGraphHelper.export(subgraph, uri, fd.protectIP(), false);

                // return a confirmation that everything works fine
                final String title = "Diagram export successful.";
                final String msg = "KLighD diagram export finished successfully.";

                MessageDialog.openInformation(shell, title, msg);
            }
        } else if (exportType.equals(EXPORT_TYPE_CHUNKY)) {
            // ---------------------------------------------------
            // Export the diagram by recursively
            // exporting hierarchy levels, once flat
            // and once with internal hierarchy expanded
            // ---------------------------------------------------
            ContainerSelectionDialog csd = new ContainerSelectionDialog(shell,
                    ResourcesPlugin.getWorkspace().getRoot(), false, "");
            int success = csd.open();
            if (success == ContainerSelectionDialog.OK) {
                IPath container = (IPath) csd.getResult()[0];
                String destination = container.toOSString().toString();

                exportRecursivelyChunky(subgraph, destination, viewContext);
            }
        }

        return null;
    }

    private void exportRecursivelyChunky(final KNode subgraph, final String destination,
            final ViewContext viewContext) {

        // collect all subgraph s
        final Queue<KNode> flatNodes = Lists.newLinkedList();
        final Queue<KNode> expandedNodes = Lists.newLinkedList();
        String path = destination + File.separator;

        flatNodes.add(subgraph);
        expandedNodes.add(subgraph);

        // --------
        // #1 flat_
        // expand the flat versions
        while (!flatNodes.isEmpty()) {
            final KNode n = flatNodes.poll();

            if (n.getChildren().size() > 1) {
                String filename =
                        "flat_" + getModelPathName(n, viewContext) + KGRAPH_FILE_EXTENSIONS[0];
                System.out.println("Exporting: " + filename);
                URI uri = URI.createPlatformResourceURI(path + filename, true);
                ExportKGraphHelper.export(n, uri, false, true);
            }

            // expand and collect the children
            for (final KNode child : n.getChildren()) {

                // TODO application specific expansion goes here
                if (!child.getChildren().isEmpty()) {
                    viewContext.getViewer().expand(child);

                    flatNodes.add(child);
                    expandedNodes.add(child);
                }
            }

            // if we expanded some nodes, perform layout
            if (!n.getChildren().isEmpty()) {
                new LightDiagramLayoutConfig(viewContext).animate(false).performLayout();
            }
        }

        // ----------------
        // #2 inchierarchy_
        // collapse everything again
        for (KNode node : expandedNodes) {
            if (node != subgraph) {
                viewContext.getViewer().collapse(node);
            }
        }
        final Queue<KNode> increaseHierarchy = Lists.newLinkedList();
        increaseHierarchy.add(subgraph);
        new LightDiagramLayoutConfig(viewContext).animate(false).performLayout();
        int i = 0;
        // SUPPRESS CHECKSTYLE NEXT MagicNumber
        int requiredPaddingZeroes = (int) Math.ceil(Math.log(expandedNodes.size()) / Math.log(10));
        do {

            KNode toExpand = increaseHierarchy.poll();
            viewContext.getViewer().expand(toExpand);
            new LightDiagramLayoutConfig(viewContext).animate(false).performLayout();

            String filename = "inchierarchy_" + getModelPathName(subgraph, viewContext) + "_"
                    + String.format("%0" + requiredPaddingZeroes + "d", i++)
                    + KGRAPH_FILE_EXTENSIONS[0];
            System.out.println("Exporting: " + filename);
            URI uri = URI.createPlatformResourceURI(path + filename, true);
            ExportKGraphHelper.export(subgraph, uri, false, false);

            for (KNode child : toExpand.getChildren()) {
                if (!child.getChildren().isEmpty()) {
                    increaseHierarchy.add(child);
                }
            }

        } while (!increaseHierarchy.isEmpty());

        // export the expanded versions
        while (!expandedNodes.isEmpty()) {
            final KNode n = expandedNodes.poll();

            if (n.getChildren().size() > 1) {
                String filename = getModelPathName(n, viewContext) + KGRAPH_FILE_EXTENSIONS[0];
                System.out.println("Exporting: " + filename);
                URI uri = URI.createPlatformResourceURI(path + "expanded_" + filename, true);

                // ----------------
                // #3 expanded_
                ExportKGraphHelper.export(n, uri, false, false);

                uri = URI.createPlatformResourceURI(path + "expanded_flat_" + filename, true);

                // ----------------
                // #4 expanded_flat_
                // remove children of compound nodes and export again
                ExportKGraphHelper.export(n, uri, false, true);
            }
        }
    }

    private String getModelPathName(final KNode node, final ViewContext vc) {
        String s = "";
        KNode parent = node;
        while (parent != null) {
            String name = parent.toString().replace("\"", "").replace(" ", "").replace("KNode", "");
            name = name.replace("de.cau.cs.kieler.klighd.kgraph.impl.Impl", "Node");
            s = "-" + name + s;
            parent = parent.getParent();
        }
        s = s.substring(1);
        if (vc != null && ((EclipseViewContext) vc).getDiagramWorkbenchPart() != null) {
            s = ((EclipseViewContext) vc).getDiagramWorkbenchPart().getTitle() + "-" + s;
        }
        return s;
    }
}
