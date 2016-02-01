(function(h){var g=h.each;
function e(d,c){var b=c.ownerDocument,k=b.createRange(),a;
k.setStartBefore(c);
k.setEnd(d.endContainer,d.endOffset);
a=b.createElement("body");
a.appendChild(k.cloneContents());
return a.innerHTML.replace(/<(br|img|object|embed|input|textarea)[^>]*>/gi,"-").replace(/<[^>]+>/g,"").length==0
}function f(P,Q,b,ae){var ap,a,X,ah;
W();
ah=Q.getParent(b.getStart(),"th,td");
if(ah){a=S(ah);
X=d();
ah=ag(a.x,a.y)
}function ad(j,k){j=j.cloneNode(k);
j.removeAttribute("id");
return j
}function W(){var j=0;
ap=[];
g(["thead","tbody","tfoot"],function(l){var k=Q.select("> "+l+" tr",P);
g(k,function(n,m){m+=j;
g(Q.select("> td, > th",n),function(s,r){var q,p,o,t;
if(ap[m]){while(ap[m][r]){r++
}}o=an(s,"rowspan");
t=an(s,"colspan");
for(p=m;
p<m+o;
p++){if(!ap[p]){ap[p]=[]
}for(q=r;
q<r+t;
q++){ap[p][q]={part:l,real:p==m&&q==r,elm:s,rowspan:o,colspan:t}
}}})
});
j+=k.length
})
}function ag(l,j){var k;
k=ap[j];
if(k){return k[l]
}}function an(j,k){return parseInt(j.getAttribute(k)||1)
}function Y(j,l,k){if(j){k=parseInt(k);
if(k===1){j.removeAttribute(l,1)
}else{j.setAttribute(l,k,1)
}}}function am(j){return j&&(Q.hasClass(j.elm,"mceSelected")||j==ah)
}function al(){var j=[];
g(P.rows,function(k){g(k.cells,function(l){if(Q.hasClass(l,"mceSelected")||l==ah.elm){j.push(k);
return false
}})
});
return j
}function aa(){var j=Q.createRng();
j.setStartAfter(P);
j.setEndAfter(P);
b.setRng(j);
Q.remove(P)
}function aq(k){var j;
h.walk(k,function(l){var m;
if(l.nodeType==3){g(Q.getParents(l.parentNode,null,k).reverse(),function(n){n=ad(n,false);
if(!j){j=m=n
}else{if(m){m.appendChild(n)
}}m=n
});
if(m){m.innerHTML=h.isIE?"&nbsp;":'<br data-mce-bogus="1" />'
}return false
}},"childNodes");
k=ad(k,false);
Y(k,"rowSpan",1);
Y(k,"colSpan",1);
if(j){k.appendChild(j)
}else{if(!h.isIE){k.innerHTML='<br data-mce-bogus="1" />'
}}return k
}function ac(){var j=Q.createRng();
g(Q.select("tr",P),function(k){if(k.cells.length==0){Q.remove(k)
}});
if(Q.select("tr",P).length==0){j.setStartAfter(P);
j.setEndAfter(P);
b.setRng(j);
Q.remove(P);
return
}g(Q.select("thead,tbody,tfoot",P),function(k){if(k.rows.length==0){Q.remove(k)
}});
W();
row=ap[Math.min(ap.length-1,a.y)];
if(row){b.select(row[Math.min(row.length-1,a.x)].elm,true);
b.collapse(true)
}}function U(l,n,j,m){var o,q,r,p,k;
o=ap[n][l].elm.parentNode;
for(r=1;
r<=j;
r++){o=Q.getNext(o,"tr");
if(o){for(q=l;
q>=0;
q--){k=ap[n+r][q].elm;
if(k.parentNode==o){for(p=1;
p<=m;
p++){Q.insertAfter(aq(k),k)
}break
}}if(q==-1){for(p=1;
p<=m;
p++){o.insertBefore(aq(o.cells[0]),o.cells[0])
}}}}}function Z(){g(ap,function(k,j){g(k,function(p,q){var m,n,l,o;
if(am(p)){p=p.elm;
m=an(p,"colspan");
n=an(p,"rowspan");
if(m>1||n>1){Y(p,"rowSpan",1);
Y(p,"colSpan",1);
for(o=0;
o<m-1;
o++){Q.insertAfter(aq(p),p)
}U(q,j,n-1,m)
}}})
})
}function af(m,p,j){var s,t,k,l,n,q,o,v,m,u,r;
if(m){pos=S(m);
s=pos.x;
t=pos.y;
k=s+(p-1);
l=t+(j-1)
}else{s=a.x;
t=a.y;
k=X.x;
l=X.y
}o=ag(s,t);
v=ag(k,l);
if(o&&v&&o.part==v.part){Z();
W();
o=ag(s,t).elm;
Y(o,"colSpan",(k-s)+1);
Y(o,"rowSpan",(l-t)+1);
for(q=t;
q<=l;
q++){for(n=s;
n<=k;
n++){if(!ap[q]||!ap[q][n]){continue
}m=ap[q][n].elm;
if(m!=o){u=h.grep(m.childNodes);
g(u,function(w){o.appendChild(w)
});
if(u.length){u=h.grep(o.childNodes);
r=0;
g(u,function(w){if(w.nodeName=="BR"&&Q.getAttrib(w,"data-mce-bogus")&&r++<u.length-1){o.removeChild(w)
}})
}Q.remove(m)
}}}ac()
}}function ak(o){var s,m,p,n,l,k,r,j,q;
g(ap,function(u,t){g(u,function(v,w){if(am(v)){v=v.elm;
l=v.parentNode;
k=ad(l,false);
s=t;
if(o){return false
}}});
if(o){return !s
}});
for(n=0;
n<ap[0].length;
n++){if(!ap[s][n]){continue
}m=ap[s][n].elm;
if(m!=p){if(!o){q=an(m,"rowspan");
if(q>1){Y(m,"rowSpan",q+1);
continue
}}else{if(s>0&&ap[s-1][n]){j=ap[s-1][n].elm;
q=an(j,"rowSpan");
if(q>1){Y(j,"rowSpan",q+1);
continue
}}}r=aq(m);
Y(r,"colSpan",m.colSpan);
k.appendChild(r);
p=m
}}if(k.hasChildNodes()){if(!o){Q.insertAfter(k,l)
}else{l.parentNode.insertBefore(k,l)
}}}function ao(k){var j,l;
g(ap,function(n,m){g(n,function(o,p){if(am(o)){j=p;
if(k){return false
}}});
if(k){return !j
}});
g(ap,function(n,m){var q,p,o;
if(!n[j]){return
}q=n[j].elm;
if(q!=l){o=an(q,"colspan");
p=an(q,"rowspan");
if(o==1){if(!k){Q.insertAfter(aq(q),q);
U(j,m,p-1,o)
}else{q.parentNode.insertBefore(aq(q),q);
U(j,m,p-1,o)
}}else{Y(q,"colSpan",q.colSpan+1)
}l=q
}})
}function ai(){var j=[];
g(ap,function(l,k){g(l,function(m,n){if(am(m)&&h.inArray(j,n)===-1){g(ap,function(o){var q=o[n].elm,p;
p=an(q,"colSpan");
if(p>1){Y(q,"colSpan",p-1)
}else{Q.remove(q)
}});
j.push(n)
}})
});
ac()
}function aj(){var j;
function k(m){var n,l,o;
n=Q.getNext(m,"tr");
g(m.cells,function(q){var p=an(q,"rowSpan");
if(p>1){Y(q,"rowSpan",p-1);
l=S(q);
U(l.x,l.y,1,1)
}});
l=S(m.cells[0]);
g(ap[l.y],function(q){var p;
q=q.elm;
if(q!=o){p=an(q,"rowSpan");
if(p<=1){Q.remove(q)
}else{Y(q,"rowSpan",p-1)
}o=q
}})
}j=al();
g(j.reverse(),function(l){k(l)
});
ac()
}function T(){var j=al();
Q.remove(j);
ac();
return j
}function c(){var j=al();
g(j,function(k,l){j[l]=ad(k,true)
});
return j
}function ab(l,m){var k=al(),n=k[m?0:k.length-1],j=n.cells.length;
g(ap,function(o){var p;
j=0;
g(o,function(q,r){if(q.real){j+=q.colspan
}if(q.elm.parentNode==n){p=1
}});
if(p){return false
}});
if(!m){l.reverse()
}g(l,function(o){var p=o.cells.length,q;
for(i=0;
i<p;
i++){q=o.cells[i];
Y(q,"colSpan",1);
Y(q,"rowSpan",1)
}for(i=p;
i<j;
i++){o.appendChild(aq(o.cells[p-1]))
}for(i=j;
i<p;
i++){Q.remove(o.cells[i])
}if(m){n.parentNode.insertBefore(o,n)
}else{Q.insertAfter(o,n)
}})
}function S(k){var j;
g(ap,function(m,l){g(m,function(n,o){if(n.elm==k){j={x:o,y:l};
return false
}});
return !j
});
return j
}function O(j){a=S(j)
}function d(){var j,k,l;
k=l=0;
g(ap,function(n,m){g(n,function(q,r){var o,p;
if(am(q)){q=ap[m][r];
if(r>k){k=r
}if(m>l){l=m
}if(q.real){o=q.colspan-1;
p=q.rowspan-1;
if(o){if(r+o>k){k=r+o
}}if(p){if(m+p>l){l=m+p
}}}}})
});
return{x:k,y:l}
}function R(l){var o,p,j,k,q,r,n,m;
X=S(l);
if(a&&X){o=Math.min(a.x,X.x);
p=Math.min(a.y,X.y);
j=Math.max(a.x,X.x);
k=Math.max(a.y,X.y);
q=j;
r=k;
for(y=p;
y<=r;
y++){l=ap[y][o];
if(!l.real){if(o-(l.colspan-1)<o){o-=l.colspan-1
}}}for(x=o;
x<=q;
x++){l=ap[p][x];
if(!l.real){if(p-(l.rowspan-1)<p){p-=l.rowspan-1
}}}for(y=p;
y<=k;
y++){for(x=o;
x<=j;
x++){l=ap[y][x];
if(l.real){n=l.colspan-1;
m=l.rowspan-1;
if(n){if(x+n>q){q=x+n
}}if(m){if(y+m>r){r=y+m
}}}}}Q.removeClass(Q.select("td.mceSelected,th.mceSelected"),"mceSelected");
for(y=p;
y<=r;
y++){for(x=o;
x<=q;
x++){if(ap[y][x]){Q.addClass(ap[y][x].elm,"mceSelected")
}}}}}function V(n,j,m){if(ae){var l=n.rows;
var k=null;
for(var o=0;
o<l.length;
o++){k=l[o];
if(o%2!=0){j.removeClass(k,"even");
j.addClass(k,"odd")
}else{j.removeClass(k,"odd");
j.addClass(k,"even")
}}}}h.extend(this,{deleteTable:aa,split:Z,merge:af,insertRow:ak,insertCol:ao,deleteCols:ai,deleteRows:aj,cutRows:T,copyRows:c,pasteRows:ab,getPos:S,setStartCell:O,setEndCell:R,currentTable:P,runSmartTableLogic:V})
}h.create("tinymce.plugins.CStudioContextTable",{init:function(p,s){var c=p.contextControl.rteTableStyles||{};
var a,o,b=true;
function d(k){var j=/^true$/i;
return j.test(k)
}function q(j){var k=p.selection,n=p.dom.getParent(j||k.getNode(),"table");
if(n){var m=d(n.getAttribute("data-smart-table"));
tableGrid=new f(n,p.dom,k,m);
if(m){var l=n.getAttribute("data-smart-table-style");
if(l){h.extend(tableGrid,{runSmartTableLogic:new Function("currentTable","dom","event",c[l])})
}}return tableGrid
}}function r(){p.getBody().style.webkitUserSelect="";
if(b){p.dom.removeClass(p.dom.select("td.mceSelected,th.mceSelected"),"mceSelected");
b=false
}}g([["table","table.desc","mceInsertTable",true],["delete_table","table.del","mceTableDelete"],["delete_col","table.delete_col_desc","mceTableDeleteCol"],["delete_row","table.delete_row_desc","mceTableDeleteRow"],["col_after","table.col_after_desc","mceTableInsertColAfter"],["col_before","table.col_before_desc","mceTableInsertColBefore"],["row_after","table.row_after_desc","mceTableInsertRowAfter"],["row_before","table.row_before_desc","mceTableInsertRowBefore"],["row_props","table.row_desc","mceTableRowProps",true],["cell_props","table.cell_desc","mceTableCellProps",true],["split_cells","table.split_cells_desc","mceTableSplitCells",true],["merge_cells","table.merge_cells_desc","mceTableMergeCells",true]],function(j){p.addButton(j[0],{title:j[1],cmd:j[2],ui:j[3]})
});
if(!h.isIE){p.onClick.add(function(k,j){j=j.target;
if(j.nodeName==="TABLE"){k.selection.select(j);
k.nodeChanged()
}})
}p.onPreProcess.add(function(v,n){var w,m,l,j=v.dom,k;
w=j.select("table",n.node);
m=w.length;
while(m--){l=w[m];
j.setAttrib(l,"data-mce-style","");
if((k=j.getAttrib(l,"width"))){j.setStyle(l,"width",k);
j.setAttrib(l,"width","")
}if((k=j.getAttrib(l,"height"))){j.setStyle(l,"height",k);
j.setAttrib(l,"height","")
}}});
p.onNodeChange.add(function(l,m,j){var k;
j=l.selection.getStart();
k=l.dom.getParent(j,"td,th,caption");
m.setActive("table",j.nodeName==="TABLE"||!!k);
if(k&&k.nodeName==="CAPTION"){k=0
}m.setDisabled("delete_table",!k);
m.setDisabled("delete_col",!k);
m.setDisabled("delete_table",!k);
m.setDisabled("delete_row",!k);
m.setDisabled("col_after",!k);
m.setDisabled("col_before",!k);
m.setDisabled("row_after",!k);
m.setDisabled("row_before",!k);
m.setDisabled("row_props",!k);
m.setDisabled("cell_props",!k);
m.setDisabled("split_cells",!k);
m.setDisabled("merge_cells",!k)
});
p.onInit.add(function(n){var u,k,j=n.dom,m;
a=n.windowManager;
n.onMouseDown.add(function(w,t){if(t.button!=2){r();
k=j.getParent(t.target,"td,th");
u=j.getParent(k,"table")
}});
j.bind(n.getDoc(),"mouseover",function(t){var B,C,E=t.target;
if(k&&(m||E!=k)&&(E.nodeName=="TD"||E.nodeName=="TH")){C=j.getParent(E,"table");
if(C==u){if(!m){m=q(C);
m.setStartCell(k);
n.getBody().style.webkitUserSelect="none"
}m.setEndCell(E);
b=true
}B=n.selection.getSel();
try{if(B.removeAllRanges){B.removeAllRanges()
}else{B.empty()
}}catch(D){}t.preventDefault()
}});
n.onMouseUp.add(function(t,Q){var M,K=t.selection,P,N=K.getSel(),O,J,L,H;
if(k){if(m){t.getBody().style.webkitUserSelect=""
}function I(z,v){var w=new h.dom.TreeWalker(z,z);
do{if(z.nodeType==3&&h.trim(z.nodeValue).length!=0){if(v){M.setStart(z,0)
}else{M.setEnd(z,z.nodeValue.length)
}return
}if(z.nodeName=="BR"){if(v){M.setStartBefore(z)
}else{M.setEndBefore(z)
}return
}}while(z=(v?w.next():w.prev()))
}P=j.select("td.mceSelected,th.mceSelected");
if(P.length>0){M=j.createRng();
J=P[0];
H=P[P.length-1];
M.setStart(J);
M.setEnd(J);
I(J,1);
O=new h.dom.TreeWalker(J,j.getParent(P[0],"table"));
do{if(J.nodeName=="TD"||J.nodeName=="TH"){if(!j.hasClass(J,"mceSelected")){break
}L=J
}}while(J=O.next());
I(L);
K.setRng(M)
}t.nodeChanged();
k=m=u=null
}});
n.onKeyUp.add(function(w,t){r()
});
if(n&&n.plugins.contextmenu){n.plugins.contextmenu.onContextMenu.add(function(C,E,t){var G,F=n.selection,D=F.getNode()||n.getBody();
if(n.dom.getParent(t,"td")||n.dom.getParent(t,"th")||n.dom.select("td.mceSelected,th.mceSelected").length){E.removeAll();
if(D.nodeName=="A"&&!n.dom.getAttrib(D,"name")){E.add({title:"advanced.link_desc",icon:"link",cmd:n.plugins.advlink?"mceAdvLink":"mceLink",ui:true});
E.add({title:"advanced.unlink_desc",icon:"unlink",cmd:"UnLink"});
E.addSeparator()
}if(D.nodeName=="IMG"&&D.className.indexOf("mceItem")==-1){E.add({title:"advanced.image_desc",icon:"image",cmd:n.plugins.advimage?"mceAdvImage":"mceImage",ui:true});
E.addSeparator()
}E.add({title:"table.desc",icon:"table",cmd:"mceInsertTable",value:{action:"insert"}});
E.add({title:"table.props_desc",icon:"table_props",cmd:"mceInsertTable"});
E.add({title:"table.del",icon:"delete_table",cmd:"mceTableDelete"});
E.addSeparator();
G=E.addMenu({title:"table.cell"});
G.add({title:"table.cell_desc",icon:"cell_props",cmd:"mceTableCellProps"});
G.add({title:"table.split_cells_desc",icon:"split_cells",cmd:"mceTableSplitCells"});
G.add({title:"table.merge_cells_desc",icon:"merge_cells",cmd:"mceTableMergeCells"});
G=E.addMenu({title:"table.row"});
G.add({title:"table.row_desc",icon:"row_props",cmd:"mceTableRowProps"});
G.add({title:"table.row_before_desc",icon:"row_before",cmd:"mceTableInsertRowBefore"});
G.add({title:"table.row_after_desc",icon:"row_after",cmd:"mceTableInsertRowAfter"});
G.add({title:"table.delete_row_desc",icon:"delete_row",cmd:"mceTableDeleteRow"});
G.addSeparator();
G.add({title:"table.cut_row_desc",icon:"cut",cmd:"mceTableCutRow"});
G.add({title:"table.copy_row_desc",icon:"copy",cmd:"mceTableCopyRow"});
G.add({title:"table.paste_row_before_desc",icon:"paste",cmd:"mceTablePasteRowBefore"}).setDisabled(!o);
G.add({title:"table.paste_row_after_desc",icon:"paste",cmd:"mceTablePasteRowAfter"}).setDisabled(!o);
G=E.addMenu({title:"table.col"});
G.add({title:"table.col_before_desc",icon:"col_before",cmd:"mceTableInsertColBefore"});
G.add({title:"table.col_after_desc",icon:"col_after",cmd:"mceTableInsertColAfter"});
G.add({title:"table.delete_col_desc",icon:"delete_col",cmd:"mceTableDeleteCol"})
}else{E.add({title:"table.desc",icon:"table",cmd:"mceInsertTable"})
}})
}if(!h.isIE){function l(){var t;
for(t=n.getBody().lastChild;
t&&t.nodeType==3&&!t.nodeValue.length;
t=t.previousSibling){}if(t&&t.nodeName=="TABLE"){n.dom.add(n.getBody(),"p",null,'<br mce_bogus="1" />')
}}if(h.isGecko){n.onKeyDown.add(function(C,E){var D,B,t=C.dom;
if(E.keyCode==37||E.keyCode==38){D=C.selection.getRng();
B=t.getParent(D.startContainer,"table");
if(B&&C.getBody().firstChild==B){if(e(D,B)){D=t.createRng();
D.setStartBefore(B);
D.setEndBefore(B);
C.selection.setRng(D);
E.preventDefault()
}}}})
}n.onKeyUp.add(l);
n.onSetContent.add(l);
n.onVisualAid.add(l);
n.onPreProcess.add(function(A,t){var z=t.node.lastChild;
if(z&&z.childNodes.length==1&&z.firstChild.nodeName=="BR"){A.dom.remove(z)
}});
l()
}});
g({mceTableSplitCells:function(j){j.split()
},mceTableMergeCells:function(l){var k,j,m;
m=p.dom.getParent(p.selection.getNode(),"th,td");
if(m){k=m.rowSpan;
j=m.colSpan
}if(!p.dom.select("td.mceSelected,th.mceSelected").length){a.open({url:s+"/merge_cells.htm",width:240+parseInt(p.getLang("table.merge_cells_delta_width",0)),height:110+parseInt(p.getLang("table.merge_cells_delta_height",0)),inline:1},{rows:k,cols:j,onaction:function(n){l.merge(m,n.cols,n.rows)
},plugin_url:s})
}else{l.merge()
}},mceTableInsertRowBefore:function(j){j.insertRow(true)
},mceTableInsertRowAfter:function(j){j.insertRow()
},mceTableInsertColBefore:function(j){j.insertCol(true)
},mceTableInsertColAfter:function(j){j.insertCol()
},mceTableDeleteCol:function(j){j.deleteCols()
},mceTableDeleteRow:function(j){j.deleteRows()
},mceTableCutRow:function(j){o=j.cutRows()
},mceTableCopyRow:function(j){o=j.copyRows()
},mceTablePasteRowBefore:function(j){j.pasteRows(o,true)
},mceTablePasteRowAfter:function(j){j.pasteRows(o)
},mceTableDelete:function(j){j.deleteTable()
}},function(j,k){p.addCommand(k,function(){var l=q();
if(l){j(l);
l.runSmartTableLogic(l.currentTable,p.dom,k);
p.execCommand("mceRepaint");
r()
}})
});
g({mceInsertTable:function(j){a.open({url:s+"/table.htm",width:400+parseInt(p.getLang("table.table_delta_width",0)),height:320+parseInt(p.getLang("table.table_delta_height",0)),inline:1},{plugin_url:s,action:j?j.action:0})
},mceTableRowProps:function(){a.open({url:s+"/row.htm",width:400+parseInt(p.getLang("table.rowprops_delta_width",0)),height:295+parseInt(p.getLang("table.rowprops_delta_height",0)),inline:1},{plugin_url:s})
},mceTableCellProps:function(){a.open({url:s+"/cell.htm",width:400+parseInt(p.getLang("table.cellprops_delta_width",0)),height:295+parseInt(p.getLang("table.cellprops_delta_height",0)),inline:1},{plugin_url:s})
}},function(j,k){p.addCommand(k,function(m,l){j(l)
})
})
}});
h.PluginManager.add("cs_table",h.plugins.CStudioContextTable)
})(tinymce);