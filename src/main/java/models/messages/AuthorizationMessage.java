package models.messages;

import models.database.DBManager;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class AuthorizationMessage extends SendMessage {

    public AuthorizationMessage(DBManager manager) throws SQLException {
        super();
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> userButtons = new LinkedList<>();
        ResultSet table = manager.getTable();

        while (table.next()) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(table.getString("firstname"));
            button.setCallbackData(button.getText() + "-" + table.getString("lastname") + "-auth");
            userButtons.add(Collections.singletonList(button));
        }

        markup.setKeyboard(userButtons);
        setReplyMarkup(markup);

        setText("Выберите своё имя:");
    }
}
