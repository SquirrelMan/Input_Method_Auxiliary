/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.jim84_000.input_method_auxiliary;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.jim84_000.input_method_auxiliary.DeviceListFragment.DeviceActionListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A fragment that manages a particular peer and allows interaction with device
 * i.e. setting up network connection and transferring data.
 */
public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener {

    private View mContentView = null;
    private WifiP2pDevice device;
    private WifiP2pInfo info;
    ProgressDialog progressDialog = null;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContentView = inflater.inflate(R.layout.device_detail, null);
        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
                        "Connecting to :" + device.deviceAddress, true, true
//                        new DialogInterface.OnCancelListener() {
//
//                            @Override
//                            public void onCancel(DialogInterface dialog) {
//                                ((DeviceActionListener) getActivity()).cancelDisconnect();
//                            }
//                        }
                );
                ((DeviceActionListener) getActivity()).connect(config);

            }
        });

        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((DeviceActionListener) getActivity()).disconnect();
                    }
                });

        mContentView.findViewById(R.id.btn_start_client).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent();
                        intent.setClass(WiFiDirectActivity.mContext,InputActivity.class);
                        startActivity(intent);
                    }
                });
        mContentView.findViewById(R.id.btn_start_speech).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        SpeechMode.path="Main";
                        Intent intent = new Intent();
                        intent.setClass(WiFiDirectActivity.mContext, SpeechMode.class);
                        startActivity(intent);
                    }
                });
        mContentView.findViewById(R.id.btn_start_speechs1).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        SpeechMode.path="Sub1";
                        Intent intent = new Intent();
                        intent.setClass(WiFiDirectActivity.mContext, SpeechMode.class);
                        startActivity(intent);
                    }
                });
        mContentView.findViewById(R.id.btn_start_speechs2).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        SpeechMode.path="Sub2";
                        Intent intent = new Intent();
                        intent.setClass(WiFiDirectActivity.mContext, SpeechMode.class);
                        startActivity(intent);
                    }
                });
        mContentView.findViewById(R.id.btn_start_speechs3).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        SpeechMode.path="Sub3";
                        Intent intent = new Intent();
                        intent.setClass(WiFiDirectActivity.mContext, SpeechMode.class);
                        startActivity(intent);
                    }
                });
        mContentView.findViewById(R.id.btn_start_speechs4).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        SpeechMode.path="Sub4";
                        Intent intent = new Intent();
                        intent.setClass(WiFiDirectActivity.mContext, SpeechMode.class);
                        startActivity(intent);
                    }
                });
        return mContentView;
    }


    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;
        this.getView().setVisibility(View.VISIBLE);

        // The owner IP is now known.
        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(getResources().getString(R.string.group_owner_text)
                + ((info.isGroupOwner == true) ? getResources().getString(R.string.yes)
                : getResources().getString(R.string.no)));

        // InetAddress from WifiP2pInfo struct.
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());

        // After the group negotiation, we assign the group owner as the file
        // server. The file server is single threaded, single connection server
        // socket.
        if (info.groupFormed && info.isGroupOwner) {
            // new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text)) .execute();
            Intent intent=new Intent();
            intent.setClass(getActivity(), DisplayActivity.class);
            startActivity(intent);
        } else if (info.groupFormed) {
            // The other device acts as the client. In this case, we enable the
            // get file button.
            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
            ((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources()
                    .getString(R.string.client_text));
            mContentView.findViewById(R.id.btn_start_speech).setVisibility(View.VISIBLE);
            //mContentView.findViewById(R.id.btn_start_speechs1).setVisibility(View.VISIBLE);
            //mContentView.findViewById(R.id.btn_start_speechs2).setVisibility(View.VISIBLE);
            //mContentView.findViewById(R.id.btn_start_speechs3).setVisibility(View.VISIBLE);
            //mContentView.findViewById(R.id.btn_start_speechs4).setVisibility(View.VISIBLE);
        }

        // hide the connect button
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
    }

    /**
     * Updates the UI with device data
     *
     * @param device the device to be displayed
     */
    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(device.deviceAddress);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(device.toString());

    }

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        mContentView.findViewById(R.id.btn_start_speech).setVisibility(View.GONE);
        mContentView.findViewById(R.id.btn_start_speechs1).setVisibility(View.GONE);
        mContentView.findViewById(R.id.btn_start_speechs2).setVisibility(View.GONE);
        mContentView.findViewById(R.id.btn_start_speechs3).setVisibility(View.GONE);
        mContentView.findViewById(R.id.btn_start_speechs4).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);
    }





    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        long startTime=System.currentTimeMillis();

        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.close();
            inputStream.close();
            long endTime=System.currentTimeMillis()-startTime;
            Log.v("","Time taken to transfer all bytes is : "+endTime);

        } catch (IOException e) {
            Log.d(WiFiDirectActivity.TAG, e.toString());
            return false;
        }
        return true;
    }

}
