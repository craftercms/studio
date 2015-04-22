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
				<@renderNavigation "/site/website", 1 />
			</ul>
            
        </div>
        <div class="span9" id="content">
            
            <div class="relative mb10" id="landing-banner">
	            
	            <a href="/womens/jeans/skinny"><button class="btn btn-danger uppercase" id="btn-shop-women">
	            	Skinny Collection
	            </button></a>
	            
            </div>
            
            <div class="desktop-hide tac">
	            
	            <button class="btn btn-danger uppercase">
	            	Shop Womens
	            </button>
	            
	            <button class="btn btn-danger uppercase">
	            	Shop Mens
	            </button>
	            
            </div>
            
            <div class="row-fluid adverts mobile-hide">
			<div class="span4 mb10">
				<@componentZone id="bottomPromos1">
					<#if model.bottomPromos1?? && model.bottomPromos1.item??>
	       				<#list model.bottomPromos1.item as module>
	        				    <@draggableComponent component=module  >
		        					<@renderComponent component=module />
		        				</@draggableComponent>
	       				</#list>
	       			</#if>
				</@componentZone>
			</div>
			<div class="span4 mb10">
				<@componentZone id="bottomPromos2">
					<#if model.bottomPromos2?? && model.bottomPromos2.item??>
	       				<#list model.bottomPromos2.item as module>
	        				    <@draggableComponent component=module  >
		        					<@renderComponent component=module />
		        				</@draggableComponent>
	       				</#list>
	       			</#if>
				</@componentZone>
				</div>
				<div class="span4 mb10">
            
				<@componentZone id="bottomPromos3">
					<#if model.bottomPromos3?? && model.bottomPromos3.item??>
	       				<#list model.bottomPromos3.item as module>
	        				    <@draggableComponent component=module  >
		        					<@renderComponent component=module />
		        				</@draggableComponent>
	       				</#list>
	       			</#if>
				</@componentZone>
            </div>            
            </div>
            
            
        </div>
    </div>

    <hr>

    <#include "/templates/web/fragments/footer.ftl"/>

</div>
<!-- /container -->

</div>

<script src="/static-assets/js/jquery.min.js"></script>
<script src="/static-assets/js/bootstrap.min.js"></script>
<script src="/static-assets/js/main.js"></script>
<@cstudioOverlaySupport/>
</body>
</html>

