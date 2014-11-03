(function(d){function c(){this._disabledInputs=[];
this.regional=[];
this.regional[""]={show24Hours:false,separator:":",ampmPrefix:"",ampmNames:["AM","PM"],spinnerTexts:["Now","Previous field","Next field","Increment","Decrement"]};
this._defaults={appendText:"",showSeconds:false,timeSteps:[1,1,1],initialField:0,useMouseWheel:true,defaultTime:null,minTime:null,maxTime:null,spinnerImage:"spinnerDefault.png",spinnerSize:[20,20,8],spinnerBigImage:"",spinnerBigSize:[40,40,16],spinnerIncDecOnly:false,spinnerRepeat:[500,250],beforeShow:null,beforeSetTime:null};
d.extend(this._defaults,this.regional[""])
}var a="timeEntry";
d.extend(c.prototype,{markerClassName:"hasTimeEntry",setDefaults:function(e){b(this._defaults,e||{});
return this
},_connectTimeEntry:function(s,r){var q=d(s);
if(q.hasClass(this.markerClassName)){return
}var p={};
p.options=d.extend({},r);
p._selectedHour=0;
p._selectedMinute=0;
p._selectedSecond=0;
p._field=0;
p.input=d(s);
d.data(s,a,p);
var o=this._get(p,"spinnerImage");
var n=this._get(p,"spinnerText");
var m=this._get(p,"spinnerSize");
var l=this._get(p,"appendText");
var k=(!o?null:d('<span class="timeEntry_control" style="display: inline-block; background: url(\''+o+"') 0 0 no-repeat; width: "+m[0]+"px; height: "+m[1]+"px;"+(d.browser.mozilla&&d.browser.version<"1.9"?" padding-left: "+m[0]+"px; padding-bottom: "+(m[1]-18)+"px;":"")+'"></span>'));
q.wrap('<span class="timeEntry_wrap"></span>').after(l?'<span class="timeEntry_append">'+l+"</span>":"").after(k||"");
q.addClass(this.markerClassName).bind("focus.timeEntry",this._doFocus).bind("blur.timeEntry",this._doBlur).bind("click.timeEntry",this._doClick).bind("keydown.timeEntry",this._doKeyDown).bind("keypress.timeEntry",this._doKeyPress);
if(d.browser.mozilla){q.bind("input.timeEntry",function(e){d.timeentry._parseTime(p)
})
}if(d.browser.msie){q.bind("paste.timeEntry",function(e){setTimeout(function(){d.timeentry._parseTime(p)
},1)
})
}if(this._get(p,"useMouseWheel")&&d.fn.mousewheel){q.mousewheel(this._doMouseWheel)
}if(k){k.mousedown(this._handleSpinner).mouseup(this._endSpinner).mouseover(this._expandSpinner).mouseout(this._endSpinner).mousemove(this._describeSpinner)
}},_enableTimeEntry:function(e){this._enableDisable(e,false)
},_disableTimeEntry:function(e){this._enableDisable(e,true)
},_enableDisable:function(e,g){var f=d.data(e,a);
if(!f){return
}e.disabled=g;
if(e.nextSibling&&e.nextSibling.nodeName.toLowerCase()=="span"){d.timeEntry._changeSpinner(f,e.nextSibling,(g?5:-1))
}d.timeEntry._disabledInputs=d.map(d.timeEntry._disabledInputs,function(h){return(h==e?null:h)
});
if(g){d.timeEntry._disabledInputs.push(e)
}},_isDisabledTimeEntry:function(e){return d.inArray(e,this._disabledInputs)>-1
},_changeTimeEntry:function(f,e){var h=d.data(f,a);
if(h){var g=this._extractTime(h);
b(h.options,e||{});
if(g){this._setTime(h,new Date(0,0,0,g[0],g[1],g[2]))
}}d.data(f,a,h)
},_destroyTimeEntry:function(e){$input=d(e);
if(!$input.hasClass(this.markerClassName)){return
}$input.removeClass(this.markerClassName).unbind(".timeEntry");
if(d.fn.mousewheel){$input.unmousewheel()
}this._disabledInputs=d.map(this._disabledInputs,function(f){return(f==e?null:f)
});
$input.parent().replaceWith($input);
d.removeData(e,a)
},_setTimeTimeEntry:function(f,e){var g=d.data(f,a);
if(g){this._setTime(g,e?(typeof e=="object"?new Date(e.getTime()):e):null)
}},_getTimeTimeEntry:function(f){var e=d.data(f,a);
var g=(e?this._extractTime(e):null);
return(!g?null:new Date(0,0,0,g[0],g[1],g[2]))
},_doFocus:function(f){var e=(f.nodeName&&f.nodeName.toLowerCase()=="input"?f:this);
if(d.timeEntry._lastInput==e||d.timeEntry._isDisabledTimeEntry(e)){d.timeEntry._focussed=false;
return
}var h=d.data(e,a);
d.timeEntry._focussed=true;
d.timeEntry._lastInput=e;
d.timeEntry._blurredInput=null;
var g=d.timeEntry._get(h,"beforeShow");
b(h.options,(g?g.apply(e,[e]):{}));
d.data(e,a,h);
d.timeEntry._parseTime(h);
setTimeout(function(){d.timeEntry._showField(h)
},10)
},_doBlur:function(e){d.timeEntry._blurredInput=d.timeEntry._lastInput;
d.timeEntry._lastInput=null
},_doClick:function(u){var t=u.target;
var s=d.data(t,a);
if(!d.timeEntry._focussed){var r=d.timeEntry._get(s,"separator").length+2;
s._field=0;
if(t.selectionStart!=null){for(var q=0;
q<=Math.max(1,s._secondField,s._ampmField);
q++){var p=(q!=s._ampmField?(q*r)+2:(s._ampmField*r)+d.timeEntry._get(s,"ampmPrefix").length+d.timeEntry._get(s,"ampmNames")[0].length);
s._field=q;
if(t.selectionStart<p){break
}}}else{if(t.createTextRange){var o=d(u.srcElement);
var n=t.createTextRange();
var m=function(e){return{thin:2,medium:4,thick:6}[e]||e
};
var l=u.clientX+document.documentElement.scrollLeft-(o.offset().left+parseInt(m(o.css("border-left-width")),10))-n.offsetLeft;
for(var q=0;
q<=Math.max(1,s._secondField,s._ampmField);
q++){var p=(q!=s._ampmField?(q*r)+2:(s._ampmField*r)+d.timeEntry._get(s,"ampmPrefix").length+d.timeEntry._get(s,"ampmNames")[0].length);
n.collapse();
n.moveEnd("character",p);
s._field=q;
if(l<n.boundingWidth){break
}}}}}d.data(t,a,s);
d.timeEntry._showField(s);
d.timeEntry._focussed=false
},_doKeyDown:function(f){if(f.keyCode>=48){return true
}var e=d.data(f.target,a);
switch(f.keyCode){case 9:return(f.shiftKey?d.timeEntry._changeField(e,-1,true):d.timeEntry._changeField(e,+1,true));
case 35:if(f.ctrlKey){d.timeEntry._setValue(e,"")
}else{e._field=Math.max(1,e._secondField,e._ampmField);
d.timeEntry._adjustField(e,0)
}break;
case 36:if(f.ctrlKey){d.timeEntry._setTime(e)
}else{e._field=0;
d.timeEntry._adjustField(e,0)
}break;
case 37:d.timeEntry._changeField(e,-1,false);
break;
case 38:d.timeEntry._adjustField(e,+1);
break;
case 39:d.timeEntry._changeField(e,+1,false);
break;
case 40:d.timeEntry._adjustField(e,-1);
break;
case 46:d.timeEntry._setValue(e,"");
break
}return false
},_doKeyPress:function(f){var e=String.fromCharCode(f.charCode==undefined?f.keyCode:f.charCode);
if(e<" "){return true
}var g=d.data(f.target,a);
d.timeEntry._handleKeyPress(g,e);
return false
},_doMouseWheel:function(f,e){if(d.timeEntry._isDisabledTimeEntry(f.target)){return
}e=(d.browser.opera?-e/Math.abs(e):(d.browser.safari?e/Math.abs(e):e));
var g=d.data(f.target,a);
g.input.focus();
if(!g.input.val()){d.timeEntry._parseTime(g)
}d.timeEntry._adjustField(g,e);
f.preventDefault()
},_expandSpinner:function(j){var q=d.timeEntry._getSpinnerTarget(j);
var p=d.data(d.timeEntry._getInput(q),a);
var o=d.timeEntry._get(p,"spinnerBigImage");
if(o){p._expanded=true;
var n=d(q).offset();
var m=null;
d(q).parents().each(function(){var e=d(this);
if(e.css("position")=="relative"||e.css("position")=="absolute"){m=e.offset()
}return !m
});
var l=d.timeEntry._get(p,"spinnerSize");
var k=d.timeEntry._get(p,"spinnerBigSize");
d('<div class="timeEntry_expand" style="position: absolute; left: '+(n.left-(k[0]-l[0])/2-(m?m.left:0))+"px; top: "+(n.top-(k[1]-l[1])/2-(m?m.top:0))+"px; width: "+k[0]+"px; height: "+k[1]+"px; background: transparent url("+o+') no-repeat 0px 0px; z-index: 10;"></div>').mousedown(d.timeEntry._handleSpinner).mouseup(d.timeEntry._endSpinner).mouseout(d.timeEntry._endExpand).mousemove(d.timeEntry._describeSpinner).insertAfter(q)
}},_getInput:function(e){return d(e).siblings("."+d.timeEntry.markerClassName)[0]
},_describeSpinner:function(f){var e=d.timeEntry._getSpinnerTarget(f);
var g=d.data(d.timeEntry._getInput(e),a);
e.title=d.timeEntry._get(g,"spinnerTexts")[d.timeEntry._getSpinnerRegion(g,f)]
},_handleSpinner:function(h){var g=d.timeEntry._getSpinnerTarget(h);
var l=d.timeEntry._getInput(g);
if(d.timeEntry._isDisabledTimeEntry(l)){return
}if(l==d.timeEntry._blurredInput){d.timeEntry._lastInput=l;
d.timeEntry._blurredInput=null
}var k=d.data(l,a);
d.timeEntry._doFocus(l);
var j=d.timeEntry._getSpinnerRegion(k,h);
d.timeEntry._changeSpinner(k,g,j);
d.timeEntry._actionSpinner(k,j);
d.timeEntry._timer=null;
d.timeEntry._handlingSpinner=true;
var i=d.timeEntry._get(k,"spinnerRepeat");
if(j>=3&&i[0]){d.timeEntry._timer=setTimeout(function(){d.timeEntry._repeatSpinner(k,j)
},i[0]);
d(g).one("mouseout",d.timeEntry._releaseSpinner).one("mouseup",d.timeEntry._releaseSpinner)
}},_actionSpinner:function(f,e){if(!f.input.val()){d.timeEntry._parseTime(f)
}switch(e){case 0:this._setTime(f);
break;
case 1:this._changeField(f,-1,false);
break;
case 2:this._changeField(f,+1,false);
break;
case 3:this._adjustField(f,+1);
break;
case 4:this._adjustField(f,-1);
break
}},_repeatSpinner:function(f,e){if(!d.timeEntry._timer){return
}d.timeEntry._lastInput=d.timeEntry._blurredInput;
this._actionSpinner(f,e);
this._timer=setTimeout(function(){d.timeEntry._repeatSpinner(f,e)
},this._get(f,"spinnerRepeat")[1])
},_releaseSpinner:function(e){clearTimeout(d.timeEntry._timer);
d.timeEntry._timer=null
},_endExpand:function(f){d.timeEntry._timer=null;
var e=d.timeEntry._getSpinnerTarget(f);
var h=d.timeEntry._getInput(e);
var g=d.data(h,a);
d(e).remove();
g._expanded=false
},_endSpinner:function(f){d.timeEntry._timer=null;
var e=d.timeEntry._getSpinnerTarget(f);
var h=d.timeEntry._getInput(e);
var g=d.data(h,a);
if(!d.timeEntry._isDisabledTimeEntry(h)){d.timeEntry._changeSpinner(g,e,-1)
}if(d.timeEntry._handlingSpinner){d.timeEntry._lastInput=d.timeEntry._blurredInput
}if(d.timeEntry._lastInput&&d.timeEntry._handlingSpinner){d.timeEntry._showField(g)
}d.timeEntry._handlingSpinner=false
},_getSpinnerTarget:function(e){return e.target||e.srcElement
},_getSpinnerRegion:function(x,w){var v=this._getSpinnerTarget(w);
var u=(d.browser.opera||d.browser.safari?d.timeEntry._findPos(v):d(v).offset());
var t=(d.browser.safari?d.timeEntry._findScroll(v):[document.documentElement.scrollLeft||document.body.scrollLeft,document.documentElement.scrollTop||document.body.scrollTop]);
var s=this._get(x,"spinnerIncDecOnly");
var r=(s?99:w.clientX+t[0]-u.left-(d.browser.msie?2:0));
var q=w.clientY+t[1]-u.top-(d.browser.msie?2:0);
var p=this._get(x,(x._expanded?"spinnerBigSize":"spinnerSize"));
var o=(s?99:p[0]-1-r);
var n=p[1]-1-q;
if(p[2]>0&&Math.abs(r-o)<=p[2]&&Math.abs(q-n)<=p[2]){return 0
}var m=Math.min(r,q,o,n);
return(m==r?1:(m==o?2:(m==q?3:4)))
},_changeSpinner:function(f,e,g){d(e).css("background-position","-"+((g+1)*this._get(f,(f._expanded?"spinnerBigSize":"spinnerSize"))[0])+"px 0px")
},_findPos:function(f){var e=curTop=0;
if(f.offsetParent){e=f.offsetLeft;
curTop=f.offsetTop;
while(f=f.offsetParent){var g=e;
e+=f.offsetLeft;
if(e<0){e=g
}curTop+=f.offsetTop
}}return{left:e,top:curTop}
},_findScroll:function(f){var e=false;
d(f).parents().each(function(){e|=d(this).css("position")=="fixed"
});
if(e){return[0,0]
}var h=f.scrollLeft;
var g=f.scrollTop;
while(f=f.parentNode){h+=f.scrollLeft||0;
g+=f.scrollTop||0
}return[h,g]
},_get:function(f,e){return(f.options[e]!=null?f.options[e]:d.timeEntry._defaults[e])
},_parseTime:function(f){var e=this._extractTime(f);
var h=this._get(f,"showSeconds");
if(e){f._selectedHour=e[0];
f._selectedMinute=e[1];
f._selectedSecond=e[2]
}else{var g=this._constrainTime(f);
f._selectedHour=g[0];
f._selectedMinute=g[1];
f._selectedSecond=(h?g[2]:0)
}f._secondField=(h?2:-1);
f._ampmField=(this._get(f,"show24Hours")?-1:(h?3:2));
f._lastChr="";
f._field=Math.max(0,Math.min(Math.max(1,f._secondField,f._ampmField),this._get(f,"initialField")));
if(f.input.val()!=""){this._showTime(f)
}},_extractTime:function(v){var u=v.input.val();
var t=this._get(v,"separator");
var s=u.split(t);
if(t==""&&u!=""){s[0]=u.substring(0,2);
s[1]=u.substring(2,4);
s[2]=u.substring(4,6)
}var r=this._get(v,"ampmNames");
var q=this._get(v,"show24Hours");
if(s.length>=2){var p=!q&&(u.indexOf(r[0])>-1);
var o=!q&&(u.indexOf(r[1])>-1);
var n=parseInt(s[0],10);
n=(isNaN(n)?0:n);
n=((p||o)&&n==12?0:n)+(o?12:0);
var m=parseInt(s[1],10);
m=(isNaN(m)?0:m);
var l=(s.length>=3?parseInt(s[2],10):0);
l=(isNaN(l)||!this._get(v,"showSeconds")?0:l);
return this._constrainTime(v,[n,m,l])
}return null
},_constrainTime:function(h,g){var n=(g!=null);
if(!n){var m=this._determineTime(this._get(h,"defaultTime"))||new Date();
g=[m.getHours(),m.getMinutes(),m.getSeconds()]
}var l=false;
var k=this._get(h,"timeSteps");
for(var j=0;
j<k.length;
j++){if(l){g[j]=0
}else{if(k[j]>1){g[j]=Math.round(g[j]/k[j])*k[j];
l=true
}}}return g
},_showTime:function(f){var e=this._get(f,"show24Hours");
var h=this._get(f,"separator");
var g=(this._formatNumber(e?f._selectedHour:((f._selectedHour+11)%12)+1)+h+this._formatNumber(f._selectedMinute)+(this._get(f,"showSeconds")?h+this._formatNumber(f._selectedSecond):"")+(e?"":this._get(f,"ampmPrefix")+this._get(f,"ampmNames")[(f._selectedHour<12?0:1)]));
this._setValue(f,g);
this._showField(f)
},_showField:function(i){var h=i.input[0];
if(i.input.is(":hidden")||d.timeEntry._lastInput!=h){return
}var n=this._get(i,"separator");
var m=n.length+2;
var l=(i._field!=i._ampmField?(i._field*m):(i._ampmField*m)-n.length+this._get(i,"ampmPrefix").length);
var k=l+(i._field!=i._ampmField?2:this._get(i,"ampmNames")[0].length);
if(h.setSelectionRange){h.setSelectionRange(l,k)
}else{if(h.createTextRange){var j=h.createTextRange();
j.moveStart("character",l);
j.moveEnd("character",k-i.input.val().length);
j.select()
}}if(!h.disabled){h.focus()
}},_formatNumber:function(e){return(e<10?"0":"")+e
},_setValue:function(f,e){if(e!=f.input.val()){f.input.val(e).trigger("change")
}},_changeField:function(f,e,h){var g=(f.input.val()==""||f._field==(e==-1?0:Math.max(1,f._secondField,f._ampmField)));
if(!g){f._field+=e
}this._showField(f);
f._lastChr="";
d.data(f.input[0],a,f);
return(g&&h)
},_adjustField:function(f,e){if(f.input.val()==""){e=0
}var g=this._get(f,"timeSteps");
this._setTime(f,new Date(0,0,0,f._selectedHour+(f._field==0?e*g[0]:0)+(f._field==f._ampmField?e*12:0),f._selectedMinute+(f._field==1?e*g[1]:0),f._selectedSecond+(f._field==f._secondField?e*g[2]:0)))
},_setTime:function(h,g){g=this._determineTime(g);
var l=this._constrainTime(h,g?[g.getHours(),g.getMinutes(),g.getSeconds()]:null);
g=new Date(0,0,0,l[0],l[1],l[2]);
var g=this._normaliseTime(g);
var k=this._normaliseTime(this._determineTime(this._get(h,"minTime")));
var j=this._normaliseTime(this._determineTime(this._get(h,"maxTime")));
g=(k&&g<k?k:(j&&g>j?j:g));
var i=this._get(h,"beforeSetTime");
if(i){g=i.apply(h.input[0],[this._getTimeTimeEntry(h.input[0]),g,k,j])
}h._selectedHour=g.getHours();
h._selectedMinute=g.getMinutes();
h._selectedSecond=g.getSeconds();
this._showTime(h);
d.data(h.input[0],a,h)
},_determineTime:function(g){var f=function(i){var h=new Date();
h.setTime(h.getTime()+i*1000);
return h
};
var e=function(i){var h=new Date();
var n=h.getHours();
var m=h.getMinutes();
var l=h.getSeconds();
var k=/([+-]?[0-9]+)\s*(s|S|m|M|h|H)?/g;
var j=k.exec(i);
while(j){switch(j[2]||"s"){case"s":case"S":l+=parseInt(j[1],10);
break;
case"m":case"M":m+=parseInt(j[1],10);
break;
case"h":case"H":n+=parseInt(j[1],10);
break
}j=k.exec(i)
}h=new Date(0,0,10,n,m,l,0);
if(/^!/.test(i)){if(h.getDate()>10){h=new Date(0,0,10,23,59,59)
}else{if(h.getDate()<10){h=new Date(0,0,10,0,0,0)
}}}return h
};
return(g?(typeof g=="string"?e(g):(typeof g=="number"?f(g):g)):null)
},_normaliseTime:function(e){if(!e){return null
}e.setFullYear(1900);
e.setMonth(0);
e.setDate(0);
return e
},_handleKeyPress:function(v,u){if(u==this._get(v,"separator")){this._changeField(v,+1,false)
}else{if(u>="0"&&u<="9"){var t=parseInt(u,10);
var s=parseInt(v._lastChr+u,10);
var r=this._get(v,"show24Hours");
var q=(v._field!=0?v._selectedHour:(r?(s<24?s:t):(s>=1&&s<=12?s:(t>0?t:v._selectedHour))%12+(v._selectedHour>=12?12:0)));
var p=(v._field!=1?v._selectedMinute:(s<60?s:t));
var o=(v._field!=v._secondField?v._selectedSecond:(s<60?s:t));
var n=this._constrainTime(v,[q,p,o]);
this._setTime(v,new Date(0,0,0,n[0],n[1],n[2]));
v._lastChr=u
}else{if(!this._get(v,"show24Hours")){var m=this._get(v,"ampmNames");
if((u==m[0].substring(0,1).toLowerCase()&&v._selectedHour>=12)||(u==m[1].substring(0,1).toLowerCase()&&v._selectedHour<12)){var l=v._field;
v._field=v._ampmField;
this._adjustField(v,+1);
v._field=l;
this._showField(v)
}}}}}});
function b(f,e){d.extend(f,e);
for(var g in e){if(e[g]==null){f[g]=null
}}return f
}d.fn.timeEntry=function(f){var e=Array.prototype.slice.call(arguments,1);
if(typeof f=="string"&&(f=="isDisabled"||f=="getTime")){return d.timeEntry["_"+f+"TimeEntry"].apply(d.timeEntry,[this[0]].concat(e))
}return this.each(function(){var h=this.nodeName.toLowerCase();
if(h=="input"){if(typeof f=="string"){d.timeEntry["_"+f+"TimeEntry"].apply(d.timeEntry,[this].concat(e))
}else{var g=(d.fn.metadata?d(this).metadata():{});
d.timeEntry._connectTimeEntry(this,d.extend(g,f))
}}})
};
d.timeEntry=new c()
})(jQuery);