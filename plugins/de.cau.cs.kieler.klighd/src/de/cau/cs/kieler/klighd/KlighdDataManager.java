/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 *
 * Copyright 2011 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 *
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klighd;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.elk.core.util.WrappedException;
import org.eclipse.elk.graph.properties.IProperty;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import de.cau.cs.kieler.klighd.internal.ISynthesis;
import de.cau.cs.kieler.klighd.syntheses.AbstractDiagramSynthesis;
import de.cau.cs.kieler.klighd.syntheses.GuiceBasedSynthesisFactory;
import de.cau.cs.kieler.klighd.syntheses.ReinitializingDiagramSynthesisProxy;

/**
 * Singleton for accessing transformations, viewers, update strategies and layout post processors
 * registered with KLighD.
 *
 * @author mri, chsch, akoc, csp
 */
public class KlighdDataManager {

    /** identifier of the extension point for viewer providers. */
    public static final String EXTP_ID_EXTENSIONS = "de.cau.cs.kieler.klighd.extensions";

    /** identifier of the extension point for model transformations. */
    private static final String EXTP_ID_DIAGRAM_SYNTHESES = "de.cau.cs.kieler.klighd.diagramSyntheses";

    /** name of the 'viewer' element. */
    private static final String ELEMENT_VIEWER = "viewer";

    /** name of the 'transformation' element. */
    private static final String ELEMENT_DIAGRAM_SYNTHESIS = "diagramSynthesis";

    /** name of the 'updateStrategy' element. */
    private static final String ELEMENT_UPDATE_STRATEGY = "updateStrategy";

    /** name of the 'styleModifier' element. */
    private static final String ELEMENT_STYLE_MODIFIER = "styleModifier";

    /** name of the 'action' element. */
    private static final String ELEMENT_ACTION = "action";

    /** name of the 'exporter' element. */
    private static final String ELEMENT_EXPORTER = "exporter";

    /** name of the 'exportHook' element. */
    private static final String ELEMENT_EXPORT_BRANDING = "exportBranding";

    /** name of the 'offscreenRenderer' element. */
    private static final String ELEMENT_OFFSCREEN_RENDERER = "offscreenRenderer";
    
    /** name of the 'preservedProperties' element. */
    private static final String PRESERVED_PROPERTIES = "preservedProperties";

    /** name of the 'startupHook' element. */
    private static final String ELEMENT_STARTUP_HOOK = "startupHook";

    /** name of the 'id' attribute in the extension points. */
    protected static final String ATTRIBUTE_ID = "id";

    /** name of the 'class' attribute in the extension points. */
    private static final String ATTRIBUTE_CLASS = "class";

    /** name of the 'supportedFormats' attribute in the 'offscreenRenderer' extensions. */
    private static final String ATTRIBUTE_SUPPORTED_FORMATS = "supportedFormats";

    /** The related extensions' name. */
    private static final String ELEMENT_WRAPPER = "wrapper";

    /** The wrapped type field's name. */
    private static final String ATTRIBUTE_FIGURE_CLASS_ENTRY_NAME = "figureClass";

    /** The wrapper type field's name. */
    private static final String ATTRIBUTE_WRAPPER_CLASS_ENTRY_NAME = "wrapperClass";

    /** the platform-specific newline delimiter. */
    private static final String NEW_LINE = Klighd.LINE_SEPARATOR;

    /** error message if registered class cannot be found. */
    public static final String CORE_EXCEPTION_ERROR_MSG =
            "The class definition <<CLAZZ>> cannot be found. " + NEW_LINE
            + "Are there any typing mistakes in the registration? " + NEW_LINE
            + "Is the (maybe generated) code available? " + NEW_LINE
            + "If required, is the class name correctly prefixed with "
            + GuiceBasedSynthesisFactory.CLASS_NAME + "?";

    /** error msg if registered class cannot be found. */
    private static final String NO_CLASS_DEF_FOUND_ERROR_MSG =
            "The class definition <<CLAZZ>> cannot be found. " + NEW_LINE
            + "Are there any typing mistakes in the registration? " + NEW_LINE
            + "Is the (maybe generated) code available?";

    private static final String UNEXPECTED_FAILURE_MSG =
            "KLighD: An unexpected failure occured while loading "
            + "class <<CLAZZ>>. See attached trace for details." + NEW_LINE;

    private static final String NO_VALID_CONSTRUCTOR_FAILURE_MSG =
            "KLighD: An unexpected failure occured while identifying the required"
            + " no-arg constructor of class <<CLAZZ>>. See attached trace for details."
            + NEW_LINE;

    private static final String INSTANTIATION_FAILURE_MSG =
            "KLighD: An unexpected failure occured while instantiating "
            + "class <<CLAZZ>>. See attached trace for details." + NEW_LINE;
    
    private static final String STARTUP_HOOK_ERROR_MSG = 
            "KLighD: an unexpected failure occured while executing a startup hook. See attached trace for details.";

    /** the singleton instance. */
    private static KlighdDataManager instance;

    /**
     * Creates the singleton and initializes it with the data from the extension point.
     */
    static {
        instance = new KlighdDataManager();

        if (Klighd.IS_PLATFORM_RUNNING) {
            // load the data from the extension points
            instance.loadKlighdExtensionsViaExtensionPoint();
            try {
                instance.loadDiagramSynthesesViaExtensionPoint(instance.idSynthesisMapping, instance.typeSynthesisMapping);
                
            } catch (final Exception e) {
                instance.idSynthesisMapping.clear();
                instance.typeSynthesisMapping.clear();
                
                Klighd.handle(
                        new Status(IStatus.ERROR, Klighd.PLUGIN_ID,
                                "KLighD: Unexptected failure while loading registered diagram syntheses.", e));
            }
            
        } else {
            instance.loadDiagramSynthesesViaServiceLoader(instance.idSynthesisMapping, instance.typeSynthesisMapping);
            instance.loadKlighdExtensionsViaServiceLoader();
        }
    }

    /**
     * Returns the singleton instance.
     *
     * @return the singleton
     */
    public static KlighdDataManager getInstance() {
        return instance;
    }

    /** the mapping of ids to the corresponding instances of {@link ISynthesis}. */
    private final Map<String, ISynthesis> idSynthesisMapping = Maps.newLinkedHashMap();

    /** the mapping of types to the corresponding instances of {@link ISynthesis}. */
    private final Multimap<Class<?>, ISynthesis> typeSynthesisMapping = ArrayListMultimap.create();

    /**
     * A caching map that avoids the effort of determining the correct syntheses for a given type
     * each time. */
    private final Map<Class<?>, Iterable<ISynthesis>> concreteTypeSynthesisMapping = Maps.newHashMap();

    /** the mapping of ids on the associated viewer providers. */
    private Map<String, IViewerProvider> idViewerProviderMapping = Maps.newHashMap();

    /** the mapping of ids on the associated update strategies. */
    private Map<String, IUpdateStrategy> idUpdateStrategyMapping = Maps.newHashMap();
    
    /** the mapping of priorities on the associated update strategies, sorted by priority. */
    private TreeMap<Integer, IUpdateStrategy> priorityUpdateStrategyMapping = Maps.newTreeMap();

    private IUpdateStrategy highestPriorityUpdateStrategy = null;

    /** the mapping of ids to the associated style modifiers. */
    private final Map<String, IStyleModifier> idStyleModifierMapping = Maps.newHashMap();

    /** the mapping of ids to the associated actions. */
    private final BiMap<String, IAction> idActionMapping = HashBiMap.create();

    /** the mapping of ids to the associated configuration elements describing the exporters. */
    protected final Map<String, IConfigurationElement> exportersMap = Maps.newHashMap();

    /** the list of the available exporters' descriptors. */
    private final List<ExporterDescriptor> descriptors = Lists.newArrayList();

    /** the mapping of ids to the associated export branding descriptors. */
    private final Map<String, ExportBrandingDescriptor> idExportBrandingMapping = Maps.newHashMap();

    /** the mapping of ids to the associated off-screen renderer descriptors. */
    private final Map<String, OffscreenRendererDescriptor> idOffscreenRendererMapping = Maps.newHashMap();

    /** the mapping of custom figure types to wrapper figure type being supported by KLighD. */
    private final List<CustomFigureWrapperDescriptor> customFigureWrapperMapping = Lists.newArrayList();
    
    /** the properties that shall be preserved from the layout graph to the kgraph */
    private final List<IProperty<?>> preservedProperties = Lists.newArrayList();

    /**
     * A private constructor to prevent instantiation.
     */
    public KlighdDataManager() {}

    /**
     * Reports an error that occurred while reading extensions.
     *
     * @param extensionPoint
     *            the identifier of the extension point
     * @param element
     *            the configuration element
     * @param attribute
     *            the attribute that contains an invalid entry
     * @param exception
     *            an optional exception that was caused by the invalid entry
     */
    protected static void reportError(final String extensionPoint,
            final IConfigurationElement element, final String attribute, final String msgSuffix, final Exception exception) {
        final String message =
                "KLighD: Element '" + element.getName() + "' extending extension point '"
                        + extensionPoint + "', contributed by '"
                        + element.getContributor().getName()
                        + "' contains invalid entry in attribute '" + attribute + "'"
                        + msgSuffix != null ? ": " + msgSuffix : ".";
        Klighd.handle(
                new Status(IStatus.WARNING, Klighd.PLUGIN_ID, 0, message, exception));
    }


    /* --------------------------------------------------------- */
    /*  registrations of extensions added via extensions points  */
    /* --------------------------------------------------------- */

    /**
     * Loads all registered extensions from the extension point 'extensions' and builds up the
     * corresponding mappings.
     */
    private void loadKlighdExtensionsViaExtensionPoint() {
        final IConfigurationElement[] extensions = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(EXTP_ID_EXTENSIONS);

        for (final IConfigurationElement element : extensions) {
            final String elementName = element.getName();
            final String id = element.getAttribute(ATTRIBUTE_ID);

            // 'wrapper', 'preservedProperties', and 'startupHook' extensions don't define an 'id' so check for that
            // type of extension first before checking for a valid id
            if (ELEMENT_WRAPPER.equals(elementName)) {
                registerCustomFigureWrapper(element);

            } else if (PRESERVED_PROPERTIES.equals(elementName)) {
                doRegisterExtension(element, IPreservedProperties.class,
                        (preservedProperties) -> registerPreservedProperties(preservedProperties));

            } else if (ELEMENT_STARTUP_HOOK.equals(elementName)) {
                doRegisterExtension(element, IKlighdStartupHook.class,
                        (startupHook) -> {
                            try {
                                startupHook.execute();
                            } catch (Throwable t) {
                                Klighd.log(new Status(IStatus.ERROR, Klighd.PLUGIN_ID, STARTUP_HOOK_ERROR_MSG, t));
                            }
                        });
                
            } else if (Strings.isNullOrEmpty(id)) {
                final String msg = "KLighD: Found element of type '" + elementName
                        + "' extending extension point '" + EXTP_ID_EXTENSIONS
                        + "', contributed by '" + element.getContributor().getName()
                        + "' with the mandatory 'id' attribute being empty.";
                Klighd.handle(new Status(IStatus.ERROR, Klighd.PLUGIN_ID, msg));

            } else if (ELEMENT_VIEWER.equals(elementName)) {
                doRegisterExtension(element, IViewerProvider.class,
                        viewerProvider -> idViewerProviderMapping.put(id, viewerProvider));

            } else if (ELEMENT_UPDATE_STRATEGY.equals(elementName)) {
                doRegisterExtension(element, IUpdateStrategy.class, 
                        updateStrategy -> doRegisterUpdateStrategy(id, updateStrategy));

            } else if (ELEMENT_STYLE_MODIFIER.equals(elementName)) {
                doRegisterExtension(element, IStyleModifier.class,
                        styleModifier -> idStyleModifierMapping.put(id, styleModifier));

            } else if (ELEMENT_ACTION.equals(elementName)) {
                doRegisterExtension(element, IAction.class,
                        action -> idActionMapping.put(id, action));

            } else if (ELEMENT_EXPORTER.equals(elementName)) {
                registerExporter(id, element);

            } else if (ELEMENT_EXPORT_BRANDING.equals(elementName)) {
                checkFormatsAndRegisterWithSupplier(element, IExportBranding.class, 
                        (supportedFormatsSplit, supplier) -> idExportBrandingMapping.put(id,
                                new ExportBrandingDescriptor(id, supportedFormatsSplit, supplier)));

            } else if (ELEMENT_OFFSCREEN_RENDERER.equals(elementName)) {
                checkFormatsAndRegisterWithSupplier(element, IOffscreenRenderer.class, 
                        (supportedFormatsSplit, supplier) -> idOffscreenRendererMapping.put(id,
                                new OffscreenRendererDescriptor(id, supportedFormatsSplit, supplier)));

            } else {
                final String msg = "KLighD: Found element with invalid name '" + elementName
                        + "' extending extension point '" + EXTP_ID_EXTENSIONS
                        + "', contributed by '" + element.getContributor().getName() + "'.";
                Klighd.log(new Status(IStatus.ERROR, Klighd.PLUGIN_ID, msg));
            }
        }
    }

    /**
     * Loads the registered {@link IViewerProvider}, {@link IUpdateStrategy}, {@link IAction},
     * and {@link IStyleModifier} via Java {@link ServiceLoader}.
     * This does not load the extensions for exporters via {@link IConfigurationElement}, {@link IExportBranding},
     * and {@link IOffscreenRenderer}, as they need further information for registration other than the class and an ID.
     * They need to be registered manually when extension points are not used.
     */
    private void loadKlighdExtensionsViaServiceLoader() {
        for (IViewerProvider viewerProvider : ServiceLoader.load(IViewerProvider.class,
                KlighdDataManager.class.getClassLoader())) {
            registerViewer(viewerProvider.getClass().getName(), viewerProvider);
        }
        for (IUpdateStrategy updateStrategy : ServiceLoader.load(IUpdateStrategy.class,
                KlighdDataManager.class.getClassLoader())) {
            registerUpdateStrategy(updateStrategy.getClass().getName(), updateStrategy);
        }
        for (IStyleModifier styleModifier : ServiceLoader.load(IStyleModifier.class,
                KlighdDataManager.class.getClassLoader())) {
            registerStyleModifier(styleModifier.getClass().getName(), styleModifier);
        }
        for (IAction action : ServiceLoader.load(IAction.class,
                KlighdDataManager.class.getClassLoader())) {
            registerAction(action.getClass().getName(), action);
        }
        for (IPreservedProperties preservedProperties : ServiceLoader.load(IPreservedProperties.class,
                KlighdDataManager.class.getClassLoader())) {
            registerPreservedProperties(preservedProperties);
        }
        for (IKlighdStartupHook startupHook : ServiceLoader.load(IKlighdStartupHook.class,
                KlighdDataManager.class.getClassLoader())) {
            try {
                startupHook.execute();
            } catch (Throwable t) {
                Klighd.log(new Status(IStatus.ERROR, Klighd.PLUGIN_ID, STARTUP_HOOK_ERROR_MSG, t));
            }
        }
    }

    private <T> void doRegisterExtension(final IConfigurationElement element,
            final Class<T> type, final Consumer<T> registration) {
        try {
            Object action = element.createExecutableExtension(ATTRIBUTE_CLASS);
            if (type != null && type.isInstance(action)) {
                registration.accept(type.cast(action));
            }
        } catch (CoreException exception) {
            Klighd.handle(exception, Klighd.PLUGIN_ID);
        }
    }

    private IUpdateStrategy doRegisterUpdateStrategy(final String id, final IUpdateStrategy updateStrategy) {
        final IUpdateStrategy previous = idUpdateStrategyMapping.put(id, updateStrategy);
        priorityUpdateStrategyMapping.put(updateStrategy.getPriority(), updateStrategy);
        
        if (this.highestPriorityUpdateStrategy == null 
                || this.highestPriorityUpdateStrategy.getPriority() < updateStrategy.getPriority()) {
            this.highestPriorityUpdateStrategy = updateStrategy;
        }
        return previous;
    }

    /**
     * Data record containing the information about an exporter.
     */
    public static final class ExporterDescriptor {

        // SUPPRESS CHECKSTYLE NEXT 5 Visibility|Javadoc
        public final String exporterId;
        public final String subFormatId;
        public final String description;
        public final String fileExtension;
        public final boolean supportsTiling;

        private ExporterDescriptor(final String exporterId, final String subFormatId,
                final String description, final String fileExtension, final boolean supportsTiling) {
            this.exporterId = exporterId;
            this.subFormatId = subFormatId;
            this.description = description;
            this.fileExtension = fileExtension;
            this.supportsTiling = supportsTiling;
        }
    }

    private void registerExporter(String id, IConfigurationElement element) {
        String subFormat = element.getAttribute("subFormat");
        if (subFormat == null) {
            subFormat = "";
        }
        final String extension = element.getAttribute("extension");
        final String descr = element.getAttribute("description");
        final boolean supportsTiling =
                Boolean.parseBoolean(element.getAttribute("supportsTiling"));

        // create the descriptor
        final ExporterDescriptor descriptor =
                new ExporterDescriptor(id, subFormat, descr, extension, supportsTiling);
        descriptors.add(descriptor);

        // put the configuration element into the map to lazy load the exporter later
        exportersMap.put(id, element);
    }

    /**
     * Data record containing the information about an export branding.
     */
    public static final class ExportBrandingDescriptor {
        public final String exporterId;
        public final Collection<String> supportedFormats;
        public final Supplier<IExportBranding> supplier;
        
        public ExportBrandingDescriptor(final String id, final Collection<String> supportedFormats,
                Supplier<IExportBranding> supplier) {
            this.exporterId = id;
            this.supportedFormats = supportedFormats;
            this.supplier = supplier;
        }
    }

    /**
     * Data record containing the information about an offscreen renderer.
     */
    public static final class OffscreenRendererDescriptor {
        public final String id;
        public final Collection<String> supportedFormats;
        public final Supplier<IOffscreenRenderer> supplier;
        
        public OffscreenRendererDescriptor(final String id, final Collection<String> supportedFormats,
                Supplier<IOffscreenRenderer> supplier) {
            this.id = id;
            this.supportedFormats = supportedFormats;
            this.supplier = supplier;
        }
    }

    private <T> void checkFormatsAndRegisterWithSupplier(final IConfigurationElement element, final Class<T> type, BiConsumer<Collection<String>, Supplier<T>> registration) {
        final String supportedFormats = element.getAttribute(ATTRIBUTE_SUPPORTED_FORMATS);

        if (Strings.isNullOrEmpty(supportedFormats)) {
            reportError(EXTP_ID_EXTENSIONS, element, ATTRIBUTE_SUPPORTED_FORMATS,
                    "Attribute value must not be empty.", null);
        } else {
            final Collection<String> supportedFormatsSplit = Lists.transform(
                    Arrays.asList(supportedFormats.split(",")), e -> e.trim());
            
            registration.accept(supportedFormatsSplit, () -> {
                try {
                    final Object extension = element.createExecutableExtension(ATTRIBUTE_CLASS);
                    if (type.isInstance(extension)) {
                        return type.cast(extension);
                    }
                } catch (final CoreException exception) {
                    Klighd.handle(exception, Klighd.PLUGIN_ID);
                }
                return null;
            });
        }
    }

    /**
     * Data record containing the information about an offscreen renderer.
     */
    public static final class CustomFigureWrapperDescriptor {
        public final String customFigureType;
        public final String wrapperType;
        public final String contributor;

        private CustomFigureWrapperDescriptor(final String customFigureType,
                final String wrapperType, final String contributor) {
            this.customFigureType = customFigureType;
            this.wrapperType = wrapperType;
            this.contributor = contributor;
        }
    }

    private void registerCustomFigureWrapper(final IConfigurationElement element) {
        final String customFigureType = element.getAttribute(ATTRIBUTE_FIGURE_CLASS_ENTRY_NAME);
        final String wrapperType = element.getAttribute(ATTRIBUTE_WRAPPER_CLASS_ENTRY_NAME);

        final String contributor = element.getContributor().getName();

        if (Strings.isNullOrEmpty(customFigureType)) {
            final String msg = "KLighD: Custom figure type is 'null' or empty "
                    + "in a custom figure wrapper extension of '" + contributor + "'.";
            Klighd.log(new Status(IStatus.ERROR, Klighd.PLUGIN_ID, msg));

        } else if (Strings.isNullOrEmpty(wrapperType)) {
            final String msg = "KLighD: Wrapper figure type is 'null' or empty "
                    + "in a custom figure wrapper extension of '" + contributor + "'.";
            Klighd.log(new Status(IStatus.ERROR, Klighd.PLUGIN_ID, msg));

        } else {
            customFigureWrapperMapping.add(new CustomFigureWrapperDescriptor(customFigureType, wrapperType, contributor));
        }
    }

    /* --------------------------------------------------------- */
    /*  API for programmatic registrations of extensions         */
    /* --------------------------------------------------------- */

    public KlighdDataManager registerViewer(final String id, final IViewerProvider viewerProvider) {
        return doRegisterExtension(id, viewerProvider, IViewerProvider.class,
                () -> idViewerProviderMapping.put(id, viewerProvider));
    }

    public KlighdDataManager registerUpdateStrategy(final String id, final IUpdateStrategy updateStrategy) {
        return doRegisterExtension(id, updateStrategy, IUpdateStrategy.class, 
                () -> doRegisterUpdateStrategy(id, updateStrategy));
    }

    public KlighdDataManager registerStyleModifier(final String id, final IStyleModifier styleModifier) {
        return doRegisterExtension(id, styleModifier, IStyleModifier.class,
                () -> idStyleModifierMapping.put(id, styleModifier));
    }

    public KlighdDataManager registerAction(final String id, final IAction action) {
        return doRegisterExtension(id, action, IAction.class,
                () -> idActionMapping.put(id, action));
    }

    public KlighdDataManager registerExportBranding(final String id,
            final IExportBranding exportBranding, String supportedFormat, final String... moreSupportedFormats) {

        return doRegisterExtension(id, exportBranding, IExportBranding.class, () -> {
            if (Strings.isNullOrEmpty(supportedFormat)) {
                throw new IllegalArgumentException(
                        "KLighD: The 'supportedFormat' must not be 'null' or empty, more formats may be added."); 
            } else {
                idExportBrandingMapping.put(id, new ExportBrandingDescriptor(id,
                        Lists.asList(supportedFormat, moreSupportedFormats), () -> exportBranding));
                return null;
            }
        });
    }

    public KlighdDataManager registerOffscreenRenderer(final String id,
            final IOffscreenRenderer offscreenRenderer, String supportedFormat, final String... moreSupportedFormats) {

        return doRegisterExtension(id, offscreenRenderer, IOffscreenRenderer.class, () -> {
            if (Strings.isNullOrEmpty(supportedFormat)) {
                throw new IllegalArgumentException(
                        "KLighD: The 'supportedFormat' must not be 'null' or empty, more formats may be added."); 
            } else {
                idOffscreenRendererMapping.put(id, new OffscreenRendererDescriptor(id,
                        Lists.asList(supportedFormat, moreSupportedFormats), () -> offscreenRenderer));
                return null;
            }
        });
    }

    private <T> KlighdDataManager doRegisterExtension(final String id, final T element,
            final Class<T> type, final Supplier<T> registration) {
        final String typeName = type.getSimpleName();
        final String msgSuffix = " while registering an '" + typeName + "'.";

        if (Strings.isNullOrEmpty(id))
            throw new IllegalArgumentException(
                    "KLighD: 'id' must not be 'null' or empty" + msgSuffix);
        else if (element == null)
            throw new IllegalArgumentException(
                    "KLighD: 'viewerProvider' must not be 'null'" + msgSuffix);
        else {
            final T previous = registration.get();
            if (previous != null)
                Klighd.log(new Status(IStatus.WARNING, Klighd.PLUGIN_ID,
                        typeName + " mapping of '" + id + "' -> '"
                                + previous.getClass().getCanonicalName()
                                + "' has been overwritten with instance of type '"
                                + type.getCanonicalName() + "'."));
        }

        return this;
    }

    public KlighdDataManager registerCustomFigureWrapper(final String customFigureType,
            final String wrapperType) {
        return registerCustomFigureWrapper(customFigureType, wrapperType, null);
    }

    public KlighdDataManager registerCustomFigureWrapper(final String customFigureType,
            final String wrapperType, final String contributingBundle) {
        if (Strings.isNullOrEmpty(customFigureType)) {
            throw new IllegalArgumentException("KLighD: Custom figure type is 'null' or empty.");

        } else if (Strings.isNullOrEmpty(wrapperType)) {
            throw new IllegalArgumentException("KLighD: Wrapper figure type is 'null' or empty.");

        } else {
            customFigureWrapperMapping.add(new CustomFigureWrapperDescriptor(customFigureType,
                    wrapperType, contributingBundle));
        }

        return this;
    }

    public KlighdDataManager registerDiagramSynthesisClass(Class<? extends AbstractDiagramSynthesis<?>> clazz) {
        return registerDiagramSynthesisClass(null, clazz);
    }

    public KlighdDataManager registerDiagramSynthesisClass(final String id,
            Class<? extends AbstractDiagramSynthesis<?>> clazz) {
        return registerDiagramSynthesisClass(id, clazz, true);
    }

    public KlighdDataManager registerDiagramSynthesisClass(final String id,
            Class<? extends AbstractDiagramSynthesis<?>> clazz, boolean wrapWithReinitializer) {
        registerDiagramSynthesisClass(id, clazz, wrapWithReinitializer,
                this.idSynthesisMapping, this.typeSynthesisMapping);

        return this;
    }
    
    public KlighdDataManager registerPreservedProperty(IProperty<?> preservedProperty) {
        this.preservedProperties.add(preservedProperty);
        return this;
    }
    
    public KlighdDataManager registerPreservedProperties(Iterable<IProperty<?>> preservedProperties) {
        for (IProperty<?> preservedProperty : preservedProperties) {
            registerPreservedProperty(preservedProperty);
        }
        return this;
    }

    /**
     * Loads the registered {@link de.cau.cs.kieler.klighd.syntheses.AbstractDiagramSynthesis
     * AbstractDiagramSynthesis} from the extension point and and returns the .
     */
    private final void loadDiagramSynthesesViaExtensionPoint(Map<String, ISynthesis> idSynthesisMapping,
            Multimap<Class<?>, ISynthesis> typeSynthesisMapping) {
        final Iterable<IConfigurationElement> extensions = Iterables.filter(
                Arrays.asList(
                        Platform.getExtensionRegistry().getConfigurationElementsFor(EXTP_ID_DIAGRAM_SYNTHESES)
                ),
                element -> ELEMENT_DIAGRAM_SYNTHESIS.equals(element.getName())
        );

        for (final IConfigurationElement element : extensions) {
            final String id = element.getAttribute(ATTRIBUTE_ID);
            if (Strings.isNullOrEmpty(id))
                reportError(EXTP_ID_DIAGRAM_SYNTHESES, element, ATTRIBUTE_ID, null, null);
            
            else {
                    
                // initialize model transformation from the extension point
                ISynthesis synthesis = null;
                try {
                    synthesis =
                            (ISynthesis) element.createExecutableExtension(ATTRIBUTE_CLASS);

                } catch (final CoreException exception) {
                    Klighd.handle(
                            new Status(IStatus.ERROR, Klighd.PLUGIN_ID,
                                    CORE_EXCEPTION_ERROR_MSG.replace("<<CLAZZ>>",
                                            element.getAttribute(ATTRIBUTE_CLASS)), exception));
                } catch (final NoClassDefFoundError exception) {
                    final String msg =
                            NO_CLASS_DEF_FOUND_ERROR_MSG.replace("<<CLAZZ>>",
                                    element.getAttribute(ATTRIBUTE_CLASS).replaceFirst(
                                            GuiceBasedSynthesisFactory.CLASS_NAME + ":", ""));
                    Klighd.handle(
                            new Status(IStatus.ERROR, Klighd.PLUGIN_ID, msg, exception));
                } catch (final WrappedException exception) {
                    final String msg =
                            NO_CLASS_DEF_FOUND_ERROR_MSG.replace("<<CLAZZ>>",
                                    element.getAttribute(ATTRIBUTE_CLASS).replaceFirst(
                                            GuiceBasedSynthesisFactory.CLASS_NAME + ":", ""));
                    Klighd.handle(
                            new Status(IStatus.ERROR, Klighd.PLUGIN_ID, msg, exception
                                    .getCause()));
                } catch (final Throwable throwable) {
                    final String msg =
                           UNEXPECTED_FAILURE_MSG.replace("<<CLAZZ>>",
                                    element.getAttribute(ATTRIBUTE_CLASS).replaceFirst(
                                            GuiceBasedSynthesisFactory.CLASS_NAME + ":", ""));
                    Klighd.handle(
                            new Status(IStatus.ERROR, Klighd.PLUGIN_ID, msg, throwable));
                }

                if (synthesis != null) {
                    idSynthesisMapping.put(id, synthesis);
                    inferInputType(synthesis, id, typeSynthesisMapping);
                }
            }
        }
    }

    /**
     * Loads the registered {@link de.cau.cs.kieler.klighd.syntheses.AbstractDiagramSynthesis
     * AbstractDiagramSynthesis} via Java {@link ServiceLoader}.
     */
    private void loadDiagramSynthesesViaServiceLoader(Map<String, ISynthesis> idSynthesisMapping,
            Multimap<Class<?>, ISynthesis> typeSynthesisMapping) {

        @SuppressWarnings("unchecked")
        final Iterable<Class<? extends AbstractDiagramSynthesis<?>>> extensions = Iterables.transform(
                ServiceLoader.load(AbstractDiagramSynthesis.class, KlighdDataManager.class.getClassLoader()),
                s -> (Class<? extends AbstractDiagramSynthesis<?>>) s.getClass()
        );

        for (Class<? extends AbstractDiagramSynthesis<?>> clazz: extensions) {
            registerDiagramSynthesisClass(null, clazz, true, idSynthesisMapping, typeSynthesisMapping);
        }
    }

    private void registerDiagramSynthesisClass(final String id,
            Class<? extends AbstractDiagramSynthesis<?>> clazz, boolean wrapWithReinitializer,
                    Map<String, ISynthesis> idSynthesisMapping,
                    Multimap<Class<?>, ISynthesis> typeSynthesisMapping) {
        final String nonNullId = !Strings.isNullOrEmpty(id) ? id : clazz.getCanonicalName();
        
        final ISynthesis synthesis;
        if (wrapWithReinitializer)
            synthesis = GuiceBasedSynthesisFactory.getReinitializingDiagramSynthesisProxy(clazz);
        else {
            final Constructor<? extends AbstractDiagramSynthesis<?>> constructor;
            try {
                constructor = clazz.getConstructor();

            } catch (final NoSuchMethodException | SecurityException e) {
                final String msg =
                        NO_VALID_CONSTRUCTOR_FAILURE_MSG.replace("<<CLAZZ>>", clazz.getCanonicalName());
                Klighd.handle(new Status(IStatus.ERROR, Klighd.PLUGIN_ID, msg, e));
                
                return;
            }
            try {
                synthesis = constructor.newInstance();

            } catch (final InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException e) {
                final String msg =
                        INSTANTIATION_FAILURE_MSG.replace("<<CLAZZ>>", clazz.getCanonicalName());
                Klighd.handle(new Status(IStatus.ERROR, Klighd.PLUGIN_ID, msg, e));

                return;
            }
        }

        idSynthesisMapping.put(nonNullId, synthesis);
        inferInputType(synthesis, nonNullId, typeSynthesisMapping);
    }

    private void inferInputType(ISynthesis synthesis, String id,
            Multimap<Class<?>, ISynthesis> typeSynthesisMapping) {
        try {
            final Class<?> inputDataType = synthesis.getInputDataType();
            if (inputDataType != null) {
                typeSynthesisMapping.put(inputDataType, synthesis);
            }
        } catch (final WrappedException exception) {
            Klighd.handle(
                    new Status(IStatus.ERROR, Klighd.PLUGIN_ID, exception
                            .getMessage(), exception.getCause()));
        } catch (final Exception exception) {
            final String msg =
                    "KLighD: An unexpected exception occured while loading "
                            + "diagram synthesis " + id
                            + ". See attached trace for details." + NEW_LINE
                            + exception.getMessage();
            Klighd.handle(
                    new Status(IStatus.ERROR, Klighd.PLUGIN_ID, msg,
                            exception.getCause()));
        }
    }

    /**
     * Returns the list of registered {@link ISynthesis} implementations whose input types are
     * compatible to <code>type</code>.
     *
     * @param type
     *            the type the of model to be translated
     * @return the matching {@link ISynthesis} implementations
     */
    public Iterable<ISynthesis> getAvailableSyntheses(final Class<?> type) {

        if (type == null) {
            return null;
        } else {

            final Iterable<ISynthesis> knownSyntheses = this.concreteTypeSynthesisMapping.get(type);
            if (knownSyntheses != null) {
                // if the fitting syntheses have been determined already, use that those
                return knownSyntheses;

            } else {
                // otherwise reveal those input types of registered ISynthesis implementations
                //  that are compatible to 'type'
                final List<Class<?>> validTypes =
                        Lists.newArrayList(Iterables.filter(typeSynthesisMapping.keySet(),
                                new Predicate<Class<?>>() {
                                    public boolean apply(final Class<?> clazz) {
                                        return clazz.isAssignableFrom(type);
                                    }
                                }));

                final Collection<ISynthesis> res;
                if (validTypes.isEmpty()) {
                    res = Collections.emptyList();

                } else {
                    // sort them s.t. the most concrete type is at position 0
                    Collections.sort(validTypes, TYPE_SORTER);

                    Builder<ISynthesis> builder = ImmutableList.builder();
                    // and reveal the collection of related ISynthesis from the main mapping
                    // for each of the valid types
                    for (Class<?> validType : validTypes) {
                        builder.addAll(typeSynthesisMapping.get(validType));
                    }

                    res = builder.build();
                }

                this.concreteTypeSynthesisMapping.put(type, res);
                return res;
            }
        }
    }

    /** Sorts a list of types s.t. a super type is place after a sub type. */
    private static final Comparator<Class<?>> TYPE_SORTER = new Comparator<Class<?>>() {

        /**
         * {@inheritDoc}
         */
        public int compare(final Class<?> o1, final Class<?> o2) {
            if (o1.isAssignableFrom(o2)) {
                return 1;
            } else if (o2.isAssignableFrom(o1)) {
                return -1;
            } else {
                return 0;
            }
        }
    };


    /**
     * Returns the {@link ISynthesis} with the given identifier.
     *
     * @param id
     *            the identifier
     * @return the {@link ISynthesis} instance or <code>null</code> if there is no synthesis with
     *         the given id
     */
    public ISynthesis getDiagramSynthesisById(final String id) {
        if (id == null) {
            return null;
        }
        return idSynthesisMapping.get(id);
    }

    /**
     * Returns the identifier of the given {@link ISynthesis} instance.
     * 
     * @param synthesis
     *            the synthesis
     * @return The ID or null if synthesis is not registered
     */
    public String getSynthesisID(final ISynthesis synthesis) {
        if (synthesis != null) {
            // Fast solution first
            // Works only when given synthesis is directly retrieved form getDiagramSynthesisById or
            // getAvailableSyntheses
            for (Entry<String, ISynthesis> entry : idSynthesisMapping.entrySet()) {
                if (synthesis == entry.getValue()) {
                    return entry.getKey();
                }
            }
            // Second case when the instance is not retrieved from this class.
            // This case is unlikely because syntheses should not be instantiated by the user.
            for (Entry<String, ISynthesis> entry : idSynthesisMapping.entrySet()) {
                ISynthesis matchSynthesis = entry.getValue();
                // Check and unwrap proxy synthesis
                if (matchSynthesis instanceof ReinitializingDiagramSynthesisProxy) {
                    if (synthesis.getClass() == ((ReinitializingDiagramSynthesisProxy<?>) matchSynthesis)
                            .getDelegate().getClass()) {
                        return entry.getKey();
                    }
                } else {
                    if (synthesis.getClass() == matchSynthesis.getClass()) {
                        return entry.getKey();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns all registered instances of {@link IViewerProvider}.
     *
     * @return an immutable collection of the registered {@link IViewerProvider IViewerProviders}
     */
    public Collection<IViewerProvider> getAvailableViewerProviders() {
        return Collections.unmodifiableCollection(idViewerProviderMapping.values());
    }

    /**
     * Returns the viewer provider with the given identifier.
     *
     * @param id
     *            the identifier
     * @return the viewer provider or null if there is no viewer provider with the given id
     */
    public IViewerProvider getViewerProviderById(final String id) {
        if (id == null) {
            return null;
        }
        return idViewerProviderMapping.get(id);
    }

    /**
     * Returns the update strategy with the given identifier.
     *
     * @param id
     *            the identifier
     * @return the update strategy
     */
    public IUpdateStrategy getUpdateStrategyById(final String id) {
        return idUpdateStrategyMapping.get(id);
    }


    /**
     * Returns one of those registered {@link IUpdateStrategy IUpdateStrategies} with the highest
     * priority. If multiple {@link IUpdateStrategy IUpdateStrategies} with the maximal priority
     * are registered, no assertion on the provided one can be made!
     *
     * @return one of those registered {@link IUpdateStrategy IUpdateStrategies} with the highest
     *         priority.
     */
    public IUpdateStrategy getHighestPriorityUpdateStrategy() {
        return this.highestPriorityUpdateStrategy;
    }
    
    /**
     * Returns one of those registered {@link IUpdateStrategy IUpdateStrategies} with the highest
     * priority strictly less than the given strategy. If multiple {@link IUpdateStrategy
     * IUpdateStrategies} with such a priority are registered, no assertion on the provided one can
     * be made!
     * 
     * @param strategy
     *            the update strategy to return the next lower prioritized to.
     * @return one of those registered {@link IUpdateStrategy IUpdateStrategies} with the highest
     *         priority strictly less than the given strategy or {@code null} if none applicable.
     */
    public IUpdateStrategy getNextPrioritizedUpdateStrategy(final IUpdateStrategy strategy) {
        Entry<Integer, IUpdateStrategy> lowerEntry =
                priorityUpdateStrategyMapping.lowerEntry(strategy.getPriority());
        if (lowerEntry != null) {
            return lowerEntry.getValue();
        } else {
            return null;
        }
    }


    /**
     * Returns the style modifier with the given identifier.
     *
     * @param id
     *            the identifier
     * @return the style modifier
     */
    public IStyleModifier getStyleModifierById(final String id) {
        return idStyleModifierMapping.get(id);
    }


    /**
     * Returns the action with the given identifier.
     *
     * @param id
     *            the identifier
     * @return the action
     */
    public IAction getActionById(final String id) {
        return idActionMapping.get(id);
    }


    /**
     * Returns the id of the given {@link IAction}.
     *
     * @param action
     *            the {@link IAction}
     * @return its id
     */
    public String getActionsId(final IAction action) {
        return idActionMapping.inverse().get(action);
    }


    /**
     * Returns the set of registered action ids.
     *
     * @return the registered action ids
     */
    public Set<String> getActionIds() {
        return idActionMapping.keySet();
    }

    /**
     * @return return a copy of the original list
     */
    public List<ExporterDescriptor> getAvailableExporters() {
        return Lists.newLinkedList(descriptors);
    }

    /**
     * Returns the collection of registered {@link IExportBranding IExportHooks} with the given
     * <code>format</code>.
     *
     * @param format
     *            the format an {@link IExportBranding} is requested for
     * @param viewContext
     *            the {@link ViewContext} providing access to the diagram' view & source model
     *
     * @return the {@link Iterable} of {@link IExportBranding IExportHooks}
     */
    public Iterable<IExportBranding> getExportBrandingByFormat(final String format,
            final ViewContext viewContext) {
        final Iterable<ExportBrandingDescriptor> descriptors = Collections2.filter(
                idExportBrandingMapping.values(),
                descr -> descr.supportedFormats.contains(format)
        );
        
        final List<IExportBranding> result = Lists.newArrayList();
        for (final ExportBrandingDescriptor descr : descriptors) {
            final IExportBranding branding = descr.supplier.get();
            
            if (branding != null) {
                branding.setViewContext(viewContext);
    
                if (branding.isEnabled()) {
                    result.add(branding);
                }
            }
        }

        return result;
    }

    /**
     * Returns the collection of registered {@link IOffscreenRenderer IOffscreenRenderers} with the
     * given <code>format</code>.
     *
     * @param format the format an {@link IOffscreenRenderer} is requested for
     *
     * @return the {@link Collection} of
     */
    public Collection<OffscreenRendererDescriptor> getOffscreenRenderersByFormat(final String format) {
        return Collections.unmodifiableCollection(
                Collections2.filter(
                        idOffscreenRendererMapping.values(),
                        descr -> descr.supportedFormats.contains(format)
                )
        );
    }

    /**
     * Returns the list of registered custom figure wrapper mappings.
     * 
     * @return the {@link List} of registered custom figure wrappers
     */
    public List<CustomFigureWrapperDescriptor> getCustomFigureWrapperDescriptors() {
        return Collections.unmodifiableList(customFigureWrapperMapping);
    }
    
    /**
     * Returns the list of registered properties that shall be preserved from the layout graph.
     * 
     * @return the {@link List} of registered properties to preserve
     */
    public List<IProperty<?>> getPreservedProperties() {
        return this.preservedProperties;
    }

}
