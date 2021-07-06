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
package de.cau.cs.kieler.klighd.eclipse;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

import de.cau.cs.kieler.klighd.KlighdConstants;

/**
 * This class contains definitions of initial & default values.
 * 
 * @author chsch
 * 
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch
 */
public final class EclipseKlighdConstants {
    /**
     * Constant definition of the black {@link RGB} color constant.
     */
    public static final RGB BLACK = new RGB(0, 0, 0);

    /**
     * Constant definition of the red {@link RGB} color constant.
     */
    public static final RGB BLUE = new RGB(0, 0, 255);

    /**
     * Constant definition of the red {@link RGB} color constant.
     */
    public static final RGB GREEN = new RGB(0, 255, 0);

    /**
     * Constant definition of the red {@link RGB} color constant.
     */
    public static final RGB RED = new RGB(255, 0, 0);

    /**
     * Constant definition of the white {@link RGB} color constant.
     */
    public static final RGB WHITE = new RGB(255, 255, 255);

    /**
     * Constant definition of the yellow {@link RGB} color constant.
     */
    public static final RGB YELLOW = new RGB(255, 255, 0);
    /**
     * Constant definition denoting the standard line drawing attributes.
     */
    public static final LineAttributes DEFAULT_LINE_ATTRIBUTES = new LineAttributes(1f);

    /**
     * This font style used for {@link de.cau.cs.kieler.klighd.krendering.KText KTexts}, if no related
     * {@link de.cau.cs.kieler.klighd.krendering.KFontBold KFontBold} and/or
     * {@link de.cau.cs.kieler.klighd.krendering.KFontItalic KFontItalic} style is attached.
     * 
     * Note: SWT font styles are composed by performing 'style |= SWT.BOLD' and/or 'style |=
     * SWT.ITALIC' while 'style' is the style integer.
     */
    public static final int DEFAULT_FONT_STYLE_SWT = SWT.NORMAL;

    /**
     * This font configuration used for {@link de.cau.cs.kieler.klighd.krendering.KText KTexts}.
     */
    public static final FontData DEFAULT_FONT = new FontData(KlighdConstants.DEFAULT_FONT_NAME,
            KlighdConstants.DEFAULT_FONT_SIZE, DEFAULT_FONT_STYLE_SWT);

    /**
     * Hidden default constructor.
     */
    private EclipseKlighdConstants() {
        
    }
    
    /**
     * Convert AWT color to SWT RGB.
     * @param color The color
     * @return An SWT RGB
     */
    public static RGB toRGB(final Color color) {
        return new RGB(color.getRed(), color.getGreen(), color.getBlue());
    }
    
    public static ImageData convertAWTImageToSWT(Image image) {
        if (image == null) {
            throw new IllegalArgumentException("Null 'image' argument.");
        }
        int w = image.getWidth(null);
        int h = image.getHeight(null);
        if (w == -1 || h == -1) {
            return null;
        }
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics g = bi.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return convertToSWT(bi);
    }
    
    /**
     * Converts a buffered image to SWT <code>ImageData</code>.
     *
     * @param bufferedImage  the buffered image (<code>null</code> not
     *         permitted).
     *
     * @return The image data.
     */
    public static ImageData convertToSWT(BufferedImage bufferedImage) {
        if (bufferedImage.getColorModel() instanceof DirectColorModel) {
            DirectColorModel colorModel
                    = (DirectColorModel) bufferedImage.getColorModel();
            PaletteData palette = new PaletteData(colorModel.getRedMask(),
                    colorModel.getGreenMask(), colorModel.getBlueMask());
            ImageData data = new ImageData(bufferedImage.getWidth(),
                    bufferedImage.getHeight(), colorModel.getPixelSize(),
                    palette);
            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[3];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    raster.getPixel(x, y, pixelArray);
                    int pixel = palette.getPixel(new RGB(pixelArray[0],
                            pixelArray[1], pixelArray[2]));
                    data.setPixel(x, y, pixel);
                }
            }
            return data;
        }
        else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
            IndexColorModel colorModel = (IndexColorModel)
                    bufferedImage.getColorModel();
            int size = colorModel.getMapSize();
            byte[] reds = new byte[size];
            byte[] greens = new byte[size];
            byte[] blues = new byte[size];
            colorModel.getReds(reds);
            colorModel.getGreens(greens);
            colorModel.getBlues(blues);
            RGB[] rgbs = new RGB[size];
            for (int i = 0; i < rgbs.length; i++) {
                rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF,
                        blues[i] & 0xFF);
            }
            PaletteData palette = new PaletteData(rgbs);
            ImageData data = new ImageData(bufferedImage.getWidth(),
                    bufferedImage.getHeight(), colorModel.getPixelSize(),
                    palette);
            data.transparentPixel = colorModel.getTransparentPixel();
            WritableRaster raster = bufferedImage.getRaster();
            int[] pixelArray = new int[1];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    raster.getPixel(x, y, pixelArray);
                    data.setPixel(x, y, pixelArray[0]);
                }
            }
            return data;
        }
        return null;
    }
}
