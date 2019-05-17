package sg.gov.dsta.mobileC3.ventilo.util;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatImageView;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularTextView;

/**
 * Utility class for Snackbar
 */
public class SnackbarUtil {

    /**
     * Creates a custom layout without action button of Android's default Snackbar and displays it
     *
     * @param parentView      - Parent view of attached
     * @param snackbarView    - Snackbar view (Child view)
     * @param snackbarMessage - Message to display
     */
    public static void showCustomSnackbar(View parentView, View snackbarView, Drawable backgroundDrawable,
                                          String snackbarMessage, String snackbarActionOkText,
                                          String snackbarActionCancelText,
                                          SnackbarActionClickListener snackbarActionClickListener) {
        Snackbar snackbar;

        // Create appropriate Snackbar with corresponding display duration
        // Info     - Display LONG duration         (Contains NO action button)
        // Alert    - Display INDEFINITE duration   (Contains 2 action buttons which can be text or image)
        Drawable infoBackgroundDrawable = ResourcesCompat.getDrawable(snackbarView.getContext().getResources(),
                R.drawable.snackbar_info_background, null);
        Drawable alertBackgroundDrawable = ResourcesCompat.getDrawable(snackbarView.getContext().getResources(),
                R.drawable.snackbar_alert_background, null);

        if (DrawableUtil.areDrawablesIdentical(backgroundDrawable, infoBackgroundDrawable)) {
            snackbar = Snackbar.make(parentView, "", Snackbar.LENGTH_LONG);
        } else {
            snackbar = Snackbar.make(parentView, "", Snackbar.LENGTH_INDEFINITE);
        }

        // Get the Snackbar's default layout view
        Snackbar.SnackbarLayout layoutSnackbar = (Snackbar.SnackbarLayout) snackbar.getView();
        layoutSnackbar.setBackgroundColor(Color.TRANSPARENT);

        // Hide the default text
        TextView textView = layoutSnackbar.findViewById(android.support.design.R.id.snackbar_text);
        textView.setVisibility(View.INVISIBLE);

        // Remove default view
        if (snackbarView.getParent() != null) {
            ((ViewGroup) snackbarView.getParent()).removeView(snackbarView);
        }

        // Set appropriate background depending on snackbar type
        AppCompatImageView imgSnackbarBackground = snackbarView.findViewById(R.id.img_snackbar_background);
        imgSnackbarBackground.setBackground(backgroundDrawable);

        // Set snackbar message
        C2OpenSansRegularTextView tvSnackbarMessage = snackbarView.findViewById(R.id.tv_snackbar_message);
        tvSnackbarMessage.setText(snackbarMessage);

        AppCompatImageView imgSnackbarActionOk = snackbarView.findViewById(R.id.img_snackbar_action_ok);
        AppCompatImageView imgSnackbarActionCancel = snackbarView.findViewById(R.id.img_snackbar_action_cancel);

        C2OpenSansRegularTextView tvSnackbarActionOk = snackbarView.findViewById(R.id.tv_snackbar_action_ok);
        C2OpenSansRegularTextView tvSnackbarActionCancel = snackbarView.findViewById(R.id.tv_snackbar_action_cancel);

        // Display relevant UI for action buttons - contains text or is simply an image
        if (DrawableUtil.areDrawablesIdentical(backgroundDrawable, alertBackgroundDrawable)) {

            // Action 'OK'
            if ("".equalsIgnoreCase(snackbarActionOkText)) {
                imgSnackbarActionOk.setVisibility(View.VISIBLE);
                imgSnackbarActionOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (snackbarActionClickListener != null) {
                            snackbar.dismiss();
                            snackbarActionClickListener.onSnackbarActionClick();
                        }
                    }
                });

                tvSnackbarActionOk.setVisibility(View.GONE);
            } else {
                tvSnackbarActionOk.setVisibility(View.VISIBLE);
                tvSnackbarActionOk.setText(snackbarActionOkText);
                tvSnackbarActionOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (snackbarActionClickListener != null) {
                            snackbar.dismiss();
                            snackbarActionClickListener.onSnackbarActionClick();
                        }
                    }
                });

                imgSnackbarActionOk.setVisibility(View.GONE);
            }

            // Action 'Cancel'
            if ("".equalsIgnoreCase(snackbarActionCancelText)) {
                imgSnackbarActionCancel.setVisibility(View.VISIBLE);
                imgSnackbarActionCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                    }
                });

                tvSnackbarActionCancel.setVisibility(View.GONE);
            } else {
                tvSnackbarActionCancel.setVisibility(View.VISIBLE);
                tvSnackbarActionCancel.setText(snackbarActionCancelText);
                tvSnackbarActionCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                    }
                });

                imgSnackbarActionCancel.setVisibility(View.GONE);
            }
        } else {
            imgSnackbarActionOk.setVisibility(View.GONE);
            imgSnackbarActionCancel.setVisibility(View.GONE);
            tvSnackbarActionOk.setVisibility(View.GONE);
            tvSnackbarActionCancel.setVisibility(View.GONE);
        }

        layoutSnackbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });

        layoutSnackbar.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                snackbar.dismiss();
                return true;
            }
        });

        // Add the view to the Snackbar's layout
        layoutSnackbar.addView(snackbarView, 0);

        // Show the Snackbar
        snackbar.show();
    }

    /**
     * Displays info snackbar without action button at the bottom right corner (No snackbarActionClickListener)
     *
     * @param parentView
     * @param snackbarView
     * @param snackbarMessage
     */
    public static void showCustomInfoSnackbar(View parentView, View snackbarView, String snackbarMessage) {
        Drawable backgroundDrawable = ResourcesCompat.getDrawable(snackbarView.getContext().getResources(),
                R.drawable.snackbar_info_background, null);
        showCustomSnackbar(parentView, snackbarView, backgroundDrawable,
                snackbarMessage, "", "", null);
    }

    /**
     * Displays alert snackbar with action image buttons
     *
     * @param parentView
     * @param snackbarView
     * @param snackbarMessage
     * @param snackbarActionClickListener
     */
    public static void showCustomAlertSnackbar(View parentView, View snackbarView, String snackbarMessage,
                                               SnackbarActionClickListener snackbarActionClickListener) {
        showCustomAlertSnackbar(parentView, snackbarView, snackbarMessage,
                "", "", snackbarActionClickListener);
    }

    /**
     * Displays alert snackbar with action buttons at the bottom right corner (With snackbarActionClickListener)
     *
     * @param parentView
     * @param snackbarView
     * @param snackbarMessage
     * @param snackbarActionOkText
     * @param snackbarActionClickListener
     */
    public static void showCustomAlertSnackbar(View parentView, View snackbarView, String snackbarMessage,
                                               String snackbarActionOkText, String snackbarActionCancelText,
                                               SnackbarActionClickListener snackbarActionClickListener) {
        Drawable backgroundDrawable = ResourcesCompat.getDrawable(snackbarView.getContext().getResources(),
                R.drawable.snackbar_alert_background, null);
        showCustomSnackbar(parentView, snackbarView, backgroundDrawable, snackbarMessage,
                snackbarActionOkText, snackbarActionCancelText, snackbarActionClickListener);
    }

    public interface SnackbarActionClickListener {
        void onSnackbarActionClick();
    }
}
