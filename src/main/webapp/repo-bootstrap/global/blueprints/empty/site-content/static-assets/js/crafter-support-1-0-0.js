var Crafter = Crafter || {};

Crafter.Components = {
	render: function(){
	    var elems = jQuery.makeArray(jQuery(".crComponent"));
	    var length = elems.length;
		
	    for(var i=0; i < length; ++i){
			var o_elem = document.getElementById("o_" + elems[i].id);
			if(document.getElementById(elems[i].id) && o_elem){
				if(o_elem.children){
					document.getElementById(elems[i].id).innerHTML = o_elem.children[0].innerHTML;
				}else{
					var j = 0;
					while(!o_elem.childNodes[j].tagName){
						j++;
					}
					document.getElementById(elems[i].id).innerHTML = o_elem.childNodes[j].innerHTML;
				}
			}
		} // For
	}
};

$(document).ready(function() {
	Crafter.Components.render();
});