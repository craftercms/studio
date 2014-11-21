{
	"resultCount": "${result.getTotal()?string}",
	"pageTotal": "${result.getNumOfItems()?string}",
	"searchFailed": "${result.isSearchFailed()?string}",
	"failCause": <#if result.getFailCause()?exists>"${result.getFailCause()?string}"<#else>""</#if>,
	"objectList":
	[
	<#setting number_format="#">
	<#list result.getItems() as item>
		{
			"type" : "${item.getContentType()?string}",
			"nodeRef" : "${item.getNodeRef()?string}",
			"object" : 
			{
				<#assign keys = item.getProperties()?keys>
				<#list keys as key>
					"${key}" : <#if item.getProperties()[key]?exists>
									<#assign value = item.getProperties()[key]>
									<#assign jvalue = item.jsonEscape(value)>
									<#if value?is_date == true>"${value?string("MMM dd yyyy HH:mm:ss")}"<#else>"${jvalue?string?trim}"</#if> 
								<#else>""
								</#if>
					<#if key_has_next>,</#if>   
				</#list>
			} 
		}
		<#if item_has_next>,</#if>
	</#list>
	]
}
