<#import "/templates/system/common/cstudio-support.ftl" as studio />

<#if RequestParameters["c1v1"]??>
  <style>
    .da-slider .mask {
      background: #3A1AFE !important;
    }
  </style>
</#if>

<!-- Start home section -->
<div id="home" <@studio.componentAttr path=model.storeUrl ice=true iceGroup="" />>
    <!-- Start cSlider -->
    <div id="da-slider" class="da-slider">
        <div class="triangle"></div>
        <!-- mask elemet use for masking background image -->
        <div class="mask"></div>
        <!-- All slides centred in container element -->
        <div class="container"   >
            <!-- Start first slide -->

			<#if slideSet??>
              <#list slideSet.banners.item as banner>
              <div class="da-slide"  >
              
                  <h2 <@studio.iceAttr path=slideSet.storeUrl iceGroup="banners"/> class="fittext2">${banner.headline!''}</h2>
                  <h4 >${banner.subheadline!''} ${x}</h4>
                  <p>${banner.explaination!''}</p>
                  <a href="#" class="da-link button">Read more</a>
                  <div class="da-img">
                      <img src="${banner.image!'NOIMAGE'}" alt="image01" width="320">
                  </div>
              </div>
              </#list>
            </#if>

            <!-- Start cSlide navigation arrows -->
            <div class="da-arrows">
                <span class="da-arrows-prev"></span>
                <span class="da-arrows-next"></span>
            </div>
            <!-- End cSlide navigation arrows -->
        </div>
    </div>
</div>