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


import java.util.LinkedList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import de.cau.cs.kieler.klighd.eclipse.IDiagramExporter.TilingData;
import de.cau.cs.kieler.klighd.ui.internal.Messages;

/**
 * Dialog for tiling options, used by the {@link SaveAsImageDialog}.
 * Gathers all needed information and provides it as a {@link TilingData} object.
 * 
 * @author csp
 */
public class TilingDialog extends Dialog {
    
    private TilingData info;
    private Spinner spinnerX;
    private Spinner spinnerY;
    private Spinner spinnerWidth;
    private Spinner spinnerHeight;
    private LinkedList<Control> rowsColsGroup;
    private LinkedList<Control> maxSizeGroup;
    private Button rowsColsButton;
    private Button maxSizeButton;
    
    /**
     * @param parentShell the parentShell
     * @param info the tilinginfo
     */
    public TilingDialog(final Shell parentShell, final TilingData info) {
        super(parentShell);
        setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.OK | SWT.APPLICATION_MODAL);
        
        this.info = info;
        rowsColsGroup = new LinkedList<Control>();
        maxSizeGroup = new LinkedList<Control>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configureShell(final Shell shell) {
        super.configureShell(shell);
        shell.setText(Messages.TilingDialog_title);
    }

    private static final int GROUP_COLUMNS = 4;
    private static final int SPINNER_WIDTH = 50;

    /**
     * {@inheritDoc}
     */
    @Override
    protected Control createDialogArea(final Composite parent) {
        Composite area =  (Composite) super.createDialogArea(parent);
        Composite composite = createComposite(area, GROUP_COLUMNS);
        
        rowsColsButton = new Button(composite, SWT.RADIO);
        rowsColsButton.setText("Define number of rows and columns");
        rowsColsButton.setSelection(!info.isMaxsize);
        rowsColsButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                updateEnabledState(false);
            }
        });
        
        GridData gridData;
        gridData = new GridData(SWT.NONE);
        gridData.horizontalSpan = GROUP_COLUMNS;
        rowsColsButton.setLayoutData(gridData);
        
        addRowsColsGroup(composite);
        
        maxSizeButton = new Button(composite, SWT.RADIO);
        maxSizeButton.setText("Define maximum size");
        maxSizeButton.setSelection(info.isMaxsize);
        maxSizeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                updateEnabledState(true);
            }
        });

        gridData = new GridData(SWT.NONE);
        gridData.horizontalSpan = GROUP_COLUMNS;
        maxSizeButton.setLayoutData(gridData);
        
        addMaxSizeGroup(composite);
        
        Button resetButton = new Button(area, SWT.PUSH);
        resetButton.setText("Reset");
        resetButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                info = TilingData.createNonTiledData();
                updateSpinnerSelections();
                rowsColsButton.setSelection(!info.isMaxsize);
                maxSizeButton.setSelection(info.isMaxsize);
                updateEnabledState(info.isMaxsize);
            }
        });
        
        updateSpinnerSelections();
        updateEnabledState(info.isMaxsize);
        return composite;
    }
    
    /**
     * Enables one of the spinner groups (rows/cols or max size) and disables the other.
     * 
     * @param isMaxSize
     *            determines, which spinner group should be enabled
     */
    private void updateEnabledState(final boolean isMaxSize) {
        for (Control control : rowsColsGroup) {
            control.setEnabled(!isMaxSize);
        }
        for (Control control : maxSizeGroup) {
            control.setEnabled(isMaxSize);
        }
    }

    /**
     * Updates the selected values of the spinners from the local {@link TilingData}.
     */
    private void updateSpinnerSelections() {
        spinnerX.setSelection(info.rows);
        spinnerY.setSelection(info.cols);
        spinnerWidth.setSelection(info.maxWidth);
        spinnerHeight.setSelection(info.maxHeight);
    }

    private void addRowsColsGroup(final Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText("Rows:");
        rowsColsGroup.add(label);
        
        spinnerX = new Spinner(parent, SWT.NONE);
        rowsColsGroup.add(spinnerX);
        
        label = new Label(parent, SWT.NONE);
        label.setText("Columns:");
        rowsColsGroup.add(label);
        
        spinnerY = new Spinner(parent, SWT.NONE);
        rowsColsGroup.add(spinnerY);
        
        spinnerX.setMinimum(1);
        spinnerX.setMaximum(Integer.MAX_VALUE);
        spinnerY.setMinimum(1);
        spinnerY.setMaximum(Integer.MAX_VALUE);
        GridData gridData;
        gridData = new GridData(SWT.NONE);
        gridData.widthHint = SPINNER_WIDTH;
        spinnerX.setLayoutData(gridData);
        spinnerY.setLayoutData(gridData);
    }

    private void addMaxSizeGroup(final Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText("Width:");
        maxSizeGroup.add(label);
        
        spinnerWidth = new Spinner(parent, SWT.NONE);
        maxSizeGroup.add(spinnerWidth);
        
        label = new Label(parent, SWT.NONE);
        label.setText("Height:");
        maxSizeGroup.add(label);
        
        spinnerHeight = new Spinner(parent, SWT.NONE);
        maxSizeGroup.add(spinnerHeight);
        
        spinnerWidth.setMinimum(1);
        spinnerWidth.setMaximum(Integer.MAX_VALUE);
        spinnerHeight.setMinimum(1);
        spinnerHeight.setMaximum(Integer.MAX_VALUE);
        GridData gridData;
        gridData = new GridData(SWT.NONE);
        gridData.widthHint = SPINNER_WIDTH;
        spinnerWidth.setLayoutData(gridData);
        spinnerHeight.setLayoutData(gridData);
    }

    private Composite createComposite(final Composite parent, final int columns) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(columns, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;

        GridData data = new GridData(SWT.FILL, SWT.NONE, true, false);
        composite.setLayoutData(data);
        composite.setLayout(gridLayout);
        return composite;
    }

    /**
     * @return the tiling information with updated values
     */
    public TilingData getTilingInfo() {
        return info;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void okPressed() {
        if (rowsColsButton.getSelection()) {
            info = TilingData.createTiledData(spinnerX.getSelection(), spinnerY.getSelection());
        } else {
            info =
                    TilingData.createMaxSizeTiledData(spinnerWidth.getSelection(),
                            spinnerHeight.getSelection());
        }
        super.okPressed();
    }
}
