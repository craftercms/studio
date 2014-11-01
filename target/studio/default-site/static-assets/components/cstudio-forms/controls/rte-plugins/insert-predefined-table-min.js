CStudioForms.Controls.RTE.InsertPredefinedTable=CStudioForms.Controls.RTE.InsertPredefinedTable||{createControl:function(b,a){var d=a.editor.contextControl.rteTables;
d=(d===undefined)?[]:(Array.isArray(d))?d:[d.table];
if(b=="predefinedTable"&&d&&d.length){var e=a.createMenuButton("predefinedTable",{title:"Insert predefined table",image:CStudioAuthoringContext.authoringAppBaseUri+"/themes/cstudioTheme/images/icons/predefined-table.png",icons:false});
e.onRenderMenu.add(function(h,g){var f=function(c){this.add({title:c.name,onclick:function(){tinyMCE.activeEditor.execCommand("mceInsertContent",false,c.prototype)
}})
};
d.forEach(f,g)
});
return e
}return null
},getInfo:function(){return{longname:"Crafter Studio Insert Predefined Table",author:"Crafter Software",authorurl:"http://www.craftercms.org",infourl:"http://www.craftercms.org",version:"1.0"}
}};
tinymce.create("tinymce.plugins.CStudioManagedPredefinedTablePlugin",CStudioForms.Controls.RTE.InsertPredefinedTable);
tinymce.PluginManager.add("insertpredefinedtable",tinymce.plugins.CStudioManagedPredefinedTablePlugin);
CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-rte-insert-predefined-table",CStudioForms.Controls.RTE.InsertPredefinedTable);