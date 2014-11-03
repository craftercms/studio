CodeMirror.defineMode("stex",function(e,h){function j(m,n){m.cmdState.push(n)
}function i(m){if(m.cmdState.length>0){return m.cmdState[m.cmdState.length-1]
}else{return null
}}function l(n){if(n.cmdState.length>0){var m=n.cmdState.pop();
m.closeBracket()
}}function a(p){var o=p.cmdState;
for(var n=o.length-1;
n>=0;
n--){var m=o[n];
if(m.name=="DEFAULT"){continue
}return m.styleIdentifier()
}return null
}function b(o,n,p,m){return function(){this.name=o;
this.bracketNo=0;
this.style=n;
this.styles=m;
this.brackets=p;
this.styleIdentifier=function(q){if(this.bracketNo<=this.styles.length){return this.styles[this.bracketNo-1]
}else{return null
}};
this.openBracket=function(q){this.bracketNo++;
return"bracket"
};
this.closeBracket=function(q){}
}
}var c=new Array();
c.importmodule=b("importmodule","tag","{[",["string","builtin"]);
c.documentclass=b("documentclass","tag","{[",["","atom"]);
c.usepackage=b("documentclass","tag","[",["atom"]);
c.begin=b("documentclass","tag","[",["atom"]);
c.end=b("documentclass","tag","[",["atom"]);
c.DEFAULT=function(){this.name="DEFAULT";
this.style="tag";
this.styleIdentifier=function(m){};
this.openBracket=function(m){};
this.closeBracket=function(m){}
};
function d(n,m){n.f=m
}function f(q,p){if(q.match(/^\\[a-zA-Z@]+/)){var n=q.current();
n=n.substr(1,n.length-1);
var m;
if(c.hasOwnProperty(n)){m=c[n]
}else{m=c.DEFAULT
}m=new m();
j(p,m);
d(p,g);
return m.style
}if(q.match(/^\\[$&%#{}_]/)){return"tag"
}if(q.match(/^\\[,;!\/]/)){return"tag"
}var o=q.next();
if(o=="%"){if(!q.eol()){d(p,k)
}return"comment"
}else{if(o=="}"||o=="]"){m=i(p);
if(m){m.closeBracket(o);
d(p,g)
}else{return"error"
}return"bracket"
}else{if(o=="{"||o=="["){m=c.DEFAULT;
m=new m();
j(p,m);
return"bracket"
}else{if(/\d/.test(o)){q.eatWhile(/[\w.%]/);
return"atom"
}else{q.eatWhile(/[\w-_]/);
return a(p)
}}}}}function k(n,m){n.skipToEnd();
d(m,f);
return"comment"
}function g(q,p){var o=q.peek();
if(o=="{"||o=="["){var m=i(p);
var n=m.openBracket(o);
q.eat(o);
d(p,f);
return"bracket"
}if(/[ \t\r]/.test(o)){q.eat(o);
return null
}d(p,f);
m=i(p);
if(m){l(p)
}return f(q,p)
}return{startState:function(){return{f:f,cmdState:[]}
},copyState:function(m){return{f:m.f,cmdState:m.cmdState.slice(0,m.cmdState.length)}
},token:function(p,o){var n=o.f(p,o);
var m=p.current();
return n
}}
});
CodeMirror.defineMIME("text/x-stex","stex");
CodeMirror.defineMIME("text/x-latex","stex");