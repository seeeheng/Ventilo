package sg.gov.dsta.mobileC3.ventilo.util;

import android.content.res.Resources;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.main.MainActivity;
import sg.gov.dsta.mobileC3.ventilo.activity.sitrep.SitRepAddFragment;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansSemiBoldTextView;

/**
 * Utility class for Snackbar
 */
public class SnackbarUtil {

    /**
     * Creates a custom layout without action button of Android's default Snackbar and displays it
     *
     * @param parentView        - Parent view of attached
     * @param snackbarView      - Snackbar view (Child view)
     * @param snackbarMessage   - Message to display
     */
    public static void showCustomSnackbar(View parentView, View snackbarView,
                                          String snackbarMessage, String snackbarActionText,
                                          SnackbarActionClickListener snackbarActionClickListener) {
        Snackbar snackbar;

        // Create the Snackbar
        if ("".equalsIgnoreCase(snackbarActionText)) {
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

        if (snackbarView.getParent() != null) {
            ((ViewGroup) snackbarView.getParent()).removeView(snackbarView);
        }

        C2OpenSansRegularTextView tvSnackbarMessage = snackbarView.findViewById(R.id.tv_snackbar_message);
        tvSnackbarMessage.setText(snackbarMessage);

        C2OpenSansSemiBoldTextView tvSnackbarAction = snackbarView.findViewById(R.id.tv_snackbar_action);
        if (!"".equalsIgnoreCase(snackbarActionText)) {
            tvSnackbarAction.setVisibility(View.VISIBLE);
            tvSnackbarAction.setText(snackbarActionText);

            tvSnackbarAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (snackbarActionClickListener != null) {
                        snackbar.dismiss();
                        snackbarActionClickListener.onSnackbarActionClick();
                    }
                }
            });
        } else {
            tvSnackbarAction.setVisibility(View.GONE);
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
    };

    /**
     * Displays snackbar without an action button at the bottom right corner (No snackbarActionClickListener)
     * @param parentView
     * @param snackbarView
     * @param snackbarMessage
     */
    public static void showCustomSnackbarWithoutAction(View parentView, View snackbarView, String snackbarMessage) {
        showCustomSnackbar(parentView, snackbarView, snackbarMessage, "", null);
    }

    /**
     * Displays snackbar with an action button at the bottom right corner (With snackbarActionClickListener)
     * @param parentView
     * @param snackbarView
     * @param snackbarMessage
     * @param snackbarActionText
     * @param snackbarActionClickListener
     */
    public static void showCustomSnackbarWithAction(View parentView, View snackbarView, String snackbarMessage,
                                                    String snackbarActionText,
                                                    SnackbarActionClickListener snackbarActionClickListener) {
        showCustomSnackbar(parentView, snackbarView, snackbarMessage, snackbarActionText, snackbarActionClickListener);
    }

    public interface SnackbarActionClickListener {
        void onSnackbarActionClick();
    }
}