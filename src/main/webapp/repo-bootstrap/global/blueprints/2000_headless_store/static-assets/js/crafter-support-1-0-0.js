/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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