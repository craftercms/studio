CodeMirror.defineMode("xquery",function(w,e){var h=function(){function P(A){return{type:A,style:"keyword"}
}var I=P("keyword a"),G=P("keyword b"),D=P("keyword c"),H=P("operator"),O={type:"atom",style:"atom"},z={type:"punctuation",style:""},Q={type:"axis_specifier",style:"qualifier"};
var F={"if":I,"switch":I,"while":I,"for":I,"else":G,then:G,"try":G,"finally":G,"catch":G,element:D,attribute:D,let:D,"implements":D,"import":D,module:D,namespace:D,"return":D,"super":D,"this":D,"throws":D,where:D,"private":D,",":z,"null":O,"fn:false()":O,"fn:true()":O};
var K=["after","ancestor","ancestor-or-self","and","as","ascending","assert","attribute","before","by","case","cast","child","comment","declare","default","define","descendant","descendant-or-self","descending","document","document-node","element","else","eq","every","except","external","following","following-sibling","follows","for","function","if","import","in","instance","intersect","item","let","module","namespace","node","node","of","only","or","order","parent","precedes","preceding","preceding-sibling","processing-instruction","ref","return","returns","satisfies","schema","schema-element","self","some","sortby","stable","text","then","to","treat","typeswitch","union","variable","version","where","xquery","empty-sequence"];
for(var M=0,J=K.length;
M<J;
M++){F[K[M]]=P(K[M])
}var N=["xs:string","xs:float","xs:decimal","xs:double","xs:integer","xs:boolean","xs:date","xs:dateTime","xs:time","xs:duration","xs:dayTimeDuration","xs:time","xs:yearMonthDuration","numeric","xs:hexBinary","xs:base64Binary","xs:anyURI","xs:QName","xs:byte","xs:boolean","xs:anyURI","xf:yearMonthDuration"];
for(var M=0,J=N.length;
M<J;
M++){F[N[M]]=O
}var L=["eq","ne","lt","le","gt","ge",":=","=",">",">=","<","<=",".","|","?","and","or","div","idiv","mod","*","/","+","-"];
for(var M=0,J=L.length;
M<J;
M++){F[L[M]]=H
}var E=["self::","attribute::","child::","descendant::","descendant-or-self::","parent::","ancestor::","ancestor-or-self::","following::","preceding::","following-sibling::","preceding-sibling::"];
for(var M=0,J=E.length;
M<J;
M++){F[E[M]]=Q
}return F
}();
var d,o;
function x(B,A,z){d=B;
o=z;
return A
}function m(B,A,z){A.tokenize=z;
return z(B,A)
}function y(G,B){var z=G.next(),I=false,J=p(G);
if(z=="<"){if(G.match("!--",true)){return m(G,B,a)
}if(G.match("![CDATA",false)){B.tokenize=g;
return x("tag","tag")
}if(G.match("?",false)){return m(G,B,c)
}var C=G.eat("/");
G.eatSpace();
var D="",F;
while((F=G.eat(/[^\s\u00a0=<>\"\'\/?]/))){D+=F
}return m(G,B,s(D,C))
}else{if(z=="{"){r(B,{type:"codeblock"});
return x("","")
}else{if(z=="}"){i(B);
return x("","")
}else{if(b(B)){if(z==">"){return x("tag","tag")
}else{if(z=="/"&&G.eat(">")){i(B);
return x("tag","tag")
}else{return x("word","variable")
}}}else{if(/\d/.test(z)){G.match(/^\d*(?:\.\d*)?(?:E[+\-]?\d+)?/);
return x("number","atom")
}else{if(z==="("&&G.eat(":")){r(B,{type:"comment"});
return m(G,B,l)
}else{if(!J&&(z==='"'||z==="'")){return m(G,B,f(z))
}else{if(z==="$"){return m(G,B,n)
}else{if(z===":"&&G.eat("=")){return x("operator","keyword")
}else{if(z==="("){r(B,{type:"paren"});
return x("","")
}else{if(z===")"){i(B);
return x("","")
}else{if(z==="["){r(B,{type:"bracket"});
return x("","")
}else{if(z==="]"){i(B);
return x("","")
}else{var H=h.propertyIsEnumerable(z)&&h[z];
if(J&&z==='"'){while(G.next()!=='"'){}}if(J&&z==="'"){while(G.next()!=="'"){}}if(!H){G.eatWhile(/[\w\$_-]/)
}var E=G.eat(":");
if(!G.eat(":")&&E){G.eatWhile(/[\w\$_-]/)
}if(G.match(/^[ \t]*\(/,false)){I=true
}var A=G.current();
H=h.propertyIsEnumerable(A)&&h[A];
if(I&&!H){H={type:"function_call",style:"variable def"}
}if(v(B)){i(B);
return x("word","variable",A)
}if(A=="element"||A=="attribute"||H.type=="axis_specifier"){r(B,{type:"xmlconstructor"})
}return H?x(H.type,H.style,A):x("word","variable",A)
}}}}}}}}}}}}}}function l(E,C){var A=false,z=false,D=0,B;
while(B=E.next()){if(B==")"&&A){if(D>0){D--
}else{i(C);
break
}}else{if(B==":"&&z){D++
}}A=(B==":");
z=(B=="(")
}return x("comment","comment")
}function f(z,A){return function(D,C){var B;
if(k(C)&&D.current()==z){i(C);
if(A){C.tokenize=A
}return x("string","string")
}r(C,{type:"string",name:z,tokenize:f(z,A)});
if(D.match("{",false)&&j(C)){C.tokenize=y;
return x("string","string")
}while(B=D.next()){if(B==z){i(C);
if(A){C.tokenize=A
}break
}else{if(D.match("{",false)&&j(C)){C.tokenize=y;
return x("string","string")
}}}return x("string","string")
}
}function n(B,z){var A=/[\w\$_-]/;
if(B.eat('"')){while(B.next()!=='"'){}B.eat(":")
}else{B.eatWhile(A);
if(!B.match(":=",false)){B.eat(":")
}}B.eatWhile(A);
z.tokenize=y;
return x("variable","variable")
}function s(z,A){return function(C,B){C.eatSpace();
if(A&&C.eat(">")){i(B);
B.tokenize=y;
return x("tag","tag")
}if(!C.eat("/")){r(B,{type:"tag",name:z,tokenize:y})
}if(!C.eat(">")){B.tokenize=u;
return x("tag","tag")
}else{B.tokenize=y
}return x("tag","tag")
}
}function u(B,A){var z=B.next();
if(z=="/"&&B.eat(">")){if(j(A)){i(A)
}if(b(A)){i(A)
}return x("tag","tag")
}if(z==">"){if(j(A)){i(A)
}return x("tag","tag")
}if(z=="="){return x("","")
}if(z=='"'||z=="'"){return m(B,A,f(z,u))
}if(!j(A)){r(A,{type:"attribute",name:name,tokenize:u})
}B.eat(/[a-zA-Z_:]/);
B.eatWhile(/[-a-zA-Z0-9_:.]/);
B.eatSpace();
if(B.match(">",false)||B.match("/",false)){i(A);
A.tokenize=y
}return x("attribute","attribute")
}function a(B,A){var z;
while(z=B.next()){if(z=="-"&&B.match("->",true)){A.tokenize=y;
return x("comment","comment")
}}}function g(B,A){var z;
while(z=B.next()){if(z=="]"&&B.match("]",true)){A.tokenize=y;
return x("comment","comment")
}}}function c(B,A){var z;
while(z=B.next()){if(z=="?"&&B.match(">",true)){A.tokenize=y;
return x("comment","comment meta")
}}}function b(z){return q(z,"tag")
}function j(z){return q(z,"attribute")
}function t(z){return q(z,"codeblock")
}function v(z){return q(z,"xmlconstructor")
}function k(z){return q(z,"string")
}function p(z){if(z.current()==='"'){return z.match(/^[^\"]+\"\:/,false)
}else{if(z.current()==="'"){return z.match(/^[^\"]+\'\:/,false)
}else{return false
}}}function q(A,z){return(A.stack.length&&A.stack[A.stack.length-1].type==z)
}function r(z,A){z.stack.push(A)
}function i(A){var z=A.stack.pop();
var B=A.stack.length&&A.stack[A.stack.length-1].tokenize;
A.tokenize=B||y
}return{startState:function(z){return{tokenize:y,cc:[],stack:[]}
},token:function(B,A){if(B.eatSpace()){return null
}var z=A.tokenize(B,A);
return z
}}
});
CodeMirror.defineMIME("application/xquery","xquery");