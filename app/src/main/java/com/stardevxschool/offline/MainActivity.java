package com.stardevxschool.offline;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * WebView wrapper untuk StarDevXSchool Offline.
 * Sengaja TIDAK pakai androidx.appcompat (biar tidak menyeret dependency
 * androidx.fragment yang bikin gagal build di AIDE) — Activity biasa sudah
 * lebih dari cukup untuk sekadar menampilkan WebView.
 */
public class MainActivity extends Activity {

    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;
    private static final int FILE_CHOOSER_REQUEST = 51;
    private static final int STORAGE_PERM_REQUEST = 52;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        webView.setBackgroundColor(0xFF06162F);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.addJavascriptInterface(new JsBridge(), "Android");

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback,
                                              FileChooserParams params) {
                filePathCallback = callback;
                Intent intent = params.createIntent();
                try {
                    startActivityForResult(intent, FILE_CHOOSER_REQUEST);
                } catch (Exception e) {
                    filePathCallback = null;
                    return false;
                }
                return true;
            }
        });

        // index.dat = isi index.html. Ekstensinya diganti supaya parser JS AIDE
        // tidak menandainya error; WebView tetap menerimanya sebagai text/html.
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return serveApp(request.getUrl().toString());
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                return serveApp(url);
            }
        });

        webView.loadUrl("file:///android_asset/www/index.dat");
    }

    private WebResourceResponse serveApp(String url) {
        if (url == null || !url.startsWith("file:///android_asset/www/")) return null;
        String path = url.substring("file:///android_asset/www/".length());
        // The primary document keeps its legacy .dat suffix for AIDE compatibility;
        // locally bundled editor modules are served from the same offline asset folder.
        if (path.contains("..") || path.contains("/")) return null;
        String mime = "text/javascript";
        if ("index.dat".equals(path)) mime = "text/html";
        else if (path.endsWith(".css")) mime = "text/css";
        else if (path.endsWith(".svg")) mime = "image/svg+xml";
        try {
            return new WebResourceResponse(mime, "UTF-8", getAssets().open("www/" + path));
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CHOOSER_REQUEST) {
            if (filePathCallback == null) return;
            Uri[] results = null;
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                results = new Uri[]{data.getData()};
            }
            filePathCallback.onReceiveValue(results);
            filePathCallback = null;
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    /** Dipanggil dari JS lewat window.Android.saveFile(filename, content) */
    private class JsBridge {
        @JavascriptInterface
        public void saveFile(final String filename, final String content) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    boolean ok = writeToDownloads(filename, content);
                    Toast.makeText(MainActivity.this,
                            ok ? "Tersimpan di Download/StarDevXSchool/" + filename : "Gagal menyimpan file",
                            Toast.LENGTH_LONG).show();
                }
            });
        }

        @JavascriptInterface
        public void saveFileBase64(final String filename, final String dataUrl) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int comma = dataUrl.indexOf(',');
                        String encoded = comma >= 0 ? dataUrl.substring(comma + 1) : dataUrl;
                        boolean ok = writeToDownloads(filename, Base64.decode(encoded, Base64.DEFAULT));
                        Toast.makeText(MainActivity.this,
                                ok ? "Tersimpan di Download/StarDevXSchool/" + filename : "Gagal menyimpan file",
                                Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Gagal menyimpan file", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private boolean writeToDownloads(String filename, String content) {
        return writeToDownloads(filename, content.getBytes(StandardCharsets.UTF_8));
    }

    private boolean writeToDownloads(String filename, byte[] bytes) {
        try {

            if (Build.VERSION.SDK_INT >= 29) {
                // Android 10+: MediaStore, tidak perlu izin runtime.
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream");
                values.put("relative_path",
                        Environment.DIRECTORY_DOWNLOADS + "/StarDevXSchool");

                Uri uri = getContentResolver().insert(
                        Uri.parse("content://media/external/downloads"), values);
                if (uri == null) return false;
                OutputStream out = getContentResolver().openOutputStream(uri);
                if (out == null) return false;
                try {
                    out.write(bytes);
                } finally {
                    out.close();
                }
                return true;
            }

            // Android 6–9: cek izin pakai API bawaan Activity (tanpa androidx).
            if (Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            STORAGE_PERM_REQUEST);
                    return false; // user perlu coba export lagi setelah izin diberikan
                }
            }
            // Android 5: izin sudah otomatis didapat lewat manifest saat install.

            File dir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "StarDevXSchool");
            if (!dir.exists()) dir.mkdirs();
            File outFile = new File(dir, filename);
            FileOutputStream fos = new FileOutputStream(outFile);
            try {
                fos.write(bytes);
            } finally {
                fos.close();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
