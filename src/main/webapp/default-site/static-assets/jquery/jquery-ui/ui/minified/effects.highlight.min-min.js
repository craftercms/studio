(function(b){b.effects.highlight=function(a){return this.queue(function(){var l=b(this),m=["backgroundImage","backgroundColor","opacity"];
var i=b.effects.setMode(l,a.options.mode||"show");
var n=a.options.color||"#ffff99";
var j=l.css("backgroundColor");
b.effects.save(l,m);
l.show();
l.css({backgroundImage:"none",backgroundColor:n});
var k={backgroundColor:j};
if(i=="hide"){k.opacity=0
}l.animate(k,{queue:false,duration:a.duration,easing:a.options.easing,complete:function(){if(i=="hide"){l.hide()
}b.effects.restore(l,m);
if(i=="show"&&b.browser.msie){this.style.removeAttribute("filter")
}if(a.callback){a.callback.apply(this,arguments)
}l.dequeue()
}})
})
}
})(jQuery);