<#import "/templates/system/common/crafter.ftl" as crafter />

<!-- Feature Component -->
<@crafter.componentRootTag $tag="article" class="feature">
  <@crafter.span class="icon ${contentModel.icon_s}" $field="icon_s"/>
  <div class="content">
    <@crafter.h3 $field="title_t">
      ${contentModel.title_t}
    </@crafter.h3>
    <@crafter.span $field="body_html">
      ${contentModel.body_html}
    </@crafter.span>
  </div>
</@crafter.componentRootTag>
<!-- /Feature Component -->
