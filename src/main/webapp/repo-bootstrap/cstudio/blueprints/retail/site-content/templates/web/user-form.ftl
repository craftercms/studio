<form method='GET' Action='/proxy/authoring/proxy/alfresco/crafterdemo/create-content'>
<input type='hidden' name='name' value='test' />
<input type='hidden' name='path' value='/site/user-submitted-recipes' />
<input type='hidden' name='site' value='rosie' />
<input type='hidden' name='successUrl' value='http://127.0.0.1:8080/feeds' />

<#list model.fields.item as field>
	<#if field.type != "textarea">
    <span>${field.title}</span><#if field.required_b == 'true'> * <#else>&nbsp;&nbsp;&nbsp;&nbsp;</#if><input name='${field.sysFieldName}' type='field.type'/><br/><br/>
    <#else>
    <span>${field.title}</span><#if field.required_b == 'true'> * <#else>&nbsp;&nbsp;&nbsp;&nbsp;</#if><textarea  name='${field.sysFieldName}' type='field.type'></textarea><br/><br/>
    </#if>
</#list>
<input type='submit' value='${model.submitLabel}' />
</form>