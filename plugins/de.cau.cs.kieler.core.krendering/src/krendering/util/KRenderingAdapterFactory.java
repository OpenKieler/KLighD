/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package krendering.util;

import de.cau.cs.kieler.core.kgraph.EMapPropertyHolder;
import de.cau.cs.kieler.core.kgraph.KGraphData;

import de.cau.cs.kieler.core.properties.IPropertyHolder;

import krendering.*;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see krendering.KRenderingPackage
 * @generated
 */
public class KRenderingAdapterFactory extends AdapterFactoryImpl {
    /**
     * The cached model package.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected static KRenderingPackage modelPackage;

    /**
     * Creates an instance of the adapter factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public KRenderingAdapterFactory() {
        if (modelPackage == null) {
            modelPackage = KRenderingPackage.eINSTANCE;
        }
    }

    /**
     * Returns whether this factory is applicable for the type of the object.
     * <!-- begin-user-doc -->
     * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
     * <!-- end-user-doc -->
     * @return whether this factory is applicable for the type of the object.
     * @generated
     */
    @Override
    public boolean isFactoryForType(Object object) {
        if (object == modelPackage) {
            return true;
        }
        if (object instanceof EObject) {
            return ((EObject)object).eClass().getEPackage() == modelPackage;
        }
        return false;
    }

    /**
     * The switch that delegates to the <code>createXXX</code> methods.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected KRenderingSwitch<Adapter> modelSwitch =
        new KRenderingSwitch<Adapter>() {
            @Override
            public Adapter caseKPosition(KPosition object) {
                return createKPositionAdapter();
            }
            @Override
            public Adapter caseKRendering(KRendering object) {
                return createKRenderingAdapter();
            }
            @Override
            public Adapter caseKEllipse(KEllipse object) {
                return createKEllipseAdapter();
            }
            @Override
            public Adapter caseKRectangle(KRectangle object) {
                return createKRectangleAdapter();
            }
            @Override
            public Adapter caseKRoundedRectangle(KRoundedRectangle object) {
                return createKRoundedRectangleAdapter();
            }
            @Override
            public Adapter caseKPolyline(KPolyline object) {
                return createKPolylineAdapter();
            }
            @Override
            public Adapter caseKPolygon(KPolygon object) {
                return createKPolygonAdapter();
            }
            @Override
            public Adapter caseKImage(KImage object) {
                return createKImageAdapter();
            }
            @Override
            public Adapter caseKDecoratorPlacementData(KDecoratorPlacementData object) {
                return createKDecoratorPlacementDataAdapter();
            }
            @Override
            public Adapter caseKContainerRendering(KContainerRendering object) {
                return createKContainerRenderingAdapter();
            }
            @Override
            public Adapter caseKArc(KArc object) {
                return createKArcAdapter();
            }
            @Override
            public Adapter caseKStyle(KStyle object) {
                return createKStyleAdapter();
            }
            @Override
            public Adapter caseKRenderingLibrary(KRenderingLibrary object) {
                return createKRenderingLibraryAdapter();
            }
            @Override
            public Adapter caseKRenderingRef(KRenderingRef object) {
                return createKRenderingRefAdapter();
            }
            @Override
            public Adapter caseKChildArea(KChildArea object) {
                return createKChildAreaAdapter();
            }
            @Override
            public Adapter caseKText(KText object) {
                return createKTextAdapter();
            }
            @Override
            public Adapter caseKPlacement(KPlacement object) {
                return createKPlacementAdapter();
            }
            @Override
            public Adapter caseKGridPlacement(KGridPlacement object) {
                return createKGridPlacementAdapter();
            }
            @Override
            public Adapter caseKStackPlacement(KStackPlacement object) {
                return createKStackPlacementAdapter();
            }
            @Override
            public Adapter caseKPlacementData(KPlacementData object) {
                return createKPlacementDataAdapter();
            }
            @Override
            public Adapter caseKGridPlacementData(KGridPlacementData object) {
                return createKGridPlacementDataAdapter();
            }
            @Override
            public Adapter caseKStackPlacementData(KStackPlacementData object) {
                return createKStackPlacementDataAdapter();
            }
            @Override
            public Adapter caseKDirectPlacementData(KDirectPlacementData object) {
                return createKDirectPlacementDataAdapter();
            }
            @Override
            public Adapter caseKPolylinePlacementData(KPolylinePlacementData object) {
                return createKPolylinePlacementDataAdapter();
            }
            @Override
            public Adapter caseKCustomRendering(KCustomRendering object) {
                return createKCustomRenderingAdapter();
            }
            @Override
            public Adapter caseKForegroundColor(KForegroundColor object) {
                return createKForegroundColorAdapter();
            }
            @Override
            public Adapter caseKColor(KColor object) {
                return createKColorAdapter();
            }
            @Override
            public Adapter caseKBackgroundColor(KBackgroundColor object) {
                return createKBackgroundColorAdapter();
            }
            @Override
            public Adapter caseKLineWidth(KLineWidth object) {
                return createKLineWidthAdapter();
            }
            @Override
            public Adapter caseKVisibility(KVisibility object) {
                return createKVisibilityAdapter();
            }
            @Override
            public Adapter caseKLineStyle(KLineStyle object) {
                return createKLineStyleAdapter();
            }
            @Override
            public Adapter caseKVerticalAlignment(KVerticalAlignment object) {
                return createKVerticalAlignmentAdapter();
            }
            @Override
            public Adapter caseKHorizontalAlignment(KHorizontalAlignment object) {
                return createKHorizontalAlignmentAdapter();
            }
            @Override
            public Adapter caseKXPosition(KXPosition object) {
                return createKXPositionAdapter();
            }
            @Override
            public Adapter caseKYPosition(KYPosition object) {
                return createKYPositionAdapter();
            }
            @Override
            public Adapter caseKLeftPosition(KLeftPosition object) {
                return createKLeftPositionAdapter();
            }
            @Override
            public Adapter caseKRightPosition(KRightPosition object) {
                return createKRightPositionAdapter();
            }
            @Override
            public Adapter caseKTopPosition(KTopPosition object) {
                return createKTopPositionAdapter();
            }
            @Override
            public Adapter caseKBottomPosition(KBottomPosition object) {
                return createKBottomPositionAdapter();
            }
            @Override
            public Adapter caseIPropertyHolder(IPropertyHolder object) {
                return createIPropertyHolderAdapter();
            }
            @Override
            public Adapter caseEMapPropertyHolder(EMapPropertyHolder object) {
                return createEMapPropertyHolderAdapter();
            }
            @Override
            public Adapter caseKGraphData(KGraphData object) {
                return createKGraphDataAdapter();
            }
            @Override
            public Adapter defaultCase(EObject object) {
                return createEObjectAdapter();
            }
        };

    /**
     * Creates an adapter for the <code>target</code>.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param target the object to adapt.
     * @return the adapter for the <code>target</code>.
     * @generated
     */
    @Override
    public Adapter createAdapter(Notifier target) {
        return modelSwitch.doSwitch((EObject)target);
    }


    /**
     * Creates a new adapter for an object of class '{@link krendering.KPosition <em>KPosition</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KPosition
     * @generated
     */
    public Adapter createKPositionAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KRendering <em>KRendering</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KRendering
     * @generated
     */
    public Adapter createKRenderingAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KEllipse <em>KEllipse</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KEllipse
     * @generated
     */
    public Adapter createKEllipseAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KRectangle <em>KRectangle</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KRectangle
     * @generated
     */
    public Adapter createKRectangleAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KRoundedRectangle <em>KRounded Rectangle</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KRoundedRectangle
     * @generated
     */
    public Adapter createKRoundedRectangleAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KPolyline <em>KPolyline</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KPolyline
     * @generated
     */
    public Adapter createKPolylineAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KPolygon <em>KPolygon</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KPolygon
     * @generated
     */
    public Adapter createKPolygonAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KImage <em>KImage</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KImage
     * @generated
     */
    public Adapter createKImageAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KDecoratorPlacementData <em>KDecorator Placement Data</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KDecoratorPlacementData
     * @generated
     */
    public Adapter createKDecoratorPlacementDataAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KContainerRendering <em>KContainer Rendering</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KContainerRendering
     * @generated
     */
    public Adapter createKContainerRenderingAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KArc <em>KArc</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KArc
     * @generated
     */
    public Adapter createKArcAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KStyle <em>KStyle</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KStyle
     * @generated
     */
    public Adapter createKStyleAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KRenderingLibrary <em>Library</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KRenderingLibrary
     * @generated
     */
    public Adapter createKRenderingLibraryAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KRenderingRef <em>Ref</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KRenderingRef
     * @generated
     */
    public Adapter createKRenderingRefAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KChildArea <em>KChild Area</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KChildArea
     * @generated
     */
    public Adapter createKChildAreaAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KText <em>KText</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KText
     * @generated
     */
    public Adapter createKTextAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KPlacement <em>KPlacement</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KPlacement
     * @generated
     */
    public Adapter createKPlacementAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KGridPlacement <em>KGrid Placement</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KGridPlacement
     * @generated
     */
    public Adapter createKGridPlacementAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KStackPlacement <em>KStack Placement</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KStackPlacement
     * @generated
     */
    public Adapter createKStackPlacementAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KPlacementData <em>KPlacement Data</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KPlacementData
     * @generated
     */
    public Adapter createKPlacementDataAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KGridPlacementData <em>KGrid Placement Data</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KGridPlacementData
     * @generated
     */
    public Adapter createKGridPlacementDataAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KStackPlacementData <em>KStack Placement Data</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KStackPlacementData
     * @generated
     */
    public Adapter createKStackPlacementDataAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KDirectPlacementData <em>KDirect Placement Data</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KDirectPlacementData
     * @generated
     */
    public Adapter createKDirectPlacementDataAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KPolylinePlacementData <em>KPolyline Placement Data</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KPolylinePlacementData
     * @generated
     */
    public Adapter createKPolylinePlacementDataAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KCustomRendering <em>KCustom Rendering</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KCustomRendering
     * @generated
     */
    public Adapter createKCustomRenderingAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KForegroundColor <em>KForeground Color</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KForegroundColor
     * @generated
     */
    public Adapter createKForegroundColorAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KColor <em>KColor</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KColor
     * @generated
     */
    public Adapter createKColorAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KBackgroundColor <em>KBackground Color</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KBackgroundColor
     * @generated
     */
    public Adapter createKBackgroundColorAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KLineWidth <em>KLine Width</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KLineWidth
     * @generated
     */
    public Adapter createKLineWidthAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KVisibility <em>KVisibility</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KVisibility
     * @generated
     */
    public Adapter createKVisibilityAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KLineStyle <em>KLine Style</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KLineStyle
     * @generated
     */
    public Adapter createKLineStyleAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KVerticalAlignment <em>KVertical Alignment</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KVerticalAlignment
     * @generated
     */
    public Adapter createKVerticalAlignmentAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KHorizontalAlignment <em>KHorizontal Alignment</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KHorizontalAlignment
     * @generated
     */
    public Adapter createKHorizontalAlignmentAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KXPosition <em>KX Position</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KXPosition
     * @generated
     */
    public Adapter createKXPositionAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KYPosition <em>KY Position</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KYPosition
     * @generated
     */
    public Adapter createKYPositionAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KLeftPosition <em>KLeft Position</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KLeftPosition
     * @generated
     */
    public Adapter createKLeftPositionAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KRightPosition <em>KRight Position</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KRightPosition
     * @generated
     */
    public Adapter createKRightPositionAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KTopPosition <em>KTop Position</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KTopPosition
     * @generated
     */
    public Adapter createKTopPositionAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link krendering.KBottomPosition <em>KBottom Position</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see krendering.KBottomPosition
     * @generated
     */
    public Adapter createKBottomPositionAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link de.cau.cs.kieler.core.properties.IPropertyHolder <em>IProperty Holder</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see de.cau.cs.kieler.core.properties.IPropertyHolder
     * @generated
     */
    public Adapter createIPropertyHolderAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link de.cau.cs.kieler.core.kgraph.EMapPropertyHolder <em>EMap Property Holder</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see de.cau.cs.kieler.core.kgraph.EMapPropertyHolder
     * @generated
     */
    public Adapter createEMapPropertyHolderAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '{@link de.cau.cs.kieler.core.kgraph.KGraphData <em>Data</em>}'.
     * <!-- begin-user-doc -->
     * This default implementation returns null so that we can easily ignore cases;
     * it's useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @see de.cau.cs.kieler.core.kgraph.KGraphData
     * @generated
     */
    public Adapter createKGraphDataAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for the default case.
     * <!-- begin-user-doc -->
     * This default implementation returns null.
     * <!-- end-user-doc -->
     * @return the new adapter.
     * @generated
     */
    public Adapter createEObjectAdapter() {
        return null;
    }

} //KRenderingAdapterFactory
