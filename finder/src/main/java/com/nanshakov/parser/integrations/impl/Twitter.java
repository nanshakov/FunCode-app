//package com.nanshakov.parser.integrations.impl;
//
//import com.nanshakov.common.dto.Platform;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//import lombok.extern.log4j.Log4j2;
//import twitter4j.Query;
//import twitter4j.QueryResult;
//import twitter4j.Status;
//import twitter4j.TwitterException;
//import twitter4j.TwitterFactory;
//import twitter4j.conf.ConfigurationBuilder;
//
//@Log4j2
//@Service
//public class Twitter extends BaseIntegrationImpl {
//
//    @Value("${tag}")
//    private String tag;
//
//    @Override
//    public void start() throws InterruptedException {
//        Thread.sleep(1000);
//        if (!type.equals(getPlatform().toString())) { return; }
//        printBaseInfo();
//        log.info("Started...");
//        ConfigurationBuilder cb = new ConfigurationBuilder();
//        cb.setDebugEnabled(true);
//        cb.setUser("nanshakov@gmail.com");
//        cb.setPassword("Nikita112233");
//        twitter4j.Twitter twitter = new TwitterFactory(cb.build()).getInstance();
//        try {
//            Query query = new Query("deutsch memes");
//            QueryResult result;
//            do {
//                result = twitter.search(query);
//                List<Status> tweets = result.getTweets();
//                for (Status tweet : tweets) {
//                    System.out.println("@" + tweet.getUser().getScreenName() + " - " + tweet.getText());
//                }
//            } while ((query = result.nextQuery()) != null);
//            System.exit(0);
//        } catch (TwitterException te) {
//            te.printStackTrace();
//            System.out.println("Failed to search tweets: " + te.getMessage());
//            System.exit(-1);
//        }
//
////        int count = Integer.MAX_VALUE;
////        for (int i = 0; i < count; i++) {
////            Document doc = getPage(i);
////            if (doc == null) {
////                close();
////                return;
////            }
////            Elements listNews = doc.select("img");
////            //получаем новые id
////            if (doc.selectFirst("li[data-next]") != null) {
////                nextId = doc.selectFirst("li[data-next]").attr("data-next");
////            } else {
////                break;
////            }
////            listNews.forEach(el -> {
////                Post post = parse(el);
////                String hash = calculateHash(post) + "02";
////                if (!exist(hash)) {
////                    sendToKafka(hash, post);
////                } else {
////                    log.trace("Post {} with hash {} found in redis, do nothing", post, hash);
////                }
////            });
////        }
//    }
//
//    @Override
//    public Platform getPlatform() {
//        return Platform.TWITTER;
//    }
//
//    void printBaseInfo() {
//        log.info(new StringBuilder()
//                .append("Module : ").append(getPlatform()).append("\n")
//                .append("Tags : ").append(tag));
//    }
//}
