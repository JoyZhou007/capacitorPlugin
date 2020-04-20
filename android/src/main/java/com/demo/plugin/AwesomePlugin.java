package com.demo.plugin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;

import com.demo.plugin.nfcread.NfcUtil;
import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;


@NativePlugin(
        permissions={
                Manifest.permission.NFC
        }
)

public class AwesomePlugin extends Plugin {

    private final int NFC_REQUEST_PERMISSION = 1000001;
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;


    @PluginMethod()
    public void echo(PluginCall call) {
        String value = call.getString("value");
        JSObject ret = new JSObject();
        ret.put("value", value);
        call.success(ret);
    }

    @PluginMethod()
    public void testEvent(PluginCall call) {
        bridge.triggerWindowJSEvent("myCustomEvent", "{ 'dataKey': 'dataValue' }");
        call.success();
    }

    @PluginMethod()
    public void storeContact(PluginCall call) {
        String name = call.getString("yourName", "default name");
        JSObject address = call.getObject("address", new JSObject());
        boolean isAwesome = call.getBoolean("isAwesome", false);

        if (!call.getData().has("id")) {
            call.reject("Must provide an id");
            return;
        }
        call.resolve();
    }


    @Override
    protected void handleOnStart() {
        super.handleOnStart();
        System.out.println("AwesomePlugin----start");
        bridge.triggerWindowJSEvent("AwesomePluginStartEvent", "{ 'AwesomePluginStartKey': 'AwesomePluginStartValue' }");
        initAdapter();
    }


    @Override
    protected void handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.handleRequestPermissionsResult(requestCode, permissions, grantResults);

        System.out.println("AwesomePlugin----handleRequestPermissionsResult");
        PluginCall savedCall = getSavedCall();
        if (savedCall == null) {
            System.out.println("AwesomePlugin No stored plugin call for permissions request result");
            return;
        }

        for(int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                savedCall.error("AwesomePlugin User denied permission");
                return;
            }
        }

        if (requestCode == NFC_REQUEST_PERMISSION) {
            // We got the permission
//            resolveIntent(intent,savedCall);
        }
    }

    @Override
    protected void handleOnNewIntent(Intent intent) {
        super.handleOnNewIntent(intent);

        System.out.println("AwesomePlugin----handleOnNewIntent");
        PluginCall savedCall = getSavedCall();
        if (!hasRequiredPermissions()) {
            pluginRequestAllPermissions();
        } else {
            if (mNfcAdapter != null) { //有nfc功能
                if (mNfcAdapter.isEnabled()) {//nfc功能打开了
                    resolveIntent(intent,savedCall);
                } else {
                    System.out.println("请打开nfc功能");
                }
            }
        }
//        NfcUtil.writeNdef(getIntent());
    }

    @Override
    protected void handleOnResume () {
        super.handleOnResume();
        System.out.println( " AwesomePlugin onResume: ");
        if (mNfcAdapter != null) { //有nfc功能
            if (mNfcAdapter.isEnabled()) {
                //nfc功能打开了
                //隐式启动
//                mNfcAdapter.enableForegroundDispatch(getActivity(), mPendingIntent, null, null);
            } else {
                System.out.println( "AwesomePlugin 请打开nfc功能");
            }
        }
    }

    @Override
    protected void handleOnPause() {
        super.handleOnPause();
        System.out.println( " AwesomePlugin handleOnPause: ");
        if (mNfcAdapter != null) {
//            mNfcAdapter.disableForegroundDispatch(getActivity());
        }
    }



    private void initAdapter(){
        if (!hasRequiredPermissions()) {
            pluginRequestAllPermissions();
        }
        mNfcAdapter = NfcAdapter.getDefaultAdapter(getContext());
        mPendingIntent = PendingIntent.getActivity(getContext(), 0,
                new Intent(getContext(), getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

    }


    //初次判断是什么类型的NFC卡
    private void resolveIntent(Intent intent,PluginCall call) {
        NdefMessage[] msgs = NfcUtil.getNdefMsg(intent); //重点功能，解析nfc标签中的数据

        if (msgs == null) {
            System.out.println("非NFC启动");
        } else {
            setNFCMsgView(msgs,call);
        }

    }

    @SuppressLint("SetTextI18n")
    @PluginMethod()
    private void setNFCMsgView(NdefMessage[] ndefMessages, PluginCall call) {
        if (ndefMessages == null || ndefMessages.length == 0) {
            return;
        }

        System.out.println("nfc监听--AwesomePlugin");
        if (call == null) {
            return;
        }
        bridge.triggerWindowJSEvent("AwesomePluginIntentEvent", "{ 'awesomePluginIntentKey': 'awesomePluginIntentValue' }");
//        JSObject ret = new JSObject();
//        ret.put("dataKey", "dataValue");
//        call.success(ret);
    }


}
