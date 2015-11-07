package net.adamsmolnik.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.PreDestroy;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

public class SimpleSqsEndpoint{

    private ScheduledExecutorService poller;

    private ExecutorService tasksExecutor;

    private AmazonSQS sqs;

    private List<ScheduledFuture<?>> pollerFutures;

    public void init() {
        poller = Executors.newSingleThreadScheduledExecutor();
        tasksExecutor = Executors.newFixedThreadPool(10);
        sqs = new AmazonSQSClient();
        pollerFutures = new ArrayList<ScheduledFuture<?>>();
    }

    public final void handleString(Function<String, String> requestProcessor, String queueIn) {
        handle(requestProcessor, Function.identity(), queueIn);
    }

    public final void handleVoid(Consumer<String> requestProcessor, String queueIn) {
        handle(request -> {
            requestProcessor.accept(request);
            return null;
        }, Function.identity(), queueIn);
    }

    public final <T, R> void handle(Function<T, R> requestProcessor, Function<String, T> requestMapper, String queueIn) {
        pollerFutures.add(poller.scheduleWithFixedDelay(() -> {
            try {
                List<Message> messages = sqs.receiveMessage(new ReceiveMessageRequest().withQueueUrl(queueIn)).getMessages();
                for (Message message : messages) {
                    T request = requestMapper.apply(message.getBody());
                    tasksExecutor.submit(() -> {
                        try {
                            requestProcessor.apply(request);
                        } catch (Throwable t) {
                            throw t;
                        }
                    });
                    sqs.deleteMessage(new DeleteMessageRequest(queueIn, message.getReceiptHandle()));
                }
            } catch (Exception ex) {
            	ex.printStackTrace();
            }

        }, 0, 10, TimeUnit.SECONDS));
    }

    @PreDestroy
    public void shutdown() {
        pollerFutures.forEach((pollerFuture) -> pollerFuture.cancel(true));
        poller.shutdownNow();
        tasksExecutor.shutdownNow();
        sqs.shutdown();
    }
}