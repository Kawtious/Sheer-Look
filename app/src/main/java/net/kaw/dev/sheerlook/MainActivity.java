package net.kaw.dev.sheerlook;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.documentfile.provider.DocumentFile;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import net.kaw.dev.sheerlook.adapters.MatchResultsAdapter;
import net.kaw.dev.sheerlook.ai.MatchResult;
import net.kaw.dev.sheerlook.analysis.LevenshteinMatch;
import net.kaw.dev.sheerlook.files.FileSearch;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;

    private DocumentFile selectedDirectory = DocumentFile.fromFile(Environment.getExternalStorageDirectory());

    private final ActivityResultLauncher<Uri> chooseFolderActivity = registerForActivityResult(
            new ActivityResultContracts.OpenDocumentTree(),
            uri -> {
                if (uri == null) {
                    return;
                }

                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                DocumentFile documentFile = DocumentFile.fromTreeUri(this, uri);

                if (documentFile == null || !documentFile.isDirectory()) {
                    Toast.makeText(
                            this,
                            getResources().getString(R.string.invalid_folder_error), Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                selectedDirectory = documentFile;
                TextView folderPath = findViewById(R.id.folderPath);

                folderPath.setText(
                        String.format(
                                getResources().getString(R.string.folder_path_current),
                                selectedDirectory.getUri()
                        )
                );
            }
    );

    private final List<MatchResult> results = new ArrayList<>();

    private MatchResultsAdapter adapter;

    private EditText promptInput;
    private Button searchBtn;
    private ListView fileList;
    private TextView folderPath;
    private Button chooseFolderBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        promptInput = findViewById(R.id.promptInput);
        searchBtn = findViewById(R.id.searchBtn);
        fileList = findViewById(R.id.fileList);
        folderPath = findViewById(R.id.folderPath);
        chooseFolderBtn = findViewById(R.id.folderPickerBtn);

        folderPath.setText(
                String.format(
                        getResources().getString(R.string.folder_path_current),
                        selectedDirectory.getUri()
                )
        );

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }

        chooseFolderBtn.setOnClickListener(v -> {
            chooseFolderActivity.launch(null);
        });

        searchBtn.setOnClickListener(v -> {
            String prompt = promptInput.getText().toString().trim();

            List<DocumentFile> files = FileSearch.search(selectedDirectory);

            results.clear();

            for (DocumentFile file : files) {
                results.add(new MatchResult(file, LevenshteinMatch.match(file.getName(), prompt), ""));
            }

            results.sort((a, b)
                    -> Double.compare(b.getMatchPercentage(), a.getMatchPercentage()));

            adapter = new MatchResultsAdapter(MainActivity.this, results);
            fileList.setAdapter(adapter);

            RequestQueue volleyQueue = Volley.newRequestQueue(this);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    API_URL,
                    null, // TODO: build JSON
                    response -> {
                        try {
                            if (response.has("choices")) {
                                JSONArray choices = response.getJSONArray("choices");
                                if (choices.length() <= 0) {
                                    //JSONObject choice = choices.get(0).getAsJsonObject();
                                    //return choice.getJSONObject("message").get("content").getAsString();
                                }
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    error -> {
                        Toast.makeText(this, "Some error occurred! Cannot fetch response", Toast.LENGTH_LONG).show();
                        Log.e("DeepSeekApiClient", "sendMessage error: ${error.localizedMessage}");
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Authorization", "Bearer " + apiKey);
                    params.put("Content-Type", "application/json");
                    return params;
                }
            };

            //volleyQueue.add(jsonObjectRequest);
        });

        fileList.setOnItemClickListener((parent, view, position, id) -> {
            MatchResult result = results.get(position);
            this.openFile(result.getFile());
        });
    }

    private String getMimeType(String filename) {
        String extension = FilenameUtils.getExtension(filename);

        switch (extension) {
            case "pdf":
                return "application/pdf";
            case "txt":
                return "text/plain";
        }

        return "*/*";
    }

    private void openFile(DocumentFile file) {
        try {
            Uri uri = file.getUri();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, getMimeType(file.getName()));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            this.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, this.getResources().getString(R.string.toast_no_app_error), Toast.LENGTH_SHORT).show();
        }
    }

    private static final String API_URL = "https://api.deepseek.com/chat/completions";
    private static final String MODEL_NAME = "deepseek-chat";
    private static final String apiKey = "";

    private JSONObject buildJsonBody(String message) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return null;
        }

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.append("model", MODEL_NAME);

            JSONArray messages = new JSONArray();

            JSONObject systemMessage = new JSONObject();
            systemMessage.append("role", "system");
            systemMessage.append("content", "You are a helpful assistant.");
            messages.put(systemMessage);

            JSONObject userMessage = new JSONObject();
            userMessage.append("role", "user");
            userMessage.append("content", message);
            messages.put(userMessage);

            jsonObject.append("messages", messages);

            return jsonObject;
        } catch (JSONException e) {
        }

        return null;
    }

    private String readTextFromUri(Uri uri) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream =
                     getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        return stringBuilder.toString();
    }
}
