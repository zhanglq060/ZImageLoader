package com.zlq.imageloader.library;

import android.content.Context;
import android.os.Process;

import java.io.File;
import java.net.URI;

public class FileUrlDownloader implements UrlDownloader {
	
    @Override
    public void download(final Context context, final String url, final String filename, final UrlDownloaderCallback callback, final ZImageLoader.CompleteListener completion) {
    	
    	ZImageLoader.excuteThread(new Runnable() {
			@Override
			public void run() {
				Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

				try {
                    callback.onDownloadComplete(FileUrlDownloader.this, null, new File(new URI(url)).getAbsolutePath(), new File(new URI(url)).length());
                }
                catch (final Throwable e) {
                    e.printStackTrace();
                }
				ZImageLoader.getMainHandler().post(new Runnable() {
					@Override
					public void run() {
						completion.onCompelte();
					}
				});
			}
		});
    }

    @Override
    public boolean allowCache() {
        return false;
    }
    
    @Override
    public boolean canDownloadUrl(String url) {
    	return url.startsWith("file:/");
    	
    }
}
