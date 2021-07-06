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
package de.cau.cs.kieler.klighd.krendering;

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;


/**
 * Some convenience methods supporting the proper handling of KText renderings.
 * 
 * @author chsch
 * 
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch 
 */
public final class KTextUtil {

    /**
     * Hidden default constructor.
     */
    private KTextUtil() {
    }

    /**
     * Method is supposed to realize the platform independent splitting of the (potentially)
     * multi-lined strings of KText renderings into lists of the separate lines.<br>
     * In addition, leading or trailing spaces of each line are removed. Delegates to
     * {@link #getTextLines(String)}.
     * 
     * @param kText
     *            the KText rendering whose separate lines are queried
     * @return the lines split up at all known valid line separators
     */
    public static List<String> getTextLines(final KText kText) {
        if (kText == null || Strings.isNullOrEmpty(kText.getText())) {
            return Arrays.asList("");
        }
        return getTextLines(kText.getText());
    }

    /**
     * Method is supposed to realize the platform independent splitting of (potentially) multi-lined
     * strings into lists of the separate lines.<br>
     * In addition, leading or trailing spaces of each line are removed.
     * 
     * @param text
     *            the String whose separate lines are queried
     * @return the lines split up at all known valid line separators
     */
    public static List<String> getTextLines(final String text) {
        if (Strings.isNullOrEmpty(text)) {
            return Arrays.asList("");
        }
        return Lists.transform(Arrays.asList(text.split("\\r?\\n|\\r")), TRIM);
    }
    
    private static final Function<String, String> TRIM = new Function<String, String>() {

        /**
         * {@inheritDoc}
         */
        public String apply(final String input) {
            return input.trim();
        }
    };


    /**
     * @param style
     *            the SWT bitwise pattern of NORMAL, ITALIC and BOLD.
     * @return the corresponding AWT pattern.
     */
    public static int swtFontStyle2Awt(final int style) {
        // the constants in SWT and AWT are the same, hence just return it.
        return style;
    }
}
