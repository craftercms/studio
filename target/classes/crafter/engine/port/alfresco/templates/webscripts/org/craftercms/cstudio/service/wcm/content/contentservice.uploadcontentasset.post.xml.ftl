<result>
	<#if result.success == true>
		<#assign asset = result.item>
		<success>true</success>
		<#if asset?exists>
			<node-ref><#if asset.nodeRef?exists>${asset.nodeRef}</#if></node-ref>
			<file-name><#if asset.fileName?exists>${asset.fileName}</#if></file-name>
			<file-extension><#if asset.fileExtension?exists>${asset.fileExtension}</#if></file-extension>
			<size><#if asset.size?exists>${asset.size?string("0.##")}${asset.sizeUnit}</#if></size>
			<width><#if asset.width?exists>${asset.width?string}<#else>-1</#if></width>
			<height><#if asset.height?exists>${asset.height?string}<#else>-1</#if></height>
		</#if>
	<#else>
		<success>true</success>
		<status>${result.status?string}</success>
		<message>${result.message}</success>
	</#if>
</result>
