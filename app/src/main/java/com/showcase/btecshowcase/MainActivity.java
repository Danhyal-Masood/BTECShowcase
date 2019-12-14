package com.showcase.btecshowcase;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;


import org.apache.commons.codec.binary.Base64;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameFilter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import one.util.streamex.DoubleStreamEx;
import one.util.streamex.StreamEx;

public class MainActivity extends AppCompatActivity {
    Intent intent;
    String TAG="::DEBUG::";
    OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();

    AndroidFrameConverter convert1=new AndroidFrameConverter();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button=findViewById(R.id.load_button);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, 7);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
         switch(requestCode){

            case 7:

                if(resultCode==RESULT_OK){
                    try {
                        ImageView v1=findViewById(R.id.image_view);
                        Uri ImageUri = data.getData();
                        Bitmap bitmap= MediaStore.Images.Media.getBitmap(this.getContentResolver(), ImageUri);
                        NetworkTask nt=new NetworkTask();

                        Bitmap bmp=nt.execute(bitmap).get();
                        v1.setImageBitmap(bmp);
                        Log.d(TAG,String.valueOf(bmp.getHeight()));

                    }catch (NullPointerException e){e.printStackTrace();}
                    catch (Exception e){e.printStackTrace();}


                }
                break;

        }
    }



    public class NetworkTask extends AsyncTask<Bitmap, Void, Bitmap> {

    @Override
    public Bitmap doInBackground(Bitmap... bitmaps) {
        Bitmap bitmap=bitmaps[0];

        Frame tempframe=convert1.convert(bitmap);
        Mat img=converterToMat.convert(tempframe);
        Log.d(TAG,String.valueOf(img.arrayHeight()));
        // init json objects
        Gson gson = new Gson();
        org.json.simple.JSONObject json=new org.json.simple.JSONObject();
        org.json.simple.JSONArray throwawayarray=new org.json.simple.JSONArray();
        JSONObject b64=new JSONObject();
        JSONParser jsonParser=new JSONParser();

        // resize matrix to match warmup data
        org.bytedeco.opencv.global.opencv_imgproc.resize(img,img,new Size(1920,1080));
        // convert matrix into b64 encoded jpeg
        ByteBuffer temp=img.getByteBuffer();
        byte[] arr = new byte[temp.remaining()];
        temp.get(arr);
        opencv_imgcodecs.imencode(".jpg", img, arr);
        String encoded= Base64.encodeBase64String(arr);


        // add b64 string to json object and prep for transmission
        b64.put("b64",encoded);
        throwawayarray.add(b64);
        json.put("instances",throwawayarray);
        String server_url = "http://92.233.63.88:8501/v1/models/coconet:predict";

        final long startTime = System.currentTimeMillis();
        OkHttpClient client = new OkHttpClient();
        final MediaType JSON= MediaType.get("application/json; charset=utf-8");
        RequestBody requestBody=RequestBody.create(json.toJSONString(),JSON);
        Request request=new Request.Builder().url(server_url).post(requestBody).build();
        try {
             String response=client.newCall(request).execute().body().string();

        // parse response into json
        Object obj=jsonParser.parse(response);
        JSONObject jobj=(JSONObject) obj;
        JSONArray content=(JSONArray) jobj.get("predictions");
        Iterator i = content.iterator();


        // filter json and convert into correct datatypes.
        JSONObject predictions = (JSONObject) i.next();

        int num_detections = ((Double) predictions.get("num_detections")).intValue();
        double[] detection_classes = gson.fromJson(predictions.get("detection_classes").toString(),(Type)double[].class);
        List<Double> detection_scores=new ArrayList<>();
        double[] detection_scoress =gson.fromJson(predictions.get("detection_scores").toString(),(Type)double[].class);
        for (double x:detection_scoress){
            if (x!=0.0f){
                detection_scores.add(x);
            }
        }
        Double[][] detection_boxess=gson.fromJson(predictions.get("detection_boxes").toString(), (Type) Double[][].class);
        List<List<Double>> detection_boxes= StreamEx.of(detection_boxess).map(a -> DoubleStreamEx.of(a).boxed().toList()).toList();
        // iterate over the number of detections to draw rectangles and text for each detection on matrix.
        for (int j=0;j<num_detections;j+=1){
            double confidance=detection_scores.get(j);
            if (confidance>0.7){
                int top= (int) (detection_boxes.get(j).get(0)*img.rows());
                int left=(int)(detection_boxes.get(j).get(1)*img.cols());
                int bottom=(int)(detection_boxes.get(j).get(2)*img.rows());
                int right=(int)(detection_boxes.get(j).get(3)*img.cols());
                org.bytedeco.opencv.global.opencv_imgproc.rectangle(img,new Point(left,top),new Point(right,bottom), Scalar.GREEN);
                //org.bytedeco.opencv.global.opencv_imgproc.putText(img, TensorCocoClasses[(int) detection_classes[j]],new Point(left,top),1,1,Scalar.RED);


            }
        }
        final long endTime = System.currentTimeMillis();
        Bitmap bmp2=convert1.convert(converterToMat.convert(img));
        System.out.print((endTime-startTime)/1000);
        // temp
        return bmp2;
        }catch (ParseException e){}
       catch (IOException e){}
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {

    }
}
}
