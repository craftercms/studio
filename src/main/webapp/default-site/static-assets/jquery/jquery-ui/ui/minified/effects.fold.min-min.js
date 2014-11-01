(function(b){b.effects.fold=function(a){return this.queue(function(){var z=b(this),t=["position","top","left"];
var w=b.effects.setMode(z,a.options.mode||"hide");
var p=a.options.size||15;
var q=!(!a.options.horizFirst);
var x=a.duration?a.duration/2:b.fx.speeds._default/2;
b.effects.save(z,t);
z.show();
var A=b.effects.createWrapper(z).css({overflow:"hidden"});
var v=((w=="show")!=q);
var y=v?["width","height"]:["height","width"];
var B=v?[A.width(),A.height()]:[A.height(),A.width()];
var u=/([0-9]+)%/.exec(p);
if(u){p=parseInt(u[1],10)/100*B[w=="hide"?0:1]
}if(w=="show"){A.css(q?{height:0,width:p}:{height:p,width:0})
}var r={},s={};
r[y[0]]=w=="show"?B[0]:p;
s[y[1]]=w=="show"?B[1]:0;
A.animate(r,x,a.options.easing).animate(s,x,a.options.easing,function(){if(w=="hide"){z.hide()
}b.effects.restore(z,t);
b.effects.removeWrapper(z);
if(a.callback){a.callback.apply(z[0],arguments)
}z.dequeue()
})
})
}
})(jQuery);