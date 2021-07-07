/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     Christoph Läubrich - add method to get preference store for a class
 *******************************************************************************/
package org.eclipse.ui;

import org.eclipse.jface.preference.IPreferenceStore;

public final class PlatformUI {
    
    public static boolean isWorkbenchRunning() {
        return false;
    }
    
    public static IWorkbench getWorkbench() {
        return null;
    }
    public static IPreferenceStore getPreferenceStore() {
        return null;
    }
}
