/*
 * BindingManager.java -- TODO Insert a short describtion of this file.
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
package org.reap.internal.core.binding;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateListStrategy;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.databinding.viewers.ObservableSetContentProvider;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.viewers.AbstractListViewer;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.reap.internal.core.Activator;
import org.reap.internal.core.DataProcessing;
import org.reap.internal.core.ModelClass;
import org.reap.internal.core.DataProcessing.DataProcessingField;
import org.reap.internal.core.ModelManager.ModelAccess;
import org.reap.internal.core.binding.config.BindingConfig;
import org.reap.internal.core.binding.config.BindingConfigReader;
import org.reap.internal.core.binding.config.BindingFieldConfig;
import org.reap.internal.core.binding.config.BindingPartConfig;
import org.reap.internal.core.binding.config.GUIBindingTypes;
import org.reap.internal.core.binding.config.UpdateStrategyConfig;

public class BindingManager {

	private final Map<String, List<Binding>>		activBindings		= new HashMap<String, List<Binding>>();
	private final List<ComputedValue>				automatCompValues	= new ArrayList<ComputedValue>();

	private Map<String, BindingPartConfig>			bindingPartConfigs	= null;

	private final Map<String, WritableEntityValue>	entityValues		= new HashMap<String, WritableEntityValue>();
	private ModelAccess								modelAccess			= null;

	public final synchronized void bindPart(final IWorkbenchPart part) {

		final BindingPartConfig bindingPartConfig = bindingPartConfigs.get(part.getSite()
				.getId());
		final WritableEntityValue writableEntityValue = findOrCreateWriteableEntityValue(bindingPartConfig);

		final Field[] declaredFields;
		try {
			declaredFields = part.getClass().getDeclaredFields();
		} catch (final SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		for (final Field field : declaredFields) {

			final Object fieldObj;
			try {
				field.setAccessible(true);
				fieldObj = field.get(part);
			} catch (final SecurityException e) {
				// TODO: handle exception
				e.printStackTrace();
				continue;
			} catch (final Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}

			if (fieldObj instanceof Control
					|| fieldObj instanceof AbstractListViewer
					|| fieldObj instanceof TableViewer) {
				BindingFieldConfig fieldConfig = bindingPartConfig.getBindingField(field.getName());
				if (fieldConfig != null && fieldConfig.isNoBinding()) {
					Activator.logInfo(0, "Skip field " + field.getName()
							+ " because no binding is set.");
					continue;
				}

				if (fieldConfig == null) {
					fieldConfig = new BindingFieldConfig(field.getName(), false);
					bindingPartConfig.addBindingField(fieldConfig);
				}

				checkValuesAndSetDefaults(
						field,
						fieldObj,
						fieldConfig,
						bindingPartConfig.getTargetEntity(),
						writableEntityValue);
				createBinding(
						fieldObj,
						fieldConfig,
						part,
						bindingPartConfig.getTargetEntity(),
						writableEntityValue);
			}
		}
	}

	public final void clearList(final IWorkbenchPart part) {
		final String id = part.getSite().getId();
		final WritableEntityValue value = entityValues.get(id);
		if (value != null) {
			final WritableList writableEntityList = value.getWritableEntityList();
			writableEntityList.clear();
		}
	}

	public final String getDatabaseAccessID(final IWorkbenchPart part) {
		final String id = part.getSite().getId();
		final BindingPartConfig config = bindingPartConfigs.get(id);
		if (config.getBindToPart() != null) {
			return null;
		}

		final WritableEntityValue value = entityValues.get(id);
		if (value != null) {
			return value.getListDatabaseAccessID();
		}
		return null;
	}

	public final Object getEntity(final IWorkbenchPart part) {
		return entityValues.get(part.getSite().getId()).getValue();
	}

	public final WritableList getEntityList(final IWorkbenchPart part) {
		final WritableEntityValue value = entityValues.get(part.getSite()
				.getId());
		if (value != null) {
			return value.getWritableEntityList();
		}
		return null;
	}

	public final WritableEntityValue getEntityValue(final IWorkbenchPart part) {
		return entityValues.get(part.getSite().getId());
	}

	public final BindingFieldConfig getFieldConfig(final IWorkbenchPart part,
			final String fieldName) {
		final BindingPartConfig partConfig = bindingPartConfigs.get(part.getSite()
				.getId());
		return partConfig.getBindingField(fieldName);
	}

	// public final ModelClass getReferencedModelClass(final IWorkbenchPart
	// part,
	// final String fieldName) {
	// final WritableEntityValue value = entityValues.get(part.getSite()
	// .getId());
	// return value.getReferencedModelClass(fieldName);
	// }

	public final String getTargetEntity(final IWorkbenchPart part) {
		return bindingPartConfigs.get(part.getSite().getId()).getTargetEntity();
	}

	// public final IObservable getViewerInput(final IWorkbenchPart part,
	// final String fieldName) {
	// final WritableEntityValue value = entityValues.get(part.getSite()
	// .getId());
	// return value.getViewerInput(fieldName);
	// }

	public final WritableValue getViewerSelect(final IWorkbenchPart part,
			final String fieldName) {
		final WritableEntityValue value = entityValues.get(part.getSite()
				.getId());
		return value.getViewerSelect(fieldName);
	}

	public final void init(final IExtensionRegistry reg,
			final ModelAccess modelAccess) {
		this.modelAccess = modelAccess;
		bindingPartConfigs = BindingConfigReader.searchBindingParts(
				reg,
				modelAccess);
	}

	public final boolean isBindingPart(final String id) {
		return bindingPartConfigs.containsKey(id);
	}

	public final void setEntity(final IWorkbenchPart part, final Object entity) {
		entityValues.get(part.getSite().getId()).setValue(entity);
	}

	public final void setList(final IWorkbenchPart part, final List retList) {
		final String id = part.getSite().getId();
		final WritableEntityValue value = entityValues.get(id);
		if (value != null) {
			final WritableList writableEntityList = value.getWritableEntityList();
			writableEntityList.addAll(retList);
		}
	}

	public final void stop() {
		bindingPartConfigs.clear();
		bindingPartConfigs = null;
		entityValues.clear();

		for (final ComputedValue compValue : automatCompValues) {
			compValue.dispose();
		}
	}

	public final void unbindPart(final IWorkbenchPart part) {
		if (part == null
				|| !bindingPartConfigs.containsKey(part.getSite().getId())) {
			return;
		}

		final List<Binding> list = activBindings.get(part.getSite().getId());
		for (final Binding binding : list) {
			binding.dispose();
		}
	}

	private void bindDataProcessings(final BindingPartConfig bindingPartConfig,
			final WritableEntityValue writeableEntityValue) {
		final List<DataProcessing> dataProcessings = modelAccess.getDataProcessings(bindingPartConfig.getTargetEntity());
		for (final DataProcessing dataProcessing : dataProcessings) {
			ComputedValue compValue = null;
			try {
				compValue = dataProcessing.newInstance();
			} catch (final Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			for (final PropertyDescriptor prop : dataProcessing.getProperties()) {
				final DataProcessingField field = dataProcessing.getProcessingField(prop.getName());
				if (field != null && field.isIgnore()) {
					Activator.logInfo(0, "Skip field " + field.getName()
							+ " because ignore is set.");
					continue;
				}

				String target;
				String targetSubEntity = null;
				if (field == null) {
					target = prop.getName();
				} else {
					target = field.getTarget();
					targetSubEntity = field.getTargetSubEntity();
				}
				final ModelClass modelClass = modelAccess.get(dataProcessing.getTargetEntity());
				if (modelClass == null
						|| !modelClass.hasProperty(targetSubEntity, target)) {
					Activator.logWarning(
							0,
							"The target "
									+ target
									+ " for field "
									+ field.getName()
									+ " isn't available in the given entity types - binding config will ignored!");
					continue;
				}

				final IObservable modelObs = modelClass.getObservable(
						targetSubEntity,
						target,
						writeableEntityValue,
						false);
				try {
					prop.getPropertyType().cast(modelObs);
					prop.getWriteMethod().invoke(compValue, modelObs);
				} catch (final Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (dataProcessing.isNotForGUI()) {
				compValue.getValue();
				automatCompValues.add(compValue);
			} else {
				writeableEntityValue.addDataProcessing(
						dataProcessing.getId(),
						compValue);
			}
		}
	}

	private void checkAndBindMasterList(final Object fieldObj,
			final BindingFieldConfig bindingField,
			final WritableEntityValue writableEntityValue, final String partId) {
		if (fieldObj instanceof AbstractListViewer
				|| fieldObj instanceof TableViewer) {
			boolean hasBind = false;
			for (final BindingConfig binding : bindingField.getBindings()) {
				if (binding.isValid()) {
					hasBind = true;
				}
			}
			if (bindingField.getDatabaseAccessID() != null || !hasBind) {
				final WritableList writableList = new WritableList();
				writableEntityValue.setWritableEntityList(writableList);
				writableEntityValue.setListDatabaseAccessID(bindingField.getDatabaseAccessID());

				final StructuredViewer viewer = (StructuredViewer) fieldObj;
				final ObservableListContentProvider contentProvider = new ObservableListContentProvider();
				viewer.setContentProvider(contentProvider);
				final String[] viewerLabelProps = bindingField.getViewerLabelProps();
				if (viewerLabelProps != null) {
					final IObservableMap[] attributeMaps = BeansObservables.observeMaps(
							contentProvider.getKnownElements(),
							modelAccess.get(
									writableEntityValue.getTargetEntity())
									.getModelClass(),
							viewerLabelProps);
					viewer.setLabelProvider(new ObservableMapLabelProvider(
							attributeMaps));
				}
				viewer.setInput(writableList);

				final IObservableValue singleSelection = ViewersObservables.observeSingleSelection((ISelectionProvider) fieldObj);
				final DataBindingContext dbc = Activator.getDefault()
						.getDataBindingContext();
				final Binding bindValue = dbc.bindValue(
						singleSelection,
						writableEntityValue,
						null,
						null);
				writableEntityValue.setBinding(bindValue);

				List<Binding> list = activBindings.get(partId);
				if (list == null) {
					list = new ArrayList<Binding>();
					activBindings.put(partId, list);
				}
				list.add(bindValue);
			}
		}
	}

	private void checkValuesAndSetDefaults(final Field field,
			final Object fieldObj, final BindingFieldConfig bindingField,
			final String gloTargetEntity,
			final WritableEntityValue writableEntityValue) {
		if (bindingField.getDatabaseAccessID() != null) {
			return;
		}

		final String name = field.getName();
		final List<BindingConfig> bindings = bindingField.getBindings();
		if (bindings.isEmpty()) {
			createNewBindingConfig(bindings, fieldObj, name);
		}
		for (final BindingConfig bindingConfig : bindings) {
			final String returnMethod = bindingConfig.getReturnMethod();
			if (returnMethod == null || returnMethod.isEmpty()) {
				bindingConfig.setReturnMethod("set"
						+ name.replaceFirst(
								name.substring(0, 1),
								name.substring(0, 1).toUpperCase())
						+ "Observable");
			}

			GUIBindingTypes type = bindingConfig.getType();
			if (type == null) {
				if (fieldObj instanceof Text) {
					type = GUIBindingTypes.TEXTMODIFY;
				} else if (fieldObj instanceof Label
						|| fieldObj instanceof CLabel) {
					type = GUIBindingTypes.TEXT;
				} else if (fieldObj instanceof Spinner
						|| fieldObj instanceof Scale
						|| fieldObj instanceof Button
						|| fieldObj instanceof List
						|| fieldObj instanceof Combo
						|| fieldObj instanceof CCombo) {
					type = GUIBindingTypes.SELECTION;
				} else if (fieldObj instanceof Table) {
					type = GUIBindingTypes.SINGLESELECTIONINDEX;
				} else if (fieldObj instanceof AbstractListViewer
						|| fieldObj instanceof TableViewer) {
					type = GUIBindingTypes.JFACESINGLESELECTION;
				} else {
					type = GUIBindingTypes.ENABLED;
				}
				bindingConfig.setType(type);
			}

			String target = bindingConfig.getTarget();
			if (bindingConfig.isProcessingBinding()) {
				if (target == null
						|| target.isEmpty()
						|| writableEntityValue.getDataProcessing(target) == null) {
					Activator.logWarning(
							0,
							"The data processing target "
									+ target
									+ " for field "
									+ bindingField.getName()
									+ " isn't available in the given entity types - binding config will ignored!");
					bindingConfig.setValid(false);
				}
			} else {
				if (target == null || target.isEmpty()) {
					bindingConfig.setTarget(name);
					target = bindingConfig.getTarget();
				}

				final String targetEntity = bindingConfig.getTargetEntity();
				final ModelClass modelClass = modelAccess.get(gloTargetEntity);
				if (modelClass == null
						|| !modelClass.hasProperty(targetEntity, target)) {
					Activator.logWarning(
							0,
							"The entity target "
									+ target
									+ " for field "
									+ bindingField.getName()
									+ " isn't available in the given entity types - binding config will ignored!");
					bindingConfig.setValid(false);
				}
			}
		}
	}

	private void createBinding(final Object fieldObj,
			final BindingFieldConfig bindingField, final IWorkbenchPart part,
			final String gloTargetEntity,
			final WritableEntityValue writableEntityValue) {
		checkAndBindMasterList(
				fieldObj,
				bindingField,
				writableEntityValue,
				part.getSite().getId());

		for (final BindingConfig bindingConfig : bindingField.getBindings()) {
			if (!bindingConfig.isValid()) {
				continue;
			}

			IObservable guiObs = null;
			try {
				if (fieldObj instanceof Control) {
					guiObs = createSWTObservable(
							(Control) fieldObj,
							bindingConfig);

					if (!bindingConfig.isOnlyReturnObservable()) {
						finishBind(
								gloTargetEntity,
								writableEntityValue,
								bindingConfig,
								guiObs,
								part);
					}
				} else if (fieldObj instanceof AbstractListViewer
						|| fieldObj instanceof TableViewer) {
					guiObs = createJFaceObservable(
							(ISelectionProvider) fieldObj,
							bindingConfig);

					if (!bindingConfig.isOnlyReturnObservable()) {
						if (bindingConfig.getType() == GUIBindingTypes.JFACEINPUT) {
							final String targetEntity = bindingConfig.getTargetEntity();
							final ModelClass modelClass = modelAccess.get(gloTargetEntity);
							final IObservable modelObs = modelClass.getObservable(
									targetEntity,
									bindingConfig.getTarget(),
									writableEntityValue,
									false);
							final StructuredViewer viewer = (StructuredViewer) fieldObj;
							IObservableSet knownElements = null;
							if (modelObs instanceof IObservableList) {
								final ObservableListContentProvider contentProvider = new ObservableListContentProvider();
								knownElements = contentProvider.getKnownElements();
								viewer.setContentProvider(contentProvider);
							} else if (modelObs instanceof IObservableSet) {
								final ObservableSetContentProvider contentProvider = new ObservableSetContentProvider();
								knownElements = contentProvider.getKnownElements();
								viewer.setContentProvider(contentProvider);
							}
							final ModelClass classForRefernce = modelClass.getModelClassForRefernce(
									targetEntity,
									bindingConfig.getTarget());
							final String[] viewerLabelProps = bindingConfig.getViewerLabelProps();
							if (viewerLabelProps != null
									&& knownElements != null
									&& classForRefernce != null) {
								final IObservableMap[] attributeMaps = BeansObservables.observeMaps(
										knownElements,
										classForRefernce.getModelClass(),
										viewerLabelProps);
								viewer.setLabelProvider(new ObservableMapLabelProvider(
										attributeMaps));
							}
							viewer.setInput(modelObs);

							boolean hasSingleSel = false;
							for (final BindingConfig bindingConfig2 : bindingField.getBindings()) {
								if (bindingConfig2.getType() == GUIBindingTypes.JFACESINGLESELECTION) {
									hasSingleSel = true;
									break;
								}
							}
							if (!hasSingleSel) {
								final IObservableValue singleSelection = ViewersObservables.observeSingleSelection((ISelectionProvider) fieldObj);
								final WritableValue selectionValue = modelClass.getSelectionValue(
										targetEntity,
										bindingConfig.getTarget(),
										writableEntityValue);
								final DataBindingContext dbc = Activator.getDefault()
										.getDataBindingContext();
								final Binding bindValue = dbc.bindValue(
										singleSelection,
										selectionValue,
										null,
										null);
								List<Binding> list = activBindings.get(part.getSite()
										.getId());
								if (list == null) {
									list = new ArrayList<Binding>();
									activBindings.put(
											part.getSite().getId(),
											list);
								}
								list.add(bindValue);
								writableEntityValue.addViewerSelect(
										bindingField.getName(),
										selectionValue);
							}
						} else {
							finishBind(
									gloTargetEntity,
									writableEntityValue,
									bindingConfig,
									guiObs,
									part);
						}
					}
				}
			} catch (final IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Activator.logWarning(0, "For the field "
						+ bindingField.getName()
						+ " and the given binding type "
						+ bindingConfig.getType()
						+ " can't create an observable!");
				continue;
			}

			try {
				final Method method = part.getClass().getMethod(
						bindingConfig.getReturnMethod(),
						IObservable.class);
				method.setAccessible(true);
				method.invoke(part, guiObs);
			} catch (final Exception e) {
				Activator.logInfo(0, "The methode "
						+ bindingConfig.getReturnMethod()
						+ " for call back the observable can't invoke.");
			}
		}
	}

	private IObservable createJFaceObservable(
			final ISelectionProvider selectionProvider,
			final BindingConfig bindingConfig) {
		IObservableValue observeSingleSelection = null;
		switch (bindingConfig.getType()) {
			case JFACESINGLESELECTION:
				observeSingleSelection = ViewersObservables.observeSingleSelection(selectionProvider);
				break;

			default:
				break;
		}
		return observeSingleSelection;
	}

	private UpdateListStrategy[] createListStrategies(
			final List<UpdateStrategyConfig> updateStrategies) {
		final UpdateListStrategy[] updateListStrategies = new UpdateListStrategy[] {
				null, null };
		for (final UpdateStrategyConfig updateStrategyConfig : updateStrategies) {
			final String policy = updateStrategyConfig.getPolicy();
			int setPolicy = UpdateListStrategy.POLICY_UPDATE;
			if (policy.equalsIgnoreCase("never")) {
				setPolicy = UpdateListStrategy.POLICY_NEVER;
			} else if (policy.equalsIgnoreCase("onRequest")) {
				setPolicy = UpdateListStrategy.POLICY_ON_REQUEST;
			}

			final UpdateListStrategy updateStrategy = new UpdateListStrategy(
					updateStrategyConfig.isProvideDefaults(), setPolicy);
			final IConverter converter = updateStrategyConfig.getConverter();
			if (converter == null) {
				updateStrategy.setConverter(converter);
			}

			if (updateStrategyConfig.getDirection().equalsIgnoreCase(
					"targetToModel")) {
				updateListStrategies[0] = updateStrategy;
			} else if (updateStrategyConfig.getDirection().equalsIgnoreCase(
					"modelToTarget")) {
				updateListStrategies[1] = updateStrategy;
			}
		}
		return updateListStrategies;
	}

	private void createNewBindingConfig(final List<BindingConfig> bindings,
			final Object fieldObj, final String name) {
		bindings.add(new BindingConfig(null, null, null, null, false, false,
				null));

		if (fieldObj instanceof List || fieldObj instanceof Combo
				|| fieldObj instanceof CCombo) {
			final StringBuffer returnMethod = new StringBuffer();
			returnMethod.append("set");
			returnMethod.append(name.replaceFirst(
					name.substring(0, 1),
					name.substring(0, 1).toUpperCase()));
			returnMethod.append("ListObservable");
			final BindingConfig bindingConfig = new BindingConfig(null, name
					+ "List", null, returnMethod.toString(), false, false, null);
			bindings.add(bindingConfig);
			bindingConfig.setType(GUIBindingTypes.ITEMS);
		} else if (fieldObj instanceof AbstractListViewer
				|| fieldObj instanceof TableViewer) {
			final StringBuffer returnMethod = new StringBuffer();
			returnMethod.append("set");
			returnMethod.append(name.replaceFirst(
					name.substring(0, 1),
					name.substring(0, 1).toUpperCase()));
			returnMethod.append("ListObservable");
			final BindingConfig bindingConfig = new BindingConfig(null, name
					+ "List", null, returnMethod.toString(), false, false, null);
			bindings.add(bindingConfig);
			bindingConfig.setType(GUIBindingTypes.JFACEINPUT);
		}
	}

	private IObservable createSWTObservable(final Control control,
			final BindingConfig binding) {
		IObservable obs = null;
		switch (binding.getType()) {
			case BACKGROUND:
				obs = SWTObservables.observeBackground(control);
				break;
			case EDITABLE:
				obs = SWTObservables.observeEditable(control);
				break;
			case ENABLED:
				obs = SWTObservables.observeEnabled(control);
				break;
			case FONT:
				obs = SWTObservables.observeFont(control);
				break;
			case FOREGROUND:
				obs = SWTObservables.observeForeground(control);
				break;
			case ITEMS:
				obs = SWTObservables.observeItems(control);
				break;
			case MAX:
				obs = SWTObservables.observeMax(control);
				break;
			case MIN:
				obs = SWTObservables.observeMin(control);
				break;
			case SELECTION:
				obs = SWTObservables.observeSelection(control);
				break;
			case SINGLESELECTIONINDEX:
				obs = SWTObservables.observeSingleSelectionIndex(control);
				break;
			case TEXT:
				obs = SWTObservables.observeText(control);
				break;
			case TEXTFOCUSOUT:
				obs = SWTObservables.observeText(control, SWT.FocusOut);
				break;
			case TEXTMODIFY:
				obs = SWTObservables.observeText(control, SWT.Modify);
				break;
			case TEXTNONE:
				obs = SWTObservables.observeText(control, SWT.None);
				break;
			case TOOLTIPTEXT:
				obs = SWTObservables.observeTooltipText(control);
				break;
			case VISIBLE:
				obs = SWTObservables.observeVisible(control);
				break;
			default:
				throw new IllegalArgumentException();
		}
		return obs;
	}

	private UpdateValueStrategy[] createValueStrategies(
			final List<UpdateStrategyConfig> updateStrategies) {
		final UpdateValueStrategy[] updateValueStrategies = new UpdateValueStrategy[] {
				null, null };
		for (final UpdateStrategyConfig updateStrategyConfig : updateStrategies) {
			final String policy = updateStrategyConfig.getPolicy();
			int setPolicy = UpdateListStrategy.POLICY_UPDATE;
			if (policy == null) {
			} else if (policy.equalsIgnoreCase("never")) {
				setPolicy = UpdateListStrategy.POLICY_NEVER;
			} else if (policy.equalsIgnoreCase("onRequest")) {
				setPolicy = UpdateListStrategy.POLICY_ON_REQUEST;
			} else if (policy.equalsIgnoreCase("convert")) {
				setPolicy = UpdateValueStrategy.POLICY_CONVERT;
			}

			final UpdateValueStrategy updateStrategy = new UpdateValueStrategy(
					updateStrategyConfig.isProvideDefaults(), setPolicy);
			final IConverter converter = updateStrategyConfig.getConverter();
			if (converter != null) {
				updateStrategy.setConverter(converter);
			}
			final IValidator afterConvV = updateStrategyConfig.getAfterConvV();
			if (afterConvV != null) {
				updateStrategy.setAfterConvertValidator(afterConvV);
			}
			final IValidator afterGetV = updateStrategyConfig.getAfterGetV();
			if (afterGetV != null) {
				updateStrategy.setAfterGetValidator(afterGetV);
			}
			final IValidator beforeSetV = updateStrategyConfig.getBeforeSetV();
			if (beforeSetV != null) {
				updateStrategy.setBeforeSetValidator(beforeSetV);
			}
			if (updateStrategyConfig.getDirection().equalsIgnoreCase(
					"targetToModel")) {
				updateValueStrategies[0] = updateStrategy;
			} else if (updateStrategyConfig.getDirection().equalsIgnoreCase(
					"modelToTarget")) {
				updateValueStrategies[1] = updateStrategy;
			}
		}
		return updateValueStrategies;
	}

	private WritableEntityValue findOrCreateWriteableEntityValue(
			final BindingPartConfig bindingPartConfig) {

		final String id = bindingPartConfig.getId();
		WritableEntityValue value = entityValues.get(id);
		if (value != null) {
			return value;
		}

		String bindToPart = bindingPartConfig.getBindToPart();
		while (bindToPart != null) {
			value = entityValues.get(bindToPart);
			if (value != null) {
				value.addForPart(id);
				entityValues.put(id, value);
				return value;
			}
			bindToPart = bindingPartConfigs.get(bindToPart).getBindToPart();
		}

		value = new WritableEntityValue(bindingPartConfig.getTargetEntity());
		value.addForPart(id);
		entityValues.put(id, value);
		bindToPart = bindingPartConfig.getBindToPart();
		while (bindToPart != null) {
			value.addForPart(bindToPart);
			entityValues.put(bindToPart, value);
			bindToPart = bindingPartConfigs.get(bindToPart).getBindToPart();
		}

		bindDataProcessings(bindingPartConfig, value);

		return value;
	}

	private void finishBind(final String gloTargetEntity,
			final WritableEntityValue writeableEntityValue,
			final BindingConfig bindingConfig, final IObservable guiObs,
			final IWorkbenchPart part) {
		final ModelClass modelClass = modelAccess.get(gloTargetEntity);

		final IObservable modelObs;
		if (bindingConfig.isProcessingBinding()) {
			modelObs = writeableEntityValue.getDataProcessing(bindingConfig.getTarget());
		} else {
			modelObs = modelClass.getObservable(
					bindingConfig.getTargetEntity(),
					bindingConfig.getTarget(),
					writeableEntityValue,
					false);
		}

		final DataBindingContext dbc = Activator.getDefault()
				.getDataBindingContext();
		Binding binding = null;
		if (guiObs instanceof IObservableList
				&& modelObs instanceof IObservableList) {
			final UpdateListStrategy[] strategies = createListStrategies(bindingConfig.getUpdateStrategies());
			binding = dbc.bindList(
					(IObservableList) guiObs,
					(IObservableList) modelObs,
					strategies[0],
					strategies[1]);
		} else if (guiObs instanceof IObservableValue
				&& modelObs instanceof IObservableValue) {
			final UpdateValueStrategy[] strategies = createValueStrategies(bindingConfig.getUpdateStrategies());
			binding = dbc.bindValue(
					(IObservableValue) guiObs,
					(IObservableValue) modelObs,
					strategies[0],
					strategies[1]);
		}

		List<Binding> list = activBindings.get(part.getSite().getId());
		if (list == null) {
			list = new ArrayList<Binding>();
			activBindings.put(part.getSite().getId(), list);
		}
		list.add(binding);
	}
}
