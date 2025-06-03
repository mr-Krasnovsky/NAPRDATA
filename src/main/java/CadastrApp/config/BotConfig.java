package CadastrApp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotConfig {

    @Value("${BOT_NAME}")
    private String botName;

    @Value("${BOT_TOKEN}")
    private String botToken;

    public String getBotName() {
        return botName;
    }

    public String getToken() {
        return botToken;
    }
}
