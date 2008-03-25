/*
 * Activator.java -- Control the live cycle of the reap core plug-in.
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

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 */
public class Activator extends AbstractUIPlugin {

	/**
	 * The plug-in ID.
	 */
	public static final String	PLUGIN_ID	= "org.reap.core";

	// The shared instance
	private static Activator	plugin;

	/**
	 * Returns the shared instance.
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Writes an error entry into the Eclipse log-file
	 * 
	 * @param code - plug-in specific error code
	 * @param msg - localised error message
	 * @param ex - throwable that caused the error or or null
	 */
	public static void logError(final int code, final String msg,
			final Throwable ex) {
		getDefault().getLog().log(
				new Status(IStatus.ERROR, PLUGIN_ID, code, msg, ex));
	}

	/**
	 * Writes an info entry into the Eclipse log-file
	 * 
	 * @param code - plug-in specific info code
	 * @param msg - localised info message
	 */
	public static void logInfo(final int code, final String msg) {
		getDefault().getLog().log(
				new Status(IStatus.INFO, PLUGIN_ID, code, msg, null));
	}

	/**
	 * Writes a warning entry into the Eclipse log-file
	 * 
	 * @param code - plug-in specific warning code
	 * @param msg - localised warning message
	 */
	public static void logWarning(final int code, final String msg) {
		getDefault().getLog().log(
				new Status(IStatus.WARNING, PLUGIN_ID, code, msg, null));
	}

	private DataBindingContext	dataBindingContext;

	private Realm				realm;

	private REAPManager			reapManager;

	/**
	 * The constructor.
	 */
	public Activator() {
	}

	/**
	 * @return the dataBindingContext
	 */
	public final DataBindingContext getDataBindingContext() {
		return dataBindingContext;
	}

	/**
	 * @return the realm
	 */
	public final Realm getRealm() {
		return realm;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public final void start(final BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		realm = SWTObservables.getRealm(PlatformUI.getWorkbench().getDisplay());
		dataBindingContext = new DataBindingContext(realm);
		reapManager = new REAPManager();
		reapManager.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public final void stop(final BundleContext context) throws Exception {
		plugin = null;
		reapManager.stop();
		dataBindingContext.dispose();
		dataBindingContext = null;
		super.stop(context);
	}
}
