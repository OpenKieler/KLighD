// SUPPRESS CHECKSTYLE NEXT Header
/******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 ****************************************************************************/
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
package de.cau.cs.kieler.klighd.ui.printing.dialog;

import java.awt.geom.Dimension2D;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;

import de.cau.cs.kieler.klighd.ui.printing.DiagramPrintOptions;
import de.cau.cs.kieler.klighd.ui.printing.KlighdUIPrintingMessages;
import de.cau.cs.kieler.klighd.ui.printing.PrintExporter;
import de.cau.cs.kieler.klighd.ui.printing.PrintOptions;

/**
 * A section of the KlighD print dialog that adds scaling support.
 *
 * @author Christian Damus (cdamus)
 * @author James Bruck (jbruck)
 * @author csp
 * @author chsch
 */
final class ScalingBlock {

    /**
     * Instantiates a new scaling block. The bindings are used to bind observable GUI elements to
     * print setting in the given options.
     *
     * @param bindings
     *            the bindings used for observables
     * @param options
     *            the current print options
     */
    private ScalingBlock() {
    }

    private static final int MAX_PAGES = 100;
    private static final int BUTTONS_GROUP_COLUMNS = 3;
    private static final int PAGES_GROUP_COLUMNS = 5;
    private static final int SCALING_GROUP_COLUMNS = 4;

    /**
     * Creates the 'Scaling' block contents. The bindings are used to bind observable GUI elements
     * to print setting in the given options.
     *
     * @param parent
     *            the parent {@link Composite} to use
     * @param bindings
     *            the bindings used for observables
     * @param options
     *            the current print options
     * @return the created {@link Group}
     */
    public static Group createContents(final Composite parent, final DataBindingContext bindings,
            final PrintOptions options) {

        // create the scaling group
        final Group result = DialogUtil.group(parent, KlighdUIPrintingMessages.PrintDialog_Scaling);
        DialogUtil.layout(result, 1);

        // This group contains three composites to be independent from the gridlayout's columns in
        // each row.

        // Group containing 3 buttons:
        // - scale to 100%
        // - scale to fit pages
        // - adjust number of pages to fit diagram
        final Composite buttonsGroup = new Composite(result, SWT.NONE);
        DialogUtil.layoutFillHorizontal(buttonsGroup, true);
        buttonsGroup.setLayout(new GridLayout(BUTTONS_GROUP_COLUMNS, false));

        final Button oneToOneBtn =
                DialogUtil.button(buttonsGroup, KlighdUIPrintingMessages.PrintDialog_Scaling_to100);

        final Button fitToPagesBtn = DialogUtil.button(buttonsGroup,
                KlighdUIPrintingMessages.PrintDialog_Scaling_fitPages);

        final Button adjustPagesBtn = DialogUtil.button(buttonsGroup,
                KlighdUIPrintingMessages.PrintDialog_Scaling_adjustPages);

        // Group containing a spinner and some text labels for scale settings.
        final Composite scalingGroup = new Composite(result, SWT.NONE);
        DialogUtil.layoutFillHorizontal(scalingGroup, true);
        scalingGroup.setLayout(new GridLayout(SCALING_GROUP_COLUMNS, false));

        DialogUtil.label(scalingGroup, KlighdUIPrintingMessages.PrintDialog_Scaling_lbl_scaleTo);
        final Spinner scaleSpinner = DialogUtil.spinner(scalingGroup, 1, Integer.MAX_VALUE);
        DialogUtil.layoutFillHorizontal(scaleSpinner, true);
        DialogUtil.label(scalingGroup, KlighdUIPrintingMessages.PrintDialog_Scaling_lbl_percent);

        // Group containing two spinners (and describing labels)
        // to set the number of pages tall and wide to print on.
        final Composite pagesGroup = new Composite(result, SWT.NONE);
        DialogUtil.layoutFillHorizontal(pagesGroup, true);
        pagesGroup.setLayout(new GridLayout(PAGES_GROUP_COLUMNS, false));

        DialogUtil.label(pagesGroup, KlighdUIPrintingMessages.PrintDialog_Scaling_lbl_printTo);

        final Spinner spinnerWide = DialogUtil.spinner(pagesGroup, 1, MAX_PAGES);
        DialogUtil.layoutWidth(spinnerWide, spinnerWide.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);

        DialogUtil.label(pagesGroup, KlighdUIPrintingMessages.PrintDialog_Scaling_lbl_pagesWide);

        final Spinner spinnerTall = DialogUtil.spinner(pagesGroup, 1, MAX_PAGES);
        DialogUtil.layoutWidth(spinnerTall, spinnerTall.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);

        DialogUtil.label(pagesGroup, KlighdUIPrintingMessages.PrintDialog_Scaling_lbl_pagesTall);

        final DiagramPrintOptions dOptions;
        if (options instanceof DiagramPrintOptions) {
            dOptions = (DiagramPrintOptions) options;

            oneToOneBtn.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(final SelectionEvent e) {
                    scaleOneToOne(dOptions);
                }
            });

            fitToPagesBtn.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(final SelectionEvent e) {
                    fitToPages(dOptions);
                }
            });

            adjustPagesBtn.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(final SelectionEvent e) {
                    // Calculate for both horizontal and vertical directions
                    // how many pages are necessary to fit the diagram in.

                    final PrintExporter exporter = dOptions.getExporter();
                    final Dimension2D trimmedPrinterBounds =
                            exporter.getTrimmedTileBounds(dOptions);
                    final Dimension2D diagramBounds = exporter.getDiagramBoundsIncludingTrim();

                    dOptions.setPagesWide((int) Math.ceil(diagramBounds.getWidth()
                            * dOptions.getScaleFactor() / trimmedPrinterBounds.getWidth()));

                    dOptions.setPagesTall((int) Math.ceil(diagramBounds.getHeight()
                            * dOptions.getScaleFactor() / trimmedPrinterBounds.getHeight()));
                }
            });

            final Realm realm = bindings.getValidationRealm();

            final IObservableValue<Object> scalePercent =
                    BeanProperties.value(dOptions.getClass().asSubclass(DiagramPrintOptions.class),
                            PrintOptions.PROPERTY_SCALE_PERCENT).observe(realm, dOptions);
            // BeansObservables.observeValue(realm, dOptions, PrintOptions.PROPERTY_SCALE_PERCENT);
            ISWTObservableValue<Object> observerScaleSpinner = WidgetProperties.widgetSelection().observe(scaleSpinner); // SWTObservables.observeSelection(scaleSpinner);
            bindings.bindValue(observerScaleSpinner, scalePercent);

            final IObservableValue<Object> pagesWide = 
                    BeanProperties.value(dOptions.getClass().asSubclass(DiagramPrintOptions.class),
                            PrintOptions.PROPERTY_PAGES_WIDE).observe(realm, dOptions); 
            // BeansObservables.observeValue(realm, dOptions, PrintOptions.PROPERTY_PAGES_WIDE);
            ISWTObservableValue<Object> observerWideSpinner = WidgetProperties.widgetSelection().observe(spinnerWide); // SWTObservables.observeSelection(spinnerWide);
            bindings.bindValue(observerWideSpinner, pagesWide);

            final IObservableValue<Object> pagesTall = 
                    BeanProperties.value(dOptions.getClass().asSubclass(DiagramPrintOptions.class),
                            PrintOptions.PROPERTY_PAGES_TALL).observe(realm, dOptions); 
            // BeansObservables.observeValue(realm, dOptions, PrintOptions.PROPERTY_PAGES_TALL);
            ISWTObservableValue<Object> observerTallSpinner = WidgetProperties.widgetSelection().observe(spinnerTall); // SWTObservables.observeSelection(spinnerTall);
            bindings.bindValue(observerTallSpinner, pagesTall);

            result.addListener(SWT.Dispose, new Listener() {

                public void handleEvent(final Event event) {
                    // while the SWTObservableValues are disposed while disposing the corresponding
                    // widgets the Beans-based ones should be disposed explicitly
                    scalePercent.dispose();
                    pagesWide.dispose();
                    pagesTall.dispose();
                }
            });

        } else {

            // in case the 'options' field is null, i.e. this instance is not linked to an instance
            // of
            // 'DiagramPrintOptions',
            // deactivate all the controls as they do not make sense for printing non-diagram
            // content
            // (some customers asked for this feature in order to provide consistent print dialogs
            // for all kinds of printable content)

            result.setEnabled(false);
            for (final Control c : result.getChildren()) {
                c.setEnabled(false);
                if (c instanceof Composite) {
                    for (final Control d : ((Composite) c).getChildren()) {
                        d.setEnabled(false);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Calculates the needed scaling to fit the diagram to the number of pages, currently set.
     *
     * @param dOptions
     *            The {@link DiagramPrintOptions} which contain the current printer settings
     */
    public static void fitToPages(final DiagramPrintOptions dOptions) {
        // Calculate the minimum of necessary horizontal and vertical scale factors
        // required to fit the whole diagram on the selected amount of pages.

        final PrintExporter exporter = dOptions.getExporter();
        final Dimension2D diagramBounds = exporter.getDiagramBoundsIncludingTrim();
        final Dimension2D trimmedPrinterBounds = exporter.getTrimmedTileBounds(dOptions);

        final double scaleX = trimmedPrinterBounds.getWidth() * dOptions.getPagesWide()
                / diagramBounds.getWidth();

        final double scaleY = trimmedPrinterBounds.getHeight() * dOptions.getPagesTall()
                / diagramBounds.getHeight();

        dOptions.setScaleFactor(Math.min(scaleX, scaleY));
    }

    /**
     * Sets the current scaling to 100%.
     *
     * @param dOptions
     *            The {@link DiagramPrintOptions} which contain the current printer settings
     */
    public static void scaleOneToOne(final DiagramPrintOptions dOptions) {
        dOptions.setScaleFactor(1);
    }
}
