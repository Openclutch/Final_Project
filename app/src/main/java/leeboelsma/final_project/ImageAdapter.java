/*
* File name: ImageAdapter.java
* Author: Edward Ding #040078518
* Course: CST2335
* Assignment: Final Project
* Date: April 27, 2017
* Professor: David Lareau
* Purpose: Helper display file for photo app
* Class list: MainEdActivity, DisplayActivity, MyHelper
*/
/**
 * class ImageAdapter
 * @author Eding
 * @version 1
 */
package leeboelsma.final_project;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by HP on 4/15/2017.
 */

public class ImageAdapter extends BaseAdapter {
    private Context context;
    ArrayList<Bitmap> bitmaps;

    public ImageAdapter(Context context, ArrayList<Bitmap> bitmaps) {
        this.context = context;
        this.bitmaps = bitmaps;
    }

    public int getCount() {
        return bitmaps.size();
    }

    public Object getItem(int position) {
        return bitmaps.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }
        Bitmap bm = bitmaps.get(position);
        imageView.setImageBitmap(bm);
        return imageView;
    }
}