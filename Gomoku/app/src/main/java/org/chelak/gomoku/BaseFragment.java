package org.chelak.gomoku;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Sergey on 31.12.2015.
 */
public abstract class BaseFragment extends Fragment {

    private View fragmentView;

    public abstract int getFragmentId();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(getFragmentId(), container, false);
        return fragmentView;
    }

    public View getFragmentView() {
        return fragmentView;
    }

    public void onPause(Preferences preferences) {
        // do nothing
    }

    @Override
    public void onPause() {
        super.onPause();
        onPause(new Preferences(getActivity()));

    }

    public void onResume(Preferences preferences) {
        // do nothing
    }

    @Override
    public void onResume() {
        super.onResume();
        onResume(new Preferences(getActivity()));
    }

}
