(function(b){b.widget("ui.selectable",b.extend({},b.ui.mouse,{_init:function(){var a=this;
this.element.addClass("ui-selectable");
this.dragged=false;
var d;
this.refresh=function(){d=b(a.options.filter,a.element[0]);
d.each(function(){var f=b(this);
var c=f.offset();
b.data(this,"selectable-item",{element:this,$element:f,left:c.left,top:c.top,right:c.left+f.outerWidth(),bottom:c.top+f.outerHeight(),startselected:false,selected:f.hasClass("ui-selected"),selecting:f.hasClass("ui-selecting"),unselecting:f.hasClass("ui-unselecting")})
})
};
this.refresh();
this.selectees=d.addClass("ui-selectee");
this._mouseInit();
this.helper=b(document.createElement("div")).css({border:"1px dotted black"}).addClass("ui-selectable-helper")
},destroy:function(){this.element.removeClass("ui-selectable ui-selectable-disabled").removeData("selectable").unbind(".selectable");
this._mouseDestroy()
},_mouseStart:function(e){var a=this;
this.opos=[e.pageX,e.pageY];
if(this.options.disabled){return
}var f=this.options;
this.selectees=b(f.filter,this.element[0]);
this._trigger("start",e);
b(f.appendTo).append(this.helper);
this.helper.css({"z-index":100,position:"absolute",left:e.clientX,top:e.clientY,width:0,height:0});
if(f.autoRefresh){this.refresh()
}this.selectees.filter(".ui-selected").each(function(){var c=b.data(this,"selectable-item");
c.startselected=true;
if(!e.metaKey){c.$element.removeClass("ui-selected");
c.selected=false;
c.$element.addClass("ui-unselecting");
c.unselecting=true;
a._trigger("unselecting",e,{unselecting:c.element})
}});
b(e.target).parents().andSelf().each(function(){var c=b.data(this,"selectable-item");
if(c){c.$element.removeClass("ui-unselecting").addClass("ui-selecting");
c.unselecting=false;
c.selecting=true;
c.selected=true;
a._trigger("selecting",e,{selecting:c.element});
return false
}})
},_mouseDrag:function(j){var p=this;
this.dragged=true;
if(this.options.disabled){return
}var n=this.options;
var o=this.opos[0],k=this.opos[1],a=j.pageX,l=j.pageY;
if(o>a){var m=a;
a=o;
o=m
}if(k>l){var m=l;
l=k;
k=m
}this.helper.css({left:o,top:k,width:a-o,height:l-k});
this.selectees.each(function(){var d=b.data(this,"selectable-item");
if(!d||d.element==p.element[0]){return
}var c=false;
if(n.tolerance=="touch"){c=(!(d.left>a||d.right<o||d.top>l||d.bottom<k))
}else{if(n.tolerance=="fit"){c=(d.left>o&&d.right<a&&d.top>k&&d.bottom<l)
}}if(c){if(d.selected){d.$element.removeClass("ui-selected");
d.selected=false
}if(d.unselecting){d.$element.removeClass("ui-unselecting");
d.unselecting=false
}if(!d.selecting){d.$element.addClass("ui-selecting");
d.selecting=true;
p._trigger("selecting",j,{selecting:d.element})
}}else{if(d.selecting){if(j.metaKey&&d.startselected){d.$element.removeClass("ui-selecting");
d.selecting=false;
d.$element.addClass("ui-selected");
d.selected=true
}else{d.$element.removeClass("ui-selecting");
d.selecting=false;
if(d.startselected){d.$element.addClass("ui-unselecting");
d.unselecting=true
}p._trigger("unselecting",j,{unselecting:d.element})
}}if(d.selected){if(!j.metaKey&&!d.startselected){d.$element.removeClass("ui-selected");
d.selected=false;
d.$element.addClass("ui-unselecting");
d.unselecting=true;
p._trigger("unselecting",j,{unselecting:d.element})
}}}});
return false
},_mouseStop:function(e){var a=this;
this.dragged=false;
var f=this.options;
b(".ui-unselecting",this.element[0]).each(function(){var c=b.data(this,"selectable-item");
c.$element.removeClass("ui-unselecting");
c.unselecting=false;
c.startselected=false;
a._trigger("unselected",e,{unselected:c.element})
});
b(".ui-selecting",this.element[0]).each(function(){var c=b.data(this,"selectable-item");
c.$element.removeClass("ui-selecting").addClass("ui-selected");
c.selecting=false;
c.selected=true;
c.startselected=true;
a._trigger("selected",e,{selected:c.element})
});
this._trigger("stop",e);
this.helper.remove();
return false
}}));
b.extend(b.ui.selectable,{version:"1.7.2",defaults:{appendTo:"body",autoRefresh:true,cancel:":input,option",delay:0,distance:0,filter:"*",tolerance:"touch"}})
})(jQuery);