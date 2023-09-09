package com.facebook.presto.metadata;

import com.google.common.base.Preconditions;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class FunctionHandle
{
    private final int id;
    private final String name;

    @JsonCreator
    public FunctionHandle(@JsonProperty("id") int id, @JsonProperty("name") String name)
    {
        Preconditions.checkArgument(id >= 0, "id must be positive");
        Preconditions.checkNotNull(name, "name is null");

        this.id = id;
        this.name = name;
    }

    @JsonProperty
    public int getId()
    {
        return id;
    }

    @JsonProperty
    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return String.format("%d:%s", id, name);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FunctionHandle that = (FunctionHandle) o;

        if (id != that.id) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return id;
    }
}
