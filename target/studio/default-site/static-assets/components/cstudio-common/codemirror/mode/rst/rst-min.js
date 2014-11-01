CodeMirror.defineMode("rst",function(C,f){function t(G,F,E){G.fn=F;
v(G,E)
}function v(F,E){F.ctx=E||{}
}function u(F,E){if(E&&(typeof E!=="string")){var G=E.current();
E=G[G.length-1]
}t(F,D,{back:E})
}function k(G){if(G){var F=CodeMirror.listModes();
for(var E in F){if(F[E]==G){return true
}}}return false
}function c(E){if(k(E)){return CodeMirror.getMode(C,E)
}else{return null
}}var y=c(f.verbatim);
var q=c("python");
var s=/^[!"#$%&'()*+,-./:;<=>?@[\\\]^_`{|}~]/;
var h=/^\s*\w([-:.\w]*\w)?::(\s|$)/;
var a=/^\s*_[\w-]+:(\s|$)/;
var i=/^\s*\[(\d+|#)\](\s|$)/;
var A=/^\s*\[[A-Za-z][\w-]*\](\s|$)/;
var z=/^\[(\d+|#)\]_/;
var j=/^\[[A-Za-z][\w-]*\]_/;
var w=/^\.\.(\s|$)/;
var p=/^::\s*$/;
var d=/^[-\s"([{</:]/;
var g=/^[-\s`'")\]}>/:.,;!?\\_]/;
var r=/^\s*((\d+|[A-Za-z#])[.)]|\((\d+|[A-Z-a-z#])\))\s/;
var x=/^\s*[-\+\*]\s/;
var m=/^\s+(>>>|In \[\d+\]:)\s/;
function D(P,F){var E,N,J;
if(P.eat(/\\/)){E=P.next();
u(F,E);
return null
}N=P.sol();
if(N&&(E=P.eat(s))){for(J=0;
P.eat(E);
J++){}if(J>=3&&P.match(/^\s*$/)){u(F,null);
return"header"
}else{P.backUp(J+1)
}}if(N&&P.match(w)){if(!P.eol()){t(F,o)
}return"meta"
}if(P.match(p)){if(!y){t(F,B)
}else{var K=y;
t(F,B,{mode:K,local:K.startState()})
}return"meta"
}if(N&&P.match(m,false)){if(!q){t(F,B);
return"meta"
}else{var K=q;
t(F,B,{mode:K,local:K.startState()});
return null
}}function M(Q){return N||!F.ctx.back||Q.test(F.ctx.back)
}function G(Q){return P.eol()||P.match(Q,false)
}function I(Q){return P.match(Q)&&M(/\W/)&&G(/\W/)
}if(I(z)){u(F,P);
return"footnote"
}if(I(j)){u(F,P);
return"citation"
}E=P.next();
if(M(d)){if((E===":"||E==="|")&&P.eat(/\S/)){var H;
if(E===":"){H="builtin"
}else{H="atom"
}t(F,e,{ch:E,wide:false,prev:null,token:H});
return H
}if(E==="*"||E==="`"){var O=E,L=false;
E=P.next();
if(E==O){L=true;
E=P.next()
}if(E&&!/\s/.test(E)){var H;
if(O==="*"){H=L?"strong":"em"
}else{H=L?"string":"string-2"
}t(F,e,{ch:O,wide:L,prev:null,token:H});
return H
}}}u(F,E);
return null
}function e(I,H){var G=I.next(),F=H.ctx.token;
function E(J){H.ctx.prev=J;
return F
}if(G!=H.ctx.ch){return E(G)
}if(/\s/.test(H.ctx.prev)){return E(G)
}if(H.ctx.wide){G=I.next();
if(G!=H.ctx.ch){return E(G)
}}if(!I.eol()&&!g.test(I.peek())){if(H.ctx.wide){I.backUp(1)
}return E(G)
}t(H,D);
u(H,G);
return F
}function o(G,F){var E=null;
if(G.match(h)){E="attribute"
}else{if(G.match(a)){E="link"
}else{if(G.match(i)){E="quote"
}else{if(G.match(A)){E="quote"
}else{G.eatSpace();
if(G.eol()){u(F,G);
return null
}else{G.skipToEnd();
t(F,b);
return"comment"
}}}}}t(F,n,{start:true});
return E
}function n(G,F){var E="body";
if(!F.ctx.start||G.sol()){return l(G,F,E)
}G.skipToEnd();
v(F);
return E
}function b(F,E){return l(F,E,"comment")
}function B(F,E){if(!y){return l(F,E,"meta")
}else{if(F.sol()){if(!F.eatSpace()){u(E,F)
}return null
}return y.token(F,E.ctx.local)
}}function l(G,F,E){if(G.eol()||G.eatSpace()){G.skipToEnd();
return E
}else{u(F,G);
return null
}}return{startState:function(){return{fn:D,ctx:{}}
},copyState:function(E){return{fn:E.fn,ctx:E.ctx}
},token:function(G,F){var E=F.fn(G,F);
return E
}}
},"python");
CodeMirror.defineMIME("text/x-rst","rst");