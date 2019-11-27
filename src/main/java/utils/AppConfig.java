package utils;

public class AppConfig {

    public static final int ALL_FRONTPAGE_SIZE = 500;

    public static final int SUBREDDIT_FRONTPAGE_SIZE = 250;

    public static final int FRONTPAGES_UPDATE_PERIOD = 12;

    public static final int FRONTPAGE_TIME_WINDOW = 36; // hours

    public static final int NUMBER_TOP_SUBREDDITS = 40;

    public static final int POST_AND_THREAD_CACHE_TIMEOUT = 30; // seconds

    public static final int POST_SCORE_UPDATE_PERIOD_ON_CACHE = 30; // minutes

    public static final int SUBREDDIT_SCORE_UPDATE_PERIOD_ON_CACHE = 3; // hours

    public static final int SCORE_UPDATE_PERIOD_ON_DB = 11; // hours

    public static final int NUMBER_CHILDREN_POSTS = 30;

    public static final int NUMBER_CHILDREN_COMMENTS = 5;

    public static final long UPVOTE_SCORE_VALUE = 2;

    public static final long DOWNVOTE_SCORE_VALUE = -3;

    public static final long COMMENT_SCORE_VALUE = 3;

    public static final boolean IS_CACHE_ON = true;
}
