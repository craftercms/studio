(function(){CodeMirror.simpleHint=function(e,b,a){var c={},g=CodeMirror.simpleHint.defaults;
for(var d in g){if(g.hasOwnProperty(d)){c[d]=(a&&a.hasOwnProperty(d)?a:g)[d]
}}function f(r){if(e.somethingSelected()){return
}var o=e.getTokenAt(e.getCursor());
if(c.closeOnTokenChange&&r!=null&&(o.start!=r.start||o.className!=r.className)){return
}var v=b(e);
if(!v||!v.list.length){return
}var s=v.list;
function t(i){e.replaceRange(i,v.from,v.to)
}if(s.length==1){t(s[0]);
return true
}var j=document.createElement("div");
j.className="CodeMirror-completions";
var k=j.appendChild(document.createElement("select"));
if(!window.opera){k.multiple=true
}for(var m=0;
m<s.length;
++m){var h=k.appendChild(document.createElement("option"));
h.appendChild(document.createTextNode(s[m]))
}k.firstChild.selected=true;
k.size=Math.min(10,s.length);
var q=e.cursorCoords();
j.style.left=q.x+"px";
j.style.top=q.yBot+"px";
document.body.appendChild(j);
var p=window.innerWidth||Math.max(document.body.offsetWidth,document.documentElement.offsetWidth);
if(p-q.x<k.clientWidth){j.style.left=(q.x-k.clientWidth)+"px"
}if(s.length<=10){j.style.width=(k.clientWidth-1)+"px"
}var l=false;
function u(){if(l){return
}l=true;
j.parentNode.removeChild(j)
}function n(){t(s[k.selectedIndex]);
u();
setTimeout(function(){e.focus()
},50)
}CodeMirror.connect(k,"blur",u);
CodeMirror.connect(k,"keydown",function(w){var i=w.keyCode;
if(i==13){CodeMirror.e_stop(w);
n()
}else{if(i==27){CodeMirror.e_stop(w);
u();
e.focus()
}else{if(i!=38&&i!=40&&i!=33&&i!=34){u();
e.focus();
e.triggerOnKeyDown(w);
if(!c.closeOnBackspace||i!=8){setTimeout(function(){f(o)
},50)
}}}}});
CodeMirror.connect(k,"dblclick",n);
k.focus();
if(window.opera){setTimeout(function(){if(!l){k.focus()
}},100)
}return true
}return f()
};
CodeMirror.simpleHint.defaults={closeOnBackspace:true,closeOnTokenChange:false}
})();