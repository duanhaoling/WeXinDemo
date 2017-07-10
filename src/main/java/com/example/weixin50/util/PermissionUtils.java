package com.example.weixin50.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.example.weixin50.R;


/**
 * Created by penneryu on 16/9/14.
 */
public final class PermissionUtils {


    // //在Activity中使用，走Activity的onRequestPermissionsResult回调
    public static boolean checkActivityPermisssion(Activity activity, String permission, String dialogMessage, int requestCode, boolean finished) {
        if (!hasPermission(activity, permission)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                createPermissionDialog(activity, dialogMessage, finished);
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
            }
            return false;
        }
        return true;
    }

    //在Fragment中使用，走Fragment的onRequestPermissionsResult回调
    public static boolean checkFragmentPermisssion(Activity activity, Fragment fragment, String permission, String dialogMessage, int requestCode, boolean finished) {
        if (!hasPermission(activity, permission)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                createPermissionDialog(activity, dialogMessage, finished);
            } else {
                fragment.requestPermissions(new String[]{permission}, requestCode);
            }
            return false;
        }
        return true;
    }

//    public static boolean checkPermisssion(Activity activity, String permission, String dialogMessage, int requestCode, boolean finished) {
//        if (!hasPermission(activity, permission)) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
//                createPermissionDialog(activity, dialogMessage, finished);
//            } else {
//                ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
//            }
//            return false;
//        }
//        return true;
//    }

    public static void createPermissionDialog(final Activity activity, String message, final boolean finished) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(false);
        builder.setMessage(message);
        builder.setTitle(activity.getString(R.string.permission));
        builder.setPositiveButton(activity.getString(R.string.permission_setting), (dialog, which) -> {
            Intent intent = new Intent("android.settings.MANAGE_APPLICATIONS_SETTINGS");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
            dialog.dismiss();
            activity.finish();
        });
        builder.setNegativeButton(activity.getString(R.string.penner_cancel), (dialog, which) -> {
            if (finished) {
                activity.finish();
            } else {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public static boolean hasPermission(Context context, String permisssion) {
        try {
            return ContextCompat.checkSelfPermission(context, permisssion) == PackageManager.PERMISSION_GRANTED;
        } catch (Throwable throwable) {
            return false;
        }
    }
}
