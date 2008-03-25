/*
 * ModelManager.java -- TODO Insert a short describtion of this file. 
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

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.reap.internal.core.DataProcessing.DataProcessingField;

public class ModelManager {

	public class ModelAccess {

		public final ModelClass get(final String entity) {
			return model.get(entity);
		}

		public final List<DataProcessing> getDataProcessings(
				final String targetEntity) {
			final List<DataProcessing> retList = new ArrayList<DataProcessing>();
			List<DataProcessing> list = dataProcessings.get(targetEntity);
			if (list != null) {
				retList.addAll(list);
			}
			final ArrayList<String> entityList = new ArrayList<String>();
			model.get(targetEntity).getSubEntities(entityList);
			for (final String entity : entityList) {
				list = dataProcessings.get(entity);
				if (list != null) {
					retList.addAll(list);
				}
			}
			return retList;
		}

		public final Collection<ModelClass> getValues() {
			return model.values();
		}

		public final boolean isEntityAvailable(final String entity) {
			return model.containsKey(entity);
		}
	}

	private final Map<String, List<DataProcessing>>	dataProcessings	= new HashMap<String, List<DataProcessing>>();

	private final Map<String, ModelClass>			model			= new HashMap<String, ModelClass>();

	public final ModelAccess init(final IExtensionRegistry reg)
			throws InvalidRegistryObjectException {
		initModel(reg);
		initDataProcesse(reg);

		return new ModelAccess();
	}

	public final void stop() {
		model.clear();
		dataProcessings.clear();
	}

	private void createReferences() {
		for (final ModelClass modelClass : model.values()) {
			for (final PropertyDescriptor prop : modelClass.getProperties()
					.values()) {
				final Method readMethod = prop.getReadMethod();
				String refClassType = null;
				ModelClass.ReferenceType refType = null;
				if (readMethod.isAnnotationPresent(OneToOne.class)) {
					refClassType = readMethod.getReturnType().getName();
					refType = ModelClass.ReferenceType.ONE_TO_ONE;
				} else if (readMethod.isAnnotationPresent(OneToMany.class)) {
					final String returnType = readMethod.getGenericReturnType()
							.toString();
					refClassType = returnType.substring(
							returnType.indexOf("<") + 1,
							returnType.indexOf(">"));
					refType = ModelClass.ReferenceType.ONE_TO_MANY;
				} else if (readMethod.isAnnotationPresent(ManyToMany.class)) {
					final String returnType = readMethod.getGenericReturnType()
							.toString();
					refClassType = returnType.substring(
							returnType.indexOf("<") + 1,
							returnType.indexOf(">"));
					refType = ModelClass.ReferenceType.MANY_TO_MANY;
				} else if (readMethod.isAnnotationPresent(ManyToOne.class)) {
					refClassType = readMethod.getReturnType().getName();
					refType = ModelClass.ReferenceType.ONE_TO_ONE;
				}
				if (refClassType != null && refType != null) {
					modelClass.addReference(
							prop,
							model.get(refClassType),
							refType);
					continue;
				}

				Field declaredField = null;
				try {
					declaredField = modelClass.getModelClass()
							.getDeclaredField(prop.getName());
				} catch (final Exception e1) {
					// TODO Auto-generated catch block
					// Sollte nicht vorkommen
					e1.printStackTrace();
				}
				if (declaredField.isAnnotationPresent(OneToOne.class)) {
					refClassType = declaredField.getType().getName();
					refType = ModelClass.ReferenceType.ONE_TO_ONE;
				} else if (declaredField.isAnnotationPresent(OneToMany.class)) {
					final String genType = declaredField.getGenericType()
							.toString();
					refClassType = genType.substring(
							genType.indexOf("<") + 1,
							genType.indexOf(">"));
					refType = ModelClass.ReferenceType.ONE_TO_MANY;
				} else if (declaredField.isAnnotationPresent(ManyToMany.class)) {
					final String genType = declaredField.getGenericType()
							.toString();
					refClassType = genType.substring(
							genType.indexOf("<") + 1,
							genType.indexOf(">"));
					refType = ModelClass.ReferenceType.MANY_TO_MANY;
				} else if (declaredField.isAnnotationPresent(ManyToOne.class)) {
					refClassType = declaredField.getType().getName();
					refType = ModelClass.ReferenceType.MANY_TO_ONE;
				}
				if (refClassType != null && refType != null) {
					modelClass.addReference(
							prop,
							model.get(refClassType),
							refType);
				}
			}
		}
	}

	private void initDataProcesse(final IExtensionRegistry reg)
			throws InvalidRegistryObjectException {
		final IConfigurationElement[] elements = reg.getConfigurationElementsFor(
				Activator.PLUGIN_ID,
				Constants.DATA_PROCESSINGS_EXTENSION_POINT_ID);

		for (final IConfigurationElement element : elements) {
			if (element.getName().equals("dataProcessing")) {
				try {
					final String id = element.getAttribute("id");
					if (id == null || id.isEmpty()) {
						Activator.logWarning(
								0,
								"Id of a binding part is NULL or empty - the binding part extension was ignored!");
						continue;
					}

					final String targetEntity = element.getAttribute("targetEntity");
					if (!model.containsKey(targetEntity)) {
						Activator.logWarning(
								0,
								"There not all entity types are available in binding part "
										+ id
										+ " - the binding part was ignored!");
						continue;
					}
					final String notForGUI = element.getAttribute("notForGUI");

					final ComputedValue dataProcessingObj = (ComputedValue) element.createExecutableExtension("class");
					final Class<? extends ComputedValue> dataProcessingClass = dataProcessingObj.getClass();
					final DataProcessing dataProcessing = new DataProcessing(
							id, dataProcessingClass, targetEntity,
							Boolean.parseBoolean(notForGUI));
					List<DataProcessing> list = dataProcessings.get(targetEntity);
					if (list == null) {
						list = new ArrayList<DataProcessing>();
						dataProcessings.put(targetEntity, list);
					}
					list.add(dataProcessing);

					final IConfigurationElement[] children = element.getChildren("bindingField");
					for (final IConfigurationElement field : children) {
						final String name = field.getAttribute("name");
						if (name == null || name.isEmpty()) {
							Activator.logWarning(
									0,
									"The Name of a binding field is NULL or empty - field was ignored!");
							continue;
						}

						final String ignore = field.getAttribute("ignore");
						final String target = field.getAttribute("targetProperty");
						final String targetSubEntity = field.getAttribute("targetSubEntity");

						final DataProcessingField processingField = dataProcessing.new DataProcessingField(
								name, Boolean.parseBoolean(ignore),
								targetSubEntity, target);

						dataProcessing.addProcessingField(processingField);
					}
				} catch (final CoreException e) {
					// TODO process instantiation exceptions here
					e.printStackTrace();
				} catch (final IntrospectionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void initModel(final IExtensionRegistry reg)
			throws InvalidRegistryObjectException {
		final IConfigurationElement[] elements = reg.getConfigurationElementsFor(
				Activator.PLUGIN_ID,
				Constants.ENTITIES_EXTENSION_POINT_ID);

		for (final IConfigurationElement element : elements) {
			if (element.getName().equals("entity")) {
				try {
					final Object entity = element.createExecutableExtension("class");
					final Class<?> entityClass = entity.getClass();
					if (entityClass.isAnnotationPresent(Entity.class)) {
						try {
							final ModelClass modelClass = new ModelClass(
									entityClass);
							model.put(entityClass.getName(), modelClass);
						} catch (final IntrospectionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						// TODO Fehlermeldung wenn Klasse einer
						// Entityerweiterung nicht die Anmerkung Entity hat.
					}
				} catch (final CoreException e) {
					// TODO process instantiation exceptions here
					e.printStackTrace();
				}
			}
		}

		createReferences();
	}
}
