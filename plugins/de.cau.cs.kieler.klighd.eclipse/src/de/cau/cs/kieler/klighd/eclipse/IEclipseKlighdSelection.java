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
package de.cau.cs.kieler.klighd.eclipse;

import org.eclipse.jface.viewers.ISelection;

import de.cau.cs.kieler.klighd.IKlighdSelection;

/**
 * A common interface of KLighD-specific implementations of {@link ISelection}.
 * 
 * @author chsch
 * 
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch
 */
public interface IEclipseKlighdSelection extends ISelection, IKlighdSelection {
}
