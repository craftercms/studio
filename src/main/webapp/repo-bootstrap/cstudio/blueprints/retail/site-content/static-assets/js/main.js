(function ($, w) {
	"use strict";
	
	var prefix = 'flag-';
	
	var App = {
		setLanguage: function (lang) {
			// current lang element
			var cle = $('#current-lang'),
				code = lang.replace('#','');
			
			cle.removeClass(prefix + cle.attr('current'))
				.addClass(prefix + code);
				
			cle.attr('current', code);
		}
	}
	
	$('#language-select .dropdown-menu a').click(function (e) {
		
		e.preventDefault();
		
		App.setLanguage( $(this).attr('href') );
		
	});
	
}) (jQuery, window);