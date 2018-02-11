package eu.micer.openvpnsample.ui.fragment;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

import eu.micer.openvpnsample.ui.activity.MainActivity;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.ProfileManager;
import eu.micer.openvpnsample.R;


public class DisconnectDialogFragment extends AppCompatDialogFragment {

    public static DisconnectDialogFragment newInstance() {
        return new DisconnectDialogFragment();
    }

    protected OpenVPNService mService;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            OpenVPNService.LocalBinder binder = (OpenVPNService.LocalBinder) service;
            mService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
        }
    };

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getActivity() == null) {
            throw new RuntimeException("Activity is null");
        }
        return new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.disconnect_vpn))
                .setPositiveButton(getString(R.string.disconnect),
                        (dialog, whichButton) -> disconnect()
                )
                .setNegativeButton(getString(R.string.cancel),
                        (dialog, whichButton) -> ((MainActivity) getActivity()).onDisconnectDialogCancelClick()
                )
                .create();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), OpenVPNService.class);
            intent.setAction(OpenVPNService.START_SERVICE);
            getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) getActivity().unbindService(mConnection);
    }

    private void disconnect() {
        if (getActivity() != null) {
            ProfileManager.setConntectedVpnProfileDisconnected(getActivity());
        }
        if (mService != null && mService.getManagement() != null) {
            mService.getManagement().stopVPN(false);
        }
    }
}
