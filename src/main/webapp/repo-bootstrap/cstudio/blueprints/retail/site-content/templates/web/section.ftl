<#import "/templates/system/common/cstudio-support.ftl" as studio/>
<#include "/templates/web/navigation/navigation.ftl">
<#include "/templates/web/google-map.ftl" />

<#macro simpleDragDrop componentList name>
	<@studio.componentZone id="${name}">
		<#if componentList?? && componentList.item??>
			<#list componentList.item as module>
				<@studio.draggableComponent component=module >
					<@studio.renderComponent component=module />
				</@studio.draggableComponent>
			</#list>
		</#if>
	</@studio.componentZone>
</#macro>


<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Rosie Rivet - Crafter Demo Site</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="Crafter Software">

    <link href="/static-assets/css/main.css" rel="stylesheet">

    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
    <@googleMapSupport />
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
          
            <@simpleDragDrop model.area1 "area1" />
            <br/>

           <div class="row-fluid adverts mobile-hide">
			<div class="span4 mb10">
				<@simpleDragDrop model.bottomPromos1 "bottomPromos1" />
			</div>
			<div class="span4 mb10">
					<@simpleDragDrop model.bottomPromos2 "bottomPromos2" />
				</div>
				<div class="span4 mb10">
            
				<@simpleDragDrop model.bottomPromos3 "bottomPromos3" />
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
<@studio.toolSupport />
</body>
</html>

