tinyMCEPopup.requireLangPack();
var MergeCellsDialog={init:function(){var b=document.forms[0];
b.numcols.value=tinyMCEPopup.getWindowArg("cols",1);
b.numrows.value=tinyMCEPopup.getWindowArg("rows",1)
},merge:function(){var d,c=document.forms[0];
tinyMCEPopup.restoreSelection();
d=tinyMCEPopup.getWindowArg("onaction");
d({cols:c.numcols.value,rows:c.numrows.value});
tinyMCEPopup.close()
}};
tinyMCEPopup.onInit.add(MergeCellsDialog.init,MergeCellsDialog);