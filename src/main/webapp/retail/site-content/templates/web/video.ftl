<div>
<#if  RequestParameters['preview']??>

	<img src="http://img.youtube.com/vi/${model.videoId}/0.jpg" style="height:200px;width:200pxpx; float:left; padding: 10px;" ></img>
<#else>
	<iframe style="float:left; padding: 10px;" width="200px" height="200px" src="http://www.youtube.com/embed/${model.videoId}" frameborder="0" allowfullscreen="true"></iframe>
</#if>
</div>