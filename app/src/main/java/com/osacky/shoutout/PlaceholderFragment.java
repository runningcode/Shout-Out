package com.osacky.shoutout;

import android.support.v4.app.Fragment;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

import static com.osacky.shoutout.Constants.ARG_SECTION_NUMBER;

@EFragment(R.layout.fragment_main)
public class PlaceholderFragment extends Fragment {

    @FragmentArg(ARG_SECTION_NUMBER)
    int sectionNumber;

    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity) getActivity()).onSectionAttached(sectionNumber);
    }
}
