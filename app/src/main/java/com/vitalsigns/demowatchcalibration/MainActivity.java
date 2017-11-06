package com.vitalsigns.demowatchcalibration;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.WindowManager;

import com.vitalsigns.demowatchcalibration.Utility.Utility;
import com.vitalsigns.demowatchcalibration.ble.VitalSignsBle;

public class MainActivity extends AppCompatActivity
{
  private static final String LOG_TAG = "MainActivity";
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    /// [CC] : Permission request ; 11/06/2017
    Utility.requestPermissionAccessCoarseLocation(this,
                                                  getString(R.string.request_permission_coarse_location_title),
                                                  getString(R.string.request_permission_coarse_location_content),
                                                  null);

    /// [CC] : Keep screen always on ; 11/06/2017
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  }

  @Override
  protected void onStart() {
    super.onStart();

    /// [CC] : Ble module initial ; 11/06/2017
    bleInit();
  }

  @Override
  protected void onStop() {
    super.onStop();

    /// [CC] : Ble module un-initial ; 11/06/2017
    bleUnInit();
  }

  private VitalSignsBle mVitalSignsBle = null;

  /**
   * @brief bleInit
   *
   * Initialize BLE module
   *
   * @return NULL
   */
  private void bleInit()
  {
    mVitalSignsBle = new VitalSignsBle(MainActivity.this, mBleEvent);
  }

  /**
   * @brief bleUnInit
   *
   * Un-initialize BLE module
   *
   * @return NULL
   */
  private void bleUnInit()
  {
    if(mVitalSignsBle == null)
    {
      return;
    }

    /// [CC] : Disconnect with device if connection ; 11/06/2017
    if(mVitalSignsBle.isConnect())
    {
      mVitalSignsBle.disconnect();
    }

    mVitalSignsBle.destroy();
    mVitalSignsBle = null;
  }

  /**
   * @brief mBleEvent
   *
   * Callback of VitalSignsBle.BleEvent
   *
   */
  private VitalSignsBle.BleEvent mBleEvent = new VitalSignsBle.BleEvent()
  {
    @Override
    public void onConnect(String strDevicename) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if(mVitalSignsBle == null)
          {
            return;
          }
        }
      });
    }

    @Override
    public void onDisconnect(String strError) {
      if(!TextUtils.isEmpty(strError))
      {
        if(mVitalSignsBle != null)
        {
          mVitalSignsBle.disconnect();
        }
      }
    }

    @Override
    public void onBindFinish() {
    }
  };
}
