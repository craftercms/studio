{
	"total": "${result.getTotal()?string}",
	"currentPage": "${result.getPage()?string}",
	"sort": "${result.getSort()?string}",
	"keyword": "${result.getKeyword()?string}",
	"numOfItems": "${result.getNumOfItems()?string}",
	"documents":
	[
	<#setting number_format="#">
	<#list result.getItems() as item>
		{
			<#assign keys = item.getProperties()?keys>
			<#list keys as key>
				"${key}" : <#if item.getProperties()[key]?exists>
								<#assign value = item.getProperties()[key]>
								<#if value?is_date == true>"${value?string("MMM dd yyyy HH:mm:ss")}"<#else>"${value?string?trim}"</#if> 
							<#else>""
							</#if>
				<#if key_has_next>,</#if>   
			</#list> 
		}
		<#if item_has_next>,</#if>
	</#list>
	]
}
