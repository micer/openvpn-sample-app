package eu.micer.openvpnsample.util;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;

import eu.micer.openvpnsample.R;

public class DialogUtil {
    private static DialogUtil instance;
    public static final String DIALOG_CONNECTING = "dialog_connecting";
    private static final String DIALOG_LOADING = "dialog_loading";
    public static final String DIALOG_VPN_CONFIRMATION_ERROR = "dialog_vpn_confirmation_error";
    private MaterialDialog connectingDialog;
    private MaterialDialog loadingDialog;
    private MaterialDialog vpnConfirmationErrorDialog;

    // It'd be better to use Dagger instead.
    public static synchronized DialogUtil getInstance() {
        if (instance == null) {
            instance = new DialogUtil();
        }
        return instance;
    }

    public void showConnectingDialog(Context context, MaterialDialog.SingleButtonCallback cancelCallback) {
        if (connectingDialog == null) {
            connectingDialog = new MaterialDialog.Builder(context)
                    .title(R.string.connecting)
                    .content(R.string.please_wait)
                    .progress(true, 0)
                    .tag(DIALOG_CONNECTING)
                    .cancelable(false)
                    .negativeText(R.string.cancel)
                    .onNegative(cancelCallback)
                    .show();
        }
    }

    public void dismissConnectingDialog() {
        if (connectingDialog != null && connectingDialog.isShowing()) {
            connectingDialog.dismiss();
        }
        connectingDialog = null;
    }

    public void showLoadingDialog(Context context) {
        if (loadingDialog == null) {
            loadingDialog = new MaterialDialog.Builder(context)
                    .title(R.string.loading)
                    .content(R.string.please_wait)
                    .progress(true, 0)
                    .tag(DIALOG_LOADING)
                    .cancelable(false)
                    .show();
        }
    }

    public void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        loadingDialog = null;
    }

    public void showVpnConfirmationErrorDialog(Context context, MaterialDialog.SingleButtonCallback openSettingsCallback) {
        if (vpnConfirmationErrorDialog != null && vpnConfirmationErrorDialog.isShowing()) {
            return;
        }
        vpnConfirmationErrorDialog = new MaterialDialog.Builder(context)
                .title(R.string.error_vpn_usage_not_authorized)
                .content(R.string.vpn_confirmation_dialog_error)
                .tag(DIALOG_VPN_CONFIRMATION_ERROR)
                .neutralText(R.string.open_settings)
                .onNeutral(openSettingsCallback)
                .positiveText(R.string.ok)
                .onPositive((dialog, which) -> vpnConfirmationErrorDialog.dismiss())
                .show();
    }
}