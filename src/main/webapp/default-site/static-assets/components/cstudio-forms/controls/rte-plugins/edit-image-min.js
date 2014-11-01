CStudioForms.Controls.RTE.ImageEditor=CStudioForms.Controls.RTE.ImageEditor||(function(){var b=function(){if(a.isSet()){if(tinymce.activeEditor.contextControl.forceImageAlts==true){var c=document.getElementById("rteImageAltText"),d=c.value.replace(/^\s\s*/,"").replace(/\s\s*$/,"");
if(!d){alert("Image description is required.")
}else{a.hide()
}}else{a.hide()
}}};
var a={isSet:function(){var c=document.getElementById("rte-image-properties");
return(c&&!YAHOO.util.Dom.hasClass(c,"hidden"))
},hide:function(){var c=document.getElementById("rte-image-properties");
if(c){YAHOO.util.Dom.addClass(c,"hidden")
}},renderImageEdit:function(f,v){var l=f.contextControl,j=document.getElementById("rte-image-properties");
var w=function w(F){var E=document.getElementById("rteImageTopMargin");
var D=document.getElementById("rteImageRightMargin");
var B=document.getElementById("rteImageBottomMargin");
var C=document.getElementById("rteImageLeftMargin");
E.value=F.style.marginTop.replace("px","");
D.value=F.style.marginRight.replace("px","");
B.value=F.style.marginBottom.replace("px","");
C.value=F.style.marginLeft.replace("px","")
};
if(!j){j=document.createElement("div");
j.id="rte-image-properties";
YAHOO.util.Dom.addClass(j,"seethrough");
YAHOO.util.Dom.addClass(j,"rte-panel");
document.body.appendChild(j)
}else{YAHOO.util.Dom.removeClass(j,"hidden");
j.innerHTML=""
}var z=document.createElement("div");
j.appendChild(z);
var y=document.createElement("div");
YAHOO.util.Dom.addClass(y,"rte-image-prop-size-container");
j.appendChild(y);
var c="<table><tr><td>Height</td><td><input id='rteImageHeight'/></td></tr><tr><td>Width</td><td><input id='rteImageWidth'/></td></tr><tr class='img-flow'><td>Text Flow</td><td><div id='rteImageAlignNone' title='Display inline image'></div><div id='rteImageAlignLeft' title='Float image to the left'></div><div id='rteImageAlignRight' title='Float image to the right'></div><div id='rteImageAlignCenter' title='Display block image centered'></div></td></tr></table>";
y.innerHTML=c;
var p=document.getElementById("rteImageHeight");
YAHOO.util.Dom.addClass(p,"rte-image-prop-size-input");
p.value=v.height;
YAHOO.util.Event.on(p,"keyup",function(){v.height=p.value
});
var e=document.getElementById("rteImageWidth");
YAHOO.util.Dom.addClass(e,"rte-image-prop-size-input");
e.value=v.width;
YAHOO.util.Event.on(e,"keyup",function(){v.width=e.value
});
var r=document.getElementById("rteImageAlignNone");
var g=document.getElementById("rteImageAlignLeft");
var k=document.getElementById("rteImageAlignRight");
var A=document.getElementById("rteImageAlignCenter");
var x=function(){YAHOO.util.Dom.removeClass(r,"rte-image-prop-align-selected");
YAHOO.util.Dom.removeClass(g,"rte-image-prop-align-selected");
YAHOO.util.Dom.removeClass(k,"rte-image-prop-align-selected");
YAHOO.util.Dom.removeClass(A,"rte-image-prop-align-selected");
if(v.align=="none"||v.align==""){YAHOO.util.Dom.addClass(r,"rte-image-prop-align-selected")
}if(v.align=="left"){YAHOO.util.Dom.addClass(g,"rte-image-prop-align-selected")
}if(v.align=="right"){YAHOO.util.Dom.addClass(k,"rte-image-prop-align-selected")
}if(v.align=="middle"){YAHOO.util.Dom.addClass(A,"rte-image-prop-align-selected")
}};
YAHOO.util.Dom.addClass(r,"rte-image-prop-align");
YAHOO.util.Dom.addClass(r,"rte-image-prop-align-none");
YAHOO.util.Event.on(r,"click",function(){v.align="none";
CStudioForms.Controls.RTE.ImageEditor.setStyleStr(f,v,"display","inline");
CStudioForms.Controls.RTE.ImageEditor.setStyleStr(f,v,"margin","");
w(v);
x()
});
YAHOO.util.Dom.addClass(g,"rte-image-prop-align");
YAHOO.util.Dom.addClass(g,"rte-image-prop-align-left");
YAHOO.util.Event.on(g,"click",function(){v.align="left";
CStudioForms.Controls.RTE.ImageEditor.setStyleStr(f,v,"display","inline");
CStudioForms.Controls.RTE.ImageEditor.setStyleStr(f,v,"margin","");
w(v);
x()
});
YAHOO.util.Dom.addClass(k,"rte-image-prop-align");
YAHOO.util.Dom.addClass(k,"rte-image-prop-align-right");
YAHOO.util.Event.on(k,"click",function(){v.align="right";
CStudioForms.Controls.RTE.ImageEditor.setStyleStr(f,v,"display","inline");
CStudioForms.Controls.RTE.ImageEditor.setStyleStr(f,v,"margin","");
w(v);
x()
});
YAHOO.util.Dom.addClass(A,"rte-image-prop-align");
YAHOO.util.Dom.addClass(A,"rte-image-prop-align-center");
YAHOO.util.Event.on(A,"click",function(){v.align="middle";
CStudioForms.Controls.RTE.ImageEditor.setStyleStr(f,v,"display","block");
CStudioForms.Controls.RTE.ImageEditor.setStyleStr(f,v,"margin","0 auto");
w(v);
x()
});
x();
var u=document.createElement("div");
YAHOO.util.Dom.addClass(u,"rte-image-prop-layout-container");
j.appendChild(u);
var n="<table><tr><td colspan='3'>Margin</td></tr><tr><td></td> <td><input id='rteImageTopMargin'/></td> <td></td><tr><td><input id='rteImageLeftMargin'/> </td><td><div id='rteImagePreview'></div></td><td><input id='rteImageRightMargin'/></td></tr><tr><td></td> <td><input id='rteImageBottomMargin'/></td> <td></td></tr></table>";
u.innerHTML=n;
var m=document.getElementById("rteImagePreview");
YAHOO.util.Dom.addClass(m,"rte-image-prop-layout-image");
m.innerHTML="<img width='50px' height='50px' src='"+v.src+"'>";
var s=document.getElementById("rteImageTopMargin");
YAHOO.util.Dom.addClass(s,"rte-image-prop-layout-input");
YAHOO.util.Dom.addClass(s,"rte-image-prop-layout-topm");
s.value=v.style.marginTop.replace("px","");
YAHOO.util.Event.on(s,"keyup",function(){CStudioForms.Controls.RTE.ImageEditor.setStyleStr(f,v,"margin-top",s.value+"px")
});
var h=document.getElementById("rteImageBottomMargin");
YAHOO.util.Dom.addClass(h,"rte-image-prop-layout-input");
YAHOO.util.Dom.addClass(h,"rte-image-prop-layout-bottomm");
h.value=v.style.marginBottom.replace("px","");
YAHOO.util.Event.on(h,"keyup",function(){CStudioForms.Controls.RTE.ImageEditor.setStyleStr(f,v,"margin-bottom",h.value+"px")
});
var q=document.getElementById("rteImageLeftMargin");
YAHOO.util.Dom.addClass(q,"rte-image-prop-layout-input");
YAHOO.util.Dom.addClass(q,"rte-image-prop-layout-leftm");
q.value=v.style.marginLeft.replace("px","");
YAHOO.util.Event.on(q,"keyup",function(){CStudioForms.Controls.RTE.ImageEditor.setStyleStr(f,v,"margin-left",q.value+"px")
});
var t=document.getElementById("rteImageRightMargin");
YAHOO.util.Dom.addClass(t,"rte-image-prop-layout-input");
YAHOO.util.Dom.addClass(t,"rte-image-prop-layout-rightm");
t.value=v.style.marginRight.replace("px","");
YAHOO.util.Event.on(t,"keyup",function(){CStudioForms.Controls.RTE.ImageEditor.setStyleStr(f,v,"margin-right",t.value+"px")
});
var i=document.createElement("div");
YAHOO.util.Dom.addClass(i,"rte-image-prop-altlink-container");
j.appendChild(i);
var d="<table><tr><td>Description</td> <td><input id='rteImageAltText'/></td></tr></table>";
i.innerHTML=d;
var o=document.getElementById("rteImageAltText");
o.value=v.alt;
YAHOO.util.Dom.addClass(o,"rte-image-prop-altlink-alttext");
YAHOO.util.Event.on(o,"keyup",function(){v.alt=o.value
})
},setStyleStr:function(e,c,d,f){tinymce.DOM.setStyle(c,d,f);
var g=typeof(c.getAttribute("style"))!=="string"?c.style.cssText:c.getAttribute("style");
c.getAttribute("style").value=g;
c.setAttribute("data-mce-style",g);
e.contextControl.save()
}};
amplify.subscribe("/rte/clicked",b);
amplify.subscribe("/rte/blurred",a.hide);
return a
})();
CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-rte-edit-image",CStudioForms.Controls.RTE.ImageEditor);