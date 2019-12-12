package com.showcase.btecshowcase;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;

import net.dongliu.requests.Requests;

import org.apache.commons.codec.binary.Base64;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

        jsonresult();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
         switch(requestCode){

            case 7:

                if(resultCode==RESULT_OK){

                    String ImagePath = data.getData().getPath();
                    jsonresult(ImagePath);

                }
                break;

        }
    }


    public  void jsonresult(String ImagePath)  {
        Bitmap bitmap= BitmapFactory.decodeFile(ImagePath);
        Frame tempframe=convert1.convert(bitmap);
        Mat matrix=converterToMat.convert(tempframe);
        System.out.print(matrix.depth());

//        Gson gson = new Gson();
//        org.bytedeco.opencv.opencv_core.Mat img = org.bytedeco.opencv.global.opencv_imgcodecs.imread("/home/danhyal/download.jpeg");
////        org.bytedeco.opencv.global.opencv_imgproc.resize(img,img,new Size(1200,1200));
//
//        ByteBuffer temp=img.getByteBuffer();
//        byte[] arr = new byte[temp.remaining()];
//        temp.get(arr);
//        opencv_imgcodecs.imencode(".jpg", img, arr);
//        String encoded= Base64.encodeBase64String(arr);
//
//        org.json.simple.JSONObject json=new org.json.simple.JSONObject();
//        org.json.simple.JSONArray oof=new org.json.simple.JSONArray();
//        JSONObject b64=new JSONObject();
//        b64.put("b64",encoded);
//        oof.add(b64);
//        json.put("instances",oof);
//        String server_url = "http://92.233.63.88:8501/v1/models/resnet_openimages:predict";
//        JSONParser jsonParser=new JSONParser();
//
//        final long startTime = System.currentTimeMillis();
//        String response = Requests.post(server_url).acceptCompress(true)
//                .jsonBody(json).socksTimeout(10000)
//                .send().readToText();
//        Object obj=jsonParser.parse(response);
//        JSONObject jobj=(JSONObject) obj;
//        JSONArray content=(JSONArray) jobj.get("predictions");
//        Iterator i = content.iterator();
//
//        JSONObject predictions = (JSONObject) i.next();
//        int num_detections = ((Double) predictions.get("num_detections")).intValue();
//        double[] detection_classes = gson.fromJson(predictions.get("detection_classes").toString(),(Type)double[].class);
//        List<Double> detection_scores=new ArrayList<>();
//        double[] detection_scoress =gson.fromJson(predictions.get("detection_scores").toString(),(Type)double[].class);
//        for (double x:detection_scoress){
//            if (x!=0.0f){
//                detection_scores.add(x);
//            }
//        }
//        Double[][] detection_boxess=gson.fromJson(predictions.get("detection_boxes").toString(), (Type) Double[][].class);
//        List<List<Double>> detection_boxes= StreamEx.of(detection_boxess).map(a -> DoubleStreamEx.of(a).boxed().toList()).toList();
//        for (int j=0;j<num_detections;j+=1){
//            double confidance=detection_scores.get(j);
//            if (confidance>0.7){
//                int top= (int) (detection_boxes.get(j).get(0)*img.rows());
//                int left=(int)(detection_boxes.get(j).get(1)*img.cols());
//                int bottom=(int)(detection_boxes.get(j).get(2)*img.rows());
//                int right=(int)(detection_boxes.get(j).get(3)*img.cols());
//                org.bytedeco.opencv.global.opencv_imgproc.rectangle(img,new Point(left,top),new Point(right,bottom), Scalar.GREEN);
//                //org.bytedeco.opencv.global.opencv_imgproc.putText(img, TensorCocoClasses[(int) detection_classes[j]],new Point(left,top),1,1,Scalar.RED);
//
//
//            }
//        }
//        final long endTime = System.currentTimeMillis();
//        return img;
    }
}
