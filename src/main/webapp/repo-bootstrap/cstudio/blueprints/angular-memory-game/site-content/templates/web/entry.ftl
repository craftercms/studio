<#import "/templates/system/common/cstudio-support.ftl" as studio />

<#assign age = 4 />
<#if profile??>
	<#assign age = profile.attributes.age />
</#if>

<!doctype html>
<html ng-app="memoryGameApp">
<head>
  <meta charset="utf-8">
  <title>Memory Game</title>
  <link rel="stylesheet" href="/static-assets/css/app.css">
  <script type="text/javascript"  src="/static-assets/lib/angular/angular.js"></script>
    <script type="text/javascript" src="/static-assets/js/app.js"></script>
  
    

</head>
<body ng-controller="GameCtrl">
 
  <div>Pairs left to match: {{game.unmatchedPairs}}</div>
  <div>Matching: {{game.firstPick.title}}</div>

  <table data-studio-ice="cards" data-studio-ice-path="{{game.id}}" >
    <tr ng-repeat="row in game.grid">
      <td ng-repeat="tile in row" ng-click="game.flipTile(tile)">

        <!-- this entire block can be replaced with our custom component mgCard:
          <mg-card tile="tile"></mg-card>
        -->
        <div class="container">
          <div class="card" ng-class="{flipped: tile.flipped}">
              <img class="front" ng-src="/static-assets/images/cards/back.png">
              <img class="back" ng-src="{{tile.title}}">
          </div>
        </div>

      </td>
    </tr>
  </table>

  <div class="message">{{game.message}}</div>
	
    
    <script>
    
      memoryGameApp.factory('game', function() {
         var game = new Game([]);
         game.age=${age!4};
         
         return game;
      });

    </script>

    <@studio.toolSupport/>
</body>
</html>
