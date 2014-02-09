package com.seraf.lisa;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
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

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.EditText;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {
    private static final int REQUEST_CODE = 1000;
    private static final int VOICE_DATA_CHECK_CODE = 0;
    protected static final int RESULT_SPEECH = 1;

    private boolean flag;
    private static TextToSpeech TTS;

    private EditText editText;
    private Button send;
    private ListView mList;

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

    private void voiceRecogniton() {

        if (isSpeechServiceRunning()){
            flag = true;
            stopService(new Intent(MainActivity.this,com.seraf.lisa.services.SpeechActivationService.class));
        }

        Intent intentGoogleVoice = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intentGoogleVoice.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intentGoogleVoice.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        startActivityForResult(intentGoogleVoice, REQUEST_CODE);
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

        editText = (EditText) findViewById(R.id.editText);
        send = (Button)findViewById(R.id.send_button);

        Button btnSpeak = (Button) findViewById(R.id.speak_button);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            if (TTS.isLanguageAvailable(French) == TextToSpeech.LANG_AVAILABLE) ;
            TTS.setLanguage(French);
        } else if (status == TextToSpeech.ERROR) {
            Toast.makeText(this, "There was an error with tts", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                TTS = new TextToSpeech(this, this);
            } else {
                Intent installTTS = new Intent();
                installTTS.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTS);
            }
        }

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            //SearchView.setQuery(matches.get(0), false);
            Log.d("String Voz", matches.get(0));

            if (flag == true) {
                startService(new Intent(MainActivity.this, com.seraf.lisa.services.SpeechActivationService.class));
                flag = false;
            }
        }

        if (resultCode == RecognizerIntent.RESULT_AUDIO_ERROR) {
            Log.e("Lisa", "Audio Error");
            Toast.makeText(this, "Audio Error", Toast.LENGTH_LONG).show();
        }

        if (resultCode == RecognizerIntent.RESULT_CLIENT_ERROR) {
            Log.e("Lisa", "Audio Error");
            Toast.makeText(this, "Audio Error", Toast.LENGTH_LONG).show();
        }

        if (resultCode == RecognizerIntent.RESULT_NETWORK_ERROR) {
            Log.e("Lisa", "Network Error");
            Toast.makeText(this, "Network Error", Toast.LENGTH_LONG).show();
        }

        if (resultCode == RecognizerIntent.RESULT_SERVER_ERROR) {
            Log.e("Lisa", "Server Error");
            Toast.makeText(this, "Server Error", Toast.LENGTH_LONG).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        if (TTS != null) {
            TTS.shutdown();
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
