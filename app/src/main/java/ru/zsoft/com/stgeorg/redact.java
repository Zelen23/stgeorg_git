package ru.zsoft.com.stgeorg;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Home on 09.01.2017.
 */

        /*получаю номер
        * делаю запрос к прльзователям
        * получат массив
        * парсиег имени
        * вывожу в едиты
        * слушаю едиты
        * если изменено то записываю*/

    /* Если меняем номер то перед записью в базу проверяю
     * есть ли он есть в базе то
     * то к id того что есть прибавляем коунт
     * а строку того ид того что есть удаляем */



public class redact extends MainActivity {

    EditText ename, efamily, enumber, eulr;
    Button bt;
    boolean flag = false;
    ArrayList<String> liner;
    ArrayList<String> redline;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.redact);

        ename = (EditText) findViewById(R.id.editName);
        efamily = (EditText) findViewById(R.id.editFamily);
        enumber = (EditText) findViewById(R.id.editNumber);
        eulr = (EditText) findViewById(R.id.editUlr);
        bt = (Button) findViewById(R.id.bRed);

        Intent intent = getIntent();
        String pk_num = intent.getStringExtra("pk_num");


        Log.i("redact_1", pk_num);
        parseName(pk_num);


        View.OnClickListener clk = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toBe();
                if (flag == false) {
                    Log.i("redact_flag", String.valueOf(flag) + "--!нет");
                    finish();
                } else {
                    // записываю в юзера данные
                    concotenate_number("--");
                    //new execute().redact_user(redact.this,toBe());
                    Log.i("redact_flag", String.valueOf(flag) + "--изменили значение");
                    // concotenate_number(enumber.getText().toString());

                }
                // Log.i("redact_flag",String.valueOf(flag)+"---"+liner.toString());
            }
        };
        bt.setOnClickListener(clk);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    // разбиваю redact_liner: [714, df, null, 8888, null, 2016-0-8, 1]
// на поля
    void parseName(String number) {

        /*получил строку
         *разбил имя
         *вывел пр едитам*/

        execute getInf = new execute();
        liner = getInf.getLine_(this, number);

        Log.i("redact_liner", liner.toString());

        if (liner.size() != 0) {
            enumber.setText(liner.get(3));
            eulr.setText(liner.get(4));

            Log.i("redact", String.valueOf(liner.size()) + "---" + liner.get(1));
            if (liner.get(2) == null) {
                if (liner.get(1) == null) {
                    ename.setText("--");
                    efamily.setText("--");
                } else {
                    String[] prs_name = liner.get(1).split(" ");
                    Log.i("redact_2", String.valueOf(prs_name.length) + "--" + prs_name[0]);

                    if (prs_name.length > 1) {
                        ename.setText(prs_name[0]);
                        efamily.setText(prs_name[1]);
                    } else {
                        ename.setText(prs_name[0]);
                        //  efamily.setText("--");
                    }
                }
            } else {
                ename.setText(liner.get(1));
                efamily.setText(liner.get(2));

            }

        }
    }

    // собираю данные из полей и проводу сравнение изменились или нет
    ArrayList<String> toBe() {
        /* если без данные из лайнера соответствуют данным в эдитаз то
        по нажатию на батон закрываемся
        если нет то пишем в базу
        * */
        String name = ename.getText().toString();
        String family = efamily.getText().toString();
        String number = enumber.getText().toString();
        String ulr = eulr.getText().toString();

        /*если значение пусто то null*/
        ArrayList<String> newline = new ArrayList();
        newline.add(liner.get(0));
        newline.add(name);
        newline.add(family);
        newline.add(number);
        newline.add(ulr);

        for (int i = 1; i < newline.size(); i++) {
            //значение соответствует
            if (newline.get(i).equals("")) {
                newline.set(i, null);
            }
        }

        if (liner.subList(0, 5).equals(newline)) {
            // если соответствует
            flag = false;
            Log.i("liner_contalins", newline.toString());
        } else {
            flag = true;
            Log.i("liner_no_contalins", newline.toString() + ""
                    + liner.subList(0, 5));

        }
        return newline;
    }

    // если номер поменялся проверяю его вхождение в users
    // пусто-пишу
    //нет....
    String concotenate_number(String number) {

        String s_num = "";
        // если номер не поменялся то update по id(exec.redact)
        if (enumber.getText().toString().equals(liner.get(3))) {

            new execute().redact_user(redact.this, toBe());
            finish();


        } else {
            List line = new ArrayList<String>();
            line = new execute().getLine_
                    (this, enumber.getText().toString());
            // если поменялся но его еше нет в users
            if (line.size() == 0) {
                new execute().redact_user(redact.this, toBe());
                finish();

            } else {

                //всплыванка номер не уникален обьеденить с "данные владельца"

                //переписываю count in users
                //поиск строк в clients (кол-во строк есть count)

                //или ищу Count в users
                // execute.getLine_(Context context, String s
                // счетчик посещений  старого номера
                String count_old_num = new execute().getLine_(this, liner.get(3)).get(6);
                //Log.i("redact_con_old_n_cnt",count_old_num);
                Log.i("redact_con_old_liner", liner.toString());
                // счетчик нового(обьединяемого) номеа
                //og.i("redact_con_union_n_cnt",line.get(6).toString());
                Log.i("redact_con_union_line", line.toString());

                // суммируем счетчик
                final int count_sum = Integer.parseInt(count_old_num)
                        + Integer.parseInt(line.get(6).toString());
                Log.i("sum_conk_count", "" + count_sum);

                //ставлю сташую дату к номеру enumber.getText()
                try {
                    bigDate(liner.get(5).toString(),
                            line.get(5).toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                AlertDialog.Builder ad;
                ad=new AlertDialog.Builder(this);
                ad.setTitle("UNION");
                final List finalLine = line;
                final List finalLine1 = line;
                ad.setCancelable(false).setPositiveButton("ok",
                        new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        ArrayList<String> nln=new ArrayList<String>();
                        //id

                        //date
                        try {
                            nln.add(finalLine.get(0).toString());
                            //name
                            nln.add(finalLine.get(1).toString());
                            //family
                            nln.add(finalLine.get(2).toString());
                            //num
                            nln.add(finalLine.get(3).toString());
                            //vk
                            nln.add(finalLine.get(4).toString());
                            //count
                            nln.add(String.valueOf(count_sum));
                            nln.add(bigDate(liner.get(5).toString(), finalLine1.get(5).toString()));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        //счетчтик count_sum новый
                        //номер на который переписали enumber.getText -  и он есть в базе
                        //правильная датаbigDate(liner.get(5).toString(),line.get(5).toString());
                        //получить _id getLine_(Context context, "новый номер").get(0)

                        // обновить записи в clients
                       /*
                        new execute().upd_cl_num(redact.this,"clients",enumber.getText().toString(),
                                liner.get(3));
                        new execute().redact_user2(redact.this,nln);

                    */
                        Toast.makeText(redact.this, "номер не уникален", Toast.LENGTH_LONG).show();

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                ad.show();


                //++++++по ОК переписываю записи в clients
                //new execute().upd_cl_num(this,"clients",
                //enumber.getText().toString(),line.get(3).toString());

                //+++++удаляю строку со старым номером
                //execute.deleterow(Context context, String table)
            }


        }


        return s_num;
    }

    String bigDate(String sdat1, String sdat2) throws ParseException {
        String res = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dat1 = sdf.parse(sdat1);
        Date dat2 = sdf.parse(sdat2);
        if (dat1.getTime() < dat2.getTime()) {
            Log.i("redact_bigD_2", sdat2);
            res = sdat2;
        } else {
            Log.i("redact_bigD_1",sdat1);
            res = sdat1;
        }
        return res;
    }





}
