function escape(a){var b=a;
b=b.replace(/&/g,"&amp;");
b=b.replace(/</g,"&lt;");
b=b.replace(/>/g,"&gt;");
b=b.replace(/"/g,"&quot;");
return b
}function diffString(g,h){g=g.replace(/\s+$/,"");
h=h.replace(/\s+$/,"");
var b=diff(g==""?[]:g.split(/\s+/),h==""?[]:h.split(/\s+/));
var f="";
var a=g.match(/\s+/g);
if(a==null){a=["\n"]
}else{a.push("\n")
}var d=h.match(/\s+/g);
if(d==null){d=["\n"]
}else{d.push("\n")
}if(b.n.length==0){for(var c=0;
c<b.o.length;
c++){f+="<del>"+escape(b.o[c])+a[c]+"</del>"
}}else{if(b.n[0].text==null){for(h=0;
h<b.o.length&&b.o[h].text==null;
h++){f+="<del>"+escape(b.o[h])+a[h]+"</del>"
}}for(var c=0;
c<b.n.length;
c++){if(b.n[c].text==null){f+="<ins>"+escape(b.n[c])+d[c]+"</ins>"
}else{var e="";
for(h=b.n[c].row+1;
h<b.o.length&&b.o[h].text==null;
h++){e+="<del>"+escape(b.o[h])+a[h]+"</del>"
}f+=" "+b.n[c].text+d[c]+e
}}}return f
}function randomColor(){return"rgb("+(Math.random()*100)+"%, "+(Math.random()*100)+"%, "+(Math.random()*100)+"%)"
}function diffString2(b,c){b=b.replace(/\s+$/,"");
c=c.replace(/\s+$/,"");
var e=diff(b==""?[]:b.split(/\s+/),c==""?[]:c.split(/\s+/));
var j=b.match(/\s+/g);
if(j==null){j=["\n"]
}else{j.push("\n")
}var g=c.match(/\s+/g);
if(g==null){g=["\n"]
}else{g.push("\n")
}var d="";
var a=new Array();
for(var f=0;
f<e.o.length;
f++){a[f]=randomColor();
if(e.o[f].text!=null){d+='<span style="background-color: '+a[f]+'">'+escape(e.o[f].text)+j[f]+"</span>"
}else{d+="<del>"+escape(e.o[f])+j[f]+"</del>"
}}var h="";
for(var f=0;
f<e.n.length;
f++){if(e.n[f].text!=null){h+='<span style="background-color: '+a[e.n[f].row]+'">'+escape(e.n[f].text)+g[f]+"</span>"
}else{h+="<ins>"+escape(e.n[f])+g[f]+"</ins>"
}}return{o:d,n:h}
}function diff(d,e){var b=new Object();
var c=new Object();
for(var a=0;
a<e.length;
a++){if(b[e[a]]==null){b[e[a]]={rows:new Array(),o:null}
}b[e[a]].rows.push(a)
}for(var a=0;
a<d.length;
a++){if(c[d[a]]==null){c[d[a]]={rows:new Array(),n:null}
}c[d[a]].rows.push(a)
}for(var a in b){if(b[a].rows.length==1&&typeof(c[a])!="undefined"&&c[a].rows.length==1){e[b[a].rows[0]]={text:e[b[a].rows[0]],row:c[a].rows[0]};
d[c[a].rows[0]]={text:d[c[a].rows[0]],row:b[a].rows[0]}
}}for(var a=0;
a<e.length-1;
a++){if(e[a].text!=null&&e[a+1].text==null&&e[a].row+1<d.length&&d[e[a].row+1].text==null&&e[a+1]==d[e[a].row+1]){e[a+1]={text:e[a+1],row:e[a].row+1};
d[e[a].row+1]={text:d[e[a].row+1],row:a+1}
}}for(var a=e.length-1;
a>0;
a--){if(e[a].text!=null&&e[a-1].text==null&&e[a].row>0&&d[e[a].row-1].text==null&&e[a-1]==d[e[a].row-1]){e[a-1]={text:e[a-1],row:e[a].row-1};
d[e[a].row-1]={text:d[e[a].row-1],row:a-1}
}}return{o:d,n:e}
};