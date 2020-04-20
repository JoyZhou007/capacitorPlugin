package com.demo.plugin;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

@NativePlugin()
public class AwesomePlugin extends Plugin {

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

    public void readMsg(){
        bridge.triggerWindowJSEvent("readMsg", "{ 'dataKey': 'dataValue' }");
    }
}
