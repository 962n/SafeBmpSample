package com.sample.bitmap.safe.safebitmapsample;

import android.graphics.Bitmap;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    //true:普通のImageView false:カスタムView
    private boolean mIsNormalImage = true;

    private String mURL = "https://lh3.ggpht.com/embNgl_sfBmFB-5PslsKRmmlRrMIJCOzrI0kPFm5dOFYz9IPvG7T9B8CgdD3GpuOzv30=w300";
    private ArrayList<Bitmap> mBmpList = new ArrayList<Bitmap>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .memoryCache(LruMemoryCacheWithRecycle.createMemoryCache(this,0))
                .build();
        ImageLoader.getInstance().init(config);

        setContentView(R.layout.activity_main);

        final ViewGroup imageArea = (ViewGroup)this.findViewById(R.id.image_area);
        final TextView text = (TextView)this.findViewById(R.id.text);

        final Button imageChangeButton = (Button)this.findViewById(R.id.image_change_button);
        imageChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageArea.removeAllViews();
                ImageView image;
                for(int i = 0;i<30;i++){
                    if(mIsNormalImage){
                        image = new ImageView(getApplicationContext());
                        text.setText("通常のImageView");
                    } else {
                        SafeBmpImageView bmpImage = new SafeBmpImageView(getApplicationContext());
                        bmpImage.setRetryBmpURL(mURL);
                        image = bmpImage;
                        text.setText("カスタムImageView");
                    }
                    image.setLayoutParams(new ViewGroup.LayoutParams(200,200));
                    imageArea.addView(image);
                    ImageLoader.getInstance().displayImage(mURL,image,new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String s, View view) {}
                        @Override
                        public void onLoadingFailed(String s, View view, FailReason failReason) {}
                        @Override
                        public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                            Log.d("", "onLoadingComplete");
                            mBmpList.add(bitmap);
                        }
                        @Override
                        public void onLoadingCancelled(String s, View view) {}
                    });
                }

                mIsNormalImage = !mIsNormalImage;

            }
        });

        final Button cacheClearButton = (Button)this.findViewById(R.id.cache_clear_button);
        cacheClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageLoader.getInstance().clearMemoryCache();
                for(Bitmap bmp:mBmpList){
                    Log.d("", "onclick recycle");
                    bmp.recycle();
                }
                mBmpList.clear();
                imageArea.invalidate();
                imageArea.removeViewAt(0);
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
