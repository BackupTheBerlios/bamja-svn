//$Id$
package org.hibernate.engine;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.MappingException;
import org.hibernate.util.ArrayHelper;

/**
 * A contract for defining the aspects of cascading various persistence actions.
 * 
 * @see CascadingAction
 * 
 * @author Gavin King
 */
public abstract class CascadeStyle implements Serializable {

	public static final class MultipleCascadeStyle extends CascadeStyle {
		private final CascadeStyle[]	styles;

		public MultipleCascadeStyle(final CascadeStyle[] styles) {
			this.styles = styles;
		}

		@Override
		public boolean doCascade(final CascadingAction action) {
			for (final CascadeStyle element : styles) {
				if (element.doCascade(action)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean hasOrphanDelete() {
			for (final CascadeStyle element : styles) {
				if (element.hasOrphanDelete()) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean reallyDoCascade(final CascadingAction action) {
			for (final CascadeStyle element : styles) {
				if (element.reallyDoCascade(action)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public String toString() {
			return ArrayHelper.toString(styles);
		}
	}

	/**
	 * save / delete / update / evict / lock / replicate / merge / persist
	 */
	public static final CascadeStyle	ALL					= new CascadeStyle() {
																@Override
																public boolean doCascade(
																		CascadingAction action) {
																	return true;
																}

																@Override
																public String toString() {
																	return "STYLE_ALL";
																}
															};

	/**
	 * save / delete / update / evict / lock / replicate / merge / persist +
	 * delete orphans
	 */
	public static final CascadeStyle	ALL_DELETE_ORPHAN	= new CascadeStyle() {
																@Override
																public boolean doCascade(
																		CascadingAction action) {
																	return true;
																}

																@Override
																public boolean hasOrphanDelete() {
																	return true;
																}

																@Override
																public String toString() {
																	return "STYLE_ALL_DELETE_ORPHAN";
																}
															};

	/**
	 * delete
	 */
	public static final CascadeStyle	DELETE				= new CascadeStyle() {
																@Override
																public boolean doCascade(
																		CascadingAction action) {
																	return action == CascadingAction.DELETE;
																}

																@Override
																public String toString() {
																	return "STYLE_DELETE";
																}
															};

	/**
	 * delete + delete orphans
	 */
	public static final CascadeStyle	DELETE_ORPHAN		= new CascadeStyle() {
																@Override
																public boolean doCascade(
																		CascadingAction action) {
																	return action == CascadingAction.DELETE
																			|| action == CascadingAction.SAVE_UPDATE;
																}

																@Override
																public boolean hasOrphanDelete() {
																	return true;
																}

																@Override
																public boolean reallyDoCascade(
																		CascadingAction action) {
																	return action == CascadingAction.DELETE;
																}

																@Override
																public String toString() {
																	return "STYLE_DELETE_ORPHAN";
																}
															};

	/**
	 * evict
	 */
	public static final CascadeStyle	EVICT				= new CascadeStyle() {
																@Override
																public boolean doCascade(
																		CascadingAction action) {
																	return action == CascadingAction.EVICT;
																}

																@Override
																public String toString() {
																	return "STYLE_EVICT";
																}
															};

	/**
	 * lock
	 */
	public static final CascadeStyle	LOCK				= new CascadeStyle() {
																@Override
																public boolean doCascade(
																		CascadingAction action) {
																	return action == CascadingAction.LOCK;
																}

																@Override
																public String toString() {
																	return "STYLE_LOCK";
																}
															};

	/**
	 * merge
	 */
	public static final CascadeStyle	MERGE				= new CascadeStyle() {
																@Override
																public boolean doCascade(
																		CascadingAction action) {
																	return action == CascadingAction.MERGE;
																}

																@Override
																public String toString() {
																	return "STYLE_MERGE";
																}
															};

	/**
	 * no cascades
	 */
	public static final CascadeStyle	NONE				= new CascadeStyle() {
																@Override
																public boolean doCascade(
																		CascadingAction action) {
																	return false;
																}

																@Override
																public String toString() {
																	return "STYLE_NONE";
																}
															};

	/**
	 * create
	 */
	public static final CascadeStyle	PERSIST				= new CascadeStyle() {
																@Override
																public boolean doCascade(
																		CascadingAction action) {
																	return action == CascadingAction.PERSIST
																			|| action == CascadingAction.PERSIST_ON_FLUSH;
																}

																@Override
																public String toString() {
																	return "STYLE_PERSIST";
																}
															};

	/**
	 * refresh
	 */
	public static final CascadeStyle	REFRESH				= new CascadeStyle() {
																@Override
																public boolean doCascade(
																		CascadingAction action) {
																	return action == CascadingAction.REFRESH;
																}

																@Override
																public String toString() {
																	return "STYLE_REFRESH";
																}
															};
	/**
	 * replicate
	 */
	public static final CascadeStyle	REPLICATE			= new CascadeStyle() {
																@Override
																public boolean doCascade(
																		CascadingAction action) {
																	return action == CascadingAction.REPLICATE;
																}

																@Override
																public String toString() {
																	return "STYLE_REPLICATE";
																}
															};

	/**
	 * save / update
	 */
	public static final CascadeStyle	UPDATE				= new CascadeStyle() {
																@Override
																public boolean doCascade(
																		CascadingAction action) {
																	return action == CascadingAction.SAVE_UPDATE
																			|| action == CascadingAction.SAVE_UPDATE_COPY;
																}

																@Override
																public String toString() {
																	return "STYLE_SAVE_UPDATE";
																}
															};

	protected static final Map			STYLES				= new HashMap();

	static {
		STYLES.put("all", ALL);
		STYLES.put("all-delete-orphan", ALL_DELETE_ORPHAN);
		STYLES.put("save-update", UPDATE);
		STYLES.put("persist", PERSIST);
		STYLES.put("merge", MERGE);
		STYLES.put("lock", LOCK);
		STYLES.put("refresh", REFRESH);
		STYLES.put("replicate", REPLICATE);
		STYLES.put("evict", EVICT);
		STYLES.put("delete", DELETE);
		STYLES.put("remove", DELETE); // adds remove as a sort-of alias for
										// delete...
		STYLES.put("delete-orphan", DELETE_ORPHAN);
		STYLES.put("none", NONE);
	}

	/**
	 * Factory method for obtaining named cascade styles
	 * 
	 * @param cascade The named cascade style name.
	 * @return The appropriate CascadeStyle
	 */
	public static CascadeStyle getCascadeStyle(final String cascade) {
		final CascadeStyle style = (CascadeStyle) STYLES.get(cascade);
		if (style == null) {
			throw new MappingException("Unsupported cascade style: " + cascade);
		} else {
			return style;
		}
	}

	/**
	 * package-protected constructor
	 */
	protected CascadeStyle() {
	}

	/**
	 * For this style, should the given action be cascaded?
	 * 
	 * @param action The action to be checked for cascade-ability.
	 * @return True if the action should be cascaded under this style; false
	 *         otherwise.
	 */
	public abstract boolean doCascade(CascadingAction action);

	/**
	 * Do we need to delete orphaned collection elements?
	 * 
	 * @return True if this style need to account for orphan delete operations;
	 *         false othwerwise.
	 */
	public boolean hasOrphanDelete() {
		return false;
	}

	/**
	 * Probably more aptly named something like doCascadeToCollectionElements();
	 * it is however used from both the collection and to-one logic branches...
	 * <p/> For this style, should the given action really be cascaded? The
	 * default implementation is simply to return {@link #doCascade}; for
	 * certain styles (currently only delete-orphan), however, we need to be
	 * able to control this seperately.
	 * 
	 * @param action The action to be checked for cascade-ability.
	 * @return True if the action should be really cascaded under this style;
	 *         false otherwise.
	 */
	public boolean reallyDoCascade(final CascadingAction action) {
		return doCascade(action);
	}
}
