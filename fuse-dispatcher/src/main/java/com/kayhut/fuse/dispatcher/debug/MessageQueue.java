package com.kayhut.fuse.dispatcher.debug;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by benishue on 05-Apr-17.
 */
public class MessageQueue {
    public static class Message {
        public Message(Date date, long threadId, String message) {
            this.date = date;
            this.threadId = threadId;
            this.message = message;
        }

        @Override
        public String toString() {
            return this.date.toString() + " : " + this.threadId + " : " + this.message;
        }

        private Date date;
        private long threadId;
        private String message;
    }

    public static MessageQueue instance = new MessageQueue();

    //region Constructors
    public MessageQueue() {
        this.messages = new ArrayList<>();
        this.sync = new Object();
    }
    //endregion

    //region Public Methods
    public void addMessage(String message) {
        synchronized (this.sync) {
            this.messages.add(new MessageQueue.Message(new Date(), Thread.currentThread().getId(), message));
        }
    }

    public Collection<Message> getMessages() {
        return this.messages;
    }

    public void clear() {
        synchronized (this.sync) {
            this.messages.clear();
        }
    }
    //endregion

    //region Fields
    private List<Message> messages;
    private Object sync;
    //endregion
}
