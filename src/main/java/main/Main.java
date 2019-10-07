package main;

import constants.Configs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws TelegramApiException {
        Logger logger = LogManager.getLogger(Main.class);
        logger.traceEntry();
        ApiContextInitializer.init();
//        DefaultBotOptions options = ApiContext.getInstance(DefaultBotOptions.class);
//        options.setProxyHost(Configs.PROXY_ADDRESS);
//        options.setProxyPort(Configs.PROXY_PORT);
//        options.setProxyType(Configs.PROXY_TYPE);
        logger.error("Initialized...");
        logger.error("Configured...");

        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        //RoomBot bot = new RoomBot(options);
        RoomBot bot = new RoomBot();
        telegramBotsApi.registerBot(bot);
        logger.error("Bot registered!");
        bot.launchBackgroundThread();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Exit? (Y/N)");
//        while (scanner.next().equalsIgnoreCase("N")) {
//            System.out.println("Exit? (Y/N)");
//        }
//        System.exit(0);
    }
}
