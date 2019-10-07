package models.messages;

import constants.BotMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Arrays;
import java.util.List;

public class MainMenuMessage extends SendMessage {

    public MainMenuMessage() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        KeyboardRow row1 = new KeyboardRow();
        row1.add(BotMessage.TRASH_OUT_TEXT);
        KeyboardRow row2 = new KeyboardRow();
        row2.add(BotMessage.TRASH_OUT_POSTPONE_TEXT);
        KeyboardRow row3 = new KeyboardRow();
        row3.add(BotMessage.BACK_TO_MAIN_MENU);
        keyboardMarkup.setKeyboard(Arrays.asList(row1, row2, row3));
        setReplyMarkup(keyboardMarkup);
    }

}
