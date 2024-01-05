package proj1.kamal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.project1_android.R;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;




import com.example.project1_android.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private MediaRecorder recorder;
    private MediaPlayer mediaPlayer;
    private Button playButton;
    private TextView recordingIndicator;
    private String fileName = null;
    private boolean isRecording = false;
    private static final int PERMISSION_CODE = 21;

    // OkHttpClient instance for network requests
    private final OkHttpClient okHttpClient = new OkHttpClient();


    private void recognizeAudio(File audioFile) {
        if (audioFile.exists()) {
            new Thread(() -> {
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("api_token", "d2f3ebd3e01641c981eecb89f49bf543")
                        .addFormDataPart("file", "audio.3gp",
                                RequestBody.create(MediaType.get("audio/3gp"), audioFile))
                        .addFormDataPart("return", "apple_music,spotify") // Optional: You can ask Audd.io to return links to the song on streaming platforms.
                        .build();

                Request request = new Request.Builder()
                        .url("https://api.audd.io/")
                        .post(requestBody)
                        .build();

                try {
                    Response response = okHttpClient.newCall(request).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        String responseData = response.body().string();
                        Log.d("Recognition", "Response data: " + responseData); // Log the response data
                        parseResponseAndUpdateUI(responseData);
                    } else {
                        Log.e("Recognition", "Failed to recognize the audio. Code: " + response.code());
                    }
                } catch (IOException e) {
                    Log.e("Recognition", "Error during recognition: " + e.getMessage(), e);
                }
            }).start();
        } else {
            Log.e("Recognition", "Audio file does not exist: " + audioFile.getAbsolutePath());
        }
    }


    // Parse the response from the server and update the UI
    private void parseResponseAndUpdateUI(String responseData) {
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            String resultStatus = jsonObject.getString("status");
            if ("success".equals(resultStatus)) {
                JSONObject resultObject = jsonObject.getJSONObject("result");
                String songTitle = resultObject.getString("title");
                String artistName = resultObject.getString("artist");

                Intent intent = new Intent(MainActivity.this, SongDetailsActivity.class);
                intent.putExtra("title", songTitle);
                intent.putExtra("artist", artistName);
                // Add a placeholder or check if the image URL exists in the response
                intent.putExtra("image", "https://seeded-session-images.scdn.co/v2/img/122/secondary/artist/4Z9hYoUqPYbFEzVAjcHDv3/de"); // Replace with actual image URL from response
                startActivity(intent);
            } else {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "No match found", Toast.LENGTH_SHORT).show());
            }
        } catch (JSONException e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to parse response", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recordingIndicator = findViewById(R.id.recordingIndicator);
        playButton = findViewById(R.id.btnPlay);
        playButton.setVisibility(View.INVISIBLE); // Initially hide the play button
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playLastRecordedFile();
            }
        });

        Button recordButton = findViewById(R.id.btnRecord);
        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/audiorecordtest.3gp";

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    stopRecording();
                    recordButton.setText("Start Recording");
                    isRecording = false;
                } else {
                    if (checkPermissions()) {
                        startRecording();
                        recordButton.setText("Stop Recording");
                        isRecording = true;
                    }
                }
            }
        });
    }

    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_CODE);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRecording();
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
            recorder.start();
            recordingIndicator.setVisibility(View.VISIBLE);
            playButton.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Recording Started", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("AudioRecordTest", "prepare() failed");
        }
    }

    private void stopRecording() {
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingIndicator.setVisibility(View.INVISIBLE);
            playButton.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Recording Stopped", Toast.LENGTH_SHORT).show();

            File audioFile = new File(fileName);
            recognizeAudio(audioFile);
        }
    }



    private void playLastRecordedFile() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(fileName);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playButton.setEnabled(true); // Re-enable the play button once playback is complete
                    mp.release();
                }
            });
            playButton.setEnabled(false); // Disable the play button during playback
        } catch (IOException e) {
            Log.e("AudioPlay", "Could not start playback", e);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isRecording) {
            stopRecording();
        }

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}