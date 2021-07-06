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

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Widget;

import de.cau.cs.kieler.klighd.Klighd;
import de.cau.cs.kieler.klighd.ui.KlighdUIPlugin;
import de.cau.cs.kieler.klighd.ui.printing.KlighdUIPrintingMessages;
import de.cau.cs.kieler.klighd.ui.printing.PrintOptions;

/**
 * A section of the KlighD print dialog that handles the number of copies of a diagram to print.
 *
 * @author Christian Damus (cdamus)
 * @author James Bruck (jbruck)
 * @author csp
 * @author chsch
 */
final class CopiesBlock {

    /** Image for collate on. */
    private static final ImageDescriptor COLLATE_ON = KlighdUIPlugin.getImageDescriptor(
            "icons/printing/collate.gif"); //$NON-NLS-1$
    /** Image for collate off. */
    public static final ImageDescriptor COLLATE_OFF = KlighdUIPlugin.getImageDescriptor(
            "icons/printing/no_collate.gif"); //$NON-NLS-1$

    /**
     * Hidden standard constructor.
     */
    private CopiesBlock() {

    }

    /**
     * Creates the 'Copies' block contents.
     * The bindings are used to bind observable GUI elements to print setting in the given options.
     *
     * @param parent
     *            the parent {@link Composite} to use
     * @param bindings
     *            the {@link DataBindingContext} managing the employed
     *            {@link org.eclipse.core.databinding.Binding Bindings}
     * @param options
     *            the {@link PrintOptions} to be used
     * @return the created {@link Group}
     */
    public static Group createContents(final Composite parent, final DataBindingContext bindings,
            final PrintOptions options) {
        final int columns = 2;

        final Realm realm = bindings.getValidationRealm();

        // create group
        final Group result =
                DialogUtil.group(parent, KlighdUIPrintingMessages.PrintDialog_Copies);
        DialogUtil.layout(result, columns);

        // number of copies (label & spinner)
        DialogUtil.label(result, KlighdUIPrintingMessages.PrintDialog_NumberOfCopies);
        final Spinner copiesSpinner = DialogUtil.spinner(result, 1, Integer.MAX_VALUE);

        final IObservableValue<Object> copiesValue = 
                BeanProperties.value(options.getClass().asSubclass(PrintOptions.class), PrintOptions.PROPERTY_COPIES).observe(realm, options);
                // BeansObservables.observeValue(realm, options, PrintOptions.PROPERTY_COPIES);
        ISWTObservableValue<Object> copiesObservation = WidgetProperties.widgetSelection().observe(copiesSpinner); //SWTObservables.observeSelection(copiesSpinner)
        bindings.bindValue(copiesObservation, copiesValue);

        final Image collateOnImage = COLLATE_ON.createImage();
        final Image collateOffImage = COLLATE_OFF.createImage();

        // collate (imagelabel & checkbox)
        final Label collateImageLabel = new Label(result, SWT.CENTER | SWT.SHADOW_NONE);
        DialogUtil.layoutAlignRight(collateImageLabel);

        final Button collateCheck =
                DialogUtil.check(result, KlighdUIPrintingMessages.PrintDialog_Collate);

        collateImageLabel.setImage(collateCheck.getSelection() ? collateOnImage : collateOffImage);

        final IObservableValue<Object> collateValue =
                BeanProperties.value(options.getClass().asSubclass(PrintOptions.class), PrintOptions.PROPERTY_COLLATE).observe(realm, options);
                // BeansObservables.observeValue(realm, options, PrintOptions.PROPERTY_COLLATE);
        ISWTObservableValue<Object> collateObservation =  WidgetProperties.widgetSelection().observe((Widget) collateCheck); //SWTObservables.observeSelection(collateCheck);
        bindings.bindValue(collateObservation, collateValue);

        collateValue.addValueChangeListener(new IValueChangeListener<Object>() {

            public void handleValueChange(final ValueChangeEvent<? extends Object> event) {

                // set image according to collate state
                if (((Boolean) event.getObservableValue().getValue()).booleanValue()) {
                    collateImageLabel.setImage(collateOnImage);
                } else {
                    collateImageLabel.setImage(collateOffImage);
                }
            }
        });

        result.addListener(SWT.Dispose, new Listener() {

            public void handleEvent(final Event event) {
                collateOnImage.dispose();
                collateOffImage.dispose();

                // while the SWTObservableValues are disposed while disposing the corresponding widgets
                //  the Beans-based ones should be disposed explicitly
                copiesValue.dispose();
                collateValue.dispose();
            }
        });

        if (Klighd.IS_MACOSX) {
            // I deactivated 'copies' block as this information can be changed in the native dialog
            //  but (on OSX) it is not delivered back within the returned PrinterData.
            // Thus, 'copies' cannot be updated properly.
            // This, however, seems not to hold for windows and linux.
            result.setToolTipText(KlighdUIPrintingMessages.KlighdPrintDialog_Copies_OSXToolTip);

            for (final Control con : result.getChildren()) {
                con.setEnabled(false);
            }
        }

        return result;
    }
}
