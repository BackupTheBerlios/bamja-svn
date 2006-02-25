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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.bamja.core.annotations.Component;
import org.bamja.core.events.Lifecycle;
import org.bamja.core.interfaces.ComponentMetadata;
import org.bamja.core.interfaces.ComponentState;
import org.bamja.core.interfaces.RequiredService;
import org.bamja.core.interfaces.RequiredServiceMetadata;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Jens Kutzsche
 * @since Version: 0.1.0 (18.02.2006)
 */
public class ComponentManager implements ServiceFactory {

    private ComponentState state = null;

    private BundleContext bundleContext = null;

    private ComponentMetadata componentMetadata = null;

    private List<RequiredServiceImpl> requiredServices = new ArrayList<RequiredServiceImpl>();

    private List<RequiredServiceImpl> requiredServicesFromUser = new ArrayList<RequiredServiceImpl>();

    private List<ComponentInstanceImpl> instances = new ArrayList<ComponentInstanceImpl>();

    private ServiceRegistration registration = null;

    private int invalidCount = 0;

    private Class componentClass = null;

    private boolean startAutomatic = false;

    /**
     * @param context
     * @param componentMetadata
     */
    public ComponentManager(BundleContext context,
            ComponentMetadata componentMetadata) {
        assert context != null;
        assert componentMetadata != null;

        this.bundleContext = context;
        this.componentMetadata = componentMetadata;

        if (!checkComponent()) {
            determine(true);
            return;
        }

        for (RequiredServiceMetadata requiredMetadata : this.componentMetadata
                .getRequiredServices()) {
            RequiredServiceImpl requiredService = new RequiredServiceImpl(this,
                    requiredMetadata);

            if (requiredMetadata.isRequiredFromUser()) {
                this.requiredServicesFromUser.add(requiredService);
            } else {
                this.requiredServices.add(requiredService);
            }
        }

        if (initialize()) {
            registerFactory();
        } else {
            determine(true);
        }
    }

    private boolean checkComponent() {
        Class c = this.componentMetadata.getComponentClass();
        if (c == null) {
            try {
                c = this.bundleContext.getBundle().getClassLoader().loadClass(
                        this.componentMetadata.getComponentClassName());
            } catch (ClassNotFoundException e) {
                BundleManager
                        .error("ComponentManager: The classloader can't load the component class whose name are defined in the xml file:"
                                + e);
                return false;
            }
        }
        this.componentClass = c;

        if (!ComponentInstanceImpl.checkComponentInstance(this)) {
            return false;
        }

        return true;
    }

    private boolean initialize() {
        this.invalidCount = 0;
        for (RequiredService requiredService : this.requiredServices) {
            if (requiredService.getState() == RequiredService.State.DEVECTIVE) {
                return false;
            }
            if (requiredService.getState() != RequiredService.State.VALID) {
                this.invalidCount++;
            }
        }

        if (this.invalidCount == 0) {
            setState(ComponentState.VALID);
        } else {
            setState(ComponentState.INVALID);
        }

        if (this.componentMetadata.isStartAutomatically()) {
            startComponent();
        }
        return true;
    }

    public boolean startComponent() {
        this.startAutomatic = true;
        if (this.state == ComponentState.VALID && this.instances.isEmpty()) {
            if (createNewInstance() == null) {
                return false;
            }
            return true;
        }
        return false;
    }

    private void registerFactory() {
        if (this.componentMetadata.getProvidedServices() != null
                && this.registration == null
                && this.state == ComponentState.VALID) {
            Properties props = this.componentMetadata.getProperties();
            this.registration = this.bundleContext.registerService(
                    this.componentMetadata.getProvidedServices(), this, props);

            BundleManager
                    .trace("ComponentManager: ServiceFactory for the component ["
                            + this.componentMetadata.getComponentClassName()
                            + "] successfully registered!");
        }
    }

    private void unregisterFactory() {
        if (this.registration != null) {
            this.registration.unregister();
            this.registration = null;

            BundleManager
                    .trace("ComponentManager: ServiceFactory for the component ["
                            + this.componentMetadata.getComponentClassName()
                            + "] is unregistered!");
        }
    }

    void changeRequiredServiceState(RequiredService.State state) {
        assert state != null;

        if (state == RequiredService.State.VALID) {
            this.invalidCount--;
            if (this.invalidCount == 0) {
                setState(ComponentState.VALID);
                if (this.startAutomatic) {
                    startComponent();
                }
                registerFactory();
            }
        } else if (state == RequiredService.State.INVALID) {
            this.invalidCount++;
            invalidate();
        }
    }

    public void determine() {
        determine(false);
    }

    void determine(boolean setDevective) {
        if (this.state == ComponentState.DETERMINE
                || (this.state == ComponentState.DEVECTIVE && setDevective)) {
            return;
        }
        if (this.state != ComponentState.DEVECTIVE) {
            for (RequiredServiceImpl req : this.requiredServices) {
                this.bundleContext.removeServiceListener(req);
            }

            invalidate();

            this.bundleContext = null;
            this.requiredServices = null;
            this.requiredServicesFromUser = null;
            this.instances = null;
            this.registration = null;
            this.componentClass = null;
        }

        if (setDevective) {
            setState(ComponentState.DEVECTIVE);
            BundleManager
                    .trace("ComponentManager: The manager of the component ["
                            + this.componentMetadata.getComponentClassName()
                            + "] is defective and therefore was determines.");
        } else {
            setState(ComponentState.DETERMINE);
            BundleManager
                    .trace("ComponentManager: The manager of the component ["
                            + this.componentMetadata.getComponentClassName()
                            + "] was determines.");
        }
    }

    private void invalidate() {
        if (this.state == ComponentState.INVALID) {
            return;
        }
        for (ComponentInstanceImpl instance : this.instances) {
            if (instance.getInstanceObject() instanceof Lifecycle) {
                ((Lifecycle) instance.getInstanceObject()).deactivate();
            }
        }

        unregisterFactory();

        try {
            for (RequiredServiceImpl req : this.requiredServices) {
                req.unbindAll();
            }
        } catch (Exception e) {
            determine(true);
            return;
        }

        this.instances.clear();

        setState(ComponentState.INVALID);
        BundleManager.trace("ComponentManager: The manager of the component ["
                + this.componentMetadata.getComponentClassName()
                + "] was invalidate.");
    }

    public Object getService(Bundle bundle, ServiceRegistration registration) {
        assert bundle != null;
        assert registration != null;

        Object ret = null;
        switch (this.componentMetadata.getInstantiationType()) {
            case SINGLETON:
                if (this.instances.size() != 0) {
                    ret = this.instances.get(0);
                    break;
                }
            case ONE_FOR_BUNDLE:
                ret = createNewInstance().getInstanceObject();
        }
        return ret;
    }

    private ComponentInstanceImpl createNewInstance() {
        ComponentInstanceImpl newInstance = null;
        try {
            newInstance = new ComponentInstanceImpl(this);
            for (RequiredServiceImpl req : this.requiredServices) {
                req.bindComponent(newInstance.getInstanceObject());
            }
        } catch (Exception e) {
            BundleManager
                    .error("ComponentManager: The instance of the component class can't create, or bind:"
                            + e);
            determine(true);
            return null;
        }
        this.instances.add(newInstance);

        if (newInstance.getInstanceObject() instanceof Lifecycle) {
            ((Lifecycle) newInstance.getInstanceObject()).activate();
        }

        return newInstance;
    }

    public void ungetService(Bundle bundle, ServiceRegistration registration,
            Object service) {
        assert bundle != null;
        assert registration != null;
        assert service != null;

        if (this.componentMetadata.getInstantiationType() == Component.InstantiationType.ONE_FOR_BUNDLE) {
            try {
                for (RequiredServiceImpl req : this.requiredServices) {
                    req.unbindComponents(service);
                }
            } catch (Exception e) {
                determine(true);
                return;
            }
            this.instances.remove(service);
        }
    }

    public Class getComponentClass() {
        return this.componentClass;
    }

    public ComponentState getState() {
        return this.state;
    }

    private void setState(ComponentState state) {
        this.state = state;
    }

    public BundleContext getBundleContext() {
        return this.bundleContext;
    }

    public ComponentMetadata getComponentMetadata() {
        return this.componentMetadata;
    }
}
