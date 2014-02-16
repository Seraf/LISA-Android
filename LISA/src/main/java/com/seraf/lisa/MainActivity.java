package com.seraf.lisa;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.Locale;
import java.util.ArrayList;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

import android.content.ActivityNotFoundException;

import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import android.widget.Button;
import android.widget.ListView;
import android.widget.EditText;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {
    protected static final int RESULT_SPEECH = 1;
    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    protected static final int MY_DATA_CHECK_CODE = 0;

    public TextToSpeech myTTS;
    private boolean flag;

    private EditText editText;
    private Button send;
    private Button btnSpeak;
    private ListView mList;
    private ArrayList<String> arrayList;
    private MyCustomAdapter mAdapter;

    // is it really dynamic ?
    Locale French = new Locale("fr");

    private boolean isSpeechServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (com.seraf.lisa.services.SpeechActivationService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (!intent.hasExtra("TRIGGER")){
            return;
        }
        else {
            if (isSpeechServiceRunning()){
                flag = true;
                stopService(new Intent(MainActivity.this,com.seraf.lisa.services.SpeechActivationService.class));
            }
            btnSpeak.performClick();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        if (!isSpeechServiceRunning()) {
            startService(new Intent(MainActivity.this, com.seraf.lisa.services.SpeechActivationService.class));
            flag = false;
        }


        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);

        arrayList = new ArrayList<String>();

        editText = (EditText) findViewById(R.id.editText);
        send = (Button)findViewById(R.id.send_button);

        //relate the listView from java to the one created in xml
        mList = (ListView)findViewById(R.id.list);
        mAdapter = new MyCustomAdapter(this, arrayList);
        Log.d("wtf", "listview: " + mList);
        mList.setAdapter(mAdapter);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = editText.getText().toString();

                //add the text in the arrayList
                arrayList.add("> " + message);

                //sends the message to the server
                //if (mTcpClient != null) {
                //    mTcpClient.sendMessage(message);
                //}

                //refresh the list
                mAdapter.notifyDataSetChanged();
                editText.setText("");
            }
        });

        btnSpeak = (Button)findViewById(R.id.speak_button);

        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent = new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "fr");

                try {
                    startActivityForResult(intent, RESULT_SPEECH);
                    editText.setText("");
                } catch (ActivityNotFoundException a) {
                    Toast t = Toast.makeText(getApplicationContext(),
                            "Opps! Your device doesn't support Speech to Text",
                            Toast.LENGTH_SHORT);
                    t.show();
                }
            }
        });

    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = myTTS.setLanguage(Locale.getDefault());

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(MainActivity.this,
                        "This Language is not supported", Toast.LENGTH_LONG).show();
            }
        }
        else if (status == TextToSpeech.ERROR) {
            Toast.makeText(MainActivity.this,
                    "Error occurred while initializing Text-To-Speech engine", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_SPEECH: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> text = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    editText.setText(text.get(0));
                    send.performClick();
                    if (flag == true) {
                        startService(new Intent(MainActivity.this, com.seraf.lisa.services.SpeechActivationService.class));
                        flag = false;
                    }
                }
                break;
            }
            case MY_DATA_CHECK_CODE: {
                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    // the user has the necessary data - create the TTS
                    myTTS = new TextToSpeech(this, this);
                } else {
                    // no data - install it now
                    Intent installTTSIntent = new Intent();
                    installTTSIntent
                            .setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installTTSIntent);
                }
                break;
            }
        }

    }

    @Override
    protected void onDestroy() {
        if (myTTS != null) {
            myTTS.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

}
