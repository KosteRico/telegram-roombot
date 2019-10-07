package constants;

public enum StateId {
    MAIN_MENU(1),
    TRASH_OUT_CONFIRM(2),
    TRASH_OUT_CONFIRM_WAITING(3);

    private final int id;

    StateId(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }

    public static boolean isRequired(int id) {
        return id == TRASH_OUT_CONFIRM.id || id == TRASH_OUT_CONFIRM_WAITING.id;
    }

    public static String requiredReplyMessageTextFactory(int stateId) {
        switch (stateId) {
            case 2:
                return "Вы ещё не выполнили предыдущую *операцию*!";
            case 3:
                return "Ожидайте подтверждения сокамерника ...";
            default:
                return null;
        }
    }

}
