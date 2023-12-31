package com.facebook.presto.sql.tree;

import javax.annotation.Nullable;

public abstract class AstVisitor<R, C>
{
    public R process(Node node, @Nullable C context)
    {
        return node.accept(this, context);
    }

    protected R visitNode(Node node, C context)
    {
        return null;
    }

    protected R visitExpression(Expression node, C context)
    {
        return visitNode(node, context);
    }


    protected R visitCurrentTime(CurrentTime node, C context)
    {
        return visitExpression(node, context);
    }

    protected R visitExtract(Extract node, C context)
    {
        return visitExpression(node, context);
    }

    protected R visitArithmeticExpression(ArithmeticExpression node, C context)
    {
        return visitExpression(node, context);
    }

    protected R visitBetweenPredicate(BetweenPredicate node, C context)
    {
        return visitExpression(node, context);
    }

    protected R visitCoalesceExpression(CoalesceExpression node, C context)
    {
        return visitExpression(node, context);
    }

    protected R visitComparisonExpression(ComparisonExpression node, C context)
    {
        return visitExpression(node, context);
    }

    protected R visitLiteral(Literal node, C context)
    {
        return visitExpression(node, context);
    }

    protected R visitDateLiteral(DateLiteral node, C context)
    {
        return visitLiteral(node, context);
    }

    protected R visitDoubleLiteral(DoubleLiteral node, C context)
    {
        return visitLiteral(node, context);
    }

    protected R visitStatement(Statement node, C context)
    {
        return visitNode(node, context);
    }

    protected R visitQuery(Query node, C context)
    {
        return visitStatement(node, context);
    }

    protected R visitShowTables(ShowTables node, C context)
    {
        return visitStatement(node, context);
    }

    protected R visitShowColumns(ShowColumns node, C context)
    {
        return visitStatement(node, context);
    }

    protected R visitShowPartitions(ShowPartitions node, C context)
    {
        return visitStatement(node, context);
    }

    protected R visitShowFunctions(ShowFunctions node, C context)
    {
        return visitStatement(node, context);
    }

    protected R visitTimeLiteral(TimeLiteral node, C context)
    {
        return visitLiteral(node, context);
    }

    protected R visitSelect(Select node, C context)
    {
        return visitNode(node, context);
    }

    protected R visitRelation(Relation node, C context)
    {
        return visitNode(node, context);
    }

    protected R visitTimestampLiteral(TimestampLiteral node, C context)
    {
        return visitLiteral(node, context);
    }

    protected R visitWhenClause(WhenClause node, C context)
    {
        return visitExpression(node, context);
    }

    protected R visitIntervalLiteral(IntervalLiteral node, C context)
    {
        return visitLiteral(node, context);
    }

    protected R visitInPredicate(InPredicate node, C context)
    {
        return visitExpression(node, context);
    }

    protected R visitFunctionCall(FunctionCall node, C context)
    {
        return visitExpression(node, context);
    }

    protected R visitAliasedExpression(AliasedExpression node, C context)
    {
        return visitExpression(node, context);
    }

    protected R visitSimpleCaseExpression(SimpleCaseExpression node, C context)
    {
        return visitExpression(node, context);
    }

    protected R visitStringLiteral(StringLiteral node, C context)
    {
        return visitLiteral(node, context);
    }

    protected R visitBooleanLiteral(BooleanLiteral node, C context)
    {
        return visitLiteral(node, context);
    }

    protected R visitInListExpression(InListExpression node, C context)
    {
        return visitExpression(node, context);
    }

    protected R visitQualifiedNameReference(QualifiedNameReference node, C context)
    {
        return visitExpression(node, context);
    }

    protected R visitNullIfExpression(NullIfExpression node, C context)
    {
        return visitExpression(node, context);
    }

    protected R visitNullLiteral(NullLiteral node, C context)
    {
        return visitLiteral(node, context);
    }

    protected R visitNegativeExpression(NegativeExpression node, C context)
    {
        return visitExpression(node, context);
    }

    protected R visitNotExpression(NotExpression node, C context)
    {
        return visitExpression(node, context);
    }

    protected R visitAllColumns(AllColumns node, C context)
    {
        return visitExpression(node, context);
    }

    protected R visitSearchedCaseExpression(SearchedCaseExpression node, C context)
    {
        return visitExpression(node, context);
    }

    protected R visitLikePredicate(LikePredicate node, C context)
    {
        return visitExpression(node, context);
    }

    protected R visitIsNotNullPredicate(IsNotNullPredicate node, C context)
    {
        return visitExpression(node, context);
    }

    protected R visitIsNullPredicate(IsNullPredicate node, C context)
    {
        return visitExpression(node, context);
    }

    protected R visitLongLiteral(LongLiteral node, C context)
    {
        return visitLiteral(node, context);
    }

    protected R visitLogicalBinaryExpression(LogicalBinaryExpression node, C context)
    {
        return visitExpression(node, context);
    }

    protected R visitSubqueryExpression(SubqueryExpression node, C context)
    {
        return visitExpression(node, context);
    }

    protected R visitSortItem(SortItem node, C context)
    {
        return visitNode(node, context);
    }

    protected R visitTable(Table node, C context)
    {
        return visitRelation(node, context);
    }

    protected R visitSubquery(Subquery node, C context)
    {
        return visitRelation(node, context);
    }

    protected R visitAliasedRelation(AliasedRelation node, C context)
    {
        return visitRelation(node, context);
    }

    protected R visitJoin(Join node, C context)
    {
        return visitRelation(node, context);
    }

    protected R visitExists(ExistsPredicate node, C context)
    {
        return visitExpression(node, context);
    }

    protected R visitCast(Cast node, C context)
    {
        return visitExpression(node, context);
    }

    public R visitInputReference(InputReference node, C context)
    {
        return visitExpression(node, context);
    }
}
