<#include "/templates/system/common/cstudio-support.ftl" />
<#include "/templates/web/navigation/navigation.ftl">

<!DOCTYPE html>
<html lang="en">
<head>
    <link href="/static-assets/css/main.css" rel="stylesheet">
</head>
<body>
<div id="main-container" class="product-page">

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
<#assign twitterConf = twitterService.createTwitterConf("qxyK1X4HuN9DMBD6zYsWxQ", "mZYvfV2PUU7uwd4rsbNEay6vyPRaY8l1yO0kE1rAw", "27591924-n9VYaXzu02X3dmv2II9QxuQYMyU2njYygv8I9xHQ", "PzxWQj50GwtUZYarIGqbrQZFO23yIcAGa1rK6WsavQ") />
                <#assign updates = twitterService.getUserFeed(twitterConf, "craftersoftware") />

                <h4 class="font-brandon-black c-white uppercase">
                <a class="pull-right button font-brandon-black" href="http://twitter.com/craftersoftware" target="_blank">
                        Follow Crafter
                    </a>
                    Twitter Feed
                </h4>
                <#list updates as update>
                	<#if update_index < 5 >
						<div class="tweet">
		                    <div class="clearfix">
		                    <a href="http://twitter.com/craftersoftware" class="user" target="_blank">@${update.user.screenName}</a>
		                    <span class="date">${twitterService.dateToRelative(update.createdAt)}</span>
		                    </div>
		                    <p>${update.text}</p>
		                </div>
	                </#if>
                </#list>
                
               <div class="span9 mb10">
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
        
        </div>
     </div>
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