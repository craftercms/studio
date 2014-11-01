CodeMirror.defineMode("haxe",function(J,N){var w=J.indentUnit;
var d=function(){function af(ai){return{type:ai,style:"keyword"}
}var aa=af("keyword a"),ah=af("keyword b"),ag=af("keyword c");
var ab=af("operator"),ae={type:"atom",style:"atom"},ad={type:"attribute",style:"attribute"};
var ac=af("typedef");
return{"if":aa,"while":aa,"else":ah,"do":ah,"try":ah,"return":ag,"break":ag,"continue":ag,"new":ag,"throw":ag,"var":af("var"),inline:ad,"static":ad,using:af("import"),"public":ad,"private":ad,cast:af("cast"),"import":af("import"),macro:af("macro"),"function":af("function"),"catch":af("catch"),untyped:af("untyped"),callback:af("cb"),"for":af("for"),"switch":af("switch"),"case":af("case"),"default":af("default"),"in":ab,never:af("property_access"),trace:af("trace"),"class":ac,"enum":ac,"interface":ac,typedef:ac,"extends":ac,"implements":ac,dynamic:ac,"true":ae,"false":ae,"null":ae}
}();
var O=/[+\-*&%=<>!?|]/;
function Y(ac,ab,aa){ab.tokenize=aa;
return aa(ac,ab)
}function g(ad,aa){var ac=false,ab;
while((ab=ad.next())!=null){if(ab==aa&&!ac){return false
}ac=!ac&&ab=="\\"
}return ac
}var Z,o;
function D(ac,ab,aa){Z=ac;
o=aa;
return ab
}function A(ae,ac){var aa=ae.next();
if(aa=='"'||aa=="'"){return Y(ae,ac,i(aa))
}else{if(/[\[\]{}\(\),;\:\.]/.test(aa)){return D(aa)
}else{if(aa=="0"&&ae.eat(/x/i)){ae.eatWhile(/[\da-f]/i);
return D("number","number")
}else{if(/\d/.test(aa)||aa=="-"&&ae.eat(/\d/)){ae.match(/^\d*(?:\.\d*)?(?:[eE][+\-]?\d+)?/);
return D("number","number")
}else{if(ac.reAllowed&&(aa=="~"&&ae.eat(/\//))){g(ae,"/");
ae.eatWhile(/[gimsu]/);
return D("regexp","string-2")
}else{if(aa=="/"){if(ae.eat("*")){return Y(ae,ac,L)
}else{if(ae.eat("/")){ae.skipToEnd();
return D("comment","comment")
}else{ae.eatWhile(O);
return D("operator",null,ae.current())
}}}else{if(aa=="#"){ae.skipToEnd();
return D("conditional","meta")
}else{if(aa=="@"){ae.eat(/:/);
ae.eatWhile(/[\w_]/);
return D("metadata","meta")
}else{if(O.test(aa)){ae.eatWhile(O);
return D("operator",null,ae.current())
}else{var ad;
if(/[A-Z]/.test(aa)){ae.eatWhile(/[\w_<>]/);
ad=ae.current();
return D("type","variable-3",ad)
}else{ae.eatWhile(/[\w_]/);
var ad=ae.current(),ab=d.propertyIsEnumerable(ad)&&d[ad];
return(ab&&ac.kwAllowed)?D(ab.type,ab.style,ad):D("variable","variable",ad)
}}}}}}}}}}}function i(aa){return function(ac,ab){if(!g(ac,aa)){ab.tokenize=A
}return D("string","string")
}
}function L(ad,ac){var aa=false,ab;
while(ab=ad.next()){if(ab=="/"&&aa){ac.tokenize=A;
break
}aa=(ab=="*")
}return D("comment","comment")
}var k={atom:true,number:true,variable:true,string:true,regexp:true};
function X(af,ab,aa,ae,ac,ad){this.indented=af;
this.column=ab;
this.type=aa;
this.prev=ac;
this.info=ad;
if(ae!=null){this.align=ae
}}function x(ac,ab){for(var aa=ac.localVars;
aa;
aa=aa.next){if(aa.name==ab){return true
}}}function u(ae,ab,aa,ad,af){var ag=ae.cc;
v.state=ae;
v.stream=af;
v.marked=null,v.cc=ag;
if(!ae.lexical.hasOwnProperty("align")){ae.lexical.align=true
}while(true){var ac=ag.length?ag.pop():z;
if(ac(aa,ad)){while(ag.length&&ag[ag.length-1].lex){ag.pop()()
}if(v.marked){return v.marked
}if(aa=="variable"&&x(ae,ad)){return"variable-2"
}if(aa=="variable"&&q(ae,ad)){return"variable-3"
}return ab
}}}function q(ad,ac){if(/[a-z]/.test(ac.charAt(0))){return false
}var aa=ad.importedtypes.length;
for(var ab=0;
ab<aa;
ab++){if(ad.importedtypes[ab]==ac){return true
}}}function R(aa){var ac=v.state;
for(var ab=ac.importedtypes;
ab;
ab=ab.next){if(ab.name==aa){return
}}ac.importedtypes={name:aa,next:ac.importedtypes}
}var v={state:null,column:null,marked:null,cc:null};
function a(){for(var aa=arguments.length-1;
aa>=0;
aa--){v.cc.push(arguments[aa])
}}function H(){a.apply(null,arguments);
return true
}function l(ab){var ac=v.state;
if(ac.context){v.marked="def";
for(var aa=ac.localVars;
aa;
aa=aa.next){if(aa.name==ab){return
}}ac.localVars={name:ab,next:ac.localVars}
}}var F={name:"this",next:null};
function s(){if(!v.state.context){v.state.localVars=F
}v.state.context={prev:v.state.context,vars:v.state.localVars}
}function r(){v.state.localVars=v.state.context.vars;
v.state.context=v.state.context.prev
}function j(ab,ac){var aa=function(){var ad=v.state;
ad.lexical=new X(ad.indented,v.stream.column(),ab,null,ad.lexical,ac)
};
aa.lex=true;
return aa
}function G(){var aa=v.state;
if(aa.lexical.prev){if(aa.lexical.type==")"){aa.indented=aa.lexical.indented
}aa.lexical=aa.lexical.prev
}}G.lex=true;
function e(ab){return function aa(ac){if(ac==ab){return H()
}else{if(ab==";"){return a()
}else{return H(arguments.callee)
}}}
}function z(aa){if(aa=="@"){return H(S)
}if(aa=="var"){return H(j("vardef"),K,e(";"),G)
}if(aa=="keyword a"){return H(j("form"),y,z,G)
}if(aa=="keyword b"){return H(j("form"),z,G)
}if(aa=="{"){return H(j("}"),s,m,G,r)
}if(aa==";"){return H()
}if(aa=="attribute"){return H(c)
}if(aa=="function"){return H(h)
}if(aa=="for"){return H(j("form"),e("("),j(")"),f,e(")"),G,z,G)
}if(aa=="variable"){return H(j("stat"),C)
}if(aa=="switch"){return H(j("form"),y,j("}","switch"),e("{"),m,G,G)
}if(aa=="case"){return H(y,e(":"))
}if(aa=="default"){return H(e(":"))
}if(aa=="catch"){return H(j("form"),s,e("("),p,e(")"),z,G,r)
}if(aa=="import"){return H(E,e(";"))
}if(aa=="typedef"){return H(W)
}return a(j("stat"),y,e(";"),G)
}function y(aa){if(k.hasOwnProperty(aa)){return H(M)
}if(aa=="function"){return H(h)
}if(aa=="keyword c"){return H(B)
}if(aa=="("){return H(j(")"),B,e(")"),G,M)
}if(aa=="operator"){return H(y)
}if(aa=="["){return H(j("]"),P(y,"]"),G,M)
}if(aa=="{"){return H(j("}"),P(n,"}"),G,M)
}return H()
}function B(aa){if(aa.match(/[;\}\)\],]/)){return a()
}return a(y)
}function M(aa,ab){if(aa=="operator"&&/\+\+|--/.test(ab)){return H(M)
}if(aa=="operator"||aa==":"){return H(y)
}if(aa==";"){return
}if(aa=="("){return H(j(")"),P(y,")"),G,M)
}if(aa=="."){return H(Q,M)
}if(aa=="["){return H(j("]"),y,e("]"),G,M)
}}function c(aa,ab){if(aa=="attribute"){return H(c)
}if(aa=="function"){return H(h)
}if(aa=="var"){return H(K)
}}function S(aa,ab){if(aa==":"){return H(S)
}if(aa=="variable"){return H(S)
}if(aa=="("){return H(j(")"),comasep(T,")"),G,z)
}}function T(aa,ab){if(typ=="variable"){return H()
}}function E(aa,ab){if(aa=="variable"&&/[A-Z]/.test(ab.charAt(0))){R(ab);
return H()
}else{if(aa=="variable"||aa=="property"||aa=="."){return H(E)
}}}function W(aa,ab){if(aa=="variable"&&/[A-Z]/.test(ab.charAt(0))){R(ab);
return H()
}}function C(aa){if(aa==":"){return H(G,z)
}return a(M,e(";"),G)
}function Q(aa){if(aa=="variable"){v.marked="property";
return H()
}}function n(aa){if(aa=="variable"){v.marked="property"
}if(k.hasOwnProperty(aa)){return H(e(":"),y)
}}function P(ac,aa){function ab(ae){if(ae==","){return H(ac,ab)
}if(ae==aa){return H()
}return H(e(aa))
}return function ad(ae){if(ae==aa){return H()
}else{return a(ac,ab)
}}
}function m(aa){if(aa=="}"){return H()
}return a(z,m)
}function K(aa,ab){if(aa=="variable"){l(ab);
return H(t,I)
}return H()
}function I(aa,ab){if(ab=="="){return H(y,I)
}if(aa==","){return H(K)
}}function f(aa,ab){if(aa=="variable"){l(ab)
}return H(j(")"),s,b,y,G,z,r)
}function b(aa,ab){if(ab=="in"){return H()
}}function h(aa,ab){if(aa=="variable"){l(ab);
return H(h)
}if(ab=="new"){return H(h)
}if(aa=="("){return H(j(")"),s,P(p,")"),G,t,z,r)
}}function t(aa,ab){if(aa==":"){return H(V)
}}function V(aa,ab){if(aa=="type"){return H()
}if(aa=="variable"){return H()
}if(aa=="{"){return H(j("}"),P(U,"}"),G)
}}function U(aa,ab){if(aa=="variable"){return H(t)
}}function p(aa,ab){if(aa=="variable"){l(ab);
return H(t)
}}return{startState:function(ab){var aa=["Int","Float","String","Void","Std","Bool","Dynamic","Array"];
return{tokenize:A,reAllowed:true,kwAllowed:true,cc:[],lexical:new X((ab||0)-w,0,"block",false),localVars:N.localVars,importedtypes:aa,context:N.localVars&&{vars:N.localVars},indented:0}
},token:function(ac,ab){if(ac.sol()){if(!ab.lexical.hasOwnProperty("align")){ab.lexical.align=false
}ab.indented=ac.indentation()
}if(ac.eatSpace()){return null
}var aa=ab.tokenize(ac,ab);
if(Z=="comment"){return aa
}ab.reAllowed=!!(Z=="operator"||Z=="keyword c"||Z.match(/^[\[{}\(,;:]$/));
ab.kwAllowed=Z!=".";
return u(ab,aa,Z,o,ac)
},indent:function(af,aa){if(af.tokenize!=A){return 0
}var ae=aa&&aa.charAt(0),ac=af.lexical;
if(ac.type=="stat"&&ae=="}"){ac=ac.prev
}var ad=ac.type,ab=ae==ad;
if(ad=="vardef"){return ac.indented+4
}else{if(ad=="form"&&ae=="{"){return ac.indented
}else{if(ad=="stat"||ad=="form"){return ac.indented+w
}else{if(ac.info=="switch"&&!ab){return ac.indented+(/^(?:case|default)\b/.test(aa)?w:2*w)
}else{if(ac.align){return ac.column+(ab?0:1)
}else{return ac.indented+(ab?0:w)
}}}}}},electricChars:"{}"}
});
CodeMirror.defineMIME("text/x-haxe","haxe");