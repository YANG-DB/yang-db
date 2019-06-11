package com.kayhut.fuse.dispatcher.debug;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
