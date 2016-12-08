'use strict';
/* App Controllers */
var memoryGameApp = angular.module('memoryGameApp', []);
memoryGameApp.factory('game', function() {
  var game = new Game([]);
  game.age=30;
  return game;
});

var mHttp;
var mMoves=0;
var mCards;

memoryGameApp.controller('GameCtrl', function GameCtrl($scope, $http, game) {
  $scope.game = game;
  mHttp = $http;

  $http({ 
    url: "/api/1/services/game/cards/get-cards.json?age="+game.age,
    method: "GET",
	game: this,
	scope: $scope
    }
	
	).success(function(data, status, headers, config) {
		var game = config.scope.game;
        var cards = data.imageLocations;
        
        game.id = data.gameId;
	    var tileDeck = game.makeDeck(cards);

	    game.grid = game.makeGrid(tileDeck);
	    game.message = Game.MESSAGE_CLICK;
	    game.unmatchedPairs = cards.length;
            mCards = cards.length;

	}).error(function(data, status, headers, config) {
		//TODO Handle error
	});	
});

//usages:
//- in the repeater as: <mg-card tile="tile"></mg-card>
//- card currently being matched as: <mg-card tile="game.firstPick"></mg-card>
memoryGameApp.directive('mgCard', function() {
});

function Tile(title) {
  this.title = title;
  this.flipped = false;
}

Tile.prototype.flip = function() {
  this.flipped = !this.flipped;
}

function Game() {
      this.flipTile = function(tile) {

      if (tile.flipped) {
        return;
      }

      tile.flip();

      if (!this.firstPick || this.secondPick) {
                mMoves++;

        if (this.secondPick) {
          this.firstPick.flip();
          this.secondPick.flip();
          this.firstPick = this.secondPick = undefined;
        }

        this.firstPick = tile;
        this.message = this.MESSAGE_ONE_MORE;

      } else {

        if (this.firstPick.title === tile.title) {
          this.unmatchedPairs--;
          this.message = (this.unmatchedPairs > 0) ? this.MESSAGE_MATCH : this.MESSAGE_WON;
          this.firstPick = this.secondPick = undefined;

// WRITE SCORE WHEN GAME IS OVER
          if(this.unmatchedPairs == 0) {
             mHttp({
                 url: "/api/1/services/game/leader/create.json",
                 method: "POST",
                 data: { name: "howard", moves: mMoves, cardCount: mCards }
                 });  
  
          }

        } else {
          this.secondPick = tile;
          this.message = this.MESSAGE_MISS;
        }
      }
    };

	/* Create an array with two of each tileName in it */
	this.makeDeck = function makeDeck(tileNames) {
	  var tileDeck = [];
	  tileNames.forEach(function(name) {
	    tileDeck.push(new Tile(name));
	    tileDeck.push(new Tile(name));
	  });

	  return tileDeck;
	};

	this.makeGrid = function makeGrid(tileDeck) {
	  var gridDimension = Math.sqrt(tileDeck.length),
	      grid = [];

	  for (var row = 0; row < gridDimension; row++) {
	    grid[row] = [];
	    for (var col = 0; col < gridDimension; col++) {
	        grid[row][col] = this.removeRandomTile(tileDeck);
	    }
	  }

	  return grid;
	};

	this.removeRandomTile = function removeRandomTile(tileDeck) {
	  var i = Math.floor(Math.random()*tileDeck.length);
	  return tileDeck.splice(i, 1)[0];
	};

	this.MESSAGE_CLICK = 'Click on a tile.';
	this.MESSAGE_ONE_MORE = 'Pick one more card.'
	this.MESSAGE_MISS = 'Try again.';
	this.MESSAGE_MATCH = 'Good job! Keep going.';
	this.MESSAGE_WON = 'You win!';
};
