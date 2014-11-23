{
	"result":<#if result.success>"success"<#else>"failure"</#if>,
	"message":<#if result.message?exists>"${result.message}"<#else>""</#if>
}
