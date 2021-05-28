<#import "/templates/system/common/crafter.ftl" as crafter />

<@crafter.componentRootTag $tag="header" id="header">
  <a href="/" class="logo">
    <#--
    TODO/FYI For docs...
    While using the macro, for whatever reason, doing src=(contentModel.logo_s!"") works
    but doing src="${contentModel.logo_text_t!""}" doesn't. Inversely on loops, $index="${item?index}"
    works whilst  $index=(item?index) doesn't.
    -->
    <@crafter.img $field="logo_s,logo_text_t" src=(contentModel.logo_s!"") alt=(contentModel.logo_text_t!"") border=0 />
    <#if profile??>
      <#assign name = profile.attributes.name!"stranger" />
    <#else>
      <#assign name = "stranger" />
    </#if>
    Howdy, ${name}
  </a>
  <@crafter.renderRepeatCollection
    $field="social_media_links_o"
    $containerAttributes={'class':'icons'};
    <#-- Nested content values passed down by the macro: -->
    item, index
  >
    <@crafter.a
      href="${item.url_s}"
      class="icon ${item.social_media_s}"
      $field="social_media_links_o.url_s,social_media_links_o.social_media_s"
      $index=index
    />
  </@crafter.renderRepeatCollection>
</@crafter.componentRootTag>
