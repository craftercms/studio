CodeMirror.runMode=function(j,f,q,s){function m(e){return e.replace(/[<&]/,function(i){return i=="<"?"&lt;":"&amp;"
})
}var h=CodeMirror.getMode(CodeMirror.defaults,f);
var o=q.nodeType==1;
var k=(s&&s.tabSize)||CodeMirror.defaults.tabSize;
if(o){var d=q,n=[],c=0;
q=function(x,v){if(x=="\n"){n.push("<br>");
c=0;
return
}var w="";
for(var y=0;
;
){var e=x.indexOf("\t",y);
if(e==-1){w+=m(x.slice(y));
c+=x.length-y;
break
}else{c+=e-y;
w+=m(x.slice(y,e));
var u=k-c%k;
c+=u;
for(var t=0;
t<u;
++t){w+=" "
}y=e+1
}}if(v){n.push('<span class="cm-'+m(v)+'">'+w+"</span>")
}else{n.push(w)
}}
}var r=CodeMirror.splitLines(j),b=CodeMirror.startState(h);
for(var g=0,l=r.length;
g<l;
++g){if(g){q("\n")
}var p=new CodeMirror.StringStream(r[g]);
while(!p.eol()){var a=h.token(p,b);
q(p.current(),a,g,p.start);
p.start=p.pos
}}if(o){d.innerHTML=n.join("")
}};