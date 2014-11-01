(function(b){b.effects.drop=function(a){return this.queue(function(){var p=b(this),q=["position","top","left","opacity"];
var l=b.effects.setMode(p,a.options.mode||"hide");
var m=a.options.direction||"left";
b.effects.save(p,q);
p.show();
b.effects.createWrapper(p);
var o=(m=="up"||m=="down")?"top":"left";
var r=(m=="up"||m=="left")?"pos":"neg";
var k=a.options.distance||(o=="top"?p.outerHeight({margin:true})/2:p.outerWidth({margin:true})/2);
if(l=="show"){p.css("opacity",0).css(o,r=="pos"?-k:k)
}var n={opacity:l=="show"?1:0};
n[o]=(l=="show"?(r=="pos"?"+=":"-="):(r=="pos"?"-=":"+="))+k;
p.animate(n,{queue:false,duration:a.duration,easing:a.options.easing,complete:function(){if(l=="hide"){p.hide()
}b.effects.restore(p,q);
b.effects.removeWrapper(p);
if(a.callback){a.callback.apply(this,arguments)
}p.dequeue()
}})
})
}
})(jQuery);