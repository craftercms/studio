<!DOCTYPE html>
<html>
<head>
    <script>
        CStudioAuthoring = {
            cookieDomain: "${cookieDomain}"
        }
    </script>
    <link rel="stylesheet" type="text/css" href="/studio/static-assets/css/diff.css" />
</head>
<body>
    <style>
        .content {
            border: 1px solid black;
            display: block;
            padding: 10px;
            margin: 10px;
            //width: 800px;
            //max-height: 100px;
            overflow: scroll;
        }
    </style>
    <div class='content'>

        <table>
            <tr>
                <th width="500px">Test Name</th>
                <th width="100px">Duration</th>
                <th width="100px">Status</th>
                <th width="500px">Message</th>
            </tr>
            <#list tests as test>
                <#assign bgcolor = "red" />
                <#if test.status == true>
                    <#assign bgcolor = "green" />
                </#if>

                <tr>
                    <td width="500px">${test.name}</td>
                    <td width="100px">${test.duration}</td>
                    <td width="100px" style="background-color:${bgcolor};">${test.status?string('Pass', 'Fail')}</td>
                    <td width="500px">${test.error!""}</td>
                </tr>
            </#list>
        </table>

    </div>
</body>
</html>