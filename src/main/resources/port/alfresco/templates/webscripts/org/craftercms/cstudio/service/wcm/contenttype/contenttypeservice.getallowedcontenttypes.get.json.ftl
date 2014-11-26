{
<#if items?exists>
"types":[
    <#list items as item>
    {
    "allowedRoles":<#if item.allowedRoles?exists>
    [
        <#list item.allowedRoles as role>
        "${role?js_string}"<#if role_has_next>,</#if>
        </#list>
    ]
    <#else>[]</#if>,
    "name":<#if item.name?exists>"${item.name?js_string}"<#else>""</#if>,
        <#if item.imageThumbnail?exists>
        "image":"${item.imageThumbnail?js_string}"
        </#if>,
    "label":<#if item.label?exists>"${item.label?js_string}"<#else>""</#if>,
    "formId":<#if item.form?exists>"${item.form?js_string}"<#else>""</#if>,
    "formPath":<#if item.formPath?exists>"${item.formPath?js_string}"<#else>""</#if>,
    "extension":<#if item.extension?exists>"${item.extension?js_string}"<#else>""</#if>,
    "modelInstancePath":<#if item.modelInstancePath?exists>"${item.modelInstancePath?js_string}"<#else>""</#if>,
    "type":<#if item.isWcm() == true>""<#else>"${item.name?js_string}"</#if>,
    "isWcm":<#if item.isWcm() == true>"true"<#else>"false"</#if>,
    "contentAsFolder":<#if item.isContentAsFolder() == true>"true"<#else>"false"</#if>
    }<#if item_has_next>,</#if>
    </#list>
]
</#if>
}
