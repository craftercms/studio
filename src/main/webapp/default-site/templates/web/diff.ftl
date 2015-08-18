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
            width: 800px;
            max-height: 100px;
            overflow: scroll;
        }
    </style>
    <div class='content'>${diff}</div>
    <div class='content'>${variantA}</div>
    <div class='content'>${variantB}</div>
    
</body>
</html>