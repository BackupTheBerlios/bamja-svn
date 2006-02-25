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

package org.bamja.core;

import org.bamja.core.impl.BundleManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The <tt>GenericActivator</tt> is the default implementation of the <tt>BundleActivator</tt>
 * interface. It starts the management of the bundle and at the end, it starts
 * the terminate of it.
 * <p>
 * Der <tt>GenericActivator</tt> ist die Standardimplementierung vom Interface
 * <tt>BundleActivator</tt>. Es startet das Managment des Bundles und startet am Ende
 * desen Beendung.
 * 
 * @author Humberto Cervantes
 * @author Jens Kutzsche
 * @since Version: 0.1.0 (15.02.2006)
 */
public abstract class GenericActivator implements BundleActivator {

    private BundleContext context = null;

    private BundleManager bundleManager = null;

    /**
     * Called upon starting of the bundle from the framework. It's invoke
     * <tt>startManage()</tt> of the <tt>BundleManager</tt> for starting the management of this
     * bundle.
     * <p>
     * Wird vom Framework aufgerufen, wenn das Bundle gestartet wird. Es ruft
     * <tt>startManage()</tt> vom <tt>BandleManager</tt> auf, um die Verwaltung des Bundles zu
     * starten.
     * 
     * @param context The context of this bundle passed by the framework.
     * @exception Exception Any exception thrown during starting the bundle.
     */
    public void start(BundleContext context) throws Exception {
        assert context != null;
        
        this.context = context;
        this.bundleManager = new BundleManager();
        this.bundleManager.startManage(context, this);
    }

    /**
     * Called upon stoping of the bundle from the framework. It's invoke
     * <tt>stopManage()</tt> of the <tt>BundleManager</tt> for terminate all services.
     * <p>
     * Wird vom Framework aufgerufen, wenn das Bundle gestoppt wird. Es ruft
     * <tt>stopManage()</tt> des <tt>BundleManager</tt> auf, um alle Dienste zu beenden.
     * 
     * @param context The context of this bundle passed by the framework
     * @exception Exception Any exception thrown during terminate the bundle.
     */
    public void stop(BundleContext context) throws java.lang.Exception {
        assert this.context == context;
        
        this.bundleManager.stopManage();
    }

    /**
     * Returns the BundleContext of this bundle.
     * 
     * @return The BundleContext of this bundle.
     */
    protected BundleContext getBundleContext() {
        return this.context;
    }
    
    protected boolean startComponent(Class componentClass) {
        return this.bundleManager.startComponent(componentClass);
    }
}