/*
 * PersistenceManager.java -- TODO Insert a short describtion of this file.
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
package org.reap.internal.core.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.reap.core.IDatabaseAccess;
import org.reap.core.IORMManager;
import org.reap.internal.core.Activator;
import org.reap.internal.core.Constants;
import org.reap.internal.core.ModelClass;
import org.reap.internal.core.ModelManager.ModelAccess;
import org.reap.internal.core.persistence.config.PersistenceConfig;
import org.reap.internal.core.persistence.config.PersistenceConfigFactory;
import org.reap.internal.core.persistence.config.PersistenceConfigPage;

public class PersistenceManager {

	private static final int					TRAILS_COUNT		= 3;

	private int									configDialogState;

	private final Map<String, DatabaseAccess>	databaseAccesses	= new HashMap<String, DatabaseAccess>();

	private EntityManager						entityManager;

	private ModelAccess							modelAccess;

	private IORMManager							ormManager;

	public final List databaseAccess(final String databaseAccessID)
			throws InstantiationException, IllegalAccessException {
		final DatabaseAccess access = databaseAccesses.get(databaseAccessID);
		if (access != null && access.isWithoutArgumentsOk()) {
			final IDatabaseAccess newInstance = access.newInstance();
			newInstance.setEntityManager(entityManager);
			return newInstance.databaseAccess();
		}
		return null;
	}

	public final void delete(final Object entity) {
		entityManager.getTransaction().begin();
		entityManager.remove(entity);
		entityManager.getTransaction().commit();
	}

	public final List executeDatabaseAcces(final String id,
			final Object... args) throws InstantiationException,
			IllegalAccessException {
		final DatabaseAccess access = databaseAccesses.get(id);
		if (access != null) {
			final IDatabaseAccess newInstance = access.newInstance();
			newInstance.setEntityManager(entityManager);
			return newInstance.databaseAccess(args);
		}
		return null;
	}

	/**
	 * @return the available
	 */
	public final boolean isAvailable() {
		return entityManager != null;
	}

	public final Object loadEntity(final Class<?> entityClass, final Object key) {
		return entityManager.find(entityClass, key);
	}

	public final void save(final Object entity) {
		try {
			entityManager.getTransaction().begin();
			entityManager.persist(entity);
		} finally {
			entityManager.getTransaction().commit();
		}
	}

	public final void start(final IExtensionRegistry reg,
			final ModelAccess modelAccess) {
		this.modelAccess = modelAccess;

		checkConfig(reg, 0);
		Activator.getDefault().savePluginPreferences();

		if (isAvailable()) {
			searchDatabaseAccesses(reg);
		}
	}

	public final void stop() {
		entityManager = null;
		if (ormManager != null) {
			ormManager.close();
		}
		ormManager = null;
		modelAccess = null;
		databaseAccesses.clear();
	}

	private void checkConfig(final IExtensionRegistry reg, final int deep) {
		if (deep >= TRAILS_COUNT) {
			return;
		}

		final PersistenceConfig config = PersistenceConfigFactory.createPersistenceConfig(
				reg,
				Activator.getDefault().getPreferenceStore());

		if (config == null) {
			return;
		}

		if (config.isValid()) {
			configORMapper(config);
			try {
				entityManager = ormManager.getEntityManager();
			} catch (final RuntimeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		showConfigDialog(config);
		if (configDialogState == Window.OK) {
			checkConfig(reg, deep + 1);
		} else {
			return;
		}
	}

	private IORMManager configORMapper(final PersistenceConfig config) {
		ormManager = config.getOrmManager().getOrmManager();
		if (!config.isUseSystemProperties()) {

			if (config.isDialectFreeSetting()) {
				ormManager.setDialect(config.getDialectConfigFree());
			} else {
				ormManager.setDialect(config.getDialectConfig().getDialect());
			}

			if (config.isDriverFreeSetting()) {
				ormManager.setConnectionDriver(config.getDriverConfigFree());
			} else {
				ormManager.setConnectionDriver(config.getDriverConfig()
						.getDriver());
			}

			final StringBuffer url = new StringBuffer();
			url.append("jdbc:")
					.append(config.getDatabaseSystem().getSubProtocol())
					.append("://")
					.append(config.getHost())
					.append(":")
					.append(config.getPort())
					.append("/")
					.append(config.getDatabase());

			ormManager.setConnectionURL(url.toString());
			ormManager.setUsername(config.getUsername());
			ormManager.setPassword(config.getPassword());
		}

		final Properties properties = new Properties();
		properties.put("hibernate.hbm2ddl.auto", "update");
		// properties.put("hibernate.bytecode.provider", "javassist");
		ormManager.setProperties(properties);

		for (final ModelClass modelClass : modelAccess.getValues()) {
			ormManager.addPersistentClass(modelClass.getModelClass());
		}
		return ormManager;
	}

	private void searchDatabaseAccesses(final IExtensionRegistry reg) {
		final IConfigurationElement[] elements = reg.getConfigurationElementsFor(
				Activator.PLUGIN_ID,
				Constants.DATABASE_ACCESSES_EXTENSION_POINT_ID);
		for (final IConfigurationElement element : elements) {
			if (element.getName().equals("databaseAccess")) {
				final String id = element.getAttribute("id");
				if (id == null || id.isEmpty()) {
					Activator.logWarning(
							0,
							"Id of a database access is NULL or empty - the database access extension was ignored!");
					continue;
				}

				try {
					DatabaseAccess databaseAccess = null;
					final IDatabaseAccess access = (IDatabaseAccess) element.createExecutableExtension("class");
					final boolean withoutArgumentsOk = access.isWithoutArgumentsOk();
					databaseAccess = new DatabaseAccess(id, withoutArgumentsOk,
							access.getClass());
					databaseAccesses.put(id, databaseAccess);
				} catch (final CoreException e) {
					// TODO process instantiation exceptions here
					e.printStackTrace();
				}
			}
		}
	}

	private void showConfigDialog(final PersistenceConfig config) {
		final Display display = Activator.getDefault()
				.getWorkbench()
				.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				final PreferenceManager manager = new PreferenceManager();
				final PreferenceDialog dialog = new PreferenceDialog(new Shell(
						display), manager);
				final PreferencePage page = new PersistenceConfigPage(config);
				final IPreferenceNode node = new PreferenceNode("Datenbank",
						page);
				manager.addToRoot(node);
				configDialogState = dialog.open();
			}
		});
	}
}
