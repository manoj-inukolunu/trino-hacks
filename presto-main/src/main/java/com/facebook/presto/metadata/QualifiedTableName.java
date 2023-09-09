package com.facebook.presto.metadata;

import com.google.common.base.Objects;

import javax.annotation.concurrent.Immutable;

import static com.facebook.presto.metadata.MetadataUtil.checkTableName;

@Immutable
public class QualifiedTableName
{
    private final String catalogName;
    private final String schemaName;
    private final String tableName;

    public QualifiedTableName(String catalogName, String schemaName, String tableName)
    {
        checkTableName(catalogName, schemaName, tableName);
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
    }

    public String getCatalogName()
    {
        return catalogName;
    }

    public String getSchemaName()
    {
        return schemaName;
    }

    public String getTableName()
    {
        return tableName;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        QualifiedTableName o = (QualifiedTableName) obj;
        return Objects.equal(catalogName, o.catalogName) &&
                Objects.equal(schemaName, o.schemaName) &&
                Objects.equal(tableName, o.tableName);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(catalogName, schemaName, tableName);
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("catalogName", catalogName)
                .add("schemaName", schemaName)
                .add("tableName", tableName)
                .toString();
    }
}
