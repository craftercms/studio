{
	"messages":[
					<#if messages?exists>
						<#list messages as message>
							{
								"title":"${message.title}",
								"body":"${message.body}"
							}
							<#if message_has_next>,</#if>   
						</#list> 
					</#if>
					
				]
}
