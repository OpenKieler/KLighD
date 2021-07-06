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
package de.cau.cs.kieler.klighd.internal;

import java.util.List;

import org.eclipse.elk.core.util.IGraphElementVisitor;
import org.eclipse.elk.core.util.Pair;
import org.eclipse.elk.graph.properties.IProperty;

import com.google.common.base.Function;

import de.cau.cs.kieler.klighd.DisplayedActionData;
import de.cau.cs.kieler.klighd.SynthesisOption;
import de.cau.cs.kieler.klighd.ViewContext;
import de.cau.cs.kieler.klighd.kgraph.KGraphElement;
import de.cau.cs.kieler.klighd.kgraph.KNode;
import de.cau.cs.kieler.klighd.krendering.KText;

/**
 * Internal interface of diagram synthesis implementations. It has been introduced to simplify the
 * internal handling of diagram synthesis implementations, e.g. avoid annoying type casts in order
 * to satisfy the constraints imposed by the type parameter of
 * {@link de.cau.cs.kieler.klighd.syntheses.AbstractDiagramSynthesis AbstractDiagramSynthesis}.<br>
 * <br>
 * Clients shall not implement this interface but extend
 * {@link de.cau.cs.kieler.klighd.syntheses.AbstractDiagramSynthesis AbstractDiagramSynthesis} as
 * required by the extension point <code>de.cau.cs.kieler.klighd.diagramSyntheses</code> and in
 * order to benefit from the convenience features of that class!
 * 
 * @author chsch
 * 
 * @kieler.design proposed by chsch
 * @kieler.rating proposed yellow by chsch
 */
public interface ISynthesis {

    /**
     * Returns the type of the input data.
     * 
     * @return the type of the input data.
     */
    Class<?> getInputDataType();

    /**
     * Method for checking whether <code>this</code> diagram synthesis implementation supports
     * the given <code>model</code>.
     * 
     * @param model
     *            the model to test
     * @param viewContext
     *            the related {@link ViewContext}
     * @return <code>true</code> if <code>this</code> diagram synthesis supports <code>model</code>,
     *         and <code>false</code> otherwise
     */
    boolean supports(Object model, ViewContext viewContext);
    
    /**
     * Translates of the source <code>model</code> into an instance of KGraph/KRendering view model
     * format.
     * 
     * @param model
     *            the source model
     * @param viewContext
     *            the related {@link ViewContext}
     * @return the desired view model
     */
    KNode transform(Object model, ViewContext viewContext);

    /**
     * Contributes the list of {@link SynthesisOption SynthesisOptions} provided
     * in the UI in order to allow users to influence steer the diagram synthesis.
     * 
     * @return the set of {@link SynthesisOption SynthesisOptions}
     */
    List<SynthesisOption> getDisplayedSynthesisOptions();


    /**
     * Returns a list of layout options that will be displayed in the diagram side bar and that are
     * directly manipulatable by the user. For each layout option a, possibly restricted, set of
     * allowed input values can be specified.
     * 
     * @return a {@link List} of {@link Pair Pairs} where each pair specifies a recommended layout
     * option.
     */
    List<Pair<IProperty<?>, List<?>>> getDisplayedLayoutOptions();

    /**
     * Contributes the list of {@link de.cau.cs.kieler.klighd.IActionIAction IActions} provided in
     * the UI allowing to quickly perform pre-defined actions on the diagram elements. In order to
     * avoid compile time dependencies on concrete {@link de.cau.cs.kieler.klighd.IAction IAction}
     * implementations they are referenced by the id used in their registration.
     * 
     * @return a {@link List} of {@link Pair Pairs} with each pair containing the id of the denoted
     *         {@link de.cau.cs.kieler.klighd.IAction IAction} (see registration) and the
     *         corresponding label shown in the UI
     */
    List<DisplayedActionData> getDisplayedActions();

    /**
     * Returns a function that is supposed to apply changes to a text element. Its returned as a
     * function so that it can be executed within transaction contexts.
     * 
     * @param kText
     *            the text element that is supposed to be changed
     * @param element
     *            the graph element whose text is supposed to be changed
     * @return a method that can change the text in the view as well as its source
     */
    Function<String, Void> getTextUpdateFunction(final KText kText, final KGraphElement element);

    /**
     * Returns a list of {@link IGraphElementVisitor IGraphElementVisitor} to be handed over to ELK in
     * order to cause additional layout runs corresponding to the provided configurations.
     * 
     * @param viewModel
     *            the view model
     * @param viewContext
     *            the related {@link ViewContext}
     * @return a {@link List} of {@link IGraphElementVisitor IGraphElementVisitor}
     */
    List<? extends IGraphElementVisitor> getAdditionalLayoutConfigs(KNode viewModel, ViewContext viewContext);
}
