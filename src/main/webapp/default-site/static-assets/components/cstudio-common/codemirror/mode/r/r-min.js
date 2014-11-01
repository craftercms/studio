CodeMirror.defineMode("r",function(d){function a(p){var o=p.split(" "),n={};
for(var m=0;
m<o.length;
++m){n[o[m]]=true
}return n
}var h=a("NULL NA Inf NaN NA_integer_ NA_real_ NA_complex_ NA_character_");
var b=a("list quote bquote eval return call parse deparse");
var g=a("if else repeat while function for in next break");
var f=a("if else repeat while function for");
var c=/[+\-*\/^<>=!&|~$:]/;
var j;
function e(p,n){j=null;
var m=p.next();
if(m=="#"){p.skipToEnd();
return"comment"
}else{if(m=="0"&&p.eat("x")){p.eatWhile(/[\da-f]/i);
return"number"
}else{if(m=="."&&p.eat(/\d/)){p.match(/\d*(?:e[+\-]?\d+)?/);
return"number"
}else{if(/\d/.test(m)){p.match(/\d*(?:\.\d+)?(?:e[+\-]\d+)?L?/);
return"number"
}else{if(m=="'"||m=='"'){n.tokenize=k(m);
return"string"
}else{if(m=="."&&p.match(/.[.\d]+/)){return"keyword"
}else{if(/[\w\.]/.test(m)&&m!="_"){p.eatWhile(/[\w\.]/);
var o=p.current();
if(h.propertyIsEnumerable(o)){return"atom"
}if(g.propertyIsEnumerable(o)){if(f.propertyIsEnumerable(o)){j="block"
}return"keyword"
}if(b.propertyIsEnumerable(o)){return"builtin"
}return"variable"
}else{if(m=="%"){if(p.skipTo("%")){p.next()
}return"variable-2"
}else{if(m=="<"&&p.eat("-")){return"arrow"
}else{if(m=="="&&n.ctx.argList){return"arg-is"
}else{if(c.test(m)){if(m=="$"){return"dollar"
}p.eatWhile(c);
return"operator"
}else{if(/[\(\){}\[\];]/.test(m)){j=m;
if(m==";"){return"semi"
}return null
}else{return null
}}}}}}}}}}}}}function k(m){return function(q,p){if(q.eat("\\")){var o=q.next();
if(o=="x"){q.match(/^[a-f0-9]{2}/i)
}else{if((o=="u"||o=="U")&&q.eat("{")&&q.skipTo("}")){q.next()
}else{if(o=="u"){q.match(/^[a-f0-9]{4}/i)
}else{if(o=="U"){q.match(/^[a-f0-9]{8}/i)
}else{if(/[0-7]/.test(o)){q.match(/^[0-7]{1,2}/)
}}}}}return"string-2"
}else{var n;
while((n=q.next())!=null){if(n==m){p.tokenize=e;
break
}if(n=="\\"){q.backUp(1);
break
}}return"string"
}}
}function i(n,m,o){n.ctx={type:m,indent:n.indent,align:null,column:o.column(),prev:n.ctx}
}function l(m){m.indent=m.ctx.indent;
m.ctx=m.ctx.prev
}return{startState:function(m){return{tokenize:e,ctx:{type:"top",indent:-d.indentUnit,align:false},indent:0,afterIdent:false}
},token:function(p,o){if(p.sol()){if(o.ctx.align==null){o.ctx.align=false
}o.indent=p.indentation()
}if(p.eatSpace()){return null
}var n=o.tokenize(p,o);
if(n!="comment"&&o.ctx.align==null){o.ctx.align=true
}var m=o.ctx.type;
if((j==";"||j=="{"||j=="}")&&m=="block"){l(o)
}if(j=="{"){i(o,"}",p)
}else{if(j=="("){i(o,")",p);
if(o.afterIdent){o.ctx.argList=true
}}else{if(j=="["){i(o,"]",p)
}else{if(j=="block"){i(o,"block",p)
}else{if(j==m){l(o)
}}}}}o.afterIdent=n=="variable"||n=="keyword";
return n
},indent:function(q,n){if(q.tokenize!=e){return 0
}var p=n&&n.charAt(0),m=q.ctx,o=p==m.type;
if(m.type=="block"){return m.indent+(p=="{"?0:d.indentUnit)
}else{if(m.align){return m.column+(o?0:1)
}else{return m.indent+(o?0:d.indentUnit)
}}}}
});
CodeMirror.defineMIME("text/x-rsrc","r");