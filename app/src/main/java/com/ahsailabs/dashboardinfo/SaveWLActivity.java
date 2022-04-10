package com.ahsailabs.dashboardinfo;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.ahsailabs.dashboardinfo.databinding.ActivitySaveWlactivityBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SaveWLActivity extends AppCompatActivity {
    private ActivitySaveWlactivityBinding binding;


    private Uri fileUri = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySaveWlactivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActivityResultLauncher<String[]> openDocLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), result -> {
            Toast.makeText(this, "uri : "+result, Toast.LENGTH_SHORT).show();
            fileUri = result;
        });

        binding.btnPick.setOnClickListener((View.OnClickListener) view -> {
            openDocLauncher.launch(new String[]{"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/pdf", "image/png"});
        });


        binding.btnUpload.setOnClickListener((View.OnClickListener) view -> {
            if(fileUri != null) {
                try {
                    uploadFile(fileUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void uploadFile(Uri result) throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(Constants.BASE_URL)
                .build();

        ApiService service = retrofit.create(ApiService.class);
        String mimeType = getContentResolver().getType(result);

        @SuppressLint("Recycle") Cursor cursor = getContentResolver().query(result, null, null, null, null);
        cursor.moveToFirst();
        @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

        InputStream is = getContentResolver().openInputStream(result);

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



        MultipartBody.Part partFile = MultipartBody.Part.createFormData("daily_mig_plan", name, requestBody);
        Call<SaveWLResponse> call = service.saveWL(partFile);

        call.enqueue(new Callback<SaveWLResponse>() {
            @Override
            public void onResponse(Call<SaveWLResponse> call, Response<SaveWLResponse> response) {
                Toast.makeText(SaveWLActivity.this, "sukses upload : "+response.body().getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<SaveWLResponse> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(SaveWLActivity.this, "Gagal upload : "+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}