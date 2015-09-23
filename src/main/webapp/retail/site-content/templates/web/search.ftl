<#include "/templates/system/common/cstudio-support.ftl" />
<#include "/templates/web/navigation/navigation.ftl">
<#include "/templates/web/library/social-support.ftl"/>

<!--get query vaues-->
<#assign productsPerPage = 10 />
<#assign pageNum = (RequestParameters["p"]!1)?number - 1 />
<#assign sort = (Cookies["category-sort"]!"")?replace("-", " ")>
<#assign keyword = (RequestParameters["q"]!"") />
<#assign t1 = (RequestParameters["t1"]!"") />
<#assign t2 = (RequestParameters["t2"]!"") />
<#assign uri = "/search" />

<!-- build and execute query -->
<#assign queryStatement = 'crafterSite:"rosie" ' />

<#if t1 != "" && t2 != "">
	<#assign queryStatement = queryStatement + 'AND (content-type:"/component/video" OR content-type:"/page/generic") ' />
<#elseif t1 != "">
    <#assign queryStatement = queryStatement + 'AND (content-type:"/page/generic") ' />
<#elseif t2 != "">
    <#assign queryStatement = queryStatement + 'AND (content-type:"/component/video") ' />
</#if>



<#if keyword??>
   <#assign queryStatement = queryStatement + 'AND (body:"*'+keyword+'*" OR description:"*'+keyword+'*") ' />
</#if>

<#assign query = searchService.createQuery()>
<#assign query = query.setQuery(queryStatement) />

<#assign filteredQuery = searchService.createQuery()>		
<#assign filteredQuery = query.setQuery(filteredQueryStatement) />
<#assign filteredQuery = query.setStart(pageNum)>
<#assign filteredQuery = query.setRows(productsPerPage)>

<#if sort?? && sort != "">
    <#assign filteredQuery = filteredQuery.addParam("sort","" + sort) />
</#if>

<#assign executedQuery = searchService.search(query) />	
<#assign productsFound = executedQuery.response.numFound>	
<#assign results = executedQuery.response.documents />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Rosie Rivet - Crafter CMS Demo Site</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">

    <link href="/static-assets/css/main.css" rel="stylesheet">
</head>
<body>    
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
        <div class="span12" id="content">
         
       <!-- body content -->
        <div id='results'  style="width: 500px;">
			<div class="pull-left">
                <strong class="uppercase">Search:</strong>
                <form method='GET' action='/search'><input name="q" <#if keyword??>value='${keyword}'</#if> /> 
                   <input type='submit' value='Search'/>
				   <#if t1 == "" && t2 == "">
                     <table><tr><td><input  style="margin: 0px 5px 7px 0px;" type='checkbox' name='t1' value='pages' checked /></td><td><label>Pages</label></td></tr>
                     <tr><td><input style="margin: 0px 5px 7px 0px;" type='checkbox' name='t2' value='videos' checked /></td><td><label>Videos</label></td></tr></table>
				   <#else>
                     <table><tr><td><input  style="margin: 0px 5px 7px 0px;" type='checkbox' name='t1' value='pages' <#if t1 != "">checked</#if> /></td><td><label>Pages</label></td></tr>
                     <tr><td><input style="margin: 0px 5px 7px 0px;" type='checkbox' name='t2' value='videos' <#if t2 != "">checked</#if> /></td><td><label>Videos</label></td></tr></table>
 				   </#if>
                </form>
            </div>

            <div class="categories clearfix"> 
				<br/><br/><br/><br/><br/><br/>
              <#list results as result>
                <div>
					<#if result.body??>
                     <a href="">${result.body?substring(0, 500)?replace("</?[^>]+(>|$)", "", "r")}</a>
					</#if>  
					<#if result.videoId??>
					<@ice componentPath=result.localId />
					<iframe width="300" height="225" src="//www.youtube.com/embed/${result.videoId}" frameborder="0" allowfullscreen></iframe>
                     <a href=""></a>
					</#if>  
					
                <div><br/>
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
		<div style="top: 200px; position: absolute; left: 970px;">
			<#include "/templates/system/common/cstudio-support.ftl" />
			<#assign lastViewedProduct = Cookies['lastViewedProduct']!"" /> 
			<#assign queryStatement = "crafterSite:rosie " /> 
			<#assign queryStatement = queryStatement + "AND content-type:\"/component/promo\" "/> 
			
			 <#assign queryStatement = queryStatement + " AND ("/>
			 <#assign count = 0 />
			 <#list results as result>
			   <#if result['related.item.key']??>
			     <#if count != 0>
   				    <#assign queryStatement = queryStatement + " OR " />
				 </#if>
				 <#assign queryStatement = queryStatement + "related.item.key:\""+result['related.item.key']+"\" " />
				 <#assign count = count + 1 />
			   </#if>
			 </#list>
			 <#assign queryStatement = queryStatement + ")"/>

			<#assign query = searchService.createQuery()>
			<#assign query = query.setQuery(queryStatement) />
		 

			<#assign executedQuery = searchService.search(query) />	
			<#assign promos = executedQuery.response.documents />
		 
			<#if promos??>
			  <#list promos as promo>
			    <@ice componentPath=promo.localId />
				<@renderComponent componentPath=promo.localId /></br>
			  </#list>
			</#if>
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