package com.example.onedee.ui.logout;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.onedee.CalendarClass;
import com.example.onedee.DisplayCalendar;
import com.example.onedee.LoginScreen;
import com.example.onedee.R;
import com.example.onedee.ui.home.HomeFragment;

public class LogoutFragment extends Fragment {

    private LogoutViewModel logoutViewModel;
    Button yesButton, noButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        logoutViewModel =
                ViewModelProviders.of(this).get(LogoutViewModel.class);
        View root = inflater.inflate(R.layout.fragment_logout, container, false);
        //final TextView textView = root.findViewById(R.id.text_logout);
        logoutViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });

        yesButton = (Button) root.findViewById(R.id.logout_yes);
        noButton = (Button) root.findViewById(R.id.logout_no);

        //If user chooses to log out, bring user back to log in screen
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LoginScreen.class);
                startActivity(intent);
            }
        });
        //else we bring the user back to the calendar
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DisplayCalendar.class);
                startActivity(intent);
            }
        });

        return root;
    }

}