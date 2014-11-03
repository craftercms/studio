(function(){function a(b,e){var d=b.getWrapperElement();
var c=d.insertBefore(document.createElement("div"),d.firstChild);
c.className="CodeMirror-dialog";
c.innerHTML="<div>"+e+"</div>";
return c
}CodeMirror.defineExtension("openDialog",function(e,i){var d=a(this,e);
var b=false,g=this;
function h(){if(b){return
}b=true;
d.parentNode.removeChild(d)
}var f=d.getElementsByTagName("input")[0],c;
if(f){CodeMirror.connect(f,"keydown",function(j){if(j.keyCode==13||j.keyCode==27){CodeMirror.e_stop(j);
h();
g.focus();
if(j.keyCode==13){i(f.value)
}}});
f.focus();
CodeMirror.connect(f,"blur",h)
}else{if(c=d.getElementsByTagName("button")[0]){CodeMirror.connect(c,"click",function(){h();
g.focus()
});
c.focus();
CodeMirror.connect(c,"blur",h)
}}return h
});
CodeMirror.defineExtension("openConfirm",function(l,f){var g=a(this,l);
var h=g.getElementsByTagName("button");
var e=false,j=this,c=1;
function m(){if(e){return
}e=true;
g.parentNode.removeChild(g);
j.focus()
}h[0].focus();
for(var d=0;
d<h.length;
++d){var k=h[d];
(function(b){CodeMirror.connect(k,"click",function(i){CodeMirror.e_preventDefault(i);
m();
if(b){b(j)
}})
})(f[d]);
CodeMirror.connect(k,"blur",function(){--c;
setTimeout(function(){if(c<=0){m()
}},200)
});
CodeMirror.connect(k,"focus",function(){++c
})
}})
})();