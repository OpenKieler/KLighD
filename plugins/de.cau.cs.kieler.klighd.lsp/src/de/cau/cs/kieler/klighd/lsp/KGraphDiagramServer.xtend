/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://rtsys.informatik.uni-kiel.de/kieler
 * 
 * Copyright 2018-2019 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 */
package de.cau.cs.kieler.klighd.lsp

import com.google.common.io.ByteStreams
import com.google.inject.Inject
import de.cau.cs.kieler.klighd.IAction
import de.cau.cs.kieler.klighd.IAction.ActionContext
import de.cau.cs.kieler.klighd.KlighdDataManager
import de.cau.cs.kieler.klighd.kgraph.KLabel
import de.cau.cs.kieler.klighd.kgraph.KNode
import de.cau.cs.kieler.klighd.krendering.KRendering
import de.cau.cs.kieler.klighd.krendering.KText
import de.cau.cs.kieler.klighd.lsp.interactive.layered.ConstraintActionHandler
import de.cau.cs.kieler.klighd.lsp.interactive.rectpack.RectPackActionHandler
import de.cau.cs.kieler.klighd.lsp.model.CheckImagesAction
import de.cau.cs.kieler.klighd.lsp.model.CheckedImagesAction
import de.cau.cs.kieler.klighd.lsp.model.ComputedTextBoundsAction
import de.cau.cs.kieler.klighd.lsp.model.PerformActionAction
import de.cau.cs.kieler.klighd.lsp.model.RefreshLayoutAction
import de.cau.cs.kieler.klighd.lsp.model.RequestTextBoundsAction
import de.cau.cs.kieler.klighd.lsp.model.SKGraph
import de.cau.cs.kieler.klighd.lsp.model.SetSynthesisAction
import de.cau.cs.kieler.klighd.lsp.model.StoreImagesAction
import de.cau.cs.kieler.klighd.lsp.utils.KGraphElementIDGenerator
import de.cau.cs.kieler.klighd.lsp.utils.KRenderingIDGenerator
import de.cau.cs.kieler.klighd.lsp.utils.SprottyProperties
import de.cau.cs.kieler.klighd.microlayout.Bounds
import de.cau.cs.kieler.klighd.util.KlighdProperties
import java.util.ArrayList
import java.util.Base64
import java.util.List
import java.util.Map
import org.apache.log4j.Logger
import org.eclipse.core.runtime.Platform
import org.eclipse.emf.ecore.EObject
import org.eclipse.sprotty.Action
import org.eclipse.sprotty.ActionMessage
import org.eclipse.sprotty.SModelElement
import org.eclipse.sprotty.SModelRoot
import org.eclipse.sprotty.SelectAction
import org.eclipse.sprotty.SelectAllAction
import org.eclipse.sprotty.SetModelAction
import org.eclipse.sprotty.UpdateModelAction
import org.eclipse.sprotty.xtext.LanguageAwareDiagramServer

/**
 * Diagram server extension adding functionality to special actions needed for handling KGraphs.
 * 
 * @author nre
 */
class KGraphDiagramServer extends LanguageAwareDiagramServer {
    static val LOG = Logger.getLogger(KGraphDiagramServer)
    
    @Inject
    protected ConstraintActionHandler constraintActionHandler
    
    @Inject
    protected RectPackActionHandler rectPackActionHandler
    
    @Inject 
    protected KGraphDiagramState diagramState
    
    /**
     * Indicates if the stored model is a completely new model and should therefore cause a SetModelAction instead of 
     * an eventual UpdateModelAction.
     */
    protected boolean newModel
    
    /**
     * The current root element of the {@link SKGraph}. Stored here during communication with the client.
     */
    protected SModelRoot currentRoot
    
    /**
     * Flag indicating if the texts of the current root are updated.
     */
    protected boolean textsUpdated = false
    
    /**
     * Flag indicating if the images of the current root are updated on the client.
     */
    protected boolean imagesUpdated = false
    
    public Object modelLock = new Object
    
    /**
     * Prepares the client side update of the model by processing the potentially needed text sizes and images on the 
     * client. Requests the calculation of text sizes by the client via the {@link RequestTextBoundsAction} and checks
     * for client-side cached images with the {@link CheckImagesAction}. If the corresponding response to the 
     * {@link CheckImagesAction} requires images to be sent, a {@link SendImagesAction} is sent first. After receiving
     * the result back, updates the model with default Sprotty behavior via the {@link #updateModel} function.
     * 
     * @param newRoot the diagram to request the text sizes for.
     */
    protected def prepareUpdateModel(SModelRoot newRoot) {
        synchronized (modelLock) {
            currentRoot = newRoot
            if (newRoot !== null) {
                textsUpdated = false
                imagesUpdated =  false
                
                // text handling
                val texts = diagramState.getTexts(newRoot.id)
                if (texts === null) {
                    throw new NullPointerException("The id of the SGraph was not found in the diagramState")
                } else if (texts.empty) {
                    textsUpdated = true
                } else {
                    val textDiagram = KGraphDiagramGenerator.generateTextDiagram(texts, newRoot.id)
                    dispatch(new RequestTextBoundsAction(textDiagram))
                    // the setOrUpdateModel is then executed after the client returns with its ComputedTextBoundsAction
                }
                
                // image handling
                val images = diagramState.getImages(newRoot.id)
                if (images === null) {
                    throw new NullPointerException("The id of the SGraph was not found in the diagramState")
                } else if (images.empty) {
                    imagesUpdated = true
                } else {
                    dispatch(new CheckImagesAction(images))
                    // the setOrUpdateModel is then executed after the client confirms it has all images cached.
                }
            } else {
                setOrUpdateModel
            }
            // If the texts and the images are already updated, the model is ready to be sent to the client.
            if (textsUpdated && imagesUpdated) {
                setOrUpdateModel
            }
        }
    }
    
    override void accept(ActionMessage message) {
        val clientId = getClientId();
        if (clientId !== null && clientId.equals(message.getClientId())) {
            // call the handle function for the ComputedTextBoundsAction or forward the call to the super implementation
            val Action action = message.getAction();
            if (action.getKind() === ComputedTextBoundsAction.KIND) {
                handle(action as ComputedTextBoundsAction)
            } else if (action.getKind() === PerformActionAction.KIND) {
                handle(action as PerformActionAction)
            } else if (action.getKind() === SetSynthesisAction.KIND) {
                handle(action as SetSynthesisAction)
            } else if (action.getKind === CheckedImagesAction.KIND) {
                handle(action as CheckedImagesAction)
            } else if (action.getKind === RefreshLayoutAction.KIND) {
                handle(action as RefreshLayoutAction)
            } else if (constraintActionHandler.canHandleAction(action.getKind)) {
                constraintActionHandler.handle(action, clientId, this)
            } else if (rectPackActionHandler.canHandleAction(action.getKind)) {
                rectPackActionHandler.handle(action, clientId, this)
            } else {
                super.accept(message)
            }
        }
    }
    
    /**
     * Called when a {@link ComputedTextBoundsAction} is received.
     * Maps the bounds for all texts referenced in the action back to their corresponding {@link KText} elements
     * and updates the model on the client.
     */
    protected def handle(ComputedTextBoundsAction action) {
        synchronized (modelLock) {
            // assume the model is still stored in 'currentRoot', since the ComputedTextBoundsAction only gets issued
            // after a RequestTextBoundsAction, where it got stored before.
            
            val textMapping = diagramState.getTextMapping(currentRoot.id)
            // Add the bounds for each label to the text's properties and remember which KTexts have been modified.
            // Also calculate the widths and heights of all texts on the fly to be applied later.
            val List<KText> texts = newArrayList
            val Map<KText, float[]> textWidths = newHashMap
            val Map<KText, float[]> textHeights = newHashMap 
            for (elementAndBound : action.bounds) {
                val elementId = elementAndBound.elementId
                val newSize = elementAndBound.newSize
                if (newSize === null) {
                    throw new NullPointerException("Estimated Size for a KText is null!")
                }
                val kText = textMapping.get(elementId)
                if (kText === null) {
                    LOG.info("The textMapping does not contain the referenced Text anymore. The model has changed before" + 
                        "completion of the request. Terminating this request.")
                    return
                }
                val index = Integer.parseInt(elementId.split("\\$").last) // Matches the character '$'
                if (!texts.contains(kText)) {
                    var container = kText.eContainer
                    while (container instanceof KRendering) {
                        container = container.eContainer
                    }
                    var String text
                    if (container instanceof KLabel) {
                        text = container.text
                    } else {
                        text = kText.text
                    }
                    var lines = text.split("\n", -1).size
                    texts.add(kText)
                    val widths = newFloatArrayOfSize(lines)
                    widths.set(index, newSize.width as float)
                    textWidths.put(kText, widths)
                    val heights = newFloatArrayOfSize(lines)
                    heights.set(index, newSize.height as float)
                    textHeights.put(kText, heights)
                } else {
                    textWidths.get(kText).set(index, newSize.width as float)
                    textHeights.get(kText).set(index, newSize.height as float)
                }
            }
            // Apply the text's bounds.
            for (text : texts) {
                text.properties.put(KlighdProperties.CALCULATED_TEXT_BOUNDS, 
                    new Bounds(0, 0, textWidths.get(text).max, textHeights.get(text).fold(0f, [ a, b | a + b ])))
                text.properties.put(SprottyProperties.CALCULATED_TEXT_LINE_WIDTHS, textWidths.get(text))
                text.properties.put(SprottyProperties.CALCULATED_TEXT_LINE_HEIGHTS, textHeights.get(text))
            }
            textsUpdated = true
            if (imagesUpdated) {
                setOrUpdateModel
            }
        }
    }
    
    /**
     * Called when a {@link PerformActionAction} is received.
     * Invokes the contained KlighD {@link IAction} on the current {@link KNode KGraph}.
     * May cause a {@link UpdateModelAction} to be sent back to the client with an updated model.
     */
    protected def handle(PerformActionAction action) {
        synchronized (diagramState) {
            
            val sourceUrl = diagramState.getURIString(clientId)
            val k2sMap = diagramState.getKGraphToSModelElementMap(sourceUrl)
            val kGraphElement = KGraphElementIDGenerator.findElementById(k2sMap, action.KGraphElementId)
            val kRendering = KRenderingIDGenerator.findRenderingById(kGraphElement, action.KRenderingId)
            
            val klighdAction = KlighdDataManager.instance.getActionById(action.actionId)
            val viewer = diagramState.viewer
            val actionContext = new ActionContext(viewer, null, kGraphElement, kRendering)
            val actionResult = klighdAction.execute(actionContext)
            if (actionResult.needsSynthesis) {
                val diagramUpdater = diagramLanguageServer.diagramUpdater
                if (diagramUpdater instanceof KGraphDiagramUpdater) {
                    diagramUpdater.updateDiagram(this)
                } else {
                    throw new IllegalStateException("The diagramUpdater was not initialized correctly")
                }
            } else if (actionResult.actionPerformed) {
                val diagramUpdater = diagramLanguageServer.diagramUpdater
                if (diagramUpdater instanceof KGraphDiagramUpdater) {
                    diagramUpdater.updateLayout(this)
                } else {
                    throw new IllegalStateException("The diagramUpdater was not initialized correctly")
                }
            }
        }
    }
    
    /**
     * Called when a {@link SetSynthesisAction} is received.
     * Configures this diagram server to use the synthesis given in the message in future runs to generate a diagram
     * and invokes an update.
     */
    protected def handle(SetSynthesisAction action) {
        synchronized (diagramState) {
            val sourceUrl = diagramState.getURIString(clientId)
            diagramState.putSynthesisId(sourceUrl, action.id)
            this.newModel = true
            diagramLanguageServer.diagramUpdater.updateDiagram(this)
        }
    }
    
    /**
     * Called when a {@link CheckedImagesAction} is received.
     * Tells the server that the images on the client have been checked if they are cached and requests any non-cached
     * images to be sent.
     * The requested images are then sent to the client.
     */
    protected def handle(CheckedImagesAction action) {
        synchronized (modelLock) {
            if (action.notCached.empty) {
                imagesUpdated = true
            } else {
                val images = new ArrayList<Pair<String, String>>
                for (notCached : action.notCached) {
                    try {
                        val bundleAndPath = notCached.split(":", 2)
                        val bundle = bundleAndPath.get(0)
                        val path = bundleAndPath.get(1)
                        val imageStream = Platform.getBundle(bundle)
                            .getResource(path)
                            .openStream
                        val imageBytes = ByteStreams.toByteArray(imageStream)
                        imageStream.close
                        val imageString = Base64.encoder.encodeToString(imageBytes)
                        images.add(notCached -> imageString)
                    } catch (Exception e) {
                        // TODO: make the user notice this image has failed to load.
                        // Either send a message that the image has not loaded with a reason or place a dummy image instead.
                    }
                }
                dispatch(new StoreImagesAction(images))
                imagesUpdated = true
            }
            if (textsUpdated) {
                setOrUpdateModel
            }
        }
    }
    
    protected def handle(RefreshLayoutAction action) {
        synchronized(diagramState) {
            this.diagramLanguageServer.diagramUpdater.updateDiagram(this)
            return
        }
    }
    
    /**
     * Selects the SGraph elements mapped by the given selectable KGraph elements.
     * 
     * @param toBeSelected The elements that will be selected in the diagram, if they are selectable. 
     */
    def void selectElements(List<EObject> toBeSelected) {
        val toBeSelectedSModelElementIDs = newArrayList
        
        synchronized(diagramState) {
            val map = diagramState.getKGraphToSModelElementMap(diagramState.getURIString(clientId))
            toBeSelected.forEach [
                val sModelElement = map.get(it)
                if (sModelElement instanceof SModelElement) {
                    toBeSelectedSModelElementIDs.add(sModelElement.id)
                }
                
            ]
        }
        // Deselect all elements first, so only the new elements are selected now.
        val deselectAllAction = new SelectAllAction
        deselectAllAction.select = false
        dispatch(deselectAllAction)
        
        val selectAction = new SelectAction
        selectAction.selectedElementsIDs = toBeSelectedSModelElementIDs
        dispatch(selectAction)
    }
    
    /**
     * Initializes the stored options in this diagram server. Usable if a diagram server is created by something other
     * than a {@link SetModelAction}.
     * 
     * @param options The options to be initialized on this diagram server.
     */
    def initializeOptions(Map<String, String> options) {
        if (getOptions.isEmpty) {
            setOptions(options)
        }
    }
    
    /** Sets or updates the {@code currentRoot} as the model. */
    protected def void setOrUpdateModel() {
        if (newModel) {
            setModel(currentRoot)                
        } else {
            updateModel(currentRoot)
        }
        newModel = false
    }
}