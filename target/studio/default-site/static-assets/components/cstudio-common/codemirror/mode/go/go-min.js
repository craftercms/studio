CodeMirror.defineMode("go",function(c,e){var g=c.indentUnit;
var f={"break":true,"case":true,chan:true,"const":true,"continue":true,"default":true,defer:true,"else":true,fallthrough:true,"for":true,func:true,go:true,"goto":true,"if":true,"import":true,"interface":true,map:true,"package":true,range:true,"return":true,select:true,struct:true,"switch":true,type:true,"var":true,bool:true,"byte":true,complex64:true,complex128:true,float32:true,float64:true,int8:true,int16:true,int32:true,int64:true,string:true,uint8:true,uint16:true,uint32:true,uint64:true,"int":true,uint:true,uintptr:true};
var h={"true":true,"false":true,iota:true,nil:true,append:true,cap:true,close:true,complex:true,copy:true,imag:true,len:true,make:true,"new":true,panic:true,print:true,println:true,real:true,recover:true};
var a={"else":true,"for":true,func:true,"if":true,"interface":true,select:true,struct:true,"switch":true};
var b=/[+\-*&^%:=<>!|\/]/;
var l;
function d(r,p){var o=r.next();
if(o=='"'||o=="'"||o=="`"){p.tokenize=n(o);
return p.tokenize(r,p)
}if(/[\d\.]/.test(o)){if(o=="."){r.match(/^[0-9]+([eE][\-+]?[0-9]+)?/)
}else{if(o=="0"){r.match(/^[xX][0-9a-fA-F]+/)||r.match(/^0[0-7]+/)
}else{r.match(/^[0-9]*\.?[0-9]*([eE][\-+]?[0-9]+)?/)
}}return"number"
}if(/[\[\]{}\(\),;\:\.]/.test(o)){l=o;
return null
}if(o=="/"){if(r.eat("*")){p.tokenize=j;
return j(r,p)
}if(r.eat("/")){r.skipToEnd();
return"comment"
}}if(b.test(o)){r.eatWhile(b);
return"operator"
}r.eatWhile(/[\w\$_]/);
var q=r.current();
if(f.propertyIsEnumerable(q)){if(q=="case"||q=="default"){l="case"
}return"keyword"
}if(h.propertyIsEnumerable(q)){return"atom"
}return"variable"
}function n(o){return function(t,r){var s=false,q,p=false;
while((q=t.next())!=null){if(q==o&&!s){p=true;
break
}s=!s&&q=="\\"
}if(p||!(s||o=="`")){r.tokenize=d
}return"string"
}
}function j(r,q){var o=false,p;
while(p=r.next()){if(p=="/"&&o){q.tokenize=d;
break
}o=(p=="*")
}return"comment"
}function m(s,p,o,r,q){this.indented=s;
this.column=p;
this.type=o;
this.align=r;
this.prev=q
}function i(q,o,p){return q.context=new m(q.indented,o,p,null,q.context)
}function k(p){var o=p.context.type;
if(o==")"||o=="]"||o=="}"){p.indented=p.context.indented
}return p.context=p.context.prev
}return{startState:function(o){return{tokenize:null,context:new m((o||0)-g,0,"top",false),indented:0,startOfLine:true}
},token:function(r,q){var o=q.context;
if(r.sol()){if(o.align==null){o.align=false
}q.indented=r.indentation();
q.startOfLine=true;
if(o.type=="case"){o.type="}"
}}if(r.eatSpace()){return null
}l=null;
var p=(q.tokenize||d)(r,q);
if(p=="comment"){return p
}if(o.align==null){o.align=true
}if(l=="{"){i(q,r.column(),"}")
}else{if(l=="["){i(q,r.column(),"]")
}else{if(l=="("){i(q,r.column(),")")
}else{if(l=="case"){o.type="case"
}else{if(l=="}"&&o.type=="}"){o=k(q)
}else{if(l==o.type){k(q)
}}}}}}q.startOfLine=false;
return p
},indent:function(s,p){if(s.tokenize!=d&&s.tokenize!=null){return 0
}var o=s.context,r=p&&p.charAt(0);
if(o.type=="case"&&/^(?:case|default)\b/.test(p)){s.context.type="}";
return o.indented
}var q=r==o.type;
if(o.align){return o.column+(q?0:1)
}else{return o.indented+(q?0:g)
}},electricChars:"{}:"}
});
CodeMirror.defineMIME("text/x-go","go");