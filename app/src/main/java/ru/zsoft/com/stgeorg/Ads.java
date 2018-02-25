package ru.zsoft.com.stgeorg;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by User on 27.01.2017.
 */

public class Ads extends BaseAdapter {
    private final Activity context;
    private ArrayList<Lines_data> data;
    Context ct;
    LayoutInflater inflter;
    String index;

    //=new ArrayList<String>();

    public Ads(Activity context, ArrayList<Lines_data> data) {
        this.context = context;
        this.data=data;
        inflter = (LayoutInflater.from(context));

    }
    public void updateResults(ArrayList<Lines_data> results) {
    /* обновляю базу
    * из базы получить массив
    * перегнать его в liner
    * data преровнять к нему
    * новый liner=data
    * */

        data = results;
        //Triggers the list update

        notifyDataSetChanged();
    }

    ArrayList<Lines_data> update(){
        windows_tables rd=new windows_tables();
        rd.set_test(rd.read2(context,MainActivity.selectedDate));
        ArrayList<Lines_data>ln_d=new ArrayList<Lines_data>();
        ln_d=rd.liner;
        return ln_d;
    }

    private class ViewHolder {

        public TextView names,time;
        public CheckBox chek;
        public EditText edit;

        public ViewHolder(View rowView ) {
            names = (TextView) rowView.findViewById(R.id.name);
            time = (TextView) rowView.findViewById(R.id.time);
            chek = (CheckBox) rowView.findViewById(R.id.checkbox);
            edit = (EditText) rowView.findViewById(R.id.edSum);
        }
    }


    @Override
    public int getCount() {
        int count=data.size();
        return count;
    }
    @Override
    public Object getItem(int i) {
        return null ;
    }
    @Override
    public long getItemId(int i) {
        return i;
    }
    @Override
    public View getView(final int i, final View convertView, ViewGroup viewGroup) {
        final ViewHolder holder;

        final Lines_data dat=data.get(i);

        View view = convertView;
        if (view == null) {
            view = inflter.inflate(R.layout.row, viewGroup, false);
            holder = new ViewHolder(view);


            view.setTag(holder);
            // return view;
        } else {
            holder = (ViewHolder) view.getTag();
        }
        //значения
        holder.names.setText(data.get(i).name);
        holder.time.setText(data.get(i).time);
        holder.edit.setText(String.valueOf(data.get(i).sum));
        holder.chek.setChecked(data.get(i).flag);


        holder.chek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer pos = (Integer)v.getTag();
                Log.i("chek0nadapter"," "+data.get(i).id+"name "+data.get(i).name+" pos:"+pos);
              // if(data.get(i).name!="_") {
                    if (holder.chek.isChecked()) {
                        execute vis = new execute();
                        vis.flag_visit(context, "true",
                                data.get(i).id, "visit");
                        updateResults(update());
                       // notifyDataSetChanged();

                    }else{
                        execute vis = new execute();
                        vis.flag_visit(context, "false",
                                data.get(i).id, "visit");
                        updateResults(update());
                        // notifyDataSetChanged();
                    }
                }
            });
        holder.edit.addTextChangedListener(new  MyTextWatcher(dat));
       /* holder.edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b){
                    holder.edit.setText("++");
                }
            }
       });
       */
        view.setTag(holder);
        return view;
        }


    private class MyTextWatcher implements TextWatcher {
        private Lines_data dat;
        private View view;
        EditText edt;
      /*  private MyTextWatcher(View view) {

            this.view = view;
        }
*/
        private  int position;
        private MyTextWatcher(Lines_data dat) {

            this.dat = dat;
        }
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {

           // edt = (EditText)view.findViewById(R.id.edSum);
            Log.i("edit0nadapter",editable + "~~~"+index );
            if (index!=null){
                new execute().flag_visit(context,editable.toString() ,
                 index,"pay");


            }

            return;


        }
    }

    /*
    holder.edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                //Получаем вид с файла prompt.xml, который применим для диалогового окна:
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.alert, null);

                //Создаем AlertDialog
                AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(context);

                //Настраиваем prompt.xml для нашего AlertDialog:
                mDialogBuilder.setView(promp    tsView);

                //Настраиваем отображение поля для ввода текста в открытом диалоге:
                final EditText userInput = (EditText) promptsView.findViewById(R.id.input_text);

                //Настраиваем сообщение в диалоговом окне:
                mDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        //Вводим текст и отображаем в строке ввода на основном экране:
                                        holder.edit.setText(userInput.getText());
                                    }
                                })
                        .setNegativeButton("Отмена",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });

                //Создаем AlertDialog:
                AlertDialog alertDialog = mDialogBuilder.create();

                //и отображаем его:
                alertDialog.show();

            }
        });*/

     /* holder.edit.addTextChangedListener(new TextWatcher(){

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //   if(holder.edit.getText().length()>2)
                Log.i("edit0nadapter", holder.edit.getText() + "~~~" + data.get(i).id);
                //   if(data.get(position).id!="") {
                execute vis = new execute();
                //Adapter_.this
                vis.flag_visit(context,holder.edit.getText().toString() ,
                        data.get(i).id,"pay");
                //  }


            }

        });

*/

//notifyDataSetChanged();

}
