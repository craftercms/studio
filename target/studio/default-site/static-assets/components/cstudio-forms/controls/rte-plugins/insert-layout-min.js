CStudioForms.Controls.RTE.InsertLayout=CStudioForms.Controls.RTE.InsertLayout||{createControl:function(g,a,d){switch(g){case"insertLayout":var b=tinyMCE.activeEditor.contextControl.rteConfig;
var e=b.rteLayouts;
if(!e){e=[]
}if(!e.length){e=[e.layout]
}if(e.length>0){var f=a.createMenuButton("insertLayout",{title:"Insert Layout",style:"mce_insertLayout",});
f.layouts=e;
f.onRenderMenu.add(function(p,j){for(var l=0;
l<e.length;
l++){var o=e[l];
var k=o.prototype;
var h=function(){tinyMCE.activeEditor.execCommand("mceInsertContent",false,this.layoutPrototype);
ed.contextControl.save()
};
var n={title:o.name,onclick:h,layoutPrototype:k};
j.add(n)
}});
return f
}else{}}return null
}};
tinymce.create("tinymce.plugins.CStudioInsertLayoutPlugin",CStudioForms.Controls.RTE.InsertLayout);
tinymce.PluginManager.add("insertlayout",tinymce.plugins.CStudioInsertLayoutPlugin);
CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-rte-insert-layout",CStudioForms.Controls.RTE.InsertLayout);