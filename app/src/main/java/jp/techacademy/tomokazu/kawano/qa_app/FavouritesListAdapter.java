package jp.techacademy.tomokazu.kawano.qa_app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class FavouritesListAdapter extends BaseAdapter {
    private LayoutInflater mLayoutInflater = null;
    private ArrayList<Question> mFavouriteArrayList;

    public FavouritesListAdapter(Context context) {
        mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return mFavouriteArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return mFavouriteArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_favourites, parent, false);
        }

        TextView titleText = (TextView) convertView.findViewById(R.id.fav_titleTextView);
        titleText.setText(mFavouriteArrayList.get(position).getTitle());

        TextView nameText = (TextView) convertView.findViewById(R.id.fav_nameTextView);
        nameText.setText(mFavouriteArrayList.get(position).getName());

        TextView resText = (TextView) convertView.findViewById(R.id.fav_resTextView);
        int resNum = mFavouriteArrayList.get(position).getAnswers().size();
        resText.setText(String.valueOf(resNum));

        byte[] bytes = mFavouriteArrayList.get(position).getImageBytes();
        if (bytes.length != 0) {
            Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length).copy(Bitmap.Config.ARGB_8888, true);
            ImageView imageView = (ImageView) convertView.findViewById(R.id.fav_imageView);
            imageView.setImageBitmap(image);
        }

        return convertView;
    }

    public void setFavouriteArrayList(ArrayList<Question> favouriteArrayList) {
        mFavouriteArrayList = favouriteArrayList;
    }
}
