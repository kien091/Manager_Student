package com.example.MidtermAndroid.User;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.MidtermAndroid.LoginActivity;
import com.example.MidtermAndroid.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{
    private final Context context;
    private final ArrayList<User> users;
    private User user;

    public UserAdapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users = users;
    }

    public User getUser() {
        return user;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.user_item, parent, false);

        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);

        ImageView img_user_avatar = holder.img_user_avatar;
        String imagePath = user.getAvatar();
        if(imagePath.equals("")){
            img_user_avatar.setImageResource(R.drawable.baseline_person_24);
        }else{
            Uri uri = Uri.parse(imagePath);
            img_user_avatar.setImageURI(uri);
        }

        TextView tv_user_name = holder.tv_user_name;
        tv_user_name.setText(user.getName());

        try {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Calendar dob = Calendar.getInstance();
            dob.setTime(Objects.requireNonNull(sdf.parse(user.getDob())));
            Calendar today = Calendar.getInstance();

            int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            TextView tv_user_age = holder.tv_user_age;
            tv_user_age.setText("Age: " + age);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        TextView tv_user_phone = holder.tv_user_phone;
        tv_user_phone.setText(user.getPhone());

        TextView tv_user_status = holder.tv_user_status;
        tv_user_status.setText("Status: " + user.getStatus());

        if(LoginActivity.getRole().equals("admin")){
            holder.itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                MenuInflater inflater = new MenuInflater(context);
                inflater.inflate(R.menu.student_context_menu, menu);

                menu.removeItem(R.id.i_certificate);
                this.user = user;
            });
        } else if (LoginActivity.getRole().equals("employee")
                || LoginActivity.getRole().equals("manager")) {
            holder.itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                MenuInflater inflater = new MenuInflater(context);
                inflater.inflate(R.menu.student_context_menu, menu);

                menu.removeItem(R.id.i_certificate);
                menu.removeItem(R.id.i_edit);
                menu.removeItem(R.id.i_delete);
                this.user = user;
            });
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tv_user_name, tv_user_age, tv_user_phone, tv_user_status;
        ImageView img_user_avatar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_user_name = itemView.findViewById(R.id.tv_user_name);
            tv_user_age = itemView.findViewById(R.id.tv_user_age);
            tv_user_phone = itemView.findViewById(R.id.tv_user_phone);
            tv_user_status = itemView.findViewById(R.id.tv_user_status);
            img_user_avatar = itemView.findViewById(R.id.img_user_avatar);
        }
    }
}
