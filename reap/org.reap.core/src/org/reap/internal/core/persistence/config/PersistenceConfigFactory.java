/*
 * PersistenceConfigFactory.java -- TODO Insert a short describtion of this file. 
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
package org.reap.internal.core.persistence.config;

import java.sql.Driver;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.reap.core.IORMManager;
import org.reap.internal.core.Activator;
import org.reap.internal.core.Constants;

public final class PersistenceConfigFactory {

	public static PersistenceConfig createPersistenceConfig(
			final IExtensionRegistry reg, final IPreferenceStore store) {
		final PersistenceConfig config = new PersistenceConfig();

		if (searchORMappers(reg, config) == 0) {
			return null;
		}

		if (searchDatabaseSystems(reg, config) == 0) {
			return null;
		}

		readPreferences(store, config);

		return config;
	}

	private static void readPreferences(final IPreferenceStore store,
			final PersistenceConfig config) {
		final String ormManager = store.getString(PersistenceConfigConstants.ORM_MANAGER);
		config.setORMManager(ormManager);
		final String databaseSystem = store.getString(PersistenceConfigConstants.DATABASE_SYSTEM);
		config.setDatabaseSystem(databaseSystem);

		final String host = store.getString(PersistenceConfigConstants.HOST);
		config.setHost(host);
		final String port = store.getString(PersistenceConfigConstants.PORT);
		config.setPort(port);
		final String database = store.getString(PersistenceConfigConstants.DATABASE);
		config.setDatabase(database);
		final String username = store.getString(PersistenceConfigConstants.USERNAME);
		config.setUsername(username);
		final String password = store.getString(PersistenceConfigConstants.PASSWORD);
		config.setPassword(password);

		final boolean useSystemProperties = store.getBoolean(PersistenceConfigConstants.USE_SYSTEM_PROPERTIES);
		config.setUseSystemProperties(useSystemProperties);

		final boolean jdbcDriverFreeSetting = store.getBoolean(PersistenceConfigConstants.JDBC_DRIVER_FREE_SETTING);
		config.setDriverFreeSetting(jdbcDriverFreeSetting);
		final boolean sqlDialectFreeSetting = store.getBoolean(PersistenceConfigConstants.SQL_DIALECT_FREE_SETTING);
		config.setDialectFreeSetting(sqlDialectFreeSetting);

		final String jdbcDriver = store.getString(PersistenceConfigConstants.JDBC_DRIVER);
		if (jdbcDriverFreeSetting) {
			config.setDriverConfigFree(jdbcDriver);
		} else {
			config.setDriverConfig(jdbcDriver);
		}
		final String sqlDialect = store.getString(PersistenceConfigConstants.SQL_DIALECT);
		if (sqlDialectFreeSetting) {
			config.setDialectConfigFree(sqlDialect);
		} else {
			config.setDialectConfig(sqlDialect);
		}
	}

	private static int searchDatabaseSystems(final IExtensionRegistry reg,
			final PersistenceConfig config)
			throws InvalidRegistryObjectException {
		int count = 0;

		final IConfigurationElement[] elements = reg.getConfigurationElementsFor(
				Activator.PLUGIN_ID,
				Constants.DATABASE_SYSTEM_EXTENSION_POINT_ID);
		for (final IConfigurationElement element : elements) {
			if (element.getName().equals("databaseSystem")) {
				final String subProtocol = element.getAttribute("subprotocol");
				final String id = element.getAttribute("id");
				final String name = element.getAttribute("name");
				final DatabaseSystemConfig databaseSystemConfig = new DatabaseSystemConfig(
						id, name, subProtocol);

				final IConfigurationElement[] driverClasses = element.getChildren("jdbcDriver");
				for (final IConfigurationElement driverClass : driverClasses) {
					try {
						final Driver driver = (Driver) driverClass.createExecutableExtension("class");
						final String isDefault = driverClass.getAttribute("isDefault");
						final boolean parseBoolean = Boolean.parseBoolean(isDefault);
						final DriverConfig driverConfig = new DriverConfig(
								driver.getClass().getName(), parseBoolean);
						databaseSystemConfig.addDriverConfig(driverConfig);
					} catch (final CoreException e) {
						// TODO process instantiation exceptions here
						e.printStackTrace();
					}
				}

				final IConfigurationElement[] dialectClasses = element.getChildren("sqlDialect");
				for (final IConfigurationElement dialectClass : dialectClasses) {
					final String ormapperID = dialectClass.getAttribute("ormapperID");
					final String dialect = dialectClass.getAttribute("class");
					final String isDefault = dialectClass.getAttribute("isDefault");
					final boolean parseBoolean = Boolean.parseBoolean(isDefault);
					final DialectConfig dialectClassConfig = new DialectConfig(
							dialect, ormapperID, parseBoolean);
					databaseSystemConfig.addDialectConfig(dialectClassConfig);
				}
				config.addDatabaseSystemConfig(databaseSystemConfig);
				count++;
			}
		}
		return count;
	}

	private static int searchORMappers(final IExtensionRegistry reg,
			final PersistenceConfig config)
			throws InvalidRegistryObjectException {
		int count = 0;

		final IConfigurationElement[] elements = reg.getConfigurationElementsFor(
				Activator.PLUGIN_ID,
				Constants.ORMAPPERS_EXTENSION_POINT_ID);
		for (final IConfigurationElement element : elements) {
			if (element.getName().equals("ormapper")) {
				final String id = element.getAttribute("id");
				final String name = element.getAttribute("name");
				try {
					final IORMManager ormManager = (IORMManager) element.createExecutableExtension("class");
					final ORMManagerConfig managerConfig = new ORMManagerConfig(
							id, name, ormManager);

					final IConfigurationElement[] dialectClasses = element.getChildren("sqlDialectClass");
					for (final IConfigurationElement dialectClass : dialectClasses) {
						final String jdbcSubprotocol = dialectClass.getAttribute("jdbcSubprotocol");
						final String dialect = dialectClass.getAttribute("class");
						final String isDefault = dialectClass.getAttribute("isDefault");
						final boolean parseBoolean = Boolean.parseBoolean(isDefault);
						managerConfig.addDialectClassConfig(new DialectConfig(
								dialect, jdbcSubprotocol, parseBoolean));
					}

					config.addORMManagerConfig(managerConfig);
					count++;
				} catch (final CoreException e) {
					// TODO process instantiation exceptions here
					e.printStackTrace();
				}
			}
		}
		return count;
	}

	private PersistenceConfigFactory() {
	}
}
