tinyMCEPopup.requireLangPack();
var EmotionsDialog={addKeyboardNavigation:function(){var a,b,c;
b=tinyMCEPopup.dom.select("a.emoticon_link","emoticon_table");
c={root:"emoticon_table",items:b};
b[0].tabindex=0;
tinyMCEPopup.dom.addClass(b[0],"mceFocus");
if(tinymce.isGecko){b[0].focus()
}else{setTimeout(function(){b[0].focus()
},100)
}tinyMCEPopup.editor.windowManager.createInstance("tinymce.ui.KeyboardNavigation",c,tinyMCEPopup.dom)
},init:function(a){tinyMCEPopup.resizeToInnerSize();
this.addKeyboardNavigation()
},insert:function(b,d){var a=tinyMCEPopup.editor,c=a.dom;
tinyMCEPopup.execCommand("mceInsertContent",false,c.createHTML("img",{src:tinyMCEPopup.getWindowArg("plugin_url")+"/img/"+b,alt:a.getLang(d),title:a.getLang(d),border:0}));
tinyMCEPopup.close()
}};
tinyMCEPopup.onInit.add(EmotionsDialog.init,EmotionsDialog);