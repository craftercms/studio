<#include "/templates/web/navigation/navigation.ftl">
<#import "/templates/system/common/craftercms-geo-lib.ftl" as crafter />

<#assign ctao = RequestParameters["c1v1"]!"none">
<#if ctao != "none">
  <#assign ctao = "background-color:#0088CC; background-image:none;">
<#else>
  <#assign ctao = "">
</#if>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>Rosie Rivet - Crafter Rivet Demo Site</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="Rivet Logic Corporation">

    <link href="/static-assets/css/main.css" rel="stylesheet">

    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

</head>
<body>
<div id="main-container">

    <!-- #include "/templates/web/fragments/iphone_header.ftl"/ -->

    <div class="container-fluid" id="content-body">

        <div class="row-fluid">

            <div class="span9" id="content">


                <div style="position: relative; height: 355px; margin-top: 10px;">


<#assign query = searchService.createQuery()>
<#assign season = "FALL" />
<#--crafter.calculateSeason()/> -->


<#assign queryStatement = "crafterSite:"+siteName + " " />
<#assign queryStatement = queryStatement + "AND content-type:/component/home-page-scenario " />
<#assign queryStatement = queryStatement + "AND (season.item.key:\""+season+"\")^10 " />


<#assign query = query.setQuery(queryStatement) />

<#assign query = query.setRows(1)>
<#assign results = searchService.search(query) />

<#if results?? && results.response.documents[0]??>
    <#assign scenario = searchService.search(query).response.documents[0]>
</#if>









                <#if scenario?? >
                    <#assign scenarioId = scenario.localId />
                <#else>
                    <#assign scenarioId = "/site/components/home-page-scenarios/default.xml" />
                </#if>

                <#assign scenarioItem = siteItemService.getSiteItem(scenarioId) />
                    <div id="mainTile"
                         style="width: ${scenarioItem.mobileMainTileWidth}px; height: ${scenarioItem.mobileMainTileHeight}px; background: url('${scenarioItem.mobileMainTileImage}') repeat scroll 0% 0% transparent; position: absolute; top: 0px; left: 5px; ">
                    
                        <div class="box-overlay">
                            <div class="box-caption">${scenarioItem.mobileMainTileText}</div>
                        <#if scenarioItem.mainTileCta1ButtonText??>
                            <div class="box-cts">
                                <button id="btn-shop-women" class="btn btn-danger uppercase"
                                        style="padding: 14px 15px; top: ${scenarioItem.mobileMainTileCta1ButtonTop}px; left: ${scenarioItem.mobileMainTileCta1ButtonLeft}%; ${ctao}">
                                ${scenarioItem.mainTileCta1ButtonText}
                                </button>
                            </div>
                        </#if>

                        <#if scenarioItem.mobileMainTileCta2ButtonText?? && scenarioItem.mobileMainTileCta2ButtonText !="">
                            <div class="box-cts">
                                <button id="btn-shop-men" class="btn btn-danger uppercase"
                                        style="padding: 14px 15px; position: relative; top: ${scenarioItem.mobileMainTileCta2ButtonTop}px; left: ${scenarioItem.mobileMainTileCta2ButtonLeft}%; ${ctao}">
                                ${scenarioItem.mainTileCta2ButtonText}
                                </button>
                            </div>
                        </#if>
                        </div>
                    </div>

                </div>

                <hr>
            <#include "/templates/web/fragments/iphone_footer.ftl"/>
            </div>

        </div>

        <script src="/static-assets/js/jquery.min.js"></script>
        <script src="/static-assets/js/bootstrap.min.js"></script>
        <script src="/static-assets/js/main.js"></script>
        <script>
            $(".tile").mouseover(function () {
                $(this).find(".gradient-overlay")[0].style.display = "block";
            });

            $(".tile").mouseout(function () {
                $(this).find(".gradient-overlay")[0].style.display = "none";
            });

            $(document).ready(
                    function () {
                        var width = this.body.offsetWidth;
                        if (width <= 340) {
                            document.getElementById("mainTile").style.width = "330px";
                            document.getElementById("mainTile").style.height = "334px";

                            var tiles = $('.tile');
                            for (var i = 0; i < tiles.length; i++) {
                                tiles[i].style.display = "none";
                            }
                        }
                        else {
                            document.getElementById("mainTile").style.width = "465px";
                            document.getElementById("mainTile").style.height = "300px";
                        }
                    });
        </script>




</body>
</html>

