<#import "/templates/system/common/ice.ftl" as studio />

<#if articles?? && articles?size &gt; 0>
  <@studio.componentRootTag $tag="section">
    <header class="major">
      <@studio.h2 $field="title_t">${contentModel.title_t}</@studio.h2>
    </header>
    <div class="mini-posts">
      <#list articles as article>
        <@studio.article $model=article>
          <a href="${article.url}" class="image">
            <img src="${article.image!"/static-assets/images/placeholder.png"}" alt="" />
          </a>
          <h4>
            <@studio.a href="${article.url}" $model=article $field="title_t">
              ${article.title}
            </@studio.a>
          </h4>
        </@studio.article>
      </#list>
    </div>
  </@studio.componentRootTag>
</#if>
