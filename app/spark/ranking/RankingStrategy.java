package spark.ranking;


public class RankingStrategy {

    public IRankingStrategy getRankingStrategy(String rankingStrategy){
        switch(rankingStrategy){
            case "cosineSimilarity":
                return new CosineSimilarity();
        }
        return null;
    }

}
