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
 **/
package org.bamja.core.impl;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

/**
 * @author Jens Kutzsche
 * @since Version: 0.1.0 (27.03.2006)
 */
public class DuplexFactoryComponentImpl extends Component implements
        DuplexFactoryComponent {

    private HashMap<Object, Component> duplexObjectMap = new HashMap<Object, Component>();

    private int useCount = 0;

    public DuplexFactoryComponentImpl(Config config, Dictionary overriddenProps) {
        super(config, overriddenProps);

        String[] interfaces = config.getServices();
        if (interfaces != null) {
            setProperty(Constants.OBJECT_CLASS, interfaces);
        }

        ArrayList<DuplexReference> duplexRefList = this.config
                .getDuplexReferences();
        if (duplexRefList != null && duplexRefList.size() != 0)
            setProperty(Constants.DUPLEX_REFERENCES, duplexRefList);
    }

    @Override
    public void registerService() {
        if (Activator.log.doDebug()) {
            Activator.log
                    .debug("DuplexFactoryComponentImpl.registerService() got BundleContext: "
                            + bundleContext);
        }

        String[] interfaces = config.getServices();
        if (interfaces == null) {
            return;
        }

        serviceRegistration = Activator.bc.registerService(
                DuplexFactoryComponent.class.getName(), this, config
                        .getProperties());
    }

    @Override
    public void satisfied() {
        registerService();
    }

    @Override
    public void unsatisfied() {
        unregisterService();
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration reg) {
        super.getService(bundle, reg);
        return this;
    }

    @Override
    public void ungetService(Bundle usingBundle, ServiceRegistration reg,
            Object obj) {
        super.ungetService(usingBundle, reg, obj);
    }

    public Object getInstance(Object duplexObject) {
        Component component = this.duplexObjectMap.get(duplexObject);
        if (component == null) {
            Config copy = config.copy();
            copy.setDuplexFactory(false);
            copy.setShouldRegisterService(false);
            component = copy.createComponent();
            component.setDuplexObject(duplexObject);
            component.enable();
            this.duplexObjectMap.put(duplexObject, component);
        }
        return component.getService(null, serviceRegistration);
    }

    public void ungetInstance(Object duplexObject) {
        Component component = this.duplexObjectMap.remove(duplexObject);
        if (component != null) {
            component.deactivate();
            component.disable();
        }
    }
}