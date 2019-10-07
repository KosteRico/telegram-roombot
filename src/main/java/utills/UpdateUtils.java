package utills;

import org.telegram.telegrambots.meta.api.objects.Update;

public final class UpdateUtils {

    private UpdateUtils() {
    }

    public static long getChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getFrom().getId();
        }
        return 0;
    }
}
