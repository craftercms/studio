tinyMCEPopup.requireLangPack();
var LinkDialog={preInit:function(){var a;
if(a=tinyMCEPopup.getParam("external_link_list_url")){document.write('<script language="javascript" type="text/javascript" src="'+tinyMCEPopup.editor.documentBaseURI.toAbsolute(a)+'"><\/script>')
}},init:function(){var b=document.forms[0],a=tinyMCEPopup.editor;
document.getElementById("hrefbrowsercontainer").innerHTML=getBrowserHTML("hrefbrowser","href","file","theme_advanced_link");
if(isVisible("hrefbrowser")){document.getElementById("href").style.width="180px"
}this.fillClassList("class_list");
this.fillFileList("link_list","tinyMCELinkList");
this.fillTargetList("target_list");
if(e=a.dom.getParent(a.selection.getNode(),"A")){b.href.value=a.dom.getAttrib(e,"href");
b.linktitle.value=a.dom.getAttrib(e,"title");
b.insert.value=a.getLang("update");
selectByValue(b,"link_list",b.href.value);
selectByValue(b,"target_list",a.dom.getAttrib(e,"target"));
selectByValue(b,"class_list",a.dom.getAttrib(e,"class"))
}},update:function(){var g=document.forms[0],d=tinyMCEPopup.editor,h,a,c=g.href.value.replace(/ /g,"%20");
tinyMCEPopup.restoreSelection();
h=d.dom.getParent(d.selection.getNode(),"A");
if(!g.href.value){if(h){a=d.selection.getBookmark();
d.dom.remove(h,1);
d.selection.moveToBookmark(a);
tinyMCEPopup.execCommand("mceEndUndoLevel");
tinyMCEPopup.close();
return
}}if(h==null){d.getDoc().execCommand("unlink",false,null);
tinyMCEPopup.execCommand("mceInsertLink",false,"#mce_temp_url#",{skip_undo:1});
tinymce.each(d.dom.select("a"),function(b){if(d.dom.getAttrib(b,"href")=="#mce_temp_url#"){h=b;
d.dom.setAttribs(h,{href:c,title:g.linktitle.value,target:g.target_list?getSelectValue(g,"target_list"):null,"class":g.class_list?getSelectValue(g,"class_list"):null})
}})
}else{d.dom.setAttribs(h,{href:c,title:g.linktitle.value,target:g.target_list?getSelectValue(g,"target_list"):null,"class":g.class_list?getSelectValue(g,"class_list"):null})
}if(h.childNodes.length!=1||h.firstChild.nodeName!="IMG"){d.focus();
d.selection.select(h);
d.selection.collapse(0);
tinyMCEPopup.storeSelection()
}tinyMCEPopup.execCommand("mceEndUndoLevel");
tinyMCEPopup.close()
},checkPrefix:function(a){if(a.value&&Validator.isEmail(a)&&!/^\s*mailto:/i.test(a.value)&&confirm(tinyMCEPopup.getLang("advanced_dlg.link_is_email"))){a.value="mailto:"+a.value
}if(/^\s*www\./i.test(a.value)&&confirm(tinyMCEPopup.getLang("advanced_dlg.link_is_external"))){a.value="http://"+a.value
}},fillFileList:function(g,c){var f=tinyMCEPopup.dom,a=f.get(g),d,b;
c=window[c];
if(c&&c.length>0){a.options[a.options.length]=new Option("","");
tinymce.each(c,function(h){a.options[a.options.length]=new Option(h[0],h[1])
})
}else{f.remove(f.getParent(g,"tr"))
}},fillClassList:function(f){var c=tinyMCEPopup.dom,a=c.get(f),d,b=[];
if(d=tinyMCEPopup.editor.contextControl){b=d.rteLinkStyles||[]
}if(b.length>0){a.options[a.options.length]=new Option(tinyMCEPopup.getLang("not_set"),"");
tinymce.each(b,function(g){a.options[a.options.length]=new Option(g.name,g.value)
})
}else{c.remove(c.getParent(f,"tr"))
}},fillTargetList:function(f){var c=tinyMCEPopup.dom,a=c.get(f),b=[],d;
if(d=tinyMCEPopup.editor.contextControl){b=d.rteLinkTargets||[]
}if(b.length>0){tinymce.each(b,function(g){a.options[a.options.length]=new Option(g.name,g.value)
})
}else{a.options[a.options.length]=new Option(tinyMCEPopup.getLang("not_set"),"");
a.options[a.options.length]=new Option(tinyMCEPopup.getLang("advanced_dlg.link_target_same"),"_self");
a.options[a.options.length]=new Option(tinyMCEPopup.getLang("advanced_dlg.link_target_blank"),"_blank")
}}};
LinkDialog.preInit();
tinyMCEPopup.onInit.add(LinkDialog.init,LinkDialog);