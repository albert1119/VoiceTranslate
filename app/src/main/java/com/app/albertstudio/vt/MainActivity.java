package com.app.albertstudio.vt;

import android.support.v4.app.ActivityCompat;
import static android.Manifest.permission.*;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech;
import java.util.Locale;
import android.os.AsyncTask;
import java.util.ArrayList;
import java.io.*;

import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.speech.RecognizerIntent;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.HttpEntity;

import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity  implements MessageDialogFragment.Listener, OnInitListener{
    public static final String GETSOURCE_BROADCAST = "COM.APP.ALBERTSTUDIO.VT.GETSOURCE_BROADCAST";
    public static final String GETTARGET_BROADCAST = "COM.APP.ALBERTSTUDIO.VT.GETTARGET_BROADCAST";
    private static final int RQS_VOICE_RECOGNITION = 1;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;
    private static final String FRAGMENT_MESSAGE_DIALOG = "message_dialog";
    private TextToSpeech tts;
    Button buttonToTranslate;
    Button buttonToTranslate2;
    Button buttonToTranslate3;
    Button buttonToTranslate4;
    Button buttonToTranslate5;
    Button buttonToTranslate6;
    Button buttonToTranslate7;
    Button buttonToTranslate8;
    Button buttonToTranslate9;
    Button stopBtn;
    Button disconnBtn;
    Button connBtn;
    Button startTranslateBtn;
    Button stopTranslateBtn;
    TextView textComment;
    TextView translatedText;
    String source;
    String target;
    boolean mIsSendSource = true;
    boolean mIsTranslate = true;

    public static InputStream in2PKG;
    public static OutputStream out2PKG;
    public static PrintWriter pw2PKG;
    public static BufferedReader br2PKG;
    public static String m_MSG = "";

    public SocketThread mSocketThread;

    private SpeechService mSpeechService;

    private VoiceRecorder mVoiceRecorder;
    private final VoiceRecorder.Callback mVoiceCallback = new VoiceRecorder.Callback() {

        @Override
        public void onVoiceStart() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textComment.setText("輸入文字：監聽中...");
                }
            });

            if (mSpeechService != null) {
                mSpeechService.startRecognizing(mVoiceRecorder.getSampleRate());
            }
        }

        @Override
        public void onVoice(byte[] data, int size) {
            if (mSpeechService != null) {
                mSpeechService.recognize(data, size);
            }
        }

        @Override
        public void onVoiceEnd() {
            if (mSpeechService != null) {
                mSpeechService.finishRecognizing();
            }

        }

    };

    public BroadcastReceiver mQueryRequestReceiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Log.d("ConversationonReceive", intent.getAction());
            if (GETTARGET_BROADCAST.equals(intent.getAction()))
            {
                mIsSendSource = false;
            }
            else if (GETSOURCE_BROADCAST.equals(intent.getAction())) {
                mIsSendSource = true;
            }

        }
    };


    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mSpeechService = SpeechService.from(binder);
            mSpeechService.addListener(mSpeechServiceListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mSpeechService = null;
        }

    };

    private final SpeechService.Listener mSpeechServiceListener =
            new SpeechService.Listener() {
                @Override
                public void onSpeechRecognized(final String text, final boolean isFinal) {
                    if (isFinal)
                    {
                        mVoiceRecorder.dismiss();
                    }
                    if (textComment != null && !TextUtils.isEmpty(text))
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isFinal)
                                {
                                    stopVoiceRecorder();
                                    if(mIsSendSource == true && mSocketThread.isConnected() == true)
                                    {
                                        m_MSG = text;
                                        new Thread(SendText).start();

                                    }
                                    textComment.setText("輸入文字：" + text);
                                    if(mIsTranslate == true)
                                    {
                                        new LoadingDataAsyncTask().execute(text);
                                    }
                                    else
                                    {
                                        CheckPermission();
                                    }
                                }
                                else
                                {
                                    if(mIsSendSource == true && mSocketThread.isConnected() == true)
                                    {
                                        m_MSG = text;
                                        new Thread(SendText).start();

                                    }
                                    textComment.setText("輸入文字：" + text);

                                }
                            }
                        });
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tts = new TextToSpeech(this, this);
        textComment = (TextView)findViewById(R.id.inputText);
        translatedText = (TextView)findViewById(R.id.OutputText);
        buttonToTranslate = (Button)findViewById(R.id.StartBtn);
        buttonToTranslate.setOnClickListener(buttonToTranslateOnClickListener);

        buttonToTranslate2 = (Button)findViewById(R.id.StartBtn2);
        buttonToTranslate2.setOnClickListener(buttonToTranslateOnClickListener2);

        buttonToTranslate3 = (Button)findViewById(R.id.StartBtn3);
        buttonToTranslate3.setOnClickListener(buttonToTranslateOnClickListener3);

        buttonToTranslate4 = (Button)findViewById(R.id.StartBtn4);
        buttonToTranslate4.setOnClickListener(buttonToTranslateOnClickListener4);

        buttonToTranslate5 = (Button)findViewById(R.id.StartBtn5);
        buttonToTranslate5.setOnClickListener(buttonToTranslateOnClickListener5);

        buttonToTranslate6 = (Button)findViewById(R.id.StartBtn6);
        buttonToTranslate6.setOnClickListener(buttonToTranslateOnClickListener6);

        buttonToTranslate7 = (Button)findViewById(R.id.StartBtn7);
        buttonToTranslate7.setOnClickListener(buttonToTranslateOnClickListener7);

        buttonToTranslate8 = (Button)findViewById(R.id.StartBtn8);
        buttonToTranslate8.setOnClickListener(buttonToTranslateOnClickListener8);

        buttonToTranslate9 = (Button)findViewById(R.id.StartBtn9);
        buttonToTranslate9.setOnClickListener(buttonToTranslateOnClickListener9);

        stopBtn = (Button)findViewById(R.id.stopBtn);
        stopBtn.setOnClickListener(StopButton);

        disconnBtn = (Button)findViewById(R.id.disconnBtn);
        disconnBtn.setOnClickListener(DisconnectButton);

        connBtn = (Button)findViewById(R.id.connBtn);
        connBtn.setOnClickListener(ConnButton);

        startTranslateBtn = (Button)findViewById(R.id.startTranslateBtn);
        startTranslateBtn.setOnClickListener(StartTranslateButton);

        stopTranslateBtn = (Button)findViewById(R.id.stopTranslateBtn);
        stopTranslateBtn.setOnClickListener(StopTranslateButton);

        registerReceiver(mQueryRequestReceiver, new IntentFilter(GETTARGET_BROADCAST));

        registerReceiver(mQueryRequestReceiver, new IntentFilter(GETSOURCE_BROADCAST));



        mSocketThread = new SocketThread(getApplicationContext());
        mSocketThread.setName("TOBY");



    }

    @Override
    protected void onStart() {
        super.onStart();

        // Prepare Cloud Speech API
        bindService(new Intent(this, SpeechService.class), mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        // Stop listening to voice
        stopVoiceRecorder();

        // Stop Cloud Speech API
        mSpeechService.removeListener(mSpeechServiceListener);
        unbindService(mServiceConnection);
        mSpeechService = null;

        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (permissions.length == 1 && grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecorder();
            } else {
                showPermissionMessageDialog();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onMessageDialogDismissed() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_AUDIO_PERMISSION);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setSpeechRate(0.8f);
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onDone(String utteranceId) {
                    CheckPermission();
                }

                @Override
                public void onError(String utteranceId) {
                }

                @Override
                public void onStart(String utteranceId) {
                }
            });
        } else {

        }
    }

    private void CheckPermission()
    {
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (permission != PackageManager.PERMISSION_GRANTED)
        {
            // 無權限，向使用者請求
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION
            );
        }
        else
        {
            startVoiceRecorder();
        }
    }

    private Runnable SendText = new Runnable() {
        @Override
        public void run() {
            pw2PKG.write(m_MSG + "\n");
            pw2PKG.flush();
        }
    };

    private void showPermissionMessageDialog() {
        MessageDialogFragment
                .newInstance(getString(R.string.permission_message))
                .show(getSupportFragmentManager(), FRAGMENT_MESSAGE_DIALOG);
    }

    private void startVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
        }
        mVoiceRecorder = new VoiceRecorder(mVoiceCallback);
        mVoiceRecorder.start();
    }

    private void stopVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
            mVoiceRecorder = null;
        }
    }

    private Button.OnClickListener StopButton = new Button.OnClickListener(){

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            stopVoiceRecorder();
            textComment.setText("輸入文字：停止收音");
        }};

    private Button.OnClickListener StartTranslateButton = new Button.OnClickListener(){

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            mIsTranslate = true;
            textComment.setText("輸入文字：啟動翻譯");
        }};

    private Button.OnClickListener StopTranslateButton = new Button.OnClickListener(){

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            mIsTranslate = false;
            textComment.setText("輸入文字：停止翻譯");
        }};

    private Button.OnClickListener DisconnectButton = new Button.OnClickListener(){

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            mSocketThread.Disconnected();
            textComment.setText("輸入文字：停止連線");
        }};

    private Button.OnClickListener ConnButton = new Button.OnClickListener(){

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            mSocketThread.start();
            textComment.setText("輸入文字：開始連線");
        }};

    private Button.OnClickListener buttonToTranslateOnClickListener = new Button.OnClickListener(){

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            stopVoiceRecorder();
            source = "zh-TW";
            target = "en";
            changeTTSLanguage();
            mSpeechService.setLanguageCode(source);

            CheckPermission();
        }};

    private Button.OnClickListener buttonToTranslateOnClickListener2 = new Button.OnClickListener(){

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            stopVoiceRecorder();
            source = "zh-TW";
            target = "ja";
            changeTTSLanguage();
            mSpeechService.setLanguageCode(source);
            CheckPermission();
        }};

    private Button.OnClickListener buttonToTranslateOnClickListener3 = new Button.OnClickListener(){

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            stopVoiceRecorder();
            source = "en";
            target = "zh-TW";
            changeTTSLanguage();
            changeTTSLanguage();
            mSpeechService.setLanguageCode(source);
            CheckPermission();
        }};

    private Button.OnClickListener buttonToTranslateOnClickListener4 = new Button.OnClickListener(){

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            stopVoiceRecorder();
            source = "zh-TW";
            target = "ko";
            changeTTSLanguage();
            changeTTSLanguage();
            mSpeechService.setLanguageCode(source);
            CheckPermission();
        }};

    private Button.OnClickListener buttonToTranslateOnClickListener5 = new Button.OnClickListener(){

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            stopVoiceRecorder();
            source = "en";
            target = "ja";
            changeTTSLanguage();
            changeTTSLanguage();
            mSpeechService.setLanguageCode(source);
            CheckPermission();
        }};

    private Button.OnClickListener buttonToTranslateOnClickListener6 = new Button.OnClickListener(){

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            stopVoiceRecorder();
            source = "ja";
            target = "zh-TW";
            changeTTSLanguage();
            changeTTSLanguage();
            mSpeechService.setLanguageCode(source);
            CheckPermission();
        }};

    private Button.OnClickListener buttonToTranslateOnClickListener7 = new Button.OnClickListener(){

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            stopVoiceRecorder();
            source = "zh-TW";
            target = "th";
            changeTTSLanguage();
            changeTTSLanguage();
            mSpeechService.setLanguageCode(source);
            CheckPermission();
        }};

    private Button.OnClickListener buttonToTranslateOnClickListener8 = new Button.OnClickListener(){

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            stopVoiceRecorder();
            source = "th";
            target = "zh-TW";
            changeTTSLanguage();
            changeTTSLanguage();
            mSpeechService.setLanguageCode(source);
            CheckPermission();
        }};

    private Button.OnClickListener buttonToTranslateOnClickListener9 = new Button.OnClickListener(){

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            stopVoiceRecorder();
            source = "ko";
            target = "zh-TW";
            changeTTSLanguage();
            changeTTSLanguage();
            mSpeechService.setLanguageCode(source);
            int permission = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
            CheckPermission();

            /*
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                    "ko");
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                    "Start Speech");
            startActivityForResult(intent, RQS_VOICE_RECOGNITION);
            */
        }};

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (requestCode == RQS_VOICE_RECOGNITION) {
            if (resultCode == RESULT_OK) {

                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String firstMatch = (String) result.get(0);
                textComment.setText("輸入文字：" + firstMatch);
                new LoadingDataAsyncTask().execute(firstMatch);
            }
            else
            {
                textComment.setText("輸入文字：辨識失敗");
            }

        }
    }

    private void changeTTSLanguage()
    {
        if( tts != null )
        {
            Locale l = Locale.US;
            if( target.equals("ja"))
            {
                l = Locale.JAPAN;
            }
            else if( target.equals("en"))
            {
                l = Locale.US;
            }
            else if( target.equals("zh-TW"))
            {
                l = Locale.TRADITIONAL_CHINESE;
            }
            else if( target.equals("ko"))
            {
                l = Locale.KOREA;
            }
            else if( target.equals("th"))
            {
                l = new Locale("th");
            }
            tts.setLanguage( l );
        }
    }

    @Override
    protected void onDestroy()
    {
        // 釋放 TTS
        if( tts != null ) tts.shutdown();
        unregisterReceiver(mQueryRequestReceiver);
        super.onDestroy();
    }



    class LoadingDataAsyncTask extends AsyncTask<String, Integer, Integer>{
        String tmpresult = "";

        @Override
        protected Integer doInBackground(String... param) {
            HttpClient httpclient = new DefaultHttpClient();

            // Prepare a request object
            HttpGet httpget = new HttpGet("https://www.googleapis.com/language/translate/v2?key=your_key&q=" + param[0].replace(" ", "%20") + "&source=" + source + "&target=" + target);

            // Execute the request
            HttpResponse response;
            try {

                response = httpclient.execute(httpget);
                // Examine the response status
                //Log.i("Praeda",response.getStatusLine().toString());

                // Get hold of the response entity
                HttpEntity entity = response.getEntity();
                // If the response does not enclose an entity, there is no need
                // to worry about connection release

                if (entity != null) {

                    // A Simple JSON Response Read
                    InputStream instream = entity.getContent();
                    tmpresult += convertStreamToString(instream);
                    // now you have the string representation of the HTML request
                    instream.close();
                }


            } catch (Exception e) {

                tmpresult = e.toString();
            }

            return null;
        }
        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            try {
                JSONObject json_read = new JSONObject(tmpresult);    //將資料丟進JSONObject
                //接下來選擇型態使用get並填入key取值
                JSONObject json_obj = json_read.getJSONObject("data");
                JSONArray info = json_obj.getJSONArray("translations");
                String tmpstr = info.getString(0);
                JSONObject json_read2 = new JSONObject(tmpstr);
                String info2 = json_read2.getString("translatedText");
                translatedText.setText("輸出文字：" + info2.replace("&#39;","'"));
                tts.speak( info2.replace("&#39;","'"), TextToSpeech.QUEUE_FLUSH, null, "123" );
                if(mIsSendSource == false && mSocketThread.isConnected() == true)
                {
                    m_MSG = info2.replace("&#39;","'");
                    new Thread(SendText).start();

                }
            }
            catch(JSONException jse)
            {
                translatedText.setText(jse.toString());
            }
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        private String convertStreamToString(InputStream is) {
    /*
     * To convert the InputStream to String we use the BufferedReader.readLine()
     * method. We iterate until the BufferedReader return null which means
     * there's no more data to read. Each line will appended to a StringBuilder
     * and returned as String.
     */
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return sb.toString();
        }
    }
}
