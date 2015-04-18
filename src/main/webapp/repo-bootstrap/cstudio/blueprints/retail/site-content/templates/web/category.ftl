<#include "/templates/system/common/cstudio-support.ftl" />
<#include "/templates/web/navigation/navigation.ftl">
<#include "/templates/web/library/social-support.ftl"/>

<#assign productsPerPage = model.itemsPerPage?number />
<#assign pageNum = (RequestParameters["p"]!1)?number - 1 />
<#assign uri = requestContext.requestUri />
<#assign gender = uri?substring(1, uri?index_of("/", 2)) />
<#assign category = uri?substring(uri?index_of("/", 3)+1, uri?last_index_of("/")) />
<#assign collection = uri?substring(uri?last_index_of("/")+1) />

<#assign sort = (Cookies["category-sort"]!"")?replace("-", " ")>
<#assign filterSize = (Cookies["category-filter-size"]!"*")>
<#assign filterColor = (Cookies["category-filter-color"]!"*")>
<#assign keyword = (RequestParameters["q"]!"") />

<#if gender == "womens" >
   <#assign gender = "female" />
<#elseif gender == "mens">
   <#assign gender = "men" />
<#else>
   <#assign gender = "" />
</#if>



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
	  var setCookie = function(name, value) {
	  	document.cookie = name + "=" + value + "; path=/;"; 
	  	document.location = document.location;
	  	return false;
	  }
	</script>
</head>
<body>
	<@facebookSupport />
    <#assign queryStatement = 'crafterSite:"rosie" ' />
    
    <#if keyword != "">
   	  <#assign queryStatement = queryStatement + 'AND attDemo:*' + keyword + '* ' />
    </#if>
    <#assign queryStatement = queryStatement + 'AND content-type:"/component/jeans" ' />
	<#assign queryStatement = queryStatement + 'AND gender.item.key:"' + gender + '" ' />
	<#assign queryStatement = queryStatement + 'AND category:"' + category + '" ' />
	<#assign queryStatement = queryStatement + 'AND collection.item.key:"' + collection + '" ' />
	
	<#assign filteredQueryStatement = queryStatement />
	<#assign filteredQueryStatement = filteredQueryStatement + 'AND size.item.value:"' + filterSize + '" ' />
	<#assign filteredQueryStatement = filteredQueryStatement + 'AND color:"' + filterColor + '" ' />

	<#assign query = searchService.createQuery()>
	<#assign query = query.setQuery(queryStatement) />
	<#assign query = query.addParam("facet","on") />
    <#assign query = query.addParam("facet.field","size.item.value") />
    <#assign query = query.addParam("facet.field","color") />

	
	<#assign filteredQuery = searchService.createQuery()>		
	<#assign filteredQuery = filteredQuery.setQuery(filteredQueryStatement) />
	<#assign filteredQuery = filteredQuery.setStart(pageNum)>
	<#assign filteredQuery = filteredQuery.setRows(productsPerPage)>

	
	<#if sort?? && sort != "">
		<#assign filteredQuery = filteredQuery.addParam("sort","" + sort) />
	</#if>

	<#assign executedQuery = searchService.search(query) />	
	<#assign executedFilteredQuery = searchService.search(filteredQuery) />

	<#assign productsFound = executedFilteredQuery.response.numFound>	
	<#assign products = executedFilteredQuery.response.documents />
	<#assign sizes = executedQuery.facet_counts.facet_fields['size.item.value'] />
	<#assign colors = executedQuery.facet_counts.facet_fields['color'] />




<div id="main-container" class="categories-page">
            	
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
            
            <div class="pull-left">
	            <strong class="uppercase">Sort By:</strong>
	            <select onchange="setCookie('category-sort', this.value);">
	            	<option  <#if sort=="arrivalDate_dt desc">selected</#if> value="arrivalDate_dt-desc">New Arrivals</option>
	            	<!--option  <#if sort=="collection.item.value asc">selected</#if> value="collection.item.value-asc">Collection</option-->
	            	<!--option>Best Sellers</option-->
	            	<option  <#if sort=="price_d desc">selected</#if>  value="price_d-desc">Price: High to Low</option>
	            	<option  <#if sort=="price_d asc">selected</#if> value="price_d-asc">Price: Low to High</option>
	            </select>
            </div>

            <div class="pull-right clearfix">
	            <strong class="uppercase">Filter By:</strong>

	            <select style="width: 90px"  onchange="setCookie('category-filter-size', this.value);">
	                <option <#if filterSize=='*'>selected</#if> value="*">Size</option>
	            	<#list sizes?keys as sizeOption>
		            	<option <#if filterSize==sizeOption>selected</#if> value="${sizeOption}">${sizeOption} (${sizes[sizeOption]})</option>
		            </#list>
	            </select>
	            
	            <select style="width: 90px"  onchange="setCookie('category-filter-color', this.value);">
	                <option <#if filterColor=='*'>selected</#if> value="*">Color</option>
	            	<#list colors?keys as colorOption>
		            	<option <#if filterColor==colorOption>selected</#if> value="${colorOption}">${colorOption} (${colors[colorOption]})</option>
		            </#list>
	            </select>           
	            
	            <select style="width: 90px"  onchange="setCookie('category-filter-fitdetail', this.value);">
	                <option selected value="*">Fit Detail</option>
	            </select>  
	            
            </div>
            
            <div class="categories clearfix">

            		<#list products as product>
            			<#assign productId = product.localId?substring(product.localId?last_index_of("/")+1)?replace('.xml','')>
            			<@ice componentPath=product.localId />
            				   
 		            	<div class="cat">
 		            		<img src="${product.frontImage}" />
		            		
		            		
		            			

		            		<div class="title" style='width:170px;'><a href="/womens/jeans/details?p=${productId}">${product.productTitle}</a></div>
		            		<div class="price">${product.price_d?string.currency}</div>
							<div class="rating">
		            			<!--span class="star checked"></span>
		            			<span class="star checked"></span>
		            			<span class="star checked"></span>
		            			<span class="star checked"></span>
		            			<span class="star"></span-->
		            			
		            			<@facebookLike contentUrl='http://www.rosiesrivets.com/womens/jeans/details?p=${productId}' width="75" faces="false" layout="button_count"/>
		            		</div>

		            	</div>
					</#list>
            
            </div>
            
            <div class="pagination">
            	<ul>
            		<#assign pages = (productsFound / productsPerPage)?round />
            		<#if pages == 0><#assign pages = 1 /></#if>
            		
            		<#list 1..pages as count>
            			<li <#if count=(pageNum+1) >class="active"</#if>><a href="${uri}?p=${count}">${count}</a></li>
            		</#list>
            	</ul>
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

</body>
</html>
<@cstudioOverlaySupport/>