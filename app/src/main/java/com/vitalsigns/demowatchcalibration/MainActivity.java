package com.vitalsigns.demowatchcalibration;

import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.vitalsigns.demowatchcalibration.ble.VitalSignsBle;
import com.vitalsigns.sdk.ble.scan.DeviceListFragment;
import com.vitalsigns.sdk.utility.RequestPermission;
import com.vitalsigns.sdk.utility.Utility;

import static com.vitalsigns.sdk.utility.RequestPermission.PERMISSION_REQUEST_COARSE_LOCATION;

public class MainActivity extends AppCompatActivity
  implements DeviceListFragment.OnEvent
{
  private static final String LOG_TAG = "MainActivity";
  private VitalSignsBle mVitalSignsBle = null;
  private Button btnScanBLEDevice;
  private PercentRelativeLayout adjWatchLayout;
  private ProgressDialog mProgressDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    /// [CC] : Keep screen always on ; 11/06/2017
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    /// [CC] : Initial the component of scan button and adjust watch layout ; 11/09/2017
    viewComponentInitial();
  }
  
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    switch(requestCode)
    {
      case PERMISSION_REQUEST_COARSE_LOCATION:
        /// [CC] : Ble module initial ; 11/09/2017
        bleInit();
        break;
    }
  }

  @Override
  protected void onStart() {
    super.onStart();

    if(RequestPermission.accessCoarseLocation(this))
    {
      /// [CC] : Ble module initial ; 11/09/2017
      bleInit();
    }

    textSizeInitial();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return (true);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if(id == R.id.show_sdk_version)
    {
      Toast.makeText(MainActivity.this,
                     ("SDK Version : " + Utility.SdkVersion()),
                     Toast.LENGTH_LONG)
           .show();
      return (true);
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onStop() {
    super.onStop();

    /// [CC] : Ble module un-initial ; 11/09/2017
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

    /// [CC] : Disconnect with device if connection ; 11/09/2017
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
          hideProgressDialog();
          if(mVitalSignsBle == null)
          {
            Log.d(LOG_TAG, "mVitalSignsBle == null");
          }
          else
          {
            Log.d(LOG_TAG, "strDevicename = " + strDevicename);
            showWatchAdjView();
            mVitalSignsBle.enterTimeCaliMode();
          }
        }
      });
    }

    @Override
    public void onDisconnect(final String strError) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          Log.d(LOG_TAG, "onDisconnect - " + strError);

          hideProgressDialog();
          
          if(mVitalSignsBle != null)
          {
            mVitalSignsBle.disconnect();
          }
  
          scanBleDevice();
        }
      });
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
      Log.d(LOG_TAG, "Device address is " + s);
      mVitalSignsBle.connect(s);

      showProgressDialog(getResources().getString(R.string.connect_device));
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

  /**
   * @brief viewComponentInitial
   *
   * Initial the component of scan button and adjust watch layout
   *
   * @return NULL
   */
  private void viewComponentInitial()
  {
    /// [CC] : Initial the view of scan BLE device ; 11/09/2017
    scanComponentInitial();

    /// [CC] : Initial the view of adjust watch ; 11/09/2017
    adjComponentInitial();
  }

  /**
   * @brief scanComponentInitial
   *
   * Initial the view of scan BLE device
   *
   * @return NULL
   */
  private void scanComponentInitial()
  {
    /// [CC] : Initial Scan button ; 11/09/2017
    btnScanBLEDevice = (Button)findViewById(R.id.scan_ble_device_btn);
    btnScanBLEDevice.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        /// [CC] : Permission request ; 11/06/2017
        if(RequestPermission.accessCoarseLocation(MainActivity.this))
        {
          showScanBleList();
        }
      }
    });
  }

  /**
   * @brief adjComponentInitial
   *
   * Initial the view of adjust watch
   *
   * @return NULL
   */
  private void adjComponentInitial()
  {
    Button btnHurInc;
    Button btnHurDec;
    Button btnMinInc;
    Button btnMinDec;
    Button btnSecInc;
    Button btnSecDec;
    Button btnAdjOk;
    Button btnAdjCancel;

    /// [CC] : Initial adjust watch layout ; 11/09/2017
    adjWatchLayout = (PercentRelativeLayout) findViewById(R.id.adj_pointer_layout);
    btnHurInc = (Button) findViewById(R.id.adj_hour_pointer_inc);
    btnHurDec = (Button) findViewById(R.id.adj_hour_pointer_dec);
    btnMinInc = (Button) findViewById(R.id.adj_minute_pointer_inc);
    btnMinDec = (Button) findViewById(R.id.adj_minute_pointer_dec);
    btnSecInc = (Button) findViewById(R.id.adj_second_pointer_inc);
    btnSecDec = (Button) findViewById(R.id.adj_second_pointer_dec);

    /// [CC] : Set click event ; 11/09/2017
    setAdjBtnClickEvent(btnHurInc, 1 ,0 ,0);
    setAdjBtnClickEvent(btnHurDec, -1 ,0 ,0);
    setAdjBtnClickEvent(btnMinInc, 0 ,1 ,0);
    setAdjBtnClickEvent(btnMinDec, 0 ,-1 ,0);
    setAdjBtnClickEvent(btnSecInc, 0 ,0 ,1);
    setAdjBtnClickEvent(btnSecDec, 0 ,0 ,-1);

    btnAdjOk = (Button) findViewById(R.id.adj_pointer_ok);
    btnAdjCancel = (Button) findViewById(R.id.adj_pointer_cancel);

    btnAdjOk.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        /// [CC] : disconnect first for scan ; 11/09/2017
        if((mVitalSignsBle != null) && (mVitalSignsBle.isConnect()))
        {
          mVitalSignsBle.timeAdjustFinish();
        }
      }
    });

    btnAdjCancel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        /// [CC] : disconnect first for scan ; 11/09/2017
        if((mVitalSignsBle != null) && (mVitalSignsBle.isConnect()))
        {
          mVitalSignsBle.timeAdjustCancel();
          mVitalSignsBle.disconnect();
        }

        scanBleDevice();
      }
    });
  }

  /**
   * @brief setAdjBtnClickEvent
   *
   * Set click event
   *
   * @return NULL
   */
  private void setAdjBtnClickEvent(Button btnAdj, final int nHur, final int nMin, final int nSec)
  {
    btnAdj.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if((mVitalSignsBle != null) && (mVitalSignsBle.isConnect()))
        {
          mVitalSignsBle.timeAdjust(nHur, nMin, nSec);
        }
      }
    });
  }

  /**
   * @brief scanBleDevice
   *
   * Show scan button and hide adjust layout
   *
   * @return NULL
   */
  private void scanBleDevice()
  {
    btnScanBLEDevice.setVisibility(View.VISIBLE);
    adjWatchLayout.setVisibility(View.INVISIBLE);
  }

  /**
   * @brief showWatchAdjView
   *
   * Show watch adjust view
   *
   * @return NULL
   */
  private void showWatchAdjView()
  {
    btnScanBLEDevice.setVisibility(View.INVISIBLE);
    adjWatchLayout.setVisibility(View.VISIBLE);
  }

  /**
   * @brief textSizeInitial
   *
   * Initial text size
   *
   * @return NULL
   */
  private void textSizeInitial()
  {
    final PercentRelativeLayout percentRelativeLayout;
    percentRelativeLayout = (PercentRelativeLayout) findViewById(R.id.adj_pointer_layout);

    percentRelativeLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {
        TextView textView2;
        TextView textView;
        TextView textViewHr;
        TextView textViewMin;
        TextView textViewSec;

        textView2 = (TextView)findViewById(R.id.adj_pointer_title2);
        textView = (TextView)findViewById(R.id.adj_pointer_title);
        textViewHr = (TextView)findViewById(R.id.adj_hour_pointer_title);
        textViewMin = (TextView)findViewById(R.id.adj_minute_pointer_title);
        textViewSec = (TextView)findViewById(R.id.adj_second_pointer_title);

        textView2.setTextSize(TypedValue.COMPLEX_UNIT_SP, percentRelativeLayout.getHeight() * 0.025f);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, percentRelativeLayout.getHeight() * 0.025f);
        textViewHr.setTextSize(TypedValue.COMPLEX_UNIT_SP, percentRelativeLayout.getHeight() * 0.025f);
        textViewMin.setTextSize(TypedValue.COMPLEX_UNIT_SP, percentRelativeLayout.getHeight() * 0.025f);
        textViewSec.setTextSize(TypedValue.COMPLEX_UNIT_SP, percentRelativeLayout.getHeight() * 0.025f);
      }
    });
  }


  /**
   * @brief showProgressDialog
   *
   * Show progress dialog
   *
   * @return NULL
   */
  private void showProgressDialog(final String strMsg) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (mProgressDialog == null)
        {
          mProgressDialog = new ProgressDialog(MainActivity.this, R.style.DialogStyle);
          mProgressDialog.setIndeterminate(true);
          mProgressDialog.setCanceledOnTouchOutside(false);
          mProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener()
          {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
            {
              if(mProgressDialog != null && mProgressDialog.isShowing())
              {
                /// [CC] : Do nothing if true ; 11/28/2017
                return (true);
              }
              return (false);
            }
          });
        }

        mProgressDialog.setMessage(strMsg);
        mProgressDialog.show();
      }
    });
  }

  /**
   * @brief hideProgressDialog
   *
   * Hide progress dialog
   *
   * @return NULL
   */
  private void hideProgressDialog() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if ((mProgressDialog != null) && (mProgressDialog.isShowing()))
        {
          mProgressDialog.dismiss();
        }
      }
    });
  }
}
