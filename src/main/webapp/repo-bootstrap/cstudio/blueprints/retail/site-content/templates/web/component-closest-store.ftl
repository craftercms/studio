<#import "/templates/system/common/craftercms-geo-lib.ftl" as crafter />
<@crafter.browserGeoLocationSupport />

<#if crafter.browserCoords?? && crafter.browserCoords != "">
	<#assign queryStatement = 'crafterSite:"rosie" ' />
	<#assign queryStatement = queryStatement + 'AND content-type:"/component/store-location" ' />

	<#assign query = searchService.createQuery()>
	<#assign query = query.setQuery(queryStatement) />


	<#assign query = query.addParam("sfield","geo_p") />
	<#assign query = query.addParam("pt","${crafter.browserCoords}") />
	<#assign query = query.addParam("sort","geodist() asc") />
   
	<#assign query = query.setRows(1)>

	<#assign executedQuery = searchService.search(query) />	

	<#if executedQuery??>
	  <#assign locations = executedQuery.response.documents />
  
	  <div>
	  <h5>Stores Closest to You!</h5>
	  <#list locations as location>
	  <div>
	    <img width='200px' height='200px' src="${location.storeFrontImage}" /><br/>
	    <span>${location.storeName}</span><br/>
	    <span>${location.address}</span>
	  </div>
	  </#list>
	  </div>
	</#if>
<#else>
  <form>
    Enter Zip Code to locate the store nearest you<br/><br/>
    <input > <br/><br/>
    <button id="abc" class="btn btn-danger uppercase">Find Store</button>
    </form>
</#if>