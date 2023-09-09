package com.facebook.presto.sql.tree;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class LikePredicate
        extends Expression
{
    private final Expression value;
    private final Expression pattern;
    private final Expression escape;

    public LikePredicate(Expression value, Expression pattern, Expression escape)
    {
        Preconditions.checkNotNull(value, "value is null");
        Preconditions.checkNotNull(pattern, "pattern is null");

        this.value = value;
        this.pattern = pattern;
        this.escape = escape;
    }

    public Expression getValue()
    {
        return value;
    }

    public Expression getPattern()
    {
        return pattern;
    }

    public Expression getEscape()
    {
        return escape;
    }

    @Override
    public <R, C> R accept(AstVisitor<R, C> visitor, C context)
    {
        return visitor.visitLikePredicate(this, context);
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("value", value)
                .add("pattern", pattern)
                .add("escape", escape)
                .omitNullValues()
                .toString();
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

        LikePredicate that = (LikePredicate) o;

        if (escape != null ? !escape.equals(that.escape) : that.escape != null) {
            return false;
        }
        if (!pattern.equals(that.pattern)) {
            return false;
        }
        if (!value.equals(that.value)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = value.hashCode();
        result = 31 * result + pattern.hashCode();
        result = 31 * result + (escape != null ? escape.hashCode() : 0);
        return result;
    }
}
