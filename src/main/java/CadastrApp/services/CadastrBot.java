package CadastrApp.services;

import CadastrApp.config.AppConstants;
import CadastrApp.models.ObjectPoint;
import CadastrApp.models.RealEstateObject;
import CadastrApp.models.UserRequest;
import lombok.Getter;
import CadastrApp.config.BotConfig;

import org.eclipse.emf.ecore.util.Switch;
import org.opengis.referencing.FactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Component
public class CadastrBot extends TelegramLongPollingBot {
    private final Logger logger = LoggerFactory.getLogger(CadastrBot.class);
    private final BotConfig config;
    private String selectedUTMZone = "";
    private String selectedFormat = "";
    String name = new String();
    LinkedHashMap<Integer, ObjectPoint> map = new LinkedHashMap<>();
    LinkedHashMap<Integer, ObjectPoint> finalMap = new LinkedHashMap<>();
    CadastralSearchInterface searchInterface = new CadastrApp.services.CadastralSearch();
    ExportToFile exporter = new ExportToFile(); // Сохраняем экземпляр ExportToFile

    public CadastrBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "get a welcome message"));
        listOfCommands.add(new BotCommand("/find", "find information about the land site"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            logger.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();

            if (messageText.equals("/start")) {
                startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
            } else if (messageText.equals("/find")) {
                sendMessage(chatId, "Пожалуйста, введите кадастровый номер участка:");
                UserState.setState(String.valueOf(chatId), UserState.WAITING_FOR_CADASTRAL_NUMBER);
            } else if (UserState.getState(String.valueOf(chatId)) == UserState.WAITING_FOR_CADASTRAL_NUMBER) {
                if (CadastralNumberValidator.isValidCadastralNumber(messageText)) {
                    String cadastralNumber = messageText;
                    UserRequest request = new UserRequest(cadastralNumber);
                    name = messageText;
                    RealEstateObject object = searchInterface.checkingAvailabilityData(request);
                    if (object == null) {
                        sendMessage(chatId, "Данных по участку с номером " + cadastralNumber +
                                " не найдено. Пожалуйста, проверьте номер участка.");
                        sendRetryButton(chatId);
                    } else {
                        sendMessage(chatId, object.getInfo_link());
                        sendUTMZoneButtons(chatId);
                        map = searchInterface.getCoords(object);
                        UserState.setState(String.valueOf(chatId), UserState.WAITING_FOR_UTM_SELECTION);
                    }
                } else {
                    sendMessage(chatId, "Неверный формат кадастрового номера. " +
                            "Попробуйте снова. Формат должен быть: 12.34.56.789");
                    sendRetryButton(chatId);
                }
            } else if (UserState.getState(String.valueOf(chatId)) == UserState.WAITING_FOR_UTM_SELECTION) {
                if (messageText.equals("utm-37") || messageText.equals("utm-38")) {
                    selectedUTMZone = messageText.equals("utm-37") ? AppConstants.UTM37 : AppConstants.UTM38;
                    sendMessage(chatId, "Вы выбрали " + selectedUTMZone);
                    CoordinateConverterInterface converter = new CoordinateConverter();
                    try {
                        finalMap = converter.convertingPoints(map, selectedUTMZone);
                        sendFormatButtons(chatId);
                        UserState.setState(String.valueOf(chatId), UserState.WAITING_FOR_FORMAT_SELECTION);
                    } catch (FactoryException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    sendMessage(chatId, "Пожалуйста, выберите зону UTM, нажав на одну из кнопок.");
                }
            } else if (UserState.getState(String.valueOf(chatId)) == UserState.WAITING_FOR_FORMAT_SELECTION) {
                if (messageText.equals("DXF") || messageText.equals("CSV")) {
                    selectedFormat = messageText;
                    if (selectedFormat.equals("CSV")) {
                        exporter.exportToCSV(finalMap);
                    } else if (selectedFormat.equals("DXF")) {
                        exporter.exportToDXF(finalMap);
                    }
                    try {
                        sendDocument(chatId, exporter.getFileName(), exporter.getFileBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    // После отправки файла, сбрасываем состояние пользователя
                    UserState.setState(String.valueOf(chatId), UserState.NONE);
                } else {
                    sendMessage(chatId, "Пожалуйста, выберите формат, нажав на одну из кнопок.");
                }
            } else {
                sendMessage(chatId, "Извините, я не понимаю эту команду. Попробуйте /start или /find.");
            }
        }
        if (update.hasCallbackQuery()) {
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String callbackData = update.getCallbackQuery().getData();

            if (callbackData.equals("retry")) {
                sendMessage(chatId, "Пожалуйста, введите кадастровый номер участка:");
                UserState.setState(String.valueOf(chatId), UserState.WAITING_FOR_CADASTRAL_NUMBER);
            } else if (callbackData.equals("utm-37") || callbackData.equals("utm-38")) {
                selectedUTMZone = callbackData.equals("utm-37") ? AppConstants.UTM37 : AppConstants.UTM38;
                CoordinateConverterInterface converter = new CoordinateConverter();
                try {
                    finalMap = converter.convertingPoints(map, selectedUTMZone);
                    sendFormatButtons(chatId);
                    UserState.setState(String.valueOf(chatId), UserState.WAITING_FOR_FORMAT_SELECTION);
                } catch (FactoryException e) {
                    throw new RuntimeException(e);
                }
            } else if (callbackData.equals("dxf") || callbackData.equals("csv")) {
                selectedFormat = callbackData.toUpperCase();
                if (selectedFormat.equals("CSV")) {
                    exporter.setFileName(name + ".csv");
                    byte[] csvBytes = exporter.exportToCSV(finalMap);
                    sendDocument(chatId, exporter.getFileName(), csvBytes);
                } else if (selectedFormat.equals("DXF")) {
                    exporter.setFileName(name + ".dxf");
                    byte[] dxfBytes = exporter.exportToDXF(finalMap);
                    sendDocument(chatId, exporter.getFileName(), dxfBytes);
                }
                UserState.setState(String.valueOf(chatId), UserState.NONE);
            }
        }
    }

    private void reconnect(){
        try{
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(this);
            logger.info("Успешное подключение");
        } catch (TelegramApiException e){
            logger.error("Ошибка переподключения: {}", e.getMessage());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ie){
              Thread.currentThread().interrupt();
            }
            reconnect();
        }
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = "Hello " + name + "! You are in the Topograf.ge bot " +
                "here you can find out the coordinates of the cadastral boundary " +
                "of the site you are interested in on the territory of Georgia, and " +
                "if you want us to show you these borders on the ground, contact us https://t.me/Topograf_ge";
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String messageText) {
        SendMessage message = new SendMessage(String.valueOf(chatId), messageText);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendUTMZoneButtons(long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createButton("utm-37", "utm-37"));
        row1.add(createButton("utm-38", "utm-38"));

        rows.add(row1);
        markup.setKeyboard(rows);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Пожалуйста, выберите зону UTM:");
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendFormatButtons(long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createButton("DXF", "dxf"));
        row1.add(createButton("CSV", "csv"));

        rows.add(row1);
        markup.setKeyboard(rows);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Пожалуйста, выберите формат:");
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    private void sendDocument(long chatId, String fileName, byte[] fileBytes) {
        InputFile inputFile = new InputFile(new ByteArrayInputStream(fileBytes), fileName);

        SendDocument document = new SendDocument();
        document.setChatId(String.valueOf(chatId));
        document.setDocument(inputFile);

        try {
            execute(document);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Ошибка при отправке документа: " + e.getMessage(), e);
        }
    }
    private void sendRetryButton(long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createButton("Повторить поиск", "retry"));

        rows.add(row);
        markup.setKeyboard(rows);

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Вы можете повторить поиск, нажав на кнопку ниже:");
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }
}
