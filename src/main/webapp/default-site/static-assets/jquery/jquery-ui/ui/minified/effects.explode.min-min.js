(function(b){b.effects.explode=function(a){return this.queue(function(){var j=a.options.pieces?Math.round(Math.sqrt(a.options.pieces)):3;
var p=a.options.pieces?Math.round(Math.sqrt(a.options.pieces)):3;
a.options.mode=a.options.mode=="toggle"?(b(this).is(":visible")?"hide":"show"):a.options.mode;
var m=b(this).show().css("visibility","hidden");
var i=m.offset();
i.top-=parseInt(m.css("marginTop"),10)||0;
i.left-=parseInt(m.css("marginLeft"),10)||0;
var n=m.outerWidth(true);
var r=m.outerHeight(true);
for(var o=0;
o<j;
o++){for(var q=0;
q<p;
q++){m.clone().appendTo("body").wrap("<div></div>").css({position:"absolute",visibility:"visible",left:-q*(n/p),top:-o*(r/j)}).parent().addClass("ui-effects-explode").css({position:"absolute",overflow:"hidden",width:n/p,height:r/j,left:i.left+q*(n/p)+(a.options.mode=="show"?(q-Math.floor(p/2))*(n/p):0),top:i.top+o*(r/j)+(a.options.mode=="show"?(o-Math.floor(j/2))*(r/j):0),opacity:a.options.mode=="show"?0:1}).animate({left:i.left+q*(n/p)+(a.options.mode=="show"?0:(q-Math.floor(p/2))*(n/p)),top:i.top+o*(r/j)+(a.options.mode=="show"?0:(o-Math.floor(j/2))*(r/j)),opacity:a.options.mode=="show"?1:0},a.duration||500)
}}setTimeout(function(){a.options.mode=="show"?m.css({visibility:"visible"}):m.css({visibility:"visible"}).hide();
if(a.callback){a.callback.apply(m[0])
}m.dequeue();
b("div.ui-effects-explode").remove()
},a.duration||500)
})
}
})(jQuery);