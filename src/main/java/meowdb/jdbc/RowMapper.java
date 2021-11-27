package meowdb.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 查询结果mapper
 *
 * @author justin.qiong@outlook.com
 * @since 2021/2/18 9:47
 */
public interface RowMapper<T> {

    T map(ResultSet rs) throws SQLException;

}
