<#import "/templates/system/common/ice.ftl" as studio />
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>${model.title_t}</title>
  <style>
    html, body {
      color: #333;
      height: 100%;
      background: #f3f3f3;
      font-family: sans-serif;
    }
    main {
      max-width: 800px;
      padding: 40px;
      background: rgba(255,255,255,0.6);
      border-radius: 20px;
      margin: 100px auto;"
    }
  </style>
</head>
<body>
<main>
    <@studio.h1 $field="title_t">${model.title_t}</@studio.h1>
    <@studio.tag $field="body_html">${model.body_html}</@studio.tag>
</main>
<@studio.initPageBuilder/>
</body>
</html>
