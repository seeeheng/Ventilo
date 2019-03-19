package sg.gov.dsta.mobileC3.ventilo.activity.report.task;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.report.ReportStatePagerAdapter;
import sg.gov.dsta.mobileC3.ventilo.helper.RabbitMQHelper;
import sg.gov.dsta.mobileC3.ventilo.util.ReportSpinnerBank;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansBlackButton;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansBlackTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansItalicLightEditTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;
import sg.gov.dsta.mobileC3.ventilo.util.task.EStatus;

public class TaskInnerDetailFragment extends Fragment {

    private static final String TAG = "TaskInnerDetailFragment";

    private C2OpenSansBlackTextView mTvTitleHeader;
    private C2OpenSansItalicLightEditTextView mEtvTitleDetail;
    private C2OpenSansBlackTextView mTvDescriptionHeader;
    private C2OpenSansItalicLightEditTextView mEtvDescriptionDetail;

    private Spinner mSpinnerDropdownTitle;

    private C2OpenSansBlackButton mBtnReport;

    private boolean mIsVisibleToUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_inner_task_detail, container, false);
        initUI(rootView);

        return rootView;
    }

    private void initUI(View rootView) {
        mTvTitleHeader = rootView.findViewById(R.id.tv_task_title_header);
        String titleHeader = getString(R.string.task_title_header).concat(":");
        mTvTitleHeader.setText(titleHeader);

        initTitleSpinner(rootView);

        mEtvTitleDetail = rootView.findViewById(R.id.etv_task_title_detail);
        mEtvTitleDetail.addTextChangedListener(titleDetailTextWatcher);
        String titleDetailHint = getString(R.string.task_title_detail_hint);
        mEtvTitleDetail.setHint(titleDetailHint);
        mEtvTitleDetail.setVisibility(View.GONE);

        mTvDescriptionHeader = rootView.findViewById(R.id.tv_task_description_header);
        String descriptionHeader = getString(R.string.task_description_header).concat(":");
        mTvDescriptionHeader.setText(descriptionHeader);

        mEtvDescriptionDetail = rootView.findViewById(R.id.etv_task_description_detail);
        mEtvDescriptionDetail.addTextChangedListener(descriptionDetailTextWatcher);
        String descriptionDetailDisabledHint = getString(R.string.task_description_detail_hint);
        mEtvDescriptionDetail.setHint(descriptionDetailDisabledHint);

        mBtnReport = rootView.findViewById(R.id.btn_task_detail_report);
        mBtnReport.setOnClickListener(onReportClickListener);
        mBtnReport.bringToFront();

    }

    private TextWatcher titleDetailTextWatcher = new TextWatcher() {
        boolean isHint = true;

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            if (charSequence.toString().trim().length() == 0) {
                // No text, hint is visible
                isHint = true;
                mEtvTitleDetail.setTextColor(getResources().getColor(R.color.circle_image_border_grey));
                int hintTextSize = (int) (getResources().getDimension(R.dimen.hint_text_size) /
                        getResources().getDisplayMetrics().density);
                mEtvTitleDetail.setTextSize(TypedValue.COMPLEX_UNIT_SP, hintTextSize);

                Typeface tfLatoLightItalic = ResourcesCompat.getFont(getContext(), R.font.lato_light_italic);
                mEtvTitleDetail.setTypeface(tfLatoLightItalic); // setting the font
            } else if (isHint) {
                // Clear EditText empty error (if any)
                mEtvTitleDetail.setError(null);

                // No hint, text is visible
                isHint = false;
                mEtvTitleDetail.setTextColor(getResources().getColor(R.color.background_2nd_level_gray));
                int etTextSize = (int) (getResources().getDimension(R.dimen.edit_text_text_size) /
                        getResources().getDisplayMetrics().density);
                mEtvTitleDetail.setTextSize(TypedValue.COMPLEX_UNIT_SP, etTextSize);

                Typeface tfLatoBlack = ResourcesCompat.getFont(getContext(), R.font.lato_black);
                mEtvTitleDetail.setTypeface(tfLatoBlack); // setting the font
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    private TextWatcher descriptionDetailTextWatcher = new TextWatcher() {
        boolean isHint = true;

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            if (charSequence.toString().trim().length() == 0) {
                // No text, hint is visible
                isHint = true;
                mEtvDescriptionDetail.setTextColor(getResources().getColor(R.color.circle_image_border_grey));
                int hintTextSize = (int) (getResources().getDimension(R.dimen.hint_text_size) /
                        getResources().getDisplayMetrics().density);
                mEtvDescriptionDetail.setTextSize(TypedValue.COMPLEX_UNIT_SP, hintTextSize);

                Typeface tfLatoLightItalic = ResourcesCompat.getFont(getContext(), R.font.lato_light_italic);
                mEtvDescriptionDetail.setTypeface(tfLatoLightItalic);
            } else if (isHint) {
                // Clear EditText empty error (if any)
                mEtvDescriptionDetail.setError(null);

                // No hint, text is visible
                isHint = false;
                mEtvDescriptionDetail.setTextColor(getResources().getColor(R.color.background_2nd_level_gray));
                int etTextSize = (int) (getResources().getDimension(R.dimen.edit_text_text_size) /
                        getResources().getDisplayMetrics().density);
                mEtvDescriptionDetail.setTextSize(TypedValue.COMPLEX_UNIT_SP, etTextSize);

                Typeface tfLatoBlack = ResourcesCompat.getFont(getContext(), R.font.lato_black);
                mEtvDescriptionDetail.setTypeface(tfLatoBlack);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    private void initTitleSpinner(View rootView) {
        mSpinnerDropdownTitle = rootView.findViewById(R.id.spinner_task_title_detail);
        String[] titleDetailStringArray = ReportSpinnerBank.getInstance(getActivity()).getTaskTitleList();

        ArrayAdapter<String> adapter = new ArrayAdapter(getActivity(),
                R.layout.spinner_row_task_title, R.id.text_item_task_title_detail, titleDetailStringArray) {

            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    // Disable the first item from Spinner
                    // First item will be used as hint
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = view.findViewById(R.id.text_item_task_title_detail);
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.primary_text_white, null));
                }
                return view;
            }
        };

        mSpinnerDropdownTitle.setAdapter(adapter);
        mSpinnerDropdownTitle.setOnItemSelectedListener(getTitleSpinnerItemSelectedListener());
    }

    private AdapterView.OnItemSelectedListener getTitleSpinnerItemSelectedListener() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
//                System.out.print("parent.getAdapter().getCount() is " + parent.getAdapter().getCount());
                // If user change the default selection
                // First item is disable and it is used for hint
                if (position == parent.getAdapter().getCount() - 1) {
                    mEtvTitleDetail.setVisibility(View.VISIBLE);

                } else if (position > 0) {
                    // Notify the selected item text
                    mEtvTitleDetail.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
    }

    private View.OnClickListener onReportClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (validateTask()) {
                TaskInnerFragment taskInnerFragment = (TaskInnerFragment) ReportStatePagerAdapter.getPageReferenceMap().
                        get(FragmentConstants.REPORT_TAB_TITLE_TASK_ID);

                if(taskInnerFragment != null) {
                    String titleDetail;
                    if (mSpinnerDropdownTitle.getSelectedItemPosition() ==
                            mSpinnerDropdownTitle.getAdapter().getCount() - 1) {
                        titleDetail = mEtvTitleDetail.getText().toString().trim();
                    } else {
                        titleDetail = mSpinnerDropdownTitle.getSelectedItem().toString();
                    }

                    String descriptionDetail = mEtvDescriptionDetail.getText().toString().trim();
                    publishTaskAdd(titleDetail, descriptionDetail);
//                    taskInnerFragment.refreshData();

                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    fragmentManager.popBackStack();
//                    taskInnerFragment.getAdapter().addItem(titleDetail, descriptionDetail);

                }
            };
        }
    };

    private boolean validateTask() {
        String titleDetail = mEtvTitleDetail.getText().toString().trim();
        String descriptionDetail = mEtvDescriptionDetail.getText().toString().trim();
        boolean isValidateSuccess = true;

        if (mEtvTitleDetail.getVisibility() == View.VISIBLE && TextUtils.isEmpty(titleDetail)) {
            mEtvTitleDetail.requestFocus();
            mEtvTitleDetail.setError(getString(R.string.error_empty_task_title_detail));
            isValidateSuccess = false;
        }

        if (TextUtils.isEmpty(descriptionDetail)) {
            if (isValidateSuccess) {
                mEtvDescriptionDetail.requestFocus();
            }

            mEtvDescriptionDetail.setError(getString(R.string.error_empty_task_description_detail));
            isValidateSuccess = false;
        }

        return isValidateSuccess;
    }

    private void resetToDefaultUI() {
        if (mEtvTitleDetail != null && mEtvTitleDetail.getText().toString().length() > 0) {
            mEtvTitleDetail.setText("");
        }

        if (mEtvDescriptionDetail != null && mEtvDescriptionDetail.getText().toString().length() > 0) {
            mEtvDescriptionDetail.setText("");
        }

        if (mSpinnerDropdownTitle != null) {
            mSpinnerDropdownTitle.setSelection(0);
        }
    }

    private void refreshUI() {
        String fragmentType;
        String defaultValue = FragmentConstants.VALUE_TASK_ADD;
        Bundle bundle = this.getArguments();

        if (bundle != null) {
            fragmentType = bundle.getString(FragmentConstants.KEY_TASK, defaultValue);
        } else {
            fragmentType = defaultValue;
        }

        if (fragmentType.equalsIgnoreCase(FragmentConstants.VALUE_TASK_VIEW)) {
            if (bundle != null) {
                if (mEtvTitleDetail != null) {
                    String title = bundle.getString(
                            FragmentConstants.KEY_TASK_TITLE, FragmentConstants.DEFAULT_STRING);
                    boolean isSpinnerOption = false;

                    for (int i = 0; i < mSpinnerDropdownTitle.getAdapter().getCount(); i++) {
                        if (mSpinnerDropdownTitle.getAdapter().getItem(i).toString().equalsIgnoreCase(title.trim())) {
                            mSpinnerDropdownTitle.setSelection(i);
                            isSpinnerOption = true;
                            break;
                        }
                    }

                    if (!isSpinnerOption) {
                        mSpinnerDropdownTitle.setSelection(mSpinnerDropdownTitle.getAdapter().getCount() - 1);
                        mEtvTitleDetail.setText(title);
                        mEtvTitleDetail.setVisibility(View.VISIBLE);
                        mEtvTitleDetail.setEnabled(false);
                    } else {
                        mEtvTitleDetail.setText("");
                        mEtvTitleDetail.setVisibility(View.GONE);
                    }

                    mSpinnerDropdownTitle.setEnabled(false);
                }

                if (mEtvDescriptionDetail != null) {
                    String description = bundle.getString(
                            FragmentConstants.KEY_TASK_DESCRIPTION, FragmentConstants.DEFAULT_STRING);
                    mEtvDescriptionDetail.setText(description);
                    mEtvDescriptionDetail.setEnabled(false);
                }
            }

            mBtnReport.setVisibility(View.GONE);
        } else {
            resetToDefaultUI();
        }
    }

    private void publishTaskAdd(String titleDetail, String descriptionDetail) {
        Bundle bundle = this.getArguments();
        int numberOfTasks = bundle.getInt(
                FragmentConstants.KEY_TASK_TOTAL_NUMBER, FragmentConstants.DEFAULT_INT);

        JSONObject newTaskJSON = new JSONObject();
        try {
            newTaskJSON.put("key", FragmentConstants.KEY_TASK_ADD);
            newTaskJSON.put("id", String.valueOf(numberOfTasks));
            newTaskJSON.put("assigner", SharedPreferenceUtil.getCurrentUser(getActivity()));
            newTaskJSON.put("assignee", "George (A33)");
            newTaskJSON.put("assigneeAvatarId", "default");
            newTaskJSON.put("title", titleDetail);
            newTaskJSON.put("description", descriptionDetail);
            newTaskJSON.put("status", EStatus.NEW.toString());
            newTaskJSON.put("date", String.valueOf(Calendar.getInstance().getTime()));

        } catch (JSONException e) {
            e.printStackTrace();
        }

//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        SharedPreferences.Editor editor = pref.edit();
//
//        String totalNumberOfTasksKey = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.TASK_TOTAL_NUMBER);
//        editor.putInt(totalNumberOfTasksKey, numberOfTasks + 1);
//
//        String taskInitials = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.HEADER_TASK).concat(SharedPreferenceConstants.SEPARATOR).
//                concat(String.valueOf(numberOfTasks));
//
//        editor.putInt(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_TASK_ID), numberOfTasks);
//        editor.putString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNER), "King (K44)");
//        editor.putString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNEE), "George (A33)");
//        editor.putInt(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNEE_AVATAR_ID), R.drawable.default_soldier_icon);
//        editor.putString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_TASK_TITLE), titleDetail);
//        editor.putString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_TASK_DESCRIPTION), descriptionDetail);
//        editor.putString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_TASK_STATUS), EStatus.NEW.toString());
//        editor.putString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_TASK_DATE), String.valueOf(Calendar.getInstance().getTime()));
//
//        editor.apply();

//        MqttHelper.getInstance().publishMessage(newTaskJSON.toString());
        RabbitMQHelper.getInstance().sendMessage(newTaskJSON.toString());
    }

//    private void onVisible() {
//
//    }
//
//    // Remove all fragments from back stack once this fragment is invisible (user navigates to other tabs)
//    private void onInvisible() {
//        System.out.println("TaskInner Invisible");
//        int count = getFragmentManager().getBackStackEntryCount();
//        while (count > 0) {
//            getFragmentManager().popBackStack();
//            count = getFragmentManager().getBackStackEntryCount();
//        }
//    }

    @Override
    public void onResume() {
        super.onResume();

        refreshUI();

//        if (mIsVisibleToUser) {
//            onVisible();
//        }
    }

    @Override
    public void onPause() {
        super.onPause();

//        if (mIsVisibleToUser) {
//            onInvisible();
//        }
    }

//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        mIsVisibleToUser = isVisibleToUser;
//        System.out.println("TaskInner Visible");
//        if (isResumed()) { // fragment has been created at this point
//            if (mIsVisibleToUser) {
//                onVisible();
//            } else {
//                onInvisible();
//            }
//        }
//    }
}
