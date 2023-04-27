package org.telegramMethods;

import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;

public class Pool {
    Poll poll;
    public Pool() {
        this.poll = new Poll();
        SendPoll sendPoll = new SendPoll();
    }
}
