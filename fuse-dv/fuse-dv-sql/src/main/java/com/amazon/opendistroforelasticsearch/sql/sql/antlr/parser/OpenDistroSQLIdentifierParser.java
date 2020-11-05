// Generated from OpenDistroSQLIdentifierParser.g4 by ANTLR 4.7.1
package com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class OpenDistroSQLIdentifierParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		SPACE=1, SPEC_SQL_COMMENT=2, COMMENT_INPUT=3, LINE_COMMENT=4, ALL=5, AND=6, 
		AS=7, ASC=8, BETWEEN=9, BY=10, CASE=11, CAST=12, CROSS=13, DATETIME=14, 
		DELETE=15, DESC=16, DESCRIBE=17, DISTINCT=18, DOUBLE=19, ELSE=20, EXISTS=21, 
		FALSE=22, FLOAT=23, FROM=24, GROUP=25, HAVING=26, IN=27, INNER=28, INT=29, 
		IS=30, JOIN=31, LEFT=32, LIKE=33, LIMIT=34, LONG=35, MATCH=36, NATURAL=37, 
		MISSING_LITERAL=38, NOT=39, NULL_LITERAL=40, ON=41, OR=42, ORDER=43, OUTER=44, 
		OVER=45, PARTITION=46, REGEXP=47, RIGHT=48, SELECT=49, SHOW=50, STRING=51, 
		THEN=52, TRUE=53, UNION=54, USING=55, WHEN=56, WHERE=57, MISSING=58, EXCEPT=59, 
		AVG=60, COUNT=61, MAX=62, MIN=63, SUM=64, SUBSTRING=65, TRIM=66, END=67, 
		FULL=68, OFFSET=69, INTERVAL=70, MICROSECOND=71, SECOND=72, MINUTE=73, 
		HOUR=74, DAY=75, WEEK=76, MONTH=77, QUARTER=78, YEAR=79, SECOND_MICROSECOND=80, 
		MINUTE_MICROSECOND=81, MINUTE_SECOND=82, HOUR_MICROSECOND=83, HOUR_SECOND=84, 
		HOUR_MINUTE=85, DAY_MICROSECOND=86, DAY_SECOND=87, DAY_MINUTE=88, DAY_HOUR=89, 
		YEAR_MONTH=90, TABLES=91, ABS=92, ACOS=93, ADD=94, ASCII=95, ASIN=96, 
		ATAN=97, ATAN2=98, CBRT=99, CEIL=100, CEILING=101, CONCAT=102, CONCAT_WS=103, 
		CONV=104, COS=105, COSH=106, COT=107, CRC32=108, CURDATE=109, DATE=110, 
		DATE_FORMAT=111, DATE_ADD=112, DATE_SUB=113, DAYOFMONTH=114, DAYOFWEEK=115, 
		DAYOFYEAR=116, DAYNAME=117, DEGREES=118, E=119, EXP=120, EXPM1=121, FLOOR=122, 
		FROM_DAYS=123, IF=124, IFNULL=125, ISNULL=126, LENGTH=127, LN=128, LOCATE=129, 
		LOG=130, LOG10=131, LOG2=132, LOWER=133, LTRIM=134, MAKETIME=135, MODULUS=136, 
		MONTHNAME=137, MULTIPLY=138, NOW=139, PI=140, POW=141, POWER=142, RADIANS=143, 
		RAND=144, REPLACE=145, RINT=146, ROUND=147, RTRIM=148, SIGN=149, SIGNUM=150, 
		SIN=151, SINH=152, SQRT=153, SUBDATE=154, SUBTRACT=155, TAN=156, TIME=157, 
		TIME_TO_SEC=158, TIMESTAMP=159, TRUNCATE=160, TO_DAYS=161, UPPER=162, 
		D=163, T=164, TS=165, LEFT_BRACE=166, RIGHT_BRACE=167, DENSE_RANK=168, 
		RANK=169, ROW_NUMBER=170, DATE_HISTOGRAM=171, DAY_OF_MONTH=172, DAY_OF_YEAR=173, 
		DAY_OF_WEEK=174, EXCLUDE=175, EXTENDED_STATS=176, FIELD=177, FILTER=178, 
		GEO_BOUNDING_BOX=179, GEO_CELL=180, GEO_DISTANCE=181, GEO_DISTANCE_RANGE=182, 
		GEO_INTERSECTS=183, GEO_POLYGON=184, HISTOGRAM=185, HOUR_OF_DAY=186, INCLUDE=187, 
		IN_TERMS=188, MATCHPHRASE=189, MATCH_PHRASE=190, MATCHQUERY=191, MATCH_QUERY=192, 
		MINUTE_OF_DAY=193, MINUTE_OF_HOUR=194, MONTH_OF_YEAR=195, MULTIMATCH=196, 
		MULTI_MATCH=197, NESTED=198, PERCENTILES=199, REGEXP_QUERY=200, REVERSE_NESTED=201, 
		QUERY=202, RANGE=203, SCORE=204, SECOND_OF_MINUTE=205, STATS=206, TERM=207, 
		TERMS=208, TOPHITS=209, WEEK_OF_YEAR=210, WILDCARDQUERY=211, WILDCARD_QUERY=212, 
		SUBSTR=213, STRCMP=214, ADDDATE=215, STAR=216, DIVIDE=217, MODULE=218, 
		PLUS=219, MINUS=220, DIV=221, MOD=222, EQUAL_SYMBOL=223, GREATER_SYMBOL=224, 
		LESS_SYMBOL=225, EXCLAMATION_SYMBOL=226, BIT_NOT_OP=227, BIT_OR_OP=228, 
		BIT_AND_OP=229, BIT_XOR_OP=230, DOT=231, LR_BRACKET=232, RR_BRACKET=233, 
		COMMA=234, SEMI=235, AT_SIGN=236, ZERO_DECIMAL=237, ONE_DECIMAL=238, TWO_DECIMAL=239, 
		SINGLE_QUOTE_SYMB=240, DOUBLE_QUOTE_SYMB=241, REVERSE_QUOTE_SYMB=242, 
		COLON_SYMB=243, START_NATIONAL_STRING_LITERAL=244, STRING_LITERAL=245, 
		DECIMAL_LITERAL=246, HEXADECIMAL_LITERAL=247, REAL_LITERAL=248, NULL_SPEC_LITERAL=249, 
		BIT_STRING=250, ID=251, DOUBLE_QUOTE_ID=252, BACKTICK_QUOTE_ID=253, ERROR_RECOGNITION=254;
	public static final int
		RULE_tableName = 0, RULE_columnName = 1, RULE_alias = 2, RULE_qualifiedName = 3, 
		RULE_ident = 4, RULE_keywordsCanBeId = 5;
	public static final String[] ruleNames = {
		"tableName", "columnName", "alias", "qualifiedName", "ident", "keywordsCanBeId"
	};

	private static final String[] _LITERAL_NAMES = {
		null, null, null, null, null, "'ALL'", "'AND'", "'AS'", "'ASC'", "'BETWEEN'", 
		"'BY'", "'CASE'", "'CAST'", "'CROSS'", "'DATETIME'", "'DELETE'", "'DESC'", 
		"'DESCRIBE'", "'DISTINCT'", "'DOUBLE'", "'ELSE'", "'EXISTS'", "'FALSE'", 
		"'FLOAT'", "'FROM'", "'GROUP'", "'HAVING'", "'IN'", "'INNER'", "'INT'", 
		"'IS'", "'JOIN'", "'LEFT'", "'LIKE'", "'LIMIT'", "'LONG'", "'MATCH'", 
		"'NATURAL'", null, "'NOT'", "'NULL'", "'ON'", "'OR'", "'ORDER'", "'OUTER'", 
		"'OVER'", "'PARTITION'", "'REGEXP'", "'RIGHT'", "'SELECT'", "'SHOW'", 
		"'STRING'", "'THEN'", "'TRUE'", "'UNION'", "'USING'", "'WHEN'", "'WHERE'", 
		null, "'MINUS'", "'AVG'", "'COUNT'", "'MAX'", "'MIN'", "'SUM'", "'SUBSTRING'", 
		"'TRIM'", "'END'", "'FULL'", "'OFFSET'", "'INTERVAL'", "'MICROSECOND'", 
		"'SECOND'", "'MINUTE'", "'HOUR'", "'DAY'", "'WEEK'", "'MONTH'", "'QUARTER'", 
		"'YEAR'", "'SECOND_MICROSECOND'", "'MINUTE_MICROSECOND'", "'MINUTE_SECOND'", 
		"'HOUR_MICROSECOND'", "'HOUR_SECOND'", "'HOUR_MINUTE'", "'DAY_MICROSECOND'", 
		"'DAY_SECOND'", "'DAY_MINUTE'", "'DAY_HOUR'", "'YEAR_MONTH'", "'TABLES'", 
		"'ABS'", "'ACOS'", "'ADD'", "'ASCII'", "'ASIN'", "'ATAN'", "'ATAN2'", 
		"'CBRT'", "'CEIL'", "'CEILING'", "'CONCAT'", "'CONCAT_WS'", "'CONV'", 
		"'COS'", "'COSH'", "'COT'", "'CRC32'", "'CURDATE'", "'DATE'", "'DATE_FORMAT'", 
		"'DATE_ADD'", "'DATE_SUB'", "'DAYOFMONTH'", "'DAYOFWEEK'", "'DAYOFYEAR'", 
		"'DAYNAME'", "'DEGREES'", "'E'", "'EXP'", "'EXPM1'", "'FLOOR'", "'FROM_DAYS'", 
		"'IF'", "'IFNULL'", "'ISNULL'", "'LENGTH'", "'LN'", "'LOCATE'", "'LOG'", 
		"'LOG10'", "'LOG2'", "'LOWER'", "'LTRIM'", "'MAKETIME'", "'MODULUS'", 
		"'MONTHNAME'", "'MULTIPLY'", "'NOW'", "'PI'", "'POW'", "'POWER'", "'RADIANS'", 
		"'RAND'", "'REPLACE'", "'RINT'", "'ROUND'", "'RTRIM'", "'SIGN'", "'SIGNUM'", 
		"'SIN'", "'SINH'", "'SQRT'", "'SUBDATE'", "'SUBTRACT'", "'TAN'", "'TIME'", 
		"'TIME_TO_SEC'", "'TIMESTAMP'", "'TRUNCATE'", "'TO_DAYS'", "'UPPER'", 
		"'D'", "'T'", "'TS'", "'{'", "'}'", "'DENSE_RANK'", "'RANK'", "'ROW_NUMBER'", 
		"'DATE_HISTOGRAM'", "'DAY_OF_MONTH'", "'DAY_OF_YEAR'", "'DAY_OF_WEEK'", 
		"'EXCLUDE'", "'EXTENDED_STATS'", "'FIELD'", "'FILTER'", "'GEO_BOUNDING_BOX'", 
		"'GEO_CELL'", "'GEO_DISTANCE'", "'GEO_DISTANCE_RANGE'", "'GEO_INTERSECTS'", 
		"'GEO_POLYGON'", "'HISTOGRAM'", "'HOUR_OF_DAY'", "'INCLUDE'", "'IN_TERMS'", 
		"'MATCHPHRASE'", "'MATCH_PHRASE'", "'MATCHQUERY'", "'MATCH_QUERY'", "'MINUTE_OF_DAY'", 
		"'MINUTE_OF_HOUR'", "'MONTH_OF_YEAR'", "'MULTIMATCH'", "'MULTI_MATCH'", 
		"'NESTED'", "'PERCENTILES'", "'REGEXP_QUERY'", "'REVERSE_NESTED'", "'QUERY'", 
		"'RANGE'", "'SCORE'", "'SECOND_OF_MINUTE'", "'STATS'", "'TERM'", "'TERMS'", 
		"'TOPHITS'", "'WEEK_OF_YEAR'", "'WILDCARDQUERY'", "'WILDCARD_QUERY'", 
		"'SUBSTR'", "'STRCMP'", "'ADDDATE'", "'*'", "'/'", "'%'", "'+'", "'-'", 
		"'DIV'", "'MOD'", "'='", "'>'", "'<'", "'!'", "'~'", "'|'", "'&'", "'^'", 
		"'.'", "'('", "')'", "','", "';'", "'@'", "'0'", "'1'", "'2'", "'''", 
		"'\"'", "'`'", "':'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "SPACE", "SPEC_SQL_COMMENT", "COMMENT_INPUT", "LINE_COMMENT", "ALL", 
		"AND", "AS", "ASC", "BETWEEN", "BY", "CASE", "CAST", "CROSS", "DATETIME", 
		"DELETE", "DESC", "DESCRIBE", "DISTINCT", "DOUBLE", "ELSE", "EXISTS", 
		"FALSE", "FLOAT", "FROM", "GROUP", "HAVING", "IN", "INNER", "INT", "IS", 
		"JOIN", "LEFT", "LIKE", "LIMIT", "LONG", "MATCH", "NATURAL", "MISSING_LITERAL", 
		"NOT", "NULL_LITERAL", "ON", "OR", "ORDER", "OUTER", "OVER", "PARTITION", 
		"REGEXP", "RIGHT", "SELECT", "SHOW", "STRING", "THEN", "TRUE", "UNION", 
		"USING", "WHEN", "WHERE", "MISSING", "EXCEPT", "AVG", "COUNT", "MAX", 
		"MIN", "SUM", "SUBSTRING", "TRIM", "END", "FULL", "OFFSET", "INTERVAL", 
		"MICROSECOND", "SECOND", "MINUTE", "HOUR", "DAY", "WEEK", "MONTH", "QUARTER", 
		"YEAR", "SECOND_MICROSECOND", "MINUTE_MICROSECOND", "MINUTE_SECOND", "HOUR_MICROSECOND", 
		"HOUR_SECOND", "HOUR_MINUTE", "DAY_MICROSECOND", "DAY_SECOND", "DAY_MINUTE", 
		"DAY_HOUR", "YEAR_MONTH", "TABLES", "ABS", "ACOS", "ADD", "ASCII", "ASIN", 
		"ATAN", "ATAN2", "CBRT", "CEIL", "CEILING", "CONCAT", "CONCAT_WS", "CONV", 
		"COS", "COSH", "COT", "CRC32", "CURDATE", "DATE", "DATE_FORMAT", "DATE_ADD", 
		"DATE_SUB", "DAYOFMONTH", "DAYOFWEEK", "DAYOFYEAR", "DAYNAME", "DEGREES", 
		"E", "EXP", "EXPM1", "FLOOR", "FROM_DAYS", "IF", "IFNULL", "ISNULL", "LENGTH", 
		"LN", "LOCATE", "LOG", "LOG10", "LOG2", "LOWER", "LTRIM", "MAKETIME", 
		"MODULUS", "MONTHNAME", "MULTIPLY", "NOW", "PI", "POW", "POWER", "RADIANS", 
		"RAND", "REPLACE", "RINT", "ROUND", "RTRIM", "SIGN", "SIGNUM", "SIN", 
		"SINH", "SQRT", "SUBDATE", "SUBTRACT", "TAN", "TIME", "TIME_TO_SEC", "TIMESTAMP", 
		"TRUNCATE", "TO_DAYS", "UPPER", "D", "T", "TS", "LEFT_BRACE", "RIGHT_BRACE", 
		"DENSE_RANK", "RANK", "ROW_NUMBER", "DATE_HISTOGRAM", "DAY_OF_MONTH", 
		"DAY_OF_YEAR", "DAY_OF_WEEK", "EXCLUDE", "EXTENDED_STATS", "FIELD", "FILTER", 
		"GEO_BOUNDING_BOX", "GEO_CELL", "GEO_DISTANCE", "GEO_DISTANCE_RANGE", 
		"GEO_INTERSECTS", "GEO_POLYGON", "HISTOGRAM", "HOUR_OF_DAY", "INCLUDE", 
		"IN_TERMS", "MATCHPHRASE", "MATCH_PHRASE", "MATCHQUERY", "MATCH_QUERY", 
		"MINUTE_OF_DAY", "MINUTE_OF_HOUR", "MONTH_OF_YEAR", "MULTIMATCH", "MULTI_MATCH", 
		"NESTED", "PERCENTILES", "REGEXP_QUERY", "REVERSE_NESTED", "QUERY", "RANGE", 
		"SCORE", "SECOND_OF_MINUTE", "STATS", "TERM", "TERMS", "TOPHITS", "WEEK_OF_YEAR", 
		"WILDCARDQUERY", "WILDCARD_QUERY", "SUBSTR", "STRCMP", "ADDDATE", "STAR", 
		"DIVIDE", "MODULE", "PLUS", "MINUS", "DIV", "MOD", "EQUAL_SYMBOL", "GREATER_SYMBOL", 
		"LESS_SYMBOL", "EXCLAMATION_SYMBOL", "BIT_NOT_OP", "BIT_OR_OP", "BIT_AND_OP", 
		"BIT_XOR_OP", "DOT", "LR_BRACKET", "RR_BRACKET", "COMMA", "SEMI", "AT_SIGN", 
		"ZERO_DECIMAL", "ONE_DECIMAL", "TWO_DECIMAL", "SINGLE_QUOTE_SYMB", "DOUBLE_QUOTE_SYMB", 
		"REVERSE_QUOTE_SYMB", "COLON_SYMB", "START_NATIONAL_STRING_LITERAL", "STRING_LITERAL", 
		"DECIMAL_LITERAL", "HEXADECIMAL_LITERAL", "REAL_LITERAL", "NULL_SPEC_LITERAL", 
		"BIT_STRING", "ID", "DOUBLE_QUOTE_ID", "BACKTICK_QUOTE_ID", "ERROR_RECOGNITION"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "OpenDistroSQLIdentifierParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public OpenDistroSQLIdentifierParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class TableNameContext extends ParserRuleContext {
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public TableNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OpenDistroSQLIdentifierParserListener ) ((OpenDistroSQLIdentifierParserListener)listener).enterTableName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OpenDistroSQLIdentifierParserListener ) ((OpenDistroSQLIdentifierParserListener)listener).exitTableName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OpenDistroSQLIdentifierParserVisitor ) return ((OpenDistroSQLIdentifierParserVisitor<? extends T>)visitor).visitTableName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableNameContext tableName() throws RecognitionException {
		TableNameContext _localctx = new TableNameContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_tableName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(12);
			qualifiedName();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ColumnNameContext extends ParserRuleContext {
		public QualifiedNameContext qualifiedName() {
			return getRuleContext(QualifiedNameContext.class,0);
		}
		public ColumnNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OpenDistroSQLIdentifierParserListener ) ((OpenDistroSQLIdentifierParserListener)listener).enterColumnName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OpenDistroSQLIdentifierParserListener ) ((OpenDistroSQLIdentifierParserListener)listener).exitColumnName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OpenDistroSQLIdentifierParserVisitor ) return ((OpenDistroSQLIdentifierParserVisitor<? extends T>)visitor).visitColumnName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColumnNameContext columnName() throws RecognitionException {
		ColumnNameContext _localctx = new ColumnNameContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_columnName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(14);
			qualifiedName();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AliasContext extends ParserRuleContext {
		public IdentContext ident() {
			return getRuleContext(IdentContext.class,0);
		}
		public AliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OpenDistroSQLIdentifierParserListener ) ((OpenDistroSQLIdentifierParserListener)listener).enterAlias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OpenDistroSQLIdentifierParserListener ) ((OpenDistroSQLIdentifierParserListener)listener).exitAlias(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OpenDistroSQLIdentifierParserVisitor ) return ((OpenDistroSQLIdentifierParserVisitor<? extends T>)visitor).visitAlias(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AliasContext alias() throws RecognitionException {
		AliasContext _localctx = new AliasContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_alias);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(16);
			ident();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class QualifiedNameContext extends ParserRuleContext {
		public QualifiedNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedName; }
	 
		public QualifiedNameContext() { }
		public void copyFrom(QualifiedNameContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class IdentsAsQualifiedNameContext extends QualifiedNameContext {
		public List<IdentContext> ident() {
			return getRuleContexts(IdentContext.class);
		}
		public IdentContext ident(int i) {
			return getRuleContext(IdentContext.class,i);
		}
		public List<TerminalNode> DOT() { return getTokens(OpenDistroSQLIdentifierParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(OpenDistroSQLIdentifierParser.DOT, i);
		}
		public IdentsAsQualifiedNameContext(QualifiedNameContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OpenDistroSQLIdentifierParserListener ) ((OpenDistroSQLIdentifierParserListener)listener).enterIdentsAsQualifiedName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OpenDistroSQLIdentifierParserListener ) ((OpenDistroSQLIdentifierParserListener)listener).exitIdentsAsQualifiedName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OpenDistroSQLIdentifierParserVisitor ) return ((OpenDistroSQLIdentifierParserVisitor<? extends T>)visitor).visitIdentsAsQualifiedName(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class KeywordsAsQualifiedNameContext extends QualifiedNameContext {
		public KeywordsCanBeIdContext keywordsCanBeId() {
			return getRuleContext(KeywordsCanBeIdContext.class,0);
		}
		public KeywordsAsQualifiedNameContext(QualifiedNameContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OpenDistroSQLIdentifierParserListener ) ((OpenDistroSQLIdentifierParserListener)listener).enterKeywordsAsQualifiedName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OpenDistroSQLIdentifierParserListener ) ((OpenDistroSQLIdentifierParserListener)listener).exitKeywordsAsQualifiedName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OpenDistroSQLIdentifierParserVisitor ) return ((OpenDistroSQLIdentifierParserVisitor<? extends T>)visitor).visitKeywordsAsQualifiedName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualifiedNameContext qualifiedName() throws RecognitionException {
		QualifiedNameContext _localctx = new QualifiedNameContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_qualifiedName);
		int _la;
		try {
			setState(27);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DOT:
			case ID:
			case DOUBLE_QUOTE_ID:
			case BACKTICK_QUOTE_ID:
				_localctx = new IdentsAsQualifiedNameContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(18);
				ident();
				setState(23);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==DOT) {
					{
					{
					setState(19);
					match(DOT);
					setState(20);
					ident();
					}
					}
					setState(25);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case AVG:
			case COUNT:
			case MAX:
			case MIN:
			case SUM:
			case FULL:
			case DATE:
			case DAYOFWEEK:
			case TIME:
			case TIMESTAMP:
			case D:
			case T:
			case TS:
			case FIELD:
				_localctx = new KeywordsAsQualifiedNameContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(26);
				keywordsCanBeId();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IdentContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(OpenDistroSQLIdentifierParser.ID, 0); }
		public TerminalNode DOT() { return getToken(OpenDistroSQLIdentifierParser.DOT, 0); }
		public TerminalNode DOUBLE_QUOTE_ID() { return getToken(OpenDistroSQLIdentifierParser.DOUBLE_QUOTE_ID, 0); }
		public TerminalNode BACKTICK_QUOTE_ID() { return getToken(OpenDistroSQLIdentifierParser.BACKTICK_QUOTE_ID, 0); }
		public IdentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ident; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OpenDistroSQLIdentifierParserListener ) ((OpenDistroSQLIdentifierParserListener)listener).enterIdent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OpenDistroSQLIdentifierParserListener ) ((OpenDistroSQLIdentifierParserListener)listener).exitIdent(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OpenDistroSQLIdentifierParserVisitor ) return ((OpenDistroSQLIdentifierParserVisitor<? extends T>)visitor).visitIdent(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentContext ident() throws RecognitionException {
		IdentContext _localctx = new IdentContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_ident);
		int _la;
		try {
			setState(35);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DOT:
			case ID:
				enterOuterAlt(_localctx, 1);
				{
				setState(30);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DOT) {
					{
					setState(29);
					match(DOT);
					}
				}

				setState(32);
				match(ID);
				}
				break;
			case DOUBLE_QUOTE_ID:
				enterOuterAlt(_localctx, 2);
				{
				setState(33);
				match(DOUBLE_QUOTE_ID);
				}
				break;
			case BACKTICK_QUOTE_ID:
				enterOuterAlt(_localctx, 3);
				{
				setState(34);
				match(BACKTICK_QUOTE_ID);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class KeywordsCanBeIdContext extends ParserRuleContext {
		public TerminalNode FULL() { return getToken(OpenDistroSQLIdentifierParser.FULL, 0); }
		public TerminalNode FIELD() { return getToken(OpenDistroSQLIdentifierParser.FIELD, 0); }
		public TerminalNode D() { return getToken(OpenDistroSQLIdentifierParser.D, 0); }
		public TerminalNode T() { return getToken(OpenDistroSQLIdentifierParser.T, 0); }
		public TerminalNode TS() { return getToken(OpenDistroSQLIdentifierParser.TS, 0); }
		public TerminalNode COUNT() { return getToken(OpenDistroSQLIdentifierParser.COUNT, 0); }
		public TerminalNode SUM() { return getToken(OpenDistroSQLIdentifierParser.SUM, 0); }
		public TerminalNode AVG() { return getToken(OpenDistroSQLIdentifierParser.AVG, 0); }
		public TerminalNode MAX() { return getToken(OpenDistroSQLIdentifierParser.MAX, 0); }
		public TerminalNode MIN() { return getToken(OpenDistroSQLIdentifierParser.MIN, 0); }
		public TerminalNode TIMESTAMP() { return getToken(OpenDistroSQLIdentifierParser.TIMESTAMP, 0); }
		public TerminalNode DATE() { return getToken(OpenDistroSQLIdentifierParser.DATE, 0); }
		public TerminalNode TIME() { return getToken(OpenDistroSQLIdentifierParser.TIME, 0); }
		public TerminalNode DAYOFWEEK() { return getToken(OpenDistroSQLIdentifierParser.DAYOFWEEK, 0); }
		public KeywordsCanBeIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keywordsCanBeId; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof OpenDistroSQLIdentifierParserListener ) ((OpenDistroSQLIdentifierParserListener)listener).enterKeywordsCanBeId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof OpenDistroSQLIdentifierParserListener ) ((OpenDistroSQLIdentifierParserListener)listener).exitKeywordsCanBeId(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OpenDistroSQLIdentifierParserVisitor ) return ((OpenDistroSQLIdentifierParserVisitor<? extends T>)visitor).visitKeywordsCanBeId(this);
			else return visitor.visitChildren(this);
		}
	}

	public final KeywordsCanBeIdContext keywordsCanBeId() throws RecognitionException {
		KeywordsCanBeIdContext _localctx = new KeywordsCanBeIdContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_keywordsCanBeId);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(37);
			_la = _input.LA(1);
			if ( !(((((_la - 60)) & ~0x3f) == 0 && ((1L << (_la - 60)) & ((1L << (AVG - 60)) | (1L << (COUNT - 60)) | (1L << (MAX - 60)) | (1L << (MIN - 60)) | (1L << (SUM - 60)) | (1L << (FULL - 60)) | (1L << (DATE - 60)) | (1L << (DAYOFWEEK - 60)))) != 0) || ((((_la - 157)) & ~0x3f) == 0 && ((1L << (_la - 157)) & ((1L << (TIME - 157)) | (1L << (TIMESTAMP - 157)) | (1L << (D - 157)) | (1L << (T - 157)) | (1L << (TS - 157)) | (1L << (FIELD - 157)))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\u0100*\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5"+
		"\3\5\7\5\30\n\5\f\5\16\5\33\13\5\3\5\5\5\36\n\5\3\6\5\6!\n\6\3\6\3\6\3"+
		"\6\5\6&\n\6\3\7\3\7\3\7\2\2\b\2\4\6\b\n\f\2\3\n\2>BFFppuu\u009f\u009f"+
		"\u00a1\u00a1\u00a5\u00a7\u00b3\u00b3\2(\2\16\3\2\2\2\4\20\3\2\2\2\6\22"+
		"\3\2\2\2\b\35\3\2\2\2\n%\3\2\2\2\f\'\3\2\2\2\16\17\5\b\5\2\17\3\3\2\2"+
		"\2\20\21\5\b\5\2\21\5\3\2\2\2\22\23\5\n\6\2\23\7\3\2\2\2\24\31\5\n\6\2"+
		"\25\26\7\u00e9\2\2\26\30\5\n\6\2\27\25\3\2\2\2\30\33\3\2\2\2\31\27\3\2"+
		"\2\2\31\32\3\2\2\2\32\36\3\2\2\2\33\31\3\2\2\2\34\36\5\f\7\2\35\24\3\2"+
		"\2\2\35\34\3\2\2\2\36\t\3\2\2\2\37!\7\u00e9\2\2 \37\3\2\2\2 !\3\2\2\2"+
		"!\"\3\2\2\2\"&\7\u00fd\2\2#&\7\u00fe\2\2$&\7\u00ff\2\2% \3\2\2\2%#\3\2"+
		"\2\2%$\3\2\2\2&\13\3\2\2\2\'(\t\2\2\2(\r\3\2\2\2\6\31\35 %";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}