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

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentConstants;

// BjÃ¶rn should we merge this with Component?

class Config {

    private String name;

    private String implementation;

    private Class implementationClass = null;

    private String factory;

    private boolean enabled;

    private boolean autoEnabled;

    private boolean serviceFactory;

    private boolean duplexFactory = false;

    private boolean immediate;

    private boolean registerService;

    private Hashtable properties;

    private ArrayList references;

    private ArrayList<DuplexReference> duplexReferences = new ArrayList<DuplexReference>();
        
    private ArrayList services;

    private ArrayList<String> virtualServices = new ArrayList<String>();

    private Component component;

    private Bundle bundle;

    public Config(Bundle bundle) {
        this.bundle = bundle;
        properties = new Hashtable();
        references = new ArrayList();
        services = new ArrayList();
        registerService = true;
    }

    public void enable() {
        enable(null);
    }

    public synchronized void enable(Dictionary overriddenProps) {
        for (Iterator iter = references.iterator(); iter.hasNext();) {
            ((Reference) iter.next()).open();
        }

        if (component == null) // is this safe?
            createComponent(overriddenProps);

        SCR.getInstance().initComponent(component);

        enabled = true;
        referenceSatisfied();
    }

    public synchronized void disable() {
        enabled = false;

        if (component != null) {
            SCR.getInstance().removeComponent(component);
        }

        referenceUnsatisfied();
        for (Iterator iter = references.iterator(); iter.hasNext();) {
            ((Reference) iter.next()).close();
        }
    }

    public Component createComponent() {
        return createComponent(null);
    }

    public Component createComponent(Dictionary overriddenProps) {
        if (getFactory() != null) {
            component = new FactoryComponent(this, overriddenProps);

        } else if (isServiceFactory()) {
            component = new ServiceFactoryComponent(this, overriddenProps);
            
        } else if (isDuplexFactory()) {
            this.component = new DuplexFactoryComponentImpl(this, overriddenProps);
        } else if (isImmediate() || services.isEmpty()) {
            component = new ImmediateComponent(this, overriddenProps);

        } else if (!isImmediate() && !services.isEmpty()) {
            component = new DelayedComponent(this, overriddenProps);
        } else {
            throw new RuntimeException(
                    "This is a bug and should not be happening.");
        }

        return component;
    }

    public boolean isSatisfied() {
        if (!isEnabled() || getImplementationClass() == null)
            return false;

        for (int i = 0; i < references.size(); i++) {
            Reference ref = (Reference) references.get(i);
            if (!ref.isSatisfied())
                return false;
        }

        return true;
    }

    public boolean getShouldRegisterService() {
        return services.size() > 0 && registerService;
    }

    public void setShouldRegisterService(boolean registerService) {
        this.registerService = registerService;
    }

    public void referenceSatisfied() {
        if (isSatisfied() && component != null) {
            component.satisfied();
        }
    }

    public void referenceUnsatisfied() {
        if (!isSatisfied() && component != null) {
            component.unsatisfied();
        }
    }

    public void bindReferences(Object instance, Object duplexObject) {
        for (int i = 0; i < references.size(); i++) {
            ((Reference) references.get(i)).bind(instance);
        }
        for (DuplexReference ref : this.duplexReferences) {
           ref.bind(instance, duplexObject);
        }
    }

    public void unbindReferences(Object instance, Object duplexObject) {
        for (int i = this.duplexReferences.size() - 1; i >= 0; i--) {
            this.duplexReferences.get(i).unbind(instance, duplexObject);
        }
        for (int i = references.size() - 1; i >= 0; i--) {
            ((Reference) references.get(i)).unbind(instance);
        }
    }

    public String[] getServices() {
        if (services.size() == 0)
            return null; // HEY, this might be dangerous

        String[] ret = new String[services.size()];
        services.toArray(ret);
        return ret;
    }

    public ArrayList getVirtualServices() {
        return this.virtualServices;
    }

    public Dictionary getProperties() {
        return properties;
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    public Bundle getBundle() {
        return bundle;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isAutoEnabled() {
        return autoEnabled;
    }

    public String getImplementation() {
        return implementation;
    }

    public Class getImplementationClass() {
        return this.implementationClass;
    }

    public boolean isImmediate() {
        return immediate;
    }

    public boolean isServiceFactory() {
        return serviceFactory;
    }

    public boolean isDuplexFactory() {
        return this.duplexFactory;
    }

    public String getFactory() {
        return factory;
    }

    protected ArrayList getReferences() {
        return references;
    }

    public Reference getReference(String name) {

        for (int i = 0; i < references.size(); i++) {
            Reference ref = (Reference) references.get(i);

            if (name.equals(ref.getName())) {
                return ref;
            }
        }

        return null;
    }

    public ArrayList<DuplexReference> getDuplexReferences() {
        return this.duplexReferences;
    }

    /**
     * Is this really safe? What should happen when CM overrides a factory's
     * properties? Should these changes be "inherited" instances created by the
     * factory?
     * 
     * 
     * Overrides properties according to 112.6 i.e avoids changing
     * component.name and component.id
     */
    public void overrideProperties(Dictionary overriddenProps) {

        for (Enumeration e = overriddenProps.keys(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            if (!key.equals(ComponentConstants.COMPONENT_NAME)
                    && !key.equals(ComponentConstants.COMPONENT_ID)) {
                setProperty(key, overriddenProps.get(key));
            }
        }
    }

    public void addReference(Reference ref) {
        ref.setConfig(this);
        references.add(ref);
    }

    public void addDuplexReference(DuplexReference ref) {
        ref.setConfig(this);
        this.duplexReferences.add(ref);
        this.duplexFactory = true;
    }

    public void addService(String interfaceName) {
        services.add(interfaceName);
    }

    public void addVirtualService(String virtualService) {
        this.virtualServices.add(virtualService);
    }

    public void setAutoEnabled(boolean autoEnabled) {
        this.autoEnabled = autoEnabled;
    }

    public void setImplementation(String impl) {
        implementation = impl;

        try {
            this.implementationClass = bundle.loadClass(impl);
        } catch (ClassNotFoundException e) {
            if (Activator.log.doError())
                Activator.log.error("Could not find class "
                        + this.implementation);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setServiceFactory(boolean isServiceFactory) {
        serviceFactory = isServiceFactory;
    }

    public void setDuplexFactory(boolean duplexFactory) {
        this.duplexFactory = duplexFactory;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }

    public void setImmediate(boolean isImmediate) {
        immediate = isImmediate;
    }

    public Config copy() {
        Config config = new Config(bundle);
        for (Enumeration e = properties.keys(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            config.setProperty(key, properties.get(key)); // TODO: Is this ok?
            // Clone value?
        }
        for (Iterator iter = references.iterator(); iter.hasNext();) {
            config.addReference(((Reference) iter.next()).copy());
        }
        for (DuplexReference ref : this.duplexReferences) {
            config.addDuplexReference(ref.copy());
        }
        for (Iterator iter = services.iterator(); iter.hasNext();) {
            config.addService((String) iter.next());
        }
        config.setAutoEnabled(autoEnabled);
        config.setImplementation(implementation);
        config.setName(name);
        config.setServiceFactory(serviceFactory);
        config.setFactory(factory);
        config.setImmediate(immediate);
        return config;
    }
}
