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

import com.ahsailabs.dashboardinfo.databinding.ActivitySaveEvidenceBinding;

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
        });

        ActivityResultLauncher<String[]> openDoc2Launcher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), result -> {
            Toast.makeText(this, "uri : "+result, Toast.LENGTH_SHORT).show();
            fileUri2 = result;
            binding.iv2.setImageURI(result);
        });



        binding.btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDocLauncher.launch(new String[]{"image/png", "image/jpg"});
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
}