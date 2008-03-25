/*
 * PreBinding.java -- TODO Insert a short describtion of this file. 
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

import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateListStrategy;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.reap.internal.core.Activator;
import org.reap.internal.core.ModelObject;
import org.reap.internal.core.binding.config.BindingConfig;
import org.reap.internal.core.binding.config.UpdateStrategyConfig;

@Deprecated
public class PreBinding {

	private final BindingConfig			bindingConfig;
	private final IObservable			obs;
	private List<PreBinding>			preBindingList;
	private final UpdateListStrategy[]	updateListStrategies	= new UpdateListStrategy[2];
	private final UpdateValueStrategy[]	updateValueStrategies	= new UpdateValueStrategy[2];

	public PreBinding(final IObservable obs, final BindingConfig bindingConfig) {
		this.obs = obs;
		this.bindingConfig = bindingConfig;
		// bindingConfig.addPreBinding(this);
		createStrategies(bindingConfig.getUpdateStrategies());
	}

	public final void bind(final ModelObject instance) {
		final DataBindingContext dbc = Activator.getDefault()
				.getDataBindingContext();
		if (obs instanceof IObservableList) {
			dbc.bindList(
					(IObservableList) obs,
					(IObservableList) instance.getObservable(bindingConfig.getTarget()),
					updateListStrategies[0],
					updateListStrategies[1]);
		} else if (obs instanceof IObservableValue) {
			dbc.bindValue(
					(IObservableValue) obs,
					(IObservableValue) instance.getObservable(bindingConfig.getTarget()),
					updateValueStrategies[0],
					updateValueStrategies[1]);
		}
	}

	public final void dispose() {
		// bindingConfig.removePreBinding(this);
		if (preBindingList != null) {
			preBindingList.remove(this);
		}
	}

	public final void setPreBindingList(final List<PreBinding> list) {
		preBindingList = list;
	}

	private void createStrategies(
			final List<UpdateStrategyConfig> updateStrategies) {
		for (final UpdateStrategyConfig updateStrategyConfig : updateStrategies) {
			final String policy = updateStrategyConfig.getPolicy();
			int setPolicy = UpdateListStrategy.POLICY_UPDATE;
			if (policy.equalsIgnoreCase("never")) {
				setPolicy = UpdateListStrategy.POLICY_NEVER;
			} else if (policy.equalsIgnoreCase("onRequest")) {
				setPolicy = UpdateListStrategy.POLICY_ON_REQUEST;
			}
			final IConverter converter = updateStrategyConfig.getConverter();
			if (obs instanceof IObservableList) {
				final UpdateListStrategy updateStrategy = new UpdateListStrategy(
						updateStrategyConfig.isProvideDefaults(), setPolicy);
				if (converter == null) {
					updateStrategy.setConverter(converter);
				}

				if (updateStrategyConfig.getDirection().equalsIgnoreCase(
						"targetToModel")) {
					updateListStrategies[0] = updateStrategy;
				} else if (updateStrategyConfig.getDirection()
						.equalsIgnoreCase("modelToTarget")) {
					updateListStrategies[1] = updateStrategy;
				}
			} else if (obs instanceof IObservableValue) {
				if (policy.equalsIgnoreCase("convert")) {
					setPolicy = UpdateValueStrategy.POLICY_CONVERT;
				}
				final UpdateValueStrategy updateStrategy = new UpdateValueStrategy(
						updateStrategyConfig.isProvideDefaults(), setPolicy);
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
				} else if (updateStrategyConfig.getDirection()
						.equalsIgnoreCase("modelToTarget")) {
					updateValueStrategies[1] = updateStrategy;
				}
			}
		}
	}
}
