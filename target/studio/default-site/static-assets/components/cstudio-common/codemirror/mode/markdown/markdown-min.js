CodeMirror.defineMode("markdown",function(s,t){var l=CodeMirror.mimeModes.hasOwnProperty("text/html");
var h=CodeMirror.getMode(s,l?"text/html":"text/plain");
var i=0;
var L=false,H=false;
var g="header",f="comment",n="quote",p="string",F="hr",m="link",j="link",C="link",A="string",I="em",v="strong",B="emstrong";
var a=/^([*\-=_])(?:\s*\1){2,}\s*$/,c=/^[*\-+]\s+/,x=/^[0-9]+\.\s+/,G=/^(?:\={1,}|-{1,})$/,w=/^[^\[*_\\<>` "'(]+/;
function K(O,N,M){N.f=N.inline=M;
return M(O,N)
}function z(O,N,M){N.f=N.block=M;
return M(O,N)
}function b(M){M.linkTitle=false;
M.code=false;
M.em=false;
M.strong=false;
M.quote=false;
if(!l&&M.f==o){M.f=J;
M.block=E
}return null
}function E(O,N){var M;
if(N.list!==false&&N.indentationDiff>=0){if(N.indentationDiff<4){N.indentation-=N.indentationDiff
}N.list=null
}else{N.list=false
}if(N.indentationDiff>=4){N.indentation-=4;
O.skipToEnd();
return f
}else{if(O.eatSpace()){return null
}else{if(O.peek()==="#"||(L&&O.match(G))){N.header=true
}else{if(O.eat(">")){N.indentation++;
N.quote=true
}else{if(O.peek()==="["){return K(O,N,d)
}else{if(O.match(a,true)){return F
}else{if(M=O.match(c,true)||O.match(x,true)){N.indentation+=4;
N.list=true
}}}}}}}return K(O,N,N.inline)
}function o(O,N){var M=h.token(O,N.htmlState);
if(l&&M==="tag"&&N.htmlState.type!=="openTag"&&!N.htmlState.context){N.f=J;
N.block=E
}if(N.md_inside&&O.current().indexOf(">")!=-1){N.f=J;
N.block=E;
N.htmlState.context=undefined
}return M
}function r(N){var M=[];
if(N.strong){M.push(N.em?B:v)
}else{if(N.em){M.push(I)
}}if(N.code){M.push(f)
}if(N.header){M.push(g)
}if(N.quote){M.push(n)
}if(N.list!==false){M.push(p)
}return M.length?M.join(" "):null
}function k(N,M){if(N.match(w,true)){return r(M)
}return undefined
}function J(V,N){var O=N.text(V,N);
if(typeof O!=="undefined"){return O
}if(N.list){N.list=null;
return p
}var M=V.next();
if(M==="\\"){V.next();
return r(N)
}if(N.linkTitle){N.linkTitle=false;
var U=M;
if(M==="("){U=")"
}U=(U+"").replace(/([.?*+^$[\]\\(){}|-])/g,"\\$1");
var T="^\\s*(?:[^"+U+"\\\\]+|\\\\\\\\|\\\\.)"+U;
if(V.match(new RegExp(T),true)){return A
}}if(M==="`"){var W=r(N);
var S=V.pos;
V.eatWhile("`");
var Q=1+V.pos-S;
if(!N.code){i=Q;
N.code=true;
return r(N)
}else{if(Q===i){N.code=false;
return W
}return r(N)
}}else{if(N.code){return r(N)
}}if(M==="["&&V.match(/.*\] ?(?:\(|\[)/,false)){return K(V,N,y)
}if(M==="<"&&V.match(/^(https?|ftps?):\/\/(?:[^\\>]|\\.)+>/,true)){return K(V,N,D(m,">"))
}if(M==="<"&&V.match(/^[^> \\]+@(?:[^\\>]|\\.)+>/,true)){return K(V,N,D(j,">"))
}if(M==="<"&&V.match(/^\w/,false)){var P=false;
if(V.string.indexOf(">")!=-1){var R=V.string.substring(1,V.string.indexOf(">"));
if(/markdown\s*=\s*('|"){0,1}1('|"){0,1}/.test(R)){N.md_inside=true
}}V.backUp(1);
return z(V,N,o)
}if(M==="<"&&V.match(/^\/\w*?>/)){N.md_inside=false;
return"tag"
}var W=r(N);
if(M==="*"||M==="_"){if(N.strong===M&&V.eat(M)){N.strong=false;
return W
}else{if(!N.strong&&V.eat(M)){N.strong=M;
return r(N)
}else{if(N.em===M){N.em=false;
return W
}else{if(!N.em){N.em=M;
return r(N)
}}}}}else{if(M===" "){if(V.eat("*")||V.eat("_")){if(V.peek()===" "){return r(N)
}else{V.backUp(1)
}}}}return r(N)
}function y(O,N){while(!O.eol()){var M=O.next();
if(M==="\\"){O.next()
}if(M==="]"){N.inline=N.f=u;
return C
}}return C
}function u(O,N){if(O.eatSpace()){return null
}var M=O.next();
if(M==="("||M==="["){return K(O,N,D(A,M==="("?")":"]"))
}return"error"
}function d(N,M){if(N.match(/^[^\]]*\]:/,true)){M.f=e;
return C
}return K(N,M,J)
}function e(N,M){if(N.eatSpace()){return null
}N.match(/^[^\s]+/,true);
if(N.peek()===undefined){M.linkTitle=true
}else{N.match(/^(?:\s+(?:"(?:[^"\\]|\\\\|\\.)+"|'(?:[^'\\]|\\\\|\\.)+'|\((?:[^)\\]|\\\\|\\.)+\)))?/,true)
}M.f=M.inline=J;
return A
}function q(M){if(!q[M]){M=(M+"").replace(/([.?*+^$[\]\\(){}|-])/g,"\\$1");
q[M]=new RegExp("^(?:[^\\\\]+?|\\\\.)*?("+M+")")
}return q[M]
}function D(N,O,M){M=M||J;
return function(Q,P){Q.match(q(O));
P.inline=P.f=M;
return N
}
}return{startState:function(){return{f:E,block:E,htmlState:CodeMirror.startState(h),indentation:0,inline:J,text:k,linkTitle:false,em:false,strong:false,header:false,list:false,quote:false}
},copyState:function(M){return{f:M.f,block:M.block,htmlState:CodeMirror.copyState(h,M.htmlState),indentation:M.indentation,inline:M.inline,text:M.text,linkTitle:M.linkTitle,em:M.em,strong:M.strong,header:M.header,list:M.list,quote:M.quote,md_inside:M.md_inside}
},token:function(O,N){if(O.sol()){if(O.match(/^\s*$/,true)){L=false;
return b(N)
}else{if(H){L=true;
H=false
}H=true
}N.header=false;
N.f=N.block;
var M=O.match(/^\s*/,true)[0].replace(/\t/g,"    ").length;
N.indentationDiff=M-N.indentation;
N.indentation=M;
if(M>0){return null
}}return N.f(O,N)
},blankLine:b,getType:r}
},"xml");
CodeMirror.defineMIME("text/x-markdown","markdown");