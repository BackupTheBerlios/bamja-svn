/*
 * WriteableEntityValue.java -- TODO Insert a short describtion of this file.
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.WritableValue;

public class WritableEntityValue extends WritableValue {

	private Binding								bindValue;
	private final Map<String, ComputedValue>	dataProcessings		= new HashMap<String, ComputedValue>();
	private final Set<String>					forParts			= new HashSet<String>();
	private String								listDatabaseAccessID;
	private final String						targetEntity;
	private final Map<String, WritableValue>	viewerSelects		= new HashMap<String, WritableValue>();
	private WritableList						writableEntityList	= null;

	public WritableEntityValue(final String targetEntity) {
		this.targetEntity = targetEntity;
	}

	public WritableEntityValue(final String targetEntity,
			final Set<String> bindToPartList) {
		this.targetEntity = targetEntity;
		forParts.addAll(bindToPartList);
	}

	public final void addDataProcessing(final String id,
			final ComputedValue compValue) {
		dataProcessings.put(id, compValue);
	}

	public final void addForPart(final String id) {
		forParts.add(id);
	}

	public final void addViewerSelect(final String name,
			final WritableValue selectionValue) {
		viewerSelects.put(name, selectionValue);
	}

	/**
	 * @return the bindValue
	 */
	public final Binding getBindValue() {
		return bindValue;
	}

	public final ComputedValue getDataProcessing(final String id) {
		return dataProcessings.get(id);
	}

	/**
	 * @return the listDatabaseAccessID
	 */
	public final String getListDatabaseAccessID() {
		return listDatabaseAccessID;
	}

	/**
	 * @return the targetEntity
	 */
	public final String getTargetEntity() {
		return targetEntity;
	}

	public final WritableValue getViewerSelect(final String fieldName) {
		return viewerSelects.get(fieldName);
	}

	/**
	 * @return the writableList
	 */
	public final WritableList getWritableEntityList() {
		return writableEntityList;
	}

	public final boolean isForPart(final String id) {
		return forParts.contains(id);
	}

	public final void setBinding(final Binding bindValue) {
		this.bindValue = bindValue;
	}

	/**
	 * @param bindValue the bindValue to set
	 */
	public final void setBindValue(final Binding bindValue) {
		this.bindValue = bindValue;
	}

	public final void setListDatabaseAccessID(final String databaseAccessID) {
		listDatabaseAccessID = databaseAccessID;
	}

	public final void setWritableEntityList(
			final WritableList writableEntityList) {
		this.writableEntityList = writableEntityList;
	}
}
