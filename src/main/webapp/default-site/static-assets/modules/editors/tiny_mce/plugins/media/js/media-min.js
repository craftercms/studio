(function(){var b;
if(b=tinyMCEPopup.getParam("media_external_list_url")){document.write('<script language="javascript" type="text/javascript" src="'+tinyMCEPopup.editor.documentBaseURI.toAbsolute(b)+'"><\/script>')
}function a(f){return document.getElementById(f)
}function e(j){var h,g,k,f;
if(null==j||"object"!=typeof j){return j
}if("length" in j){k=[];
for(h=0,g=j.length;
h<g;
++h){k[h]=e(j[h])
}return k
}k={};
for(f in j){if(j.hasOwnProperty(f)){k[f]=e(j[f])
}}return k
}function d(g){var f=a(g);
if(f.nodeName=="SELECT"){return f.options[f.selectedIndex].value
}if(f.type=="checkbox"){return f.checked
}return f.value
}function c(i,g,f){if(typeof(g)!="undefined"&&g!=null){var h=a(i);
if(h.nodeName=="SELECT"){selectByValue(document.forms[0],i,g)
}else{if(h.type=="checkbox"){if(typeof(g)=="string"){g=g.toLowerCase();
g=(!f&&g==="true")||(f&&g===f.toLowerCase())
}h.checked=!!g
}else{h.value=g
}}}}window.Media={init:function(){var g,h,f=this;
f.editor=h=tinyMCEPopup.editor;
a("filebrowsercontainer").innerHTML=getBrowserHTML("filebrowser","src","media","media");
a("qtsrcfilebrowsercontainer").innerHTML=getBrowserHTML("qtsrcfilebrowser","quicktime_qtsrc","media","media");
a("bgcolor_pickcontainer").innerHTML=getColorPickerHTML("bgcolor_pick","bgcolor");
a("video_altsource1_filebrowser").innerHTML=getBrowserHTML("video_filebrowser_altsource1","video_altsource1","media","media");
a("video_altsource2_filebrowser").innerHTML=getBrowserHTML("video_filebrowser_altsource2","video_altsource2","media","media");
a("audio_altsource1_filebrowser").innerHTML=getBrowserHTML("audio_filebrowser_altsource1","audio_altsource1","media","media");
a("audio_altsource2_filebrowser").innerHTML=getBrowserHTML("audio_filebrowser_altsource2","audio_altsource2","media","media");
a("video_poster_filebrowser").innerHTML=getBrowserHTML("filebrowser_poster","video_poster","image","media");
g=f.getMediaListHTML("medialist","src","media","media");
if(g==""){a("linklistrow").style.display="none"
}else{a("linklistcontainer").innerHTML=g
}if(isVisible("filebrowser")){a("src").style.width="230px"
}if(isVisible("video_filebrowser_altsource1")){a("video_altsource1").style.width="220px"
}if(isVisible("video_filebrowser_altsource2")){a("video_altsource2").style.width="220px"
}if(isVisible("audio_filebrowser_altsource1")){a("audio_altsource1").style.width="220px"
}if(isVisible("audio_filebrowser_altsource2")){a("audio_altsource2").style.width="220px"
}if(isVisible("filebrowser_poster")){a("video_poster").style.width="220px"
}h.dom.setOuterHTML(a("media_type"),f.getMediaTypeHTML(h));
f.setDefaultDialogSettings(h);
f.data=e(tinyMCEPopup.getWindowArg("data"));
f.dataToForm();
f.preview();
updateColor("bgcolor_pick","bgcolor")
},insert:function(){var f=tinyMCEPopup.editor;
this.formToData();
f.execCommand("mceRepaint");
tinyMCEPopup.restoreSelection();
f.selection.setNode(f.plugins.media.dataToImg(this.data));
tinyMCEPopup.close()
},preview:function(){a("prev").innerHTML=this.editor.plugins.media.dataToHtml(this.data,true)
},moveStates:function(o,n){var j=this.data,k=this.editor,l=k.plugins.media,i,f,p,g,f;
g={quicktime_autoplay:true,quicktime_controller:true,flash_play:true,flash_loop:true,flash_menu:true,windowsmedia_autostart:true,windowsmedia_enablecontextmenu:true,windowsmedia_invokeurls:true,realmedia_autogotourl:true,realmedia_imagestatus:true};
function h(r){var q={};
if(r){tinymce.each(r.split("&"),function(s){var t=s.split("=");
q[unescape(t[0])]=unescape(t[1])
})
}return q
}function m(t,w){var s,r,q,v,u;
if(t==j.type||t=="global"){w=tinymce.explode(w);
for(s=0;
s<w.length;
s++){r=w[s];
q=t=="global"?r:t+"_"+r;
if(t=="global"){u=j
}else{if(t=="video"||t=="audio"){u=j.video.attrs;
if(!u&&!o){j.video.attrs=u={}
}}else{u=j.params
}}if(u){if(o){c(q,u[r],t=="video"||t=="audio"?r:"")
}else{delete u[r];
v=d(q);
if((t=="video"||t=="audio")&&v===true){v=r
}if(g[q]){if(v!==g[q]){v=""+v;
u[r]=v
}}else{if(v){v=""+v;
u[r]=v
}}}}}}}if(!o){j.type=a("media_type").options[a("media_type").selectedIndex].value;
j.width=d("width");
j.height=d("height");
f=d("src");
if(n=="src"){i=f.replace(/^.*\.([^.]+)$/,"$1");
if(p=l.getType(i)){j.type=p.name.toLowerCase()
}c("media_type",j.type)
}if(j.type=="video"||j.type=="audio"){if(!j.video.sources){j.video.sources=[]
}j.video.sources[0]={src:d("src")}
}}a("video_options").style.display="none";
a("audio_options").style.display="none";
a("flash_options").style.display="none";
a("quicktime_options").style.display="none";
a("shockwave_options").style.display="none";
a("windowsmedia_options").style.display="none";
a("realmedia_options").style.display="none";
a("embeddedaudio_options").style.display="none";
if(a(j.type+"_options")){a(j.type+"_options").style.display="block"
}c("media_type",j.type);
m("flash","play,loop,menu,swliveconnect,quality,scale,salign,wmode,base,flashvars");
m("quicktime","loop,autoplay,cache,controller,correction,enablejavascript,kioskmode,autohref,playeveryframe,targetcache,scale,starttime,endtime,target,qtsrcchokespeed,volume,qtsrc");
m("shockwave","sound,progress,autostart,swliveconnect,swvolume,swstretchstyle,swstretchhalign,swstretchvalign");
m("windowsmedia","autostart,enabled,enablecontextmenu,fullscreen,invokeurls,mute,stretchtofit,windowlessvideo,balance,baseurl,captioningid,currentmarker,currentposition,defaultframe,playcount,rate,uimode,volume");
m("realmedia","autostart,loop,autogotourl,center,imagestatus,maintainaspect,nojava,prefetch,shuffle,console,controls,numloop,scriptcallbacks");
m("video","poster,autoplay,loop,muted,preload,controls");
m("audio","autoplay,loop,preload,controls");
m("embeddedaudio","autoplay,loop,controls");
m("global","id,name,vspace,hspace,bgcolor,align,width,height");
if(o){if(j.type=="video"){if(j.video.sources[0]){c("src",j.video.sources[0].src)
}f=j.video.sources[1];
if(f){c("video_altsource1",f.src)
}f=j.video.sources[2];
if(f){c("video_altsource2",f.src)
}}else{if(j.type=="audio"){if(j.video.sources[0]){c("src",j.video.sources[0].src)
}f=j.video.sources[1];
if(f){c("audio_altsource1",f.src)
}f=j.video.sources[2];
if(f){c("audio_altsource2",f.src)
}}else{if(j.type=="flash"){tinymce.each(k.getParam("flash_video_player_flashvars",{url:"$url",poster:"$poster"}),function(r,q){if(r=="$url"){j.params.src=h(j.params.flashvars)[q]||j.params.src||""
}})
}c("src",j.params.src)
}}}else{f=d("src");
if(f.match(/youtube\.com\/embed\/\w+/)){j.width=425;
j.height=350;
j.params.frameborder="0";
j.type="iframe";
c("src",f);
c("media_type",j.type)
}else{if(f.match(/youtu\.be\/[a-z1-9.-_]+/)){j.width=425;
j.height=350;
j.params.frameborder="0";
j.type="iframe";
f="http://www.youtube.com/embed/"+f.match(/youtu.be\/([a-z1-9.-_]+)/)[1];
c("src",f);
c("media_type",j.type)
}if(f.match(/youtube\.com(.+)v=([^&]+)/)){j.width=425;
j.height=350;
j.params.frameborder="0";
j.type="iframe";
f="http://www.youtube.com/embed/"+f.match(/v=([^&]+)/)[1];
c("src",f);
c("media_type",j.type)
}}if(f.match(/video\.google\.com(.+)docid=([^&]+)/)){j.width=425;
j.height=326;
j.type="flash";
f="http://video.google.com/googleplayer.swf?docId="+f.match(/docid=([^&]+)/)[1]+"&hl=en";
c("src",f);
c("media_type",j.type)
}if(f.match(/vimeo\.com\/([0-9]+)/)){j.width=425;
j.height=350;
j.params.frameborder="0";
j.type="iframe";
f="http://player.vimeo.com/video/"+f.match(/vimeo.com\/([0-9]+)/)[1];
c("src",f);
c("media_type",j.type)
}if(f.match(/stream\.cz\/((?!object).)*\/([0-9]+)/)){j.width=425;
j.height=350;
j.params.frameborder="0";
j.type="iframe";
f="http://www.stream.cz/object/"+f.match(/stream.cz\/[^/]+\/([0-9]+)/)[1];
c("src",f);
c("media_type",j.type)
}if(f.match(/maps\.google\.([a-z]{2,3})\/maps\/(.+)msid=(.+)/)){j.width=425;
j.height=350;
j.params.frameborder="0";
j.type="iframe";
f="http://maps.google.com/maps/ms?msid="+f.match(/msid=(.+)/)[1]+"&output=embed";
c("src",f);
c("media_type",j.type)
}if(j.type=="video"){if(!j.video.sources){j.video.sources=[]
}j.video.sources[0]={src:f};
f=d("video_altsource1");
if(f){j.video.sources[1]={src:f}
}f=d("video_altsource2");
if(f){j.video.sources[2]={src:f}
}}else{if(j.type=="audio"){if(!j.video.sources){j.video.sources=[]
}j.video.sources[0]={src:f};
f=d("audio_altsource1");
if(f){j.video.sources[1]={src:f}
}f=d("audio_altsource2");
if(f){j.video.sources[2]={src:f}
}}else{j.params.src=f
}}c("width",j.width||(j.type=="audio"?300:320));
c("height",j.height||(j.type=="audio"?32:240))
}},dataToForm:function(){this.moveStates(true)
},formToData:function(f){if(f=="width"||f=="height"){this.changeSize(f)
}if(f=="source"){this.moveStates(false,f);
c("source",this.editor.plugins.media.dataToHtml(this.data));
this.panel="source"
}else{if(this.panel=="source"){this.data=e(this.editor.plugins.media.htmlToData(d("source")));
this.dataToForm();
this.panel=""
}this.moveStates(false,f);
this.preview()
}},beforeResize:function(){this.width=parseInt(d("width")||(this.data.type=="audio"?"300":"320"),10);
this.height=parseInt(d("height")||(this.data.type=="audio"?"32":"240"),10)
},changeSize:function(i){var h,f,j,g;
if(a("constrain").checked){h=parseInt(d("width")||(this.data.type=="audio"?"300":"320"),10);
f=parseInt(d("height")||(this.data.type=="audio"?"32":"240"),10);
if(i=="width"){this.height=Math.round((h/this.width)*f);
c("height",this.height)
}else{this.width=Math.round((f/this.height)*h);
c("width",this.width)
}}},getMediaListHTML:function(){if(typeof(tinyMCEMediaList)!="undefined"&&tinyMCEMediaList.length>0){var g="";
g+='<select id="linklist" name="linklist" style="width: 250px" onchange="this.form.src.value=this.options[this.selectedIndex].value;Media.formToData(\'src\');">';
g+='<option value="">---</option>';
for(var f=0;
f<tinyMCEMediaList.length;
f++){g+='<option value="'+tinyMCEMediaList[f][1]+'">'+tinyMCEMediaList[f][0]+"</option>"
}g+="</select>";
return g
}return""
},getMediaTypeHTML:function(h){function g(j,i){if(!h.schema.getElementRule(i||j)){return""
}return'<option value="'+j+'">'+tinyMCEPopup.editor.translate("media_dlg."+j)+"</option>"
}var f="";
f+='<select id="media_type" name="media_type" onchange="Media.formToData(\'type\');">';
f+=g("video");
f+=g("audio");
f+=g("flash","object");
f+=g("quicktime","object");
f+=g("shockwave","object");
f+=g("windowsmedia","object");
f+=g("realmedia","object");
f+=g("iframe");
if(h.getParam("media_embedded_audio",false)){f+=g("embeddedaudio","object")
}f+="</select>";
return f
},setDefaultDialogSettings:function(g){var f=g.getParam("media_dialog_defaults",{});
tinymce.each(f,function(i,h){c(h,i)
})
}};
tinyMCEPopup.requireLangPack();
tinyMCEPopup.onInit.add(function(){Media.init()
})
})();