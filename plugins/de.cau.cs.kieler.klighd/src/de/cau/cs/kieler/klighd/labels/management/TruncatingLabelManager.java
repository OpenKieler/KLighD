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
package de.cau.cs.kieler.klighd.labels.management;

import java.awt.Font;
import java.util.List;

import org.eclipse.elk.graph.ElkLabel;

import com.google.common.collect.Lists;

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
public class TruncatingLabelManager extends AbstractKlighdLabelManager {
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Static Things
    
    /**
     * The possible modes the label manager can be in.
     */
    public static enum TruncationMode {
        /** Truncate such that only the first line of text remains. */
        FIRST_LINE,
        /** Truncate such that only the first n words remain. */
        FIRST_WORDS,
        /** Truncate once reaching the target width. */
        TARGET_WIDTH
    }
    
    /** The string appended to a truncated label text. */
    protected static final String ELLIPSES = "...";

    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Variables and Configuration
    
    /** The mode we're in. Some of those use additional configuration variables. */
    protected TruncationMode truncationMode = TruncationMode.TARGET_WIDTH;
    /** Whether to append an ellipsis to truncated text. */
    protected boolean appendEllipsis = true;
    /** How many words should survive truncation in {@link TruncationMode#FIRST_WORDS} mode. */
    private int wordsToSurvive = 1;
    
    
    /**
     * Configures the label manager to truncate label text after the first line.
     * 
     * @return this label manager for method chaining.
     */
    public TruncatingLabelManager truncateAfterFirstLine() {
        this.truncationMode = TruncationMode.FIRST_LINE;
        return this;
    }
    
    /**
     * Configures the label manager to truncate label text after the given number of words.
     * 
     * @param words
     *            the number of words that should remain after truncation. Must be {@code > 0}.
     * @return this label manager for method chaining.
     * @throws IllegalArgumentException
     *             if {@code words <= 0}.
     */
    public TruncatingLabelManager truncateAfterFirstWords(final int words) {
        if (words <= 0) {
            throw new IllegalArgumentException("words must be > 0.");
        }
        
        this.truncationMode = TruncationMode.FIRST_WORDS;
        this.wordsToSurvive = words;
        return this;
    }
    
    /**
     * Configures the label manager to truncate label text after the target width.
     * 
     * @return this label manager for method chaining.
     */
    public TruncatingLabelManager truncateAfterTargetWidth() {
        this.truncationMode = TruncationMode.TARGET_WIDTH;
        return this;
    }
    
    /**
     * Configures the label manager to append an ellipsis to truncated text or not.
     * 
     * @param ellipsis
     *            {@code true} if an ellipsis should be appended.
     * @return this label manager for method chaining.
     */
    public TruncatingLabelManager appendEllipsisToTruncatedText(final boolean ellipsis) {
        this.appendEllipsis = ellipsis;
        return this;
    }
    
    /**
     * Return which truncation mode we're in.
     * 
     * @return the truncation mode.
     */
    public TruncationMode getTruncationMode() {
        return truncationMode;
    }

    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Label Management Entry Point
    
    @Override
    public Result doResizeLabel(final ElkLabel label, final double targetWidth) {
        // Check whether we should actuall do anything
        if (!doWeActuallyHaveToGoAndDoStuff(label, targetWidth)) {
            return Result.unmodified();
        }
        
        switch (truncationMode) {
        case FIRST_LINE:
            return firstLine(label, targetWidth);
            
        case FIRST_WORDS:
            return firstWords(label, targetWidth);
            
        case TARGET_WIDTH:
            return targetWidth(label, targetWidth);
            
        default:
            assert false;
            return Result.unmodified();
        }
    }

    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // First Line Implementation
    
    /**
     * Implementation that returns only the first line.
     */
    private Result firstLine(final ElkLabel label, final double targetWidth) {
        int lineBreakIndex = label.getText().indexOf('\n');
        
        if (lineBreakIndex == -1) {
            return Result.unmodified();
        } else {
            String newText = label.getText().substring(0, lineBreakIndex);
            return ellipsifyIfRequired(newText);
        }
    }

    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // First Words Implementation
    
    /**
     * Implementation the returns only the first n words.
     */
    private Result firstWords(final ElkLabel label, final double targetWidth) {
        // Split the string at whitespace boundaries (both horizontal and vertical). We want at
        // most words+1 results since the last result contains the unmatched rest of the string
        List<String> components = Lists.newArrayList(
                label.getText().split("[\\h\\v]+", wordsToSurvive + 1));
        
        // If there are more components than words that should survive, we have an unmatched
        // remainder. Remove that. Nobody wants that.
        if (components.size() > wordsToSurvive) {
            components.remove(wordsToSurvive);
        }
        
        return ellipsifyIfRequired(String.join(" ", components));
    }

    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Target Width Implementation
    
    /**
     * Implementation based on the target width.
     */
    private Result targetWidth(final ElkLabel label, final double targetWidth) {
        final Font labelFont = LabelManagementUtil.fontFor(label);
        String calculatedText = "";
        
        // Size of three ellipses, if they should be added
        final float ellipseWidth = appendEllipsis
                ? PlacementUtil.estimateTextSize(labelFont, ELLIPSES).getWidth()
                : 0;

        // If there is enough space for some text and ellipses, calculate the fitting text
        if (targetWidth > ellipseWidth) {
            calculatedText = LabelManagementUtil.findFittingString(label.getText(),
                    LabelManagementUtil.fontFor(label), targetWidth - ellipseWidth);

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
            Bounds textBounds = PlacementUtil.estimateTextSize(
                    LabelManagementUtil.fontFor(label), label.getText());
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
