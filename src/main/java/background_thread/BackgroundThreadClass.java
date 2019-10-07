package background_thread;

import constants.BotMessage;
import main.RoomBot;
import models.CycleQueue;
import models.database.DBManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.Date;

public class BackgroundThreadClass extends Thread {

    private DBManager dbManager;
    private Logger log;
    private RoomBot bot;
    private CycleQueue remindTimes;

    public BackgroundThreadClass(RoomBot bot) {
        log = LogManager.getLogger(getClass());
        log.traceEntry();
        dbManager = DBManager.getInstance();
        this.bot = bot;
        initRemindTimes();
    }

    private void initRemindTimes() {
        remindTimes = new CycleQueue();
        remindTimes.add(LocalTime.parse("20:00"));
        remindTimes.add(LocalTime.parse("07:00"));
        remindTimes.add(LocalTime.parse("09:00"));
        remindTimes.add(LocalTime.parse("16:00"));
        remindTimes.adjust();
    }

    @Override
    public void run() {
        DateTime topDate;
        log.error("UpdateDateThread is started!!!");

        while (!Thread.currentThread().isInterrupted()) {
            try {
                topDate = new DateTime(dbManager.getTopDate());
                if (dbManager.hasTrashOutMan() && isNow(remindTimes.peek())) {
                    sendReminder();
                    remindTimes.next();
                }

                if (!dbManager.hasTrashOutMan()
                        && isToday(topDate)) {
                    dbManager.setTrashOutMan(true);
                }

            } catch (SQLException | TelegramApiException e) {
                System.out.println(e.getMessage());
                Thread.currentThread().interrupt();
            }
        }

    }

    private void sendReminder() throws SQLException, TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(dbManager.getClosestChatId());
        message.enableMarkdown(true);
        message.setText("*Вы выбросили мусор?*\nПожайлуста, ыбросите мусор " +
                "и выберите:\n\"*"
                + BotMessage.TRASH_OUT_TEXT + "*\" или \n\"*"
                + BotMessage.TRASH_OUT_POSTPONE_TEXT + "*\"");
        bot.execute(message);
    }

    private boolean isToday(DateTime dateTime) {
        int now = DateTime.now().getDayOfYear();
        return now == dateTime.getDayOfYear();
    }

    private boolean isNow(LocalTime time) {
        Minutes lessThan = Minutes.minutesBetween(LocalTime.now(), time);
        return Math.abs(lessThan.getMinutes()) <= 1;
    }

}
