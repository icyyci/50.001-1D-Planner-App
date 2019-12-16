package com.example.onedee.ui.todolist;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.onedee.AddEventScreen;
import com.example.onedee.CalendarClass;
import com.example.onedee.EventClass;
import com.example.onedee.LoginScreen;
import com.example.onedee.R;
import com.example.onedee.ToDoSettingsPage;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TodoListFragment extends Fragment {

    com.example.onedee.ui.todolist.TodoListViewModel TodoListViewModel;
    Button btnAddToDo, btnOptimise;
    ListView ToDoList;
    public static ArrayList<String> arrayList = new ArrayList<String>();
    public static ArrayList<EventClass> eventList = new ArrayList<EventClass>();
    public static final String TITLE = "";


    ArrayAdapter<String> adapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        TodoListViewModel =
                ViewModelProviders.of(this).get(TodoListViewModel.class);
        View root = inflater.inflate(R.layout.fragment_todo_list, container, false);
        //final TextView textView = root.findViewById(R.id.text_todo_list);
        TodoListViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });

        for(EventClass i: CalendarClass.getFlexibleEvents()){
            if(!arrayList.contains(i.toString())) {
                arrayList.add(i.toString());
            }
            if(!eventList.contains(i)) {
                eventList.add(i);
            }
        }

        btnAddToDo = (Button) root.findViewById(R.id.btnPlus);
        btnOptimise = (Button) root.findViewById(R.id.btnOptimise);
        ToDoList = (ListView) root.findViewById(R.id.lvtodolist);


        adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, arrayList);

        ToDoList.setAdapter(adapter);




        btnAddToDo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ToDoSettingsPage.class);
                startActivity(intent);
            }
        });

        btnOptimise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = CalendarClass.optimise();
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        });

        return root;



    }

    @Override
    public void onResume() {
        super.onResume();
        for(EventClass i: CalendarClass.getFlexibleEvents()){
            if(!arrayList.contains(i.toString())) {
                arrayList.add(i.toString());
            }
        }

        adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, arrayList);

        ToDoList.setAdapter(adapter);

        ToDoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                PopupMenu popup = new PopupMenu(getActivity(), ToDoList);
                popup.getMenuInflater()
                        .inflate(R.menu.popup_todo, popup.getMenu());
                final int index = position;
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    public boolean onMenuItemClick(MenuItem item) {

                        if(item.getTitle().equals("Delete Todo")){
                            Log.i("delete todo", "Event list: "+ eventList);
                            Log.i("delete todo", "Event list: "+ arrayList);

                            /** When we want to delete a flexible event, we need to query for that particular event then we delete */

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                            Query EventsQuery;
                            EventsQuery = ref.child(LoginScreen.getUsername()).child("FlexibleEvents").orderByChild("name").equalTo(eventList.get(index).getName());

                            EventsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot eventSnapshot: dataSnapshot.getChildren()) {
                                        eventSnapshot.getRef().removeValue();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });

                            Toast.makeText(getActivity(), "You deleted "+eventList.get(index).name, Toast.LENGTH_LONG).show();
                            CalendarClass.deleteEvent(eventList.get(index));
                            eventList.remove(index);
                            arrayList.remove(index);
                            adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, arrayList);
                            ToDoList.setAdapter(adapter);

                        }

                        else{
                            Intent intent = new Intent(getActivity(),ToDoSettingsPage.class);
                            intent.putExtra(TITLE, eventList.get(index).name);
                            intent.putExtra("INDEX", index);
                            intent.putExtra("DURATION", eventList.get(index).duration);
                            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                            String formatDate = eventList.get(index).dueDate.format(dateFormatter);
                            String formatTime = eventList.get(index).dueDate.format(timeFormatter);
                            intent.putExtra("DUE_DATE", formatDate);
                            intent.putExtra("DUE_TIME", formatTime);

                            startActivity(intent);
                        }

                        return true;

                    }

                });
                popup.show();
            }
        });

    }
}