<#import "/templates/system/common/cstudio-support.ftl" as studio />
<header id="header" <@studio.componentAttr path=contentModel.storeUrl ice=true iceGroup="header"/>>
    <a href="/" class="logo"><img border="0" alt="${contentModel.logo_text!""}" src="${contentModel.logo!""}">
        <#if profile??>
            <#assign name = profile.attributes.name!"stranger" />            
        <#else>
            <#assign name = "stranger" />
        </#if>
        Howdy, ${name}
    </a>

    <ul class="icons">
    <#list contentModel.social_media_links.item as item>
        <li><a href="${item.url}" class="icon ${item.social_media}"></a></li>
    </#list>
    </ul>
</header>
