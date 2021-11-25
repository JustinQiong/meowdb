package meowdb.dao;

import com.mysql.cj.jdbc.MysqlDataSource;
import meowdb.entity.jdbc.ColumnInfo;
import meowdb.entity.jdbc.DataTable;
import meowdb.entity.jdbc.PrimaryKey;
import meowdb.jdbc.JdbcTemplate;
import meowdb.util.CollectionUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 查询类
 *
 * @author justin.qiong@outlook.com
 * @since 2021/2/25 9:21
 */
public class JdbcRepository {

    public static List<String> findDataBases(MysqlDataSource ds) throws SQLException {
        return new JdbcTemplate(ds).queryStringRows("show databases");
    }

    public static void dropDatabase(MysqlDataSource ds, String dbName) throws SQLException {
        new JdbcTemplate(ds).executeUpdate("drop database `" + dbName + "`");
    }

    public static void createDatabase(MysqlDataSource ds, String dbName) throws SQLException {
        new JdbcTemplate(ds).executeUpdate("create database `" + dbName + "`");
    }

    public static List<String> findTables(MysqlDataSource ds) throws SQLException {
        String dbName = ds.getDatabaseName();
        Objects.requireNonNull(dbName, "databaseName is required in dataSource");
        return new JdbcTemplate(ds).queryStringRows("show tables from `" + dbName + "`");
    }

    public static void dropTable(MysqlDataSource ds, String tableName) throws SQLException {
        requireDbNameNonNull(ds);
        new JdbcTemplate(ds).executeUpdate("drop table `" + tableName + "`");
    }

    public static void truncateTable(MysqlDataSource ds, String tableName) throws SQLException {
        requireDbNameNonNull(ds);
        new JdbcTemplate(ds).executeUpdate("truncate table `" + tableName + "`");
    }

    public static void connectionTest(MysqlDataSource ds) throws SQLException {
        new JdbcTemplate(ds).queryStringRows("select 1");
    }

    public static List<ColumnInfo> findColumns(MysqlDataSource ds, String tableName) throws SQLException {
        requireDbNameNonNull(ds);
        DataTable result = new JdbcTemplate(ds).query("show columns from `" + tableName + "`");
        return result.getRows().stream().map(row -> {
            ColumnInfo info = new ColumnInfo();
            info.setField(row.get("field"));
            info.setType(row.get("type"));
            info.setNull(row.get("null"));
            info.setKey(row.get("key"));
            info.setDef(row.get("default"));
            info.setExtra(row.get("extra"));
            return info;
        }).collect(Collectors.toList());
    }

    public static Map<String, DataTable> query(MysqlDataSource ds, String sql) throws SQLException {
        requireDbNameNonNull(ds);
        return new JdbcTemplate(ds).multiQuery(sql);
    }

    public static void updateSingleValue(MysqlDataSource ds, String table, String column, String val, List<PrimaryKey> pks) throws SQLException {
        requireDbNameNonNull(ds);
        if (CollectionUtils.isEmpty(pks)) {
            throw new IllegalArgumentException("Primary keys is required when update row.");
        }
        StringBuilder where = new StringBuilder(" where ");
        int size = pks.size();
        for (int i = 0; i < size; i++) {
            PrimaryKey pk = pks.get(i);
            where.append("`");
            where.append(pk.getName());
            where.append("`");
            where.append("=");
            where.append("'");
            where.append(pk.getValue());
            where.append("'");
            if (i != size - 1) {
                where.append(" and ");
            }
        }
        new JdbcTemplate(ds).executeUpdate("update `" + table + "` set `" + column + "`='" + val + "'" + where);
    }

    private static void requireDbNameNonNull(MysqlDataSource ds) {
        Objects.requireNonNull(ds.getDatabaseName(), "databaseName is required in dataSource");
    }
}
