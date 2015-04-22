<#include "/templates/system/common/cstudio-support.ftl" />
<#include "/templates/web/navigation/navigation.ftl">

<!DOCTYPE html>
<html lang="en">
<head>
    <title>Global Integrity</title>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="/static-assets/css/bootstrap-responsive.css" rel="stylesheet">
    <link href="/static-assets/css/main.css" rel="stylesheet">

</head>
<body>
<div class="home container">

    <header>
        <div class="top">
            <div class="pad">
                <nav>
                    <ul class="main-nav clearfix">
                        <@renderNavigation "/site/website", 2 />
                    </ul>
                <nav>
                <#include "/templates/web/common/page-actions.ftl" />
            </div>
        </div>
        <div class="bottom">
            <div class="pad">
                <h1 class="off-text logo" href="/">Global Integrity</h1>
                <#include "/templates/web/common/search.ftl" />
            </div>
        </div>
    </header>
    <div class="content" data-studio-ice="heroImage" style="overflow: hidden; background: url(${model.heroImage!''}) no-repeat scroll center transparent;">

        <@ice id="heroImage" />
        <span id="test" class="arial"
              data-studio-ice="pageNote"
              style="position:absolute; left:28px; bottom:16px; color:#fff;">
            This is a Crafter CMS Demo Site
        </span>

        <#if model.enableTouts == "true">
        <div id="carousel" class="carousel slide">
            <@ice id="touts" />
            <div class="left-cap cap"></div>
            <div class="right-cap cap"></div>
            <div class="carousel-inner">

                <#assign segment = 'Anonymous' />
                <#if profile??>
                    <#assign segment = profile['segment']!'Anonymous' />
                </#if>

                <#assign queryStatement = "content-type:/component/tout AND (segments.item.key:\"" + segment + "\"^10 OR segments.item.key:All)" />

                <#assign query = searchService.createQuery()>
                <#assign query = query.setQuery(queryStatement)>
                <#assign query = query.setRows(10)>
                <#assign touts = searchService.search(query).response.documents>

                <#list touts as tout>
                    <#assign toutKey = tout.localId >
                    <#assign toutItem = siteItemService.getSiteItem(toutKey) />
                    <div class="elem mr3">
                        <@ice componentPath=toutKey />
                        <img src="${toutItem.image!''}" />
                        <div class="museo300">
                            <h1 class="text-green">${toutItem.headline!""}</h1>
                            <p>${toutItem.subhead!""}</p>
                        </div>
                    </div>
                </#list>

            </div>
            <a class="carousel-control left" href="#carousel" data-slide="prev">
                &lsaquo;
            </a>
            <a class="carousel-control right" href="#carousel" data-slide="next">
                &rsaquo;
            </a>
        </div>
        </#if>

    </div>

<#include "/templates/web/common/footer.ftl" />

</div>

<script src="/static-assets/js/jquery-1.10.2.min.js"></script>
<script src="/static-assets/js/bootstrap.min.js"></script>
<script src="/static-assets/js/main.js"></script>

<script src="/studio/static-assets/libs/requirejs/require.js"
        data-main="/studio/overlayhook?site=foo&page=/&foo.js"></script>

</body>
</html>