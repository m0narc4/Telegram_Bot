import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.*;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EquipmentRentalBot extends TelegramLongPollingBot {
    // команды
    private ArrayList<BotCommand> commands = new ArrayList<>();
    private String command;

    //
    private String usname = "postgres";
    private String usPassword = "password";
    private String host = "jdbc:postgresql://localhost:5432/postgres";
    private String driver = "org.postgresql.Driver";

    // счётчики
    private int i = -1;
    private int counter = 1;

    // информация о боте
    final String botName = "EquipmentRental_bot";
    final String botToken = "5990268750:AAHPHnDYIRzhw5W67odfQV8k0ouFE_mmBC4";

    // данные пользователя при авторизации
    private String entUserName = "";
    private String entUserPassword = "";

    // данные пользователя при регистрации
    private String name;
    private String password;
    private String email;

    // путь к изображению
    private String source = "D:\\Домашка\\2-ой курс\\2-ой семестр\\Програмная инженерия (Матрёнина)\\Bot\\src\\main\\java\\";

    // информация об оборудовании
    private String modelName = "";
    private double price;

    //
    private ArrayList<Equipment> equipment = new ArrayList<>();
    private User loginnedUser = new User();

    //
    private Long id;

    // работа с заказом
    private int rentalPeriod;
    private Equipment bookedEquipment;

    public EquipmentRentalBot() throws SQLException, ClassNotFoundException {
        commands.add(new BotCommand("/start", "start the bot"));

        try {
            this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        String str = "SELECT * FROM schema2.equipment;";
        ResultSet res = getResult(str);

        while (res.next()) {
            Equipment eq = new Equipment(res.getInt(1), res.getString(2), res.getDouble(3), res.getDouble(4));
            equipment.add(eq);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message messageFrom = update.getMessage();
            id = messageFrom.getChatId();
            Long chatID = id;
            SendMessage message = new SendMessage();
            if (messageFrom.isCommand()) {
                switch (messageFrom.getText()) {
                    case "/start":
                        message.setChatId(chatID);
                        message.setText("Привет! Я бот, который позволит арендовать любое желаемое Вами оборудование для самых разных целей.");
                        try {
                            execute(message);
                            execute(sendLogOrRegKeyboard(chatID));
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            } else {
                switch (this.command) {
                    case "/login":
                        i++;
                        switch (i) {
                            case 0:
                                entUserName = messageFrom.getText();
                                sendMsg(chatID, "Введите пароль: ");
                                break;
                            case 1:
                                entUserPassword = messageFrom.getText();
                                try {
                                    String str = "SELECT * FROM schema2.userTable WHERE userName = '" + entUserName + "';";
                                    ResultSet res = getResult(str);
                                    res.next();

                                    String userName = null;
                                    String userPassword = null;
                                    if (res.getRow() != 0) {
                                        userName = res.getString(2);
                                        userPassword = res.getString(3);
                                    }

                                    if (entUserName.equals(userName) && entUserPassword.equals(userPassword)) {
                                        sendMsg(chatID, "Успешная авторизация!");
                                        loginnedUser.setUserId(res.getInt(1));
                                        loginnedUser.setUserName(entUserName);
                                        loginnedUser.setUserPassword(entUserPassword);
                                        loginnedUser.setUserEmail(res.getString(4));
                                        loginnedUser.setAdmin(res.getBoolean(5));
                                        for (Equipment eq : equipment) {
                                            SendPhoto photo = new SendPhoto();
                                            photo.setChatId(chatID);
                                            try {
                                                String src = source + eq.getEquipmentId() + ".png";
                                                photo.setPhoto(new InputFile(new File(src)));
                                                execute(photo);

                                                InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                                                ArrayList<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

                                                ArrayList<InlineKeyboardButton> rowInline = new ArrayList<>();

                                                InlineKeyboardButton button1 = new InlineKeyboardButton();
                                                InlineKeyboardButton button2 = new InlineKeyboardButton();

                                                button1.setText("Выбрать модель");
                                                button1.setCallbackData(String.valueOf(eq.getEquipmentId()));


                                                button2.setText("Посмотреть доп. информацию");
                                                button2.setCallbackData(String.valueOf(eq.getEquipmentId()));


                                                rowInline.add(button1);
                                                rowInline.add(button2);

                                                rowsInline.add(rowInline);
                                                markupInline.setKeyboard(rowsInline);

                                                SendMessage message1 = new SendMessage();
                                                message1.setChatId(chatID);
                                                message1.setText("Оборудование: " + eq.getEquipmentName());
                                                message1.setReplyMarkup(markupInline);
                                                execute(message1);

                                            } catch (TelegramApiException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    else
                                        sendMsg(chatID, "Неверный логин или пароль. Попробуйте снова или зарегистрируйтесь, если вы этого не сделали");
                                } catch (ClassNotFoundException | SQLException e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                        break;
                    case "1":
                    case "2":
                        LocalDateTime time = LocalDateTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-d");
                        String date = time.format(formatter);
                        try {
                            String msg = update.getMessage().getText();
                            rentalPeriod = Integer.parseInt(msg);

                            String str = "INSERT INTO schema2.orders(userId, equipmentId, rentalPeriod, firstPrice, orderDate) VALUES('" + loginnedUser.getUserId() + "', " + bookedEquipment.getEquipmentId() + ", "+  rentalPeriod + ", " + bookedEquipment.getFirstPrice() + ", '" + date + "');";
                            insertQuery(str);
                            str = "UPDATE schema2.equipment SET isBooked = true WHERE equipmentId = " + bookedEquipment.getEquipmentId() + ";";
                            insertQuery(str);
                            sendMsg(id, "Оборудование успешно забронировано на ваше имя. Первоначальный взнос за аренду составляет " + bookedEquipment.getFirstPrice() + ". Ежедневная плата за аренду составляет " + bookedEquipment.getPrice() + ". Для продолжения введите команду /pay");
                        } catch (ClassNotFoundException | SQLException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        sendMsg(chatID, "Извините, я пока не умею отвечать на ваши сообщения :)");
                        break;
                }
            }
        } else if (update.hasCallbackQuery()) {
            id = update.getCallbackQuery().getMessage().getChatId();
            try {
                String com = update.getCallbackQuery().getData();
                SendMessage message = new SendMessage();
                message.setChatId(id);

                switch (com) {
                    case "/login":
                        message.setText("Введите логин:");
                        this.command = "/login";
                        execute(message);
                        break;
                    case "/register":
                        message.setText("Чтобы зарегистрироваться в системе, введите данные.");
                        this.command = "/register";
                        execute(message);
                        break;
                    case "1":
                        bookedEquipment = equipment.get(0);
                        if (!bookedEquipment.isBooked) {
                            sendMsg(id, "Выбранное оборудование: " + bookedEquipment.getEquipmentName());
                            SendPhoto photo2 = new SendPhoto();
                            photo2.setChatId(id);
                            String src = source + bookedEquipment.getEquipmentId() + ".png";
                            photo2.setPhoto(new InputFile(new File(src)));
                            execute(photo2);
                            sendMsg(id, "Для продолжения введите срок действия аренды в числовом формате (кол-во дней аренды):");
                            this.command = "1";
                        } else {
                            sendMsg(id, "Извините, выбранное вами оборудование уже кем-то забронировано");
                        }
                        break;
                    case "2":
                        bookedEquipment = equipment.get(1);
                        if (!bookedEquipment.isBooked) {
                            sendMsg(id, "Выбранное оборудование: " + bookedEquipment.getEquipmentName());
                            SendPhoto photo2 = new SendPhoto();
                            photo2.setChatId(id);
                            String src = source + bookedEquipment.getEquipmentId() + ".png";
                            photo2.setPhoto(new InputFile(new File(src)));
                            execute(photo2);
                            sendMsg(id, "Для продолжения введите срок действия аренды в числовом формате (кол-во дней аренды):");
                            this.command = "2";
                        } else {
                            sendMsg(id, "Извините, выбранное вами оборудование уже кем-то забронировано");
                        }
                        break;
                }


            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else {
            switch (command) {
                case "1":
                    sendMsg(id, "Месседж");
                    break;
            }
        }
    }

    public ResultSet getResult (String query) throws ClassNotFoundException, SQLException {
        Class.forName(driver);
        Connection con = DriverManager.getConnection(host, usname, usPassword);
        Statement stmt = con.createStatement();
        return stmt.executeQuery(query);
    }

    public void insertQuery (String query) throws ClassNotFoundException, SQLException {
        Class.forName(driver);
        Connection con = DriverManager.getConnection(host, usname, usPassword);
        Statement stmt = con.createStatement();
        stmt.executeUpdate(query);
    }

    public void sendMsg(Long who, String what){
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString())
                .text(what).build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public SendMessage sendLogOrRegKeyboard(long chatId) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        ArrayList<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        ArrayList<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();

        button1.setText("Войти в систему");
        button1.setCallbackData("/login");


        button2.setText("Зарегистрироваться");
        button2.setCallbackData("/register");


        rowInline.add(button1);
        rowInline.add(button2);

        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Пожалуйста, выберите действие:");
        message.setReplyMarkup(markupInline);
        return message;
    }

    public SendMessage menuKeyboard(long chatId) {
        InlineKeyboardMarkup mInline = new InlineKeyboardMarkup();
        ArrayList<List<InlineKeyboardButton>> rsInline = new ArrayList<>();

        ArrayList<InlineKeyboardButton> rInline = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();

        button1.setText("Выбрать модель");
        button1.setCallbackData("/chooseModel");

        button2.setText("Посмотреть подробную информацию");
        button2.setCallbackData("/anyInfo");

        rInline.add(button1);
        rInline.add(button2);

        rsInline.add(rInline);
        mInline.setKeyboard(rsInline);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите действие:");
        message.setReplyMarkup(mInline);
        return message;
    }

    @Override
    public String getBotUsername() {
        return this.botName;
    }

    @Override
    public String getBotToken() {
        return this.botToken;
    }
}
