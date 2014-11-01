CStudioForms.Controls.RTE.ChannelSelect=CStudioForms.Controls.RTE.ChannelSelect||{createControl:function(b,g,f){switch(b){case"channel":var c=tinyMCE.activeEditor.contextControl.fieldConfig;
var e=null;
for(var d=0;
d<c.properties.length;
d++){var a=c.properties[d];
if(a.name=="supportedChannels"){if(a.value&&a.Value!=""){e=a.value
}}}if(e){if(e.length&&e.length>0){var h=g.createListBox("channelSelect",{title:"Channel",onselect:function(k){var l=k.split(":");
tinyMCE.activeEditor.contextControl._applyChannelStyleSheets(l[0]);
var i=document.getElementById(tinyMCE.activeEditor.id+"_ifr");
var m=document.getElementById(tinyMCE.activeEditor.id+"_tbl");
i.style.width=l[1]+"px";
m.style.width=l[1]+"px"
}});
var j=tinyMCE.activeEditor.contextControl.inputEl._width;
h.add("Default","default:"+j);
for(var d=0;
d<e.length;
d++){h.add(e[d].value,e[d].name+":"+e[d].size)
}return h
}else{}}else{}}return null
}};
tinymce.create("tinymce.plugins.CStudioChannelSelectPlugin",CStudioForms.Controls.RTE.ChannelSelect);
tinymce.PluginManager.add("channel",tinymce.plugins.CStudioChannelSelectPlugin);
CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-rte-channel",CStudioForms.Controls.RTE.ChannelSelect);