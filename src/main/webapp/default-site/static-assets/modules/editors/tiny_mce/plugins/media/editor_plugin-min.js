(function(){var q=tinymce.explode("id,name,width,height,style,align,class,hspace,vspace,bgcolor,type"),r=tinymce.makeMap(q.join(",")),m=tinymce.html.Node,o,j,k=tinymce.util.JSON,l;
o=[["Flash","d27cdb6e-ae6d-11cf-96b8-444553540000","application/x-shockwave-flash","http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,40,0"],["ShockWave","166b1bca-3f9c-11cf-8075-444553540000","application/x-director","http://download.macromedia.com/pub/shockwave/cabs/director/sw.cab#version=8,5,1,0"],["WindowsMedia","6bf52a52-394a-11d3-b153-00c04f79faa6,22d6f312-b0f6-11d0-94ab-0080c74c7e95,05589fa1-c356-11ce-bf01-00aa0055595a","application/x-mplayer2","http://activex.microsoft.com/activex/controls/mplayer/en/nsmp2inf.cab#Version=5,1,52,701"],["QuickTime","02bf25d5-8c17-4b23-bc80-d3488abddc6b","video/quicktime","http://www.apple.com/qtactivex/qtplugin.cab#version=6,0,2,0"],["RealMedia","cfcdaa03-8be4-11cf-b84b-0020afbbccfa","audio/x-pn-realaudio-plugin","http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,40,0"],["Java","8ad9c840-044e-11d1-b3e9-00805f499d93","application/x-java-applet","http://java.sun.com/products/plugin/autodl/jinstall-1_5_0-windows-i586.cab#Version=1,5,0,0"],["Silverlight","dfeaf541-f3e1-4c24-acac-99c30715084a","application/x-silverlight-2"],["Iframe"],["Video"],["EmbeddedAudio"],["Audio"]];
function n(a){return typeof(a)=="string"?a.replace(/[^0-9%]/g,""):a
}function p(a){var b,d,c;
if(a&&!a.splice){d=[];
for(c=0;
true;
c++){if(a[c]){d[c]=a[c]
}else{break
}}return d
}return a
}tinymce.create("tinymce.plugins.MediaPlugin",{init:function(e,i){var a=this,g={},f,c,b,h;
function d(t){return t&&t.nodeName==="IMG"&&e.dom.hasClass(t,"mceItemMedia")
}a.editor=e;
a.url=i;
j="";
for(f=0;
f<o.length;
f++){h=o[f][0];
b={name:h,clsids:tinymce.explode(o[f][1]||""),mimes:tinymce.explode(o[f][2]||""),codebase:o[f][3]};
for(c=0;
c<b.clsids.length;
c++){g["clsid:"+b.clsids[c]]=b
}for(c=0;
c<b.mimes.length;
c++){g[b.mimes[c]]=b
}g["mceItem"+h]=b;
g[h.toLowerCase()]=b;
j+=(j?"|":"")+h
}tinymce.each(e.getParam("media_types","video=mp4,m4v,ogv,webm;silverlight=xap;flash=swf,flv;shockwave=dcr;quicktime=mov,qt,mpg,mpeg;shockwave=dcr;windowsmedia=avi,wmv,wm,asf,asx,wmx,wvx;realmedia=rm,ra,ram;java=jar;audio=mp3,ogg").split(";"),function(w){var z,x,y;
w=w.split(/=/);
x=tinymce.explode(w[1].toLowerCase());
for(z=0;
z<x.length;
z++){y=g[w[0].toLowerCase()];
if(y){g[x[z]]=y
}}});
j=new RegExp("write("+j+")\\(([^)]+)\\)");
a.lookup=g;
e.onPreInit.add(function(){e.schema.addValidElements("object[id|style|width|height|classid|codebase|*],param[name|value],embed[id|style|width|height|type|src|*],video[*],audio[*],source[*]");
e.parser.addNodeFilter("object,embed,video,audio,script,iframe",function(v){var u=v.length;
while(u--){a.objectToImg(v[u])
}});
e.serializer.addNodeFilter("img",function(B,z,A){var y=B.length,x;
while(y--){x=B[y];
if((x.attr("class")||"").indexOf("mceItemMedia")!==-1){a.imgToObject(x,A)
}}})
});
e.onInit.add(function(){if(e.theme&&e.theme.onResolveName){e.theme.onResolveName.add(function(v,u){if(u.name==="img"&&e.dom.hasClass(u.node,"mceItemMedia")){u.name="media"
}})
}if(e&&e.plugins.contextmenu){e.plugins.contextmenu.onContextMenu.add(function(w,v,x){if(x.nodeName==="IMG"&&x.className.indexOf("mceItemMedia")!==-1){v.add({title:"media.edit",icon:"media",cmd:"mceMedia"})
}})
}});
e.addCommand("mceMedia",function(){var u,v;
v=e.selection.getNode();
if(d(v)){u=e.dom.getAttrib(v,"data-mce-json");
if(u){u=k.parse(u);
tinymce.each(q,function(t){var s=e.dom.getAttrib(v,t);
if(s){u[t]=s
}});
u.type=a.getType(v.className).name.toLowerCase()
}}if(!u){u={type:"flash",video:{sources:[]},params:{}}
}e.windowManager.open({file:i+"/media.htm",width:430+parseInt(e.getLang("media.delta_width",0)),height:500+parseInt(e.getLang("media.delta_height",0)),inline:1},{plugin_url:i,data:u})
});
e.addButton("media",{title:"media.desc",cmd:"mceMedia"});
e.onNodeChange.add(function(w,x,v){x.setActive("media",d(v))
})
},convertUrl:function(b,f){var c=this,g=c.editor,a=g.settings,e=a.url_converter,d=a.url_converter_scope||c;
if(!b){return b
}if(f){return g.documentBaseURI.toAbsolute(b)
}return e.call(d,b,"src","object")
},getInfo:function(){return{longname:"Media",author:"Moxiecode Systems AB",authorurl:"http://tinymce.moxiecode.com",infourl:"http://wiki.moxiecode.com/index.php/TinyMCE:Plugins/media",version:tinymce.majorVersion+"."+tinymce.minorVersion}
},dataToImg:function(f,h){var a=this,d=a.editor,c=d.documentBaseURI,i,b,e,g;
f.params.src=a.convertUrl(f.params.src,h);
b=f.video.attrs;
if(b){b.src=a.convertUrl(b.src,h)
}if(b){b.poster=a.convertUrl(b.poster,h)
}i=p(f.video.sources);
if(i){for(g=0;
g<i.length;
g++){i[g].src=a.convertUrl(i[g].src,h)
}}e=a.editor.dom.create("img",{id:f.id,style:f.style,align:f.align,hspace:f.hspace,vspace:f.vspace,src:a.editor.theme.url+"/img/trans.gif","class":"mceItemMedia mceItem"+a.getType(f.type).name,"data-mce-json":k.serialize(f,"'")});
e.width=f.width=n(f.width||(f.type=="audio"?"300":"320"));
e.height=f.height=n(f.height||(f.type=="audio"?"32":"240"));
return e
},dataToHtml:function(b,a){return this.editor.serializer.serialize(this.dataToImg(b,a),{forced_root_block:"",force_absolute:a})
},htmlToData:function(b){var c,d,a;
a={type:"flash",video:{sources:[]},params:{}};
c=this.editor.parser.parse(b);
d=c.getAll("img")[0];
if(d){a=k.parse(d.attr("data-mce-json"));
a.type=this.getType(d.attr("class")).name.toLowerCase();
tinymce.each(q,function(f){var e=d.attr(f);
if(e){a[f]=e
}})
}return a
},getType:function(a){var c,d,b;
d=tinymce.explode(a," ");
for(c=0;
c<d.length;
c++){b=this.lookup[d[c]];
if(b){return b
}}},imgToObject:function(a,U){var J=this,T=J.editor,P,e,Y,L,d,b,h,f,X,K,N,R,S,M,V,c,W,Q,i;
function O(y,u){var w,x,v,s,t;
t=T.getParam("flash_video_player_url",J.convertUrl(J.url+"/moxieplayer.swf"));
if(t){w=T.documentBaseURI;
h.params.src=t;
if(T.getParam("flash_video_player_absvideourl",true)){y=w.toAbsolute(y||"",true);
u=w.toAbsolute(u||"",true)
}v="";
x=T.getParam("flash_video_player_flashvars",{url:"$url",poster:"$poster"});
tinymce.each(x,function(z,A){z=z.replace(/\$url/,y||"");
z=z.replace(/\$poster/,u||"");
if(z.length>0){v+=(v?"&":"")+A+"="+escape(z)
}});
if(v.length){h.params.flashvars=v
}s=T.getParam("flash_video_player_params",{allowfullscreen:true,allowscriptaccess:true});
tinymce.each(s,function(z,A){h.params[A]=""+z
})
}}h=a.attr("data-mce-json");
if(!h){return
}h=k.parse(h);
R=this.getType(a.attr("class"));
Q=a.attr("data-mce-style");
if(!Q){Q=a.attr("style");
if(Q){Q=T.dom.serializeStyle(T.dom.parseStyle(Q,"img"))
}}h.width=a.attr("width")||h.width;
h.height=a.attr("height")||h.height;
if(R.name==="Iframe"){c=new m("iframe",1);
tinymce.each(q,function(t){var s=a.attr(t);
if(t=="class"&&s){s=s.replace(/mceItem.+ ?/g,"")
}if(s&&s.length>0){c.attr(t,s)
}});
for(d in h.params){c.attr(d,h.params[d])
}c.attr({style:Q,src:h.params.src});
a.replace(c);
return
}if(this.editor.settings.media_use_script){c=new m("script",1).attr("type","text/javascript");
b=new m("#text",3);
b.value="write"+R.name+"("+k.serialize(tinymce.extend(h.params,{width:a.attr("width"),height:a.attr("height")}))+");";
c.append(b);
a.replace(c);
return
}if(R.name==="Video"&&h.video.sources[0]){P=new m("video",1).attr(tinymce.extend({id:a.attr("id"),width:n(a.attr("width")),height:n(a.attr("height")),style:Q},h.video.attrs));
if(h.video.attrs){W=h.video.attrs.poster
}X=h.video.sources=p(h.video.sources);
for(S=0;
S<X.length;
S++){if(/\.mp4$/.test(X[S].src)){V=X[S].src
}}if(!X[0].type){P.attr("src",X[0].src);
X.splice(0,1)
}for(S=0;
S<X.length;
S++){f=new m("source",1).attr(X[S]);
f.shortEnded=true;
P.append(f)
}if(V){O(V,W);
R=J.getType("flash")
}else{h.params.src=""
}}if(R.name==="Audio"&&h.video.sources[0]){i=new m("audio",1).attr(tinymce.extend({id:a.attr("id"),width:n(a.attr("width")),height:n(a.attr("height")),style:Q},h.video.attrs));
if(h.video.attrs){W=h.video.attrs.poster
}X=h.video.sources=p(h.video.sources);
if(!X[0].type){i.attr("src",X[0].src);
X.splice(0,1)
}for(S=0;
S<X.length;
S++){f=new m("source",1).attr(X[S]);
f.shortEnded=true;
i.append(f)
}h.params.src=""
}if(R.name==="EmbeddedAudio"){Y=new m("embed",1);
Y.shortEnded=true;
Y.attr({id:a.attr("id"),width:n(a.attr("width")),height:n(a.attr("height")),style:Q,type:a.attr("type")});
for(d in h.params){Y.attr(d,h.params[d])
}tinymce.each(q,function(s){if(h[s]&&s!="type"){Y.attr(s,h[s])
}});
h.params.src=""
}if(h.params.src){if(/\.flv$/i.test(h.params.src)){O(h.params.src,"")
}if(U&&U.force_absolute){h.params.src=T.documentBaseURI.toAbsolute(h.params.src)
}e=new m("object",1).attr({id:a.attr("id"),width:n(a.attr("width")),height:n(a.attr("height")),style:Q});
tinymce.each(q,function(t){var s=h[t];
if(t=="class"&&s){s=s.replace(/mceItem.+ ?/g,"")
}if(s&&t!="type"){e.attr(t,s)
}});
for(d in h.params){N=new m("param",1);
N.shortEnded=true;
b=h.params[d];
if(d==="src"&&R.name==="WindowsMedia"){d="url"
}N.attr({name:d,value:b});
e.append(N)
}if(this.editor.getParam("media_strict",true)){e.attr({data:h.params.src,type:R.mimes[0]})
}else{e.attr({classid:"clsid:"+R.clsids[0],codebase:R.codebase});
Y=new m("embed",1);
Y.shortEnded=true;
Y.attr({id:a.attr("id"),width:n(a.attr("width")),height:n(a.attr("height")),style:Q,type:R.mimes[0]});
for(d in h.params){Y.attr(d,h.params[d])
}tinymce.each(q,function(s){if(h[s]&&s!="type"){Y.attr(s,h[s])
}});
e.append(Y)
}if(h.object_html){b=new m("#text",3);
b.raw=true;
b.value=h.object_html;
e.append(b)
}if(P){P.append(e)
}}if(P){if(h.video_html){b=new m("#text",3);
b.raw=true;
b.value=h.video_html;
P.append(b)
}}if(i){if(h.video_html){b=new m("#text",3);
b.raw=true;
b.value=h.video_html;
i.append(b)
}}var g=P||i||e||Y;
if(g){a.replace(g)
}else{a.remove()
}},objectToImg:function(Z){var c,ai,T,X,b,a,g,ad,i,R,U,V,ab,h,aa,ah,e,ae,P=this.lookup,ag,d,Q=this.editor.settings.url_converter,af=this.editor.settings.url_converter_scope,O,Y,W,aj;
function S(s){return new tinymce.html.Serializer({inner:true,validate:false}).serialize(s)
}function f(s,t){return P[(s.attr(t)||"").toLowerCase()]
}function ac(s){var t=s.replace(/^.*\.([^.]+)$/,"$1");
return P[t.toLowerCase()||""]
}if(!Z.parent){return
}if(Z.name==="script"){if(Z.firstChild){ag=j.exec(Z.firstChild.value)
}if(!ag){return
}ae=ag[1];
e={video:{},params:k.parse(ag[2])};
ad=e.params.width;
i=e.params.height
}e=e||{video:{},params:{}};
b=new m("img",1);
b.attr({src:this.editor.theme.url+"/img/trans.gif"});
a=Z.name;
if(a==="video"||a=="audio"){T=Z;
c=Z.getAll("object")[0];
ai=Z.getAll("embed")[0];
ad=T.attr("width");
i=T.attr("height");
g=T.attr("id");
e.video={attrs:{},sources:[]};
d=e.video.attrs;
for(a in T.attributes.map){d[a]=T.attributes.map[a]
}aa=Z.attr("src");
if(aa){e.video.sources.push({src:Q.call(af,aa,"src",Z.name)})
}ah=T.getAll("source");
for(U=0;
U<ah.length;
U++){aa=ah[U].remove();
e.video.sources.push({src:Q.call(af,aa.attr("src"),"src","source"),type:aa.attr("type"),media:aa.attr("media")})
}if(d.poster){d.poster=Q.call(af,d.poster,"poster",Z.name)
}}if(Z.name==="object"){c=Z;
ai=Z.getAll("embed")[0]
}if(Z.name==="embed"){ai=Z
}if(Z.name==="iframe"){X=Z;
ae="Iframe"
}if(c){ad=ad||c.attr("width");
i=i||c.attr("height");
R=R||c.attr("style");
g=g||c.attr("id");
O=O||c.attr("hspace");
Y=Y||c.attr("vspace");
W=W||c.attr("align");
aj=aj||c.attr("bgcolor");
e.name=c.attr("name");
h=c.getAll("param");
for(U=0;
U<h.length;
U++){ab=h[U];
a=ab.remove().attr("name");
if(!r[a]){e.params[a]=ab.attr("value")
}}e.params.src=e.params.src||c.attr("data")
}if(ai){ad=ad||ai.attr("width");
i=i||ai.attr("height");
R=R||ai.attr("style");
g=g||ai.attr("id");
O=O||ai.attr("hspace");
Y=Y||ai.attr("vspace");
W=W||ai.attr("align");
aj=aj||ai.attr("bgcolor");
for(a in ai.attributes.map){if(!r[a]&&!e.params[a]){e.params[a]=ai.attributes.map[a]
}}}if(X){ad=n(X.attr("width"));
i=n(X.attr("height"));
R=R||X.attr("style");
g=X.attr("id");
O=X.attr("hspace");
Y=X.attr("vspace");
W=X.attr("align");
aj=X.attr("bgcolor");
tinymce.each(q,function(s){b.attr(s,X.attr(s))
});
for(a in X.attributes.map){if(!r[a]&&!e.params[a]){e.params[a]=X.attributes.map[a]
}}}if(e.params.movie){e.params.src=e.params.src||e.params.movie;
delete e.params.movie
}if(e.params.src){e.params.src=Q.call(af,e.params.src,"src","object")
}if(T){if(Z.name==="video"){ae=P.video.name
}else{if(Z.name==="audio"){ae=P.audio.name
}}}if(c&&!ae){ae=(f(c,"clsid")||f(c,"classid")||f(c,"type")||{}).name
}if(ai&&!ae){ae=(f(ai,"type")||ac(e.params.src)||{}).name
}if(ai&&ae=="EmbeddedAudio"){e.params.type=ai.attr("type")
}Z.replace(b);
if(ai){ai.remove()
}if(c){V=S(c.remove());
if(V){e.object_html=V
}}if(T){V=S(T.remove());
if(V){e.video_html=V
}}e.hspace=O;
e.vspace=Y;
e.align=W;
e.bgcolor=aj;
b.attr({id:g,"class":"mceItemMedia mceItem"+(ae||"Flash"),style:R,width:ad||(Z.name=="audio"?"300":"320"),height:i||(Z.name=="audio"?"32":"240"),hspace:O,vspace:Y,align:W,bgcolor:aj,"data-mce-json":k.serialize(e,"'")})
}});
tinymce.PluginManager.add("media",tinymce.plugins.MediaPlugin)
})();