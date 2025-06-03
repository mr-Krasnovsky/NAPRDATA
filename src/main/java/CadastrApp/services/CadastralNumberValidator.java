package CadastrApp.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CadastralNumberValidator {

    private static final Pattern CADASTRAL_NUMBER_PATTERN =
            Pattern.compile("^(\\d{2}\\.\\d{2}\\.\\d{2}\\.\\d{3}|\\d{2}\\.\\d{2}\\.\\d{2}\\.\\d{3}\\.\\d{3})$");


    public static boolean isValidCadastralNumber(String text) {
        if (text == null) {
            return false;
        }
        Matcher matcher = CADASTRAL_NUMBER_PATTERN.matcher(text);
        return matcher.matches();
    }
}
