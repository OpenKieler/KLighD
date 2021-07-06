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
package de.cau.cs.kieler.klighd.ui.internal.options;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.google.common.collect.Maps;

import de.cau.cs.kieler.klighd.DisplayedActionData;
import de.cau.cs.kieler.klighd.IAction;
import de.cau.cs.kieler.klighd.IAction.ActionContext;
import de.cau.cs.kieler.klighd.IAction.ActionResult;
import de.cau.cs.kieler.klighd.IKlighdSelection;
import de.cau.cs.kieler.klighd.IViewChangeListener;
import de.cau.cs.kieler.klighd.IViewer;
import de.cau.cs.kieler.klighd.KlighdDataManager;
import de.cau.cs.kieler.klighd.LightDiagramLayoutConfig;
import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.ZoomStyle;
import de.cau.cs.kieler.klighd.eclipse.EclipseKlighdConstants;
import de.cau.cs.kieler.klighd.eclipse.IEclipseViewer;
import de.cau.cs.kieler.klighd.eclipse.KlighdTreeSelection;
import de.cau.cs.kieler.klighd.kgraph.KGraphElement;
import de.cau.cs.kieler.klighd.krendering.KRendering;
import de.cau.cs.kieler.klighd.util.Iterables2;

/**
 * A factory providing methods for creating action controls in the diagram side bar.
 *
 * @author chsch
 */
public class ActionControlFactory implements ISelectionChangedListener, IViewChangeListener {

    /** The parent composite into which controls are created. */
    private Composite parent;
    /** the form toolkit used to create controls. */
    private FormToolkit formToolkit;
    /** The set of controls to be disposed when {@link #clear()} is called. */
    private final Collection<Control> controls = new LinkedList<Control>();

    /** the vertical space between different option controls, e.g. 2 check buttons. */
    private static final int MAJOR_VERTICAL_SPACING = 6;


    /**
     * Constructor.
     *
     * @param parent the widget container
     * @param formToolkit the form toolkit to be used to create controls
     */
    public ActionControlFactory(final Composite parent, final FormToolkit formToolkit) {
        this.parent = parent;
        this.formToolkit = formToolkit;

        // configure the parent's layout
        final GridLayout gl = new GridLayout(1, false);
        gl.verticalSpacing = MAJOR_VERTICAL_SPACING;
        this.parent.setLayout(gl);
    }

    /**
     * Clear the previously created option controls.
     */
    public void clear() {
        for (final Control c : controls) {
            c.dispose();
        }
        controls.clear();
        actionDataControlMap.clear();
    }

    private Map<DisplayedActionData, Control> actionDataControlMap = Maps.newHashMap();

    /**
     * Factory method for creating a check button related to a 'check' option.
     *
     * @param actionData
     *            an instance of {@link DisplayedActionData} providing the required information
     * @param viewContext
     *            the related {@link ViewContext} the option is declared in
     */
    public void createActionControl(final DisplayedActionData actionData,
            final ViewContext viewContext) {

        final Button button = new Button(parent, SWT.PUSH);
        button.setText(actionData.displayedName);
        button.setToolTipText(actionData.tooltipText);

        if (actionData.image != null) {
            button.setImage(new Image(parent.getDisplay(), EclipseKlighdConstants.convertAWTImageToSWT(actionData.image)));
        }

        // the following call is taken from FormToolkit#createButton(...)
        //  (didn't rely on that method since it forces SWT.FLAT style that looks ugly on OSX)
        formToolkit.adapt(button, true, true);
        controls.add(button);

        if (actionData.enablementTester == null) {
            button.setEnabled(true);
        } else {
            actionDataControlMap.put(actionData, button);
        }

        // add a selection listener for instant diagram updates
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent event) {
                final IAction action =
                        KlighdDataManager.getInstance().getActionById(actionData.actionId);
                final IViewer viewer = viewContext.getViewer();

                ActionResult result = null;

                // this flag is used to track the successful execution of actions
                //  in order to enable animated diagram changes, the viewer must be informed to
                //  record view model changes, which is done once an action is actually executed
                boolean anyActionPerformed = false;
                
                // This flag tracks if any successful action requires a new synthesis to be executed before the layout.
                boolean anyActionRequiresSynthesis = false;

                viewContext.getLayoutRecorder().startRecording();

                // check if we actually have a selection
                final KlighdTreeSelection diagramSelection = ((IEclipseViewer) viewer).getDiagramSelection();
                if (diagramSelection.isEmpty()) {
                    // call the action on the root of the view model
                    result = action.execute(new ActionContext(
                            viewer, null, viewer.getViewContext().getViewModel(), null));
                    anyActionPerformed = result.getActionPerformed();
                    anyActionRequiresSynthesis = result.getNeedsSynthesis();
                } else {
                    // call the action on all selected elements
                    for (final EObject e : Iterables2.toIterable(
                            diagramSelection.diagramElementsIterator())) {

                        final ActionContext aContext;

                        if (e instanceof KGraphElement) {
                            aContext = new ActionContext(viewer, null, (KGraphElement) e, null);
                        } else if (e instanceof KRendering) {
                            aContext = new ActionContext(viewer, null, null, (KRendering) e);
                        } else {
                            continue;
                        }

                        final ActionResult res = action.execute(aContext);
                        if (res != null) {
                            result = res;
                            anyActionPerformed |= res.getActionPerformed();
                            anyActionRequiresSynthesis |= res.getNeedsSynthesis();
                        }
                    }
                }

                if (anyActionRequiresSynthesis) {
                    viewContext.update();
                }
                
                if (anyActionPerformed) {
                    new LightDiagramLayoutConfig(viewContext)
                            .animate(result.getAnimateLayout())
                            .zoomStyle(ZoomStyle.create(result, viewContext))
                            .focusElement(result.getFocusElement())
                            .previousPosition(result.getPreviousPosition())
                            .options(result.getLayoutConfigs())
                            .performLayout();
                } else {
                    viewContext.getLayoutRecorder().stopRecording(ZoomStyle.NONE, null, 0);
                }
            }
        });
    }


    /**
     * {@inheritDoc}
     */
    public void selectionChanged(final SelectionChangedEvent event) {
        if (!(event.getSelection() instanceof IKlighdSelection)) {
            return;
        }

        updateControls((IKlighdSelection) event.getSelection());
    }

    /**
     * {@inheritDoc}
     */
    public void viewChanged(final ViewChange change) {
        updateControls(((IEclipseViewer) change.getViewer()).getSelection());
    }

    /**
     * Executes the enablement tester of the tracked {@link DisplayedActionData} and set the
     * corresponding controls' enablement accordingly.
     *
     * @param selection the current {@link IKlighdSelection}
     */
    private void updateControls(final IKlighdSelection selection) {
        for (final Map.Entry<DisplayedActionData, Control> entry : actionDataControlMap.entrySet()) {
            // since only those actionData with 'enablementTester != null' are added to the map ...
            entry.getValue().setEnabled(entry.getKey().enablementTester.apply(selection));
        }
    }
}
