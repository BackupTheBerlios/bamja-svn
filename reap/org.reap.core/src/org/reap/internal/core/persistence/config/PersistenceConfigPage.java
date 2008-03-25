/*
 * ORMPreferencePage.java -- TODO Insert a short describtion of this file.
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

import java.util.HashSet;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.masterdetail.MasterDetailObservables;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.databinding.viewers.ObservableSetContentProvider;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.reap.internal.core.Activator;

public class PersistenceConfigPage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private final class NegateBoolean extends UpdateValueStrategy {
		@Override
		public Object convert(final Object value) {
			super.convert(value);
			if (value instanceof Boolean) {
				Boolean select = (Boolean) value;
				select = !select;
				return select;
			}
			return value;
		}
	}

	private Composite			comp;

	private PersistenceConfig	config;

	private ComboViewer			crmViewer;
	private ComboViewer			databaseSystemViewer;
	private Text				databaseText;
	private Combo				dialectCombo;
	private Text				dialectText;
	private ComboViewer			dialectViewer;
	private Button				freeDialectInput;
	private Button				freeDriverInput;
	private Text				hostText;
	private Combo				jdbcCombo;
	private Text				jdbcText;
	private ComboViewer			jdbcViewer;
	private Text				passwordText;
	private Text				portText;
	private Label				urlLabel;
	private Text				usernameText;
	private Button				useSystemProperties;

	public PersistenceConfigPage() {
		super();
	}

	public PersistenceConfigPage(final PersistenceConfig config) {
		super();
		this.config = config;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	@Override
	public final void dispose() {
		super.dispose();
	}

	@Override
	public final void init(final IWorkbench workbench) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public final boolean performOk() {
		final IPreferenceStore store = Activator.getDefault()
				.getPreferenceStore();

		store.setValue(
				PersistenceConfigConstants.USE_SYSTEM_PROPERTIES,
				config.isUseSystemProperties());

		store.setValue(PersistenceConfigConstants.HOST, config.getHost());
		store.setValue(PersistenceConfigConstants.PORT, config.getPort());
		store.setValue(
				PersistenceConfigConstants.DATABASE,
				config.getDatabase());
		store.setValue(
				PersistenceConfigConstants.USERNAME,
				config.getUsername());
		store.setValue(
				PersistenceConfigConstants.PASSWORD,
				config.getPassword());

		store.setValue(
				PersistenceConfigConstants.JDBC_DRIVER_FREE_SETTING,
				config.isDriverFreeSetting());
		store.setValue(
				PersistenceConfigConstants.JDBC_DRIVER,
				config.isDriverFreeSetting() ? config.getDriverConfigFree()
						: config.getDriverConfig().getDriver());

		store.setValue(
				PersistenceConfigConstants.SQL_DIALECT_FREE_SETTING,
				config.isDialectFreeSetting());
		store.setValue(
				PersistenceConfigConstants.SQL_DIALECT,
				config.isDialectFreeSetting() ? config.getDialectConfigFree()
						: config.getDialectConfig().getDialect());

		store.setValue(
				PersistenceConfigConstants.ORM_MANAGER,
				config.getOrmManager().getId());
		store.setValue(
				PersistenceConfigConstants.DATABASE_SYSTEM,
				config.getDatabaseSystem().getId());

		return super.performOk();
	}

	private void bind() {
		if (config == null) {
			final IExtensionRegistry reg = Platform.getExtensionRegistry();
			final IPreferenceStore store = Activator.getDefault()
					.getPreferenceStore();
			config = PersistenceConfigFactory.createPersistenceConfig(
					reg,
					store);
		}

		final DataBindingContext context = new DataBindingContext();

		final IObservableValue observeUseSystemProperties = BeansObservables.observeValue(
				config,
				"useSystemProperties");
		context.bindValue(
				SWTObservables.observeSelection(useSystemProperties),
				observeUseSystemProperties,
				null,
				null);
		context.bindValue(
				SWTObservables.observeVisible(comp),
				observeUseSystemProperties,
				null,
				new NegateBoolean());

		final IObservableValue observeHost = BeansObservables.observeValue(
				config,
				"host");
		context.bindValue(
				SWTObservables.observeText(hostText, SWT.Modify),
				observeHost,
				null,
				null);
		final IObservableValue observePort = BeansObservables.observeValue(
				config,
				"port");
		context.bindValue(
				SWTObservables.observeText(portText, SWT.Modify),
				observePort,
				null,
				null);
		final IObservableValue observeDatabase = BeansObservables.observeValue(
				config,
				"database");
		context.bindValue(
				SWTObservables.observeText(databaseText, SWT.Modify),
				observeDatabase,
				null,
				null);
		context.bindValue(
				SWTObservables.observeText(usernameText, SWT.Modify),
				BeansObservables.observeValue(config, "username"),
				null,
				null);
		context.bindValue(
				SWTObservables.observeText(passwordText, SWT.Modify),
				BeansObservables.observeValue(config, "password"),
				null,
				null);

		final IObservableValue observejdbcFreeSetting = BeansObservables.observeValue(
				config,
				"driverFreeSetting");
		context.bindValue(
				SWTObservables.observeSelection(freeDriverInput),
				observejdbcFreeSetting,
				null,
				null);
		context.bindValue(
				SWTObservables.observeEnabled(jdbcCombo),
				observejdbcFreeSetting,
				null,
				new NegateBoolean());
		context.bindValue(
				SWTObservables.observeEnabled(jdbcText),
				observejdbcFreeSetting,
				null,
				null);
		final IObservableValue observeDialectFreeSetting = BeansObservables.observeValue(
				config,
				"dialectFreeSetting");
		context.bindValue(
				SWTObservables.observeSelection(freeDialectInput),
				observeDialectFreeSetting,
				null,
				null);
		context.bindValue(
				SWTObservables.observeEnabled(dialectCombo),
				observeDialectFreeSetting,
				null,
				new NegateBoolean());
		context.bindValue(
				SWTObservables.observeEnabled(dialectText),
				observeDialectFreeSetting,
				null,
				null);

		final IObservableMap attributeMapOrm = BeansObservables.observeMap(
				Observables.staticObservableSet(new HashSet<ORMManagerConfig>(
						config.getOrmManagers())),
				ORMManagerConfig.class,
				"name");
		crmViewer.setLabelProvider(new ObservableMapLabelProvider(
				attributeMapOrm));
		crmViewer.setContentProvider(new ArrayContentProvider());
		crmViewer.setInput(config.getOrmManagers());
		final IObservableValue observeOrmManager = BeansObservables.observeValue(
				config,
				"ormManager");
		context.bindValue(
				ViewersObservables.observeSingleSelection(crmViewer),
				observeOrmManager,
				null,
				null);

		final IObservableMap attributeMapDatabase = BeansObservables.observeMap(
				Observables.staticObservableSet(new HashSet<DatabaseSystemConfig>(
						config.getDatabaseSystems())),
				DatabaseSystemConfig.class,
				"name");
		databaseSystemViewer.setLabelProvider(new ObservableMapLabelProvider(
				attributeMapDatabase));
		databaseSystemViewer.setContentProvider(new ArrayContentProvider());
		databaseSystemViewer.setInput(config.getDatabaseSystems());
		final IObservableValue observeDatabankSystem = BeansObservables.observeValue(
				config,
				"databaseSystem");
		context.bindValue(
				ViewersObservables.observeSingleSelection(databaseSystemViewer),
				observeDatabankSystem,
				null,
				null);

		final IObservableSet observeDetailSet = BeansObservables.observeDetailSet(
				Realm.getDefault(),
				observeDatabankSystem,
				"driverConfigs",
				DriverConfig.class);
		final IObservableMap attributeMapDriverConfig = BeansObservables.observeMap(
				observeDetailSet,
				DriverConfig.class,
				"driver");
		jdbcViewer.setLabelProvider(new ObservableMapLabelProvider(
				attributeMapDriverConfig));
		jdbcViewer.setContentProvider(new ObservableSetContentProvider());
		jdbcViewer.setInput(observeDetailSet);
		context.bindValue(
				ViewersObservables.observeSingleSelection(jdbcViewer),
				BeansObservables.observeValue(config, "driverConfig"),
				null,
				null);

		context.bindValue(
				SWTObservables.observeText(jdbcText, SWT.Modify),
				BeansObservables.observeValue(config, "driverConfigFree"),
				null,
				null);

		final IObservableSet dialectSet = MasterDetailObservables.detailSet(
				new ComputedValue() {
					@Override
					protected Object calculate() {
						ORMManagerConfig value = (ORMManagerConfig) observeOrmManager.getValue();
						DatabaseSystemConfig value2 = (DatabaseSystemConfig) observeDatabankSystem.getValue();

						final WritableSet retSet = WritableSet.withElementType(DialectConfig.class);

						if (value != null && value2 != null) {
							for (DialectConfig dialectConfig : value.getDialectConfigs()) {
								if (dialectConfig.getReference().equals(
										value2.getSubProtocol())) {
									retSet.add(dialectConfig);
								}
							}
							databaseDialects: for (DialectConfig dialectConfig : value2.getDialectConfigs()) {
								if (dialectConfig.getReference().equals(
										value.getId())) {
									for (Object o : retSet) {
										if (((DialectConfig) o).getDialect()
												.equals(
														dialectConfig.getDialect())) {
											continue databaseDialects;
										}
									}
									retSet.add(dialectConfig);
								}
							}
						}

						return retSet;
					}
				},
				new IObservableFactory() {
					public IObservable createObservable(final Object target) {
						return (WritableSet) target;
					}
				},
				DialectConfig.class);

		final IObservableMap attributeMapDialectConfig = BeansObservables.observeMap(
				dialectSet,
				DialectConfig.class,
				"dialect");
		dialectViewer.setLabelProvider(new ObservableMapLabelProvider(
				attributeMapDialectConfig));
		dialectViewer.setContentProvider(new ObservableSetContentProvider());
		dialectViewer.setInput(dialectSet);
		context.bindValue(
				ViewersObservables.observeSingleSelection(dialectViewer),
				BeansObservables.observeValue(config, "dialectConfig"),
				null,
				null);

		context.bindValue(
				SWTObservables.observeText(dialectText, SWT.Modify),
				BeansObservables.observeValue(config, "dialectConfigFree"),
				null,
				null);

		context.bindValue(
				SWTObservables.observeText(urlLabel),
				new ComputedValue() {
					@Override
					protected Object calculate() {
						final StringBuffer buffer = new StringBuffer();
						buffer.append("jdbc:")
								.append(
										((DatabaseSystemConfig) observeDatabankSystem.getValue()).getSubProtocol())
								.append("://")
								.append(observeHost.getValue())
								.append(":")
								.append(observePort.getValue())
								.append("/")
								.append(observeDatabase.getValue());
						return buffer.toString();
					}
				},
				null,
				null);
	}

	@Override
	protected final Control createContents(final Composite parent) {
		final Composite comp1 = new Composite(parent, SWT.NONE);
		comp1.setLayout(new RowLayout(SWT.VERTICAL));

		useSystemProperties = new Button(comp1, SWT.CHECK);
		useSystemProperties.setText("Nutzt Systemvariablen");

		comp = new Composite(comp1, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));

		final GridDataFactory gridData = GridDataFactory.swtDefaults().hint(
				300,
				SWT.DEFAULT);

		new Label(comp, SWT.NONE).setText("O/R-Abbilder:");
		final Combo crmCombo = new Combo(comp, SWT.BORDER | SWT.READ_ONLY);
		crmViewer = new ComboViewer(crmCombo);
		gridData.applyTo(crmCombo);

		new Label(comp, SWT.NONE).setText("Datenbanksystem:");
		final Combo databaseSystemCombo = new Combo(comp, SWT.BORDER
				| SWT.READ_ONLY);
		databaseSystemViewer = new ComboViewer(databaseSystemCombo);
		gridData.applyTo(databaseSystemCombo);

		new Label(comp, SWT.NONE).setText("Freie Dialekteingabe:");
		freeDriverInput = new Button(comp, SWT.CHECK);

		new Label(comp, SWT.NONE).setText("JDBC-Treiber:");
		jdbcCombo = new Combo(comp, SWT.BORDER | SWT.READ_ONLY);
		jdbcViewer = new ComboViewer(jdbcCombo);
		gridData.applyTo(jdbcCombo);

		new Label(comp, SWT.NONE).setText("JDBC-Treiber:");
		jdbcText = new Text(comp, SWT.BORDER);
		gridData.applyTo(jdbcText);

		new Label(comp, SWT.NONE).setText("Freie Dialekteingabe:");
		freeDialectInput = new Button(comp, SWT.CHECK);

		new Label(comp, SWT.NONE).setText("SQL-Dialekt:");
		dialectCombo = new Combo(comp, SWT.BORDER | SWT.READ_ONLY);
		dialectViewer = new ComboViewer(dialectCombo);
		gridData.applyTo(dialectCombo);

		new Label(comp, SWT.NONE).setText("SQL-Dialekt:");
		dialectText = new Text(comp, SWT.BORDER);
		gridData.applyTo(dialectText);

		new Label(comp, SWT.NONE).setText("Adresse:");
		hostText = new Text(comp, SWT.BORDER);
		gridData.applyTo(hostText);

		new Label(comp, SWT.NONE).setText("Port:");
		portText = new Text(comp, SWT.BORDER);
		gridData.applyTo(portText);

		new Label(comp, SWT.NONE).setText("Datenbank:");
		databaseText = new Text(comp, SWT.BORDER);
		gridData.applyTo(databaseText);

		new Label(comp, SWT.NONE).setText("Nutzername:");
		usernameText = new Text(comp, SWT.BORDER);
		gridData.applyTo(usernameText);

		new Label(comp, SWT.NONE).setText("Passwort:");
		passwordText = new Text(comp, SWT.BORDER | SWT.PASSWORD);
		gridData.applyTo(passwordText);

		new Label(comp, SWT.NONE).setText("Verbindungs-URL:");
		urlLabel = new Label(comp, SWT.NONE);
		gridData.applyTo(urlLabel);

		noDefaultAndApplyButton();

		bind();

		return comp1;
	}
}
