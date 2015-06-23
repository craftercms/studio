package scripts.libs

@Grab('com.gmongo:gmongo:1.2')
import com.gmongo.GMongo

class MyDAO {

    def mongo = null;
    def db = null

    /**
     * initialize our object
     */
    def init() {
        mongo = new GMongo()
        db = mongo.getDB('memory')
    }

    /**
     * create a leaderboard record
     * @param name name of the player
     * @param moved moves made during the game
     * @param count number of cards in the game
     */
    def createLeader(name, moves, cardCount) {
        def pairs = cardCount / 2;
        def score = new Double((pairs / moves) * 100);
        db.scores.insert([name: name, moves: moves, cardCount: cardCount, score: score])
    }

    def getLeaders() {
       def leaders = [];

       def leaderResults = db.scores.find().sort([ score: -1 ]);

       def index = 0;
       leaderResults.each { record ->
          def leader = [:];
          leader.name = record.name;
          leader.moves = record.moves;
          leader.cardCount = record.cardCount;
          leader.score = record.score;
          leaders[index++] = leader;          
       }

       return leaders;
    }
}
