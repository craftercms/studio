<#import "/templates/system/common/cstudio-support.ftl" as studio />
<article <@studio.componentAttr path=contentModel.storeUrl />>
  <span class="icon ${contentModel.icon}"></span>
  <div class="content">
    <h3>${contentModel.title}</h3>
    ${contentModel.body_html}
  </div>
</article>
