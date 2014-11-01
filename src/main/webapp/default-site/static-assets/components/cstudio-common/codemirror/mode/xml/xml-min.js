CodeMirror.defineMode("xml",function(y,k){var r=y.indentUnit;
var x=k.htmlMode?{autoSelfClosers:{area:true,base:true,br:true,col:true,command:true,embed:true,frame:true,hr:true,img:true,input:true,keygen:true,link:true,meta:true,param:true,source:true,track:true,wbr:true},implicitlyClosed:{dd:true,li:true,optgroup:true,option:true,p:true,rp:true,rt:true,tbody:true,td:true,tfoot:true,th:true,tr:true},contextGrabbers:{dd:{dd:true,dt:true},dt:{dd:true,dt:true},li:{li:true},option:{option:true,optgroup:true},optgroup:{optgroup:true},p:{address:true,article:true,aside:true,blockquote:true,dir:true,div:true,dl:true,fieldset:true,footer:true,form:true,h1:true,h2:true,h3:true,h4:true,h5:true,h6:true,header:true,hgroup:true,hr:true,menu:true,nav:true,ol:true,p:true,pre:true,section:true,table:true,ul:true},rp:{rp:true,rt:true},rt:{rp:true,rt:true},tbody:{tbody:true,tfoot:true},td:{td:true,th:true},tfoot:{tbody:true},th:{td:true,th:true},thead:{tbody:true,tfoot:true},tr:{tr:true}},doNotIndent:{pre:true},allowUnquoted:true,allowMissing:true}:{autoSelfClosers:{},implicitlyClosed:{},contextGrabbers:{},doNotIndent:{},allowUnquoted:false,allowMissing:false};
var a=k.alignCDATA;
var f,g;
function o(E,D){function B(G){D.tokenize=G;
return G(E,D)
}var C=E.next();
if(C=="<"){if(E.eat("!")){if(E.eat("[")){if(E.match("CDATA[")){return B(w("atom","]]>"))
}else{return null
}}else{if(E.match("--")){return B(w("comment","-->"))
}else{if(E.match("DOCTYPE",true,true)){E.eatWhile(/[\w\._\-]/);
return B(z(1))
}else{return null
}}}}else{if(E.eat("?")){E.eatWhile(/[\w\._\-]/);
D.tokenize=w("meta","?>");
return"meta"
}else{g=E.eat("/")?"closeTag":"openTag";
E.eatSpace();
f="";
var F;
while((F=E.eat(/[^\s\u00a0=<>\"\'\/?]/))){f+=F
}D.tokenize=n;
return"tag"
}}}else{if(C=="&"){var A;
if(E.eat("#")){if(E.eat("x")){A=E.eatWhile(/[a-fA-F\d]/)&&E.eat(";")
}else{A=E.eatWhile(/[\d]/)&&E.eat(";")
}}else{A=E.eatWhile(/[\w\.\-:]/)&&E.eat(";")
}return A?"atom":"error"
}else{E.eatWhile(/[^&<]/);
return null
}}}function n(C,B){var A=C.next();
if(A==">"||(A=="/"&&C.eat(">"))){B.tokenize=o;
g=A==">"?"endTag":"selfcloseTag";
return"tag"
}else{if(A=="="){g="equals";
return null
}else{if(/[\'\"]/.test(A)){B.tokenize=j(A);
return B.tokenize(C,B)
}else{C.eatWhile(/[^\s\u00a0=<>\"\'\/?]/);
return"word"
}}}}function j(A){return function(C,B){while(!C.eol()){if(C.next()==A){B.tokenize=n;
break
}}return"string"
}
}function w(B,A){return function(D,C){while(!D.eol()){if(D.match(A)){C.tokenize=o;
break
}D.next()
}return B
}
}function z(A){return function(D,C){var B;
while((B=D.next())!=null){if(B=="<"){C.tokenize=z(A+1);
return C.tokenize(D,C)
}else{if(B==">"){if(A==1){C.tokenize=o;
break
}else{C.tokenize=z(A-1);
return C.tokenize(D,C)
}}}}return"meta"
}
}var l,h;
function b(){for(var A=arguments.length-1;
A>=0;
A--){l.cc.push(arguments[A])
}}function e(){b.apply(null,arguments);
return true
}function i(A,C){var B=x.doNotIndent.hasOwnProperty(A)||(l.context&&l.context.noIndent);
l.context={prev:l.context,tagName:A,indent:l.indented,startOfLine:C,noIndent:B}
}function u(){if(l.context){l.context=l.context.prev
}}function d(A){if(A=="openTag"){l.tagName=f;
return e(m,c(l.startOfLine))
}else{if(A=="closeTag"){var B=false;
if(l.context){if(l.context.tagName!=f){if(x.implicitlyClosed.hasOwnProperty(l.context.tagName.toLowerCase())){u()
}B=!l.context||l.context.tagName!=f
}}else{B=true
}if(B){h="error"
}return e(s(B))
}}return e()
}function c(A){return function(B){if(B=="selfcloseTag"||(B=="endTag"&&x.autoSelfClosers.hasOwnProperty(l.tagName.toLowerCase()))){q(l.tagName.toLowerCase());
return e()
}if(B=="endTag"){q(l.tagName.toLowerCase());
i(l.tagName,A);
return e()
}return e()
}
}function s(A){return function(B){if(A){h="error"
}if(B=="endTag"){u();
return e()
}h="error";
return e(arguments.callee)
}
}function q(B){var A;
while(true){if(!l.context){return
}A=l.context.tagName.toLowerCase();
if(!x.contextGrabbers.hasOwnProperty(A)||!x.contextGrabbers[A].hasOwnProperty(B)){return
}u()
}}function m(A){if(A=="word"){h="attribute";
return e(p,m)
}if(A=="endTag"||A=="selfcloseTag"){return b()
}h="error";
return e(m)
}function p(A){if(A=="equals"){return e(v,m)
}if(!x.allowMissing){h="error"
}return(A=="endTag"||A=="selfcloseTag")?b():e()
}function v(A){if(A=="string"){return e(t)
}if(A=="word"&&x.allowUnquoted){h="string";
return e()
}h="error";
return(A=="endTag"||A=="selfCloseTag")?b():e()
}function t(A){if(A=="string"){return e(t)
}else{return b()
}}return{startState:function(){return{tokenize:o,cc:[],indented:0,startOfLine:true,tagName:null,context:null}
},token:function(D,C){if(D.sol()){C.startOfLine=true;
C.indented=D.indentation()
}if(D.eatSpace()){return null
}h=g=f=null;
var B=C.tokenize(D,C);
C.type=g;
if((B||g)&&B!="comment"){l=C;
while(true){var A=C.cc.pop()||d;
if(A(g||B)){break
}}}C.startOfLine=false;
return h||B
},indent:function(D,A,C){var B=D.context;
if((D.tokenize!=n&&D.tokenize!=o)||B&&B.noIndent){return C?C.match(/^(\s*)/)[0].length:0
}if(a&&/<!\[CDATA\[/.test(A)){return 0
}if(B&&/^<\//.test(A)){B=B.prev
}while(B&&!B.startOfLine){B=B.prev
}if(B){return B.indent+r
}else{return 0
}},electricChars:"/"}
});
CodeMirror.defineMIME("text/xml","xml");
CodeMirror.defineMIME("application/xml","xml");
if(!CodeMirror.mimeModes.hasOwnProperty("text/html")){CodeMirror.defineMIME("text/html",{name:"xml",htmlMode:true})
};