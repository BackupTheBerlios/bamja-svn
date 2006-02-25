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
 * *************************************************
 * *************************************************
 * 
 * This file based on the work from Humberto Cervantes, which was licensed under
 * the BSD Software License and conains the follow informations:
 * 
 * Die Datei basiert auf der Arbeit von Humberto Cervantes, welche unter der 
 * BSD Lizenz veröffentlicht wurde und folgende Hinweise enthält:
 * 
 * -------------------------------------------------
 * ServiceBinder - A mechanism to automate service binding in OSGi
 * Copyright (C) 2002  Humberto Cervantes
 *
 * This program is licensed under the BSD Software License;
 * refer to the LICENSE.txt file included with this program for details.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * Contact: Humberto Cervantes (Humberto.Cervantes@imag.fr)
 * Contributor(s): Richard S. Hall (heavy@ungoverned.org)
 *
 **/

package org.bamja.core.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.bamja.core.annotations.Component;
import org.bamja.core.interfaces.ComponentMetadata;
import org.bamja.core.interfaces.RequiredServiceMetadata;

/**
 * This meta data describe a component which should manage by bamja.
 * 
 * @author Humberto Cervantes
 * @author Jens Kutzsche
 * @since Version: 0.1.0 (16.02.2006)
 */

public class ComponentMetadataImpl implements ComponentMetadata {
    private String componentClassName;

    private Class componentClass = null;

    private Component.InstantiationType instantiationType = Component.InstantiationType.SINGLETON;

    private Collection<String> providedServices = new ArrayList<String>();

    private Properties properties = new Properties();

    private Collection<RequiredServiceMetadata> requiredServices = new ArrayList<RequiredServiceMetadata>();
    
    private boolean startAutomatically = false;

    /**
     * @param componentName The name of the component class.
     * @param instantiationType The type for the instantiation of this component.
     */
    ComponentMetadataImpl(String componentName, String instantiationType, String startAutomatically) {
        this.componentClassName = componentName;
        if (instantiationType
                .equalsIgnoreCase(Component.InstantiationType.ONE_FOR_BUNDLE
                        .name())) {
            this.instantiationType = Component.InstantiationType.ONE_FOR_BUNDLE;
        }
        
        if (startAutomatically.equals("true")) {
            this.startAutomatically = true;
        }
    }

    /**
     * @param componentClass The <tt>Class</tt> of the component.
     * @param instantiationType The type for the instantiation of this component.
     */
    ComponentMetadataImpl(Class componentClass,
            Component.InstantiationType instantiationType, boolean startAutomatically) {
        this.componentClassName = componentClass.getName();
        this.componentClass = componentClass;
        this.instantiationType = instantiationType;
        this.startAutomatically = startAutomatically;
    }

    /**
     * Used to add an provided service to the component meta data.
     * 
     * @param newProvidedService The name of a provided service (interface)
     *            implemented by the component object
     */
    void addProvidedService(String newProvidedService) {
        this.providedServices.add(newProvidedService);
    }

    /**
     * Used to add a property to the component meta data.
     * 
     * @param newProperty A property to be added.
     */
    void addProperty(PropertyMetadata newProperty) {
        String key = newProperty.getName();
        Object value = newProperty.getValue();
        if (key != null && value != null) {
            this.properties.put(key, value);
        }
    }

    /**
     * Used to add the meta data of a new required service to the component
     * meta data.
     * 
     * @param newRequiredService The meta data of a new required to be added.
     */
    void addRequiredService(RequiredServiceMetadata newRequiredService) {
        this.requiredServices.add(newRequiredService);
    }

    public String getComponentClassName() {
        return this.componentClassName;
    }

    public Class getComponentClass() {
        return this.componentClass;
    }

    public String[] getProvidedServices() {
        if (this.providedServices.size() == 0) {
            return null;
        }
        String provided[] = new String[this.providedServices.size()];
        provided = this.providedServices.toArray(provided);
        return provided;
    }

    public Properties getProperties() {
        return this.properties;
    }

    public Collection<RequiredServiceMetadata> getRequiredServices() {
        return this.requiredServices;
    }

    public Component.InstantiationType getInstantiationType() {
        return this.instantiationType;
    }

    public boolean isStartAutomatically() {
        return this.startAutomatically;
    }

}
