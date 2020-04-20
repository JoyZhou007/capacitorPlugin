package com.demo.plugin;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.util.Log;

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

public class NfcPlugin extends Plugin {

    private final int NFC_REQUEST_PERMISSION = 2102;
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;

    StringBuffer ncfMsg;

    @Override
    protected void handleOnStart() {
        super.handleOnStart();
        initAdapter();
    }

    @Override
    protected void handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.handleRequestPermissionsResult(requestCode, permissions, grantResults);

        System.out.println("handling request perms result");
        PluginCall savedCall = getSavedCall();
        if (savedCall == null) {
            System.out.println("No stored plugin call for permissions request result");
            return;
        }

        for(int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                savedCall.error("User denied permission");
                return;
            }
        }

        if (requestCode == NFC_REQUEST_PERMISSION) {
            // We got the permission
//            resolveIntent(intent,savedCall);
            System.out.println("权限获取成功");
        }else{
           getCurrentPosition(savedCall);
        }
    }



    @Override
    protected void handleOnNewIntent(Intent intent) {
        super.handleOnNewIntent(intent);

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
        System.out.println( "onResume: ");
        if (mNfcAdapter != null) { //有nfc功能
            if (mNfcAdapter.isEnabled()) {
                //nfc功能打开了
                //隐式启动
//                mNfcAdapter.enableForegroundDispatch(getActivity(), mPendingIntent, null, null);
            } else {
                System.out.println( "请打开nfc功能");
            }
        }
    }

    @Override
    protected void handleOnPause() {
        super.handleOnPause();
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

        bridge.triggerWindowJSEvent("NfcPluginStartEvent", "{ 'NfcPlugnStartKey': 'nfcPluginValueStart' }");

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
    private void setNFCMsgView(NdefMessage[] ndefMessages,PluginCall call) {
        if (ndefMessages == null || ndefMessages.length == 0) {
            return;
        }

//        Calendar calendar = Calendar.getInstance();
//        int hour = calendar.get(Calendar.HOUR_OF_DAY);
//        int minute = calendar.get(Calendar.MINUTE);
//        ncfMsg.append(hour + ":" + minute + "\n");
//        List<ParsedNdefRecord> records = NfcUtil.parse(ndefMessages[0]);
//        final int size = records.size();
//        for (int i = 0; i < size; i++) {
//            ParsedNdefRecord record = records.get(i);
//            System.out.println("test==="+record.getViewText());
//            ncfMsg.append(record.getViewText() + "\n");
//        }


        if (call == null) {
            return;
        }
        bridge.triggerWindowJSEvent("NfcPluginIntentEvent", "{ 'nfcPluginIntentKey': 'nfcPluginIntentValue' }");
//        JSObject ret = new JSObject();
//        ret.put("dataKey", "dataValue");
//        call.success(ret);
    }

    @PluginMethod()
    public void getCurrentPosition(PluginCall call) {
        Log.d(getLogTag(), "Requesting current position");
        if (!hasRequiredPermissions()) {
            Log.d(getLogTag(), "Not permitted. Asking permission...");
            saveCall(call);
            pluginRequestAllPermissions();
        } else {
            if (mNfcAdapter != null) { //有nfc功能
                if (mNfcAdapter.isEnabled()) {//nfc功能打开了
//                    Intent intent = new Intent(Intent.ACTION_VIEW);
//                    startActivityForResult(intent);
//                    resolveIntent(intent,call);
                } else {
                    System.out.println("请打开nfc功能");
                }
            }
        }
    }


    @PluginMethod()
    public void readNfcData(PluginCall call) {
        String value = call.getString("dataKey");
        JSObject ret = new JSObject();
        ret.put("dataKey", value);
        call.success(ret);
    }



}
