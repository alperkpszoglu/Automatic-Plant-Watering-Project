package com.example.projeiot;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {
    TextView SicaklikVeri, NemVeri, IsikVeri, ToprakVeri, EsikDegertxtw,ManuelBilgi;
    String sicaklikDeger, nemDeger, isikDeger, toprakDeger;
    SeekBar EsikBar;
    Button veriGetir;
    public int esikDeger;
    public int barSonKonum;
    public int baslangicKontrol=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        SicaklikVeri = findViewById(R.id.sicaklikVeri);
        NemVeri = findViewById(R.id.nemVeri);
        IsikVeri = findViewById(R.id.isikVeri);
        ToprakVeri = findViewById(R.id.toprakVeri);
        EsikDegertxtw = findViewById(R.id.esikDeger);
        EsikBar = findViewById(R.id.esikBar);
        veriGetir = findViewById(R.id.verigetir);
        ManuelBilgi=findViewById(R.id.manuelBilgi);



        ApiGetBarSonDurum();//E??ik De??erini ald??????m??z bar??n en son nas??l b??rak??ld??ysa ??yle ba??lamas??n?? istedi??imiz i??in get ile son bar konumunu ??ektik ver bar?? o konumdan ba??lat??yoruz
        //bar??n son konumunu thingSpeakden al??p ba??lang???? konumunu o konum olarak ayarlar
        //
        ApiGetSensorler(); //program a????ld??????nda ekran??n bo?? gelmemesi i??in verilerin apiden gelmesini sa??l??yorum
        final Handler handler = new Handler(); //saniye cinsinden veri g??ncelleme k??sm??
        Runnable refresh = new Runnable() {
            @Override
            public void run() {
                ApiGetSensorler();
                handler.postDelayed(this, 4000); //4 saniyede bir verileri g??ncelle
            }
        };
        handler.postDelayed(refresh, 4000);




        if (barSonKonum == 0) {
            EsikDegertxtw.setText("Topra????n Nem Oran?? %20'nin Alt??na D????t??????nde Sulama yap");
        }
        if (barSonKonum == 1) {
            EsikDegertxtw.setText("Topra????n Nem Oran?? %40'??n Alt??na D????t??????nde Sulama yap");
        }
        if (barSonKonum == 2) {
            EsikDegertxtw.setText("Topra????n Nem Oran?? %60'??n Alt??na D????t??????nde Sulama yap");
        }
        veriGetir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                veriGetir.setEnabled(false);
                ApiPostManuelSulama(1);//butona bast??????m??zda manuel sulaman??n istendi??i bilgisi 1 olarak thingspeak'e gider
                ButonGeriSayim();       //ilk geri say??m 32 saniyede bir thingspeake veri g??nderebildi??imiz i??in butona bas??ld??????nda de??eri 1 olarak kaydeder ve 32 saniye bekleyip de??eri 0 a ??eker bunu yapmam??z??n amac?? arduino ??zerinden 1 de??erini okuyup sulamay?? ba??latmam??z i??in
                ButonGeriSayim2();      //ikinci say??m 60 saniye ????nk?? ilk ??nce de??eri 1 yap??p 32 saniye bekleyip 0 yapt??????m??z i??in bir 15 saniye daha thingspeake eklemek i??in beklemeye ihtiyac??m??z var
                Toast.makeText(MainActivity.this, "Manuel Sulama Ba??lat??l??yor", Toast.LENGTH_SHORT).show();
            }
        });

            EsikBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int esikKonum, boolean b) {
                    if(baslangicKontrol==1) { //program ilk a????ld??????nda veritaban??ndan esik de??eri hangi konumda b??rak??ld??ysa onu ??ekiyoruz fakat
                                             //apiden son konum gelip bar konumu de??i??tirilmek istendi??inde onchange fonksiyonuna girip i??ierisinde yazd??????m
                                             //15 saniyelik bekleme s??resine giriyor bu y??zden ba??lang????ta apiden gelen response okey oldu??unda i??erisindeki kodlar?? yap dedik

                        if (esikKonum == 0) {
                            EsikDegertxtw.setText("Topra????n Nem Oran?? %20'nin Alt??na D????t??????nde Sulama yap");
                            esikDeger = 0;
                            ApiPostEsik();
                            EsikBar.setEnabled(false); //serverda 15 saniye s??n??r?? oldu??u i??in bar'?? kullan??lamaz yap??yorum
                            BarGeriSayim(); //15 saniyeden geriye sayd??rarak ekrana yazd??r??yoruz
                            PostDelay(); // bar'?? 15 saniye sonra aktif etmek i??in 15 saniye saya?? koyup aktif ediyorum.
                        }
                        if (esikKonum == 1) {
                            EsikDegertxtw.setText("Topra????n Nem Oran?? %40'??n Alt??na D????t??????nde Sulama yap");
                            esikDeger = 1;
                            ApiPostEsik();
                            EsikBar.setEnabled(false);
                            BarGeriSayim(); //15 saniyeden geriye sayd??rarak ekrana yazd??r??yoruz
                            PostDelay();
                        }
                        if (esikKonum == 2) {
                            EsikDegertxtw.setText("Topra????n Nem Oran?? %60'??n Alt??na D????t??????nde Sulama yap");
                            esikDeger = 2;
                            ApiPostEsik();
                            EsikBar.setEnabled(false);
                            BarGeriSayim(); //15 saniyeden geriye sayd??rarak ekrana yazd??r??yoruz
                            PostDelay();
                        }
                    }

                }


                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });


    }
    public void ButonGeriSayim(){
        new CountDownTimer(32000, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                ApiPostManuelSulama(0);   //geri say??m bitti??inde durumu 0 a ??ek

            }

        }.start();
    }
    public void ButonGeriSayim2(){
        new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                ManuelBilgi.setText("Yeniden Sulayabilmek ????in Kalan S??re: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                veriGetir.setEnabled(true);
                ManuelBilgi.setText("Bitki Topra????n Nem Oran??na G??re Zaten Sulan??yor Fakat Kendin Manuel Sulamak ??stersen A??a????daki Butona Basarak Sulayabilirsin.");

            }

        }.start();
    }
    public void BarGeriSayim(){  //Bar disable olduktan sonra enable olmas??na ka?? saniye kald??????n?? kullan??c??ya g??stermek i??in 15 den 0 a sayan bir saya?? olu??turuyorum
        new CountDownTimer(15000, 1000) {

            public void onTick(long millisUntilFinished) {
                EsikDegertxtw.setText("De??eri Yeniden De??i??tirebilmek ????in Kalan S??re: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                if(esikDeger==0){EsikDegertxtw.setText("Topra????n Nem Oran?? %20'??n Alt??na D????t??????nde Sulama yap");}
                if(esikDeger==1){EsikDegertxtw.setText("Topra????n Nem Oran?? %40'??n Alt??na D????t??????nde Sulama yap");}
                if(esikDeger==2){EsikDegertxtw.setText("Topra????n Nem Oran?? %60'??n Alt??na D????t??????nde Sulama yap");}
            }

        }.start();
    }
    public void PostDelay(){
    Handler handlerPostDelay = new Handler();
            handlerPostDelay.postDelayed(new Runnable() {
        public void run () {
            EsikBar.setEnabled(true);
        }
    },15000);   //15 saniye sonra bar?? kullan??labilir yap
    }

    public void ApiPostEsik(){ //Kullan??c?? e??ik de??erini de??i??tirdi??inde thingspeak apisine kaydedilir ve  arduino kodu ??zerinden de??er de??i??tirilir
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            String URL = "https://api.thingspeak.com/update?api_key=00EY7F73TLOSQUEZ&"; //ThingSpeak POST Api URL
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("field1", esikDeger);
            final String requestBody = jsonBody.toString();

            StringRequest stringRequestPost = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Desteklenmeyen kodlama %s kullan??m?? %s", requestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            requestQueue.add(stringRequestPost);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void ApiPostManuelSulama(int durumManuel){ //Kullan??c?? e??ik de??erini de??i??tirdi??inde thingspeak apisine kaydedilir ve  arduino kodu ??zerinden de??er de??i??tirilir
                try {
                    RequestQueue requestQueue = Volley.newRequestQueue(this);
                    String URL = "https://api.thingspeak.com/update?api_key=768HM3F6KLGNI3VT&"; //ThingSpeak POST Api URL
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("field1", durumManuel);//manuel sulama tu??una bas??ld??????nda ThingSpeak'e durum de??i??keninin de??eri olarak kaydet


                    final String requestBody = jsonBody.toString();

                    StringRequest stringRequestPost = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) { //i??lem ger??ekle??irse i??erisini yapar

                        }
                    }, new Response.ErrorListener() { //i??lem hata verirse i??erisini yapar
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(MainActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }) {
                        @Override
                        public String getBodyContentType() {
                            return "application/json; charset=utf-8";
                        }

                        @Override
                        public byte[] getBody() throws AuthFailureError {
                            try {
                                return requestBody == null ? null : requestBody.getBytes("utf-8");
                            } catch (UnsupportedEncodingException uee) {
                                VolleyLog.wtf("Desteklenmeyen kodlama %s kullan??m?? %s", requestBody, "utf-8");
                                return null;
                            }
                        }

                        @Override
                        protected Response<String> parseNetworkResponse(NetworkResponse response) {
                            String responseString = "";
                            if (response != null) {
                                responseString = String.valueOf(response.statusCode);
                            }
                            return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                        }
                    };

                    requestQueue.add(stringRequestPost);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "hataya d????t??m", Toast.LENGTH_SHORT).show();
                }
    }


    public void ApiGetSensorler(){
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        String urlget = "https://api.thingspeak.com/channels/1686086/feeds/last.json?api_key=HL13V1HW21GLPJZO&field=0";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlget,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsnOBJ = new JSONObject(response); //String olarak gelen json verileri obje t??r??ne ??eviriyoruz
                            sicaklikDeger=jsnOBJ.getString("field1");//obje i??erisinden field1' de bulunan sicaklik de??erini al??yorum
                            SicaklikVeri.setText(sicaklikDeger.substring(0,4)+"??C");

                            nemDeger=jsnOBJ.getString("field2");//obje i??erisinden field2' de bulunan nem de??erini al??yorum
                            NemVeri.setText("%"+nemDeger.substring(0,4));

                            toprakDeger=jsnOBJ.getString("field3");//obje i??erisinden field3' de bulunan toprak nem de??erini al??yorum
                            ToprakVeri.setText("%"+toprakDeger.substring(0,4));

                            isikDeger=jsnOBJ.getString("field4");//obje i??erisinden field4' de bulunan ??????k de??erini al??yorum
                            IsikVeri.setText("%"+isikDeger.substring(0,4));

                        } catch (Throwable t) {
                            Toast.makeText(MainActivity.this, "Json string json objeye d??nusturulemedi", Toast.LENGTH_SHORT).show();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Hata", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(stringRequest);
    }
    public void ApiGetBarSonDurum(){
        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        String urlget = "https://api.thingspeak.com/channels/1688783/feeds/last.json?api_key=FSYK2KYPP0W054QO&field=0";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlget,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsnOBJ = new JSONObject(response);
                            barSonKonum=Integer.parseInt(jsnOBJ.getString("field1")); //bar??n son konumunu ??ektik
                            EsikBar.setProgress(barSonKonum); //ba??lad??????nda hangi konumda oldu??u
                            baslangicKontrol=1;


                        } catch (Throwable t) {
                            Toast.makeText(MainActivity.this, "Json string json objeye d??nusturulemedi", Toast.LENGTH_SHORT).show();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Hata", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(stringRequest);
    }
}





