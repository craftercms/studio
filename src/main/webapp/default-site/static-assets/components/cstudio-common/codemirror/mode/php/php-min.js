(function(){function c(g){var e={},f=g.split(" ");
for(var d=0;
d<f.length;
++d){e[f[d]]=true
}return e
}function a(d){return function(f,e){if(f.match(d)){e.tokenize=null
}else{f.skipToEnd()
}return"string"
}
}var b={name:"clike",keywords:c("abstract and array as break case catch class clone const continue declare default do else elseif enddeclare endfor endforeach endif endswitch endwhile extends final for foreach function global goto if implements interface instanceof namespace new or private protected public static switch throw trait try use var while xor die echo empty exit eval include include_once isset list require require_once return print unset __halt_compiler self static parent"),blockKeywords:c("catch do else elseif for foreach if switch try while"),atoms:c("true false null TRUE FALSE NULL"),multiLineStrings:true,hooks:{"$":function(e,d){e.eatWhile(/[\w\$_]/);
return"variable-2"
},"<":function(e,d){if(e.match(/<</)){e.eatWhile(/[\w\.]/);
d.tokenize=a(e.current().slice(3));
return d.tokenize(e,d)
}return false
},"#":function(e,d){while(!e.eol()&&!e.match("?>",false)){e.next()
}return"comment"
},"/":function(e,d){if(e.eat("/")){while(!e.eol()&&!e.match("?>",false)){e.next()
}return"comment"
}return false
}}};
CodeMirror.defineMode("php",function(f,h){var j=CodeMirror.getMode(f,{name:"xml",htmlMode:true});
var i=CodeMirror.getMode(f,"javascript");
var g=CodeMirror.getMode(f,"css");
var d=CodeMirror.getMode(f,b);
function e(p,n){var m=n.curMode==d;
if(p.sol()&&n.pending!='"'){n.pending=null
}if(n.curMode==j){if(p.match(/^<\?\w*/)){n.curMode=d;
n.curState=n.php;
n.curClose="?>";
return"meta"
}if(n.pending=='"'){while(!p.eol()&&p.next()!='"'){}var l="string"
}else{if(n.pending&&p.pos<n.pending.end){p.pos=n.pending.end;
var l=n.pending.style
}else{var l=j.token(p,n.curState)
}}n.pending=null;
var o=p.current(),k=o.search(/<\?/);
if(k!=-1){if(l=="string"&&/\"$/.test(o)&&!/\?>/.test(o)){n.pending='"'
}else{n.pending={end:p.pos,style:l}
}p.backUp(o.length-k)
}else{if(l=="tag"&&p.current()==">"&&n.curState.context){if(/^script$/i.test(n.curState.context.tagName)){n.curMode=i;
n.curState=i.startState(j.indent(n.curState,""));
n.curClose=/^<\/\s*script\s*>/i
}else{if(/^style$/i.test(n.curState.context.tagName)){n.curMode=g;
n.curState=g.startState(j.indent(n.curState,""));
n.curClose=/^<\/\s*style\s*>/i
}}}}return l
}else{if((!m||n.php.tokenize==null)&&p.match(n.curClose,m)){n.curMode=j;
n.curState=n.html;
n.curClose=null;
if(m){return"meta"
}else{return e(p,n)
}}else{return n.curMode.token(p,n.curState)
}}}return{startState:function(){var k=j.startState();
return{html:k,php:d.startState(),curMode:h.startOpen?d:j,curState:h.startOpen?d.startState():k,curClose:h.startOpen?/^\?>/:null,mode:h.startOpen?"php":"html",pending:null}
},copyState:function(n){var l=n.html,m=CodeMirror.copyState(j,l),p=n.php,k=CodeMirror.copyState(d,p),o;
if(n.curState==l){o=m
}else{if(n.curState==p){o=k
}else{o=CodeMirror.copyState(n.curMode,n.curState)
}}return{html:m,php:k,curMode:n.curMode,curState:o,curClose:n.curClose,mode:n.mode,pending:n.pending}
},token:e,indent:function(l,k){if((l.curMode!=d&&/^\s*<\//.test(k))||(l.curMode==d&&/^\?>/.test(k))){return j.indent(l.html,k)
}return l.curMode.indent(l.curState,k)
},electricChars:"/{}:",innerMode:function(k){return{state:k.curState,mode:k.curMode}
}}
},"xml","clike","javascript","css");
CodeMirror.defineMIME("application/x-httpd-php","php");
CodeMirror.defineMIME("application/x-httpd-php-open",{name:"php",startOpen:true});
CodeMirror.defineMIME("text/x-php",b)
})();