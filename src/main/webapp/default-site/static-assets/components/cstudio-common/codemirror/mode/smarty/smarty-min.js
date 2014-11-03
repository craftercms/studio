CodeMirror.defineMode("smarty",function(b,e){var h=["debug","extends","function","include","literal"];
var l;
var c={operatorChars:/[+\-*&%=<>!?]/,validIdentifier:/[a-zA-Z0-9\_]/,stringChar:/[\'\"]/};
var d=(typeof b.mode.leftDelimiter!="undefined")?b.mode.leftDelimiter:"{";
var j=(typeof b.mode.rightDelimiter!="undefined")?b.mode.rightDelimiter:"}";
function g(n,m){l=m;
return n
}function k(o,n){function m(p){n.tokenize=p;
return p(o,n)
}if(o.match(d,true)){if(o.eat("*")){return m(i("comment","*"+j))
}else{n.tokenize=f;
return"tag"
}}else{o.next();
return null
}}function f(r,p){if(r.match(j,true)){p.tokenize=k;
return g("tag",null)
}var o=r.next();
if(o=="$"){r.eatWhile(c.validIdentifier);
return g("variable-2","variable")
}else{if(o=="."){return g("operator","property")
}else{if(c.stringChar.test(o)){p.tokenize=a(o);
return g("string","string")
}else{if(c.operatorChars.test(o)){r.eatWhile(c.operatorChars);
return g("operator","operator")
}else{if(o=="["||o=="]"){return g("bracket","bracket")
}else{if(/\d/.test(o)){r.eatWhile(/\d/);
return g("number","number")
}else{if(p.last=="variable"){if(o=="@"){r.eatWhile(c.validIdentifier);
return g("property","property")
}else{if(o=="|"){r.eatWhile(c.validIdentifier);
return g("qualifier","modifier")
}}}else{if(p.last=="whitespace"){r.eatWhile(c.validIdentifier);
return g("attribute","modifier")
}else{if(p.last=="property"){r.eatWhile(c.validIdentifier);
return g("property",null)
}else{if(/\s/.test(o)){l="whitespace";
return null
}}}}var q="";
if(o!="/"){q+=o
}var s="";
while((s=r.eat(c.validIdentifier))){q+=s
}var n,m;
for(n=0,m=h.length;
n<m;
n++){if(h[n]==q){return g("keyword","keyword")
}}if(/\s/.test(o)){return null
}return g("tag","tag")
}}}}}}}function a(m){return function(o,n){while(!o.eol()){if(o.next()==m){n.tokenize=f;
break
}}return"string"
}
}function i(n,m){return function(p,o){while(!p.eol()){if(p.match(m)){o.tokenize=k;
break
}p.next()
}return n
}
}return{startState:function(){return{tokenize:k,mode:"smarty",last:null}
},token:function(o,n){var m=n.tokenize(o,n);
n.last=l;
return m
},electricChars:""}
});
CodeMirror.defineMIME("text/x-smarty","smarty");