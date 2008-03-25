/*
 * REAPManager.java -- TODO Insert a short describtion of this file. 
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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Id;

import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.reap.core.IREAPContext;
import org.reap.internal.core.ModelManager.ModelAccess;
import org.reap.internal.core.binding.BindingManager;
import org.reap.internal.core.binding.WritableEntityValue;
import org.reap.internal.core.binding.config.BindingConfig;
import org.reap.internal.core.binding.config.BindingFieldConfig;
import org.reap.internal.core.binding.config.GUIBindingTypes;
import org.reap.internal.core.persistence.PersistenceManager;

public class REAPManager implements IREAPContext {

	private static final int			TRAILS_COUNT		= 3;

	private final BindingManager		bindingManager		= new BindingManager();

	private final IPartListener2		bindPartListener	= new IPartListener2() {
																@Override
																public void partActivated(
																		final IWorkbenchPartReference partRef) {
																}

																@Override
																public void partBroughtToTop(
																		final IWorkbenchPartReference partRef) {
																}

																@Override
																public void partClosed(
																		final IWorkbenchPartReference partRef) {
																}

																@Override
																public void partDeactivated(
																		final IWorkbenchPartReference partRef) {
																}

																@Override
																public void partHidden(
																		final IWorkbenchPartReference partRef) {
																	runningUnbindingProcess(partRef.getPart(false));
																}

																@Override
																public void partInputChanged(
																		final IWorkbenchPartReference partRef) {
																}

																@Override
																public void partOpened(
																		final IWorkbenchPartReference partRef) {
																}

																@Override
																public void partVisible(
																		final IWorkbenchPartReference partRef) {
																	runningBindingProcess(partRef.getPart(false));
																}
															};

	private final IWindowListener		bindWindowListener	= new IWindowListener() {
																@Override
																public void windowActivated(
																		final IWorkbenchWindow window) {
																}

																@Override
																public void windowClosed(
																		final IWorkbenchWindow window) {
																	window.getPartService()
																			.removePartListener(
																					bindPartListener);
																}

																@Override
																public void windowDeactivated(
																		final IWorkbenchWindow window) {
																}

																@Override
																public void windowOpened(
																		final IWorkbenchWindow window) {
																	window.getPartService()
																			.addPartListener(
																					bindPartListener);
																}
															};

	private final Map<String, Object>	lastValues			= new HashMap<String, Object>();

	private ModelAccess					modelAccess;

	private final ModelManager			modelManager		= new ModelManager();

	private final PersistenceManager	persistenceManager	= new PersistenceManager();

	@Override
	public void createNew(final IWorkbenchPart part) {
		final String targetEntity = bindingManager.getTargetEntity(part);
		try {
			final Object instance = modelAccess.get(targetEntity).newInstance();
			bindingManager.setEntity(part, instance);
		} catch (final InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public final void createNew(final IWorkbenchPart part,
			final String fieldName) {
		try {
			final BindingFieldConfig fieldConfig = bindingManager.getFieldConfig(
					part,
					fieldName);
			final List<BindingConfig> bindings = fieldConfig.getBindings();
			String targetEntity = null;
			String target = null;
			for (final BindingConfig bindingConfig : bindings) {
				if (bindingConfig.getType() == GUIBindingTypes.JFACEINPUT) {
					targetEntity = bindingConfig.getTargetEntity();
					target = bindingConfig.getTarget();
				}
			}

			if (target != null) {
				final String gloTargetEntity = bindingManager.getTargetEntity(part);

				final ModelClass modelClass = modelAccess.get(gloTargetEntity);
				final ModelClass modelClass2 = modelClass.getModelClass(
						targetEntity,
						target);
				final ModelClass refClass = modelClass.getModelClassForRefernce(
						targetEntity,
						target);
				if (refClass != null) {
					final PropertyDescriptor descriptor = modelClass2.getProperties()
							.get(target);

					Object instance = null;
					if (descriptor != null) {
						instance = refClass.newInstance();

						final WritableEntityValue writableEntityValue = bindingManager.getEntityValue(part);
						final IObservableValue modelObs = (IObservableValue) modelClass.getObservable(
								targetEntity,
								target,
								writableEntityValue,
								true);

						final Object object = descriptor.getReadMethod()
								.invoke(modelObs.getValue());
						if (object instanceof Collection) {
							final Collection coll = (Collection) object;
							coll.add(instance);
						}

						for (final PropertyDescriptor desc : refClass.getProperties()
								.values()) {
							final Class<?> propertyType = desc.getPropertyType();
							try {
								propertyType.cast(modelObs.getValue());
								desc.getWriteMethod().invoke(
										instance,
										modelObs.getValue());
								break;
							} catch (final ClassCastException e) {
							}
						}
					}

					final Object entity = bindingManager.getEntity(part);
					bindingManager.setEntity(part, null);
					bindingManager.setEntity(part, entity);

					final WritableValue viewerSelect = bindingManager.getViewerSelect(
							part,
							fieldName);
					if (viewerSelect != null) {
						viewerSelect.setValue(instance);
					}
				}
			}
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void delete(final IWorkbenchPart part) {
		final Object entity = bindingManager.getEntity(part);
		persistenceManager.delete(entity);
		final WritableList entityList = bindingManager.getEntityList(part);
		if (entityList != null) {
			entityList.remove(entity);
		}
	}

	@Override
	public final void delete(final IWorkbenchPart part, final String fieldName) {
		final WritableValue viewerSelect = bindingManager.getViewerSelect(
				part,
				fieldName);
		final Object value = viewerSelect.getValue();

		try {
			final BindingFieldConfig fieldConfig = bindingManager.getFieldConfig(
					part,
					fieldName);
			final List<BindingConfig> bindings = fieldConfig.getBindings();
			String targetEntity = null;
			String target = null;
			for (final BindingConfig bindingConfig : bindings) {
				if (bindingConfig.getType() == GUIBindingTypes.JFACEINPUT) {
					targetEntity = bindingConfig.getTargetEntity();
					target = bindingConfig.getTarget();
				}
			}

			if (target != null) {
				final String gloTargetEntity = bindingManager.getTargetEntity(part);

				final ModelClass modelClass = modelAccess.get(gloTargetEntity);
				final ModelClass modelClass2 = modelClass.getModelClass(
						targetEntity,
						target);
				final ModelClass refClass = modelClass.getModelClassForRefernce(
						targetEntity,
						target);
				if (refClass != null) {
					final PropertyDescriptor descriptor = modelClass2.getProperties()
							.get(target);

					if (descriptor != null) {
						final WritableEntityValue writableEntityValue = bindingManager.getEntityValue(part);
						final IObservableValue modelObs = (IObservableValue) modelClass.getObservable(
								targetEntity,
								target,
								writableEntityValue,
								true);

						final Object object = descriptor.getReadMethod()
								.invoke(modelObs.getValue());
						if (object instanceof Collection) {
							final Collection coll = (Collection) object;
							coll.remove(value);
						}

						for (final PropertyDescriptor desc : refClass.getProperties()
								.values()) {
							final Class<?> propertyType = desc.getPropertyType();
							try {
								propertyType.cast(modelObs.getValue());
								desc.getWriteMethod().invoke(
										value,
										(Object) null);
								break;
							} catch (final ClassCastException e) {
							}
						}
					}

					final Object entity = bindingManager.getEntity(part);
					bindingManager.setEntity(part, null);
					bindingManager.setEntity(part, entity);

					viewerSelect.setValue(null);
				}
			}
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// persistenceManager.delete(value);
	}

	@Override
	public final void executeDatabaseAcces(final IWorkbenchPart part,
			final String id, final Object... args)
			throws InstantiationException, IllegalAccessException {
		List list = null;
		try {
			list = persistenceManager.executeDatabaseAcces(id, args);
		} catch (final RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final WritableList entityList = bindingManager.getEntityList(part);
		if (entityList != null) {
			bindingManager.clearList(part);
			if (list != null) {
				bindingManager.setList(part, list);
			}
		} else {
			bindingManager.setEntity(part, null);
			if (list != null && list.size() > 0) {
				bindingManager.setEntity(part, list.get(0));
			}
		}
	}

	@Override
	public final List executeDatabaseAcces(final String id,
			final Object... args) throws InstantiationException,
			IllegalAccessException {
		List list = null;
		try {
			list = persistenceManager.executeDatabaseAcces(id, args);
		} catch (final RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}

	@Override
	public final void save(final IWorkbenchPart part) {
		final Object entity = bindingManager.getEntity(part);
		persistenceManager.save(entity);
		final WritableList entityList = bindingManager.getEntityList(part);
		if (entityList != null && !entityList.contains(entity)) {
			entityList.add(entity);
		}
	}

	public final void start() {
		Activator.logInfo(0, "reap will starting");
		for (int i = 0; i < TRAILS_COUNT; i++) {
			final IExtensionRegistry reg = Platform.getExtensionRegistry();
			try {
				modelAccess = modelManager.init(reg);
				persistenceManager.start(reg, modelAccess);
				bindingManager.init(reg, modelAccess);
				break;
			} catch (final InvalidRegistryObjectException e) {
				e.printStackTrace();
				stop();
			}
		}

		final IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.addWindowListener(bindWindowListener);

		final IWorkbenchWindow[] workbenchWindows = workbench.getWorkbenchWindows();
		for (final IWorkbenchWindow workbenchWindow : workbenchWindows) {
			workbenchWindow.getPartService().addPartListener(bindPartListener);
			final IWorkbenchPage activePage = workbenchWindow.getActivePage();
			final IViewReference[] viewRefs = activePage.getViewReferences();
			for (final IViewReference viewRef : viewRefs) {
				final IViewPart view = viewRef.getView(false);
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						runningBindingProcess(view);
					}
				});
			}
			final IEditorPart activeEditor = activePage.getActiveEditor();
			if (activeEditor != null) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						runningBindingProcess(activeEditor);
					}
				});
			}
		}

		// bindingManager.start();
		Activator.logInfo(1, "reap starting ends with success");
	}

	public final void stop() {
		PlatformUI.getWorkbench().removeWindowListener(bindWindowListener);
		final IWorkbenchWindow[] workbenchWindows = PlatformUI.getWorkbench()
				.getWorkbenchWindows();
		for (final IWorkbenchWindow workbenchWindow : workbenchWindows) {
			workbenchWindow.getPartService().removePartListener(
					bindPartListener);
		}
		persistenceManager.stop();
		bindingManager.stop();
		modelManager.stop();
	}

	private void firstBinding(final IWorkbenchPart part) {
		final String databaseAccessID = bindingManager.getDatabaseAccessID(part);
		List retList = null;
		if (databaseAccessID != null && !databaseAccessID.isEmpty()) {
			try {
				retList = persistenceManager.databaseAccess(databaseAccessID);
				if (retList != null) {
					bindingManager.setList(part, retList);
				}
			} catch (final Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		final Object lastObject = lastValues.get(part.getSite().getId());
		if (lastObject != null && retList != null) {
			retList: for (final Object object : retList) {
				for (final Method method : object.getClass().getMethods()) {
					if (method.getName().startsWith("get")
							&& method.isAnnotationPresent(Id.class)) {
						try {
							final Object invoke = method.invoke(object);
							if (invoke.equals(lastObject)) {
								bindingManager.setEntity(part, object);
								break retList;
							}
							break;
						} catch (final Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							break;
						}
					}
				}
			}
		} else if (lastObject != null && retList == null) {
			final String targetEntity = bindingManager.getTargetEntity(part);
			final Object entity = persistenceManager.loadEntity(
					modelAccess.get(targetEntity).getModelClass(),
					lastObject);
			bindingManager.setEntity(part, entity);
		} else {
			final String targetEntity = bindingManager.getTargetEntity(part);
			final ModelClass modelClass = modelAccess.get(targetEntity);
			try {
				final Object instance = modelClass.newInstance();
				bindingManager.setEntity(part, instance);
			} catch (final Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private synchronized void runningBindingProcess(final IWorkbenchPart part) {
		if (part == null || !part.getSite().getPage().isPartVisible(part)
				|| !bindingManager.isBindingPart(part.getSite().getId())) {
			return;
		}

		final Class<? extends IWorkbenchPart> class1 = part.getClass();
		try {
			final Method method = class1.getMethod("beforeBindingFromREAP");
			method.setAccessible(true);
			method.invoke(part);
		} catch (final Exception e) {
			// TODO Auto-generated catch block
		}
		bindingManager.bindPart(part);
		try {
			final Method method = class1.getMethod("beforeInitFromREAP");
			method.setAccessible(true);
			method.invoke(part);
		} catch (final Exception e) {
			// TODO Auto-generated catch block
		}
		firstBinding(part);
		try {
			final Method method = class1.getMethod(
					"afterInitFromREAP",
					IREAPContext.class);
			method.setAccessible(true);
			method.invoke(part, this);
		} catch (final Exception e) {
			// TODO Auto-generated catch block
		}
	}

	private synchronized void runningUnbindingProcess(final IWorkbenchPart part) {
		if (part == null
				|| !bindingManager.isBindingPart(part.getSite().getId())) {
			return;
		}

		final Object entity = bindingManager.getEntity(part);
		if (entity != null) {
			Object id = null;
			for (final Method method : entity.getClass().getMethods()) {
				if (method.getName().startsWith("get")
						&& method.isAnnotationPresent(Id.class)) {
					try {
						id = method.invoke(entity);
						break;
					} catch (final Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}
				}
			}
			lastValues.put(part.getSite().getId(), id);
		}

		bindingManager.unbindPart(part);
	}
}
