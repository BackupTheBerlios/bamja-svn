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

package org.bamja.core.interfaces;

/**
 * This meta data describe a required service of a component. A service is after
 * the definition of OSGi a Java interface.
 * 
 * @author Humberto Cervantes
 * @author Jens Kutzsche
 * @since Version: 0.1.0 (17.02.2006)
 */
public interface RequiredServiceMetadata {

    enum Policy {
        STATIC, STATIC_ONE_START, ADD_ONLY, REMOVE_ONLY, REMOVE_ONLY_ONE_START, DYNAMIC
    }

    /**
     * @return The name of the required service (interface).
     */
    String getServiceName();

    /**
     * @return The class of the required service (interface).
     */
    Class getServiceClass();

    /**
     * @return A string with the filter in LDAP syntax.
     */
    String getFilter();

    /**
     * @return A <tt>String</tt> with the name of the bind method.
     */
    String getBindMethodName();

    /**
     * @return A <tt>String</tt> with the name of the unbind method.
     */
    String getUnbindMethodName();

    /**
     * @return <tt>true</tt> if the dependency is static (policy ist static).
     */
    Policy getPolicy();

    /**
     * @return <tt>true</tt> if the dependency is optional (cardinality 0..1
     *         or 0..n).
     */
    boolean isOptional();

    /**
     * @return <tt>true</tt> if the dependency can be multiple (cardinality
     *         0..n or 1..n).
     */
    boolean isMultiple();

    /**
     * @return <tt>true</tt> if this required service should be deliver as a
     *         factory and <tt>false</tt> when should deliver as an instance.
     */
    boolean isNeedFactory();

    /**
     * @return <tt>true</tt> if the service required from the using component
     *         and <tt>false</tt> if it required from someone component.
     */
    boolean isRequiredFromUser();
}