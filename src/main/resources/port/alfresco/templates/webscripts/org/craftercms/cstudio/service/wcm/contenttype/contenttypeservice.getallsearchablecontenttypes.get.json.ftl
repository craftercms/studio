{
	<#if items?exists>
		"types":[
			<#list items as item>
			{
				"allowedRoles":<#if item.allowedRoles?exists>
									[
										<#list item.allowedRoles as role>
											"${role}"<#if role_has_next>,</#if>
										</#list>
									]
								<#else>[]</#if>,
				"name":<#if item.name?exists>"${item.name}"<#else>""</#if>,
				"label":<#if item.label?exists>"${item.label}"<#else>""</#if>,
				"formId":<#if item.form?exists>"${item.form}"<#else>""</#if>,
				"formPath":<#if item.formPath?exists>"${item.formPath}"<#else>""</#if>,
				"extension":<#if item.extension?exists>"${item.extension}"<#else>""</#if>,
				"modelInstancePath":<#if item.modelInstancePath?exists>"${item.modelInstancePath}"<#else>""</#if>,
				"type":<#if item.isWcm() == true>""<#else>"${item.name}"</#if>,
				"isWcm":<#if item.isWcm() == true>"true"<#else>"false"</#if>,
				"contentAsFolder":<#if item.isContentAsFolder() == true>"true"<#else>"false"</#if>
			}<#if item_has_next>,</#if>
			</#list>
		]
	</#if>
}
