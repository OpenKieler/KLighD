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
package de.cau.cs.kieler.klighd.piccolo.internal.controller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import de.cau.cs.kieler.klighd.kgraph.KLabel;
import de.cau.cs.kieler.klighd.krendering.KRendering;
import de.cau.cs.kieler.klighd.krendering.KRenderingUtil;
import de.cau.cs.kieler.klighd.krendering.KText;
import de.cau.cs.kieler.klighd.microlayout.Bounds;
import de.cau.cs.kieler.klighd.microlayout.PlacementUtil;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KLabelNode;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KlighdStyledText;
import edu.umd.cs.piccolo.PNode;

/**
 * @author mri, chsch
 */
public class KLabelRenderingController extends AbstractKGERenderingController<KLabel, KLabelNode> {

    /** whether figure descriptions shall be checked for at most one {@link KText} element. */
    private final boolean validateSingleKText;

    /**
     * Constructs a rendering controller for a label.
     * 
     * @param label
     *            the Piccolo node representing a label
     * @param multipleKTextsPerKLabel
     *            whether figure descriptions of KLabels may contain multiple KTexts.
     */
    public KLabelRenderingController(final KLabelNode label, final boolean multipleKTextsPerKLabel) {
        super(label.getViewModelElement(), label);
        this.validateSingleKText = !multipleKTextsPerKLabel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PNodeController<?> internalUpdateRendering() {
        final KLabelNode repNode = getRepresentation();

        // evaluate the rendering data
        final KRendering currentRendering = getCurrentRendering();

        final PNodeController<?> renderingNodeController;
        if (currentRendering != null) {
            renderingNodeController = handleLabelRendering(currentRendering, repNode);
        } else {
            renderingNodeController = handleLabelRendering(createDefaultRendering(), repNode);
        }

        return renderingNodeController;
    }

    /**
     * Creates the Piccolo node for a rendering of a {@code KLabel} inside a parent Piccolo node.<br>
     * <br>
     * The rendering has to be a {@code KText} or the method fails.
     * 
     * @param rendering
     *            the rendering
     * @param parent
     *            the parent Piccolo label node
     * @return the {@link PNodeController} managing the Piccolo2D node that represents
     *         <code>rendering</code>
     */
    private PNodeController<?> handleLabelRendering(final KRendering rendering,
            final KLabelNode parent) {
        // the rendering of a label has to contain exactly one KText
        //  that "inherits" the text from the KLabel itself
        final Iterator<KText> kTexts =
                Iterators.filter(KRenderingUtil.selfAndAllChildren(rendering), KText.class);
        
        final KText kText = Iterators.getNext(kTexts, null); 
        
        if (kText == null || validateSingleKText && kTexts.hasNext()) {
            throw new RuntimeException("KLabel " + getGraphElement()
                    + " must (deeply) contain exactly 1 KText element.");
        }
        
        // create the rendering
        final PNodeController<?> controller =
                createRendering(rendering, parent, Bounds.of(parent.getBoundsReference()));
        
        @SuppressWarnings("unchecked")
        final PNodeController<KlighdStyledText> textController = (PNodeController<KlighdStyledText>)
                Iterables.getFirst(this.getPNodeController(kText), null);
        
        if (textController != null) {
            // the opposite should never happen (see test above), this test is just for preventing
            //  null pointer exceptions
            
            textController.getNode().setText(
                    parent.getText() != null ? parent.getText() : kText.getText());
            
            // add a listener on the parent's bend points
            addListener(KLabelNode.PROPERTY_TEXT, parent, controller.getNode(),
                    new PropertyChangeListener() {

                        public void propertyChange(final PropertyChangeEvent e) {
                            textController.getNode().setText(
                                    parent.getText() != null ? parent.getText() : kText.getText());
                            textController.getNode().repaint();
                        }
                    });
        }

        // add a listener on the parent's bounds
        addListener(PNode.PROPERTY_BOUNDS, parent, controller.getNode(),
                new PropertyChangeListener() {

                    public void propertyChange(final PropertyChangeEvent e) {
                        // calculate the new bounds of the rendering
                        Bounds bounds = PlacementUtil.evaluateAreaPlacement(KRenderingUtil
                                .asAreaPlacementData(KRenderingUtil.getPlacementData(rendering)),
                                parent.getBoundsReference());
                        
                        // use the controller to apply the new bounds
                        controller.setBounds(bounds);
                    }
                });

        return controller;
    }
}
