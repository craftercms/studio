<#include "/templates/system/common/cstudio-support.ftl" />
<#include "/templates/web/navigation/navigation.ftl">

<#assign objectId = RequestParameters["p"]!"none" />
<#assign uri = requestContext.requestUri />
<#assign gender = uri?substring(1, uri?index_of("/", 2)) />
<#assign category = uri?substring(uri?index_of("/", 2)+1, uri?last_index_of("/")) />
<#assign productPath = "/site/components/products/" + category + "/" + gender + "/" + objectId + ".xml" />

<#assign product = siteItemService.getSiteItem(productPath) />

<#assign visted = (profile.setAttribute("visitedProductPage", "true")) />

<!DOCTYPE html>
<html lang="en">
<head>
    
    <meta charset="utf-8">
    <title>Rosie Rivet - Crafter Rivet Demo Site</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="Rivet Logic Corporation">

    <link href="/static-assets/css/main.css" rel="stylesheet">
    <link type="text/css" href="/static-assets/css/mojozoom.css" rel="stylesheet" />  
    <script src="/static-assets/js/mojozoom.js"></script>
    
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->



<style type="text/css">
	#MainImageRegion {
		width: auto;
	}
</style>
<style type="text/css">
	/* <![CDATA[ > */
	/* Highlight style */
	#realZOOMHighlight {
		background-image: url(/static-assets/images/grid.gif);
		border-width: 0px;
		border-style: solid;
		border-color: #252525;
	}
	/* ]]> */
</style>
	<script>
	  var setCookie = function(name, value) {
	  	document.cookie = name + "=" + value + "; path=/;"; 
	  	return false;
	  }
	</script>

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
				<@renderNavigation "/site/website", 5 />	
			</ul>
            
        </div>
        <div class="span9" id="content">
            
			<div class="pagination mt0 mb5">
				<ul>
					<li><a href="#">&lt; Previous Item</a></li>
					<li><a href="#">Next Item &gt;</a></li>
				</ul>
			</div>
			
			<div class="row-fluid">
				<div class="span6">
				
					<div  class="product-img mb20">
					
					<div id='MainImageRegion'>	
						<img id='ContentPlaceHolder1_ProductImage' class="img-big"  onmouseover="javascript:window.status=&#39;&#39;;return true;" src="${product.frontImage!''}"  data-zoomsrc=""/>
						</div>
						
						<img class="img-sml" src="${product.frontImageThumb!''}" />
						
						<img class="img-sml" src="${product.backImage?replace(".png", "-small.png")!''}" />
						
						<img class="img-sml" src="${product.zoomImage?replace(".png", "-small.png")!''}" />
					</div>
					
					<script>
					MojoZoom.makeZoomable(  
    document.getElementById("ContentPlaceHolder1_ProductImage"),   
    "${product.frontImage!''}",  
    document.getElementById("zoom"),  
    440, 625,  
    false);  
					</script>
				</div>
				<div id="zoom"></div>
                    
					<@ice componentPath=productPath/>
					<script>setCookie('lastViewedProduct', '${productPath}')</script>
					<h2 class="amaranth mt0">${product.productTitle}</h2>
					<div class="price mt5">${product.price_d?string.currency}</div>
					
					<div class="sku mt5">SKU | WLNS22S33-09</div>
					
					<div class="rating mt5">
						<div class="stars">
							<span class="star checked"></span>
							<span class="star checked"></span>
							<span class="star checked"></span>
							<span class="star"></span>
							<span class="star"></span>
						</div>
						<a class="rosie-red" href="javscript:">
							reviews
						</a> |
						<a class="rosie-red" href="javscript:">
							write a review
						</a>
					</div>
					
					<p class="description mt5">
						${product.description}
					</p>
					
					<div class="measures mt5">
						<strong class="uppercase">Standard Measurement</strong>
						<ul>
							<li>
								${product.measurementDetails}
							</li>
							<li>
								Front Rise: ${product.frontRise}"
							</li>
							<li>
								Back Rise: ${product.backRise}"
							</li>
							<li>
								Inseam: ${product.inseam}"
							</li>
							<li>
								Leg Opening: ${product.legOpening}"
							</li>
							<li>
								Measurements may vary based on size, fabric and wash.
							</li>
						</ul>
					</div>				

					<#if product.fit??>
					<div class="fit">
						<strong class="uppercase">Fit Details</strong>
						<ul>
							<#list product.fit.item as item>
							  <li>${item.fitNote}</li>
							</#list>
						</ul>
					</div>
					</#if>
					
					<div class="options">
						
						<select>
							<option>wash</option>
						</select>
					
						<select class="small">
							<option>size</option>
						</select>
					
						<select class="small">
							<option>quantity</option>
						</select>
						
					</div>
					
					<button class="btn btn-danger uppercase">
						Add to Bag
					</button>
				
				</div>
			</div>
			<div class="row-fluid">
				<!--div class="span6">
				
					<div class="dark-box pad">
						<h5>We also recommend</h5>
					</div>
					
					<div class="product-img">
						<img class="img-sml" src="img/cms-product-2.png" />
						
						<img class="img-sml" src="img/cms-product-3.png" />
						
						<img class="img-sml" src="img/cms-product-4.png" />
					</div>
				
				
				</div>
				<div class="span6">
				
					<div class="dark-box pad">
						<h5>Fashion Blogger Looks</h5>
					</div>
					
					<div class="look-img">
						<img class="look" src="img/cms-look1.png" />
						
						<img class="look" src="img/cms-look1.png" />
						
						<img class="look" src="img/cms-look1.png" />
					</div>
				
				</div>
			</div-->
            
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
