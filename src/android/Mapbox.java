package com.telerik.plugins;

import org.apache.cordova.*;
import org.json.JSONException;
import org.json.JSONObject;

public class Mapbox extends CordovaPlugin {

  public static final String ACTION_TEST = "test";

  @Override
  public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
    try {
      if (ACTION_TEST.equals(action)) {
        final JSONObject options = args.getJSONObject(0);

        callbackContext.success(result);
        return true;
      }
    } catch (Throwable t) {
      t.printStackTrace();
      callbackContext.error("ERROR: " + t.getMessage());
    }
    return false;
  }
}