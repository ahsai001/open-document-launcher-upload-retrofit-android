package com.ahsailabs.dashboardinfo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import com.ahsailabs.dashboardinfo.databinding.ActivitySaveEvidenceBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SaveEvidenceActivity extends AppCompatActivity {
    private ActivitySaveEvidenceBinding binding;
    private Uri fileUri1;
    private Uri fileUri2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySaveEvidenceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActivityResultLauncher<String[]> openDocLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), result -> {
            Toast.makeText(this, "uri : "+result, Toast.LENGTH_SHORT).show();
            fileUri1 = result;
            binding.iv1.setImageURI(result);
            showMetaInfo(fileUri1);
        });

        ActivityResultLauncher<Uri> openCamLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result) {
                binding.iv1.setImageURI(fileUri1);
                showMetaInfo(fileUri1);
            }
        });

        ActivityResultLauncher<String[]> openDoc2Launcher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), result -> {
            Toast.makeText(this, "uri : "+result, Toast.LENGTH_SHORT).show();
            fileUri2 = result;
            binding.iv2.setImageURI(result);
            showMetaInfo(fileUri2);
        });



        binding.btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //openDocLauncher.launch(new String[]{"image/png", "image/jpg"});
                try {
                    File file = File.createTempFile("test", ".png", getCacheDir());
                    fileUri1 = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID+".provider", file);
                    openCamLauncher.launch(fileUri1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        binding.btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDoc2Launcher.launch(new String[]{"image/png", "image/jpg"});
            }
        });


        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    submitData(fileUri1, fileUri2,
                            binding.etTeam.getText().toString(),
                            binding.etTools.getText().toString(),
                            binding.etSpace.getText().toString(),
                            binding.etProgress.getText().toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    binding.tvResult.setText(e.getMessage());
                }
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("", "");

    }



    private void submitData(Uri fileUri1, Uri fileUri2, String team, String tools, String space, String progress) throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(Constants.BASE_URL)
                .build();

        ApiService service = retrofit.create(ApiService.class);

        Call<SaveEvidenceResponse> call = service.saveEvidence(
                getFilePart("team_avaibility_pic", fileUri1),
                getFilePart("tools_avaibility_pic", fileUri2),
                getStringPart(team),
                getStringPart(tools),
                getStringPart(space),
                getStringPart(progress)
                );

        call.enqueue(new Callback<SaveEvidenceResponse>() {
            @Override
            public void onResponse(Call<SaveEvidenceResponse> call, Response<SaveEvidenceResponse> response) {
                Toast.makeText(SaveEvidenceActivity.this, "sukses submit : "+response.body().getMessage(), Toast.LENGTH_SHORT).show();
                binding.tvResult.setText(response.body().getMessage());
            }

            @Override
            public void onFailure(Call<SaveEvidenceResponse> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(SaveEvidenceActivity.this, "Gagal submit : "+t.getMessage(), Toast.LENGTH_SHORT).show();
                binding.tvResult.setText(t.getMessage());
            }
        });
    }

    private MultipartBody.Part getFilePart(String fieldName, Uri fileUri) throws IOException {
        String mimeType = getContentResolver().getType(fileUri);

        @SuppressLint("Recycle") Cursor cursor = getContentResolver().query(fileUri, null, null, null, null);
        cursor.moveToFirst();
        @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

        InputStream is = getContentResolver().openInputStream(fileUri);

        /*// cara 1 : pake temp file
        File file = File.createTempFile("test", ".xls", getCacheDir());
        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            int read;
            byte[] bytes = new byte[8192];
            while ((read = is.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse(mimeType), file);
         */

        // cara 2 : pakai bytearray
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int read;
        byte[] bytes = new byte[8192];
        while ((read = is.read(bytes)) != -1) {
            buffer.write(bytes, 0, read);
        }
        buffer.flush();
        byte[] targetArray = buffer.toByteArray();

        RequestBody requestBody = RequestBody.create(MediaType.parse(mimeType), targetArray);

        return MultipartBody.Part.createFormData(fieldName, name, requestBody);
    }

    private RequestBody getStringPart(String info){
        return RequestBody.create(MediaType.parse("text/plain"), info);
    }


    private void showMetaInfo(Uri uri){
        if (uri != null) {
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                ExifInterface exifInterface = new ExifInterface(is);
                double[] latlng = exifInterface.getLatLong();
                if (latlng != null) {
                    Log.d("ahmad", "lat long gambar : "+latlng[0]+"/"+ latlng[1]);
                }

                Long gpsTime = exifInterface.getGpsDateTime();
                if (gpsTime != null) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.getDefault());
                    Date date = new Date();
                    date.setTime(exifInterface.getGpsDateTime());
                    Log.d("ahmad", "date time gambar : " + dateFormat.format(date));
                }

                if(exifInterface.hasThumbnail()){
                    Log.d("ahmad", "ada thumbnail gambar");
                    //binding.iv1.setImageBitmap(exifInterface.getThumbnailBitmap());
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}