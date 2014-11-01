CodeMirror.defineMode("clike",function(c,e){var g=c.indentUnit,f=e.keywords||{},n=e.builtin||{},a=e.blockKeywords||{},h=e.atoms||{},q=e.hooks||{},i=e.multiLineStrings;
var b=/[+\-*&%=<>!?|\/]/;
var m;
function d(v,t){var s=v.next();
if(q[s]){var r=q[s](v,t);
if(r!==false){return r
}}if(s=='"'||s=="'"){t.tokenize=p(s);
return t.tokenize(v,t)
}if(/[\[\]{}\(\),;\:\.]/.test(s)){m=s;
return null
}if(/\d/.test(s)){v.eatWhile(/[\w\.]/);
return"number"
}if(s=="/"){if(v.eat("*")){t.tokenize=k;
return k(v,t)
}if(v.eat("/")){v.skipToEnd();
return"comment"
}}if(b.test(s)){v.eatWhile(b);
return"operator"
}v.eatWhile(/[\w\$_]/);
var u=v.current();
if(f.propertyIsEnumerable(u)){if(a.propertyIsEnumerable(u)){m="newstatement"
}return"keyword"
}if(n.propertyIsEnumerable(u)){if(a.propertyIsEnumerable(u)){m="newstatement"
}return"builtin"
}if(h.propertyIsEnumerable(u)){return"atom"
}return"variable"
}function p(r){return function(w,u){var v=false,t,s=false;
while((t=w.next())!=null){if(t==r&&!v){s=true;
break
}v=!v&&t=="\\"
}if(s||!(v||i)){u.tokenize=null
}return"string"
}
}function k(u,t){var r=false,s;
while(s=u.next()){if(s=="/"&&r){t.tokenize=null;
break
}r=(s=="*")
}return"comment"
}function o(v,s,r,u,t){this.indented=v;
this.column=s;
this.type=r;
this.align=u;
this.prev=t
}function j(t,r,s){return t.context=new o(t.indented,r,s,null,t.context)
}function l(s){var r=s.context.type;
if(r==")"||r=="]"||r=="}"){s.indented=s.context.indented
}return s.context=s.context.prev
}return{startState:function(r){return{tokenize:null,context:new o((r||0)-g,0,"top",false),indented:0,startOfLine:true}
},token:function(u,t){var r=t.context;
if(u.sol()){if(r.align==null){r.align=false
}t.indented=u.indentation();
t.startOfLine=true
}if(u.eatSpace()){return null
}m=null;
var s=(t.tokenize||d)(u,t);
if(s=="comment"||s=="meta"){return s
}if(r.align==null){r.align=true
}if((m==";"||m==":")&&r.type=="statement"){l(t)
}else{if(m=="{"){j(t,u.column(),"}")
}else{if(m=="["){j(t,u.column(),"]")
}else{if(m=="("){j(t,u.column(),")")
}else{if(m=="}"){while(r.type=="statement"){r=l(t)
}if(r.type=="}"){r=l(t)
}while(r.type=="statement"){r=l(t)
}}else{if(m==r.type){l(t)
}else{if(r.type=="}"||r.type=="top"||(r.type=="statement"&&m=="newstatement")){j(t,u.column(),"statement")
}}}}}}}t.startOfLine=false;
return s
},indent:function(v,s){if(v.tokenize!=d&&v.tokenize!=null){return 0
}var r=v.context,u=s&&s.charAt(0);
if(r.type=="statement"&&u=="}"){r=r.prev
}var t=u==r.type;
if(r.type=="statement"){return r.indented+(u=="{"?0:g)
}else{if(r.align){return r.column+(t?0:1)
}else{return r.indented+(t?0:g)
}}},electricChars:"{}"}
});
(function(){function d(j){var g={},h=j.split(" ");
for(var f=0;
f<h.length;
++f){g[h[f]]=true
}return g
}var b="auto if break int case long char register continue return default short do sizeof double static else struct entry switch extern typedef float union for unsigned goto while enum void const signed volatile";
function e(g,f){if(!f.startOfLine){return false
}g.skipToEnd();
return"meta"
}function c(h,g){var f;
while((f=h.next())!=null){if(f=='"'&&!h.eat('"')){g.tokenize=null;
break
}}return"string"
}function a(f,h){for(var g=0;
g<f.length;
++g){CodeMirror.defineMIME(f[g],h)
}}a(["text/x-csrc","text/x-c","text/x-chdr"],{name:"clike",keywords:d(b),blockKeywords:d("case do else for if switch while struct"),atoms:d("null"),hooks:{"#":e}});
a(["text/x-c++src","text/x-c++hdr"],{name:"clike",keywords:d(b+" asm dynamic_cast namespace reinterpret_cast try bool explicit new static_cast typeid catch operator template typename class friend private this using const_cast inline public throw virtual delete mutable protected wchar_t"),blockKeywords:d("catch class do else finally for if struct switch try while"),atoms:d("true false null"),hooks:{"#":e}});
CodeMirror.defineMIME("text/x-java",{name:"clike",keywords:d("abstract assert boolean break byte case catch char class const continue default do double else enum extends final finally float for goto if implements import instanceof int interface long native new package private protected public return short static strictfp super switch synchronized this throw throws transient try void volatile while"),blockKeywords:d("catch class do else finally for if switch try while"),atoms:d("true false null"),hooks:{"@":function(g,f){g.eatWhile(/[\w\$_]/);
return"meta"
}}});
CodeMirror.defineMIME("text/x-csharp",{name:"clike",keywords:d("abstract as base break case catch checked class const continue default delegate do else enum event explicit extern finally fixed for foreach goto if implicit in interface internal is lock namespace new operator out override params private protected public readonly ref return sealed sizeof stackalloc static struct switch this throw try typeof unchecked unsafe using virtual void volatile while add alias ascending descending dynamic from get global group into join let orderby partial remove select set value var yield"),blockKeywords:d("catch class do else finally for foreach if struct switch try while"),builtin:d("Boolean Byte Char DateTime DateTimeOffset Decimal Double Guid Int16 Int32 Int64 Object SByte Single String TimeSpan UInt16 UInt32 UInt64 bool byte char decimal double short int long object sbyte float string ushort uint ulong"),atoms:d("true false null"),hooks:{"@":function(g,f){if(g.eat('"')){f.tokenize=c;
return c(g,f)
}g.eatWhile(/[\w\$_]/);
return"meta"
}}});
CodeMirror.defineMIME("text/x-scala",{name:"clike",keywords:d("abstract case catch class def do else extends false final finally for forSome if implicit import lazy match new null object override package private protected return sealed super this throw trait try trye type val var while with yield _ : = => <- <: <% >: # @ assert assume require print println printf readLine readBoolean readByte readShort readChar readInt readLong readFloat readDouble AnyVal App Application Array BufferedIterator BigDecimal BigInt Char Console Either Enumeration Equiv Error Exception Fractional Function IndexedSeq Integral Iterable Iterator List Map Numeric Nil NotNull Option Ordered Ordering PartialFunction PartialOrdering Product Proxy Range Responder Seq Serializable Set Specializable Stream StringBuilder StringContext Symbol Throwable Traversable TraversableOnce Tuple Unit Vector :: #:: Boolean Byte Character CharSequence Class ClassLoader Cloneable Comparable Compiler Double Exception Float Integer Long Math Number Object Package Pair Process Runtime Runnable SecurityManager Short StackTraceElement StrictMath String StringBuffer System Thread ThreadGroup ThreadLocal Throwable Triple Void"),blockKeywords:d("catch class do else finally for forSome if match switch try while"),atoms:d("true false null"),hooks:{"@":function(g,f){g.eatWhile(/[\w\$_]/);
return"meta"
}}})
}());