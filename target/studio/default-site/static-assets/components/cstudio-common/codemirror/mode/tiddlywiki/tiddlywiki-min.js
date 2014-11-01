CodeMirror.defineMode("tiddlywiki",function(z,j){var q=z.indentUnit;
var x=function(){function G(H){return{type:H,style:"text"}
}return{}
}();
var k=function(){function G(H){return{type:H,style:"macro"}
}return{allTags:G("allTags"),closeAll:G("closeAll"),list:G("list"),newJournal:G("newJournal"),newTiddler:G("newTiddler"),permaview:G("permaview"),saveChanges:G("saveChanges"),search:G("search"),slider:G("slider"),tabs:G("tabs"),tag:G("tag"),tagging:G("tagging"),tags:G("tags"),tiddler:G("tiddler"),timeline:G("timeline"),today:G("today"),version:G("version"),option:G("option"),"with":G("with"),filter:G("filter")}
}();
var A=/[\w_\-]/i,p=/^\-\-\-\-+$/,h=/^\/\*\*\*$/,a=/^\*\*\*\/$/,u=/^<<<$/,f=/^\/\/\{\{\{$/,w=/^\/\/\}\}\}$/,n=/^<!--\{\{\{-->$/,F=/^<!--\}\}\}-->$/,y=/^\{\{\{$/,l=/^\}\}\}$/,g=/\{\{\{/,d=/.*?\}\}\}/;
function r(I,H,G){H.tokenize=G;
return G(I,H)
}function t(J,G){var I=false,H;
while((H=J.next())!=null){if(H==G&&!I){return false
}I=!I&&H=="\\"
}return I
}var e,s;
function D(I,H,G){e=I;
s=G;
return H
}function o(M,K){var I=M.sol(),H,G;
K.block=false;
H=M.peek();
if(I&&/[<\/\*{}\-]/.test(H)){if(M.match(y)){K.block=true;
return r(M,K,v)
}if(M.match(u)){return D("quote","quote")
}if(M.match(h)||M.match(a)){return D("code","comment")
}if(M.match(f)||M.match(w)||M.match(n)||M.match(F)){return D("code","comment")
}if(M.match(p)){return D("hr","hr")
}}H=M.next();
if(I&&/[\/\*!#;:>|]/.test(H)){if(H=="!"){M.skipToEnd();
return D("header","header")
}if(H=="*"){M.eatWhile("*");
return D("list","comment")
}if(H=="#"){M.eatWhile("#");
return D("list","comment")
}if(H==";"){M.eatWhile(";");
return D("list","comment")
}if(H==":"){M.eatWhile(":");
return D("list","comment")
}if(H==">"){M.eatWhile(">");
return D("quote","quote")
}if(H=="|"){return D("table","header")
}}if(H=="{"&&M.match(/\{\{/)){return r(M,K,v)
}if(/[hf]/i.test(H)){if(/[ti]/i.test(M.peek())&&M.match(/\b(ttps?|tp|ile):\/\/[\-A-Z0-9+&@#\/%?=~_|$!:,.;]*[A-Z0-9+&@#\/%=~_|$]/i)){return D("link","link")
}}if(H=='"'){return D("string","string")
}if(H=="~"){return D("text","brace")
}if(/[\[\]]/.test(H)){if(M.peek()==H){M.next();
return D("brace","brace")
}}if(H=="@"){M.eatWhile(A);
return D("link","link")
}if(/\d/.test(H)){M.eatWhile(/\d/);
return D("number","number")
}if(H=="/"){if(M.eat("%")){return r(M,K,b)
}else{if(M.eat("/")){return r(M,K,B)
}}}if(H=="_"){if(M.eat("_")){return r(M,K,c)
}}if(H=="-"){if(M.eat("-")){if(M.peek()!=" "){return r(M,K,E)
}if(M.peek()==" "){return D("text","brace")
}}}if(H=="'"){if(M.eat("'")){return r(M,K,i)
}}if(H=="<"){if(M.eat("<")){return r(M,K,m)
}}else{return D(H)
}M.eatWhile(/[\w\$_]/);
var L=M.current(),J=x.propertyIsEnumerable(L)&&x[L];
return J?D(J.type,J.style,L):D("text",null,L)
}function C(G){return function(I,H){if(!t(I,G)){H.tokenize=o
}return D("string","string")
}
}function b(J,I){var G=false,H;
while(H=J.next()){if(H=="/"&&G){I.tokenize=o;
break
}G=(H=="%")
}return D("comment","comment")
}function i(J,I){var G=false,H;
while(H=J.next()){if(H=="'"&&G){I.tokenize=o;
break
}G=(H=="'")
}return D("text","strong")
}function v(I,H){var G,J=H.block;
if(J&&I.current()){return D("code","comment")
}if(!J&&I.match(d)){H.tokenize=o;
return D("code","comment")
}if(J&&I.sol()&&I.match(l)){H.tokenize=o;
return D("code","comment")
}G=I.next();
return(J)?D("code","comment"):D("code","comment")
}function B(J,I){var G=false,H;
while(H=J.next()){if(H=="/"&&G){I.tokenize=o;
break
}G=(H=="/")
}return D("text","em")
}function c(J,I){var G=false,H;
while(H=J.next()){if(H=="_"&&G){I.tokenize=o;
break
}G=(H=="_")
}return D("text","underlined")
}function E(K,J){var G=false,H,I;
while(H=K.next()){if(H=="-"&&G){J.tokenize=o;
break
}G=(H=="-")
}return D("text","strikethrough")
}function m(L,J){var H,G,K,I;
if(L.current()=="<<"){return D("brace","macro")
}H=L.next();
if(!H){J.tokenize=o;
return D(H)
}if(H==">"){if(L.peek()==">"){L.next();
J.tokenize=o;
return D("brace","macro")
}}L.eatWhile(/[\w\$_]/);
K=L.current();
I=k.propertyIsEnumerable(K)&&k[K];
if(I){return D(I.type,I.style,K)
}else{return D("macro",null,K)
}}return{startState:function(G){return{tokenize:o,indented:0,level:0}
},token:function(I,H){if(I.eatSpace()){return null
}var G=H.tokenize(I,H);
return G
},electricChars:""}
});
CodeMirror.defineMIME("text/x-tiddlywiki","tiddlywiki");