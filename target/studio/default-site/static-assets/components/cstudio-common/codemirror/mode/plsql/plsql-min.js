CodeMirror.defineMode("plsql",function(c,g){var i=c.indentUnit,h=g.keywords,f=g.functions,j=g.types,e=g.sqlplus,k=g.multiLineStrings;
var b=/[+\-*&%=<>!?:\/|]/;
function a(r,q,p){q.tokenize=p;
return p(r,q)
}var n;
function m(q,p){n=q;
return p
}function d(r,q){var p=r.next();
if(p=='"'||p=="'"){return a(r,q,o(p))
}else{if(/[\[\]{}\(\),;\.]/.test(p)){return m(p)
}else{if(/\d/.test(p)){r.eatWhile(/[\w\.]/);
return m("number","number")
}else{if(p=="/"){if(r.eat("*")){return a(r,q,l)
}else{r.eatWhile(b);
return m("operator","operator")
}}else{if(p=="-"){if(r.eat("-")){r.skipToEnd();
return m("comment","comment")
}else{r.eatWhile(b);
return m("operator","operator")
}}else{if(p=="@"||p=="$"){r.eatWhile(/[\w\d\$_]/);
return m("word","variable")
}else{if(b.test(p)){r.eatWhile(b);
return m("operator","operator")
}else{r.eatWhile(/[\w\$_]/);
if(h&&h.propertyIsEnumerable(r.current().toLowerCase())){return m("keyword","keyword")
}if(f&&f.propertyIsEnumerable(r.current().toLowerCase())){return m("keyword","builtin")
}if(j&&j.propertyIsEnumerable(r.current().toLowerCase())){return m("keyword","variable-2")
}if(e&&e.propertyIsEnumerable(r.current().toLowerCase())){return m("keyword","variable-3")
}return m("word","variable")
}}}}}}}}function o(p){return function(u,s){var t=false,r,q=false;
while((r=u.next())!=null){if(r==p&&!t){q=true;
break
}t=!t&&r=="\\"
}if(q||!(t||k)){s.tokenize=d
}return m("string","plsql-string")
}
}function l(s,r){var p=false,q;
while(q=s.next()){if(q=="/"&&p){r.tokenize=d;
break
}p=(q=="*")
}return m("comment","plsql-comment")
}return{startState:function(p){return{tokenize:d,startOfLine:true}
},token:function(r,q){if(r.eatSpace()){return null
}var p=q.tokenize(r,q);
return p
}}
});
(function(){function d(j){var g={},h=j.split(" ");
for(var f=0;
f<h.length;
++f){g[h[f]]=true
}return g
}var c="abort accept access add all alter and any array arraylen as asc assert assign at attributes audit authorization avg base_table begin between binary_integer body boolean by case cast char char_base check close cluster clusters colauth column comment commit compress connect connected constant constraint crash create current currval cursor data_base database date dba deallocate debugoff debugon decimal declare default definition delay delete desc digits dispose distinct do drop else elsif enable end entry escape exception exception_init exchange exclusive exists exit external fast fetch file for force form from function generic goto grant group having identified if immediate in increment index indexes indicator initial initrans insert interface intersect into is key level library like limited local lock log logging long loop master maxextents maxtrans member minextents minus mislabel mode modify multiset new next no noaudit nocompress nologging noparallel not nowait number_base object of off offline on online only open option or order out package parallel partition pctfree pctincrease pctused pls_integer positive positiven pragma primary prior private privileges procedure public raise range raw read rebuild record ref references refresh release rename replace resource restrict return returning reverse revoke rollback row rowid rowlabel rownum rows run savepoint schema segment select separate session set share snapshot some space split sql start statement storage subtype successful synonym tabauth table tables tablespace task terminate then to trigger truncate type union unique unlimited unrecoverable unusable update use using validate value values variable view views when whenever where while with work";
var b="abs acos add_months ascii asin atan atan2 average bfilename ceil chartorowid chr concat convert cos cosh count decode deref dual dump dup_val_on_index empty error exp false floor found glb greatest hextoraw initcap instr instrb isopen last_day least lenght lenghtb ln lower lpad ltrim lub make_ref max min mod months_between new_time next_day nextval nls_charset_decl_len nls_charset_id nls_charset_name nls_initcap nls_lower nls_sort nls_upper nlssort no_data_found notfound null nvl others power rawtohex reftohex round rowcount rowidtochar rpad rtrim sign sin sinh soundex sqlcode sqlerrm sqrt stddev substr substrb sum sysdate tan tanh to_char to_date to_label to_multi_byte to_number to_single_byte translate true trunc uid upper user userenv variance vsize";
var a="bfile blob character clob dec float int integer mlslabel natural naturaln nchar nclob number numeric nvarchar2 real rowtype signtype smallint string varchar varchar2";
var e="appinfo arraysize autocommit autoprint autorecovery autotrace blockterminator break btitle cmdsep colsep compatibility compute concat copycommit copytypecheck define describe echo editfile embedded escape exec execute feedback flagger flush heading headsep instance linesize lno loboffset logsource long longchunksize markup native newpage numformat numwidth pagesize pause pno recsep recsepchar release repfooter repheader serveroutput shiftinout show showmode size spool sqlblanklines sqlcase sqlcode sqlcontinue sqlnumber sqlpluscompatibility sqlprefix sqlprompt sqlterminator suffix tab term termout time timing trimout trimspool ttitle underline verify version wrap";
CodeMirror.defineMIME("text/x-plsql",{name:"plsql",keywords:d(c),functions:d(b),types:d(a),sqlplus:d(e)})
}());