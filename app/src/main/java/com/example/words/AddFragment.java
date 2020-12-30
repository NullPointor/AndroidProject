package com.example.words;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddFragment extends Fragment {
    private Button mButtonSubmit;
    private EditText mEditTextEnglish,mEditTextChinese;
    private WordViewModel mWordViewModel;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentActivity activity = requireActivity();
        //WordViewModel的作用域是整个Activity，所以这里传入activity
        mWordViewModel = ViewModelProviders.of(activity).get(WordViewModel.class);
        mButtonSubmit = activity.findViewById(R.id.buttonSubmit);
        mEditTextEnglish = activity.findViewById(R.id.editTextEnglish);
        mEditTextChinese = activity.findViewById(R.id.editTextChinese);
        mButtonSubmit.setEnabled(false);
        //获取焦点
        mEditTextEnglish.requestFocus();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.showSoftInput(mEditTextEnglish,0);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String english = mEditTextEnglish.getText().toString().trim();
                String chinese = mEditTextChinese.getText().toString().trim();
                mButtonSubmit.setEnabled(!english.isEmpty()&&!chinese.isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        mEditTextEnglish.addTextChangedListener(textWatcher);
        mEditTextChinese.addTextChangedListener(textWatcher);
        mButtonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String english = mEditTextEnglish.getText().toString().trim();
                String chinese = mEditTextChinese.getText().toString().trim();
                Word word = new Word(english,chinese);
                mWordViewModel.insertWords(word);
                NavController navController = Navigation.findNavController(v);
                navController.navigateUp();
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.hideSoftInputFromWindow(v.getWindowToken(),0);
            }
        });
    }

    public AddFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add, container, false);
    }
}
