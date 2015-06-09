<#import "/templates/system/common/cstudio-support.ftl" as studio />
<#import "/templates/system/common/craftercms-geo-lib.ftl" as crafter />
<@crafter.browserGeoLocationSupport />

<#if crafter.browserCoords?? && crafter.browserCoords != "">
	<#assign queryStatement = 'crafterSite:"pluton" ' />
	<#assign queryStatement = queryStatement + 'AND content-type:"/component/store-location" ' />

	<#assign query = searchService.createQuery()>
	<#assign query = query.setQuery(queryStatement) />


	<#assign query = query.addParam("sfield","geo_p") />
	<#assign query = query.addParam("pt","${crafter.browserCoords}") />
	<#assign query = query.addParam("sort","geodist() asc") />
   
	<#assign query = query.setRows(1)>

	<#assign executedQuery = searchService.search(query) />	


	<div <@studio.componentAttr path=model.storeUrl ice=true iceGroup="content" />>
	<#if executedQuery??>
	  <#assign locations = executedQuery.response.documents />
  
	  <h5>Stores Closest to You!</h5>
	  <#list locations as location>
	  <div>
	    <img width='200px' height='200px' style="border:1px solid white;"  src="${location.storeFrontImage}" /><br/>
	    <span>${location.storeName}</span><br/>
	    <span>${location.address}</span>
	  </div>
	  </#list>
	</#if>
<#else>
  <form>
  	  <h5>[[${crafter.browserCoords}]]</h5>
    Enter Zip Code to locate the store nearest you<br/><br/>
    <input > <br/><br/>
    <a href="#" style="margin-left:20px; color: white; border:1px solid #FECE1A;" class="da-link button">Locate Office</a>
    </form>
</#if>
</div>

