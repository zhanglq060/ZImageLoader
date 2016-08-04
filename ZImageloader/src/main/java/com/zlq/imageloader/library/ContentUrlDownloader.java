package com.zlq.imageloader.library;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import java.io.InputStream;

public class ContentUrlDownloader implements UrlDownloader {
    @Override
    public void download(final Context context, final String url, final String filename, final UrlDownloaderCallback callback, final ZImageLoader.CompleteListener completion) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    final ContentResolver cr = context.getContentResolver();
                    InputStream is = cr.openInputStream(Uri.parse(url));
                    callback.onDownloadComplete(ContentUrlDownloader.this, is, null, 0);
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
        };
        ZImageLoader.excuteThread(runnable);
    }

    @Override
    public boolean allowCache() {
        return false;
    }
    
    @Override
    public boolean canDownloadUrl(String url) {
        return url.startsWith(ContentResolver.SCHEME_CONTENT);
    }
}
