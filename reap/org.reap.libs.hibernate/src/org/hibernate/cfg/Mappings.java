//$Id: Mappings.java 11051 2007-01-16 23:24:17Z epbernard $
package org.hibernate.cfg;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.DuplicateMappingException;
import org.hibernate.MappingException;
import org.hibernate.engine.FilterDefinition;
import org.hibernate.engine.NamedQueryDefinition;
import org.hibernate.engine.NamedSQLQueryDefinition;
import org.hibernate.engine.ResultSetMappingDefinition;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.DenormalizedTable;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.TypeDef;
import org.hibernate.mapping.AuxiliaryDatabaseObject;
import org.hibernate.mapping.Column;
import org.hibernate.util.StringHelper;

/**
 * A collection of mappings from classes and collections to relational database
 * tables. (Represents a single <tt>&lt;hibernate-mapping&gt;</tt> element.)
 * 
 * @author Gavin King
 */
public class Mappings implements Serializable {

	static public class ColumnNames implements Serializable {
		// <String, String>
		public Map	logicalToPhysical	= new HashMap();
		// <String, String>
		public Map	physicalToLogical	= new HashMap();

		public ColumnNames() {
		}
	}

	static public class TableDescription implements Serializable {
		public Table	denormalizedSupertable;

		public String	logicalName;

		public TableDescription(final String logicalName,
				final Table denormalizedSupertable) {
			this.logicalName = logicalName;
			this.denormalizedSupertable = denormalizedSupertable;
		}
	}

	static final class PropertyReference implements Serializable {
		String	propertyName;
		String	referencedClass;
		boolean	unique;
	}

	private static final Log		log	= LogFactory.getLog(Mappings.class);
	protected boolean				autoImport;
	protected final List			auxiliaryDatabaseObjects;
	protected String				catalogName;
	protected final Map				classes;
	protected final Map				collections;
	/**
	 * binding table between the logical column name and the name out of the
	 * naming strategy for each table. According that when the column name is
	 * not set, the property name is considered as such This means that while
	 * theorically possible through the naming strategy contract, it is
	 * forbidden to have 2 real columns having the same logical name <Table,
	 * ColumnNames >
	 */
	protected final Map				columnNameBindingPerTable;
	protected String				defaultAccess;
	protected String				defaultCascade;
	protected boolean				defaultLazy;
	protected String				defaultPackage;
	protected final Map				extendsQueue;
	// private final List extendsQueue;
	protected final Map				filterDefinitions;
	protected final Map				imports;
	protected final NamingStrategy	namingStrategy;
	protected final List			propertyReferences;
	protected final Map				queries;
	protected final Map				resultSetMappings;

	protected String				schemaName;

	protected final List			secondPasses;
	protected final Map				sqlqueries;

	/**
	 * binding between logical table name and physical one (ie after the naming
	 * strategy has been applied) <String, TableDescription>
	 */
	protected final Map				tableNameBinding;

	protected final Map				tables;
	protected final Map				typeDefs;

	protected Mappings(final Map classes, final Map collections,
			final Map tables, final Map queries, final Map sqlqueries,
			final Map sqlResultSetMappings, final Map imports,
			final List secondPasses, final List propertyReferences,
			final NamingStrategy namingStrategy, final Map typeDefs,
			final Map filterDefinitions,
			// final List extendsQueue,
			final Map extendsQueue, final List auxiliaryDatabaseObjects,
			final Map tableNamebinding, final Map columnNameBindingPerTable) {
		this.classes = classes;
		this.collections = collections;
		this.queries = queries;
		this.sqlqueries = sqlqueries;
		resultSetMappings = sqlResultSetMappings;
		this.tables = tables;
		this.imports = imports;
		this.secondPasses = secondPasses;
		this.propertyReferences = propertyReferences;
		this.namingStrategy = namingStrategy;
		this.typeDefs = typeDefs;
		this.filterDefinitions = filterDefinitions;
		this.extendsQueue = extendsQueue;
		this.auxiliaryDatabaseObjects = auxiliaryDatabaseObjects;
		tableNameBinding = tableNamebinding;
		this.columnNameBindingPerTable = columnNameBindingPerTable;
	}

	public void addAuxiliaryDatabaseObject(
			final AuxiliaryDatabaseObject auxiliaryDatabaseObject) {
		auxiliaryDatabaseObjects.add(auxiliaryDatabaseObject);
	}

	public void addClass(final PersistentClass persistentClass)
			throws MappingException {
		final Object old = classes.put(
				persistentClass.getEntityName(),
				persistentClass);
		if (old != null) {
			throw new DuplicateMappingException("class/entity",
					persistentClass.getEntityName());
		}
	}

	public void addCollection(final Collection collection)
			throws MappingException {
		final Object old = collections.put(collection.getRole(), collection);
		if (old != null) {
			throw new DuplicateMappingException("collection role",
					collection.getRole());
		}
	}

	public void addColumnBinding(final String logicalName,
			final Column finalColumn, final Table table) {
		ColumnNames binding = (ColumnNames) columnNameBindingPerTable.get(table);
		if (binding == null) {
			binding = new ColumnNames();
			columnNameBindingPerTable.put(table, binding);
		}
		final String oldFinalName = (String) binding.logicalToPhysical.put(
				logicalName.toLowerCase(),
				finalColumn.getQuotedName());
		if (oldFinalName != null
				&& !(finalColumn.isQuoted() ? oldFinalName.equals(finalColumn.getQuotedName())
						: oldFinalName.equalsIgnoreCase(finalColumn.getQuotedName()))) {
			// TODO possibly relax that
			throw new MappingException(
					"Same logical column name referenced by different physical ones: "
							+ table.getName() + "." + logicalName + " => '"
							+ oldFinalName + "' and '"
							+ finalColumn.getQuotedName() + "'");
		}
		final String oldLogicalName = (String) binding.physicalToLogical.put(
				finalColumn.getQuotedName(),
				logicalName);
		if (oldLogicalName != null && !oldLogicalName.equals(logicalName)) {
			// TODO possibly relax that
			throw new MappingException(
					"Same physical column represented by different logical column names: "
							+ table.getName() + "."
							+ finalColumn.getQuotedName() + " => '"
							+ oldLogicalName + "' and '" + logicalName + "'");
		}
	}

	public Table addDenormalizedTable(final String schema,
			final String catalog, final String name, final boolean isAbstract,
			final String subselect, final Table includedTable)
			throws MappingException {
		final String key = subselect == null ? Table.qualify(
				catalog,
				schema,
				name) : subselect;
		if (tables.containsKey(key)) {
			throw new DuplicateMappingException("table", name);
		}

		final Table table = new DenormalizedTable(includedTable);
		table.setAbstract(isAbstract);
		table.setName(name);
		table.setSchema(schema);
		table.setCatalog(catalog);
		table.setSubselect(subselect);
		tables.put(key, table);
		return table;
	}

	public void addFilterDefinition(final FilterDefinition definition) {
		filterDefinitions.put(definition.getFilterName(), definition);
	}

	public void addImport(final String className, final String rename)
			throws MappingException {
		final String existing = (String) imports.put(rename, className);
		if (existing != null) {
			if (existing.equals(className)) {
				log.info("duplicate import: " + className + "->" + rename);
			} else {
				throw new DuplicateMappingException("duplicate import: "
						+ rename + " refers to both " + className + " and "
						+ existing + " (try using auto-import=\"false\")",
						"import", rename);
			}
		}
	}

	public void addPropertyReference(final String referencedClass,
			final String propertyName) {
		final PropertyReference upr = new PropertyReference();
		upr.referencedClass = referencedClass;
		upr.propertyName = propertyName;
		propertyReferences.add(upr);
	}

	public void addQuery(final String name, final NamedQueryDefinition query)
			throws MappingException {
		checkQueryExist(name);
		queries.put(name.intern(), query);
	}

	public void addResultSetMapping(
			final ResultSetMappingDefinition sqlResultSetMapping) {
		final String name = sqlResultSetMapping.getName();
		if (resultSetMappings.containsKey(name)) {
			throw new DuplicateMappingException("resultSet", name);
		}
		resultSetMappings.put(name, sqlResultSetMapping);
	}

	public void addSecondPass(final SecondPass sp) {
		addSecondPass(sp, false);
	}

	public void addSecondPass(final SecondPass sp, final boolean onTopOfTheQueue) {
		if (onTopOfTheQueue) {
			secondPasses.add(0, sp);
		} else {
			secondPasses.add(sp);
		}
	}

	public void addSQLQuery(final String name,
			final NamedSQLQueryDefinition query) throws MappingException {
		checkQueryExist(name);
		sqlqueries.put(name.intern(), query);
	}

	public Table addTable(final String schema, final String catalog,
			final String name, final String subselect, boolean isAbstract) {
		final String key = subselect == null ? Table.qualify(
				catalog,
				schema,
				name) : subselect;
		Table table = (Table) tables.get(key);

		if (table == null) {
			table = new Table();
			table.setAbstract(isAbstract);
			table.setName(name);
			table.setSchema(schema);
			table.setCatalog(catalog);
			table.setSubselect(subselect);
			tables.put(key, table);
		} else {
			if (!isAbstract) {
				table.setAbstract(false);
			}
		}

		return table;
	}

	public void addTableBinding(final String schema, final String catalog,
			final String logicalName, final String physicalName,
			final Table denormalizedSuperTable) {
		final String key = buildTableNameKey(schema, catalog, physicalName);
		final TableDescription tableDescription = new TableDescription(
				logicalName, denormalizedSuperTable);
		final TableDescription oldDescriptor = (TableDescription) tableNameBinding.put(
				key,
				tableDescription);
		if (oldDescriptor != null
				&& !oldDescriptor.logicalName.equals(logicalName)) {
			// TODO possibly relax that
			throw new MappingException(
					"Same physical table name reference several logical table names: "
							+ physicalName + " => " + "'"
							+ oldDescriptor.logicalName + "' and '"
							+ logicalName + "'");
		}
	}

	public void addToExtendsQueue(final ExtendsQueueEntry entry) {
		extendsQueue.put(entry, null);
	}

	public void addTypeDef(final String typeName, final String typeClass,
			final Properties paramMap) {
		final TypeDef def = new TypeDef(typeClass, paramMap);
		typeDefs.put(typeName, def);
		log.debug("Added " + typeName + " with class " + typeClass);
	}

	public void addUniquePropertyReference(final String referencedClass,
			final String propertyName) {
		final PropertyReference upr = new PropertyReference();
		upr.referencedClass = referencedClass;
		upr.propertyName = propertyName;
		upr.unique = true;
		propertyReferences.add(upr);
	}

	public String getCatalogName() {
		return catalogName;
	}

	public PersistentClass getClass(final String className) {
		return (PersistentClass) classes.get(className);
	}

	public Collection getCollection(final String role) {
		return (Collection) collections.get(role);
	}

	public String getDefaultAccess() {
		return defaultAccess;
	}

	public String getDefaultCascade() {
		return defaultCascade;
	}

	/**
	 * @return Returns the defaultPackage.
	 */
	public String getDefaultPackage() {
		return defaultPackage;
	}

	public FilterDefinition getFilterDefinition(final String name) {
		return (FilterDefinition) filterDefinitions.get(name);
	}

	public Map getFilterDefinitions() {
		return filterDefinitions;
	}

	public String getLogicalColumnName(final String physicalName,
			final Table table) {
		String logical = null;
		Table currentTable = table;
		TableDescription description = null;
		do {
			final ColumnNames binding = (ColumnNames) columnNameBindingPerTable.get(currentTable);
			if (binding != null) {
				logical = (String) binding.physicalToLogical.get(physicalName);
			}
			final String key = buildTableNameKey(
					currentTable.getSchema(),
					currentTable.getCatalog(),
					currentTable.getName());
			description = (TableDescription) tableNameBinding.get(key);
			if (description != null) {
				currentTable = description.denormalizedSupertable;
			}
		} while (logical == null && currentTable != null && description != null);
		if (logical == null) {
			throw new MappingException(
					"Unable to find logical column name from physical name "
							+ physicalName + " in table " + table.getName());
		}
		return logical;
	}

	public String getLogicalTableName(final Table table) {
		return getLogicalTableName(
				table.getQuotedSchema(),
				table.getCatalog(),
				table.getQuotedName());
	}

	public NamingStrategy getNamingStrategy() {
		return namingStrategy;
	}

	public String getPhysicalColumnName(String logicalName, final Table table) {
		logicalName = logicalName.toLowerCase();
		String finalName = null;
		Table currentTable = table;
		do {
			final ColumnNames binding = (ColumnNames) columnNameBindingPerTable.get(currentTable);
			if (binding != null) {
				finalName = (String) binding.logicalToPhysical.get(logicalName);
			}
			final String key = buildTableNameKey(
					currentTable.getSchema(),
					currentTable.getCatalog(),
					currentTable.getName());
			final TableDescription description = (TableDescription) tableNameBinding.get(key);
			if (description != null) {
				currentTable = description.denormalizedSupertable;
			}
		} while (finalName == null && currentTable != null);
		if (finalName == null) {
			throw new MappingException(
					"Unable to find column with logical name " + logicalName
							+ " in table " + table.getName());
		}
		return finalName;
	}

	public NamedQueryDefinition getQuery(final String name) {
		return (NamedQueryDefinition) queries.get(name);
	}

	public ResultSetMappingDefinition getResultSetMapping(final String name) {
		return (ResultSetMappingDefinition) resultSetMappings.get(name);
	}

	public String getSchemaName() {
		return schemaName;
	}

	public Table getTable(final String schema, final String catalog,
			final String name) {
		final String key = Table.qualify(catalog, schema, name);
		return (Table) tables.get(key);
	}

	public TypeDef getTypeDef(final String typeName) {
		return (TypeDef) typeDefs.get(typeName);
	}

	/**
	 * Returns the autoImport.
	 * 
	 * @return boolean
	 */
	public boolean isAutoImport() {
		return autoImport;
	}

	public boolean isDefaultLazy() {
		return defaultLazy;
	}

	public Iterator iterateCollections() {
		return collections.values().iterator();
	}

	public Iterator iterateTables() {
		return tables.values().iterator();
	}

	public PersistentClass locatePersistentClassByEntityName(
			final String entityName) {
		PersistentClass persistentClass = (PersistentClass) classes.get(entityName);
		if (persistentClass == null) {
			final String actualEntityName = (String) imports.get(entityName);
			if (StringHelper.isNotEmpty(actualEntityName)) {
				persistentClass = (PersistentClass) classes.get(actualEntityName);
			}
		}
		return persistentClass;
	}

	/**
	 * Sets the autoImport.
	 * 
	 * @param autoImport The autoImport to set
	 */
	public void setAutoImport(final boolean autoImport) {
		this.autoImport = autoImport;
	}

	/**
	 * Sets the catalogName.
	 * 
	 * @param catalogName The catalogName to set
	 */
	public void setCatalogName(final String catalogName) {
		this.catalogName = catalogName;
	}

	/**
	 * sets the default access strategy
	 * 
	 * @param defaultAccess the default access strategy.
	 */
	public void setDefaultAccess(final String defaultAccess) {
		this.defaultAccess = defaultAccess;
	}

	/**
	 * Sets the defaultCascade.
	 * 
	 * @param defaultCascade The defaultCascade to set
	 */
	public void setDefaultCascade(final String defaultCascade) {
		this.defaultCascade = defaultCascade;
	}

	public void setDefaultLazy(final boolean defaultLazy) {
		this.defaultLazy = defaultLazy;
	}

	/**
	 * @param defaultPackage The defaultPackage to set.
	 */
	public void setDefaultPackage(final String defaultPackage) {
		this.defaultPackage = defaultPackage;
	}

	/**
	 * Sets the schemaName.
	 * 
	 * @param schemaName The schemaName to set
	 */
	public void setSchemaName(final String schemaName) {
		this.schemaName = schemaName;
	}

	private String buildTableNameKey(final String schema, final String catalog,
			final String finalName) {
		final StringBuffer keyBuilder = new StringBuffer();
		if (schema != null) {
			keyBuilder.append(schema);
		}
		keyBuilder.append(".");
		if (catalog != null) {
			keyBuilder.append(catalog);
		}
		keyBuilder.append(".");
		keyBuilder.append(finalName);
		return keyBuilder.toString();
	}

	private void checkQueryExist(final String name) throws MappingException {
		if (sqlqueries.containsKey(name) || queries.containsKey(name)) {
			throw new DuplicateMappingException("query", name);
		}
	}

	private String getLogicalTableName(final String schema,
			final String catalog, final String physicalName) {
		final String key = buildTableNameKey(schema, catalog, physicalName);
		final TableDescription descriptor = (TableDescription) tableNameBinding.get(key);
		if (descriptor == null) {
			throw new MappingException("Unable to find physical table: "
					+ physicalName);
		}
		return descriptor.logicalName;
	}
}