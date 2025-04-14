package net.kaw.dev.sheerlook;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class FileListAdapter extends ArrayAdapter<FileMatchResult> {
    public FileListAdapter(Context context, List<FileMatchResult> results) {
        super(context, 0, results);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        FileMatchResult result = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        assert result != null;

        ((TextView) convertView.findViewById(android.R.id.text1))
                .setText(result.getFile().getName());

        ((TextView) convertView.findViewById(android.R.id.text2))
                .setText(
                        String.format(
                                getContext().getResources().getString(R.string.file_list_match),
                                result.getMatchPercentage(),
                                result.getFile().getAbsolutePath()
                        )
                );

        return convertView;
    }
}
