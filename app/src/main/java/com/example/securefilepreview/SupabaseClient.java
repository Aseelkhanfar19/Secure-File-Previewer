package com.example.securefilepreview;

import android.content.Context;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public final class SupabaseClient {
    public interface CodeCheckCallback {
        void onValidCode(String filePath, String mimeType);
        void onInvalidCode(String message);
    }

    private Context context;
    final String supabaseUrl = "https://astwkwiowbucqkadrdom.supabase.co";
    //the path which okhttp will use it to send requests,data , it is a road to a party
    final String supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFzdHdrd2lvd2J1Y3FrYWRyZG9tIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzg0MzA3NjgsImV4cCI6MjA5NDAwNjc2OH0.YQISQtzYP4VoryrYqHoGieo6zcGQ7j7rRmziWDgTbg8";
    // this is anon key , stands for anonymous key, which is used to access the database without authentication, but with limited permissions. It is used for client-side applications where you want to allow users to read data without requiring them to log in. However, it should be used with caution, as it can potentially expose your database to unauthorized access if not configured properly.
    // this is the invitation card to that party
    // without the url , you are hold an invitation card without the address of the party, and without the key, you know the address of the party but without the invitation card to enter the party, so both of them are essential to access the database and storage in supabase.


    final okhttp3.OkHttpClient client;
    /* this is the client that we will used to send requests to the supabase server, it is a library that allows us to make HTTP requests in a simple and efficient way. It provides a convenient API for sending GET, POST, PUT, DELETE requests and handling responses. We will use this client to interact with the Supabase API and perform operations such as uploading files, creating rows in the database, and retrieving data.*/

    public SupabaseClient(Context context){ //constructor
        client = new okhttp3.OkHttpClient();
        this.context = context;

    }

    public void uploadToStorage(String fileName, byte[] fileData , String mimeType, int generatedCode) {
        String the_unique_file= java.util.UUID.randomUUID().toString() + "_" + fileName;

        String full_path = supabaseUrl + "/storage/v1/object/shared-files/" + the_unique_file;
        okhttp3.RequestBody body = okhttp3.RequestBody.create(fileData, okhttp3.MediaType.parse(mimeType));
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(full_path)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + supabaseKey)
                .addHeader("Content-Type", "image/jpeg")
                .post(body)
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                System.out.println("Upload failed " + e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful()) {
                    String relativePath = "shared-files/" + the_unique_file;
                    saveToDatabase(generatedCode , relativePath);
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            removeFileFromStorage(the_unique_file, generatedCode);
                        }
                    }, 20000);

                } else {
                    System.out.println("Server rejected the request " + response.code());
                }
            }
        });
    }

    public void saveToDatabase(int generatedCode, String filePath ){
        String full_path = supabaseUrl + "/rest/v1/files_code";
        String json_content = "{\"secret_code\": " + generatedCode + ", \"file_path\": \"" + filePath + "\"}"; //data will inserted to the database in json format
        okhttp3.RequestBody body = okhttp3.RequestBody.create(json_content,
                okhttp3.MediaType.parse("application/json; charset=utf-8"));
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(full_path)
                .post(body)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + supabaseKey)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                android.util.Log.e("Supabase_Error", "Network Error: " + e.getMessage());
            } //end of onFailure

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful()) {
                    android.util.Log.d("Supabase_Success", "Success! Data inserted.");
                }
                else {
                    android.util.Log.e("Supabase_Error", "Error Code: " + response.code());
                    android.util.Log.e("Supabase_Error", "Response Body: " + response.body().string());
                }
            } //end of onResponse
        }); //end of enqueue
    }//end of saveToDatabase


    //this method is responsible for deleting the file from storage, it takes the file path and the code as parameters, and it sends a delete request to the supabase storage API to delete the file.
    public void removeFileFromStorage(String filePath ,int code) {
        String the_file = supabaseUrl + "/storage/v1/object/shared-files";
        String jsonBody = "{\"prefixes\": [\"" + filePath + "\"]}";
        okhttp3.RequestBody body = okhttp3.RequestBody.create(
                jsonBody,
                okhttp3.MediaType.parse("application/json")
        );
        okhttp3.Request request = new okhttp3.Request
                .Builder()
                .url(the_file)
                .delete(body)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + supabaseKey)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                android.util.Log.e("Supabase_Error", "Network Error: " + e.getMessage());
            } //end of onFailure

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful()) {
                    android.util.Log.d("Supabase_Success", "File deleted successfully.");
                    deleteCodeFromDatabase(code);
                } else {
                    android.util.Log.e("Supabase_Error", "Error Code: " + response.code());
                    android.util.Log.e("Supabase_Error", "Response Body: " + response.body().string());
                }
            }//end of onResponse

        }//end of new Callback
        );//end of enqueue

    } //end of removeFileFromStorage

    //this method is responsible for deleting the row in DB after the file is deleted from storage
    public void deleteCodeFromDatabase(int code){
        String full_path = supabaseUrl + "/rest/v1/files_code?secret_code=eq." + code;
        // /rest/v1/files_code this is the path , ? is the separator between the path and the quer,
        // secret_code is the column name in the database, eq. is the operator for equality, and code is the value we want to match. This query will delete the row where the secret_code column matches the provided code.


        okhttp3.Request request = new okhttp3.Request
                .Builder()
                .url(full_path)
                .delete()
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + supabaseKey)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                android.util.Log.e("Supabase_Error", "Network Error: " + e.getMessage());
            } //end of onFailure

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful()) {
                    android.util.Log.d("Supabase_Success", "Code deleted successfully.");
                } else {
                    android.util.Log.e("Supabase_Error", "Error Code: " + response.code());
                    android.util.Log.e("Supabase_Error", "Response Body: " + response.body().string());
                }
            }//end of onResponse

        }//end of new Callback
        );//end of enqueue
    }//end of deleteCodeFromDatabase



    public void checkTheCode(String code, CodeCheckCallback callback) {
        String url = supabaseUrl + "/rest/v1/files_code?secret_code=eq." + code;
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .get() //to get the code
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + supabaseKey)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() { //send the request asynchronously
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                callback.onInvalidCode("Network Error: " + e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();

                    // supasebase use JSON when it return the data
                    if (responseBody.equals("[]")) {
                        callback.onInvalidCode("Invalid or expired code! ❌");
                    } else {
                        try {
                            // convert the response body to JSON array and get the first object, because we expect only one object to be returned since the code is unique
                            org.json.JSONArray array = new org.json.JSONArray(responseBody);
                            org.json.JSONObject result = array.getJSONObject(0);


                            String filePath = result.getString("file_path");
                            String lowerPath = filePath.toLowerCase();
                            String mimeType = "image/png";
                            if (filePath.toLowerCase().contains("video")) {
                                mimeType = "video/mp4";
                            }
                            else if (lowerPath.endsWith(".pdf")) {
                                mimeType = "application/pdf"; //
                            } else if (lowerPath.endsWith(".txt") || lowerPath.contains("text")) {
                                mimeType = "text/plain";
                            }

                            callback.onValidCode(filePath, mimeType);

                        } catch (Exception e) {
                            callback.onInvalidCode(e.getMessage());
                        }
                    }
                } else {
                    callback.onInvalidCode("Server Error: " + response.code());
                }
            }
        });
    } //end of checkTheCode
    public String getFileUrlFromStorage(String filePath) {
        String rawUrl = supabaseUrl + "/storage/v1/object/public/" + filePath;
        String secureUrl = rawUrl.substring(0, 8) + rawUrl.substring(8).replace(":", "%3A");

        return secureUrl;
    }//end of getFileUrlFromStorage



}//end of SupabaseClient class
