package main;

import background_thread.BackgroundThreadClass;
import constants.BotCallbackQuery;
import constants.BotCommand;
import constants.BotMessage;
import constants.Configs;
import models.database.DBManager;
import models.messages.AuthorizationMessage;
import models.messages.MainMenuMessage;
import models.messages.YesNoMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.LocalDate;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import utills.UpdateUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static constants.StateId.*;

public class RoomBot extends TelegramLongPollingBot {

    private Thread updateDateThread;
    private DBManager dbManager;
    private SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy");
    private Logger log;

    public RoomBot(DefaultBotOptions options) {
        super(options);
        init();
    }

    public RoomBot() {
        super();
        init();
    }

    public void launchBackgroundThread() {
        updateDateThread = new BackgroundThreadClass(this);
        updateDateThread.start();
    }

    private void init() {
        log = LogManager.getLogger(getClass());
        log.traceEntry();
        dbManager = DBManager.getInstance();
        log.error("Roombot is started!!");
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() || update.hasCallbackQuery()) {
            try {
                if (dbManager.hasMenChatIdInTable(update)) {
                    final int stateId = dbManager.getStateId(update);

                    if (update.hasMessage()) {
                        String message = update.getMessage().getText();

                        if (!isRequired(stateId)) {
                            if (BotCommand.isStart(message)) {
                                if (stateId != MAIN_MENU.id()) {
                                    dbManager.setStateId(update, MAIN_MENU);
                                }
                                onWelcome(update);
                            } else if (BotMessage.isBackToMainMenu(message)
                                    && stateId != MAIN_MENU.id()) {
                                dbManager.setStateId(update, MAIN_MENU);
                                onMainMenu(update);
                            } else if (BotMessage.isTrashOutText(message)) {
                                onTrashOut(update);
                            } else if (BotMessage.isTrashOutConfirm(message)) {
                                dbManager.setStateId(update, TRASH_OUT_CONFIRM);
                            } else if (BotMessage.isTrashOutPostpone(message)) {
                                onTrashOutPostpone(update);
                            } else if (BotCommand.isTrashout(message)) {
                                onLoadSchedule(update);
                            }
                        } else {
                            if (BotCommand.isTrashout(message)) {
                                onLoadSchedule(update);
                            }
                        }
                    } else if (update.hasCallbackQuery()) {
                        String query = update.getCallbackQuery().getData();

                        if (BotCallbackQuery.isTrashOutConfirmQuery(query)) {
                            forSendTrashOutConfirmQuery(update);
                        }

                    }

                    if (isRequired(stateId)) {
                        sendQuickMessage(update, requiredReplyMessageTextFactory(stateId));
                    }
                } else {
                    if (update.hasCallbackQuery()) {
                        if (update.getCallbackQuery().getData().endsWith("-auth")) {
                            dbManager.updatePersonChatId(update);
                            onWelcome(update);
                        }
                    } else {
                        onUserAuthorization(update);
                    }
                }
            } catch (SQLException | TelegramApiException e) {
                log.fatal(e.getMessage());
            }
        }
    }

    private void onWelcome(Update update) throws SQLException, TelegramApiException {
        sendReplyMainMenuMessage(update, "Приветствую, " + dbManager.getFirstName(update));
    }

    private void onTrashOutPostpone(Update update) throws SQLException, TelegramApiException {

        if (!checkTrashQueue(update)) return;

        ResultSet closest = dbManager.getSortedSchedule();
        closest.next();

        SendMessage message = new SendMessage();
        message.setChatId(UpdateUtils.getChatId(update));
        if (closest.getLong("chatid") == UpdateUtils.getChatId(update)) {
            Date date = closest.getDate("date");
            dbManager.postpone();
            message.setText(String.format("Вы отложили вынос мусора! Дата вашей очереди: %s (%s)",
                    formatDate.format(date), parseDay(date)));
        } else {
            message.setText("Ваша очередь ещё не подошла");
        }
        closest.close();
        execute(message);
    }

    private boolean checkTrashQueue(Update update) throws SQLException, TelegramApiException {

        if (!dbManager.hasTrashOutMan()) {
            sendQuickMessage(update, "Очередь выносить мусор ещё *не подошла*. " +
                    "Не переживайте, ваша очередь наступит в *" + parseDay(dbManager.getDateByUpdate(update)) + "*");
            return false;
        }
        return true;
    }

    private void onTrashOut(Update update) throws SQLException, TelegramApiException {

        if (!checkTrashQueue(update)) return;

        List<Long> chatIds = dbManager.getChatIdsExceptFor(update);
        for (Long chatId : chatIds) {
            YesNoMessage yesNoMessage = new YesNoMessage();
            String mes = UpdateUtils.getChatId(update) + "-%s-trashout-confirm";
            yesNoMessage.setYesCallbackData(String.format(mes, "yes"));
            yesNoMessage.setNoCallbackData(String.format(mes, "no"));
            yesNoMessage.setChatId(chatId);
            yesNoMessage.setText("Выбросил ли " + dbManager.getFirstName(update) + " мусор?");
            dbManager.setStateId(chatId, TRASH_OUT_CONFIRM);
            dbManager.addMessageForDeletion(execute(yesNoMessage));
        }
        sendQuickMessage(update, "Ждите подтверждение вашего соседа ...");
    }

    private void forSendTrashOutConfirmQuery(Update update) throws SQLException, TelegramApiException {
        String[] querySplit = update.getCallbackQuery().getData().split("-");
        SendMessage message = new SendMessage();
        message.setChatId(Long.parseLong(querySplit[0]));
        String text = null;
        switch (querySplit[1]) {
            case "yes":
                text = dbManager.getFirstName(update) + " подтвердил, что вы вынесли мусор";
                if (!dbManager.isTrashOutMan(update)) {
                    dbManager.swapDates(update);
                } else {
                    dbManager.setTrashOutMan(false);
                }
                dbManager.incrementTopPersonSchedule();
                break;
            case "no":
                text = dbManager.getFirstName(update) + " утверждает, что вы не вынесли мусор";
                break;
        }
        message.setText(text);

        List<DeleteMessage> list = dbManager.getDeleteMessages();

        for (DeleteMessage deleteMessage : list) {
            long id = Long.parseLong(deleteMessage.getChatId());
            dbManager.setStateId(id, MAIN_MENU);
            try {
                execute(deleteMessage);
            } catch (TelegramApiException ignore) {
            }
            dbManager.deleteMessageForDeletion(id);
        }

        execute(message);
    }

    private void onMainMenu(Update update) throws TelegramApiException {
        MainMenuMessage menuMessage = new MainMenuMessage();
        menuMessage.setText("Главное меню");
        menuMessage.setChatId(UpdateUtils.getChatId(update));
        execute(menuMessage);
    }

    private void onUserAuthorization(Update update) throws SQLException, TelegramApiException {
        AuthorizationMessage sendMessage = new AuthorizationMessage(dbManager);
        sendMessage.setChatId(UpdateUtils.getChatId(update));
        execute(sendMessage);
    }

    public void onLoadSchedule(Update update) {

        StringBuilder string = new StringBuilder("Текущее расписание:\n");
        ResultSet set = dbManager.getSortedSchedule();

        try {
            while (set.next()) {

                Date date = set.getDate("date");

                string.append(set.getString("firstname"))
                        .append("\t")
                        .append(set.getString("lastname"))
                        .append("\t")
                        .append(formatDate.format(date))
                        .append(" (")
                        .append(parseDay(date))
                        .append(")")
                        .append("\n");
            }
            set.close();

            sendQuickMessage(update, string.toString());
        } catch (SQLException | TelegramApiException e) {
            log.fatal(e.getMessage());
        }

    }

    private String parseDayInWeek(int day) {
        switch (day) {
            case 2:
                return "понедельник";
            case 3:
                return "вторник";
            case 4:
                return "среда";
            case 5:
                return "четверг";
            case 6:
                return "пятница";
            case 7:
                return "суббота";
            case 1:
                return "воскресенье";
            default:
                return null;
        }
    }

    private Message sendQuickMessage(Update update, String message) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(UpdateUtils.getChatId(update));
        sendMessage.setText(message);
        sendMessage.enableMarkdown(true);
        return execute(sendMessage);
    }

    private String parseDay(Date nextDate) {
        LocalDate now = LocalDate.now();
        int dayOfWeek = now.getDayOfWeek();
        int dif = new LocalDate(nextDate).getDayOfYear() - now.getDayOfYear();

        switch (dif) {
            case -2:
                return "позавчера";
            case -1:
                return "вчера";
            case 0:
                return "сегодня";
            case 1:
                return "завтра";
            case 2:
                return "послезавтра";
            default:
                return parseDayInWeek(dayOfWeek);
        }
    }

    private Message sendReplyMainMenuMessage(Update update, String message) throws TelegramApiException {
        MainMenuMessage menuMessage = new MainMenuMessage();
        menuMessage.setChatId(UpdateUtils.getChatId(update));
        menuMessage.setText(message);
        menuMessage.enableMarkdown(true);
        return execute(menuMessage);
    }

    @Override
    public void onClosing() {
        updateDateThread.interrupt();
        dbManager.close();
        System.out.println("Bot was closed!!!");
    }

    @Override
    public String getBotUsername() {
        return Configs.BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return Configs.TOKEN;
    }

}
