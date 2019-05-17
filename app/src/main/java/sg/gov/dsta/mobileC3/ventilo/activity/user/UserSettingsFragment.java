package sg.gov.dsta.mobileC3.ventilo.activity.user;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.main.MainActivity;
import sg.gov.dsta.mobileC3.ventilo.repository.ExcelSpreadsheetRepository;
import sg.gov.dsta.mobileC3.ventilo.util.SnackbarUtil;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansSemiBoldTextView;

public class UserSettingsFragment extends Fragment implements SnackbarUtil.SnackbarActionClickListener{

    private static final String TAG = UserSettingsFragment.class.getSimpleName();

    // Main
    View mLayoutMain;

    // Image buttons
    View mImgLayoutPullFromExcel;
    View mImgLayoutPushToExcel;
    View mImgLayoutSyncUp;
    View mImgLayoutLogout;

    /**
     * Snackbar Options:
     * 0 - Save Settings,
     * 1 - Pull from Excel,
     * 2 - Push to Excel,
     * 3 - Sync Up,
     * 4 - Logout
     */
    int snackbarOption;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_user_settings, container, false);
//        observerSetup();
        initUI(rootView);

        return rootView;
    }

    private void initUI(View rootView) {
        mLayoutMain = rootView.findViewById(R.id.layout_user_settings);

        initToolbar(rootView);
        initImageButtons(rootView);
    }

    private void initToolbar(View rootView) {
        View layoutToolbar = rootView.findViewById(R.id.layout_toolbar_user_settings_text_left_text_right);
        layoutToolbar.setClickable(true);

        LinearLayout linearLayoutBtnBack = layoutToolbar.findViewById(R.id.layout_toolbar_top_left_btn);
        linearLayoutBtnBack.setOnClickListener(onBackClickListener);
        LinearLayout linearLayoutBtnSave = layoutToolbar.findViewById(R.id.layout_toolbar_top_right_btn);
        linearLayoutBtnSave.setOnClickListener(onSaveClickListener);

        C2OpenSansSemiBoldTextView tvToolbarSave = layoutToolbar.findViewById(R.id.toolbar_top_right_btn_text);
        tvToolbarSave.setText(getString(R.string.btn_save));
        tvToolbarSave.setTextColor(ResourcesCompat.getColor(getResources(),
                R.color.primary_highlight_cyan, null));
    }

    private void initImageButtons(View rootView) {
        mImgLayoutPullFromExcel = rootView.findViewById(R.id.layout_pull_from_excel_text_img_btn);
        LinearLayout layoutPullFromExcel = mImgLayoutPullFromExcel.findViewById(R.id.linear_layout_img_text_img_btn);
        C2OpenSansSemiBoldTextView tvPullFromExcel = mImgLayoutPullFromExcel.findViewById(R.id.tv_text_img_btn);
        AppCompatImageView imgPullFromExcel = mImgLayoutPullFromExcel.findViewById(R.id.img_pic_img_btn);
        layoutPullFromExcel.setBackgroundResource(
                R.drawable.img_btn_background_green_border);
        tvPullFromExcel.setText(getString(R.string.btn_pull_from_excel));
        tvPullFromExcel.setTextColor(ResourcesCompat.getColor(getResources(), R.color.dull_green, null));
        imgPullFromExcel.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                R.drawable.side_nav_menu_sitrep_btn, null));
        imgPullFromExcel.setColorFilter(ContextCompat.getColor(getContext(), R.color.dull_green),
                PorterDuff.Mode.SRC_ATOP);

        mImgLayoutPushToExcel = rootView.findViewById(R.id.layout_push_to_excel_img_text_img_btn);
        LinearLayout layoutPushToExcel = mImgLayoutPushToExcel.findViewById(R.id.linear_layout_img_text_img_btn);
        C2OpenSansSemiBoldTextView tvPushToExcel = mImgLayoutPushToExcel.findViewById(R.id.tv_text_img_btn);
        AppCompatImageView imgPushToExcel = mImgLayoutPushToExcel.findViewById(R.id.img_pic_img_btn);
        layoutPushToExcel.setBackground(ResourcesCompat.getDrawable(getResources(),
                R.drawable.img_btn_background_green_border, null));
        tvPushToExcel.setText(getString(R.string.btn_push_to_excel));
        tvPushToExcel.setTextColor(ResourcesCompat.getColor(getResources(), R.color.dull_green, null));
        imgPushToExcel.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                R.drawable.side_nav_menu_sitrep_btn, null));
        imgPushToExcel.setColorFilter(ContextCompat.getColor(getContext(), R.color.dull_green),
                PorterDuff.Mode.SRC_ATOP);

        mImgLayoutSyncUp = rootView.findViewById(R.id.layout_sync_img_text_img_btn);
        LinearLayout layoutSyncUp = mImgLayoutSyncUp.findViewById(R.id.linear_layout_img_text_img_btn);
        C2OpenSansSemiBoldTextView tvPullSyncUp = mImgLayoutSyncUp.findViewById(R.id.tv_text_img_btn);
        AppCompatImageView imgPullSyncUp = mImgLayoutSyncUp.findViewById(R.id.img_pic_img_btn);
        layoutSyncUp.setBackground(ResourcesCompat.getDrawable(getResources(),
                R.drawable.img_btn_background_cyan_border, null));
        tvPullSyncUp.setText(getString(R.string.btn_sync_up));
        tvPullSyncUp.setTextColor(ResourcesCompat.getColor(getResources(), R.color.primary_highlight_cyan, null));
        imgPullSyncUp.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                R.drawable.icon_sync, null));
        imgPullSyncUp.setColorFilter(ContextCompat.getColor(getContext(), R.color.primary_highlight_cyan),
                PorterDuff.Mode.SRC_ATOP);

        mImgLayoutLogout = rootView.findViewById(R.id.layout_logout_img_text_img_btn);
        LinearLayout layoutLogout = mImgLayoutLogout.findViewById(R.id.linear_layout_img_text_img_btn);
        C2OpenSansSemiBoldTextView tvLogout = mImgLayoutLogout.findViewById(R.id.tv_text_img_btn);
        AppCompatImageView imgLogout = mImgLayoutLogout.findViewById(R.id.img_pic_img_btn);
        layoutLogout.setBackground(ResourcesCompat.getDrawable(getResources(),
                R.drawable.img_btn_background_red_border, null));
        tvLogout.setText(getString(R.string.btn_logout));
        tvLogout.setTextColor(ResourcesCompat.getColor(getResources(), R.color.dull_red, null));
        imgLogout.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                R.drawable.icon_logout, null));
        imgLogout.setColorFilter(ContextCompat.getColor(getContext(), R.color.dull_red),
                PorterDuff.Mode.SRC_ATOP);

        mImgLayoutPullFromExcel.setOnClickListener(onPullFromExcelClickListener);
        mImgLayoutPushToExcel.setOnClickListener(onPushToExcelClickListener);
        mImgLayoutSyncUp.setOnClickListener(onSyncUpClickListener);
        mImgLayoutLogout.setOnClickListener(onLogoutClickListener);
    }

    private View.OnClickListener onBackClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.i(TAG, "Back button pressed.");
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.popBackStack();
        }
    };

    private View.OnClickListener onSaveClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // TODO
            if (getSnackbarView() != null) {
                snackbarOption = 0;
                SnackbarUtil.showCustomAlertSnackbar(mLayoutMain, getSnackbarView(),
                        getString(R.string.snackbar_settings_save_message),
                        UserSettingsFragment.this);
            }
        }
    };

    private View.OnClickListener onPullFromExcelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (getSnackbarView() != null) {
                snackbarOption = 1;
                SnackbarUtil.showCustomAlertSnackbar(mLayoutMain, getSnackbarView(),
                        getString(R.string.snackbar_settings_pull_from_excel_message),
                        UserSettingsFragment.this);
                pullDataFromExcelToDatabase();
            }
        }
    };

    private View.OnClickListener onPushToExcelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (getSnackbarView() != null) {
                snackbarOption = 2;
                SnackbarUtil.showCustomAlertSnackbar(mLayoutMain, getSnackbarView(),
                        getString(R.string.snackbar_settings_push_to_excel_message),
                        UserSettingsFragment.this);
                pushToExcelFromDatabase();
            }
        }
    };

    private View.OnClickListener onSyncUpClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (getSnackbarView() != null) {
                snackbarOption = 3;
                SnackbarUtil.showCustomAlertSnackbar(mLayoutMain, getSnackbarView(),
                        getString(R.string.snackbar_settings_sync_up_message),
                        UserSettingsFragment.this);
            }
        }
    };

    private View.OnClickListener onLogoutClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (getSnackbarView() != null) {
                snackbarOption = 4;
                SnackbarUtil.showCustomAlertSnackbar(mLayoutMain, getSnackbarView(),
                        getString(R.string.snackbar_settings_logout_message),
                        UserSettingsFragment.this);
            }
        }
    };

    private View getSnackbarView() {
        if (getActivity() instanceof MainActivity) {
            return ((MainActivity) getActivity()).getSnackbarView();
        } else {
            return null;
        }
    }

    private void pullDataFromExcelToDatabase() {
        Log.i(TAG, "Pulling data from Excel to Database...");
        ExcelSpreadsheetRepository excelSpreadsheetRepository =
                new ExcelSpreadsheetRepository();
        excelSpreadsheetRepository.pullDataFromExcelToDatabase();
    }

    private void pushToExcelFromDatabase() {
        Log.i(TAG, "Pushing data to Excel from Database...");
        ExcelSpreadsheetRepository excelSpreadsheetRepository =
                new ExcelSpreadsheetRepository();
        excelSpreadsheetRepository.pushDataToExcelFromDatabase();
    }

    @Override
    public void onSnackbarActionClick() {
        switch (snackbarOption) {
            case 0: // Save Settings
                break;

            case 1: // Pull from Excel
                pullDataFromExcelToDatabase();
                break;

            case 2: // Push to Excel
                pushToExcelFromDatabase();
                break;

            case 3: // Sync Up
                break;

            case 4: // Logout
                break;

            default:
        }
    }
}
