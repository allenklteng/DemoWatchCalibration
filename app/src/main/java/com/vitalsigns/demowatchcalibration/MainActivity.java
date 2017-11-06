package com.vitalsigns.demowatchcalibration;

import android.app.FragmentTransaction;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.vitalsigns.demowatchcalibration.ble.VitalSignsBle;
import com.vitalsigns.sdk.ble.scan.DeviceListFragment;
import com.vitalsigns.sdk.utility.RequestPermission;

import static com.vitalsigns.sdk.utility.RequestPermission.PERMISSION_REQUEST_COARSE_LOCATION;

public class MainActivity extends AppCompatActivity
  implements DeviceListFragment.OnEvent
{
  private static final String LOG_TAG = "MainActivity";
  private VitalSignsBle mVitalSignsBle = null;
  private Button btnScanBLEDevice;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    /// [CC] : Keep screen always on ; 11/06/2017
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    btnScanBLEDevice = (Button)findViewById(R.id.scan_ble_device_btn);
    btnScanBLEDevice.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        /// [CC] : Permission request ; 11/06/2017
        if(RequestPermission.accessCoarseLocation(MainActivity.this) == true)
        {
          showScanBleList();
        }
      }
    });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch(requestCode)
    {
      case PERMISSION_REQUEST_COARSE_LOCATION:
        /// [CC] : Ble module initial ; 11/06/2017
        bleInit();
        break;
    }
  }

  @Override
  protected void onStart() {
    super.onStart();

    if(RequestPermission.accessCoarseLocation(this) == true)
    {
      /// [CC] : Ble module initial ; 11/06/2017
      bleInit();
    }
  }

  @Override
  protected void onStop() {
    super.onStop();

    /// [CC] : Ble module un-initial ; 11/06/2017
    bleUnInit();
  }

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
    public void onConnect(final String strDevicename) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if(mVitalSignsBle == null)
          {
            Log.d(LOG_TAG, "mVitalSignsBle == null");
          }
          else
          {
            Log.d(LOG_TAG, "strDevicename = " + strDevicename);
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
//      showScanBleList();
    }
  };

  /**
   * @brief showScanBleList
   *
   * Show Scan BLE list
   *
   * @return NULL
   */
  private void showScanBleList()
  {
    /// [CC] : Scan device ; 08/21/2017
    DeviceListFragment fragment;
    FragmentTransaction ft;

    fragment = DeviceListFragment.newInstance(DeviceListFragment.ACTION_SCAN_BLE_DEVICE,
                                              DeviceListFragment.STYLE_DEFAULT_BLACK);
    ft = getFragmentManager().beginTransaction();
    ft.add(fragment, getResources().getString(R.string.device_list_fragment_tag));
    ft.commitAllowingStateLoss();
  }

  @Override
  public void onBleDeviceSelected(String s) {
    if(s == null)
    {
      Log.d(LOG_TAG, "Device address is null");
      return;
    }

    if(mVitalSignsBle != null)
    {
      /// [CC] : Connect BLE device ; 08/21/2017
      mVitalSignsBle.connect(s);
    }
  }

  @Override
  public void onDfuDeviceSelected(BluetoothDevice bluetoothDevice) {
    Log.d(LOG_TAG, "onDfuDeviceSelected");
  }

  @Override
  public void onSendCrashMsg(String s, String s1) {
    Log.d(LOG_TAG, "onSendCrashMsg");
  }
}
