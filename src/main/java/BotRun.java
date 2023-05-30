import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.sql.SQLException;

public class BotRun {
    public static void main(String[] args) throws TelegramApiException, SQLException, ClassNotFoundException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        EquipmentRentalBot bot = new EquipmentRentalBot();
        telegramBotsApi.registerBot(bot);
    }
}