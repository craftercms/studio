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
					"text": "${serverName}"
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
					"text": "*Total Space (Bytes):*"
				},
				{
					"type": "plain_text",
					"text": "${payload.diskInfo.totalSpace}"
				},
				{
					"type": "mrkdwn",
					"text": "*Used Space (Bytes):*"
				},
				{
					"type": "plain_text",
					"text": "${payload.diskInfo.usedSpace}"
				},
				{
					"type": "mrkdwn",
					"text": "*Free Space (Bytes):*"
				},
				{
					"type": "plain_text",
					"text": "${payload.diskInfo.freeSpace}"
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
