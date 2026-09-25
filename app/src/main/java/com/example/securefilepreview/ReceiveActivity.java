package com.example.securefilepreview;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class ReceiveActivity extends AppCompatActivity{


    FrameLayout file_container;
    String finalFileUrl;
    LinearLayout viewer;
    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
                android.view.WindowManager.LayoutParams.FLAG_SECURE,
                android.view.WindowManager.LayoutParams.FLAG_SECURE
        );
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_receive);
        EditText the_code = findViewById(R.id.codeInput);
        Button check_btn = findViewById(R.id.check_code_btn);
        Button back_home = findViewById(R.id.back_home_btn);
         viewer = findViewById(R.id.code_enter_layout);
         file_container = findViewById(R.id.Content_viewer);
        back_home.setOnClickListener(v -> {
            if (finalFileUrl != null) {
                SupabaseClient supabase = new SupabaseClient(ReceiveActivity.this);
                supabase.removeFileFromStorage(finalFileUrl,Integer.parseInt(the_code.getText().toString())); // Remove the file from Supabase storage
            }

            Toast.makeText(ReceiveActivity.this, "exit successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ReceiveActivity.this, MainActivity.class));
        }); //to back to home page


        check_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = the_code.getText().toString().trim();
                if (code.isEmpty()) {
                    Toast.makeText(ReceiveActivity.this, "Please enter the code", Toast.LENGTH_LONG).show();
                    return;
                }
                // Check if the code is correct
                SupabaseClient supabase = new SupabaseClient(ReceiveActivity.this);
                supabase.checkTheCode(code, new SupabaseClient.CodeCheckCallback() {
                    @Override
                    public void onValidCode(String filePath, String mimeType) {
                        runOnUiThread(() -> {
                            viewer.setVisibility(View.GONE);
                            file_container.setVisibility(View.VISIBLE);


                            finalFileUrl = supabase.getFileUrlFromStorage(filePath);

                            displayFileInFrame(mimeType, finalFileUrl);
                        });
                    }//end of onValidCode

                    @Override
                    public void onInvalidCode(String message) {
                        // if the code is invalid due to wrong value or expired time
                        runOnUiThread(() -> {
                            Toast.makeText(ReceiveActivity.this, message, Toast.LENGTH_LONG).show();
                        });
                    }//end of onInvalidCode
                });//end of checkTheCode
            };



        });//end of check_btn listener

    }

    //this method will detect the file type and create a proper viewer for it.
    private void displayFileInFrame(String mimeType, String url) {
        file_container.removeAllViews(); // Clear any existing views in the container
        if (mimeType != null && mimeType.startsWith("image/")) {
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            com.bumptech.glide.Glide.with(this).load(url).into(imageView);

            file_container.addView(imageView);


        } //end of if for image
        else if (mimeType != null && mimeType.startsWith("video/")) {
            VideoView videoView = new VideoView(this);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.gravity = android.view.Gravity.CENTER;
            videoView.setLayoutParams(params);

            videoView.setVideoPath(url);
            file_container.addView(videoView);
            android.widget.MediaController mediaController = new android.widget.MediaController(this);
            mediaController.setAnchorView(file_container);
            videoView.setMediaController(mediaController);
            videoView.start();
        }

    }//end of displayFileInFrame

}
