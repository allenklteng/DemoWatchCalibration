package com.vitalsigns.demowatchcalibration.ble;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.vitalsigns.sdk.ble.BleAlertData;
import com.vitalsigns.sdk.ble.BleCmdService;
import com.vitalsigns.sdk.ble.BlePedometerData;
import com.vitalsigns.sdk.ble.BleService;
import com.vitalsigns.sdk.ble.BleSleepData;
import com.vitalsigns.sdk.ble.BleSwitchData;
import com.vitalsigns.sdk.utility.Utility;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by coge on 2017/11/6.
 */

public class VitalSignsBle {
  private static final String LOG_TAG = "VitalSignsBle";
  private static final int BLE_DATA_QUEUE_SIZE = 128;
  private static BlockingQueue<int []> mBleIntDataQueue = new ArrayBlockingQueue<>(BLE_DATA_QUEUE_SIZE);
  private BleEvent mBleEvent = null;
  private Context mContext = null;
  private boolean mBleServiceBind = false;
  private BleService mBleService = null;
  private boolean bSendTimePosCmd = false;
  private boolean bSendTimeSetCmd = false;

  public interface BleEvent
  {
    void onConnect(String strDevicename);
    void onDisconnect(String strError);
  }

  public VitalSignsBle (@NotNull Context context, @NotNull BleEvent event)
  {
    mContext = context;
    mBleEvent = event;

    /// [CC] : Bind service ; 11/16/2017
    Intent intent = new Intent(context, BleService.class);
    mBleServiceBind = context.bindService(intent, mBleServiceConnection, BIND_AUTO_CREATE);
  }

  private ServiceConnection mBleServiceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
      mBleService = ((BleService.LocalBinder)iBinder).getService();
      mBleService.Initialize(mBleIntDataQueue, BleCmdService.HW_TYPE.CARDIO);
      mBleService.RegisterClient(onAckListener,
                                 onErrorListener,
                                 onStatusListener,
                                 onDataListener,
                                 onBleRawListener);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }
  };

  private BleCmdService.OnAckListener onAckListener = new BleCmdService.OnAckListener() {
    @Override
    public void ackBleWatchSyncTime(boolean b) {

    }

    @Override
    public void ackTimeGet(int i, int i1, int i2) {

    }

    @Override
    public void ackTimeCali(boolean b) {

    }

    @Override
    public void ackSwitchGet(BleSwitchData bleSwitchData) {

    }

    @Override
    public void ackSwitchCnt(int i) {

    }

    @Override
    public void ackSwitchSts(int i, boolean b) {

    }

    @Override
    public void ackAlertGet(BleAlertData bleAlertData) {

    }

    @Override
    public void ackNameGet(String s) {

    }

    @Override
    public void ackTemperatureGet(ArrayList<Integer> arrayList) {

    }

    @Override
    public void ackPasswordCheck(boolean b) {

    }
  };

  private BleCmdService.OnErrorListener onErrorListener = new BleCmdService.OnErrorListener() {
    @Override
    public void bleConnectionLost(String s) {
      Log.d(LOG_TAG, "bleConnectionLost()");
      if(mBleEvent != null)
      {
        mBleEvent.onDisconnect(s);
      }
    }

    @Override
    public void bleGattState() {
      Log.d(LOG_TAG, "bleGattState()");
    }

    @Override
    public void bleTransmitTimeout() {
      Log.d(LOG_TAG, "bleTransmitTimeout()");
      if(mBleEvent != null)
      {
        mBleEvent.onDisconnect("bleTransmitTimeout");
      }
    }

    @Override
    public void bleAckError(String s) {
      Log.d(LOG_TAG, "bleAckError()");
      if(mBleEvent != null)
      {
        mBleEvent.onDisconnect(s);
      }
    }
  };

  private BleCmdService.OnStatusListener onStatusListener = new BleCmdService.OnStatusListener() {
    @Override
    public void bleReadyToGetData() {
      if((mBleEvent != null) &&
         (mBleService != null) &&
         (mBleService.IsBleConnected()))
      {
        mBleEvent.onConnect(mBleService.GetBleDevice().getName());
      }
    }

    @Override
    public void bleOtaAck() {

    }

    @Override
    public void bleStopAck() {

    }

    @Override
    public void bleEcgReady() {

    }
  };

  private BleCmdService.OnDataListener onDataListener = new BleCmdService.OnDataListener() {
    @Override
    public void chartNumberConfig(int i, int i1, int[] ints) {

    }

    @Override
    public void pedometerData(int i, ArrayList<BlePedometerData> arrayList) {

    }

    @Override
    public void sleepData(int i, int i1, ArrayList<BleSleepData> arrayList) {

    }

    @Override
    public void todayStep(int i) {

    }

    @Override
    public void loopTick(long l) {

    }

    @Override
    public void tick(long l) {

    }

    @Override
    public void mfaData(byte[] bytes) {

    }
  };

  private BleCmdService.OnBleRawListener onBleRawListener = new BleCmdService.OnBleRawListener() {
    @Override
    public void ackReceived(byte[] bytes) {
      if(bSendTimePosCmd)
      {
        bSendTimePosCmd = false;

        /// [CC] : This command will set current time to watch ; 11/09/2017
        bSendTimeSetCmd = true;
        mBleService.CmdTimeSet(Utility.GetHour(), Utility.GetMinute(), Utility.GetSecond());
      }

      /// [CC] : Time set finish, disconnect ; 11/09/2017
      if(bSendTimeSetCmd)
      {
        bSendTimeSetCmd = false;
        if(mBleEvent != null)
        {
          mBleEvent.onDisconnect("Calibration finish");
        }
      }
    }
  };

  /**
   * @brief isConnect
   *
   * Get device is connected or not
   *
   * @return true if connected
   */
  public boolean isConnect()
  {
    return ((mBleService != null) && mBleService.IsBleConnected());
  }

  /**
   * @brief connect
   *
   * Connect to the device
   *
   * @param mac device mac address
   *
   * return NULL
   */
  public void connect(String mac)
  {
    if(mBleService != null)
    {
      mBleService.SetBleDevice(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mac));
      mBleService.Connect();
    }
  }

  /**
   * @brief disconnect
   *
   * Disconnect the device
   *
   * @return NULL
   */
  public void disconnect()
  {
    if(mBleService != null)
    {
      mBleService.Disconnect();
    }
  }

  /**
   * @brief destroy
   *
   * Unbind service
   *
   * @return NULL
   */
  public void destroy()
  {
    if(mBleServiceBind)
    {
      mContext.unbindService(mBleServiceConnection);
      if(mBleEvent != null)
      {
        mBleEvent = null;
      }
    }
  }

  /**
   * @brief enterTimeCaliMode
   *
   * This command will set watch enter time calibration mode
   *
   * @return NULL
   */
  public void enterTimeCaliMode()
  {
    if(mBleService != null)
    {
      mBleService.CmdTimeCali();
    }
  }

  /**
   * @brief timeAdjust
   *
   * Adjust pointer by specify value
   * EX : nHur = 1, hour pointer increase 2 degree
   *      nMin = -1, minute pointer decrease 2 degree
   *
   * @return NULL
   */
  public void timeAdjust(int nHur, int nMin, int nSec)
  {
    if(mBleService != null)
    {
      mBleService.CmdTimeAdjust(nHur, nMin, nSec);
    }
  }

  /**
   * @brief timeAdjustFinish
   *
   * This command will tell watch current pointer position
   * The three argument means the degree of pointer ("12" is degree 0 and clockwise)
   * hour  90 : hour pointer point to 3
   * minute 0 : minute pointer point to 0
   * second 0 : second pointer point to 0
   *
   * @return NULL
   */
  public void timeAdjustFinish()
  {
    if(mBleService != null)
    {
      mBleService.CmdTimePos(90, 0, 0);
      bSendTimePosCmd = true;
    }
  }

  /**
   * @brief timeAdjustCancel
   *
   * This command will set current time to watch
   *
   * @return NULL
   */
  public void timeAdjustCancel()
  {
    if(mBleService != null)
    {
      bSendTimePosCmd = false;
      bSendTimeSetCmd = false;

      /// [CC] : This command will set current time to watch ; 11/09/2017
      mBleService.CmdTimeSet(Utility.GetHour(), Utility.GetMinute(), Utility.GetSecond());
    }
  }
}
