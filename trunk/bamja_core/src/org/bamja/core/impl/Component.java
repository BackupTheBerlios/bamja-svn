/*
 * This file is a part of the project "bamja". Bamja is a rich client platform
 * for data acquisition programs based on OSGi.
 * 
 * Copyright (C) 2006  Jens Kutzsche
 *
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by the 
 * Free Software Foundation; either version 2.1 of the License, 
 * or any later version.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along 
 * with this program; if not, write to the Free Software Foundation, Inc., 
 * 51 Franklin St, Fifth Floor, Boston, MA 02110, USA 
 * You find the license in the LICENSE.txt file and in the internet:
 * http://www.gnu.org/copyleft/lesser.html
 * 
 * Contact: Jens Kutzsche (genty@users.berlios.de)
 * 
 * =================================================
 * 
 * Diese Datei ist Teil des Projektes "bamja". Bamja ist eine Rich Client 
 * Plattform für Programme zur Datenerfassungs, welche auf einem OSGi-Framework
 * aufbaut.
 * 
 * Copyright (C) 2006 Jens Kutzsche
 * 
 * Dieses Programm ist freie Software. Sie können es unter den Bedingungen der
 * GNU Lesser General Public License, wie von der Free Software Foundation
 * veröffentlicht, weitergeben und/oder modifizieren, entweder gemäß Version 2.1
 * der Lizenz oder jeder späteren Version.
 * 
 * Die Veröffentlichung dieses Programms erfolgt in der Hoffnung, daß es Ihnen
 * von Nutzen sein wird, aber OHNE IRGENDEINE GARANTIE, sogar ohne die implizite
 * Garantie der MARKTREIFE oder der VERWENDBARKEIT FÜR EINEN BESTIMMTEN ZWECK.
 * Details finden Sie in der GNU General Public License.
 * 
 * Sie sollten eine Kopie der GNU Lesser General Public License zusammen mit diesem
 * Programm erhalten haben. Falls nicht, schreiben Sie an die Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA. 
 * Sie finden den Lizenztext in der Datei LICENSE.txt und im Internet:
 * Englisches Orginal: http://www.gnu.org/copyleft/lesser.html 
 * Deutsche Übersetzung: http://www.gnu.de/lgpl-ger.html
 * 
 * Kontakt: Jens Kutzsche (genty@users.berlios.de)
 * 
 * *************************************************
 * *************************************************
 * 
 * This file based on the work of the Knopflerfish project, which was 
 * licensed under a BSD Software License and conains the follow informations:
 * 
 * Die Datei basiert auf der Arbeit des Knopflerfish Projekts, welche unter einer
 * BSD Lizenz veröffentlicht wurde und folgende Hinweise enthält:
 * 
 * -------------------------------------------------
 * Copyright (c) 2006, KNOPFLERFISH project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution.
 *
 * - Neither the name of the KNOPFLERFISH project nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.bamja.core.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.bamja.core.ComponentContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentInstance;

abstract class Component implements ServiceFactory {

    protected Config config;

    private boolean active;

    private Object instance;

    private Object duplexObject = null;

    protected BundleContext bundleContext;

    protected ServiceRegistration serviceRegistration = null;

    protected ComponentContext componentContext;

    protected ComponentInstance componentInstance;

    protected Bundle usingBundle;

    private Hashtable effectiveProperties; // Properties from cm. These can be
                                            // discarded.

    public Component(Config config, Dictionary overriddenProps) {
        this.config = config;

        instance = null;
        componentContext = null;

        bundleContext = Backdoor.getBundleContext(config.getBundle());

        if (overriddenProps != null) {
            config.overrideProperties(overriddenProps);
        }

        config.setProperty(ComponentConstants.COMPONENT_NAME, config.getName());
        cmDeleted();
    }

    public void enable() {
        config.enable();
    }

    public void disable() {
        config.disable();
    }

    /**
     * Activates a component. If the component isn't enabled or satisfied,
     * nothing will happen. If the component is already activated nothing will
     * happen.
     */
    public synchronized void activate() {
        // this method is described on page 297 r4

        // Synchronized because the service is registered before activation,
        // enabling another thread to get the service and thereby trigger a
        // second activate() call.

        if (!config.isEnabled() || !config.isSatisfied())
            return;

        if (isActivated())
            return;

        // 1. get class
        Class klass = config.getImplementationClass();

        try {
            // 2. create ComponentContext and ComponentInstance
            instance = klass.newInstance();
            componentInstance = new ComponentInstanceImpl();
            componentContext = new ComponentContextImpl(componentInstance);

        } catch (IllegalAccessException e) {
            if (Activator.log.doError())
                Activator.log.error("Could not access constructor of class "
                        + config.getImplementation());
            return;

        } catch (InstantiationException e) {
            if (Activator.log.doError())
                Activator.log
                        .error("Could not create instance of "
                                + config.getImplementation()
                                + " isn't a proper class.");
            return;

        } catch (ExceptionInInitializerError e) {
            if (Activator.log.doError())
                Activator.log.error("Constructor for "
                        + config.getImplementation() + " threw exception.", e);
            return;

        } catch (SecurityException e) {
            if (Activator.log.doError())
                Activator.log.error(
                        "Did not have permissions to create an instance of "
                                + config.getImplementation(), e);
            return;
        }

        // 3. Bind the services. This should be sent to all the references.
        config.bindReferences(instance, getDuplexObject());

        try {
            Method method = klass.getDeclaredMethod("activate",
                    new Class[] { ComponentContext.class });
            method.setAccessible(true);
            method.invoke(instance, new Object[] { componentContext });

        } catch (NoSuchMethodException e) {
            // this instance does not have an activate method, (which is ok)
            if (Activator.log.doDebug()) {
                Activator.log
                        .debug("this instance does not have an activate method, (which is ok)");
            }
        } catch (IllegalAccessException e) {
            Activator.log.error(
                    "Declarative Services could not invoke \"deactivate\""
                            + " method in component \"" + config.getName()
                            + "\". Got exception", e);
            return;

        } catch (InvocationTargetException e) {
            // the method threw an exception.
            Activator.log.error(
                    "Declarative Services got exception when invoking "
                            + "\"activate\" in component " + config.getName(),
                    e);

            // if this happens the component should not be activatated
            config.unbindReferences(instance, getDuplexObject());
            instance = null;
            componentContext = null;

            return;
        }

        active = true;
    }

    /** deactivates a component */
    public synchronized void deactivate() {
        // this method is described on page 432 r4

        if (!isActivated())
            return;
        
        try {
            Class klass = instance.getClass();
            Method method = klass.getDeclaredMethod("deactivate",
                    new Class[] { ComponentContext.class });
            method.setAccessible(true);
            method.invoke(instance, new Object[] { componentContext });

        } catch (NoSuchMethodException e) {
            // this instance does not have a deactivate method, (which is ok)
            if (Activator.log.doDebug()) {
                Activator.log
                        .debug("this instance does not have a deactivate method, (which is ok)");
            }
        } catch (IllegalAccessException e) {
            Activator.log.error(
                    "Declarative Services could not invoke \"deactivate\""
                            + " method in component \"" + config.getName()
                            + "\". Got exception", e);

        } catch (InvocationTargetException e) {
            // the method threw an exception.
            Activator.log
                    .error(
                            "Declarative Services got exception when invoking "
                                    + "\"deactivate\" in component "
                                    + config.getName(), e);
        }
        
        config.unbindReferences(instance, getDuplexObject());

        instance = null;
        componentContext = null;
        componentInstance = null;
        active = false;
    }

    public boolean isActivated() {
        return active;
    }

    public void unregisterService() {
        if (serviceRegistration != null) {
            try {
                serviceRegistration.unregister();
            } catch (IllegalStateException ignored) {
                // Nevermind this, it might have been unregistered previously.
            }
        }
    }

    public void registerService() {
        if (Activator.log.doDebug()) {
            Activator.log.debug("registerService() got BundleContext: "
                    + bundleContext);
        }

        if (!config.getShouldRegisterService())
            return;

        String[] interfaces = config.getServices();

        if (interfaces == null) {
            return;
        }
        serviceRegistration = bundleContext.registerService(interfaces, this,
                effectiveProperties);
    }

    /**
     * This must be overridden
     */
    public Object getService(Bundle usingBundle, ServiceRegistration reg) {
        this.usingBundle = usingBundle;
        return instance;
    }

    /**
     * This must be overridden
     */
    public void ungetService(Bundle usingBundle, ServiceRegistration reg,
            Object obj) {
        this.usingBundle = null;
    }

    /**
     * this method is called whenever this components configuration becomes
     * satisfied.
     */
    public abstract void satisfied();

    /**
     * this method is called whenever this components configuration becomes
     * unsatisfied.
     */

    public abstract void unsatisfied();

    public void setProperty(Object key, Object value) {
        config.setProperty((String) key, value);
    }

    // to provide compability with component context
    private class ComponentContextImpl implements ComponentContext {
        private ComponentInstance componentInstance;

        private Dictionary immutable;

        public ComponentContextImpl(ComponentInstance componentInstance) {
            this.componentInstance = componentInstance;
        }

        public Dictionary getProperties() {
            if (immutable == null) {
                immutable = new ImmutableDictionary(effectiveProperties);
            }

            return immutable;
        }

        public Object locateService(Object bindObject, String name) {
            Reference ref = config.getReference(name);
            return ref.getService(bindObject);
        }

        public Object locateService(Object bindObject, String name, ServiceReference sRef) {
            Reference ref = config.getReference(name);
            return ref.getService(bindObject, sRef);
        }

        public Object[] locateServices(Object bindObject, String name) {
            Reference ref = config.getReference(name);
            ServiceReference[] refs = ref.getServiceReferences();
            Object[] ret = new Object[refs.length];
            for (int i = 0; i < refs.length; i++) {
                ret[i] = ref.getService(bindObject, refs[i]);
            }
            return ret;
        }

        public BundleContext getBundleContext() {
            return bundleContext;
        }

        public ComponentInstance getComponentInstance() {
            return componentInstance;
        }

        public Bundle getUsingBundle() {
            return usingBundle;
        }

        public void enableComponent(String name) {
            Collection collection = SCR.getInstance().getComponents(
                    config.getBundle());

            for (Iterator i = collection.iterator(); i.hasNext();) {

                Config config = (Config) i.next();

                if (name == null || config.getName().equals(name)) {

                    if (!config.isEnabled()) {
                        Component component = config.createComponent();
                        component.enable();
                    }
                }
            }
        }

        public void disableComponent(String name) {
            Collection collection = SCR.getInstance().getComponents(
                    config.getBundle());

            for (Iterator i = collection.iterator(); i.hasNext();) {

                Config config = (Config) i.next();

                if (name == null || config.getName().equals(name)) {

                    if (config.isEnabled()) {
                        config.disable();
                    }
                }
            }
        }

        public ServiceReference getServiceReference() {
            /*
             * We need to do it like this since this function might be called
             * before *we* even know what it is. However, this value is know by
             * the framework, hence we can actually retrieve it.
             */

            if (serviceRegistration == null) {

                Object thisComponentId = config.getProperties().get(
                        ComponentConstants.COMPONENT_ID);

                try {
                    ServiceReference[] refs = bundleContext
                            .getServiceReferences(config.getImplementation(),
                                    "(" + ComponentConstants.COMPONENT_ID + "="
                                            + thisComponentId + ")");
                    if (refs == null) {
                        return null;
                    }

                    return refs[0];

                } catch (Exception e) {
                    throw new RuntimeException("This is a bug.", e);
                }

            } else {
                return serviceRegistration.getReference();
            }
        }
    }

    private class ComponentInstanceImpl implements ComponentInstance {

        public void dispose() {
            unregisterService();
            deactivate();
            disable();
        }

        public Object getInstance() {
            return instance; // will be null when the component is not
            // activated.
        }

    }

    public Config getConfig() {
        return config;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public Object getInstance() {
        return instance;
    }

    public ComponentInstance getComponentInstance() {
        return componentInstance;
    }

    public Object getDuplexObject() {
        return this.duplexObject;
    }

    public void setDuplexObject(Object duplexObject) {
        this.duplexObject = duplexObject;
    }

    /*
     * We need to keep track of the entries that has been changed by CM since
     * these might have to be removed when a CM_DELETED event occurs..
     */

    public void cmUpdated(Dictionary dict) {
        if (dict == null)
            return;

        for (Enumeration e = dict.keys(); e.hasMoreElements();) {
            Object key = e.nextElement();
            if (!key.equals(ComponentConstants.COMPONENT_NAME)
                    && !key.equals(ComponentConstants.COMPONENT_ID)) {

                effectiveProperties.put(key, dict.get(key));
            }
        }
    }

    public void cmDeleted() {
        Dictionary dict = config.getProperties();
        effectiveProperties = new Hashtable();
        for (Enumeration e = dict.keys(); e.hasMoreElements();) {
            Object key = e.nextElement();
            effectiveProperties.put(key, dict.get(key));
        }
    }
}
