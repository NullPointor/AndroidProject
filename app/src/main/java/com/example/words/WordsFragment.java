package com.example.words;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class WordsFragment extends Fragment {

    private WordViewModel mWordViewModel;
    private RecyclerView mRecyclerView;
    private MyAdapter mMyAdapter1,mMyAdapter2;
    private LiveData<List<Word>> filteredWords;
    private static final String VIEW_TYPE_SHP = "view_type_shp";
    private static final String IS_USING_CARD_vIEW = "is_using_card_view";
    private List<Word> mWords;
    private boolean undoAction;
    private DividerItemDecoration mDividerItemDecoration;

    public WordsFragment() {
        // Required empty public constructor
        //设置展示工具条
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_words, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mWordViewModel = ViewModelProviders.of(requireActivity()).get(WordViewModel.class);
        mRecyclerView = requireActivity().findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        //添加完之后，刷新序号,回调
        mRecyclerView.setItemAnimator(new DefaultItemAnimator(){
            @Override
            public void onAddFinished(RecyclerView.ViewHolder item) {
                super.onAddFinished(item);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
                assert linearLayoutManager != null;
                int firstPosition = linearLayoutManager.findFirstVisibleItemPosition();
                int lastPosition = linearLayoutManager.findLastVisibleItemPosition();
                for (int i = firstPosition; i <= lastPosition; i++) {//将可见部分的序列号改变 +1
                    MyAdapter.MyViewHolder holder = (MyAdapter.MyViewHolder) mRecyclerView.findViewHolderForAdapterPosition(i);
                    if (holder != null) {
                        holder.textViewNumber.setText(String.valueOf(i+1));
                    }
                }
            }
        });
        mMyAdapter1 = new MyAdapter(false, mWordViewModel);
        mMyAdapter2 = new MyAdapter(true, mWordViewModel);

        SharedPreferences shp = requireActivity().getSharedPreferences(VIEW_TYPE_SHP,Context.MODE_PRIVATE);
        boolean viewType = shp.getBoolean(IS_USING_CARD_vIEW,false);

        mDividerItemDecoration = new DividerItemDecoration(requireActivity(),DividerItemDecoration.VERTICAL);

        if (viewType){//卡片
            mRecyclerView.setAdapter(mMyAdapter2);
        }else {
            mRecyclerView.setAdapter(mMyAdapter1);
            mRecyclerView.addItemDecoration(mDividerItemDecoration);
        }
        filteredWords = mWordViewModel.getAllWordLive();
        //这里的observe里面需要传进一个LifecycleOwner，但是我们这里传进一个Activity，
        //因为整个程序中，Activity一直是存在的，所以换一个Fragment对于View的LifecycleOwner
        //这样每次fragment被销毁后，回来时，这个LifecycleOwner都是新创建的，所以每次都是一个不同的LifecycleOwner
        mWordViewModel.getAllWordLive().observe(getViewLifecycleOwner(), new Observer<List<Word>>() {
            @Override
            public void onChanged(List<Word> words) {
                int temp = mMyAdapter1.getItemCount();
                mWords = words;//刷新数据后，在此fragment中保存备用
                if (temp != words.size()){
                    if (temp < words.size() && !undoAction){
                        //因为数据太多后，只是刷新序列号，而不是给用户一个好的视觉反馈，所以就让列表向下滚动200
                        //但是这里不是滚到到第一条，这里如果有需求需要自己去处理一下
                        mRecyclerView.smoothScrollBy(0,-200);
                    }
                    mMyAdapter1.submitList(words);
                    mMyAdapter2.submitList(words);
                    //通知第0个位置插入了一个内容，并且动画比较平滑
//                    mMyAdapter1.notifyItemInserted(0);
//                    mMyAdapter2.notifyItemInserted(0);
                    //每次都刷新全部数据的话，开销太大
//                    mMyAdapter1.notifyDataSetChanged();
//                    mMyAdapter2.notifyDataSetChanged();
                }
            }
        });

        //实现滑动删除的功能
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.START|ItemTouchHelper.END) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                //这个滑动的效果不是很理想，所以需要自己先调一下
//                Word wordFrom = mWords.get(viewHolder.getAdapterPosition());
//                Word wordTo = mWords.get(target.getAdapterPosition());
//                int idTemp = wordFrom.getId();
//                wordFrom.setId(wordTo.getId());
//                wordTo.setId(idTemp);
//                mWordViewModel.updateWords(wordFrom,wordTo);
//                mMyAdapter1.notifyItemMoved(viewHolder.getAdapterPosition(),target.getAdapterPosition());
//                mMyAdapter2.notifyItemMoved(viewHolder.getAdapterPosition(),target.getAdapterPosition());
                return false;
            }



            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                //因为LiveData是一个异步获取数据，所以不确定在这里操作的时候，LiveData已经完成刷新，可能造成空指针异常
                final Word wordToDelete = mWords.get(viewHolder.getAdapterPosition());
                mWordViewModel.deleteWords(wordToDelete);
                //需要配合CoordinatorLayout使用，不然Snackbar弹起的时候会挡住上面的空间
                Snackbar.make(requireActivity().findViewById(R.id.wordsFragmentView),"删除了一个词汇",Snackbar.LENGTH_SHORT)
                        .setAction("撤销", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                undoAction = true;
                                mWordViewModel.insertWords(wordToDelete);
                            }
                        })
                        .show();
            }

            Drawable icon = ContextCompat.getDrawable(requireActivity(),R.drawable.ic_delete_forever_black_24dp);
            Drawable background = new ColorDrawable(Color.LTGRAY);
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                View itemView = viewHolder.itemView;
                int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;

                int iconLeft,iconRight,iconTop,iconBottom;
                int backTop,backBottom,backLeft,backRight;
                backTop = itemView.getTop();
                backBottom = itemView.getBottom();
                iconTop = itemView.getTop() + iconMargin;
                iconBottom = iconTop + icon.getIntrinsicHeight();
                if (dX > 0){
                    backLeft = itemView.getLeft();
                    backRight = backLeft + (int)dX;
                    background.setBounds(backLeft,backTop,backRight,backBottom);
                    iconLeft = itemView.getLeft() + iconMargin;
                    iconRight = iconLeft + icon.getIntrinsicWidth();
                    icon.setBounds(iconLeft,iconTop,iconRight,iconBottom);
                }else if (dX < 0){
                    backRight = itemView.getRight();
                    backLeft = backRight + (int)dX;
                    background.setBounds(backLeft,backTop,backRight,backBottom);
                    iconRight = itemView.getRight() - iconMargin;
                    iconLeft = iconRight - icon.getIntrinsicWidth();
                    icon.setBounds(iconLeft,iconTop,iconRight,iconBottom);
                }else{
                    background.setBounds(0,0,0,0);
                    icon.setBounds(0,0,0,0);
                }
                background.draw(c);
                icon.draw(c);
            }

        }).attachToRecyclerView(mRecyclerView);

        FloatingActionButton floatingActionButton = requireActivity().findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_wordsFragment_to_addFragment);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.clearData:
                new AlertDialog.Builder(requireActivity())
                        .setTitle("清空数据")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mWordViewModel.deleteAllWords();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create()
                        .show();
                break;
            case R.id.switchViewType:
                SharedPreferences shp = requireActivity().getSharedPreferences(VIEW_TYPE_SHP,Context.MODE_PRIVATE);
                boolean viewType = shp.getBoolean(IS_USING_CARD_vIEW,false);
                SharedPreferences.Editor editor = shp.edit();
                if (viewType){
                    mRecyclerView.setAdapter(mMyAdapter1);
                    editor.putBoolean(IS_USING_CARD_vIEW,false);
                    mRecyclerView.addItemDecoration(mDividerItemDecoration);//添加划分线
                }else {
                    mRecyclerView.setAdapter(mMyAdapter2);
                    editor.putBoolean(IS_USING_CARD_vIEW,true);
                    mRecyclerView.removeItemDecoration(mDividerItemDecoration);//移除
                }
                editor.apply();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu,menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setMaxWidth(1000);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String patten = newText.trim();
                filteredWords = mWordViewModel.findWordWithPatten(patten);
                //由于之前已经设置过监察了，这里应该先移除之前的观察，再添加新的观察
                //这里就是造成每次点击切换按钮之后的重影的问题
                filteredWords.removeObservers(getViewLifecycleOwner());
                filteredWords = mWordViewModel.findWordWithPatten(patten);
                //将参数LifecycleOwner从getActivity换成到getViewLifecycleOwner的原因参照上面
                filteredWords.observe(getViewLifecycleOwner(), new Observer<List<Word>>() {
                    @Override
                    public void onChanged(List<Word> words) {
                        int temp = mMyAdapter1.getItemCount();
                        mWords = words;//刷新数据后，在此fragment中保存备用
                        if (temp != words.size()){
                            if (temp < words.size() && !undoAction){
                                //因为数据太多后，只是刷新序列号，而不是给用户一个好的视觉反馈，所以就让列表向下滚动200
                                //但是这里不是滚到到第一条，这里如果有需求需要自己去处理一下
                                mRecyclerView.smoothScrollBy(0,-200);
                            }
                            mMyAdapter1.submitList(words);
                            mMyAdapter2.submitList(words);
                        }
                    }
                });
                return true;
            }
        });
    }
}
