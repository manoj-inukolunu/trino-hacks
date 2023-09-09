package com.facebook.presto.metadata;

import com.facebook.presto.tuple.TupleInfo;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TableColumnMapper
        implements ResultSetMapper<TableColumn>
{
    @Override
    public TableColumn map(int index, ResultSet r, StatementContext ctx)
            throws SQLException
    {
        return new TableColumn(r.getString("catalog_name"),
                r.getString("schema_name"),
                r.getString("table_name"),
                r.getString("column_name"),
                r.getInt("ordinal_position"),
                TupleInfo.Type.fromName(r.getString("data_type")));
    }
}
