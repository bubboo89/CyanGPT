
package it.cyangpt.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private TextView status;
    private Uri latestImage;
    private static final int REQUEST_PERMISSION = 10;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(32, 40, 32, 40);

        TextView title = new TextView(this);
        title.setText("CyanGPT");
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Colmo V03 • CyanBridge");
        subtitle.setTextSize(16);
        subtitle.setGravity(Gravity.CENTER);
        layout.addView(subtitle);

        Button search = new Button(this);
        search.setText("Trova ultima foto CyanBridge");
        layout.addView(search);

        Button share = new Button(this);
        share.setText("Invia foto a ChatGPT");
        layout.addView(share);

        Button open = new Button(this);
        open.setText("Apri ChatGPT");
        layout.addView(open);

        status = new TextView(this);
        status.setText("Pronto");
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, 30, 0, 0);
        layout.addView(status);

        setContentView(layout);

        search.setOnClickListener(v -> findPhoto());
        share.setOnClickListener(v -> {
            if (latestImage == null) {
                Toast.makeText(this, "Prima trova una foto", Toast.LENGTH_SHORT).show();
            } else {
                sharePhoto();
            }
        });
        open.setOnClickListener(v -> openChatGPT());
    }

    private void findPhoto() {
        String permission = Build.VERSION.SDK_INT >= 33
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{permission}, REQUEST_PERMISSION);
            return;
        }

        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.RELATIVE_PATH
        };

        try (Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                Build.VERSION.SDK_INT >= 29
                        ? "relative_path LIKE ? OR relative_path LIKE ?"
                        : null,
                Build.VERSION.SDK_INT >= 29
                        ? new String[]{"%CyanBridge%", "%HeyCyan%"}
                        : null,
                MediaStore.Images.Media.DATE_ADDED + " DESC")) {

            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));

                latestImage = Uri.withAppendedPath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        String.valueOf(id));

                status.setText("Foto trovata!");
            } else {
                status.setText("Nessuna foto CyanBridge trovata.");
            }

        } catch (Exception e) {
            status.setText("Errore: " + e.getMessage());
        }
    }

    private void sharePhoto() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, latestImage);
        intent.putExtra(Intent.EXTRA_TEXT,
                "Analizza questa foto e rispondi in italiano.");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(intent, "Invia a ChatGPT"));
    }

    private void openChatGPT() {
        Intent intent = getPackageManager()
                .getLaunchIntentForPackage("com.openai.chatgpt");

        if (intent != null) {
            startActivity(intent);
        } else {
            startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://chatgpt.com")));
        }
    }
}
