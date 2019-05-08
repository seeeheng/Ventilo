package sg.gov.dsta.mobileC3.ventilo.util;

import android.text.TextUtils;

import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansItalicLightEditTextView;

public class ValidationUtil {

    public static boolean isNumberField(String value) {
        String regexStr = "^[0-9]*$";

        if(value.trim().matches(regexStr)) {
            return true;
        }
        else {
            return false;
        }
    }

    public static boolean validateEditTextField(C2OpenSansItalicLightEditTextView etv, String errorString) {
        if (etv != null) {
            String urlLinkDetail = etv.getText().toString().trim();

            if (TextUtils.isEmpty(urlLinkDetail)) {
                etv.requestFocus();
                etv.setError(errorString);

                return false;
            }

            return true;
        }

        return false;
    }

    public static String getFirstWord(String text) {
        int index = text.indexOf(' ');
        if (index > -1) {                       // Check if there is more than one word
            return text.substring(0, index);    // Extract first word
        } else {
            return text;                        // Text is the first word itself
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
}
