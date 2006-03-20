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

/* This class works more or less like the org.osgi.framework.util.ServiceTracker
 * except that it adds a few extra "events". The code is partly based on the current
 * a in-house modification of the current osgi ServiceTracker.
 */
package org.bamja.core.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

class ExtendedServiceTracker implements ServiceListener {
    /* the filter */
    protected Filter filter;

    /* the current bundle context */
    protected BundleContext context;

    /* all tracked service references */
    private ArrayList tracking = new ArrayList();

    /* all service objects */
    private HashMap objects = new HashMap();

    protected Config config;

    ExtendedServiceTracker(BundleContext context, Filter filter) {
        this.context = context;
        this.filter = filter;

    }

    void open() {
        try {
            ServiceReference[] refs = context.getServiceReferences(null, filter
                    .toString());
            context.addServiceListener(this, filter.toString());
            if (refs == null) {
                return;
            }

            for (int i = 0; i < refs.length; i++) {
                serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED,
                        refs[i]));
            }
        } catch (InvalidSyntaxException e) {
            /*
             * this can not happen, since we could have created the filter in
             * that case
             */
        }
    }

    void close() {
        context.removeServiceListener(this);
        try {
            synchronized (tracking) {
                for (Iterator iter = objects.keySet().iterator(); iter
                        .hasNext();) {
                    context.ungetService((ServiceReference) iter.next());
                }
            }
        } catch (IllegalStateException e) { /* ignored. */
        }

        tracking = new ArrayList();
        objects = new HashMap();
    }

    public void serviceChanged(ServiceEvent event) {
        ServiceReference ref = event.getServiceReference();

        ArrayList<DuplexReference> duplexRefList = (ArrayList) ref
                .getProperty(org.bamja.core.impl.Constants.DUPLEX_REFERENCE);
        if (duplexRefList != null) {
            Class implClass = this.config.getImplementationClass();
            ArrayList<String> classes = new ArrayList<String>();
            for (Class superClass = implClass; superClass != null; superClass = superClass
                    .getSuperclass()) {
                classes.add(superClass.getName());
            }
            for (Class c : implClass.getInterfaces()) {
                classes.add(c.getName());
            }

            for (DuplexReference duplexRef : duplexRefList) {
                if (!classes.contains(duplexRef.getInterfaceName())
                        && !duplexRef.isOptional()) {
                    return;
                }
            }
        }

        switch (event.getType()) {
            case ServiceEvent.REGISTERED: {
                Object cache = null;
                try {
                    cache = addingService(ref);
                } catch (Throwable e) {
                }

                includeService(ref, cache);
                addedService(ref, cache);
                // Should be safe to throw the exception

            }
                break;

            case ServiceEvent.UNREGISTERING: {
                synchronized (tracking) {
                    if (!tracking.contains(ref)) {
                        // in this case we have removed this reference from the
                        // tracker.
                        return;
                    }
                }

                Object object = objects.get(ref);

                try {
                    removingService(ref, object);
                } catch (Throwable ignored) {
                }

                excludeService(ref);
                removedService(ref, object);

            }
        }
    }

    protected Object addingService(ServiceReference ref) {
        // return context.getService(ref);
        return null;
    }

    protected void addedService(ServiceReference ref, Object service) {

    }

    protected void removingService(ServiceReference ref, Object service) {

    }

    protected void removedService(ServiceReference ref, Object object) {

    }

    private void excludeService(ServiceReference ref) {
        synchronized (tracking) {
            if (tracking.remove(ref)) {
                objects.remove(ref);
            }
        }
    }

    private void includeService(ServiceReference ref, Object cached) {
        synchronized (tracking) {
            if (tracking.isEmpty()) {
                tracking.add(0, ref);
                if (cached != null) {
                    objects.put(ref, cached);
                }
                return;
            }

            Object tmp = ref.getProperty(Constants.SERVICE_RANKING);
            int refInt = (tmp instanceof Integer) ? ((Integer) tmp).intValue()
                    : 0;
            Long refId = (Long) ref.getProperty(Constants.SERVICE_ID);

            int i = 0;
            for (int n = tracking.size(); i < n; i++) {
                ServiceReference challenger = (ServiceReference) tracking
                        .get(i);
                tmp = challenger.getProperty(Constants.SERVICE_RANKING);
                int challengerInt = (tmp instanceof Integer) ? ((Integer) tmp)
                        .intValue() : 0;

                if (challengerInt < refInt) {
                    break;
                } else if (challengerInt == refInt) {
                    Long challengerId = (Long) challenger
                            .getProperty(Constants.SERVICE_ID);
                    if (refId.compareTo(challengerId) < 0) {
                        break;
                    }
                }
            }
            tracking.add(i, ref);
            objects.put(ref, cached);
        }
    }

    void ungetService(ServiceReference ref) {
        synchronized (tracking) {
            objects.remove(ref);
            context.ungetService(ref);
        }
    }

    Object getService() {
        synchronized (tracking) {
            if (tracking.isEmpty()) {
                return null;
            }

            return getService((ServiceReference) tracking.get(0));
        }
    }

    Object getService(ServiceReference ref) {
        synchronized (tracking) {
            if (objects.containsKey(ref)) {
                return objects.get(ref);
            } else {
                Object o = context.getService(ref);
                if (o != null) {
                    objects.put(ref, o);
                    return o;
                }
            }
        }
        return null;
    }

    ServiceReference getServiceReference() {
        synchronized (tracking) {
            return (ServiceReference) (tracking.isEmpty() ? null : tracking
                    .get(0));
        }
    }

    ServiceReference[] getServiceReferences() {
        synchronized (tracking) {
            int n = tracking.size();
            ServiceReference[] refs = new ServiceReference[n];

            for (int i = 0; i < n; i++) {
                refs[i] = (ServiceReference) tracking.get(i);
            }

            return refs;
        }
    }

    Object[] getServices() {
        synchronized (tracking) {
            if (tracking.isEmpty()) {
                return null;
            }

            ArrayList retval = new ArrayList();

            int i = 0;
            for (Iterator iter = tracking.iterator(); iter.hasNext(); i++) {
                ServiceReference ref = (ServiceReference) iter.next();
                if (!objects.containsKey(ref)) {
                    retval.add(i, objects.get(ref));

                } else {
                    Object o = context.getService(ref);

                    if (o != null) {
                        objects.put(ref, o);
                        retval.add(i, o);
                    }
                }
            }

            return retval.toArray(new Object[0]);
        }
    }

    void remove(ServiceReference ref) {
        serviceChanged(new ServiceEvent(ServiceEvent.UNREGISTERING, ref));
    }

    int size() {
        return tracking.size();
    }

    public void setConfig(Config config) {
        this.config = config;
    }
}
