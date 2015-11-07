package net.adamsmolnik.handler;

import twitter4j.RawStreamListener;

public class TweetStreamListener implements RawStreamListener {

	private final String keyword;

	public TweetStreamListener(String keyword) {
		this.keyword = keyword;
	}

	@Override
	public void onMessage(String rawString) {
		System.out.println(keyword + ": " + rawString);

	}

	@Override
	public void onException(Exception ex) {

	}

}
