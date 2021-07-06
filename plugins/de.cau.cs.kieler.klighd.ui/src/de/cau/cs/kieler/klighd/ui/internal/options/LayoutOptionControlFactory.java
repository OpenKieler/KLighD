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
package de.cau.cs.kieler.klighd.ui.internal.options;

import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Pattern;

import org.eclipse.elk.core.LayoutConfigurator;
import org.eclipse.elk.core.data.LayoutMetaDataService;
import org.eclipse.elk.core.data.LayoutOptionData;
import org.eclipse.elk.core.math.ElkMath;
import org.eclipse.elk.core.service.ILayoutConfigurationStore;
import org.eclipse.elk.core.service.LayoutConfigurationManager;
import org.eclipse.elk.core.service.ui.EclipseLayoutConnectorsService;
import org.eclipse.elk.graph.ElkGraphElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.inject.Injector;

import de.cau.cs.kieler.klighd.LightDiagramLayoutConfig;
import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.eclipse.EclipseViewContext;
import de.cau.cs.kieler.klighd.eclipse.IDiagramWorkbenchPart;
import de.cau.cs.kieler.klighd.kgraph.KNode;

/**
 * A factory for controls for layout options.
 *
 * @author msp
 * @author chsch
 */
public class LayoutOptionControlFactory {

    /** The parent composite into which controls are created. */
    private Composite parent;

    private ViewContext viewContext;

    /** The workbench part containing the diagram viewer. */
    private IDiagramWorkbenchPart workbenchPart;
    /** the form toolkit used to create controls. */
    private FormToolkit formToolkit;
    /** The layout configurator that stores the values set by option controls. */
    private LayoutConfigurator layoutConfig;
    /** The layout configuration manager for retrieving initial values of controls. */
    private LayoutConfigurationManager layoutConfigManager;
    /** The layout configuration store for retrieving initial values of controls. */
    private ILayoutConfigurationStore layoutConfigStore;
    /** The set of controls to be disposed when {@link #clear()} is called. */
    private final Collection<Control> controls = new LinkedList<Control>();
    /** Whether the layout shall be refreshed automatically. */
    private boolean autoRefreshLayout = true;

    /** number of columns in the grid for enumeration value selection. */
    private static final int ENUM_GRID_COLS = 1;
    /** widget data identifier for the attached selection listener. */
    private static final String DATA_SELECTION_LISTENER = "klighd.selectionListener";

    /**
     * Create an option control factory.
     *
     * @param parent
     *            the parent container
     * @param formToolkit
     *            the form toolkit used to create controls
     */
    public LayoutOptionControlFactory(final Composite parent, final FormToolkit formToolkit) {
        this.parent = parent;
        this.formToolkit = formToolkit;

        // configure the parent's layout
        this.parent.setLayout(new GridLayout(1, false));
    }

    /**
     * Clear the current layout configuration and reinitialize option values. These values
     * are used when new controls are created. If any controls have been created before,
     * they should be removed first using {@link #clear()}.
     *
     * @param theViewContext
     *            the viewContext belonging to the current diagram
     */
    public void initialize(final ViewContext theViewContext) {
        layoutConfig = new LayoutConfigurator();
        
        this.viewContext = theViewContext;
        this.workbenchPart = ((EclipseViewContext) viewContext).getDiagramWorkbenchPart();

        final KNode viewModel = this.viewContext.getViewModel();
        
        // We need to obtain the LayoutConfigurationManager responsible for the view context to get
        // our hands at default option values later
        Injector injector = EclipseLayoutConnectorsService.getInstance().getInjector(null, theViewContext);
        layoutConfigManager = injector.getInstance(LayoutConfigurationManager.class);
        
        // We also need to get the configuration store for the top-level diagram element
        final ILayoutConfigurationStore.Provider layoutConfigStoreProvider =
                injector.getInstance(ILayoutConfigurationStore.Provider.class);
        layoutConfigStore = layoutConfigStoreProvider.get(workbenchPart, viewModel);
    }
    
    /**
     * Provides the {@link LayoutConfigurator} with layout settings from the controls created by this
     * factory. The returned config can change, so don't attempt to cache it.
     *
     * @return the employed {@link LayoutConfigurator}
     */
    public LayoutConfigurator getLayoutConfig() {
        return layoutConfig;
    }

    /**
     * Clear the previously created option controls.
     */
    public void clear() {
        for (final Control c : controls) {
            c.dispose();
        }
        controls.clear();
    }

    /**
     * Refresh the layout of the displayed diagram.
     *
     * @param animate whether the new layout shall be animated
     */
    private void refreshLayout(final boolean animate) {
        if (autoRefreshLayout) {
            new LightDiagramLayoutConfig(viewContext)
                    .animate(animate)
                    .performLayout();
        }
    }

    /**
     * Create a control for the given layout option identifier.
     *
     * @param optionId a layout option identifier
     */
    public void createControl(final String optionId) {
        final LayoutOptionData optionData = LayoutMetaDataService.getInstance().getOptionData(optionId);
        if (optionData != null) {
            createControl(optionData, null, null, null);
        }
    }

    /**
     * Create a control for the given layout option identifier with given bounds.
     *
     * @param optionId a layout option identifier
     * @param minValue the minimal value for the option
     * @param maxValue the maximal value for the option
     */
    public void createControl(final String optionId, final Float minValue, final Float maxValue) {
        final LayoutOptionData optionData = LayoutMetaDataService.getInstance().getOptionData(optionId);
        if (optionData != null) {
            createControl(optionData, minValue, maxValue, null);
        }
    }

    /**
     * Create a control for the given layout option identifier with given available option values.
     *
     * @param optionId a layout option identifier
     * @param availableValues the set of values to offer
     */
    public void createControl(final String optionId, final Collection<?> availableValues) {
        final LayoutOptionData optionData = LayoutMetaDataService.getInstance().getOptionData(optionId);
        if (optionData != null) {
            createControl(optionData, null, null, availableValues);
        }
    }

    /**
     * Reset all displayed layout options to their default values.
     *
     * @param doLayout
     *            if <code>true</code> a subsequent layout run will be triggered
     */
    @SuppressWarnings("incomplete-switch")
    public void resetToDefaults(final boolean doLayout) {
        // temporarily disable auto-refresh to avoid multiple layout runs triggered by listeners
        autoRefreshLayout = false;
        layoutConfig = new LayoutConfigurator();
        for (final Control control : controls) {
            if (control.getData() instanceof LayoutOptionData) {
                final LayoutOptionData optionData = (LayoutOptionData) control.getData();
                final Object defaultValue = layoutConfigManager.getOptionValue(optionData,
                        layoutConfigStore);

                switch (optionData.getType()) {
                case INT:
                case DOUBLE: {
                    final Scale slider = (Scale) control;
                    final SliderListener sliderListener = (SliderListener) control.getData(
                            DATA_SELECTION_LISTENER);
                    if (sliderListener != null) {
                        final double initialValue =
                                ElkMath.boundd(((Number) defaultValue).doubleValue(),
                                        sliderListener.minVal, sliderListener.maxVal);
                        final int selection =
                                (int) Math.round((initialValue - sliderListener.minVal)
                                / (sliderListener.maxVal - sliderListener.minVal)
                                * (slider.getMaximum() - slider.getMinimum()))
                                + slider.getMinimum();
                        slider.setSelection(selection);
                    }
                    break;
                }

                case BOOLEAN:
                case ENUM: {
                    // the composite's children store the available values in their 'Data' fields
                    final Composite composite = (Composite) control;
                    final Control selection = Iterators.find(Iterators.forArray(composite.getChildren()),
                            new Predicate<Control>() {
                        public boolean apply(final Control input) {
                            if (input.getData() != null) {
                                return input.getData().equals(defaultValue);
                            }
                            return false;
                        }
                    }, null);
                    for (final Control c : composite.getChildren()) {
                        ((Button) c).setSelection(c == selection);
                    }
                    break;
                }

                }
            }
        }
        autoRefreshLayout = true;

        if (doLayout) {
            refreshLayout(true);
        }
    }

    /**
     * Create a control for the given layout option data instance with given bounds.
     *
     * @param optionData a layout option data instance
     * @param minValue the minimal value for the option, or {@code null}
     * @param maxValue the maximal value for the option, or {@code null}
     * @param availableValues the set of values to offer, or {@code null}
     */
    private void createControl(final LayoutOptionData optionData, final Float minValue,
            final Float maxValue, final Collection<?> availableValues) {
        
        final Label label = formToolkit.createLabel(parent, optionData.getName() + ":");
        label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        controls.add(label);

        switch (optionData.getType()) {
        case INT:
        case DOUBLE: {
            final Scale slider = new Scale(parent, SWT.NONE);
            // the following setting is needed on windows
            slider.setBackground(parent.getBackground());
            slider.setToolTipText(optionData.getDescription());
            final SliderListener sliderListener = new SliderListener(label, optionData,
                    getMinValue(optionData, minValue), getMaxValue(optionData, maxValue));
            final GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false);
            slider.setLayoutData(gridData);
            slider.setData(optionData);
            controls.add(slider);
            // set initial value for the slider
            double initialValue = ((Number) layoutConfigManager.getOptionValue(optionData,
                    layoutConfigStore)).doubleValue();
            initialValue = ElkMath.boundd(initialValue, sliderListener.minVal,
                    sliderListener.maxVal);

            label.setText(optionData.getName() + ": " + initialValue);

            final int selection = (int) Math.round((initialValue - sliderListener.minVal)
                    / (sliderListener.maxVal - sliderListener.minVal)
                    * (slider.getMaximum() - slider.getMinimum())) + slider.getMinimum();
            slider.setSelection(selection);

            // add selection listener for instant layout updates
            slider.addSelectionListener(sliderListener);
            slider.setData(DATA_SELECTION_LISTENER, sliderListener);
            break;
        }

        case BOOLEAN: {
            final Composite valuesContainer = formToolkit.createComposite(parent);
            valuesContainer.setLayout(new GridLayout(2, false));
            final Button trueButton = formToolkit.createButton(valuesContainer, "True", SWT.RADIO);
            trueButton.setToolTipText(optionData.getDescription());
            trueButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
            trueButton.setData(true);
            final Button falseButton = formToolkit.createButton(valuesContainer, "False", SWT.RADIO);
            falseButton.setToolTipText(optionData.getDescription());
            falseButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
            falseButton.setData(false);
            valuesContainer.setData(optionData);
            controls.add(valuesContainer);
            // set initial value for the radio buttons
            if ((Boolean) layoutConfigManager.getOptionValue(optionData, layoutConfigStore)) {
                trueButton.setSelection(true);
            } else {
                falseButton.setSelection(true);
            }
            // add selection listeners for instant layout updates
            trueButton.addSelectionListener(new EnumerationListener(optionData, Boolean.TRUE));
            falseButton.addSelectionListener(new EnumerationListener(optionData, Boolean.FALSE));
            break;
        }

        case ENUM: {
            final Composite valuesContainer = formToolkit.createComposite(parent);
            valuesContainer.setLayout(new GridLayout(ENUM_GRID_COLS, false));
            Object[] values;
            if (availableValues != null) {
                values = availableValues.toArray();
            } else {
                @SuppressWarnings({ "rawtypes", "unchecked" })
                final Class<Enum> clazz = (Class<Enum>) optionData.getOptionClass();
                values = clazz.getEnumConstants();
            }
            final Object initialValue = layoutConfigManager.getOptionValue(optionData,
                    layoutConfigStore);
            for (final Object value : values) {
                final Button button = formToolkit.createButton(valuesContainer, getUserValue(value),
                        SWT.RADIO);
                button.setToolTipText(optionData.getDescription());
                button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
                button.setData(value);
                if (value.equals(initialValue)) {
                    button.setSelection(true);
                }
                button.addSelectionListener(new EnumerationListener(optionData, value));
            }
            valuesContainer.setData(optionData);
            controls.add(valuesContainer);
            break;
        }

        case ENUMSET: {
            final Composite valuesContainer = formToolkit.createComposite(parent);
            valuesContainer.setLayout(new GridLayout(ENUM_GRID_COLS, false));

            final Object[] values;
            if (availableValues != null) {
                values = availableValues.toArray();

            } else {
                final Label errorLabel = new Label(parent, SWT.NONE);
                errorLabel.setText("This option type requires pre-defined values.");
                errorLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
                controls.add(errorLabel);
                break;
            }

            final Object initialValue = layoutConfigManager.getOptionValue(optionData,
                    layoutConfigStore);
            for (final Object value : values) {
                final Button button = formToolkit.createButton(valuesContainer, getUserValue(value),
                        SWT.RADIO);
                button.setToolTipText(optionData.getDescription());
                button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
                button.setData(value);
                if (value.equals(initialValue)) {
                    button.setSelection(true);
                }
                button.addSelectionListener(new EnumerationListener(optionData, value));
            }
            valuesContainer.setData(optionData);
            controls.add(valuesContainer);
            break;
        }

        default:
            final Label errorLabel = new Label(parent, SWT.NONE);
            errorLabel.setText("This option type is not supported");
            errorLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
            controls.add(errorLabel);
        }
    }

    /** The default value for the lower bound. */
    private static final int DEFAULT_MIN = 0;

    /**
     * Get a lower bound for values of the given option.
     *
     * @param optionData a layout option data instance
     * @param requested the requested bound, or {@code null}
     * @return a minimal value
     */
    private static float getMinValue(final LayoutOptionData optionData, final Float requested) {
        if (requested != null) {
            return requested;
        }
        final Object lowerBound = optionData.getLowerBound();
        if (lowerBound instanceof Number) {
            return ((Number) lowerBound).floatValue();
        }
        return DEFAULT_MIN;
    }

    /** The default value for the upper bound. */
    private static final int DEFAULT_MAX = 100;

    /**
     * Get an upper bound for values of the given option.
     *
     * @param optionData a layout option data instance
     * @param requested the requested bound, or {@code null}
     * @return a maximal value
     */
    private static float getMaxValue(final LayoutOptionData optionData, final Float requested) {
        if (requested != null) {
            return requested;
        }
        final Object upperBound = optionData.getUpperBound();
        if (upperBound instanceof Number) {
            return ((Number) upperBound).floatValue();
        }
        return DEFAULT_MAX;
    }

    /**
     * Get a user-friendly value for the given object.
     *
     * @param object an object, e.g. an enumeration value
     * @return a user-friendly string to display in the UI
     */
    private static String getUserValue(final Object object) {
        final String string = object.toString();
        final StringBuilder builder = new StringBuilder(string.length());
        boolean capital = true;
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == '_') {
                builder.append(' ');
                capital = true;
            } else if (capital) {
                builder.append(Character.toUpperCase(string.charAt(i)));
                capital = false;
            } else {
                builder.append(Character.toLowerCase(string.charAt(i)));
            }
        }
        return builder.toString();
    }

    private static final Pattern MAX_2_DIGITS = Pattern.compile("(.*\\.\\d[^0]?)\\d*");
    private static final String FIRST_GROUP = "$1";

    /**
     * A listener for sliders.
     */
    private class SliderListener extends SelectionAdapter {

        /** the corresponding name label used updating the selected value. */
        private final Label correspondingLabel;
        /** the layout option that is affected by the slider. */
        private final LayoutOptionData optionData;
        /** the maximal value. */
        private final double minVal;
        /** the minimal value. */
        private final double maxVal;

        /**
         * Create a slider listener.
         * @param textLabel the corresponding name label used updating the selected value
         * @param optionData the layout option that is affected by the slider
         * @param minVal the maximal value
         * @param maxVal the minimal value
         */
        SliderListener(final Label textLabel, final LayoutOptionData optionData,
                final double minVal, final double maxVal) {
            this.correspondingLabel = textLabel;
            this.optionData = optionData;
            this.minVal = minVal;
            if (maxVal <= minVal) {
                this.maxVal = minVal + 1;
            } else {
                this.maxVal = maxVal;
            }
        }

        @Override
        public void widgetSelected(final SelectionEvent event) {
            final Scale slider = (Scale) event.widget;
            final double sliderValue = (float) (slider.getSelection() - slider.getMinimum())
                    / (slider.getMaximum() - slider.getMinimum());
            final double optionValue = minVal + sliderValue * (maxVal - minVal);
            setOptionValue(optionValue);
            correspondingLabel.setText(optionData.getName() + ": "
                    + MAX_2_DIGITS.matcher(String.valueOf(optionValue)).replaceFirst(FIRST_GROUP));
            correspondingLabel.getParent().layout(true);

            // trigger a new layout on the displayed diagram
            refreshLayout(false);
        }

        /**
         * Set the new value of the layout option.
         *
         * @param optionValue the layout option value
         */
        @SuppressWarnings({ "incomplete-switch" })
        public void setOptionValue(final double optionValue) {
            switch (optionData.getType()) {
            case INT:
                layoutConfig.configure(ElkGraphElement.class).setProperty(
                        optionData, (int) optionValue);
                break;
            case DOUBLE:
                layoutConfig.configure(ElkGraphElement.class).setProperty(
                        optionData, optionValue);
                break;
            }
        }
    }

    /**
     * A listener for enumeration values.
     */
    private class EnumerationListener extends SelectionAdapter {

        /** the layout option that is affected by the button. */
        private LayoutOptionData optionData;
        /** the enumeration value to set when selection is triggered. */
        private Object value;

        /**
         * Creates an enumeration listener.
         *
         * @param optionData the layout option that is affected by the button
         * @param value the enumeration value to set when selection is triggered
         */
        EnumerationListener(final LayoutOptionData optionData, final Object value) {
            this.optionData = optionData;
            this.value = value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void widgetSelected(final SelectionEvent event) {
            if (((Button) event.widget).getSelection()) {
                layoutConfig.configure(ElkGraphElement.class).setProperty(optionData, value);
                refreshLayout(true);
            }
        }
    }

}
