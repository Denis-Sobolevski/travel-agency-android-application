package com.example.tourexpert;

import android.app.Activity;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * adapter class for the FireBaseRecyclerAdapter
 * which is responsible to display a list of Hotel purchase history Objects on a RecyclerView
 */
public class HotelPurchaseAdapter extends FirebaseRecyclerAdapter<HotelPurchase, HotelPurchaseAdapter.myViewHolder> {

    DateTimeFormatter formatter;
    Activity parent_activity;
    AppGeneralActivities appGeneralActivities;
    CloudActivities cloudActivities;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public HotelPurchaseAdapter(@NonNull FirebaseRecyclerOptions<HotelPurchase> options) {
        super(options);

        formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'at' hh:mm a");
        appGeneralActivities = new AppGeneralActivities();
        cloudActivities = new CloudActivities();
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull HotelPurchase model) {
        LocalDateTime dateOfPurchase = LocalDateTime.ofInstant(Instant.ofEpochMilli(model.getDateOfPurchase()), TimeZone.getDefault().toZoneId());

        holder.dateOfPurchase.setText(dateOfPurchase.format(formatter));
        holder.key.setText(model.getKey());
        holder.amount.setText(model.getAmount() + "");
        holder.price.setText(model.getPrice() + "$");

        holder.hotelName.setText(model.getHotelName());
        holder.type.setText(model.getType());
        holder.arrivalDate.setText(model.getArrivalDate());
        holder.departureDate.setText(model.getDepartureDate());

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appGeneralActivities.click(parent_activity);
                if (holder.more_information.getVisibility() == View.GONE) {
                    holder.more_information.setVisibility(View.VISIBLE);
                } else {
                    holder.more_information.setVisibility(View.GONE);
                }
            }
        });

        holder.btn_create_receipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appGeneralActivities.click(parent_activity);
                try {
                    PDFMaker pdfMaker = new PDFMaker();
                    if (pdfMaker.createPDFreceipt(parent_activity, model)) {
                        // the copy was successfully made - notify the user:
                        appGeneralActivities.displayAlertDialog(parent_activity, "receipt copy was made", "the requested receipt copy was made, please " +
                                "check your:\nInternal storage/Documents directory to watch it", R.drawable.information);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hotel_purchase_card_view, parent, false);
        // get access to the parent activity of the recyclerView:
        this.parent_activity = SavedActivity.getActivity();
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder {

        protected View cardView;
        protected TextView dateOfPurchase,
                key, amount, price, hotelName, type, arrivalDate, departureDate;

        protected Button btn_create_receipt;
        protected LinearLayout more_information;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            dateOfPurchase = itemView.findViewById(R.id.dateOfPurchase);
            key = itemView.findViewById(R.id.key);
            amount = itemView.findViewById(R.id.amount);
            price = itemView.findViewById(R.id.price);

            hotelName = itemView.findViewById(R.id.hotelName);
            type = itemView.findViewById(R.id.type);
            arrivalDate = itemView.findViewById(R.id.arrivalDate);
            departureDate = itemView.findViewById(R.id.departureDate);

            cardView = itemView.findViewById(R.id.card_view);
            btn_create_receipt = itemView.findViewById(R.id.btn_create_receipt);

            more_information = itemView.findViewById(R.id.more_information);
        }
    }

}
