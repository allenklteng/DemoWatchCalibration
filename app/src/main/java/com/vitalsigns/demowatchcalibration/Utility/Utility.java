package com.vitalsigns.demowatchcalibration.Utility;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;

/**
 * Created by coge on 2017/11/6.
 */

public class Utility {
  public static final int PERMISSION_REQUEST_COARSE_LOCATION = 1001;
  public interface OnCancel
  {
    void Run();
  }

  /**
   * @brief requestPermissionAccessCoarseLocation
   *
   * Request permission to access coarse location
   *
   * @param activity Activity
   * @param title alert dialog title
   * @param message alert dialog message
   * @param onCancel callback if cancel clicked
   *
   * @return true if permission granted
   */
  public static boolean requestPermissionAccessCoarseLocation(final Activity activity,
                                                              String title,
                                                              String message,
                                                              final OnCancel onCancel)
  {
    AlertDialog.Builder builder;

    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
    {
      /// [CC] Android M Permission check ; 11/06/2017
      if(activity.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
      {
        builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(android.R.string.ok,
          new DialogInterface.OnClickListener()
          {
            /// [CC] : Click "ok" event ; 11/06/2017
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
              ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                PERMISSION_REQUEST_COARSE_LOCATION);
            }
          });
        builder.setNegativeButton(android.R.string.cancel,
          new DialogInterface.OnClickListener()
          {
            /// [CC] : Click "cancel" event ; 11/06/2017
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
              if(onCancel != null)
              {
                onCancel.Run();
              }
            }
          });
        builder.show();
        return (false);
      }
    }
    return (true);
  }
}
