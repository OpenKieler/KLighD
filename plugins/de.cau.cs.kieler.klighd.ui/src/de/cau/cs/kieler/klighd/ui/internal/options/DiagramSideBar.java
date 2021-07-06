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
package de.cau.cs.kieler.klighd.ui.internal.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.elk.core.LayoutConfigurator;
import org.eclipse.elk.core.util.Pair;
import org.eclipse.elk.graph.properties.IProperty;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import de.cau.cs.kieler.klighd.DisplayedActionData;
import de.cau.cs.kieler.klighd.IDiagramWorkbenchPart;
import de.cau.cs.kieler.klighd.IViewer;
import de.cau.cs.kieler.klighd.KlighdConstants;
import de.cau.cs.kieler.klighd.KlighdPreferences;
import de.cau.cs.kieler.klighd.LightDiagramServices;
import de.cau.cs.kieler.klighd.SynthesisOption;
import de.cau.cs.kieler.klighd.ViewChangeType;
import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.ZoomStyle;
import de.cau.cs.kieler.klighd.internal.ISynthesis;
import de.cau.cs.kieler.klighd.ui.KlighdUIPlugin;
import de.cau.cs.kieler.klighd.ui.parts.DiagramViewPart;
import de.cau.cs.kieler.klighd.util.KlighdSynthesisProperties;

/**
 * Builds up the diagram side bar containing the controls for configuring diagram synthesis and
 * layout options.
 *
 * @author chsch
 */
public final class DiagramSideBar {

    /** The initial width of the option pane and the diagram viewer. */
    private static final int INITIAL_OPTIONS_FORM_WIDTH = 230;

    /** The minimal width of the option pane and the diagram viewer. */
    private static final int MINIMAL_OPTIONS_FORM_WIDTH = 100;

    /** The space left between the 'Diagram options' and 'Layout options' sub forms. */
    private static final int SYNTHESIS_LAYOUT_OPTIONS_SPACE = 20;

    /** The width of the separator. */
    private static final int SASH_WIDTH = 7;

    /** Constant value denoting 100%. */
    private static final int FULL = 100;

    /** Right and top offset for the toolbar buttons shown in the canvas. */
    private static final int CANVAS_ZOOM_BTNS_OFFSET = 3;

    private boolean zoomButtonsVisible = KlighdPreferences.isShowZoomConfigButtons();

    private boolean initiallyExpanded = KlighdPreferences.isExpandSideBar();

    private final Composite sideBarParent;
    
    /** the factory for action controls. */
    private ActionControlFactory actionControlFactory;

    /** the factory for diagram synthesis option controls. */
    private SynthesisOptionControlFactory synthesisOptionControlFactory;

    /** the factory for layout option controls. */
    private LayoutOptionControlFactory layoutOptionControlFactory;

    /** the form toolkit, is only kept to properly dispose it finally. */
    private FormToolkit optionsformToolkit;

    /** The form that holds the zoom buttons in the sidebar. */
    private Form sideZoomBtnsForm;

    /** The composite that holds the zoom buttons in the canvas. */
    private Composite canvasZoomBtnsContainer;

    /** The scrolled form that holds option forms. */
    private ScrolledForm scrolledRootForm;
    
    /** The form that holds actions. */
    private Form actionsForm;

    /** The form that holds synthesis options. */
    private Form synthesisOptionsForm;

    /** The form that holds layout options. */
    private Form layoutOptionsForm;

    /** the set of resources to be disposed when the view is closed. */
    private final List<Resource> resources = new LinkedList<Resource>();

    private final List<Control> sideBarControls = new ArrayList<Control>(5);

    private FormData sashLayoutData = null;

    /**
     * The horizontal position of the side bar's sash in terms of the (negative) offset wrt. the
     * workbenchPart's right side. Its kept as an attribute in order to avoid the re-initialization
     * of the side bar's size in case a different model has been assigned to the current view.
     */
    private int horizontalPos = initiallyExpanded ? -INITIAL_OPTIONS_FORM_WIDTH : -SASH_WIDTH;

    private ViewContext viewContext;

    /**
     * Hidden constructor.
     *
     * @param parent
     *            the parent {@link Composite} to attach the side bar to
     */
    private DiagramSideBar(final Composite parent) {
        this.sideBarParent = parent;
    }

    /**
     * Provides the {@link LayoutConfigurator} with layout settings from <code>this</code> side bar.
     *
     * @return the employed {@link LayoutConfigurator}
     */
    public LayoutConfigurator getLayoutConfig() {
        return layoutOptionControlFactory.getLayoutConfig();
    }

    /**
     * Creates and attaches a side bar to the given <code>parent</code> {@link Composite}.<br>
     * The given <code>diagramContainer</code> is thereby integrated in the tree of widgets.
     *
     * @param parent
     *            the parent {@link Composite} to attach the side bar to
     * @param diagramContainer
     *            the {@link Composite} accommodating the diagram (later on)
     * @param viewContext
     *            the {@link ViewContext} to get the configured option entries from
     * @return an instance of {@link DiagramSideBar} allowing to access the employed
     *         {@link ILayoutConfig}
     */
    public static DiagramSideBar createSideBar(final Composite parent,
            final Composite diagramContainer, final ViewContext viewContext) {
        return new DiagramSideBar(parent).initialize(diagramContainer, viewContext);
    }

    /**
     * @see #createSideBar(Composite, Composite, ViewContext)
     */
    private DiagramSideBar initialize(final Composite diagramContainer,
            final ViewContext theViewContext) {
        // SUPPRESS CHECKSTYLE PREVIOUS 2 Length -- UI stuff is lengthy :-(

        viewContext = theViewContext;

        // in addition to the side bar initialization preference setting
        //  a diagram specific configuration shall be possible; thus...
        switch (viewContext.getProperty(KlighdSynthesisProperties.REQUESTED_SIDE_BAR_HANDLING)) {
        case UNDEFINED:
            break;
        case EXPAND:
            initiallyExpanded = true;
            horizontalPos = -INITIAL_OPTIONS_FORM_WIDTH;
            break;
        case COLLAPSE:
            initiallyExpanded = false;
            horizontalPos = -SASH_WIDTH;
        }

        switch (viewContext.getProperty(
                            KlighdSynthesisProperties.REQUESTED_ZOOM_CONFIG_BUTTONS_HANDLING)) {
        case UNDEFINED:
            break;
        case SHOW:
            zoomButtonsVisible = true;
            break;
        case HIDE:
            zoomButtonsVisible = false;
        }

        sideBarParent.setLayout(new FormLayout());

        // for easier managing the visibility of the side bar's collapse/expand arrows
        //  let's put them in a dedicated container, this is advantageous below
        final Composite arrowsContainer = new Composite(sideBarParent, SWT.NONE);
        sideBarControls.add(arrowsContainer);
        arrowsContainer.setVisible(false);
        final StackLayout arrowLabelContainerLayout = new StackLayout();
        arrowsContainer.setLayout(arrowLabelContainerLayout);

        // create the right arrow for collapsing the options pane
        final Label rightArrowLabel = new Label(arrowsContainer, SWT.NONE);
        final Image rightArrow = KlighdUIPlugin
                .getImageDescriptorFromKlighdBase("icons/arrow-right.gif").createImage();
        resources.add(rightArrow);
        rightArrowLabel.setImage(rightArrow);
        rightArrowLabel.setVisible(true);

        // create the left arrow for expanding the options pane
        final Label leftArrowLabel = new Label(arrowsContainer, SWT.NONE);
        final Image leftArrow = KlighdUIPlugin
                .getImageDescriptorFromKlighdBase("icons/arrow-left.gif").createImage();
        resources.add(leftArrow);
        leftArrowLabel.setImage(leftArrow);
        leftArrowLabel.setVisible(true);

        // depending on the preference of initially expanding or collapsing the side bar
        //  let the corresponding symbol be visible
        arrowLabelContainerLayout.topControl = initiallyExpanded ? rightArrowLabel : leftArrowLabel;

        // create the sash for resizing the options pane
        final Sash sash = new Sash(sideBarParent, SWT.VERTICAL);
        sideBarControls.add(sash);
        sash.addPaintListener(new LinePainter());
        sash.setVisible(false);

        optionsformToolkit = new FormToolkit(sideBarParent.getDisplay());

        if (zoomButtonsVisible) {
            // create toolbar buttons in the sidebar
            sideZoomBtnsForm = optionsformToolkit.createForm(sideBarParent);
            sideZoomBtnsForm.setText(null);
            sideBarControls.add(sideZoomBtnsForm);
            final Composite sideZoomBtnsContainer = sideZoomBtnsForm.getBody();
            sideZoomBtnsForm.setBackground(Display.getCurrent().getSystemColor(
                    SWT.COLOR_LIST_BACKGROUND));

            initializeZoomButtons(sideZoomBtnsContainer);
        }

        scrolledRootForm = optionsformToolkit.createScrolledForm(sideBarParent);
        scrolledRootForm.setText(null);
        sideBarControls.add(scrolledRootForm);

        final Composite formRoot = scrolledRootForm.getBody();
        formRoot.setLayout(new FormLayout());

        // create container for diagram synthesis options
        actionsForm = optionsformToolkit.createForm(formRoot);
        sideBarControls.add(actionsForm);
        actionsForm.setText("Actions");
        actionsForm.setVisible(false);
        final Composite actionsContainer = actionsForm.getBody();

        // create the factory for diagram synthesis option controls to fill the options container
        actionControlFactory = new ActionControlFactory(actionsContainer, optionsformToolkit);

        // create container for diagram synthesis options
        synthesisOptionsForm = optionsformToolkit.createForm(formRoot);
        sideBarControls.add(synthesisOptionsForm);
        synthesisOptionsForm.setText("Diagram Options");
        synthesisOptionsForm.setVisible(false);
        final Composite synthesisOptionsContainer = synthesisOptionsForm.getBody();

        // create the factory for diagram synthesis option controls to fill the options container
        synthesisOptionControlFactory = new SynthesisOptionControlFactory(
                synthesisOptionsContainer, optionsformToolkit);

        // create container for layout options
        layoutOptionsForm = optionsformToolkit.createForm(formRoot);
        sideBarControls.add(layoutOptionsForm);
        layoutOptionsForm.setText("Layout Options");
        layoutOptionsForm.setVisible(false);
        final Composite layoutOptionsContainer = layoutOptionsForm.getBody();

        // create the factory for layout option controls to fill the options container
        layoutOptionControlFactory = new LayoutOptionControlFactory(
                layoutOptionsContainer, optionsformToolkit);

        // prepare the form layout data for each of the above created widgets
        final FormData diagramContainerLayoutData = new FormData();
        diagramContainerLayoutData.top = new FormAttachment(0);
        diagramContainerLayoutData.bottom = new FormAttachment(FULL);
        diagramContainerLayoutData.left = new FormAttachment(0);
        diagramContainerLayoutData.right = new FormAttachment(sash);
        diagramContainer.setLayoutData(diagramContainerLayoutData);

        final FormData arrowsContainerLayoutData = new FormData();
        arrowsContainerLayoutData.top = new FormAttachment(0);
        arrowsContainerLayoutData.bottom = new FormAttachment(sash);
        arrowsContainerLayoutData.left = new FormAttachment(diagramContainer);
        arrowsContainerLayoutData.right = new FormAttachment(scrolledRootForm);
        arrowsContainer.setLayoutData(arrowsContainerLayoutData);

        // initially put the sash outside its parent composite's visible area
        //  right next to that widget's right border (i.e. at 100% of its width)
        // if the side bar is to be shown later on, a negative absolute offset
        //  will be configured on the 'left' form attachment
        sashLayoutData = new FormData();
        sashLayoutData.top = new FormAttachment(arrowsContainer);
        sashLayoutData.bottom = new FormAttachment(FULL);
        sashLayoutData.left = new FormAttachment(FULL);
        sashLayoutData.width = SASH_WIDTH;
        sash.setLayoutData(sashLayoutData);

        if (zoomButtonsVisible) {
            final FormData toolbarFormLayoutData = new FormData();
            toolbarFormLayoutData.top = new FormAttachment(1);
            toolbarFormLayoutData.left = new FormAttachment(sash);
            toolbarFormLayoutData.right = new FormAttachment(FULL);
            sideZoomBtnsForm.setLayoutData(toolbarFormLayoutData);
        }

        final FormData formRootLayoutData = new FormData();
        formRootLayoutData.top =
                zoomButtonsVisible ? new FormAttachment(sideZoomBtnsForm) : new FormAttachment(0);
        formRootLayoutData.bottom = new FormAttachment(FULL);
        formRootLayoutData.left = new FormAttachment(sash);
        formRootLayoutData.right = new FormAttachment(FULL);
        scrolledRootForm.setLayoutData(formRootLayoutData);

        final FormData acionsFormLayoutData = new FormData();
        acionsFormLayoutData.top = new FormAttachment(0);
        acionsFormLayoutData.left = new FormAttachment(0);
        acionsFormLayoutData.right = new FormAttachment(FULL);
        actionsForm.setLayoutData(acionsFormLayoutData);

        final FormData synthesisOptionsFormLayoutData = new FormData();
        synthesisOptionsFormLayoutData.top = new FormAttachment(
                actionsForm, SYNTHESIS_LAYOUT_OPTIONS_SPACE);
        synthesisOptionsFormLayoutData.left = new FormAttachment(0);
        synthesisOptionsFormLayoutData.right = new FormAttachment(FULL);
        synthesisOptionsForm.setLayoutData(synthesisOptionsFormLayoutData);

        final FormData layoutOptionsFormLayoutData = new FormData();
        layoutOptionsFormLayoutData.top = new FormAttachment(
                synthesisOptionsForm, SYNTHESIS_LAYOUT_OPTIONS_SPACE);
        layoutOptionsFormLayoutData.bottom = new FormAttachment(FULL);
        layoutOptionsFormLayoutData.left = new FormAttachment(0);
        layoutOptionsFormLayoutData.right = new FormAttachment(FULL);
        layoutOptionsForm.setLayoutData(layoutOptionsFormLayoutData);

        // register the sash moving handler for resizing the options pane
        sash.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                updateZoomButtons(true);
                final int maxDiagSize = sideBarParent.getClientArea().width - MINIMAL_OPTIONS_FORM_WIDTH;
                if (maxDiagSize > event.x) {
                    sashLayoutData.left.numerator = FULL;
                    sashLayoutData.left.offset = -(sideBarParent.getClientArea().width - event.x);
                } else {
                    sashLayoutData.left.numerator = FULL;
                    sashLayoutData.left.offset = -MINIMAL_OPTIONS_FORM_WIDTH;
                    // The following line appears to be evil, but this is required
                    //  to let the sash respect the limit correctly.
                    event.x = maxDiagSize;
                }
                horizontalPos = sashLayoutData.left.offset;
                arrowLabelContainerLayout.topControl = rightArrowLabel;
                sideBarParent.layout(true, true);
            }
        });

        // a "pseudo" field ;-)
        // the initialization with #INITIAL_OPTIONS_FORM_WIDTH is required for proper expansion
        //  in case the side bar is initially to be collapsed
        final int[] lastXpos = new int[] { INITIAL_OPTIONS_FORM_WIDTH };

        // register actions for the collapse / expand labels
        rightArrowLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(final MouseEvent event) {
                updateZoomButtons(false);
                sashLayoutData.left.numerator = FULL;
                sashLayoutData.left.offset = -sashLayoutData.width;
                lastXpos[0] = sideBarParent.getClientArea().width - sash.getBounds().x;
                horizontalPos = sashLayoutData.left.offset;
                arrowLabelContainerLayout.topControl = leftArrowLabel;
                sideBarParent.layout(true, true);
            }
        });

        leftArrowLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(final MouseEvent event) {
                updateZoomButtons(true);
                sashLayoutData.left.numerator = FULL;
                sashLayoutData.left.offset = -lastXpos[0];
                horizontalPos = sashLayoutData.left.offset;
                arrowLabelContainerLayout.topControl = rightArrowLabel;
                sideBarParent.layout(true, true);
            }
        });

        return this;
    }

    /** Constant definition of the key used for referring to buttons' zoom style. */
    private static final String BUTTON_TYPE = "type";

    /**
     * Initialize the toolbar, that is create the three buttons and register selection listeners.
     *
     * @param parent
     *            the {@link Composite} to which the buttons are being added
     */
    private void initializeZoomButtons(final Composite parent) {
        if (!zoomButtonsVisible) {
            return;
        }


        parent.setLayout(new RowLayout());
        final Button zoomToFitBtn = new Button(parent, SWT.TOGGLE | SWT.FLAT);
        final Button zoomToFocusBtn = new Button(parent, SWT.TOGGLE | SWT.FLAT);
        final Button zoomToOneBtn = new Button(parent, SWT.PUSH | SWT.FLAT);

        // Zoom to Fit
        final Image zoomToFitImage = KlighdUIPlugin
                .getImageDescriptorFromKlighdBase("icons/kieler-zoomtofit.gif").createImage();
        resources.add(zoomToFitImage);
        zoomToFitBtn.setImage(zoomToFitImage);
        zoomToFitBtn.setToolTipText("Toggle Zoom to Fit");

        // set extra data to identify the buttons while updating later
        zoomToFitBtn.setData(BUTTON_TYPE, ZoomStyle.ZOOM_TO_FIT);
        zoomToFitBtn.setSelection(viewContext.isZoomToFit());
        zoomToFitBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                viewContext.setZoomStyle(
                        ZoomStyle.create(false, zoomToFitBtn.getSelection(), false, false, false));

                // perform zoom to fit upon activation of the toggle button
                if (zoomToFitBtn.getSelection()) {
                    LightDiagramServices.zoomDiagram(viewContext);
                    // uncheck the zoom to focus button
                    zoomToFocusBtn.setSelection(false);
                }
            }
        });

        // Zoom to focus
        final Image zoomToFocusImage = KlighdUIPlugin
                .getImageDescriptorFromKlighdBase("icons/kieler-zoomtofocus.gif").createImage();
        resources.add(zoomToFocusImage);
        zoomToFocusBtn.setImage(zoomToFocusImage);
        zoomToFocusBtn.setToolTipText("Toggle Zoom to Focus");
        // set extra data to identify the buttons while updating later
        zoomToFocusBtn.setData(BUTTON_TYPE, ZoomStyle.getZoomToFocusStyle());
        zoomToFocusBtn.setSelection(viewContext.isZoomToFocus());
        zoomToFocusBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                viewContext.setZoomStyle(
                        ZoomStyle.create(false, false, false, zoomToFocusBtn.getSelection(), false));

                // perform zoom to focus upon activation of the toggle button
                if (zoomToFocusBtn.getSelection()) {
                    LightDiagramServices.zoomDiagram(viewContext);
                    // uncheck the zoom to fit button
                    zoomToFitBtn.setSelection(false);
                }
            }
        });

        // Zoom to actual size (1:1)
        final Image zoomToOneImage = KlighdUIPlugin
                .getImageDescriptorFromKlighdBase("icons/kieler-zoomtoone.gif").createImage();
        resources.add(zoomToOneImage);
        zoomToOneBtn.setImage(zoomToOneImage);
        zoomToOneBtn.setToolTipText("Zoom to Original Size");
        zoomToOneBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                viewContext.getViewer().zoomToLevel(1, KlighdConstants.DEFAULT_ANIMATION_TIME);
            }
        });

        final int maxWidth =
                Math.max(zoomToFitBtn.getBounds().width,
                        Math.max(zoomToFocusBtn.getBounds().width, zoomToOneBtn.getBounds().width));
        final int maxHeight =
                Math.max(zoomToFitBtn.getBounds().height,
                        Math.max(zoomToFocusBtn.getBounds().height, zoomToOneBtn.getBounds().height));
        zoomToFitBtn.setSize(maxWidth, maxHeight);
        zoomToFocusBtn.setSize(maxWidth, maxHeight);
        zoomToOneBtn.setSize(maxWidth, maxHeight);
    }

    /**
     * A simple paint listener that draws a vertical line.
     */
    private final class LinePainter implements PaintListener {
        public void paintControl(final PaintEvent event) {
            final Point size = ((Control) event.widget).getSize();
            event.gc.setForeground(Display.getCurrent().getSystemColor(
                    SWT.COLOR_WIDGET_NORMAL_SHADOW));
            event.gc.drawLine(event.width / 2, 0, event.width / 2, size.y);
        }
    }

    /**
     * Update the options to be displayed in the options pane.
     *
     * @param diagramComposite
     *            the {@link Composite} the diagram canvas is contained in
     * @param theViewContext
     *            the current {@link ViewContext}
     * @param fitSpace
     *            true if the diagram shall fit the available space
     */
    public void updateOptions(final Composite diagramComposite, final ViewContext theViewContext,
            final boolean fitSpace) {

        viewContext = theViewContext;

        if (diagramComposite.isDisposed()) {
            return;
        }

        // remove any option controls that have been created before
        actionControlFactory.clear();

        boolean actionsAvailable = false;

        LinkedHashSet<DisplayedActionData> allActionData = new LinkedHashSet<DisplayedActionData>();
        allActionData.addAll(viewContext.getDisplayedActions());
        if (!viewContext.getChildViewContexts(false).isEmpty()) {
            // Collect all action from child view contexts
            for (ViewContext childVC : viewContext.getChildViewContexts(true)) {
                allActionData.addAll(childVC.getDisplayedActions());
            }
        }
        for (final DisplayedActionData actionData : allActionData) {
            actionControlFactory.createActionControl(actionData, theViewContext);
            actionsAvailable = true;
        }

        final IViewer viewer = viewContext.getViewer();
        if (actionsAvailable) {
            // register the actionsControlFactory as selection listener in the current context viewer
            //  multiple additions are harmless as internally a LinkedHashSet is used hold the listeners
            viewer.getContextViewer().addSelectionChangedListener(actionControlFactory);

            // register the actionsControlFactory as view change listener in the current (diagram) viewer
            //  multiple additions are harmless as internally a HashMultimap is used hold the listeners
            viewer.addViewChangeListener(actionControlFactory,
                    ViewChangeType.clipCollapseExpandHideShow());
        } else {
            viewer.getContextViewer().removeSelectionChangedListener(actionControlFactory);
            viewer.removeViewChangeListener(actionControlFactory);
        }

        // remove any option controls that have been created before
        synthesisOptionControlFactory.clear();

        boolean synthesisOptionsAvailable = false;
        
        // Create synthesis option sections for all used syntheses
        if (!viewContext.getChildViewContexts(false).isEmpty()) {
            LinkedHashSet<ISynthesis> allSyntheses = new LinkedHashSet<ISynthesis>();
            allSyntheses.add(viewContext.getDiagramSynthesis());
            // Collect all syntheses from child view contexts
            for (ViewContext childVC : viewContext.getChildViewContexts(true)) {
                allSyntheses.add(childVC.getDiagramSynthesis());
            }
            for (final ISynthesis synthesis : allSyntheses) {
                if (synthesis != null) {
                    // Create a new section for the synthesis options
                    SynthesisOptionControlFactory subFactory = synthesisOptionControlFactory
                            .createSubSynthesisOptionControlFactory(synthesis);
                    // Add options
                    for (final SynthesisOption option : synthesis
                            .getDisplayedSynthesisOptions()) {
                        synthesisOptionsAvailable |=
                                subFactory.createOptionControl(option, viewContext);
                    }
                }
            }
        } else {
            for (final SynthesisOption option : viewContext.getDisplayedSynthesisOptions()) {
                synthesisOptionsAvailable |=
                        synthesisOptionControlFactory.createOptionControl(option, viewContext);
            }
        }

        // remove any option controls that have been created before
        layoutOptionControlFactory.clear();

        // initialize a layout configuration for retrieving default values
        layoutOptionControlFactory.initialize(viewContext);

        boolean layoutOptionsAvailable = false;

        for (final Pair<IProperty<?>, List<?>> pair : viewContext.getDisplayedLayoutOptions()) {
            final Object first;
            final Object second;
            if (pair.getSecond() instanceof Collection) {
                final Iterator<?> it = ((Collection<?>) pair.getSecond()).iterator();
                first = it.hasNext() ? it.next() : null;
                second = it.hasNext() ? it.next() : null;
            } else {
                first = null;
                second = null;
            }

            if (first instanceof Number && second instanceof Number) {
                layoutOptionControlFactory.createControl(pair.getFirst().getId(),
                        ((Number) first).floatValue(), ((Number) second).floatValue());
                layoutOptionsAvailable = true;
            } else if (pair.getSecond() == null) {
                layoutOptionControlFactory.createControl(pair.getFirst().getId());
                layoutOptionsAvailable = true;
            } else {
                layoutOptionControlFactory.createControl(pair.getFirst().getId(),
                        pair.getSecond());
                layoutOptionsAvailable = true;
            }
        }

        final boolean sideBarEnabled = this.enableOptionsSideBar(fitSpace,
                actionsAvailable, synthesisOptionsAvailable, layoutOptionsAvailable);

        updateZoomButtons(sideBarEnabled);

        final IDiagramWorkbenchPart part = viewContext.getDiagramWorkbenchPart();
        if (part instanceof DiagramViewPart) {
            final IAction action = ((DiagramViewPart) part).getAction(
                    DiagramViewPart.ACTION_ID_RESET_LAYOUT_OPTIONS);
            if (action != null) {
                action.setEnabled(layoutOptionsAvailable);
            }
        }
    }

    /**
     * Update the zoom buttons. In case the canvas-buttons are not yet initialized but need to be
     * shown, initialize them (see {@link DiagramSideBar#initializeZoomButtons(Composite)}).
     */
    private void updateZoomButtons(final boolean sideBarEnabled) {
        if (!zoomButtonsVisible) {
            return;
        }

        // Set the visibility only if the canvasToolbar is already initialized.
        if (canvasZoomBtnsContainer != null) {
            canvasZoomBtnsContainer.setVisible(!sideBarEnabled);
        }

        if (sideBarEnabled) {
            // side bar is expanded: simply update the selections status of the buttons.
            updateZoomButtonSelection(sideZoomBtnsForm.getBody());

            return;
        }

        // side bar is hidden: if required, initialize the canvas buttons.
        if (viewContext.getViewer().getControl() instanceof Composite
                && canvasZoomBtnsContainer == null) {

            final Composite canvas = (Composite) viewContext.getViewer().getControl();
            canvas.addDisposeListener(new DisposeListener() {

                public void widgetDisposed(final DisposeEvent e) {
                    // Whenever a new model is shown in the diagram view part, i.e. the view part
                    // is re-initialized via DiagramViewPart.initialize(), the employed viewer
                    //  and its canvas (KlighdCanvas) along with its child 'canvasZoomBtnsContainer'
                    //  (if initialized) will be disposed (see ContextViewer.setModel()).
                    // Therefore we need to re-create a new container when the buttons are updated,
                    //  which is indicated by 'canvasZoomBtnsContainer == null'.
                    canvasZoomBtnsContainer = null;
                }
            });

            // The container composite must not accept the focus if 'canvas.setFocus()' is called.
            // Otherwise 'SWT.KeyDown' and 'SWT.KeyUp' will be forwarded to that composite rather than
            //  the canvas and the magnifier glass and other key-based features won't work anymore.
            canvasZoomBtnsContainer = new Composite(canvas, SWT.NO_FOCUS);
            final Color white = new Color(Display.getCurrent(), KlighdConstants.WHITE);
            resources.add(white);
            canvasZoomBtnsContainer.setBackground(white);

            initializeZoomButtons(canvasZoomBtnsContainer);
            // Pack the Composite's children so a size is calculated.
            canvasZoomBtnsContainer.pack();

            // Take care of the positioning of the canvasToolbar because the KlightCanvas
            // has no Layout set.

            final Point canvasSize = canvas.getSize();
            final Point buttonBarSize = canvasZoomBtnsContainer.getSize();

            // Initial Positioning
            canvasZoomBtnsContainer.setLocation(canvasSize.x - buttonBarSize.x
                    - CANVAS_ZOOM_BTNS_OFFSET, CANVAS_ZOOM_BTNS_OFFSET);

            // Position the Toolbar whenever the view is resized.
            canvas.addListener(SWT.Resize, new Listener() {

                public void handleEvent(final Event event) {
                    final Point canvasSize = canvas.getSize();
                    final Point buttonBarSize = canvasZoomBtnsContainer.getSize();

                    canvasZoomBtnsContainer.setLocation(canvasSize.x - buttonBarSize.x
                            - CANVAS_ZOOM_BTNS_OFFSET, CANVAS_ZOOM_BTNS_OFFSET);
                }
            });
        }

        // finally update the selection status
        updateZoomButtonSelection(canvasZoomBtnsContainer);
    }

    /**
     * Update the selection-status of the zoom buttons.
     *
     * @param container
     *            the {@link Composite} the buttons are contained in
     */
    private void updateZoomButtonSelection(final Composite container) {
        for (final Control button : container.getChildren()) {
            final Object type = button.getData(BUTTON_TYPE);
            if (ZoomStyle.ZOOM_TO_FIT == type) {
                ((Button) button).setSelection(viewContext.isZoomToFit());
            } else if (ZoomStyle.getZoomToFocusStyle() == type) {
                ((Button) button).setSelection(viewContext.isZoomToFocus());
            }
        }
    }

    /**
     * A simple enabler of the side bar controls. It is to be executed in case there are diagram
     * options to provide in the side bar.
     *
     * @param zoomToFit
     *            <code>true</code> if the diagram shall fit the available space
     * @param showActions
     *            <code>true</code> if the actions group should be displayed.
     * @param showSynthesisOptions
     *            <code>true</code> if the synthesis options group should be displayed.
     * @param showLayoutOptions
     *            <code>true</code> if the layout options group should be displayed.
     */
    private boolean enableOptionsSideBar(final boolean zoomToFit, final boolean showActions,
            final boolean showSynthesisOptions, final boolean showLayoutOptions) {
        final boolean enabled;

        if (showActions || showSynthesisOptions || showLayoutOptions) {
            // define the controls (sash, right arrow, form) to be visible
            for (final Control c : this.sideBarControls) {
                if (c == actionsForm) {
                    c.setVisible(showActions);
                } else if (c == synthesisOptionsForm) {
                    c.setVisible(showSynthesisOptions);
                } else if (c == layoutOptionsForm) {
                    c.setVisible(showLayoutOptions);
                } else {
                    c.setVisible(true);
                }
            }

            if (showSynthesisOptions) {
                if (showActions) {
                    // in case actions and diagram options are available ...
                    ((FormData) synthesisOptionsForm.getLayoutData()).top =
                            new FormAttachment(actionsForm, SYNTHESIS_LAYOUT_OPTIONS_SPACE);
                } else {
                    // in case no actions are available
                    //  put the diagram options form at the top
                    ((FormData) synthesisOptionsForm.getLayoutData()).top = new FormAttachment(0);
                }

                // put the layout options form below the diagram options
                ((FormData) layoutOptionsForm.getLayoutData()).top =
                        new FormAttachment(synthesisOptionsForm, SYNTHESIS_LAYOUT_OPTIONS_SPACE);
            } else {
                if (showActions) {
                    // in case no diagram options but actions and layout options are available ...
                    ((FormData) layoutOptionsForm.getLayoutData()).top =
                            new FormAttachment(actionsForm, SYNTHESIS_LAYOUT_OPTIONS_SPACE);
                } else {
                    // in case only layout options are available
                    //  put the layout options form at the top
                    ((FormData) layoutOptionsForm.getLayoutData()).top = new FormAttachment(0);
                }
            }

            if (this.sashLayoutData != null) {
                this.sashLayoutData.left.numerator = FULL;
                this.sashLayoutData.left.offset = horizontalPos;
            }

            enabled = true;

        } else {
            for (final Control c : this.sideBarControls) {
                c.setVisible(false);
            }
            if (this.sashLayoutData != null) {
                if (this.sashLayoutData.left.offset != 0) {
                    // The horizontal position of the side bar's sash should only be saved
                    // if it is visible in current view.
                    // It' propose is to avoid the re-initialization of the side bar's size
                    // in case a different model has been assigned to the current view.
                    // But if a sequence of models is displayed where only some of them have a sidebar
                    // The problem appears that horizontalPos may be set to 0 which permanently
                    // hides the sidebar and thus this zero check is needed.
                    this.horizontalPos = this.sashLayoutData.left.offset;
                }
                this.sashLayoutData.left.numerator = FULL;
                this.sashLayoutData.left.offset = 0;
            }

            enabled = false;
        }

        // re-layout the view part's composite
        if (!this.sideBarParent.isDisposed()) {
            scrolledRootForm.reflow(true); // Fixes KIPRA-1890
            this.sideBarParent.layout(true, true);
        }

        // let the diagram fit the available space,
        if (zoomToFit) {
            viewContext.getViewer().zoom(ZoomStyle.ZOOM_TO_FIT, 0);
        }

        return enabled;
    }

    /**
     * Resets the layout option settings configured via the sidebar's controls.
     *
     * @param doLayout
     *            if <code>true</code> a subsequent layout run will be triggered
     */
    public void resetLayoutOptionsToDefaults(final boolean doLayout) {
        this.layoutOptionControlFactory.resetToDefaults(doLayout);
    }

    /**
     * Disposes the {@link Resource Resources}.
     */
    public void dispose() {
        if (optionsformToolkit != null) {
            optionsformToolkit.dispose();
            optionsformToolkit = null;
        }
        for (final Resource res : resources) {
            res.dispose();
        }
        resources.clear();

        viewContext = null;
        layoutOptionControlFactory = null;
        synthesisOptionControlFactory = null;
    }
}
