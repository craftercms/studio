CodeMirror.defineMode("verilog",function(c,e){var g=c.indentUnit,f=e.keywords||{},a=e.blockKeywords||{},h=e.atoms||{},p=e.hooks||{},i=e.multiLineStrings;
var b=/[&|~><!\)\(*#%@+\/=?\:;}{,\.\^\-\[\]]/;
var m;
function d(u,s){var r=u.next();
if(p[r]){var q=p[r](u,s);
if(q!==false){return q
}}if(r=='"'){s.tokenize=o(r);
return s.tokenize(u,s)
}if(/[\[\]{}\(\),;\:\.]/.test(r)){m=r;
return null
}if(/[\d']/.test(r)){u.eatWhile(/[\w\.']/);
return"number"
}if(r=="/"){if(u.eat("*")){s.tokenize=k;
return k(u,s)
}if(u.eat("/")){u.skipToEnd();
return"comment"
}}if(b.test(r)){u.eatWhile(b);
return"operator"
}u.eatWhile(/[\w\$_]/);
var t=u.current();
if(f.propertyIsEnumerable(t)){if(a.propertyIsEnumerable(t)){m="newstatement"
}return"keyword"
}if(h.propertyIsEnumerable(t)){return"atom"
}return"variable"
}function o(q){return function(v,t){var u=false,s,r=false;
while((s=v.next())!=null){if(s==q&&!u){r=true;
break
}u=!u&&s=="\\"
}if(r||!(u||i)){t.tokenize=d
}return"string"
}
}function k(t,s){var q=false,r;
while(r=t.next()){if(r=="/"&&q){s.tokenize=d;
break
}q=(r=="*")
}return"comment"
}function n(u,r,q,t,s){this.indented=u;
this.column=r;
this.type=q;
this.align=t;
this.prev=s
}function j(s,q,r){return s.context=new n(s.indented,q,r,null,s.context)
}function l(r){var q=r.context.type;
if(q==")"||q=="]"||q=="}"){r.indented=r.context.indented
}return r.context=r.context.prev
}return{startState:function(q){return{tokenize:null,context:new n((q||0)-g,0,"top",false),indented:0,startOfLine:true}
},token:function(t,s){var q=s.context;
if(t.sol()){if(q.align==null){q.align=false
}s.indented=t.indentation();
s.startOfLine=true
}if(t.eatSpace()){return null
}m=null;
var r=(s.tokenize||d)(t,s);
if(r=="comment"||r=="meta"){return r
}if(q.align==null){q.align=true
}if((m==";"||m==":")&&q.type=="statement"){l(s)
}else{if(m=="{"){j(s,t.column(),"}")
}else{if(m=="["){j(s,t.column(),"]")
}else{if(m=="("){j(s,t.column(),")")
}else{if(m=="}"){while(q.type=="statement"){q=l(s)
}if(q.type=="}"){q=l(s)
}while(q.type=="statement"){q=l(s)
}}else{if(m==q.type){l(s)
}else{if(q.type=="}"||q.type=="top"||(q.type=="statement"&&m=="newstatement")){j(s,t.column(),"statement")
}}}}}}}s.startOfLine=false;
return r
},indent:function(u,r){if(u.tokenize!=d&&u.tokenize!=null){return 0
}var t=r&&r.charAt(0),q=u.context,s=t==q.type;
if(q.type=="statement"){return q.indented+(t=="{"?0:g)
}else{if(q.align){return q.column+(s?0:1)
}else{return q.indented+(s?0:g)
}}},electricChars:"{}"}
});
(function(){function e(j){var g={},h=j.split(" ");
for(var f=0;
f<h.length;
++f){g[h[f]]=true
}return g
}var a="always and assign automatic begin buf bufif0 bufif1 case casex casez cell cmos config deassign default defparam design disable edge else end endcase endconfig endfunction endgenerate endmodule endprimitive endspecify endtable endtask event for force forever fork function generate genvar highz0 highz1 if ifnone incdir include initial inout input instance integer join large liblist library localparam macromodule medium module nand negedge nmos nor noshowcancelled not notif0 notif1 or output parameter pmos posedge primitive pull0 pull1 pulldown pullup pulsestyle_onevent pulsestyle_ondetect rcmos real realtime reg release repeat rnmos rpmos rtran rtranif0 rtranif1 scalared showcancelled signed small specify specparam strong0 strong1 supply0 supply1 table task time tran tranif0 tranif1 tri tri0 tri1 triand trior trireg unsigned use vectored wait wand weak0 weak1 while wire wor xnor xor";
var d="begin bufif0 bufif1 case casex casez config else end endcase endconfig endfunction endgenerate endmodule endprimitive endspecify endtable endtask for forever function generate if ifnone macromodule module primitive repeat specify table task while";
function b(g,f){g.eatWhile(/[\w\$_]/);
return"meta"
}function c(h,g){var f;
while((f=h.next())!=null){if(f=='"'&&!h.eat('"')){g.tokenize=null;
break
}}return"string"
}CodeMirror.defineMIME("text/x-verilog",{name:"verilog",keywords:e(a),blockKeywords:e(d),atoms:e("null"),hooks:{"`":b,"$":b}})
}());