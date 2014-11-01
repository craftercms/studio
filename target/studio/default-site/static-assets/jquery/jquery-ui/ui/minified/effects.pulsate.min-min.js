(function(b){b.effects.pulsate=function(a){return this.queue(function(){var k=b(this);
var h=b.effects.setMode(k,a.options.mode||"show");
var i=a.options.times||5;
var j=a.duration?a.duration/2:b.fx.speeds._default/2;
if(h=="hide"){i--
}if(k.is(":hidden")){k.css("opacity",0);
k.show();
k.animate({opacity:1},j,a.options.easing);
i=i-2
}for(var l=0;
l<i;
l++){k.animate({opacity:0},j,a.options.easing).animate({opacity:1},j,a.options.easing)
}if(h=="hide"){k.animate({opacity:0},j,a.options.easing,function(){k.hide();
if(a.callback){a.callback.apply(this,arguments)
}})
}else{k.animate({opacity:0},j,a.options.easing).animate({opacity:1},j,a.options.easing,function(){if(a.callback){a.callback.apply(this,arguments)
}})
}k.queue("fx",function(){k.dequeue()
});
k.dequeue()
})
}
})(jQuery);