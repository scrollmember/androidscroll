
package jp.ac.tokushima_u.is.ll.sphinx.adapter;

import java.util.ArrayList;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.sphinx.classes.LLO;
import jp.ac.tokushima_u.is.ll.util.ApiConstants;
import jp.ac.tokushima_u.is.ll.util.BitmapUtil;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * ListViewとデータをつなぐアダプター。
 * データ内容によってリストの表示の仕方を変えたりしてます
 */
public class LLOThumbnailAdapter extends ArrayAdapter<LLO> {

    @SuppressWarnings("unused")
    private static final String TAG = LLOThumbnailAdapter.class.getSimpleName();
    private final LLOThumbnailAdapter self = this;

    private ArrayList<LLO> items;
    private LayoutInflater inflater;
    private int textViewResourceId;

    public LLOThumbnailAdapter(Context context, int textViewResourceId, ArrayList<LLO> items) {
        super(context, textViewResourceId, items);

        this.items = items;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.textViewResourceId = textViewResourceId;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LLO item = items.get(position);

        if (!isEnabled(position)) {

            String text = item.getName().substring(2);

            TextView textView = new TextView(getContext());
            textView.setText(text);
            textView.setTextColor(Color.WHITE);
            textView.setBackgroundColor(Color.BLACK);
            textView.setTextSize(18);

            return textView;
        } else {
            View view = convertView;

            if (view == null) {
                view = inflater.inflate(textViewResourceId, null);
            }

            TextView name = (TextView) view.findViewById(R.id.name);
            ImageView image = (ImageView) view.findViewById(R.id.image);

            // TODO: ここでヌルポでやすい
            
            name.setText(item.getName());

            Bitmap bitmap = BitmapUtil
                    .getBitmap(parent.getContext(), item.getImage(),
                            ApiConstants.MiddleSizePostfix);
            if (bitmap != null) {
                image.setImageBitmap(bitmap);
            } else {
                image.setImageResource(R.drawable.noimage);
            }

            return view;
        }
    }

    @Override
    public boolean isEnabled(int position) {
        // TODO Auto-generated method stub
        return !(items.get(position).getName().startsWith("[#"));
    }
}
