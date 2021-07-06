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
package de.cau.cs.kieler.klighd.eclipse;

import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Widget;

import de.cau.cs.kieler.klighd.KlighdPreferences;

/**
 * IDs and default values of preferences defined by KLighD and stored in the preference store.
 *
 * @author cds
 * @author chsch
 *
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch
 */
public final class EclipseKlighdPreferences {

    /**
     * Registers the given {@link IPreferenceChangeListener}.
     *
     * @param widget
     *            an SWT {@link Widget} onto which a {@link DisposeListener} is installed to for
     *            properly removing the preference change listener once it is not required anymore,
     *            may be <code>null</code>
     * @param listener
     *            an {@link IPreferenceChangeListener} performing internal updates
     */
    public static void registerPrefChangeListener(final Widget widget,
            final IPreferenceChangeListener listener) {
        KlighdPreferences.STORE.addPreferenceChangeListener(listener);

        if (widget == null) {
            return;
        }

        widget.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(final DisposeEvent e) {
                KlighdPreferences.STORE.removePreferenceChangeListener(listener);
                e.widget.removeDisposeListener(this);
            }
        });
    }
}
