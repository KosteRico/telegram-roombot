package constants;

public final class BotCommand {

    private BotCommand() {
    }

    private static final String START = "/start";
    private static final String TRASHOUT = "/trashout";

    public static boolean isStart(String mes) {
        return mes.equals(START);
    }

    public static boolean isTrashout(String mes) {
        return mes.equals(TRASHOUT);
    }
}
