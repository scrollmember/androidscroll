
package jp.ac.tokushima_u.is.ll.sphinx.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jp.ac.tokushima_u.is.ll.R;
import jp.ac.tokushima_u.is.ll.sphinx.classes.Quiz;
import jp.ac.tokushima_u.is.ll.util.ApiConstants;
import jp.ac.tokushima_u.is.ll.util.BitmapUtil;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.GridLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class QuizDetailAdapter extends ArrayAdapter<Quiz> {

    @SuppressWarnings("unused")
    private static final String TAG = QuizDetailAdapter.class.getSimpleName();

    private ArrayList<Quiz> items;
    private Map<String, Bitmap> bitmapMap;
    private LayoutInflater inflater;
    private int textViewResourceId;

    public QuizDetailAdapter(Context context, int textViewResourceId, ArrayList<Quiz> items) {
        super(context, textViewResourceId, items);

        bitmapMap = new HashMap<String, Bitmap>();

        this.items = items;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.textViewResourceId = textViewResourceId;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            view = inflater.inflate(textViewResourceId, null);
        }

        Quiz item = (Quiz) items.get(position);

        ImageView[] images = {
                (ImageView) view.findViewById(R.id.image1),
                (ImageView) view.findViewById(R.id.image2),
                (ImageView) view.findViewById(R.id.image3),
                (ImageView) view.findViewById(R.id.image4)
        };

        GridLayout imageGrid = (GridLayout) view.findViewById(R.id.imageGrid);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        TextView title = (TextView) view.findViewById(R.id.textView_title);
        TextView author = (TextView) view.findViewById(R.id.textView_author);
        TextView createdAt = (TextView) view.findViewById(R.id.textView_createdAt);

        title.setText(item.getName()[0]);
        if (item.getAuthor() != null) {
            author.setText("Author: " + item.getAuthor());
        } else {
            author.setText("");
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.JAPAN);
        String formatted = dateFormat.format(item.getCreatedAt());

        createdAt.setText(formatted);

        String[] imageUrl = item.getQuizId();

            for (int i = 0; i < 4; i++) {
                
                Bitmap bitmap = null;
                if(imageUrl != null){
                    bitmap = loadBitmap(getContext(), imageUrl[i]);
                }
                
                if (bitmap != null) {
                    images[i].setImageBitmap(bitmap);
                } else {
                    images[i].setImageResource(R.drawable.noimage);
                }
            }


        progressBar.setVisibility(View.GONE);
        imageGrid.setVisibility(View.VISIBLE);

        return view;
    }

    /**
     * ビットマップデータを読み込みます。 もしキャッシュのデータとヒットしたら、キャッシュを返します。
     * 
     * @param id
     * @return bitmap
     */
    private Bitmap loadBitmap(Context context, String id) {
        if (bitmapMap.get(id) != null) {
            // キャッシュが存在するのでそちらを返す
            //Log.d(TAG, "Cache HIT!");

            return bitmapMap.get(id);
        } else {
            // キャッシュが存在しないのでbitmaputilからロードしてキャッシュする
            //Log.d(TAG, "Cache Set...");

            Bitmap bm = BitmapUtil
                    .getBitmap(context, id, ApiConstants.SmallestSizePostfix);

            bitmapMap.put(id, bm);

            return bm;
        }
    }
}
