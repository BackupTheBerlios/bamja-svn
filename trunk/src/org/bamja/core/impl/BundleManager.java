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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.bamja.core.GenericActivator;
import org.bamja.core.interfaces.ComponentMetadata;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

/**
 * It will manage the bundle with all its components. It will read the
 * configuration from the annotaions or from metadata.xml file (this jobs
 * delegate the <tt>BundleManager</tt> to special Klasses) and will create the
 * corresponding <tt>ComponentManager</tt> for each component.
 * 
 * @author Humberto Cervantes
 * @author Jens Kutzsche
 * @since Version: 0.1.0 (16.02.2006)
 */
public final class BundleManager {
    private BundleContext context = null;

    private GenericActivator activator = null;

    private List<ComponentManager> componentManagers = new ArrayList<ComponentManager>();

    private static URL schemaURL = null;

    private static boolean trace = false;

    private static boolean error = true;

    private static final String METADATA_LOCATION = "Metadata-Location";

    private static final String SCHEMA_NAME = "metadata.xsd";

    private static final String VERSION = "0.1.0 (20060218)";

    /**
     * Static initializations based on system properties. Get system properties
     * to see if traces, errors, or the version need to be displayed.
     */
    static {
        String result = System.getProperty("org.bamja.showtrace");
        if (result != null && result.equals("true")) {
            BundleManager.trace = true;
        }
        result = System.getProperty("org.bamja.showerrors");
        if (result != null && result.equals("false")) {
            BundleManager.error = false;
        }
        result = System.getProperty("org.bamja.showversion");
        if (result != null && result.equals("true")) {
            System.out.println("[ You use the follow version of bamja: "
                    + VERSION + " ]\n");
        }
    }

    /**
     * Starting the management of the bundle. This method arranges reading the
     * configuration and invokes initialize().
     * 
     * @param context The context of the bundle which manages from this
     *            <tt>BundleManager</tt>.
     * @param activator The activator of the bundle which manages from this
     *            <tt>BundleManager</tt>.
     * @exception Exception Any exception thrown during starting the management
     *                of the bundle.
     */
    public void startManage(BundleContext context, GenericActivator activator)
            throws Exception {
        assert context != null;
        assert activator != null;

        this.context = context;
        this.activator = activator;

        if (((String) this.context.getBundle().getHeaders().get(
                Constants.BUNDLE_NAME)).equalsIgnoreCase("bamja")) {
            // coreBundleID = this.context.getBundle().getBundleId();
            schemaURL = this.context.getBundle().getResource(SCHEMA_NAME);
            assert schemaURL != null : new java.io.FileNotFoundException(
                    "Schema of the metadata.xml not found under " + SCHEMA_NAME);
        }

        try {
            List<? extends ComponentMetadata> componentMetadatas;
            if (this.activator.getClass().getAnnotations().length != 0) {
                componentMetadatas = parseAnnotations();
            } else {
                componentMetadatas = parseXML();
            }
            initialize(componentMetadatas);
        } catch (Exception e) {
            BundleManager.error("BundleManager : in bundle ["
                    + context.getBundle().getBundleId() + "] : " + e);
            e.printStackTrace();
            throw e;
        }
    }

    private List<? extends ComponentMetadata> parseXML() throws Exception {
        BundleManager.trace("Read meta data from XML!");

        // Get the location of the metadata.xml value from the manifest
        String metadataLocation = (String) this.context.getBundle()
                .getHeaders().get(BundleManager.METADATA_LOCATION);

        if (metadataLocation == null) {
            throw new FileNotFoundException(
                    "Metadata-Location entry not found in the manifest");
        }

        if (metadataLocation.startsWith("/") == false) {
            metadataLocation = "/" + metadataLocation;
        }

        InputStream stream = this.context.getBundle().getResource(
                metadataLocation).openStream();
        if (stream == null) {
            throw new java.io.FileNotFoundException(
                    "Meta data file not found at:" + metadataLocation);
        }

        // Bundle bundle = this.context.getBundle(coreBundleID);
        // if (bundle == null) {
        // throw new ServiceBinderException("Bundle \"Bamja\", with ID "
        // + coreBundleID + " not found");
        // }

        // InputStream schema = bundle.getResource("metadata.xsd").openStream();
        InputStream schema = schemaURL.openStream();
        assert schema != null : new java.io.FileNotFoundException(
                "Meta data schema not found at: " + schemaURL);

        XmlHandler handler = new XmlHandler(stream, schema);
        return handler.getComponentMetadatas();
    }

    private List<? extends ComponentMetadata> parseAnnotations() {
        BundleManager.trace("Read meta data from annotations!");
        AnnotationHandler handler = new AnnotationHandler(this.activator);
        return handler.getComponentMetadatas();
    }

    private void initialize(List<? extends ComponentMetadata> componentMetadatas)
            throws Exception {
        for (ComponentMetadata metadata : componentMetadatas) {
            ComponentManager componentManager = new ComponentManager(this.context,
                    metadata);
            this.componentManagers.add(componentManager);
        }
    }
    
    public boolean startComponent(Class componentClass) {
        for(ComponentManager manager : this.componentManagers) {
            if (manager.getComponentClass() == componentClass) {
                return manager.startComponent();
            }
        }
        return false;
    }

    /**
     * Stopping the management of the bundle. This method arranges destroying
     * all the component managers.
     * 
     */
    public void stopManage(){
        BundleManager.trace("BundleManager : Bundle ["
                + this.context.getBundle().getBundleId() + "] will destroy "
                + this.componentManagers.size() + " components");

        for (ComponentManager componentManager : this.componentManagers) {
            componentManager.determine();
        }

        this.componentManagers = null;
        this.activator = null;

        BundleManager.trace("BundleManager : Bundle ["
                + this.context.getBundle().getBundleId() + "] STOPPED");
        
        this.context = null;
    }

    /**
     * @return The context of the bundle which manages from this
     *         <tt>BundleManager</tt>.
     */
    public BundleContext getBundleContext() {
        return this.context;
    }

    /*
     * Returns the list of instance references currently associated to this
     * activator
     * 
     * @return the list of instance references
     */
    // public List getInstanceReferences() {
    // return this.instanceManagers;
    // }
    /**
     * Add an <tt>ComponentManager</tt> to the list of all component managers.
     * 
     * @param componentManager The <tt>ComponentManager</tt> for adding.
     */
    // synchronized void addComponentManager(ComponentManager componentManager)
    // {
    // this.componentManagers.add(componentManager);
    // }
    /**
     * Removes a <tt>ComponentManager</tt> instance from the list of all
     * managed component managers.
     * 
     * @param componentManager The <tt>ComponentManager</tt> for removing.
     */
    // synchronized void removeComponentManager(ComponentManager
    // componentManager) {
    // this.componentManagers.remove(componentManager);
    // }
    // InstanceReference findInstanceReference(Object obj) {
    // Object[] refs = this.instanceManagers.toArray();
    //
    // for (int i = 0; i < refs.length; i++) {
    // InstanceReference current = (InstanceReference) refs[i];
    // if (current.getObject() == obj) {
    // return current;
    // }
    // }
    // return null;
    // }
    /**
     * Method to display trace messages.
     * 
     * @param s The trace message to be displayed.
     */
    public static void trace(String s) {
        if (BundleManager.trace) {
            System.out.println("--- " + s);
        }
    }

    /**
     * Method to display error messages.
     * 
     * @param s The error message to be displayed.
     */
    public static void error(String s) {
        if (BundleManager.error) {
            System.err.println("### " + s);
        }
    }

    /**
     * @return Returns The activator of the bundle which manages from this
     *         <tt>BundleManager</tt>.
     */
    public GenericActivator getActivator() {
        return this.activator;
    }
}
