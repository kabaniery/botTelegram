package org.session;

import org.db.DataBase;
import org.db.HtmlRequest;
import org.example.Bot;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator;

public class chatSession extends UserSession {
    public chatSession(Long id, Bot bot) {
        super(id, true, bot);
    }
    DataBase.Draw draw;
    @Override
    public void update(Message message) {
        if (this.draw != null && this.draw.getActive()) {
            this.draw.addNewUser(message.getFrom().getId(), message.getFrom().getUserName());
        }
        String status = HtmlRequest.getChatMember(this.id, message.getFrom().getId(), bot.getBotToken());
        if (status.equals("creator") || status.equals("administrator")) {
            if (message.getText().equals("Start draw")) {
                this.draw = new DataBase.Draw();
                bot.sendMessage(id, "Draw started!!!");
            } else if (message.getText().equals("Get result")) {
                if (this.draw == null) {
                    bot.sendMessage(id, "Draw hasn't started");
                } else if (!draw.getActive()) {
                    bot.sendMessage(id, "Draw hasn't started");
                } else {
                    bot.sendMessage(id, "Winner is: @" + draw.getResults());
                }
            }
        }
    }
}
