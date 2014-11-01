(function(f){var d={dragStart:"start.draggable",drag:"drag.draggable",dragStop:"stop.draggable",maxHeight:"maxHeight.resizable",minHeight:"minHeight.resizable",maxWidth:"maxWidth.resizable",minWidth:"minWidth.resizable",resizeStart:"start.resizable",resize:"drag.resizable",resizeStop:"stop.resizable"},e="ui-dialog ui-widget ui-widget-content ui-corner-all ";
f.widget("ui.dialog",{_init:function(){this.originalTitle=this.element.attr("title");
var b=this,a=this.options,n=a.title||this.originalTitle||"&nbsp;",s=f.ui.dialog.getTitleId(this.element),c=(this.uiDialog=f("<div/>")).appendTo(document.body).hide().addClass(e+a.dialogClass).css({position:"absolute",overflow:"hidden",zIndex:a.zIndex}).attr("tabIndex",-1).css("outline",0).keydown(function(g){(a.closeOnEscape&&g.keyCode&&g.keyCode==f.ui.keyCode.ESCAPE&&b.close(g))
}).attr({role:"dialog","aria-labelledby":s}).mousedown(function(g){b.moveToTop(false,g)
}),q=this.element.show().removeAttr("title").addClass("ui-dialog-content ui-widget-content").appendTo(c),r=(this.uiDialogTitlebar=f("<div></div>")).addClass("ui-dialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix").prependTo(c),o=f('<a href="#"/>').addClass("ui-dialog-titlebar-close ui-corner-all").attr("role","button").hover(function(){o.addClass("ui-state-hover")
},function(){o.removeClass("ui-state-hover")
}).focus(function(){o.addClass("ui-state-focus")
}).blur(function(){o.removeClass("ui-state-focus")
}).mousedown(function(g){g.stopPropagation()
}).click(function(g){b.close(g);
return false
}).appendTo(r),p=(this.uiDialogTitlebarCloseText=f("<span/>")).addClass("ui-icon ui-icon-closethick").text(a.closeText).appendTo(o),t=f("<span/>").addClass("ui-dialog-title").attr("id",s).html(n).prependTo(r);
r.find("*").add(r).disableSelection();
(a.draggable&&f.fn.draggable&&this._makeDraggable());
(a.resizable&&f.fn.resizable&&this._makeResizable());
this._createButtons(a.buttons);
this._isOpen=false;
(a.bgiframe&&f.fn.bgiframe&&c.bgiframe());
(a.autoOpen&&this.open())
},destroy:function(){(this.overlay&&this.overlay.destroy());
this.uiDialog.hide();
this.element.unbind(".dialog").removeData("dialog").removeClass("ui-dialog-content ui-widget-content").hide().appendTo("body");
this.uiDialog.remove();
(this.originalTitle&&this.element.attr("title",this.originalTitle))
},close:function(a){var c=this;
if(false===c._trigger("beforeclose",a)){return
}(c.overlay&&c.overlay.destroy());
c.uiDialog.unbind("keypress.ui-dialog");
(c.options.hide?c.uiDialog.hide(c.options.hide,function(){c._trigger("close",a)
}):c.uiDialog.hide()&&c._trigger("close",a));
f.ui.dialog.overlay.resize();
c._isOpen=false;
if(c.options.modal){var b=0;
f(".ui-dialog").each(function(){if(this!=c.uiDialog[0]){b=Math.max(b,f(this).css("z-index"))
}});
f.ui.dialog.maxZ=b
}},isOpen:function(){return this._isOpen
},moveToTop:function(a,b){if((this.options.modal&&!a)||(!this.options.stack&&!this.options.modal)){return this._trigger("focus",b)
}if(this.options.zIndex>f.ui.dialog.maxZ){f.ui.dialog.maxZ=this.options.zIndex
}(this.overlay&&this.overlay.$el.css("z-index",f.ui.dialog.overlay.maxZ=++f.ui.dialog.maxZ));
var c={scrollTop:this.element.attr("scrollTop"),scrollLeft:this.element.attr("scrollLeft")};
this.uiDialog.css("z-index",++f.ui.dialog.maxZ);
this.element.attr(c);
this._trigger("focus",b)
},open:function(){if(this._isOpen){return
}var a=this.options,b=this.uiDialog;
this.overlay=a.modal?new f.ui.dialog.overlay(this):null;
(b.next().length&&b.appendTo("body"));
this._size();
this._position(a.position);
b.show(a.show);
this.moveToTop(true);
(a.modal&&b.bind("keypress.ui-dialog",function(j){if(j.keyCode!=f.ui.keyCode.TAB){return
}var k=f(":tabbable",this),c=k.filter(":first")[0],l=k.filter(":last")[0];
if(j.target==l&&!j.shiftKey){setTimeout(function(){c.focus()
},1)
}else{if(j.target==c&&j.shiftKey){setTimeout(function(){l.focus()
},1)
}}}));
f([]).add(b.find(".ui-dialog-content :tabbable:first")).add(b.find(".ui-dialog-buttonpane :tabbable:first")).add(b).filter(":first").focus();
this._trigger("open");
this._isOpen=true
},_createButtons:function(a){var b=this,h=false,c=f("<div></div>").addClass("ui-dialog-buttonpane ui-widget-content ui-helper-clearfix");
this.uiDialog.find(".ui-dialog-buttonpane").remove();
(typeof a=="object"&&a!==null&&f.each(a,function(){return !(h=true)
}));
if(h){f.each(a,function(j,g){f('<button type="button"></button>').addClass("ui-state-default ui-corner-all").text(j).click(function(){g.apply(b.element[0],arguments)
}).hover(function(){f(this).addClass("ui-state-hover")
},function(){f(this).removeClass("ui-state-hover")
}).focus(function(){f(this).addClass("ui-state-focus")
}).blur(function(){f(this).removeClass("ui-state-focus")
}).appendTo(c)
});
c.appendTo(this.uiDialog)
}},_makeDraggable:function(){var c=this,a=this.options,b;
this.uiDialog.draggable({cancel:".ui-dialog-content",handle:".ui-dialog-titlebar",containment:"document",start:function(){b=a.height;
f(this).height(f(this).height()).addClass("ui-dialog-dragging");
(a.dragStart&&a.dragStart.apply(c.element[0],arguments))
},drag:function(){(a.drag&&a.drag.apply(c.element[0],arguments))
},stop:function(){f(this).removeClass("ui-dialog-dragging").height(b);
(a.dragStop&&a.dragStop.apply(c.element[0],arguments));
f.ui.dialog.overlay.resize()
}})
},_makeResizable:function(a){a=(a===undefined?this.options.resizable:a);
var h=this,b=this.options,c=typeof a=="string"?a:"n,e,s,w,se,sw,ne,nw";
this.uiDialog.resizable({cancel:".ui-dialog-content",alsoResize:this.element,maxWidth:b.maxWidth,maxHeight:b.maxHeight,minWidth:b.minWidth,minHeight:b.minHeight,start:function(){f(this).addClass("ui-dialog-resizing");
(b.resizeStart&&b.resizeStart.apply(h.element[0],arguments))
},resize:function(){(b.resize&&b.resize.apply(h.element[0],arguments))
},handles:c,stop:function(){f(this).removeClass("ui-dialog-resizing");
b.height=f(this).height();
b.width=f(this).width();
(b.resizeStop&&b.resizeStop.apply(h.element[0],arguments));
f.ui.dialog.overlay.resize()
}}).find(".ui-resizable-se").addClass("ui-icon ui-icon-grip-diagonal-se")
},_position:function(a){var k=f(window),j=f(document),c=j.scrollTop(),l=j.scrollLeft(),b=c;
if(f.inArray(a,["center","top","right","bottom","left"])>=0){a=[a=="right"||a=="left"?a:"center",a=="top"||a=="bottom"?a:"middle"]
}if(a.constructor!=Array){a=["center","middle"]
}if(a[0].constructor==Number){l+=a[0]
}else{switch(a[0]){case"left":l+=0;
break;
case"right":l+=k.width()-this.uiDialog.outerWidth();
break;
default:case"center":l+=(k.width()-this.uiDialog.outerWidth())/2
}}if(a[1].constructor==Number){c+=a[1]
}else{switch(a[1]){case"top":c+=0;
break;
case"bottom":c+=k.height()-this.uiDialog.outerHeight();
break;
default:case"middle":c+=(k.height()-this.uiDialog.outerHeight())/2
}}c=Math.max(c,b);
this.uiDialog.css({top:c,left:l})
},_setData:function(c,b){(d[c]&&this.uiDialog.data(d[c],b));
switch(c){case"buttons":this._createButtons(b);
break;
case"closeText":this.uiDialogTitlebarCloseText.text(b);
break;
case"dialogClass":this.uiDialog.removeClass(this.options.dialogClass).addClass(e+b);
break;
case"draggable":(b?this._makeDraggable():this.uiDialog.draggable("destroy"));
break;
case"height":this.uiDialog.height(b);
break;
case"position":this._position(b);
break;
case"resizable":var h=this.uiDialog,a=this.uiDialog.is(":data(resizable)");
(a&&!b&&h.resizable("destroy"));
(a&&typeof b=="string"&&h.resizable("option","handles",b));
(a||this._makeResizable(b));
break;
case"title":f(".ui-dialog-title",this.uiDialogTitlebar).html(b||"&nbsp;");
break;
case"width":this.uiDialog.width(b);
break
}f.widget.prototype._setData.apply(this,arguments)
},_size:function(){var a=this.options;
this.element.css({height:0,minHeight:0,width:"auto"});
var b=this.uiDialog.css({height:"auto",width:a.width}).height();
this.element.css({minHeight:Math.max(a.minHeight-b,0),height:a.height=="auto"?"auto":Math.max(a.height-b,0)})
}});
f.extend(f.ui.dialog,{version:"1.7.2",defaults:{autoOpen:true,bgiframe:false,buttons:{},closeOnEscape:true,closeText:"close",dialogClass:"",draggable:true,hide:null,height:"auto",maxHeight:false,maxWidth:false,minHeight:150,minWidth:150,modal:false,position:"center",resizable:true,show:null,stack:true,title:"",width:300,zIndex:1000},getter:"isOpen",uuid:0,maxZ:0,getTitleId:function(a){return"ui-dialog-title-"+(a.attr("id")||++this.uuid)
},overlay:function(a){this.$el=f.ui.dialog.overlay.create(a)
}});
f.extend(f.ui.dialog.overlay,{instances:[],maxZ:0,events:f.map("focus,mousedown,mouseup,keydown,keypress,click".split(","),function(a){return a+".dialog-overlay"
}).join(" "),create:function(a){if(this.instances.length===0){setTimeout(function(){if(f.ui.dialog.overlay.instances.length){f(document).bind(f.ui.dialog.overlay.events,function(h){var c=f(h.target).parents(".ui-dialog").css("zIndex")||0;
return(c>f.ui.dialog.overlay.maxZ)
})
}},1);
f(document).bind("keydown.dialog-overlay",function(c){(a.options.closeOnEscape&&c.keyCode&&c.keyCode==f.ui.keyCode.ESCAPE&&a.close(c))
});
f(window).bind("resize.dialog-overlay",f.ui.dialog.overlay.resize)
}var b=f("<div></div>").appendTo(document.body).addClass("ui-widget-overlay").css({width:this.width(),height:this.height()});
(a.options.bgiframe&&f.fn.bgiframe&&b.bgiframe());
this.instances.push(b);
return b
},destroy:function(b){this.instances.splice(f.inArray(this.instances,b),1);
if(this.instances.length===0){f([document,window]).unbind(".dialog-overlay")
}b.remove();
var a=0;
f.each(this.instances,function(){a=Math.max(a,this.css("z-index"))
});
this.maxZ=a
},height:function(){if(f.browser.msie&&f.browser.version<7){var a=Math.max(document.documentElement.scrollHeight,document.body.scrollHeight);
var b=Math.max(document.documentElement.offsetHeight,document.body.offsetHeight);
if(a<b){return f(window).height()+"px"
}else{return a+"px"
}}else{return f(document).height()+"px"
}},width:function(){if(f.browser.msie&&f.browser.version<7){var b=Math.max(document.documentElement.scrollWidth,document.body.scrollWidth);
var a=Math.max(document.documentElement.offsetWidth,document.body.offsetWidth);
if(b<a){return f(window).width()+"px"
}else{return b+"px"
}}else{return f(document).width()+"px"
}},resize:function(){var a=f([]);
f.each(f.ui.dialog.overlay.instances,function(){a=a.add(this)
});
a.css({width:0,height:0}).css({width:f.ui.dialog.overlay.width(),height:f.ui.dialog.overlay.height()})
}});
f.extend(f.ui.dialog.overlay.prototype,{destroy:function(){f.ui.dialog.overlay.destroy(this.$el)
}})
})(jQuery);