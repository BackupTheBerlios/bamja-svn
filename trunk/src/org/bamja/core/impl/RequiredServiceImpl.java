/*
 * This file is a part of the project "bamja". Bamja is a rich client platform 
 * based on OSGi.
 * 
 * Copyright (C) 2006  Jens Kutzsche
 *
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the 
 * Free Software Foundation; either version 2 of the License, 
 * or any later version.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along 
 * with this program; if not, write to the Free Software Foundation, Inc., 
 * 51 Franklin St, Fifth Floor, Boston, MA 02110, USA 
 * You find the license in the LICENSE.txt file and in the internet:
 * http://www.fsf.org/licenses/gpl.html 
 * 
 * Contact: Jens Kutzsche (genty@users.berlios.de)
 * 
 * =================================================
 * 
 * Diese Datei ist Teil des Projektes "bamja". Bamja ist eine Rich Client 
 * Plattform aufbauend auf einem OSGi-Framework.
 * 
 * Copyright (C) 2006 Jens Kutzsche
 * 
 * Dieses Programm ist freie Software. Sie können es unter den Bedingungen der
 * GNU General Public License, wie von der Free Software Foundation
 * veröffentlicht, weitergeben und/oder modifizieren, entweder gemäß Version 2
 * der Lizenz oder jeder späteren Version.
 * 
 * Die Veröffentlichung dieses Programms erfolgt in der Hoffnung, daß es Ihnen
 * von Nutzen sein wird, aber OHNE IRGENDEINE GARANTIE, sogar ohne die implizite
 * Garantie der MARKTREIFE oder der VERWENDBARKEIT FÜR EINEN BESTIMMTEN ZWECK.
 * Details finden Sie in der GNU General Public License.
 * 
 * Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
 * Programm erhalten haben. Falls nicht, schreiben Sie an die Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA. 
 * Sie finden den Lizenztext in der Datei LICENSE.txt und im Internet:
 * Englisches Orginal: http://www.fsf.org/licenses/gpl.html 
 * Deutsche Übersetzung: http://www.gnu.de/gpl-ger.html
 * 
 * Kontakt: Jens Kutzsche (genty@users.berlios.de)
 *
 **/
package org.bamja.core.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bamja.core.interfaces.ComponentInstance;
import org.bamja.core.interfaces.InstanceFactory;
import org.bamja.core.interfaces.RequiredService;
import org.bamja.core.interfaces.RequiredServiceMetadata;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

/**
 * @author Jens Kutzsche
 * @since Version: 0.1.0 (18.02.2006)
 */
public class RequiredServiceImpl implements RequiredService, ServiceListener {

    private RequiredService.State state = null;

    private ComponentManager componentManager = null;

    private RequiredServiceMetadata metadata = null;

    private List<ServiceReference> references = null;

    private Method bindMethod = null;

    private Method unbindMethod = null;

    private Map<Object, HashMap<ServiceReference, Object>> boundMap = new HashMap<Object, HashMap<ServiceReference, Object>>();

    private boolean oneStart = false;

    /**
     * @param metadata
     * @throws InvalidSyntaxException
     */
    public RequiredServiceImpl(ComponentManager componentManager,
            RequiredServiceMetadata metadata) {
        assert componentManager != null;
        assert metadata != null;

        this.componentManager = componentManager;
        this.metadata = metadata;

        if (!checkRequiredService()) {
            setState(RequiredService.State.DEVECTIVE);
            return;
        }

        ServiceReference[] refs = null;
        try {
            this.componentManager.getBundleContext().addServiceListener(this,
                    this.metadata.getFilter());

            refs = this.componentManager.getBundleContext()
                    .getServiceReferences(this.metadata.getServiceName(),
                            this.metadata.getFilter());
        } catch (InvalidSyntaxException e) {
            BundleManager
                    .error("RequiredServiceImpl: The Filter contains an invalide filter string:"
                            + e);
            setState(RequiredService.State.DEVECTIVE);
            return;
        }

        if (refs == null) {
            this.references = new ArrayList<ServiceReference>();
            setState(this.metadata.isOptional() ? RequiredService.State.VALID
                    : RequiredService.State.INVALID);
        } else {
            this.references = Arrays.asList(refs);
            setState(RequiredService.State.VALID);
        }
    }

    private boolean checkRequiredService() {
        try {
            this.bindMethod = getBindingMethod(this.metadata
                    .getBindMethodName());
            this.unbindMethod = getBindingMethod(this.metadata
                    .getUnbindMethodName());
        } catch (NoSuchMethodException e) {
            BundleManager
                    .error("RequiredServiceImpl: The methods are defined in the xml file or with the annotation can't find:"
                            + e);
            return false;
        } catch (ClassNotFoundException e) {
            BundleManager
                    .error("RequiredServiceImpl: The classloader can't load the required service class whose name are defined in the xml file:"
                            + e);
            return false;
        }
        return true;
    }

    private Method getBindingMethod(String methodName)
            throws NoSuchMethodException, ClassNotFoundException {
        Method method = null;

        if (this.metadata.isNeedFactory()) {
            try {
                method = this.componentManager.getComponentClass().getMethod(
                        methodName, new Class[] { InstanceFactory.class });
            } catch (NoSuchMethodException e) {
                method = this.componentManager.getComponentClass().getMethod(
                        methodName,
                        new Class[] { ServiceReference.class,
                                InstanceFactory.class });
            }
        } else {
            try {
                method = this.componentManager.getComponentClass().getMethod(
                        methodName, new Class[] { getServiceClass() });
            } catch (NoSuchMethodException e) {
                method = this.componentManager.getComponentClass()
                        .getMethod(
                                methodName,
                                new Class[] { ServiceReference.class,
                                        getServiceClass() });
            }
        }
        return method;
    }

    private Class getServiceClass() throws ClassNotFoundException {
        Class c = this.metadata.getServiceClass();
        if (c == null) {
            c = this.componentManager.getBundleContext().getBundle()
                    .getClassLoader().loadClass(this.metadata.getServiceName());
        }
        return c;
    }

    public void bindComponent(Object componentObject) throws Exception {
        assert componentObject != null
                && componentObject.getClass().equals(
                        this.componentManager.getComponentClass());

        if (this.state != RequiredService.State.VALID) {
            return;
        }

        try {
            HashMap<ServiceReference, Object> tmpMap = new HashMap<ServiceReference, Object>();
            int max = this.metadata.isMultiple() ? this.references.size() : 1;

            for (int i = 0; i < max; i++) {
                ServiceReference reference = this.references.get(i);

                tmpMap.put(reference,
                        callBindMethod(componentObject, reference));
            }

            this.boundMap.put(componentObject, tmpMap);
        } catch (Exception e) {
            BundleManager
                    .error("RequiredServiceImpl : The methode "
                            + this.metadata.getBindMethodName()
                            + " can't invoke: " + e);
            setState(RequiredService.State.DEVECTIVE);
            throw e;
        }
        
        this.oneStart = true;
    }

    private void bindNewReference(ServiceReference reference) throws Exception {
        if (this.state != RequiredService.State.VALID) {
            return;
        }

        try {
            for (Object componentObject : this.boundMap.keySet()) {
                HashMap<ServiceReference, Object> tmpMap = this.boundMap
                        .get(componentObject);

                tmpMap.put(reference,
                        callBindMethod(componentObject, reference));
            }
        } catch (Exception e) {
            BundleManager
                    .error("RequiredServiceImpl : The methode "
                            + this.metadata.getBindMethodName()
                            + " can't invoke: " + e);
            setState(RequiredService.State.DEVECTIVE);
            throw e;
        }
    }

    private Object callBindMethod(Object componentObject,
            ServiceReference reference) throws Exception {
        Object o = null;
        if (this.metadata.isNeedFactory()) {
            o = new InstanceFactoryImpl(this.componentManager
                    .getBundleContext(), reference);
        } else {
            o = this.componentManager.getBundleContext().getService(reference);
        }

        if (this.bindMethod.getParameterTypes().length == 1) {
            this.bindMethod.invoke(componentObject, new Object[] { o });
        } else if (this.bindMethod.getParameterTypes().length == 2) {
            this.bindMethod.invoke(componentObject,
                    new Object[] { reference, o });
        }
        return o;
    }

    public void unbindComponents(Object componentObject) throws Exception {
        assert componentObject != null
                && this.boundMap.containsKey(componentObject);

        HashMap<ServiceReference, Object> referenceMap = this.boundMap
                .get(componentObject);

        try {
            for (ServiceReference reference : referenceMap.keySet()) {
                Object o = referenceMap.get(reference);
                callUnbindMethod(componentObject, reference, o);
            }
        } catch (Exception e) {
            BundleManager.error("RequiredServiceImpl : The methode "
                    + this.metadata.getUnbindMethodName() + " can't invoke: "
                    + e);
            setState(RequiredService.State.DEVECTIVE);
            referenceMap.clear();
            this.boundMap.clear();
            throw e;
        }

        referenceMap.clear();
        this.boundMap.remove(componentObject);
    }

    private void unbindReference(ServiceReference reference) throws Exception {
        try {
            for (Object componentObject : this.boundMap.keySet()) {
                Object o = this.boundMap.get(componentObject).get(reference);
                callUnbindMethod(componentObject, reference, o);
            }
        } catch (Exception e) {
            BundleManager.error("RequiredServiceImpl : The methode "
                    + this.metadata.getUnbindMethodName() + " can't invoke: "
                    + e);
            setState(RequiredService.State.DEVECTIVE);
            throw e;
        }
    }

    private void callUnbindMethod(Object componentObject,
            ServiceReference reference, Object o) throws Exception {
        if (this.unbindMethod.getParameterTypes().length == 1) {
            this.unbindMethod.invoke(componentObject, new Object[] { o });
        } else if (this.unbindMethod.getParameterTypes().length == 2) {
            this.unbindMethod.invoke(componentObject, new Object[] { reference,
                    o });
        }

        if (this.metadata.isNeedFactory() && o instanceof InstanceFactoryImpl) {
            ((InstanceFactoryImpl) o).ungetAll();
        } else {
            this.componentManager.getBundleContext().ungetService(reference);
        }
    }

    public void unbindAll() throws Exception {
        Object[] objects = this.boundMap.keySet().toArray();
        for (Object componentObject : objects) {
            unbindComponents(componentObject);
        }
    }

    public void serviceChanged(ServiceEvent event) {
        assert event != null;

        if (event.getType() == ServiceEvent.REGISTERED) {
            switch (this.metadata.getPolicy()) {
                case STATIC_ONE_START:
                case REMOVE_ONLY_ONE_START:
                    if (!this.oneStart) {
                        break;
                    }
                case STATIC:
                case REMOVE_ONLY:
                    return;
                case DYNAMIC:
                case ADD_ONLY:
                    if (this.metadata.isMultiple()
                            || this.references.size() == 0) {
                        try {
                            bindNewReference(event.getServiceReference());
                        } catch (Exception e) {
                            this.componentManager.determine(true);
                            return;
                        }
                    }
            }

            this.references.add(event.getServiceReference());
            setState(RequiredService.State.VALID);
        } else if (event.getType() == ServiceEvent.UNREGISTERING) {
            switch (this.metadata.getPolicy()) {
                case STATIC:
                case ADD_ONLY:
                    setState(RequiredService.State.INVALID);
                    return;
                case STATIC_ONE_START:
                    if (this.oneStart) {
                        setState(RequiredService.State.INVALID);
                        break;
                    }
                case REMOVE_ONLY_ONE_START:
                case REMOVE_ONLY:
                case DYNAMIC:
                    if (!this.references.remove(event.getServiceReference())) {
                        BundleManager.trace("RequiredServiceImpl: "
                                + this.metadata.getServiceName()
                                + ": The ServiceReference ["
                                + event.getServiceReference()
                                + "] are not in the list of references.");
                    }
                    if (this.references.size() == 0
                            && !this.metadata.isOptional()) {
                        setState(RequiredService.State.INVALID);
                        return;
                    }
                    try {
                        unbindReference(event.getServiceReference());
                    } catch (Exception e) {
                        this.componentManager.determine(true);
                        return;
                    }
            }
        }
    }

    private void setState(RequiredService.State state) {
        RequiredService.State tmpState = this.state;
        this.state = state;
        if (tmpState != null && state != tmpState) {
            this.componentManager.changeRequiredServiceState(state);
        }
    }

    public RequiredService.State getState() {
        return this.state;
    }

    public RequiredServiceMetadata getRequiredServiceMetadata() {
        return this.metadata;
    }

    public List<ComponentInstance> getBoundComponentInstances() {
        // return this.boundComponentInstances;
        return null;
    }
}
