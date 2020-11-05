// Generated from OpenDistroSQLParser.g4 by ANTLR 4.7.1
package com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link OpenDistroSQLParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface OpenDistroSQLParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#root}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoot(OpenDistroSQLParser.RootContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#sqlStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSqlStatement(OpenDistroSQLParser.SqlStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#dmlStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDmlStatement(OpenDistroSQLParser.DmlStatementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code simpleSelect}
	 * labeled alternative in {@link OpenDistroSQLParser#selectStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleSelect(OpenDistroSQLParser.SimpleSelectContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#querySpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuerySpecification(OpenDistroSQLParser.QuerySpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#selectClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectClause(OpenDistroSQLParser.SelectClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#selectElements}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectElements(OpenDistroSQLParser.SelectElementsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#selectElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectElement(OpenDistroSQLParser.SelectElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#fromClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromClause(OpenDistroSQLParser.FromClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#whereClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhereClause(OpenDistroSQLParser.WhereClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#groupByClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupByClause(OpenDistroSQLParser.GroupByClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#groupByElements}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupByElements(OpenDistroSQLParser.GroupByElementsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#groupByElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupByElement(OpenDistroSQLParser.GroupByElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#orderByClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderByClause(OpenDistroSQLParser.OrderByClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#orderByElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderByElement(OpenDistroSQLParser.OrderByElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#windowFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowFunction(OpenDistroSQLParser.WindowFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#rankingWindowFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRankingWindowFunction(OpenDistroSQLParser.RankingWindowFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#overClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOverClause(OpenDistroSQLParser.OverClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#partitionByClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPartitionByClause(OpenDistroSQLParser.PartitionByClauseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code string}
	 * labeled alternative in {@link OpenDistroSQLParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitString(OpenDistroSQLParser.StringContext ctx);
	/**
	 * Visit a parse tree produced by the {@code signedDecimal}
	 * labeled alternative in {@link OpenDistroSQLParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSignedDecimal(OpenDistroSQLParser.SignedDecimalContext ctx);
	/**
	 * Visit a parse tree produced by the {@code signedReal}
	 * labeled alternative in {@link OpenDistroSQLParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSignedReal(OpenDistroSQLParser.SignedRealContext ctx);
	/**
	 * Visit a parse tree produced by the {@code boolean}
	 * labeled alternative in {@link OpenDistroSQLParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBoolean(OpenDistroSQLParser.BooleanContext ctx);
	/**
	 * Visit a parse tree produced by the {@code datetime}
	 * labeled alternative in {@link OpenDistroSQLParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDatetime(OpenDistroSQLParser.DatetimeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code interval}
	 * labeled alternative in {@link OpenDistroSQLParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterval(OpenDistroSQLParser.IntervalContext ctx);
	/**
	 * Visit a parse tree produced by the {@code null}
	 * labeled alternative in {@link OpenDistroSQLParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNull(OpenDistroSQLParser.NullContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#decimalLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDecimalLiteral(OpenDistroSQLParser.DecimalLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#stringLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringLiteral(OpenDistroSQLParser.StringLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#booleanLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanLiteral(OpenDistroSQLParser.BooleanLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#realLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRealLiteral(OpenDistroSQLParser.RealLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#sign}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSign(OpenDistroSQLParser.SignContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#nullLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullLiteral(OpenDistroSQLParser.NullLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#datetimeLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDatetimeLiteral(OpenDistroSQLParser.DatetimeLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#dateLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateLiteral(OpenDistroSQLParser.DateLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#timeLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeLiteral(OpenDistroSQLParser.TimeLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#timestampLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimestampLiteral(OpenDistroSQLParser.TimestampLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#intervalLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntervalLiteral(OpenDistroSQLParser.IntervalLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#intervalUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntervalUnit(OpenDistroSQLParser.IntervalUnitContext ctx);
	/**
	 * Visit a parse tree produced by the {@code orExpression}
	 * labeled alternative in {@link OpenDistroSQLParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrExpression(OpenDistroSQLParser.OrExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code andExpression}
	 * labeled alternative in {@link OpenDistroSQLParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndExpression(OpenDistroSQLParser.AndExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code notExpression}
	 * labeled alternative in {@link OpenDistroSQLParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotExpression(OpenDistroSQLParser.NotExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code predicateExpression}
	 * labeled alternative in {@link OpenDistroSQLParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicateExpression(OpenDistroSQLParser.PredicateExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code expressionAtomPredicate}
	 * labeled alternative in {@link OpenDistroSQLParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionAtomPredicate(OpenDistroSQLParser.ExpressionAtomPredicateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binaryComparisonPredicate}
	 * labeled alternative in {@link OpenDistroSQLParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryComparisonPredicate(OpenDistroSQLParser.BinaryComparisonPredicateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code isNullPredicate}
	 * labeled alternative in {@link OpenDistroSQLParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIsNullPredicate(OpenDistroSQLParser.IsNullPredicateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code likePredicate}
	 * labeled alternative in {@link OpenDistroSQLParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLikePredicate(OpenDistroSQLParser.LikePredicateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code regexpPredicate}
	 * labeled alternative in {@link OpenDistroSQLParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRegexpPredicate(OpenDistroSQLParser.RegexpPredicateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code constantExpressionAtom}
	 * labeled alternative in {@link OpenDistroSQLParser#expressionAtom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantExpressionAtom(OpenDistroSQLParser.ConstantExpressionAtomContext ctx);
	/**
	 * Visit a parse tree produced by the {@code functionCallExpressionAtom}
	 * labeled alternative in {@link OpenDistroSQLParser#expressionAtom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCallExpressionAtom(OpenDistroSQLParser.FunctionCallExpressionAtomContext ctx);
	/**
	 * Visit a parse tree produced by the {@code fullColumnNameExpressionAtom}
	 * labeled alternative in {@link OpenDistroSQLParser#expressionAtom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFullColumnNameExpressionAtom(OpenDistroSQLParser.FullColumnNameExpressionAtomContext ctx);
	/**
	 * Visit a parse tree produced by the {@code nestedExpressionAtom}
	 * labeled alternative in {@link OpenDistroSQLParser#expressionAtom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNestedExpressionAtom(OpenDistroSQLParser.NestedExpressionAtomContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mathExpressionAtom}
	 * labeled alternative in {@link OpenDistroSQLParser#expressionAtom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathExpressionAtom(OpenDistroSQLParser.MathExpressionAtomContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#mathOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathOperator(OpenDistroSQLParser.MathOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonOperator(OpenDistroSQLParser.ComparisonOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#nullNotnull}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullNotnull(OpenDistroSQLParser.NullNotnullContext ctx);
	/**
	 * Visit a parse tree produced by the {@code scalarFunctionCall}
	 * labeled alternative in {@link OpenDistroSQLParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScalarFunctionCall(OpenDistroSQLParser.ScalarFunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by the {@code windowFunctionCall}
	 * labeled alternative in {@link OpenDistroSQLParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowFunctionCall(OpenDistroSQLParser.WindowFunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by the {@code aggregateFunctionCall}
	 * labeled alternative in {@link OpenDistroSQLParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregateFunctionCall(OpenDistroSQLParser.AggregateFunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#scalarFunctionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScalarFunctionName(OpenDistroSQLParser.ScalarFunctionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#aggregateFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregateFunction(OpenDistroSQLParser.AggregateFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#aggregationFunctionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregationFunctionName(OpenDistroSQLParser.AggregationFunctionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#mathematicalFunctionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathematicalFunctionName(OpenDistroSQLParser.MathematicalFunctionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#trigonometricFunctionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrigonometricFunctionName(OpenDistroSQLParser.TrigonometricFunctionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#dateTimeFunctionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeFunctionName(OpenDistroSQLParser.DateTimeFunctionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#textFunctionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTextFunctionName(OpenDistroSQLParser.TextFunctionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#functionArgs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionArgs(OpenDistroSQLParser.FunctionArgsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#functionArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionArg(OpenDistroSQLParser.FunctionArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#tableName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableName(OpenDistroSQLParser.TableNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#columnName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnName(OpenDistroSQLParser.ColumnNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlias(OpenDistroSQLParser.AliasContext ctx);
	/**
	 * Visit a parse tree produced by the {@code identsAsQualifiedName}
	 * labeled alternative in {@link OpenDistroSQLParser#qualifiedName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentsAsQualifiedName(OpenDistroSQLParser.IdentsAsQualifiedNameContext ctx);
	/**
	 * Visit a parse tree produced by the {@code keywordsAsQualifiedName}
	 * labeled alternative in {@link OpenDistroSQLParser#qualifiedName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeywordsAsQualifiedName(OpenDistroSQLParser.KeywordsAsQualifiedNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#ident}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdent(OpenDistroSQLParser.IdentContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLParser#keywordsCanBeId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeywordsCanBeId(OpenDistroSQLParser.KeywordsCanBeIdContext ctx);
}