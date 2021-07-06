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
package de.cau.cs.kieler.klighd.ui.wizard;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

import com.google.common.base.Strings;

/**
 * A wizard page that lets the user specify the project, package, name, and language for a new
 * KLighD diagram synthesis project.
 * 
 * @author uru
 */
public class KlighdNewProjectCreationPage extends WizardNewProjectCreationPage {

    private Text transformationPackage;
    private Text transformationName;
    private Text sourceModel;
    private Text fileEnding;
    private Button useJavaLang;
    private Button useXtendLang;
    private ComboViewer execEnvCombo;
    private Button createMenuContribution;
    private Button useFileEnding;
    private Label fileEndingLabel;
    

    /**
     * Create a new wizard configuration page.
     * 
     * @param pageName
     *            name of the page
     */
    public KlighdNewProjectCreationPage(final String pageName) {
        super(pageName);
        setTitle(JavaUIMessages.KlighdNewProjectCreationPage_PageTitle);
        setDescription(JavaUIMessages.KlighdNewProjectCreationPage_PageDescr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createControl(final Composite parent) {
        setInitialProjectName(KlighdWizardSetup.DEFAULT_PROJECT); //$NON-NLS-1$
        super.createControl(parent);

        // query transformation details
        createTransformationSelectionGroup((Composite) getControl());

        // set default values
        setDefaults();
        Dialog.applyDialogFont(getControl());
    }

    /**
     * Sets default values for the text boxes and check boxes.
     */
    protected void setDefaults() {
        transformationName.setText(KlighdWizardSetup.DEFAULT_TRANSFORMATION_NAME);
        transformationPackage.setText(KlighdWizardSetup.DEFAULT_PROJECT);
        useXtendLang.setSelection(true);
        createMenuContribution.setSelection(true);
        fileEnding.setEnabled(false);
        fileEndingLabel.setEnabled(false);
        validatePage();
    }

    /**
     * Creates all ui elements.
     * 
     * @param parent
     *            parent composite
     */
    // SUPPRESS CHECKSTYLE NEXT Length -- ui stuff is lengthy, don't bother me!
    protected void createTransformationSelectionGroup(final Composite parent) {
        // Source Model
        final Group typeGroup = new Group(parent, SWT.NONE);
        typeGroup.setText(JavaUIMessages.KlighdNewProjectCreationPage_SourceModelGroupTitle);
        typeGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
        typeGroup.setLayout(new GridLayout(2, false));

        // text field
        sourceModel = new Text(typeGroup, SWT.BORDER);
        GridData textData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textData.horizontalSpan = 1;
        textData.horizontalIndent = 0;
        sourceModel.setLayoutData(textData);
        sourceModel.setToolTipText(JavaUIMessages.KlighdNewProjectCreationPage_SourceModelTooltip);
        // button
        Button browseButton = new Button(typeGroup, SWT.PUSH);
        browseButton.setText(JavaUIMessages.KlighdNewProjectCreationPage_BrowseButtonText);
        browseButton.setFont(parent.getFont());
        Listener browseListener = new Listener() {

            @SuppressWarnings("restriction")
            public void handleEvent(final Event event) {
                try {
                    // open a dialog querying for the source model type
                    IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
                    SelectionDialog dialog =
                            JavaUI.createTypeDialog(parent.getShell(), new ProgressMonitorDialog(
                                    parent.getShell()), scope,
                                    IJavaElementSearchConstants.CONSIDER_ALL_TYPES, false);
                    dialog.setTitle(JavaUIMessages.KlighdNewProjectCreationPage_SourceModelDialogTitle);
                    dialog.setMessage(
                            org.eclipse.jdt.internal.ui.JavaUIMessages.OpenTypeAction_dialogMessage);

                    int result = dialog.open();
                    if (result != IDialogConstants.OK_ID) {
                        return;
                    }

                    // retrieve the qualified name
                    Object[] res = dialog.getResult();
                    if (res != null && res.length > 0) {
                        IJavaElement element = (IJavaElement) res[0];
                        if (element.getElementType() == IJavaElement.TYPE) {
                            IType type = (IType) element;
                            sourceModel.setText(type.getFullyQualifiedName());
                            transformationName.setText(type.getElementName()
                                    + KlighdWizardSetup.DEFAULT_TRANSFORMATION_NAME);
                        }
                    }
                } catch (JavaModelException e) {
                    // fallback to object
                    sourceModel.setText(Object.class.getCanonicalName());
                }
            }
        };
        browseButton.addListener(SWT.Selection, browseListener);

        // DiagramSynthesis group
        Group transformationGroup = new Group(parent, SWT.NONE);
        transformationGroup.setText(
                JavaUIMessages.KlighdNewProjectCreationPage_DiagramSynthesisGroupTitle);
        transformationGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        transformationGroup.setLayout(new GridLayout(1, false));

        Composite composite = new Composite(transformationGroup, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        composite.setLayout(new GridLayout(2, false));

        // Name
        Label transformationLabel = new Label(composite, SWT.NONE);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 1;
        transformationLabel.setText(JavaUIMessages.KlighdNewProjectCreationPage_Name);

        transformationName = new Text(composite, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 1;
        transformationName.setLayoutData(data);
        transformationName.setFont(parent.getFont());
        transformationName.setToolTipText(
                JavaUIMessages.KlighdNewProjectCreationPage_SynthesisNameTooltip);
        
        // Package
        Label transformationPackageLabel = new Label(composite, SWT.NONE);
        GridData data2 = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 1;
        transformationPackageLabel.setText(JavaUIMessages.KlighdNewProjectCreationPage_Package);

        transformationPackage = new Text(composite, SWT.BORDER);
        data2 = new GridData(GridData.FILL_HORIZONTAL);
        data2.horizontalSpan = 1;
        transformationPackage.setLayoutData(data);
        transformationPackage.setFont(parent.getFont());
        transformationPackage.setToolTipText(JavaUIMessages.KlighdNewProjectCreationPage_PackageTooltip);

        // Which language to use, Java or Xtend?
        final Group languageGroup = new Group(parent, SWT.NONE);
        languageGroup.setText(JavaUIMessages.KlighdNewProjectCreationPage_LanguageGroupText);
        languageGroup.setToolTipText(JavaUIMessages.KlighdNewProjectCreationPage_LanguageTooltip);
        languageGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        languageGroup.setLayout(new GridLayout(4, false)); // SUPPRESS CHECKSTYLE MagicNumber

        useJavaLang = new Button(languageGroup, SWT.RADIO);
        useJavaLang.setText(JavaUIMessages.KlighdNewProjectCreationPage_LanguageJava);
        useJavaLang.setLayoutData(new GridData());
        useJavaLang.setToolTipText(JavaUIMessages.KlighdNewProjectCreationPage_LanguageTooltip);

        useXtendLang = new Button(languageGroup, SWT.RADIO);
        useXtendLang.setText(JavaUIMessages.KlighdNewProjectCreationPage_LanguageXtend);
        final GridData useXtendLangLayout = new GridData();
        useXtendLangLayout.grabExcessHorizontalSpace = true;
        useXtendLangLayout.horizontalAlignment = SWT.CENTER;
        useXtendLang.setLayoutData(useXtendLangLayout);
        useXtendLang.setToolTipText(JavaUIMessages.KlighdNewProjectCreationPage_LanguageTooltip);

        transformationName.addListener(SWT.Modify, modifyListener);
        transformationPackage.addListener(SWT.Modify, modifyListener);
        useJavaLang.addListener(SWT.Modify, modifyListener);
        useXtendLang.addListener(SWT.Modify, modifyListener);
        sourceModel.addListener(SWT.Modify, modifyListener);

        final Label execEnvLabel = new Label(languageGroup, SWT.NONE);
        execEnvLabel.setText(JavaUIMessages.KlighdNewProjectCreationPage_RuntimeExecEnv);
        final GridData execEnvLabelLayout = new GridData();
        execEnvLabelLayout.grabExcessHorizontalSpace = true;
        execEnvLabelLayout.horizontalAlignment = SWT.RIGHT;
        execEnvLabel.setLayoutData(execEnvLabelLayout);

        execEnvCombo = new ComboViewer(languageGroup);
        execEnvCombo.getControl().setLayoutData(new GridData());
        execEnvCombo.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(final Object element) {
                return ((IExecutionEnvironment) element).getId();
            }
        });

        final IVMInstall defaultVM = JavaRuntime.getDefaultVMInstall();
        final int minJavaVersion = 5;

        for (IExecutionEnvironment e
                : JavaRuntime.getExecutionEnvironmentsManager().getExecutionEnvironments()) {

            final String id = e.getId();
            if (id.startsWith("J")
                    && Character.getNumericValue(id.charAt(id.length() - 1)) >= minJavaVersion) {
                execEnvCombo.add(e);
                if (e.isStrictlyCompatible(defaultVM)) {
                    execEnvCombo.setSelection(new StructuredSelection(e));
                }
            }
        }
        
        final Group uiConfigGroup = new Group(parent, SWT.NONE);
        uiConfigGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        uiConfigGroup.setText(JavaUIMessages.KlighdNewProjectCreationPage_UIGroupText);
        uiConfigGroup.setLayout(new GridLayout(1, false));
        createMenuContribution = new Button(uiConfigGroup, SWT.CHECK);
        createMenuContribution.setText(
                JavaUIMessages.KlighdNewProjectCreationPage_CreateMenuContributionText);
        createMenuContribution.setLayoutData(new GridData());
        createMenuContribution.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                useFileEnding.setEnabled(createMenuContribution.getSelection());
                if (!createMenuContribution.getSelection()) {
                    fileEnding.setEnabled(false);
                    fileEndingLabel.setEnabled(false);
                } else {
                    fileEnding.setEnabled(useFileEnding.getSelection());
                    fileEndingLabel.setEnabled(useFileEnding.getSelection());
                }
            }
        });
        useFileEnding = new Button(uiConfigGroup, SWT.CHECK);
        useFileEnding.setText(JavaUIMessages.KlighdNewProjectCreationPage_UseFileEndingText);
        useFileEnding.setLayoutData(new GridData());
        useFileEnding.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                fileEnding.setEnabled(useFileEnding.getSelection());
                fileEndingLabel.setEnabled(useFileEnding.getSelection());
            }
        });
        
        Composite fileEndingComposite = new Composite(uiConfigGroup, SWT.NONE);
        fileEndingComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        fileEndingComposite.setLayout(new GridLayout(2, false));
        
        fileEndingLabel = new Label(fileEndingComposite, SWT.NONE);
        data = new GridData();
        data.horizontalSpan = 1;
        fileEndingLabel.setLayoutData(data);
        fileEndingLabel.setText(JavaUIMessages.KlighdNewProjectCreationPage_Ending);
        
        fileEnding = new Text(fileEndingComposite, SWT.BORDER);
        fileEnding.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        fileEnding.setFont(parent.getFont());
        fileEnding.setToolTipText(JavaUIMessages.KlighdNewProjectCreationPage_FileEndingTooltip);
        
        createMenuContribution.addListener(SWT.Selection, modifyListener);
        useFileEnding.addListener(SWT.Selection, modifyListener);
        fileEnding.addListener(SWT.Modify, modifyListener);
    }
    
    // Listen to changes
    private Listener modifyListener = new Listener() {
        public void handleEvent(final Event event) {
            // be careful to call the super implementation as otherwise we'll get a
            //  stack overflow due to #setPageComplete(boolean) below!
            KlighdNewProjectCreationPage.super.setPageComplete(validatePage());
        }
    };
 
    /**
     * {@inheritDoc}
     * 
     * This specialization realizes the update of the package field in case the project name is
     * changed. Unfortunately there is no other way to react on such changes.
     */
    public void setPageComplete(final boolean complete) {
        if (this.transformationPackage != null) {
            this.transformationPackage.setText(this.getProjectName());
        }
        super.setPageComplete(complete);
    }

    @Override
    protected boolean validatePage() {

        // are we still in initialization phase? Then we assume 
        // the page to be kindof valid.
        if (transformationName == null || transformationPackage == null || sourceModel == null
                || fileEnding == null || useFileEnding == null || createMenuContribution == null) {
            return false;
        }
        
        if (!super.validatePage()) {
            return false;
        }

        // check project name
        IStatus status =
                JavaConventions.validatePackageName(getProjectName(), JavaCore.VERSION_1_5,
                        JavaCore.VERSION_1_5);
        if (!status.isOK()) {
            setErrorMessage(JavaUIMessages.KlighdNewProjectCreationPage_MsgInvalidProjectName
                    + status.getMessage());
            return false;
        }

        // check menu contribution with file ending
        if (createMenuContribution.getSelection() && useFileEnding.getSelection() 
                && Strings.isNullOrEmpty(fileEnding.getText())) {
            setErrorMessage(JavaUIMessages.KlighdNewProjectCreationPage_MsgFileEndingEmpty);
            return false;
        }
        
        // check transformation name and package
        if (Strings.isNullOrEmpty(transformationName.getText())
                || Strings.isNullOrEmpty(transformationPackage.getText())) {
            return false;
        }
        status = 
                JavaConventions.validateJavaTypeName(transformationName.getText(),
                        JavaCore.VERSION_1_5, JavaCore.VERSION_1_5, null);
        if (!status.isOK()) {
            setErrorMessage(JavaUIMessages.KlighdNewProjectCreationPage_MsgInvalidTransformationName
                    + status.getMessage());
            return false;
        }

        // check source model
        if (Strings.isNullOrEmpty(sourceModel.getText())) {
            return false;
        }
        if (!sourceModel.getText().contains(".")) {
            // this check actually keeps people from writing transformations against models that lie in
            // the default package, but no one should use the default package anyway, so... screw it.
            setErrorMessage(JavaUIMessages.KlighdNewProjectCreationPage_MsgSourceModelNotQualified);
            return false;
        }
        status =
                JavaConventions.validateJavaTypeName(sourceModel.getText(), JavaCore.VERSION_1_5,
                        JavaCore.VERSION_1_5);
        if (!status.isOK()) {
            setErrorMessage(JavaUIMessages.KlighdNewProjectCreationPage_MsgInvalidSourceModelType
                    + status.getMessage());
            return false;
        }
        
        setMessage(null);
        setErrorMessage(null);
        
        return true;
    }

    /**
     * @return the transformation name
     */
    public String getTransformationName() {
        return transformationName.getText();
    }

    /**
     * @return the transformation package
     */
    public String getTransformationPackage() {
        return transformationPackage.getText();
    }

    /**
     * @return the fully qualified source model name
     */
    public String getSourceModel() {
        return sourceModel.getText();
    }

    /**
     * @return whether to create an xtend file or a java file
     */
    public boolean isCreateXtendFile() {
        return useXtendLang.getSelection();
    }

    /**
     * @return the <code>id</code> chosen java runtime execution environment
     */
    public String getExecEnvironment() {
        final ISelection selection = execEnvCombo.getSelection();
        return selection instanceof IStructuredSelection
                ? ((IExecutionEnvironment) ((IStructuredSelection) selection).getFirstElement()).getId()
                        : null;
    }

    /**
     * @return whether to create an xtend file or a java file
     */
    public boolean isCreateMenuContribution() {
        return createMenuContribution.getSelection();
    }

    /**
     * @return whether file ending should be used for menu contributions
     */
    public boolean isUseFileEnding() {
        return useFileEnding.getSelection();
    }

    /**
     * @return the file ending to be used for menu contributions
     */
    public String getFileEnding() {
        return fileEnding.getText();
    }
    
}
