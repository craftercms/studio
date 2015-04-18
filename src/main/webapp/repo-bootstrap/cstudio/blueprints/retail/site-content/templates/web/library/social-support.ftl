<#macro facebookSupport>
	<div id="fb-root"></div>
	<script>(function(d, s, id) {
	  var js, fjs = d.getElementsByTagName(s)[0];
	  if (d.getElementById(id)) return;
	  js = d.createElement(s); js.id = id;
	  js.src = "//connect.facebook.net/en_US/all.js#xfbml=1&appId=${model.facebookAppKey}";
	  fjs.parentNode.insertBefore(js, fjs);
	}(document, 'script', 'facebook-jssdk'));</script>
</#macro>

<#macro facebookLike contentUrl width="450" faces="true" send="true" layout="default">
   <div class="fb-like" data-href="${contentUrl}" data-send="${send}" data-layout="${layout}" data-width="${width}" data-show-faces="${faces}"></div>
</#macro>

<#macro facebookComment contentUrl  width="450" posts="10">
   <div class="fb-comments" data-href="${contentUrl}" data-width="${width}" data-num-posts="${posts}"></div>
</#macro>