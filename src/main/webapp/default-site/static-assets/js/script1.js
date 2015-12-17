/* Author: David Quiros
   Last Modified: March 8, 2012
*/

$('#main').bgStretcher({
	images: ['/static-assets/img/gc3.jpg'], 
	imageWidth: 1533,
	imageHeight: 1148,
    anchoringImg: 'center top'
});

$('.bgOpaque').each(function(i) {
    $(this).
    	children().wrapAll('<div class="opaque-container" />').end().
		append('<div class="opaque-background" />'); 
});