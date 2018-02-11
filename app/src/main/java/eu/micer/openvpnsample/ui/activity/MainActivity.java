package eu.micer.openvpnsample.ui.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.kyleduo.switchbutton.SwitchButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import eu.micer.openvpnsample.util.DialogUtil;
import eu.micer.openvpnsample.util.NotificationUtil;
import eu.micer.openvpnsample.vpn.VpnManager;
import de.blinkt.openvpn.core.VpnStatus;
import eu.micer.openvpnsample.R;

public class MainActivity extends AppCompatActivity implements VpnManager.VpnStateListener,
        MaterialDialog.SingleButtonCallback {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String ACTION_CONNECT = "action_connect";
    private static final int REQUEST_CODE_VPN_PREPARE = 50;
    private VpnManager vpnManager;

    @BindView(R.id.layout_main)
    ViewGroup layoutMain;

    @BindView(R.id.switch_connect)
    SwitchButton switchConnect;

    @BindView(R.id.tv_vpn_is_on_off)
    TextView tvVpnStateText;

    @BindView(R.id.tv_vpn_state)
    TextView tvVpnState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        vpnManager = new VpnManager(this, this);

        switchConnect.setOnCheckedChangeListener((compoundButton, checked) -> {
            if (checked) {
                // handle VPN connection
                if (!vpnManager.isConnected()) {
                    switchConnect.setEnabled(false);
                    startVpn();
                }
            } else {
                // handle VPN connection
                if (vpnManager.isConnected()) {
                    vpnManager.disconnect();
                }
            }
        });

        if (getIntent().getExtras() != null) {
            boolean actionConnect = getIntent().getBooleanExtra(ACTION_CONNECT, false);
            if (actionConnect && !vpnManager.isConnected()) {
                startVpn();
            }
        }
    }

    private void updateUi(boolean connected) {
        Drawable backgroundDrawable;
        if (connected) {
            backgroundDrawable = ContextCompat.getDrawable(this, R.drawable.bg_main_on);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.background_on_end));
            }
        } else {
            backgroundDrawable = ContextCompat.getDrawable(this, R.drawable.bg_main_off);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.background_off_end));
            }
        }
        layoutMain.setBackground(backgroundDrawable);
        tvVpnStateText.setText(getString(connected ? R.string.vpn_disconnected : R.string.vpn_connected));
    }

    private void startVpn() {
        // Check if some action (like confirm auth dialog) needs to be done before using VPN.
        Intent intent = VpnService.prepare(this);
        if (intent != null) {
            startActivityForResult(intent, REQUEST_CODE_VPN_PREPARE);
        } else {
            // TODO Enter username and password if needed.
            vpnManager.connect("", "");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_VPN_PREPARE) {
            if (resultCode == RESULT_OK) {
                startVpn();
            } else {
                Log.d(TAG, "VPN usage not authorized from user.");
                switchConnect.setChecked(false);
                switchConnect.setEnabled(true);
                DialogUtil.getInstance().dismissConnectingDialog();
                DialogUtil.getInstance().showVpnConfirmationErrorDialog(this, this);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationUtil.getInstance().cancelNotification(this, NotificationUtil.NOT_CONNECTED_INFO_NOTIFICATION_ID);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!vpnManager.isConnected()) {
            NotificationUtil.getInstance().showNotConnectedInfo(this);
        }
    }

    @Override
    public void onStateChange(VpnStatus.ConnectionStatus level, String message) {
        if (TextUtils.isEmpty(message)) {
            message = getString(R.string.state_welcome);
        }

        // Currently message is only logged, not shown in UI.
        Log.d(TAG, message);

        tvVpnState.setText(message);

        if (level == VpnStatus.ConnectionStatus.LEVEL_CONNECTED) {
            switchConnect.setEnabled(true);
            switchConnect.setChecked(true);
            updateUi(true);
            DialogUtil.getInstance().dismissConnectingDialog();
        } else if (level == VpnStatus.ConnectionStatus.LEVEL_NOTCONNECTED) {
            switchConnect.setEnabled(true);
            switchConnect.setChecked(false);
            updateUi(false);
            DialogUtil.getInstance().dismissConnectingDialog();
        } else {
            Log.d(TAG, "connecting VPN...");
            DialogUtil.getInstance().showConnectingDialog(this, this);
        }
    }

    public void onDisconnectDialogCancelClick() {
        Log.i(TAG, "Disconnect dialog canceled");
        switchConnect.setChecked(true);
    }

    @Override
    protected void onDestroy() {
        if (vpnManager != null) {
            vpnManager.release();
        }
        super.onDestroy();
    }

    /**
     * MaterialDialogs callback on click action.
     *
     * @param dialog dialog that called this
     * @param which  type of action, i.e. {@link DialogAction#POSITIVE}
     */
    @Override
    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
        // DIALOG_VPN_CONFIRMATION_ERROR
        if (dialog.getTag() != null
                && dialog.getTag().equals(DialogUtil.DIALOG_VPN_CONFIRMATION_ERROR)
                && which.equals(DialogAction.NEUTRAL)) {
            // Open Settings
            startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
        }

        // DIALOG_CONNECTING
        if (dialog.getTag() != null
                && dialog.getTag().equals(DialogUtil.DIALOG_CONNECTING)
                && which.equals(DialogAction.NEGATIVE)) {

            switchConnect.setEnabled(true);
            switchConnect.setChecked(false);
        }

        // FIXME cancel connection not working properly

//        DialogUtil.getInstance().dismissConnectingDialog();
    }
}
