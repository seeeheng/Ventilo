package sg.gov.dsta.mobileC3.ventilo.util;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularTextView;

public class ProgressBarUtil {

    private static AlertDialog mAlertDialog;

    /**
     * Creates Progress Dialog; Requires Theme.Appcompat theme activity/fragment context
     *
     * @param context
     */
    public static void createProgressDialog(Context context) {

        int linearLayoutPadding = 30;
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setPadding(0, linearLayoutPadding,
                0, linearLayoutPadding);
        linearLayout.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams linearLayoutParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        linearLayoutParam.gravity = Gravity.CENTER;
        linearLayout.setLayoutParams(linearLayoutParam);

        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);
        progressBar.setPadding(0, 0, linearLayoutPadding, 0);
        progressBar.setLayoutParams(linearLayoutParam);

        linearLayoutParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayoutParam.gravity = Gravity.CENTER;
        C2OpenSansRegularTextView tvText = new C2OpenSansRegularTextView(context);
        tvText.setText(context.getString(R.string.progress_bar_loading_data));
        tvText.setTextColor(ContextCompat.getColor(context, R.color.primary_white));
        tvText.setTextSize(context.getResources().getDimension(R.dimen.text_view_extra_small_text_size));
        tvText.setLayoutParams(linearLayoutParam);

        linearLayout.addView(progressBar);
        linearLayout.addView(tvText);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setView(linearLayout);

        mAlertDialog = builder.create();
        mAlertDialog.show();
        Window window = mAlertDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(mAlertDialog.getWindow().getAttributes());
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            window.setAttributes(layoutParams);
            window.setBackgroundDrawable(context.getDrawable(R.drawable.progress_dialog_background_half_opacity));
        }
    }

    public static void dismissProgressDialog() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }
}
