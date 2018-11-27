package kuhiep.resty.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;

import io.supercharge.shimmerlayout.ShimmerLayout;
import kuhiep.resty.Adapter.ReservationListAdapter;

import kuhiep.resty.Model.Reservation;
import kuhiep.resty.R;
import kuhiep.resty.RecyclerItemClickListener;
import kuhiep.resty.Utility.Config;
import kuhiep.resty.Utility.InternetTime;

public class ReservationsActivity extends AppCompatActivity {

    private RecyclerView activeBookingsRecyclerView, recentBookingsRecyclerView;
    private ReservationListAdapter activeReservationAdapter, recentReservationAdapter;
    private SharedPreferences sharedPreferences;
    private ArrayList<Reservation> activeReservationList, recentReservationList;
    private String hash;

    // Shimmer and Error
    private ShimmerLayout shimmerLayout;
    private LinearLayout errorLayout;
    private ImageView errorImage;
    private TextView errorMessageText;
    private CardView errorTryAgainButton;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservations);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("My Reservations");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferences = getSharedPreferences(Config.RESERVATION_SP, MODE_PRIVATE);
        hash = sharedPreferences.getString(Config.RESERVATION_HASH_ID, null);

        // Shimmer and Error
        shimmerLayout = findViewById(R.id.shimmer_layout_reservation_act);
        shimmerLayout.startShimmerAnimation();
        shimmerLayout.setVisibility(View.VISIBLE);
        errorLayout = findViewById(R.id.error_container);
        errorImage = findViewById(R.id.error_imageview);
        errorMessageText = findViewById(R.id.error_textview);
        errorTryAgainButton = findViewById(R.id.error_try_again_button);
        errorLayout.setVisibility(View.GONE);
        errorTryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadReservations(hash);
            }
        });

        activeBookingsRecyclerView = findViewById(R.id.active_res_recyclerview);
        activeBookingsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        activeBookingsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        activeBookingsRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(ReservationsActivity.this, BookingDetail.class);
                intent.putExtra(Config.RESERVATION_INFO_ITEM, activeReservationList.get(position));
                intent.putExtra(Config.RES_INFO_BACK_INTENT, 1);
                startActivity(intent);
            }
        }));

        recentBookingsRecyclerView = findViewById(R.id.recent_res_recyclerview);
        recentBookingsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recentBookingsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        recentBookingsRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(ReservationsActivity.this, BookingDetail.class);
                intent.putExtra(Config.RESERVATION_INFO_ITEM, recentReservationList.get(position));
                intent.putExtra(Config.RES_INFO_BACK_INTENT, 1);
                startActivity(intent);
            }
        }));

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                shimmerLayout.setVisibility(View.VISIBLE);
                shimmerLayout.startShimmerAnimation();
                errorLayout.setVisibility(View.GONE);
                (findViewById(R.id.recent_res_section)).setVisibility(View.GONE);
                (findViewById(R.id.active_res_section)).setVisibility(View.GONE);
                loadReservations(hash);
            }
        });

        /* Api Request */
        // TODO : Handle if userId == null
        loadReservations(hash);

    }

    public void loadReservations(String hash) {
        // Adapter
        activeReservationList = new ArrayList<>();
        recentReservationList = new ArrayList<>();
        activeReservationAdapter = new ReservationListAdapter(activeReservationList);
        recentReservationAdapter = new ReservationListAdapter(recentReservationList);
        activeBookingsRecyclerView.setAdapter(activeReservationAdapter);
        recentBookingsRecyclerView.setAdapter(recentReservationAdapter);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("reservation")
                .whereEqualTo("hash", hash)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().size() != 0) {
                                String dateToFormat = InternetTime.getTimeFormated();
                                InternetTime internetTime = new InternetTime(dateToFormat);
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(internetTime.getDate());
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Reservation currentReservation = document.toObject(Reservation.class);
                                    if (calendar.getTimeInMillis() > currentReservation.getBookingDate()) {
                                        recentReservationList.add(currentReservation);
                                        (findViewById(R.id.recent_res_section)).setVisibility(View.VISIBLE);
                                    } else {
                                        activeReservationList.add(0, currentReservation);
                                        (findViewById(R.id.active_res_section)).setVisibility(View.VISIBLE);
                                    }
                                    Log.d("FirebaseReservation", document.getId() + " => " + document.getData());
                                }
                                recentReservationAdapter.notifyDataSetChanged();
                                activeReservationAdapter.notifyDataSetChanged();
                                shimmerLayout.stopShimmerAnimation();
                                shimmerLayout.setVisibility(View.GONE);
                                swipeRefreshLayout.setRefreshing(false);
                            } else {
                                shimmerLayout.stopShimmerAnimation();
                                shimmerLayout.setVisibility(View.GONE);
                                errorLayout.setVisibility(View.VISIBLE);
                                errorImage.setImageResource(R.drawable.oh_snap);
                                errorMessageText.setText("You haven't made any previous reservations!");
                                errorTryAgainButton.setVisibility(View.GONE);
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        } else {
                            Log.d("FirebaseReservation", "Error getting documents: ", task.getException());
                        }
                    }
                });


            // End
//        Call<ReservationResponse> call = apiService.getReservationForHash(hash);
//        call.enqueue(new Callback<ReservationResponse>() {
//            @Override
//            public void onResponse(Call<ReservationResponse> call, Response<ReservationResponse> response) {
//                if (response.isSuccessful()) {
//                    if (response.body().getReservationList().size() != 0) {
//                        ArrayList<Reservation> allReservationsList = response.body().getReservationList();
//                        // Internet Time
//                        InternetTime internetTime = new InternetTime(response.body().getTime());
//                        Calendar calendar = Calendar.getInstance();
//                        calendar.setTime(internetTime.getDate());
//                        // Classifying as Active/Passive
//                        for (int i = 0; i < allReservationsList.size(); i++) {
//                            Reservation currentReservation = allReservationsList.get(i);
//                            if (calendar.getTimeInMillis() > currentReservation.getBookingDate()) {
//                                recentReservationList.add(currentReservation);
//                                (findViewById(R.id.recent_res_section)).setVisibility(View.VISIBLE);
//                            } else {
//                                activeReservationList.add(0, currentReservation);
//                                (findViewById(R.id.active_res_section)).setVisibility(View.VISIBLE);
//                            }
//                        }
//                        recentReservationAdapter.notifyDataSetChanged();
//                        activeReservationAdapter.notifyDataSetChanged();
//                        shimmerLayout.stopShimmerAnimation();
//                        shimmerLayout.setVisibility(View.GONE);
//                    } else {
//                        shimmerLayout.stopShimmerAnimation();
//                        shimmerLayout.setVisibility(View.GONE);
//                        errorLayout.setVisibility(View.VISIBLE);
//                        errorImage.setImageResource(R.drawable.oh_snap);
//                        errorMessageText.setText("You haven't made any previous reservations!");
//                        errorTryAgainButton.setVisibility(View.GONE);
//                    }
//                } else {
//                    shimmerLayout.stopShimmerAnimation();
//                    shimmerLayout.setVisibility(View.GONE);
//                    errorLayout.setVisibility(View.VISIBLE);
//                    errorImage.setImageResource(R.drawable.oh_snap);
//                    errorMessageText.setText("Oops! There seems to be a technical error");
//                }
//                swipeRefreshLayout.setRefreshing(false);
//            }
//
//            @Override
//            public void onFailure(Call<ReservationResponse> call, Throwable t) {
//                shimmerLayout.stopShimmerAnimation();
//                shimmerLayout.setVisibility(View.GONE);
//                errorLayout.setVisibility(View.VISIBLE);
//                errorImage.setImageResource(R.drawable.offline_error);
//                errorMessageText.setText("Oops! Some error trying to connect to our servers!");
//                swipeRefreshLayout.setRefreshing(false);
//            }
//        });
        }
    }
