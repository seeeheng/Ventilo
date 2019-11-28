package sg.gov.dsta.mobileC3.ventilo.activity.radiolinkstatus;

import android.app.Application;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.UserViewModel;
import sg.gov.dsta.mobileC3.ventilo.model.waverelay.WaveRelayRadioModel;
import sg.gov.dsta.mobileC3.ventilo.repository.WaveRelayRadioRepository;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansLightTextView;
import sg.gov.dsta.mobileC3.ventilo.util.enums.radioLinkStatus.ERadioConnectionStatus;
import timber.log.Timber;

public class RadioLinkStatusFragment extends Fragment {

    private static final String TAG = RadioLinkStatusFragment.class.getSimpleName();

    // View Models
    private UserViewModel mUserViewModel;

    // Main layout
    private View mRootView;

    // UI components
    private C2OpenSansLightTextView mTvOfflineTotal;
    private C2OpenSansLightTextView mTvOnlineTotal;

    // Recycler View
    private RecyclerView mRecyclerViewStatusOffline;
    private RadioLinkStatusOfflineRecyclerAdapter mRecyclerAdapterStatusOffline;
    private RecyclerView.LayoutManager mRecyclerLayoutManagerStatusOffline;

    private List<UserModel> mRadioLinkStatusUserListItems;
    private boolean mIsFragmentVisibleToUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setRetainInstance(true);
        observerSetup();

        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_radio_link_status, container, false);
            initUI(mRootView);
        }

        return mRootView;
    }

    /**
     * Initialise view with recycler data
     *
     * @param rootView
     */
    private void initUI(View rootView) {
        mTvOfflineTotal = rootView.findViewById(R.id.tv_radio_link_status_offline_total);
        mTvOnlineTotal = rootView.findViewById(R.id.tv_radio_link_status_online_total);

        mRecyclerViewStatusOffline = rootView.findViewById(R.id.recycler_ship_blueprint_personnel_status_link);
        mRecyclerViewStatusOffline.setHasFixedSize(true);
        mRecyclerViewStatusOffline.setNestedScrollingEnabled(false);

        mRecyclerLayoutManagerStatusOffline = new LinearLayoutManager(getActivity());
        mRecyclerViewStatusOffline.setLayoutManager(mRecyclerLayoutManagerStatusOffline);

        if (mRadioLinkStatusUserListItems == null) {
            mRadioLinkStatusUserListItems = new ArrayList<>();
        }

        mRecyclerAdapterStatusOffline = new RadioLinkStatusOfflineRecyclerAdapter(getContext(),
                mRadioLinkStatusUserListItems, new ArrayList<>());
        mRecyclerViewStatusOffline.setAdapter(mRecyclerAdapterStatusOffline);
        mRecyclerViewStatusOffline.setItemAnimator(new DefaultItemAnimator());
    }

    /**
     * Set Wave Relay Radio Info model into recycler adapter
     */
    private synchronized void setWaveRelayRadiosInfo() {
        WaveRelayRadioRepository waveRelayRadioRepository = new
                WaveRelayRadioRepository((Application) MainApplication.getAppContext());

        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
        // asynchronously in the background thread
        SingleObserver<List<WaveRelayRadioModel>> singleObserverAllWaveRelayRadio = new
                SingleObserver<List<WaveRelayRadioModel>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        // add it to a CompositeDisposable
                    }

                    @Override
                    public void onSuccess(List<WaveRelayRadioModel> waveRelayRadioModelList) {
                        Timber.i("onSuccess singleObserverAllWaveRelayRadio, setWaveRelayRadiosInfo. waveRelayRadioModel.size(): %d", waveRelayRadioModelList.size());

                        mRecyclerAdapterStatusOffline.setWaveRelayListItems(waveRelayRadioModelList);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.d("onError singleObserverAllWaveRelayRadio, " +
                                "setWaveRelayRadiosInfo. " +
                                "Error Msg: " + e.toString());
                    }
                };

        waveRelayRadioRepository.getAllWaveRelayRadios(singleObserverAllWaveRelayRadio);
    }

    /**
     * Refresh UI with updated data
     */
    private void refreshUI() {
        if (mRadioLinkStatusUserListItems != null) {
            // Extracts list of radio connection status from UserModel
            List<String> radioConnectionStatusList = mRadioLinkStatusUserListItems.stream().map(
                    UserModel::getRadioFullConnectionStatus).collect(Collectors.toList());

            // Get count of number of users offline and online before displaying count
            long noOfUsersOffline = radioConnectionStatusList.stream().filter(status ->
                    status.equalsIgnoreCase(ERadioConnectionStatus.OFFLINE.toString())).count();
            long noOfUsersOnline = radioConnectionStatusList.stream().filter(status ->
                    status.equalsIgnoreCase(ERadioConnectionStatus.ONLINE.toString())).count();

            String offlineTotal = String.valueOf(noOfUsersOffline).concat(StringUtil.TRAILING_SLASH).
                    concat(String.valueOf(mRadioLinkStatusUserListItems.size()));
            String onlineTotal = String.valueOf(noOfUsersOnline).concat(StringUtil.TRAILING_SLASH).
                    concat(String.valueOf(mRadioLinkStatusUserListItems.size()));

            mTvOfflineTotal.setText(offlineTotal);
            mTvOnlineTotal.setText(onlineTotal);
        }
    }

    /**
     * Set up observer for live updates on view models and update UI accordingly
     */
    private void observerSetup() {
        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);

        /*
         * Refreshes recyclerview UI whenever there is a change in user data (insert, update or delete)
         */
        mUserViewModel.getAllUsersLiveData().observe(this, new Observer<List<UserModel>>() {
            @Override
            public void onChanged(@Nullable List<UserModel> userModelList) {

                synchronized (mRadioLinkStatusUserListItems) {
                    if (mRadioLinkStatusUserListItems == null) {
                        mRadioLinkStatusUserListItems = new ArrayList<>();
                    } else {
                        mRadioLinkStatusUserListItems.clear();
                    }

                    if (userModelList != null) {
                        mRadioLinkStatusUserListItems.addAll(userModelList);
                    }

                    if (mRecyclerAdapterStatusOffline != null) {
                        mRecyclerAdapterStatusOffline.setUserListItems(mRadioLinkStatusUserListItems);
                        setWaveRelayRadiosInfo();
                    }

                    refreshUI();
                }
            }
        });
    }

    /**
     * Pops back stack of ONLY current tab
     *
     * @return
     */
    public boolean popBackStack() {
        if (!isAdded())
            return false;

        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStackImmediate();
            return true;
        } else
            return false;
    }

    private void onVisible() {
        Timber.i("onVisible");
        mRecyclerAdapterStatusOffline.notifyDataSetChanged();
    }

    private void onInvisible() {
        Timber.i("onInvisible");

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mIsFragmentVisibleToUser = isVisibleToUser;

        if (isResumed()) { // fragment has been created at this point
            if (mIsFragmentVisibleToUser) {
                Timber.i("setUserVisibleHint onVisible");
                onVisible();
            } else {
                Timber.i("setUserVisibleHint onInvisible");
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
