/**
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


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>KFont Bold</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * FontStyle to dertermine whether to draw it bold or not.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link de.cau.cs.kieler.klighd.krendering.KFontBold#isBold <em>Bold</em>}</li>
 * </ul>
 *
 * @see de.cau.cs.kieler.klighd.krendering.KRenderingPackage#getKFontBold()
 * @model
 * @generated
 */
public interface KFontBold extends KStyle {
    /**
     * Returns the value of the '<em><b>Bold</b></em>' attribute.
     * The default value is <code>"true"</code>.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * <!-- begin-model-doc -->
     * Set true to draw the text bold.
     * <!-- end-model-doc -->
     * @return the value of the '<em>Bold</em>' attribute.
     * @see #setBold(boolean)
     * @see de.cau.cs.kieler.klighd.krendering.KRenderingPackage#getKFontBold_Bold()
     * @model default="true" required="true"
     * @generated
     */
    boolean isBold();

    /**
     * Sets the value of the '{@link de.cau.cs.kieler.klighd.krendering.KFontBold#isBold <em>Bold</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Bold</em>' attribute.
     * @see #isBold()
     * @generated
     */
    void setBold(boolean value);

} // KFontBold
