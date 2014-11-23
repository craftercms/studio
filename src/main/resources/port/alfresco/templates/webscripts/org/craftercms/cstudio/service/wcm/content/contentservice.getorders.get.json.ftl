{
	"orderName":"${orderName}",
	"order": [
			<#if order?exists>
			<#list order as item>
				{
				"id":"${item.getId()}", "order":"${item.getOrder()?c}","internalName":"${item.getName()}",
				
				<#if item.getPlaceInNav() != "false">
				  "disabled":"${item.getDisabled()}",
				</#if>
				}
				<#if item_has_next>,</#if>
			</#list>
			</#if>
			]
}
