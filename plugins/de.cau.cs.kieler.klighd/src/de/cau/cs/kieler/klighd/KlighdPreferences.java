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
package de.cau.cs.kieler.klighd;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.osgi.framework.Bundle;

import com.google.common.base.Strings;

import de.cau.cs.kieler.klighd.internal.preferences.KlighdPlainPreferenceStore;

/**
 * IDs and default values of preferences defined by KLighD and stored in the preference store.
 *
 * @author cds
 * @author chsch
 *
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch
 */
public final class KlighdPreferences {

    /** ID of the Animate Layout preference. */
    public static final String ANIMATE_LAYOUT =
            "de.cau.cs.kieler.klighd.preferences.animateLayout"; //$NON-NLS-1$

    /** Default value of the Animate Layout preference. */
    public static final boolean ANIMATE_LAYOUT_DEFAULT = true;

    /** ID of the zoom style preference. */
    public static final String ZOOM_STYLE =
            "de.cau.cs.kieler.klighd.preferences.zoomStyle"; //$NON-NLS-1$

    /** Default zoom style value. */
    public static final String ZOOM_STYLE_DEFAULT = ZoomStyle.ZOOM_TO_FIT.name();

    /**
     * ID of the 'zoom to focus' style preference. This preference is (currently) not offered in the
     * preference page since it is intended just for programmatic application customization.
     */
    public static final String ZOOM_TO_FOCUS_STYLE =
            "de.cau.cs.kieler.klighd.preferences.zoomToFocusStyle";

    /** Default 'zoom to focus' style value. */
    public static final String ZOOM_TO_FOCUS_STYLE_DEFAULT =
            ZoomStyle.ZOOM_TO_FOCUS_OR_INCREASE_TO_FIT.name();

    /** ID of the 'zoom on workbench part change' preference. */
    public static final String ZOOM_ON_WORKBENCHPART_CHANGE =
            "de.cau.cs.kieler.klighd.preferences.zoomOnWorkBenchPartChange";

    /** Default 'zoom on workbench part change' value. */
    public static final boolean ZOOM_ON_WORKBENCHPART_CHANGE_DEFAULT = false;

    /** ID of the magnification lens enablement preference. */
    public static final String MAGNIFICATION_LENS_ENABLED =
            "de.cau.cs.kieler.klighd.preferences.magnificationLensEnabled";

    /** Default magnification lens enablement value. */
    public static final boolean MAGNIFICATION_LENS_ENABLED_DEFAULT = true;

    /** ID of the magnification lens width preference. */
    public static final String MAGNIFICATION_LENS_WIDTH =
            "de.cau.cs.kieler.klighd.preferences.magnificationLensWidth";

    /** Default magnification lens width value. */
    public static final int MAGNIFICATION_LENS_WIDTH_DEFAULT = 400;

    /** ID of the magnification lens height preference. */
    public static final String MAGNIFICATION_LENS_HEIGHT =
            "de.cau.cs.kieler.klighd.preferences.magnificationLensHeight";

    /** Default magnification lens height value. */
    public static final int MAGNIFICATION_LENS_HEIGHT_DEFAULT = 400;

    /** ID of the magnification lens scale preference, unit is percent. */
    public static final String MAGNIFICATION_LENS_SCALE =
            "de.cau.cs.kieler.klighd.preferences.magnificationLensScale";

    /** Default magnification lens scale value in percent. */
    public static final int MAGNIFICATION_LENS_SCALE_DEFAULT = 200;

    /**
     * ID of the advanced panning mode preference; advanced panning means diagram panning continues
     * if cursor leaves diagram area until in re-enters or button is released.
     */
    public static final String ADVANCED_PANNING_MODE =
            "de.cau.cs.kieler.klighd.preferences.advancedPanning";

    /** Default advanced panning mode setting. */
    public static final boolean ADVANCED_PANNING_MODE_DEFAULT = false;

    /** ID of the expand side bar preference. */
    public static final String EXPAND_SIDE_BAR =
            "de.cau.cs.kieler.klighd.preferences.expandSideBar";

    /** Default expand side bar setting. */
    public static final boolean EXPAND_SIDE_BAR_DEFAULT = true;

    /** ID of the show zoom buttons preference. */
    public static final String SHOW_ZOOM_CONFIG_BUTTONS =
            "de.cau.cs.kieler.klighd.preferences.showZoomConfigButtons";

    /** Default zoom buttons visibility. */
    public static final boolean SHOW_ZOOM_CONFIG_BUTTONS_DEFAULT = true;

    /**
     * ID of the minimal user zoom level preference. If set the tool user can only
     * "zoom out of the diagram" until the given level is reached.
     */
    public static final String USER_ZOOMING_MINIMAL_LEVEL =
            "de.cau.cs.kieler.klighd.preferences.userZoomingMinimalLevel";

    /** Default minimal user zoom level value. */
    public static final Number USER_ZOOMING_MINIMAL_LEVEL_DEFAULT = null;

    /**
     * ID of the maximal user zoom level preference. If set the tool user can only
     * "zoom into the diagram" until the given level is reached.
     */
    public static final String USER_ZOOMING_MAXIMAL_LEVEL =
            "de.cau.cs.kieler.klighd.preferences.userZoomingMaximalLevel";

    /** Default maximal user zoom level value. */
    public static final Number USER_ZOOMING_MAXIMAL_LEVEL_DEFAULT = null;


    /** The {@link IKlighdPreferenceStore} used for KLighD-specific preferences. */
    public static final IKlighdPreferenceStore STORE;

    static {
        IKlighdPreferenceStore theStore = new KlighdPlainPreferenceStore();
        if (Klighd.IS_PLATFORM_RUNNING) {
            final Bundle klighdUi = Platform.getBundle(Klighd.UI_PLUGIN_ID);
            if (klighdUi != null) {
                try {
                    final Class<?> clazz = klighdUi.loadClass(
                            Klighd.UI_PLUGIN_ID + ".internal.preferences.KlighdPlatformPreferenceStore");
                    theStore = (IKlighdPreferenceStore) clazz.getDeclaredField("INSTANCE").get(null);
                    
                } catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                    Klighd.log(new Status(IStatus.ERROR, Klighd.PLUGIN_ID, "Accessing the JFace-based preference store failed with an exception.", e));
                }
            }
        }
        STORE = theStore;
    }
    
    /**
     * This class cannot be instantiated.
     */
    private KlighdPreferences() {
    }

    public static IKlighdPreferenceStore getPreferenceStore() {
        return STORE;
    }

    /**
     * Getter.
     *
     * @return <code>true</code> if 'animated layout' is active, <code>false</code> otherwise.
     */
    public static boolean isAnimateLayout() {
        return STORE.getBoolean(ANIMATE_LAYOUT);
    }

    /**
     * Getter.
     *
     * @return the preferred {@link ZoomStyle}
     */
    public static ZoomStyle getPreferredZoomStyle() {
        return ZoomStyle.valueOf(STORE.getString(ZOOM_STYLE));
    }

    /**
     * Getter.
     *
     * @return the configured 'zoom to focus' {@link ZoomStyle}
     */
    public static ZoomStyle getZoomToFocusStyle() {
        return ZoomStyle.valueOf(STORE.getString(ZOOM_TO_FOCUS_STYLE));
    }

    /**
     * Getter.
     *
     * @return <code>true</code> if advanced panning is active, <code>false</code> otherwise.
     */
    public static boolean isAdvancedPanningMode() {
        return STORE.getBoolean(ADVANCED_PANNING_MODE);
    }

    /**
     * Getter.
     *
     * @return <code>true</code> if expand side bar is active, <code>false</code> otherwise.
     */
    public static boolean isExpandSideBar() {
        return STORE.getBoolean(EXPAND_SIDE_BAR);
    }

    /**
     * Getter.
     *
     * @return <code>true</code> if zoom buttons should be shown in the side bar or on the diagram
     *         canvas respectively, <code>false</code> otherwise.
     */
    public static boolean isShowZoomConfigButtons() {
        return STORE.getBoolean(SHOW_ZOOM_CONFIG_BUTTONS);
    }

    /**
     * Getter.
     *
     * @return <code>true</code> if the magnification lens is enabled, <code>false</code> otherwise.
     */
    public static boolean isMagnificationLensEnabled() {
        return STORE.getBoolean(MAGNIFICATION_LENS_ENABLED);
    }

    /**
     * Getter.
     *
     * @return <code>true</code> if the magnification lens is enabled, <code>false</code> otherwise.
     */
    public static boolean isZoomOnWorkbenchpartChange() {
        return STORE.getBoolean(ZOOM_ON_WORKBENCHPART_CHANGE);
    }

    /**
     * Getter.
     *
     * @param defaultValue
     *            the value to be returned in case no value is stored
     * @return the configured minimal zoom level the user is allowed to select, or <code>null</code>
     *         if none is configured.
     */
    public static double getUserZoomingMinimalLevel(final double defaultValue) {
        final String config = STORE.getString(USER_ZOOMING_MINIMAL_LEVEL);

        return Strings.isNullOrEmpty(config)
                ? defaultValue : STORE.getDouble(USER_ZOOMING_MINIMAL_LEVEL);
    }

    /**
     * Getter.
     *
     * @param defaultValue
     *            the value to be returned in case no value is stored
     * @return the configured maximal zoom level the user is allowed to select, or <code>null</code>
     *         if none is configured.
     */
    public static double getUserZoomingMaximalLevel(final double defaultValue) {
        final String config = STORE.getString(USER_ZOOMING_MAXIMAL_LEVEL);

        return Strings.isNullOrEmpty(config)
                ? defaultValue : STORE.getDouble(USER_ZOOMING_MAXIMAL_LEVEL);
    }
}
