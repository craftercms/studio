(function(){function h(p,r){for(var q=0,s=p.length;
q<s;
++q){r(p[q])
}}function f(p,r){if(!Array.prototype.indexOf){var q=p.length;
while(q--){if(p[q]===r){return true
}}return false
}return p.indexOf(r)!=-1
}function i(t,s,w){var v=t.getCursor(),r=w(t,v),p=r;
if(!/^[\w$_]*$/.test(r.string)){r=p={start:v.ch,end:v.ch,string:"",state:r.state,className:r.string==":"?"pig-type":null}
}if(!q){var q=[]
}q.push(p);
var u=k(r,q);
u=u.sort();
if(u.length==1){u.push(" ")
}return{list:u,from:{line:v.line,ch:r.start},to:{line:v.line,ch:r.end}}
}CodeMirror.pigHint=function(p){return i(p,o,function(q,r){return q.getTokenAt(r)
})
};
function d(p){return p.replace(/(?:^|\s)\w/g,function(q){return q.toUpperCase()
})
}var j="VOID IMPORT RETURNS DEFINE LOAD FILTER FOREACH ORDER CUBE DISTINCT COGROUP JOIN CROSS UNION SPLIT INTO IF OTHERWISE ALL AS BY USING INNER OUTER ONSCHEMA PARALLEL PARTITION GROUP AND OR NOT GENERATE FLATTEN ASC DESC IS STREAM THROUGH STORE MAPREDUCE SHIP CACHE INPUT OUTPUT STDERROR STDIN STDOUT LIMIT SAMPLE LEFT RIGHT FULL EQ GT LT GTE LTE NEQ MATCHES TRUE FALSE";
var o=j.split(" ");
var g=j.toLowerCase().split(" ");
var n="BOOLEAN INT LONG FLOAT DOUBLE CHARARRAY BYTEARRAY BAG TUPLE MAP";
var m=n.split(" ");
var b=n.toLowerCase().split(" ");
var c="ABS ACOS ARITY ASIN ATAN AVG BAGSIZE BINSTORAGE BLOOM BUILDBLOOM CBRT CEIL CONCAT COR COS COSH COUNT COUNT_STAR COV CONSTANTSIZE CUBEDIMENSIONS DIFF DISTINCT DOUBLEABS DOUBLEAVG DOUBLEBASE DOUBLEMAX DOUBLEMIN DOUBLEROUND DOUBLESUM EXP FLOOR FLOATABS FLOATAVG FLOATMAX FLOATMIN FLOATROUND FLOATSUM GENERICINVOKER INDEXOF INTABS INTAVG INTMAX INTMIN INTSUM INVOKEFORDOUBLE INVOKEFORFLOAT INVOKEFORINT INVOKEFORLONG INVOKEFORSTRING INVOKER ISEMPTY JSONLOADER JSONMETADATA JSONSTORAGE LAST_INDEX_OF LCFIRST LOG LOG10 LOWER LONGABS LONGAVG LONGMAX LONGMIN LONGSUM MAX MIN MAPSIZE MONITOREDUDF NONDETERMINISTIC OUTPUTSCHEMA  PIGSTORAGE PIGSTREAMING RANDOM REGEX_EXTRACT REGEX_EXTRACT_ALL REPLACE ROUND SIN SINH SIZE SQRT STRSPLIT SUBSTRING SUM STRINGCONCAT STRINGMAX STRINGMIN STRINGSIZE TAN TANH TOBAG TOKENIZE TOMAP TOP TOTUPLE TRIM TEXTLOADER TUPLESIZE UCFIRST UPPER UTF8STORAGECONVERTER";
var e=c.split(" ").join("() ").split(" ");
var l=c.toLowerCase().split(" ").join("() ").split(" ");
var a=("BagSize BinStorage Bloom BuildBloom ConstantSize CubeDimensions DoubleAbs DoubleAvg DoubleBase DoubleMax DoubleMin DoubleRound DoubleSum FloatAbs FloatAvg FloatMax FloatMin FloatRound FloatSum GenericInvoker IntAbs IntAvg IntMax IntMin IntSum InvokeForDouble InvokeForFloat InvokeForInt InvokeForLong InvokeForString Invoker IsEmpty JsonLoader JsonMetadata JsonStorage LongAbs LongAvg LongMax LongMin LongSum MapSize MonitoredUDF Nondeterministic OutputSchema PigStorage PigStreaming StringConcat StringMax StringMin StringSize TextLoader TupleSize Utf8StorageConverter").split(" ").join("() ").split(" ");
function k(r,q){var t=[],w=r.string;
function v(x){if(x.indexOf(w)==0&&!f(t,x)){t.push(x)
}}function p(x){if(x==":"){h(b,v)
}else{h(e,v);
h(l,v);
h(a,v);
h(m,v);
h(b,v);
h(o,v);
h(g,v)
}}if(q){var u=q.pop(),s;
if(u.className=="pig-word"){s=u.string
}else{if(u.className=="pig-type"){s=":"+u.string
}}while(s!=null&&q.length){s=s[q.pop().string]
}if(s!=null){p(s)
}}return t
}})();