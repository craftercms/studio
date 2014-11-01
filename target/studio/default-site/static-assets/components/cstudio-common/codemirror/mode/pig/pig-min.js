CodeMirror.defineMode("pig",function(d,f){var h=d.indentUnit,g=f.keywords,b=f.builtins,i=f.types,j=f.multiLineStrings;
var c=/[*+\-%<>=&?:\/!|]/;
function a(q,p,o){p.tokenize=o;
return o(q,p)
}var m;
function l(p,o){m=p;
return o
}function k(r,q){var p=false;
var o;
while(o=r.next()){if(o=="/"&&p){q.tokenize=e;
break
}p=(o=="*")
}return l("comment","comment")
}function n(o){return function(t,r){var s=false,q,p=false;
while((q=t.next())!=null){if(q==o&&!s){p=true;
break
}s=!s&&q=="\\"
}if(p||!(s||j)){r.tokenize=e
}return l("string","error")
}
}function e(q,p){var o=q.next();
if(o=='"'||o=="'"){return a(q,p,n(o))
}else{if(/[\[\]{}\(\),;\.]/.test(o)){return l(o)
}else{if(/\d/.test(o)){q.eatWhile(/[\w\.]/);
return l("number","number")
}else{if(o=="/"){if(q.eat("*")){return a(q,p,k)
}else{q.eatWhile(c);
return l("operator","operator")
}}else{if(o=="-"){if(q.eat("-")){q.skipToEnd();
return l("comment","comment")
}else{q.eatWhile(c);
return l("operator","operator")
}}else{if(c.test(o)){q.eatWhile(c);
return l("operator","operator")
}else{q.eatWhile(/[\w\$_]/);
if(g&&g.propertyIsEnumerable(q.current().toUpperCase())){if(q.eat(")")||q.eat(".")){}else{return("keyword","keyword")
}}if(b&&b.propertyIsEnumerable(q.current().toUpperCase())){return("keyword","variable-2")
}if(i&&i.propertyIsEnumerable(q.current().toUpperCase())){return("keyword","variable-3")
}return l("variable","pig-word")
}}}}}}}return{startState:function(o){return{tokenize:e,startOfLine:true}
},token:function(q,p){if(q.eatSpace()){return null
}var o=p.tokenize(q,p);
return o
}}
});
(function(){function b(h){var f={},g=h.split(" ");
for(var e=0;
e<g.length;
++e){f[g[e]]=true
}return f
}var a="ABS ACOS ARITY ASIN ATAN AVG BAGSIZE BINSTORAGE BLOOM BUILDBLOOM CBRT CEIL CONCAT COR COS COSH COUNT COUNT_STAR COV CONSTANTSIZE CUBEDIMENSIONS DIFF DISTINCT DOUBLEABS DOUBLEAVG DOUBLEBASE DOUBLEMAX DOUBLEMIN DOUBLEROUND DOUBLESUM EXP FLOOR FLOATABS FLOATAVG FLOATMAX FLOATMIN FLOATROUND FLOATSUM GENERICINVOKER INDEXOF INTABS INTAVG INTMAX INTMIN INTSUM INVOKEFORDOUBLE INVOKEFORFLOAT INVOKEFORINT INVOKEFORLONG INVOKEFORSTRING INVOKER ISEMPTY JSONLOADER JSONMETADATA JSONSTORAGE LAST_INDEX_OF LCFIRST LOG LOG10 LOWER LONGABS LONGAVG LONGMAX LONGMIN LONGSUM MAX MIN MAPSIZE MONITOREDUDF NONDETERMINISTIC OUTPUTSCHEMA  PIGSTORAGE PIGSTREAMING RANDOM REGEX_EXTRACT REGEX_EXTRACT_ALL REPLACE ROUND SIN SINH SIZE SQRT STRSPLIT SUBSTRING SUM STRINGCONCAT STRINGMAX STRINGMIN STRINGSIZE TAN TANH TOBAG TOKENIZE TOMAP TOP TOTUPLE TRIM TEXTLOADER TUPLESIZE UCFIRST UPPER UTF8STORAGECONVERTER ";
var c="VOID IMPORT RETURNS DEFINE LOAD FILTER FOREACH ORDER CUBE DISTINCT COGROUP JOIN CROSS UNION SPLIT INTO IF OTHERWISE ALL AS BY USING INNER OUTER ONSCHEMA PARALLEL PARTITION GROUP AND OR NOT GENERATE FLATTEN ASC DESC IS STREAM THROUGH STORE MAPREDUCE SHIP CACHE INPUT OUTPUT STDERROR STDIN STDOUT LIMIT SAMPLE LEFT RIGHT FULL EQ GT LT GTE LTE NEQ MATCHES TRUE FALSE ";
var d="BOOLEAN INT LONG FLOAT DOUBLE CHARARRAY BYTEARRAY BAG TUPLE MAP ";
CodeMirror.defineMIME("text/x-pig",{name:"pig",builtins:b(a),keywords:b(c),types:b(d)})
}());