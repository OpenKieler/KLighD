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
package de.cau.cs.kieler.klighd.ui.viewers;

import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.google.common.base.Function;

import de.cau.cs.kieler.klighd.IKlighdSelection;
import de.cau.cs.kieler.klighd.IViewer;
import de.cau.cs.kieler.klighd.Klighd;
import de.cau.cs.kieler.klighd.KlighdConstants;
import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.eclipse.EclipseViewContext;
import de.cau.cs.kieler.klighd.eclipse.IEclipseKlighdSelection;
import de.cau.cs.kieler.klighd.eclipse.IEclipseViewerProvider;
import de.cau.cs.kieler.klighd.eclipse.IModelModificationHandler;
import de.cau.cs.kieler.klighd.kgraph.KGraphElement;
import de.cau.cs.kieler.klighd.krendering.KText;
import de.cau.cs.kieler.klighd.piccolo.IKlighdNode.IKNodeNode;
import de.cau.cs.kieler.klighd.piccolo.KlighdNode.KlighdFigureNode;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KLabelNode;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KlighdMainCamera;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KlighdStyledText;
import de.cau.cs.kieler.klighd.piccolo.internal.util.NodeUtil;
import de.cau.cs.kieler.klighd.piccolo.viewer.PiccoloOutlinePage;
import de.cau.cs.kieler.klighd.piccolo.viewer.PiccoloViewer;
import de.cau.cs.kieler.klighd.ui.KlighdTextSelection;
import de.cau.cs.kieler.klighd.ui.KlighdUIPlugin;
import de.cau.cs.kieler.klighd.ui.modifymodel.ModelModificationHandlerProvider;
import de.cau.cs.kieler.klighd.viewers.ContextViewer;
import edu.umd.cs.piccolo.PNode;

/**
 * UI stuff such as the text input mechanism and registry of actions are extracted from the
 * PiccoloViewer to this class to optimize dependencies. PiccoloViewerUi instance will be generated
 * by the PiccoloViewerProvider.
 *
 * @author chsch
 * @author ckru
 */
public class PiccoloViewerUI extends PiccoloViewer {

    /** The identifier of this viewer type as specified in the extension. */
    public static final String ID = "de.cau.cs.kieler.klighd.ui.viewers.PiccoloViewerUI";

    /**
     * The required corresponding provider class.
     */
    public static class Provider implements IEclipseViewerProvider {

        /**
         * {@inheritDoc}
         */
        public IViewer createViewer(final ContextViewer parentViewer, final Composite parent) {
            return new PiccoloViewerUI(parentViewer, parent);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public IViewer createViewer(ContextViewer parentViewer) {
            // TODO Auto-generated method stub
            return null;
        }
    }

    /**
     * Creates a Piccolo2D viewer with default style.
     *
     * @param parentViewer
     *            the parent {@link ContextViewer}
     * @param parent
     *            the parent composite
     */
    public PiccoloViewerUI(final ContextViewer parentViewer, final Composite parent) {
        this(parentViewer, parent, SWT.None);
    }

    /**
     * Creates a Piccolo2D viewer with given style.
     *
     * @param parentViewer
     *            the parent {@link ContextViewer}
     * @param parent
     *            the parent composite
     * @param style
     *            the style attributes
     */
    public PiccoloViewerUI(final ContextViewer parentViewer, final Composite parent, final int style) {
        super(parentViewer, parent);

        addLabelTextWidget(parentViewer);
    }

    /**
     * SWT text element that acts as an overlay for labels in some situations.
     */
    private StyledText labelWidget;

    /**
     * Cached display display scale compensation factor, used for compensating the display scale
     * when configuring the text label widget.
     */
    private float dpiScaleY = 1f;

    /**
     * Getter providing the employed {@link StyledText} SWT widget, e.g. for installing additional
     * event {@link Listener Listeners}.
     *
     * @return the employed {@link StyledText} widget.
     */
    protected StyledText getLabelTextWidget() {
        return labelWidget;
    }

    /**
     * Adds a text widget to the viewer that can be used to select and edit texts.
     *
     * @param parentViewer
     *            the viewer to which to add the text widget
     */
    protected void addLabelTextWidget(final ContextViewer parentViewer) {
        labelWidget = new StyledText(this.getControl(), SWT.MULTI);
        labelWidget.setEditable(false);
        labelWidget.setVisible(false);

        if (!Klighd.isSuppressDisplayScaleCompensationWhileHandlingText()) {
            dpiScaleY =
                    KlighdConstants.DEFAULT_DISPLAY_DPI / this.getControl().getDisplay().getDPI().y;
        }

        // Configures a new font since on win32 the initially employed font
        //  is used in most other widget of the UI, too!
        // Thus, disposing that font, as done in
        //  KlighdLabelWidgetHandler#updateWidgetBounds(), is not allowed
        //  and we prevent that by initially setting a new one with the same font data ;-)
        labelWidget.setFont(
                new Font(labelWidget.getDisplay(), labelWidget.getFont().getFontData()));

        // create a additional (context) menu manager, ...
        final MenuManager menu = new MenuManager();

        // ... install it on the text input control, and ...
        labelWidget.setMenu(menu.createContextMenu(labelWidget));

        // ... and register it in the workbench part site, in order to let the work bench populate it!
        final IWorkbenchPart part = ((EclipseViewContext) parentViewer.getViewContext()).getDiagramWorkbenchPart();
        if (part != null)
            part.getSite().registerContextMenu(
                    KlighdUIPlugin.FLOATING_TEXT_MENU_ID, menu, (ISelectionProvider) parentViewer);

        this.getControl().getCamera().addInputEventListener(
                new KlighdLabelWidgetEventHandler(this, labelWidget));

        // add a selection changed listener to the diagram viewer in order to deactivate
        //  the cursor selection text widget on diagram selections
        final ISelectionChangedListener selectionListener = new ISelectionChangedListener() {

            public void selectionChanged(final SelectionChangedEvent event) {
                if (event.getSelection() instanceof KlighdTextSelection) {
                    return;
                }
                // chsch: deactivated the following line as it doesn't make sense to me
                //   triggering model updates after switching the selection is IMO
                //   in general not desirable
                // updateModelAfterTextChange(labelTextWidget, thisViewer.getViewContext());
                labelWidget.setSelection(0, 0);
                PiccoloViewerUI.this.deactivateLabelWidget();
            }
        };
        ((ISelectionProvider) parentViewer).addSelectionChangedListener(selectionListener);

        labelWidget.addListener(SWT.Dispose, new Listener() {
            public void handleEvent(final Event event) {
                ((ISelectionProvider) parentViewer).removeSelectionChangedListener(selectionListener);
            }
        });

        // create and register (in constructor) a dedicated SWT event listener on the labelTextWidget
        attachLabelTextWidgetEventListener(labelWidget);
    }

    /**
     * Extension hook method registering an SWT event {@link Listener} on the employed
     * <code>textLabelWidget</code>.
     *
     * @param textLabelWidget
     *            the employed instance of {@link StyledText}
     */
    protected void attachLabelTextWidgetEventListener(final StyledText textLabelWidget) {
        // this listener registers itself within its constructor there's no need to store it somewhere
        new LabelTextWidgetListener(this, textLabelWidget);
    }


    /**
     * SWT event {@link Listener} implementation that is dedicated to the creation & propagation of
     * {@link KlighdTextSelection KlighdTextSelections} after corresponding mouse and key events
     * notified to the employed label text widget.<br>
     * <br>
     * <b>Note:</b> This class may be subclassed. However, make sure to call
     * <code>super.handleEvent(event)</code> for the event types distinguished in this
     * implementation. Make also sure to register your subclass for additional event types if necessary,
     * see {@link #LabelTextWidgetListener(PiccoloViewerUI, StyledText)}.
     *
     * @author chsch
     */
    public static class LabelTextWidgetListener implements Listener {

        private final PiccoloViewerUI diagramViewer;
        private final StyledText labelWidget;

        /**
         * Constructor.
         *
         * @param viewer
         *            employed instance of {@link PiccoloViewerUI}
         * @param textLabelWidget
         *            the employed instance of {@link StyledText}
         */
        public LabelTextWidgetListener(final PiccoloViewerUI viewer,
                final StyledText textLabelWidget) {
            this.diagramViewer = viewer;
            this.labelWidget = textLabelWidget;

            textLabelWidget.addListener(SWT.MouseDown, this);
            textLabelWidget.addListener(SWT.MouseUp, this);
            textLabelWidget.addListener(SWT.KeyDown, this);
            textLabelWidget.addListener(SWT.KeyUp, this);
        }

        /**
         * Provides the {@link KlighdFigureNode} the label text widget is currently attached to.
         * Make sure to let this method be executed by the UI thread as it relies on
         * {@link org.eclipse.swt.widgets.Widget#getData() Widget#getData()}.
         *
         * @return the desired {@link KlighdFigureNode}
         */
        protected KlighdFigureNode<KText> getFigureNode() {
            @SuppressWarnings("unchecked")
            final KlighdFigureNode<KText> figureNode =
            (KlighdFigureNode<KText>) labelWidget.getData(STYLED_TEXT_FIGURE_KEY);
            return figureNode;
        }


        private String prevSelection = null;

        /**
         * @return the prevSelection
         */
        protected String getPrevSelection() {
            return prevSelection;
        }

        /**
         * @param prevSelection the prevSelection to set
         */
        protected void setPrevSelection(final String prevSelection) {
            this.prevSelection = prevSelection;
        }


        /**
         * {@inheritDoc}
         */
        public void handleEvent(final Event event) {
            final StyledText text = labelWidget;

            final String selection;
            switch (event.type) {
            case SWT.KeyDown:
                if (((char) event.keyCode == SWT.CR) && ((event.stateMask & SWT.SHIFT) != 0)) {
                    diagramViewer.updateModelAfterTextChange(text, diagramViewer.getViewContext());
                    text.setEditable(false);
                    text.setVisible(false);

                } else if ((char) event.keyCode == SWT.SHIFT) {
                    prevSelection = labelWidget.getSelectionText();
                }
                break;

            case SWT.KeyUp:
                if (event.keyCode != SWT.SHIFT) {
                    break;
                }
                selection = labelWidget.getSelectionText();
                if (selection.equals(prevSelection)) {
                    break;
                }
                diagramViewer.updateSelection((IKlighdSelection) new KlighdTextSelection(selection,
                        labelWidget.getSelection().x, false, false, getFigureNode(), diagramViewer));
                break;

            case SWT.MouseDown:
                if (event.button == 1 && (event.stateMask & SWT.SHIFT) == 0) {
                    // button 1 has been pressed and shift key is not pressed
                    prevSelection = labelWidget.getSelectionText();
                }
                break;

            case SWT.MouseUp:
                if (event.button != 1) {
                    break;
                }
                selection = text.getSelectionText();
                if (selection.equals(prevSelection)) {
                    break;
                }
                diagramViewer.updateSelection((IKlighdSelection) new KlighdTextSelection(selection,
                        labelWidget.getSelection().x, false, false, getFigureNode(), diagramViewer));
                break;
            }
        }
    }

    /**
     * Asynchronously executes {@link #updateSelection(ISelection)} in order to let the calling
     * method terminate quickly and do not block any display modifications.
     *
     * @param selection
     *            the new {@link IKlighdSelection} to be propagated to the selection service.
     */
    protected void updateSelection(final IKlighdSelection selection) {
        getControl().getDisplay().asyncExec(new Runnable() {
            public void run() {
                PiccoloViewerUI.super.updateSelection((IEclipseKlighdSelection) selection);
            }
        });
    }

    // the following field is package protected by intention
    //  since it is used in KlighdLabelWidgetEventHandler, too
    /**
     * String key for caching the {@link PropertyChangeListener} used for keeping the labelWidget's
     * styling up to date.
     */
    static final String TEXT_STYLING_CHANGE_LISTENER_KEY = "TEXT_STYLING_CHANGE_LISTENER_KEY";

    /** String key for caching the KlighdStyledText in the labelWidget's data list. */
    private static final String STYLED_TEXT_FIGURE_KEY = "STYLED_TEXT_FIGURE_KEY";

    /** String key for caching the font scale factor in the labelWidget's data list. */
    private static final String FONT_SCALE_FACTOR_KEY = "FONT_SCALE_FACTOR_KEY";

    /** Example: ...|11.0|.. */
    private static final String FONT_HEIGHT_PATTERN_REGEX = "\\|\\d*\\p{Punct}\\d*\\|";

    /** The pattern employed in configuring the fonts, is kept in order to avoid re-compilations. */
    private static Pattern fontHeightPattern = null;


    /**
     * Aligns the label text widget to the given <code>styledText</code> in terms of position, font
     * size, and size.
     *
     * @param styledText
     *            the {@link KlighdStyledText} the text label widget is to be aligned to
     */
    // method is package protected as it is used in KlighdLabelWidgetEventHander and
    //  KlighdLabelWidgetViewChangeListener
    void updateWidgetBounds(final KlighdStyledText styledText) {

        final KlighdStyledText theStyledText;
        if (styledText != null) {
            labelWidget.setData(STYLED_TEXT_FIGURE_KEY, styledText);
            theStyledText = styledText;
        } else {
            if (labelWidget.isDisposed()) {
                return;
            }

            theStyledText = (KlighdStyledText) labelWidget.getData(STYLED_TEXT_FIGURE_KEY);
            if (theStyledText == null) {
                return;
            }
        }

        final KlighdMainCamera camera = this.getControl().getCamera();

        // determine global position of the text element
        //  although 'clipRelativeGlobalBoundsOf' may return null that should never happen here as
        //  this is method is supposed to be only called for 'styledText' element that are contained
        //  in the current clip
        final Rectangle2D bounds =
                NodeUtil.clipRelativeGlobalBoundsOf(theStyledText, camera.getDisplayedKNodeNode());

        if (bounds == null) {
            return;
        }

        camera.getViewTransformReference().transform(bounds, bounds);

        labelWidget.setLocation((int) Math.round(bounds.getX()), (int) Math.round(bounds.getY()));

        final Float prevFontScale = (Float) labelWidget.getData(FONT_SCALE_FACTOR_KEY);
        final float curViewScale = (float) camera.getViewScale() * dpiScaleY;

        // in case styledText = null, i.e. this method has been called due to a view transform change
        //  and the widget is not moved to another text field,
        //  and the previously applied scale factor is configured and is equal to the current one
        // skip the resizing of the widget, it is not required.
        if (styledText == null && prevFontScale != null && prevFontScale.floatValue() == curViewScale) {
            return;
        }

        // backup the current view/font scale ...
        labelWidget.setData(FONT_SCALE_FACTOR_KEY, Float.valueOf(curViewScale));

        // ... and compose the updated FontData by means of a String-based configuration
        final String fontConfig = theStyledText.getFontData().toString();

        if (fontHeightPattern == null) {
            fontHeightPattern = Pattern.compile(FONT_HEIGHT_PATTERN_REGEX);
        }

        final Matcher matcher = fontHeightPattern.matcher(fontConfig);

        final float givenHeight;
        if (matcher.find()) {
            givenHeight = Float.valueOf(fontConfig.substring(matcher.start() + 1, matcher.end() - 1));
        } else {
            givenHeight = theStyledText.getFontData().getHeight();
        }

        // Create the updated FontData ...
        final FontData fd = new FontData(
                matcher.replaceFirst("|" + Float.toString(givenHeight * curViewScale) + "|"));

        final Font previousFont = labelWidget.getFont();

        // ... dispose the previous Font, configure the new one, and update the text widget's size
        labelWidget.setFont(new Font(labelWidget.getDisplay(), fd));
        labelWidget.setSize(labelWidget.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        previousFont.dispose();
    }

    void deactivateLabelWidget() {
        labelWidget.setVisible(false);

        final KlighdStyledText node = (KlighdStyledText) labelWidget.getData(STYLED_TEXT_FIGURE_KEY);
        if (node != null) {
            node.setOccludedOnMainDiagram(false);
            node.removePropertyChangeListener(
                    (PropertyChangeListener) labelWidget.getData(TEXT_STYLING_CHANGE_LISTENER_KEY));
        }
    }


    /**
     * Is used to update the underlying business model according to the change of text (label)
     * represented by the {@link StyledText} widget <code>textWidget</code>. Identifies the
     * corresponding KText & {@link KGraphElement} data, asks for a matching model update function,
     * and, if available, executes that function (wraps it with transaction helpers if required).
     *
     * @param textWidget
     *            the {@link StyledText} widget containing the updated text (label)
     * @param viewContext
     *            the employed {@link ViewContext}
     */
    private void updateModelAfterTextChange(final StyledText textWidget, final ViewContext viewContext) {
        final KlighdStyledText textNode = (KlighdStyledText) textWidget.getData(STYLED_TEXT_FIGURE_KEY);

        if (textNode == null) {
            return;
        }

        final KLabelNode relatedLabel;
        final KGraphElement relatedKGE;

        // determine whether 'textNode' is contained by a KLabelNode and if so reveal that label node
        //  this is done based on the PNodes since potential KRenderingRefs are resolved while
        //  building up the PNode network,
        // identifying the corresponding KLabel on the KGraph/KRendering will be much more difficult
        //  due to potentially involved KRenderingRefs!
        PNode node = textNode;
        while (true) {
            node = node.getParent();
            if (node == null) {
                // this may happen if the text was selected while the diagram was updated,
                //  thus 'node' may be removed from the diagram's figure tree
                return;
            }

            if (node instanceof IKNodeNode) {
                // the textNode appears not to be contained in a KLabelNode but
                //  (via path nodes and helper ones) directly in a KNodeNode or KNodeTopNode
                relatedLabel = null;
                relatedKGE = ((IKNodeNode) node).getViewModelElement();
                break;

            } else if (node instanceof KLabelNode) {
                // the text is contained in a KLabelNode ...
                relatedLabel = (KLabelNode) node;
                relatedKGE = relatedLabel.getViewModelElement();
                break;
            }
        }

        if (relatedLabel != null && textWidget.getText().equals(relatedLabel.getText())
                || relatedLabel == null && textWidget.getText().equals(textNode.getText())) {
            // A performance short cut: if the widget's text doesn't differ from the related KText's
            //  text or, if available, the corresponding KLabel's text stop here!
            // Note that the '== null' check in 2nd condition alternative is important!!
            return;
        }

        // by means of the (accessible) KText and parent KGraphElement request a
        //  model update function from the employed diagram synthesis
        final Function<String, Void> f = viewContext.getDiagramSynthesis().getTextUpdateFunction(
                textNode.getViewModelElement(), relatedKGE);

        if (f == null) {
            // in case no function for updating that particular text (label) is available,
            // hence this text cannot be updated, stop here!
            return;
        }

        final IWorkbenchPart wPart = ((EclipseViewContext) viewContext).getSourceWorkbenchPart();
        final IModelModificationHandler handler =
                ModelModificationHandlerProvider.getInstance().getFittingHandler(wPart);
        try {
            if (handler != null) {
                handler.execute(wPart, f, textWidget.getText());
            } else {
                f.apply(textWidget.getText());
            }
        } catch (final Exception e) {
            final String msg =
                    "KLighD: An error occured while applying the updated string value in "
                            + ((EclipseViewContext) viewContext).getDiagramWorkbenchPart().getPartId() + "!";
            Klighd.handle(new Status(IStatus.ERROR, KlighdUIPlugin.PLUGIN_ID, msg, e));
        }
    }


    @Override
    protected PiccoloOutlinePage createDiagramOutlinePage() {
        return new PiccoloContentOutlinePage();
    }

    /**
     * A subclass of the {@link PiccoloOutlinePage} that implements the required
     * {@link IContentOutlinePage} interface.<br>
     * <br>
     * Since that interface requires an additional UI dependency but luckily is simply a composition
     * of {@link org.eclipse.ui.part.IPage IPage} and
     * {@link org.eclipse.jface.viewers.ISelectionProvider ISelectionProvider}, and we do not
     * support any selection providing functionality, {@link PiccoloOutlinePage} only implements
     * {@link org.eclipse.ui.part.IPage IPage} for the sake of reducing dependencies. The required
     * (empty) {@link org.eclipse.jface.viewers.ISelectionProvider ISelectionProvider} methods are
     * than contributed by this sub class.
     *
     * @author chsch
     */
    private static class PiccoloContentOutlinePage extends PiccoloOutlinePage implements
    IContentOutlinePage {

        /**
         * {@inheritDoc}
         */
        public void addSelectionChangedListener(final ISelectionChangedListener listener) {
            // selection is not supported by this outline page
        }

        /**
         * {@inheritDoc}
         */
        public void removeSelectionChangedListener(final ISelectionChangedListener listener) {
            // selection is not supported by this outline page
        }

        /**
         * {@inheritDoc}
         */
        public ISelection getSelection() {
            // selection is not supported by this outline page
            return null;
        }

        /**
         * {@inheritDoc}
         */
        public void setSelection(final ISelection selection) {
            // selection is not supported by this outline page
        }
    }
}
