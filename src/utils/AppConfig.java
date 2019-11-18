package utils;

public class AppConfig {

    public static final int RALL_FRONTPAGE_SIZE = 500;

    public static final int SUBREDDIT_FRONTPAGE_SIZE = 250;

    public static final boolean RALL_DEPENDS_ON_SUBREDDITS = true;

    public static final int HOURS_OF_FRONT_PAGE_POSTS = 24;

    public static final int NUMBER_TOP_SUBREDDITS = 40;

    public static final int EXPIRE_TIMEOUT_OLD_TOP_SUBREDDIT = 60; // seconds

    public static final int EXPIRE_TIMEOUT_TOP = 60 * 20; // seconds

    public static final int SCORE_UPDATE_PERIOD = 10; // minutes

    public static final double SCORE_LOST_PER_PERIOD = 0.995;

    public static final int NUMBER_CHILDREN_POSTS = 30;

    public static final int NUMBER_CHILDREN_COMMENTS = 5;
}
