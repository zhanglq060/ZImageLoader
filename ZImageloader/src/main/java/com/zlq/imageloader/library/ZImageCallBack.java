package com.zlq.imageloader.library;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Created by zhanglq on 15/7/5.
 */
public abstract class ZImageCallBack {

	public abstract void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache);
	public void onLoadProgress(long curValue, long totalSize){

	}

}
