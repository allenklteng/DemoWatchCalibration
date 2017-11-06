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

  public interface BleEvent
  {
    void onConnect(String strDevicename);
    void onDisconnect(String strError);
    void onBindFinish();
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
      if(mBleEvent != null)
      {
        mBleEvent.onBindFinish();
      }
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
}
