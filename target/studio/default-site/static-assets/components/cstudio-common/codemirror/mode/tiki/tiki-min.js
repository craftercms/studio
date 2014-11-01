CodeMirror.defineMode("tiki",function(v,j){function u(z,y,x){return function(B,A){while(!B.eol()){if(B.match(y)){A.tokenize=n;
break
}B.next()
}if(x){A.tokenize=x
}return z
}
}function m(y,x){return function(A,z){while(!A.eol()){A.next()
}z.tokenize=n;
return y
}
}function n(D,C){function y(F){C.tokenize=F;
return F(D,C)
}var B=D.sol();
var A=D.next();
switch(A){case"{":var z=D.eat("/")?"closeTag":"openTag";
D.eatSpace();
var x="";
var E;
while((E=D.eat(/[^\s\u00a0=\"\'\/?(}]/))){x+=E
}C.tokenize=w;
return"tag";
break;
case"_":if(D.eat("_")){return y(u("strong","__",n))
}break;
case"'":if(D.eat("'")){return y(u("em","''",n))
}break;
case"(":if(D.eat("(")){return y(u("variable-2","))",n))
}break;
case"[":return y(u("variable-3","]",n));
break;
case"|":if(D.eat("|")){return y(u("comment","||"))
}break;
case"-":if(D.eat("=")){return y(u("header string","=-",n))
}else{if(D.eat("-")){return y(u("error tw-deleted","--",n))
}}break;
case"=":if(D.match("==")){return y(u("tw-underline","===",n))
}break;
case":":if(D.eat(":")){return y(u("comment","::"))
}break;
case"^":return y(u("tw-box","^"));
break;
case"~":if(D.match("np~")){return y(u("meta","~/np~"))
}break
}if(B){switch(A){case"!":if(D.match("!!!!!")){return y(m("header string"))
}else{if(D.match("!!!!")){return y(m("header string"))
}else{if(D.match("!!!")){return y(m("header string"))
}else{if(D.match("!!")){return y(m("header string"))
}else{return y(m("header string"))
}}}}break;
case"*":case"#":case"+":return y(m("tw-listitem bracket"));
break
}}return null
}var o=v.indentUnit;
var a,f;
function w(A,z){var y=A.next();
var x=A.peek();
if(y=="}"){z.tokenize=n;
return"tag"
}else{if(y=="("||y==")"){return"bracket"
}else{if(y=="="){f="equals";
if(x==">"){y=A.next();
x=A.peek()
}if(!/[\'\"]/.test(x)){z.tokenize=s()
}return"operator"
}else{if(/[\'\"]/.test(y)){z.tokenize=i(y);
return z.tokenize(A,z)
}else{A.eatWhile(/[^\s\u00a0=\"\'\/?]/);
return"keyword"
}}}}}function i(x){return function(z,y){while(!z.eol()){if(z.next()==x){y.tokenize=w;
break
}}return"string"
}
}function s(){return function(A,z){while(!A.eol()){var y=A.next();
var x=A.peek();
if(y==" "||y==","||/[ )}]/.test(x)){z.tokenize=w;
break
}}return"string"
}
}var k,g;
function b(){for(var x=arguments.length-1;
x>=0;
x--){k.cc.push(arguments[x])
}}function d(){b.apply(null,arguments);
return true
}function h(y,z){var x=k.context&&k.context.noIndent;
k.context={prev:k.context,pluginName:y,indent:k.indented,startOfLine:z,noIndent:x}
}function r(){if(k.context){k.context=k.context.prev
}}function c(x){if(x=="openPlugin"){k.pluginName=a;
return d(l,p(k.startOfLine))
}else{if(x=="closePlugin"){var y=false;
if(k.context){y=k.context.pluginName!=a;
r()
}else{y=true
}if(y){g="error"
}return d(e(y))
}else{if(x=="string"){if(!k.context||k.context.name!="!cdata"){h("!cdata")
}if(k.tokenize==n){r()
}return d()
}else{return d()
}}}}function p(x){return function(y){if(y=="selfclosePlugin"||y=="endPlugin"){return d()
}if(y=="endPlugin"){h(k.pluginName,x);
return d()
}return d()
}
}function e(x){return function(y){if(x){g="error"
}if(y=="endPlugin"){return d()
}return b()
}
}function l(x){if(x=="keyword"){g="attribute";
return d(l)
}if(x=="equals"){return d(t,l)
}return b()
}function t(x){if(x=="keyword"){g="string";
return d()
}if(x=="string"){return d(q)
}return b()
}function q(x){if(x=="string"){return d(q)
}else{return b()
}}return{startState:function(){return{tokenize:n,cc:[],indented:0,startOfLine:true,pluginName:null,context:null}
},token:function(A,z){if(A.sol()){z.startOfLine=true;
z.indented=A.indentation()
}if(A.eatSpace()){return null
}g=f=a=null;
var y=z.tokenize(A,z);
if((y||f)&&y!="comment"){k=z;
while(true){var x=z.cc.pop()||c;
if(x(f||y)){break
}}}z.startOfLine=false;
return g||y
},indent:function(z,x){var y=z.context;
if(y&&y.noIndent){return 0
}if(y&&/^{\//.test(x)){y=y.prev
}while(y&&!y.startOfLine){y=y.prev
}if(y){return y.indent+o
}else{return 0
}},electricChars:"/"}
});
CodeMirror.defineMIME("text/tiki","tiki");