// Generated from OpenDistroSQLParser.g4 by ANTLR 4.7.1
package com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link OpenDistroSQLParser}.
 */
public interface OpenDistroSQLParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#root}.
	 * @param ctx the parse tree
	 */
	void enterRoot(OpenDistroSQLParser.RootContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#root}.
	 * @param ctx the parse tree
	 */
	void exitRoot(OpenDistroSQLParser.RootContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#sqlStatement}.
	 * @param ctx the parse tree
	 */
	void enterSqlStatement(OpenDistroSQLParser.SqlStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#sqlStatement}.
	 * @param ctx the parse tree
	 */
	void exitSqlStatement(OpenDistroSQLParser.SqlStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#dmlStatement}.
	 * @param ctx the parse tree
	 */
	void enterDmlStatement(OpenDistroSQLParser.DmlStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#dmlStatement}.
	 * @param ctx the parse tree
	 */
	void exitDmlStatement(OpenDistroSQLParser.DmlStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code simpleSelect}
	 * labeled alternative in {@link OpenDistroSQLParser#selectStatement}.
	 * @param ctx the parse tree
	 */
	void enterSimpleSelect(OpenDistroSQLParser.SimpleSelectContext ctx);
	/**
	 * Exit a parse tree produced by the {@code simpleSelect}
	 * labeled alternative in {@link OpenDistroSQLParser#selectStatement}.
	 * @param ctx the parse tree
	 */
	void exitSimpleSelect(OpenDistroSQLParser.SimpleSelectContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#querySpecification}.
	 * @param ctx the parse tree
	 */
	void enterQuerySpecification(OpenDistroSQLParser.QuerySpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#querySpecification}.
	 * @param ctx the parse tree
	 */
	void exitQuerySpecification(OpenDistroSQLParser.QuerySpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#selectClause}.
	 * @param ctx the parse tree
	 */
	void enterSelectClause(OpenDistroSQLParser.SelectClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#selectClause}.
	 * @param ctx the parse tree
	 */
	void exitSelectClause(OpenDistroSQLParser.SelectClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#selectElements}.
	 * @param ctx the parse tree
	 */
	void enterSelectElements(OpenDistroSQLParser.SelectElementsContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#selectElements}.
	 * @param ctx the parse tree
	 */
	void exitSelectElements(OpenDistroSQLParser.SelectElementsContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#selectElement}.
	 * @param ctx the parse tree
	 */
	void enterSelectElement(OpenDistroSQLParser.SelectElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#selectElement}.
	 * @param ctx the parse tree
	 */
	void exitSelectElement(OpenDistroSQLParser.SelectElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#fromClause}.
	 * @param ctx the parse tree
	 */
	void enterFromClause(OpenDistroSQLParser.FromClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#fromClause}.
	 * @param ctx the parse tree
	 */
	void exitFromClause(OpenDistroSQLParser.FromClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#whereClause}.
	 * @param ctx the parse tree
	 */
	void enterWhereClause(OpenDistroSQLParser.WhereClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#whereClause}.
	 * @param ctx the parse tree
	 */
	void exitWhereClause(OpenDistroSQLParser.WhereClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#groupByClause}.
	 * @param ctx the parse tree
	 */
	void enterGroupByClause(OpenDistroSQLParser.GroupByClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#groupByClause}.
	 * @param ctx the parse tree
	 */
	void exitGroupByClause(OpenDistroSQLParser.GroupByClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#groupByElements}.
	 * @param ctx the parse tree
	 */
	void enterGroupByElements(OpenDistroSQLParser.GroupByElementsContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#groupByElements}.
	 * @param ctx the parse tree
	 */
	void exitGroupByElements(OpenDistroSQLParser.GroupByElementsContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#groupByElement}.
	 * @param ctx the parse tree
	 */
	void enterGroupByElement(OpenDistroSQLParser.GroupByElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#groupByElement}.
	 * @param ctx the parse tree
	 */
	void exitGroupByElement(OpenDistroSQLParser.GroupByElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#orderByClause}.
	 * @param ctx the parse tree
	 */
	void enterOrderByClause(OpenDistroSQLParser.OrderByClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#orderByClause}.
	 * @param ctx the parse tree
	 */
	void exitOrderByClause(OpenDistroSQLParser.OrderByClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#orderByElement}.
	 * @param ctx the parse tree
	 */
	void enterOrderByElement(OpenDistroSQLParser.OrderByElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#orderByElement}.
	 * @param ctx the parse tree
	 */
	void exitOrderByElement(OpenDistroSQLParser.OrderByElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#windowFunction}.
	 * @param ctx the parse tree
	 */
	void enterWindowFunction(OpenDistroSQLParser.WindowFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#windowFunction}.
	 * @param ctx the parse tree
	 */
	void exitWindowFunction(OpenDistroSQLParser.WindowFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#rankingWindowFunction}.
	 * @param ctx the parse tree
	 */
	void enterRankingWindowFunction(OpenDistroSQLParser.RankingWindowFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#rankingWindowFunction}.
	 * @param ctx the parse tree
	 */
	void exitRankingWindowFunction(OpenDistroSQLParser.RankingWindowFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#overClause}.
	 * @param ctx the parse tree
	 */
	void enterOverClause(OpenDistroSQLParser.OverClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#overClause}.
	 * @param ctx the parse tree
	 */
	void exitOverClause(OpenDistroSQLParser.OverClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#partitionByClause}.
	 * @param ctx the parse tree
	 */
	void enterPartitionByClause(OpenDistroSQLParser.PartitionByClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#partitionByClause}.
	 * @param ctx the parse tree
	 */
	void exitPartitionByClause(OpenDistroSQLParser.PartitionByClauseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code string}
	 * labeled alternative in {@link OpenDistroSQLParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterString(OpenDistroSQLParser.StringContext ctx);
	/**
	 * Exit a parse tree produced by the {@code string}
	 * labeled alternative in {@link OpenDistroSQLParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitString(OpenDistroSQLParser.StringContext ctx);
	/**
	 * Enter a parse tree produced by the {@code signedDecimal}
	 * labeled alternative in {@link OpenDistroSQLParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterSignedDecimal(OpenDistroSQLParser.SignedDecimalContext ctx);
	/**
	 * Exit a parse tree produced by the {@code signedDecimal}
	 * labeled alternative in {@link OpenDistroSQLParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitSignedDecimal(OpenDistroSQLParser.SignedDecimalContext ctx);
	/**
	 * Enter a parse tree produced by the {@code signedReal}
	 * labeled alternative in {@link OpenDistroSQLParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterSignedReal(OpenDistroSQLParser.SignedRealContext ctx);
	/**
	 * Exit a parse tree produced by the {@code signedReal}
	 * labeled alternative in {@link OpenDistroSQLParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitSignedReal(OpenDistroSQLParser.SignedRealContext ctx);
	/**
	 * Enter a parse tree produced by the {@code boolean}
	 * labeled alternative in {@link OpenDistroSQLParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterBoolean(OpenDistroSQLParser.BooleanContext ctx);
	/**
	 * Exit a parse tree produced by the {@code boolean}
	 * labeled alternative in {@link OpenDistroSQLParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitBoolean(OpenDistroSQLParser.BooleanContext ctx);
	/**
	 * Enter a parse tree produced by the {@code datetime}
	 * labeled alternative in {@link OpenDistroSQLParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterDatetime(OpenDistroSQLParser.DatetimeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code datetime}
	 * labeled alternative in {@link OpenDistroSQLParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitDatetime(OpenDistroSQLParser.DatetimeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code interval}
	 * labeled alternative in {@link OpenDistroSQLParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterInterval(OpenDistroSQLParser.IntervalContext ctx);
	/**
	 * Exit a parse tree produced by the {@code interval}
	 * labeled alternative in {@link OpenDistroSQLParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitInterval(OpenDistroSQLParser.IntervalContext ctx);
	/**
	 * Enter a parse tree produced by the {@code null}
	 * labeled alternative in {@link OpenDistroSQLParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterNull(OpenDistroSQLParser.NullContext ctx);
	/**
	 * Exit a parse tree produced by the {@code null}
	 * labeled alternative in {@link OpenDistroSQLParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitNull(OpenDistroSQLParser.NullContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#decimalLiteral}.
	 * @param ctx the parse tree
	 */
	void enterDecimalLiteral(OpenDistroSQLParser.DecimalLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#decimalLiteral}.
	 * @param ctx the parse tree
	 */
	void exitDecimalLiteral(OpenDistroSQLParser.DecimalLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#stringLiteral}.
	 * @param ctx the parse tree
	 */
	void enterStringLiteral(OpenDistroSQLParser.StringLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#stringLiteral}.
	 * @param ctx the parse tree
	 */
	void exitStringLiteral(OpenDistroSQLParser.StringLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#booleanLiteral}.
	 * @param ctx the parse tree
	 */
	void enterBooleanLiteral(OpenDistroSQLParser.BooleanLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#booleanLiteral}.
	 * @param ctx the parse tree
	 */
	void exitBooleanLiteral(OpenDistroSQLParser.BooleanLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#realLiteral}.
	 * @param ctx the parse tree
	 */
	void enterRealLiteral(OpenDistroSQLParser.RealLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#realLiteral}.
	 * @param ctx the parse tree
	 */
	void exitRealLiteral(OpenDistroSQLParser.RealLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#sign}.
	 * @param ctx the parse tree
	 */
	void enterSign(OpenDistroSQLParser.SignContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#sign}.
	 * @param ctx the parse tree
	 */
	void exitSign(OpenDistroSQLParser.SignContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#nullLiteral}.
	 * @param ctx the parse tree
	 */
	void enterNullLiteral(OpenDistroSQLParser.NullLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#nullLiteral}.
	 * @param ctx the parse tree
	 */
	void exitNullLiteral(OpenDistroSQLParser.NullLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#datetimeLiteral}.
	 * @param ctx the parse tree
	 */
	void enterDatetimeLiteral(OpenDistroSQLParser.DatetimeLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#datetimeLiteral}.
	 * @param ctx the parse tree
	 */
	void exitDatetimeLiteral(OpenDistroSQLParser.DatetimeLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#dateLiteral}.
	 * @param ctx the parse tree
	 */
	void enterDateLiteral(OpenDistroSQLParser.DateLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#dateLiteral}.
	 * @param ctx the parse tree
	 */
	void exitDateLiteral(OpenDistroSQLParser.DateLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#timeLiteral}.
	 * @param ctx the parse tree
	 */
	void enterTimeLiteral(OpenDistroSQLParser.TimeLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#timeLiteral}.
	 * @param ctx the parse tree
	 */
	void exitTimeLiteral(OpenDistroSQLParser.TimeLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#timestampLiteral}.
	 * @param ctx the parse tree
	 */
	void enterTimestampLiteral(OpenDistroSQLParser.TimestampLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#timestampLiteral}.
	 * @param ctx the parse tree
	 */
	void exitTimestampLiteral(OpenDistroSQLParser.TimestampLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#intervalLiteral}.
	 * @param ctx the parse tree
	 */
	void enterIntervalLiteral(OpenDistroSQLParser.IntervalLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#intervalLiteral}.
	 * @param ctx the parse tree
	 */
	void exitIntervalLiteral(OpenDistroSQLParser.IntervalLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#intervalUnit}.
	 * @param ctx the parse tree
	 */
	void enterIntervalUnit(OpenDistroSQLParser.IntervalUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#intervalUnit}.
	 * @param ctx the parse tree
	 */
	void exitIntervalUnit(OpenDistroSQLParser.IntervalUnitContext ctx);
	/**
	 * Enter a parse tree produced by the {@code orExpression}
	 * labeled alternative in {@link OpenDistroSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterOrExpression(OpenDistroSQLParser.OrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code orExpression}
	 * labeled alternative in {@link OpenDistroSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitOrExpression(OpenDistroSQLParser.OrExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code andExpression}
	 * labeled alternative in {@link OpenDistroSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAndExpression(OpenDistroSQLParser.AndExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code andExpression}
	 * labeled alternative in {@link OpenDistroSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAndExpression(OpenDistroSQLParser.AndExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code notExpression}
	 * labeled alternative in {@link OpenDistroSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterNotExpression(OpenDistroSQLParser.NotExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code notExpression}
	 * labeled alternative in {@link OpenDistroSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitNotExpression(OpenDistroSQLParser.NotExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code predicateExpression}
	 * labeled alternative in {@link OpenDistroSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterPredicateExpression(OpenDistroSQLParser.PredicateExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code predicateExpression}
	 * labeled alternative in {@link OpenDistroSQLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitPredicateExpression(OpenDistroSQLParser.PredicateExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code expressionAtomPredicate}
	 * labeled alternative in {@link OpenDistroSQLParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterExpressionAtomPredicate(OpenDistroSQLParser.ExpressionAtomPredicateContext ctx);
	/**
	 * Exit a parse tree produced by the {@code expressionAtomPredicate}
	 * labeled alternative in {@link OpenDistroSQLParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitExpressionAtomPredicate(OpenDistroSQLParser.ExpressionAtomPredicateContext ctx);
	/**
	 * Enter a parse tree produced by the {@code binaryComparisonPredicate}
	 * labeled alternative in {@link OpenDistroSQLParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterBinaryComparisonPredicate(OpenDistroSQLParser.BinaryComparisonPredicateContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binaryComparisonPredicate}
	 * labeled alternative in {@link OpenDistroSQLParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitBinaryComparisonPredicate(OpenDistroSQLParser.BinaryComparisonPredicateContext ctx);
	/**
	 * Enter a parse tree produced by the {@code isNullPredicate}
	 * labeled alternative in {@link OpenDistroSQLParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterIsNullPredicate(OpenDistroSQLParser.IsNullPredicateContext ctx);
	/**
	 * Exit a parse tree produced by the {@code isNullPredicate}
	 * labeled alternative in {@link OpenDistroSQLParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitIsNullPredicate(OpenDistroSQLParser.IsNullPredicateContext ctx);
	/**
	 * Enter a parse tree produced by the {@code likePredicate}
	 * labeled alternative in {@link OpenDistroSQLParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterLikePredicate(OpenDistroSQLParser.LikePredicateContext ctx);
	/**
	 * Exit a parse tree produced by the {@code likePredicate}
	 * labeled alternative in {@link OpenDistroSQLParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitLikePredicate(OpenDistroSQLParser.LikePredicateContext ctx);
	/**
	 * Enter a parse tree produced by the {@code regexpPredicate}
	 * labeled alternative in {@link OpenDistroSQLParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterRegexpPredicate(OpenDistroSQLParser.RegexpPredicateContext ctx);
	/**
	 * Exit a parse tree produced by the {@code regexpPredicate}
	 * labeled alternative in {@link OpenDistroSQLParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitRegexpPredicate(OpenDistroSQLParser.RegexpPredicateContext ctx);
	/**
	 * Enter a parse tree produced by the {@code constantExpressionAtom}
	 * labeled alternative in {@link OpenDistroSQLParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void enterConstantExpressionAtom(OpenDistroSQLParser.ConstantExpressionAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code constantExpressionAtom}
	 * labeled alternative in {@link OpenDistroSQLParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void exitConstantExpressionAtom(OpenDistroSQLParser.ConstantExpressionAtomContext ctx);
	/**
	 * Enter a parse tree produced by the {@code functionCallExpressionAtom}
	 * labeled alternative in {@link OpenDistroSQLParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCallExpressionAtom(OpenDistroSQLParser.FunctionCallExpressionAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code functionCallExpressionAtom}
	 * labeled alternative in {@link OpenDistroSQLParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCallExpressionAtom(OpenDistroSQLParser.FunctionCallExpressionAtomContext ctx);
	/**
	 * Enter a parse tree produced by the {@code fullColumnNameExpressionAtom}
	 * labeled alternative in {@link OpenDistroSQLParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void enterFullColumnNameExpressionAtom(OpenDistroSQLParser.FullColumnNameExpressionAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code fullColumnNameExpressionAtom}
	 * labeled alternative in {@link OpenDistroSQLParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void exitFullColumnNameExpressionAtom(OpenDistroSQLParser.FullColumnNameExpressionAtomContext ctx);
	/**
	 * Enter a parse tree produced by the {@code nestedExpressionAtom}
	 * labeled alternative in {@link OpenDistroSQLParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void enterNestedExpressionAtom(OpenDistroSQLParser.NestedExpressionAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code nestedExpressionAtom}
	 * labeled alternative in {@link OpenDistroSQLParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void exitNestedExpressionAtom(OpenDistroSQLParser.NestedExpressionAtomContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mathExpressionAtom}
	 * labeled alternative in {@link OpenDistroSQLParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void enterMathExpressionAtom(OpenDistroSQLParser.MathExpressionAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mathExpressionAtom}
	 * labeled alternative in {@link OpenDistroSQLParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void exitMathExpressionAtom(OpenDistroSQLParser.MathExpressionAtomContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#mathOperator}.
	 * @param ctx the parse tree
	 */
	void enterMathOperator(OpenDistroSQLParser.MathOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#mathOperator}.
	 * @param ctx the parse tree
	 */
	void exitMathOperator(OpenDistroSQLParser.MathOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterComparisonOperator(OpenDistroSQLParser.ComparisonOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitComparisonOperator(OpenDistroSQLParser.ComparisonOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#nullNotnull}.
	 * @param ctx the parse tree
	 */
	void enterNullNotnull(OpenDistroSQLParser.NullNotnullContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#nullNotnull}.
	 * @param ctx the parse tree
	 */
	void exitNullNotnull(OpenDistroSQLParser.NullNotnullContext ctx);
	/**
	 * Enter a parse tree produced by the {@code scalarFunctionCall}
	 * labeled alternative in {@link OpenDistroSQLParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterScalarFunctionCall(OpenDistroSQLParser.ScalarFunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code scalarFunctionCall}
	 * labeled alternative in {@link OpenDistroSQLParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitScalarFunctionCall(OpenDistroSQLParser.ScalarFunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code windowFunctionCall}
	 * labeled alternative in {@link OpenDistroSQLParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterWindowFunctionCall(OpenDistroSQLParser.WindowFunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code windowFunctionCall}
	 * labeled alternative in {@link OpenDistroSQLParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitWindowFunctionCall(OpenDistroSQLParser.WindowFunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code aggregateFunctionCall}
	 * labeled alternative in {@link OpenDistroSQLParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterAggregateFunctionCall(OpenDistroSQLParser.AggregateFunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code aggregateFunctionCall}
	 * labeled alternative in {@link OpenDistroSQLParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitAggregateFunctionCall(OpenDistroSQLParser.AggregateFunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#scalarFunctionName}.
	 * @param ctx the parse tree
	 */
	void enterScalarFunctionName(OpenDistroSQLParser.ScalarFunctionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#scalarFunctionName}.
	 * @param ctx the parse tree
	 */
	void exitScalarFunctionName(OpenDistroSQLParser.ScalarFunctionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#aggregateFunction}.
	 * @param ctx the parse tree
	 */
	void enterAggregateFunction(OpenDistroSQLParser.AggregateFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#aggregateFunction}.
	 * @param ctx the parse tree
	 */
	void exitAggregateFunction(OpenDistroSQLParser.AggregateFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#aggregationFunctionName}.
	 * @param ctx the parse tree
	 */
	void enterAggregationFunctionName(OpenDistroSQLParser.AggregationFunctionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#aggregationFunctionName}.
	 * @param ctx the parse tree
	 */
	void exitAggregationFunctionName(OpenDistroSQLParser.AggregationFunctionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#mathematicalFunctionName}.
	 * @param ctx the parse tree
	 */
	void enterMathematicalFunctionName(OpenDistroSQLParser.MathematicalFunctionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#mathematicalFunctionName}.
	 * @param ctx the parse tree
	 */
	void exitMathematicalFunctionName(OpenDistroSQLParser.MathematicalFunctionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#trigonometricFunctionName}.
	 * @param ctx the parse tree
	 */
	void enterTrigonometricFunctionName(OpenDistroSQLParser.TrigonometricFunctionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#trigonometricFunctionName}.
	 * @param ctx the parse tree
	 */
	void exitTrigonometricFunctionName(OpenDistroSQLParser.TrigonometricFunctionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#dateTimeFunctionName}.
	 * @param ctx the parse tree
	 */
	void enterDateTimeFunctionName(OpenDistroSQLParser.DateTimeFunctionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#dateTimeFunctionName}.
	 * @param ctx the parse tree
	 */
	void exitDateTimeFunctionName(OpenDistroSQLParser.DateTimeFunctionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#textFunctionName}.
	 * @param ctx the parse tree
	 */
	void enterTextFunctionName(OpenDistroSQLParser.TextFunctionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#textFunctionName}.
	 * @param ctx the parse tree
	 */
	void exitTextFunctionName(OpenDistroSQLParser.TextFunctionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#functionArgs}.
	 * @param ctx the parse tree
	 */
	void enterFunctionArgs(OpenDistroSQLParser.FunctionArgsContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#functionArgs}.
	 * @param ctx the parse tree
	 */
	void exitFunctionArgs(OpenDistroSQLParser.FunctionArgsContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#functionArg}.
	 * @param ctx the parse tree
	 */
	void enterFunctionArg(OpenDistroSQLParser.FunctionArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#functionArg}.
	 * @param ctx the parse tree
	 */
	void exitFunctionArg(OpenDistroSQLParser.FunctionArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#tableName}.
	 * @param ctx the parse tree
	 */
	void enterTableName(OpenDistroSQLParser.TableNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#tableName}.
	 * @param ctx the parse tree
	 */
	void exitTableName(OpenDistroSQLParser.TableNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#columnName}.
	 * @param ctx the parse tree
	 */
	void enterColumnName(OpenDistroSQLParser.ColumnNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#columnName}.
	 * @param ctx the parse tree
	 */
	void exitColumnName(OpenDistroSQLParser.ColumnNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#alias}.
	 * @param ctx the parse tree
	 */
	void enterAlias(OpenDistroSQLParser.AliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#alias}.
	 * @param ctx the parse tree
	 */
	void exitAlias(OpenDistroSQLParser.AliasContext ctx);
	/**
	 * Enter a parse tree produced by the {@code identsAsQualifiedName}
	 * labeled alternative in {@link OpenDistroSQLParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void enterIdentsAsQualifiedName(OpenDistroSQLParser.IdentsAsQualifiedNameContext ctx);
	/**
	 * Exit a parse tree produced by the {@code identsAsQualifiedName}
	 * labeled alternative in {@link OpenDistroSQLParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void exitIdentsAsQualifiedName(OpenDistroSQLParser.IdentsAsQualifiedNameContext ctx);
	/**
	 * Enter a parse tree produced by the {@code keywordsAsQualifiedName}
	 * labeled alternative in {@link OpenDistroSQLParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void enterKeywordsAsQualifiedName(OpenDistroSQLParser.KeywordsAsQualifiedNameContext ctx);
	/**
	 * Exit a parse tree produced by the {@code keywordsAsQualifiedName}
	 * labeled alternative in {@link OpenDistroSQLParser#qualifiedName}.
	 * @param ctx the parse tree
	 */
	void exitKeywordsAsQualifiedName(OpenDistroSQLParser.KeywordsAsQualifiedNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#ident}.
	 * @param ctx the parse tree
	 */
	void enterIdent(OpenDistroSQLParser.IdentContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#ident}.
	 * @param ctx the parse tree
	 */
	void exitIdent(OpenDistroSQLParser.IdentContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSQLParser#keywordsCanBeId}.
	 * @param ctx the parse tree
	 */
	void enterKeywordsCanBeId(OpenDistroSQLParser.KeywordsCanBeIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSQLParser#keywordsCanBeId}.
	 * @param ctx the parse tree
	 */
	void exitKeywordsCanBeId(OpenDistroSQLParser.KeywordsCanBeIdContext ctx);
}