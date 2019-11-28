<#import "/templates/system/common/cstudio-support.ftl" as studio />
<header id="header" <@studio.componentAttr component=contentModel ice=true iceGroup="header"/>>
    <a href="/" class="logo"><img border="0" alt="${contentModel.logo_text_t!""}" src="${contentModel.logo_s!""}">
        <#if profile??>
            <#assign name = profile.attributes.name!"stranger" />            
        <#else>
            <#assign name = "stranger" />
        </#if>
        Howdy, ${name}
    </a>

    <ul class="icons">
    <#list contentModel.social_media_links_o.item as item>
        <li><a href="${item.url_s}" class="icon ${item.social_media_s}"></a></li>
    </#list>
    </ul>
</header>
