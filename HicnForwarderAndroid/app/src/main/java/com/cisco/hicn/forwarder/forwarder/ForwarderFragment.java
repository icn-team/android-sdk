package com.cisco.hicn.forwarder.forwarder;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.cisco.hicn.forwarder.R;
import com.cisco.hicn.forwarder.service.BackendAndroidService;
import com.cisco.hicn.forwarder.supportlibrary.NativeAccess;
import com.cisco.hicn.forwarder.utility.Constants;

public class ForwarderFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private TextView forwarderStatusTextView = null;
    private Switch forwarderSwitch = null;


    private OnFragmentInteractionListener mListener;

    public ForwarderFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ForwarderFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ForwarderFragment newInstance(String param1, String param2) {
        ForwarderFragment fragment = new ForwarderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_forwarder, container, false);
        initView(root);
        return root;
    }

    private void initView(View root) {
        if (forwarderStatusTextView == null)
            forwarderStatusTextView = root.findViewById(R.id.forwarder_status_text_view);
        NativeAccess nativeAccess = NativeAccess.getInstance();

        if (forwarderSwitch == null) {
            forwarderSwitch = root.findViewById(R.id.forwarder_switch);
        }

        forwarderSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.v("Switch State=", "" + isChecked);
                if (isChecked) {
                    forwarderStatusTextView.setText(Constants.ENABLED);
                    startBackend();

                } else {
                    forwarderStatusTextView.setText(Constants.DISABLED);
                    stopBackend();
                }
            }

        });

        if (NativeAccess.getInstance().isRunningForwarder()) {
            forwarderStatusTextView.setText(Constants.ENABLED);
            forwarderSwitch.setChecked(true);
        } else {
            forwarderStatusTextView.setText(Constants.DISABLED);
        }

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



    private void startBackend() {
        Intent intent = new Intent(getActivity(), BackendAndroidService.class);
        getActivity().startService(intent);
    }

    private void stopBackend() {
        Intent intent = new Intent(getActivity(), BackendAndroidService.class);
        getActivity().stopService(intent);
    }
}
