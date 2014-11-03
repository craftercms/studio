(function(){function a(b,e,g,d){this.atOccurrence=false;
this.cm=b;
if(d==null&&typeof e=="string"){d=false
}g=g?b.clipPos(g):{line:0,ch:0};
this.pos={from:g,to:g};
if(typeof e!="string"){if(!e.global){e=new RegExp(e.source,e.ignoreCase?"ig":"g")
}this.matches=function(j,m){if(j){e.lastIndex=0;
var h=b.getLine(m.line).slice(0,m.ch),i=e.exec(h),l=0;
while(i){l+=i.index;
h=h.slice(i.index);
e.lastIndex=0;
var k=e.exec(h);
if(k){i=k
}else{break
}l++
}}else{e.lastIndex=m.ch;
var h=b.getLine(m.line),i=e.exec(h),l=i&&i.index
}if(i){return{from:{line:m.line,ch:l},to:{line:m.line,ch:l+i[0].length},match:i}
}}
}else{if(d){e=e.toLowerCase()
}var c=d?function(h){return h.toLowerCase()
}:function(h){return h
};
var f=e.split("\n");
if(f.length==1){this.matches=function(k,l){var i=c(b.getLine(l.line)),h=e.length,j;
if(k?(l.ch>=h&&(j=i.lastIndexOf(e,l.ch-h))!=-1):(j=i.indexOf(e,l.ch))!=-1){return{from:{line:l.line,ch:j},to:{line:l.line,ch:j+h}}
}}
}else{this.matches=function(m,o){var n=o.line,p=(m?f.length-1:0),k=f[p],q=c(b.getLine(n));
var l=(m?q.indexOf(k)+k.length:q.lastIndexOf(k));
if(m?l>=o.ch||l!=k.length:l<=o.ch||l!=q.length-k.length){return
}for(;
;
){if(m?!n:n==b.lineCount()-1){return
}q=c(b.getLine(n+=m?-1:1));
k=f[m?--p:++p];
if(p>0&&p<f.length-1){if(q!=k){return
}else{continue
}}var j=(m?q.lastIndexOf(k):q.indexOf(k)+k.length);
if(m?j!=q.length-k.length:j!=k.length){return
}var h={line:o.line,ch:l},i={line:n,ch:j};
return{from:m?i:h,to:m?h:i}
}}
}}}a.prototype={findNext:function(){return this.find(false)
},findPrevious:function(){return this.find(true)
},find:function(c){var b=this,f=this.cm.clipPos(c?this.pos.from:this.pos.to);
function d(g){var h={line:g,ch:0};
b.pos={from:h,to:h};
b.atOccurrence=false;
return false
}for(;
;
){if(this.pos=this.matches(c,f)){this.atOccurrence=true;
return this.pos.match||true
}if(c){if(!f.line){return d(0)
}f={line:f.line-1,ch:this.cm.getLine(f.line-1).length}
}else{var e=this.cm.lineCount();
if(f.line==e-1){return d(e)
}f={line:f.line+1,ch:0}
}}},from:function(){if(this.atOccurrence){return this.pos.from
}},to:function(){if(this.atOccurrence){return this.pos.to
}},replace:function(c){var b=this;
if(this.atOccurrence){b.pos.to=this.cm.replaceRange(c,b.pos.from,b.pos.to)
}}};
CodeMirror.defineExtension("getSearchCursor",function(c,d,b){return new a(this,c,d,b)
})
})();