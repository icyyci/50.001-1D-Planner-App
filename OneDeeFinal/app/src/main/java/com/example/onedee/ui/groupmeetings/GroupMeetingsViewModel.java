package com.example.onedee.ui.groupmeetings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class GroupMeetingsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public GroupMeetingsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is group meetings fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}