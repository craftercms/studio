(function(){tinymce.create("tinymce.plugins.VisualBlocks",{init:function(e,d){var f;
if(!window.NodeList){return
}e.addCommand("mceVisualBlocks",function(){var a=e.dom,b;
if(!f){f=a.uniqueId();
b=a.create("link",{id:f,rel:"stylesheet",href:d+"/css/visualblocks.css"});
e.getDoc().getElementsByTagName("head")[0].appendChild(b)
}else{b=a.get(f);
b.disabled=!b.disabled
}e.controlManager.setActive("visualblocks",!b.disabled)
});
e.addButton("visualblocks",{title:"visualblocks.desc",cmd:"mceVisualBlocks"});
e.onInit.add(function(){if(e.settings.visualblocks_default_state){e.execCommand("mceVisualBlocks",false,null,{skip_focus:true})
}})
},getInfo:function(){return{longname:"Visual blocks",author:"Moxiecode Systems AB",authorurl:"http://tinymce.moxiecode.com",infourl:"http://wiki.moxiecode.com/index.php/TinyMCE:Plugins/visualblocks",version:tinymce.majorVersion+"."+tinymce.minorVersion}
}});
tinymce.PluginManager.add("visualblocks",tinymce.plugins.VisualBlocks)
})();