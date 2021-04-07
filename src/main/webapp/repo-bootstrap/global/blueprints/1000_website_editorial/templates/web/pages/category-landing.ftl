<#import "/templates/system/common/ice.ftl" as studio />

<!DOCTYPE HTML>
<!--
	Editorial by HTML5 UP
	html5up.net | @ajlkn
	Free for personal and commercial use under the CCA 3.0 license (html5up.net/license)
-->
<html lang="en">
<head>
    <#include "/templates/web/fragments/head.ftl">
</head>
<body>
<!-- Wrapper -->
<div id="wrapper">

  <!-- Main -->
  <div id="main">
    <div class="inner">

      <!-- Header -->
      <@renderComponent component=contentModel.header_o.item />

      <!-- Section -->
      <@studio.tag $tag="section" $model=contentModel>
        <header class="main">
          <h1>${contentModel.articles_title_t}</h1>
        </header>
        <div class="posts">
          <#list articles as article>
            <@studio.article $model=article>
              <a href="${article.url}" class="image">
                <@studio.img
                  $model=article
                  $field="image_s"
                  src=article.image???then(article.image, "/static-assets/images/placeholder.png")
                  alt=""
                />
              </a>
              <h3>
                <@studio.a $model=article $field="subject_t" href="${article.url}">
                  ${article.title}
                </@studio.a>
              </h3>
              <@studio.p $model=article $field="summary_t">
                ${article.summary}
              </@studio.p>
              <ul class="actions">
                <li><a href="${article.url}" class="button">More</a></li>
              </ul>
            </@studio.article>
          </#list>
        </div>
      </@studio.tag>

    </div>
  </div>

  <!-- Left Rail -->
  <@renderComponent component=contentModel.left_rail_o.item />

</div>

<#include "/templates/web/fragments/scripts.ftl">

<@studio.initPageBuilder/>
</body>
