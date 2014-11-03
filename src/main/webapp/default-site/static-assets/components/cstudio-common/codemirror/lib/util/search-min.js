(function(){function k(){this.posFrom=this.posTo=this.query=null;
this.marked=[]
}function f(o){return o._searchState||(o._searchState=new k())
}function m(o,p,q){return o.getSearchCursor(p,q,typeof p=="string"&&p==p.toLowerCase())
}function j(o,r,p,q){if(o.openDialog){o.openDialog(r,q)
}else{q(prompt(p,""))
}}function n(p,r,q,o){if(p.openConfirm){p.openConfirm(r,o)
}else{if(confirm(q)){o[0]()
}}}function c(p){var o=p.match(/^\/(.*)\/([a-z]*)$/);
return o?new RegExp(o[1],o[2].indexOf("i")==-1?"":"i"):p
}var b='Search: <input type="text" style="width: 10em"/> <span style="color: #888">(Use /re/ syntax for regexp search)</span>';
function i(o,p){var q=f(o);
if(q.query){return g(o,p)
}j(o,b,"Search for:",function(r){o.operation(function(){if(!r||q.query){return
}q.query=c(r);
if(o.lineCount()<2000){for(var s=m(o,q.query);
s.findNext();
){q.marked.push(o.markText(s.from(),s.to(),"CodeMirror-searching"))
}}q.posFrom=q.posTo=o.getCursor();
g(o,p)
})
})
}function g(o,p){o.operation(function(){var q=f(o);
var r=m(o,q.query,p?q.posFrom:q.posTo);
if(!r.find(p)){r=m(o,q.query,p?{line:o.lineCount()-1}:{line:0,ch:0});
if(!r.find(p)){return
}}o.setSelection(r.from(),r.to());
q.posFrom=r.from();
q.posTo=r.to()
})
}function l(o){o.operation(function(){var q=f(o);
if(!q.query){return
}q.query=null;
for(var p=0;
p<q.marked.length;
++p){q.marked[p].clear()
}q.marked.length=0
})
}var e='Replace: <input type="text" style="width: 10em"/> <span style="color: #888">(Use /re/ syntax for regexp search)</span>';
var h='With: <input type="text" style="width: 10em"/>';
var d="Replace? <button>Yes</button> <button>No</button> <button>Stop</button>";
function a(o,p){j(o,e,"Replace:",function(q){if(!q){return
}q=c(q);
j(o,h,"Replace with:",function(u){if(p){o.compoundChange(function(){o.operation(function(){for(var w=m(o,q);
w.findNext();
){if(typeof q!="string"){var v=o.getRange(w.from(),w.to()).match(q);
w.replace(u.replace(/\$(\d)/,function(x,y){return v[y]
}))
}else{w.replace(u)
}}})
})
}else{l(o);
var t=m(o,q,o.getCursor());
function s(){var w=t.from(),v;
if(!(v=t.findNext())){t=m(o,q);
if(!(v=t.findNext())||(w&&t.from().line==w.line&&t.from().ch==w.ch)){return
}}o.setSelection(t.from(),t.to());
n(o,d,"Replace?",[function(){r(v)
},s])
}function r(v){t.replace(typeof q=="string"?u:u.replace(/\$(\d)/,function(x,y){return v[y]
}));
s()
}s()
}})
})
}CodeMirror.commands.find=function(o){l(o);
i(o)
};
CodeMirror.commands.findNext=i;
CodeMirror.commands.findPrev=function(o){i(o,true)
};
CodeMirror.commands.clearSearch=l;
CodeMirror.commands.replace=a;
CodeMirror.commands.replaceAll=function(o){a(o,true)
}
})();