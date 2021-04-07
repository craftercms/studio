<#import "/templates/system/common/ice.ftl" as studio />

<!--
	Editorial by HTML5 UP
	html5up.net | @ajlkn
	Free for personal and commercial use under the CCA 3.0 license (html5up.net/license)
-->
<!doctype html>
<html lang="en">
<head>
  <#include "/templates/web/fragments/head.ftl">
</head>
<body class="is-preload">

<!-- Wrapper -->
<div id="wrapper">

  <!-- Main -->
  <div id="main">
    <div class="inner">

      <!-- Header -->
      <@studio.renderComponentCollection $field="header_o"/>
      <!-- /Header -->

      <!-- Banner -->
      <section id="banner">
        <div class="content">
          <@studio.header $field="hero_title_html" $label="Hero Title">
            ${contentModel.hero_title_html}
          </@studio.header>
          <@studio.tag $field="hero_text_html">
            ${contentModel.hero_text_html}
          </@studio.tag>
        </div>
        <span class="image object">
          <@studio.img $field="hero_image_s" src=(contentModel.hero_image_s!"") alt=""/>
        </span>
      </section>
      <!-- /Banner -->

      <!-- Section: Features -->
      <section>
        <header class="major">
          <@studio.tag $tag="h2" $field="features_title_t">
            ${contentModel.features_title_t}
          </@studio.tag>
        </header>
        <@studio.renderComponentCollection $field="features_o" class="features" $itemAttrs={ "class": "feature-container" }/>
      </section>
      <!-- /Section: Features -->

      <!-- Section: Articles -->
      <section>
        <header class="major">
          <h2>Featured Articles</h2>
        </header>
        <div class="posts">
          <#list articles as article>
            <@studio.article $model=article>
              <a href="${article.url}" class="image">
                <#--
                Note for docs:
                Works: src=article.image???then(article.image, "/static-assets/images/placeholder.png")
                Error: src="${article.image???then(article.image, "/static-assets/images/placeholder.png")}" 🤷
                however...
                Works: href="${article.url}"
                -->
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
                <li>
                  <a href="${article.url}" class="button">More</a>
                </li>
              </ul>
            </@studio.article>
          </#list>
        </div>
      </section>
      <!-- /Section: Articles -->

    </div>
  </div>
  <!-- /Main -->

  <!-- Left Rail -->
  <@studio.renderComponentCollection $field="left_rail_o" />
  <!-- /Left Rail -->

</div>
<!-- /Wrapper -->

<#include "/templates/web/fragments/scripts.ftl">
<@studio.initPageBuilder/>

</body>
</html>
