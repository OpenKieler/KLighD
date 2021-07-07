/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;

public interface ISelectionProvider {
	public void addSelectionChangedListener(ISelectionChangedListener listener);
//
//	/**
//	 * Returns the current selection for this provider.
//	 *
//	 * @return the current selection
//	 */
//	public ISelection getSelection();aram listener a selection changed listener
//	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener);
//	public void setSelection(ISelection selection);
}
