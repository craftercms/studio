CodeMirror.defineMode("javascript",function(I,M){var v=I.indentUnit;
var Q=M.json;
var b=function(){function W(Z){return{type:Z,style:"keyword"}
}var T=W("keyword a"),Y=W("keyword b"),X=W("keyword c");
var U=W("operator"),V={type:"atom",style:"atom"};
return{"if":T,"while":T,"with":T,"else":Y,"do":Y,"try":Y,"finally":Y,"return":X,"break":X,"continue":X,"new":X,"delete":X,"throw":X,"var":W("var"),"const":W("var"),let:W("var"),"function":W("function"),"catch":W("catch"),"for":W("for"),"switch":W("switch"),"case":W("case"),"default":W("default"),"in":U,"typeof":U,"instanceof":U,"true":V,"false":V,"null":V,"undefined":V,"NaN":V,"Infinity":V}
}();
var N=/[+\-*&%=<>!?|]/;
function R(V,U,T){U.tokenize=T;
return T(V,U)
}function h(W,T){var V=false,U;
while((U=W.next())!=null){if(U==T&&!V){return false
}V=!V&&U=="\\"
}return V
}var S,p;
function B(V,U,T){S=V;
p=T;
return U
}function l(X,V){var T=X.next();
if(T=='"'||T=="'"){return R(X,V,z(T))
}else{if(/[\[\]{}\(\),;\:\.]/.test(T)){return B(T)
}else{if(T=="0"&&X.eat(/x/i)){X.eatWhile(/[\da-f]/i);
return B("number","number")
}else{if(/\d/.test(T)||T=="-"&&X.eat(/\d/)){X.match(/^\d*(?:\.\d*)?(?:[eE][+\-]?\d+)?/);
return B("number","number")
}else{if(T=="/"){if(X.eat("*")){return R(X,V,f)
}else{if(X.eat("/")){X.skipToEnd();
return B("comment","comment")
}else{if(V.reAllowed){h(X,"/");
X.eatWhile(/[gimy]/);
return B("regexp","string-2")
}else{X.eatWhile(N);
return B("operator",null,X.current())
}}}}else{if(T=="#"){X.skipToEnd();
return B("error","error")
}else{if(N.test(T)){X.eatWhile(N);
return B("operator",null,X.current())
}else{X.eatWhile(/[\w\$_]/);
var W=X.current(),U=b.propertyIsEnumerable(W)&&b[W];
return(U&&V.kwAllowed)?B(U.type,U.style,W):B("variable","variable",W)
}}}}}}}}function z(T){return function(V,U){if(!h(V,T)){U.tokenize=l
}return B("string","string")
}
}function f(W,V){var T=false,U;
while(U=W.next()){if(U=="/"&&T){V.tokenize=l;
break
}T=(U=="*")
}return B("comment","comment")
}var k={atom:true,number:true,variable:true,string:true,regexp:true};
function t(Y,U,T,X,V,W){this.indented=Y;
this.column=U;
this.type=T;
this.prev=V;
this.info=W;
if(X!=null){this.align=X
}}function w(V,U){for(var T=V.localVars;
T;
T=T.next){if(T.name==U){return true
}}}function E(X,U,T,W,Y){var Z=X.cc;
u.state=X;
u.stream=Y;
u.marked=null,u.cc=Z;
if(!X.lexical.hasOwnProperty("align")){X.lexical.align=true
}while(true){var V=Z.length?Z.pop():Q?x:y;
if(V(T,W)){while(Z.length&&Z[Z.length-1].lex){Z.pop()()
}if(u.marked){return u.marked
}if(T=="variable"&&w(X,W)){return"variable-2"
}return U
}}}var u={state:null,column:null,marked:null,cc:null};
function a(){for(var T=arguments.length-1;
T>=0;
T--){u.cc.push(arguments[T])
}}function G(){a.apply(null,arguments);
return true
}function m(U){var V=u.state;
if(V.context){u.marked="def";
for(var T=V.localVars;
T;
T=T.next){if(T.name==U){return
}}V.localVars={name:U,next:V.localVars}
}}var D={name:"this",next:{name:"arguments"}};
function s(){u.state.context={prev:u.state.context,vars:u.state.localVars};
u.state.localVars=D
}function r(){u.state.localVars=u.state.context.vars;
u.state.context=u.state.context.prev
}function j(U,V){var T=function(){var W=u.state;
W.lexical=new t(W.indented,u.stream.column(),U,null,W.lexical,V)
};
T.lex=true;
return T
}function F(){var T=u.state;
if(T.lexical.prev){if(T.lexical.type==")"){T.indented=T.lexical.indented
}T.lexical=T.lexical.prev
}}F.lex=true;
function c(U){return function T(V){if(V==U){return G()
}else{if(U==";"){return a()
}else{return G(arguments.callee)
}}}
}function y(T){if(T=="var"){return G(j("vardef"),J,c(";"),F)
}if(T=="keyword a"){return G(j("form"),x,y,F)
}if(T=="keyword b"){return G(j("form"),y,F)
}if(T=="{"){return G(j("}"),n,F)
}if(T==";"){return G()
}if(T=="function"){return G(i)
}if(T=="for"){return G(j("form"),c("("),j(")"),g,c(")"),F,y,F)
}if(T=="variable"){return G(j("stat"),C)
}if(T=="switch"){return G(j("form"),x,j("}","switch"),c("{"),n,F,F)
}if(T=="case"){return G(x,c(":"))
}if(T=="default"){return G(c(":"))
}if(T=="catch"){return G(j("form"),s,c("("),q,c(")"),y,F,r)
}return a(j("stat"),x,c(";"),F)
}function x(T){if(k.hasOwnProperty(T)){return G(L)
}if(T=="function"){return G(i)
}if(T=="keyword c"){return G(A)
}if(T=="("){return G(j(")"),A,c(")"),F,L)
}if(T=="operator"){return G(x)
}if(T=="["){return G(j("]"),O(x,"]"),F,L)
}if(T=="{"){return G(j("}"),O(o,"}"),F,L)
}return G()
}function A(T){if(T.match(/[;\}\)\],]/)){return a()
}return a(x)
}function L(T,U){if(T=="operator"&&/\+\+|--/.test(U)){return G(L)
}if(T=="operator"&&U=="?"){return G(x,c(":"),x)
}if(T==";"){return
}if(T=="("){return G(j(")"),O(x,")"),F,L)
}if(T=="."){return G(P,L)
}if(T=="["){return G(j("]"),x,c("]"),F,L)
}}function C(T){if(T==":"){return G(F,y)
}return a(L,c(";"),F)
}function P(T){if(T=="variable"){u.marked="property";
return G()
}}function o(T){if(T=="variable"){u.marked="property"
}if(k.hasOwnProperty(T)){return G(c(":"),x)
}}function O(V,T){function U(X){if(X==","){return G(V,U)
}if(X==T){return G()
}return G(c(T))
}return function W(X){if(X==T){return G()
}else{return a(V,U)
}}
}function n(T){if(T=="}"){return G()
}return a(y,n)
}function J(T,U){if(T=="variable"){m(U);
return G(H)
}return G()
}function H(T,U){if(U=="="){return G(x,H)
}if(T==","){return G(J)
}}function g(T){if(T=="var"){return G(J,e)
}if(T==";"){return a(e)
}if(T=="variable"){return G(K)
}return a(e)
}function K(T,U){if(U=="in"){return G(x)
}return G(L,e)
}function e(T,U){if(T==";"){return G(d)
}if(U=="in"){return G(x)
}return G(x,c(";"),d)
}function d(T){if(T!=")"){G(x)
}}function i(T,U){if(T=="variable"){m(U);
return G(i)
}if(T=="("){return G(j(")"),s,O(q,")"),F,y,r)
}}function q(T,U){if(T=="variable"){m(U);
return G()
}}return{startState:function(T){return{tokenize:l,reAllowed:true,kwAllowed:true,cc:[],lexical:new t((T||0)-v,0,"block",false),localVars:M.localVars,context:M.localVars&&{vars:M.localVars},indented:0}
},token:function(V,U){if(V.sol()){if(!U.lexical.hasOwnProperty("align")){U.lexical.align=false
}U.indented=V.indentation()
}if(V.eatSpace()){return null
}var T=U.tokenize(V,U);
if(S=="comment"){return T
}U.reAllowed=!!(S=="operator"||S=="keyword c"||S.match(/^[\[{}\(,;:]$/));
U.kwAllowed=S!=".";
return E(U,T,S,p,V)
},indent:function(Y,T){if(Y.tokenize!=l){return 0
}var X=T&&T.charAt(0),V=Y.lexical;
if(V.type=="stat"&&X=="}"){V=V.prev
}var W=V.type,U=X==W;
if(W=="vardef"){return V.indented+4
}else{if(W=="form"&&X=="{"){return V.indented
}else{if(W=="stat"||W=="form"){return V.indented+v
}else{if(V.info=="switch"&&!U){return V.indented+(/^(?:case|default)\b/.test(T)?v:2*v)
}else{if(V.align){return V.column+(U?0:1)
}else{return V.indented+(U?0:v)
}}}}}},electricChars:":{}"}
});
CodeMirror.defineMIME("text/javascript","javascript");
CodeMirror.defineMIME("application/json",{name:"javascript",json:true});