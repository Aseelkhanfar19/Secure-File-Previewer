package com.example.securefilepreview;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.UUID;

/**
 * Upload flow:
 *   1. User picks a file.
 *   2. Bytes are read once and uploaded to Supabase Storage at
 *      shared-files/{uuid}/{fileName}.
 *   3. A row in public.shared_links maps the generated 4-digit code
 *      to that object path. The code is shown to the user to share.
 */
public class SendActivity extends AppCompatActivity {

    private TextView showSelectedFile;
    private Uri selectedFile;
    TextView secretCodeDisplayer;
    Button backBtn;


    // to open file picker [ file manager]
    ActivityResultLauncher<String> filePicker = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>(){
                @Override
                public void onActivityResult(Uri chosenFile) {
                    if (chosenFile == null) return;
                    selectedFile = chosenFile;
                    showSelectedFile = findViewById(R.id.displayPlace);
                    secretCodeDisplayer = findViewById(R.id.secret_code);


                    Button confirmBtn = findViewById(R.id.sendFile);
                    Button uploadBtn = findViewById(R.id.btnUpload);
                    TextView title = findViewById(R.id.title);
                    showSelectedFile.setText(
                            "The selected file:\n\n" + chosenFile.getLastPathSegment());
                    //hide the upload button after the user picks a file
                    confirmBtn.setVisibility(View.VISIBLE); //button for confirmation after upload the MIME
                    // the btn clicked
                    confirmBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            confirmBtn.setVisibility(View.GONE);
                            uploadBtn.setVisibility(View.GONE);
                            showSelectedFile.setVisibility(View.GONE);
                            secretCodeDisplayer.setVisibility(View.VISIBLE);
                            byte[] convertedData = convertData(selectedFile);
                            if(convertedData == null){
                                Toast.makeText(SendActivity.this, "Failed to read the file", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            SupabaseClient sc = new SupabaseClient(SendActivity.this);
                            int code = generateCode();
                            String fileName = selectedFile.getLastPathSegment();
                            sc.uploadToStorage(fileName, convertedData, getMimeType(selectedFile),code);

                            title.setText("Your file has been uploaded successfully! \n\n The code to share is: ");
                            secretCodeDisplayer.setText(String.valueOf(code));
                            secretCodeDisplayer.append("\n\n Note: The code will expire in 20 seconds and the file will be removed from the server.");


                        }//end of onclick
                    }); //end of setOnClickListener

                }

            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_send);

        Button filePickerBtn = findViewById(R.id.btnUpload);
        filePickerBtn.setOnClickListener(v -> filePicker.launch("*/*"));
        backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent prev_page = new Intent(SendActivity.this, MainActivity.class);
                startActivity(prev_page);



                // Close the current activity and return to the previous one
            }
        });

    }


    /**
     * Generates a 4-digit secret code in the range [1000, 9999].
     */
    public int generateCode() {
        return new Random().nextInt(9000) + 1000;
    }

    public byte[] convertData(Uri FileUri){
        try{
        InputStream in = getContentResolver().openInputStream(FileUri);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead = 1024;
        byte[] data = new byte[nRead];
        int len;//the length of the data read from the input stream, it will be used to write the correct amount of data to the buffer
        while ((len = in.read(data)) != -1){
            buffer.write(data,0, len);//write the data read from the input stream to the buffer, starting from index 0 and writing len bytes
        }
        return buffer.toByteArray();
        }
        catch (IOException e){
            Toast.makeText(this, "Failed to read the file", Toast.LENGTH_SHORT).show();
            return null;
        }


    }
    public String getMimeType(Uri uri) {
        ContentResolver cR = getContentResolver();
        return cR.getType(uri);
        // هاد السطر بيرجع "image/png" أو "application/pdf" حسب الملف
    }


}


