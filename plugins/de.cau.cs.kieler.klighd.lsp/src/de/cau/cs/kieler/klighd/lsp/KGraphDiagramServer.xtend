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

import com.google.common.base.Strings
import com.google.common.base.Throwables
import com.google.common.io.ByteStreams
import com.google.inject.Inject
import de.cau.cs.kieler.klighd.IAction
import de.cau.cs.kieler.klighd.IAction.ActionContext
import de.cau.cs.kieler.klighd.Klighd
import de.cau.cs.kieler.klighd.KlighdDataManager
import de.cau.cs.kieler.klighd.ViewContext
import de.cau.cs.kieler.klighd.kgraph.KLabel
import de.cau.cs.kieler.klighd.kgraph.KNode
import de.cau.cs.kieler.klighd.krendering.KRendering
import de.cau.cs.kieler.klighd.krendering.KText
import de.cau.cs.kieler.klighd.lsp.interactive.layered.LayeredInteractiveActionHandler
import de.cau.cs.kieler.klighd.lsp.interactive.rectpacking.RectpackingInteractiveActionHandler
import de.cau.cs.kieler.klighd.lsp.launch.AbstractLanguageServer
import de.cau.cs.kieler.klighd.lsp.model.CheckImagesAction
import de.cau.cs.kieler.klighd.lsp.model.CheckedImagesAction
import de.cau.cs.kieler.klighd.lsp.model.ComputedTextBoundsAction
import de.cau.cs.kieler.klighd.lsp.model.LayoutOptionUIData
import de.cau.cs.kieler.klighd.lsp.model.PerformActionAction
import de.cau.cs.kieler.klighd.lsp.model.RefreshDiagramAction
import de.cau.cs.kieler.klighd.lsp.model.RefreshLayoutAction
import de.cau.cs.kieler.klighd.lsp.model.RequestTextBoundsAction
import de.cau.cs.kieler.klighd.lsp.model.SKGraph
import de.cau.cs.kieler.klighd.lsp.model.SetSynthesisAction
import de.cau.cs.kieler.klighd.lsp.model.StoreImagesAction
import de.cau.cs.kieler.klighd.lsp.model.UpdateDiagramOptionsAction
import de.cau.cs.kieler.klighd.lsp.model.ValuedSynthesisOption
import de.cau.cs.kieler.klighd.lsp.utils.KRenderingIdGenerator
import de.cau.cs.kieler.klighd.lsp.utils.SprottyProperties
import de.cau.cs.kieler.klighd.microlayout.Bounds
import de.cau.cs.kieler.klighd.util.KlighdProperties
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.ArrayList
import java.util.Base64
import java.util.Collection
import java.util.List
import java.util.Map
import java.util.concurrent.CompletableFuture
import org.apache.log4j.Logger
import org.eclipse.core.runtime.Platform
import org.eclipse.elk.core.data.LayoutMetaDataService
import org.eclipse.elk.core.data.LayoutOptionData
import org.eclipse.elk.core.data.LayoutOptionData.Visibility
import org.eclipse.elk.graph.properties.IProperty
import org.eclipse.emf.ecore.EObject
import org.eclipse.sprotty.Action
import org.eclipse.sprotty.ActionMessage
import org.eclipse.sprotty.ILayoutEngine
import org.eclipse.sprotty.IModelUpdateListener
import org.eclipse.sprotty.LayoutAction
import org.eclipse.sprotty.RequestBoundsAction
import org.eclipse.sprotty.RequestModelAction
import org.eclipse.sprotty.SModelCloner
import org.eclipse.sprotty.SModelElement
import org.eclipse.sprotty.SModelRoot
import org.eclipse.sprotty.SelectAction
import org.eclipse.sprotty.SelectAllAction
import org.eclipse.sprotty.SetModelAction
import org.eclipse.sprotty.UpdateModelAction
import org.eclipse.sprotty.xtext.LanguageAwareDiagramServer
import org.eclipse.xtend.lib.annotations.Accessors

/**
 * Diagram server extension adding functionality to special actions needed for handling KGraphs.
 * 
 * @author nre
 */
class KGraphDiagramServer extends LanguageAwareDiagramServer {
    static val LOG = Logger.getLogger(KGraphDiagramServer)
    
    @Inject
    protected LayeredInteractiveActionHandler constraintActionHandler
    
    @Inject
    protected RectpackingInteractiveActionHandler rectpackingActionHandler
    
    @Inject 
    protected KGraphDiagramState diagramState
    
    @Inject
    INotificationHandler notificationHandler
    
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
    
    @Accessors(PUBLIC_GETTER)
    protected Object modelLock = new Object
    
    /**
     * Needed for KeithUpdateModelAction
     * 
     * FIXME Remove this if UpdateModelAction has a cause.
     */
    protected String lastSubmittedModelType
    
    /**
     * Needed for KeithUpdateModelAction
     * 
     * FIXME Remove this if UpdateModelAction has a cause.
     */
    protected int revision = 0
    
    /**
     * Needed for KeithUpdateModelAction
     * 
     * FIXME Remove this if UpdateModelAction has a cause.
     */
    new() {
        currentRoot = new SModelRoot();
        currentRoot.setType("NONE");
        currentRoot.setId("ROOT");
    }
    
    /**
     * Prepares the client side update of the model by processing the potentially needed text sizes and images on the 
     * client. Requests the calculation of text sizes by the client via the {@link RequestTextBoundsAction} and checks
     * for client-side cached images with the {@link CheckImagesAction}. If the corresponding response to the 
     * {@link CheckImagesAction} requires images to be sent, a {@link SendImagesAction} is sent first. After receiving
     * the result back, updates the model with default Sprotty behavior via the {@link #updateModel} function.
     * Also handles updating the diagram options on the client.
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
                val imageData = diagramState.getImageData(newRoot.id)
                if (imageData === null) {
                    throw new NullPointerException("The id of the SGraph was not found in the diagramState")
                } else if (imageData.empty) {
                    imagesUpdated = true
                } else {
                    dispatch(new CheckImagesAction(imageData))
                    // the setOrUpdateModel is then executed after the client confirms it has all images cached.
                }
                
                // Update the diagram options with the current used values on the client.
                updateDiagramOptions(newRoot.id)
            } else {
                setOrUpdateModel
            }
            // If the texts and the images are already updated, the model is ready to be sent to the client.
            if (textsUpdated && imagesUpdated) {
                setOrUpdateModel
            }
        }
    }
    
    /**
     * Updates the diagram options on the client with the values from the currently used view context found under the
     * given URI.
     * 
     * @param uri The URI of the model to take the options from.
     */
    def void updateDiagramOptions(String uri) {
        synchronized (diagramState) {
            val ViewContext viewContext = diagramState.getKGraphContext(uri)
            if (viewContext !== null) {
                val synthesisOptions = new ArrayList<ValuedSynthesisOption>
                for (option : viewContext.displayedSynthesisOptions) {
                    var currentValue = viewContext.getOptionValue(option)
                    if (currentValue instanceof Enum) {
                        currentValue = currentValue.toString
                    }
                    synthesisOptions.add(new ValuedSynthesisOption(option, currentValue))
                }
                val layoutOptionUIData = calculateLayoutOptionUIData(viewContext.displayedLayoutOptions)
                val actionData = viewContext.displayedActions
                dispatch(new UpdateDiagramOptionsAction(synthesisOptions, layoutOptionUIData, actionData, uri))
            }
        }
    }
    
    /**
     * Packs all data needed to display the layout options in any user interface into a single list of
     * {@link LayoutOptionUIData}.
     * 
     * @param displayedLayoutOptions The layout options that should be displayed. 
     */
    def calculateLayoutOptionUIData(List<org.eclipse.elk.core.util.Pair<IProperty<?>, List<?>>> displayedLayoutOptions) {
        val List<LayoutOptionUIData> layoutOptionUIData = new ArrayList
        for (pair : displayedLayoutOptions) {
            var Object first
            var Object second
            if (pair.second instanceof Collection) {
                val iterator = (pair.second as Collection<?>).iterator
                first = if (iterator.hasNext) iterator.next else null
                second = if (iterator.hasNext) iterator.next else null
            }

            val LayoutOptionData optionData = LayoutMetaDataService.instance.getOptionData(pair.first.id)
            if (optionData.visibility !== Visibility.HIDDEN) {
                if (first instanceof Number && second instanceof Number) {
                    layoutOptionUIData.add(new LayoutOptionUIData(optionData, (first as Number).floatValue,
                        (second as Number).floatValue, null))
                } else {
                    layoutOptionUIData.add(new LayoutOptionUIData(optionData, null, null, pair.second))
                }
            }
        }
        return layoutOptionUIData
    }
    
    override void accept(ActionMessage message) {
        try {
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
                } else if (action.getKind === RefreshDiagramAction.KIND) {
                    handle(action as RefreshDiagramAction)
                } else if (action.getKind === RefreshLayoutAction.KIND) {
                    handle(action as RefreshLayoutAction)
                } else if (constraintActionHandler.canHandleAction(action.getKind)) {
                    constraintActionHandler.handle(action, clientId, this)
                } else if (rectpackingActionHandler.canHandleAction(action.getKind)) {
                    rectpackingActionHandler.handle(action, clientId, this)
                } else {
                    super.accept(message)
                }
            }
        } catch (Exception e) {
            // Any exception that happened during some request is displayed to the user on the client.
            notificationHandler.sendErrorAndThrow(e)
        }
    }
        
    /**
     * Submit a new or updated model to the client. If client layout is required, a {@link RequestBoundsAction}
     * is sent, otherwise either a {@link SetModelAction} or an {@link UpdateModelAction} is sent depending on
     * the {@code update} parameter.
     * Needed for KeithUpdateModelAction
     * 
     * FIXME Remove this if UpdateModelAction has a cause.
     */
    override CompletableFuture<Void> submitModel(SModelRoot newRoot, boolean update, Action cause) {
        if (needsClientLayout(newRoot)) {
            if (!needsServerLayout(newRoot, cause)) {
                // In this case the client won't send us the computed bounds
                dispatch(new RequestBoundsAction(newRoot));
                val IModelUpdateListener listener = getModelUpdateListener();
                if (listener !== null) {
                    listener.modelSubmitted(newRoot, this);
                }
            } else {
                return request(new RequestBoundsAction(newRoot)).handle([response, exception | {
                    if (exception !== null) {
                        LOG.error("RequestBoundsAction failed with an exception.", exception);
                    } else {
                        try {
                            var SModelRoot model = handle(response);
                            if (model !== null)
                                doSubmitModel(model, true, cause);
                        } catch (Exception exc) {
                            LOG.error("Exception while processing ComputedBoundsAction.", exc);
                        }
                    }
                    return null;
                }])
            }
        } else {
            doSubmitModel(newRoot, update, cause);
        }
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Needed for KeithUpdateModelAction
     * 
     * FIXME Remove this if UpdateModelAction has a cause.
     */
    override CompletableFuture<Void> setModel(SModelRoot newRoot) {
        if (newRoot === null)
            throw new NullPointerException();
        synchronized(modelLock) {
            newRoot.setRevision(revision + 1);
            revision++
            currentRoot = newRoot;
        }
        return submitModel(newRoot, false, null);
    }
    
    /**
     * Needed for KeithUpdateModelAction
     * 
     * FIXME Remove this if UpdateModelAction has a cause.
     */
    override CompletableFuture<Void> updateModel(SModelRoot newRoot) {
        if (newRoot === null)
            throw new IllegalArgumentException("updateModel() cannot be called with null");
        synchronized(modelLock) {
            currentRoot = newRoot;
            newRoot.setRevision(revision + 1);
            revision++
        }
        return submitModel(newRoot, true, null);
    }
    
    
    /**
     * Needed for KeithUpdateModelAction
     * 
     * FIXME Remove this if UpdateModelAction has a cause.
     */
    def void doSubmitModel(SModelRoot newRoot, boolean update, Action cause) {
        val ILayoutEngine layoutEngine = getLayoutEngine();
        if (needsServerLayout(newRoot, cause)) {
            AbstractLanguageServer.addToMainThreadQueue([
                layoutEngine.layout(newRoot, cause)
            ])
        }
        synchronized (modelLock) {
            if (newRoot.getRevision() == revision) {
                var String modelType = newRoot.getType();
                if (cause instanceof RequestModelAction
                        && !Strings.isNullOrEmpty((cause as RequestModelAction).getRequestId())) {
                    var RequestModelAction request = cause as RequestModelAction;
                    var SetModelAction response = new SetModelAction(newRoot);
                    response.setResponseId(request.getRequestId());
                    dispatch(response);
                } else if (update && modelType !== null && modelType.equals(lastSubmittedModelType)) {
                    dispatch(new UpdateModelAction(newRoot));
                } else {
                    dispatch(new SetModelAction(newRoot));
                }
                lastSubmittedModelType = modelType;
                var IModelUpdateListener listener = getModelUpdateListener();
                if (listener !== null) {
                    listener.modelSubmitted(newRoot, this);
                }
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
            if (currentRoot.getRevision() !== action.getRevision()) {
                return
            }
            // Assume the model is still stored in 'currentRoot', since the ComputedTextBoundsAction only gets issued
            // after a RequestTextBoundsAction, where it got stored before. Only applies if no other diagram revision
            // is issued first.
            
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
                    if (text !== null) {
                        var lines = text.split("\\r?\\n", -1).size
                        texts.add(kText)
                        val widths = newFloatArrayOfSize(lines)
                        widths.set(index, newSize.width as float)
                        textWidths.put(kText, widths)
                        val heights = newFloatArrayOfSize(lines)
                        heights.set(index, newSize.height as float)
                        textHeights.put(kText, heights)
                    }
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
     * Taken from {@code DefaultDiagramServer.handle(RequestModelAction)} to use this getModel.
     * Needed for KeithUpdateModelAction
     * 
     * FIXME Remove this if UpdateModelAction has a cause.
     */
    override protected handle(RequestModelAction request) {
        if (model.type == 'NONE' && diagramLanguageServer !== null) {
            if (!request.requestId.nullOrEmpty)
                LOG.warn("Model requests are not supported by the Xtext diagram server.")
            copyOptions(request)
            diagramLanguageServer.diagramUpdater.updateDiagram(this)
        } else {
            super.handle(request)
        }
    }
    
    /**
     * Taken from {@code DefaultDiagramServer.handle(LayoutAction)} to use this getModel.
     * Needed for KeithUpdateModelAction
     * 
     * FIXME Remove this if UpdateModelAction has a cause.
     */
    override void handle(LayoutAction action) {
        if (needsServerLayout(model,action)) {
            // Clone the current model, as it has already been sent to the client with the old revision
            val SModelCloner cloner = getSModelCloner();
            val SModelRoot newRoot = cloner.clone(model);
            synchronized(modelLock) {
                newRoot.setRevision(revision + 1);
                revision++
                currentRoot = newRoot;
            }
            // the actual layout is performed in doSubmitModel
            doSubmitModel(newRoot, true, action);
        }
    }
    
    /**
     * Called when a {@link PerformActionAction} is received.
     * Invokes the contained KlighD {@link IAction} on the current {@link KNode KGraph}.
     * May cause a {@link UpdateModelAction} to be sent back to the client with an updated model.
     */
    protected def handle(PerformActionAction action) {
        synchronized (diagramState) {
            if (currentRoot.getRevision() !== action.revision) {
                return
            }
            
            val sourceUri = diagramState.getURIString(clientId)
            val kGraphElement = diagramState.getIdToKGraphMap(sourceUri).get(action.KGraphElementId)
            val kRendering = KRenderingIdGenerator.findRenderingById(kGraphElement, action.KRenderingId)
            
            val klighdAction = KlighdDataManager.instance.getActionById(action.actionId)
            val viewer = diagramState.viewer
            val actionContext = new ActionContext(viewer, null, kGraphElement, kRendering)
            val actionResult = klighdAction.execute(actionContext)
            if (actionResult.needsSynthesis) {
                updateDiagram()
            } else if (actionResult.actionPerformed) {
                updateLayout()
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
            val uri = diagramState.getURIString(clientId)
            diagramState.putSynthesisId(uri, action.id)
            this.newModel = true
            updateDiagram()
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
                val images = new ArrayList<Pair<Pair<String, String>, String>>
                val platformIsRunning = Klighd.IS_PLATFORM_RUNNING;
                for (notCached : action.notCached) {
                    try {
                        val bundle = notCached.key
                        val path = notCached.value
                        val InputStream imageStream =
                            if (platformIsRunning) {
                                // If the platform is running, the image can be found in the bundle under the resource path.
                                Platform.getBundle(bundle)
                                    ?.getResource(path)
                                    ?.openStream
                            } else {
                                // In the jar or plain Java application case, the bundle is ignored and the file path
                                // is searched on the classpath directly
                                this.class.getResourceAsStream("/" + path)
                            }
                        if (imageStream !== null) {
                            try {
                                val imageBytes = ByteStreams.toByteArray(imageStream)
                                val imageString = Base64.encoder.encodeToString(imageBytes)
                                images.add(notCached -> imageString)
                            } finally {
                                // Always close the stream.
                                imageStream.close
                            }
                        } else {
                            throw new FileNotFoundException("The image for bundle "
                                + bundle
                                + " and path "
                                + path
                                + " has not been found.")
                        }
                    } catch (Exception e) {
                        // Do not re-throw the exception here, we can still show the diagram, just without the images.
                        // Do notify the user about the exception, though.
                        notificationHandler.sendError(Throwables.getStackTraceAsString(e))
                        e.printStackTrace
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

    /**
     * Called when a {@link RefreshDiagramAction} is received.
     * Tells the server that the diagram should be refreshed.
     */
    protected def handle(RefreshDiagramAction action) {
        updateDiagram()
        return
    }
    
    /**
     * Called when a {@link RefreshLayoutAction} is received.
     * Tells the server that the diagram layout should be refreshed.
     */
    protected def handle(RefreshLayoutAction action) {
        updateLayout()
        return
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
     * Updates the current diagram.
     */
    def updateDiagram() {
        synchronized (diagramState) {
            val diagramUpdater = diagramLanguageServer.diagramUpdater
            if (diagramUpdater instanceof KGraphDiagramUpdater) {
                diagramUpdater.updateDiagram(this)
            } else {
                throw new IllegalStateException("The diagramUpdater was not initialized correctly")
            }
        }
    }
    
    /**
     * Updates the layout of the current diagram.
     */
    def updateLayout() {
        synchronized (diagramState) {
            val diagramUpdater = diagramLanguageServer.diagramUpdater
            if (diagramUpdater instanceof KGraphDiagramUpdater) {
                diagramUpdater.updateLayout(this)
            } else {
                throw new IllegalStateException("The diagramUpdater was not initialized correctly")
            }
        }
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

    /**
     * Needed for KeithUpdateModelAction
     * 
     * FIXME Remove this if UpdateModelAction has a cause.
     */
    override SModelRoot getModel() {
        return currentRoot;
    }
}
