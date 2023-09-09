package com.facebook.presto.sql.tree;

import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public class ShowColumns
        extends Statement
{
    private final QualifiedName table;

    public ShowColumns(QualifiedName table)
    {
        this.table = checkNotNull(table, "table is null");
    }

    public QualifiedName getTable()
    {
        return table;
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context)
    {
        return visitor.visitShowColumns(this, context);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(table);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        ShowColumns o = (ShowColumns) obj;
        return Objects.equal(table, o.table);
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("table", table)
                .toString();
    }
}
