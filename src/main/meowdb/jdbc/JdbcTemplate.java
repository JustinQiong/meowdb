package meowdb.jdbc;

import meowdb.entity.jdbc.DataTable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC访问模板类
 *
 * @author justin.qiong@outlook.com
 * @since 2021/2/18 9:15
 */
public class JdbcTemplate {

    private DataSource dataSource;

    public JdbcTemplate(DataSource ds) {
        this.dataSource = ds;
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        Connection connection = dataSource.getConnection();
        PreparedStatement ps = connection.prepareStatement(sql);
        return ps.executeQuery();
    }

    public List<String> queryStringRows(String sql) throws SQLException {
        List<String> rows = new ArrayList<>();
        try (ResultSet rs = executeQuery(sql)) {
            while (rs.next()) {
                String row = rs.getString(1);
                rows.add(row);
            }
        }
        return rows;
    }

    public int executeUpdate(String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                return statement.executeUpdate(sql);
            }
        }
    }

    public Map<String, DataTable> multiQuery(String sql) throws SQLException {
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        String[] names = parseResultSetName(sql);
        boolean hasResult = statement.execute(sql);
        Map<String, DataTable> tables = new LinkedHashMap<>();
        int index = 0;
        while (hasResult) {
            ResultSet rs = statement.getResultSet();
            DataTable dataTable = new DataTable();
            ResultSetMetaData meta = rs.getMetaData();
            int count = meta.getColumnCount();
            for (int i = 0; i < count; i++) {
                String colName = meta.getColumnLabel(i + 1);
                dataTable.addColumn(colName.toLowerCase());
            }
            while (rs.next()) {
                Map<String, String> row = new LinkedHashMap<>();
                for (int i = 0; i < count; i++) {
                    String colName = meta.getColumnLabel(i + 1);
                    row.put(colName.toLowerCase(), rs.getString(colName));
                }
                dataTable.addRow(row);
            }
            tables.put(names[index] + "_" + index, dataTable);
            hasResult = statement.getMoreResults();
            index++;
        }

        return tables;
    }

    public DataTable query(String sql) throws SQLException {
        return multiQuery(sql).values().stream().findFirst().orElse(null);
    }
    private static String[] parseResultSetName(String sql) {
        String[] stmts = sql.split(";");
        String[] names = new String[stmts.length];
        for (int j = 0; j < stmts.length; j++) {
            String stmt = stmts[j];
            String[] words = stmt.split(" ");
            for (int i = 0; i < words.length; i++) {
                String word = words[i];
                if ((word.equalsIgnoreCase("from") || word.equalsIgnoreCase("show"))
                        && i != words.length - 1) {
                    names[j] = words[i + 1];
                    break;
                }
            }
        }
        return names;
    }
}
