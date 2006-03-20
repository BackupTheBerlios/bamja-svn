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
import java.lang.reflect.Modifier;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * 
 * @author Jens Kutzsche
 * @since Version: 0.1.0 (18.03.2006)
 */
public class DuplexReference {

    private boolean optional;

    private String bindMethodName;

    private String unbindMethodName;

    private String interfaceName;

    private Config config;

    private BundleContext context = null;

    private String filter = null;

    private ServiceReference bound = null;

    private boolean overrideUnsatisfied = false;

    public DuplexReference(String interfaceName, boolean optional,
            String bindMethodName, String unbindMethodName, BundleContext bc) {

        this.context = bc;
        this.optional = optional;
        this.bindMethodName = bindMethodName;
        this.unbindMethodName = unbindMethodName;
        this.interfaceName = interfaceName;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public boolean isOptional() {
        return this.optional;
    }

    public String getInterfaceName() {
        return this.interfaceName;
    }

    public DuplexReference copy() {
        return new DuplexReference(interfaceName, optional, bindMethodName,
                unbindMethodName, context);
    }

    public void bind(Object instance, Bundle bundle) {
        if (bundle == null) {
            return;
        }
        String filterString = "(&(" + org.osgi.framework.Constants.OBJECTCLASS
                + "=" + interfaceName + ")" + "(" + Constants.BUNDLE_ID + "="
                + bundle.getBundleId() + "))";
        try {
            Filter filter = this.context.createFilter(filterString);
            ServiceReference[] refs = this.context.getServiceReferences(null,
                    filter.toString());
            bound = refs[0];
            invokeEventMethod(instance, bindMethodName, bound);
        } catch (InvalidSyntaxException e) {
            Activator.log.error("Couldn't create filter for reference "
                    + filterString + "\" used by component \""
                    + config.getName() + "\". Got exception.", e);
        }
    }

    public void unbind(Object instance) {
        if (this.bound == null) {
            return;
        }
        invokeEventMethod(instance, unbindMethodName, bound);
        this.context.ungetService(bound);
    }

    private void invokeEventMethod(Object instance, String methodName,
            ServiceReference ref) {
        if (methodName == null) {
            return;
        }

        Class instanceClass = instance.getClass();
        Bundle bundle = ref.getBundle();

        Class serviceClass = null;
        try {
            serviceClass = bundle.loadClass(getInterfaceName());
        } catch (ClassNotFoundException e) {
            Activator.log.error("Declarative Services could not load class", e);
            return;
        }

        while (instanceClass != null) {
            Method[] ms = instanceClass.getDeclaredMethods();

            // searches this class for a suitable method.
            for (int i = 0; i < ms.length; i++) {
                if (methodName.equals(ms[i].getName())
                        && (Modifier.isProtected(ms[i].getModifiers()) || Modifier
                                .isPublic(ms[i].getModifiers()))) {

                    Class[] parms = ms[i].getParameterTypes();

                    if (parms.length == 1) {
                        try {
                            // if (ServiceReference.class.equals(parms[0])) {
                            // ms[i].setAccessible(true);
                            // ms[i].invoke(instance, new Object[] { ref });
                            // return;
                            // } else
                            if (parms[0].isAssignableFrom(serviceClass)) {
                                Object service = this.context.getService(ref);
                                ms[i].setAccessible(true);
                                ms[i]
                                        .invoke(instance,
                                                new Object[] { service });
                                return;
                            }
                        } catch (IllegalAccessException e) {
                            Activator.log.error(
                                    "Declarative Services could not access the method \""
                                            + methodName
                                            + "\" used by component \""
                                            + config.getName()
                                            + "\". Got exception.", e);
                        } catch (InvocationTargetException e) {
                            Activator.log.error(
                                    "Declarative Services got exception while invoking\""
                                            + methodName
                                            + "\" used by component \""
                                            + config.getName()
                                            + "\". Got exception.", e);
                        }
                    }
                }
            }

            instanceClass = instanceClass.getSuperclass();
        }
        // did not find any such method.
        Activator.log
                .error("Declarative Services could not find bind/unbind method \""
                        + methodName
                        + "\" in class \""
                        + config.getImplementation()
                        + "\" used by component "
                        + config.getName() + "\".");
    }
}
