<#include "/templates/system/common/cstudio-support.ftl" />
<#assign lastViewedProduct = Cookies['lastViewedProduct']!"" /> 
<#assign queryStatement = "crafterSite:\"${siteName}\" " /> 
<#assign queryStatement = queryStatement + "AND content-type:/component/promo "/> 
<#assign queryStatement = queryStatement + "AND related.item.key:\"${lastViewedProduct}\"" />

<#assign query = searchService.createQuery()>
<#assign query = query.setQuery(queryStatement) />
<#assign query = query.setRows(1)>

<#assign executedQuery = searchService.search(query) />	
<#assign promos = executedQuery.response.documents />

<#if promos??>
  <#list promos as promo>
    <@ice componentPath=promo.localId />
	<@renderComponent componentPath=promo.localId />
  </#list>
</#if>

