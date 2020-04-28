package com.image_upload;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Images extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private ImageAdapter mAdapter;

    private ProgressBar mProgressCircle;

    private DatabaseReference mDatabaseRef;
    private List<Upload> mUploads;
    private String lastSnapshot;
    //private boolean isScrolling = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);

        Button loadMore = findViewById(R.id.loadMore);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mProgressCircle = findViewById(R.id.progress_circle);

        mUploads = new ArrayList<>();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        loadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadImages();
            }
        });
        loadImages();
    }

    private void loadImages() {
        Query query;
        if(lastSnapshot == null) {
            query = mDatabaseRef.orderByKey().limitToFirst(3);
        } else {
            query = mDatabaseRef.orderByKey().startAt(lastSnapshot).limitToFirst(3);
        }
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    if (lastSnapshot == null) {
                        mUploads.add(postSnapshot.getValue(Upload.class));
                    } else if(!lastSnapshot.equals(postSnapshot.getKey())) {
                        mUploads.add(postSnapshot.getValue(Upload.class));
                    }
                    lastSnapshot = postSnapshot.getKey();
                }
                mAdapter = new ImageAdapter(Images.this, mUploads);
                mRecyclerView.setAdapter(mAdapter);
                mProgressCircle.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Images.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                mProgressCircle.setVisibility(View.INVISIBLE);
            }
        });
    }
}
