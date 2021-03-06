package com.example.tekbae;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

//필수 변경사항(앱 가동시)
// date_info 를 Connection 의 key 값으로 입력, 문자전송시 01032516989 -> phoneList[i] 수정

public class Send_URL extends AppCompatActivity {

    private LinearLayout listlayout;
    private Button btnSubmmit;
    private TextView todayText;
    private String date_text, date_info;

    private int dbSize = 12; // 데이터베이스 열 갯수
    private String[][] dbList, simpleList;
    private String[] phoneList;
    private String output;

    final int SMS_SEND_PERMISSION = 1; //SMS 송신 퍼미션


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_url);

        btnSubmmit = (Button) findViewById(R.id.btnSub);
        listlayout = (LinearLayout) findViewById(R.id.listLayout);
        todayText = (TextView) findViewById(R.id.todayDate);

        //Url 송신 권한 얻기 & 전화번호 목록 >>  배포 버튼 클릭 시 Url 전송

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "SMS 송신권한 있음", Toast.LENGTH_SHORT).show();

        }else {
            Toast.makeText(getApplicationContext(), "SMS 송신권한 없음", Toast.LENGTH_SHORT).show();
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
                Toast.makeText(getApplicationContext(), "SMS 권한이 필요합니다", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_SEND_PERMISSION);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_SEND_PERMISSION);
            }
        }

        //오늘 날짜 출력
        Date currentTime = Calendar.getInstance().getTime();
        date_text = new SimpleDateFormat("yyyy년 MM월 dd일 EE요일", Locale.getDefault()).format(currentTime);
        date_info = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(currentTime);
        todayText.setText(date_text);

        //고객 리스트 DB전체 받아오기
        try {
            output = new Connection("Uber", "select", date_info, "Date", null, null)
                    .execute(BuildConfig.SERVER_HOST+"/regosterUser.php?").get();

            if(!output.equals("null")) {
                String arr[] = output.split("/");
                dbList = new String[arr.length][dbSize];
                for (int i = 0; i < arr.length; i++) {
                    String arr2[] = arr[i].split(",");
                    for (int j = 0; j < arr2.length; j++) {
                        dbList[i][j] = arr2[j];
                    }
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 심플 고객 리스트 만들기 & 고객 리스트 받아오면서 전화번호만 로 파싱
        if(!output.equals("null")) {
            simpleList = new String[dbList.length][7];
            phoneList = new String[dbList.length];

            for (int i = 0; i < simpleList.length; i++) {
                simpleList[i][0] = dbList[i][0];
                simpleList[i][1] = "0" + dbList[i][1];
                phoneList[i] = simpleList[i][1]; // 전화번호 목록만 따로 파싱
                simpleList[i][2] = dbList[i][2];
                simpleList[i][3] = dbList[i][3];
                simpleList[i][4] = dbList[i][4];
                simpleList[i][5] = "0" + dbList[i][5];
                simpleList[i][6] = dbList[i][10];
            }

            // listLayout 에 버튼 동적생성 만들기
            Button btn[] = new Button[simpleList.length];
            for (int i = 0; i < btn.length; i++) {
                btn[i] = new Button(this);
                btn[i].setText("받는분 : " + simpleList[i][0] + "\r\n받는분 번호 : " + simpleList[i][1] + "\r\n받는분 주소 : " + simpleList[i][2] + "\r\n제품명 : " + simpleList[i][3] + "\r\n보내는곳 : " + simpleList[i][4]
                        + "\r\n보내는분 번호 : " + simpleList[i][5] + "\r\n배송 날짜 : " + simpleList[i][6]);
                btn[i].setTextSize(11);
                btn[i].setGravity(Gravity.LEFT);
                btn[i].setId(View.generateViewId());
                listlayout.addView(btn[i]);
            }
        }else {
            TextView noListText = new TextView(this);
            noListText.setText(date_info+" 일에 배송할 고객이 존재하지 않습니다");
            noListText.setTextSize(25);
            noListText.setGravity(Gravity.CENTER);
            listlayout.addView(noListText);
        }

        btnSubmmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(output.equals("null")){
                    Toast.makeText(getApplicationContext(), "전송할 고객이 존재하지 않습니다",Toast.LENGTH_SHORT).show();
                } else {
                    String Url = BuildConfig.SERVER_HOST+"/survey.html";
                    SmsManager sms = SmsManager.getDefault();
                    for(int i=0; i<phoneList.length; i++){
                        sms.sendTextMessage(phoneList[i], null, Url, null, null);
                        sms.sendTextMessage(phoneList[i], null, "안녕하세요 TAKBAE 고객님, 오늘부로 00시~00시 사이에 주문하신 물품이 배송될 예정입니다.", null, null);
                    }
                    Toast.makeText(getApplicationContext(), "Url을 전송하였습니다", Toast.LENGTH_SHORT).show();
                    finish();

                }
            }
        });

    }
}
