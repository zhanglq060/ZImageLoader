
package com.zlq.imageloader.library;

import android.content.Context;
import android.os.Process;

import java.io.InputStream;

public class AssetUrlDownloader implements UrlDownloader {
    @Override
    public void download(final Context context, final String url, final String filename,
            final UrlDownloaderCallback callback, final ZImageLoader.CompleteListener completion) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                try {
                    String relativePath = url.replaceFirst("file:///android_asset/", "");
                    InputStream is = context.getAssets().open(relativePath);
                    callback.onDownloadComplete(AssetUrlDownloader.this, is, null, 0);
                }catch (Exception e){
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
        return url.startsWith("file:///android_asset/");
    }
}
