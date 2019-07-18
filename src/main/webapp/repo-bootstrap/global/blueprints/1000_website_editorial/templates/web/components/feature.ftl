<#import "/templates/system/common/cstudio-support.ftl" as studio />
<article <@studio.componentAttr path=contentModel.storeUrl />>
  <span class="icon ${contentModel.icon_s}"></span>
  <div class="content">
    <h3>${contentModel.title_t}</h3>
    ${contentModel.body_html}
  </div>
</article>
