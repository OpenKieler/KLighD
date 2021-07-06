/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2015, 2017 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klighd.eclipse.labels.management;

import org.eclipse.elk.graph.ElkLabel;
import org.eclipse.swt.graphics.FontData;

import de.cau.cs.kieler.klighd.eclipse.microlayout.EclipsePlacementUtil;
import de.cau.cs.kieler.klighd.labels.management.LabelManagementUtil;
import de.cau.cs.kieler.klighd.labels.management.TruncatingLabelManager;
import de.cau.cs.kieler.klighd.microlayout.Bounds;
import de.cau.cs.kieler.klighd.microlayout.PlacementUtil;

/**
 * Modifies the size of labels by truncating them once the truncation condition is met. The
 * truncation condition can bei either reaching the end of the first line of text, reaching the end
 * of a given number of words, or reaching the target width (default). The label manager can be
 * configured as to whether it should append an ellipsis to the truncated text to indicate
 * truncation (by default it does).
 * 
 * <p>
 * It does not make sense to put this label manager into {@link TruncationMode#ALWAYS_ON} if it is set to
 * truncate after reaching the target width. If a label does not exceed a target width, there's no
 * sense in truncating anything.
 * </p>
 * 
 * @author ybl
 * @author cds
 */
public class EclipseTruncatingLabelManager extends TruncatingLabelManager {
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Static Things

    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Variables and Configuration
     

    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Label Management Entry Point

    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // First Line Implementation

    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // First Words Implementation

    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Target Width Implementation
    
    /**
     * Implementation based on the target width.
     */
    private Result targetWidth(final ElkLabel label, final double targetWidth) {
        final FontData labelFontData = EclipseLabelManagementUtil.fontDataFor(label);
        String calculatedText = "";
        
        // Size of three ellipses, if they should be added
        final float ellipseWidth = appendEllipsis
                ? EclipsePlacementUtil.estimateTextSize(labelFontData, ELLIPSES).getWidth()
                : 0;

        // If there is enough space for some text and ellipses, calculate the fitting text
        if (targetWidth > ellipseWidth) {
            calculatedText = EclipseLabelManagementUtil.findFittingString(label.getText(),
                    EclipseLabelManagementUtil.fontDataFor(label), targetWidth - ellipseWidth);

            // Delete whitespaces
            calculatedText = calculatedText.trim();                
        }
        
        return ellipsifyIfRequired(calculatedText);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Utility Methods

    /**
     * Checks whether action is required on our part.
     */
    private boolean doWeActuallyHaveToGoAndDoStuff(final ElkLabel label, final double targetWidth) {
        if (getMode() == Mode.ALWAYS_ON && truncationMode != TruncationMode.TARGET_WIDTH) {
            // We always have to do something if we have to truncate after the first line or number
            // of words in "always on" mode
            return true;
            
        } else {
            // In all other cases whether we need to do stuff depends on whether the label exceeds
            // the supplied target width
            Bounds textBounds = EclipsePlacementUtil.estimateTextSize(
                    EclipseLabelManagementUtil.fontDataFor(label), label.getText());
            return textBounds.getWidth() > targetWidth;
        }
    }
    
    /**
     * Returns a modified result with either the naked text or the text with an ellipsis appended,
     * depending on the label manager's settings.
     */
    private Result ellipsifyIfRequired(final String text) {
        if (appendEllipsis) {
            return Result.modified(text + ELLIPSES);
        } else {
            return Result.modified(text);
        }
    }

}
