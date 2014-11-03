(function(){CodeMirror.xmlHints=[];
CodeMirror.xmlHint=function(e,g){if(g.length>0){var f=e.getCursor();
e.replaceSelection(g);
f={line:f.line,ch:f.ch+1};
e.setCursor(f)
}var d=editor.getTokenAt;
editor.getTokenAt=function(){return"disabled"
};
CodeMirror.simpleHint(e,a);
editor.getTokenAt=d
};
var a=function(d){var k=d.getCursor();
if(k.ch>0){var j=d.getRange({line:0,ch:0},k);
var h="";
var l="";
for(var e=j.length-1;
e>=0;
e--){if(j[e]==" "||j[e]=="<"){l=j[e];
break
}else{h=j[e]+h
}}j=j.slice(0,j.length-h.length);
var g=b(d,j)+l;
var f=CodeMirror.xmlHints[g];
if(typeof f==="undefined"){f=[""]
}else{f=f.slice(0);
for(var e=f.length-1;
e>=0;
e--){if(f[e].indexOf(h)!=0){f.splice(e,1)
}}}return{list:f,from:{line:k.line,ch:k.ch-h.length},to:k}
}};
var b=function(g,l){var d="";
if(l.length>=0){var j=new RegExp("<([^!?][^\\s/>]*).*?>","g");
var f=[];
var h;
while((h=j.exec(l))!=null){f.push({tag:h[1],selfclose:(h[0].slice(h[0].length-2)==="/>")})
}for(var e=f.length-1,k=0;
e>=0;
e--){var m=f[e];
if(m.tag[0]=="/"){k++
}else{if(m.selfclose==false){if(k>0){k--
}else{d="<"+m.tag+">"+d
}}}}d+=c(l)
}return d
};
var c=function(g){var d=g.lastIndexOf("<");
var f=g.lastIndexOf(">");
if(f<d){g=g.slice(d);
if(g!="<"){var e=g.indexOf(" ");
if(e<0){e=g.indexOf("\t")
}if(e<0){e=g.indexOf("\n")
}if(e<0){e=g.length
}return g.slice(0,e)
}}return""
}
})();