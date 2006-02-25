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
 */
package org.bamja.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A required service of a component.
 * 
 * @author Jens Kutzsche
 * @since Version: 0.1.0 (16.02.2006)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiredService {
    /**
     * The supportet policys for requirement handling.
     * 
     * @author Jens Kutzsche
     * @since Version: 0.2.0 (16.02.2006)
     */
    enum Policy {
        STATIC, STATIC_ONE_START, ADD_ONLY, REMOVE_ONLY, REMOVE_ONLY_ONE_START, DYNAMIC
    }

    /**
     * @return The <tt>Class</tt> of a required service (of an interface).
     */
    Class service();

    /**
     * @return A filter in LDAP syntax to narrow the search.
     */
    String filter() default "";

    /**
     * @return <tt>true</tt> if this required service is optional and
     *         <tt>false</tt> he is obligatory.
     */
    boolean optional() default false;

    /**
     * @return <tt>true</tt> if this required service can be multiple and
     *         <tt>false</tt> he isn't it.
     */
    boolean multiple() default false;

    /**
     * @return The policy for requirement handling.
     */
    Policy policy() default Policy.STATIC;

    /**
     * @return The name of the methode in the component, which should be invoked
     *         for bind the required service.
     */
    String bindMethod();

    /**
     * @return The name of the methode in the component, which should be invoked
     *         for unbind the required service.
     */
    String unbindMethod();

    /**
     * @return <tt>true</tt> if this required service should be deliver as a
     *         factory and <tt>false</tt> when should deliver as an instance.
     */
    boolean needFactory() default true;

    /**
     * For deliver a services, the component required this service from the
     * using component.
     * 
     * @return <tt>true</tt> if the service required from the using component
     *         and <tt>false</tt> if it required from someone component.
     */
    boolean requiredFromUser() default false;
}