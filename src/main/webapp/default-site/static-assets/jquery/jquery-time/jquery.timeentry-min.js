(function(c){function b(){this._disabledInputs=[];
this.regional=[];
this.regional[""]={show24Hours:false,separator:":",ampmPrefix:"",ampmNames:["AM","PM"],spinnerTexts:["Now","Previous field","Next field","Increment","Decrement"]};
this._defaults={appendText:"",showSeconds:false,timeSteps:[1,1,1],initialField:0,useMouseWheel:true,defaultTime:null,minTime:null,maxTime:null,spinnerImage:"spinnerDefault.png",spinnerSize:[15,17,8],spinnerBigImage:"",spinnerBigSize:[40,40,16],spinnerIncDecOnly:true,spinnerRepeat:[500,250],beforeShow:null,beforeSetTime:null};
c.extend(this._defaults,this.regional[""])
}var d="timeEntry";
c.extend(b.prototype,{markerClassName:"hasTimeEntry",setDefaults:function(e){a(this._defaults,e||{});
return this
},_connectTimeEntry:function(h,m){var k=c(h);
if(k.hasClass(this.markerClassName)){return
}var g={};
g.options=c.extend({},m);
g._selectedHour=0;
g._selectedMinute=0;
g._selectedSecond=0;
g._field=0;
g.input=c(h);
c.data(h,d,g);
var e=this._get(g,"spinnerImage");
var i=this._get(g,"spinnerText");
var j=this._get(g,"spinnerSize");
var f=this._get(g,"appendText");
var l=(!e?null:c('<span class="timeEntry_control" style="display: inline-block; background: url(\''+e+"') 0 0 no-repeat; width: "+j[0]+"px; height: "+j[1]+"px;"+(c.browser.mozilla&&c.browser.version<"1.9"?" padding-left: "+j[0]+"px; padding-bottom: "+(j[1]-18)+"px;":"")+'"></span>'));
k.wrap('<span class="timeEntry_wrap"></span>').after(f?'<span class="timeEntry_append">'+f+"</span>":"").after(l||"");
k.addClass(this.markerClassName).bind("focus.timeEntry",this._doFocus).bind("blur.timeEntry",this._doBlur).bind("click.timeEntry",this._doClick).bind("keydown.timeEntry",this._doKeyDown).bind("keypress.timeEntry",this._doKeyPress);
if(c.browser.mozilla){k.bind("input.timeEntry",function(n){c.timeentry._parseTime(g)
})
}if(c.browser.msie){k.bind("paste.timeEntry",function(n){setTimeout(function(){c.timeentry._parseTime(g)
},1)
})
}if(this._get(g,"useMouseWheel")&&c.fn.mousewheel){k.mousewheel(this._doMouseWheel)
}if(l){l.mousedown(this._handleSpinner).mouseup(this._endSpinner).mouseover(this._expandSpinner).mouseout(this._endSpinner).mousemove(this._describeSpinner)
}},_enableTimeEntry:function(e){this._enableDisable(e,false)
},_disableTimeEntry:function(e){this._enableDisable(e,true)
},_enableDisable:function(e,f){var g=c.data(e,d);
if(!g){return
}e.disabled=f;
if(e.nextSibling&&e.nextSibling.nodeName.toLowerCase()=="span"){c.timeEntry._changeSpinner(g,e.nextSibling,(f?5:-1))
}c.timeEntry._disabledInputs=c.map(c.timeEntry._disabledInputs,function(h){return(h==e?null:h)
});
if(f){c.timeEntry._disabledInputs.push(e)
}},_isDisabledTimeEntry:function(e){return c.inArray(e,this._disabledInputs)>-1
},_changeTimeEntry:function(e,f){var h=c.data(e,d);
if(h){var g=this._extractTime(h);
a(h.options,f||{});
if(g){this._setTime(h,new Date(0,0,0,g[0],g[1],g[2]))
}}c.data(e,d,h)
},_destroyTimeEntry:function(e){$input=c(e);
if(!$input.hasClass(this.markerClassName)){return
}$input.removeClass(this.markerClassName).unbind(".timeEntry");
if(c.fn.mousewheel){$input.unmousewheel()
}this._disabledInputs=c.map(this._disabledInputs,function(f){return(f==e?null:f)
});
$input.parent().replaceWith($input);
c.removeData(e,d)
},_setTimeTimeEntry:function(e,g){var f=c.data(e,d);
if(f){this._setTime(f,g?(typeof g=="object"?new Date(g.getTime()):g):null)
}},_getTimeTimeEntry:function(e){var g=c.data(e,d);
var f=(g?this._extractTime(g):null);
return(!f?null:new Date(0,0,0,f[0],f[1],f[2]))
},_doFocus:function(g){var e=(g.nodeName&&g.nodeName.toLowerCase()=="input"?g:this);
if(c.timeEntry._lastInput==e||c.timeEntry._isDisabledTimeEntry(e)){c.timeEntry._focussed=false;
return
}var f=c.data(e,d);
c.timeEntry._focussed=true;
c.timeEntry._lastInput=e;
c.timeEntry._blurredInput=null;
var h=c.timeEntry._get(f,"beforeShow");
a(f.options,(h?h.apply(e,[e]):{}));
c.data(e,d,f);
c.timeEntry._parseTime(f);
setTimeout(function(){c.timeEntry._showField(f)
},10)
},_doBlur:function(e){c.timeEntry._blurredInput=c.timeEntry._lastInput;
c.timeEntry._lastInput=null
},_doClick:function(f){var m=f.target;
var i=c.data(m,d);
if(!c.timeEntry._focussed){var n=c.timeEntry._get(i,"separator").length+2;
i._field=0;
if(m.selectionStart!=null){for(var l=0;
l<=Math.max(1,i._secondField,i._ampmField);
l++){var g=(l!=i._ampmField?(l*n)+2:(i._ampmField*n)+c.timeEntry._get(i,"ampmPrefix").length+c.timeEntry._get(i,"ampmNames")[0].length);
i._field=l;
if(m.selectionStart<g){break
}}}else{if(m.createTextRange){var e=c(f.srcElement);
var j=m.createTextRange();
var k=function(o){return{thin:2,medium:4,thick:6}[o]||o
};
var h=f.clientX+document.documentElement.scrollLeft-(e.offset().left+parseInt(k(e.css("border-left-width")),10))-j.offsetLeft;
for(var l=0;
l<=Math.max(1,i._secondField,i._ampmField);
l++){var g=(l!=i._ampmField?(l*n)+2:(i._ampmField*n)+c.timeEntry._get(i,"ampmPrefix").length+c.timeEntry._get(i,"ampmNames")[0].length);
j.collapse();
j.moveEnd("character",g);
i._field=l;
if(h<j.boundingWidth){break
}}}}}c.data(m,d,i);
c.timeEntry._showField(i);
c.timeEntry._focussed=false
},_doKeyDown:function(e){if(e.keyCode>=48){return true
}var f=c.data(e.target,d);
switch(e.keyCode){case 9:return(e.shiftKey?c.timeEntry._changeField(f,-1,true):c.timeEntry._changeField(f,+1,true));
case 35:if(e.ctrlKey){c.timeEntry._setValue(f,"")
}else{f._field=Math.max(1,f._secondField,f._ampmField);
c.timeEntry._adjustField(f,0)
}break;
case 36:if(e.ctrlKey){c.timeEntry._setTime(f)
}else{f._field=0;
c.timeEntry._adjustField(f,0)
}break;
case 37:c.timeEntry._changeField(f,-1,false);
break;
case 38:c.timeEntry._adjustField(f,+1);
break;
case 39:c.timeEntry._changeField(f,+1,false);
break;
case 40:c.timeEntry._adjustField(f,-1);
break;
case 46:c.timeEntry._setValue(f,"");
break
}return false
},_doKeyPress:function(f){var e=String.fromCharCode(f.charCode==undefined?f.keyCode:f.charCode);
if(e<" "){return true
}var g=c.data(f.target,d);
c.timeEntry._handleKeyPress(g,e);
return false
},_doMouseWheel:function(e,g){if(c.timeEntry._isDisabledTimeEntry(e.target)){return
}g=(c.browser.opera?-g/Math.abs(g):(c.browser.safari?g/Math.abs(g):g));
var f=c.data(e.target,d);
f.input.focus();
if(!f.input.val()){c.timeEntry._parseTime(f)
}c.timeEntry._adjustField(f,g);
e.preventDefault()
},_expandSpinner:function(h){var l=c.timeEntry._getSpinnerTarget(h);
var j=c.data(c.timeEntry._getInput(l),d);
var g=c.timeEntry._get(j,"spinnerBigImage");
if(g){j._expanded=true;
var k=c(l).offset();
var i=null;
c(l).parents().each(function(){var m=c(this);
if(m.css("position")=="relative"||m.css("position")=="absolute"){i=m.offset()
}return !i
});
var f=c.timeEntry._get(j,"spinnerSize");
var e=c.timeEntry._get(j,"spinnerBigSize");
c('<div class="timeEntry_expand" style="position: absolute; left: '+(k.left-(e[0]-f[0])/2-(i?i.left:0))+"px; top: "+(k.top-(e[1]-f[1])/2-(i?i.top:0))+"px; width: "+e[0]+"px; height: "+e[1]+"px; background: transparent url("+g+') no-repeat 0px 0px; z-index: 10;"></div>').mousedown(c.timeEntry._handleSpinner).mouseup(c.timeEntry._endSpinner).mouseout(c.timeEntry._endExpand).mousemove(c.timeEntry._describeSpinner).insertAfter(l)
}},_getInput:function(e){return c(e).siblings("."+c.timeEntry.markerClassName)[0]
},_describeSpinner:function(e){var g=c.timeEntry._getSpinnerTarget(e);
var f=c.data(c.timeEntry._getInput(g),d);
g.title=c.timeEntry._get(f,"spinnerTexts")[c.timeEntry._getSpinnerRegion(f,e)]
},_handleSpinner:function(f){var j=c.timeEntry._getSpinnerTarget(f);
var e=c.timeEntry._getInput(j);
if(c.timeEntry._isDisabledTimeEntry(e)){return
}if(e==c.timeEntry._blurredInput){c.timeEntry._lastInput=e;
c.timeEntry._blurredInput=null
}var h=c.data(e,d);
c.timeEntry._doFocus(e);
var i=c.timeEntry._getSpinnerRegion(h,f);
c.timeEntry._changeSpinner(h,j,i);
c.timeEntry._actionSpinner(h,i);
c.timeEntry._timer=null;
c.timeEntry._handlingSpinner=true;
var g=c.timeEntry._get(h,"spinnerRepeat");
if(i>=3&&g[0]){c.timeEntry._timer=setTimeout(function(){c.timeEntry._repeatSpinner(h,i)
},g[0]);
c(j).one("mouseout",c.timeEntry._releaseSpinner).one("mouseup",c.timeEntry._releaseSpinner)
}},_actionSpinner:function(e,f){if(!e.input.val()){c.timeEntry._parseTime(e)
}switch(f){case 0:this._setTime(e);
break;
case 1:this._changeField(e,-1,false);
break;
case 2:this._changeField(e,+1,false);
break;
case 3:this._adjustField(e,+1);
break;
case 4:this._adjustField(e,-1);
break
}},_repeatSpinner:function(e,f){if(!c.timeEntry._timer){return
}c.timeEntry._lastInput=c.timeEntry._blurredInput;
this._actionSpinner(e,f);
this._timer=setTimeout(function(){c.timeEntry._repeatSpinner(e,f)
},this._get(e,"spinnerRepeat")[1])
},_releaseSpinner:function(e){clearTimeout(c.timeEntry._timer);
c.timeEntry._timer=null
},_endExpand:function(f){c.timeEntry._timer=null;
var h=c.timeEntry._getSpinnerTarget(f);
var e=c.timeEntry._getInput(h);
var g=c.data(e,d);
c(h).remove();
g._expanded=false
},_endSpinner:function(f){c.timeEntry._timer=null;
var h=c.timeEntry._getSpinnerTarget(f);
var e=c.timeEntry._getInput(h);
var g=c.data(e,d);
if(!c.timeEntry._isDisabledTimeEntry(e)){c.timeEntry._changeSpinner(g,h,-1)
}if(c.timeEntry._handlingSpinner){c.timeEntry._lastInput=c.timeEntry._blurredInput
}if(c.timeEntry._lastInput&&c.timeEntry._handlingSpinner){c.timeEntry._showField(g)
}c.timeEntry._handlingSpinner=false
},_getSpinnerTarget:function(e){return e.target||e.srcElement
},_getSpinnerRegion:function(k,g){var p=this._getSpinnerTarget(g);
var m=(c.browser.opera||c.browser.safari?c.timeEntry._findPos(p):c(p).offset());
var h=(c.browser.safari?c.timeEntry._findScroll(p):[document.documentElement.scrollLeft||document.body.scrollLeft,document.documentElement.scrollTop||document.body.scrollTop]);
var f=this._get(k,"spinnerIncDecOnly");
var i=(f?99:g.clientX+h[0]-m.left-(c.browser.msie?2:0));
var n=g.clientY+h[1]-m.top-(c.browser.msie?2:0);
var l=this._get(k,(k._expanded?"spinnerBigSize":"spinnerSize"));
var o=(f?99:l[0]-1-i);
var e=l[1]-1-n;
if(l[2]>0&&Math.abs(i-o)<=l[2]&&Math.abs(n-e)<=l[2]){return 0
}var j=Math.min(i,n,o,e);
return(j==i?1:(j==o?2:(j==n?3:4)))
},_changeSpinner:function(e,g,f){c(g).css("background-position","-"+((f+1)*this._get(e,(e._expanded?"spinnerBigSize":"spinnerSize"))[0])+"px 0px")
},_findPos:function(g){var f=curTop=0;
if(g.offsetParent){f=g.offsetLeft;
curTop=g.offsetTop;
while(g=g.offsetParent){var e=f;
f+=g.offsetLeft;
if(f<0){f=e
}curTop+=g.offsetTop
}}return{left:f,top:curTop}
},_findScroll:function(g){var f=false;
c(g).parents().each(function(){f|=c(this).css("position")=="fixed"
});
if(f){return[0,0]
}var h=g.scrollLeft;
var e=g.scrollTop;
while(g=g.parentNode){h+=g.scrollLeft||0;
e+=g.scrollTop||0
}return[h,e]
},_get:function(f,e){return(f.options[e]!=null?f.options[e]:c.timeEntry._defaults[e])
},_parseTime:function(h){var g=this._extractTime(h);
var f=this._get(h,"showSeconds");
if(g){h._selectedHour=g[0];
h._selectedMinute=g[1];
h._selectedSecond=g[2]
}else{var e=this._constrainTime(h);
h._selectedHour=e[0];
h._selectedMinute=e[1];
h._selectedSecond=(f?e[2]:0)
}h._secondField=(f?2:-1);
h._ampmField=(this._get(h,"show24Hours")?-1:(f?3:2));
h._lastChr="";
h._field=Math.max(0,Math.min(Math.max(1,h._secondField,h._ampmField),this._get(h,"initialField")));
if(h.input.val()!=""){this._showTime(h)
}},_extractTime:function(l){var o=l.input.val();
var k=this._get(l,"separator");
var f=o.split(k);
if(k==""&&o!=""){f[0]=o.substring(0,2);
f[1]=o.substring(2,4);
f[2]=o.substring(4,6)
}var e=this._get(l,"ampmNames");
var n=this._get(l,"show24Hours");
if(f.length>=2){var m=!n&&(o.indexOf(e[0])>-1);
var j=!n&&(o.indexOf(e[1])>-1);
var i=parseInt(f[0],10);
i=(isNaN(i)?0:i);
i=((m||j)&&i==12?0:i)+(j?12:0);
var h=parseInt(f[1],10);
h=(isNaN(h)?0:h);
var g=(f.length>=3?parseInt(f[2],10):0);
g=(isNaN(g)||!this._get(l,"showSeconds")?0:g);
return this._constrainTime(l,[i,h,g])
}return null
},_constrainTime:function(l,f){var k=(f!=null);
if(!k){var g=this._determineTime(this._get(l,"defaultTime"))||new Date();
f=[g.getHours(),g.getMinutes(),g.getSeconds()]
}var j=false;
var e=this._get(l,"timeSteps");
for(var h=0;
h<e.length;
h++){if(j){f[h]=0
}else{if(e[h]>1){f[h]=Math.round(f[h]/e[h])*e[h];
j=true
}}}return f
},_showTime:function(g){var e=this._get(g,"show24Hours");
var h=this._get(g,"separator");
var f=(this._formatNumber(e?g._selectedHour:((g._selectedHour+11)%12)+1)+h+this._formatNumber(g._selectedMinute)+(this._get(g,"showSeconds")?h+this._formatNumber(g._selectedSecond):"")+(e?"":this._get(g,"ampmPrefix")+this._get(g,"ampmNames")[(g._selectedHour<12?0:1)]));
this._setValue(g,f);
this._showField(g)
},_showField:function(i){var h=i.input[0];
if(i.input.is(":hidden")||c.timeEntry._lastInput!=h){return
}var j=this._get(i,"separator");
var e=j.length+2;
var k=(i._field!=i._ampmField?(i._field*e):(i._ampmField*e)-j.length+this._get(i,"ampmPrefix").length);
var f=k+(i._field!=i._ampmField?2:this._get(i,"ampmNames")[0].length);
if(h.setSelectionRange){h.setSelectionRange(k,f)
}else{if(h.createTextRange){var g=h.createTextRange();
g.moveStart("character",k);
g.moveEnd("character",f-i.input.val().length);
g.select()
}}if(!h.disabled){h.focus()
}},_formatNumber:function(e){return(e<10?"0":"")+e
},_setValue:function(f,e){if(e!=f.input.val()){f.input.val(e).trigger("change")
}},_changeField:function(f,h,g){var e=(f.input.val()==""||f._field==(h==-1?0:Math.max(1,f._secondField,f._ampmField)));
if(!e){f._field+=h
}this._showField(f);
f._lastChr="";
c.data(f.input[0],d,f);
return(e&&g)
},_adjustField:function(f,g){if(f.input.val()==""){g=0
}var e=this._get(f,"timeSteps");
this._setTime(f,new Date(0,0,0,f._selectedHour+(f._field==0?g*e[0]:0)+(f._field==f._ampmField?g*12:0),f._selectedMinute+(f._field==1?g*e[1]:0),f._selectedSecond+(f._field==f._secondField?g*e[2]:0)))
},_setTime:function(i,j){j=this._determineTime(j);
var e=this._constrainTime(i,j?[j.getHours(),j.getMinutes(),j.getSeconds()]:null);
j=new Date(0,0,0,e[0],e[1],e[2]);
var j=this._normaliseTime(j);
var f=this._normaliseTime(this._determineTime(this._get(i,"minTime")));
var h=this._normaliseTime(this._determineTime(this._get(i,"maxTime")));
j=(f&&j<f?f:(h&&j>h?h:j));
var g=this._get(i,"beforeSetTime");
if(g){j=g.apply(i.input[0],[this._getTimeTimeEntry(i.input[0]),j,f,h])
}i._selectedHour=j.getHours();
i._selectedMinute=j.getMinutes();
i._selectedSecond=j.getSeconds();
this._showTime(i);
c.data(i.input[0],d,i)
},_determineTime:function(g){var f=function(i){var h=new Date();
h.setTime(h.getTime()+i*1000);
return h
};
var e=function(n){var l=new Date();
var h=l.getHours();
var m=l.getMinutes();
var i=l.getSeconds();
var k=/([+-]?[0-9]+)\s*(s|S|m|M|h|H)?/g;
var j=k.exec(n);
while(j){switch(j[2]||"s"){case"s":case"S":i+=parseInt(j[1],10);
break;
case"m":case"M":m+=parseInt(j[1],10);
break;
case"h":case"H":h+=parseInt(j[1],10);
break
}j=k.exec(n)
}l=new Date(0,0,10,h,m,i,0);
if(/^!/.test(n)){if(l.getDate()>10){l=new Date(0,0,10,23,59,59)
}else{if(l.getDate()<10){l=new Date(0,0,10,0,0,0)
}}}return l
};
return(g?(typeof g=="string"?e(g):(typeof g=="number"?f(g):g)):null)
},_normaliseTime:function(e){if(!e){return null
}e.setFullYear(1900);
e.setMonth(0);
e.setDate(0);
return e
},_handleKeyPress:function(j,i){if(i==this._get(j,"separator")){this._changeField(j,+1,false)
}else{if(i>="0"&&i<="9"){var n=parseInt(i,10);
var m=parseInt(j._lastChr+i,10);
var l=this._get(j,"show24Hours");
var h=(j._field!=0?j._selectedHour:(l?(m<24?m:n):(m>=1&&m<=12?m:(n>0?n:j._selectedHour))%12+(j._selectedHour>=12?12:0)));
var g=(j._field!=1?j._selectedMinute:(m<60?m:n));
var f=(j._field!=j._secondField?j._selectedSecond:(m<60?m:n));
var k=this._constrainTime(j,[h,g,f]);
this._setTime(j,new Date(0,0,0,k[0],k[1],k[2]));
j._lastChr=i
}else{if(!this._get(j,"show24Hours")){var e=this._get(j,"ampmNames");
if((i==e[0].substring(0,1).toLowerCase()&&j._selectedHour>=12)||(i==e[1].substring(0,1).toLowerCase()&&j._selectedHour<12)){var o=j._field;
j._field=j._ampmField;
this._adjustField(j,+1);
j._field=o;
this._showField(j)
}}}}}});
function a(g,f){c.extend(g,f);
for(var e in f){if(f[e]==null){g[e]=null
}}return g
}c.fn.timeEntry=function(f){var e=Array.prototype.slice.call(arguments,1);
if(typeof f=="string"&&(f=="isDisabled"||f=="getTime")){return c.timeEntry["_"+f+"TimeEntry"].apply(c.timeEntry,[this[0]].concat(e))
}return this.each(function(){var h=this.nodeName.toLowerCase();
if(h=="input"){if(typeof f=="string"){c.timeEntry["_"+f+"TimeEntry"].apply(c.timeEntry,[this].concat(e))
}else{var g=(c.fn.metadata?c(this).metadata():{});
c.timeEntry._connectTimeEntry(this,c.extend(g,f))
}}})
};
c.timeEntry=new b()
})(jQuery);