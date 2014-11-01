CodeMirror.defineMode("smalltalk",function(c,k){var d=/[+\-/\\*~<>=@%|&?!.:;^]/;
var g=/true|false|nil|self|super|thisContext/;
var h=function(m,l){this.next=m;
this.parent=l
};
var b=function(l,m,n){this.name=l;
this.context=m;
this.eos=n
};
var i=function(){this.context=new h(f,null);
this.expectVariable=true;
this.indentation=0;
this.userIndentationDelta=0
};
i.prototype.userIndent=function(l){this.userIndentationDelta=l>0?(l/c.indentUnit-this.indentation):0
};
var f=function(p,n,o){var m=new b(null,n,false);
var l=p.next();
if(l==='"'){m=a(p,new h(a,n))
}else{if(l==="'"){m=j(p,new h(j,n))
}else{if(l==="#"){p.eatWhile(/[^ .]/);
m.name="string-2"
}else{if(l==="$"){p.eatWhile(/[^ ]/);
m.name="string-2"
}else{if(l==="|"&&o.expectVariable){m.context=new h(e,n)
}else{if(/[\[\]{}()]/.test(l)){m.name="bracket";
m.eos=/[\[{(]/.test(l);
if(l==="["){o.indentation++
}else{if(l==="]"){o.indentation=Math.max(0,o.indentation-1)
}}}else{if(d.test(l)){p.eatWhile(d);
m.name="operator";
m.eos=l!==";"
}else{if(/\d/.test(l)){p.eatWhile(/[\w\d]/);
m.name="number"
}else{if(/[\w_]/.test(l)){p.eatWhile(/[\w\d_]/);
m.name=o.expectVariable?(g.test(p.current())?"keyword":"variable"):null
}else{m.eos=o.expectVariable
}}}}}}}}}return m
};
var a=function(m,l){m.eatWhile(/[^"]/);
return new b("comment",m.eat('"')?l.parent:l,true)
};
var j=function(m,l){m.eatWhile(/[^']/);
return new b("string",m.eat("'")?l.parent:l,false)
};
var e=function(p,n,o){var m=new b(null,n,false);
var l=p.next();
if(l==="|"){m.context=n.parent;
m.eos=true
}else{p.eatWhile(/[^|]/);
m.name="variable"
}return m
};
return{startState:function(){return new i
},token:function(n,m){m.userIndent(n.indentation());
if(n.eatSpace()){return null
}var l=m.context.next(n,m.context,m);
m.context=l.context;
m.expectVariable=l.eos;
m.lastToken=l;
return l.name
},blankLine:function(l){l.userIndent(0)
},indent:function(n,l){var m=n.context.next===f&&l&&l.charAt(0)==="]"?-1:n.userIndentationDelta;
return(n.indentation+m)*c.indentUnit
},electricChars:"]"}
});
CodeMirror.defineMIME("text/x-stsrc",{name:"smalltalk"});