package sg.gov.dsta.mobileC3.ventilo.activity.sitrep;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.model.join.UserSitRepJoinModel;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.SitRepViewModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.UserSitRepJoinViewModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.UserViewModel;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQPublisher;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.GsonCreator;
import sg.gov.dsta.mobileC3.ventilo.util.PhotoCaptureUtil;
import sg.gov.dsta.mobileC3.ventilo.util.ReportSpinnerBank;
import sg.gov.dsta.mobileC3.ventilo.util.SnackbarUtil;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.ValidationUtil;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansSemiBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;

public class SitRepAddFragment extends Fragment implements SnackbarUtil.SnackbarActionClickListener {

    private static final String TAG = SitRepAddFragment.class.getSimpleName();

    // View Models
    private UserViewModel mUserViewModel;
    private SitRepViewModel mSitRepViewModel;
    private UserSitRepJoinViewModel mUserSitRepJoinViewModel;

    // Main
    private LinearLayout mMainLayout;

    // Toolbar section
    private LinearLayout mLinearLayoutBtnSendOrUpdate;
    private C2OpenSansSemiBoldTextView mTvToolbarSendOrUpdate;

    // Header Title section
    private C2OpenSansBoldTextView mTvHeaderTitle;

    // Picture section
    private AppCompatImageView mImgPhotoGallery;
    private AppCompatImageView mImgOpenCamera;
    private FrameLayout mFrameLayoutPicture;
    private SubsamplingScaleImageView mImgPicture;
    private AppCompatImageView mImgClose;
    private String mImageFileAbsolutePath;
    private Bitmap mCompressedFileBitmap;

    // Location section
    private Spinner mSpinnerLocation;

    // Activity section
    private RelativeLayout mLayoutActivity;
    private Spinner mSpinnerActivity;

    // Personnel section
    private LinearLayout mLayoutPersonnel;
    private C2OpenSansRegularTextView mTvPersonnelNumberT;
    private C2OpenSansRegularTextView mTvPersonnelNumberS;
    private C2OpenSansRegularTextView mTvPersonnelNumberD;

    // Next coa section
    private RelativeLayout mLayoutNextCoa;
    private Spinner mSpinnerNextCoa;

    // Request section
    private RelativeLayout mLayoutRequest;
    private Spinner mSpinnerRequest;

    // Snackbar
    private View mViewSnackbar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_sitrep, container, false);
        observerSetup();
        initUI(rootView);

        return rootView;
    }

    private void observerSetup() {
        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        mSitRepViewModel = ViewModelProviders.of(this).get(SitRepViewModel.class);
        mUserSitRepJoinViewModel = ViewModelProviders.of(this).get(UserSitRepJoinViewModel.class);
    }

    /**
     * Initialise UIs of every section
     *
     * @param rootView
     */
    private void initUI(View rootView) {
        mMainLayout = rootView.findViewById(R.id.layout_sitrep_add_fragment);

        initToolbarUI(rootView);
        initCallsignTitleUI(rootView);
        initPicUI(rootView);
        initLocationUI(rootView);
        initActivityUI(rootView);
        initPersonnelUI(rootView);
        initNextCoaUI(rootView);
        initRequestUI(rootView);
        initSnackbar();

//        initLayouts(rootView);
//        initSpinners(rootView);
    }

    private void initToolbarUI(View rootView) {
        View layoutToolbar = rootView.findViewById(R.id.layout_toolbar_sitrep_text_left_text_right);
        layoutToolbar.setClickable(true);

        LinearLayout linearLayoutBtnBack = layoutToolbar.findViewById(R.id.layout_toolbar_top_left_btn);
        linearLayoutBtnBack.setOnClickListener(onBackClickListener);
        mLinearLayoutBtnSendOrUpdate = layoutToolbar.findViewById(R.id.layout_toolbar_top_right_btn);
        mLinearLayoutBtnSendOrUpdate.setEnabled(false);

        mTvToolbarSendOrUpdate = layoutToolbar.findViewById(R.id.toolbar_top_right_btn_text);
        mTvToolbarSendOrUpdate.setTextColor(ResourcesCompat.getColor(getResources(),
                R.color.primary_text_hint_dark_grey, null));
        mTvToolbarSendOrUpdate.setText(getString(R.string.btn_send));

        if (getString(R.string.btn_send).equalsIgnoreCase(
                mTvToolbarSendOrUpdate.getText().toString().trim())) {
            mLinearLayoutBtnSendOrUpdate.setOnClickListener(onSendClickListener);
        } else {
            mLinearLayoutBtnSendOrUpdate.setOnClickListener(onUpdateClickListener);
        }
    }

    private void initCallsignTitleUI(View rootView) {
        C2OpenSansSemiBoldTextView tvCallsignTitle = rootView.findViewById(R.id.tv_sitrep_callsign);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        StringBuilder callsignTitleBuilder = new StringBuilder();
        callsignTitleBuilder.append(pref.getString(SharedPreferenceConstants.CALLSIGN_USER,
                SharedPreferenceConstants.DEFAULT_STRING));
        callsignTitleBuilder.append(StringUtil.SPACE);
        callsignTitleBuilder.append(getString(R.string.sitrep_callsign_header));

        tvCallsignTitle.setText(callsignTitleBuilder.toString());
    }

    private void initPicUI(View rootView) {
        mImgPhotoGallery = rootView.findViewById(R.id.img_sitrep_photo_gallery);
        mImgOpenCamera = rootView.findViewById(R.id.img_sitrep_open_camera);
        mFrameLayoutPicture = rootView.findViewById(R.id.layout_sitrep_picture);
        mImgPicture = rootView.findViewById(R.id.img_sitrep_picture);
        mImgClose = rootView.findViewById(R.id.img_sitrep_picture_close);

        mImgPhotoGallery.setOnClickListener(onPhotoGalleryClickListener);
        mImgOpenCamera.setOnClickListener(onOpenCameraClickListener);
        mImgClose.setOnClickListener(onPictureCloseClickListener);
    }

    private void initLocationUI(View rootView) {
        initLocationSpinner(rootView);
    }

    private void initActivityUI(View rootView) {
        mLayoutActivity = rootView.findViewById(R.id.layout_sitrep_activity);
        mLayoutActivity.setVisibility(View.GONE);
        initActivitySpinner(rootView);
    }

    private void initPersonnelUI(View rootView) {
        mLayoutPersonnel = rootView.findViewById(R.id.layout_sitrep_personnel);
        mLayoutPersonnel.setVisibility(View.GONE);

        View layoutContainerPersonnelT = rootView.findViewById(R.id.layout_sitrep_personnel_T);
        View layoutContainerPersonnelS = rootView.findViewById(R.id.layout_sitrep_personnel_S);
        View layoutContainerPersonnelD = rootView.findViewById(R.id.layout_sitrep_personnel_D);

        initPersonnelTextViewsUI(layoutContainerPersonnelT, layoutContainerPersonnelS, layoutContainerPersonnelD);
        initPersonnelBtns(layoutContainerPersonnelT, layoutContainerPersonnelS, layoutContainerPersonnelD);
    }

    private void initPersonnelTextViewsUI(View layoutContainerPersonnelT, View layoutContainerPersonnelS,
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

    private void initPersonnelBtns(View layoutContainerPersonnelT,
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
    }

    private void initNextCoaUI(View rootView) {
        mLayoutNextCoa = rootView.findViewById(R.id.layout_sitrep_next_coa);
        mLayoutNextCoa.setVisibility(View.GONE);
        initNextCoaSpinner(rootView);
    }

    private void initRequestUI(View rootView) {
        mLayoutRequest = rootView.findViewById(R.id.layout_sitrep_request);
        mLayoutRequest.setVisibility(View.GONE);
        initRequestSpinner(rootView);
    }

    private void initSnackbar() {
        mViewSnackbar = getLayoutInflater().inflate(R.layout.layout_custom_snackbar, null);
    }

    private View.OnClickListener onBackClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.popBackStack();
        }
    };

    private View.OnClickListener onSendClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SnackbarUtil.showCustomSnackbarWithAction(mMainLayout, mViewSnackbar,
                    getString(R.string.snackbar_sitrep_send_confirmation_message),
                    getString(R.string.snackbar_sitrep_confirmation_action),
                    SitRepAddFragment.this);
        }
    };

    private View.OnClickListener onUpdateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SnackbarUtil.showCustomSnackbarWithAction(mMainLayout, mViewSnackbar,
                    getString(R.string.snackbar_sitrep_update_confirmation_message),
                    getString(R.string.snackbar_sitrep_confirmation_action),
                    SitRepAddFragment.this);
        }
    };

    private View.OnClickListener onPhotoGalleryClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent photoGalleryIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(photoGalleryIntent, PhotoCaptureUtil.PHOTO_GALLERY_REQUEST_CODE);
        }
    };

    private View.OnClickListener onOpenCameraClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            openCameraIntent();
        }
    };

    private View.OnClickListener onPictureCloseClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            closeSelectedPictureUI();
        }
    };

    private void openCameraIntent() {
        ContentValues values = new ContentValues();

//        StringBuilder imageFullTitle = new StringBuilder();
//        imageFullTitle.append(getString(R.string.sitrep_picture_general_title));
//        imageFullTitle.append(UNDERSCORE);
//        imageFullTitle.append(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
//        values.put(MediaStore.Images.Media.TITLE, imageFullTitle.toString());
//        values.put(MediaStore.Images.Media.DESCRIPTION, "Captured through ventilo app");
//
//        mPhotoURI = getActivity().getContentResolver().insert(
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//        Intent openCameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoURI);
//        startActivityForResult(openCameraIntent, PhotoCaptureUtil.OPEN_CAMERA_REQUEST_CODE);

        Intent openCameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        if (openCameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            //Create a file to store the image
            File photoFile = null;
            try {
                photoFile = PhotoCaptureUtil.createImageFile(getContext());
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d(TAG, "onOpenCameraClickListener: Error creating file for captured camera shot");
            }
            if (photoFile != null) {
                try {
                    mImageFileAbsolutePath = photoFile.getAbsolutePath();
                    Uri photoURI = FileProvider.getUriForFile(getActivity().getApplicationContext(),
                            "sg.gov.dsta.mobileC3.ventilo.fileprovider", photoFile);
                    openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            photoURI);
                    startActivityForResult(openCameraIntent,
                            PhotoCaptureUtil.OPEN_CAMERA_REQUEST_CODE);
                } catch (NullPointerException ex) {
                    Log.d(TAG, "onOpenCameraClickListener: " + ex.toString());
                } catch (IllegalArgumentException ex) {
                    Log.d(TAG, "onOpenCameraClickListener: " + ex.toString());
                }
            }
        }
    }

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
                mLinearLayoutBtnSendOrUpdate.setEnabled(true);
                mTvToolbarSendOrUpdate.setTextColor(ResourcesCompat.getColor(getResources(),
                        R.color.primary_highlight_cyan, null));
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private void refreshUI() {
        String fragmentType;
        String defaultValue = FragmentConstants.VALUE_SITREP_ADD;
        Bundle bundle = this.getArguments();

        if (bundle != null) {
            fragmentType = bundle.getString(FragmentConstants.KEY_SITREP, defaultValue);
        } else {
            fragmentType = defaultValue;
        }

        if (fragmentType.equalsIgnoreCase(FragmentConstants.VALUE_SITREP_VIEW)) {
            if (bundle != null) {
                // Get Sitrep ID with all fields
                Long sitRepId = bundle.getLong(FragmentConstants.KEY_SITREP_ID, FragmentConstants.DEFAULT_LONG);
                querySitRepBySitRepIdFromDatabase(sitRepId);

//                if (mTvHeaderTitle != null) {
//                    String reporter = bundle.getString(
//                            FragmentConstants.KEY_SITREP_REPORTER, FragmentConstants.DEFAULT_STRING);
//                    StringBuilder sitRepTitleBuilder = new StringBuilder();
//                    sitRepTitleBuilder.append("Team ");
//                    sitRepTitleBuilder.append(reporter);
//                    sitRepTitleBuilder.append(" SITREP");
//                    mTvHeaderTitle.setText(sitRepTitleBuilder.toString().trim());
//                }
//
//                if (mEtvDescriptionDetail != null) {
//                    String description = bundle.getString(
//                            FragmentConstants.KEY_TASK_DESCRIPTION, FragmentConstants.DEFAULT_STRING);
//                    mEtvDescriptionDetail.setText(description);
//                    mEtvDescriptionDetail.setEnabled(false);
//                }
//
//                mSpinnerDropdownTitle.setEnabled(false);
//
//
//                if (mEtvDescriptionDetail != null) {
//                    String description = bundle.getString(
//                            FragmentConstants.KEY_TASK_DESCRIPTION, FragmentConstants.DEFAULT_STRING);
//                    mEtvDescriptionDetail.setText(description);
//                    mEtvDescriptionDetail.setEnabled(false);
//                }
            }

//            mBtnReport.setVisibility(View.GONE);
        } else {
//            resetToDefaultUI();
        }
    }

    private void querySitRepBySitRepIdFromDatabase(long sitRepId) {
        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
        // asynchronously in the background thread and apply changes on the main UI thread
        SingleObserver<SitRepModel> singleObserverSitRep = new SingleObserver<SitRepModel>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(SitRepModel sitRepModel) {
                Log.d(TAG, "onSuccess singleObserverSitRep, " +
                        "querySitRepBySitRepIdFromDatabase. " +
                        "SitRepId: " + sitRepModel.getId());

                if (mTvHeaderTitle != null) {
                    StringBuilder sitRepTitleBuilder = new StringBuilder();
                    sitRepTitleBuilder.append("Team ");
                    sitRepTitleBuilder.append(sitRepModel.getReporter());
                    sitRepTitleBuilder.append(" SITREP");
                    mTvHeaderTitle.setText(sitRepTitleBuilder.toString().trim());
                }

                if (mSpinnerLocation != null) {
//                    mSpinnerLocation.setSelection();
//                    mSpinnerLocation.setEnabled(false);
                }

                if (mTvPersonnelNumberT != null) {
                    mTvPersonnelNumberT.setText(sitRepModel.getPersonnelT());
                }

                if (mTvPersonnelNumberS != null) {
                    mTvPersonnelNumberS.setText(sitRepModel.getPersonnelS());
                }

                if (mTvPersonnelNumberD != null) {
                    mTvPersonnelNumberD.setText(sitRepModel.getPersonnelD());
                }

                if (mSpinnerRequest != null) {
//                    mSpinnerRequest.setSelection();
//                    mSpinnerRequest.setEnabled(false);
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError singleObserverSitRep, " +
                        "querySitRepBySitRepIdFromDatabase. " +
                        "Error Msg: " + e.toString());
            }
        };

        mSitRepViewModel.querySitRepBySitRepId(sitRepId, singleObserverSitRep);
    }

    /**
     * Displays scaled bitmap image (of size 1024px which is not too large) to ensure smooth page loading
     *
     * @param compressedFilePathName
     */
    private void displayScaledBitmap(String compressedFilePathName) {
        Bitmap bitMapThumbnail = BitmapFactory.decodeFile(compressedFilePathName);
        int maxScaledHeight = (int) (bitMapThumbnail.getHeight() *
                (PhotoCaptureUtil.MAX_SCALED_WIDTH_OF_DISPLAY_IN_PIXEL / bitMapThumbnail.getWidth()));
        Bitmap scaledBitMapThumbnail = Bitmap.createScaledBitmap(bitMapThumbnail,
                (int) PhotoCaptureUtil.MAX_SCALED_WIDTH_OF_DISPLAY_IN_PIXEL, maxScaledHeight, true);
        displaySelectedPictureUI(scaledBitMapThumbnail);
    }

    /**
     * Remove selected photo from UI
     */
    private void closeSelectedPictureUI() {
        mFrameLayoutPicture.setVisibility(View.GONE);
        mImgPicture.recycle();
    }

    /**
     * Displays user selected photo in UI
     *
     * @param bitmap
     */
    private void displaySelectedPictureUI(Bitmap bitmap) {
        mFrameLayoutPicture.setVisibility(View.VISIBLE);
        mImgPicture.setImage(ImageSource.bitmap(bitmap));
    }

    /**
     * Create new Sit Rep Model from form
     *
     * @param userId
     * @return
     */
    private SitRepModel createNewSitRepModelFromForm(String userId) {
        SitRepModel newSitRepModel = new SitRepModel();
        newSitRepModel.setReporter(userId);
        byte[] imageByteArray = null;
        if (mCompressedFileBitmap != null) {
            imageByteArray = PhotoCaptureUtil.getByteArrayFromImage(mCompressedFileBitmap, 100);
        }
        newSitRepModel.setSnappedPhoto(imageByteArray);
        newSitRepModel.setLocation(mSpinnerLocation.getSelectedItem().toString());
        newSitRepModel.setActivity(mSpinnerActivity.getSelectedItem().toString());
        newSitRepModel.setPersonnelT(Integer.valueOf(mTvPersonnelNumberT.getText().toString().trim()));
        newSitRepModel.setPersonnelS(Integer.valueOf(mTvPersonnelNumberS.getText().toString().trim()));
        newSitRepModel.setPersonnelD(Integer.valueOf(mTvPersonnelNumberD.getText().toString().trim()));
        newSitRepModel.setNextCoa(mSpinnerNextCoa.getSelectedItem().toString());
        newSitRepModel.setRequest(mSpinnerRequest.getSelectedItem().toString());
        newSitRepModel.setReportedDateTime(DateTimeUtil.getCurrentTime());

        return newSitRepModel;
    }

    /**
     * Broadcasts update of data to connected devices in the network
     *
     * @param sitRepModel
     */
    private void broadcastDataUpdateOverSocket(SitRepModel sitRepModel) {
        // Sit Rep table data
        Gson gson = GsonCreator.createGson();
        String newSitRepModelJson = gson.toJson(sitRepModel);
        JeroMQPublisher.getInstance().sendTaskMessage(newSitRepModelJson, JeroMQPublisher.TOPIC_UPDATE);

        mSitRepViewModel.updateSitRep(sitRepModel);
    }

    /**
     * Broadcasts insertion of data to connected devices in the network
     *
     * @param sitRepModel
     */
    private void broadcastDataInsertionOverSocket(SitRepModel sitRepModel) {
        // Sit Rep table data
        Log.d(TAG, "broadcastDataInsertionOverSocket");
        Gson gson = GsonCreator.createGson();
        String newSitRepModelJson = gson.toJson(sitRepModel);
        JeroMQPublisher.getInstance().sendSitRepMessage(newSitRepModelJson, JeroMQPublisher.TOPIC_INSERT);

        // UserSitRepJoin table data
//        String userSitRepJoinModelJson = gson.toJson(userSitRepJoinModel);
//        JeroMQPublisher.getInstance(new ZContext()).
//                sendUserSitRepJoinMessage(userSitRepJoinModelJson);
    }

    private void addItemToSqliteDatabase(SitRepModel sitRepModel, String userId) {
        // Store Sit Rep data locally
//            mSitRepViewModel.insertSitRep(newSitRepModel);
        SingleObserver<Long> singleObserverAddSitRep = new SingleObserver<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(Long sitRepId) {
                Log.d(TAG, "onSuccess singleObserverAddSitRep, " +
                        "addItemToSqliteDatabase. " +
                        "SitRepId: " + sitRepId);

                sitRepModel.setRefId(sitRepId);
                mSitRepViewModel.updateSitRep(sitRepModel);

                // Store UserSitRepJoin data locally
                UserSitRepJoinModel newUserSitRepJoinModel = new UserSitRepJoinModel(
                        userId, sitRepId);
                mUserSitRepJoinViewModel.addUserSitRepJoin(newUserSitRepJoinModel);

                // Send newly created Sit Rep model to all other devices
                broadcastDataInsertionOverSocket(sitRepModel);

                // Show snackbar message and return to main Sit Rep fragment page
                SnackbarUtil.showCustomSnackbarWithoutAction(mMainLayout, mViewSnackbar,
                        getString(R.string.snackbar_sitrep_sent_message));
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.popBackStack();
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError singleObserverAddTask, addItemToSqliteDatabase. " +
                        "Error Msg: " + e.toString());
            }
        };

        mSitRepViewModel.addSitRep(sitRepModel, singleObserverAddSitRep);
    }

    private void performActionClick() {
        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
        // asynchronously in the background thread and apply changes on the main UI thread
        SingleObserver<UserModel> singleObserverUser = new SingleObserver<UserModel>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(UserModel userModel) {
                Log.d(TAG, "onSuccess singleObserverUser, " +
                        "performActionClick. " +
                        "UserId: " + userModel.getUserId());

                SitRepModel newSitRepModel = createNewSitRepModelFromForm(userModel.getUserId());

                // Send button
                if (getString(R.string.btn_send).equalsIgnoreCase(
                        mTvToolbarSendOrUpdate.getText().toString().trim())) {

                    addItemToSqliteDatabase(newSitRepModel, userModel.getUserId());

                } else {    // Update button
                    broadcastDataUpdateOverSocket(newSitRepModel);
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError singleObserverUser, " +
                        "addSitRepToCompositeTableInDatabase. " +
                        "Error Msg: " + e.toString());
            }
        };

        mUserViewModel.queryUserByAccessToken(SharedPreferenceUtil.getCurrentUserAccessToken(getActivity()),
                singleObserverUser);
    }

    @Override
    public void onSnackbarActionClick() {
        if (mCompressedFileBitmap != null) {
            String compressedFilePathName = PhotoCaptureUtil.storeFileIntoPhoneMemory(mCompressedFileBitmap, 100);
            File compressedBitmapFile = new File(compressedFilePathName);
            getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(compressedBitmapFile)));
        }

        performActionClick();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Gets file from gallery and displays UI
        // Stores reference of bitmap for storage at a later stage, if confirmed
        if (requestCode == PhotoCaptureUtil.PHOTO_GALLERY_REQUEST_CODE) {

            if (resultCode == Activity.RESULT_OK) {
                Uri targetUri = data.getData();
                String filePathName = PhotoCaptureUtil.getRealPathFromURI(getContext(),
                        targetUri, null, null);
                mCompressedFileBitmap = PhotoCaptureUtil.compressImage(getContext(), filePathName);
                displayScaledBitmap(filePathName);

            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getContext(), "Photo gallery picture selection operation cancelled",
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Opens default camera function.
        // Snapped photo will be compressed and stored in gallery before display of UI.
        // Compressed image is then deleted while bitmap reference is kept for storage at a later stage, if confirmed.
        if (requestCode == PhotoCaptureUtil.OPEN_CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                mCompressedFileBitmap = PhotoCaptureUtil.compressImage(getContext(), mImageFileAbsolutePath);
                String filePathName = PhotoCaptureUtil.storeFileIntoPhoneMemory(mCompressedFileBitmap, 80);

                displayScaledBitmap(filePathName);

                File tempCompressedBitmapFile = new File(filePathName);
                if (tempCompressedBitmapFile.exists()) {
                    tempCompressedBitmapFile.delete();
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getContext(), "Photo taking operation cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
