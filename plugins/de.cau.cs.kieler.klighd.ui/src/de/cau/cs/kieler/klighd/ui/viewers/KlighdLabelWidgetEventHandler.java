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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;

import com.google.common.base.Strings;

import de.cau.cs.kieler.klighd.KlighdConstants;
import de.cau.cs.kieler.klighd.eclipse.EclipseKlighdConstants;
import de.cau.cs.kieler.klighd.krendering.KText;
import de.cau.cs.kieler.klighd.piccolo.IKlighdNode.IKGraphElementNode;
import de.cau.cs.kieler.klighd.piccolo.internal.events.KlighdBasicInputEventHandler;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KLabelNode;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KNodeNode;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KlighdMainCamera;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KlighdStyledText;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.NodeDisposeListener;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PText;

/**
 * A dedicated {@link KlighdBasicInputEventHandler} in charge of putting the text label widget in
 * place and updating it.<br>
 * <br>
 * The text label shall be as smoothly as possible integrated into the diagram meaning that there
 * should be as little as possible additional clicks required in order to select the text (or parts
 * of it) and copy it, for example. This however requires quite some event handling magic like
 * duplicating SWT events.<br>
 * <br>
 * <b>Update:</b> deactivated the magic code as it doesn't work properly on windows setups
 *
 * @author chsch
 */
public class KlighdLabelWidgetEventHandler extends KlighdBasicInputEventHandler {

    private final PiccoloViewerUI viewer;
    private final KlighdMainCamera camera;
    private StyledText labelWidget;
    private PropertyChangeListener labelWidgetStylingEventListener;

    /**
     * Constructor that just calls super.
     *
     * @param viewer
     *            the employed {@link PiccoloViewerUI}
     * @param labelWidget
     *            the employed labelWidget
     */
    public KlighdLabelWidgetEventHandler(final PiccoloViewerUI viewer, final StyledText labelWidget) {
        super();

        this.viewer = viewer;
        this.camera = viewer.getControl().getCamera();
        this.labelWidget = labelWidget;

        new PropertyChangeListener() {
            {
                camera.addPropertyChangeListener(PCamera.PROPERTY_VIEW_TRANSFORM, this);
                camera.addPropertyChangeListener(NodeDisposeListener.DISPOSE, this);
            }

            public void propertyChange(final PropertyChangeEvent event) {
                final String propName = event.getPropertyName();

                if (PCamera.PROPERTY_VIEW_TRANSFORM.equals(propName)) {
                    viewer.updateWidgetBounds(null);

                } else if (NodeDisposeListener.DISPOSE.equals(propName)) {
                    camera.removePropertyChangeListener(this);
                }
            }
        };

        labelWidgetStylingEventListener = new PropertyChangeListener() {
            {
                labelWidget.setData(PiccoloViewerUI.TEXT_STYLING_CHANGE_LISTENER_KEY, this);
            }

            public void propertyChange(final PropertyChangeEvent event) {
                final String propName = event.getPropertyName();

                // although strings are compared here I used '==' since 'propName' is expected
                //  to be exactly one of the mentioned constant definitions!
                final boolean update =
                        PNode.PROPERTY_PAINT == propName || PText.PROPERTY_FONT == propName
                                || PText.PROPERTY_TEXT_PAINT == propName;

                if (update) {
                    KlighdLabelWidgetEventHandler.this
                            .updateTextInputColoringAndSize((KlighdStyledText) event.getSource());
                }
            }
        };

        viewer.addViewChangedListener(new KlighdLabelWidgetViewChangeListener(viewer, labelWidget));
    }

    private boolean widgetJustPrepared = false;

    /**
     * {@inheritDoc}<br>
     * <br>
     * This method triggers the preparation of the text label widget if it's not blocked due to a
     * text selection and the mouse moved over a text figure node, and hides the widget if the mouse
     * moved over a non-text figure and no text selection was defined.
     */
    @Override
    public void mouseMoved(final PInputEvent event) {
        if (widgetJustPrepared) {
//            // This special case is active if the text widget is been prepared some milliseconds ago.
//            // Thus, prevent the canvas, i.e. the registered handlers, from reacting on this event,
//            //  and "forward" it to the label widget.
//
//            forwardEventToLabel(event.getSourceSwingEvent());

            // Don't let any further event handler evaluate 'event'.
            //  This works fine since this handler is supposed to be the last one in the row of
            //  registered handlers and is, therefore, called first
            event.setHandled(true);

        } else if (viewer.isMagnificationLensVisible()) {
            return;

        } else if (event.getPickedNode() instanceof KlighdStyledText) {
            updateTextInput(event, false);

            // event.setHandled(true) is skipped here by intention
            //  - other handlers might want to react on it

        } else if (labelWidget.getSelectionCount() == 0) {
            viewer.deactivateLabelWidget();

            // event.setHandled(true) is skipped here by intention
            //  - other handlers might want to react on it
        }
    }

    /**
     * {@inheritDoc}<br>
     * <br>
     * This method triggers the preparation of the text label widget if the user clicked on it,
     * regardless whether the widget is blocked due to text selection.
     */
    @Override
    public void mousePressed(final PInputEvent event) {
        if (widgetJustPrepared) {
//            // This special case is active if the text widget is been prepared some milliseconds ago.
//            // Thus, prevent the canvas, i.e. the registered handlers, from reacting on this event,
//            //  and "forward" it to the label widget.
//
//            forwardEventToLabel(event.getSourceSwingEvent());

            // Don't let any further event handler evaluate 'event'.
            //  This works fine since this handler is supposed to be the last one in the row of
            //  registered handlers and is, therefore, called first
            event.setHandled(true);

        } else if (viewer.isMagnificationLensVisible()) {
            return;

        } else if (labelWidget.isVisible() && labelWidget.getSelectionCount() != 0) {
            // if the widget is shown and blocked by a text selection try to steel the text label
            //  widget and reset it to the picked styled text (indicated by 'true')

            final boolean widgetVisible = updateTextInput(event, true);
            if (!widgetVisible) {
                // stop in case the widget couldn't be brought to the picked figure, since it is
                //  not a text figure for example
                return;
            }

            // otherwise prevent other handler from evaluating this event
            event.setHandled(true);

//            // fire a small mouse movement that helps much in capturing the first character of a
//            //  line in case a mouse-press-and-move-selection event
//            injectMoveLeftEvent(event.getSourceSwingEvent());

//            // "forward" the mouse pressed event to the label widget in order to initiate the
//            //  selection process
//            forwardEventToLabel(event.getSourceSwingEvent());

        } else if (!labelWidget.isVisible()) {
            // if the widget is not visible and try to prepare the label text widget,
            //  this is helpful in case the whole tool was inactive and, therefore, no move events
            //  are propagated to the canvas widget
            final boolean widgetVisible = this.updateTextInput(event, false);
            if (widgetVisible) {
                event.setHandled(true);

//                // "forward" the mouse pressed event to the label widget in order to initiate the
//                //  selection process if required
//                forwardEventToLabel(event.getSourceSwingEvent());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseReleased(final PInputEvent event) {
        if (widgetJustPrepared) {
            // This special case is active if the text widget is been prepared some milliseconds ago.
            // Thus, prevent the canvas, i.e. the registered handlers, from reacting on this event,
            //  and "forward" it to the label widget in order to enable immediate double clicks

            event.setHandled(true);
//            forwardEventToLabel(event.getSourceSwingEvent());
        }
    }


    /**
     * Configures the text label widget to exhibit the text content in case event's picked node is a
     * {@link KlighdStyledText} that is configured to be cursorSelectable and its text is non-
     * <code>null</code>.
     *
     * @param event
     *            the event that triggered this update.
     * @return <code>true</code> if the text widget has been updated successfully,
     *         <code>false</code> if the update has been aborted
     */
    private boolean updateTextInput(final PInputEvent event, final boolean force) {
        final PNode n = event.getPickedNode();
        final KText kText;
        final KlighdStyledText styledText;

        if (n instanceof KlighdStyledText) {
            styledText = (KlighdStyledText) n;
            kText = styledText.getViewModelElement();
        } else {
            return false;
        }

        if (kText == null || !kText.isCursorSelectable() || !force
                && labelWidget.getSelectionCount() > 0) {
            return false;
        }

        // this call is required in order to re-enable the visibility of the formerly corresponding
        //  text figure and to remove any listeners from that figure
        this.viewer.deactivateLabelWidget();

        final String text = styledText.getText();

        // determine text value
        if (text == null) {
            return false;
        } else if (Strings.isNullOrEmpty(text)) {
            labelWidget.setText(" ");
        } else {
            labelWidget.setText(text);
        }

        labelWidget.setEditable(kText.isEditable());

        updateTextInputColoringAndSize(styledText);

        styledText.addPropertyChangeListener(labelWidgetStylingEventListener);

        attachTextsParentInformation(styledText);

        // due to rounding issues in the font size handling the label widget does not completely
        //  overlap the text figure so we set that one occluded here
        styledText.setOccludedOnMainDiagram(true);

        labelWidget.setVisible(true);
        labelWidget.setFocus();
        setWidgetPrepared();

        return true;
    }


    private void updateTextInputColoringAndSize(final KlighdStyledText styledText) {

        viewer.updateWidgetBounds(styledText);

        // determine text color ...
        final Color oldColor = labelWidget.getForeground();
        labelWidget.setForeground(new Color(labelWidget.getDisplay(), styledText.getPenColor()));
        oldColor.dispose();

        // ... and the text's background color ...
        final Color oldBackground = labelWidget.getBackground();

        if (styledText.getBackgroundColor() != null) {
            labelWidget.setBackground(
                    new Color(labelWidget.getDisplay(), styledText.getBackgroundColor()));
            oldBackground.dispose();

        } else if (!oldBackground.getRGB().equals(KlighdConstants.WHITE)) {
            labelWidget.setBackground(new Color(labelWidget.getDisplay(), EclipseKlighdConstants.WHITE));
            oldBackground.dispose();
        }
    }


    /** String key for caching the KlighdStyledText in the labelWidget's data list. */
    // this field is package protected by intention
    static final String STYLED_TEXT_PARENTS_KEY = "STYLED_TEXT_PARENTS_KEY";

    private void attachTextsParentInformation(final KlighdStyledText text) {

        PNode parent = getParentGraphNode(text);
        if (parent == null) {
            return;
        }

        final List<PNode> parents = new ArrayList<PNode>(3);

        if (parent instanceof KLabelNode) {
            parents.add(parent);

            parent = getParentGraphNode(parent);
        }
        if (parent != null && !(parent instanceof KNodeNode)) {
            // hence KEdgeNode or KPortNode
            parents.add(parent);

            parent = getParentGraphNode(parent);
        }
        if (parent != null) {
            // must now be a KNodeNode
            parents.add(parent);
        }

        labelWidget.setData(STYLED_TEXT_PARENTS_KEY, parents);
    }

    // this method is static and package protected by intention
    static PNode getParentGraphNode(final PNode node) {
        PNode parent = node;
        do {
            parent = parent.getParent();
        } while (!(parent == null || parent instanceof IKGraphElementNode));
        return parent;
    }



    private void setWidgetPrepared() {
        widgetJustPrepared = true;
        // System.out.println("'widgetPrepared' set");

        // SUPPRESS CHECKSTYLE NEXT MagicNumber
        labelWidget.getDisplay().timerExec(250, resetWidgetJustPrepared);
    }

    private final Runnable resetWidgetJustPrepared = new Runnable() {
        public void run() {
            widgetJustPrepared = false;
            // System.out.println("'widgetPrepared' reset");
        }
    };

//    /**
//     * Fires an additional {@link SWT#MouseMove MouseMove} {@link Event} one unit in left of the
//     * position given in <code>inputEvent</code>.<br>
//     * <br>
//     * See {@link org.eclipse.swt.widgets.Display#post(Event) Display#post(Event)} for details on that.
//     */
//    private void injectMoveLeftEvent(final InputEvent inputEvent) {
//        final MouseEvent sourceEvent = ((KlighdMouseEvent) inputEvent).getEvent();
//        final Point canvasLocation = ((Control) sourceEvent.widget).toDisplay(0, 0);
//
//        final Event event = new Event();
//        event.type = SWT.MouseMove;
//        event.x = canvasLocation.x + sourceEvent.x - 1;
//        event.y = canvasLocation.y + sourceEvent.y;
//
//        sourceEvent.display.post(event);
//    }
//
//    /**
//     * Fires an additional {@link Event} event one unit at the position given in
//     * <code>inputEvent</code>. This is used for "forwarding" events delivered to the canvas to the
//     * label text widget.<br>
//     * <br>
//     * See {@link org.eclipse.swt.widgets.Display#post(Event) Display#post(Event)} for details on that.
//     */
//    private void forwardEventToLabel(final InputEvent input) {
//        final KlighdMouseEvent kme = (KlighdMouseEvent) input;
//        final MouseEvent sourceEvent = kme.getEvent();
//        final int eventType = kme.getEventType();
//
//        final Event event = new Event();
//        event.button = sourceEvent.button;
//        event.type = eventType;
//
//        if (eventType == SWT.MouseMove) {
//            final Point canvasLocation = ((Control) sourceEvent.widget).toDisplay(0, 0);
//            event.x = sourceEvent.x + canvasLocation.x;
//            event.y = sourceEvent.y + canvasLocation.y;
//        }
//
//        sourceEvent.display.post(event);
//    }
}
