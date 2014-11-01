(function(b){b.effects.transfer=function(a){return this.queue(function(){var k=b(this),i=b(a.options.to),l=i.offset(),j={top:l.top,left:l.left,height:i.innerHeight(),width:i.innerWidth()},m=k.offset(),n=b('<div class="ui-effects-transfer"></div>').appendTo(document.body).addClass(a.options.className).css({top:m.top,left:m.left,height:k.innerHeight(),width:k.innerWidth(),position:"absolute"}).animate(j,a.duration,a.options.easing,function(){n.remove();
(a.callback&&a.callback.apply(k[0],arguments));
k.dequeue()
})
})
}
})(jQuery);