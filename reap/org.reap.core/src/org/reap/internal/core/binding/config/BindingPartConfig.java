/*
 * BindingPartConfig.java -- TODO Insert a short describtion of this file.
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

public class BindingPartConfig {

	private final Map<String, BindingFieldConfig>	bindingFields	= new HashMap<String, BindingFieldConfig>();
	private final String							bindToPart;
	private final String							id;
	private final String							targetEntity;

	public BindingPartConfig(final String id, final String targetEntity,
			final String bindToPart) {
		this.id = id;
		this.targetEntity = targetEntity;
		this.bindToPart = bindToPart;
	}

	public final void addBindingField(final BindingFieldConfig bindingField) {
		bindingFields.put(bindingField.getName(), bindingField);
	}

	public final BindingFieldConfig getBindingField(final String name) {
		return bindingFields.get(name);
	}

	/**
	 * @return the bindingFields
	 */
	public final Map<String, BindingFieldConfig> getBindingFields() {
		return bindingFields;
	}

	/**
	 * @return the bindToPart
	 */
	public final String getBindToPart() {
		return bindToPart;
	}

	/**
	 * @return the id
	 */
	public final String getId() {
		return id;
	}

	/**
	 * @return the entityTypes
	 */
	public final String getTargetEntity() {
		return targetEntity;
	}
}
