<#import "/templates/system/common/crafter.ftl" as crafter />

<@crafter.div>
  <#if contentModel.header_s?has_content>
    <@crafter.span $field="header_s">${contentModel.header_s}</@crafter.span>
  </#if>

  <@crafter.renderRepeatGroup
    $field="accounts_o"
    $containerTag="ul"
    $containerAttributes={ "class": "social-media-container ${contentModel.showItemsInline_b?then('inline', '')}" }
    $itemTag="li"
    $itemAttributes={ "class": "social-media-item" };
    account, index
  >
    <#assign defaultSocialNetworks = ["facebook", "twitter", "linkedin", "github", "instagram", "pinterest", "youtube", "snapchat", "vimeo"] />
    <#assign iconName = account.network_s?has_content?then(
      defaultSocialNetworks?seq_contains(account.network_s)?then(account.network_s + '.svg', 'exclamation-circle.png') ,
      'exclamation-circle.png'
    ) />
    <#assign icon="/static-assets/images/social-media-widget/${iconName}"/>
    <@crafter.a $field="accounts_o.url_s" $index=index href="${account.url_s}" target="_blank">
      <@crafter.img
        $field="accounts_o.icon_s"
        $index=index
        src="${(account.icon_s)?has_content?then(account.icon_s, icon)}"
        width="${contentModel.iconsWidth_i!''}"
        height="${contentModel.iconsHeight_i!''}"
      />
      <#if contentModel.title_s?has_content>
        <@crafter.span $field="title_s">${contentModel.title_s!''}</@crafter.span>
      </#if>
    </@crafter.a>
  </@crafter.renderRepeatGroup>
</@crafter.div>
