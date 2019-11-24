package functions;

import api.PostsResource;
import api.SubredditsResource;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import resources.Post;
import resources.Subreddit;
import resources.Vote;
import utils.AppConfig;
import utils.Database;
import utils.Date;
import utils.Votes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateScoresOnDB {

    @FunctionName("update_scores_on_db")
    public void updateScoresOnDB(@TimerTrigger(name = "keepAliveTrigger",
            schedule = "0 0 */" + AppConfig.SCORE_UPDATE_PERIOD_ON_DB + " * * *") String timerInfo,
                                 ExecutionContext context) {

        String query = "SELECT * FROM " + Votes.VOTE_COL + " v" +
                " WHERE v.timestamp >= " + Date.timestampMinusHours(AppConfig.SCORE_UPDATE_PERIOD_ON_DB);

        List<Document> timeWindowVotes = Database.getResourceListDocs(Votes.VOTE_COL, query);

        Map<String, Long> accScore = new HashMap<>(timeWindowVotes.size());

        for(Document voteDoc : timeWindowVotes) {
            Vote vote = Vote.fromDocument(voteDoc);

            Long score = accScore.get(vote.getPostId());

            if (score == null)
                accScore.put(vote.getPostId(), vote.isUp() ? AppConfig.UPVOTE_SCORE_VALUE : AppConfig.DOWNVOTE_SCORE_VALUE);
            else {
                score += vote.isUp() ? AppConfig.UPVOTE_SCORE_VALUE : AppConfig.DOWNVOTE_SCORE_VALUE;
                accScore.put(vote.getPostId(), score);
            }
        }

        query = "SELECT c.rootPost FROM " + PostsResource.POST_COL + " c" +
                " WHERE c.timestamp >= " + Date.timestampMinusHours(AppConfig.SCORE_UPDATE_PERIOD_ON_DB) +
                " AND c.parentPost != ''";

        List<Document> comments = Database.getResourceListDocs(PostsResource.POST_COL, query);

        comments.forEach(commentDoc -> {
            String rootPostId = commentDoc.getString("rootPost");
            Long score = accScore.get(rootPostId);

            if (score == null)
                accScore.put(rootPostId, AppConfig.COMMENT_SCORE_VALUE);
            else {
                score += AppConfig.COMMENT_SCORE_VALUE;
                accScore.put(rootPostId, score);
            }
        });

        Map<String, Long> subredditAccScore = new HashMap<>();

        accScore.forEach((postId, newScore) -> {
            Post post = Post.fromDocument(Database.getResourceDocById(PostsResource.POST_COL, postId));
            post.setScore(post.getScore() + newScore);
            Database.putResourceOverwrite(post.toDocument(), PostsResource.POST_COL);

            String subredditId = post.getSubreddit();
            Long score = subredditAccScore.get(subredditId);
            if(score == null)
                subredditAccScore.put(subredditId, newScore);
            else {
                score += newScore;
                subredditAccScore.put(subredditId, score);
            }
        });

        subredditAccScore.forEach((subredditId, newScore) -> {
            Subreddit subreddit = Subreddit.fromDocument(Database.getResourceDocById(SubredditsResource.SUBREDDIT_COL, subredditId));
            subreddit.setScore(subreddit.getScore() + newScore);
            Database.putResourceOverwrite(subreddit.toDocument(), SubredditsResource.SUBREDDIT_COL);
        });
    }
}
