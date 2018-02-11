package groupproject.hse.pokolenie;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    private static final int SELECT_IMAGE = 128;
    private String ASSET_PATH = "file:///android_asset/";

    private ImageView mMainImageView;
    private ImageView mWordImage;

    private MenuItem mSaveItem;
    private MenuItem mOpenItem;

    private ViewGroup mRootLayout;
    private int xDelta;
    private int yDelta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainImageView = findViewById(R.id.mainImage);
        mWordImage = findViewById(R.id.wordImage);
        mRootLayout = findViewById(R.id.root);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(150, 150);
        mWordImage.setLayoutParams(layoutParams);
        mWordImage.setOnTouchListener(this);

        Picasso.with(getBaseContext()).load(ASSET_PATH + "source.png").into(mMainImageView);
        Picasso.with(getBaseContext())
                .load(ASSET_PATH + "template.png")
                .into(mWordImage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_open){
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                    SELECT_IMAGE);

            return true;
        }

        if (item.getItemId() == R.id.action_save){
            String filename;
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            filename =  sdf.format(date);

            try{

                String path = Environment.getExternalStorageDirectory().toString();

                File wallpaperDirectory = new File(path + "/DCIM/Pokolenie");
                wallpaperDirectory.mkdirs();

                OutputStream fOut = null;
                File file = new File(wallpaperDirectory, filename+".jpg");
                fOut = new FileOutputStream(file);

                createSingleImage().compress(Bitmap.CompressFormat.JPEG, 100, fOut);

                fOut.flush();
                fOut.close();

                MediaStore.Images.Media.insertImage(getContentResolver()
                        ,file.getAbsolutePath(),file.getName(),file.getName());

                Toast toast = Toast.makeText(getApplicationContext(), "Saved!", Toast.LENGTH_SHORT);
                toast.show();
            }catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

        return true;
    }

    private Bitmap createSingleImage(){

        Bitmap mainBitmap = ((BitmapDrawable)mMainImageView.getDrawable()).getBitmap();
        Bitmap wordBitmap = ((BitmapDrawable)mWordImage.getDrawable()).getBitmap();

        Bitmap resultBitmap = Bitmap.createBitmap(mainBitmap.getWidth(), mainBitmap.getHeight(), mainBitmap.getConfig());
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(mainBitmap, 0f, 0f, null);


        canvas.drawBitmap(wordBitmap, mWordImage.getLeft(), mWordImage.getTop(), null);

        return resultBitmap;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            if (requestCode == SELECT_IMAGE){
                if (data != null) {
                    Uri selectedImageUri = data.getData();

                    Picasso.with(getBaseContext())
                            .load(selectedImageUri)
                            .into(mMainImageView);
                }
            }
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        final int X = (int) event.getRawX();
        final int Y = (int) event.getRawY();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                xDelta = X - lParams.leftMargin;
                yDelta = Y - lParams.topMargin;
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view
                        .getLayoutParams();
                layoutParams.leftMargin = X - xDelta;
                layoutParams.topMargin = Y - yDelta;
                layoutParams.rightMargin = -250;
                layoutParams.bottomMargin = -250;
                view.setLayoutParams(layoutParams);
                break;
        }
        mRootLayout.invalidate();
        return true;
    }
}
