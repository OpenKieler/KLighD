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
package de.cau.cs.kieler.klighd.piccolo.internal.events;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.swt.widgets.Widget;

import de.cau.cs.kieler.klighd.KlighdPreferences;
import de.cau.cs.kieler.klighd.eclipse.EclipseKlighdPreferences;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PPanEventHandler;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDimension;

/**
 * A specialization of {@link PPanEventHandler} with some customizations in the configuration and
 * non-configurable code. The latter requires some method overriding.
 *
 * @author chsch
 */
public class KlighdPanEventHandler extends PPanEventHandler {

    /** Our minimal autopan speed as opposed to {@link PPanEventHandler#DEFAULT_MIN_AUTOPAN_SPEED}. */
    private static final int KLIGHD_MIN_AUTOPAN_SPEED = 100;

    /** Our maximal autopan speed as opposed to {@link PPanEventHandler#DEFAULT_MAX_AUTOPAN_SPEED}. */
    private static final int KLIGHD_MAX_AUTOPAN_SPEED = 2000;

    /**
     * Constructor.
     *
     * @param widget
     *            an SWT {@link Widget} corresponding to the current diagram, required only for
     *            reacting on its disposal in order to cleanup installed change listeners
     */
    public KlighdPanEventHandler(final Widget widget) {
        super();

        this.setMinAutopanSpeed(KLIGHD_MIN_AUTOPAN_SPEED);
        this.setMaxAutopanSpeed(KLIGHD_MAX_AUTOPAN_SPEED);

        setAutopan(KlighdPreferences.isAdvancedPanningMode());

        EclipseKlighdPreferences.registerPrefChangeListener(widget, new IPreferenceChangeListener() {

            @Override
            public void preferenceChange(final PreferenceChangeEvent event) {
                if (KlighdPreferences.ADVANCED_PANNING_MODE.equals(event.getKey())) {
                    setAutopan(KlighdPreferences.isAdvancedPanningMode());
                }
            }
        });
    }

    @Override
    protected boolean shouldStartDragInteraction(final PInputEvent event) {
        return getAutopan() && !event.isControlDown() && super.shouldStartDragInteraction(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseDragged(final PInputEvent event) {
        // This method has been overridden in order to enable the call of 'drag' in case
        //  'isDragging()' returns 'false', which always the case if auto panning if off
        //  (which is the default setting), see 'shouldStartDragInteraction(...)' above.
        // In case auto panning is active, I assume that a proper 'mouse pressed' is received first,
        //  which will put the handler in dragging mode and enable the employed drag activity.

        if (event.isControlDown()) {
            // in this case the KlighdSelectiveZoomEventHandler or the
            //  KlighdSelectionEventHandler are in charge of handling 'event'
            return;
        } else {
            drag(event);
        }
    }

    @Override
    protected void pan(final PInputEvent event) {
        // The reason for overriding this method is the replacement of
        //  'event.getCamera()' by 'event.getTopCamera()' and
        //  'event.getDelta' by 'event.getCanvasDelta()' -> 'cam.localToView(delta)'

        final PCamera cam = event.getTopCamera();
        final Point2D cp = event.getInputManager().getCurrentCanvasPosition();

        if (!getAutopan() || cam.getBoundsReference().contains(cp)) {
            final PDimension delta = event.getCanvasDelta();

            cam.localToView(delta);
            cam.translateView(delta.width, delta.height);

            event.setHandled(true);
        }
    }


    @Override
    protected void dragActivityStep(final PInputEvent event) {
        // The reason for overriding this method is the replacement of
        //  'event.getCamera()' by 'event.getTopCamera()'.
        // Besides some simplification have been done as our top camera always starts at (x,y) = (0,0).

        final PCamera c = event.getTopCamera();
        final PBounds b = c.getBoundsReference();
        final Point2D.Double l = (Point2D.Double) event.getCanvasPosition();

        final int outcode = b.outcode(l);
        final PDimension delta = new PDimension();

        // SUPPRESS CHECKSTYLE NEXT 15 MagicNumber

        if ((outcode & Rectangle2D.OUT_TOP) != 0) {
            delta.height = validatePanningSpeed(-1.0 - 0.5 * Math.abs(l.y));

        } else if ((outcode & Rectangle2D.OUT_BOTTOM) != 0) {
            delta.height = validatePanningSpeed(1.0 + 0.5 * Math.abs(l.y - b.height));
        }

        if ((outcode & Rectangle2D.OUT_RIGHT) != 0) {
            delta.width = validatePanningSpeed(1.0 + 0.5 * Math.abs(l.x - b.width));

        } else if ((outcode & Rectangle2D.OUT_LEFT) != 0) {
            delta.width = validatePanningSpeed(-1.0 - 0.5 * Math.abs(l.x));
        }

        c.localToView(delta);

        if (delta.width != 0 || delta.height != 0) {
            c.translateView(delta.width, delta.height);
        }
    }
}
