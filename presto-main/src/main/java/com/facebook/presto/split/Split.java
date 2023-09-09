package com.facebook.presto.split;

import com.facebook.presto.metadata.DataSourceType;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = NativeSplit.class, name = "native"),
        @JsonSubTypes.Type(value = InternalSplit.class, name = "internal"),
        @JsonSubTypes.Type(value = ImportSplit.class, name = "import")})
public interface Split
{
    DataSourceType getDataSourceType();
}
