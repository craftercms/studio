CodeMirror.tagRangeFinder=function(j,n,y){var v="A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD";
var A=v+"-:.0-9\\u00B7\\u0300-\\u036F\\u203F-\\u2040";
var c=new RegExp("^["+v+"]["+A+"]*");
var e=j.getLine(n);
var o=false;
var B=null;
var f=0;
while(!o){f=e.indexOf("<",f);
if(-1==f){return
}if(f+1<e.length&&e[f+1]=="/"){f++;
continue
}if(!e.substr(f+1).match(c)){f++;
continue
}var m=e.indexOf(">",f+1);
if(-1==m){var u=n+1;
var q=false;
var a=j.lineCount();
while(u<a&&!q){var g=j.getLine(u);
var p=g.indexOf(">");
if(-1!=p){q=true;
var r=g.lastIndexOf("/",p);
if(-1!=r&&r<p){var s=e.substr(r,p-r+1);
if(!s.match(/\/\s*\>/)){if(y===true){u++
}return u
}}}u++
}o=true
}else{var x=e.lastIndexOf("/",m);
if(-1==x){o=true
}else{var s=e.substr(x,m-x+1);
if(!s.match(/\/\s*\>/)){o=true
}}}if(o){var b=e.substr(f+1);
B=b.match(c);
if(B){B=B[0];
if(-1!=e.indexOf("</"+B+">",f)){o=false
}}else{o=false
}}if(!o){f++
}}if(o){var t="(\\<\\/"+B+"\\>)|(\\<"+B+"\\>)|(\\<"+B+"\\s)|(\\<"+B+"$)";
var d=new RegExp(t,"g");
var k="</"+B+">";
var z=1;
var u=n+1;
var a=j.lineCount();
while(u<a){e=j.getLine(u);
var h=e.match(d);
if(h){for(var w=0;
w<h.length;
w++){if(h[w]==k){z--
}else{z++
}if(!z){if(y===true){u++
}return u
}}}u++
}return
}};
CodeMirror.braceRangeFinder=function(j,o,p){var g=j.getLine(o),a=g.length,q,l;
for(;
;
){var n=g.lastIndexOf("{",a);
if(n<0){break
}l=j.getTokenAt({line:o,ch:n}).className;
if(!/^(comment|string)/.test(l)){q=n;
break
}a=n-1
}if(q==null||g.lastIndexOf("}")>q){return
}var e=1,h=j.lineCount(),b;
outer:for(var c=o+1;
c<h;
++c){var m=j.getLine(c),f=0;
for(;
;
){var d=m.indexOf("{",f),k=m.indexOf("}",f);
if(d<0){d=m.length
}if(k<0){k=m.length
}f=Math.min(d,k);
if(f==m.length){break
}if(j.getTokenAt({line:c,ch:f+1}).className==l){if(f==d){++e
}else{if(!--e){b=c;
break outer
}}}++f
}}if(b==null||b==o+1){return
}if(p===true){b++
}return b
};
CodeMirror.indentRangeFinder=function(a,c){var h=a.getOption("tabSize");
var d=a.getLineHandle(c).indentation(h),f;
for(var e=c+1,b=a.lineCount();
e<b;
++e){var g=a.getLineHandle(e);
if(!/^\s*$/.test(g.text)){if(g.indentation(h)<=d){break
}f=e
}}if(!f){return null
}return f+1
};
CodeMirror.newFoldFunction=function(f,a,b){var e=[];
if(a==null){a='<div style="position: absolute; left: 2px; color:#600">&#x25bc;</div>%N%'
}function d(g,k){for(var h=0;
h<e.length;
++h){var j=g.lineInfo(e[h].start);
if(!j){e.splice(h--,1)
}else{if(j.line==k){return{pos:h,region:e[h]}
}}}}function c(g,j){g.clearMarker(j.start);
for(var h=0;
h<j.hidden.length;
++h){g.showLine(j.hidden[h])
}}return function(g,h){g.operation(function(){var l=d(g,h);
if(l){e.splice(l.pos,1);
c(g,l.region)
}else{var j=f(g,h,b);
if(j==null){return
}var n=[];
for(var k=h+1;
k<j;
++k){var m=g.hideLine(k);
if(m){n.push(m)
}}var p=g.setMarker(h,a);
var o={start:p,hidden:n};
g.onDeleteLine(p,function(){c(g,o)
});
e.push(o)
}})
}
};