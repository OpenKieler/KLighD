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
package de.cau.cs.kieler.klighd.eclipse.internal;

import org.eclipse.ui.part.IPage;

/**
 * An extension of {@link IContentOutlinePage} allowing to delay the visibility of the outline
 * diagram until the diagram structures are initialized properly.<br>
 * This interface does not extend IContentOutlinePage as that one requires a dependency to the
 * bundle <code>org.eclipse.ui.views</code>. Since IContentOutlinePage is {@link IPage} +
 * {@link org.eclipse.jface.viewers.ISelectionProvider ISelectionProvider} we can avoid that
 * dependency this way ({@link org.eclipse.jface.viewers.ISelectionProvider ISelectionProvider}
 * might be added here if it is really required some day, our current outline page, however, doesn't
 * provide a selection).<br>
 * <br>
 * This is an internal interface that is not to be used by clients.
 * {@link de.cau.cs.kieler.klighd.IViewer IViewers} that want to contribute an outline diagram have
 * to implement {@link IDiagramOutlinePage.Provider}.
 * 
 * @author chsch
 * 
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch
 */
public interface IDiagramOutlinePage extends IPage {
    
    /**
     * Sets the outline diagram (in-) visible.
     * 
     * @param visible <code>true</code> if the outline diagram shall be set visible
     */
    void setVisible(boolean visible);
    
    /**
     * A common interface for {@link de.cau.cs.kieler.klighd.IViewer IViewers} providing
     * {@link IDiagramOutlinePage IDiagramContentOutlinePages}.
     * 
     * @author chsch
     */
    public interface Provider {
        
        /**
         * Factory method for providing an {@link IDiagramOutlinePage}.
         * 
         * @return the requested {@link IDiagramOutlinePage}
         */
        IDiagramOutlinePage getDiagramOutlinePage();
    }
}
