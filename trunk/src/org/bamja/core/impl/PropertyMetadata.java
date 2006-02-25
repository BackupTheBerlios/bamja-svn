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

/**
 * A property descriptor that contains the information for properties defined
 * for a component. Transform string values in corresponding wrapper objects.
 * 
 * @author Humberto Cervantes
 * @author Jens Kutzsche
 * @since Version: 0.1.0 (17.02.2006)
 */
public class PropertyMetadata {
    private String name;

    private String type;

    private Object value;

    /**
     * Create a <tt>PropertyMetadata</tt> object and pack the value in a corresponding
     * wrapper object.
     * 
     * @param name The name of the property.
     * @param type The type of the property (string, boolean, byte, char, short,
     *            int, long, float or double).
     * @param value The value of the property.
     */
    public PropertyMetadata(String name, String type, String value) {
        assert name != null;
        assert type != null;
        assert value != null;
        
        this.name = name;
        this.type = type.toLowerCase();
        this.value = null;

        if (this.type.equals("string")) {
            this.value = new String(value);
        } else if (this.type.equals("boolean")) {
            this.value = new Boolean(value);
        } else if (this.type.equals("byte")) {
            this.value = new Byte(value);
        } else if (this.type.equals("char")) {
            this.value = new Byte(value);
        } else if (this.type.equals("short")) {
            this.value = new Short(value);
        } else if (this.type.equals("int")) {
            this.value = new Integer(value);
        } else if (this.type.equals("long")) {
            this.value = new Long(value);
        } else if (this.type.equals("float")) {
            this.value = new Float(value);
        } else if (this.type.equals("double")) {
            this.value = new Double(value);
        }
    }

    /**
     * @return The name of the property.
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return The type of the property as string.
     */
    public String getType() {
        return this.type;
    }

    /**
     * @return The value of the property as an Object.
     */
    public Object getValue() {
        return this.value;
    }

}
