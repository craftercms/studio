(function(){CodeMirror.extendMode("css",{commentStart:"/*",commentEnd:"*/",wordWrapChars:[";","\\{","\\}"],autoFormatLineBreaks:function(d){return d.replace(new RegExp("(;|\\{|\\})([^\r\n])","g"),"$1\n$2")
}});
function b(j){var h=[/for\s*?\((.*?)\)/,/\"(.*?)(\"|$)/,/\'(.*?)(\'|$)/,/\/\*(.*?)(\*\/|$)/,/\/\/.*/];
var f=[];
for(var e=0;
e<h.length;
e++){var g=0;
while(g<j.length){var d=j.substr(g).match(h[e]);
if(d!=null){f.push({start:g+d.index,end:g+d.index+d[0].length});
g+=d.index+Math.max(1,d[0].length)
}else{break
}}}f.sort(function(k,i){return k.start-i.start
});
return f
}CodeMirror.extendMode("javascript",{commentStart:"/*",commentEnd:"*/",wordWrapChars:[";","\\{","\\}"],autoFormatLineBreaks:function(j){var g=0;
var h=/(;|\{|\})([^\r\n;])/g;
var f=b(j);
if(f!=null){var e="";
for(var d=0;
d<f.length;
d++){if(f[d].start>g){e+=j.substring(g,f[d].start).replace(h,"$1\n$2");
g=f[d].start
}if(f[d].start<=g&&f[d].end>=g){e+=j.substring(g,f[d].end);
g=f[d].end
}}if(g<j.length){e+=j.substr(g).replace(h,"$1\n$2")
}return e
}else{return j.replace(h,"$1\n$2")
}}});
CodeMirror.extendMode("xml",{commentStart:"<!--",commentEnd:"-->",wordWrapChars:[">"],autoFormatLineBreaks:function(k){var e=k.split("\n");
var g=new RegExp("(^\\s*?<|^[^<]*?)(.+)(>\\s*?$|[^>]*?$)");
var d=new RegExp("<","g");
var j=new RegExp("(>)([^\r\n])","g");
for(var f=0;
f<e.length;
f++){var h=e[f].match(g);
if(h!=null&&h.length>3){e[f]=h[1]+h[2].replace(d,"\n$&").replace(j,"$1\n$2")+h[3];
continue
}}return e.join("\n")
}});
function a(d,e){return CodeMirror.innerMode(d.getMode(),d.getTokenAt(e).state).mode
}function c(j,o,e,f){var m=j.getMode(),l=j.getLine(o);
if(f==null){f=l.length
}if(!m.innerMode){return[{from:e,to:f,mode:m}]
}var d=j.getTokenAt({line:o,ch:e}).state;
var g=CodeMirror.innerMode(m,d).mode;
var n=[],k=new CodeMirror.StringStream(l);
k.pos=k.start=e;
for(;
;
){m.token(k,d);
var h=CodeMirror.innerMode(m,d).mode;
if(h!=g){var i=k.start;
if(g.name=="xml"&&l.charAt(k.pos-1)==">"){i=k.pos
}n.push({from:e,to:i,mode:g});
e=i;
g=h
}if(k.pos>=f){break
}k.start=k.pos
}if(e<f){n.push({from:e,to:f,mode:g})
}return n
}CodeMirror.defineExtension("commentRange",function(e,h,g){var f=a(this,h),d=this;
this.operation(function(){if(e){d.replaceRange(f.commentEnd,g);
d.replaceRange(f.commentStart,h);
if(h.line==g.line&&h.ch==g.ch){d.setCursor(h.line,h.ch+f.commentStart.length)
}}else{var k=d.getRange(h,g);
var j=k.indexOf(f.commentStart);
var i=k.lastIndexOf(f.commentEnd);
if(j>-1&&i>-1&&i>j){k=k.substr(0,j)+k.substring(j+f.commentStart.length,i)+k.substr(i+f.commentEnd.length)
}d.replaceRange(k,h,g)
}})
});
CodeMirror.defineExtension("autoIndentRange",function(f,e){var d=this;
this.operation(function(){for(var g=f.line;
g<=e.line;
g++){d.indentLine(g,"smart")
}})
});
CodeMirror.defineExtension("autoFormatRange",function(f,e){var d=this;
d.operation(function(){for(var p=f.line,j=e.line;
p<=j;
++p){var n={line:p,ch:p==f.line?f.ch:0};
var r={line:p,ch:p==j?e.ch:null};
var h=c(d,p,n.ch,r.ch),l="";
var q=d.getRange(n,r);
for(var k=0;
k<h.length;
++k){var g=h.length>1?q.slice(h[k].from,h[k].to):q;
if(l){l+="\n"
}if(h[k].mode.autoFormatLineBreaks){l+=h[k].mode.autoFormatLineBreaks(g)
}else{l+=q
}}if(l!=q){for(var m=0,o=l.indexOf("\n");
o!=-1;
o=l.indexOf("\n",o+1),++m){}d.replaceRange(l,n,r);
p+=m;
j+=m
}}for(var p=f.line+1;
p<=j;
++p){d.indentLine(p,"smart")
}d.setSelection(f,d.getCursor(false))
})
})
})();