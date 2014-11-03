(function(){var c=tinymce.DOM;
var d=function(g,a,b){var h=function(j){var e=g.controlManager.get(j);
var f=a.controlManager.get(j);
if(e&&f){f.displayColor(e.value)
}};
h("forecolor");
h("backcolor");
a.setContent(g.getContent({format:"raw"}),{format:"raw"});
a.selection.moveToBookmark(b);
if(g.plugins.spellchecker&&a.plugins.spellchecker){a.plugins.spellchecker.setLanguage(g.plugins.spellchecker.selectedLang)
}};
tinymce.create("tinymce.plugins.FullScreenPlugin",{init:function(s,y){var p=this,b={},q=c.doc.documentElement,x,a,t,u,v,w,r;
s.addCommand("mceFullScreen",function(){var f,e;
if(s.getParam("fullscreen_is_enabled")){if(s.getParam("fullscreen_new_window")){closeFullscreen()
}else{c.win.setTimeout(function(){var h=s;
var i=tinyMCE.get(h.getParam("fullscreen_editor_id"));
i.plugins.fullscreen.saveState(h);
tinyMCE.remove(h)
},10)
}return
}if(s.getParam("fullscreen_new_window")){p.fullscreenSettings={bookmark:s.selection.getBookmark()};
f=c.win.open(y+"/fullscreen.htm","mceFullScreenPopup","fullscreen=yes,menubar=no,toolbar=no,scrollbars=no,resizable=yes,left=0,top=0,width="+screen.availWidth+",height="+screen.availHeight);
try{f.resizeTo(screen.availWidth,screen.availHeight)
}catch(g){}}else{a=c.getStyle(c.doc.body,"overflow",1)||"auto";
t=c.getStyle(q,"overflow",1);
x=c.getViewPort();
u=x.x;
v=x.y;
if(tinymce.isOpera&&a=="visible"){a="auto"
}if(tinymce.isIE&&a=="scroll"){a="auto"
}if(tinymce.isIE&&(t=="visible"||t=="scroll")){t="auto"
}if(a=="0px"){a=""
}c.setStyle(c.doc.body,"overflow","hidden");
q.style.overflow="hidden";
x=c.getViewPort();
c.win.scrollTo(0,0);
if(tinymce.isIE){x.h-=1
}if(tinymce.isIE6||document.compatMode=="BackCompat"){w="absolute;top:"+x.y
}else{w="fixed;top:0"
}n=c.add(c.doc.body,"div",{id:"mce_fullscreen_container",style:"position:"+w+";left:0;width:"+x.w+"px;height:"+x.h+"px;z-index:200000;"});
c.add(n,"div",{id:"mce_fullscreen"});
tinymce.each(s.settings,function(i,h){b[h]=i
});
b.id="mce_fullscreen";
b.width=n.clientWidth;
b.height=n.clientHeight-15;
b.fullscreen_is_enabled=true;
b.fullscreen_editor_id=s.id;
b.theme_advanced_resizing=false;
b.save_onsavecallback=function(){s.setContent(tinyMCE.get(b.id).getContent());
s.execCommand("mceSave")
};
tinymce.each(s.getParam("fullscreen_settings"),function(h,i){b[i]=h
});
p.fullscreenSettings={bookmark:s.selection.getBookmark(),fullscreen_overflow:a,fullscreen_html_overflow:t,fullscreen_scrollx:u,fullscreen_scrolly:v};
if(b.theme_advanced_toolbar_location==="external"){b.theme_advanced_toolbar_location="top"
}tinyMCE.oldSettings=tinyMCE.settings;
p.fullscreenEditor=new tinymce.Editor("mce_fullscreen",b);
p.fullscreenEditor.onInit.add(function(){p.loadState(p.fullscreenEditor)
});
p.fullscreenEditor.render();
p.fullscreenElement=new tinymce.dom.Element("mce_fullscreen_container");
p.fullscreenElement.update();
p.resizeFunc=tinymce.dom.Event.add(c.win,"resize",function(){var h=tinymce.DOM.getViewPort(),j=p.fullscreenEditor,k,i;
k=j.dom.getSize(j.getContainer().getElementsByTagName("table")[0]);
i=j.dom.getSize(j.getContainer().getElementsByTagName("iframe")[0]);
j.theme.resizeTo(h.w-k.w+i.w,h.h-k.h+i.h)
})
}});
s.addButton("fullscreen",{title:"fullscreen.desc",cmd:"mceFullScreen"});
s.onNodeChange.add(function(e,f){f.setActive("fullscreen",e.getParam("fullscreen_is_enabled"))
});
p.loadState=function(e){if(!(e&&p.fullscreenSettings)){throw"No fullscreen editor to load to"
}d(s,e,p.fullscreenSettings.bookmark);
e.focus()
};
p.saveState=function(e){if(!(e&&p.fullscreenSettings)){throw"No fullscreen editor to restore from"
}var f=p.fullscreenSettings;
d(e,s,e.selection.getBookmark());
if(!s.getParam("fullscreen_new_window")){tinymce.dom.Event.remove(c.win,"resize",p.resizeFunc);
delete p.resizeFunc;
c.remove("mce_fullscreen_container");
c.doc.documentElement.style.overflow=f.fullscreen_html_overflow;
c.setStyle(c.doc.body,"overflow",f.fullscreen_overflow);
c.win.scrollTo(f.fullscreen_scrollx,f.fullscreen_scrolly)
}tinyMCE.settings=tinyMCE.oldSettings;
delete tinyMCE.oldSettings;
delete p.fullscreenEditor;
delete p.fullscreenElement;
delete p.fullscreenSettings;
c.win.setTimeout(function(){s.selection.moveToBookmark(r);
s.focus()
},10)
}
},getInfo:function(){return{longname:"Fullscreen",author:"Moxiecode Systems AB",authorurl:"http://tinymce.moxiecode.com",infourl:"http://wiki.moxiecode.com/index.php/TinyMCE:Plugins/fullscreen",version:tinymce.majorVersion+"."+tinymce.minorVersion}
}});
tinymce.PluginManager.add("fullscreen",tinymce.plugins.FullScreenPlugin)
})();