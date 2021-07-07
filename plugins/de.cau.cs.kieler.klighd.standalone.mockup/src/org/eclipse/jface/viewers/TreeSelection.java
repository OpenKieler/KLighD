/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 430873
 *     Mikael Barbero (Eclipse Foundation) - Bug 254570
 *******************************************************************************/
package org.eclipse.jface.viewers;

import java.util.Iterator;

public class TreeSelection {
    public TreeSelection(TreePath treePath) {
    }
    public TreeSelection(TreePath[] paths) {
    }
    public Iterator iterator() {
        return null;
    }
    public boolean isEmpty() {
        return true;
    }
    public Object getFirstElement() {
        return null;
    }
    public TreePath[] getPathsFor(Object element) {
        return null;
    }
}
