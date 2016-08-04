package com.zlq.imageloader.library;

import android.content.Context;

import java.io.InputStream;

public interface UrlDownloader {
	
    public static interface UrlDownloaderCallback {
        public void onDownloadComplete(UrlDownloader downloader, InputStream in, String filename, long fileTotalSize);
    }
    
    public void download(Context context, String url, String filename, UrlDownloaderCallback callback, ZImageLoader.CompleteListener completion);
    public boolean allowCache();
    public boolean canDownloadUrl(String url);
}