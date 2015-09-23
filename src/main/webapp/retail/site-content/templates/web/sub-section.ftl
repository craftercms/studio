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

<div id="main-container" class="promo-page">
            	
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
            

            <div id="fit-detail-view" style="width: 868px;">
			    <div id="fit-info" >
				    <div id="fit-desc"  style="display: inline-block; padding-top: 0px; position: relative; width: 175px; top: 26px; margin-top: -27px;">
					    <h3>Skinny</h3>
					    <p>
                    Our superstar Skinny jean. Lower rise silhouette with slim leg opening is always flattering and provides abundant day-to-night styling appeal. Fitted and forward, Stella always gets special treatment and is the go-to choice for exclusive embellishments and distinctive washes.
                </p>
				    </div>
				   
				   
			    <div id="fit-small-product-view"  style="display:inline-block; left:-10px; position:relative;">
				    <div id="rotate-left-arrow"><div></div></div>
				    <div id="rotate-right-arrow"><div></div></div>

				    <div id="rotate-product-img">
					    <ul style="margin:0;">
					    <li style="opacity: 1; display: list-item; list-style:none;"><img src="/static-assets/images/nokeep/front.png"></li>
					    <li style="display: none;"><img src="/static-assets/images/nokeep/back.png"></li>
					    <li style="display: none;"><img src="/static-assets/images/nokeep/side.png"></li>
					    <li style="display: none;"><img src="/static-assets/images/nokeep/side2.png"></li></ul>
				    </div>

			    </div>

			    <div id="fit-large-product-img"   style="display:inline-block; left:-115px; position:relative;">
				    <ul style="margin:0;">
				    <li style="opacity: 1; display: list-item; list-style:none;"><img src="/static-assets/images/nokeep/front-large.png"></li>
				    <li style="display: none;"><img src="/static-assets/images/nokeepback-large.png"></li>
				    <li style="display: none;"><img src="/static-assets/images/nokeepside-large.png"></li>
				    <li style="display: none;"><img src="/static-assets/images/nokeepside2-large.png"></li></ul>
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
