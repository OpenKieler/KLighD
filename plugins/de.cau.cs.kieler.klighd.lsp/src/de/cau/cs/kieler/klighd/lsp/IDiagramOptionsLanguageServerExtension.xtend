/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://rtsys.informatik.uni-kiel.de/kieler
 * 
 * Copyright 2018,2020 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 */
package de.cau.cs.kieler.klighd.lsp

import de.cau.cs.kieler.klighd.SynthesisOption
import de.cau.cs.kieler.klighd.lsp.model.PerformActionParam
import de.cau.cs.kieler.klighd.lsp.model.SetLayoutOptionsParam
import de.cau.cs.kieler.klighd.lsp.model.SetSynthesisOptionsParam
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment

/**
 * Interface describing methods needed for functionality of sending and receiving messages for diagram options.
 * 
 * @author nre
 */
@JsonSegment('keith/diagramOptions')
interface IDiagramOptionsLanguageServerExtension {
    
    /**
     * Method called by a client to set the {@link SynthesisOption}s of the diagram resolved by {@code param.uri} to 
     * the options given in {@code param.synthesisOptions}. Also updates the diagram.
     * 
     * @param param The {@code uri} to resolve the diagram and the new options given by {@code synthesisOption}.
     */
    @JsonNotification('setSynthesisOptions')
    def void setSynthesisOptions(SetSynthesisOptionsParam param)
    
    /**
     * Method called by a client to set the layout options of the diagram resolved by {@code param.uri} to 
     * the options given in {@code param.layoutOptions}. Also updates the layout of the diagram.
     * 
     * @param param The {@code uri} to resolve the diagram and the new options given by {@code synthesisOption}.
     */
    @JsonNotification('setLayoutOptions')
    def void setLayoutOptions(SetLayoutOptionsParam param)
    
    /**
     * Method called by a client to perform an action on the diagram resolved by {@code param.uri}. The diagram may be
     * consequently updated by this action.
     * 
     * @param param The {@code uri} to resolve the diagram and the {@code actionId} that identifies the action to be
     * executed.
     */
    @JsonNotification('performAction')
    def void performAction(PerformActionParam param)
}