(function(){tinymce.create("tinymce.plugins.Directionality",{init:function(e,h){var g=this;
g.editor=e;
function f(d){var a=e.dom,b,c=e.selection.getSelectedBlocks();
if(c.length){b=a.getAttrib(c[0],"dir");
tinymce.each(c,function(j){if(!a.getParent(j.parentNode,"*[dir='"+d+"']",a.getRoot())){if(b!=d){a.setAttrib(j,"dir",d)
}else{a.setAttrib(j,"dir",null)
}}});
e.nodeChanged()
}}e.addCommand("mceDirectionLTR",function(){f("ltr")
});
e.addCommand("mceDirectionRTL",function(){f("rtl")
});
e.addButton("ltr",{title:"directionality.ltr_desc",cmd:"mceDirectionLTR"});
e.addButton("rtl",{title:"directionality.rtl_desc",cmd:"mceDirectionRTL"});
e.onNodeChange.add(g._nodeChange,g)
},getInfo:function(){return{longname:"Directionality",author:"Moxiecode Systems AB",authorurl:"http://tinymce.moxiecode.com",infourl:"http://wiki.moxiecode.com/index.php/TinyMCE:Plugins/directionality",version:tinymce.majorVersion+"."+tinymce.minorVersion}
},_nodeChange:function(f,g,h){var i=f.dom,j;
h=i.getParent(h,i.isBlock);
if(!h){g.setDisabled("ltr",1);
g.setDisabled("rtl",1);
return
}j=i.getAttrib(h,"dir");
g.setActive("ltr",j=="ltr");
g.setDisabled("ltr",0);
g.setActive("rtl",j=="rtl");
g.setDisabled("rtl",0)
}});
tinymce.PluginManager.add("directionality",tinymce.plugins.Directionality)
})();