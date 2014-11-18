<html>
<head>
    <script>document.domain = '${cookieDomain}'</script>
</head>
<body>
    {
    <#if result.success == true>
        "success":"true"
            <#assign asset = result.item>
            <#if asset?exists>
            ,"nodeRef":<#if asset.nodeRef?exists>"${asset.nodeRef}"<#else>""</#if>,
            "fileName":<#if asset.fileName?exists>"${asset.fileName}"<#else>""</#if>,
            "fileExtension":<#if asset.fileExtension?exists>"${asset.fileExtension}"<#else>""</#if>,
            "size":<#if asset.size?exists>"${asset.size?string("0.##")}${asset.sizeUnit}"<#else>""</#if>,
            "width":<#if asset.width?exists>"${asset.width?string}"<#else>"-1"</#if>,
            "height":<#if asset.height?exists>"${asset.height?string}"<#else>"-1"</#if>
        </#if>
        <#else>
            "success":"false",
            "status":"${result.status?string}",
            "message":"${result.message}"
    </#if>
    }
</body>
</html>