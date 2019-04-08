package net.thetechstack;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.util.List;

public class App {
    public static void main(String[] args) throws TwitterException {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("sFCQF5Nssrh7ukLnhRuiS45Hx")
                .setOAuthConsumerSecret("MHXtyoOGSmMDEJ4HGtRS9JhAo4ilrhiTvPOFUfiEsc9vxqM9of")
                .setOAuthAccessToken("1036728000010760195-aEQPF0dqflVqSmBI3tu6llgDIry0tq")
                .setOAuthAccessTokenSecret("FaAzjrWQTCexQ1G6nG8SkYryKPLq7bQ8MylYw3qiTl9eW");
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();
        List<Status> statuses = twitter.getHomeTimeline();
        System.out.println("Showing home timeline");
        for (Status status : statuses) {
            System.out.println(status.getUser().getName() + ":" +
                    status.getText());
        }
    }
}
