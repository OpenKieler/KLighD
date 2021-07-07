/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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
package org.eclipse.swt.widgets;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GCData;
import org.eclipse.swt.graphics.Point;

public class Display extends Device {
    
    private static Display currentDisplay = new Display();
    
    public static Display getCurrent () {
        return currentDisplay;
    }
    
    public Point getDPI () {
        return null;
    }
    
    public void syncExec(Runnable r) {
        
    }
    
    public void asyncExec(Runnable r) {
        
    }
    
    public Color getSystemColor(int i) {
        return null;
    }
    
    public void wake() {
        
    }
    
    public boolean readAndDispatch() {
        return false;
    }
    
    public void sleep() {
        
    }
    public static Display getDefault () {
        return null;
    }

    @Override
    public long internal_new_GC(GCData data) {
        return 0;
    }

    @Override
    public void internal_dispose_GC(long handle, GCData data) {
        
    }
}
