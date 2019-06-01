package sg.gov.dsta.mobileC3.ventilo.activity.sitrep;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.main.MainActivity;
import sg.gov.dsta.mobileC3.ventilo.activity.main.MainStatePagerAdapter;
import sg.gov.dsta.mobileC3.ventilo.model.join.UserSitRepJoinModel;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.SitRepViewModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.UserSitRepJoinViewModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.UserViewModel;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQBroadcastOperation;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.PhotoCaptureUtil;
import sg.gov.dsta.mobileC3.ventilo.util.ReportSpinnerBank;
import sg.gov.dsta.mobileC3.ventilo.util.SnackbarUtil;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.ValidationUtil;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularEditTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansSemiBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.DatabaseTableConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.MainNavigationConstants;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;

public class SitRepAddUpdateFragment extends Fragment implements SnackbarUtil.SnackbarActionClickListener {

    private static final String TAG = SitRepAddUpdateFragment.class.getSimpleName();

    // View Models
    private UserViewModel mUserViewModel;
    private SitRepViewModel mSitRepViewModel;
    private UserSitRepJoinViewModel mUserSitRepJoinViewModel;

    // Main
    private FrameLayout mMainLayout;

    // Toolbar section
    private LinearLayout mLinearLayoutBtnSendOrUpdate;
    private C2OpenSansSemiBoldTextView mTvToolbarSendOrUpdate;

    // Header Title section
    private C2OpenSansSemiBoldTextView mTvCallsignTitle;

    // Picture section
    private AppCompatImageView mImgPhotoGallery;
    private AppCompatImageView mImgOpenCamera;
    private FrameLayout mFrameLayoutPicture;
    private SubsamplingScaleImageView mImgPicture;
    private AppCompatImageView mImgClose;
    private String mImageFileAbsolutePath;
    private Bitmap mCompressedFileBitmap;

    // Location section
    private RelativeLayout mLayoutLocationInputOthers;
    private AppCompatImageView mImgLocationInputOthers;
    private Spinner mSpinnerLocation;
    private C2OpenSansRegularEditTextView mEtvLocationOthers;

    // Activity section
    private RelativeLayout mLayoutActivityInputOthers;
    private AppCompatImageView mImgActivityInputOthers;
    private Spinner mSpinnerActivity;
    private C2OpenSansRegularEditTextView mEtvActivityOthers;

    // Personnel section;
    private C2OpenSansRegularTextView mTvPersonnelNumberT;
    private C2OpenSansRegularTextView mTvPersonnelNumberS;
    private C2OpenSansRegularTextView mTvPersonnelNumberD;

    // Next coa section
    private RelativeLayout mLayoutNextCoaInputOthers;
    private AppCompatImageView mImgNextCoaInputOthers;
    private Spinner mSpinnerNextCoa;
    private C2OpenSansRegularEditTextView mEtvNextCoaOthers;

    // Request section
    private RelativeLayout mLayoutRequestInputOthers;
    private AppCompatImageView mImgRequestInputOthers;
    private Spinner mSpinnerRequest;
    private C2OpenSansRegularEditTextView mEtvRequestOthers;

    // Others section
    private C2OpenSansRegularEditTextView mEtvOthers;

    private SitRepModel mSitRepModelToUpdate;

    private boolean mIsFragmentVisibleToUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_update_sitrep, container, false);
        observerSetup();
        initUI(rootView);
        checkBundle();

//        setRetainInstance(true);

        Log.i(TAG, "Fragment view created.");

        return rootView;
    }

    /**
     * Set up observer for live updates on view models and update UI accordingly
     */
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
        mMainLayout = rootView.findViewById(R.id.layout_add_update_sitrep_fragment);

        initToolbarUI(rootView);
        initCallsignTitleUI(rootView);
        initPicUI(rootView);
        initLocationUI(rootView);
        initActivityUI(rootView);
        initPersonnelUI(rootView);
        initNextCoaUI(rootView);
        initRequestUI(rootView);
        initOthers(rootView);

//        initLayouts(rootView);
//        initSpinners(rootView);
    }

    /**
     * Initialise toolbar UI with back (left) and send/update (right) buttons
     *
     * @param rootView
     */
    private void initToolbarUI(View rootView) {
        View layoutToolbar = rootView.findViewById(R.id.layout_toolbar_add_sitrep_text_left_text_right);
        layoutToolbar.setClickable(true);

        LinearLayout linearLayoutBtnBack = layoutToolbar.findViewById(R.id.layout_toolbar_top_left_btn);
        linearLayoutBtnBack.setOnClickListener(onBackClickListener);
        mLinearLayoutBtnSendOrUpdate = layoutToolbar.findViewById(R.id.layout_toolbar_top_right_btn);

        mTvToolbarSendOrUpdate = layoutToolbar.findViewById(R.id.toolbar_top_right_btn_text);
        mTvToolbarSendOrUpdate.setText(getString(R.string.btn_send));
        mLinearLayoutBtnSendOrUpdate.setOnClickListener(onSendClickListener);
    }

    private void initCallsignTitleUI(View rootView) {
        mTvCallsignTitle = rootView.findViewById(R.id.tv_add_update_sitrep_callsign);

        StringBuilder callsignTitleBuilder = new StringBuilder();
        callsignTitleBuilder.append(SharedPreferenceUtil.getCurrentUserCallsignID());
        callsignTitleBuilder.append(StringUtil.SPACE);
        callsignTitleBuilder.append(getString(R.string.sitrep_callsign_header));

        mTvCallsignTitle.setText(callsignTitleBuilder.toString());
    }

    private void initPicUI(View rootView) {
        mImgPhotoGallery = rootView.findViewById(R.id.img_add_update_sitrep_photo_gallery);
        mImgOpenCamera = rootView.findViewById(R.id.img_add_update_sitrep_open_camera);
        mFrameLayoutPicture = rootView.findViewById(R.id.layout_add_update_sitrep_picture);
        mImgPicture = rootView.findViewById(R.id.img_add_update_sitrep_picture);
        mImgClose = rootView.findViewById(R.id.img_add_update_sitrep_picture_close);

        mImgPhotoGallery.setOnClickListener(onPhotoGalleryClickListener);
        mImgOpenCamera.setOnClickListener(onOpenCameraClickListener);
        mImgClose.setOnClickListener(onPictureCloseClickListener);
    }

    private void initLocationUI(View rootView) {
        initInputLocationUI(rootView);
    }

    private void initActivityUI(View rootView) {
        initInputActivity(rootView);
    }

    private void initPersonnelUI(View rootView) {
        View layoutContainerPersonnelT = rootView.findViewById(R.id.layout_add_update_sitrep_personnel_T);
        View layoutContainerPersonnelS = rootView.findViewById(R.id.layout_add_update_sitrep_personnel_S);
        View layoutContainerPersonnelD = rootView.findViewById(R.id.layout_add_update_sitrep_personnel_D);

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
        initInputNextCoa(rootView);
    }

    private void initRequestUI(View rootView) {
        initInputRequest(rootView);
    }

    private View.OnClickListener onBackClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.i(TAG, "Back button pressed.");
            popChildBackStack();
        }
    };

    private View.OnClickListener onSendClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (isFormCompleteForFurtherAction()) {
                if (getSnackbarView() != null) {
                    SnackbarUtil.showCustomAlertSnackbar(mMainLayout, getSnackbarView(),
                            getString(R.string.snackbar_sitrep_send_confirmation_message),
                            SitRepAddUpdateFragment.this);
                }
            }
        }
    };

    private View.OnClickListener onUpdateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (isFormCompleteForFurtherAction()) {
                if (getSnackbarView() != null) {
                    SnackbarUtil.showCustomAlertSnackbar(mMainLayout, getSnackbarView(),
                            getString(R.string.snackbar_sitrep_update_confirmation_message),
                            SitRepAddUpdateFragment.this);
                }
            }
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

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                return super.getView(position, convertView, parent);
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = view.findViewById(R.id.tv_spinner_sitrep_text);
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.primary_white, null));
                }
                return view;
            }
        };

        return adapter;
    }

    /**
     * Initialise Sit Rep input location UI
     * <p>
     * Suppression is to remove warning for overriding OnTouchListener which Android requires proper
     * handling of the performClick() method thereafter, in which the standard UI views all set up to provide
     * blind users with appropriate feedback through Accessibility services. However, in this use case,
     * it is not crucial and does not affect targeted user experience.
     *
     * @param rootView
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initInputLocationUI(View rootView) {
        View viewInputLocation = rootView.findViewById(R.id.layout_add_update_sitrep_input_location);
        mLayoutLocationInputOthers = viewInputLocation.findViewById(R.id.layout_spinner_edittext_input_others_icon);
        mImgLocationInputOthers = viewInputLocation.findViewById(R.id.img_spinner_edittext_input_others_icon);
        mSpinnerLocation = viewInputLocation.findViewById(R.id.spinner_broad);

        // Save Enabled set to false to prevent Android from restoring its state by default
        // (This means that text contained in the last of such component (in this case, the spinner) will
        // be saved and be used to populate all components on refresh-- For setSaveEnabled(true) )
        // This is required as there are some layouts that use the same id for Spinners and EditTextView
        mSpinnerLocation.setSaveEnabled(false);
        mEtvLocationOthers = viewInputLocation.findViewById(R.id.etv_spinner_edittext_others_info);
        mEtvLocationOthers.setSaveEnabled(false);
        mEtvLocationOthers.setOnTouchListener(onViewTouchListener);

        String[] locationStringArray = ReportSpinnerBank.getInstance().getLocationList();

        mLayoutLocationInputOthers.setOnClickListener(onLocationInputOthersClickListener);

        mSpinnerLocation.setAdapter(getSpinnerArrayAdapter(locationStringArray));
        mSpinnerLocation.setOnItemSelectedListener(getLocationSpinnerItemSelectedListener);

        mEtvLocationOthers.setHint(getString(R.string.sitrep_location_hint));
    }

    private View.OnClickListener onLocationInputOthersClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            view.setSelected(!view.isSelected());

            if (view.isSelected()) {
                setLocationInputOthersSelectedUI(view);
            } else {
                setLocationInputOthersUnselectedUI(view);
            }
        }
    };

    private void setLocationInputOthersSelectedUI(View view) {
        view.setBackgroundColor(ResourcesCompat.getColor(getResources(),
                R.color.primary_highlight_cyan, null));
        mImgLocationInputOthers.setColorFilter(ResourcesCompat.getColor(
                getResources(), R.color.background_main_black, null));
        mSpinnerLocation.setVisibility(View.GONE);
        mEtvLocationOthers.setVisibility(View.VISIBLE);
    }

    private void setLocationInputOthersUnselectedUI(View view) {
        view.setBackgroundColor(ResourcesCompat.getColor(getResources(),
                R.color.background_dark_grey, null));
        mImgLocationInputOthers.setColorFilter(null);
        mEtvLocationOthers.setVisibility(View.GONE);
        mSpinnerLocation.setVisibility(View.VISIBLE);
    }

    /**
     * Enable internal vertical scrolling for edit text views where content exceed maximum height
     */
    private View.OnTouchListener onViewTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (view.hasFocus()) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_SCROLL:
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                        return true;
                }
            }
            return false;
        }
    };

    private AdapterView.OnItemSelectedListener getLocationSpinnerItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String selectedItemText = (String) parent.getItemAtPosition(position);
            // If user change the default selection
            // First item is disable and it is used for hint
            if (position > 0) {
                // Notify the selected item text
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    /**
     * Initialise Sit Rep input activity request UI
     * <p>
     * Suppression is to remove warning for overriding OnTouchListener which Android requires proper
     * handling of the performClick() method thereafter, in which the standard UI views all set up to provide
     * blind users with appropriate feedback through Accessibility services. However, in this use case,
     * it is not crucial and does not affect targeted user experience.
     *
     * @param rootView
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initInputActivity(View rootView) {
        View viewInputActivity = rootView.findViewById(R.id.layout_add_update_sitrep_input_activity);
        mLayoutActivityInputOthers = viewInputActivity.findViewById(R.id.layout_spinner_edittext_input_others_icon);
        mImgActivityInputOthers = viewInputActivity.findViewById(R.id.img_spinner_edittext_input_others_icon);
        mSpinnerActivity = viewInputActivity.findViewById(R.id.spinner_broad);
        mSpinnerActivity.setSaveEnabled(false);
        mEtvActivityOthers = viewInputActivity.findViewById(R.id.etv_spinner_edittext_others_info);
        mEtvActivityOthers.setSaveEnabled(false);
        mEtvActivityOthers.setOnTouchListener(onViewTouchListener);

        String[] activityStringArray = ReportSpinnerBank.getInstance().getActivityList();

        mLayoutActivityInputOthers.setOnClickListener(onActivityInputOthersClickListener);

        mSpinnerActivity.setAdapter(getSpinnerArrayAdapter(activityStringArray));
        mSpinnerActivity.setOnItemSelectedListener(getActivitySpinnerItemSelectedListener);

        mEtvActivityOthers.setHint(getString(R.string.sitrep_activity_hint));
    }

    private View.OnClickListener onActivityInputOthersClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            view.setSelected(!view.isSelected());

            if (view.isSelected()) {
                setActivityInputOthersSelectedUI(view);
            } else {
                setActivityInputOthersUnselectedUI(view);
            }
        }
    };

    private void setActivityInputOthersSelectedUI(View view) {
        view.setBackgroundColor(ResourcesCompat.getColor(getResources(),
                R.color.primary_highlight_cyan, null));
        mImgActivityInputOthers.setColorFilter(ResourcesCompat.getColor(
                getResources(), R.color.background_main_black, null));
        mSpinnerActivity.setVisibility(View.GONE);
        mEtvActivityOthers.setVisibility(View.VISIBLE);
    }

    private void setActivityInputOthersUnselectedUI(View view) {
        view.setBackgroundColor(ResourcesCompat.getColor(getResources(),
                R.color.background_dark_grey, null));
        mImgActivityInputOthers.setColorFilter(null);
        mEtvActivityOthers.setVisibility(View.GONE);
        mSpinnerActivity.setVisibility(View.VISIBLE);
    }

    private AdapterView.OnItemSelectedListener getActivitySpinnerItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String selectedItemText = (String) parent.getItemAtPosition(position);
            // If user change the default selection
            // First item is disable and it is used for hint
            if (position > 0) {
                // Notify the selected item text
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    /**
     * Initialise Sit Rep input next course of action UI
     * <p>
     * Suppression is to remove warning for overriding OnTouchListener which Android requires proper
     * handling of the performClick() method thereafter, in which the standard UI views all set up to provide
     * blind users with appropriate feedback through Accessibility services. However, in this use case,
     * it is not crucial and does not affect targeted user experience.
     *
     * @param rootView
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initInputNextCoa(View rootView) {
        View viewInputNextCoa = rootView.findViewById(R.id.layout_add_update_sitrep_input_next_coa);
        mLayoutNextCoaInputOthers = viewInputNextCoa.findViewById(R.id.layout_spinner_edittext_input_others_icon);
        mImgNextCoaInputOthers = viewInputNextCoa.findViewById(R.id.img_spinner_edittext_input_others_icon);
        mSpinnerNextCoa = viewInputNextCoa.findViewById(R.id.spinner_broad);
        mSpinnerNextCoa.setSaveEnabled(false);
        mEtvNextCoaOthers = viewInputNextCoa.findViewById(R.id.etv_spinner_edittext_others_info);
        mEtvNextCoaOthers.setSaveEnabled(false);
        mEtvNextCoaOthers.setOnTouchListener(onViewTouchListener);

        String[] nextCoaStringArray = ReportSpinnerBank.getInstance().getNextCoaList();

        mLayoutNextCoaInputOthers.setOnClickListener(onNextCoaInputOthersClickListener);

        mSpinnerNextCoa.setAdapter(getSpinnerArrayAdapter(nextCoaStringArray));
        mSpinnerNextCoa.setOnItemSelectedListener(getNextCoaSpinnerItemSelectedListener);

        mEtvNextCoaOthers.setHint(getString(R.string.sitrep_next_coa_hint));
    }

    private View.OnClickListener onNextCoaInputOthersClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            view.setSelected(!view.isSelected());

            if (view.isSelected()) {
                setNextCoaInputOthersSelectedUI(view);
            } else {
                setNextCoaInputOthersUnselectedUI(view);
            }
        }
    };

    private void setNextCoaInputOthersSelectedUI(View view) {
        view.setBackgroundColor(ResourcesCompat.getColor(getResources(),
                R.color.primary_highlight_cyan, null));
        mImgNextCoaInputOthers.setColorFilter(ResourcesCompat.getColor(
                getResources(), R.color.background_main_black, null));
        mSpinnerNextCoa.setVisibility(View.GONE);
        mEtvNextCoaOthers.setVisibility(View.VISIBLE);
    }

    private void setNextCoaInputOthersUnselectedUI(View view) {
        view.setBackgroundColor(ResourcesCompat.getColor(getResources(),
                R.color.background_dark_grey, null));
        mImgNextCoaInputOthers.setColorFilter(null);
        mEtvNextCoaOthers.setVisibility(View.GONE);
        mSpinnerNextCoa.setVisibility(View.VISIBLE);
    }

    private AdapterView.OnItemSelectedListener getNextCoaSpinnerItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String selectedItemText = (String) parent.getItemAtPosition(position);
            // If user change the default selection
            // First item is disable and it is used for hint
            if (position > 0) {
                // Notify the selected item text
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    /**
     * Initialise Sit Rep input request UI
     * <p>
     * Suppression is to remove warning for overriding OnTouchListener which Android requires proper
     * handling of the performClick() method thereafter, in which the standard UI views all set up to provide
     * blind users with appropriate feedback through Accessibility services. However, in this use case,
     * it is not crucial and does not affect targeted user experience.
     *
     * @param rootView
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initInputRequest(View rootView) {
        View viewInputRequest = rootView.findViewById(R.id.layout_add_update_sitrep_input_request);
        mLayoutRequestInputOthers = viewInputRequest.findViewById(R.id.layout_spinner_edittext_input_others_icon);
        mImgRequestInputOthers = viewInputRequest.findViewById(R.id.img_spinner_edittext_input_others_icon);
        mSpinnerRequest = viewInputRequest.findViewById(R.id.spinner_broad);
        mSpinnerRequest.setSaveEnabled(false);
        mEtvRequestOthers = viewInputRequest.findViewById(R.id.etv_spinner_edittext_others_info);
        mEtvRequestOthers.setSaveEnabled(false);
        mEtvRequestOthers.setOnTouchListener(onViewTouchListener);

        mLayoutRequestInputOthers.setOnClickListener(onRequestInputOthersClickListener);

        String[] requestStringArray = ReportSpinnerBank.getInstance().getRequestList();
        mSpinnerRequest.setAdapter(getSpinnerArrayAdapter(requestStringArray));
        mSpinnerRequest.setOnItemSelectedListener(getRequestSpinnerItemSelectedListener);

        mEtvRequestOthers.setHint(getString(R.string.sitrep_request_hint));
    }

    private View.OnClickListener onRequestInputOthersClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            view.setSelected(!view.isSelected());

            if (view.isSelected()) {
                setRequestInputOthersSelectedUI(view);
            } else {
                setRequestInputOthersUnselectedUI(view);
            }
        }
    };

    private void setRequestInputOthersSelectedUI(View view) {
        view.setBackgroundColor(ResourcesCompat.getColor(getResources(),
                R.color.primary_highlight_cyan, null));
        mImgRequestInputOthers.setColorFilter(ResourcesCompat.getColor(
                getResources(), R.color.background_main_black, null));
        mSpinnerRequest.setVisibility(View.GONE);
        mEtvRequestOthers.setVisibility(View.VISIBLE);
    }

    private void setRequestInputOthersUnselectedUI(View view) {
        view.setBackgroundColor(ResourcesCompat.getColor(getResources(),
                R.color.background_dark_grey, null));
        mImgRequestInputOthers.setColorFilter(null);
        mEtvRequestOthers.setVisibility(View.GONE);
        mSpinnerRequest.setVisibility(View.VISIBLE);
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

    /**
     * Initialise Sit Rep others UI
     * <p>
     * Suppression is to remove warning for overriding OnTouchListener which Android requires proper
     * handling of the performClick() method thereafter, in which the standard UI views all set up to provide
     * blind users with appropriate feedback through Accessibility services. However, in this use case,
     * it is not crucial and does not affect targeted user experience.
     *
     * @param rootView
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initOthers(View rootView) {
        mEtvOthers = rootView.findViewById(R.id.etv_add_update_sitrep_input_others);
        mEtvOthers.setSaveEnabled(false);
        mEtvOthers.setOnTouchListener(onViewTouchListener);
    }

    /**
     * Get Snackbar view from main activity
     *
     * @return
     */
    private View getSnackbarView() {
        if (getActivity() instanceof MainActivity) {
            return ((MainActivity) getActivity()).getSnackbarView();
        } else {
            return null;
        }
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
        if (mFrameLayoutPicture != null && mImgPicture != null) {
            mFrameLayoutPicture.setVisibility(View.GONE);
            mImgPicture.recycle();
        }
    }

    /**
     * Displays user selected photo in UI
     *
     * @param bitmap
     */
    private void displaySelectedPictureUI(Bitmap bitmap) {
        if (mFrameLayoutPicture != null && mImgPicture != null) {
            mFrameLayoutPicture.setVisibility(View.VISIBLE);
            mImgPicture.setImage(ImageSource.bitmap(bitmap));
        }
    }

    /**
     * Create new Sit Rep Model from form
     *
     * @param userId
     * @return
     */
    private SitRepModel createNewOrUpdateSitRepModelFromForm(String userId, boolean toUpdate) {
        SitRepModel newSitRepModel;

        if (toUpdate) {
            newSitRepModel = mSitRepModelToUpdate;
        } else {
            newSitRepModel = new SitRepModel();
            newSitRepModel.setRefId(DatabaseTableConstants.LOCAL_REF_ID);
        }

        newSitRepModel.setReporter(userId);
        byte[] imageByteArray = null;
        if (mCompressedFileBitmap != null) {
            imageByteArray = PhotoCaptureUtil.getByteArrayFromImage(mCompressedFileBitmap, 100);
        }
        newSitRepModel.setSnappedPhoto(imageByteArray);

        String location = "";
        if (mLayoutLocationInputOthers.isSelected()) {
            location = mEtvLocationOthers.getText().toString().trim();
        } else {
            location = mSpinnerLocation.getSelectedItem().toString();
        }

        String activity = "";
        if (mLayoutActivityInputOthers.isSelected()) {
            activity = mEtvActivityOthers.getText().toString().trim();
        } else {
            activity = mSpinnerActivity.getSelectedItem().toString();
        }

        String nextCoa = "";
        if (mLayoutNextCoaInputOthers.isSelected()) {
            nextCoa = mEtvNextCoaOthers.getText().toString().trim();
        } else {
            nextCoa = mSpinnerNextCoa.getSelectedItem().toString();
        }

        String request = "";
        if (mLayoutRequestInputOthers.isSelected()) {
            request = mEtvRequestOthers.getText().toString().trim();
        } else {
            request = mSpinnerRequest.getSelectedItem().toString();
        }

        String others = "";
        others = mEtvOthers.getText().toString().trim();

        newSitRepModel.setLocation(location);
        newSitRepModel.setActivity(activity);
        newSitRepModel.setPersonnelT(Integer.valueOf(mTvPersonnelNumberT.getText().toString().trim()));
        newSitRepModel.setPersonnelS(Integer.valueOf(mTvPersonnelNumberS.getText().toString().trim()));
        newSitRepModel.setPersonnelD(Integer.valueOf(mTvPersonnelNumberD.getText().toString().trim()));
        newSitRepModel.setNextCoa(nextCoa);
        newSitRepModel.setRequest(request);
        newSitRepModel.setOthers(others);

//        System.out.println("DateTimeFormatter.ISO_ZONED_DATE_TIME.toString() is " + DateTimeFormatter.ISO_ZONED_DATE_TIME.toString());
        newSitRepModel.setCreatedDateTime(DateTimeUtil.getCurrentTime());

        return newSitRepModel;
    }

//    /**
//     * Broadcasts update of data to connected devices in the network
//     *
//     * @param sitRepModel
//     */
//    private void broadcastDataUpdateOverSocket(SitRepModel sitRepModel) {
//        // Sit Rep table data
//        Log.d(TAG, "broadcastDataUpdateOverSocket");
//        Gson gson = GsonCreator.createGson();
//        String newSitRepModelJson = gson.toJson(sitRepModel);
//        JeroMQPublisher.getInstance().sendSitRepMessage(newSitRepModelJson, JeroMQPublisher.TOPIC_UPDATE);
//    }
//
//    /**
//     * Broadcasts insertion of data to connected devices in the network
//     *
//     * @param sitRepModel
//     */
//    private void broadcastDataInsertionOverSocket(SitRepModel sitRepModel) {
//        // Sit Rep table data
//        Log.d(TAG, "broadcastDataInsertionOverSocket");
//        Gson gson = GsonCreator.createGson();
//        String newSitRepModelJson = gson.toJson(sitRepModel);
//        JeroMQPublisher.getInstance().sendSitRepMessage(newSitRepModelJson, JeroMQPublisher.TOPIC_INSERT);
//    }

    /**
     * Stores Sit Rep data locally and broadcasts to other devices
     *
     * @param sitRepModel
     * @param userId
     */
    private void addItemToLocalDatabaseAndBroadcast(SitRepModel sitRepModel, String userId) {

        SingleObserver<Long> singleObserverAddSitRep = new SingleObserver<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(Long sitRepId) {
                Log.d(TAG, "onSuccess singleObserverAddSitRep, " +
                        "addItemToLocalDatabaseAndBroadcast. " +
                        "SitRepId: " + sitRepId);

                sitRepModel.setRefId(sitRepId);
                mSitRepViewModel.updateSitRep(sitRepModel);

                // Store UserSitRepJoin data locally
                UserSitRepJoinModel newUserSitRepJoinModel = new UserSitRepJoinModel(
                        userId, sitRepId);
                mUserSitRepJoinViewModel.addUserSitRepJoin(newUserSitRepJoinModel);

                // Send newly created Sit Rep model to all other devices
                JeroMQBroadcastOperation.broadcastDataInsertionOverSocket(sitRepModel);

                // Show snackbar message and return to main Sit Rep fragment page
                if (getSnackbarView() != null) {
                    SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                            getString(R.string.snackbar_sitrep_sent_message));
                }

                popChildBackStack();
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError singleObserverAddSitRep, addItemToLocalDatabaseAndBroadcast. " +
                        "Error Msg: " + e.toString());
            }
        };

        mSitRepViewModel.insertSitRepWithObserver(sitRepModel, singleObserverAddSitRep);
    }

    /**
     * Validates form before enabling Save/Update button
     *
     * @return
     */
    private boolean isFormCompleteForFurtherAction() {

        // Count used to check that all fields are complete
        // 4 means form is completed, otherwise it is incomplete
        int formCompletedCount = 0;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getString(R.string.snackbar_form_incomplete_message));

        // Validate location field
        if (!mLayoutLocationInputOthers.isSelected()) {
            if (mSpinnerLocation.getSelectedItemPosition() != 0) {
                formCompletedCount++;
            } else {
                stringBuilder.append(System.lineSeparator());
                stringBuilder.append(StringUtil.HYPHEN);
                stringBuilder.append(StringUtil.SPACE);
                stringBuilder.append(getString(R.string.sitrep_location));
            }
        } else {
            if (!TextUtils.isEmpty(mEtvLocationOthers.getText().toString().trim())) {
                formCompletedCount++;
            } else {
                stringBuilder.append(System.lineSeparator());
                stringBuilder.append(StringUtil.HYPHEN);
                stringBuilder.append(StringUtil.SPACE);
                stringBuilder.append(getString(R.string.sitrep_location));
            }
        }

        // Validate activity field
        if (!mLayoutActivityInputOthers.isSelected()) {
            if (mSpinnerActivity.getSelectedItemPosition() != 0) {
                formCompletedCount++;
            } else {
                stringBuilder.append(System.lineSeparator());
                stringBuilder.append(StringUtil.HYPHEN);
                stringBuilder.append(StringUtil.SPACE);
                stringBuilder.append(getString(R.string.sitrep_activity));
            }
        } else {
            if (!TextUtils.isEmpty(mEtvActivityOthers.getText().toString().trim())) {
                formCompletedCount++;
            } else {
                stringBuilder.append(System.lineSeparator());
                stringBuilder.append(StringUtil.HYPHEN);
                stringBuilder.append(StringUtil.SPACE);
                stringBuilder.append(getString(R.string.sitrep_activity));
            }
        }

        // Validate next course of action field
        if (!mLayoutNextCoaInputOthers.isSelected()) {
            if (mSpinnerNextCoa.getSelectedItemPosition() != 0) {
                formCompletedCount++;
            } else {
                stringBuilder.append(System.lineSeparator());
                stringBuilder.append(StringUtil.HYPHEN);
                stringBuilder.append(StringUtil.SPACE);
                stringBuilder.append(getString(R.string.sitrep_next_coa));
            }
        } else {
            if (!TextUtils.isEmpty(mEtvNextCoaOthers.getText().toString().trim())) {
                formCompletedCount++;
            } else {
                stringBuilder.append(System.lineSeparator());
                stringBuilder.append(StringUtil.HYPHEN);
                stringBuilder.append(StringUtil.SPACE);
                stringBuilder.append(getString(R.string.sitrep_next_coa));
            }
        }

        // Validate request field
        if (!mLayoutRequestInputOthers.isSelected()) {
            if (mSpinnerRequest.getSelectedItemPosition() != 0) {
                formCompletedCount++;
            } else {
                stringBuilder.append(System.lineSeparator());
                stringBuilder.append(StringUtil.HYPHEN);
                stringBuilder.append(StringUtil.SPACE);
                stringBuilder.append(getString(R.string.sitrep_request));
            }
        } else {
            if (!TextUtils.isEmpty(mEtvRequestOthers.getText().toString().trim())) {
                formCompletedCount++;
            } else {
                stringBuilder.append(System.lineSeparator());
                stringBuilder.append(StringUtil.HYPHEN);
                stringBuilder.append(StringUtil.SPACE);
                stringBuilder.append(getString(R.string.sitrep_request));
            }
        }

        // Form is incomplete; show snackbar message to fill required fields
        if (formCompletedCount != 4) {
            String fieldsToCompleteMessage = stringBuilder.toString().trim();
            if (getSnackbarView() != null) {
                SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                        fieldsToCompleteMessage);
            }
        } else { // form is complete
            return true;
        }

        return false;
    }

    /**
     * Populates form with Sit Rep model data that is to be updated
     *
     * @param sitRepModel
     */
    private void updateFormData(SitRepModel sitRepModel) {
        if (sitRepModel != null) {
            mTvToolbarSendOrUpdate.setText(getString(R.string.btn_update));
            mTvToolbarSendOrUpdate.setOnClickListener(onUpdateClickListener);

            StringBuilder sitRepTitleBuilder = new StringBuilder();
            sitRepTitleBuilder.append(getString(R.string.team_header));
            sitRepTitleBuilder.append(StringUtil.SPACE);
            sitRepTitleBuilder.append(sitRepModel.getReporter());
            sitRepTitleBuilder.append(StringUtil.SPACE);
            sitRepTitleBuilder.append(getString(R.string.sitrep_callsign_header));
            mTvCallsignTitle.setText(sitRepTitleBuilder.toString().trim());

            // Display selected location information
            String[] locationStringArray = ReportSpinnerBank.getInstance().getLocationList();
            String sitRepLocation = sitRepModel.getLocation();
            boolean isLocationFoundInSpinner = false;
            for (int i = 0; i < locationStringArray.length; i++) {
                if (sitRepLocation.equalsIgnoreCase(locationStringArray[i])) {
                    mSpinnerLocation.setSelection(i);
                    isLocationFoundInSpinner = true;
                    break;
                }
            }

            if (!isLocationFoundInSpinner) {
                mLayoutLocationInputOthers.setSelected(true);
                setLocationInputOthersSelectedUI(mLayoutLocationInputOthers);
                mEtvLocationOthers.setText(sitRepModel.getLocation());
            }

            // Display selected activity information
            String[] activityStringArray = ReportSpinnerBank.getInstance().getActivityList();
            String sitRepActivity = sitRepModel.getActivity();
            boolean isActivityFoundInSpinner = false;
            for (int i = 0; i < activityStringArray.length; i++) {
                if (sitRepActivity.equalsIgnoreCase(activityStringArray[i])) {
                    mSpinnerActivity.setSelection(i);
                    isActivityFoundInSpinner = true;
                    break;
                }
            }

            if (!isActivityFoundInSpinner) {
                mLayoutActivityInputOthers.setSelected(true);
                setActivityInputOthersSelectedUI(mLayoutActivityInputOthers);
                mEtvActivityOthers.setText(sitRepModel.getActivity());
            }

            // Display selected personnel information
            mTvPersonnelNumberT.setText(String.valueOf(sitRepModel.getPersonnelT()));
            mTvPersonnelNumberS.setText(String.valueOf(sitRepModel.getPersonnelS()));
            mTvPersonnelNumberD.setText(String.valueOf(sitRepModel.getPersonnelD()));

            // Display selected next course of action information
            String[] nextCoaStringArray = ReportSpinnerBank.getInstance().getNextCoaList();
            String sitRepNextCoa = sitRepModel.getNextCoa();
            boolean isNextCoaFoundInSpinner = false;
            for (int i = 0; i < nextCoaStringArray.length; i++) {
                if (sitRepNextCoa.equalsIgnoreCase(nextCoaStringArray[i])) {
                    mSpinnerNextCoa.setSelection(i);
                    isNextCoaFoundInSpinner = true;
                    break;
                }
            }

            if (!isNextCoaFoundInSpinner) {
                mLayoutNextCoaInputOthers.setSelected(true);
                setNextCoaInputOthersSelectedUI(mLayoutNextCoaInputOthers);
                mEtvNextCoaOthers.setText(sitRepModel.getNextCoa());
            }

            // Display selected request information
            String[] requestStringArray = ReportSpinnerBank.getInstance().getRequestList();
            String sitRepRequest = sitRepModel.getRequest();
            boolean isRequestFoundInSpinner = false;
            for (int i = 0; i < requestStringArray.length; i++) {
                if (sitRepRequest.equalsIgnoreCase(requestStringArray[i])) {
                    mSpinnerRequest.setSelection(i);
                    isRequestFoundInSpinner = true;
                    break;
                }
            }

            if (!isRequestFoundInSpinner) {
                mLayoutRequestInputOthers.setSelected(true);
                setRequestInputOthersSelectedUI(mLayoutRequestInputOthers);
                mEtvRequestOthers.setText(sitRepModel.getRequest());
            }

            mEtvOthers.setText(sitRepModel.getOthers());
        }
    }

    /**
     * Performs Send or Update Task actions accordingly based on
     */
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

                // Send button
                if (getString(R.string.btn_send).equalsIgnoreCase(
                        mTvToolbarSendOrUpdate.getText().toString().trim())) {

                    // Create new Sit Rep, store in local database and broadcast to other connected devices
                    SitRepModel newSitRepModel = createNewOrUpdateSitRepModelFromForm(userModel.getUserId(),
                            false);
                    addItemToLocalDatabaseAndBroadcast(newSitRepModel, userModel.getUserId());

                } else {    // Update button

                    // Update existing Sit Rep model
                    SitRepModel newSitRepModel = createNewOrUpdateSitRepModelFromForm(userModel.getUserId(),
                            true);

                    // Update local Sit Rep data
                    mSitRepViewModel.updateSitRep(newSitRepModel);

                    // Send updated Sit Rep data to other connected devices
                    JeroMQBroadcastOperation.broadcastDataUpdateOverSocket(newSitRepModel);

                    // Show snackbar message and return to Sit Rep edit fragment page
                    if (getSnackbarView() != null) {
                        SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                                getString(R.string.snackbar_sitrep_updated_message));
                    }

                    popChildBackStack();
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError singleObserverUser, " +
                        "addSitRepToCompositeTableInDatabase. " +
                        "Error Msg: " + e.toString());
            }
        };

        mUserViewModel.queryUserByAccessToken(SharedPreferenceUtil.getCurrentUserAccessToken(),
                singleObserverUser);
    }

    /**
     * Accesses child base fragment of current selected view pager item and remove this fragment
     * from child base fragment's stack.
     * <p>
     * Possible Selected View Pager Item: Sit Rep / Video Stream
     * Child Base Fragment: SitRepFragment / VideoStreamFragment
     */
    private void popChildBackStack() {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = ((MainActivity) getActivity());

            if (mainActivity.getViewPager().getCurrentItem() ==
                    MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID) {
                mainActivity.popChildFragmentBackStack(
                        MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID);
            } else if (mainActivity.getViewPager().getCurrentItem() ==
                    MainNavigationConstants.SIDE_MENU_TAB_VIDEO_STREAM_POSITION_ID) {
                mainActivity.popChildFragmentBackStack(
                        MainNavigationConstants.SIDE_MENU_TAB_VIDEO_STREAM_POSITION_ID);
            }
        }
    }

    private void onVisible() {
        Log.d(TAG, "onVisible");

//        FragmentManager fm = getActivity().getSupportFragmentManager();
//        boolean isFragmentFound = false;
//
//        int count = fm.getBackStackEntryCount();
//
//        // Checks if current fragment exists in Back stack
//        for (int i = 0; i < count; i++) {
//            if (this.getClass().getSimpleName().equalsIgnoreCase(fm.getBackStackEntryAt(i).getName())) {
//                isFragmentFound = true;
//            }
//        }
//
//        // If not found, add to current fragment to Back stack
//        if (!isFragmentFound) {
//            FragmentTransaction ft = fm.beginTransaction();
//            ft.addToBackStack(this.getClass().getSimpleName());
//            ft.commit();
//        }
    }

    private void onInvisible() {
        Log.d(TAG, "onInvisible");
    }

    /**
     * Checks for existing data transferred over by Bundle
     */
    private void checkBundle() {
        // Checks if this current fragment is for creation of new Sit Rep or update of existing Sit Rep
        // Else it is returning a selected
        String fragmentType;
        String defaultValue = FragmentConstants.VALUE_SITREP_ADD;
        Bundle bundle = this.getArguments();

        if (bundle != null) {
            fragmentType = bundle.getString(FragmentConstants.KEY_SITREP, defaultValue);
        } else {
            fragmentType = defaultValue;
        }

        if (fragmentType.equalsIgnoreCase(FragmentConstants.VALUE_SITREP_UPDATE)) {

//            System.out.println("mSitRepModelToUpdate is " + mSitRepModelToUpdate);
//            mSitRepModelToUpdate = EventBus.getDefault().getStickyEvent(SitRepModel.class);
//            SitRepModel sitRepModelToUpdate = null;
            if (getActivity() instanceof MainActivity) {
                Object objectToUpdate = ((MainActivity) getActivity()).
                        getStickyModel(SitRepModel.class.getSimpleName());
                if (objectToUpdate instanceof SitRepModel) {
                    System.out.println("objectToUpdate is " + objectToUpdate);
                    mSitRepModelToUpdate = (SitRepModel) objectToUpdate;
                    System.out.println("mSitRepModelToUpdate is " + mSitRepModelToUpdate);
                }
            }

            updateFormData(mSitRepModelToUpdate);

        } else {
            byte[] selectedImageByteArray = bundle.getByteArray(FragmentConstants.KEY_SITREP_PICTURE);
            if (selectedImageByteArray != null) {
                Bitmap selectedImageBitmap = BitmapFactory.decodeByteArray(selectedImageByteArray, 0,
                        selectedImageByteArray.length);

                if (selectedImageBitmap != null) {
                    displaySelectedPictureUI(selectedImageBitmap);
                }
            }
        }
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

    /**
     * Executes screenshot capture of picture or retrieval of picture from Photo Gallery
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mIsFragmentVisibleToUser = isVisibleToUser;
        if (isResumed()) { // fragment has been created at this point
            if (mIsFragmentVisibleToUser) {
                onVisible();
            } else {
                onInvisible();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mIsFragmentVisibleToUser) {
            onVisible();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mIsFragmentVisibleToUser) {
            onInvisible();
        }
    }
}
