(function(){var d=2;
function c(){this.marked=[]
}function a(f){return f._matchHighlightState||(f._matchHighlightState=new c())
}function e(f){var h=a(f);
for(var g=0;
g<h.marked.length;
++g){h.marked[g].clear()
}h.marked=[]
}function b(f,g,h){e(f);
h=(typeof h!=="undefined"?h:d);
if(f.somethingSelected()&&f.getSelection().replace(/^\s+|\s+$/g,"").length>=h){var j=a(f);
var i=f.getSelection();
f.operation(function(){if(f.lineCount()<2000){for(var k=f.getSearchCursor(i);
k.findNext();
){if(!(k.from().line===f.getCursor(true).line&&k.from().ch===f.getCursor(true).ch)){j.marked.push(f.markText(k.from(),k.to(),g))
}}}})
}}CodeMirror.defineExtension("matchHighlight",function(f,g){b(this,f,g)
})
})();