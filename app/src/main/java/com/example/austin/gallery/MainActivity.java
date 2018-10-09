package com.example.austin.gallery;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.database.Cursor;
import java.io.File;
import java.util.ArrayList;
import android.os.Build;
import android.Manifest;
import android.content.pm.PackageManager;
import android.util.LruCache;
import android.content.Intent;

public class MainActivity extends Activity {

    GridView gallery; //My gridview
    public LruCache<String, Bitmap> myCache; //Caching
    public int maxMemory;
    public int cacheSize;

    public int currentPosition; //used to store current position of the view, so when activity recreated it will stay on where it was
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Ask for permission
        if(Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        //Cache set up
        maxMemory = (int)(Runtime.getRuntime().maxMemory() / 1024);
        cacheSize = maxMemory / 3;
        myCache =  new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
        currentPosition = 0;
        gallery = findViewById(R.id.pictures);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    /*Initialise function to set adapters to gridview*/
    public void init(){
        final GalleryAdaptor adp = new GalleryAdaptor(myCache);
        adp.myCursor.moveToFirst();
        /*setting adapters*/
        gallery.setAdapter(adp);
        gallery.setSelection(currentPosition);
        /*setting on item click listener*/
        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*Create a new intent which will open a new activity to show signle photo*/
                currentPosition = gallery.getFirstVisiblePosition();
                Intent myIntent = new Intent(MainActivity.this, DisplayActivity.class);
                myIntent.putExtra("path", adp.paths.get(position)); /*pass the path of that image to the new activity*/
                startActivity(myIntent);
            }
        });
    }

    public class GalleryAdaptor extends BaseAdapter {

        class ViewHolder{
            int position;
            ImageView image;
        }

        public Cursor myCursor;
        public ArrayList<String> paths; //used for storing the path to the image
        private int[] degree; //used for rotation
        private LruCache<String, Bitmap> myCache; //cache
        public GalleryAdaptor(LruCache<String, Bitmap> cache){

            myCache = cache;
            /*Using content provide to load all images*/
            myCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media.ORIENTATION}, null, null,
                    "date_added DESC");
            paths = new ArrayList<String>();
            degree = new int[myCursor.getCount()];
            int i = 0;
            while(myCursor.moveToNext()){
                /*add path to list*/
                paths.add(myCursor.getString(myCursor.getColumnIndex("_data")));
                degree[i] = myCursor.getColumnIndex("orientation");
                i++;
            }
        }

        @Override
        public int getCount() {
            return paths.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder vh;

            if (convertView == null) {
                /*Convert view is null, inflate it*/
                convertView = getLayoutInflater().inflate(R.layout.picture, parent, false);
                vh = new ViewHolder();
                vh.image = (ImageView) convertView.findViewById(R.id.picture);
                /*add view holder to the tag*/
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
                vh.position = position;
                vh.image.setImageBitmap(null);
                /*start a new async task*/
                new AsyncTask<ViewHolder, Integer, Bitmap>() {
                    private ViewHolder vh;
                    @Override
                    protected Bitmap doInBackground(ViewHolder...parms) {
                        vh = parms[0];
                        Bitmap bit = null;
                        try {
                            bit = myCache.get(paths.get(position));
                            if(bit != null){
                                /*this bitmap is already in cache, return it*/
                                return bit;
                            }
                            /*bitmap not in cache, load the image*/
                            File imgFile = new File(paths.get(position));
                            if(imgFile.exists()) {
                                /*load with low resolution thumbnail*/
                                BitmapFactory.Options option = new BitmapFactory.Options();
                                option.inSampleSize = 1;
                                bit = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), option);
                                bit = ThumbnailUtils.extractThumbnail(bit, vh.image.getWidth(), vh.image.getHeight());
                                myCursor.moveToNext();
                            } else {
                                Log.i("err", "File doesnt exist");
                                return null;
                            }
                        } catch (Exception e) {
                            Log.i("Exc", "Found Exc");
                            e.printStackTrace();
                        }
                        myCache.put(paths.get(position), bit);
                        return bit;
                    }

                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        if(bitmap != null) {
                            vh.image.setImageBitmap(bitmap);

                        }
                    }
                }.execute(vh);

                return convertView;
            }

        }
    }



