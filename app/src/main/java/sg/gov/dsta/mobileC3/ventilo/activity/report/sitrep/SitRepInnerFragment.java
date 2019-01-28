package sg.gov.dsta.mobileC3.ventilo.activity.report.sitrep;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.util.ReportSpinnerBank;
import sg.gov.dsta.mobileC3.ventilo.util.ValidationUtil;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2LatoLightEditTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2LatoRegularTextView;

public class SitRepInnerFragment extends Fragment {

    // Linear Layouts
    private LinearLayout mLayoutLocation;
    private LinearLayout mLayoutActivity;
    private LinearLayout mLayoutPersonnel;
    private LinearLayout mLayoutNextCoa;
    private LinearLayout mLayoutRequest;

    // Text Views
    private C2LatoRegularTextView mTvTeam;
    private C2LatoRegularTextView mTvLocation;
    private C2LatoRegularTextView mTvActivity;
    private C2LatoRegularTextView mTvPersonnel;
    private C2LatoRegularTextView mTvNextCoa;
    private C2LatoRegularTextView mTvRequest;

    // Edit Texts
    private C2LatoLightEditTextView mEtvT;
    private C2LatoLightEditTextView mEtvS;
    private C2LatoLightEditTextView mEtvD;

    // Buttons
    private CircleImageView mCircleBtnAddT;
    private CircleImageView mCircleBtnReduceT;
    private CircleImageView mCircleBtnAddS;
    private CircleImageView mCircleBtnReduceS;
    private CircleImageView mCircleBtnAddD;
    private CircleImageView mCircleBtnReduceD;
    private ImageButton mImgBtnConfirm;

    // Spinners
    private Spinner mSpinnerDropdownLocation;
    private Spinner mSpinnerDropdownActivity;
    private Spinner mSpinnerDropdownNextCoa;
    private Spinner mSpinnerDropdownRequest;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_inner_sitrep, container, false);
        initUI(rootView);

        return rootView;
    }

    private void initUI(View rootView) {
        initLayouts(rootView);
        initTextViews(rootView);
        initEditTextViews(rootView);
        initButtons(rootView);
        initSpinners(rootView);
    }

    private void initLayouts(View rootView) {
        mLayoutLocation = rootView.findViewById(R.id.layout_sitrep_location);
        mLayoutActivity = rootView.findViewById(R.id.layout_sitrep_activity);
        mLayoutPersonnel = rootView.findViewById(R.id.layout_sitrep_personnel);
        mLayoutNextCoa = rootView.findViewById(R.id.layout_sitrep_next_coa);
        mLayoutRequest = rootView.findViewById(R.id.layout_sitrep_request);

        mLayoutActivity.setVisibility(View.GONE);
        mLayoutPersonnel.setVisibility(View.GONE);
        mLayoutNextCoa.setVisibility(View.GONE);
        mLayoutRequest.setVisibility(View.GONE);
    }

    private void initTextViews(View rootView) {
        int teamNo = 1;
        String teamHeader = getString(R.string.sitrep_team).concat(" ").concat(String.valueOf(teamNo));
        mTvTeam = rootView.findViewById(R.id.sitrep_tv_team);
        mTvTeam.setText(teamHeader);

        mTvLocation = rootView.findViewById(R.id.sitrep_tv_location);
        String locationText = getString(R.string.sitrep_location).concat(":");
        mTvLocation.setText(locationText);

        mTvActivity = rootView.findViewById(R.id.sitrep_tv_activity);
        String activityText = getString(R.string.sitrep_activity).concat(":");
        mTvActivity.setText(activityText);

        mTvPersonnel = rootView.findViewById(R.id.sitrep_tv_personnel);
        String personnelText = getString(R.string.sitrep_personnel).concat(":");
        mTvPersonnel.setText(personnelText);

        mTvNextCoa = rootView.findViewById(R.id.sitrep_tv_next_coa);
        String nextCoaText = getString(R.string.sitrep_next_coa).concat(":");
        mTvNextCoa.setText(nextCoaText);

        mTvRequest = rootView.findViewById(R.id.sitrep_tv_request);
        String requestText = getString(R.string.sitrep_request).concat(":");
        mTvRequest.setText(requestText);
    }

    private void initEditTextViews(View rootView) {
        mEtvT = rootView.findViewById(R.id.sitrep_etv_T);
        mEtvS = rootView.findViewById(R.id.sitrep_etv_S);
        mEtvD = rootView.findViewById(R.id.sitrep_etv_D);

        mEtvT.setText("0");
        mEtvS.setText("0");
        mEtvD.setText("0");
    }

    private void initButtons(View rootView) {
        mCircleBtnAddT = rootView.findViewById(R.id.circle_img_view_add_T);
        mCircleBtnAddT.bringToFront();
        mCircleBtnAddT.setOnClickListener(onAddTClickListener);
        mCircleBtnReduceT = rootView.findViewById(R.id.circle_img_view_reduce_T);
        mCircleBtnReduceT.bringToFront();
        mCircleBtnReduceT.setOnClickListener(onReduceTClickListener);

        mCircleBtnAddS = rootView.findViewById(R.id.circle_img_view_add_S);
        mCircleBtnAddS.bringToFront();
        mCircleBtnAddS.setOnClickListener(onAddSClickListener);
        mCircleBtnReduceS = rootView.findViewById(R.id.circle_img_view_reduce_S);
        mCircleBtnReduceS.bringToFront();
        mCircleBtnReduceS.setOnClickListener(onReduceSClickListener);

        mCircleBtnAddD = rootView.findViewById(R.id.circle_img_view_add_D);
        mCircleBtnAddD.bringToFront();
        mCircleBtnAddD.setOnClickListener(onAddDClickListener);
        mCircleBtnReduceD = rootView.findViewById(R.id.circle_img_view_reduce_D);
        mCircleBtnReduceD.bringToFront();
        mCircleBtnReduceD.setOnClickListener(onReduceDClickListener);

        mImgBtnConfirm = rootView.findViewById(R.id.btn_sitrep_confirm);
        mImgBtnConfirm.setOnClickListener(onConfirmClickListener);
        mImgBtnConfirm.setVisibility(View.GONE);
    }

    private View.OnClickListener onAddTClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mEtvT.getText() != null && ValidationUtil.isNumberField(mEtvT.getText().toString())) {
                int newValue = Integer.valueOf(mEtvT.getText().toString()) + 1;
                mEtvT.setText(String.valueOf(newValue));
            }
        }
    };

    private View.OnClickListener onReduceTClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mEtvT.getText() != null && ValidationUtil.isNumberField(mEtvT.getText().toString())
                    && Integer.valueOf(mEtvT.getText().toString()) > 0) {
                int newValue = Integer.valueOf(mEtvT.getText().toString()) - 1;
                mEtvT.setText(String.valueOf(newValue));
            }
        }
    };

    private View.OnClickListener onAddSClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mEtvS.getText() != null && ValidationUtil.isNumberField(mEtvS.getText().toString())) {
                int newValue = Integer.valueOf(mEtvS.getText().toString()) + 1;
                mEtvS.setText(String.valueOf(newValue));
            }
        }
    };

    private View.OnClickListener onReduceSClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mEtvS.getText() != null && ValidationUtil.isNumberField(mEtvS.getText().toString())
                    && Integer.valueOf(mEtvS.getText().toString()) > 0) {
                int newValue = Integer.valueOf(mEtvS.getText().toString()) - 1;
                mEtvS.setText(String.valueOf(newValue));
            }
        }
    };

    private View.OnClickListener onAddDClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mEtvD.getText() != null && ValidationUtil.isNumberField(mEtvD.getText().toString())) {
                int newValue = Integer.valueOf(mEtvD.getText().toString()) + 1;
                mEtvD.setText(String.valueOf(newValue));
            }
        }
    };

    private View.OnClickListener onReduceDClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mEtvD.getText() != null && ValidationUtil.isNumberField(mEtvD.getText().toString())
                    && Integer.valueOf(mEtvD.getText().toString()) > 0) {
                int newValue = Integer.valueOf(mEtvD.getText().toString()) - 1;
                mEtvD.setText(String.valueOf(newValue));
            }
        }
    };

    private void initSpinners(View rootView) {
        initLocationSpinner(rootView);
        initActivitySpinner(rootView);
        initNextCoaSpinner(rootView);
        initRequestSpinner(rootView);
    }

    private void initLocationSpinner(View rootView) {
        mSpinnerDropdownLocation = rootView.findViewById(R.id.spinner_sitrep_location);
        String[] locationStringArray = ReportSpinnerBank.getInstance(getActivity()).getLocationList();

        ArrayAdapter<String> adapter = new ArrayAdapter(getActivity(),
                R.layout.spinner_row_sitrep_location, R.id.text_item_sitrep_location, locationStringArray) {

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
                TextView tv = view.findViewById(R.id.text_item_sitrep_location);
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.primary_text_white, null));
                }
                return view;
            }
        };

        mSpinnerDropdownLocation.setAdapter(adapter);
        mSpinnerDropdownLocation.setOnItemSelectedListener(getLocationSpinnerItemSelectedListener);
    }

    private AdapterView.OnItemSelectedListener getLocationSpinnerItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String selectedItemText = (String) parent.getItemAtPosition(position);
            // If user change the default selection
            // First item is disable and it is used for hint
            if (position > 0) {
                // Notify the selected item text
                mLayoutActivity.setVisibility(View.VISIBLE);
                mLayoutPersonnel.setVisibility(View.VISIBLE);
                mSpinnerDropdownActivity.setEnabled(true);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private void initActivitySpinner(View rootView) {
        mSpinnerDropdownActivity = rootView.findViewById(R.id.spinner_sitrep_activity);
        String[] activityStringArray = ReportSpinnerBank.getInstance(getActivity()).getActivityList();

        ArrayAdapter<String> adapter = new ArrayAdapter(getActivity(),
                R.layout.spinner_row_sitrep_activity, R.id.text_item_sitrep_activity, activityStringArray) {

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
                TextView tv = view.findViewById(R.id.text_item_sitrep_activity);
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.primary_text_white, null));
                }
                return view;
            }
        };

        mSpinnerDropdownActivity.setAdapter(adapter);
        mSpinnerDropdownActivity.setOnItemSelectedListener(getActivitySpinnerItemSelectedListener);
        mSpinnerDropdownActivity.setEnabled(false);
    }

    private AdapterView.OnItemSelectedListener getActivitySpinnerItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String selectedItemText = (String) parent.getItemAtPosition(position);
            // If user change the default selection
            // First item is disable and it is used for hint
            if (position > 0) {
                // Notify the selected item text
                mLayoutNextCoa.setVisibility(View.VISIBLE);
                mSpinnerDropdownNextCoa.setEnabled(true);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private void initNextCoaSpinner(View rootView) {
        mSpinnerDropdownNextCoa = rootView.findViewById(R.id.spinner_sitrep_next_coa);
        String[] nextCoaStringArray = ReportSpinnerBank.getInstance(getActivity()).getNextCoaList();

        ArrayAdapter<String> adapter = new ArrayAdapter(getActivity(),
                R.layout.spinner_row_sitrep_next_coa, R.id.text_item_sitrep_next_coa, nextCoaStringArray) {

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
                TextView tv = view.findViewById(R.id.text_item_sitrep_next_coa);
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.primary_text_white, null));
                }
                return view;
            }
        };

        mSpinnerDropdownNextCoa.setAdapter(adapter);
        mSpinnerDropdownNextCoa.setOnItemSelectedListener(getNextCoaSpinnerItemSelectedListener);
        mSpinnerDropdownNextCoa.setEnabled(false);
    }

    private AdapterView.OnItemSelectedListener getNextCoaSpinnerItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String selectedItemText = (String) parent.getItemAtPosition(position);
            // If user change the default selection
            // First item is disable and it is used for hint
            if (position > 0) {
                // Notify the selected item text
                mLayoutRequest.setVisibility(View.VISIBLE);
                mSpinnerDropdownRequest.setEnabled(true);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private void initRequestSpinner(View rootView) {
        mSpinnerDropdownRequest = rootView.findViewById(R.id.spinner_sitrep_request);
        String[] requestStringArray = ReportSpinnerBank.getInstance(getActivity()).getRequestList();

        ArrayAdapter<String> adapter = new ArrayAdapter(getActivity(),
                R.layout.spinner_row_sitrep_request, R.id.text_item_sitrep_request, requestStringArray) {

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
                TextView tv = view.findViewById(R.id.text_item_sitrep_request);
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.primary_text_white, null));
                }
                return view;
            }
        };

        mSpinnerDropdownRequest.setAdapter(adapter);
        mSpinnerDropdownRequest.setOnItemSelectedListener(getRequestSpinnerItemSelectedListener);
        mSpinnerDropdownRequest.setEnabled(false);
    }

    private AdapterView.OnItemSelectedListener getRequestSpinnerItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String selectedItemText = (String) parent.getItemAtPosition(position);
            // If user change the default selection
            // First item is disable and it is used for hint
            if (position > 0) {
                // Notify the selected item text
                mImgBtnConfirm.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private View.OnClickListener onConfirmClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };
}
