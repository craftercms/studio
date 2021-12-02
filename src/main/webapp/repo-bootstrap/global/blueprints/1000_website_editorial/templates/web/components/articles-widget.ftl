<#import "/templates/system/common/crafter.ftl" as crafter />

<#if articles?? && articles?size &gt; 0>
  <@crafter.section>
    <header class="major">
      <@crafter.h2 $field="title_t">${contentModel.title_t}</@crafter.h2>
    </header>
    <div class="mini-posts">
      <#list articles as article>
        <@crafter.article $model=article>
          <a href="${article.url}" class="image">
            <img src="${article.image!"/static-assets/images/placeholder.png"}" alt="" />
          </a>
          <h4>
            <@crafter.a href="${article.url}" $model=article $field="title_t">
              ${article.title}
            </@crafter.a>
          </h4>
        </@crafter.article>
      </#list>
    </div>
  </@crafter.section>
</#if>
