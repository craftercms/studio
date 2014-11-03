(function(){tinymce.create("tinymce.plugins.WordCount",{block:0,id:null,countre:null,cleanre:null,init:function(n,m){var l=this,k=0,j=tinymce.VK;
l.countre=n.getParam("wordcount_countregex",/[\w\u2019\'-]+/g);
l.cleanre=n.getParam("wordcount_cleanregex",/[0-9.(),;:!?%#$?\'\"_+=\\\/-]*/g);
l.update_rate=n.getParam("wordcount_update_rate",2000);
l.update_on_delete=n.getParam("wordcount_update_on_delete",false);
l.id=n.id+"-word-count";
n.onPostRender.add(function(c,d){var b,a;
a=c.getParam("wordcount_target_id");
if(!a){b=tinymce.DOM.get(c.id+"_path_row");
if(b){tinymce.DOM.add(b.parentNode,"div",{style:"float: right"},c.getLang("wordcount.words","Words: ")+'<span id="'+l.id+'">0</span>')
}}else{tinymce.DOM.add(a,"span",{},'<span id="'+l.id+'">0</span>')
}});
n.onInit.add(function(a){a.selection.onSetContent.add(function(){l._count(a)
});
l._count(a)
});
n.onSetContent.add(function(a){l._count(a)
});
function h(a){return a!==k&&(a===j.ENTER||k===j.SPACEBAR||i(k))
}function i(a){return a===j.DELETE||a===j.BACKSPACE
}n.onKeyUp.add(function(b,a){if(h(a.keyCode)||l.update_on_delete&&i(a.keyCode)){l._count(b)
}k=a.keyCode
})
},_getCount:function(h){var f=0;
var e=h.getContent({format:"raw"});
if(e){e=e.replace(/\.\.\./g," ");
e=e.replace(/<.[^<>]*?>/g," ").replace(/&nbsp;|&#160;/gi," ");
e=e.replace(/(\w+)(&.+?;)+(\w+)/,"$1$3").replace(/&.+?;/g," ");
e=e.replace(this.cleanre,"");
var g=e.match(this.countre);
if(g){f=g.length
}}return f
},_count:function(d){var c=this;
if(c.block){return
}c.block=1;
setTimeout(function(){if(!d.destroyed){var a=c._getCount(d);
tinymce.DOM.setHTML(c.id,a.toString());
setTimeout(function(){c.block=0
},c.update_rate)
}},1)
},getInfo:function(){return{longname:"Word Count plugin",author:"Moxiecode Systems AB",authorurl:"http://tinymce.moxiecode.com",infourl:"http://wiki.moxiecode.com/index.php/TinyMCE:Plugins/wordcount",version:tinymce.majorVersion+"."+tinymce.minorVersion}
}});
tinymce.PluginManager.add("wordcount",tinymce.plugins.WordCount)
})();