/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://rtsys.informatik.uni-kiel.de/kieler
 * 
 * Copyright 2015 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 */
package de.cau.cs.kieler.klighd.labels.management;

import java.awt.Font;
import java.util.Iterator;

import org.eclipse.elk.graph.ElkLabel;

import com.google.common.collect.Iterators;

import de.cau.cs.kieler.klighd.KlighdOptions;
import de.cau.cs.kieler.klighd.krendering.KRendering;
import de.cau.cs.kieler.klighd.krendering.KRenderingOptions;
import de.cau.cs.kieler.klighd.krendering.KRenderingUtil;
import de.cau.cs.kieler.klighd.krendering.KText;
import de.cau.cs.kieler.klighd.microlayout.Bounds;
import de.cau.cs.kieler.klighd.microlayout.PlacementUtil;

/**
 * Label management utility methods.
 * 
 * @author cds
 */
public final class LabelManagementUtil {
    
    /**
     * This class is not supposed to be instantiated.
     */
    private LabelManagementUtil() {
        
    }

    
    /**
     * Returns the KText that controls the rendering for the given label. The rendering is found by
     * inspecting the {@link KlighdOptions#K_RENDERING} property.
     * 
     * @param label
     *            the label whose KText to retrieve.
     * @return the KText or {@code null} if none could be found.
     */
    public static KText ktextFor(final ElkLabel label) {
        // Try finding the KText we will inspect for the font
        KRendering rootRendering = label.getProperty(KRenderingOptions.K_RENDERING);
        
        final Iterator<KText> kTexts = Iterators.filter(
                KRenderingUtil.selfAndAllChildren(rootRendering), KText.class);
        return Iterators.getNext(kTexts, null);
    }
    
    // XXX
    /**
     * Determines the font data associated with the given label. This method requires the label to
     * have the {@link KlighdOptions#K_RENDERING} property set to the rendering which will be used
     * to actually render the label later.
     * 
     * @param label
     *            the label whose font data to retrieve.
     * @return the font data.
     */
    public static Font fontFor(final ElkLabel label) {
        return PlacementUtil.fontFor(ktextFor(label));
    }

    /**
     * Determines the first part of the String which fits in a certain width depending on the font.
     * 
     * @param text
     *            the text which is supposed to be shortened.
     * @param fontData
     *            the font the text will be rendered with.
     * @param targetWidth
     *            the width the text is supposed to fit in.
     * @return the part of the text which fits in the target width.
     */
    public static String findFittingString(final String text, final Font font,
            final double targetWidth) {
        
        String textWithoutWraps = text.replace("\n", " ");
        Bounds newSize = PlacementUtil.estimateTextSize(font, textWithoutWraps);
        String newText = "";

        // Guess how many characters will fit into the target width (and make sure it's not more
        // than the number of characters we actually have)
        int newTextLength = (int) (targetWidth / (newSize.getWidth() / textWithoutWraps.length()));
        newTextLength = Math.min(newTextLength, textWithoutWraps.length());

        // Shorten the text accordingly and calculate its bounds
        newText = textWithoutWraps.substring(0, newTextLength);
        newSize = PlacementUtil.estimateTextSize(font, newText);

        if (newSize.getWidth() > targetWidth) {
            // Text is still to long and some more characters have to go
            while (newSize.getWidth() > targetWidth && newTextLength > 1) {
                newTextLength--;
                newText = textWithoutWraps.substring(0, newTextLength);
                newSize = PlacementUtil.estimateTextSize(font, newText);
            }
        } else {
            // There is some space for more characters
            while (newSize.getWidth() < targetWidth
                    && newTextLength < textWithoutWraps.length() - 1) {
                
                newTextLength++;
                String newTextCandidate = textWithoutWraps.substring(0, newTextLength);
                newSize = PlacementUtil.estimateTextSize(font, newTextCandidate);
                
                if (newSize.getWidth() <= targetWidth) {
                    newText = newTextCandidate;
                }
            }
        }
        
        return newText;
    }

    /**
     * Find the String with the biggest width according to a font in string array.
     * 
     * @param font
     *            font of the text used to estimate the text size.
     * @param words
     *            words from which the biggest one is searched.
     * @return biggest word.
     */
    public static float getWidthOfBiggestWord(final Font font, final String[] words) {
        Bounds bWSize = PlacementUtil.estimateTextSize(font, "");
        Bounds textPartSize;

        for (String textpart : words) {
            textPartSize = PlacementUtil.estimateTextSize(font, textpart);
            if (bWSize.getWidth() < textPartSize.getWidth()) {
                bWSize = textPartSize;
            }
        }
        
        return bWSize.getWidth();
    }
    
}
