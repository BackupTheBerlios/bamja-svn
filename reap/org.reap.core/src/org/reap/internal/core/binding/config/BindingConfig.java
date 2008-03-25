/*
 * BindingConfig.java -- TODO Insert a short describtion of this file. 
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

import java.util.ArrayList;
import java.util.List;

public class BindingConfig {

	private boolean								onlyReturnObservable;
	private final boolean						processingBinding;
	private String								returnMethod;
	private String								target;
	private String								targetEntity;
	private GUIBindingTypes						type;
	private final List<UpdateStrategyConfig>	updateStrategies	= new ArrayList<UpdateStrategyConfig>();
	private boolean								valid				= true;
	private final String[]						viewerLabelProps;

	public BindingConfig(final String type, final String target,
			final String targetEntity, final String returnMethod,
			final boolean onlyReturnObservable,
			final boolean processingBinding, final String[] viewerLabelProps)
			throws IllegalArgumentException {
		this.target = target;
		this.targetEntity = targetEntity;
		this.returnMethod = returnMethod;
		this.onlyReturnObservable = onlyReturnObservable;
		this.processingBinding = processingBinding;
		this.viewerLabelProps = viewerLabelProps;
		this.type = null;
		if (type != null) {
			for (final GUIBindingTypes bindingType : GUIBindingTypes.values()) {
				if (bindingType.name().equalsIgnoreCase(type)) {
					this.type = bindingType;
				}
			}
		}
		if (processingBinding
				&& (GUIBindingTypes.ITEMS == this.type || GUIBindingTypes.JFACEINPUT == this.type)) {
			throw new IllegalArgumentException();
		}
	}

	public final void addUpdateStrategyConfig(
			final UpdateStrategyConfig updateStrategyConfig) {
		updateStrategies.add(updateStrategyConfig);
	}

	/**
	 * @return the returnMethod
	 */
	public final String getReturnMethod() {
		return returnMethod;
	}

	/**
	 * @return the target
	 */
	public final String getTarget() {
		return target;
	}

	/**
	 * @return the targetEntity
	 */
	public final String getTargetEntity() {
		return targetEntity;
	}

	/**
	 * @return the type
	 */
	public final GUIBindingTypes getType() {
		return type;
	}

	/**
	 * @return the updateStrategy
	 */
	public final List<UpdateStrategyConfig> getUpdateStrategies() {
		return updateStrategies;
	}

	/**
	 * @return the viewerLabelProps
	 */
	public final String[] getViewerLabelProps() {
		return viewerLabelProps;
	}

	/**
	 * @return the onlyReturnObservable
	 */
	public final boolean isOnlyReturnObservable() {
		return onlyReturnObservable;
	}

	/**
	 * @return the processingBinding
	 */
	public final boolean isProcessingBinding() {
		return processingBinding;
	}

	/**
	 * @return the valid
	 */
	public final boolean isValid() {
		return valid;
	}

	/**
	 * @param onlyReturnObservable the onlyReturnObservable to set
	 */
	public final void setOnlyReturnObservable(final boolean onlyReturnObservable) {
		this.onlyReturnObservable = onlyReturnObservable;
	}

	/**
	 * @param returnMethod the returnMethod to set
	 */
	public final void setReturnMethod(final String returnMethod) {
		this.returnMethod = returnMethod;
	}

	/**
	 * @param target the target to set
	 */
	public final void setTarget(final String target) {
		this.target = target;
	}

	/**
	 * @param targetEntity the targetEntity to set
	 */
	public final void setTargetEntity(final String targetEntity) {
		this.targetEntity = targetEntity;
	}

	/**
	 * @param type the type to set
	 */
	public final void setType(final GUIBindingTypes type) {
		this.type = type;
	}

	/**
	 * @param valid the valid to set
	 */
	public final void setValid(final boolean valid) {
		this.valid = valid;
	}

}
