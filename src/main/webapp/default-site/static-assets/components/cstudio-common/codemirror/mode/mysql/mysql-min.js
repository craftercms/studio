CodeMirror.defineMode("mysql",function(b){var e=b.indentUnit;
var k;
function g(m){return new RegExp("^(?:"+m.join("|")+")$","i")
}var a=g(["str","lang","langmatches","datatype","bound","sameterm","isiri","isuri","isblank","isliteral","union","a"]);
var d=g([("ACCESSIBLE"),("ALTER"),("AS"),("BEFORE"),("BINARY"),("BY"),("CASE"),("CHARACTER"),("COLUMN"),("CONTINUE"),("CROSS"),("CURRENT_TIMESTAMP"),("DATABASE"),("DAY_MICROSECOND"),("DEC"),("DEFAULT"),("DESC"),("DISTINCT"),("DOUBLE"),("EACH"),("ENCLOSED"),("EXIT"),("FETCH"),("FLOAT8"),("FOREIGN"),("GRANT"),("HIGH_PRIORITY"),("HOUR_SECOND"),("IN"),("INNER"),("INSERT"),("INT2"),("INT8"),("INTO"),("JOIN"),("KILL"),("LEFT"),("LINEAR"),("LOCALTIME"),("LONG"),("LOOP"),("MATCH"),("MEDIUMTEXT"),("MINUTE_SECOND"),("NATURAL"),("NULL"),("OPTIMIZE"),("OR"),("OUTER"),("PRIMARY"),("RANGE"),("READ_WRITE"),("REGEXP"),("REPEAT"),("RESTRICT"),("RIGHT"),("SCHEMAS"),("SENSITIVE"),("SHOW"),("SPECIFIC"),("SQLSTATE"),("SQL_CALC_FOUND_ROWS"),("STARTING"),("TERMINATED"),("TINYINT"),("TRAILING"),("UNDO"),("UNLOCK"),("USAGE"),("UTC_DATE"),("VALUES"),("VARCHARACTER"),("WHERE"),("WRITE"),("ZEROFILL"),("ALL"),("AND"),("ASENSITIVE"),("BIGINT"),("BOTH"),("CASCADE"),("CHAR"),("COLLATE"),("CONSTRAINT"),("CREATE"),("CURRENT_TIME"),("CURSOR"),("DAY_HOUR"),("DAY_SECOND"),("DECLARE"),("DELETE"),("DETERMINISTIC"),("DIV"),("DUAL"),("ELSEIF"),("EXISTS"),("FALSE"),("FLOAT4"),("FORCE"),("FULLTEXT"),("HAVING"),("HOUR_MINUTE"),("IGNORE"),("INFILE"),("INSENSITIVE"),("INT1"),("INT4"),("INTERVAL"),("ITERATE"),("KEYS"),("LEAVE"),("LIMIT"),("LOAD"),("LOCK"),("LONGTEXT"),("MASTER_SSL_VERIFY_SERVER_CERT"),("MEDIUMINT"),("MINUTE_MICROSECOND"),("MODIFIES"),("NO_WRITE_TO_BINLOG"),("ON"),("OPTIONALLY"),("OUT"),("PRECISION"),("PURGE"),("READS"),("REFERENCES"),("RENAME"),("REQUIRE"),("REVOKE"),("SCHEMA"),("SELECT"),("SET"),("SPATIAL"),("SQLEXCEPTION"),("SQL_BIG_RESULT"),("SSL"),("TABLE"),("TINYBLOB"),("TO"),("TRUE"),("UNIQUE"),("UPDATE"),("USING"),("UTC_TIMESTAMP"),("VARCHAR"),("WHEN"),("WITH"),("YEAR_MONTH"),("ADD"),("ANALYZE"),("ASC"),("BETWEEN"),("BLOB"),("CALL"),("CHANGE"),("CHECK"),("CONDITION"),("CONVERT"),("CURRENT_DATE"),("CURRENT_USER"),("DATABASES"),("DAY_MINUTE"),("DECIMAL"),("DELAYED"),("DESCRIBE"),("DISTINCTROW"),("DROP"),("ELSE"),("ESCAPED"),("EXPLAIN"),("FLOAT"),("FOR"),("FROM"),("GROUP"),("HOUR_MICROSECOND"),("IF"),("INDEX"),("INOUT"),("INT"),("INT3"),("INTEGER"),("IS"),("KEY"),("LEADING"),("LIKE"),("LINES"),("LOCALTIMESTAMP"),("LONGBLOB"),("LOW_PRIORITY"),("MEDIUMBLOB"),("MIDDLEINT"),("MOD"),("NOT"),("NUMERIC"),("OPTION"),("ORDER"),("OUTFILE"),("PROCEDURE"),("READ"),("REAL"),("RELEASE"),("REPLACE"),("RETURN"),("RLIKE"),("SECOND_MICROSECOND"),("SEPARATOR"),("SMALLINT"),("SQL"),("SQLWARNING"),("SQL_SMALL_RESULT"),("STRAIGHT_JOIN"),("THEN"),("TINYTEXT"),("TRIGGER"),("UNION"),("UNSIGNED"),("USE"),("UTC_TIME"),("VARBINARY"),("VARYING"),("WHILE"),("XOR"),("FULL"),("COLUMNS"),("MIN"),("MAX"),("STDEV"),("COUNT")]);
var f=/[*+\-<>=&|]/;
function c(r,p){var o=r.next();
k=null;
if(o=="$"||o=="?"){r.match(/^[\w\d]*/);
return"variable-2"
}else{if(o=="<"&&!r.match(/^[\s\u00a0=]/,false)){r.match(/^[^\s\u00a0>]*>?/);
return"atom"
}else{if(o=='"'||o=="'"){p.tokenize=l(o);
return p.tokenize(r,p)
}else{if(o=="`"){p.tokenize=i(o);
return p.tokenize(r,p)
}else{if(/[{}\(\),\.;\[\]]/.test(o)){k=o;
return null
}else{if(o=="-"){var m=r.next();
if(m=="-"){r.skipToEnd();
return"comment"
}}else{if(f.test(o)){r.eatWhile(f);
return null
}else{if(o==":"){r.eatWhile(/[\w\d\._\-]/);
return"atom"
}else{r.eatWhile(/[_\w\d]/);
if(r.eat(":")){r.eatWhile(/[\w\d_\-]/);
return"atom"
}var q=r.current(),n;
if(a.test(q)){return null
}else{if(d.test(q)){return"keyword"
}else{return"variable"
}}}}}}}}}}}function l(m){return function(q,o){var p=false,n;
while((n=q.next())!=null){if(n==m&&!p){o.tokenize=c;
break
}p=!p&&n=="\\"
}return"string"
}
}function i(m){return function(q,o){var p=false,n;
while((n=q.next())!=null){if(n==m&&!p){o.tokenize=c;
break
}p=!p&&n=="\\"
}return"variable-2"
}
}function h(o,n,m){o.context={prev:o.context,indent:o.indent,col:m,type:n}
}function j(m){m.indent=m.context.indent;
m.context=m.context.prev
}return{startState:function(m){return{tokenize:c,context:null,indent:0,col:0}
},token:function(o,n){if(o.sol()){if(n.context&&n.context.align==null){n.context.align=false
}n.indent=o.indentation()
}if(o.eatSpace()){return null
}var m=n.tokenize(o,n);
if(m!="comment"&&n.context&&n.context.align==null&&n.context.type!="pattern"){n.context.align=true
}if(k=="("){h(n,")",o.column())
}else{if(k=="["){h(n,"]",o.column())
}else{if(k=="{"){h(n,"}",o.column())
}else{if(/[\]\}\)]/.test(k)){while(n.context&&n.context.type=="pattern"){j(n)
}if(n.context&&k==n.context.type){j(n)
}}else{if(k=="."&&n.context&&n.context.type=="pattern"){j(n)
}else{if(/atom|string|variable/.test(m)&&n.context){if(/[\}\]]/.test(n.context.type)){h(n,"pattern",o.column())
}else{if(n.context.type=="pattern"&&!n.context.align){n.context.align=true;
n.context.col=o.column()
}}}}}}}}return m
},indent:function(q,m){var p=m&&m.charAt(0);
var o=q.context;
if(/[\]\}]/.test(p)){while(o&&o.type=="pattern"){o=o.prev
}}var n=o&&p==o.type;
if(!o){return 0
}else{if(o.type=="pattern"){return o.col
}else{if(o.align){return o.col+(n?0:1)
}else{return o.indent+(n?0:e)
}}}}}
});
CodeMirror.defineMIME("text/x-mysql","mysql");