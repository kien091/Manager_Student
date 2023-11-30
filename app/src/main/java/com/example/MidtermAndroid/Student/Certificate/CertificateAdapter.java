package com.example.MidtermAndroid.Student.Certificate;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.MidtermAndroid.LoginActivity;
import com.example.MidtermAndroid.R;

import java.util.ArrayList;

public class CertificateAdapter extends RecyclerView.Adapter<CertificateAdapter.ViewHolder>{
    private final CertificateActivity context;
    private final ArrayList<Certificate> certificates;
    private Certificate certificate;

    public CertificateAdapter(CertificateActivity context, ArrayList<Certificate> certificates) {
        this.context = context;
        this.certificates = certificates;
    }

    public Certificate getCertificate() {
        return certificate;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.certificate_item, parent, false);

        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Certificate certificate = certificates.get(position);

        TextView tv_name = holder.tv_name;
        tv_name.setText(certificate.getName());

        TextView tv_issuer = holder.tv_issuer;
        tv_issuer.setText("Người cấp: " + certificate.getIssuer());

        TextView tv_date = holder.tv_date;
        tv_date.setText("Ngày: " + certificate.getDate());

        if(LoginActivity.getRole().equals("admin")
                || LoginActivity.getRole().equals("manager")){
            holder.itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                MenuInflater inflater = new MenuInflater(context);
                inflater.inflate(R.menu.student_context_menu, menu);

                menu.removeItem(R.id.i_detail);
                menu.removeItem(R.id.i_certificate);
                menu.removeItem(R.id.i_history);
                this.certificate = certificate;
            });
        }
    }

    @Override
    public int getItemCount() {
        return certificates.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tv_name, tv_issuer, tv_date;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_name = itemView.findViewById(R.id.tv_name);
            tv_issuer = itemView.findViewById(R.id.tv_issuer);
            tv_date = itemView.findViewById(R.id.tv_date);
        }
    }
}
