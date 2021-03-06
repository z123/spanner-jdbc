package nl.topicus.jdbc.test.ddl;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.Assert;

/**
 * Class for testing different MetaData methods of the JDBC driver.
 * 
 * @author loite
 *
 */
public class MetaDataTester
{
	private static final Logger log = Logger.getLogger(MetaDataTester.class.getName());

	private static final String[] TABLES = { "TEST", "TESTCHILD" };

	private static final String[][] COLUMNS = {
			{ "ID", "UUID", "ACTIVE", "AMOUNT", "DESCRIPTION", "CREATED_DATE", "LAST_UPDATED" },
			{ "ID", "CHILDID", "DESCRIPTION" } };

	private static final int[][] COLUMN_TYPES = {
			{ Types.BIGINT, Types.BINARY, Types.BOOLEAN, Types.DOUBLE, Types.NVARCHAR, Types.DATE, Types.TIMESTAMP },
			{ Types.BIGINT, Types.BIGINT, Types.NVARCHAR } };

	private static final Map<String, String[]> INDEX_COLUMNS = new HashMap<>();
	static
	{
		INDEX_COLUMNS.put("TEST.PRIMARY_KEY", new String[] { "ID" });
		INDEX_COLUMNS.put("TESTCHILD.PRIMARY_KEY", new String[] { "ID", "CHILDID" });
		INDEX_COLUMNS.put("TEST.IDX_TEST_UUID", new String[] { "UUID" });
		INDEX_COLUMNS.put("TESTCHILD.IDX_TESTCHILD_DESCRIPTION", new String[] { "DESCRIPTION" });
	}

	private static final Map<String, Boolean> INDEX_UNIQUE = new HashMap<>();
	static
	{
		INDEX_UNIQUE.put("TEST.PRIMARY_KEY", Boolean.TRUE);
		INDEX_UNIQUE.put("TESTCHILD.PRIMARY_KEY", Boolean.TRUE);
		INDEX_UNIQUE.put("TEST.IDX_TEST_UUID", Boolean.TRUE);
		INDEX_UNIQUE.put("TESTCHILD.IDX_TESTCHILD_DESCRIPTION", Boolean.FALSE);
	}

	private Connection connection;

	public MetaDataTester(Connection connection)
	{
		this.connection = connection;
	}

	public void runMetaDataTests() throws SQLException
	{
		log.info("Starting table meta data tests");
		runTableMetaDataTests();
		log.info("Starting column meta data tests");
		runColumnMetaDataTests();
		log.info("Starting index meta data tests");
		runIndexMetaDataTests();
		log.info("Starting other meta data tests");
		runOtherMetaDataTests();
		log.info("Finished meta data tests");
	}

	private void runTableMetaDataTests() throws SQLException
	{
		DatabaseMetaData metadata = connection.getMetaData();
		int count = 0;
		try (ResultSet tables = metadata.getTables("", "", null, null))
		{
			while (tables.next())
			{
				assertEquals(TABLES[count], tables.getString("TABLE_NAME"));
				assertEquals("TABLE", tables.getString("TABLE_TYPE"));
				count++;
			}
		}
		assertEquals(2, count);
	}

	private void runColumnMetaDataTests() throws SQLException
	{
		DatabaseMetaData metadata = connection.getMetaData();
		int tableIndex = 0;
		for (String table : TABLES)
		{
			int columnIndex = 0;
			try (ResultSet columns = metadata.getColumns("", "", table, null))
			{
				while (columns.next())
				{
					assertEquals(COLUMNS[tableIndex][columnIndex], columns.getString("COLUMN_NAME"));
					assertEquals(COLUMN_TYPES[tableIndex][columnIndex], columns.getInt("DATA_TYPE"));
					columnIndex++;
				}
			}
			tableIndex++;
		}
	}

	private void runIndexMetaDataTests() throws SQLException
	{
		String currentKey = "";
		DatabaseMetaData metadata = connection.getMetaData();
		for (String table : TABLES)
		{
			int columnIndex = 0;
			try (ResultSet indexes = metadata.getIndexInfo("", "", table, false, false))
			{
				while (indexes.next())
				{
					String key = indexes.getString("TABLE_NAME") + "." + indexes.getString("INDEX_NAME");
					if (!currentKey.equals(key))
					{
						columnIndex = 0;
						currentKey = key;
					}
					String[] columns = INDEX_COLUMNS.get(key);
					Boolean unique = INDEX_UNIQUE.get(key);
					Assert.assertNotNull(columns);
					assertEquals(columns[columnIndex], indexes.getString("COLUMN_NAME"));
					assertEquals(unique, !indexes.getBoolean("NON_UNIQUE"));
					columnIndex++;
				}
			}
		}
	}

	private void runOtherMetaDataTests() throws SQLException
	{
		DatabaseMetaData metadata = connection.getMetaData();
		try (ResultSet rs = metadata.getCatalogs())
		{
		}
		try (ResultSet rs = metadata.getAttributes("", "", null, null))
		{
		}
		try (ResultSet rs = metadata.getClientInfoProperties())
		{
		}
		try (ResultSet rs = metadata.getFunctionColumns("", "", null, null))
		{
		}
		try (ResultSet rs = metadata.getFunctions("", "", null))
		{
		}
		try (ResultSet rs = metadata.getProcedureColumns("", "", null, null))
		{
		}
		try (ResultSet rs = metadata.getProcedures("", "", null))
		{
		}
		try (ResultSet rs = metadata.getSchemas())
		{
		}
		try (ResultSet rs = metadata.getSchemas("", null))
		{
		}
		try (ResultSet rs = metadata.getSuperTypes("", "", null))
		{
		}
		try (ResultSet rs = metadata.getTableTypes())
		{
		}
		for (String table : TABLES)
		{
			try (ResultSet rs = metadata.getExportedKeys("", "", table))
			{
			}
			try (ResultSet rs = metadata.getImportedKeys("", "", table))
			{
			}
			try (ResultSet rs = metadata
					.getBestRowIdentifier("", "", table, DatabaseMetaData.bestRowTransaction, false))
			{
			}
			try (ResultSet rs = metadata.getColumnPrivileges("", "", table, null))
			{
			}
			try (ResultSet rs = metadata.getPrimaryKeys("", "", table))
			{
			}
			try (ResultSet rs = metadata.getPseudoColumns("", "", table, null))
			{
			}
			try (ResultSet rs = metadata.getSuperTables("", "", table))
			{
			}
			try (ResultSet rs = metadata.getTablePrivileges("", "", table))
			{
			}
			try (ResultSet rs = metadata.getVersionColumns("", "", table))
			{
			}
		}
	}

}
