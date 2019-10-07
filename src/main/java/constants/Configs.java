package constants;

import org.telegram.telegrambots.bots.DefaultBotOptions;

import java.time.ZoneId;

public final class Configs {

    private Configs() {
    }

    public static final ZoneId ZONE_ID = ZoneId.of("UTC+3");
    public static final Long CHAT_ID = -1001332099086L;
    public static String TOKEN = "945350923:AAFUzici68QJYq6Nh03E8nc2wpjzAUpp5QU";
    public static final String BOT_USERNAME = "ourRoombot";
    public static final long ME_ID = 695279040;
    public static final String PROXY_ADDRESS = "34.65.130.163";
    public static final int PROXY_PORT = 3128;
    public static final DefaultBotOptions.ProxyType PROXY_TYPE = DefaultBotOptions.ProxyType.HTTP;
}
