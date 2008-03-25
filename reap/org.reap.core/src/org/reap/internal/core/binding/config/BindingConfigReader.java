/*
 * BindingConfigReader.java -- TODO Insert a short describtion of this file.
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
package org.reap.internal.core.binding.config;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.reap.internal.core.Activator;
import org.reap.internal.core.Constants;
import org.reap.internal.core.ModelManager.ModelAccess;

public final class BindingConfigReader {

	public static void searchBindingFields(final BindingPartConfig partConfig,
			final IConfigurationElement bindingPart) {
		final IConfigurationElement[] children = bindingPart.getChildren("bindingField");
		for (final IConfigurationElement field : children) {
			final String name = field.getAttribute("name");
			if (name == null || name.isEmpty()) {
				Activator.logWarning(
						0,
						"The Name of a binding field is NULL or empty - field was ignored!");
				continue;
			}

			final String noBinding = field.getAttribute("noBinding");
			final BindingFieldConfig fieldConfig = new BindingFieldConfig(name,
					Boolean.parseBoolean(noBinding));

			searchBindings(fieldConfig, field);

			partConfig.addBindingField(fieldConfig);
		}
	}

	public static Map<String, BindingPartConfig> searchBindingParts(
			final IExtensionRegistry reg, final ModelAccess modelAccess)
			throws InvalidRegistryObjectException {
		final Map<String, BindingPartConfig> bindingPartConfigs = new HashMap<String, BindingPartConfig>();

		final IConfigurationElement[] bindingParts = reg.getConfigurationElementsFor(
				Activator.PLUGIN_ID,
				Constants.BINDING_PARTS_EXTENSION_POINT_ID);
		for (final IConfigurationElement bindingPart : bindingParts) {
			if (bindingPart.getName().equals("bindingPart")) {
				final String id = bindingPart.getAttribute("id");
				if (id == null || id.isEmpty()) {
					Activator.logWarning(
							0,
							"Id of a binding part is NULL or empty - the binding part extension was ignored!");
					continue;
				}

				final String targetEntity = bindingPart.getAttribute("targetEntity");
				if (!modelAccess.isEntityAvailable(targetEntity)) {
					Activator.logWarning(
							0,
							"There not all entity types are available in binding part "
									+ id + " - the binding part was ignored!");
					continue;
				}

				final String bindToPart = bindingPart.getAttribute("bindToPart");

				final BindingPartConfig partConfig = new BindingPartConfig(id,
						targetEntity, bindToPart);

				searchBindingFields(partConfig, bindingPart);

				bindingPartConfigs.put(id, partConfig);
			}
		}
		return bindingPartConfigs;
	}

	public static void searchBindings(final BindingFieldConfig fieldConfig,
			final IConfigurationElement field) {
		final IConfigurationElement[] listInput = field.getChildren("listInput");
		if (listInput.length > 0) {
			final String databaseAccessID = listInput[0].getAttribute("databaseAccessID");
			final String viewerLabelProps = listInput[0].getAttribute("viewerLabelProps");
			String[] split = null;
			if (viewerLabelProps != null) {
				split = viewerLabelProps.split(",");
			}
			fieldConfig.setListInput(databaseAccessID);
			fieldConfig.setViewerLabelProps(split);
			return;
		}

		final IConfigurationElement[] bindings = field.getChildren("binding");
		for (final IConfigurationElement binding : bindings) {
			final String type = binding.getAttribute("type");
			final String returnMethod = binding.getAttribute("returnMethode");
			final boolean onlyReturnObservable = Boolean.parseBoolean(binding.getAttribute("onlyReturnObservable"));
			final String viewerLabelProps = binding.getAttribute("viewerLabelProps");
			String[] split = null;
			if (viewerLabelProps != null) {
				split = viewerLabelProps.split(",");
			}
			String target = null;
			String targetEntity = null;
			boolean processingBinding = false;

			final IConfigurationElement[] entityBindings = binding.getChildren("entityBinding");
			if (entityBindings.length > 0) {
				target = entityBindings[0].getAttribute("targetProperty");
				targetEntity = entityBindings[0].getAttribute("targetSubEntity");
			} else {
				final IConfigurationElement[] dataProcessingBindings = binding.getChildren("dataProcessingBinding");
				if (dataProcessingBindings.length > 0) {
					target = dataProcessingBindings[0].getAttribute("dataProcessingID");
					processingBinding = true;
				}
			}

			BindingConfig bindingConfig;
			try {
				bindingConfig = new BindingConfig(type, target, targetEntity,
						returnMethod, onlyReturnObservable, processingBinding,
						split);
			} catch (final IllegalArgumentException e) {
				Activator.logWarning(
						0,
						"Data processings are not allowed for list bindings of types ITEMS and JFACEINPUT!");
				continue;
			}

			searchUpdateStrategies(binding, bindingConfig);

			fieldConfig.addBindingConfig(bindingConfig);
		}
	}

	private static void searchUpdateStrategies(
			final IConfigurationElement binding,
			final BindingConfig bindingConfig) {
		final IConfigurationElement[] updateStrategies = binding.getChildren("updateStrategy");
		for (final IConfigurationElement updateStrategy : updateStrategies) {
			final String direction = updateStrategy.getAttribute("direction");
			if (direction == null || direction.isEmpty()) {
				Activator.logWarning(
						0,
						"Direction of a update strategy is NULL or empty - this update strategy was ignored!");
				continue;
			}

			final String policy = updateStrategy.getAttribute("policy");
			final String provideDefaults = updateStrategy.getAttribute("provideDefaults");
			IConverter converter = null;
			IValidator afterGetV = null;
			IValidator afterConvV = null;
			IValidator beforeSetV = null;
			try {
				converter = (IConverter) updateStrategy.createExecutableExtension("converter");
				converter.getClass().getMethod("init").invoke(converter);
				// } catch (final CoreException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
			} catch (final Exception e) {
				Activator.logInfo(
						0,
						"Can't invoke the init method for converter.");
			}

			try {
				afterGetV = (IValidator) updateStrategy.createExecutableExtension("afterGetValidator");
				afterGetV.getClass().getMethod("init").invoke(afterGetV);
				// } catch (final CoreException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
			} catch (final Exception e) {
				Activator.logInfo(
						0,
						"Can't invoke the init method for after get validator.");
			}

			try {
				afterConvV = (IValidator) updateStrategy.createExecutableExtension("afterConvertValidator");
				afterConvV.getClass().getMethod("init").invoke(afterConvV);
				// } catch (final CoreException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
			} catch (final Exception e) {
				Activator.logInfo(
						0,
						"Can't invoke the init method for after convert validator.");
			}

			try {
				beforeSetV = (IValidator) updateStrategy.createExecutableExtension("beforeSetValidator");
				beforeSetV.getClass().getMethod("init").invoke(beforeSetV);
				// } catch (final CoreException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
			} catch (final Exception e) {
				Activator.logInfo(
						0,
						"Can't invoke the init method for before set validator.");
			}

			final UpdateStrategyConfig updateStrategyConfig = new UpdateStrategyConfig(
					direction, policy, provideDefaults, converter, afterConvV,
					afterGetV, beforeSetV);
			bindingConfig.addUpdateStrategyConfig(updateStrategyConfig);
		}
	}

	private BindingConfigReader() {
	}

}
