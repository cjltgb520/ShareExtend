package com.zt.shareextend;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodChannel;
// REMOVE THIS LINE: import io.flutter.plugin.common.PluginRegistry; // PluginRegistry itself is also largely deprecated for this use case
// REMOVE THIS LINE: import io.flutter.plugin.common.PluginRegistry.Registrar; // Remove old import

/**
 * Plugin method host for presenting a share sheet via Intent
 */
// Remove PluginRegistry.RequestPermissionsResultListener from implements list
public class ShareExtendPlugin implements FlutterPlugin, ActivityAware {

    /// the authorities for FileProvider
    private static final int CODE_ASK_PERMISSION = 100;
    private static final String CHANNEL = "com.zt.shareextend/share_extend";

    private FlutterPlugin.FlutterPluginBinding pluginBinding; // Use fully qualified name for clarity
    private ActivityPluginBinding activityBinding;

    private MethodChannel methodChannel;
    private MethodCallHandlerImpl callHandler;
    private Share share;

    // REMOVE THIS STATIC METHOD: This is the old registration method.
    /*
    public static void registerWith(Registrar registrar) {
        ShareExtendPlugin plugin = new ShareExtendPlugin();
        plugin.setUpChannel(registrar.context(), registrar.messenger(), registrar, null);
    }
    */

    @Override
    public void onAttachedToEngine(@NonNull FlutterPlugin.FlutterPluginBinding flutterPluginBinding) {
        this.pluginBinding = flutterPluginBinding; // Use 'this' for clarity
        // Initialize Share with applicationContext here since we have it
        share = new Share(flutterPluginBinding.getApplicationContext());
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPlugin.FlutterPluginBinding flutterPluginBinding) {
        this.pluginBinding = null; // Use 'this' for clarity
        // No need to tearDown here, as methodChannel and activityBinding are null or will be cleaned in onDetachedFromActivity
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding activityPluginBinding) {
        this.activityBinding = activityPluginBinding; // Use 'this' for clarity
        // Setup channel using the binding data
        setUpChannel(activityPluginBinding.getActivity(), pluginBinding.getBinaryMessenger(), activityPluginBinding);
        // Add permission listener here
        activityBinding.addRequestPermissionsResultListener(this::onRequestPermissionsResult); // Use method reference
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity();
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding activityPluginBinding) {
        onAttachedToActivity(activityPluginBinding);
    }

    @Override
    public void onDetachedFromActivity() {
        tearDown();
    }

    // Removed Registrar from parameters
    private void setUpChannel(Context context, BinaryMessenger messenger, ActivityPluginBinding activityBinding) {
        methodChannel = new MethodChannel(messenger, CHANNEL);
        // Share object is now initialized in onAttachedToEngine with applicationContext
        // Re-initialize if you need activity context specifically, but usually applicationContext is fine.
        // If you specifically need the Activity context for 'share', then move its initialization here.
        // For simplicity, let's assume application context is sufficient for 'Share' for now.
        // If Share needs the Activity itself, you'd pass activityBinding.getActivity() to it.
        // For now, it seems 'Share' just needs a Context.

        callHandler = new MethodCallHandlerImpl(share); // Using the 'share' initialized in onAttachedToEngine
        methodChannel.setMethodCallHandler(callHandler);
        // Permission listener is now added in onAttachedToActivity
    }

    private void tearDown() {
        // Only remove the listener if activityBinding is not null, to prevent NullPointerException
        if (activityBinding != null) {
            activityBinding.removeRequestPermissionsResultListener(this::onRequestPermissionsResult);
        }
        activityBinding = null;
        if (methodChannel != null) { // Check if channel is not null before unsetting handler
            methodChannel.setMethodCallHandler(null);
            methodChannel = null;
        }
    }

    // This method needs to be public as it's an interface method, but it will be passed as a method reference.
    // The interface PluginRegistry.RequestPermissionsResultListener is no longer implemented directly by the class.
    // Instead, it's passed as a lambda/method reference to addRequestPermissionsResultListener.
    public boolean onRequestPermissionsResult(int requestCode, String[] perms, int[] grantResults) {
        if (requestCode == CODE_ASK_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            share.share();
            return true; // Return true if the result was handled
        }
        return false;
    }
}
