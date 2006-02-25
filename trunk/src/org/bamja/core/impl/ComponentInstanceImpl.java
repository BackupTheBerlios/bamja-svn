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
 * Dieses Programm ist freie Software. Sie k�nnen es unter den Bedingungen der
 * GNU General Public License, wie von der Free Software Foundation
 * ver�ffentlicht, weitergeben und/oder modifizieren, entweder gem�� Version 2
 * der Lizenz oder jeder sp�teren Version.
 * 
 * Die Ver�ffentlichung dieses Programms erfolgt in der Hoffnung, da� es Ihnen
 * von Nutzen sein wird, aber OHNE IRGENDEINE GARANTIE, sogar ohne die implizite
 * Garantie der MARKTREIFE oder der VERWENDBARKEIT F�R EINEN BESTIMMTEN ZWECK.
 * Details finden Sie in der GNU General Public License.
 * 
 * Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
 * Programm erhalten haben. Falls nicht, schreiben Sie an die Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA. 
 * Sie finden den Lizenztext in der Datei LICENSE.txt und im Internet:
 * Englisches Orginal: http://www.fsf.org/licenses/gpl.html 
 * Deutsche �bersetzung: http://www.gnu.de/gpl-ger.html
 * 
 * Kontakt: Jens Kutzsche (genty@users.berlios.de)
 *
 **/
package org.bamja.core.impl;

import java.lang.reflect.Constructor;

import org.bamja.core.interfaces.ComponentContext;
import org.bamja.core.interfaces.ComponentInstance;
import org.bamja.core.interfaces.ComponentMetadata;
import org.bamja.core.interfaces.ComponentState;
import org.bamja.core.interfaces.RequiredService;
import org.osgi.framework.BundleContext;

/**
 * @author Jens Kutzsche
 * @since Version: 0.1.0 (18.02.2006)
 */
public class ComponentInstanceImpl implements ComponentInstance {

    private ComponentManager manager = null;

    private ComponentContext compContext = null;

    private Object instanceObject = null;

    public ComponentInstanceImpl(ComponentManager manager) throws Exception {
        assert manager != null;

        this.manager = manager;

        this.compContext = new ComponentContextImpl(this);

        try {
            Constructor constructor = this.manager.getComponentClass()
                    .getConstructor(new Class[] { ComponentContext.class });
            this.instanceObject = constructor.newInstance(this.compContext);
        } catch (NoSuchMethodException e) {
            this.instanceObject = this.manager.getComponentClass()
                    .newInstance();
        }
    }

    public static boolean checkComponentInstance(ComponentManager manager) {
        try {
            manager.getComponentClass().getConstructor(
                    new Class[] { ComponentContext.class });
        } catch (NoSuchMethodException e) {
            try {
                manager.getComponentClass().getConstructor(new Class[] {});
            } catch (NoSuchMethodException e1) {
                BundleManager
                        .error("ComponentInstanceIml: The required constructor of the component class can't find:"
                                + e);
                return false;
            }
        }
        return true;
    }

    public ComponentState getState() {
        return this.manager.getState();
    }

    public long getBundleID() {
        return this.manager.getBundleContext().getBundle().getBundleId();
    }

    public ComponentMetadata getComponentMetadata() {
        return this.manager.getComponentMetadata();
    }

    public RequiredService[] getRequiredServices() {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getProperty(String name) {
        return this.getComponentMetadata().getProperties().get(name);
    }

    public Object getInstanceObject() {
        return this.instanceObject;
    }

    /**
     * This class implements the <tt>ComponentContext</tt>, which cannot be
     * directly implemented by the activator, because of the
     * getInstanceReference() method.
     */
    private class ComponentContextImpl implements ComponentContext {

        private ComponentInstance bindComponent;

        ComponentContextImpl(ComponentInstance bindComponent) {
            this.bindComponent = bindComponent;
        }

        public BundleContext getBundleContext() {
            return ComponentInstanceImpl.this.manager.getBundleContext();
        }

        public ComponentInstance getComponentInstance() {
            return this.bindComponent;
        }
    }

}
