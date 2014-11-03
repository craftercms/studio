(function(){tinymce.create("tinymce.plugins.StylePlugin",{init:function(d,c){d.addCommand("mceStyleProps",function(){var g=false;
var a=d.selection.getSelectedBlocks();
var b=[];
if(a.length===1){b.push(d.selection.getNode().style.cssText)
}else{tinymce.each(a,function(f){b.push(d.dom.getAttrib(f,"style"))
});
g=true
}d.windowManager.open({file:c+"/props.htm",width:480+parseInt(d.getLang("style.delta_width",0)),height:340+parseInt(d.getLang("style.delta_height",0)),inline:1},{applyStyleToBlocks:g,plugin_url:c,styles:b})
});
d.addCommand("mceSetElementStyle",function(a,b){if(e=d.selection.getNode()){d.dom.setAttrib(e,"style",b);
d.execCommand("mceRepaint")
}});
d.onNodeChange.add(function(b,g,a){g.setDisabled("styleprops",a.nodeName==="BODY")
});
d.addButton("styleprops",{title:"style.desc",cmd:"mceStyleProps"})
},getInfo:function(){return{longname:"Style",author:"Moxiecode Systems AB",authorurl:"http://tinymce.moxiecode.com",infourl:"http://wiki.moxiecode.com/index.php/TinyMCE:Plugins/style",version:tinymce.majorVersion+"."+tinymce.minorVersion}
}});
tinymce.PluginManager.add("style",tinymce.plugins.StylePlugin)
})();