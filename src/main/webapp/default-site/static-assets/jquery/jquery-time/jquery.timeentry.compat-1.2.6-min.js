(function(a){a.extend(a.timeEntry,{reconfigureFor:function(b,c){b=(b.jquery?b[0]:(typeof b=="string"?a(b)[0]:b));
a.timeEntry._changeTimeEntry(b,c)
},enableFor:function(b){b=(b.jquery?b:a(b));
b.each(function(){a.timeEntry._enableTimeEntry(this)
})
},disableFor:function(b){b=(b.jquery?b:a(b));
b.each(function(){a.timeEntry._disableTimeEntry(this)
})
},isDisabled:function(b){b=(b.jquery?b[0]:(typeof b=="string"?a(b)[0]:b));
return a.timeEntry._isDisabledTimeEntry(b)
},setTimeFor:function(b,c){b=(b.jquery?b[0]:(typeof b=="string"?a(b)[0]:b));
a.timeEntry._setTimeTimeEntry(b,c)
},getTimeFor:function(b){b=(b.jquery?b[0]:(typeof b=="string"?a(b)[0]:b));
return a.timeEntry._getTimeTimeEntry(b)
}})
})(jQuery);