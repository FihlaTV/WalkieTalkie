package com.elicng.walkietalkie.activities;

import android.net.nsd.NsdManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.elicng.walkietalkie.R;
import com.elicng.walkietalkie.audios.AudioRecorderRunnable;
import com.elicng.walkietalkie.net.Client;
import com.elicng.walkietalkie.net.NsdHelper;
import com.elicng.walkietalkie.net.Server;

import java.util.ArrayList;
import java.util.Collection;

/**
 * ProgressBar details: http://developer.samsung.com/technical-doc/view.do?v=T000000086
 */

public class WalkieTalkieActivity extends ActionBarActivity implements AudioRecorderRunnable.AudioRecorderHandler {

    private AudioRecorderRunnable audioRecorder;
    private Collection<Client> clients = new ArrayList<>();
    private ProgressBar audioAmplitude;
    private Server server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recorder_buffer);
        audioAmplitude = (ProgressBar) findViewById(R.id.progressBar);

        // Create a server instance to listen to connecting clients
        server = new Server();
        server.start();

        NsdHelper nsdHelper = new NsdHelper((NsdManager) getSystemService(NSD_SERVICE));
        nsdHelper.initDiscovery(new NsdHelper.ServerFoundListener() {
            @Override
            public void onServerFound(String ipAddress, int port) {
                Client client = new Client();
                client.listen(ipAddress, port);
                clients.add(client);
            }
        });
        nsdHelper.registerService(server.getPort());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_audio_recorder_buffer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void btnStartRecording_onClick(View view) {

        if (audioRecorder == null) {
            audioRecorder = new AudioRecorderRunnable(this);
            new Thread(audioRecorder).start();
        }

    }

    public void btnStopRecording_onClick(View view) {
        if (audioRecorder != null) {
            audioRecorder.stopRecording();
            audioRecorder = null;
        }

        audioAmplitude.setProgress(0);

    }

    @Override
    public void onRecording(byte[] buffer) {
        server.writeByte(buffer);

        // Find the recording amplitude ( on 100 )
        int sum = 0;
        for (int i = 0; i < buffer.length; i++) {
            sum += buffer[i] * buffer[i];
        }
        final int amplitude = (int) Math.sqrt(sum / buffer.length);

        // set the visual
        audioAmplitude.setProgress(amplitude);

    }

    private void log(String message) {
        Log.d("com.elicng.walkietalkie", message);
    }
}
