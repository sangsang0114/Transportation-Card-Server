package com.sku.yoon.tcserver;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.stetho.Stetho;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MainActivity<pbulic> extends AppCompatActivity {
    final static int BT_REQUEST_ENABLE = 1;
    final static int BT_MESSAGE_READ = 2;
    final static int BT_CONNECTING_STATUS = 3;
    final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //    final static UUID BT_UUID = UUID.fromString("74278BDA-B644-4520-8F0C-720EAF059935");
    TextView mTvBluetoothStatus;
    TextView mTvReceiveData;
    TextView mTvSendData;
    Button mBtnBluetoothOn;
    Button mBtnBluetoothOff;
    Button mBtnConnect;
    Button mBtnSendData;

    Button refreshBtn;
    Button tableCreateBtn;
    Button setDB;
    EditText setNameText;

    Button b1, b2, b3, b4, b5, b6, b7, b8, b9;
    Button redBus;
    Button greenBus;
    Button blueBus;

    TextView dataTexts;
    SQLiteDatabase database;
    BluetoothAdapter mBluetoothAdapter;
    Set<BluetoothDevice> mPairedDevices;
    List<String> mListPairedDevices;
    Handler mBluetoothHandler;
    ConnectedBluetoothThread mThreadConnectedBluetooth;
    BluetoothDevice mBluetoothDevice;
    BluetoothSocket mBluetoothSocket;
    String busType = "BLUE_BUS";
    String stop = "사당역";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Stetho.initializeWithDefaults(this);

        b1 = (Button) findViewById(R.id.s1);
        b2 = (Button) findViewById(R.id.s2);
        b3 = (Button) findViewById(R.id.s3);
        b4 = (Button) findViewById(R.id.s4);
        b5 = (Button) findViewById(R.id.s5);
        b6 = (Button) findViewById(R.id.s6);
        b7 = (Button) findViewById(R.id.s7);
        b8 = (Button) findViewById(R.id.s8);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send("서정마을");
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send("상암동");
            }
        });
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send("신금호역");
            }
        });
        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send("연서시장");
            }
        });
        b5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send("구파발");
            }
        });
        b6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send("정릉동");
            }
        });
        b7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send("혜화동");
            }
        });
        b8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send("사당역");
            }
        });


        mTvBluetoothStatus = (TextView) findViewById(R.id.tvBluetoothStatus);
        mTvReceiveData = (TextView) findViewById(R.id.tvReceiveData);
        mTvSendData = (EditText) findViewById(R.id.tvSendData);
        mBtnBluetoothOn = (Button) findViewById(R.id.btnBluetoothOn);
        mBtnBluetoothOff = (Button) findViewById(R.id.btnBluetoothOff);
        mBtnConnect = (Button) findViewById(R.id.btnConnect);
        mBtnSendData = (Button) findViewById(R.id.btnSendData);

        dataTexts = (TextView) findViewById(R.id.dataText);
        refreshBtn = (Button) findViewById(R.id.dataRefresh); //데이터 새로고침 버튼
        tableCreateBtn = (Button) findViewById(R.id.createBtn);
        setDB = (Button) findViewById(R.id.set);
        createDatabase("businfo");

        redBus = (Button) findViewById(R.id.redbus);
        blueBus = (Button) findViewById(R.id.bluebus);
        greenBus = (Button) findViewById(R.id.greenbus);


        setDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertRecord("1,2,테스트,가나다");
            }
        });

        tableCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createTable("GREEN_Bus");
            }
        });

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeQuery();
            }
        });


        //블루투스 관련
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBtnBluetoothOn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothOn();
            }
        });
        mBtnBluetoothOff.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothOff();
            }
        });
        mBtnConnect.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                listPairedDevices();
            }
        });
        mBtnSendData.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mThreadConnectedBluetooth != null) {
                    mThreadConnectedBluetooth.write(mTvSendData.getText().toString());
                    mTvSendData.setText("");
                }
            }
        });

        final String[] temp = new String[1];
        mBluetoothHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == BT_MESSAGE_READ) {
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    int idx = readMessage.indexOf("!");
                    System.out.println(idx);
                    System.out.println(readMessage);

                    String s = readMessage.substring(0, idx);
                    System.out.println(s);
                    insertRecord(s);
                    executeQuery();
                }
            }
        };

        redBus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                busType = "RED_Bus";
                if (mThreadConnectedBluetooth != null) {
                    mThreadConnectedBluetooth.write("RED_BUS");
                    mTvSendData.setText("광역버스");
                }
            }
        });

        blueBus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                busType = "BLUE_Bus";
                if (mThreadConnectedBluetooth != null) {
                    mThreadConnectedBluetooth.write("BLUE_BUS");
                    mTvSendData.setText("간선버스");
                }
            }
        });

        greenBus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                busType = "GREEN_Bus";

                if (mThreadConnectedBluetooth != null) {
                    mThreadConnectedBluetooth.write("GREEN_BUS");
                    mTvSendData.setText("지선버스");
                }
            }
        });
    }

    public void println(String data) {
        dataTexts.append(data + "\n");
    }

    private void send(String s) {
        if (mThreadConnectedBluetooth != null) {
            mThreadConnectedBluetooth.write(s);
        }
    }

    private void createDatabase(String name) {
        println("CreateDB 호출됨");

        database = openOrCreateDatabase(name, MODE_PRIVATE, null);
        println("생성 완료");
    }

    private void createTable(String name) {
        println("CreateTable 호출됨");

        database.execSQL("CREATE TABLE IF NOT EXISTS " + name + "("
                + "id_ INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + "cardNum text,"
                + "balance integer,"
                + "result text,"
                + "currentStop text,"
                + "proceedTime text"
                + ");");
        println(name + "TABLE 생성 완료");
    }

    public void insertRecord(String args) {
        String data[] = args.split(",");

        String cn = data[0];
        String balance = data[1];
        String result = data[2];
        String currentStop = data[3];

        database.execSQL("INSERT INTO " + busType
                + "(cardNum , balance , result , currentStop ,proceedTime)"
                + " VALUES( '" + cn + "','" + balance + "','" + result + "','" + currentStop + "',datetime('now', 'localtime'))");
    }

    public void executeQuery() {
        System.out.println("executeQuery가 호출 되었습니다");
        Cursor cursor = database.rawQuery("SELECT cardNum , balance , result , currentStop , proceedTime from " + busType, null);
        int recordCount = cursor.getCount();
        println("레코드 개수 : " + recordCount);

        if (recordCount < 0) {
            println("알 수 없는 오류");
            return;
        }
        dataTexts.setText("#" + getString(R.string.tab) + "카드번호" + getString(R.string.tab) + "잔액" + getString(R.string.tab) + "결과" + getString(R.string.tab) + "정류장\n");
        for (int i = 0; i < recordCount; i++) {
            cursor.moveToNext();
            String cardNum = cursor.getString(0);
            int balance = cursor.getInt(1);
            String result = cursor.getString(2);
            String currentStop = cursor.getString(3);
            String time = cursor.getString(4);
            println(String.format(Locale.KOREA, "#%3d %15s %7d %20s %10s %30s", i, cardNum, balance, result, currentStop, time));
//            println("#" + i + getString(R.string.tab) + cardNum + getString(R.string.tab) + balance + getString(R.string.tab) + result + getString(R.string.tab) + currentStop + getString(R.string.tab) + time);
        }
        dataTexts.append("--------------------------------------------------------------------------------------------------------------------------------\n");
        //dataTexts.append("#" + getString(R.string.tab) + "카드번호" + getString(R.string.tab) + getString(R.string.tab) + "잔액" + getString(R.string.tab) + "결과"
        //         + getString(R.string.tab) + "정류장" + getString(R.string.tab) + getString(R.string.tab) + "처리시간\n");

        dataTexts.append(String.format(Locale.KOREA, "%4s %15s %7s %20s %10s %30s", "#", "카드번호", "잔액", "결과", "정류장", "처리시간"));
        cursor.close();
    }

    void bluetoothOn() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_LONG).show();
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "블루투스가 이미 활성화 되어 있습니다.", Toast.LENGTH_LONG).show();
                mTvBluetoothStatus.setText("활성화");
            } else {
                Toast.makeText(getApplicationContext(), "블루투스가 활성화 되어 있지 않습니다.", Toast.LENGTH_LONG).show();
                Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intentBluetoothEnable, BT_REQUEST_ENABLE);
            }
        }
    }

    void bluetoothOff() {
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            Toast.makeText(getApplicationContext(), "블루투스가 비활성화 되었습니다.", Toast.LENGTH_SHORT).show();
            mTvBluetoothStatus.setText("비활성화");
        } else {
            Toast.makeText(getApplicationContext(), "블루투스가 이미 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BT_REQUEST_ENABLE:
                if (resultCode == RESULT_OK) { // 블루투스 활성화를 확인을 클릭하였다면
                    Toast.makeText(getApplicationContext(), "블루투스 활성화", Toast.LENGTH_LONG).show();
                    mTvBluetoothStatus.setText("활성화");
                } else if (resultCode == RESULT_CANCELED) { // 블루투스 활성화를 취소를 클릭하였다면
                    Toast.makeText(getApplicationContext(), "취소", Toast.LENGTH_LONG).show();
                    mTvBluetoothStatus.setText("비활성화");
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    void listPairedDevices() {
        if (mBluetoothAdapter.isEnabled()) {
            mPairedDevices = mBluetoothAdapter.getBondedDevices();

            if (mPairedDevices.size() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("장치 선택");

                mListPairedDevices = new ArrayList<String>();
                for (BluetoothDevice device : mPairedDevices) {
                    mListPairedDevices.add(device.getName());
                    //mListPairedDevices.add(device.getName() + "\n" + device.getAddress());
                }
                final CharSequence[] items = mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);
                mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        connectSelectedDevice(items[item].toString());
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                Toast.makeText(getApplicationContext(), "페어링된 장치가 없습니다.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "블루투스가 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    void connectSelectedDevice(String selectedDeviceName) {
        for (BluetoothDevice tempDevice : mPairedDevices) {
            if (selectedDeviceName.equals(tempDevice.getName())) {
                mBluetoothDevice = tempDevice;
                break;
            }
        }
        try {
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID);
            System.out.println("connect Try");
            mBluetoothSocket.connect();
            System.out.println("start Try");

            mThreadConnectedBluetooth = new ConnectedBluetoothThread(mBluetoothSocket);
            mThreadConnectedBluetooth.start();
            mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 1, -1).sendToTarget();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
        }
    }

    private class ConnectedBluetoothThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedBluetoothThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "소켓 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        SystemClock.sleep(100);
                        bytes = mmInStream.available();
                        bytes = mmInStream.read(buffer, 0, bytes);
                        mBluetoothHandler.obtainMessage(BT_MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(String str) {
            byte[] bytes = str.getBytes();
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "데이터 전송 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "소켓 해제 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        }
    }
}