package sg.gov.dsta.mobileC3.ventilo.util;

import android.text.TextUtils;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2LatoItalicLightEditTextView;

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

    public static boolean validateEditTextField(C2LatoItalicLightEditTextView etv, String errorString) {
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

}
