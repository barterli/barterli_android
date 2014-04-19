
package li.barter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import li.barter.R;
import li.barter.models.CropOption;

/**
 * Adapter for crop option list.
 * 
 * @author Sharath Pandeshwar
 */
public class CropOptionAdapter extends ArrayAdapter<CropOption> {
    private final ArrayList<CropOption> mOptions;
    private final LayoutInflater        mInflater;

    public CropOptionAdapter(final Context context, final ArrayList<CropOption> options) {
        super(context, R.layout.crop_selector, options);

        mOptions = options;

        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(final int position, View convertView,
                    final ViewGroup group) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.crop_selector, null);
        }

        final CropOption item = mOptions.get(position);

        if (item != null) {
            ((ImageView) convertView.findViewById(R.id.iv_icon))
                            .setImageDrawable(item.icon);
            ((TextView) convertView.findViewById(R.id.tv_name))
                            .setText(item.title);

            return convertView;
        }

        return null;
    }
}
