(function(b){b.effects.slide=function(a){return this.queue(function(){var p=b(this),q=["position","top","left"];
var l=b.effects.setMode(p,a.options.mode||"show");
var m=a.options.direction||"left";
b.effects.save(p,q);
p.show();
b.effects.createWrapper(p).css({overflow:"hidden"});
var o=(m=="up"||m=="down")?"top":"left";
var r=(m=="up"||m=="left")?"pos":"neg";
var k=a.options.distance||(o=="top"?p.outerHeight({margin:true}):p.outerWidth({margin:true}));
if(l=="show"){p.css(o,r=="pos"?-k:k)
}var n={};
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