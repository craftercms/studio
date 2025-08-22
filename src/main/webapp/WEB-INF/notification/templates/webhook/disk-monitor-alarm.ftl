{
	"blocks": [
		{
			"type": "section",
			"text": {
				"type": "mrkdwn",
				"text": "*High Disk Usage Alert*"
			}
		},
		{
			"type": "section",
			"text": {
				"type": "mrkdwn",
				"text": "The disk usage on the server has exceeded the defined threshold. Please review the details below"
			}
		},
		{
			"type": "section",
			"fields": [
				{
					"type": "mrkdwn",
					"text": "*Server Name:*"
				},
				{
					"type": "plain_text",
					"text": "${serverName?json_string}"
				},
				{
					"type": "mrkdwn",
					"text": "*Disk Usage:*"
				},
				{
					"type": "plain_text",
					"text": "${payload.diskInfo.diskUsage}%"
				},
				{
					"type": "mrkdwn",
					"text": "*High Usage Watermark:*"
				},
				{
					"type": "plain_text",
					"text": "${payload.highWaterMark}%"
				}
			]
		},
		{
			"type": "divider"
		},
		{
			"type": "section",
			"fields": [
				{
					"type": "mrkdwn",
					"text": "*Total Space:*"
				},
				{
					"type": "plain_text",
					"text": "${byteCountToDisplaySize.apply(payload.diskInfo.totalSpace)?json_string}"
				},
				{
					"type": "mrkdwn",
					"text": "*Used Space:*"
				},
				{
					"type": "plain_text",
					"text": "${byteCountToDisplaySize.apply(payload.diskInfo.usedSpace)?json_string}"
				},
				{
					"type": "mrkdwn",
					"text": "*Free Space:*"
				},
				{
					"type": "plain_text",
					"text": "${byteCountToDisplaySize.apply(payload.diskInfo.freeSpace)?json_string}"
				}
			]
		}
	]
	<#if payloadJson??>
	,
	"attachments": [
		{
			"blocks": [
				{
					"type": "section",
					"text": {
						"type": "mrkdwn",
						"text": "*Disk status:*"
					}
				},
				{
					"type": "section",
					"text": {
						"type": "plain_text",
						"text": "${payloadJson?json_string}"
					}
				}
			]
		}
	]
	</#if>
}
