package ru.zsoft.com.stgeorg;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import com.jjoe64.graphview.*;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


/*
* Подсчет записей в текущем месяце поденно
*         Новых клиентов
* общая сумма за месяц
* кликнув день открыть журнал
*               {4 мая------5
                 5мая-----7
                 8мая-----11 выбрали: открылась сумма и имена клиентов }
* вкладка клиенты:
{лист со списком имен и фамилий. Если в Клиенте не заполнен реквизит то .....


записывать номер фамилию имя в 2е таблици если нет номера в Клиенте
*
* */
public class common extends MainActivity {
    /*
    обновляшку окна
    gw4 поменять на выпадающтй список
    * группировка по имени
    *
    * подсветить совпадение времени на дату в win_table*/
    private db mDbHelper;

    ListView lw3,lv_Day_count;
    EditText edit_search;
    GridView gw_search,gw_bar;
    Spinner sp_search;
    Button bt_search;
    GraphView graph;
    int ink;
    List<String>resultset;
    //List<String> sss=new ArrayList<String>();
   // ProgressBar prb;
   // String q_year;
   // ArrayList<ArrayList<Map<String, String>>> childData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
/// очееоедь
        super.onCreate(savedInstanceState);

        Log.i("L_data_yy", String.valueOf(yyyy));
        setContentView(R.layout.common2);
        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();
        TabHost.TabSpec tabSpec;

        tabSpec = tabHost.newTabSpec("tab1");
        tabSpec.setContent(R.id.tab1);
        tabSpec.setIndicator("Журнал");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tab2");
        tabSpec.setContent(R.id.tab2);
        tabSpec.setIndicator("Клиенты");
        tabHost.addTab(tabSpec);


        tabSpec = tabHost.newTabSpec("tab5");
        tabSpec.setContent(R.id.tab5);
        tabSpec.setIndicator("Cчетчик ");
        tabHost.addTab(tabSpec);
        tabHost.setCurrentTab(0);

        lw3 = (ListView) findViewById(R.id.users);
        lv_Day_count =(ListView)findViewById(R.id.lv_day_log);

        //tab2(клиенты)
        ArrayAdapter<String> ad_dat3 = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, userlist("limit 30",this));
        lw3.setAdapter(ad_dat3);

        //очеоедь
        go_searh();
        get_Day_ord();

        if(new execute().Day_count(this).size()!=0)
        graph(new execute().Day_count(this));
        statistic_options();
        //count_graph();

    }
    // читаю юзер all 30 lim
    List<String> userlist(String lim, Context ct) {
        List<String> uslist = new ArrayList<String>();
        mDbHelper = new db(ct);
        mDbHelper.getWritableDatabase();
        SQLiteDatabase db2 = mDbHelper.getWritableDatabase();
        //db1.execSQL("SELECT * FROM clients where date like '"+ddat+"%'");
        //Cursor c = db1.query("clients",null,null,null,null,null,null);
        // Cursor c = db1.rawQuery("SELECT * FROM clients where date like '" + ddat + "%'", null);
        Cursor c = db2.rawQuery("SELECT * FROM user where last is not null order by count desc "+lim, null);

        if (c.moveToFirst()) {
            int pk_num = c.getColumnIndex("pk_num");
            int name = c.getColumnIndex("name");
            int family = c.getColumnIndex("family");
            int url = c.getColumnIndex("url");
            int count = c.getColumnIndex("count");
            int last = c.getColumnIndex("last");

/*обход нулей прри чтении*/
            do {
                uslist.add(c.getString(pk_num).toString() + "   "
                        + c.getString(name).toString() + "   "
                        //+ c.getString(family).toString() + " "
                        //  + c.getString(url).toString() + " "
                        + c.getString(last).toString() + "  колличество:  "
                        + c.getString(count).toString());
            }

            while (c.moveToNext());

            c.close();
        }
        return uslist;


    }

    // поиск по имени или номеру
    void go_searh(){
     /*  на onCreate  выести все ресурсы
      *  по батон клику запустить метод поиска
      */
        String[]param={"Номер","Имя"};

        bt_search = (Button) findViewById(R.id.button5);
        edit_search = (EditText) findViewById(R.id.search_user);
        gw_search = (GridView) findViewById(R.id.gv_search);
        sp_search=(Spinner)findViewById(R.id.sp_param);

        final ArrayAdapter ad_search = new ArrayAdapter(this, android.R.layout.simple_list_item_1, param);
        sp_search.setAdapter(ad_search);

        // клик по имени и номер, закрыть слушатель если i%3==0
        gw_search.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i%3==0){

                edit_search.setText(new windows_tables().getnum(common.this,resultset.get(i),
                        resultset.get(i+2),resultset.get(i+1)));
            }else{
                    edit_search.setText("");
                }

            }
        });
        // show lastDate and date1 from clients
        gw_search.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(common.this,"--",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        View.OnClickListener clk=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int attr= (int) sp_search.getSelectedItemId();
                String coloumn = null;

                switch (attr) {
                    case 0:
                        coloumn="sf_num";
                break;

                    case 1:
                        coloumn="name";
                break;
                }

                String qu_searsh="select \n" +
                        "       name,    \n" +
                        "       time1,   \n" +
                        "       sf_num,  \n" +
                        "       pay,     \n" +
                        "       date,    \n" +
                        "       date1    \n" +
                        "                \n" +
                        "from clients where "+coloumn+" like '%"+edit_search.getText()+
                        "%' order by date DESC limit 200";

                execute search_cl=new execute();
                search_cl.search(common.this,qu_searsh);

                resultset= search_cl.search(common.this,qu_searsh);
                final ArrayAdapter res_search = new ArrayAdapter(common.this, android.R.layout.
                                                                 simple_list_item_1,resultset);
                gw_search.setAdapter(res_search);

                Log.i("attr",qu_searsh);
                // Log.i("attr",search_cl.search(common.this,qu_searsh).toString());

            }

            ;
        };
        bt_search.setOnClickListener(clk);




    }

    void get_Day_ord(){
        //new execute().Day_count(this);
        lv_Day_count.setAdapter(new ArrayAdapter<String>
                (this,android.R.layout.simple_list_item_1,new execute().Day_count(this)));

    }

    void graph(List l_day_count){
       // List<String>data=new execute().Day_count(this);

        List<String>data=l_day_count;
        List<String> gr_dat = new ArrayList<String>();;
        String[] x=new String[data.size()];

         graph = (GraphView) findViewById(R.id.graph);
         graph.setTitle("Test");

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
        });

       if(data.size()!=0)
        for(int i=0;i<data.size();i++) {
            String[] s = data.get(i).split(" ");
            gr_dat.add(s[0]);
            x[i] = (s[3]+" "+s[4]);
           // series.appendData(new DataPoint(i, Integer.parseInt(s[0])), true,data.size(), true);
            series.appendData(new DataPoint(i,Integer.parseInt(s[0])),true,data.size(),true);

        }
            series.setColor(Color.GREEN);
            series.setDrawDataPoints(true);
            series.setDataPointsRadius(5);
            series.setThickness(6);
            Log.i("DAT", gr_dat.toString() + "  count  " + data.size());

            StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
           staticLabelsFormatter.setHorizontalLabels(x);
            Log.i("common_graph_[x]",""+x[1]);
            graph.addSeries(series);



    }

    void statistic_options(){

        /*Выбор интервала
        * всплывашка с календарем(от/до)
        * общий недельный
        * общ месяц*/

        /*Статистика
        * count momth(year)
        * count week*/


        gw_bar=(GridView)findViewById(R.id.gr_bar);
        final List bar=new ArrayList<String>();
        bar.add("<<<");
        bar.add("+++");
        bar.add(">>>");
        final List <String>l_day=new execute().Day_count(common.this);
        final int i_wiev=30;

        gw_bar.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,bar));
        if(l_day.size()>i_wiev)
       // {

            gw_bar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    int ost = l_day.size() % i_wiev;
                    switch (bar.get(i).toString()) {
                        case ">>>":

                            if (ink + i_wiev + ost >= l_day.size()) {
                                graph.removeAllSeries();
                                graph(l_day.subList(ink, l_day.size()));
                                break;
                            } else {
                                graph.removeAllSeries();
                                ink = ink + i_wiev;
                                graph(l_day.subList(ink, ink + i_wiev));

                                Log.i("comm_st_o_ink", ""
                                        + l_day.subList(ink, ink + i_wiev));
                            }
                            break;
                        case "<<<":

                            if (ink < i_wiev) {
                                break;
                            } else
                                graph.removeAllSeries();
                            ink = ink - i_wiev;
                            graph(l_day.subList(ink, ink + i_wiev));
                            Log.i("comm_st_o_ink", ""
                                    + l_day.subList(ink, ink + i_wiev));
                            break;

                        case "+++":

                            LayoutInflater li = LayoutInflater.from(common.this);
                            View alview = li.inflate(R.layout.alert_interval_select, null);

                            AlertDialog.Builder ad_bld = new AlertDialog.Builder(common.this);
                            ad_bld.setView(alview);

                            final EditText et_fr = (EditText) alview.findViewById(R.id.et_from);
                            final EditText et_to = (EditText) alview.findViewById(R.id.et_to);

                            ad_bld.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    Toast.makeText(common.this, interval("May"),
                                            //et_fr.getText().toString(),
                                            Toast.LENGTH_SHORT).show();

                                    graph.removeAllSeries();
                                    graph(l_day.subList(
                                            Integer.parseInt(interval(et_fr.getText().toString())),
                                            Integer.parseInt(interval(et_to.getText().toString()))));
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });

                            AlertDialog alert = ad_bld.create();
                            alert.show();
                            break;
                    }

                }
            });
      // }else{
      //      if(l_day.size()!=0)
       //     graph.removeAllSeries();
       //     graph(l_day.subList(ink, l_day.size()));
      //  }

    }
    // получить индекс массива по дате
    String interval(String d1){
        int ind=0;
        String s_ind="";
        for(Object elt:new execute().Day_count(this)){

        ind++;
            if(elt.toString().contains(d1)){
                s_ind=""+ind;
            }

        }

        return s_ind;
    }



}

