<#include "/templates/system/common/cstudio-support.ftl" />
<#include "/templates/web/navigation/navigation.ftl">
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
				<@renderNavigation "/site/website", 2 />
			</ul>
            
        </div>
        <div class="span9" id="content">
			<h2>My Account</h2>
			<table border="0">
				<tr>
					<th>Username:</th>
					<td>${profile.userName}</td>
				</tr>
				<#if profile.attributes?? && (profile.attributes?size > 0)>
					<#list profile.attributes?keys as attributeName>
						<tr>
							<th>${attributeName?cap_first}:</th>
							<td>${profile.attributes[attributeName]}</td>
						</tr>
					</#list>
				</#if>				
			</table>
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


<@cstudioOverlaySupport/>
</body>
</html>