package net.kaw.dev.sheerlook.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.kaw.dev.sheerlook.R;
import net.kaw.dev.sheerlook.ai.MatchResult;

import java.util.List;

public class MatchResultsAdapter extends ArrayAdapter<MatchResult> {
    public MatchResultsAdapter(Context context, List<MatchResult> results) {
        super(context, 0, results);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        MatchResult result = getItem(position);

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
                                result.getFile().getUri(),
                                result.getDescription()
                        )
                );

        return convertView;
    }
}
