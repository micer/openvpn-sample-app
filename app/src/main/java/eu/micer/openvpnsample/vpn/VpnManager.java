package eu.micer.openvpnsample.vpn;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import eu.micer.openvpnsample.ui.activity.MainActivity;
import eu.micer.openvpnsample.ui.fragment.DisconnectDialogFragment;
import de.blinkt.openvpn.core.VpnProfile;
import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VPNLaunchHelper;
import de.blinkt.openvpn.core.VpnStatus;
import eu.micer.openvpnsample.R;

public class VpnManager implements VpnStatus.StateListener {
    private static final String TAG = VpnManager.class.getSimpleName();
    private static final String CONFIG_FILE_NAME = "my_config_file.ovpn";
    private Context context;
    private Handler handler;
    private VpnStateListener stateListener;
    private String vpnConfigFileCached;

    public VpnManager(@NonNull Context context, @NonNull VpnStateListener listener) {
        this.context = context;
        this.stateListener = listener;
        handler = new Handler(Looper.getMainLooper());
        VpnStatus.addStateListener(this);
    }

    public void release() {
        VpnStatus.removeStateListener(this);
        handler = null;
        context = null;
        stateListener = null;
        vpnConfigFileCached = null;
    }

    public boolean isConnected() {
        return VpnStatus.isVPNActive();
    }

    public void connect(String username, String password) {
        ConfigParser configParser = new ConfigParser();
        try {

            // Read file from storage if not done yet.
            if (TextUtils.isEmpty(vpnConfigFileCached)) {
                vpnConfigFileCached = getConfigFromAsset();
            }

            configParser.parseConfig(new StringReader(vpnConfigFileCached));
            VpnProfile vp = configParser.convertProfile();
            vp.mName = context.getString(R.string.app_name);
            if (vp.checkProfile(context) != de.blinkt.openvpn.R.string.no_error_found) {
                throw new RuntimeException(context.getString(vp.checkProfile(context)));
            }

            vp.mProfileCreator = context.getPackageName();
            vp.mUsername = username;
            vp.mPassword = password;

            ProfileManager.setTemporaryProfile(vp);

            VPNLaunchHelper.startOpenVpn(vp, context);

        } catch (RuntimeException | ConfigParser.ConfigParseError | IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void disconnect() {
        DialogFragment disconnectDialogFragment = DisconnectDialogFragment.newInstance();
        disconnectDialogFragment.setCancelable(false);
        disconnectDialogFragment.show(((MainActivity) context).getSupportFragmentManager(), "disconnect_dialog");
    }

    @Override
    public void updateState(String state, String msg, int localizedResId, final VpnStatus.ConnectionStatus level) {
        if (handler == null) {
            Log.e(TAG, "Connection handler was released (== null)");
            return;
        }
        final String stateMessage = VpnStatus.getLastCleanLogMessage(context);
        handler.post(() -> {
            if (stateListener != null) {
                stateListener.onStateChange(level, stateMessage);
            }
        });
    }

    public interface VpnStateListener {
        void onStateChange(VpnStatus.ConnectionStatus level, String message);
    }

    private String getConfigFromAsset() {
        StringBuilder config = new StringBuilder();
        InputStream is = null;
        BufferedReader br = null;
        try {
            is = context.getAssets().open(CONFIG_FILE_NAME);
            br = new BufferedReader(new InputStreamReader(is));
            String line;
            while (true) {
                line = br.readLine();
                if (line == null)
                    break;
                config.append(line).append("\n");
            }
            br.readLine();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
        return config.toString();
    }
}
