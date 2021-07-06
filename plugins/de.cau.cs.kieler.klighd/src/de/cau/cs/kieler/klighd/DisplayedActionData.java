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
package de.cau.cs.kieler.klighd;

import java.awt.Image;

import com.google.common.base.Predicate;

/**
 * A concise helper record class accommodating the required information for
 * {@link de.cau.cs.kieler.klighd.IAction IAction}'s offered in the UI's side bar.
 * 
 * @author chsch
 * 
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch
 */
public final class DisplayedActionData {
    

    /**
     * Static factory method providing an {@link DisplayedActionData} instance without tooltip and image.
     * Its execution is always enabled.
     *  
     * @param actionId
     *            the referenced {@link de.cau.cs.kieler.klighd.IAction IAction}'s id
     * @param displayedName
     *            the name displayed on the UI control
     * @return the desired {@link DisplayedActionData} record
     */
    public static DisplayedActionData create(final String actionId, final String displayedName) {
        return new DisplayedActionData(actionId, displayedName, null, null, null);
    }


    /**
     * Static factory method providing an {@link DisplayedActionData} instance without tooltip and image.
     * Its execution is always enabled.
     *  
     * @param actionId
     *            the referenced {@link de.cau.cs.kieler.klighd.IAction IAction}'s id
     * @param displayedName
     *            the name displayed on the UI control
     * @param enablement
     *            a {@link Predicate} implementing the enablement decision based one the
     *            {@link de.cau.cs.kieler.klighd.IViewer IViewer}'s current selection.
     * @return the desired {@link DisplayedActionData} record
     */
    public static DisplayedActionData create(final String actionId, final String displayedName,
            final Predicate<IKlighdSelection> enablement) {
        return new DisplayedActionData(actionId, displayedName, null, null, enablement);
    }


    /**
     * Static factory method providing an {@link DisplayedActionData} instance without tooltip and image.
     * Its execution is always enabled.
     *  
     * @param actionId
     *            the referenced {@link de.cau.cs.kieler.klighd.IAction IAction}'s id
     * @param displayedName
     *            the name displayed on the UI control
     * @param tooltipText
     *            the tooltip text displayed on the UI control
     * @return the desired {@link DisplayedActionData} record
     */
    public static DisplayedActionData create(final String actionId, final String displayedName,
            final String tooltipText) {
        return new DisplayedActionData(actionId, displayedName, tooltipText, null, null);
    }


    /**
     * Static factory method providing an {@link DisplayedActionData} instance without tooltip and image.
     * Its execution is always enabled.
     *  
     * @param actionId
     *            the referenced {@link de.cau.cs.kieler.klighd.IAction IAction}'s id
     * @param displayedName
     *            the name displayed on the UI control
     * @param tooltipText
     *            the tooltip text displayed on the UI control
     * @param enablement
     *            a {@link Predicate} implementing the enablement decision based one the
     *            {@link de.cau.cs.kieler.klighd.IViewer IViewer}'s current selection.
     * @return the desired {@link DisplayedActionData} record
     */
    public static DisplayedActionData create(final String actionId, final String displayedName,
            final String tooltipText, final Predicate<IKlighdSelection> enablement) {
        return new DisplayedActionData(actionId, displayedName, tooltipText, null, enablement);
    }


    /**
     * Static factory method providing an {@link DisplayedActionData} instance without tooltip and image.
     * Its execution is always enabled.
     *  
     * @param actionId
     *            the referenced {@link de.cau.cs.kieler.klighd.IAction IAction}'s id
     * @param displayedName
     *            the name displayed on the UI control
     * @param image
     *            the image displayed on the UI control
     * @return the desired {@link DisplayedActionData} record
     */
    public static DisplayedActionData create(final String actionId, final String displayedName,
            final Image image) {
        return new DisplayedActionData(actionId, displayedName, null, image, null);
    }


    /**
     * Static factory method providing an {@link DisplayedActionData} instance without tooltip and image.
     * Its execution is always enabled.
     *  
     * @param actionId
     *            the referenced {@link de.cau.cs.kieler.klighd.IAction IAction}'s id
     * @param displayedName
     *            the name displayed on the UI control
     * @param image
     *            the image displayed on the UI control
     * @param enablement
     *            a {@link Predicate} implementing the enablement decision based one the
     *            {@link de.cau.cs.kieler.klighd.IViewer IViewer}'s current selection.
     * @return the desired {@link DisplayedActionData} record
     */
    public static DisplayedActionData create(final String actionId, final String displayedName,
            final Image image, final Predicate<IKlighdSelection> enablement) {
        return new DisplayedActionData(actionId, displayedName, null, image, enablement);
    }


    /**
     * Static factory method providing an {@link DisplayedActionData} instance without tooltip and image.
     * Its execution is always enabled.
     *  
     * @param actionId
     *            the referenced {@link de.cau.cs.kieler.klighd.IAction IAction}'s id
     * @param displayedName
     *            the name displayed on the UI control
     * @param tooltipText
     *            the tooltip text displayed on the UI control
     * @param image
     *            the image displayed on the UI control
     * @return the desired {@link DisplayedActionData} record
     */
    public static DisplayedActionData create(final String actionId, final String displayedName,
            final String tooltipText, final Image image) {
        return new DisplayedActionData(actionId, displayedName, tooltipText, image, null);
    }


    /**
     * Static factory method providing an {@link DisplayedActionData} instance without tooltip and image.
     * Its execution is always enabled.
     *  
     * @param actionId
     *            the referenced {@link de.cau.cs.kieler.klighd.IAction IAction}'s id
     * @param displayedName
     *            the name displayed on the UI control
     * @param tooltipText
     *            the tooltip text displayed on the UI control
     * @param image
     *            the image displayed on the UI control
     * @param enablement
     *            a {@link Predicate} implementing the enablement decision based one the
     *            {@link de.cau.cs.kieler.klighd.IViewer IViewer}'s current selection.
     * @return the desired {@link DisplayedActionData} record
     */
    public static DisplayedActionData create(final String actionId, final String displayedName,
            final String tooltipText, final Image image, final Predicate<IKlighdSelection> enablement) {
        return new DisplayedActionData(actionId, displayedName, tooltipText, image, enablement);
    }

    // we don't getters since the following fields are final so we can set them 'public' and state...
    // SUPPRESS CHECKSTYLE NEXT 6 Visibility|Javadoc

    public final String actionId;
    public final String displayedName;
    public final String tooltipText;
    public final Image image;
    public final Predicate<IKlighdSelection> enablementTester;


    /**
     * Constructor.
     * 
     * @param actionId
     *            the referenced {@link de.cau.cs.kieler.klighd.IAction IAction}'s id
     * @param displayedName
     *            the name displayed on the UI control
     * @param tooltipText
     *            the tooltip text displayed on the UI control
     * @param image
     *            the {@link Image} to be displayed on the UI control
     * @param enablement
     *            a {@link Predicate} implementing the enablement decision based one the
     *            {@link de.cau.cs.kieler.klighd.IViewer IViewer}'s current selection.
     */
    public DisplayedActionData(final String actionId, final String displayedName,
            final String tooltipText, final Image image, final Predicate<IKlighdSelection> enablement) {
        this.actionId = actionId;
        this.displayedName = displayedName;
        this.tooltipText = tooltipText;
        this.image = image;
        this.enablementTester = enablement;
    }
}

