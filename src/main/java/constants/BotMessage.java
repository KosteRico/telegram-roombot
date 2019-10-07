package constants;

public final class BotMessage {

    private static final String TRASH_EMOJI = "\uD83D\uDEAE";

    public static final String BACK_TO_MAIN_MENU = "Вернуться в главное меню";
    public static final String TRASH_OUT_TEXT = "Я вынес мусор " + TRASH_EMOJI;
    public static final String TRASH_OUT_POSTPONE_TEXT = "Хочу отложить вынос мусора";

    public static boolean isTrashOutConfirm(String string) {
        return string.contains("Выбросил ли ") && string.contains("мусор?");
    }

    public static boolean isBackToMainMenu(String string) {
        return string.equals(BACK_TO_MAIN_MENU);
    }

    public static boolean isTrashOutText(String mes) {
        return mes.equals(TRASH_OUT_TEXT);
    }

    public static boolean isTrashOutPostpone(String mes) {
        return mes.equals(TRASH_OUT_POSTPONE_TEXT);
    }

    private BotMessage() {
    }
}
