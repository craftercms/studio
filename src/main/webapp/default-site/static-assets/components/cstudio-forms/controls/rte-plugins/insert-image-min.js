CStudioForms.Controls.RTE.ImageInsert=CStudioForms.Controls.RTE.ImageInsert||{init:function(b,c){var a=this;
var d={beforeSave:function(){var h=b.dom.doc.body;
if(h){var e=h.getElementsByTagName("img");
if(e.length>0){for(var g=--e.length;
g>=0;
g--){var j=e[g];
var f=a.cleanUrl(j.src);
j.setAttribute("src",f);
j.setAttribute("data-mce-src",f)
}b.contextControl.save()
}}},editor:b};
b.contextControl.form.registerBeforeSaveCallback(d);
b.addCommand("mceInsertManagedImage",function(f,e){if(e){if(e.insertImageAction){e.insertImageAction({success:function(g){b.execCommand("mceInsertContent",false,'<img src="'+g.previewUrl+'" />');
b.contextControl.save()
},failure:function(g){alert(g)
}})
}else{alert("The configured datasource is not an image manager")
}}else{alert("No datasource has been associated with this editor")
}});
b.onNodeChange.add(function(f,e,g){e.setActive("managedImage",g.nodeName=="IMG")
})
},cleanUrl:function(a){var b=a.replace(CStudioAuthoringContext.previewAppBaseUri,"");
return b
},createControl:function(d,a){var b=a.editor.contextControl.imageManagerName;
b=(!b)?"":(Array.isArray(b))?b.join(","):b;
if(d=="managedImage"&&b){var e=a.createMenuButton("managedImage",{title:"Insert Image",image:CStudioAuthoringContext.authoringAppBaseUri+"/themes/cstudioTheme/images/insert_image.png",icons:false});
e.onRenderMenu.add(function(j,g){var i=this.editor.contextControl.form.datasourceMap,h=this.editor.contextControl.form.definition.datasources;
var f=function(k){var c=new RegExp("("+k.id+")[\\s,]|("+k.id+")$"),l;
if(b.search(c)>-1){l=i[k.id];
this.add({title:k.title,onclick:function(){tinyMCE.activeEditor.execCommand("mceInsertManagedImage",false,l)
}})
}};
h.forEach(f,g)
});
return e
}return null
},getInfo:function(){return{longname:"Crafter Studio Insert Image",author:"Crafter Software",authorurl:"http://www.craftercms.org",infourl:"http://www.craftercms.org",version:"1.0"}
}};
tinymce.create("tinymce.plugins.CStudioManagedImagePlugin",CStudioForms.Controls.RTE.ImageInsert);
tinymce.PluginManager.add("insertimage",tinymce.plugins.CStudioManagedImagePlugin);
CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-rte-insert-image",CStudioForms.Controls.RTE.ImageInsert);