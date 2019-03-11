package sg.gov.dsta.mobileC3.ventilo.activity.report.incident;

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
import android.widget.Spinner;
import android.widget.TextView;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.report.ReportStatePagerAdapter;
import sg.gov.dsta.mobileC3.ventilo.util.ReportSpinnerBank;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2LatoBlackButton;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2LatoBlackTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2LatoItalicLightEditTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;

public class IncidentInnerDetailFragment extends Fragment {

    private C2LatoBlackTextView mTvTitleHeader;
    private C2LatoItalicLightEditTextView mEtvTitleDetail;
    private C2LatoBlackTextView mTvDescriptionHeader;
    private C2LatoItalicLightEditTextView mEtvDescriptionDetail;

    private Spinner mSpinnerDropdownTitle;

    private C2LatoBlackButton mBtnReport;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_inner_incident_detail, container, false);
        initUI(rootView);

        return rootView;
    }

    private void initUI(View rootView) {
        mTvTitleHeader = rootView.findViewById(R.id.tv_incident_title_header);
        String titleHeader = getString(R.string.incident_title_header).concat(":");
        mTvTitleHeader.setText(titleHeader);

        mEtvTitleDetail = rootView.findViewById(R.id.etv_incident_title_detail);
        mEtvTitleDetail.addTextChangedListener(titleDetailTextWatcher);
        String titleDetailHint = getString(R.string.incident_title_detail_hint);
        mEtvTitleDetail.setHint(titleDetailHint);
        mEtvTitleDetail.setVisibility(View.GONE);

        mTvDescriptionHeader = rootView.findViewById(R.id.tv_incident_description_header);
        String descriptionHeader = getString(R.string.incident_description_header).concat(":");
        mTvDescriptionHeader.setText(descriptionHeader);

        mEtvDescriptionDetail = rootView.findViewById(R.id.etv_incident_description_detail);
        mEtvDescriptionDetail.addTextChangedListener(descriptionDetailTextWatcher);
        String descriptionDetailDisabledHint = getString(R.string.incident_description_detail_hint);
        mEtvDescriptionDetail.setHint(descriptionDetailDisabledHint);

        initTitleSpinner(rootView);

        mBtnReport = rootView.findViewById(R.id.btn_incident_detail_report);
        mBtnReport.setOnClickListener(onConfirmClickListener);
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
        mSpinnerDropdownTitle = rootView.findViewById(R.id.spinner_incident_title_detail);
        String[] titleDetailStringArray = ReportSpinnerBank.getInstance(getActivity()).getIncidentTitleList();

        ArrayAdapter<String> adapter = new ArrayAdapter(getActivity(),
                R.layout.spinner_row_incident_title, R.id.text_item_incident_title_detail, titleDetailStringArray) {

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
                TextView tv = view.findViewById(R.id.text_item_incident_title_detail);
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
            if (validateIncident()) {
                IncidentInnerFragment incidentInnerFragment = (IncidentInnerFragment) ReportStatePagerAdapter.getPageReferenceMap().
                        get(FragmentConstants.REPORT_TAB_TITLE_INCIDENT_ID);

                if(incidentInnerFragment != null) {
                    String titleDetail;
                    if (mSpinnerDropdownTitle.getSelectedItemPosition() ==
                            mSpinnerDropdownTitle.getAdapter().getCount() - 1) {
                        titleDetail = mEtvTitleDetail.getText().toString().trim();
                    } else {
                        titleDetail = mSpinnerDropdownTitle.getSelectedItem().toString();
                    }

                    String descriptionDetail = mEtvDescriptionDetail.getText().toString().trim();
                    incidentInnerFragment.getAdapter().addItem(titleDetail, descriptionDetail);
                    getFragmentManager().popBackStack();
                }
            };
        }
    };

    private boolean validateIncident() {
        String titleDetail = mEtvTitleDetail.getText().toString().trim();
        String descriptionDetail = mEtvDescriptionDetail.getText().toString().trim();
        boolean isValidateSuccess = true;

        if (mEtvTitleDetail.getVisibility() == View.VISIBLE && TextUtils.isEmpty(titleDetail)) {
            mEtvTitleDetail.requestFocus();
            mEtvTitleDetail.setError(getString(R.string.error_empty_incident_title_detail));
            isValidateSuccess = false;
        }

        if (TextUtils.isEmpty(descriptionDetail)) {
            if (isValidateSuccess) {
                mEtvDescriptionDetail.requestFocus();
            }

            mEtvDescriptionDetail.setError(getString(R.string.error_empty_incident_description_detail));
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
        String defaultValue = FragmentConstants.VALUE_INCIDENT_ADD;
        Bundle bundle = this.getArguments();

        if (bundle != null) {
            fragmentType = bundle.getString(FragmentConstants.KEY_INCIDENT, defaultValue);
        } else {
            fragmentType = defaultValue;
        }

        if (fragmentType.equalsIgnoreCase(FragmentConstants.VALUE_INCIDENT_VIEW)) {
            if (bundle != null) {
                if (mEtvTitleDetail != null) {
                    String title = bundle.getString(
                            FragmentConstants.KEY_INCIDENT_TITLE, FragmentConstants.DEFAULT_STRING);
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
                            FragmentConstants.KEY_INCIDENT_DESCRIPTION, FragmentConstants.DEFAULT_STRING);
                    mEtvDescriptionDetail.setText(description);
                    mEtvDescriptionDetail.setEnabled(false);
                }
            }

            mBtnReport.setVisibility(View.GONE);
        } else {
            resetToDefaultUI();
        }
    }

//    TextView text;
//
//    // TODO: Rename parameter arguments, choose names that match
//    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//    private static String TEXT_VALUE = "textValue";
//
//    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;
//
//
//
//    private OnFragmentInteractionListener mListener;
//
//    public IncidentFragment() {
//        // Required empty public constructor
//    }
//
//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment IncidentFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static IncidentFragment newInstance(String param1, String param2) {
//        IncidentFragment fragment = new IncidentFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//
//            final IncidentFragmentArgs incidentFragmentArgs = IncidentFragmentArgs.fromBundle(getArguments());
//            TEXT_VALUE = incidentFragmentArgs.getData();
//        }
//    }
//
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        View view = (View) inflater
//                .inflate(R.layout.fragment_incident, container, false);
//        ButterKnife.bind(this, view);
//
//        //Sample to display data from map fragment
//        text.setText(TEXT_VALUE);
//        return view;
//    }

//    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onIncidentFragmentInteraction(uri);
//        }
//    }
//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onIncidentFragmentInteraction(Uri uri);
//    }

    @Override
    public void onResume() {
        super.onResume();

        refreshUI();
    }
}
