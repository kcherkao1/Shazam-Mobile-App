package proj1.kamal;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.project1_android.R;

public class SongDetailsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_details);

        try {
            TextView titleTextView = findViewById(R.id.titleTextView);
            TextView artistTextView = findViewById(R.id.artistTextView);
            ImageView albumImageView = findViewById(R.id.albumImageView);

            String title = getIntent().getStringExtra("title");
            String artist = getIntent().getStringExtra("artist");
            String imageUrl = getIntent().getStringExtra("image");

            titleTextView.setText(title);
            artistTextView.setText(artist);

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this).load(imageUrl).into(albumImageView);
            }
        } catch (Exception e) {
            Log.e("SongDetailsActivity", "Error: " + e.getMessage(), e);
        }
    }
}