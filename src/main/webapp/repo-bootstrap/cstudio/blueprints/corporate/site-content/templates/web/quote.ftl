<div class='quoteBody' style="float:${model.position};">
<div class='quoteBodyTop'>
</div>
<div class='quoteBodyMid'>

<div class='quoteBodyContent'>
<#if model.headshot?? && model.headshot == "">
	<img class='quoteImage' src="/static-assets/images/quote_thumbnail.png" >	
<#else>
	<img  class='quoteImage'  src='${model.headshot!"/static-assets/images/quote_thumbnail.png"}' >
</#if>

    ${model.body!""}<br>
    ${model.title!""}<br>
    ${model.company!""}
  </div>
</div>
<div class='quoteBodyBottom'>
</div>
</div>