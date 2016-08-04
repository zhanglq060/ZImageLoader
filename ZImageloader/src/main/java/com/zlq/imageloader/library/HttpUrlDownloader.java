package com.zlq.imageloader.library;

import android.content.Context;
import android.os.Process;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class HttpUrlDownloader implements UrlDownloader {

    @Override
    public void download(final Context context, final String url, final String filename, final UrlDownloaderCallback callback, final ZImageLoader.CompleteListener completion) {
    	
    	Runnable r = new Runnable() {
			@Override
			public void run() {
				Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
				try {
                    InputStream is = null;
                    String thisUrl = url;
                    HttpURLConnection urlConnection;
                    while (true) {
                        final URL u = new URL(thisUrl);
                        urlConnection = (HttpURLConnection)u.openConnection();
                        urlConnection.setInstanceFollowRedirects(true);

						if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_MOVED_TEMP
								&& urlConnection.getResponseCode() != HttpURLConnection.HTTP_MOVED_PERM){
							break;
						}
						thisUrl = urlConnection.getHeaderField("Location");
                    }

                    if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
						is = urlConnection.getInputStream();
						callback.onDownloadComplete(HttpUrlDownloader.this, is, null, urlConnection.getContentLength());
                    }
                }catch (final Throwable e) {
                    e.printStackTrace();
                }
				ZImageLoader.getMainHandler().post(new Runnable() {
					@Override
					public void run() {
						completion.onCompelte();
					}
				});
			}
		};
		ZImageLoader.excuteThread(r);
    }

    @Override
    public boolean allowCache() {
        return true;
    }
    
    @Override
    public boolean canDownloadUrl(String url) {
        return url.startsWith("http");
    }
}
