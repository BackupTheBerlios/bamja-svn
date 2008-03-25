/*
 * ORMConfigBase.java -- TODO Insert a short describtion of this file. 
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PersistenceConfig {

	private String								database			= "";
	private DatabaseSystemConfig				databaseSystem		= null;
	private final List<DatabaseSystemConfig>	databaseSystems		= new ArrayList<DatabaseSystemConfig>();
	private DialectConfig						dialectConfig		= null;
	private String								dialectConfigFree	= "";
	private boolean								dialectFreeSetting	= false;
	private DriverConfig						driverConfig		= null;
	private String								driverConfigFree	= "";
	private boolean								driverFreeSetting	= false;
	private String								host				= "";
	private ORMManagerConfig					ormManager			= null;
	private final List<ORMManagerConfig>		ormManagers			= new ArrayList<ORMManagerConfig>();
	private String								password			= "";
	private String								port				= "";
	private String								username			= "";
	private boolean								useSystemProperties	= false;

	public final void addDatabaseSystemConfig(final DatabaseSystemConfig config) {
		databaseSystems.add(config);
	}

	public final void addORMManagerConfig(final ORMManagerConfig config) {
		ormManagers.add(config);
	}

	/**
	 * @return the database
	 */
	public final String getDatabase() {
		return database;
	}

	/**
	 * @return the databaseSystem
	 */
	public final DatabaseSystemConfig getDatabaseSystem() {
		if (databaseSystem == null && !databaseSystems.isEmpty()) {
			return databaseSystems.get(0);
		}
		return databaseSystem;
	}

	/**
	 * @return the databaseSystems
	 */
	public final List<DatabaseSystemConfig> getDatabaseSystems() {
		return databaseSystems;
	}

	/**
	 * @return the dialectConfig
	 */
	public final DialectConfig getDialectConfig() {
		final DatabaseSystemConfig ds = getDatabaseSystem();
		final ORMManagerConfig ormM = getOrmManager();
		if (dialectConfig == null && ds != null && ormM != null) {
			DialectConfig first = null;
			for (final DialectConfig dialectConfig : ormM.getDialectConfigs()) {
				if (dialectConfig.getReference().equals(ds.getSubProtocol())) {
					if (first == null) {
						first = dialectConfig;
					}
					if (dialectConfig.isDefaultDialect()) {
						return dialectConfig;
					}
				}
			}
			if (first != null) {
				return first;
			}

			for (final DialectConfig dialectConfig : ds.getDialectConfigs()) {
				if (dialectConfig.getReference().equals(ormM.getId())) {
					if (first == null) {
						first = dialectConfig;
					}
					if (dialectConfig.isDefaultDialect()) {
						return dialectConfig;
					}
				}
			}
			return first;
		}
		return dialectConfig;
	}

	/**
	 * @return the dialectConfigFree
	 */
	public final String getDialectConfigFree() {
		return dialectConfigFree;
	}

	/**
	 * @return the driverConfig
	 */
	public final DriverConfig getDriverConfig() {
		DatabaseSystemConfig ds = getDatabaseSystem();
		if (driverConfig == null && ds != null) {
			final Set<DriverConfig> driverConfigs = ds.getDriverConfigs();
			if (!driverConfigs.isEmpty()) {
				for (final DriverConfig driverConfig : driverConfigs) {
					if (driverConfig.isDefaultDriver()) {
						return driverConfig;
					}
				}
				return driverConfigs.iterator().next();
			}
		}
		return driverConfig;
	}

	/**
	 * @return the driverConfigFree
	 */
	public final String getDriverConfigFree() {
		return driverConfigFree;
	}

	/**
	 * @return the host
	 */
	public final String getHost() {
		return host;
	}

	/**
	 * @return the ormManager
	 */
	public final ORMManagerConfig getOrmManager() {
		if (ormManager == null && !ormManagers.isEmpty()) {
			return ormManagers.get(0);
		}
		return ormManager;
	}

	/**
	 * @return the ormManagers
	 */
	public final List<ORMManagerConfig> getOrmManagers() {
		return ormManagers;
	}

	/**
	 * @return the password
	 */
	public final String getPassword() {
		return password;
	}

	/**
	 * @return the port
	 */
	public final String getPort() {
		return port;
	}

	/**
	 * @return the username
	 */
	public final String getUsername() {
		return username;
	}

	/**
	 * @return the sqlDialectManualSetting
	 */
	public final boolean isDialectFreeSetting() {
		return dialectFreeSetting;
	}

	/**
	 * @return the jdbcDriverManualSetting
	 */
	public final boolean isDriverFreeSetting() {
		return driverFreeSetting;
	}

	/**
	 * @return the useSystemProperties
	 */
	public final boolean isUseSystemProperties() {
		return useSystemProperties;
	}

	public final boolean isValid() {
		if (useSystemProperties) {
			return true;
		}

		if (ormManager != null && databaseSystem != null && !host.isEmpty()
				&& !port.isEmpty() && !database.isEmpty()
				&& !username.isEmpty() && !password.isEmpty()) {
			if (driverFreeSetting ? !driverConfigFree.isEmpty()
					: driverConfig != null && dialectFreeSetting ? !dialectConfigFree.isEmpty()
							: dialectConfig != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param database the database to set
	 */
	public final void setDatabase(final String database) {
		this.database = database;
	}

	/**
	 * @param databaseSystem the databaseSystem to set
	 */
	public final void setDatabaseSystem(
			final DatabaseSystemConfig databaseSystem) {
		this.databaseSystem = databaseSystem;
		dialectConfig = null;
		driverConfig = null;
	}

	/**
	 * @param databaseSystem the databaseSystem to set
	 */
	public final void setDatabaseSystem(final String databaseSystemId) {
		for (final DatabaseSystemConfig databaseSystemConfig : databaseSystems) {
			if (databaseSystemConfig.getId().equals(databaseSystemId)) {
				databaseSystem = databaseSystemConfig;
				break;
			}
		}
	}

	/**
	 * @param dialectConfig the dialectConfig to set
	 */
	public final void setDialectConfig(final DialectConfig dialectConfig) {
		this.dialectConfig = dialectConfig;
	}

	public final void setDialectConfig(final String sqlDialect) {
		if (ormManager != null && databaseSystem != null) {
			for (final DialectConfig dialectConfig : ormManager.getDialectConfigs()) {
				if (dialectConfig.getDialect().equals(sqlDialect)
						&& dialectConfig.getReference().equals(
								databaseSystem.getSubProtocol())) {
					this.dialectConfig = dialectConfig;
					return;
				}
			}
			for (final DialectConfig dialectConfig : databaseSystem.getDialectConfigs()) {
				if (dialectConfig.getDialect().equals(sqlDialect)
						&& dialectConfig.getReference().equals(
								ormManager.getId())) {
					this.dialectConfig = dialectConfig;
					return;
				}
			}
		}
	}

	/**
	 * @param dialectConfigFree the dialectConfigFree to set
	 */
	public final void setDialectConfigFree(final String dialectConfigFree) {
		this.dialectConfigFree = dialectConfigFree;
	}

	/**
	 * @param sqlDialectManualSetting the sqlDialectManualSetting to set
	 */
	public final void setDialectFreeSetting(
			final boolean sqlDialectManualSetting) {
		dialectFreeSetting = sqlDialectManualSetting;
	}

	/**
	 * @param driverConfig the driverConfig to set
	 */
	public final void setDriverConfig(final DriverConfig driverConfig) {
		this.driverConfig = driverConfig;
	}

	public final void setDriverConfig(final String jdbcDriver) {
		if (databaseSystem != null) {
			for (final DriverConfig driverConfig : databaseSystem.getDriverConfigs()) {
				if (driverConfig.getDriver().equals(jdbcDriver)) {
					this.driverConfig = driverConfig;
					break;
				}
			}
		}
	}

	/**
	 * @param driverConfigFree the driverConfigFree to set
	 */
	public final void setDriverConfigFree(final String driverConfigFree) {
		this.driverConfigFree = driverConfigFree;
	}

	/**
	 * @param jdbcDriverManualSetting the jdbcDriverManualSetting to set
	 */
	public final void setDriverFreeSetting(final boolean jdbcDriverManualSetting) {
		driverFreeSetting = jdbcDriverManualSetting;
	}

	/**
	 * @param host the host to set
	 */
	public final void setHost(final String host) {
		this.host = host;
	}

	/**
	 * @param ormManager the ormManager to set
	 */
	public final void setOrmManager(final ORMManagerConfig ormManager) {
		this.ormManager = ormManager;
		dialectConfig = null;
	}

	public final void setORMManager(final String ormManagerID) {
		for (final ORMManagerConfig managerConfig : ormManagers) {
			if (managerConfig.getId().equals(ormManagerID)) {
				ormManager = managerConfig;
				break;
			}
		}
	}

	/**
	 * @param password the password to set
	 */
	public final void setPassword(final String password) {
		this.password = password;
	}

	/**
	 * @param port the port to set
	 */
	public final void setPort(final String port) {
		this.port = port;
	}

	/**
	 * @param username the username to set
	 */
	public final void setUsername(final String username) {
		this.username = username;
	}

	/**
	 * @param useSystemProperties the useSystemProperties to set
	 */
	public final void setUseSystemProperties(final boolean useSystemProperties) {
		this.useSystemProperties = useSystemProperties;
	}
}
