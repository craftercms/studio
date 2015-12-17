<#if  RequestParameters['preview']??>
	<img src='http://img.youtube.com/vi/${model.youtubeId!"NO-ID"}/0.jpg' style="height:200px;width:200pxpx;" ></img>
<#else>
	<iframe width="200px" height="200px" src="http://www.youtube.com/embed/${model.youtubeId}" frameborder="0" allowfullscreen="true"></iframe>
</#if>