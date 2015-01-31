Bitmapのメモリ対策用のサンプルプロジェクトです。
これはAndroid-Universal-Image-Loaderライブラリで画像を取得することを前提としたものになっています。

■コアクラスは以下です。

LruCacheMemoryWithRecycle.java
 →ImageLoaderクラス標準のMemoryCacheではBitmapのrecycle(破棄)メソッドを呼んでいません。
  そのためキャッシュから削除されてもGCが走るまでBitmapオブジェクトが浮遊します。
  これによりOOM発生の確率があがります。
  そのため、独自でCacheクラスを作成し、Cacheから削除された場合にBitmapのrecycleをメソッドを呼ぶように
  しています。明示的にBitmapを破棄することとでメモリの使用量を押さえるようにしています。

SafeBmpImageView.java
 →例えば以下のようなコードをくむとExceptionが発生し、アプリが強制終了します。
  —————
  ImageView image = new ImageView(Context);
  image.setImageBitmap(bmp);
  parent.addView(image);
  bmp.recycle();
  —————
  recycle(破棄)されたBitmapをImageViewに設定しているとFrameWork側でExceptionをはくからです。
  CacheクラスでBitmapを破棄するようにした結果、発生する弊害です。
  (アプリの画面スタックなどによっては発生しませんが、対策をたてることがベターなものです)
  対策としてはカスタマイズしたImageViewを用意し、自クラスに設定されたBitmapがrecycleされているか
  どうかを監視します。
  そして、Bitmapが破棄されていると検知した場合に再度ImageLoaderを使用して画像を取得するようにします。
  (再取得処理をいれないと何も表示されないため)
  

■自分のアプリに組み込みたい場合にやること
　①LruCacheMemoryWithRecycle.javaをImageLoaderConfigurationでImageLoaderで使用する
   メモリキャッシュに設定します。
　②ImageLoaderを使って画像を設定しているすべてのImageViewに対してSafeBmpImageViewを使うように修正します。
   ImageLoaderで画像を取得する場合は必ずその前にSafeBmpImageView#setRetryBmpURL()を呼び出し、再取得するための
   画像URLを設定するようにしてください。(以下のコード参照)
   —————
   SafeBmpImageView image = new SafeBmpImageView(context);
   image.setRetryBmpURL(“http://hogehoge”);
   ImageLoader.getInstance().displayImage(“http://hogehoge”,image);
   —————
