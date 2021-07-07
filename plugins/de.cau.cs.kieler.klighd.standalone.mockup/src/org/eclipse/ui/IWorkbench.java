/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
package org.eclipse.ui;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.IProgressService;

public interface IWorkbench {
	Display getDisplay();
	IProgressService getProgressService();
   void addWorkbenchListener(IWorkbenchListener listener);
   IWorkbenchWindow getActiveWorkbenchWindow();

}
