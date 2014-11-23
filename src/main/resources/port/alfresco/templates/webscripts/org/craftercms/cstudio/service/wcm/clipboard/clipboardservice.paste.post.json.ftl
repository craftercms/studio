{
	"paths": 
	[
		<#if items?exists>
			<#list items as item>"${item}"<#if item_has_next>,</#if></#list>
		</#if>
	]
}
