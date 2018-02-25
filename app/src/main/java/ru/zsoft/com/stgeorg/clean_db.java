package ru.zsoft.com.stgeorg;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.renderscript.Element;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * копировать базу в другую дирректорию
 * копировать из history в temp_history
                users в temp_users
 * удалить clients за исключением посл месяца
 * переписать из temp_history в history
               temp user в user(id or pk_num and count)
 * очистить temp
 */

public class clean_db extends MainActivity {
    private db mDbHelper;
    ListView  lw2,lwView;
    ProgressBar  prb;
    Button bt6;
    Button bt_qu;
    ExpandableListView exp;
    EditText tv_name, tv2_num;
    GridView gw_t;
    NumberPicker np;
    Spinner month;
    int np_day, sp_m;
    String  head_gr,head_ch;

    public static final int IDM_DELETE = 102;
    public static final int IDM_RETURN = 103;
    public static final int IDM_DELETE_BUP = 104;


    final String ATTR_GROUP_NAME = "groupname";
    final String ATTR_PHONE_NAME = "phoneneame";
    public static final String[] s_month = {"январь", "февраль", "март", "апрель",
            "май", "июнь", "июль", "август",
            "сентябрь", "октябрь", "ноябрь", "декабрь"};

    Map<String, String> m;

    ArrayList<Map<String, String>> groupData;
    ArrayList<Map<String, String>> childItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.common);
        TabHost tabHost = (TabHost) findViewById(R.id.tabHost2);
        tabHost.setup();
        TabHost.TabSpec tabSpec;


        tabSpec = tabHost.newTabSpec("tab4");
        tabSpec.setContent(R.id.tab4);
        tabSpec.setIndicator("Настойки");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tab3");
        tabSpec.setContent(R.id.tab3);
        tabSpec.setIndicator("Очередь");
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tab6");
        tabSpec.setContent(R.id.tab6);
        tabSpec.setIndicator("Пусто");
        tabHost.addTab(tabSpec);


        lw2 = (ListView) findViewById(R.id.journals);
        lw2.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,new ArrayList<String>()));

        queue();
        view_buckUp();

        prb=(ProgressBar)findViewById(R.id.prBar);
        prb.setVisibility(View.GONE);

        bt6=(Button)findViewById(R.id.button6);
      //  bt6.setVisibility(View.GONE);
        View.OnClickListener clk=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cleandb catTask = new cleandb();
                catTask.execute();
            }
        };
        bt6.setOnClickListener(clk);
    }

    // st_1() копирую дневные каунты во временную таблицу history to temp
    public class cleandb extends AsyncTask<Void, Integer, List<String>> {
        execute tt=   new execute();
        ArrayAdapter adapt;
        long st,fn;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            prb.setMax(tt.get_count("history",clean_db.this));
            adapt=(ArrayAdapter<String>)lw2.getAdapter();
            bt6.setVisibility(View.INVISIBLE);
            prb.setVisibility(View.VISIBLE);
            backup_2();
            adapt.clear();
            st=System.currentTimeMillis();
        }

        @Override
        protected List<String> doInBackground(Void... lt) {

            //  tt.reads(common.this);
            //   publishProgress( tt.reads(common.this));
            int ii = 0;

            mDbHelper = new db(getApplicationContext());
            mDbHelper.getWritableDatabase();
            SQLiteDatabase db1 = mDbHelper.getWritableDatabase();

            ContentValues val = new ContentValues();
            Cursor c = db1.rawQuery("SELECT * FROM history", null);
            db1.beginTransaction();
            if (c.moveToFirst()) {
                int id = c.getColumnIndex("id");
                int date = c.getColumnIndex("date");
                int d_count = c.getColumnIndex("d_count");

                do {
                    ii++;
                    publishProgress(ii++);
                    val.put(db.id_his_t, c.getString(id));
                    val.put(db.date_his_t, c.getString(date));
                    val.put(db.count_his_t, c.getString(d_count));

                    db1.insert("temp_hist", null, val);

                }
                while (c.moveToNext());
                c.close();
            }
           db1.setTransactionSuccessful();
            db1.endTransaction();

            // TimeUnit.SECONDS.sleep(1);
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            //adapt.add(values[0]);

            prb.setProgress(values[0]);


        }

        @Override
        protected void onPostExecute(List<String> aVoid) {
            super.onPostExecute(aVoid);
            fn=System.currentTimeMillis()-st;
            prb.setProgress(0);
            adapt.add("1-st_step table have "+tt.get_count("history",clean_db.this)+" rows\n"+fn);
            bt6.setVisibility(View.VISIBLE);  // показываем кнопку
            Log.i("sss_sss", String.valueOf(aVoid));
            new clean_db2().execute();

        }


    }
    // st_2tr копирую всех посетителей во временную таблицу user to temp
    // удаляю все значения из clients кроме последних deleterow()
    public class clean_db2 extends AsyncTask<Void, Integer, Void> {
        ArrayAdapter adp;
        execute tt=   new execute();
        long st,fn;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            adp=(ArrayAdapter<String>)lw2.getAdapter();
            prb.setMax(tt.get_count("user",clean_db.this));
            st=System.currentTimeMillis();
            prb.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            int ii = 0;

            mDbHelper = new db(getApplicationContext());
            mDbHelper.getWritableDatabase();
            SQLiteDatabase db1 = mDbHelper.getWritableDatabase();

            ContentValues val = new ContentValues();
            Cursor c = db1.rawQuery("SELECT id,pk_num,count FROM user", null);
            db1.beginTransaction();
            if (c.moveToFirst()) {
                int id = c.getColumnIndex("id");
                int pk_num = c.getColumnIndex("pk_num");
                int count = c.getColumnIndex("count");

                do {
                    ii++;
                    publishProgress(ii++);

                    val.put(db.ID_TEMP,c.getString(id));
                    val.put(db.CONTACT_TEMP,c.getString(pk_num));
                    val.put(db.PAY_TEMP,c.getString(count));
                    val.put(db.NAME_TEMP,"--");

                    db1.insert("temp", null, val);
                }
                while (c.moveToNext());
                c.close();
            }
            db1.setTransactionSuccessful();
            db1.endTransaction();
            db1.close();
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            prb.setProgress(values[0]);
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            fn=System.currentTimeMillis()-st;
            adp.add("2-nd_step_table have "+tt.get_count("user",clean_db.this)+" rows \n"+fn);
            prb.setProgress(0);
            delete_row();
            new upd_user().execute();


        }
    }
    // st_3 tr переписываю users
    public class upd_user extends AsyncTask<Void, Integer, Void>{
        execute tt=   new execute();
        ArrayAdapter adapt;
        long st,fn;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            prb.setMax(tt.get_count("temp",clean_db.this));
            adapt=(ArrayAdapter<String>)lw2.getAdapter();
            prb.setVisibility(View.VISIBLE);
            st=System.currentTimeMillis();

        }

        @Override
        protected Void doInBackground(Void... voids) {
            int ii = 0;

            mDbHelper = new db(getApplicationContext());
            mDbHelper.getWritableDatabase();
            SQLiteDatabase db1 = mDbHelper.getWritableDatabase();
            db1.beginTransaction();
            ContentValues val = new ContentValues();
            Cursor c = db1.rawQuery("SELECT * FROM temp", null);

            if (c.moveToFirst()) {
                int id = c.getColumnIndex("_id");
                int pk_num = c.getColumnIndex("sf_num");
                int count = c.getColumnIndex("pay");

                do {
                    ii++;
                    publishProgress(ii++);
                    //  val.put(db.ID_TEMP,c.getString(id));
                    //  val.put(db.CONTACT_TEMP,c.getString(pk_num));
                    val.put(db.count_us,c.getString(count));
                    db1.update("user",val,"pk_num = '"+c.getString(pk_num)+"'",null);


                }
                while (c.moveToNext());
                c.close();
            }
            db1.setTransactionSuccessful();
            db1.endTransaction();
            db1.close();
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            prb.setProgress(values[0]);
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            fn=System.currentTimeMillis()-st;
            adapt.add("3-rd_step_table have "+tt.get_count("temp",clean_db.this)+" rows \n"+fn);
            new execute(). deleterow(clean_db.this, "temp");
            prb.setProgress(0);
            view_buckUp();
            Toast.makeText(clean_db.this,"выполннено",Toast.LENGTH_SHORT).show();
            //  new upd_hist().execute();
        }


    }
    //st_4(from tmp_to_history)NOT USE
    public class upd_hist extends AsyncTask<Void, Integer, Void>{
        execute tt=   new execute();
        ArrayAdapter adapt;
        long st,fn;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            prb.setMax(tt.get_count("temp_hist",clean_db.this));
            adapt=(ArrayAdapter<String>)lw2.getAdapter();
            prb.setVisibility(View.VISIBLE);
            st=System.currentTimeMillis();

        }

        @Override
        protected Void doInBackground(Void... voids) {
            int ii = 0;

            mDbHelper = new db(getApplicationContext());
            mDbHelper.getWritableDatabase();
            SQLiteDatabase db1 = mDbHelper.getWritableDatabase();
           // db1.beginTransaction();
            ContentValues val = new ContentValues();
            Cursor c = db1.rawQuery("SELECT * FROM temp_hist", null);

            if (c.moveToFirst()) {
                int id = c.getColumnIndex("id");
                int date = c.getColumnIndex("date");
                int d_count = c.getColumnIndex("d_count");

                do {
                    ii++;
                    publishProgress(ii++);
                    val.put(db.id_his,c.getString(id));
                    val.put(db.date_his,c.getString(date));
                    val.put(db.count_his,c.getString(d_count));
                    db1.insert("history",null,val);
                    // db1.setTransactionSuccessful();

                }
                while (c.moveToNext());
              //  db1.endTransaction();
                c.close();

            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            prb.setProgress(values[0]);
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            fn=System.currentTimeMillis()-st;
            adapt.add("4-rd_step_table have "+tt.get_count("temp_hist",clean_db.this)+" rows \n"+fn);
            new execute(). deleterow(clean_db.this, "temp_hist");
            prb.setProgress(0);
        }




    }

    void backup_2(){
        InputStream in=null;
        OutputStream out=null;

       //SimpleDateFormat datinback=new SimpleDateFormat("YYYY:MM:hh:mm");

        Date now =Calendar.getInstance().getTime();
        // косяк из-за текущего формата на устройстве
        // String nowDate = String.valueOf(datinback.format(now));

      
      String nowDate = String.valueOf(now);

        try {
        File mdr=new File("/sdcard/archive");
        if(!mdr.exists()){
            mdr.mkdirs();
        }

            in= new FileInputStream("/sdcard/sdcard/st.db");
            out= new FileOutputStream("/sdcard/archive/st_"+nowDate+".db");
            byte buff []=new byte[1024];
            int read;

            try {
                while ((read=in.read(buff))!=-1){
                    out.write(buff,0,read);
                }
                in.close();
                in=null;

                out.flush();
                out.close();
                out=null;

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    void view_buckUp(){
        /*показать папку archive*/

        lwView=(ListView)findViewById(R.id.lvViwerBuck);
        lwView.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,
                View("/sdcard/archive")));
        /*
        lwView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(clean_db.this, View("/sdcard/archive").get(i)+" ",Toast.LENGTH_SHORT).show();
            }
        });
        */
        registerForContextMenu(lwView);
        lwView.setOnCreateContextMenuListener(this);
    }

    public  List View(String way){
        File dir=new File(way);
        List fld= new ArrayList<ArrayList>();

        if(dir.isDirectory()){
            File[] lfld=dir.listFiles();
            fld.add("<<<");
            for(File elt:lfld){
                fld.add(elt.getName());

            }
        }
        return fld;
    }
    //clients чищу таблицу
    public boolean  delete_row(){
        mDbHelper = new db(this);
        SQLiteDatabase db1 = mDbHelper.getWritableDatabase();
        //return db1.delete("clients"," _id not in (select _id from clients order by date DESC limit 450)",null)>0;
        return db1.delete("clients"," date like '2017%' ",null)>0;
    }

    public void del_buckup(String way,String fl){
        File fld=new File(way+fl);
        Boolean delete=fld.delete();

    }

    public void return_copy(String way,String fl){
        /*копируем в папку sdcard
        * удаляем st.db
        * переимеовываеи то что скопировали в st.db*/

        InputStream in=null;
        OutputStream out=null;
        del_buckup("/sdcard/sdcard/","st.db");
        try {
            File mdr=new File(way);
            if(!mdr.exists()){
                mdr.mkdirs();
            }

            in= new FileInputStream("/sdcard/archive/"+fl);
            out= new FileOutputStream("/sdcard/sdcard/st.db");
            byte buff []=new byte[1024];
            int read;

            try {
                while ((read=in.read(buff))!=-1){
                    out.write(buff,0,read);
                }
                in.close();
                in=null;

                out.flush();
                out.close();
                out=null;

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


      @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {


                if(v.getId()==R.id.lvViwerBuck){
                    head_gr=null;

                    AdapterView.AdapterContextMenuInfo inf =
                            (AdapterView.AdapterContextMenuInfo) menuInfo;
                    head_gr=View("/sdcard/archive").get(inf.position).toString();
                    menu.setHeaderTitle(View("/sdcard/archive").get(inf.position).toString());
                    menu.add(menu.NONE, IDM_RETURN, menu.NONE, "Восстановить");
                    menu.add(menu.NONE, IDM_DELETE_BUP, menu.NONE, "Удалить точку");

                }else if(v.getId()==R.id.exp_lw){
                    head_gr=null;
                    ExpandableListView.ExpandableListContextMenuInfo info =
                            (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
                    // через кейс

                    int type =
                            ExpandableListView.getPackedPositionType(info.packedPosition);
                    int group =
                            ExpandableListView.getPackedPositionGroup(info.packedPosition);
                    int child =
                            ExpandableListView.getPackedPositionChild(info.packedPosition);
                    // Only create a context menu for child items
                    if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                        // Array created earlier when we built the expandable list
                        head_ch = ((TextView) info.targetView).getText().toString();
                        menu.setHeaderTitle("-----"+head_ch);
                        menu.add(menu.NONE, IDM_DELETE, menu.NONE, "Удалить Запись");

                    }
                    else if(type == ExpandableListView.PACKED_POSITION_TYPE_GROUP){
                        head_gr = ((TextView) info.targetView).getText().toString();
                        menu.setHeaderTitle("-----"+head_gr);
                        menu.add(menu.NONE, IDM_DELETE, menu.NONE, "Удалить Запись");
                    }


                }


    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {


        windows_tables del=new windows_tables();

        switch (item.getItemId()) {


            case IDM_DELETE:
                del.deleterow(this,"queue_user",head_gr,"%","");
                queue();
                /*
                Intent i = new Intent( this , this.getClass() );
                finish();
//колхозный запуск активити
                Intent mainUpd=new Intent( this,common.class);
                mainUpd.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainUpd);
                */

                break;
            case IDM_RETURN:
                return_copy("/sdcard/sdcard/",head_gr);

                break;

            case IDM_DELETE_BUP:
                del_buckup("/sdcard/archive/",head_gr);
                view_buckUp();


                break;
        }
        return super.onContextItemSelected(item);
    }

    // не используется очередь
    public void queue() {
        //   gw4 = (GridView) findViewById(R.id.gr_queue);
        gw_t = (GridView) findViewById(R.id.gr_time);
        month = (Spinner) findViewById(R.id.spinner);
        np = (NumberPicker) findViewById(R.id.numberPicker);
        bt_qu = (Button) findViewById(R.id.button4);
        tv_name = (EditText) findViewById(R.id.editText);
        tv2_num = (EditText) findViewById(R.id.editText2);

//        registerForContextMenu(gw_t);
//       gw_t.setOnCreateContextMenuListener(this);

        np.setMinValue(1);
        np.setMaxValue(31);
        //21.12
        np.setValue(Integer.parseInt(day()[0]));
        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

                if (newVal != 0) ;
                np_day = newVal;
                Log.i("3_number", String.valueOf(newVal));
            }
        });

        List<String> list_queue = new ArrayList<String>();
        final List<Integer> list_time = new ArrayList<Integer>();
        for (int i = 0; i <= 10; i++) {
            list_queue.add(String.valueOf(i));
        }

        for (int i = 0; i <= 23; i++) {
            list_time.add(i);
        }
        adjustGridView(3, 4);

        final ArrayAdapter<String> gv_queue;
        final ArrayAdapter<Integer> gv_time;
        final ArrayAdapter<String> sp_month;
        final execute queue_real = new execute();
        List<String> list_in_adapter = queue_real.read_in(this, "%");

        //gv_queue = new ArrayAdapter(this, android.R.layout.simple_list_item_1,list_in_adapter );

        gv_time = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list_time);
        sp_month = new ArrayAdapter(this, android.R.layout.simple_list_item_1, s_month);
        int ind_spinner;
        // selected month
        final int[] ind_s = new int[1];
        month.setAdapter(sp_month);
        //21.12
        month.setSelection(Integer.parseInt(day()[1]));
        month.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // позишен это индекс месяца, получаю макс кол дей
                GregorianCalendar cld = new GregorianCalendar();
                cld.set(Calendar.MONTH, position);
                int max = cld.getActualMaximum(Calendar.DAY_OF_MONTH);
                sp_m = position;
                np.setMaxValue(max);
                Log.i("3_spinner", String.valueOf(position));

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });
//gw4.setAdapter(gv_queue);
        expand((ArrayList<String>) list_in_adapter);

        final List<String> l_time = new ArrayList<String>();
        gw_t.setAdapter(gv_time);
        gw_t.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                final String[] timeline = {"01:00:00", "02:00:00", "03:00:00", "04:00:00", "05:00:00", "06:00:00", "07:00:00", "08:00:00", "09:00:00",
                        "10:00:00", "11:00:00", "12:00:00", "13:00:00", "14:00:00", "15:00:00", "16:00:00", "17:00:00", "18:00:00",
                        "19:00:00", "20:00:00", "21:00:00", "22:00:00", "23:00:00", "00:00:00"};

                l_time.add(timeline[position - 1]);

                ((TextView) view).setTextSize(15);
                view.setBackgroundColor(0xff00ff00);
            }
        });

        View.OnClickListener clk = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*снимаю имя снисаю номер
                * лист времени
                * месяц из spiner
                * дата из picker
                *
                * записываю в базу и обнуляю*/
                String s_name = tv_name.getText().toString();
                String s_num = tv2_num.getText().toString();
                if (np_day != 0d)

        /* Log.i("L_time+", String.valueOf(l_time) + "day  " + np_day + "month  " + sp_m);
         Log.i("L_data", s_name + "___" + s_num+"___"+yyyy);*/

                    Log.i("L_time+", String.valueOf(l_time) + "day  " + np_day + "month  " + sp_m);
                Log.i("L_data", s_name + "___" + s_num+"___"+yyyy);
                // write in base in queue
                gw_t.setAdapter(gv_time);
                execute ex_qu_write = new execute();
                //  MainActivity ye=new MainActivity();
                // int yy=ye.yyyy;
                ex_qu_write.write_in(clean_db.this, "queue_user", l_time, String.valueOf(MainActivity.yyyy)+"-" + sp_m + "-" + np_day, s_name, s_num, "*****");
                //   ex_qu_write.write_in(common.this, "queue_user", l_time, String.valueOf(q_year)+"-" + sp_m + "-" + np_day, s_name, s_num, "*****");
                l_time.clear();

                List<String> list_in_adapter = queue_real.read_in(clean_db.this, "%");
                //  gw4.setAdapter(new ArrayAdapter(common.this, android.R.layout.simple_list_item_1, queue_real.read_in(common.this,"%") ));
                //вышел за массив
                expand((ArrayList<String>) list_in_adapter);
            }
        };

        bt_qu.setOnClickListener(clk);


    }
    // не испльзуется (создаем експанд лист из обычного массива группировка по имени)
    // для очереди
    void expand(ArrayList<String> inp_mass) {
        exp = (ExpandableListView) findViewById(R.id.exp_lw);


        ArrayList<String> arr = new ArrayList<>();

        int i = 0;
        for (String elt : inp_mass) {

            if (i % 4 == 0 || i == 0) {
                arr.add(inp_mass.get(i));
            }
            i++;

        }


        groupData = new ArrayList<Map<String, String>>();
        for (Object group : new HashSet(arr)) {
            m = new HashMap<String, String>();
            m.put(ATTR_GROUP_NAME, String.valueOf(group));
            groupData.add(m);
        }

        String groupFrom[] = new String[]{ATTR_GROUP_NAME};
        int groupTo[] = new int[]{android.R.id.text1};

        ArrayList<ArrayList<Map<String, String>>> childData = new ArrayList<ArrayList<Map<String, String>>>();

        ArrayList<String> hashArr = new ArrayList<String>();
        for (Object harr : new HashSet(arr)) {
            hashArr.add((String) harr);
        }

        for (int k = 0; k < hashArr.size(); k++) {
            ArrayList<String> spt = new ArrayList<String>();
            childItem = new ArrayList<Map<String, String>>();
            for (int j = 0; j < inp_mass.size(); j++) {
                if (inp_mass.get(j).equals(hashArr.get(k))) {
                    //вышел за массив
                    spt.add(inp_mass.get(j + 1) + "  " + inp_mass.get(j + 2));
                    // spt.add(mass[j+2]);
                }

            }
            Log.i("1_cont_ind", spt.toString());

            for (String elt : spt) {
                m = new HashMap<String, String>();
                m.put(ATTR_PHONE_NAME, elt);
                childItem.add(m);
            }
            childData.add(childItem);
        }


        String childFrom[] = new String[]{ATTR_PHONE_NAME};
        int childTo[] = new int[]{android.R.id.text1};

        SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(this,
                groupData, android.R.layout.simple_expandable_list_item_1, groupFrom, groupTo,
                childData, android.R.layout.simple_list_item_1, childFrom, childTo);

        exp.setAdapter(adapter);
        registerForContextMenu(exp);
        exp.setOnCreateContextMenuListener(this);






    }
    // gridview coloumns
    private void adjustGridView(int n, int m) {
        //  gw4.setNumColumns(n);
        gw_t.setNumColumns(m);

    }
}


