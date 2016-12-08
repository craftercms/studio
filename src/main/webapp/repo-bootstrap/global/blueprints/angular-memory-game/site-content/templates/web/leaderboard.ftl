<#include "/templates/system/common/cstudio-support.ftl" />
<!doctype html>
<html ng-app="memoryGameApp">
<head>
  <meta charset="utf-8">
  <title>Memory Game</title>
  <link rel="stylesheet" href="/static-assets/css/app.css">
</head>

<body>
   <h1>Leader Board</h1>

   <table border='1'>
       <tr>
         <th>Player Name</th>
         <th>Score</th>
       </tr>

     <#if leaders??>
       <#list leaders as leader>
         <tr>
           <td>${leader.name}</td>
           <td>${leader.score}</td>
         </tr>
       </#list>
      </#if>
   </table>   
   
   <#-- @cstudioOverlaySupport/-->
</body>
</html>
