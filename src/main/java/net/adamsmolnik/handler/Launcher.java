package net.adamsmolnik.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import twitter4j.FilterQuery;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;

public class Launcher {

	public static final String PUBLIC_KEY = "...";
	public static final String SECRET_KEY = "...";
	public static final String PUBLIC_ACCESS_TOKEN = "...";
	public static final String SECRET_ACCESS_TOKEN = "...";

	public static void main(String[] args) throws Exception {
		List<TwitterStream> twitterStreams = new ArrayList<>();

		String key1 = "AWS";
		String key2 = "iphone";
		String key3 = "sex";

		new Thread() {
			@Override
			public void run() {
				TwitterStream ts = newInstance();
				twitterStreams.add(ts);
				addListenerWithFilter(key1, ts);

			}

		}.start();

		new Thread() {
			@Override
			public void run() {
				TwitterStream ts = newInstance();
				twitterStreams.add(ts);
				addListenerWithFilter(key2, ts);
			}

		}.start();

		new Thread() {
			@Override
			public void run() {
				TwitterStream ts = newInstance();
				twitterStreams.add(ts);
				addListenerWithFilter(key3, ts);

			}

		}.start();

		TimeUnit.MINUTES.sleep(3);
		twitterStreams.forEach(ts -> {
			ts.cleanUp();
		});

	}

	private static TwitterStream newInstance() {
		TwitterStream ts = new TwitterStreamFactory().getInstance();
		ts.setOAuthConsumer(PUBLIC_KEY, SECRET_KEY);
		ts.setOAuthAccessToken(new AccessToken(PUBLIC_ACCESS_TOKEN, SECRET_ACCESS_TOKEN));
		return ts;
	}

	private static void addListenerWithFilter(String key, TwitterStream ts) {
		ts.addListener(new TweetStreamListener(key));
		FilterQuery filter = new FilterQuery();
		filter.track(key);
		ts.filter(filter);
	}

}
