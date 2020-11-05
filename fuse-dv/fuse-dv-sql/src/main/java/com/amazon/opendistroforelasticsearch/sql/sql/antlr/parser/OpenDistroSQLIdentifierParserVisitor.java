// Generated from OpenDistroSQLIdentifierParser.g4 by ANTLR 4.7.1
package com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link OpenDistroSQLIdentifierParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface OpenDistroSQLIdentifierParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLIdentifierParser#tableName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableName(OpenDistroSQLIdentifierParser.TableNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLIdentifierParser#columnName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnName(OpenDistroSQLIdentifierParser.ColumnNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLIdentifierParser#alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlias(OpenDistroSQLIdentifierParser.AliasContext ctx);
	/**
	 * Visit a parse tree produced by the {@code identsAsQualifiedName}
	 * labeled alternative in {@link OpenDistroSQLIdentifierParser#qualifiedName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentsAsQualifiedName(OpenDistroSQLIdentifierParser.IdentsAsQualifiedNameContext ctx);
	/**
	 * Visit a parse tree produced by the {@code keywordsAsQualifiedName}
	 * labeled alternative in {@link OpenDistroSQLIdentifierParser#qualifiedName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeywordsAsQualifiedName(OpenDistroSQLIdentifierParser.KeywordsAsQualifiedNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLIdentifierParser#ident}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdent(OpenDistroSQLIdentifierParser.IdentContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSQLIdentifierParser#keywordsCanBeId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeywordsCanBeId(OpenDistroSQLIdentifierParser.KeywordsCanBeIdContext ctx);
}