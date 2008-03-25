/*
 * DataProcessing.java -- TODO Insert a short describtion of this file.
 * Copyright (C) 2007 Jens Kutzsche
 * 
 * This file is part of the Reap Project.
 *
 * Reap is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as published by
 * the Free Software Foundation.
 *
 * Reap is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Reap; see the file LICENSE.txt.  If not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 *
 * Linking this code statically or dynamically with other modules, or use 
 * the offered extension points, is making a combined work based on this code.
 * Thus, the terms and conditions of the GNU General Public License cover 
 * the whole combination.
 *
 * As a special exception, the copyright holders of this code give you 
 * permission to link this code with independent modules and use the extension 
 * points, provided the licenses of these independent modules are admitted by 
 * the Open Source Initiative (OSI). You has the permission to copy and 
 * distribute the resulting combined work under terms of a licens admitted 
 * by the OSI too. Another condition is that you also meet, for each 
 * linked independent module, the terms and conditions of the license of 
 * that module. 
 * An independent module is a module which is not derived from or based on 
 * this library. If you modify this library, you may extend this exception 
 * to your version of the library, but you are not obligated to do so. If 
 * you do not wish to do so, delete this exception statement from your 
 * version.
 */
package org.reap.internal.core;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.observable.value.ComputedValue;

public class DataProcessing {

	public class DataProcessingField {

		private final boolean	ignore;
		private final String	name;
		private final String	target;
		private final String	targetSubEntity;

		public DataProcessingField(final String name, final boolean ignore,
				final String targetSubEntity, final String target) {
			this.name = name;
			this.ignore = ignore;
			this.targetSubEntity = targetSubEntity;
			this.target = target;
		}

		/**
		 * @return the name
		 */
		public final String getName() {
			return name;
		}

		/**
		 * @return the target
		 */
		public final String getTarget() {
			return target;
		}

		/**
		 * @return the targetSubEntity
		 */
		public final String getTargetSubEntity() {
			return targetSubEntity;
		}

		/**
		 * @return the parseBoolean
		 */
		public final boolean isIgnore() {
			return ignore;
		}
	}

	private final Class<? extends ComputedValue>	dataProcessingClass;
	private final String							id;
	private final boolean							notForGUI;
	private final Map<String, DataProcessingField>	processingFields	= new HashMap<String, DataProcessingField>();
	private final Map<String, PropertyDescriptor>	properties			= new HashMap<String, PropertyDescriptor>();
	private final String							targetEntity;

	public DataProcessing(final String id,
			final Class<? extends ComputedValue> dataProcessingClass,
			final String targetEntity, final boolean notForGUI)
			throws IntrospectionException {
		this.id = id;
		this.dataProcessingClass = dataProcessingClass;
		this.targetEntity = targetEntity;
		this.notForGUI = notForGUI;

		final BeanInfo beanInfo = Introspector.getBeanInfo(
				dataProcessingClass,
				ComputedValue.class);
		final PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
		for (final PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			properties.put(propertyDescriptor.getName(), propertyDescriptor);
		}
	}

	public final void addProcessingField(
			final DataProcessingField processingField) {
		processingFields.put(processingField.getName(), processingField);
	}

	/**
	 * @return the id
	 */
	public final String getId() {
		return id;
	}

	/**
	 * @return the processingFields
	 */
	public final DataProcessingField getProcessingField(final String name) {
		return processingFields.get(name);
	}

	/**
	 * @return the properties
	 */
	public final Collection<PropertyDescriptor> getProperties() {
		return properties.values();
	}

	/**
	 * @return the targetEntity
	 */
	public final String getTargetEntity() {
		return targetEntity;
	}

	/**
	 * @return the notForGUI
	 */
	public final boolean isNotForGUI() {
		return notForGUI;
	}

	public final ComputedValue newInstance() throws InstantiationException,
			IllegalAccessException {
		return dataProcessingClass.newInstance();
	}
}
