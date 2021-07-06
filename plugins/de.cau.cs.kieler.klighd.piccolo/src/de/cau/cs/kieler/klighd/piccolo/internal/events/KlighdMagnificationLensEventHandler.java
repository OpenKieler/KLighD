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
package de.cau.cs.kieler.klighd.piccolo.internal.events;

import java.awt.geom.Point2D;

import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;

import de.cau.cs.kieler.klighd.IKlighdPreferenceStore;
import de.cau.cs.kieler.klighd.KlighdPreferences;
import de.cau.cs.kieler.klighd.eclipse.EclipseKlighdPreferences;
import de.cau.cs.kieler.klighd.piccolo.internal.KlighdCanvas;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KlighdMagnificationLensCamera;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KlighdMainCamera;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.KlighdPath;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PAffineTransform;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * An input event handler that puts, moves, and removes a magnifying lens on the corresponding
 * diagram's canvas if ALT + CTRL/CMD keys are pressed (and released respectively). 
 * 
 * @author chsch
 */
public class KlighdMagnificationLensEventHandler extends KlighdBasicInputEventHandler {
    
    private static final IKlighdPreferenceStore STORE = KlighdPreferences.getPreferenceStore();
    private static final int CTRL_CMD = KlighdKeyEventListener.OS_MACOSX ? SWT.COMMAND : SWT.CTRL;
    
    private final KlighdMainCamera mainCamera;
    private final PCamera lensCamera;

    /**
     * Constructor.
     * 
     * @param canvasCamera
     *            the {@link KlighdMainCamera} employed in the corresponding diagram canvas for
     *            drawing the diagram.
     */
    public KlighdMagnificationLensEventHandler(final KlighdMainCamera canvasCamera) {
        this.disabled = !KlighdPreferences.isMagnificationLensEnabled();
        this.mainCamera = canvasCamera;
        this.lensCamera = new KlighdMagnificationLensCamera();
        this.lensCamera.setPickable(false);

        final KlighdPath path = new KlighdPath() {
            private static final long serialVersionUID = -4353599895034123565L;

            // SUPPRESS CHECKSTYLE NEXT 20 MagicNumber

            {
                final int width = STORE.getInt(KlighdPreferences.MAGNIFICATION_LENS_WIDTH);
                final int height = STORE.getInt(KlighdPreferences.MAGNIFICATION_LENS_HEIGHT);
                this.setVisible(false);
                this.setPathToRoundRectangle(-width / 2, -height / 2, width, height, 20, 20);
                this.setLineWidth(2);
                this.setPaint(new RGB(240, 240, 255));
                this.addChild(lensCamera);
                mainCamera.addChild(this);
            }
            
            @Override
            protected void layoutChildren() {
                final PBounds bounds = getBounds();
                bounds.setRect(bounds.x + 5, bounds.y + 5, bounds.width - 10, bounds.height - 10);
                
                for (final Object child : getChildrenReference()) {
                    ((PNode) child).setBounds(bounds);
                }
            }            
        };

        EclipseKlighdPreferences.registerPrefChangeListener((KlighdCanvas) canvasCamera.getComponent(),
                new IPreferenceChangeListener() {

            @Override
            public void preferenceChange(PreferenceChangeEvent event) {
                final String pref = event.getKey();
                
                // SUPPRESS CHECKSTYLE NEXT 15 MagicNumber
                
                if (KlighdPreferences.MAGNIFICATION_LENS_ENABLED.equals(pref)) {
                    disabled = !KlighdPreferences.isMagnificationLensEnabled();
                    
                } else if (KlighdPreferences.MAGNIFICATION_LENS_WIDTH.equals(pref)) {
                    final float width = STORE.getInt(KlighdPreferences.MAGNIFICATION_LENS_WIDTH);
                    final float height = (float) path.getHeight();
                    path.setPathToRoundRectangle(-width / 2, -height / 2, width, height, 20, 20);
                    
                } else if (KlighdPreferences.MAGNIFICATION_LENS_HEIGHT.equals(pref)) {
                    final float width = (float) path.getWidth();
                    final int height = STORE.getInt(KlighdPreferences.MAGNIFICATION_LENS_HEIGHT);
                    path.setPathToRoundRectangle(-width / 2, -height / 2, width, height, 20, 20);
                }
            }
        });
    }

    private boolean disabled = true;

    private boolean lensVisible = false;

    /**
     * @return the visibility state of the magnifying lens
     */
    public boolean isLensVisible() {
        return lensVisible;
    }

    /**
     * Determines the lens position (especially its midpoint) based on the <code>event</code>'s
     * canvas point. This point is adjusted wrt. the minimum, the half of the lens' width & height,
     * as well as the maximum, the canvas' size minus the lens's half width & height.
     * 
     * @param event
     *            the {@link PInputEvent} to get the canvas position from
     * @return the desired lens position in shape of a {@link Point2D}
     */
    private Point2D determineLensOffset(final PInputEvent event) {
        final PBounds lensSize = lensCamera.getParent().getBounds();
        lensSize.setRect(lensSize.x, lensSize.y, lensSize.width / 2, lensSize.height / 2);
        
        final Point2D canvasPos = event.getCanvasPosition();
        canvasPos.setLocation(Math.max(canvasPos.getX(), lensSize.width),
                Math.max(canvasPos.getY(), lensSize.height));
        
        final Point canvasSize = ((KlighdCanvas) event.getComponent()).getSize();
        canvasPos.setLocation(Math.min(canvasPos.getX(), canvasSize.x - lensSize.width),
                Math.min(canvasPos.getY(), canvasSize.y - lensSize.height));

        return canvasPos;
    }

    /**
     * Composes a {@link PAffineTransform} incorporating the lens magnification according to the
     * corresponding preference setting, the inverse of the clip node's scaling, and event's mouse
     * pointer position in terms the diagram coordinates (rather than canvas coordinates).
     * 
     * @param event
     *            the {@link PInputEvent} to get the diagram position from
     * @return the desired {@link PAffineTransform} denoting the lens camera's view transform
     */
    private PAffineTransform createViewTransform(final PInputEvent event) {
        final PAffineTransform viewTransform = new PAffineTransform();
        
        // SUPPRESS CHECKSTYLE NEXT MagicNumber -- the preference unit is percent        
        final float scale = STORE.getFloat(KlighdPreferences.MAGNIFICATION_LENS_SCALE) / 100f;
        viewTransform.scale(scale, scale);

        final double clipScale = mainCamera.getDisplayedKNodeNode().getScale();
        viewTransform.scale(1 / clipScale, 1 / clipScale);
        
        final Point2D pos = event.getPosition();
        viewTransform.translate(-pos.getX(), -pos.getY());
        return viewTransform;
    }


    @Override
    public void keyPressed(final PInputEvent event) {
        if (disabled || lensVisible) {
            return;

        } else if (event.isControlDown() && (event.getKeyCode() & SWT.ALT) != 0
                || event.isAltDown() && (event.getKeyCode() & CTRL_CMD) != 0) {
            event.setHandled(true);

            lensCamera.getParent().setOffset(determineLensOffset(event));            
            lensCamera.setViewTransform(createViewTransform(event));

            lensCamera.getLayersReference().clear();
            lensCamera.addLayer(mainCamera.getDisplayedKNodeNode());
            lensVisible = true;
            lensCamera.getParent().setVisible(true);
        }
    }

    @Override
    public void keyReleased(final PInputEvent event) {
        if (lensVisible
                && ((event.getKeyCode() & SWT.ALT) != 0 || (event.getKeyCode() & CTRL_CMD) != 0)) {
            event.setHandled(true);

            lensCamera.getParent().setVisible(false);
            lensVisible = false;
            lensCamera.getLayersReference().clear();
        }
    }
    
    @Override
    public void mouseMoved(final PInputEvent event) {
        if (event.isAltDown() && event.isControlDown()) {
            event.setHandled(true);

            lensCamera.getParent().setOffset(determineLensOffset(event));            
            lensCamera.setViewTransform(createViewTransform(event));
        } else if (lensVisible) {
            // don't set the event to handled because this case is more or less
            //  exception handling after calls of CTRL+ALT+del on windows machines
            //  that may activate the lens and leave it open until it is re-called

            lensCamera.getParent().setVisible(false);
            lensVisible = false;
            lensCamera.getLayersReference().clear();
        }
    }
}
