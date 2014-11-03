(function(){CodeMirror.defaults.closeTagEnabled=true;
CodeMirror.defaults.closeTagIndent=["applet","blockquote","body","button","div","dl","fieldset","form","frameset","h1","h2","h3","h4","h5","h6","head","html","iframe","layer","legend","object","ol","p","select","table","ul"];
CodeMirror.defaults.closeTagVoid=["area","base","br","col","command","embed","hr","img","input","keygen","link","meta","param","source","track","wbr"];
function b(g,h){return CodeMirror.innerMode(g.getMode(),h).state
}CodeMirror.defineExtension("closeTag",function(n,g,j,k){if(!n.getOption("closeTagEnabled")){throw CodeMirror.Pass
}var m=n.getCursor();
var p=n.getTokenAt(m);
var h=b(n,p.state);
if(h){if(g==">"){var l=h.type;
if(p.className=="tag"&&l=="closeTag"){throw CodeMirror.Pass
}n.replaceSelection(">");
m={line:m.line,ch:m.ch+1};
n.setCursor(m);
p=n.getTokenAt(n.getCursor());
h=b(n,p.state);
if(!h){throw CodeMirror.Pass
}var l=h.type;
if(p.className=="tag"&&l!="selfcloseTag"){var i=h.tagName;
if(i.length>0&&f(n,k,i)){a(n,j,m,i)
}return
}n.setSelection({line:m.line,ch:m.ch-1},m);
n.replaceSelection("")
}else{if(g=="/"){if(p.className=="tag"&&p.string=="<"){var o=h.context,i=o?o.tagName:"";
if(i.length>0){c(n,m,i);
return
}}}}}throw CodeMirror.Pass
});
function a(h,g,j,i){if(d(h,g,i)){h.replaceSelection("\n\n</"+i+">","end");
h.indentLine(j.line+1);
h.indentLine(j.line+2);
h.setCursor({line:j.line+1,ch:h.getLine(j.line+1).length})
}else{h.replaceSelection("</"+i+">");
h.setCursor(j)
}}function d(h,g,i){if(typeof g=="undefined"||g==null||g==true){g=h.getOption("closeTagIndent")
}if(!g){g=[]
}return e(g,i.toLowerCase())!=-1
}function f(g,h,i){if(g.getOption("mode")=="xml"){return true
}if(typeof h=="undefined"||h==null){h=g.getOption("closeTagVoid")
}if(!h){h=[]
}return e(h,i.toLowerCase())==-1
}function e(k,g){if(k.indexOf){return k.indexOf(g)
}for(var h=0,j=k.length;
h<j;
++h){if(k[h]==g){return h
}}return -1
}function c(g,i,h){g.replaceSelection("/"+h+">");
g.setCursor({line:i.line,ch:i.ch+h.length+2})
}})();