(function(c){var d=c.each;
function b(f,g){var h=g.ownerDocument,e=h.createRange(),j;
e.setStartBefore(g);
e.setEnd(f.endContainer,f.endOffset);
j=h.createElement("body");
j.appendChild(e.cloneContents());
return j.innerHTML.replace(/<(br|img|object|embed|input|textarea)[^>]*>/gi,"-").replace(/<[^>]+>/g,"").length==0
}function a(J,I,M,q){var f,N,E,o;
u();
o=I.getParent(M.getStart(),"th,td");
if(o){N=H(o);
E=K();
o=A(N.x,N.y)
}function B(P,O){P=P.cloneNode(O);
P.removeAttribute("id");
return P
}function u(){var O=0;
f=[];
d(["thead","tbody","tfoot"],function(P){var Q=I.select("> "+P+" tr",J);
d(Q,function(R,S){S+=O;
d(I.select("> td, > th",R),function(Y,T){var U,V,W,X;
if(f[S]){while(f[S][T]){T++
}}W=h(Y,"rowspan");
X=h(Y,"colspan");
for(V=S;
V<S+W;
V++){if(!f[V]){f[V]=[]
}for(U=T;
U<T+X;
U++){f[V][U]={part:P,real:V==S&&U==T,elm:Y,rowspan:W,colspan:X}
}}})
});
O+=Q.length
})
}function A(O,Q){var P;
P=f[Q];
if(P){return P[O]
}}function h(P,O){return parseInt(P.getAttribute(O)||1)
}function t(Q,O,P){if(Q){P=parseInt(P);
if(P===1){Q.removeAttribute(O,1)
}else{Q.setAttribute(O,P,1)
}}}function j(O){return O&&(I.hasClass(O.elm,"mceSelected")||O==o)
}function k(){var O=[];
d(J.rows,function(P){d(P.cells,function(Q){if(I.hasClass(Q,"mceSelected")||Q==o.elm){O.push(P);
return false
}})
});
return O
}function s(){var O=I.createRng();
O.setStartAfter(J);
O.setEndAfter(J);
M.setRng(O);
I.remove(J)
}function e(O){var P;
c.walk(O,function(R){var Q;
if(R.nodeType==3){d(I.getParents(R.parentNode,null,O).reverse(),function(S){S=B(S,false);
if(!P){P=Q=S
}else{if(Q){Q.appendChild(S)
}}Q=S
});
if(Q){Q.innerHTML=c.isIE?"&nbsp;":'<br data-mce-bogus="1" />'
}return false
}},"childNodes");
O=B(O,false);
t(O,"rowSpan",1);
t(O,"colSpan",1);
if(P){O.appendChild(P)
}else{if(!c.isIE){O.innerHTML='<br data-mce-bogus="1" />'
}}return O
}function r(){var O=I.createRng();
d(I.select("tr",J),function(P){if(P.cells.length==0){I.remove(P)
}});
if(I.select("tr",J).length==0){O.setStartAfter(J);
O.setEndAfter(J);
M.setRng(O);
I.remove(J);
return
}d(I.select("thead,tbody,tfoot",J),function(P){if(P.rows.length==0){I.remove(P)
}});
u();
row=f[Math.min(f.length-1,N.y)];
if(row){M.select(row[Math.min(row.length-1,N.x)].elm,true);
M.collapse(true)
}}function v(U,S,W,T){var R,P,O,Q,V;
R=f[S][U].elm.parentNode;
for(O=1;
O<=W;
O++){R=I.getNext(R,"tr");
if(R){for(P=U;
P>=0;
P--){V=f[S+O][P].elm;
if(V.parentNode==R){for(Q=1;
Q<=T;
Q++){I.insertAfter(e(V),V)
}break
}}if(P==-1){for(Q=1;
Q<=T;
Q++){R.insertBefore(e(R.cells[0]),R.cells[0])
}}}}}function D(){d(f,function(O,P){d(O,function(R,Q){var U,T,V,S;
if(j(R)){R=R.elm;
U=h(R,"colspan");
T=h(R,"rowspan");
if(U>1||T>1){t(R,"rowSpan",1);
t(R,"colSpan",1);
for(S=0;
S<U-1;
S++){I.insertAfter(e(R),R)
}v(Q,P,T-1,U)
}}})
})
}function p(X,U,aa){var R,Q,Z,Y,W,T,V,O,X,P,S;
if(X){pos=H(X);
R=pos.x;
Q=pos.y;
Z=R+(U-1);
Y=Q+(aa-1)
}else{R=N.x;
Q=N.y;
Z=E.x;
Y=E.y
}V=A(R,Q);
O=A(Z,Y);
if(V&&O&&V.part==O.part){D();
u();
V=A(R,Q).elm;
t(V,"colSpan",(Z-R)+1);
t(V,"rowSpan",(Y-Q)+1);
for(T=Q;
T<=Y;
T++){for(W=R;
W<=Z;
W++){if(!f[T]||!f[T][W]){continue
}X=f[T][W].elm;
if(X!=V){P=c.grep(X.childNodes);
d(P,function(ab){V.appendChild(ab)
});
if(P.length){P=c.grep(V.childNodes);
S=0;
d(P,function(ab){if(ab.nodeName=="BR"&&I.getAttrib(ab,"data-mce-bogus")&&S++<P.length-1){V.removeChild(ab)
}})
}I.remove(X)
}}}r()
}}function l(S){var O,U,R,T,V,W,P,X,Q;
d(f,function(Y,Z){d(Y,function(ab,aa){if(j(ab)){ab=ab.elm;
V=ab.parentNode;
W=B(V,false);
O=Z;
if(S){return false
}}});
if(S){return !O
}});
for(T=0;
T<f[0].length;
T++){if(!f[O][T]){continue
}U=f[O][T].elm;
if(U!=R){if(!S){Q=h(U,"rowspan");
if(Q>1){t(U,"rowSpan",Q+1);
continue
}}else{if(O>0&&f[O-1][T]){X=f[O-1][T].elm;
Q=h(X,"rowSpan");
if(Q>1){t(X,"rowSpan",Q+1);
continue
}}}P=e(U);
t(P,"colSpan",U.colSpan);
W.appendChild(P);
R=U
}}if(W.hasChildNodes()){if(!S){I.insertAfter(W,V)
}else{V.parentNode.insertBefore(W,V)
}}}function g(P){var Q,O;
d(f,function(R,S){d(R,function(U,T){if(j(U)){Q=T;
if(P){return false
}}});
if(P){return !Q
}});
d(f,function(U,V){var R,S,T;
if(!U[Q]){return
}R=U[Q].elm;
if(R!=O){T=h(R,"colspan");
S=h(R,"rowspan");
if(T==1){if(!P){I.insertAfter(e(R),R);
v(Q,V,S-1,T)
}else{R.parentNode.insertBefore(e(R),R);
v(Q,V,S-1,T)
}}else{t(R,"colSpan",R.colSpan+1)
}O=R
}})
}function n(){var O=[];
d(f,function(P,Q){d(P,function(S,R){if(j(S)&&c.inArray(O,R)===-1){d(f,function(V){var T=V[R].elm,U;
U=h(T,"colSpan");
if(U>1){t(T,"colSpan",U-1)
}else{I.remove(T)
}});
O.push(R)
}})
});
r()
}function m(){var P;
function O(S){var R,T,Q;
R=I.getNext(S,"tr");
d(S.cells,function(U){var V=h(U,"rowSpan");
if(V>1){t(U,"rowSpan",V-1);
T=H(U);
v(T.x,T.y,1,1)
}});
T=H(S.cells[0]);
d(f[T.y],function(U){var V;
U=U.elm;
if(U!=Q){V=h(U,"rowSpan");
if(V<=1){I.remove(U)
}else{t(U,"rowSpan",V-1)
}Q=U
}})
}P=k();
d(P.reverse(),function(Q){O(Q)
});
r()
}function G(){var O=k();
I.remove(O);
r();
return O
}function L(){var O=k();
d(O,function(Q,P){O[P]=B(Q,true)
});
return O
}function C(Q,P){var R=k(),O=R[P?0:R.length-1],S=O.cells.length;
d(f,function(U){var T;
S=0;
d(U,function(W,V){if(W.real){S+=W.colspan
}if(W.elm.parentNode==O){T=1
}});
if(T){return false
}});
if(!P){Q.reverse()
}d(Q,function(V){var U=V.cells.length,T;
for(i=0;
i<U;
i++){T=V.cells[i];
t(T,"colSpan",1);
t(T,"rowSpan",1)
}for(i=U;
i<S;
i++){V.appendChild(e(V.cells[U-1]))
}for(i=S;
i<U;
i++){I.remove(V.cells[i])
}if(P){O.parentNode.insertBefore(V,O)
}else{I.insertAfter(V,O)
}})
}function H(O){var P;
d(f,function(Q,R){d(Q,function(T,S){if(T.elm==O){P={x:S,y:R};
return false
}});
return !P
});
return P
}function z(O){N=H(O)
}function K(){var Q,P,O;
P=O=0;
d(f,function(R,S){d(R,function(U,T){var W,V;
if(j(U)){U=f[S][T];
if(T>P){P=T
}if(S>O){O=S
}if(U.real){W=U.colspan-1;
V=U.rowspan-1;
if(W){if(T+W>P){P=T+W
}}if(V){if(S+V>O){O=S+V
}}}}})
});
return{x:P,y:O}
}function w(U){var R,Q,W,V,P,O,S,T;
E=H(U);
if(N&&E){R=Math.min(N.x,E.x);
Q=Math.min(N.y,E.y);
W=Math.max(N.x,E.x);
V=Math.max(N.y,E.y);
P=W;
O=V;
for(y=Q;
y<=O;
y++){U=f[y][R];
if(!U.real){if(R-(U.colspan-1)<R){R-=U.colspan-1
}}}for(x=R;
x<=P;
x++){U=f[Q][x];
if(!U.real){if(Q-(U.rowspan-1)<Q){Q-=U.rowspan-1
}}}for(y=Q;
y<=V;
y++){for(x=R;
x<=W;
x++){U=f[y][x];
if(U.real){S=U.colspan-1;
T=U.rowspan-1;
if(S){if(x+S>P){P=x+S
}}if(T){if(y+T>O){O=y+T
}}}}}I.removeClass(I.select("td.mceSelected,th.mceSelected"),"mceSelected");
for(y=Q;
y<=O;
y++){for(x=R;
x<=P;
x++){if(f[y][x]){I.addClass(f[y][x].elm,"mceSelected")
}}}}}function F(P,T,Q){if(q){var R=P.rows;
var S=null;
for(var O=0;
O<R.length;
O++){S=R[O];
if(O%2!=0){T.removeClass(S,"even");
T.addClass(S,"odd")
}else{T.removeClass(S,"odd");
T.addClass(S,"even")
}}}}c.extend(this,{deleteTable:s,split:D,merge:p,insertRow:l,insertCol:g,deleteCols:n,deleteRows:m,cutRows:G,copyRows:L,pasteRows:C,getPos:H,setStartCell:z,setEndCell:w,currentTable:J,runSmartTableLogic:F})
}c.create("tinymce.plugins.CStudioContextTable",{init:function(h,e){var l=h.contextControl.rteTableStyles||{};
var n,j,m=true;
function k(o){var p=/^true$/i;
return p.test(o)
}function g(s){var r=h.selection,o=h.dom.getParent(s||r.getNode(),"table");
if(o){var p=k(o.getAttribute("data-smart-table"));
tableGrid=new a(o,h.dom,r,p);
if(p){var q=o.getAttribute("data-smart-table-style");
if(q){c.extend(tableGrid,{runSmartTableLogic:new Function("currentTable","dom","event",l[q])})
}}return tableGrid
}}function f(){h.getBody().style.webkitUserSelect="";
if(m){h.dom.removeClass(h.dom.select("td.mceSelected,th.mceSelected"),"mceSelected");
m=false
}}d([["table","table.desc","mceInsertTable",true],["delete_table","table.del","mceTableDelete"],["delete_col","table.delete_col_desc","mceTableDeleteCol"],["delete_row","table.delete_row_desc","mceTableDeleteRow"],["col_after","table.col_after_desc","mceTableInsertColAfter"],["col_before","table.col_before_desc","mceTableInsertColBefore"],["row_after","table.row_after_desc","mceTableInsertRowAfter"],["row_before","table.row_before_desc","mceTableInsertRowBefore"],["row_props","table.row_desc","mceTableRowProps",true],["cell_props","table.cell_desc","mceTableCellProps",true],["split_cells","table.split_cells_desc","mceTableSplitCells",true],["merge_cells","table.merge_cells_desc","mceTableMergeCells",true]],function(o){h.addButton(o[0],{title:o[1],cmd:o[2],ui:o[3]})
});
if(!c.isIE){h.onClick.add(function(o,p){p=p.target;
if(p.nodeName==="TABLE"){o.selection.select(p);
o.nodeChanged()
}})
}h.onPreProcess.add(function(p,q){var o,r,s,u=p.dom,t;
o=u.select("table",q.node);
r=o.length;
while(r--){s=o[r];
u.setAttrib(s,"data-mce-style","");
if((t=u.getAttrib(s,"width"))){u.setStyle(s,"width",t);
u.setAttrib(s,"width","")
}if((t=u.getAttrib(s,"height"))){u.setStyle(s,"height",t);
u.setAttrib(s,"height","")
}}});
h.onNodeChange.add(function(q,o,s){var r;
s=q.selection.getStart();
r=q.dom.getParent(s,"td,th,caption");
o.setActive("table",s.nodeName==="TABLE"||!!r);
if(r&&r.nodeName==="CAPTION"){r=0
}o.setDisabled("delete_table",!r);
o.setDisabled("delete_col",!r);
o.setDisabled("delete_table",!r);
o.setDisabled("delete_row",!r);
o.setDisabled("col_after",!r);
o.setDisabled("col_before",!r);
o.setDisabled("row_after",!r);
o.setDisabled("row_before",!r);
o.setDisabled("row_props",!r);
o.setDisabled("cell_props",!r);
o.setDisabled("split_cells",!r);
o.setDisabled("merge_cells",!r)
});
h.onInit.add(function(p){var o,s,t=p.dom,q;
n=p.windowManager;
p.onMouseDown.add(function(u,v){if(v.button!=2){f();
s=t.getParent(v.target,"td,th");
o=t.getParent(s,"table")
}});
t.bind(p.getDoc(),"mouseover",function(A){var w,v,z=A.target;
if(s&&(q||z!=s)&&(z.nodeName=="TD"||z.nodeName=="TH")){v=t.getParent(z,"table");
if(v==o){if(!q){q=g(v);
q.setStartCell(s);
p.getBody().style.webkitUserSelect="none"
}q.setEndCell(z);
m=true
}w=p.selection.getSel();
try{if(w.removeAllRanges){w.removeAllRanges()
}else{w.empty()
}}catch(u){}A.preventDefault()
}});
p.onMouseUp.add(function(D,E){var v,z=D.selection,F,G=z.getSel(),u,A,w,C;
if(s){if(q){D.getBody().style.webkitUserSelect=""
}function B(H,J){var I=new c.dom.TreeWalker(H,H);
do{if(H.nodeType==3&&c.trim(H.nodeValue).length!=0){if(J){v.setStart(H,0)
}else{v.setEnd(H,H.nodeValue.length)
}return
}if(H.nodeName=="BR"){if(J){v.setStartBefore(H)
}else{v.setEndBefore(H)
}return
}}while(H=(J?I.next():I.prev()))
}F=t.select("td.mceSelected,th.mceSelected");
if(F.length>0){v=t.createRng();
A=F[0];
C=F[F.length-1];
v.setStartBefore(A);
v.setEndAfter(A);
B(A,1);
u=new c.dom.TreeWalker(A,t.getParent(F[0],"table"));
do{if(A.nodeName=="TD"||A.nodeName=="TH"){if(!t.hasClass(A,"mceSelected")){break
}w=A
}}while(A=u.next());
B(w);
z.setRng(v)
}D.nodeChanged();
s=q=o=null
}});
p.onKeyUp.add(function(u,v){f()
});
if(p&&p.plugins.contextmenu){p.plugins.contextmenu.onContextMenu.add(function(w,u,A){var B,z=p.selection,v=z.getNode()||p.getBody();
if(p.dom.getParent(A,"td")||p.dom.getParent(A,"th")||p.dom.select("td.mceSelected,th.mceSelected").length){u.removeAll();
if(v.nodeName=="A"&&!p.dom.getAttrib(v,"name")){u.add({title:"advanced.link_desc",icon:"link",cmd:p.plugins.advlink?"mceAdvLink":"mceLink",ui:true});
u.add({title:"advanced.unlink_desc",icon:"unlink",cmd:"UnLink"});
u.addSeparator()
}if(v.nodeName=="IMG"&&v.className.indexOf("mceItem")==-1){u.add({title:"advanced.image_desc",icon:"image",cmd:p.plugins.advimage?"mceAdvImage":"mceImage",ui:true});
u.addSeparator()
}u.add({title:"table.desc",icon:"table",cmd:"mceInsertTable",value:{action:"insert"}});
u.add({title:"table.props_desc",icon:"table_props",cmd:"mceInsertTable"});
u.add({title:"table.del",icon:"delete_table",cmd:"mceTableDelete"});
u.addSeparator();
B=u.addMenu({title:"table.cell"});
B.add({title:"table.cell_desc",icon:"cell_props",cmd:"mceTableCellProps"});
B.add({title:"table.split_cells_desc",icon:"split_cells",cmd:"mceTableSplitCells"});
B.add({title:"table.merge_cells_desc",icon:"merge_cells",cmd:"mceTableMergeCells"});
B=u.addMenu({title:"table.row"});
B.add({title:"table.row_desc",icon:"row_props",cmd:"mceTableRowProps"});
B.add({title:"table.row_before_desc",icon:"row_before",cmd:"mceTableInsertRowBefore"});
B.add({title:"table.row_after_desc",icon:"row_after",cmd:"mceTableInsertRowAfter"});
B.add({title:"table.delete_row_desc",icon:"delete_row",cmd:"mceTableDeleteRow"});
B.addSeparator();
B.add({title:"table.cut_row_desc",icon:"cut",cmd:"mceTableCutRow"});
B.add({title:"table.copy_row_desc",icon:"copy",cmd:"mceTableCopyRow"});
B.add({title:"table.paste_row_before_desc",icon:"paste",cmd:"mceTablePasteRowBefore"}).setDisabled(!j);
B.add({title:"table.paste_row_after_desc",icon:"paste",cmd:"mceTablePasteRowAfter"}).setDisabled(!j);
B=u.addMenu({title:"table.col"});
B.add({title:"table.col_before_desc",icon:"col_before",cmd:"mceTableInsertColBefore"});
B.add({title:"table.col_after_desc",icon:"col_after",cmd:"mceTableInsertColAfter"});
B.add({title:"table.delete_col_desc",icon:"delete_col",cmd:"mceTableDeleteCol"})
}else{u.add({title:"table.desc",icon:"table",cmd:"mceInsertTable"})
}})
}if(!c.isIE){function r(){var u;
for(u=p.getBody().lastChild;
u&&u.nodeType==3&&!u.nodeValue.length;
u=u.previousSibling){}if(u&&u.nodeName=="TABLE"){p.dom.add(p.getBody(),"p",null,'<br mce_bogus="1" />')
}}if(c.isGecko){p.onKeyDown.add(function(v,z){var u,w,A=v.dom;
if(z.keyCode==37||z.keyCode==38){u=v.selection.getRng();
w=A.getParent(u.startContainer,"table");
if(w&&v.getBody().firstChild==w){if(b(u,w)){u=A.createRng();
u.setStartBefore(w);
u.setEndBefore(w);
v.selection.setRng(u);
z.preventDefault()
}}}})
}p.onKeyUp.add(r);
p.onSetContent.add(r);
p.onVisualAid.add(r);
p.onPreProcess.add(function(u,w){var v=w.node.lastChild;
if(v&&v.childNodes.length==1&&v.firstChild.nodeName=="BR"){u.dom.remove(v)
}});
r()
}});
d({mceTableSplitCells:function(o){o.split()
},mceTableMergeCells:function(p){var q,r,o;
o=h.dom.getParent(h.selection.getNode(),"th,td");
if(o){q=o.rowSpan;
r=o.colSpan
}if(!h.dom.select("td.mceSelected,th.mceSelected").length){n.open({url:e+"/merge_cells.htm",width:240+parseInt(h.getLang("table.merge_cells_delta_width",0)),height:110+parseInt(h.getLang("table.merge_cells_delta_height",0)),inline:1},{rows:q,cols:r,onaction:function(s){p.merge(o,s.cols,s.rows)
},plugin_url:e})
}else{p.merge()
}},mceTableInsertRowBefore:function(o){o.insertRow(true)
},mceTableInsertRowAfter:function(o){o.insertRow()
},mceTableInsertColBefore:function(o){o.insertCol(true)
},mceTableInsertColAfter:function(o){o.insertCol()
},mceTableDeleteCol:function(o){o.deleteCols()
},mceTableDeleteRow:function(o){o.deleteRows()
},mceTableCutRow:function(o){j=o.cutRows()
},mceTableCopyRow:function(o){j=o.copyRows()
},mceTablePasteRowBefore:function(o){o.pasteRows(j,true)
},mceTablePasteRowAfter:function(o){o.pasteRows(j)
},mceTableDelete:function(o){o.deleteTable()
}},function(p,o){h.addCommand(o,function(){var q=g();
if(q){p(q);
q.runSmartTableLogic(q.currentTable,h.dom,o);
h.execCommand("mceRepaint");
f()
}})
});
d({mceInsertTable:function(o){n.open({url:e+"/table.htm",width:400+parseInt(h.getLang("table.table_delta_width",0)),height:320+parseInt(h.getLang("table.table_delta_height",0)),inline:1},{plugin_url:e,action:o?o.action:0})
},mceTableRowProps:function(){n.open({url:e+"/row.htm",width:400+parseInt(h.getLang("table.rowprops_delta_width",0)),height:295+parseInt(h.getLang("table.rowprops_delta_height",0)),inline:1},{plugin_url:e})
},mceTableCellProps:function(){n.open({url:e+"/cell.htm",width:400+parseInt(h.getLang("table.cellprops_delta_width",0)),height:295+parseInt(h.getLang("table.cellprops_delta_height",0)),inline:1},{plugin_url:e})
}},function(p,o){h.addCommand(o,function(q,r){p(r)
})
})
}});
c.PluginManager.add("cs_table",c.plugins.CStudioContextTable)
})(tinymce);