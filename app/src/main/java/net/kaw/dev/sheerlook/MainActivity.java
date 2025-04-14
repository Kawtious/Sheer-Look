package net.kaw.dev.sheerlook;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int REQUEST_CODE_PICK_FOLDER = 200;
    private File selectedDirectory = Environment.getExternalStorageDirectory();

    private EditText promptInput;
    private Button chooseFolderBtn;
    private TextView folderPath;
    private Button searchBtn;
    private ListView fileList;
    private FileListAdapter adapter;
    private List<FileMatchResult> results;

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
                        selectedDirectory.getAbsolutePath()
                )
        );

        chooseFolderBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, REQUEST_CODE_PICK_FOLDER);
        });

        searchBtn.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }

            search();
        });

        fileList.setOnItemClickListener((parent, view, position, id) -> {
            FileMatchResult result = results.get(position);
            FileManager.openFile(this, result.getFile());
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != REQUEST_CODE_PICK_FOLDER || resultCode != RESULT_OK) {
            return;
        }

        Uri uri = data.getData();

        if (uri == null) {
            return;
        }

        File file = FileUtil.uriToFile(this, uri);

        if (file == null || !file.isDirectory()) {
            Toast.makeText(
                    this,
                    getResources().getString(R.string.toast_no_app_error), Toast.LENGTH_SHORT
            ).show();
            return;
        }

        selectedDirectory = file;
        TextView folderPath = findViewById(R.id.folderPath);

        folderPath.setText(
                String.format(
                        getResources().getString(R.string.folder_path_current),
                        selectedDirectory.getAbsolutePath()
                )
        );
    }

    private void search() {
        String prompt = promptInput.getText().toString().trim();

        if (prompt.isEmpty()) {
            Toast.makeText(
                    this,
                    getResources().getString(R.string.toast_prompt_empty), Toast.LENGTH_SHORT
            ).show();
            return;
        }

        results = FileSearcher.search(selectedDirectory, prompt);
        Log.d("debug", results.toString());
        adapter = new FileListAdapter(MainActivity.this, results);
        fileList.setAdapter(adapter);
    }
}
