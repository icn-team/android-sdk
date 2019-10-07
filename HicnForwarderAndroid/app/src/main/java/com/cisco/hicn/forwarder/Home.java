package com.cisco.hicn.forwarder;

import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
//import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cisco.hicn.forwarder.forwarder.ForwarderFragment;
import com.cisco.hicn.forwarder.hiperf.HiPerfFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Home.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Home#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Home extends Fragment {
    BottomNavigationView bottomNavigationView;

    FragmentManager fragmentManager;

    ForwarderFragment forwarder;
    InterfaceFragment interfaces;
    ApplicationsFragment applications;
    HiPerfFragment hiperf;

    private OnFragmentInteractionListener mListener;

    public Home() {
    }

    public static Home newInstance() {
        Home fragment = new Home();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentManager = getChildFragmentManager();

        forwarder = new ForwarderFragment();
        interfaces = new InterfaceFragment();
        hiperf = new HiPerfFragment();
        hiperf.setHome(this);

        fragmentManager.beginTransaction().replace(R.id.subviewLayout, forwarder).commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        bottomNavigationView = (BottomNavigationView) getView().findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment fragment;
                        switch (item.getItemId()) {
                            case R.id.action_forwarder:
                                fragment = forwarder;
                                break;
                            case R.id.action_interfaces:
                                fragment = interfaces;
                                break;
                            /*case R.id.action_applications:
                                fragment = applications;
                                break;*/
                            case R.id.action_hiperf:
                                fragment = hiperf;
                                break;
                            default:
                                return false;
                        }

                        fragmentManager.beginTransaction().replace(R.id.subviewLayout, fragment).commit();
                        return false;
                    }
                });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void disableItem(int index) {
        bottomNavigationView.findViewById(index).setEnabled(false);
    }

    public void enableItem(int index) {
        bottomNavigationView.findViewById(index).setEnabled(true);
    }


}
