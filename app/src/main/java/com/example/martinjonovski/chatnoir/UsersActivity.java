package com.example.martinjonovski.chatnoir;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mUsersList;
    private DatabaseReference mUsersDatabase;
    private String mCurrentUserId;
    private UsersAdapter mAdapter;
    private int pos;

    private EditText searchText;
    private ImageButton searchButton;
    private List<Users> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        mCurrentUserId = getIntent().getStringExtra("user_id");
        mToolbar = (Toolbar) findViewById(R.id.users_appbar);
        mUsersList = (RecyclerView) findViewById(R.id.users_list);
        userList = new ArrayList<>();
        searchText = (EditText) findViewById(R.id.search_bar_users);
        searchButton = (ImageButton) findViewById(R.id.chat_send_btn_ua);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        FirebaseDatabase.getInstance().getReference().child("Users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> list = dataSnapshot.getChildren();
                        // Filter User

                        for (DataSnapshot dataSnapshot1 : list) {
                            if (!dataSnapshot1.getKey().equals(mCurrentUserId)) {
                                Users userToAdd = dataSnapshot1.getValue(Users.class);
                                if (userToAdd.getName() != null) {
                                    userToAdd.setUid(dataSnapshot1.getKey().toString());
                                    userList.add(userToAdd);
                                }
                            }
                        }

                        // Setting data
                        //    mBaseRecyclerAdapter.setItems(userList);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        mAdapter = new UsersAdapter(userList, getApplicationContext(), true);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        mUsersList.setAdapter(mAdapter);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (searchText.getText().toString() != null && !searchText.getText().toString().isEmpty()) {
                    String userToSearch = searchText.getText().toString();
                    updateUsersList(userToSearch);
                } else {
                    mAdapter = new UsersAdapter(userList, getApplicationContext(), true);
                    mUsersList.setAdapter(mAdapter);
                }

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        });

    }

    private void updateUsersList(String userToSearch) {
        userToSearch = userToSearch.toLowerCase();
        List<Users> result = new ArrayList<>();
        for (Users user : userList) {
            if (user.getName().toLowerCase().contains(userToSearch)) {
                result.add(user);
            }
        }
        mAdapter = new UsersAdapter(result, getApplicationContext(), true);
        mUsersList.setAdapter(mAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();

        mUsersList.setAdapter(mAdapter);
        // mUsersList.getAdapter().notifyItemRemoved(pos);

    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name) {
            TextView mDisplayName = (TextView) mView.findViewById(R.id.user_single_name);
            mDisplayName.setText(name);
        }

        public void setStatus(String status) {
            TextView mDisplayStatus = (TextView) mView.findViewById(R.id.user_single_status);
            mDisplayStatus.setText(status);
        }

        public void setOnlineStatus(boolean bool) {
            ImageView imageView = (ImageView) mView.findViewById(R.id.single_online_img);
            Bitmap icon = BitmapFactory.decodeResource(mView.getContext().getResources(),
                    R.drawable.online_bg);

            if (bool) {

                imageView.setImageBitmap(icon);

            } else {
                Bitmap iconOff = BitmapFactory.decodeResource(mView.getContext().getResources(),
                        R.drawable.offline_icon);
                imageView.setImageBitmap(iconOff);

            }
        }

        public void setThumb(String thumb, Context context) {
            CircleImageView circleImage = (CircleImageView) mView.findViewById(R.id.user_single_image);
            Picasso.with(context).load(thumb).placeholder(R.drawable.photo).into(circleImage);

        }

    }
}
