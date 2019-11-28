package sg.gov.dsta.mobileC3.ventilo.util;

import sg.gov.dsta.mobileC3.ventilo.util.security.RandomString;

public class StringUtil {

    public static final String EMPTY_STRING = "";
    public static final String INVALID_STRING = "-1";
    public static final String N_A = "---";
    public static final String COLON = ":";
    public static final String COMMA = ",";
    public static final String DOT = ".";
    public static final String SPACE = " ";
    public static final String QUESTION_MARK = "?";
    public static final String SINGLE_QUOTATION = "'";
    public static final String TAB = "\t";
    public static final String OPEN_BRACKET = "(";
    public static final String CLOSE_BRACKET = ")";
    public static final String HYPHEN = "-";
    public static final String UNDERSCORE = "_";
    public static final String BACK_ONE_LEVEL_DIRECTORY = "..";
    public static final String TRAILING_SLASH = "/";
    public static final String BACKWARD_SLASH = "\\";
    public static final String DEFAULT_INT = "0";

    // Regex
    public static final String REGEX_EVERYTHING = "*";
    public static final String REGEX_SPACE = "\\s";
    public static final String REGEX_NOT_SPACE = "\\S";
    public static final String REGEX_WORD = "\\w";
    public static final String REGEX_NOT_WORD = "\\W";

    public static String[] removeCommasAndExtraSpaces(String stringToStrip) {
        String regexStringToBeReplaced = SPACE.concat(REGEX_EVERYTHING).
                concat(COMMA).concat(SPACE).concat(REGEX_EVERYTHING);

        if (stringToStrip != null) {
            String stringToStripReplaced = stringToStrip.replaceAll(regexStringToBeReplaced, COMMA);
            String[] strippedStringArray = stringToStripReplaced.split(StringUtil.COMMA);
            return strippedStringArray;
        }

        String[] strippedStringArray = {EMPTY_STRING};
        return strippedStringArray;
    }

    public static String[] removeUnderscores(String stringToStrip) {

        if (stringToStrip != null) {
            String[] strippedStringArray = stringToStrip.split(StringUtil.UNDERSCORE);
            return strippedStringArray;
        }

        String[] strippedStringArray = {EMPTY_STRING};
        return strippedStringArray;
    }

    public static String[] removeTrailingSlashes(String stringToStrip) {

        if (stringToStrip != null) {
            String[] strippedStringArray = stringToStrip.split(StringUtil.TRAILING_SLASH);
            return strippedStringArray;
        }

        String[] strippedStringArray = {EMPTY_STRING};
        return strippedStringArray;
    }

    public static String getFirstWord(String text) {
        int index = text.indexOf(' ');
        if (index > -1) {                       // Check if there is more than one word
            return text.substring(0, index);    // Extract first word
        } else {
            return text;                        // Text is the first word itself
        }
    }

    public static String getLastWord(String text) {
        int index = text.lastIndexOf(' ');
        if (index > -1) {                       // Check if there is more than one word
            return text.substring(index + 1);   // Extract last word
        } else {
            return text;                        // Text is the word itself
        }
    }

    public static String removeFirstWord(String text) {
        int index = text.indexOf(' ');
        if (index > -1) {                       // Check if there is more than one word
            return text.substring(index + 1);   // Remove first word
        } else {
            return "";                          // Remaining text is empty after removing first word
        }
    }

    public static String removeFirstWordForMoreThanOneWord(String text) {
        int index = text.indexOf(' ');
        if (index > -1) {                       // Check if there is more than one word
            return text.substring(index + 1);   // Remove first word
        } else {
            return text;                        // Returned text is the original text
                                                // as there is only one word
        }
    }

    public static String getLastChar(String text) {
        return String.valueOf(text.charAt(text.length() - 1));
    }

    public static String getStringAfterLastIndexOfChar(String text, String lastIndexOfChar) {
        return text.substring(text.lastIndexOf(lastIndexOfChar) + 1);
    }

    public static String generateRandomString() {
        RandomString randomString = new RandomString();
        return randomString.nextString();
    }
}