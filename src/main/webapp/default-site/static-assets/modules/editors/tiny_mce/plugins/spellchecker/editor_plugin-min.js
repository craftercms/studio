(function(){var e=tinymce.util.JSONRequest,f=tinymce.each,d=tinymce.DOM;
tinymce.create("tinymce.plugins.SpellcheckerPlugin",{getInfo:function(){return{longname:"Spellchecker",author:"Moxiecode Systems AB",authorurl:"http://tinymce.moxiecode.com",infourl:"http://wiki.moxiecode.com/index.php/TinyMCE:Plugins/spellchecker",version:tinymce.majorVersion+"."+tinymce.minorVersion}
},init:function(c,b){var a=this,h;
a.url=b;
a.editor=c;
a.rpcUrl=c.getParam("spellchecker_rpc_url","{backend}");
if(a.rpcUrl=="{backend}"){if(tinymce.isIE){return
}a.hasSupport=true;
c.onContextMenu.addToTop(function(j,g){if(a.active){return false
}})
}c.addCommand("mceSpellCheck",function(){if(a.rpcUrl=="{backend}"){a.editor.getBody().spellcheck=a.active=!a.active;
return
}if(!a.active){c.setProgressState(1);
a._sendRPC("checkWords",[a.selectedLang,a._getWords()],function(g){if(g.length>0){a.active=1;
a._markWords(g);
c.setProgressState(0);
c.nodeChanged()
}else{c.setProgressState(0);
if(c.getParam("spellchecker_report_no_misspellings",true)){c.windowManager.alert("spellchecker.no_mpell")
}}})
}else{a._done()
}});
if(c.settings.content_css!==false){c.contentCSS.push(b+"/css/content.css")
}c.onClick.add(a._showMenu,a);
c.onContextMenu.add(a._showMenu,a);
c.onBeforeGetContent.add(function(){if(a.active){a._removeWords()
}});
c.onNodeChange.add(function(g,j){j.setActive("spellchecker",a.active)
});
c.onSetContent.add(function(){a._done()
});
c.onBeforeGetContent.add(function(){a._done()
});
c.onBeforeExecCommand.add(function(j,g){if(g=="mceFullScreen"){a._done()
}});
a.languages={};
f(c.getParam("spellchecker_languages","+English=en,Danish=da,Dutch=nl,Finnish=fi,French=fr,German=de,Italian=it,Polish=pl,Portuguese=pt,Spanish=es,Swedish=sv","hash"),function(g,j){if(j.indexOf("+")===0){j=j.substring(1);
a.selectedLang=g
}a.languages[j]=g
})
},createControl:function(a,j){var c=this,b,i=c.editor;
if(a=="spellchecker"){if(c.rpcUrl=="{backend}"){if(c.hasSupport){b=j.createButton(a,{title:"spellchecker.desc",cmd:"mceSpellCheck",scope:c})
}return b
}b=j.createSplitButton(a,{title:"spellchecker.desc",cmd:"mceSpellCheck",scope:c});
b.onRenderMenu.add(function(g,h){h.add({title:"spellchecker.langs","class":"mceMenuItemTitle"}).setDisabled(1);
c.menuItems={};
f(c.languages,function(r,k){var q={icon:1},o;
q.onclick=function(){if(r==c.selectedLang){return
}c._updateMenu(o);
c.selectedLang=r
};
q.title=k;
o=h.add(q);
o.setSelected(r==c.selectedLang);
c.menuItems[r]=o;
if(r==c.selectedLang){c.selectedItem=o
}})
});
return b
}},setLanguage:function(a){var b=this;
if(a==b.selectedLang){return
}if(tinymce.grep(b.languages,function(c){return c===a
}).length===0){throw"Unknown language: "+a
}b.selectedLang=a;
if(b.menuItems){b._updateMenu(b.menuItems[a])
}if(b.active){b._done()
}},_updateMenu:function(a){a.setSelected(1);
this.selectedItem.setSelected(0);
this.selectedItem=a
},_walk:function(a,c){var b=this.editor.getDoc(),j;
if(b.createTreeWalker){j=b.createTreeWalker(a,NodeFilter.SHOW_TEXT,null,false);
while((a=j.nextNode())!=null){c.call(this,a)
}}else{tinymce.walk(a,c,"childNodes")
}},_getSeparators:function(){var b="",c,a=this.editor.getParam("spellchecker_word_separator_chars",'\\s!"#$%&()*+,-./:;<=>?@[]^_{|}����������������\u201d\u201c');
for(c=0;
c<a.length;
c++){b+="\\"+a.charAt(c)
}return b
},_getWords:function(){var i=this.editor,b=[],j="",c={},a=[];
this._walk(i.getBody(),function(g){if(g.nodeType==3){j+=g.nodeValue+" "
}});
if(i.getParam("spellchecker_word_pattern")){a=j.match("("+i.getParam("spellchecker_word_pattern")+")","gi")
}else{j=j.replace(new RegExp("([0-9]|["+this._getSeparators()+"])","g")," ");
j=tinymce.trim(j.replace(/(\s+)/g," "));
a=j.split(" ")
}f(a,function(g){if(!c[g]){b.push(g);
c[g]=1
}});
return b
},_removeWords:function(j){var i=this.editor,a=i.dom,b=i.selection,c=b.getRng(true);
f(a.select("span").reverse(),function(g){if(g&&(a.hasClass(g,"mceItemHiddenSpellWord")||a.hasClass(g,"mceItemHidden"))){if(!j||a.decode(g.innerHTML)==j){a.remove(g,1)
}}});
b.setRng(c)
},_markWords:function(b){var p=this.editor,q=p.dom,n=p.getDoc(),o=p.selection,t=o.getRng(true),s=[],c=b.join("|"),a=this._getSeparators(),r=new RegExp("(^|["+a+"])("+c+")(?=["+a+"]|$)","g");
this._walk(p.getBody(),function(g){if(g.nodeType==3){s.push(g)
}});
f(s,function(g){var i,j,l,h,k=g.nodeValue;
r.lastIndex=0;
if(r.test(k)){k=q.encode(k);
j=q.create("span",{"class":"mceItemHidden"});
if(tinymce.isIE){k=k.replace(r,"$1<mcespell>$2</mcespell>");
while((h=k.indexOf("<mcespell>"))!=-1){l=k.substring(0,h);
if(l.length){i=n.createTextNode(q.decode(l));
j.appendChild(i)
}k=k.substring(h+10);
h=k.indexOf("</mcespell>");
l=k.substring(0,h);
k=k.substring(h+11);
j.appendChild(q.create("span",{"class":"mceItemHiddenSpellWord"},l))
}if(k.length){i=n.createTextNode(q.decode(k));
j.appendChild(i)
}}else{j.innerHTML=k.replace(r,'$1<span class="mceItemHiddenSpellWord">$2</span>')
}q.replace(j,g)
}});
o.setRng(t)
},_showMenu:function(n,c){var m=this,n=m.editor,q=m._menu,a,b=n.dom,o=b.getViewPort(n.getWin()),p=c.target;
c=0;
if(!q){q=n.controlManager.createDropMenu("spellcheckermenu",{"class":"mceNoIcons"});
m._menu=q
}if(b.hasClass(p,"mceItemHiddenSpellWord")){q.removeAll();
q.add({title:"spellchecker.wait","class":"mceMenuItemTitle"}).setDisabled(1);
m._sendRPC("getSuggestions",[m.selectedLang,b.decode(p.innerHTML)],function(g){var h;
q.removeAll();
if(g.length>0){q.add({title:"spellchecker.sug","class":"mceMenuItemTitle"}).setDisabled(1);
f(g,function(i){q.add({title:i,onclick:function(){b.replace(n.getDoc().createTextNode(i),p);
m._checkDone()
}})
});
q.addSeparator()
}else{q.add({title:"spellchecker.no_sug","class":"mceMenuItemTitle"}).setDisabled(1)
}if(n.getParam("show_ignore_words",true)){h=m.editor.getParam("spellchecker_enable_ignore_rpc","");
q.add({title:"spellchecker.ignore_word",onclick:function(){var i=p.innerHTML;
b.remove(p,1);
m._checkDone();
if(h){n.setProgressState(1);
m._sendRPC("ignoreWord",[m.selectedLang,i],function(j){n.setProgressState(0)
})
}}});
q.add({title:"spellchecker.ignore_words",onclick:function(){var i=p.innerHTML;
m._removeWords(b.decode(i));
m._checkDone();
if(h){n.setProgressState(1);
m._sendRPC("ignoreWords",[m.selectedLang,i],function(j){n.setProgressState(0)
})
}}})
}if(m.editor.getParam("spellchecker_enable_learn_rpc")){q.add({title:"spellchecker.learn_word",onclick:function(){var i=p.innerHTML;
b.remove(p,1);
m._checkDone();
n.setProgressState(1);
m._sendRPC("learnWord",[m.selectedLang,i],function(j){n.setProgressState(0)
})
}})
}q.update()
});
a=d.getPos(n.getContentAreaContainer());
q.settings.offset_x=a.x;
q.settings.offset_y=a.y;
n.selection.select(p);
a=b.getPos(p);
q.showMenu(a.x,a.y+p.offsetHeight-o.y);
return tinymce.dom.Event.cancel(c)
}else{q.hideMenu()
}},_checkDone:function(){var c=this,h=c.editor,a=h.dom,b;
f(a.select("span"),function(g){if(g&&a.hasClass(g,"mceItemHiddenSpellWord")){b=true;
return false
}});
if(!b){c._done()
}},_done:function(){var b=this,a=b.active;
if(b.active){b.active=0;
b._removeWords();
if(b._menu){b._menu.hideMenu()
}if(a){b.editor.nodeChanged()
}}},_sendRPC:function(c,a,h){var b=this;
e.sendRPC({url:b.rpcUrl,method:c,params:a,success:h,error:function(g,j){b.editor.setProgressState(0);
b.editor.windowManager.alert(g.errstr||("Error response: "+j.responseText))
}})
}});
tinymce.PluginManager.add("spellchecker",tinymce.plugins.SpellcheckerPlugin)
})();