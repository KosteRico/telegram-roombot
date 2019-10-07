package constants;

public final class BotCallbackQuery {

    private BotCallbackQuery() {
    }

    public static boolean isTrashOutConfirmQuery(String query) {
        return query.endsWith("-trashout-confirm");
    }

}
