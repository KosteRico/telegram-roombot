package models.messages;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class YesNoMessage extends SendMessage {

    private InlineKeyboardButton noButton;
    private InlineKeyboardButton yesButton;

    public YesNoMessage() {
        super();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        yesButton = new InlineKeyboardButton();
        noButton = new InlineKeyboardButton();

        yesButton.setText("Да");
        noButton.setText("Нет");

        markup.setKeyboard(Collections
                .singletonList(Arrays.asList(yesButton, noButton)));
        setReplyMarkup(markup);
    }

    public void setYesCallbackData(String callbackData) {
        yesButton.setCallbackData(callbackData);
    }

    public void setNoCallbackData(String callbackData) {
        noButton.setCallbackData(callbackData);
    }

}
