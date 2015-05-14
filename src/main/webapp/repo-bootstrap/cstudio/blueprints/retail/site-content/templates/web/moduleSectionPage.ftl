<#include "/templates/system/common/cstudio-support.ftl" />
<#include "/templates/web/navigation/navigation.ftl">
<#include "/templates/web/library/multivariant-support.ftl">
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Rosie Rivet - Crafter Rivet Demo Site</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="Rivet Logic Corporation">

    <link href="/static-assets/css/main.css" rel="stylesheet">

    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
    
    <script>
  (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-41992468-1', '127.0.0.1');
  ga('send', 'pageview');

</script>
<!-- Google Analytics Content Experiment code -->


<@experiment>
<script>function utmx_section(){}function utmx(){}(function(){var
k='73876097-0',d=document,l=d.location,c=d.cookie;
if(l.search.indexOf('utm_expid='+k)>0)return;
function f(n){if(c){var i=c.indexOf(n+'=');if(i>-1){var j=c.
indexOf(';',i);return escape(c.substring(i+n.length+1,j<0?c.
length:j))}}}var x=f('__utmx'),xx=f('__utmxx'),h=l.hash;d.write(
'<sc'+'ript src="'+'http'+(l.protocol=='https:'?'s://ssl':
'://www')+'.google-analytics.com/ga_exp.js?'+'utmxkey='+k+
'&utmx='+(x?x:'')+'&utmxx='+(xx?xx:'')+'&utmxtime='+new Date().
valueOf()+(h?'&utmxhash='+escape(h.substr(1)):'')+
'" type="text/javascript" charset="utf-8"><\/sc'+'ript>')})();
</script><script>utmx('url','A/B');</script>
<!-- End of Google Analytics Content Experiment code -->
</@experiment>

</head>
<body>

<div id="main-container">
            	
<#include "/templates/web/fragments/header.ftl"/>

<div class="container-fluid" id="content-body">
    
    <div class="row-fluid">
        <div class="span3 mb10" id="site-nav">
        
        	<div class="input-append" id="site-search">
	        	<input type="text" class="wauto" placeholder="search" />
	        	<a class="add-on">
		        	<i class="icon icon-search"></i>
	        	</a>
        	</div>
            
			<ul class="nav nav-list amaranth uppercase">
				<@renderNavigation "/site/website", 1 />
			</ul>
            
        </div>
        <div class="span9" id="content">
				<@componentZone id="modules">
					<#if model.modules?? && model.modules.item??>
	       				<#list model.modules.item as module>
	        				    <@draggableComponent component=module  >
		        					<@renderComponent component=module />
		        				</@draggableComponent>
	       				</#list>
	       			</#if>
				</@componentZone>

        </div>
    <hr>
    <#include "/templates/web/fragments/footer.ftl"/>

</div>
<!-- /container -->

</div>

<script src="/static-assets/js/jquery.min.js"></script>
<script src="/static-assets/js/bootstrap.min.js"></script>
<script src="/static-assets/js/main.js"></script>
<script>
 $(".tile").mouseover(function( ) {
	$(this).find(".gradient-overlay")[0].style.display =  "block";
});

 $(".tile").mouseout(function( ) {
	$(this).find(".gradient-overlay")[0].style.display =  "none";
});

</script>



<@cstudioOverlaySupport/>
</body>
</html>
