{
	<#if result?exists>
		"name":<#if result.name?exists>"${result.name}"<#else>""</#if>,
		"namespace":<#if result.namespace?exists>"${result.namespace}"<#else>""</#if>,
		"label":<#if result.label?exists>"${result.label}"<#else>""</#if>,
		"form":<#if result.form?exists>"${result.form}"<#else>""</#if>,
		"formPath":<#if result.formPath?exists>"${result.formPath}"<#else>""</#if>,
		"extension":<#if result.extension?exists>"${result.extension}"<#else>""</#if>,
		"modelInstancePath":<#if result.modelInstancePath?exists>"${result.modelInstancePath}"<#else>""</#if>,
		"isWcm":<#if result.isWcm() == true>"true"<#else>"false"</#if>,
		"type":<#if result.type?exists>"${result.type}"<#else>"unknown"</#if>,
		"contentAsFolder":<#if result.isContentAsFolder() == true>"true"<#else>"false"</#if>,
		"previewable":<#if result.previewable?exists && result.previewable == true>"true"<#else>"false"</#if>
	</#if>
}
