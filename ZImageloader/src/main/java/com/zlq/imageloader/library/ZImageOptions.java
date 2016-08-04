package com.zlq.imageloader.library;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * Created by zhanglq on 15/7/5.
 */
public class ZImageOptions {

	public Context mContext;
	public String url;
	public long cacheDuring = ZImageLoader.CACHE_DURATION_THREE_DAYS;
	public int mDefaultDrawableId;
	public Drawable mDefaultDrawable;
	public Bitmap mDefaultBitmap;
	public ZImageCallBack mCallback;
	public ImageView mImageView;

	public ZImageOptions(Context context){
		this.mContext = context;
	}

	public ZImageOptions setImageUrl(String url){
		this.url = url;
		return this;
	}

	public ZImageOptions setCacheDuring(long during){
		this.cacheDuring = during;
		return this;
	}

	public ZImageOptions setDefaultDrawableId(int defaultDrawableId){
		this.mDefaultDrawableId = defaultDrawableId;
		return this;
	}

	public ZImageOptions setDefaultDrawable(Drawable defaultDrawable){
		this.mDefaultDrawable = defaultDrawable;
		return this;
	}

	public ZImageOptions setDefaultBitmap(Bitmap defaultBitmap){
		this.mDefaultBitmap = defaultBitmap;
		return this;
	}

	public ZImageOptions setImageCallback(ZImageCallBack callback){
		this.mCallback = callback;
		return this;
	}

	public ZImageOptions setTargetImageView(ImageView imageView){
		this.mImageView = imageView;
		return this;
	}

	public void load(){
		ZImageLoader.load(this);
	}

	public Drawable getDefaultDrawable(){
		if (mDefaultDrawableId > 0){
			return mContext.getResources().getDrawable(mDefaultDrawableId);
		}else if (mDefaultDrawable != null){
			return mDefaultDrawable;
		}else if(mDefaultBitmap != null){
			return new BitmapDrawable(mContext.getResources(), mDefaultBitmap);
		}
		return null;
	}
}
