/*
 * Copyright (c) 2019 Cisco and/or its affiliates.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cisco.hicn.forwarder.preferences;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.cisco.hicn.forwarder.R;
import com.cisco.hicn.forwarder.supportlibrary.Facemgr;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DiscardInterfacesPreferenceFragment extends Fragment {

    private ListView lv;
    private ArrayAdapter<String> adapter;
    private CRUD crud = new CRUD();
    private Dialog dialog;
    private JSONArray jSONArray;
    private JSONObject jSONObject;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        Context context = getActivity();
        View root = inflater.inflate(R.layout.discard_interface_preference_fragment, container, false);

        FloatingActionButton fab = root.findViewById(R.id.fab);

        lv = root.findViewById(R.id.lv);

        lv.setOnItemClickListener((adapterView, view, i, l) -> {
            if (dialog != null) {
                if (!dialog.isShowing()) {
                    displayDeleteModifyDialog(i);
                } else {
                    dialog.dismiss();
                }
            } else {
                displayDeleteModifyDialog(i);
            }
        });

        fab.setOnClickListener(view -> displayInputDialog(-1));

        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String interfacesString = preferences.getString(getString(R.string.interfaces_list_key), getString(R.string.empty_string));
            jSONObject = new JSONObject(interfacesString);
            jSONArray = jSONObject.getJSONArray(getString(R.string.interfaces_list_tag));
            if (jSONArray != null) {
                int len = jSONArray.length();
                for (int i = 0; i < len; i++) {
                    crud.save(jSONArray.get(i).toString());
                    adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, crud.getInterfacesArrayList());
                    lv.setAdapter(adapter);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return root;
    }

    private void displayInputDialog(final int pos) {
        dialog = new Dialog(getContext());
        dialog.setTitle(getString(R.string.input_dialog_title));
        dialog.setContentView(R.layout.input_dialog);

        final EditText inptuInterfaceNameEditText = dialog.findViewById(R.id.inputInterfaceNameEditText);
        Button addBtn = dialog.findViewById(R.id.addBtn);

        if (pos == -1) {
            addBtn.setEnabled(true);
        } else {
            addBtn.setEnabled(true);
            inptuInterfaceNameEditText.setText(crud.getInterfacesArrayList().get(pos));
        }

        addBtn.setOnClickListener(v -> {
            String interfaceName = inptuInterfaceNameEditText.getText().toString();
            if (interfaceName.length() > 0 && interfaceName != null) {
                if (!crud.getInterfacesArrayList().contains(interfaceName)) {
                    crud.save(interfaceName);
                    if (jSONArray != null) {
                        jSONArray.put(interfaceName);
                    } else {
                        jSONObject = new JSONObject();
                        JSONArray jSONArray = new JSONArray(crud.getInterfacesArrayList());
                        try {
                            jSONObject.put(getString(R.string.interfaces_list_tag), jSONArray);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                    Facemgr facemgr = Facemgr.getInstance();
                    facemgr.discardInterface(interfaceName);
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(getString(R.string.interfaces_list_key), jSONObject.toString());
                    editor.apply();
                    inptuInterfaceNameEditText.setText(getString(R.string.empty_string));
                    adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, crud.getInterfacesArrayList());
                    lv.setAdapter(adapter);
                }
                dialog.closeOptionsMenu();
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), getString(R.string.null_interface_name_message), Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void displayDeleteModifyDialog(final int pos) {
        dialog = new Dialog(getContext());
        dialog.setTitle(getString(R.string.modify_delete_dialog_title));
        dialog.setContentView(R.layout.delete_modify_dialog);

        final EditText deleteModifyInterfaceNameEditText = dialog.findViewById(R.id.deleteModifyInterfaceNameEditText);
        Button modifyBtn = dialog.findViewById(R.id.modifyBtn);
        Button deleteBtn = dialog.findViewById(R.id.deleteBtn);

        if (pos == -1) {
            modifyBtn.setEnabled(false);
            deleteBtn.setEnabled(false);
        } else {
            modifyBtn.setEnabled(true);
            deleteBtn.setEnabled(true);
            deleteModifyInterfaceNameEditText.setText(crud.getInterfacesArrayList().get(pos));
            deleteModifyInterfaceNameEditText.setSelection(deleteModifyInterfaceNameEditText.getText().length());
        }


        modifyBtn.setOnClickListener(v -> {
            String newInterfaceName = deleteModifyInterfaceNameEditText.getText().toString();
            if (newInterfaceName.length() > 0 && newInterfaceName != null) {
                if (!crud.getInterfacesArrayList().contains(newInterfaceName)) {
                    String oldInterfaceNameName = crud.getInterfacesArrayList().get(pos);
                    if (crud.update(pos, newInterfaceName)) {
                        deleteModifyInterfaceNameEditText.setText(newInterfaceName);
                        JSONArray jSONArray = new JSONArray(crud.getInterfacesArrayList());
                        try {
                            jSONObject.put(getString(R.string.interfaces_list_tag), jSONArray);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Facemgr facemgr = Facemgr.getInstance();
                        facemgr.removeDiscardInterface(oldInterfaceNameName);
                        facemgr.discardInterface(newInterfaceName);
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(getString(R.string.interfaces_list_key), jSONObject.toString());
                        editor.apply();

                        adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, crud.getInterfacesArrayList());
                        lv.setAdapter(adapter);

                    }
                }
                dialog.closeOptionsMenu();
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), getString(R.string.null_interface_name_message), Toast.LENGTH_SHORT).show();
            }
        });
        deleteBtn.setOnClickListener(v -> {
            String oldInterfaceName = crud.getInterfacesArrayList().get(pos);
            if (crud.delete(pos)) {
                deleteModifyInterfaceNameEditText.setText(getString(R.string.empty_string));
                JSONArray jSONArray = new JSONArray(crud.getInterfacesArrayList());
                try {
                    jSONObject.put(getString(R.string.interfaces_list_tag), jSONArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Facemgr facemgr = Facemgr.getInstance();
                facemgr.removeDiscardInterface(oldInterfaceName);
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(getString(R.string.interfaces_list_key), jSONObject.toString());
                editor.apply();
                adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, crud.getInterfacesArrayList());
                lv.setAdapter(adapter);
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}