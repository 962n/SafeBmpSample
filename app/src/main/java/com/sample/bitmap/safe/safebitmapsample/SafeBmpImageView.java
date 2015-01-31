package com.sample.bitmap.safe.safebitmapsample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

/**
 * 設定されたBitmapが破棄されていた場合に再度画像の取得を行うImageViewクラス
 * (注意)ImageLoaderライブラリを使用することが前提
 */
public class SafeBmpImageView extends ImageView {

    //setImageBitmapで設定されたBitmapが破棄されたかどうかを監視するための保持用の変数
    private Bitmap mBitmap = null;

    //Bitmapが破棄された場合に再取得するためのURL格納用
    private String mUrl = null;

    public SafeBmpImageView(Context context) {
        super(context);
    }

    public SafeBmpImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 画像が破棄された場合に再取得するための画像のURLを設定します。
     * @param url 画像のURL
     */
    public void setRetryBmpURL(String url){
        this.mUrl = url;
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        mBitmap = bm;
        super.setImageBitmap(bm);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if(mBitmap != null && mBitmap.isRecycled()){
            //Bitmapが破棄されていた場合、ImageViewの初期化を行い、ImageLoaderで再取得を行う
            this.setImageBitmap(null);
            ImageLoader loader = ImageLoader.getInstance();
            loader.displayImage(mUrl,this);
            return;
        }
        //Bitmapが破棄されていない場合、もしくはBitmapが設定されていない場合は通常の描画処理
        super.onDraw(canvas);
    }
}
