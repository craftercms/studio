CodeMirror.defineMode("velocity",function(c){function e(q){var o={},p=q.split(" ");
for(var n=0;
n<p.length;
++n){o[p[n]]=true
}return o
}var h=c.indentUnit;
var g=e("#end #else #break #stop #[[ #]] #{end} #{else} #{break} #{stop}");
var f=e("#if #elseif #foreach #set #include #parse #macro #define #evaluate #{if} #{elseif} #{foreach} #{set} #{include} #{parse} #{macro} #{define} #{evaluate}");
var k=e("$foreach.count $foreach.hasNext $foreach.first $foreach.last $foreach.topmost $foreach.parent $velocityCount");
var b=/[+\-*&%=<>!?:\/|]/;
var i=true;
function a(p,o,n){o.tokenize=n;
return n(p,o)
}function d(r,p){var o=p.beforeParams;
p.beforeParams=false;
var n=r.next();
if((n=='"'||n=="'")&&p.inParams){return a(r,p,m(n))
}else{if(/[\[\]{}\(\),;\.]/.test(n)){if(n=="("&&o){p.inParams=true
}else{if(n==")"){p.inParams=false
}}return null
}else{if(/\d/.test(n)){r.eatWhile(/[\w\.]/);
return"number"
}else{if(n=="#"&&r.eat("*")){return a(r,p,j)
}else{if(n=="#"&&r.match(/ *\[ *\[/)){return a(r,p,l)
}else{if(n=="#"&&r.eat("#")){r.skipToEnd();
return"comment"
}else{if(n=="$"){r.eatWhile(/[\w\d\$_\.{}]/);
if(k&&k.propertyIsEnumerable(r.current().toLowerCase())){return"keyword"
}else{p.beforeParams=true;
return"builtin"
}}else{if(b.test(n)){r.eatWhile(b);
return"operator"
}else{r.eatWhile(/[\w\$_{}]/);
var q=r.current().toLowerCase();
if(g&&g.propertyIsEnumerable(q)){return"keyword"
}if(f&&f.propertyIsEnumerable(q)||r.current().match(/^#[a-z0-9_]+ *$/i)&&r.peek()=="("){p.beforeParams=true;
return"keyword"
}return null
}}}}}}}}}function m(n){return function(s,q){var r=false,p,o=false;
while((p=s.next())!=null){if(p==n&&!r){o=true;
break
}r=!r&&p=="\\"
}if(o){q.tokenize=d
}return"string"
}
}function j(q,p){var n=false,o;
while(o=q.next()){if(o=="#"&&n){p.tokenize=d;
break
}n=(o=="*")
}return"comment"
}function l(q,p){var n=0,o;
while(o=q.next()){if(o=="#"&&n==2){p.tokenize=d;
break
}if(o=="]"){n++
}else{if(o!=" "){n=0
}}}return"meta"
}return{startState:function(n){return{tokenize:d,beforeParams:false,inParams:false}
},token:function(o,n){if(o.eatSpace()){return null
}return n.tokenize(o,n)
}}
});
CodeMirror.defineMIME("text/velocity","velocity");