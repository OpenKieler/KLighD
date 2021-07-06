/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2015, 2017 by
 * + Christian-Albrechts-University of Kiel
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
import de.cau.cs.kieler.klighd.labels.management.SoftWrappingLabelManager;

/**
 * Label manager which soft wraps the text so it fits a certain width. Soft wrapping only inserts
 * line breaks between words. If a word is longer than the target width, the width of the longest
 * word is used as the target width instead.
 * 
 * <p>
 * It does not make sense to put this label manager into {@link Mode#ALWAYS_ON}. If a label does not
 * exceed a target width, there's no sense in inserting wraps anywhere.
 * </p>
 * 
 * @author ybl
 * @author cds
 */
public class EclipseSoftWrappingLabelManager extends SoftWrappingLabelManager {

    @Override
    public Result doResizeLabel(final ElkLabel label, final double targetWidth) {
        final FontData font = EclipseLabelManagementUtil.fontDataFor(label);
        
        if (EclipsePlacementUtil.estimateTextSize(font, label.getText()).getWidth() > targetWidth) {
            String textWithoutLineBreaks = label.getText().replace("\n", " ");

            // Divide the text into "words"
            String[] words = textWithoutLineBreaks.split(" ");
            StringBuilder resultText = new StringBuilder(label.getText().length());
            String currentLineText;
            double effectiveTargetWidth =
                    Math.max(EclipseLabelManagementUtil.getWidthOfBiggestWord(font, words), targetWidth);

            // iterate over the lines
            int currWordIndex = 0;
            while (currWordIndex < words.length) {
                currentLineText = words[currWordIndex];
                String testText = currentLineText;

                // Test if another word fits into this line
                double lineWidth = 0;
                do {
                    currentLineText = testText;
                    if (currWordIndex < words.length - 1) {
                        testText = currentLineText + " " + words[++currWordIndex];
                    } else {
                        testText = " ";
                        currWordIndex++;
                    }
                    lineWidth = EclipsePlacementUtil.estimateTextSize(font, testText).getWidth();
                } while (lineWidth < effectiveTargetWidth && currWordIndex < words.length);

                // No more words fit so the line is added to the result
                if (currWordIndex < words.length) {
                    resultText.append(currentLineText).append("\n");
                } else {
                    resultText.append(currentLineText);
                    break;
                }
            }
            
            return Result.modified(resultText.toString());
            
        } else {
            // We label wasn't too long
            return Result.unmodified();
        }
        
        
        
    }

}
