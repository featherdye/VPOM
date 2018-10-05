package com.example.parijat.vpom;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by Parijat on 21-Nov-17.
 */

public class MicActivity extends Activity
        implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {
    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;


    //private RecordButton mRecordButton = null;
    private MediaRecorder mRecorder = null;

    //private PlayButton   mPlayButton = null;
    private MediaPlayer mPlayer = null;
    private String Path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/aa/";
    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private List<String> songs;
    //current position
    private int songPosn;
    private static final String TAG = MicActivity.class.getSimpleName();

    boolean mStartPlaying = true;
    boolean mStartRecording = true;
    DatabaseHandler db;
    TextView number;
    Button play, stop, record;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Media Activity created");
        setContentView(R.layout.activity_mic);
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        Intent intent = getIntent();
        currentfence = intent.getExtras().getString("key");
        Log.d(TAG,currentfence+"inside geofence, should play now");
        DatabaseHandler db = new DatabaseHandler(this);
        mPlayer = new MediaPlayer();
        songs = new ArrayList <>();
        refillplaylist(currentfence);


        play=(Button) findViewById(R.id.mPlayButton);
        //stop=(Button) findViewById(R.id.mStopButton);
        record=(Button) findViewById(R.id.mRecordButton);
        //stop.findViewById(R.id.mStopButton).setOnClickListener(mGlobal_OnClickListener);
        play.setOnClickListener(mGlobal_OnClickListener);
        record.findViewById(R.id.mRecordButton).setOnClickListener(mGlobal_OnClickListener);
        initMusicPlayer();
        //List<String> songs = new ArrayList<String>();
    }
    @Override
    public void onStop()
    {
        super.onStop();
        if(mPlayer.isPlaying()==true)
        {//mPlayer.stop();
            mPlayer.release();
        }
    }
    public String currentfence;
    final View.OnClickListener mGlobal_OnClickListener = new View.OnClickListener() {
        public void onClick(final View v) {

            switch (v.getId()) {
                case R.id.mPlayButton:
                    //Inform the user the button1 has been clicked
                    Log.d(TAG, "Play button clicked");
                    for (int i = 0; i < songs.size(); i++) {
                        Log.d("the loaded songs are", Path+songs.get(i));
                    }
//                    Log.d(TAG, Integer.toString(songs.size()));
                    onPlay(mStartPlaying);
                    if(mStartPlaying==false)
                    {play.setText("Play");}
                    else
                    {play.setText("Stop");}

                    mStartPlaying = !mStartPlaying;
                    break;

                case R.id.mRecordButton:
                    //Inform when record button is clicked
                    onRecord(mStartRecording);
                    if(mStartRecording==true)
                    {record.setText("Stop");}
                    else
                    {record.setText("Record");}
                    mStartRecording = !mStartRecording;
                    Log.d(TAG, "record button clicked");

            }
        }
    };
    List <String> playlist;
    void refillplaylist(String geoId)
    {
        Log.d(TAG, geoId+"refillplaylist: removed last playlist, filled with new");
        // playlist=null;
        DatabaseHandler db = new DatabaseHandler(this);
        String file=db.readbygeoid(geoId);
        // Log.d(TAG, file);
        if (file == null || file == "") {
            Log.d(TAG,"Returned hoi gawa");
            return;
        }
        Log.d(TAG,file+" filenames from db");

        playlist = Arrays.asList(file.split("\\s*,\\s*"));
        TextView myTextView= (TextView) findViewById(R.id.listlength);
        Log.d("size of playlist", Integer.toString(playlist.size()));
        String length="There are "+Integer.toString(playlist.size()-1)+" recordings here";
        myTextView.setText(length);
        for (int i = 0; i < playlist.size(); i++) {
            //if (playlist.get(i)!= null && playlist.get(i)!=" " && playlist.get(i)!="" && playlist.get(i)!="  " && !playlist.isEmpty()) {

            String gg =playlist.get(i);
            if (gg.trim().length() <= 1) {
                Log.d("Letters::::::" , "Skipping the entry "+ gg + " " + Boolean.toString(gg.trim().length() > 1)+"//p");
                continue;
            }
            Log.d("Letters::::::" , "playlist//"+gg+"//p");
            Log.d("Letters::::::" , "Did the song get added to the list? "+Boolean.toString(songs.add(gg)));
            // }
        }
    }

    public void initMusicPlayer() {
        // set player properties
        mPlayer.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //set listeners
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {

        if (start) {
            Log.d(TAG, "Starting some randome thing...");
            for (int i = 0; i < songs.size(); i++) {
                Log.d(TAG, songs.get(i));
            }
            Log.d(TAG, Integer.toString(songPosn));

            // Log.d(TAG, songs.get(songPosn));
            startPlaying();
        } else {
            stopPlaying();
            Log.d(TAG, "We stoped the playback");
        }
    }

    private void startPlaying() {
        Log.d(TAG, songs.get(songPosn));
        if (mPlayer != null) {
            mPlayer.reset();
        }
        // mPlayer = new MediaPlayer();
//        mPlayer.setWakeMode(getApplicationContext(),
//                PowerManager.PARTIAL_WAKE_LOCK);
//        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//        //set listeners
//        mPlayer.setOnPreparedListener(this);
//        mPlayer.setOnCompletionListener(this);
//        mPlayer.setOnErrorListener(this);
        Log.d("trying to prepare file", songs.get(songPosn));
        try {
            mPlayer.setDataSource(Path +  songs.get(songPosn));
            mPlayer.prepareAsync();
//            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback
        mPlayer.start();
        for (int i = 0; i < songs.size(); i++) {
            //Log.d(TAG, songs.get(i));
        }
        Log.d(TAG, Integer.toString(songs.size()));
        Log.d(TAG, "onPrepared got called");

    }



    @Override
    public void onCompletion(MediaPlayer player) {
        //check if playback has reached the end of a track
        Log.d("oncompletion", "aayo ke");
        Log.d(TAG, "onCompletion: player katthey hai? "+ Integer.toString(player.getCurrentPosition()));
        player.reset();
        playNext();
        if (player.getCurrentPosition() > 0) {

            player.reset();
            playNext();
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void onplayerPause() {

    }

    String newsoundfilenames() {
        Log.d(TAG, "newsoundfilename: generated a new name and passed to sqlable filename");
        String filename = UUID.randomUUID().toString() + ".3gp";
        return filename;
    }

    private void startRecording() {
        String newFileName = newsoundfilenames();
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(Path+newFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
        updatefilename(currentfence, newFileName);;
        refillplaylist(currentfence);
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.v("MUSIC PLAYER", "Playback Error");
        mp.reset();
        return false;
    }


    //song list

    public void playNext() {
        Log.d("songposition",Integer.toString(songPosn));

        songPosn++;
        Log.d("songposition",Integer.toString(songPosn));
        if (songPosn >= songs.size()) songPosn = 0;
        onPlay(true);
    }

//    public void setList(ArrayList<String> theSongs) {
//        songs = theSongs;
//    }




    public boolean mode;




    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION: {
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
            }
        }
        //if (!permissionToRecordAccepted) finish();
    }
    /////////////////////SQLITE OPERATIONS//////////////////////////////////////////////////SQLITE OPERATIONS//////////////
    private void writeDatabase(LatLng latLng, String geoid, String fileList) {
        Double latitude = latLng.latitude;
        Double longitude = latLng.longitude;
        String lat = Double.toString(latitude);
        String lon = Double.toString(longitude);
        String nam = fileList;
        DatabaseHandler db = new DatabaseHandler(this);
        db.addContact(new GajaBaja(lat, lon, nam, geoid));
        Log.d(TAG, "writeDatabase:" + lat + "   " + lon + "   " + nam + "   " + geoid + "   ");

    }

    void updatefilename (String geoId, String cookedfilename) {
        if (geoId == null || geoId == "") {
            Log.d(TAG, "Kya khali peli");
            return;
        }
        Log.d(TAG, "updateFilename:updated the string");
        DatabaseHandler db = new DatabaseHandler(this);
        String file = db.readbygeoid(geoId) + ",";
        db.updatebygeoid(geoId, file + cookedfilename);
    }

    //public List playlist;


}


