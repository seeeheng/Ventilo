package sg.gov.dsta.mobileC3.ventilo.activity.report.task;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.util.ReportSpinnerBank;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2LatoBlackTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2LatoItalicLightEditTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.ReportFragmentConstants;

public class TaskInnerDetailFragment extends Fragment {

    private C2LatoBlackTextView mTvTitleHeader;
    private C2LatoItalicLightEditTextView mEtvTitleDetail;
    private C2LatoBlackTextView mTvDescriptionHeader;
    private C2LatoItalicLightEditTextView mEtvDescriptionDetail;

    private Spinner mSpinnerDropdownTitle;

    private ImageButton mImgBtnConfirm;

    private enum fragmentType {
        VIEW, ADD
    }

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

        mImgBtnConfirm = rootView.findViewById(R.id.img_btn_task_confirm);
        mImgBtnConfirm.setOnClickListener(onConfirmClickListener);
        mImgBtnConfirm.bringToFront();

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

    private View.OnClickListener onConfirmClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            validateTask();
        }
    };

    private void validateTask() {
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
        }
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
        String defaultValue = ReportFragmentConstants.VALUE_TASK_ADD;
        Bundle bundle = this.getArguments();

        if (bundle != null) {
            fragmentType = bundle.getString(ReportFragmentConstants.KEY_TASK, defaultValue);
        } else {
            fragmentType = defaultValue;
        }

        if (fragmentType.equalsIgnoreCase(ReportFragmentConstants.VALUE_TASK_VIEW)) {
            if (bundle != null) {
                if (mEtvTitleDetail != null) {
                    String title = bundle.getString(
                            ReportFragmentConstants.KEY_TASK_TITLE, ReportFragmentConstants.EMPTY_STRING);
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
                            ReportFragmentConstants.KEY_TASK_DESCRIPTION, ReportFragmentConstants.EMPTY_STRING);
                    mEtvDescriptionDetail.setText(description);
                    mEtvDescriptionDetail.setEnabled(false);
                }
            }

            mImgBtnConfirm.setVisibility(View.GONE);
        } else {
            resetToDefaultUI();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshUI();
    }
}
