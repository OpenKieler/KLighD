/**
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 * 
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2012 by
 * + Kiel University
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.klighd.krendering.impl;

import de.cau.cs.kieler.klighd.kgraph.KGraphPackage;

import de.cau.cs.kieler.klighd.kgraph.impl.KGraphPackageImpl;

import de.cau.cs.kieler.klighd.krendering.Arc;
import de.cau.cs.kieler.klighd.krendering.Colors;
import de.cau.cs.kieler.klighd.krendering.HorizontalAlignment;
import de.cau.cs.kieler.klighd.krendering.KAction;
import de.cau.cs.kieler.klighd.krendering.KArc;
import de.cau.cs.kieler.klighd.krendering.KAreaPlacementData;
import de.cau.cs.kieler.klighd.krendering.KBackground;
import de.cau.cs.kieler.klighd.krendering.KBottomPosition;
import de.cau.cs.kieler.klighd.krendering.KChildArea;
import de.cau.cs.kieler.klighd.krendering.KColor;
import de.cau.cs.kieler.klighd.krendering.KColoring;
import de.cau.cs.kieler.klighd.krendering.KContainerRendering;
import de.cau.cs.kieler.klighd.krendering.KCustomRendering;
import de.cau.cs.kieler.klighd.krendering.KDecoratorPlacementData;
import de.cau.cs.kieler.klighd.krendering.KEllipse;
import de.cau.cs.kieler.klighd.krendering.KFontBold;
import de.cau.cs.kieler.klighd.krendering.KFontItalic;
import de.cau.cs.kieler.klighd.krendering.KFontName;
import de.cau.cs.kieler.klighd.krendering.KFontSize;
import de.cau.cs.kieler.klighd.krendering.KForeground;
import de.cau.cs.kieler.klighd.krendering.KGridPlacement;
import de.cau.cs.kieler.klighd.krendering.KGridPlacementData;
import de.cau.cs.kieler.klighd.krendering.KHorizontalAlignment;
import de.cau.cs.kieler.klighd.krendering.KImage;
import de.cau.cs.kieler.klighd.krendering.KInvisibility;
import de.cau.cs.kieler.klighd.krendering.KLeftPosition;
import de.cau.cs.kieler.klighd.krendering.KLineCap;
import de.cau.cs.kieler.klighd.krendering.KLineJoin;
import de.cau.cs.kieler.klighd.krendering.KLineStyle;
import de.cau.cs.kieler.klighd.krendering.KLineWidth;
import de.cau.cs.kieler.klighd.krendering.KPlacement;
import de.cau.cs.kieler.klighd.krendering.KPlacementData;
import de.cau.cs.kieler.klighd.krendering.KPointPlacementData;
import de.cau.cs.kieler.klighd.krendering.KPolygon;
import de.cau.cs.kieler.klighd.krendering.KPolyline;
import de.cau.cs.kieler.klighd.krendering.KPosition;
import de.cau.cs.kieler.klighd.krendering.KRectangle;
import de.cau.cs.kieler.klighd.krendering.KRendering;
import de.cau.cs.kieler.klighd.krendering.KRenderingFactory;
import de.cau.cs.kieler.klighd.krendering.KRenderingLibrary;
import de.cau.cs.kieler.klighd.krendering.KRenderingPackage;
import de.cau.cs.kieler.klighd.krendering.KRenderingRef;
import de.cau.cs.kieler.klighd.krendering.KRightPosition;
import de.cau.cs.kieler.klighd.krendering.KRotation;
import de.cau.cs.kieler.klighd.krendering.KRoundedBendsPolyline;
import de.cau.cs.kieler.klighd.krendering.KRoundedRectangle;
import de.cau.cs.kieler.klighd.krendering.KShadow;
import de.cau.cs.kieler.klighd.krendering.KSpline;
import de.cau.cs.kieler.klighd.krendering.KStyle;
import de.cau.cs.kieler.klighd.krendering.KStyleHolder;
import de.cau.cs.kieler.klighd.krendering.KStyleRef;
import de.cau.cs.kieler.klighd.krendering.KText;
import de.cau.cs.kieler.klighd.krendering.KTextStrikeout;
import de.cau.cs.kieler.klighd.krendering.KTextUnderline;
import de.cau.cs.kieler.klighd.krendering.KTopPosition;
import de.cau.cs.kieler.klighd.krendering.KVerticalAlignment;
import de.cau.cs.kieler.klighd.krendering.KXPosition;
import de.cau.cs.kieler.klighd.krendering.KYPosition;
import de.cau.cs.kieler.klighd.krendering.LineCap;
import de.cau.cs.kieler.klighd.krendering.LineJoin;
import de.cau.cs.kieler.klighd.krendering.LineStyle;
import de.cau.cs.kieler.klighd.krendering.ModifierState;
import de.cau.cs.kieler.klighd.krendering.Trigger;
import de.cau.cs.kieler.klighd.krendering.Underline;
import de.cau.cs.kieler.klighd.krendering.VerticalAlignment;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.ETypeParameter;

import org.eclipse.emf.ecore.impl.EPackageImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class KRenderingPackageImpl extends EPackageImpl implements KRenderingPackage {
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kPositionEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kRenderingEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kEllipseEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kRectangleEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kRoundedRectangleEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kPolylineEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kPolygonEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kImageEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kDecoratorPlacementDataEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kContainerRenderingEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kArcEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kStyleEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kRenderingLibraryEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kRenderingRefEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kChildAreaEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kTextEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kPlacementEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kGridPlacementEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kPlacementDataEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kGridPlacementDataEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kAreaPlacementDataEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kCustomRenderingEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kColorEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kLineWidthEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kLineStyleEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kVerticalAlignmentEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kHorizontalAlignmentEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kxPositionEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kyPositionEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kLeftPositionEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kRightPositionEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kTopPositionEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kBottomPositionEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kSplineEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kForegroundEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kColoringEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kBackgroundEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kFontBoldEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kFontItalicEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kFontNameEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kFontSizeEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kRoundedBendsPolylineEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kRotationEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kLineCapEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kActionEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kPointPlacementDataEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kStyleHolderEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kInvisibilityEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kShadowEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kTextUnderlineEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kStyleRefEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kTextStrikeoutEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass kLineJoinEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EEnum lineStyleEEnum = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EEnum horizontalAlignmentEEnum = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EEnum verticalAlignmentEEnum = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EEnum lineCapEEnum = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EEnum modifierStateEEnum = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EEnum triggerEEnum = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EEnum underlineEEnum = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EEnum lineJoinEEnum = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EEnum arcEEnum = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EDataType colorsEDataType = null;

    /**
     * Creates an instance of the model <b>Package</b>, registered with
     * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
     * package URI value.
     * <p>Note: the correct way to create the package is via the static
     * factory method {@link #init init()}, which also performs
     * initialization of the package, or returns the registered package,
     * if one already exists.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.eclipse.emf.ecore.EPackage.Registry
     * @see de.cau.cs.kieler.klighd.krendering.KRenderingPackage#eNS_URI
     * @see #init()
     * @generated
     */
    private KRenderingPackageImpl() {
        super(eNS_URI, KRenderingFactory.eINSTANCE);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private static boolean isInited = false;

    /**
     * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
     *
     * <p>This method is used to initialize {@link KRenderingPackage#eINSTANCE} when that field is accessed.
     * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #eNS_URI
     * @see #createPackageContents()
     * @see #initializePackageContents()
     * @generated
     */
    public static KRenderingPackage init() {
        if (isInited) return (KRenderingPackage)EPackage.Registry.INSTANCE.getEPackage(KRenderingPackage.eNS_URI);

        // Obtain or create and register package
        Object registeredKRenderingPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
        KRenderingPackageImpl theKRenderingPackage = registeredKRenderingPackage instanceof KRenderingPackageImpl ? (KRenderingPackageImpl)registeredKRenderingPackage : new KRenderingPackageImpl();

        isInited = true;

        // Initialize simple dependencies
        KGraphPackage.eINSTANCE.eClass();

        // Create package meta-data objects
        theKRenderingPackage.createPackageContents();

        // Initialize created meta-data
        theKRenderingPackage.initializePackageContents();

        // Mark meta-data to indicate it can't be changed
        theKRenderingPackage.freeze();

        // Update the registry and return the package
        EPackage.Registry.INSTANCE.put(KRenderingPackage.eNS_URI, theKRenderingPackage);
        return theKRenderingPackage;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKPosition() {
        return kPositionEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getKPosition_X() {
        return (EReference)kPositionEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getKPosition_Y() {
        return (EReference)kPositionEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKRendering() {
        return kRenderingEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getKRendering_Parent() {
        return (EReference)kRenderingEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getKRendering_PlacementData() {
        return (EReference)kRenderingEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getKRendering_Actions() {
        return (EReference)kRenderingEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKEllipse() {
        return kEllipseEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKRectangle() {
        return kRectangleEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKRoundedRectangle() {
        return kRoundedRectangleEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKRoundedRectangle_CornerWidth() {
        return (EAttribute)kRoundedRectangleEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKRoundedRectangle_CornerHeight() {
        return (EAttribute)kRoundedRectangleEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKPolyline() {
        return kPolylineEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getKPolyline_Points() {
        return (EReference)kPolylineEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getKPolyline_JunctionPointRendering() {
        return (EReference)kPolylineEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKPolygon() {
        return kPolygonEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKImage() {
        return kImageEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKImage_BundleName() {
        return (EAttribute)kImageEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKImage_ImagePath() {
        return (EAttribute)kImageEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKImage_ImageObject() {
        return (EAttribute)kImageEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getKImage_ClipShape() {
        return (EReference)kImageEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKDecoratorPlacementData() {
        return kDecoratorPlacementDataEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKDecoratorPlacementData_Absolute() {
        return (EAttribute)kDecoratorPlacementDataEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKDecoratorPlacementData_XOffset() {
        return (EAttribute)kDecoratorPlacementDataEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKDecoratorPlacementData_YOffset() {
        return (EAttribute)kDecoratorPlacementDataEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKDecoratorPlacementData_RotateWithLine() {
        return (EAttribute)kDecoratorPlacementDataEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKDecoratorPlacementData_Width() {
        return (EAttribute)kDecoratorPlacementDataEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKDecoratorPlacementData_Height() {
        return (EAttribute)kDecoratorPlacementDataEClass.getEStructuralFeatures().get(5);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKDecoratorPlacementData_Relative() {
        return (EAttribute)kDecoratorPlacementDataEClass.getEStructuralFeatures().get(6);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKContainerRendering() {
        return kContainerRenderingEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getKContainerRendering_Children() {
        return (EReference)kContainerRenderingEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getKContainerRendering_ChildPlacement() {
        return (EReference)kContainerRenderingEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKArc() {
        return kArcEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKArc_StartAngle() {
        return (EAttribute)kArcEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKArc_ArcAngle() {
        return (EAttribute)kArcEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKArc_ArcType() {
        return (EAttribute)kArcEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKStyle() {
        return kStyleEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKStyle_PropagateToChildren() {
        return (EAttribute)kStyleEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKStyle_ModifierId() {
        return (EAttribute)kStyleEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKStyle_Selection() {
        return (EAttribute)kStyleEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKRenderingLibrary() {
        return kRenderingLibraryEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getKRenderingLibrary_Renderings() {
        return (EReference)kRenderingLibraryEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKRenderingRef() {
        return kRenderingRefEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getKRenderingRef_Rendering() {
        return (EReference)kRenderingRefEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKChildArea() {
        return kChildAreaEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKText() {
        return kTextEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKText_Text() {
        return (EAttribute)kTextEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKText_CursorSelectable() {
        return (EAttribute)kTextEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKText_Editable() {
        return (EAttribute)kTextEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKPlacement() {
        return kPlacementEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKGridPlacement() {
        return kGridPlacementEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKGridPlacement_NumColumns() {
        return (EAttribute)kGridPlacementEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getKGridPlacement_TopLeft() {
        return (EReference)kGridPlacementEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getKGridPlacement_BottomRight() {
        return (EReference)kGridPlacementEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKPlacementData() {
        return kPlacementDataEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKGridPlacementData() {
        return kGridPlacementDataEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKGridPlacementData_MinCellWidth() {
        return (EAttribute)kGridPlacementDataEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKGridPlacementData_MinCellHeight() {
        return (EAttribute)kGridPlacementDataEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKGridPlacementData_FlexibleWidth() {
        return (EAttribute)kGridPlacementDataEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKGridPlacementData_FlexibleHeight() {
        return (EAttribute)kGridPlacementDataEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKAreaPlacementData() {
        return kAreaPlacementDataEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getKAreaPlacementData_TopLeft() {
        return (EReference)kAreaPlacementDataEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getKAreaPlacementData_BottomRight() {
        return (EReference)kAreaPlacementDataEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKCustomRendering() {
        return kCustomRenderingEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKCustomRendering_ClassName() {
        return (EAttribute)kCustomRenderingEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKCustomRendering_BundleName() {
        return (EAttribute)kCustomRenderingEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKCustomRendering_FigureObject() {
        return (EAttribute)kCustomRenderingEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKColor() {
        return kColorEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKColor_Red() {
        return (EAttribute)kColorEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKColor_Green() {
        return (EAttribute)kColorEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKColor_Blue() {
        return (EAttribute)kColorEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKLineWidth() {
        return kLineWidthEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKLineWidth_LineWidth() {
        return (EAttribute)kLineWidthEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKLineStyle() {
        return kLineStyleEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKLineStyle_LineStyle() {
        return (EAttribute)kLineStyleEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKLineStyle_DashPattern() {
        return (EAttribute)kLineStyleEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKLineStyle_DashOffset() {
        return (EAttribute)kLineStyleEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKVerticalAlignment() {
        return kVerticalAlignmentEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKVerticalAlignment_VerticalAlignment() {
        return (EAttribute)kVerticalAlignmentEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKHorizontalAlignment() {
        return kHorizontalAlignmentEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKHorizontalAlignment_HorizontalAlignment() {
        return (EAttribute)kHorizontalAlignmentEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKXPosition() {
        return kxPositionEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKXPosition_Absolute() {
        return (EAttribute)kxPositionEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKXPosition_Relative() {
        return (EAttribute)kxPositionEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKYPosition() {
        return kyPositionEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKYPosition_Absolute() {
        return (EAttribute)kyPositionEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKYPosition_Relative() {
        return (EAttribute)kyPositionEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKLeftPosition() {
        return kLeftPositionEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKRightPosition() {
        return kRightPositionEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKTopPosition() {
        return kTopPositionEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKBottomPosition() {
        return kBottomPositionEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKSpline() {
        return kSplineEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKForeground() {
        return kForegroundEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKColoring() {
        return kColoringEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getKColoring_Color() {
        return (EReference)kColoringEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKColoring_Alpha() {
        return (EAttribute)kColoringEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getKColoring_TargetColor() {
        return (EReference)kColoringEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKColoring_TargetAlpha() {
        return (EAttribute)kColoringEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKColoring_GradientAngle() {
        return (EAttribute)kColoringEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKBackground() {
        return kBackgroundEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKFontBold() {
        return kFontBoldEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKFontBold_Bold() {
        return (EAttribute)kFontBoldEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKFontItalic() {
        return kFontItalicEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKFontItalic_Italic() {
        return (EAttribute)kFontItalicEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKFontName() {
        return kFontNameEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKFontName_Name() {
        return (EAttribute)kFontNameEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKFontSize() {
        return kFontSizeEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKFontSize_Size() {
        return (EAttribute)kFontSizeEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKFontSize_ScaleWithZoom() {
        return (EAttribute)kFontSizeEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKRoundedBendsPolyline() {
        return kRoundedBendsPolylineEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKRoundedBendsPolyline_BendRadius() {
        return (EAttribute)kRoundedBendsPolylineEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKRotation() {
        return kRotationEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKRotation_Rotation() {
        return (EAttribute)kRotationEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getKRotation_RotationAnchor() {
        return (EReference)kRotationEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKLineCap() {
        return kLineCapEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKLineCap_LineCap() {
        return (EAttribute)kLineCapEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKAction() {
        return kActionEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKAction_ActionId() {
        return (EAttribute)kActionEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKAction_Trigger() {
        return (EAttribute)kActionEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKAction_AltPressed() {
        return (EAttribute)kActionEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKAction_CtrlCmdPressed() {
        return (EAttribute)kActionEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKAction_ShiftPressed() {
        return (EAttribute)kActionEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKPointPlacementData() {
        return kPointPlacementDataEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getKPointPlacementData_ReferencePoint() {
        return (EReference)kPointPlacementDataEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKPointPlacementData_HorizontalAlignment() {
        return (EAttribute)kPointPlacementDataEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKPointPlacementData_VerticalAlignment() {
        return (EAttribute)kPointPlacementDataEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKPointPlacementData_HorizontalMargin() {
        return (EAttribute)kPointPlacementDataEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKPointPlacementData_VerticalMargin() {
        return (EAttribute)kPointPlacementDataEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKPointPlacementData_MinWidth() {
        return (EAttribute)kPointPlacementDataEClass.getEStructuralFeatures().get(5);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKPointPlacementData_MinHeight() {
        return (EAttribute)kPointPlacementDataEClass.getEStructuralFeatures().get(6);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKStyleHolder() {
        return kStyleHolderEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getKStyleHolder_Styles() {
        return (EReference)kStyleHolderEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKStyleHolder_Id() {
        return (EAttribute)kStyleHolderEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKInvisibility() {
        return kInvisibilityEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKInvisibility_Invisible() {
        return (EAttribute)kInvisibilityEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKShadow() {
        return kShadowEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKShadow_XOffset() {
        return (EAttribute)kShadowEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKShadow_YOffset() {
        return (EAttribute)kShadowEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKShadow_Blur() {
        return (EAttribute)kShadowEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getKShadow_Color() {
        return (EReference)kShadowEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKTextUnderline() {
        return kTextUnderlineEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKTextUnderline_Underline() {
        return (EAttribute)kTextUnderlineEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getKTextUnderline_Color() {
        return (EReference)kTextUnderlineEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKStyleRef() {
        return kStyleRefEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getKStyleRef_StyleHolder() {
        return (EReference)kStyleRefEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKStyleRef_ReferencedTypes() {
        return (EAttribute)kStyleRefEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKTextStrikeout() {
        return kTextStrikeoutEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKTextStrikeout_StruckOut() {
        return (EAttribute)kTextStrikeoutEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getKTextStrikeout_Color() {
        return (EReference)kTextStrikeoutEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getKLineJoin() {
        return kLineJoinEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKLineJoin_LineJoin() {
        return (EAttribute)kLineJoinEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getKLineJoin_MiterLimit() {
        return (EAttribute)kLineJoinEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EEnum getLineStyle() {
        return lineStyleEEnum;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EEnum getHorizontalAlignment() {
        return horizontalAlignmentEEnum;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EEnum getVerticalAlignment() {
        return verticalAlignmentEEnum;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EEnum getLineCap() {
        return lineCapEEnum;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EEnum getModifierState() {
        return modifierStateEEnum;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EEnum getTrigger() {
        return triggerEEnum;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EEnum getUnderline() {
        return underlineEEnum;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EEnum getLineJoin() {
        return lineJoinEEnum;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EEnum getArc() {
        return arcEEnum;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EDataType getColors() {
        return colorsEDataType;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public KRenderingFactory getKRenderingFactory() {
        return (KRenderingFactory)getEFactoryInstance();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private boolean isCreated = false;

    /**
     * Creates the meta-model objects for the package.  This method is
     * guarded to have no affect on any invocation but its first.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void createPackageContents() {
        if (isCreated) return;
        isCreated = true;

        // Create classes and their features
        kPositionEClass = createEClass(KPOSITION);
        createEReference(kPositionEClass, KPOSITION__X);
        createEReference(kPositionEClass, KPOSITION__Y);

        kRenderingEClass = createEClass(KRENDERING);
        createEReference(kRenderingEClass, KRENDERING__PARENT);
        createEReference(kRenderingEClass, KRENDERING__PLACEMENT_DATA);
        createEReference(kRenderingEClass, KRENDERING__ACTIONS);

        kEllipseEClass = createEClass(KELLIPSE);

        kRectangleEClass = createEClass(KRECTANGLE);

        kRoundedRectangleEClass = createEClass(KROUNDED_RECTANGLE);
        createEAttribute(kRoundedRectangleEClass, KROUNDED_RECTANGLE__CORNER_WIDTH);
        createEAttribute(kRoundedRectangleEClass, KROUNDED_RECTANGLE__CORNER_HEIGHT);

        kPolylineEClass = createEClass(KPOLYLINE);
        createEReference(kPolylineEClass, KPOLYLINE__POINTS);
        createEReference(kPolylineEClass, KPOLYLINE__JUNCTION_POINT_RENDERING);

        kPolygonEClass = createEClass(KPOLYGON);

        kImageEClass = createEClass(KIMAGE);
        createEAttribute(kImageEClass, KIMAGE__BUNDLE_NAME);
        createEAttribute(kImageEClass, KIMAGE__IMAGE_PATH);
        createEAttribute(kImageEClass, KIMAGE__IMAGE_OBJECT);
        createEReference(kImageEClass, KIMAGE__CLIP_SHAPE);

        kDecoratorPlacementDataEClass = createEClass(KDECORATOR_PLACEMENT_DATA);
        createEAttribute(kDecoratorPlacementDataEClass, KDECORATOR_PLACEMENT_DATA__ABSOLUTE);
        createEAttribute(kDecoratorPlacementDataEClass, KDECORATOR_PLACEMENT_DATA__XOFFSET);
        createEAttribute(kDecoratorPlacementDataEClass, KDECORATOR_PLACEMENT_DATA__YOFFSET);
        createEAttribute(kDecoratorPlacementDataEClass, KDECORATOR_PLACEMENT_DATA__ROTATE_WITH_LINE);
        createEAttribute(kDecoratorPlacementDataEClass, KDECORATOR_PLACEMENT_DATA__WIDTH);
        createEAttribute(kDecoratorPlacementDataEClass, KDECORATOR_PLACEMENT_DATA__HEIGHT);
        createEAttribute(kDecoratorPlacementDataEClass, KDECORATOR_PLACEMENT_DATA__RELATIVE);

        kContainerRenderingEClass = createEClass(KCONTAINER_RENDERING);
        createEReference(kContainerRenderingEClass, KCONTAINER_RENDERING__CHILDREN);
        createEReference(kContainerRenderingEClass, KCONTAINER_RENDERING__CHILD_PLACEMENT);

        kArcEClass = createEClass(KARC);
        createEAttribute(kArcEClass, KARC__START_ANGLE);
        createEAttribute(kArcEClass, KARC__ARC_ANGLE);
        createEAttribute(kArcEClass, KARC__ARC_TYPE);

        kStyleEClass = createEClass(KSTYLE);
        createEAttribute(kStyleEClass, KSTYLE__PROPAGATE_TO_CHILDREN);
        createEAttribute(kStyleEClass, KSTYLE__MODIFIER_ID);
        createEAttribute(kStyleEClass, KSTYLE__SELECTION);

        kRenderingLibraryEClass = createEClass(KRENDERING_LIBRARY);
        createEReference(kRenderingLibraryEClass, KRENDERING_LIBRARY__RENDERINGS);

        kRenderingRefEClass = createEClass(KRENDERING_REF);
        createEReference(kRenderingRefEClass, KRENDERING_REF__RENDERING);

        kChildAreaEClass = createEClass(KCHILD_AREA);

        kTextEClass = createEClass(KTEXT);
        createEAttribute(kTextEClass, KTEXT__TEXT);
        createEAttribute(kTextEClass, KTEXT__CURSOR_SELECTABLE);
        createEAttribute(kTextEClass, KTEXT__EDITABLE);

        kPlacementEClass = createEClass(KPLACEMENT);

        kGridPlacementEClass = createEClass(KGRID_PLACEMENT);
        createEAttribute(kGridPlacementEClass, KGRID_PLACEMENT__NUM_COLUMNS);
        createEReference(kGridPlacementEClass, KGRID_PLACEMENT__TOP_LEFT);
        createEReference(kGridPlacementEClass, KGRID_PLACEMENT__BOTTOM_RIGHT);

        kPlacementDataEClass = createEClass(KPLACEMENT_DATA);

        kGridPlacementDataEClass = createEClass(KGRID_PLACEMENT_DATA);
        createEAttribute(kGridPlacementDataEClass, KGRID_PLACEMENT_DATA__MIN_CELL_WIDTH);
        createEAttribute(kGridPlacementDataEClass, KGRID_PLACEMENT_DATA__MIN_CELL_HEIGHT);
        createEAttribute(kGridPlacementDataEClass, KGRID_PLACEMENT_DATA__FLEXIBLE_WIDTH);
        createEAttribute(kGridPlacementDataEClass, KGRID_PLACEMENT_DATA__FLEXIBLE_HEIGHT);

        kAreaPlacementDataEClass = createEClass(KAREA_PLACEMENT_DATA);
        createEReference(kAreaPlacementDataEClass, KAREA_PLACEMENT_DATA__TOP_LEFT);
        createEReference(kAreaPlacementDataEClass, KAREA_PLACEMENT_DATA__BOTTOM_RIGHT);

        kCustomRenderingEClass = createEClass(KCUSTOM_RENDERING);
        createEAttribute(kCustomRenderingEClass, KCUSTOM_RENDERING__CLASS_NAME);
        createEAttribute(kCustomRenderingEClass, KCUSTOM_RENDERING__BUNDLE_NAME);
        createEAttribute(kCustomRenderingEClass, KCUSTOM_RENDERING__FIGURE_OBJECT);

        kColorEClass = createEClass(KCOLOR);
        createEAttribute(kColorEClass, KCOLOR__RED);
        createEAttribute(kColorEClass, KCOLOR__GREEN);
        createEAttribute(kColorEClass, KCOLOR__BLUE);

        kLineWidthEClass = createEClass(KLINE_WIDTH);
        createEAttribute(kLineWidthEClass, KLINE_WIDTH__LINE_WIDTH);

        kLineStyleEClass = createEClass(KLINE_STYLE);
        createEAttribute(kLineStyleEClass, KLINE_STYLE__LINE_STYLE);
        createEAttribute(kLineStyleEClass, KLINE_STYLE__DASH_PATTERN);
        createEAttribute(kLineStyleEClass, KLINE_STYLE__DASH_OFFSET);

        kVerticalAlignmentEClass = createEClass(KVERTICAL_ALIGNMENT);
        createEAttribute(kVerticalAlignmentEClass, KVERTICAL_ALIGNMENT__VERTICAL_ALIGNMENT);

        kHorizontalAlignmentEClass = createEClass(KHORIZONTAL_ALIGNMENT);
        createEAttribute(kHorizontalAlignmentEClass, KHORIZONTAL_ALIGNMENT__HORIZONTAL_ALIGNMENT);

        kxPositionEClass = createEClass(KX_POSITION);
        createEAttribute(kxPositionEClass, KX_POSITION__ABSOLUTE);
        createEAttribute(kxPositionEClass, KX_POSITION__RELATIVE);

        kyPositionEClass = createEClass(KY_POSITION);
        createEAttribute(kyPositionEClass, KY_POSITION__ABSOLUTE);
        createEAttribute(kyPositionEClass, KY_POSITION__RELATIVE);

        kLeftPositionEClass = createEClass(KLEFT_POSITION);

        kRightPositionEClass = createEClass(KRIGHT_POSITION);

        kTopPositionEClass = createEClass(KTOP_POSITION);

        kBottomPositionEClass = createEClass(KBOTTOM_POSITION);

        kSplineEClass = createEClass(KSPLINE);

        kForegroundEClass = createEClass(KFOREGROUND);

        kColoringEClass = createEClass(KCOLORING);
        createEReference(kColoringEClass, KCOLORING__COLOR);
        createEAttribute(kColoringEClass, KCOLORING__ALPHA);
        createEReference(kColoringEClass, KCOLORING__TARGET_COLOR);
        createEAttribute(kColoringEClass, KCOLORING__TARGET_ALPHA);
        createEAttribute(kColoringEClass, KCOLORING__GRADIENT_ANGLE);

        kBackgroundEClass = createEClass(KBACKGROUND);

        kFontBoldEClass = createEClass(KFONT_BOLD);
        createEAttribute(kFontBoldEClass, KFONT_BOLD__BOLD);

        kFontItalicEClass = createEClass(KFONT_ITALIC);
        createEAttribute(kFontItalicEClass, KFONT_ITALIC__ITALIC);

        kFontNameEClass = createEClass(KFONT_NAME);
        createEAttribute(kFontNameEClass, KFONT_NAME__NAME);

        kFontSizeEClass = createEClass(KFONT_SIZE);
        createEAttribute(kFontSizeEClass, KFONT_SIZE__SIZE);
        createEAttribute(kFontSizeEClass, KFONT_SIZE__SCALE_WITH_ZOOM);

        kRoundedBendsPolylineEClass = createEClass(KROUNDED_BENDS_POLYLINE);
        createEAttribute(kRoundedBendsPolylineEClass, KROUNDED_BENDS_POLYLINE__BEND_RADIUS);

        kRotationEClass = createEClass(KROTATION);
        createEAttribute(kRotationEClass, KROTATION__ROTATION);
        createEReference(kRotationEClass, KROTATION__ROTATION_ANCHOR);

        kLineCapEClass = createEClass(KLINE_CAP);
        createEAttribute(kLineCapEClass, KLINE_CAP__LINE_CAP);

        kActionEClass = createEClass(KACTION);
        createEAttribute(kActionEClass, KACTION__ACTION_ID);
        createEAttribute(kActionEClass, KACTION__TRIGGER);
        createEAttribute(kActionEClass, KACTION__ALT_PRESSED);
        createEAttribute(kActionEClass, KACTION__CTRL_CMD_PRESSED);
        createEAttribute(kActionEClass, KACTION__SHIFT_PRESSED);

        kPointPlacementDataEClass = createEClass(KPOINT_PLACEMENT_DATA);
        createEReference(kPointPlacementDataEClass, KPOINT_PLACEMENT_DATA__REFERENCE_POINT);
        createEAttribute(kPointPlacementDataEClass, KPOINT_PLACEMENT_DATA__HORIZONTAL_ALIGNMENT);
        createEAttribute(kPointPlacementDataEClass, KPOINT_PLACEMENT_DATA__VERTICAL_ALIGNMENT);
        createEAttribute(kPointPlacementDataEClass, KPOINT_PLACEMENT_DATA__HORIZONTAL_MARGIN);
        createEAttribute(kPointPlacementDataEClass, KPOINT_PLACEMENT_DATA__VERTICAL_MARGIN);
        createEAttribute(kPointPlacementDataEClass, KPOINT_PLACEMENT_DATA__MIN_WIDTH);
        createEAttribute(kPointPlacementDataEClass, KPOINT_PLACEMENT_DATA__MIN_HEIGHT);

        kStyleHolderEClass = createEClass(KSTYLE_HOLDER);
        createEReference(kStyleHolderEClass, KSTYLE_HOLDER__STYLES);
        createEAttribute(kStyleHolderEClass, KSTYLE_HOLDER__ID);

        kInvisibilityEClass = createEClass(KINVISIBILITY);
        createEAttribute(kInvisibilityEClass, KINVISIBILITY__INVISIBLE);

        kShadowEClass = createEClass(KSHADOW);
        createEAttribute(kShadowEClass, KSHADOW__XOFFSET);
        createEAttribute(kShadowEClass, KSHADOW__YOFFSET);
        createEAttribute(kShadowEClass, KSHADOW__BLUR);
        createEReference(kShadowEClass, KSHADOW__COLOR);

        kTextUnderlineEClass = createEClass(KTEXT_UNDERLINE);
        createEAttribute(kTextUnderlineEClass, KTEXT_UNDERLINE__UNDERLINE);
        createEReference(kTextUnderlineEClass, KTEXT_UNDERLINE__COLOR);

        kStyleRefEClass = createEClass(KSTYLE_REF);
        createEReference(kStyleRefEClass, KSTYLE_REF__STYLE_HOLDER);
        createEAttribute(kStyleRefEClass, KSTYLE_REF__REFERENCED_TYPES);

        kTextStrikeoutEClass = createEClass(KTEXT_STRIKEOUT);
        createEAttribute(kTextStrikeoutEClass, KTEXT_STRIKEOUT__STRUCK_OUT);
        createEReference(kTextStrikeoutEClass, KTEXT_STRIKEOUT__COLOR);

        kLineJoinEClass = createEClass(KLINE_JOIN);
        createEAttribute(kLineJoinEClass, KLINE_JOIN__LINE_JOIN);
        createEAttribute(kLineJoinEClass, KLINE_JOIN__MITER_LIMIT);

        // Create enums
        lineStyleEEnum = createEEnum(LINE_STYLE);
        horizontalAlignmentEEnum = createEEnum(HORIZONTAL_ALIGNMENT);
        verticalAlignmentEEnum = createEEnum(VERTICAL_ALIGNMENT);
        lineCapEEnum = createEEnum(LINE_CAP);
        modifierStateEEnum = createEEnum(MODIFIER_STATE);
        triggerEEnum = createEEnum(TRIGGER);
        underlineEEnum = createEEnum(UNDERLINE);
        lineJoinEEnum = createEEnum(LINE_JOIN);
        arcEEnum = createEEnum(ARC);

        // Create data types
        colorsEDataType = createEDataType(COLORS);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private boolean isInitialized = false;

    /**
     * Complete the initialization of the package and its meta-model.  This
     * method is guarded to have no affect on any invocation but its first.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void initializePackageContents() {
        if (isInitialized) return;
        isInitialized = true;

        // Initialize package
        setName(eNAME);
        setNsPrefix(eNS_PREFIX);
        setNsURI(eNS_URI);

        // Obtain other dependent packages
        KGraphPackage theKGraphPackage = (KGraphPackage)EPackage.Registry.INSTANCE.getEPackage(KGraphPackage.eNS_URI);

        // Create type parameters
        ETypeParameter kxPositionEClass_T = addETypeParameter(kxPositionEClass, "T");
        ETypeParameter kyPositionEClass_T = addETypeParameter(kyPositionEClass, "T");
        ETypeParameter kColoringEClass_T = addETypeParameter(kColoringEClass, "T");

        // Set bounds for type parameters
        EGenericType g1 = createEGenericType(this.getKXPosition());
        EGenericType g2 = createEGenericType(kxPositionEClass_T);
        g1.getETypeArguments().add(g2);
        kxPositionEClass_T.getEBounds().add(g1);
        g1 = createEGenericType(this.getKYPosition());
        g2 = createEGenericType(kyPositionEClass_T);
        g1.getETypeArguments().add(g2);
        kyPositionEClass_T.getEBounds().add(g1);
        g1 = createEGenericType(this.getKColoring());
        g2 = createEGenericType(kColoringEClass_T);
        g1.getETypeArguments().add(g2);
        kColoringEClass_T.getEBounds().add(g1);

        // Add supertypes to classes
        kRenderingEClass.getESuperTypes().add(theKGraphPackage.getKGraphData());
        kRenderingEClass.getESuperTypes().add(this.getKStyleHolder());
        kEllipseEClass.getESuperTypes().add(this.getKContainerRendering());
        kRectangleEClass.getESuperTypes().add(this.getKContainerRendering());
        kRoundedRectangleEClass.getESuperTypes().add(this.getKContainerRendering());
        kPolylineEClass.getESuperTypes().add(this.getKContainerRendering());
        kPolygonEClass.getESuperTypes().add(this.getKPolyline());
        kImageEClass.getESuperTypes().add(this.getKContainerRendering());
        kDecoratorPlacementDataEClass.getESuperTypes().add(this.getKPlacementData());
        kContainerRenderingEClass.getESuperTypes().add(this.getKRendering());
        kArcEClass.getESuperTypes().add(this.getKContainerRendering());
        kStyleEClass.getESuperTypes().add(theKGraphPackage.getEMapPropertyHolder());
        kRenderingLibraryEClass.getESuperTypes().add(theKGraphPackage.getKGraphData());
        kRenderingRefEClass.getESuperTypes().add(this.getKRendering());
        kChildAreaEClass.getESuperTypes().add(this.getKRendering());
        kTextEClass.getESuperTypes().add(this.getKRendering());
        kGridPlacementEClass.getESuperTypes().add(this.getKPlacement());
        kGridPlacementDataEClass.getESuperTypes().add(this.getKAreaPlacementData());
        kAreaPlacementDataEClass.getESuperTypes().add(this.getKPlacementData());
        kCustomRenderingEClass.getESuperTypes().add(this.getKContainerRendering());
        kLineWidthEClass.getESuperTypes().add(this.getKStyle());
        kLineStyleEClass.getESuperTypes().add(this.getKStyle());
        kVerticalAlignmentEClass.getESuperTypes().add(this.getKStyle());
        kHorizontalAlignmentEClass.getESuperTypes().add(this.getKStyle());
        g1 = createEGenericType(this.getKXPosition());
        g2 = createEGenericType(this.getKLeftPosition());
        g1.getETypeArguments().add(g2);
        kLeftPositionEClass.getEGenericSuperTypes().add(g1);
        g1 = createEGenericType(this.getKXPosition());
        g2 = createEGenericType(this.getKRightPosition());
        g1.getETypeArguments().add(g2);
        kRightPositionEClass.getEGenericSuperTypes().add(g1);
        g1 = createEGenericType(this.getKYPosition());
        g2 = createEGenericType(this.getKTopPosition());
        g1.getETypeArguments().add(g2);
        kTopPositionEClass.getEGenericSuperTypes().add(g1);
        g1 = createEGenericType(this.getKYPosition());
        g2 = createEGenericType(this.getKBottomPosition());
        g1.getETypeArguments().add(g2);
        kBottomPositionEClass.getEGenericSuperTypes().add(g1);
        kSplineEClass.getESuperTypes().add(this.getKPolyline());
        g1 = createEGenericType(this.getKColoring());
        g2 = createEGenericType(this.getKForeground());
        g1.getETypeArguments().add(g2);
        kForegroundEClass.getEGenericSuperTypes().add(g1);
        kColoringEClass.getESuperTypes().add(this.getKStyle());
        g1 = createEGenericType(this.getKColoring());
        g2 = createEGenericType(this.getKBackground());
        g1.getETypeArguments().add(g2);
        kBackgroundEClass.getEGenericSuperTypes().add(g1);
        kFontBoldEClass.getESuperTypes().add(this.getKStyle());
        kFontItalicEClass.getESuperTypes().add(this.getKStyle());
        kFontNameEClass.getESuperTypes().add(this.getKStyle());
        kFontSizeEClass.getESuperTypes().add(this.getKStyle());
        kRoundedBendsPolylineEClass.getESuperTypes().add(this.getKPolyline());
        kRotationEClass.getESuperTypes().add(this.getKStyle());
        kLineCapEClass.getESuperTypes().add(this.getKStyle());
        kPointPlacementDataEClass.getESuperTypes().add(this.getKPlacementData());
        kInvisibilityEClass.getESuperTypes().add(this.getKStyle());
        kShadowEClass.getESuperTypes().add(this.getKStyle());
        kTextUnderlineEClass.getESuperTypes().add(this.getKStyle());
        kStyleRefEClass.getESuperTypes().add(this.getKStyle());
        kTextStrikeoutEClass.getESuperTypes().add(this.getKStyle());
        kLineJoinEClass.getESuperTypes().add(this.getKStyle());

        // Initialize classes and features; add operations and parameters
        initEClass(kPositionEClass, KPosition.class, "KPosition", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        g1 = createEGenericType(this.getKXPosition());
        g2 = createEGenericType();
        g1.getETypeArguments().add(g2);
        initEReference(getKPosition_X(), g1, null, "x", null, 1, 1, KPosition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        g1 = createEGenericType(this.getKYPosition());
        g2 = createEGenericType();
        g1.getETypeArguments().add(g2);
        initEReference(getKPosition_Y(), g1, null, "y", null, 1, 1, KPosition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        EOperation op = addEOperation(kPositionEClass, ecorePackage.getEBoolean(), "equals", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEJavaObject(), "other", 0, 1, IS_UNIQUE, IS_ORDERED);

        op = addEOperation(kPositionEClass, this.getKPosition(), "setPositions", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(this.getKXPosition());
        g2 = createEGenericType();
        g1.getETypeArguments().add(g2);
        addEParameter(op, g1, "x", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(this.getKYPosition());
        g2 = createEGenericType();
        g1.getETypeArguments().add(g2);
        addEParameter(op, g1, "y", 0, 1, IS_UNIQUE, IS_ORDERED);

        initEClass(kRenderingEClass, KRendering.class, "KRendering", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getKRendering_Parent(), this.getKContainerRendering(), this.getKContainerRendering_Children(), "parent", null, 0, 1, KRendering.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getKRendering_PlacementData(), this.getKPlacementData(), null, "placementData", null, 0, 1, KRendering.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getKRendering_Actions(), this.getKAction(), null, "actions", null, 0, -1, KRendering.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kEllipseEClass, KEllipse.class, "KEllipse", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

        initEClass(kRectangleEClass, KRectangle.class, "KRectangle", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

        initEClass(kRoundedRectangleEClass, KRoundedRectangle.class, "KRoundedRectangle", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getKRoundedRectangle_CornerWidth(), ecorePackage.getEFloat(), "cornerWidth", null, 1, 1, KRoundedRectangle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKRoundedRectangle_CornerHeight(), ecorePackage.getEFloat(), "cornerHeight", null, 1, 1, KRoundedRectangle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kPolylineEClass, KPolyline.class, "KPolyline", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getKPolyline_Points(), this.getKPosition(), null, "points", null, 0, -1, KPolyline.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getKPolyline_JunctionPointRendering(), this.getKRendering(), null, "junctionPointRendering", null, 0, 1, KPolyline.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kPolygonEClass, KPolygon.class, "KPolygon", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

        initEClass(kImageEClass, KImage.class, "KImage", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getKImage_BundleName(), ecorePackage.getEString(), "bundleName", null, 0, 1, KImage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKImage_ImagePath(), ecorePackage.getEString(), "imagePath", null, 0, 1, KImage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKImage_ImageObject(), ecorePackage.getEJavaObject(), "imageObject", null, 0, 1, KImage.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getKImage_ClipShape(), this.getKRendering(), null, "clipShape", null, 0, 1, KImage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kDecoratorPlacementDataEClass, KDecoratorPlacementData.class, "KDecoratorPlacementData", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getKDecoratorPlacementData_Absolute(), ecorePackage.getEFloat(), "absolute", null, 1, 1, KDecoratorPlacementData.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKDecoratorPlacementData_XOffset(), ecorePackage.getEFloat(), "xOffset", null, 0, 1, KDecoratorPlacementData.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKDecoratorPlacementData_YOffset(), ecorePackage.getEFloat(), "yOffset", null, 0, 1, KDecoratorPlacementData.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKDecoratorPlacementData_RotateWithLine(), ecorePackage.getEBoolean(), "rotateWithLine", null, 1, 1, KDecoratorPlacementData.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKDecoratorPlacementData_Width(), ecorePackage.getEFloat(), "width", null, 1, 1, KDecoratorPlacementData.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKDecoratorPlacementData_Height(), ecorePackage.getEFloat(), "height", null, 1, 1, KDecoratorPlacementData.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKDecoratorPlacementData_Relative(), ecorePackage.getEFloat(), "relative", null, 0, 1, KDecoratorPlacementData.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kContainerRenderingEClass, KContainerRendering.class, "KContainerRendering", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getKContainerRendering_Children(), this.getKRendering(), this.getKRendering_Parent(), "children", null, 0, -1, KContainerRendering.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getKContainerRendering_ChildPlacement(), this.getKPlacement(), null, "childPlacement", null, 0, 1, KContainerRendering.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kArcEClass, KArc.class, "KArc", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getKArc_StartAngle(), ecorePackage.getEFloat(), "startAngle", null, 0, 1, KArc.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKArc_ArcAngle(), ecorePackage.getEFloat(), "arcAngle", null, 0, 1, KArc.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKArc_ArcType(), this.getArc(), "arcType", null, 0, 1, KArc.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kStyleEClass, KStyle.class, "KStyle", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getKStyle_PropagateToChildren(), ecorePackage.getEBoolean(), "propagateToChildren", null, 1, 1, KStyle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKStyle_ModifierId(), ecorePackage.getEString(), "modifierId", null, 0, 1, KStyle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKStyle_Selection(), ecorePackage.getEBoolean(), "selection", "false", 0, 1, KStyle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kRenderingLibraryEClass, KRenderingLibrary.class, "KRenderingLibrary", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getKRenderingLibrary_Renderings(), this.getKStyleHolder(), null, "renderings", null, 0, -1, KRenderingLibrary.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kRenderingRefEClass, KRenderingRef.class, "KRenderingRef", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getKRenderingRef_Rendering(), this.getKRendering(), null, "rendering", null, 1, 1, KRenderingRef.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kChildAreaEClass, KChildArea.class, "KChildArea", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

        initEClass(kTextEClass, KText.class, "KText", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getKText_Text(), ecorePackage.getEString(), "text", null, 0, 1, KText.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKText_CursorSelectable(), ecorePackage.getEBoolean(), "cursorSelectable", "false", 0, 1, KText.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKText_Editable(), ecorePackage.getEBoolean(), "editable", "false", 0, 1, KText.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kPlacementEClass, KPlacement.class, "KPlacement", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

        initEClass(kGridPlacementEClass, KGridPlacement.class, "KGridPlacement", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getKGridPlacement_NumColumns(), ecorePackage.getEInt(), "numColumns", null, 1, 1, KGridPlacement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getKGridPlacement_TopLeft(), this.getKPosition(), null, "topLeft", null, 0, 1, KGridPlacement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getKGridPlacement_BottomRight(), this.getKPosition(), null, "bottomRight", null, 0, 1, KGridPlacement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kPlacementDataEClass, KPlacementData.class, "KPlacementData", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

        initEClass(kGridPlacementDataEClass, KGridPlacementData.class, "KGridPlacementData", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getKGridPlacementData_MinCellWidth(), ecorePackage.getEFloat(), "minCellWidth", "0", 0, 1, KGridPlacementData.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKGridPlacementData_MinCellHeight(), ecorePackage.getEFloat(), "minCellHeight", "0", 0, 1, KGridPlacementData.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKGridPlacementData_FlexibleWidth(), ecorePackage.getEBooleanObject(), "flexibleWidth", "true", 0, 1, KGridPlacementData.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKGridPlacementData_FlexibleHeight(), ecorePackage.getEBooleanObject(), "flexibleHeight", "true", 0, 1, KGridPlacementData.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kAreaPlacementDataEClass, KAreaPlacementData.class, "KAreaPlacementData", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getKAreaPlacementData_TopLeft(), this.getKPosition(), null, "topLeft", null, 0, 1, KAreaPlacementData.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getKAreaPlacementData_BottomRight(), this.getKPosition(), null, "bottomRight", null, 0, 1, KAreaPlacementData.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kCustomRenderingEClass, KCustomRendering.class, "KCustomRendering", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getKCustomRendering_ClassName(), ecorePackage.getEString(), "className", null, 0, 1, KCustomRendering.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKCustomRendering_BundleName(), ecorePackage.getEString(), "bundleName", null, 0, 1, KCustomRendering.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKCustomRendering_FigureObject(), ecorePackage.getEJavaObject(), "figureObject", null, 0, 1, KCustomRendering.class, IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kColorEClass, KColor.class, "KColor", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getKColor_Red(), ecorePackage.getEInt(), "red", "0", 1, 1, KColor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKColor_Green(), ecorePackage.getEInt(), "green", "0", 1, 1, KColor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKColor_Blue(), ecorePackage.getEInt(), "blue", "0", 1, 1, KColor.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        op = addEOperation(kColorEClass, this.getKColor(), "setColor", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "red", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "green", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "blue", 0, 1, IS_UNIQUE, IS_ORDERED);

        op = addEOperation(kColorEClass, this.getKColor(), "setColor", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getColors(), "color", 0, 1, IS_UNIQUE, IS_ORDERED);

        op = addEOperation(kColorEClass, this.getKColor(), "setColor", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getKColor(), "kColor", 0, 1, IS_UNIQUE, IS_ORDERED);

        op = addEOperation(kColorEClass, ecorePackage.getEBoolean(), "equals", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEJavaObject(), "other", 0, 1, IS_UNIQUE, IS_ORDERED);

        initEClass(kLineWidthEClass, KLineWidth.class, "KLineWidth", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getKLineWidth_LineWidth(), ecorePackage.getEFloat(), "lineWidth", "1.0", 1, 1, KLineWidth.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kLineStyleEClass, KLineStyle.class, "KLineStyle", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getKLineStyle_LineStyle(), this.getLineStyle(), "lineStyle", null, 1, 1, KLineStyle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKLineStyle_DashPattern(), ecorePackage.getEFloat(), "dashPattern", null, 0, -1, KLineStyle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKLineStyle_DashOffset(), ecorePackage.getEFloat(), "dashOffset", null, 0, 1, KLineStyle.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kVerticalAlignmentEClass, KVerticalAlignment.class, "KVerticalAlignment", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getKVerticalAlignment_VerticalAlignment(), this.getVerticalAlignment(), "verticalAlignment", null, 1, 1, KVerticalAlignment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kHorizontalAlignmentEClass, KHorizontalAlignment.class, "KHorizontalAlignment", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getKHorizontalAlignment_HorizontalAlignment(), this.getHorizontalAlignment(), "horizontalAlignment", null, 1, 1, KHorizontalAlignment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kxPositionEClass, KXPosition.class, "KXPosition", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getKXPosition_Absolute(), ecorePackage.getEFloat(), "absolute", null, 0, 1, KXPosition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKXPosition_Relative(), ecorePackage.getEFloat(), "relative", null, 0, 1, KXPosition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        op = addEOperation(kxPositionEClass, ecorePackage.getEBoolean(), "equals", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEJavaObject(), "other", 0, 1, IS_UNIQUE, IS_ORDERED);

        op = addEOperation(kxPositionEClass, null, "setPosition", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEFloat(), "absolute", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEFloat(), "relative", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kxPositionEClass_T);
        initEOperation(op, g1);

        initEClass(kyPositionEClass, KYPosition.class, "KYPosition", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getKYPosition_Absolute(), ecorePackage.getEFloat(), "absolute", null, 0, 1, KYPosition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKYPosition_Relative(), ecorePackage.getEFloat(), "relative", null, 0, 1, KYPosition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        op = addEOperation(kyPositionEClass, ecorePackage.getEBoolean(), "equals", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEJavaObject(), "other", 0, 1, IS_UNIQUE, IS_ORDERED);

        op = addEOperation(kyPositionEClass, null, "setPosition", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEFloat(), "absolute", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEFloat(), "relative", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kyPositionEClass_T);
        initEOperation(op, g1);

        initEClass(kLeftPositionEClass, KLeftPosition.class, "KLeftPosition", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

        initEClass(kRightPositionEClass, KRightPosition.class, "KRightPosition", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

        initEClass(kTopPositionEClass, KTopPosition.class, "KTopPosition", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

        initEClass(kBottomPositionEClass, KBottomPosition.class, "KBottomPosition", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

        initEClass(kSplineEClass, KSpline.class, "KSpline", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

        initEClass(kForegroundEClass, KForeground.class, "KForeground", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

        initEClass(kColoringEClass, KColoring.class, "KColoring", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getKColoring_Color(), this.getKColor(), null, "color", null, 0, 1, KColoring.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKColoring_Alpha(), ecorePackage.getEInt(), "alpha", "255", 0, 1, KColoring.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getKColoring_TargetColor(), this.getKColor(), null, "targetColor", null, 0, 1, KColoring.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKColoring_TargetAlpha(), ecorePackage.getEInt(), "targetAlpha", "255", 0, 1, KColoring.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKColoring_GradientAngle(), ecorePackage.getEFloat(), "gradientAngle", "0", 0, 1, KColoring.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        op = addEOperation(kColoringEClass, null, "setColor", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "red", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "green", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "blue", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setColor", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getColors(), "color", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setColor", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "red", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "green", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "blue", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "alpha", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setColor", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getColors(), "color", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "alpha", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setColor2", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getKColor(), "color", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setColor2", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getKColor(), "color", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "alpha", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setColorCopyOf", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getKColor(), "kColor", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setColorCopyOf", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getKColor(), "kColor", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "alpha", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setColorCopiedFrom", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(this.getKColoring());
        g2 = createEGenericType();
        g1.getETypeArguments().add(g2);
        addEParameter(op, g1, "coloring", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setColorAndAlphaCopiedFrom", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(this.getKColoring());
        g2 = createEGenericType();
        g1.getETypeArguments().add(g2);
        addEParameter(op, g1, "coloring", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setTargetColor", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "red", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "green", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "blue", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setTargetColor", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getColors(), "color", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setTargetColor", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "red", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "green", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "blue", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "alpha", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setTargetColor", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getColors(), "color", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "alpha", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setTargetColor2", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getKColor(), "targetColor", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setTargetColor2", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getKColor(), "targetColor", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "targetAlpha", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setTargetColorCopyOf", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getKColor(), "targetColor", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setTargetColorCopyOf", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getKColor(), "targetColor", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "targetAlpha", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setTargetColorCopiedFrom", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(this.getKColoring());
        g2 = createEGenericType();
        g1.getETypeArguments().add(g2);
        addEParameter(op, g1, "coloring", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setTargetColorAndAlphaCopiedFrom", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(this.getKColoring());
        g2 = createEGenericType();
        g1.getETypeArguments().add(g2);
        addEParameter(op, g1, "coloring", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setGradientAngle2", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEFloat(), "angle", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setColors", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getKColor(), "color", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getKColor(), "targetColor", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setColors", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getKColor(), "color", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getKColor(), "targetColor", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEFloat(), "angle", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setColors", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getKColor(), "color", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "alpha", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getKColor(), "targetColor", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "targetAlpha", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setColors", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getKColor(), "color", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "alpha", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getKColor(), "targetColor", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "targetAlpha", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEFloat(), "angle", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setColors", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getColors(), "color", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getColors(), "targetColor", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setColors", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getColors(), "color", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getColors(), "targetColor", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEFloat(), "angle", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setColors", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getColors(), "color", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "alpha", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getColors(), "targetColor", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "targetAlpha", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setColors", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getColors(), "color", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "alpha", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getColors(), "targetColor", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "targetAlpha", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEFloat(), "angle", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setColorsCopiesOf", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getKColor(), "color", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getKColor(), "targetColor", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setColorsCopiesOf", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getKColor(), "color", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getKColor(), "targetColor", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEFloat(), "angle", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setColorsCopiesOf", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getKColor(), "color", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "alpha", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getKColor(), "targetColor", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "targetAlpha", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setColorsCopiesOf", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getKColor(), "color", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "alpha", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, this.getKColor(), "targetColor", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEInt(), "targetAlpha", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEFloat(), "angle", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setColorsCopiedFrom", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(this.getKColoring());
        g2 = createEGenericType();
        g1.getETypeArguments().add(g2);
        addEParameter(op, g1, "coloring", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, null, "setColorsAlphasGradientAngleCopiedFrom", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(this.getKColoring());
        g2 = createEGenericType();
        g1.getETypeArguments().add(g2);
        addEParameter(op, g1, "coloring", 0, 1, IS_UNIQUE, IS_ORDERED);
        g1 = createEGenericType(kColoringEClass_T);
        initEOperation(op, g1);

        op = addEOperation(kColoringEClass, ecorePackage.getEBoolean(), "equals", 0, 1, IS_UNIQUE, IS_ORDERED);
        addEParameter(op, ecorePackage.getEJavaObject(), "other", 0, 1, IS_UNIQUE, IS_ORDERED);

        initEClass(kBackgroundEClass, KBackground.class, "KBackground", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

        initEClass(kFontBoldEClass, KFontBold.class, "KFontBold", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getKFontBold_Bold(), ecorePackage.getEBoolean(), "bold", "true", 1, 1, KFontBold.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kFontItalicEClass, KFontItalic.class, "KFontItalic", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getKFontItalic_Italic(), ecorePackage.getEBoolean(), "italic", "true", 1, 1, KFontItalic.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kFontNameEClass, KFontName.class, "KFontName", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getKFontName_Name(), ecorePackage.getEString(), "name", null, 1, 1, KFontName.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kFontSizeEClass, KFontSize.class, "KFontSize", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getKFontSize_Size(), ecorePackage.getEInt(), "size", "10", 1, 1, KFontSize.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKFontSize_ScaleWithZoom(), ecorePackage.getEBoolean(), "scaleWithZoom", "false", 0, 1, KFontSize.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kRoundedBendsPolylineEClass, KRoundedBendsPolyline.class, "KRoundedBendsPolyline", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getKRoundedBendsPolyline_BendRadius(), ecorePackage.getEFloat(), "bendRadius", null, 0, 1, KRoundedBendsPolyline.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kRotationEClass, KRotation.class, "KRotation", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getKRotation_Rotation(), ecorePackage.getEFloat(), "rotation", "0.0", 1, 1, KRotation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getKRotation_RotationAnchor(), this.getKPosition(), null, "rotationAnchor", null, 0, 1, KRotation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kLineCapEClass, KLineCap.class, "KLineCap", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getKLineCap_LineCap(), this.getLineCap(), "lineCap", null, 1, 1, KLineCap.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kActionEClass, KAction.class, "KAction", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getKAction_ActionId(), ecorePackage.getEString(), "actionId", null, 1, 1, KAction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKAction_Trigger(), this.getTrigger(), "trigger", null, 1, 1, KAction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKAction_AltPressed(), this.getModifierState(), "altPressed", null, 0, 1, KAction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKAction_CtrlCmdPressed(), this.getModifierState(), "ctrlCmdPressed", null, 0, 1, KAction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKAction_ShiftPressed(), this.getModifierState(), "shiftPressed", null, 0, 1, KAction.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kPointPlacementDataEClass, KPointPlacementData.class, "KPointPlacementData", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getKPointPlacementData_ReferencePoint(), this.getKPosition(), null, "referencePoint", null, 0, 1, KPointPlacementData.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKPointPlacementData_HorizontalAlignment(), this.getHorizontalAlignment(), "horizontalAlignment", "LEFT", 0, 1, KPointPlacementData.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKPointPlacementData_VerticalAlignment(), this.getVerticalAlignment(), "verticalAlignment", "TOP", 0, 1, KPointPlacementData.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKPointPlacementData_HorizontalMargin(), ecorePackage.getEFloat(), "horizontalMargin", null, 0, 1, KPointPlacementData.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKPointPlacementData_VerticalMargin(), ecorePackage.getEFloat(), "verticalMargin", null, 0, 1, KPointPlacementData.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKPointPlacementData_MinWidth(), ecorePackage.getEFloat(), "minWidth", null, 0, 1, KPointPlacementData.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKPointPlacementData_MinHeight(), ecorePackage.getEFloat(), "minHeight", null, 0, 1, KPointPlacementData.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kStyleHolderEClass, KStyleHolder.class, "KStyleHolder", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getKStyleHolder_Styles(), this.getKStyle(), null, "styles", null, 0, -1, KStyleHolder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKStyleHolder_Id(), ecorePackage.getEString(), "id", null, 0, 1, KStyleHolder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kInvisibilityEClass, KInvisibility.class, "KInvisibility", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getKInvisibility_Invisible(), ecorePackage.getEBoolean(), "invisible", "true", 1, 1, KInvisibility.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kShadowEClass, KShadow.class, "KShadow", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getKShadow_XOffset(), ecorePackage.getEFloat(), "xOffset", "3", 0, 1, KShadow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKShadow_YOffset(), ecorePackage.getEFloat(), "yOffset", "3", 0, 1, KShadow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKShadow_Blur(), ecorePackage.getEFloat(), "blur", "0.0", 0, 1, KShadow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getKShadow_Color(), this.getKColor(), null, "color", null, 0, 1, KShadow.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kTextUnderlineEClass, KTextUnderline.class, "KTextUnderline", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getKTextUnderline_Underline(), this.getUnderline(), "underline", null, 1, 1, KTextUnderline.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getKTextUnderline_Color(), this.getKColor(), null, "color", null, 0, 1, KTextUnderline.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kStyleRefEClass, KStyleRef.class, "KStyleRef", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getKStyleRef_StyleHolder(), this.getKStyleHolder(), null, "styleHolder", null, 1, 1, KStyleRef.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        g1 = createEGenericType(ecorePackage.getEJavaClass());
        g2 = createEGenericType(this.getKStyle());
        g1.getETypeArguments().add(g2);
        initEAttribute(getKStyleRef_ReferencedTypes(), g1, "referencedTypes", null, 0, -1, KStyleRef.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kTextStrikeoutEClass, KTextStrikeout.class, "KTextStrikeout", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getKTextStrikeout_StruckOut(), ecorePackage.getEBooleanObject(), "struckOut", "true", 1, 1, KTextStrikeout.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getKTextStrikeout_Color(), this.getKColor(), null, "color", null, 0, 1, KTextStrikeout.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(kLineJoinEClass, KLineJoin.class, "KLineJoin", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getKLineJoin_LineJoin(), this.getLineJoin(), "lineJoin", null, 1, 1, KLineJoin.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getKLineJoin_MiterLimit(), ecorePackage.getEFloat(), "miterLimit", "10", 0, 1, KLineJoin.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        // Initialize enums and add enum literals
        initEEnum(lineStyleEEnum, LineStyle.class, "LineStyle");
        addEEnumLiteral(lineStyleEEnum, LineStyle.SOLID);
        addEEnumLiteral(lineStyleEEnum, LineStyle.DASH);
        addEEnumLiteral(lineStyleEEnum, LineStyle.DOT);
        addEEnumLiteral(lineStyleEEnum, LineStyle.DASHDOT);
        addEEnumLiteral(lineStyleEEnum, LineStyle.DASHDOTDOT);
        addEEnumLiteral(lineStyleEEnum, LineStyle.CUSTOM);

        initEEnum(horizontalAlignmentEEnum, HorizontalAlignment.class, "HorizontalAlignment");
        addEEnumLiteral(horizontalAlignmentEEnum, HorizontalAlignment.LEFT);
        addEEnumLiteral(horizontalAlignmentEEnum, HorizontalAlignment.CENTER);
        addEEnumLiteral(horizontalAlignmentEEnum, HorizontalAlignment.RIGHT);

        initEEnum(verticalAlignmentEEnum, VerticalAlignment.class, "VerticalAlignment");
        addEEnumLiteral(verticalAlignmentEEnum, VerticalAlignment.TOP);
        addEEnumLiteral(verticalAlignmentEEnum, VerticalAlignment.CENTER);
        addEEnumLiteral(verticalAlignmentEEnum, VerticalAlignment.BOTTOM);

        initEEnum(lineCapEEnum, LineCap.class, "LineCap");
        addEEnumLiteral(lineCapEEnum, LineCap.CAP_FLAT);
        addEEnumLiteral(lineCapEEnum, LineCap.CAP_ROUND);
        addEEnumLiteral(lineCapEEnum, LineCap.CAP_SQUARE);

        initEEnum(modifierStateEEnum, ModifierState.class, "ModifierState");
        addEEnumLiteral(modifierStateEEnum, ModifierState.DONT_CARE);
        addEEnumLiteral(modifierStateEEnum, ModifierState.PRESSED);
        addEEnumLiteral(modifierStateEEnum, ModifierState.NOT_PRESSED);

        initEEnum(triggerEEnum, Trigger.class, "Trigger");
        addEEnumLiteral(triggerEEnum, Trigger.SINGLECLICK);
        addEEnumLiteral(triggerEEnum, Trigger.DOUBLECLICK);
        addEEnumLiteral(triggerEEnum, Trigger.SINGLE_OR_MULTICLICK);
        addEEnumLiteral(triggerEEnum, Trigger.MIDDLE_SINGLECLICK);
        addEEnumLiteral(triggerEEnum, Trigger.MIDDLE_DOUBLECLICK);
        addEEnumLiteral(triggerEEnum, Trigger.MIDDLE_SINGLE_OR_MULTICLICK);

        initEEnum(underlineEEnum, Underline.class, "Underline");
        addEEnumLiteral(underlineEEnum, Underline.NONE);
        addEEnumLiteral(underlineEEnum, Underline.SINGLE);
        addEEnumLiteral(underlineEEnum, Underline.DOUBLE);
        addEEnumLiteral(underlineEEnum, Underline.ERROR);
        addEEnumLiteral(underlineEEnum, Underline.SQUIGGLE);
        addEEnumLiteral(underlineEEnum, Underline.LINK);

        initEEnum(lineJoinEEnum, LineJoin.class, "LineJoin");
        addEEnumLiteral(lineJoinEEnum, LineJoin.JOIN_MITER);
        addEEnumLiteral(lineJoinEEnum, LineJoin.JOIN_ROUND);
        addEEnumLiteral(lineJoinEEnum, LineJoin.JOIN_BEVEL);

        initEEnum(arcEEnum, Arc.class, "Arc");
        addEEnumLiteral(arcEEnum, Arc.OPEN);
        addEEnumLiteral(arcEEnum, Arc.CHORD);
        addEEnumLiteral(arcEEnum, Arc.PIE);

        // Initialize data types
        initEDataType(colorsEDataType, Colors.class, "Colors", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);

        // Create resource
        createResource(eNS_URI);
    }

} //KRenderingPackageImpl
