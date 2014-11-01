tinyMCEPopup.requireLangPack();
var LinkDialog={preInit:function(){var b;
if(b=tinyMCEPopup.getParam("external_link_list_url")){document.write('<script language="javascript" type="text/javascript" src="'+tinyMCEPopup.editor.documentBaseURI.toAbsolute(b)+'"><\/script>')
}},init:function(){var c=document.forms[0],d=tinyMCEPopup.editor;
document.getElementById("hrefbrowsercontainer").innerHTML=getBrowserHTML("hrefbrowser","href","file","theme_advanced_link");
if(isVisible("hrefbrowser")){document.getElementById("href").style.width="180px"
}this.fillClassList("class_list");
this.fillFileList("link_list","tinyMCELinkList");
this.fillTargetList("target_list");
if(e=d.dom.getParent(d.selection.getNode(),"A")){c.href.value=d.dom.getAttrib(e,"href");
c.linktitle.value=d.dom.getAttrib(e,"title");
c.insert.value=d.getLang("update");
selectByValue(c,"link_list",c.href.value);
selectByValue(c,"target_list",d.dom.getAttrib(e,"target"));
selectByValue(c,"class_list",d.dom.getAttrib(e,"class"))
}},update:function(){var i=document.forms[0],j=tinyMCEPopup.editor,f,b,k=i.href.value.replace(/ /g,"%20");
tinyMCEPopup.restoreSelection();
f=j.dom.getParent(j.selection.getNode(),"A");
if(!i.href.value){if(f){b=j.selection.getBookmark();
j.dom.remove(f,1);
j.selection.moveToBookmark(b);
tinyMCEPopup.execCommand("mceEndUndoLevel");
tinyMCEPopup.close();
return
}}if(f==null){j.getDoc().execCommand("unlink",false,null);
tinyMCEPopup.execCommand("mceInsertLink",false,"#mce_temp_url#",{skip_undo:1});
tinymce.each(j.dom.select("a"),function(a){if(j.dom.getAttrib(a,"href")=="#mce_temp_url#"){f=a;
j.dom.setAttribs(f,{href:k,title:i.linktitle.value,target:i.target_list?getSelectValue(i,"target_list"):null,"class":i.class_list?getSelectValue(i,"class_list"):null})
}})
}else{j.dom.setAttribs(f,{href:k,title:i.linktitle.value,target:i.target_list?getSelectValue(i,"target_list"):null,"class":i.class_list?getSelectValue(i,"class_list"):null})
}if(f.childNodes.length!=1||f.firstChild.nodeName!="IMG"){j.focus();
j.selection.select(f);
j.selection.collapse(0);
tinyMCEPopup.storeSelection()
}tinyMCEPopup.execCommand("mceEndUndoLevel");
tinyMCEPopup.close()
},checkPrefix:function(b){if(b.value&&Validator.isEmail(b)&&!/^\s*mailto:/i.test(b.value)&&confirm(tinyMCEPopup.getLang("advanced_dlg.link_is_email"))){b.value="mailto:"+b.value
}if(/^\s*www\./i.test(b.value)&&confirm(tinyMCEPopup.getLang("advanced_dlg.link_is_external"))){b.value="http://"+b.value
}},fillFileList:function(j,m){var k=tinyMCEPopup.dom,i=k.get(j),l,h;
m=window[m];
if(m&&m.length>0){i.options[i.options.length]=new Option("","");
tinymce.each(m,function(a){i.options[i.options.length]=new Option(a[0],a[1])
})
}else{k.remove(k.getParent(j,"tr"))
}},fillClassList:function(i){var k=tinyMCEPopup.dom,h=k.get(i),j,g=[];
if(j=tinyMCEPopup.editor.contextControl){g=j.rteLinkStyles||[]
}if(g.length>0){h.options[h.options.length]=new Option(tinyMCEPopup.getLang("not_set"),"");
tinymce.each(g,function(a){h.options[h.options.length]=new Option(a.name,a.value)
})
}else{k.remove(k.getParent(i,"tr"))
}},fillTargetList:function(i){var k=tinyMCEPopup.dom,h=k.get(i),g=[],j;
if(j=tinyMCEPopup.editor.contextControl){g=j.rteLinkTargets||[]
}if(g.length>0){tinymce.each(g,function(a){h.options[h.options.length]=new Option(a.name,a.value)
})
}else{h.options[h.options.length]=new Option(tinyMCEPopup.getLang("not_set"),"");
h.options[h.options.length]=new Option(tinyMCEPopup.getLang("advanced_dlg.link_target_same"),"_self");
h.options[h.options.length]=new Option(tinyMCEPopup.getLang("advanced_dlg.link_target_blank"),"_blank")
}}};
LinkDialog.preInit();
tinyMCEPopup.onInit.add(LinkDialog.init,LinkDialog);