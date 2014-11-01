tinyMCEPopup.requireLangPack();
tinyMCEPopup.onInit.add(onLoadInit);
function saveContent(){tinyMCEPopup.editor.setContent(document.getElementById("htmlSource").value,{source_view:true});
tinyMCEPopup.close()
}function onLoadInit(){tinyMCEPopup.resizeToInnerSize();
if(tinymce.isGecko){document.body.spellcheck=tinyMCEPopup.editor.getParam("gecko_spellcheck")
}document.getElementById("htmlSource").value=tinyMCEPopup.editor.getContent({source_view:true});
if(tinyMCEPopup.editor.getParam("theme_advanced_source_editor_wrap",true)){turnWrapOn();
document.getElementById("wraped").checked=true
}resizeInputs()
}function setWrap(c){var a,d,b=document.getElementById("htmlSource");
b.wrap=c;
if(!tinymce.isIE){a=b.value;
d=b.cloneNode(false);
d.setAttribute("wrap",c);
b.parentNode.replaceChild(d,b);
d.value=a
}}function setWhiteSpaceCss(b){var a=document.getElementById("htmlSource");
tinymce.DOM.setStyle(a,"white-space",b)
}function turnWrapOff(){if(tinymce.isWebKit){setWhiteSpaceCss("pre")
}else{setWrap("off")
}}function turnWrapOn(){if(tinymce.isWebKit){setWhiteSpaceCss("pre-wrap")
}else{setWrap("soft")
}}function toggleWordWrap(a){if(a.checked){turnWrapOn()
}else{turnWrapOff()
}}function resizeInputs(){var a=tinyMCEPopup.dom.getViewPort(window),b;
b=document.getElementById("htmlSource");
if(b){b.style.width=(a.w-20)+"px";
b.style.height=(a.h-65)+"px"
}};