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

import org.bamja.core.GenericActivator;
import org.bamja.core.annotations.Component;
import org.bamja.core.annotations.Components;
import org.bamja.core.annotations.NoneComponent;
import org.bamja.core.annotations.Properties;
import org.bamja.core.annotations.Property;
import org.bamja.core.annotations.ProvidedService;
import org.bamja.core.annotations.ProvidedServices;
import org.bamja.core.annotations.RequiredService;
import org.bamja.core.annotations.RequiredServices;
import org.bamja.core.interfaces.ComponentMetadata;

/**
 * Simple handler for the component description in the annotations of the
 * activator and the component classes. Builds a list of
 * <tt>ComponentMetadata</tt>.
 * 
 * @author Jens Kutzsche
 * @since Version: 0.1.0 (16.02.2006)
 */
public class AnnotationHandler {

    private ComponentMetadataImpl currentMetadata = null;

    private List<ComponentMetadataImpl> metadatas = new ArrayList<ComponentMetadataImpl>();

    /**
     * Builds the list of <tt>ComponentMetadata</tt> for each component if
     * specified by the annotations.
     * 
     * @param activator The GenericActivator as the superclass of the bundle
     *            activator, wich contains the annotations.
     */
    public AnnotationHandler(GenericActivator activator) {
        assert activator != null;

        Class<?> c = activator.getClass();

        if (c.isAnnotationPresent(Components.class)) {
            Component[] components = c.getAnnotation(Components.class).value();
            for (Component component : components) {
                scanComponent(component);
            }
        } else if (c.isAnnotationPresent(Component.class)) {
            scanComponent(c.getAnnotation(Component.class));
        } else if (c.isAnnotationPresent(NoneComponent.class)) {
        }
    }

    /**
     * Scans the components are specified in the bundle activator class and
     * build the corresponding list of <tt>ComponentMetadata</tt>.
     * 
     * @param component A <tt>Component</tt> annotation for one component.
     */
    private void scanComponent(Component component) {

        Class<?> c = component.className();
        this.currentMetadata = new ComponentMetadataImpl(c, component
                .instantiationType(), component.startAutomatically());
        this.metadatas.add(this.currentMetadata);

        if (c.isAnnotationPresent(Properties.class)) {
            Property[] properties = c.getAnnotation(Properties.class).value();
            for (Property property : properties) {
                workProperties(property);
            }
        } else if (c.isAnnotationPresent(Property.class)) {
            workProperties(c.getAnnotation(Property.class));
        }

        if (c.isAnnotationPresent(ProvidedServices.class)) {
            ProvidedService[] provided = c
                    .getAnnotation(ProvidedServices.class).value();
            for (ProvidedService prov : provided) {
                this.currentMetadata.addProvidedService(prov.service()
                        .getName());
            }
        } else if (c.isAnnotationPresent(ProvidedService.class)) {
            ProvidedService provided = c.getAnnotation(ProvidedService.class);
            this.currentMetadata.addProvidedService(provided.service()
                    .getName());
        }

        if (c.isAnnotationPresent(RequiredServices.class)) {
            RequiredService[] required = c
                    .getAnnotation(RequiredServices.class).value();
            for (RequiredService req : required) {
                workRequiredService(req);
            }
        } else if (c.isAnnotationPresent(RequiredService.class)) {
            workRequiredService(c.getAnnotation(RequiredService.class));
        }
    }

    /**
     * Create a <tt>PropertyMetadata</tt> for a property.
     * 
     * @param property A <tt>Property</tt> annotation for one property
     *            specified in the component class.
     */
    private void workProperties(Property property) {
        PropertyMetadata propMetadata = new PropertyMetadata(property.name(),
                property.type().name(), property.value());
        this.currentMetadata.addProperty(propMetadata);
    }

    /**
     * Create a <tt>RequiredServiceMetadata</tt> for a required service.
     * 
     * @param required A <tt>RequiredService</tt> annotation for one required
     *            service specified in the component class.
     */
    private void workRequiredService(RequiredService required) {
        RequiredServiceMetadataImpl reqMetadata = new RequiredServiceMetadataImpl(
                required.service(), required.optional(), required.multiple(),
                required.policy(), required.filter(), required
                        .bindMethod(), required.unbindMethod(), required
                        .needFactory(), required.requiredFromUser());
        this.currentMetadata.addRequiredService(reqMetadata);
    }

    /**
     * Called to retrieve the meta datas of the components.
     * 
     * @return A list of component descriptors.
     */
    List<? extends ComponentMetadata> getComponentMetadatas() {
        return this.metadatas;
    }
}
