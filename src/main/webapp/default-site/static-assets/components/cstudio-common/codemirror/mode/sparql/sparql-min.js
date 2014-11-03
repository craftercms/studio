CodeMirror.defineMode("sparql",function(b){var e=b.indentUnit;
var j;
function g(l){return new RegExp("^(?:"+l.join("|")+")$","i")
}var a=g(["str","lang","langmatches","datatype","bound","sameterm","isiri","isuri","isblank","isliteral","union","a"]);
var d=g(["base","prefix","select","distinct","reduced","construct","describe","ask","from","named","where","order","limit","offset","filter","optional","graph","by","asc","desc"]);
var f=/[*+\-<>=&|]/;
function c(p,n){var m=p.next();
j=null;
if(m=="$"||m=="?"){p.match(/^[\w\d]*/);
return"variable-2"
}else{if(m=="<"&&!p.match(/^[\s\u00a0=]/,false)){p.match(/^[^\s\u00a0>]*>?/);
return"atom"
}else{if(m=='"'||m=="'"){n.tokenize=k(m);
return n.tokenize(p,n)
}else{if(/[{}\(\),\.;\[\]]/.test(m)){j=m;
return null
}else{if(m=="#"){p.skipToEnd();
return"comment"
}else{if(f.test(m)){p.eatWhile(f);
return null
}else{if(m==":"){p.eatWhile(/[\w\d\._\-]/);
return"atom"
}else{p.eatWhile(/[_\w\d]/);
if(p.eat(":")){p.eatWhile(/[\w\d_\-]/);
return"atom"
}var o=p.current(),l;
if(a.test(o)){return null
}else{if(d.test(o)){return"keyword"
}else{return"variable"
}}}}}}}}}}function k(l){return function(p,n){var o=false,m;
while((m=p.next())!=null){if(m==l&&!o){n.tokenize=c;
break
}o=!o&&m=="\\"
}return"string"
}
}function h(n,m,l){n.context={prev:n.context,indent:n.indent,col:l,type:m}
}function i(l){l.indent=l.context.indent;
l.context=l.context.prev
}return{startState:function(l){return{tokenize:c,context:null,indent:0,col:0}
},token:function(n,m){if(n.sol()){if(m.context&&m.context.align==null){m.context.align=false
}m.indent=n.indentation()
}if(n.eatSpace()){return null
}var l=m.tokenize(n,m);
if(l!="comment"&&m.context&&m.context.align==null&&m.context.type!="pattern"){m.context.align=true
}if(j=="("){h(m,")",n.column())
}else{if(j=="["){h(m,"]",n.column())
}else{if(j=="{"){h(m,"}",n.column())
}else{if(/[\]\}\)]/.test(j)){while(m.context&&m.context.type=="pattern"){i(m)
}if(m.context&&j==m.context.type){i(m)
}}else{if(j=="."&&m.context&&m.context.type=="pattern"){i(m)
}else{if(/atom|string|variable/.test(l)&&m.context){if(/[\}\]]/.test(m.context.type)){h(m,"pattern",n.column())
}else{if(m.context.type=="pattern"&&!m.context.align){m.context.align=true;
m.context.col=n.column()
}}}}}}}}return l
},indent:function(p,l){var o=l&&l.charAt(0);
var n=p.context;
if(/[\]\}]/.test(o)){while(n&&n.type=="pattern"){n=n.prev
}}var m=n&&o==n.type;
if(!n){return 0
}else{if(n.type=="pattern"){return n.col
}else{if(n.align){return n.col+(m?0:1)
}else{return n.indent+(m?0:e)
}}}}}
});
CodeMirror.defineMIME("application/x-sparql-query","sparql");