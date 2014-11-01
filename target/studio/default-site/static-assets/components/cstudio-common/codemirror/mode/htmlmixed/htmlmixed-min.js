CodeMirror.defineMode("htmlmixed",function(b){var h=CodeMirror.getMode(b,{name:"xml",htmlMode:true});
var g=CodeMirror.getMode(b,"javascript");
var f=CodeMirror.getMode(b,"css");
function e(k,j){var i=h.token(k,j.htmlState);
if(i=="tag"&&k.current()==">"&&j.htmlState.context){if(/^script$/i.test(j.htmlState.context.tagName)){j.token=a;
j.localState=g.startState(h.indent(j.htmlState,""))
}else{if(/^style$/i.test(j.htmlState.context.tagName)){j.token=c;
j.localState=f.startState(h.indent(j.htmlState,""))
}}}return i
}function d(o,j,k){var n=o.current();
var l=n.search(j),i;
if(l>-1){o.backUp(n.length-l)
}else{if(i=n.match(/<\/?$/)){o.backUp(n[0].length);
if(!o.match(j,false)){o.match(n[0])
}}}return k
}function a(j,i){if(j.match(/^<\/\s*script\s*>/i,false)){i.token=e;
i.localState=null;
return e(j,i)
}return d(j,/<\/\s*script\s*>/,g.token(j,i.localState))
}function c(j,i){if(j.match(/^<\/\s*style\s*>/i,false)){i.token=e;
i.localState=null;
return e(j,i)
}return d(j,/<\/\s*style\s*>/,f.token(j,i.localState))
}return{startState:function(){var i=h.startState();
return{token:e,localState:null,mode:"html",htmlState:i}
},copyState:function(j){if(j.localState){var i=CodeMirror.copyState(j.token==c?f:g,j.localState)
}return{token:j.token,localState:i,mode:j.mode,htmlState:CodeMirror.copyState(h,j.htmlState)}
},token:function(j,i){return i.token(j,i)
},indent:function(j,i){if(j.token==e||/^\s*<\//.test(i)){return h.indent(j.htmlState,i)
}else{if(j.token==a){return g.indent(j.localState,i)
}else{return f.indent(j.localState,i)
}}},electricChars:"/{}:",innerMode:function(i){var j=i.token==e?h:i.token==a?g:f;
return{state:i.localState||i.htmlState,mode:j}
}}
},"xml","javascript","css");
CodeMirror.defineMIME("text/html","htmlmixed");