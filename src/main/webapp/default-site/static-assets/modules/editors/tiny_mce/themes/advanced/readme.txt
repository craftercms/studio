Changes made to editor_template.js:

#1) Add the name of the window to the tinymce size cookie:

This snippet of code:
Cookie.setHash("TinyMCE_" + ed.id + "_size" + window.name, {

was originally:
Cookie.setHash("TinyMCE_" + ed.id + "_size", {

============================================================

This snippet of code:
var o = Cookie.getHash("TinyMCE_" + ed.id + "_size" + window.name), c = DOM.get(ed.id + '_tbl');

was originally:
var o = Cookie.getHash("TinyMCE_" + ed.id + "_size"), c = DOM.get(ed.id + '_tbl');

