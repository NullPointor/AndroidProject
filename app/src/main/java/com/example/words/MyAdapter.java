package com.example.words;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class MyAdapter extends ListAdapter<Word,MyAdapter.MyViewHolder> {
    //继承自ListAdapter之后，就不再需要传进来List了，而是直接再外面提交List
    private boolean useCarView;
    private WordViewModel wordViewModel;

    MyAdapter(boolean useCarView,WordViewModel wordViewModel) {
        super(new DiffUtil.ItemCallback<Word>() {
            @Override
            public boolean areItemsTheSame(@NonNull Word oldItem, @NonNull Word newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull Word oldItem, @NonNull Word newItem) {
                return (oldItem.getWord().equals(newItem.getWord())
                        && oldItem.getChineseMeaning().equals(newItem.getChineseMeaning())
                        && oldItem.getChineseInvisible() == newItem.getChineseInvisible());
            }
        });
        this.useCarView = useCarView;
        this.wordViewModel = wordViewModel;
    }

    @Override
    public void onViewAttachedToWindow(@NonNull MyViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        //当数据从后台出现在屏幕上的时候，也要对数据进行一次刷新，因为外面已经刷新过一次，所以这里相当于是双保险
        holder.textViewNumber.setText(String.valueOf(holder.getAdapterPosition() + 1));
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView;
        if (useCarView){
            itemView = layoutInflater.inflate(R.layout.cell_card_2,parent,false);
        }else{
            itemView = layoutInflater.inflate(R.layout.cell_normal_2,parent,false);
        }

        final MyViewHolder holder = new MyViewHolder(itemView);
        holder.itemView.setOnClickListener(new View.OnClickListener() {//设置链接，点击自动查询单词
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("http://m.youdao.com/dict?le=eng&q="+holder.textViewEnglish.getText());
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(uri);
                holder.itemView.getContext().startActivity(intent);
            }
        });
        holder.mSwitchChineseVisible.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Word word = (Word) holder.itemView.getTag(R.id.word_for_view_holder);
                if (isChecked){//将textView进行设置，同时改变switch的值(但这只是视图上面的改变),我们还要对数据库进行改变
                    holder.textViewChinese.setVisibility(View.GONE);
                    word.setChineseInvisible(true);
                    wordViewModel.updateWords(word);//对数据库进行更新
                }else{
                    holder.textViewChinese.setVisibility(View.VISIBLE);
                    word.setChineseInvisible(false);
                    wordViewModel.updateWords(word);
                }
            }
        });
        return holder;
    }

    //position是当前位置
    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        final Word word = getItem(position);
        holder.itemView.setTag(R.id.word_for_view_holder, word);
        holder.textViewEnglish.setText(word.getWord());
        holder.textViewChinese.setText(word.getChineseMeaning());
        holder.textViewNumber.setText(String.valueOf(position+1));
        //如果不添加这个，会造成，滚出屏幕的item的switch状态改变，如从选中到未选中
//        holder.mSwitchChineseVisible.setOnCheckedChangeListener(null);
        if (word.getChineseInvisible()){
            holder.textViewChinese.setVisibility(View.GONE);//如果是Invisible，就没有单词填充父布局时的动画
            holder.mSwitchChineseVisible.setChecked(true);
        }else{
            holder.textViewChinese.setVisibility(View.VISIBLE);
            holder.mSwitchChineseVisible.setChecked(false);
        }

        //因为每次滑动的时候，回收后的View都要调用这个方法，所以会造成设置监听的事件反复调用，故应该把他放到onCreateViewHolder
        //去，这样只需要设置一次即可，性能得到优化
//        holder.itemView.setOnClickListener(new View.OnClickListener() {//设置链接，点击自动查询单词
//            @Override
//            public void onClick(View view) {
//                Uri uri = Uri.parse("http://m.youdao.com/dict?le=eng&q="+holder.textViewEnglish.getText());
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setData(uri);
//                holder.itemView.getContext().startActivity(intent);
//            }
//        });
//        holder.mSwitchChineseVisible.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
//                if (isChecked){//将textView进行设置，同时改变switch的值(但这只是视图上面的改变),我们还要对数据库进行改变
//                    holder.textViewChinese.setVisibility(View.GONE);
//                    word.setChineseInvisible(true);
//                    wordViewModel.updateWords(word);//对数据库进行更新
//                }else{
//                    holder.textViewChinese.setVisibility(View.VISIBLE);
//                    word.setChineseInvisible(false);
//                    wordViewModel.updateWords(word);
//                }
//            }
//        });
    }

    //这里的static是放置内存泄漏
    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNumber;
        TextView textViewEnglish;
        TextView textViewChinese;
        Switch mSwitchChineseVisible;
        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNumber = itemView.findViewById(R.id.textViewNumber);
            textViewEnglish = itemView.findViewById(R.id.textViewEnglish);
            textViewChinese = itemView.findViewById(R.id.textViewChinese);
            mSwitchChineseVisible = itemView.findViewById(R.id.switchChineseInvisible);
        }
    }
}
