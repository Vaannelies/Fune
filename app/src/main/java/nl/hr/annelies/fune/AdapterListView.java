package nl.hr.annelies.fune;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class AdapterListView extends BaseAdapter {

    Context context;
    ArrayList<ListItemModel> arr;

    public AdapterListView(Context context, ArrayList<ListItemModel> arr) {
        this.context = context;
        this.arr = arr;
    }

    @Override
    public int getCount() {
        return arr.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);

        TextView id = convertView.findViewById(R.id.id);
        TextView name = convertView.findViewById(R.id.title);
        TextView category = convertView.findViewById(R.id.desc);
        Log.i("adapter", arr.get(position).getName());
//        Log.i("adapter", name);


//        id.setText("hoi" + arr.get(position).getId());
        name.setText(arr.get(position).getName());
        category.setText(arr.get(position).getCategory());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("name", arr.get(position).getName());
                intent.putExtra("id", arr.get(position).getId());
                context.startActivity(intent);
            }
        });



        return convertView;
    }
}
