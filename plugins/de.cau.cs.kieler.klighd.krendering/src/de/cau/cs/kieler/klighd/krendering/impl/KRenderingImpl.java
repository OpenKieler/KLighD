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

import de.cau.cs.kieler.klighd.kgraph.EMapPropertyHolder;
import de.cau.cs.kieler.klighd.kgraph.KGraphData;
import de.cau.cs.kieler.klighd.kgraph.KGraphPackage;
import de.cau.cs.kieler.klighd.kgraph.PersistentEntry;

import de.cau.cs.kieler.klighd.kgraph.impl.IPropertyToObjectMapImpl;

import de.cau.cs.kieler.klighd.kgraph.impl.KGraphDataImpl;
import de.cau.cs.kieler.klighd.krendering.KAction;
import de.cau.cs.kieler.klighd.krendering.KContainerRendering;
import de.cau.cs.kieler.klighd.krendering.KPlacementData;
import de.cau.cs.kieler.klighd.krendering.KRendering;
import de.cau.cs.kieler.klighd.krendering.KRenderingPackage;

import de.cau.cs.kieler.klighd.krendering.KStyle;
import de.cau.cs.kieler.klighd.krendering.KStyleHolder;
import java.util.Collection;
import java.util.Map;

import org.eclipse.elk.graph.properties.IProperty;
import org.eclipse.elk.graph.properties.IPropertyHolder;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EcoreEMap;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>KRendering</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link de.cau.cs.kieler.klighd.krendering.impl.KRenderingImpl#getStyles <em>Styles</em>}</li>
 *   <li>{@link de.cau.cs.kieler.klighd.krendering.impl.KRenderingImpl#getId <em>Id</em>}</li>
 *   <li>{@link de.cau.cs.kieler.klighd.krendering.impl.KRenderingImpl#getParent <em>Parent</em>}</li>
 *   <li>{@link de.cau.cs.kieler.klighd.krendering.impl.KRenderingImpl#getPlacementData <em>Placement Data</em>}</li>
 *   <li>{@link de.cau.cs.kieler.klighd.krendering.impl.KRenderingImpl#getActions <em>Actions</em>}</li>
 * </ul>
 *
 * @generated
 */
public abstract class KRenderingImpl extends KGraphDataImpl implements KRendering {
    /**
     * The cached value of the '{@link #getStyles() <em>Styles</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getStyles()
     * @generated
     * @ordered
     */
    protected EList<KStyle> styles;

    /**
     * The default value of the '{@link #getId() <em>Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getId()
     * @generated
     * @ordered
     */
    protected static final String ID_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getId()
     * @generated
     * @ordered
     */
    protected String id = ID_EDEFAULT;

    /**
     * The cached value of the '{@link #getPlacementData() <em>Placement Data</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getPlacementData()
     * @generated
     * @ordered
     */
    protected KPlacementData placementData;

    /**
     * The cached value of the '{@link #getActions() <em>Actions</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getActions()
     * @generated
     * @ordered
     */
    protected EList<KAction> actions;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected KRenderingImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return KRenderingPackage.Literals.KRENDERING;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<KStyle> getStyles() {
        if (styles == null) {
            styles = new EObjectContainmentEList<KStyle>(KStyle.class, this, KRenderingPackage.KRENDERING__STYLES);
        }
        return styles;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String getId() {
        return id;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setId(String newId) {
        String oldId = id;
        id = newId;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, KRenderingPackage.KRENDERING__ID, oldId, id));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public KContainerRendering getParent() {
        if (eContainerFeatureID() != KRenderingPackage.KRENDERING__PARENT) return null;
        return (KContainerRendering)eInternalContainer();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetParent(KContainerRendering newParent, NotificationChain msgs) {
        msgs = eBasicSetContainer((InternalEObject)newParent, KRenderingPackage.KRENDERING__PARENT, msgs);
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setParent(KContainerRendering newParent) {
        if (newParent != eInternalContainer() || (eContainerFeatureID() != KRenderingPackage.KRENDERING__PARENT && newParent != null)) {
            if (EcoreUtil.isAncestor(this, newParent))
                throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
            NotificationChain msgs = null;
            if (eInternalContainer() != null)
                msgs = eBasicRemoveFromContainer(msgs);
            if (newParent != null)
                msgs = ((InternalEObject)newParent).eInverseAdd(this, KRenderingPackage.KCONTAINER_RENDERING__CHILDREN, KContainerRendering.class, msgs);
            msgs = basicSetParent(newParent, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, KRenderingPackage.KRENDERING__PARENT, newParent, newParent));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public KPlacementData getPlacementData() {
        return placementData;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetPlacementData(KPlacementData newPlacementData, NotificationChain msgs) {
        KPlacementData oldPlacementData = placementData;
        placementData = newPlacementData;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, KRenderingPackage.KRENDERING__PLACEMENT_DATA, oldPlacementData, newPlacementData);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setPlacementData(KPlacementData newPlacementData) {
        if (newPlacementData != placementData) {
            NotificationChain msgs = null;
            if (placementData != null)
                msgs = ((InternalEObject)placementData).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - KRenderingPackage.KRENDERING__PLACEMENT_DATA, null, msgs);
            if (newPlacementData != null)
                msgs = ((InternalEObject)newPlacementData).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - KRenderingPackage.KRENDERING__PLACEMENT_DATA, null, msgs);
            msgs = basicSetPlacementData(newPlacementData, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, KRenderingPackage.KRENDERING__PLACEMENT_DATA, newPlacementData, newPlacementData));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EList<KAction> getActions() {
        if (actions == null) {
            actions = new EObjectContainmentEList<KAction>(KAction.class, this, KRenderingPackage.KRENDERING__ACTIONS);
        }
        return actions;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case KRenderingPackage.KRENDERING__PARENT:
                if (eInternalContainer() != null)
                    msgs = eBasicRemoveFromContainer(msgs);
                return basicSetParent((KContainerRendering)otherEnd, msgs);
        }
        return super.eInverseAdd(otherEnd, featureID, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case KRenderingPackage.KRENDERING__STYLES:
                return ((InternalEList<?>)getStyles()).basicRemove(otherEnd, msgs);
            case KRenderingPackage.KRENDERING__PARENT:
                return basicSetParent(null, msgs);
            case KRenderingPackage.KRENDERING__PLACEMENT_DATA:
                return basicSetPlacementData(null, msgs);
            case KRenderingPackage.KRENDERING__ACTIONS:
                return ((InternalEList<?>)getActions()).basicRemove(otherEnd, msgs);
        }
        return super.eInverseRemove(otherEnd, featureID, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
        switch (eContainerFeatureID()) {
            case KRenderingPackage.KRENDERING__PARENT:
                return eInternalContainer().eInverseRemove(this, KRenderingPackage.KCONTAINER_RENDERING__CHILDREN, KContainerRendering.class, msgs);
        }
        return super.eBasicRemoveFromContainerFeature(msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case KRenderingPackage.KRENDERING__STYLES:
                return getStyles();
            case KRenderingPackage.KRENDERING__ID:
                return getId();
            case KRenderingPackage.KRENDERING__PARENT:
                return getParent();
            case KRenderingPackage.KRENDERING__PLACEMENT_DATA:
                return getPlacementData();
            case KRenderingPackage.KRENDERING__ACTIONS:
                return getActions();
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @SuppressWarnings("unchecked")
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case KRenderingPackage.KRENDERING__STYLES:
                getStyles().clear();
                getStyles().addAll((Collection<? extends KStyle>)newValue);
                return;
            case KRenderingPackage.KRENDERING__ID:
                setId((String)newValue);
                return;
            case KRenderingPackage.KRENDERING__PARENT:
                setParent((KContainerRendering)newValue);
                return;
            case KRenderingPackage.KRENDERING__PLACEMENT_DATA:
                setPlacementData((KPlacementData)newValue);
                return;
            case KRenderingPackage.KRENDERING__ACTIONS:
                getActions().clear();
                getActions().addAll((Collection<? extends KAction>)newValue);
                return;
        }
        super.eSet(featureID, newValue);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void eUnset(int featureID) {
        switch (featureID) {
            case KRenderingPackage.KRENDERING__STYLES:
                getStyles().clear();
                return;
            case KRenderingPackage.KRENDERING__ID:
                setId(ID_EDEFAULT);
                return;
            case KRenderingPackage.KRENDERING__PARENT:
                setParent((KContainerRendering)null);
                return;
            case KRenderingPackage.KRENDERING__PLACEMENT_DATA:
                setPlacementData((KPlacementData)null);
                return;
            case KRenderingPackage.KRENDERING__ACTIONS:
                getActions().clear();
                return;
        }
        super.eUnset(featureID);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public boolean eIsSet(int featureID) {
        switch (featureID) {
            case KRenderingPackage.KRENDERING__STYLES:
                return styles != null && !styles.isEmpty();
            case KRenderingPackage.KRENDERING__ID:
                return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
            case KRenderingPackage.KRENDERING__PARENT:
                return getParent() != null;
            case KRenderingPackage.KRENDERING__PLACEMENT_DATA:
                return placementData != null;
            case KRenderingPackage.KRENDERING__ACTIONS:
                return actions != null && !actions.isEmpty();
        }
        return super.eIsSet(featureID);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public int eBaseStructuralFeatureID(int derivedFeatureID, Class<?> baseClass) {
        if (baseClass == KStyleHolder.class) {
            switch (derivedFeatureID) {
                case KRenderingPackage.KRENDERING__STYLES: return KRenderingPackage.KSTYLE_HOLDER__STYLES;
                case KRenderingPackage.KRENDERING__ID: return KRenderingPackage.KSTYLE_HOLDER__ID;
                default: return -1;
            }
        }
        return super.eBaseStructuralFeatureID(derivedFeatureID, baseClass);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public int eDerivedStructuralFeatureID(int baseFeatureID, Class<?> baseClass) {
        if (baseClass == KStyleHolder.class) {
            switch (baseFeatureID) {
                case KRenderingPackage.KSTYLE_HOLDER__STYLES: return KRenderingPackage.KRENDERING__STYLES;
                case KRenderingPackage.KSTYLE_HOLDER__ID: return KRenderingPackage.KRENDERING__ID;
                default: return -1;
            }
        }
        return super.eDerivedStructuralFeatureID(baseFeatureID, baseClass);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public String toString() {
        if (eIsProxy()) return super.toString();

        StringBuilder result = new StringBuilder(super.toString());
        result.append(" (id: ");
        result.append(id);
        result.append(')');
        return result.toString();
    }

} //KRenderingImpl
