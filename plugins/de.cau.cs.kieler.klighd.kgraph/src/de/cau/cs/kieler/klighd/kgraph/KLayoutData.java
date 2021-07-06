/**
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 * 
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2009 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klighd.kgraph;



/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>KLayout Data</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Common interface for shape layouts and edge layouts. Shape layouts are
 * used by nodes, ports, and labels, while edge layouts are used by edges.
 * <!-- end-model-doc -->
 *
 *
 * @see de.cau.cs.kieler.klighd.kgraph.KGraphPackage#getKLayoutData()
 * @model interface="true" abstract="true"
 * @generated
 */
public interface KLayoutData extends EMapPropertyHolder {
	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * <!-- begin-model-doc -->
     * Whether the concrete layout data have been modified since the layout data instance
     * was created or the modification flag was reset. For shape layouts this refers to the
     * position or size, and for edge layouts it refers to the source point, target point, or
     * bend points.
     * <!-- end-model-doc -->
     * @model kind="operation" required="true"
     * @generated
     */
	boolean isModified();

	/**
     * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * <!-- begin-model-doc -->
     * Reset the modification flag to {@code false}. Layout algorithms should not do this.
     * <!-- end-model-doc -->
     * @model
     * @generated
     */
	void resetModificationFlag();

} // KLayoutData
