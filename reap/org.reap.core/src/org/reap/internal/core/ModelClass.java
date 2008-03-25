/*
 * ModelClass.java -- TODO Insert a short describtion of this file. 
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.reap.internal.core.binding.WritableEntityValue;

public class ModelClass {
	public class ModelClassReference {
		private final PropertyDescriptor	prop;
		private final ModelClass			refClass;
		private final ReferenceType			type;

		public ModelClassReference(final PropertyDescriptor prop,
				final ModelClass refClass, final ReferenceType type) {
			this.prop = prop;
			this.refClass = refClass;
			this.type = type;
		}

		/**
		 * @return the prop
		 */
		public final PropertyDescriptor getProp() {
			return prop;
		}

		/**
		 * @return the refClass
		 */
		public final ModelClass getRefClass() {
			return refClass;
		}

		/**
		 * @return the type
		 */
		public final ReferenceType getType() {
			return type;
		}
	}

	public enum ReferenceType {
		MANY_TO_MANY, MANY_TO_ONE, ONE_TO_MANY, ONE_TO_ONE
	}

	private boolean													hasVisit	= false;
	private boolean													javaBean	= true;

	private final Class<?>											modelClass;
	private final Map<IObservableValue, Map<String, IObservable>>	observables	= new HashMap<IObservableValue, Map<String, IObservable>>();

	private final Map<String, PropertyDescriptor>					properties	= new HashMap<String, PropertyDescriptor>();

	private final List<ModelClassReference>							references	= new ArrayList<ModelClassReference>();

	public ModelClass(final Class<?> modelClass) throws IntrospectionException {
		this.modelClass = modelClass;

		final BeanInfo beanInfo = Introspector.getBeanInfo(
				modelClass,
				Object.class);
		final PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
		for (final PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			properties.put(propertyDescriptor.getName(), propertyDescriptor);
			if (!(propertyDescriptor.isBound() || propertyDescriptor.isConstrained())) {
				javaBean = false;
			}
		}
	}

	public final void addReference(final PropertyDescriptor prop,
			final ModelClass refModelClass, final ReferenceType referenceType) {
		final ModelClassReference classReference = new ModelClassReference(
				prop, refModelClass, referenceType);
		references.add(classReference);
	}

	/**
	 * @return the modelClass
	 */
	public final Class<?> getModelClass() {
		return modelClass;
	}

	public final ModelClass getModelClass(final String targetEntity,
			final String target) {
		if (getName().equals(targetEntity) && !properties.containsKey(target)) {
			return null;
		} else if ((targetEntity == null || targetEntity.equals(getName()))
				&& properties.containsKey(target)) {
			return this;
		} else {
			hasVisit = true;
			for (final ModelClassReference ref : references) {
				if (ref.getRefClass().hasVisit) {
					continue;
				}
				final ModelClass modelClass = ref.getRefClass().getModelClass(
						targetEntity,
						target);
				if (modelClass != null) {
					hasVisit = false;
					return modelClass;
				}
			}
			hasVisit = false;
		}

		return null;
	}

	public final ModelClass getModelClassForRefernce(final String targetEntity,
			final String target) {
		final ModelClass modelClass2 = getModelClass(targetEntity, target);
		if (modelClass2 != null) {
			for (final ModelClassReference ref : modelClass2.getReferences()) {
				if (ref.prop.getName().equals(target)) {
					return ref.getRefClass();
				}
			}
		}
		return null;
	}

	public final String getName() {
		return modelClass.getName();
	}

	public final IObservable getObservable(final String targetEntity,
			final String property, final WritableEntityValue masterObs,
			final boolean retParent) {
		return getObservable(
				targetEntity,
				property,
				(IObservableValue) masterObs,
				retParent);
	}

	/**
	 * @return the properties
	 */
	public final Map<String, PropertyDescriptor> getProperties() {
		return properties;
	}

	/**
	 * @return the references
	 */
	public final List<ModelClassReference> getReferences() {
		return references;
	}

	public final WritableValue getSelectionValue(final String targetEntity,
			final String property, final WritableEntityValue masterObs) {
		return getSelectionValue(
				targetEntity,
				property,
				(IObservableValue) masterObs);
	}

	public final void getSubEntities(final List<String> entityList) {
		hasVisit = true;
		for (final ModelClassReference ref : references) {
			if (ref.getRefClass().hasVisit) {
				continue;
			}
			entityList.add(ref.getRefClass().getName());
			ref.getRefClass().getSubEntities(entityList);
		}
		hasVisit = false;
	}

	public final synchronized boolean hasProperty(final String targetEntity,
			final String target) {
		if (getName().equals(targetEntity)) {
			return properties.containsKey(target);
		} else if (targetEntity == null && properties.containsKey(target)) {
			return true;
		} else {
			hasVisit = true;
			for (final ModelClassReference ref : references) {
				if (ref.getRefClass().hasVisit) {
					continue;
				}
				final boolean hasProperty = ref.getRefClass().hasProperty(
						targetEntity,
						target);
				if (hasProperty) {
					hasVisit = false;
					return hasProperty;
				}
			}
			hasVisit = false;
		}

		return false;
	}

	/**
	 * @return the javaBean
	 */
	public final boolean isJavaBean() {
		return javaBean;
	}

	public final Object newInstance() throws InstantiationException,
			IllegalAccessException {
		return modelClass.newInstance();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString() {
		return modelClass.toString();
	}

	private IObservable getObservable(final String targetEntity,
			final String property, final IObservableValue masterObs,
			final boolean retParent) {
		if (getName().equals(targetEntity) && !properties.containsKey(property)) {
			return null;
		} else if ((targetEntity == null || targetEntity.equals(getName()))
				&& properties.containsKey(property)) {
			if (retParent) {
				return masterObs;
			}

			Map<String, IObservable> map = observables.get(masterObs);
			if (map == null) {
				map = new HashMap<String, IObservable>();
				observables.put(masterObs, map);
			}
			IObservable obs = map.get(property);
			if (obs == null) {
				final PropertyDescriptor prop = properties.get(property);
				final Class<?>[] interfaces = prop.getPropertyType()
						.getInterfaces();
				final Class<?>[] interf = new Class<?>[interfaces.length + 1];
				int i;
				for (i = 0; i < interfaces.length; i++) {
					interf[i] = interfaces[i];
				}
				interf[i] = prop.getPropertyType();

				for (final Class<?> intf : interf) {
					if (intf.getName().equals(List.class.getName())) {
						obs = BeansObservables.observeDetailList(
								Realm.getDefault(),
								masterObs,
								property,
								prop.getPropertyType());
					} else if (intf.getName().equals(Set.class.getName())) {
						obs = BeansObservables.observeDetailSet(
								Realm.getDefault(),
								masterObs,
								property,
								prop.getPropertyType());
					}
				}

				if (obs == null) {
					obs = BeansObservables.observeDetailValue(
							Realm.getDefault(),
							masterObs,
							property,
							prop.getPropertyType());
				}
				map.put(property, obs);
			}
			return obs;
		} else {
			hasVisit = true;
			Map<String, IObservable> map = observables.get(masterObs);
			if (map == null) {
				map = new HashMap<String, IObservable>();
				observables.put(masterObs, map);
			}

			IObservableValue obs;
			for (final ModelClassReference ref : references) {
				if (ref.getRefClass().hasVisit) {
					continue;
				}
				switch (ref.getType()) {
					case ONE_TO_ONE:
					case MANY_TO_ONE:
						obs = (IObservableValue) map.get(ref.getProp()
								.getName());
						if (obs == null) {
							obs = BeansObservables.observeDetailValue(
									Realm.getDefault(),
									masterObs,
									ref.getProp().getName(),
									ref.getRefClass().getModelClass());
							map.put(ref.getProp().getName(), obs);
						}
						break;
					case ONE_TO_MANY:
					case MANY_TO_MANY:
						obs = (IObservableValue) map.get(ref.getProp()
								.getName()
								+ "#REF#");
						if (obs == null) {
							obs = new WritableValue();
							map.put(ref.getProp().getName() + "#REF#", obs);
						}
						break;
					default:
						obs = null;
				}
				final IObservable observable = ref.getRefClass().getObservable(
						targetEntity,
						property,
						obs,
						retParent);
				if (observable != null) {
					hasVisit = false;
					return observable;
				}
			}
			hasVisit = false;
			return null;
		}
	}

	private WritableValue getSelectionValue(final String targetEntity,
			final String property, final IObservableValue masterObs) {
		if (getName().equals(targetEntity) && !properties.containsKey(property)) {
			return null;
		} else if ((targetEntity == null || targetEntity.equals(getName()))
				&& properties.containsKey(property)) {
			Map<String, IObservable> map = observables.get(masterObs);
			if (map == null) {
				map = new HashMap<String, IObservable>();
				observables.put(masterObs, map);
			}
			WritableValue obs = (WritableValue) map.get(property + "#REF#");
			if (obs == null) {
				obs = new WritableValue();
				map.put(property + "#REF#", obs);
				map.put(property, obs);
			}
			return obs;
		} else {
			hasVisit = true;
			Map<String, IObservable> map = observables.get(masterObs);
			if (map == null) {
				map = new HashMap<String, IObservable>();
				observables.put(masterObs, map);
			}

			IObservableValue obs;
			for (final ModelClassReference ref : references) {
				if (ref.getRefClass().hasVisit) {
					continue;
				}
				switch (ref.getType()) {
					case ONE_TO_ONE:
					case MANY_TO_ONE:
						obs = (IObservableValue) map.get(ref.getProp()
								.getName());
						if (obs == null) {
							obs = BeansObservables.observeDetailValue(
									Realm.getDefault(),
									masterObs,
									ref.getProp().getName(),
									getModelClass());
							map.put(ref.getProp().getName(), obs);
						}
						break;
					case ONE_TO_MANY:
					case MANY_TO_MANY:
						obs = (IObservableValue) map.get(ref.getProp()
								.getName()
								+ "#REF#");
						if (obs == null) {
							obs = new WritableValue();
							map.put(ref.getProp().getName() + "#REF#", obs);
						}
						break;
					default:
						obs = null;
				}
				final WritableValue observable = ref.getRefClass()
						.getSelectionValue(targetEntity, property, obs);
				if (observable != null) {
					hasVisit = false;
					return observable;
				}
			}
			hasVisit = false;
			return null;
		}
	}
}
