package li.barter.adapters;

import java.util.Locale;

import li.barter.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BooksAroundMeAdapter extends BaseAdapter {

	private static final int COUNT = 20;

	private static final String FORMAT = "Book #%d";

	@Override
	public int getCount() {
		return COUNT;
	}

	@Override
	public Object getItem(int position) {
		return String.format(Locale.US, FORMAT, position + 1);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View view = convertView;

		if (convertView == null) {
			view = LayoutInflater.from(parent.getContext()).inflate(
					R.layout.layout_item_book, null);
			view.setTag(R.id.text_book_name,
					view.findViewById(R.id.text_book_name));
		}

		((TextView) view.getTag(R.id.text_book_name))
				.setText((String) getItem(position));
		return view;
	}

}
