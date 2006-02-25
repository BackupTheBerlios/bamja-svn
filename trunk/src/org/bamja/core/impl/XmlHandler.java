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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.bamja.core.interfaces.ComponentMetadata;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

/**
 * Simple handler for the component description in xml files and for the xml
 * schema of this files. Builds a list of <tt>ComponentMetadata</tt>.
 * 
 * @author Humberto Cervantes
 * @author Jens Kutzsche
 * @since Version: 0.1.0 (16.02.2006)
 */
public class XmlHandler {

    private ComponentMetadataImpl currentMetadata = null;

    private List<ComponentMetadataImpl> metadatas = new ArrayList<ComponentMetadataImpl>();

    /**
     * Builds the list of <tt>ComponentMetadata</tt> for each component in the
     * xml file. The xml file will be validate with the xml schema.
     * 
     * @param xmlStream The xml description file for a bundle.
     * @param schemaStream The xml schema of the description files.
     * @throws Exception
     */
    public XmlHandler(InputStream xmlStream, InputStream schemaStream)
            throws Exception {
        assert xmlStream != null;
        assert schemaStream != null;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlStream);

            SchemaFactory schemaFactory = SchemaFactory
                    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Source schemaFile = new StreamSource(schemaStream);
            Schema schema = schemaFactory.newSchema(schemaFile);
            Validator validator = schema.newValidator();
            validator.validate(new DOMSource(document));

            scanDOM(document.getDocumentElement());

        } catch (SAXParseException e) {
            BundleManager.error("Parse Error" + "\nZeile: " + e.getLineNumber()
                    + "\nDatei: " + e.getSystemId());
            BundleManager.error(e.getMessage() + "\n");
        } catch (IOException e) {
            BundleManager.error("IO-Fehler: " + e.getMessage());
        }

    }

    /**
     * Scans the DOM of the xml description file recursive and build the
     * corresponding list of <tt>ComponentMetadata</tt>.
     * 
     * @param node The document root element of the DOM.
     */
    private void scanDOM(Node node) {
        Element element = (Element) node;
        String name = node.getNodeName();
        if (name.equals("component")) {
            this.currentMetadata = new ComponentMetadataImpl(element
                    .getAttribute("class-name"), element
                    .getAttribute("instantiation-type"), element
                    .getAttribute("start-automatcally"));
            this.metadatas.add(this.currentMetadata);
        } else if (this.currentMetadata != null) {
            if (name.equals("provided-service")) {
                this.currentMetadata.addProvidedService(element
                        .getAttribute("service"));
            } else if (name.equals("property")) {
                PropertyMetadata prop = new PropertyMetadata(element
                        .getAttribute("name"), element.getAttribute("type"),
                        element.getAttribute("value"));
                this.currentMetadata.addProperty(prop);
            } else if (name.equals("required-service")) {
                RequiredServiceMetadataImpl reqMetadata = new RequiredServiceMetadataImpl(
                        element.getAttribute("service"), element
                                .getAttribute("cardinality"), element
                                .getAttribute("policy"), element
                                .getAttribute("filter"), element
                                .getAttribute("bind-method"), element
                                .getAttribute("unbind-method"), element
                                .getAttribute("factory"), element
                                .getAttribute("required-from-user"));
                this.currentMetadata.addRequiredService(reqMetadata);
            }
        }

        NodeList childs = node.getChildNodes();
        for (int i = 0, count = childs.getLength(); i < count; i++) {
            Node childNode = childs.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                scanDOM(childNode);
            }
        }
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
