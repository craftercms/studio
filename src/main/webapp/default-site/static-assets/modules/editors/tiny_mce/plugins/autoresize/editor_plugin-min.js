(function(){tinymce.create("tinymce.plugins.AutoResizePlugin",{init:function(g,j){var i=this,h=0;
if(g.getParam("fullscreen_is_enabled")){return
}function f(){var c,d=g.getDoc(),n=d.body,a=d.documentElement,e=tinymce.DOM,b=i.autoresize_min_height,m;
m=tinymce.isIE?n.scrollHeight:(tinymce.isWebKit&&n.clientHeight==0?0:n.offsetHeight);
if(m>i.autoresize_min_height){b=m
}if(i.autoresize_max_height&&m>i.autoresize_max_height){b=i.autoresize_max_height;
n.style.overflowY="auto";
a.style.overflowY="auto"
}else{n.style.overflowY="hidden";
a.style.overflowY="hidden";
n.scrollTop=0
}if(b!==h){c=b-h;
e.setStyle(e.get(g.id+"_ifr"),"height",b+"px");
h=b;
if(tinymce.isWebKit&&c<0){f()
}}}i.editor=g;
i.autoresize_min_height=parseInt(g.getParam("autoresize_min_height",g.getElement().offsetHeight));
i.autoresize_max_height=parseInt(g.getParam("autoresize_max_height",0));
g.onInit.add(function(a){a.dom.setStyle(a.getBody(),"paddingBottom",a.getParam("autoresize_bottom_margin",50)+"px")
});
g.onChange.add(f);
g.onSetContent.add(f);
g.onPaste.add(f);
g.onKeyUp.add(f);
g.onPostRender.add(f);
if(g.getParam("autoresize_on_init",true)){g.onLoad.add(f);
g.onLoadContent.add(f)
}g.addCommand("mceAutoResize",f)
},getInfo:function(){return{longname:"Auto Resize",author:"Moxiecode Systems AB",authorurl:"http://tinymce.moxiecode.com",infourl:"http://wiki.moxiecode.com/index.php/TinyMCE:Plugins/autoresize",version:tinymce.majorVersion+"."+tinymce.minorVersion}
}});
tinymce.PluginManager.add("autoresize",tinymce.plugins.AutoResizePlugin)
})();