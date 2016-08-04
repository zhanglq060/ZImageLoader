package com.zlq.imageloader.library;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by zhanglq on 15/7/5.
 */
public class ZImageLoader {

	public static final String TAG = "ZImageLoader";
	public static final boolean DEBUG = false;

	public static final String IMAGE_CACHE_DIR = "/cache/zimageloader/";

	public static final int CACHE_DURATION_INFINITE = Integer.MAX_VALUE;
	public static final int CACHE_DURATION_ONE_DAY = 1000 * 60 * 60 * 24;
	public static final int CACHE_DURATION_TWO_DAYS = CACHE_DURATION_ONE_DAY * 2;
	public static final int CACHE_DURATION_THREE_DAYS = CACHE_DURATION_ONE_DAY * 3;
	public static final int CACHE_DURATION_FOUR_DAYS = CACHE_DURATION_ONE_DAY * 4;
	public static final int CACHE_DURATION_FIVE_DAYS = CACHE_DURATION_ONE_DAY * 5;
	public static final int CACHE_DURATION_SIX_DAYS = CACHE_DURATION_ONE_DAY * 6;
	public static final int CACHE_DURATION_ONE_WEEK = CACHE_DURATION_ONE_DAY * 7;

	private static Handler mHandler = null;

	private static String CACHE_DIR_PATH;
	private static boolean mUseBitmapScaling = true;//是否优化图片缩小至 imageview大小
	private static ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
	private static DisplayMetrics mMetrics;

	private static Hashtable<ImageView, String> mPendingViews = new Hashtable<ImageView, String>();
	private static Hashtable<String, ArrayList<ImageView>> mPendingDownloads = new Hashtable<String, ArrayList<ImageView>>();

	private static DrawableCache mLiveCache = DrawableCache.getInstance();
	private static LruBitmapCache mDeadCache;
	private static HashSet<Bitmap> mAllCache = new HashSet<Bitmap>();

	private static HttpUrlDownloader mHttpDownloader = new HttpUrlDownloader();
	private static ContentUrlDownloader mContentDownloader = new ContentUrlDownloader();
	private static ContactContentUrlDownloader mContactDownloader = new ContactContentUrlDownloader();
	private static AssetUrlDownloader mAssetDownloader = new AssetUrlDownloader();
	private static FileUrlDownloader mFileDownloader = new FileUrlDownloader();
	private static ArrayList<UrlDownloader> mDownloaders = new ArrayList<UrlDownloader>();
	public static ArrayList<UrlDownloader> getDownloaders() {
		return mDownloaders;
	}

	static {
		mDownloaders.add(mHttpDownloader);
		mDownloaders.add(mContactDownloader);
		mDownloaders.add(mContentDownloader);
		mDownloaders.add(mAssetDownloader);
		mDownloaders.add(mFileDownloader);
	}

	public static void initCachePath(String cacheDirPath){
		if (cacheDirPath == null || cacheDirPath.length() == 0){
			return;
		}
		File cacheDirFile = new File(cacheDirPath);
		boolean mkdirSuccess = true;
		if (!cacheDirFile.exists()){
			mkdirSuccess = cacheDirFile.mkdirs();
		}
		if(mkdirSuccess) CACHE_DIR_PATH = cacheDirFile.getPath() + File.separator;
	}

	public static ZImageOptions with(Context context){
		ZImageOptions options = new ZImageOptions(context);
		return options;
	}

	//默认的图片缓存目录,如果要修改 请自行修改
	private static String initDefaultCacheDirPath(Context context){
		if (CACHE_DIR_PATH == null) {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				CACHE_DIR_PATH = getDiskCacheRootDirPath(context);
			}else {
				CACHE_DIR_PATH = getAppCacheRootDirPath(context);
			}
		}
		if (CACHE_DIR_PATH != null) {
			File cacheDirFile = new File(CACHE_DIR_PATH);
			if (!cacheDirFile.exists()) {
				cacheDirFile.mkdirs();
			}
		}
		return CACHE_DIR_PATH;
	}

	private static String getDiskCacheRootDirPath(Context context){
		return Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/Android/data/"+ context.getPackageName()
				+ IMAGE_CACHE_DIR;
	}

	private static String getAppCacheRootDirPath(Context context){
		return context.getCacheDir().getAbsolutePath()
				+ IMAGE_CACHE_DIR;
	}

	public static String getCacheDirPath(){
		return CACHE_DIR_PATH;
	}

	public static Bitmap getCacheBitmap(String url){
		if (mLiveCache.get(url) != null) return ((BitmapDrawable)mLiveCache.get(url)).getBitmap();
		if (mDeadCache.get(url) != null) return mDeadCache.get(url);
		return null;
	}

	public static String getCacheFileName(final String url) {
		if (url == null || url.equals("")) {
			return null;
		}
		MessageDigest alg = null;
		try {
			alg = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		alg.update(url.getBytes());
		byte[] digest=alg.digest();

		String hs = "";
		String stmp = "";
		for (int i = 0; i < digest.length; i++) {
			stmp = (Integer.toHexString(digest[i] & 0XFF));
			if (stmp.length() == 1)
				hs = hs + "0" + stmp;
			else
				hs = hs + stmp;
		}
		return hs.toLowerCase(Locale.CHINA);
	}

	public static String getCacheImagePath(String url){
		if (CACHE_DIR_PATH == null){
			return null;
		}
		return CACHE_DIR_PATH + getCacheFileName(url);
	}

	public static void cleanImageCache(Context context){
		cleanImageCache(context, 0);
	}

	public static void cleanImageCache(Context context, long age){
		String diskCacheDir = getDiskCacheRootDirPath(context);
		String appCacheDir = getAppCacheRootDirPath(context);

		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
			File diskCacheDirFile = new File(diskCacheDir);
			if (!diskCacheDirFile.exists()){
				diskCacheDirFile.mkdirs();
				return;
			}
			final String[] diskFiles = diskCacheDirFile.list();
			if(diskFiles != null && diskFiles.length > 0){
				for(final String fileName : diskFiles){
					final File f = new File(diskCacheDir + fileName);
					if (System.currentTimeMillis() > f.lastModified() + age) {
						f.delete();
					}
				}
			}
		}

		File appCacheDirFile = new File(appCacheDir);
		if (!appCacheDirFile.exists()){
			appCacheDirFile.mkdirs();
			return;
		}
		final String[] appFiles = appCacheDirFile.list();
		if(appFiles != null && appFiles.length > 0){
			for(final String fileName : appFiles){
				final File f = new File(appCacheDir + fileName);
				if (System.currentTimeMillis() > f.lastModified() + age) {
					f.delete();
				}
			}
		}
	}

	private static boolean checkCacheDuration(File file, long cacheDurationMs) {
		return cacheDurationMs == CACHE_DURATION_INFINITE || System.currentTimeMillis() < file.lastModified() + cacheDurationMs;
	}

	public static void load(final ZImageOptions options){

		if (options == null){
			return;
		}else if(Looper.getMainLooper().getThread() != Thread.currentThread()){
			return;
		}

		//初始化默认缓存目录
		initDefaultCacheDirPath(options.mContext);

		// disassociate this ImageView from any pending downloads
		if (options.url == null || options.url.length() == 0) {
			if (options.mImageView != null) {
				mPendingViews.remove(options.mImageView);
				options.mImageView.setImageDrawable(options.getDefaultDrawable());
			}
			return;
		}

		if (mHandler == null){
			mHandler = new Handler();
		}

		final int displayImageWidth;
		final int displayImageHeight;
		if (mMetrics == null){
			mMetrics = new DisplayMetrics();
			((WindowManager) options.mContext.getSystemService(Context.WINDOW_SERVICE))
					.getDefaultDisplay().getMetrics(mMetrics);
		}
		displayImageWidth = options.mImageView.getWidth() > 0 ? options.mImageView.getWidth() : mMetrics.widthPixels;
		displayImageHeight = options.mImageView.getHeight() > 0 ? options.mImageView.getHeight() : mMetrics.heightPixels;

		final String tartFilePath = CACHE_DIR_PATH + getCacheFileName(options.url);
		final File targetFile = new File(tartFilePath);

		// check the dead and live cache to see if we can find this url's bitmap
		if (mDeadCache == null) {
			int maxLruCacheSize = ((ActivityManager)options.mContext.getSystemService(Context.ACTIVITY_SERVICE))
					.getMemoryClass() * 1024 * 1024 / 8;
			mDeadCache = new LruBitmapCache(maxLruCacheSize);
		}
		Drawable drawable = null;
		Bitmap bitmap = mDeadCache.remove(options.url);
		if (bitmap == null){
			drawable = mLiveCache.get(options.url);
		}

		// if something was found, verify it was fresh.
		if (drawable != null || bitmap != null) {
			if (targetFile.exists() && !checkCacheDuration(targetFile, options.cacheDuring)) {
				if (drawable != null && drawable instanceof ZombieDrawable){
					((ZombieDrawable)drawable).headshot();
				}
				drawable = null;
				bitmap = null;
			}
		}

		// if the bitmap is fresh, set the imageview
		if (drawable != null || bitmap != null) {
			if (options.mImageView != null) {
				mPendingViews.remove(options.mImageView);
				if (drawable instanceof ZombieDrawable)
					drawable = ((ZombieDrawable)drawable).clone(options.mContext.getResources());
				else if (bitmap != null)
					drawable = new ZombieDrawable(options.url, options.mContext.getResources(), bitmap);
				options.mImageView.setImageDrawable(drawable);
			}
			// invoke any bitmap callbacks
			if (options.mCallback != null) {
				if (bitmap == null && drawable instanceof ZombieDrawable){
					bitmap = ((ZombieDrawable)drawable).getBitmap();
				}
				options.mCallback.onLoaded(options.mImageView, bitmap, options.url, true);
			}
			return;
		}

		if (options.mImageView != null){
			options.mImageView.setImageDrawable(options.getDefaultDrawable());
			mPendingViews.put(options.mImageView, options.url);
		}

		final ArrayList<ImageView> currentDownload = mPendingDownloads.get(options.url);
		if (currentDownload != null && currentDownload.size() != 0) {
			if (options.mImageView != null) {
				currentDownload.add(options.mImageView);
			}
			return;
		}

		final ArrayList<ImageView> downloads = new ArrayList<ImageView>();
		if (options.mImageView != null) {
			downloads.add(options.mImageView);
		}
		mPendingDownloads.put(options.url, downloads);

		final int targetWidth = displayImageWidth <= 0 ? Integer.MAX_VALUE : displayImageWidth;
		final int targetHeight = displayImageHeight <= 0 ? Integer.MAX_VALUE : displayImageHeight;

 		final Loader loader = new Loader(tartFilePath, options, targetWidth, targetHeight);
		final CompleteListener completeListener = new CompleteListener(options, loader);

		if (targetFile.exists()){
			try {
				if (checkCacheDuration(targetFile, options.cacheDuring)) {
					threadPool.execute(new Runnable() {
						@Override
						public void run() {
							Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
							loader.onDownloadComplete(null, null, tartFilePath, targetFile.length());
							mHandler.post(new Runnable() {
								@Override
								public void run() {
									completeListener.onCompelte();
								}
							});
						}
					});
					return;
				}
			}catch (final Exception ex) {
				ex.printStackTrace();
			}
		}

		for (UrlDownloader downloader: mDownloaders) {
			if (downloader.canDownloadUrl(options.url)) {
				try {
					downloader.download(options.mContext, options.url, tartFilePath, loader, completeListener);
				} catch (Exception e) {
					mPendingDownloads.remove(options.url);
					if (options.mImageView != null) {
						mPendingViews.remove(options.mImageView);
					}
				}
				return;
			}
		}
		options.mImageView.setImageDrawable(options.getDefaultDrawable());
	}

	private static class Loader implements UrlDownloader.UrlDownloaderCallback{

		private String filename;
		public Bitmap result;
		public ZImageOptions options;
		public int targetWidth;
		public int targetHeight;

		public Loader(String filename, ZImageOptions options, int targetWidth, int targetHeight){
			this.filename = filename;
			this.options = options;
			this.targetWidth = targetWidth;
			this.targetHeight = targetHeight;
		}
		@Override
		public void onDownloadComplete(UrlDownloader downloader, InputStream in, String existingFilename, final long fileTotalSize) {
			try {
				int READ_BUFFER_SIZE = 8192;
				if (in == null && existingFilename == null) return;
				String targetFilename = filename;
				if (in != null) {
					in = new BufferedInputStream(in, READ_BUFFER_SIZE);
					OutputStream fout = new BufferedOutputStream(new FileOutputStream(filename), READ_BUFFER_SIZE);
					final byte[] stuff = new byte[READ_BUFFER_SIZE];
					int read;
					int total = 0;
					while ((read = in.read(stuff)) != -1)
					{
						fout.write(stuff, 0, read);
						total += read;
						final int curValue = total;
						if(options.mCallback != null){
							mHandler.post(new Runnable() {
								@Override
								public void run() {
									options.mCallback.onLoadProgress(curValue, fileTotalSize);
								}
							});
						}
					}
					fout.close();
				}else {
					targetFilename = existingFilename;
				}
				result = loadBitmapFromStream(options.mContext, options.url, targetFilename, targetWidth, targetHeight);
			}catch (final Exception ex) {
				new File(filename).delete();
			}finally {
				// if we're not supposed to cache this thing, delete the temp file.
				if (downloader != null && !downloader.allowCache()){
					new File(filename).delete();
				}
			}
		}
	}

	public static class CompleteListener{

		private ZImageOptions options;
		private Loader loader;

		public CompleteListener(ZImageOptions options, Loader loader){
			this.options = options;
			this.loader = loader;
		}

		public void onCompelte(){
			if(!Looper.myLooper().equals(Looper.getMainLooper())){
				return;
			}
			Bitmap bitmap = loader.result;
			Drawable usableResult = null;
			if (bitmap != null) {
				usableResult = new ZombieDrawable(options.url, options.mContext.getResources(), bitmap);
			}
			if (usableResult == null) {
				usableResult = options.getDefaultDrawable();
				mLiveCache.put(options.url, usableResult);
			}
			ArrayList<ImageView> downloads = mPendingDownloads.remove(options.url);
			int waitingCount = 0;
			for (final ImageView iv: downloads) {
				final String pendingUrl = mPendingViews.get(iv);
				if (!options.url.equals(pendingUrl)) {
					continue;
				}
				waitingCount++;
				mPendingViews.remove(iv);
				if (usableResult != null) {
					iv.setImageDrawable(usableResult);
				}
				if (options.mCallback != null && iv == options.mImageView){
					options.mCallback.onLoaded(iv, loader.result, options.url, false);
				}
			}
		}
	}

	private static class ZombieDrawable extends BitmapDrawable {
		private static class Brains {
			int mRefCounter;
			boolean mHeadshot;
		}
		public ZombieDrawable(final String url, Resources resources, final Bitmap bitmap) {
			this(url, resources, bitmap, new Brains());
		}

		Brains mBrains;
		private ZombieDrawable(final String url, Resources resources, final Bitmap bitmap, Brains brains) {
			super(resources, bitmap);
			mUrl = url;
			mBrains = brains;

			mAllCache.add(bitmap);
			mDeadCache.remove(url);
			mLiveCache.put(url, this);

			mBrains.mRefCounter++;
		}

		public ZombieDrawable clone(Resources resources) {
			return new ZombieDrawable(mUrl, resources, getBitmap(), mBrains);
		}

		String mUrl;

		@Override
		protected void finalize() throws Throwable {
			super.finalize();

			mBrains.mRefCounter--;
			if (mBrains.mRefCounter == 0) {
				if (!mBrains.mHeadshot){
					mDeadCache.put(mUrl, getBitmap());
				}
				mAllCache.remove(getBitmap());
				mLiveCache.remove(mUrl);
			}
			if (DEBUG) Log.i(TAG, "ZombieDrawable finalize Refcounter = " + mBrains.mRefCounter);
		}

		// kill this zombie, forever.
		public void headshot() {
			mBrains.mHeadshot = true;
			mLiveCache.remove(mUrl);
			mAllCache.remove(getBitmap());
		}
	}

	private static Bitmap loadBitmapFromStream(final Context context, final String url, final String filename, final int targetWidth, final int targetHeight) {

		InputStream stream = null;
		try {
			BitmapFactory.Options o = null;
			if (mUseBitmapScaling) {
				o = new BitmapFactory.Options();
				o.inJustDecodeBounds = true;
				stream = new BufferedInputStream(new FileInputStream(filename), 8192);

				Bitmap bm = BitmapFactory.decodeStream(stream, null, o);
				if(bm != null && !bm.isRecycled()){
					bm.recycle();
				}

				stream.close();
				int scale = 0;

				if(o.outWidth < o.outHeight){
					while ((o.outWidth >> scale) > targetWidth) {
						scale++;
					}
				}else{
					while ((o.outHeight >> scale) > targetHeight) {
						scale++;
					}
				}
				o = new BitmapFactory.Options();
				o.inSampleSize = 1 << scale;
			}
			stream = new BufferedInputStream(new FileInputStream(filename), 8192);
			final Bitmap bitmap = BitmapFactory.decodeStream(stream, null, o);
			return bitmap;
		} catch (final IOException e) {
			return null;
		}catch(OutOfMemoryError oom){
			return null;
		}
		finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					if (DEBUG) Log.w(TAG, "Failed to close FileInputStream",e);
				}
			}
		}
	}

	public static void excuteThread(Runnable runnable){
		threadPool.execute(runnable);
	}

	public static Handler getMainHandler(){
		return mHandler;
	}
}
