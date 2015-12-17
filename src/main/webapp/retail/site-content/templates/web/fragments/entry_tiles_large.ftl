<#import "/templates/system/common/craftercms-geo-lib.ftl" as crafter />

<#assign ctao = RequestParameters["c1v1"]!"none">
<#if ctao != "none">
  <#assign ctao = "background-color:#0088CC; background-image:none;">
<#else>
  <#assign ctao = "">
</#if>

<div style="position: relative; height: auto;">

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

<#if scenario??>
    <#assign scenarioId = scenario.localId />
<#else>
    <#assign scenarioId = "/site/components/home-page-scenarios/default.xml" />
</#if>

<#assign scenarioItem = siteItemService.getSiteItem(scenarioId) />
    <div>



	<div <@studio.iceAttr iceGroup="body" componentPath=scenarioId/> style="width: ${scenarioItem.mainTileWidth}px; height: ${scenarioItem.mainTileHeight}px; background: url('${scenarioItem.mainTileImage}') scroll 0% 0% transparent; position: absolute; top: 0px; left: 0px;">
 
        <div  class="box-overlay">
            <div class="box-caption">${scenarioItem.mainTileText}</div>
        <#if scenarioItem.mainTileCta1ButtonText??>
            <div class="box-cts">
                <button id="btn-shop-women" class="btn btn-danger uppercase"
                        style="position: relative; top: ${scenarioItem.mainTileCta1ButtonTop}px; left: ${scenarioItem.mainTileCta1ButtonLeft}px; ${ctao}">
                ${scenarioItem.mainTileCta1ButtonText}
                </button>
            </div>
        </#if>

        <#if scenarioItem.mainTileCta2ButtonText?? && scenarioItem.mainTileCta2ButtonText !="">
            <div class="box-cts">
                <button id="btn-shop-men" class="btn btn-danger uppercase"
                        style="position: relative; top: ${scenarioItem.mainTileCta2ButtonTop}px; left: ${scenarioItem.mainTileCta2ButtonLeft}px; ${ctao}">
                ${scenarioItem.mainTileCta2ButtonText}
                </button>
            </div>
        </#if>
        </div>
    </div>

<#if device?? && device == "mobile">
    <#if scenarioItem.mobileTiles??>
        <#assign tiles = scenarioItem.mobileTiles.item>
    </#if>
<#else>
    <#if scenarioItem.tiles??>
        <#assign tiles = scenarioItem.tiles.item>
    </#if>
</#if>

<#if tiles??>
    <#list tiles as tile>
        <div <@studio.iceAttr iceGroup="body" componentPath=scenarioId/> class="tile"
             style="width: ${tile.width}px; height: ${tile.height}px; background: url('${tile.image}') scroll 0% 0% transparent; position: absolute; top: ${tile.top}px; left: ${tile.left}px;">
            <a class="overlay-link" href="#"></a>

            <div class="gradient-overlay" style="display: none;">
                <div>${tile.mouseOverText}</div>
            </div>
        </div>
    </#list>
</#if>

</div>
</div>
</div>

