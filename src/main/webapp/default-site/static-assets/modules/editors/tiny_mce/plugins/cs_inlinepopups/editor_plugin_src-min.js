(function(){var d=tinymce.DOM,b=tinymce.dom.Element,a=tinymce.dom.Event,e=tinymce.each,c=tinymce.is;
tinymce.create("tinymce.plugins.CStudioInlinePopups",{init:function(f,g){f.onBeforeRenderUI.add(function(){f.windowManager=new tinymce.InlineWindowManager(f);
d.loadCSS(g+"/skins/"+(f.settings.inlinepopups_skin||"clearlooks2")+"/window.css")
})
},getInfo:function(){return{longname:"Crafter Studio Inline Popups Customization",author:"Crafter Software",authorurl:"http://www.craftercms.org",infourl:"http://www.craftercms.org",version:"1.0"}
}});
tinymce.create("tinymce.InlineWindowManager:tinymce.WindowManager",{InlineWindowManager:function(f){var g=this;
g.parent(f);
g.zIndex=300000;
g.count=0;
g.windows={}
},open:function(B,v){var r=this,x,g="",k=r.editor,q=0,A=0,z,y,i,l,m,n,o,C,j,h,s;
B=B||{};
v=v||{};
if(!B.inline){return r.parent(B,v)
}C=r._frontWindow();
if(C&&d.get(C.id+"_ifr")){C.focussedElement=d.get(C.id+"_ifr").contentWindow.document.activeElement
}if(!B.type){r.bookmark=k.selection.getBookmark(1)
}x=d.uniqueId();
z=d.getViewPort();
B.width=parseInt(B.width||320);
h=B.url||"";
j=(!h||h.lastIndexOf("/")==-1)?"":h.substring(h.lastIndexOf("/")+1);
switch(j){case"color_picker.htm":B.height=270;
B.opaque=true;
break;
case"link.htm":B.height=244;
v.authoringObj=CStudioAuthoring;
v.contextObj=CStudioAuthoringContext;
break;
case"anchor.htm":B.height=148;
break;
case"table.htm":B.height=370;
break;
case"row.htm":B.height=295;
break;
case"cell.htm":B.height=320;
break;
case"image.htm":B.height=295;
v.contextObj=CStudioAuthoringContext;
break;
default:B.height=B.height||100
}B.height+=tinymce.isIE?8:0;
B.min_width=parseInt(B.min_width||150);
B.min_height=parseInt(B.min_height||100);
B.max_width=parseInt(B.max_width||2000);
B.max_height=parseInt(B.max_height||2000);
B.left=B.left||Math.round(Math.max(z.x,z.x+(z.w/2)-(B.width/2)));
B.top=B.top||Math.round(Math.max(z.y,z.y+(z.h/2)-(B.height/2)));
B.movable=B.resizable=false;
v.mce_width=B.width;
v.mce_height=B.height;
v.mce_inline=true;
v.mce_window_id=x;
v.mce_auto_focus=B.auto_focus;
r.features=B;
r.params=v;
r.onOpen.dispatch(r,B,v);
r.mouseEvent=false;
if(B.type){g+=" mceModal";
if(B.type){g+=" mce"+B.type.substring(0,1).toUpperCase()+B.type.substring(1)
}B.resizable=false
}if(B.statusbar){g+=" mceStatusbar"
}if(B.resizable){g+=" mceResizable"
}if(B.minimizable){g+=" mceMinimizable"
}if(B.maximizable){g+=" mceMaximizable"
}if(B.movable){g+=" mceMovable"
}r._addAll(d.doc.body,["div",{id:x,role:"dialog","aria-labelledby":B.type?x+"_content":x+"_title","class":(B.opaque?"opaque":"")+" "+(k.settings.inlinepopups_skin||"clearlooks2")+(tinymce.isIE&&window.getSelection?" ie9":""),style:"width:100px;height:100px"},["div",{id:x+"_wrapper","class":"mceWrapper"+g},["div",{id:x+"_top","class":"mceTop"},["div",{"class":"mceLeft"}],["div",{"class":"mceCenter"}],["div",{"class":"mceRight"}],["span",{id:x+"_title"},B.title||""]],["div",{id:x+"_middle","class":"mceMiddle"},["div",{id:x+"_left","class":"mceLeft",tabindex:"0"}],["span",{id:x+"_content"}],["div",{id:x+"_right","class":"mceRight",tabindex:"0"}]],["div",{id:x+"_bottom","class":"mceBottom"},["div",{"class":"mceLeft"}],["div",{"class":"mceCenter"}],["div",{"class":"mceRight"}],["span",{id:x+"_status"},"Content"]],["a",{"class":"mceMove",tabindex:"-1",href:"javascript:;"}],["a",{"class":"mceMin",tabindex:"-1",href:"javascript:;",onmousedown:"return false;"}],["a",{"class":"mceMax",tabindex:"-1",href:"javascript:;",onmousedown:"return false;"}],["a",{"class":"mceMed",tabindex:"-1",href:"javascript:;",onmousedown:"return false;"}],["a",{"class":"mceClose",tabindex:"-1",href:"javascript:;",onmousedown:"return false;"}],["a",{id:x+"_resize_n","class":"mceResize mceResizeN",tabindex:"-1",href:"javascript:;"}],["a",{id:x+"_resize_s","class":"mceResize mceResizeS",tabindex:"-1",href:"javascript:;"}],["a",{id:x+"_resize_w","class":"mceResize mceResizeW",tabindex:"-1",href:"javascript:;"}],["a",{id:x+"_resize_e","class":"mceResize mceResizeE",tabindex:"-1",href:"javascript:;"}],["a",{id:x+"_resize_nw","class":"mceResize mceResizeNW",tabindex:"-1",href:"javascript:;"}],["a",{id:x+"_resize_ne","class":"mceResize mceResizeNE",tabindex:"-1",href:"javascript:;"}],["a",{id:x+"_resize_sw","class":"mceResize mceResizeSW",tabindex:"-1",href:"javascript:;"}],["a",{id:x+"_resize_se","class":"mceResize mceResizeSE",tabindex:"-1",href:"javascript:;"}]]]);
d.setStyles(x,{top:-10000,left:-10000});
if(tinymce.isGecko){d.setStyle(x,"overflow","auto")
}if(!B.type){q+=d.get(x+"_left").clientWidth;
q+=d.get(x+"_right").clientWidth;
A+=d.get(x+"_top").clientHeight;
A+=d.get(x+"_bottom").clientHeight
}d.setStyles(x,{top:B.top,left:B.left,width:B.width+q,height:B.height+A});
o=B.url||B.file;
if(o){if(tinymce.relaxedDomain){o+=(o.indexOf("?")==-1?"?":"&")+"mce_rdomain="+tinymce.relaxedDomain
}o=tinymce._addVer(o)
}if(!B.type){d.add(x+"_content","iframe",{id:x+"_ifr",src:'javascript:""',frameBorder:0,style:"border:0;width:10px;height:10px"});
d.setStyles(x+"_ifr",{width:"100%",height:B.height});
d.setAttrib(x+"_ifr","src",o);
amplify.subscribe("/rte/clicked",B.clickSubscription=function(){var p,f=document.getElementById(x+"_ifr");
if(f){p=f.contentDocument.getElementById("cancel");
if(p){r.mouseEvent=true;
p.click()
}}});
amplify.subscribe("/rte/blurred",B.blurSubscription=function(){var p,f=document.getElementById(x+"_ifr");
if(f){p=f.contentDocument.getElementById("cancel");
if(p){r.mouseEvent=true;
p.click()
}}})
}else{d.add(x+"_wrapper","a",{id:x+"_ok","class":"mceButton mceOk",href:"javascript:;",onmousedown:"return false;"},"Ok");
if(B.type=="confirm"){d.add(x+"_wrapper","a",{"class":"mceButton mceCancel",href:"javascript:;",onmousedown:"return false;"},"Cancel")
}d.add(x+"_middle","div",{"class":"mceIcon"});
d.setHTML(x+"_content",B.content.replace("\n","<br />"));
a.add(x,"keyup",function(f){var p=27;
if(f.keyCode===p){B.button_func(false);
return a.cancel(f)
}});
a.add(x,"keydown",function(f){var t,p=9;
if(f.keyCode===p){t=d.select("a.mceCancel",x+"_wrapper")[0];
if(t&&t!==f.target){t.focus()
}else{d.get(x+"_ok").focus()
}return a.cancel(f)
}})
}i=a.add(x,"mousedown",function(t){var u=t.target,f,p;
f=r.windows[x];
r.focus(x);
if(u.nodeName=="A"||u.nodeName=="a"){if(u.className=="mceMax"){f.oldPos=f.element.getXY();
f.oldSize=f.element.getSize();
p=d.getViewPort();
p.w-=2;
p.h-=2;
f.element.moveTo(p.x,p.y);
f.element.resizeTo(p.w,p.h);
d.setStyles(x+"_ifr",{width:p.w-f.deltaWidth,height:p.h-f.deltaHeight});
d.addClass(x+"_wrapper","mceMaximized")
}else{if(u.className=="mceMed"){f.element.moveTo(f.oldPos.x,f.oldPos.y);
f.element.resizeTo(f.oldSize.w,f.oldSize.h);
f.iframeElement.resizeTo(f.oldSize.w-f.deltaWidth,f.oldSize.h-f.deltaHeight);
d.removeClass(x+"_wrapper","mceMaximized")
}else{if(u.className=="mceMove"){return r._startDrag(x,t,u.className)
}else{if(d.hasClass(u,"mceResize")){return r._startDrag(x,t,u.className.substring(13))
}}}}}});
l=a.add(x,"click",function(f){var p=f.target;
r.focus(x);
if(p.nodeName=="A"||p.nodeName=="a"){switch(p.className){case"mceClose":r.close(null,x);
return a.cancel(f);
case"mceButton mceOk":case"mceButton mceCancel":B.button_func(p.className=="mceButton mceOk");
return a.cancel(f)
}}});
a.add([x+"_left",x+"_right"],"focus",function(p){var t=d.get(x+"_ifr");
if(t){var f=t.contentWindow.document.body;
var u=d.select(":input:enabled,*[tabindex=0]",f);
if(p.target.id===(x+"_left")){u[u.length-1].focus()
}else{u[0].focus()
}}else{d.get(x+"_ok").focus()
}});
n=r.windows[x]={id:x,mousedown_func:i,click_func:l,element:new b(x,{blocker:1,container:k.getContainer()}),iframeElement:new b(x+"_ifr"),features:B,deltaWidth:q,deltaHeight:A};
n.iframeElement.on("focus",function(){r.focus(x)
});
if(r.count==0&&r.editor.getParam("dialog_type","modal")=="modal"){d.add(d.doc.body,"div",{id:"mceModalBlocker","class":(r.editor.settings.inlinepopups_skin||"clearlooks2")+"_modalBlocker",style:{zIndex:r.zIndex-1}});
d.hide("mceModalBlocker");
d.setAttrib(d.doc.body,"aria-hidden","true")
}else{d.setStyle("mceModalBlocker","z-index",r.zIndex-1);
d.show("mceModalBlocker")
}if(tinymce.isIE6||/Firefox\/2\./.test(navigator.userAgent)||(tinymce.isIE&&!d.boxModel)){d.setStyles("mceModalBlocker",{position:"absolute",left:z.x,top:z.y,width:z.w-2,height:z.h-2})
}d.setAttrib(x,"aria-hidden","false");
r.focus(x);
r._fixIELayout(x,1);
if(d.get(x+"_ok")){d.get(x+"_ok").focus()
}r.count++;
return n
},focus:function(h){var g=this,f;
if(f=g.windows[h]){f.zIndex=this.zIndex++;
f.element.setStyle("zIndex",f.zIndex);
f.element.update();
h=h+"_wrapper";
d.removeClass(g.lastId,"mceFocus");
d.addClass(h,"mceFocus");
g.lastId=h;
if(f.focussedElement){f.focussedElement.focus()
}else{if(d.get(h+"_ok")){d.get(f.id+"_ok").focus()
}else{if(d.get(f.id+"_ifr")){d.get(f.id+"_ifr").focus()
}}}}},_addAll:function(k,h){var g,l,f=this,j=tinymce.DOM;
if(c(h,"string")){k.appendChild(j.doc.createTextNode(h))
}else{if(h.length){k=k.appendChild(j.create(h[0],h[1]));
for(g=2;
g<h.length;
g++){f._addAll(k,h[g])
}}}},_startDrag:function(v,G,E){var o=this,u,z,C=d.doc,f,l=o.windows[v],h=l.element,y=h.getXY(),x,q,F,g,A,s,r,j,i,m,k,n,B;
g={x:0,y:0};
A=d.getViewPort();
A.w-=2;
A.h-=2;
j=G.screenX;
i=G.screenY;
m=k=n=B=0;
u=a.add(C,"mouseup",function(p){a.remove(C,"mouseup",u);
a.remove(C,"mousemove",z);
if(f){f.remove()
}h.moveBy(m,k);
h.resizeBy(n,B);
q=h.getSize();
d.setStyles(v+"_ifr",{width:q.w-l.deltaWidth,height:q.h-l.deltaHeight});
o._fixIELayout(v,1);
return a.cancel(p)
});
if(E!="Move"){D()
}function D(){if(f){return
}o._fixIELayout(v,0);
d.add(C.body,"div",{id:"mceEventBlocker","class":"mceEventBlocker "+(o.editor.settings.inlinepopups_skin||"clearlooks2"),style:{zIndex:o.zIndex+1}});
if(tinymce.isIE6||(tinymce.isIE&&!d.boxModel)){d.setStyles("mceEventBlocker",{position:"absolute",left:A.x,top:A.y,width:A.w-2,height:A.h-2})
}f=new b("mceEventBlocker");
f.update();
x=h.getXY();
q=h.getSize();
s=g.x+x.x-A.x;
r=g.y+x.y-A.y;
d.add(f.get(),"div",{id:"mcePlaceHolder","class":"mcePlaceHolder",style:{left:s,top:r,width:q.w,height:q.h}});
F=new b("mcePlaceHolder")
}z=a.add(C,"mousemove",function(w){var p,H,t;
D();
p=w.screenX-j;
H=w.screenY-i;
switch(E){case"ResizeW":m=p;
n=0-p;
break;
case"ResizeE":n=p;
break;
case"ResizeN":case"ResizeNW":case"ResizeNE":if(E=="ResizeNW"){m=p;
n=0-p
}else{if(E=="ResizeNE"){n=p
}}k=H;
B=0-H;
break;
case"ResizeS":case"ResizeSW":case"ResizeSE":if(E=="ResizeSW"){m=p;
n=0-p
}else{if(E=="ResizeSE"){n=p
}}B=H;
break;
case"mceMove":m=p;
k=H;
break
}if(n<(t=l.features.min_width-q.w)){if(m!==0){m+=n-t
}n=t
}if(B<(t=l.features.min_height-q.h)){if(k!==0){k+=B-t
}B=t
}n=Math.min(n,l.features.max_width-q.w);
B=Math.min(B,l.features.max_height-q.h);
m=Math.max(m,A.x-(s+A.x));
k=Math.max(k,A.y-(r+A.y));
m=Math.min(m,(A.w+A.x)-(s+q.w+A.x));
k=Math.min(k,(A.h+A.y)-(r+q.h+A.y));
if(m+k!==0){if(s+m<0){m=0
}if(r+k<0){k=0
}F.moveTo(s+m,r+k)
}if(n+B!==0){F.resizeTo(q.w+n,q.h+B)
}return a.cancel(w)
});
return a.cancel(G)
},resizeBy:function(g,h,i){var f=this.windows[i];
if(f){f.element.resizeBy(g,h);
f.iframeElement.resizeBy(g,h)
}},close:function(i,k){var g=this,f,j=d.doc,h,k;
k=g._findId(k||i);
f=g.windows[k];
if(!f){g.parent(i);
return
}else{if(f.features&&!f.features.type){amplify.unsubscribe("/rte/clicked",f.features.clickSubscription);
amplify.unsubscribe("/rte/blurred",f.features.blurSubscription)
}}g.count--;
if(g.count==0){d.remove("mceModalBlocker");
d.setAttrib(d.doc.body,"aria-hidden","false");
if(!g.mouseEvent){g.editor.focus()
}}else{if(g.count==1){d.hide("mceModalBlocker")
}}if(f){g.onClose.dispatch(g);
a.remove(j,"mousedown",f.mousedownFunc);
a.remove(j,"click",f.clickFunc);
a.clear(k);
a.clear(k+"_ifr");
d.setAttrib(k+"_ifr","src",'javascript:""');
f.element.remove();
delete g.windows[k];
h=g._frontWindow();
if(h){g.focus(h.id)
}}g.mouseEvent=false
},_frontWindow:function(){var g,f=0;
e(this.windows,function(h){if(h.zIndex>f){g=h;
f=h.zIndex
}});
return g
},setTitle:function(f,g){var h;
f=this._findId(f);
if(h=d.get(f+"_title")){h.innerHTML=d.encode(g)
}},alert:function(g,f,j){var i=this,h;
h=i.open({title:i,type:"alert",button_func:function(k){if(f){f.call(k||i,k)
}i.close(null,h.id)
},content:d.encode(i.editor.getLang(g,g)),inline:1,width:400,height:130})
},confirm:function(g,f,j){var i=this,h;
h=i.open({title:i,type:"confirm",button_func:function(k){if(f){f.call(k||i,k)
}i.close(null,h.id)
},content:d.encode(i.editor.getLang(g,g)),inline:1,width:400,height:130})
},_findId:function(f){var g=this;
if(typeof(f)=="string"){return f
}e(g.windows,function(h){var i=d.get(h.id+"_ifr");
if(i&&f==i.contentWindow){f=h.id;
return false
}});
return f
},_fixIELayout:function(i,h){var f,g;
if(!tinymce.isIE6){return
}e(["n","s","w","e","nw","ne","sw","se"],function(j){var k=d.get(i+"_resize_"+j);
d.setStyles(k,{width:h?k.clientWidth:"",height:h?k.clientHeight:"",cursor:d.getStyle(k,"cursor",1)});
d.setStyle(i+"_bottom","bottom","-1px");
k=0
});
if(f=this.windows[i]){f.element.hide();
f.element.show();
e(d.select("div,a",i),function(k,j){if(k.currentStyle.backgroundImage!="none"){g=new Image();
g.src=k.currentStyle.backgroundImage.replace(/url\(\"(.+)\"\)/,"$1")
}});
d.get(i).style.filter=""
}}});
tinymce.PluginManager.add("cs_inlinepopups",tinymce.plugins.CStudioInlinePopups)
})();