package io.madcamp.yh.mc_assignment1;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import io.madcamp.yh.mc_assignment1.R;
import io.madcamp.yh.mc_assignment1.Tab2Fragment;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import io.madcamp.yh.mc_assignment1.Retrofit.IMyService;
import io.madcamp.yh.mc_assignment1.Retrofit.RetrofitClient;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class UploadImageActivity extends AppCompatActivity {
    ImageView image;
    Button choose, upload;
    int PICK_IMAGE_REQUEST = 111;
    String URL ="http://socrip4.kaist.ac.kr:4080/upload";
    Bitmap bitmap, mybitmap;
    ProgressDialog progressDialog;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    IMyService iMyService;
    //private static Context mmContext;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        Intent intent = getIntent();
        final String imageUri = intent.getStringExtra("imageUri");
        String imageTag = intent.getStringExtra("imageTag");

        image = (ImageView)findViewById(R.id.image);
        upload = (Button)findViewById(R.id.upload);

        System.out.println("imageUri : " + imageUri);
        System.out.println("imageTag : " + imageTag);


        File imgFile = new File(imageUri);
        mybitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        image.setImageBitmap(mybitmap);
        //image.setImageURI(imageUri);

        Retrofit retrofitClient = RetrofitClient.getInstance();
        iMyService = retrofitClient.create(IMyService.class);



        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //uploadImage(imageUri);

//                progressDialog = new ProgressDialog(Tab2Fragment.getActivity());
//                progressDialog.setMessage("Uploading, please wait...");
//                progressDialog.show();

                //converting image to base64 string
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                mybitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                final String imageString = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
                System.out.println("222222 : " + imageString);
                //sending image to server
                StringRequest request = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>(){
                    @Override
                    public void onResponse(String s) {
                        //progressDialog.dismiss();
                        if(s.indexOf("Success")>-1){
                            Toast.makeText(UploadImageActivity.this, "Uploaded Successful", Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(UploadImageActivity.this, "Some error occurred!", Toast.LENGTH_LONG).show();
                        }
                    }
                },new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(UploadImageActivity.this, "Some error occurred -> "+volleyError, Toast.LENGTH_LONG).show();;
                    }
                }) {
                    //adding parameters to send
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> parameters = new HashMap<String, String>();
                        parameters.put("image", imageString);
                        return parameters;
                    }
                };

                RequestQueue rQueue = Volley.newRequestQueue(UploadImageActivity.this);
                rQueue.add(request);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();

            try {
                //getting image from gallery
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);

                //Setting image to ImageView
                image.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

//    private void uploadImage(String imageurl) {
//        if (TextUtils.isEmpty(imageurl)) {
//            Toast.makeText(this, "Null Image", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        compositeDisposable.add(iMyService.uploadImage(imageurl)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Consumer<String>() {
//                    @Override
//                    public void accept(String response) throws Exception {
//                        Toast.makeText(Tab2Fragment.mContext, "" + response, Toast.LENGTH_SHORT).show();
//
//                    }
//                }));
//    }
}