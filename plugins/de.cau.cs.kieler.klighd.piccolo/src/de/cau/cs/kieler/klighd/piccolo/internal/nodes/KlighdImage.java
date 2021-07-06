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
package de.cau.cs.kieler.klighd.piccolo.internal.nodes;

import java.awt.Shape;
import java.awt.geom.RectangularShape;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

import com.google.common.collect.Lists;

import de.cau.cs.kieler.klighd.Klighd;
import de.cau.cs.kieler.klighd.krendering.KImage;
import de.cau.cs.kieler.klighd.piccolo.KlighdNode;
import de.cau.cs.kieler.klighd.piccolo.KlighdPiccoloPlugin;
import de.cau.cs.kieler.klighd.piccolo.KlighdSWTGraphics;
import de.cau.cs.kieler.klighd.piccolo.internal.nodes.NodeDisposeListener.IResourceEmployer;
import de.cau.cs.kieler.klighd.piccolo.internal.util.KlighdPaintContext;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * A special {@link edu.umd.cs.piccolo.PNode PNode} for integrating images in KLighD diagrams. The
 * implementation is inspired by that of {@link edu.umd.cs.piccolox.swt.PSWTImage}, some major
 * differences wrt. design requirements led to this new implementation. These differences involve
 * the non-dependency to any specific canvas implementation as well as to
 * {@link org.eclipse.swt.widgets.Display Display} being a specific
 * {@link org.eclipse.swt.graphics.Device Device}.<br>
 * <br>
 * In contrast to {@link edu.umd.cs.piccolox.swt.PSWTImage} the bounds of nodes of this type are not
 * set while setting the image object to be displayed. Doing so results in flickering, at least
 * while drawing diagrams without animation. Instead the bounds are set top down by KlighD, see
 * {@link de.cau.cs.kieler.klighd.piccolo.internal.controller.KGERenderingControllerHelper#createImage(
 * de.cau.cs.kieler.klighd.piccolo.internal.controller.AbstractKGERenderingController,
 * de.cau.cs.kieler.klighd.krendering.KImage, java.util.List, edu.umd.cs.piccolo.PNode,
 * de.cau.cs.kieler.klighd.microlayout.Bounds)
 * KGERenderingControllerHelper#createImage(...)}.<br>
 * <br>
 * If the amount of instances of {@link Image} created while drawing diagrams, e.g. such with lots
 * of copies of the same icon will lead to performance/memory issues, one might introduce a further
 * caching mechanism beyond that in
 * {@link de.cau.cs.kieler.klighd.piccolo.internal.controller.KGERenderingControllerHelper#IMAGE_BUFFER
 * KGERenderingControllerHelper#IMAGE_BUFFER}. This could be done like for colors and fonts.
 * However, keeping every image in a lookup for the whole JVM life cycle sounds problematic...
 * 
 * @author chsch
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch
 */
public class KlighdImage extends KlighdNode.KlighdFigureNode<KImage> implements IResourceEmployer {

    private static final long serialVersionUID = 7201328608113593385L;
    
    private static final ImageRegistry IMAGE_REGISTRY = Klighd.IS_PLATFORM_RUNNING ?
            KlighdPiccoloPlugin.getDefault().getImageRegistry() : new ImageRegistry();
    
    // These two fields are to be kept consistent,
    //  i.e. both shall denote the same image.
    // This is useful for efficient drawing on SWT and non-SWT-based canvases.
    private transient Image image;
    private transient ImageData imageData;
    
    private String imageKey = null;
    private List<String> formerImages = null;
    
    /**
     * The shape defining the clip area to be applied to this image.
     */
    private Shape clip;
    private RectangularShape rectClip;
    
    /**
     * Common private constructor.
     *
     * @param kImage
     *            the {@link KImage} of this KlighdImage, especially for Properties
     */
    private KlighdImage(final KImage kImage) {
        super(kImage);
    }

    /**
     * Constructor.
     * 
     * @param image
     *            image to be displayed by this {@link KlighdImage}
     * @param kImage
     *            the {@link KImage} of this KlighdImage, especially for Properties
     */
    public KlighdImage(final Image image, final KImage kImage) {
        this(kImage);
        setImage(image);
    }

    /**
     * Constructor.
     * 
     * @param image
     *            image to be displayed by this {@link KlighdImage}
     * @param kImage
     *            the {@link KImage} of this KlighdImage, especially for Properties
     */
    public KlighdImage(final ImageData image, final KImage kImage) {
        this(kImage);
        setImage(image);
    }

    /**
     * Constructor.
     * 
     * @param uri
     *            the {@link URL} denoting the location of the image, must be valid (perform checks
     *            before calling this constructor!)
     * @param kImage
     *            the {@link KImage} of this KlighdImage, especially for Properties
     */
    public KlighdImage(final URL url, final KImage kImage) {
        this(kImage);
        setImage(url);
     }

    /**
     * Constructor.<br>
     * <br>
     * <b>Closes the provided {@link InputStream} by calling {@link InputStream#close()}.</b>
     * 
     * @param input
     *            stream providing the image, will be read and converted to an Image internally
     * @param kImage
     *            the {@link KImage} of this KlighdImage, especially for Properties
     */
    public KlighdImage(final InputStream input, final KImage kImage) {
        this(kImage);
        try {
            setImage(input).close();
        } catch (final IOException e) {
            this.setVisible(false);
        }
    }
    
    /**
     * Getter.
     * 
     * @return the image that is displayed by this node, may be null
     */
    public Image getImage() {
        return image;
    }

    /**
     * Sets the image to be displayed by this node.
     *  
     * @param newImage the image to be displayed, may be null
     */
    public void setImage(final Image newImage) {
        final Image old = this.image;
        this.image = newImage;
        this.imageData = newImage.getImageData();

        if (old != null) {
            old.dispose();
        }

        firePropertyChange(PImage.PROPERTY_CODE_IMAGE, PImage.PROPERTY_IMAGE, old, image);
    }

    /**
     * Sets the image to be displayed by this node.
     *  
     * @param newImageData the image to be displayed, may be null
     */
    public void setImage(final ImageData newImageData) {
        final Image old = this.image;
        this.image = null;
        this.imageData = newImageData;

        if (old != null) {
            old.dispose();
        }

        firePropertyChange(PImage.PROPERTY_CODE_IMAGE, PImage.PROPERTY_IMAGE, old, imageData);
    }

    /**
     * Sets the image to be displayed by this node by delegating to
     * {@link ImageData#ImageData(InputStream)} and {@link #setImage(ImageData)}.<br>
     * <br>
     * <b><font color="red">Make sure to close the {@link InputStream} afterwards!</font></b>
     * 
     * @param input
     *            stream providing the image data
     * @return the provided {@link InputStream} for convenience
     */
    public InputStream setImage(final InputStream input) {
        setImage(new ImageData(input));
        return input;
    }

    /**
     * Sets the image to be displayed by this node.
     * 
     * @param uri
     *            the {@link URL} denoting the location of the image, must be valid (perform checks
     *            before calling this setter!)
     */
    public void setImage(final URL url) {
        if (imageKey != null) {
            if (formerImages == null) {
                formerImages = Lists.newArrayList();
            }
            formerImages.add(imageKey);
        }
        
        imageKey = url.toString();
        final ImageDescriptor descr = IMAGE_REGISTRY.getDescriptor(imageKey);

        if (descr != null) {
            setImage(descr.getImageData(100));

        } else {
            try {
                setImage(url.openStream()).close();

                KlighdPiccoloPlugin.getDefault().getImageRegistry().put(imageKey,
                        ImageDescriptor.createFromImageData(imageData));

            } catch (final Exception e) {
                final String msg =
                        "KLighD: Error occurred while loading the image at " + url.toString() + ".";
                Klighd.log(new Status(IStatus.ERROR, Klighd.PLUGIN_ID, msg, e));
            }
        }
    }

    /**
     * Returns the currently configured clip shape if it is a {@link RectangularShape},
     * <code>null</code> otherwise. This allows the update of the clip shape's coordinates
     * instead of releasing it and creating a new one, which however only makes sense for
     * {@link RectangularShape RectangularShapes}.
     * 
     * @return the currently configured clip shape if it is a {@link RectangularShape},
     *         <code>null</code> otherwise.
     */
    public RectangularShape getClip() {
        return this.rectClip;
    }

    /**
     * Set the clip shape to be applied to this image, removes the existing clip
     * if <code>clip</code> is <code>null</code>.
     * 
     * @param clip the clip to set, may be <code>null</code>
     */
    public void setClip(final Shape clip) {
        this.clip = clip;
        if (clip instanceof RectangularShape) {
            this.rectClip = (RectangularShape) clip;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void disposeSWTResource() {
        if (image != null) {
            image.dispose();
            image = null;
        }

        if (imageKey != null) {
            IMAGE_REGISTRY.remove(imageKey);
        }

        if (formerImages != null) {
            for (final String key : formerImages) {
                IMAGE_REGISTRY.remove(key);
            }
        }

        // Do not release 'imageData' here as a new Image may has to be
        //  created based on them later on 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void paint(final KlighdPaintContext kpc) {

        final KlighdSWTGraphics graphics = kpc.getKlighdGraphics();

        if (image != null && imageData == null) {
            // if this KlighdImage has been initialized with an Image object
            //  take the corresponding ImageData for drawing the image 
            this.imageData = image.getImageData();            
        }

        if (imageData != null) {
            final boolean setClip = clip != null;
            final Shape prevClip = graphics.getClip();

            if (setClip) {
                graphics.clip(clip);
            }

            addSemanticData(kpc);

            // we here rely on the imageData as the graphics layer is supposed to create
            //  appropriate platform specific images and dispose them properly
            final PBounds b = getBoundsReference();
            graphics.drawImage(imageData, b.width, b.height);

            if (setClip) {
                graphics.setClip(prevClip);
            }
        }
    }
}
