package sg.gov.dsta.mobileC3.ventilo.activity.sitrep;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.util.ReportSpinnerBank;
import sg.gov.dsta.mobileC3.ventilo.util.ValidationUtil;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansBlackButton;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansSemiBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;

public class SitRepAddFragment extends Fragment {

    private static final String TAG = SitRepAddFragment.class.getSimpleName();

    // Layouts
    private RelativeLayout mLayoutActivity;
    private LinearLayout mLayoutPersonnel;
    private RelativeLayout mLayoutNextCoa;
    private RelativeLayout mLayoutRequest;

    // Text views
    private C2OpenSansRegularTextView mTvPersonnelNumberT;
    private C2OpenSansRegularTextView mTvPersonnelNumberS;
    private C2OpenSansRegularTextView mTvPersonnelNumberD;

    // Buttons
    private C2OpenSansBlackButton mBtnConfirm;

    // Spinners
    private Spinner mSpinnerLocation;
    private Spinner mSpinnerActivity;
    private Spinner mSpinnerNextCoa;
    private Spinner mSpinnerRequest;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_sitrep, container, false);
        initUI(rootView);

        return rootView;
    }

    private void initUI(View rootView) {
        initLayouts(rootView);
        initSpinners(rootView);
    }

    private void initLayouts(View rootView) {
        View layoutToolbar = rootView.findViewById(R.id.layout_toolbar_sitrep_text_left_text_right);
        LinearLayout linearLayoutBtnBack = layoutToolbar.findViewById(R.id.layout_toolbar_top_left_btn);
        linearLayoutBtnBack.setOnClickListener(onBackClickListener);
        LinearLayout linearLayoutBtnDone = layoutToolbar.findViewById(R.id.layout_toolbar_top_right_btn);
        linearLayoutBtnDone.setOnClickListener(onDoneClickListener);

        C2OpenSansSemiBoldTextView tvToolbarDone = layoutToolbar.findViewById(R.id.toolbar_top_right_btn_text);
        tvToolbarDone.setText(getString(R.string.btn_send));

        mLayoutActivity = rootView.findViewById(R.id.layout_sitrep_activity);
        mLayoutPersonnel = rootView.findViewById(R.id.layout_sitrep_personnel);
        mLayoutNextCoa = rootView.findViewById(R.id.layout_sitrep_next_coa);
        mLayoutRequest = rootView.findViewById(R.id.layout_sitrep_request);

        mLayoutActivity.setVisibility(View.GONE);
        mLayoutPersonnel.setVisibility(View.GONE);
        mLayoutNextCoa.setVisibility(View.GONE);
        mLayoutRequest.setVisibility(View.GONE);

        View layoutContainerPersonnelT = rootView.findViewById(R.id.layout_sitrep_personnel_T);
        View layoutContainerPersonnelS = rootView.findViewById(R.id.layout_sitrep_personnel_S);
        View layoutContainerPersonnelD = rootView.findViewById(R.id.layout_sitrep_personnel_D);

        initTextViews(layoutContainerPersonnelT, layoutContainerPersonnelS, layoutContainerPersonnelD);
        initButtons(rootView, layoutContainerPersonnelT, layoutContainerPersonnelS, layoutContainerPersonnelD);
    }

    private void initTextViews(View layoutContainerPersonnelT, View layoutContainerPersonnelS,
                               View layoutContainerPersonnelD) {
        C2OpenSansSemiBoldTextView tvPersonnelT = layoutContainerPersonnelT.findViewById(R.id.tv_sitrep_personnel_type);
        C2OpenSansSemiBoldTextView tvPersonnelS = layoutContainerPersonnelS.findViewById(R.id.tv_sitrep_personnel_type);
        C2OpenSansSemiBoldTextView tvPersonnelD = layoutContainerPersonnelD.findViewById(R.id.tv_sitrep_personnel_type);

        tvPersonnelT.setText(getString(R.string.sitrep_T));
        tvPersonnelS.setText(getString(R.string.sitrep_S));
        tvPersonnelD.setText(getString(R.string.sitrep_D));

        mTvPersonnelNumberT = layoutContainerPersonnelT.findViewById(R.id.tv_sitrep_personnel_number);
        mTvPersonnelNumberS = layoutContainerPersonnelS.findViewById(R.id.tv_sitrep_personnel_number);
        mTvPersonnelNumberD = layoutContainerPersonnelD.findViewById(R.id.tv_sitrep_personnel_number);

        mTvPersonnelNumberT.setText("0");
        mTvPersonnelNumberS.setText("0");
        mTvPersonnelNumberD.setText("0");
    }

    private void initButtons(View rootView, View layoutContainerPersonnelT,
                             View layoutContainerPersonnelS, View layoutContainerPersonnelD) {
        AppCompatImageView imgPersonnelAddT = layoutContainerPersonnelT.findViewById(R.id.img_personnel_add);
        imgPersonnelAddT.setOnClickListener(onAddTClickListener);
        AppCompatImageView imgPersonnelReduceT = layoutContainerPersonnelT.findViewById(R.id.img_personnel_reduce);
        imgPersonnelReduceT.setOnClickListener(onReduceTClickListener);

        AppCompatImageView imgPersonnelAddS = layoutContainerPersonnelS.findViewById(R.id.img_personnel_add);
        imgPersonnelAddS.setOnClickListener(onAddSClickListener);
        AppCompatImageView imgPersonnelReduceS = layoutContainerPersonnelS.findViewById(R.id.img_personnel_reduce);
        imgPersonnelReduceS.setOnClickListener(onReduceSClickListener);

        AppCompatImageView imgPersonnelAddD = layoutContainerPersonnelD.findViewById(R.id.img_personnel_add);
        imgPersonnelAddD.setOnClickListener(onAddDClickListener);
        AppCompatImageView imgPersonnelReduceD = layoutContainerPersonnelD.findViewById(R.id.img_personnel_reduce);
        imgPersonnelReduceD.setOnClickListener(onReduceDClickListener);

        mBtnConfirm = rootView.findViewById(R.id.btn_sitrep_confirm);
        mBtnConfirm.setOnClickListener(onConfirmClickListener);
        mBtnConfirm.setVisibility(View.GONE);
    }

    private View.OnClickListener onBackClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.popBackStack();
        }
    };

    private View.OnClickListener onDoneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.popBackStack();
        }
    };

    private View.OnClickListener onAddTClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mTvPersonnelNumberT.getText() != null && ValidationUtil.isNumberField(mTvPersonnelNumberT.getText().toString())) {
                int newValue = Integer.valueOf(mTvPersonnelNumberT.getText().toString()) + 1;
                mTvPersonnelNumberT.setText(String.valueOf(newValue));
            }
        }
    };

    private View.OnClickListener onReduceTClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mTvPersonnelNumberT.getText() != null && ValidationUtil.isNumberField(mTvPersonnelNumberT.getText().toString())
                    && Integer.valueOf(mTvPersonnelNumberT.getText().toString()) > 0) {
                int newValue = Integer.valueOf(mTvPersonnelNumberT.getText().toString()) - 1;
                mTvPersonnelNumberT.setText(String.valueOf(newValue));
            }
        }
    };

    private View.OnClickListener onAddSClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mTvPersonnelNumberS.getText() != null && ValidationUtil.isNumberField(mTvPersonnelNumberS.getText().toString())) {
                int newValue = Integer.valueOf(mTvPersonnelNumberS.getText().toString()) + 1;
                mTvPersonnelNumberS.setText(String.valueOf(newValue));
            }
        }
    };

    private View.OnClickListener onReduceSClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mTvPersonnelNumberS.getText() != null && ValidationUtil.isNumberField(mTvPersonnelNumberS.getText().toString())
                    && Integer.valueOf(mTvPersonnelNumberS.getText().toString()) > 0) {
                int newValue = Integer.valueOf(mTvPersonnelNumberS.getText().toString()) - 1;
                mTvPersonnelNumberS.setText(String.valueOf(newValue));
            }
        }
    };

    private View.OnClickListener onAddDClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mTvPersonnelNumberD.getText() != null && ValidationUtil.isNumberField(mTvPersonnelNumberD.getText().toString())) {
                int newValue = Integer.valueOf(mTvPersonnelNumberD.getText().toString()) + 1;
                mTvPersonnelNumberD.setText(String.valueOf(newValue));
            }
        }
    };

    private View.OnClickListener onReduceDClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mTvPersonnelNumberD.getText() != null && ValidationUtil.isNumberField(mTvPersonnelNumberD.getText().toString())
                    && Integer.valueOf(mTvPersonnelNumberD.getText().toString()) > 0) {
                int newValue = Integer.valueOf(mTvPersonnelNumberD.getText().toString()) - 1;
                mTvPersonnelNumberD.setText(String.valueOf(newValue));
            }
        }
    };

    private void initSpinners(View rootView) {
        initLocationSpinner(rootView);
        initActivitySpinner(rootView);
        initNextCoaSpinner(rootView);
        initRequestSpinner(rootView);
    }

    private ArrayAdapter<String> getSpinnerArrayAdapter(String[] stringArray) {
        ArrayAdapter<String> adapter = new ArrayAdapter(getActivity(),
                R.layout.spinner_row_sitrep, R.id.tv_spinner_sitrep_text, stringArray) {

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
                TextView tv = view.findViewById(R.id.tv_spinner_sitrep_text);
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.primary_text_white, null));
                }
                return view;
            }
        };

        return adapter;
    }

    private void initLocationSpinner(View rootView) {
        View viewSpinnerLocation = rootView.findViewById(R.id.spinner_sitrep_location);
        mSpinnerLocation = viewSpinnerLocation.findViewById(R.id.spinner_broad);
        String[] locationStringArray = ReportSpinnerBank.getInstance(getActivity()).getLocationList();

        mSpinnerLocation.setAdapter(getSpinnerArrayAdapter(locationStringArray));
        mSpinnerLocation.setOnItemSelectedListener(getLocationSpinnerItemSelectedListener);
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
                mSpinnerActivity.setEnabled(true);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private void initActivitySpinner(View rootView) {
        View viewSpinnerActivity = rootView.findViewById(R.id.spinner_sitrep_activity);
        mSpinnerActivity = viewSpinnerActivity.findViewById(R.id.spinner_broad);
        String[] activityStringArray = ReportSpinnerBank.getInstance(getActivity()).getActivityList();

        mSpinnerActivity.setAdapter(getSpinnerArrayAdapter(activityStringArray));
        mSpinnerActivity.setOnItemSelectedListener(getActivitySpinnerItemSelectedListener);
        mSpinnerActivity.setEnabled(false);
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
                mSpinnerNextCoa.setEnabled(true);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private void initNextCoaSpinner(View rootView) {
        View viewSpinnerNextCoa = rootView.findViewById(R.id.spinner_sitrep_next_coa);
        mSpinnerNextCoa = viewSpinnerNextCoa.findViewById(R.id.spinner_broad);
        String[] nextCoaStringArray = ReportSpinnerBank.getInstance(getActivity()).getNextCoaList();

        mSpinnerNextCoa.setAdapter(getSpinnerArrayAdapter(nextCoaStringArray));
        mSpinnerNextCoa.setOnItemSelectedListener(getNextCoaSpinnerItemSelectedListener);
        mSpinnerNextCoa.setEnabled(false);
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
                mSpinnerRequest.setEnabled(true);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private void initRequestSpinner(View rootView) {
        View viewSpinnerRequest = rootView.findViewById(R.id.spinner_sitrep_request);
        mSpinnerRequest = viewSpinnerRequest.findViewById(R.id.spinner_broad);
        String[] requestStringArray = ReportSpinnerBank.getInstance(getActivity()).getRequestList();

        mSpinnerRequest.setAdapter(getSpinnerArrayAdapter(requestStringArray));
        mSpinnerRequest.setOnItemSelectedListener(getRequestSpinnerItemSelectedListener);
        mSpinnerRequest.setEnabled(false);
    }

    private AdapterView.OnItemSelectedListener getRequestSpinnerItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String selectedItemText = (String) parent.getItemAtPosition(position);
            // If user change the default selection
            // First item is disable and it is used for hint
            if (position > 0) {
                // Notify the selected item text
                mBtnConfirm.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private View.OnClickListener onConfirmClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Fragment sitRepInnerDetailFragment = new SitRepDetailFragment();
            Bundle bundle = new Bundle();
            bundle.putString(FragmentConstants.KEY_SITREP_LOCATION, mSpinnerLocation.getSelectedItem().toString());
            bundle.putString(FragmentConstants.KEY_SITREP_ACTIVITY, mSpinnerActivity.getSelectedItem().toString());
            bundle.putInt(FragmentConstants.KEY_SITREP_PERSONNEL_T, Integer.valueOf(mTvPersonnelNumberT.getText().toString().trim()));
            bundle.putInt(FragmentConstants.KEY_SITREP_PERSONNEL_S, Integer.valueOf(mTvPersonnelNumberS.getText().toString().trim()));
            bundle.putInt(FragmentConstants.KEY_SITREP_PERSONNEL_D, Integer.valueOf(mTvPersonnelNumberD.getText().toString().trim()));
            bundle.putString(FragmentConstants.KEY_SITREP_NEXT_COA, mSpinnerNextCoa.getSelectedItem().toString());
            bundle.putString(FragmentConstants.KEY_SITREP_REQUEST, mSpinnerRequest.getSelectedItem().toString());
            sitRepInnerDetailFragment.setArguments(bundle);

            // Pass info to fragment
            FragmentManager fm = getActivity().getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_right,
                    R.anim.slide_in_from_right, R.anim.slide_out_to_right);
            ft.replace(R.id.layout_sitrep_inner_add_fragment, sitRepInnerDetailFragment,
                    sitRepInnerDetailFragment.getClass().getSimpleName());
            ft.addToBackStack(sitRepInnerDetailFragment.getClass().getSimpleName());
            ft.commit();
        }
    };
}
