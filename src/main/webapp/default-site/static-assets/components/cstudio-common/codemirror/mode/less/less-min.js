CodeMirror.defineMode("less",function(c){var f=c.indentUnit,h;
function g(m,n){h=n;
return m
}var l="a abbr acronym address applet area article aside audio b base basefont bdi bdo big blockquote body br button canvas caption cite code col colgroup command datalist dd del details dfn dir div dl dt em embed fieldset figcaption figure font footer form frame frameset h1 h2 h3 h4 h5 h6 head header hgroup hr html i iframe img input ins keygen kbd label legend li link map mark menu meta meter nav noframes noscript object ol optgroup option output p param pre progress q rp rt ruby s samp script section select small source span strike strong style sub summary sup table tbody td textarea tfoot th thead time title tr track tt u ul var video wbr".split(" ");
function d(n){for(var m=0;
m<l.length;
m++){if(n===l[m]){return true
}}}var j=/(^\:root$|^\:nth\-child$|^\:nth\-last\-child$|^\:nth\-of\-type$|^\:nth\-last\-of\-type$|^\:first\-child$|^\:last\-child$|^\:first\-of\-type$|^\:last\-of\-type$|^\:only\-child$|^\:only\-of\-type$|^\:empty$|^\:link|^\:visited$|^\:active$|^\:hover$|^\:focus$|^\:target$|^\:lang$|^\:enabled^\:disabled$|^\:checked$|^\:first\-line$|^\:first\-letter$|^\:before$|^\:after$|^\:not$|^\:required$|^\:invalid$)/;
function e(s,q){var p=s.next();
if(p=="@"){s.eatWhile(/[\w\-]/);
return g("meta",s.current())
}else{if(p=="/"&&s.eat("*")){q.tokenize=k;
return k(s,q)
}else{if(p=="<"&&s.eat("!")){q.tokenize=b;
return b(s,q)
}else{if(p=="="){g(null,"compare")
}else{if(p=="|"&&s.eat("=")){return g(null,"compare")
}else{if(p=='"'||p=="'"){q.tokenize=i(p);
return q.tokenize(s,q)
}else{if(p=="/"){if(s.eat("/")){q.tokenize=a;
return a(s,q)
}else{if(h=="string"||h=="("){return g("string","string")
}if(q.stack[q.stack.length-1]!=undefined){return g(null,p)
}s.eatWhile(/[\a-zA-Z0-9\-_.\s]/);
if(/\/|\)|#/.test(s.peek()||(s.eatSpace()&&s.peek()==")"))||s.eol()){return g("string","string")
}}}else{if(p=="!"){s.match(/^\s*\w*/);
return g("keyword","important")
}else{if(/\d/.test(p)){s.eatWhile(/[\w.%]/);
return g("number","unit")
}else{if(/[,+<>*\/]/.test(p)){if(s.peek()=="="||h=="a"){return g("string","string")
}return g(null,"select-op")
}else{if(/[;{}:\[\]()~\|]/.test(p)){if(p==":"){s.eatWhile(/[a-z\\\-]/);
if(j.test(s.current())){return g("tag","tag")
}else{if(s.peek()==":"){s.next();
s.eatWhile(/[a-z\\\-]/);
if(s.current().match(/\:\:\-(o|ms|moz|webkit)\-/)){return g("string","string")
}if(j.test(s.current().substring(1))){return g("tag","tag")
}return g(null,p)
}else{return g(null,p)
}}}else{if(p=="~"){if(h=="r"){return g("string","string")
}}else{return g(null,p)
}}}else{if(p=="."){if(h=="("||h=="string"){return g("string","string")
}s.eatWhile(/[\a-zA-Z0-9\-_]/);
if(s.peek()==" "){s.eatSpace()
}if(s.peek()==")"){return g("number","unit")
}return g("tag","tag")
}else{if(p=="#"){s.eatWhile(/[A-Za-z0-9]/);
if(s.current().length==4||s.current().length==7){if(s.current().match(/[A-Fa-f0-9]{6}|[A-Fa-f0-9]{3}/,false)!=null){if(s.current().substring(1)!=s.current().match(/[A-Fa-f0-9]{6}|[A-Fa-f0-9]{3}/,false)){return g("atom","tag")
}s.eatSpace();
if(/[\/<>.(){!$%^&*_\-\\?=+\|#'~`]/.test(s.peek())){return g("atom","tag")
}else{if(s.peek()=="}"){return g("number","unit")
}else{if(/[a-zA-Z\\]/.test(s.peek())){return g("atom","tag")
}else{if(s.eol()){return g("atom","tag")
}else{return g("number","unit")
}}}}}else{s.eatWhile(/[\w\\\-]/);
return g("atom","tag")
}}else{s.eatWhile(/[\w\\\-]/);
return g("atom","tag")
}}else{if(p=="&"){s.eatWhile(/[\w\-]/);
return g(null,p)
}else{s.eatWhile(/[\w\\\-_%.{]/);
if(h=="string"){return g("string","string")
}else{if(s.current().match(/(^http$|^https$)/)!=null){s.eatWhile(/[\w\\\-_%.{:\/]/);
return g("string","string")
}else{if(s.peek()=="<"||s.peek()==">"){return g("tag","tag")
}else{if(/\(/.test(s.peek())){return g(null,p)
}else{if(s.peek()=="/"&&q.stack[q.stack.length-1]!=undefined){return g("string","string")
}else{if(s.current().match(/\-\d|\-.\d/)){return g("number","unit")
}else{if(d(s.current().toLowerCase())){return g("tag","tag")
}else{if(/\/|[\s\)]/.test(s.peek()||s.eol()||(s.eatSpace()&&s.peek()=="/"))&&s.current().indexOf(".")!==-1){if(s.current().substring(s.current().length-1,s.current().length)=="{"){s.backUp(1);
return g("tag","tag")
}s.eatSpace();
if(/[{<>.a-zA-Z\/]/.test(s.peek())||s.eol()){return g("tag","tag")
}return g("string","string")
}else{if(s.eol()||s.peek()=="["||s.peek()=="#"||h=="tag"){if(s.current().substring(s.current().length-1,s.current().length)=="{"){s.backUp(1)
}return g("tag","tag")
}else{if(h=="compare"||h=="a"||h=="("){return g("string","string")
}else{if(h=="|"||s.current()=="-"||h=="["){return g(null,p)
}else{if(s.peek()==":"){s.next();
var n=s.peek()==":"?true:false;
if(!n){var o=s.pos;
var r=s.current().length;
s.eatWhile(/[a-z\\\-]/);
var m=s.pos;
if(s.current().substring(r-1).match(j)!=null){s.backUp(m-(o-1));
return g("tag","tag")
}else{s.backUp(m-(o-1))
}}else{s.backUp(1)
}if(n){return g("tag","tag")
}else{return g("variable","variable")
}}else{return g("variable","variable")
}}}}}}}}}}}}}}}}}}}}}}}}}}}function a(n,m){n.skipToEnd();
m.tokenize=e;
return g("comment","comment")
}function k(p,o){var m=false,n;
while((n=p.next())!=null){if(m&&n=="/"){o.tokenize=e;
break
}m=(n=="*")
}return g("comment","comment")
}function b(p,o){var n=0,m;
while((m=p.next())!=null){if(n>=2&&m==">"){o.tokenize=e;
break
}n=(m=="-")?n+1:0
}return g("comment","comment")
}function i(m){return function(q,o){var p=false,n;
while((n=q.next())!=null){if(n==m&&!p){break
}p=!p&&n=="\\"
}if(!p){o.tokenize=e
}return g("string","string")
}
}return{startState:function(m){return{tokenize:e,baseIndent:m||0,stack:[]}
},token:function(p,o){if(p.eatSpace()){return null
}var n=o.tokenize(p,o);
var m=o.stack[o.stack.length-1];
if(h=="hash"&&m=="rule"){n="atom"
}else{if(n=="variable"){if(m=="rule"){n=null
}else{if(!m||m=="@media{"){n=p.current()=="when"?"variable":/[\s,|\s\)|\s]/.test(p.peek())?"tag":h
}}}}if(m=="rule"&&/^[\{\};]$/.test(h)){o.stack.pop()
}if(h=="{"){if(m=="@media"){o.stack[o.stack.length-1]="@media{"
}else{o.stack.push("{")
}}else{if(h=="}"){o.stack.pop()
}else{if(h=="@media"){o.stack.push("@media")
}else{if(m=="{"&&h!="comment"){o.stack.push("rule")
}}}}return n
},indent:function(o,m){var p=o.stack.length;
if(/^\}/.test(m)){p-=o.stack[o.stack.length-1]=="rule"?2:1
}return o.baseIndent+p*f
},electricChars:"}"}
});
CodeMirror.defineMIME("text/x-less","less");
if(!CodeMirror.mimeModes.hasOwnProperty("text/css")){CodeMirror.defineMIME("text/css","less")
};