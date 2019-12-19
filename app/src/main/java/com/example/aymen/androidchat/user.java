package com.example.aymen.androidchat;

import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class user extends AppCompatActivity {
    public RecyclerView myRecylerView ;
    Context context;
    public List<Message> MessageList ;
    public ChatBoxAdapter chatBoxAdapter;
    public  EditText messagetxt ;
    public  Button send ;
    //declare socket object
    private Socket socket;
    public static final String NICKNAME1 = "usernickname";
    public static final String NICKNAME2 = "usernickname";
    public String Nickname ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        messagetxt = (EditText) findViewById(R.id.message) ;
        send = (Button)findViewById(R.id.send);
        // get the nickame of the user

        Intent i= getIntent();
        i.getStringExtra("NICKNAME");

        Nickname= (String)getIntent().getExtras().getString(MainActivity.NICKNAME);
        //connect you socket client to the server
        try {
            socket = IO.socket("http://192.168.0.122:3000");
            socket.connect();
            socket.emit("join", Nickname);
        } catch (URISyntaxException e) {
            e.printStackTrace();

        }
        //setting up recyler
        MessageList = new ArrayList<>();
        myRecylerView = (RecyclerView) findViewById(R.id.messagelist);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        myRecylerView.setLayoutManager(mLayoutManager);
        myRecylerView.setItemAnimator(new DefaultItemAnimator());

        myRecylerView.addOnItemTouchListener(
                new RecyclerItemClickListener(context, myRecylerView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        String title1 = ((TextView) myRecylerView.findViewHolderForAdapterPosition(position).itemView.findViewById(R.id.message)).getText().toString();
                       // Toast.makeText(getApplicationContext(), title1, Toast.LENGTH_SHORT).show();
                        Intent i  = new Intent(user.this,ChatBoxActivity.class);
                        //retreive nickname from textview and add it to intent extra
                        i.putExtra("NICKNAME1", Nickname);
                        i.putExtra("NICKNAME2", title1);
                        //  i.putExtra(NICKNAME1,Nickname);
                        // i.putExtra(NICKNAME2,messagetxt.getText().toString());
                        startActivity(i);



                        //   Toast.makeText(user.this, "asdf", Toast.LENGTH_SHORT).show();

                    }

                    @Override public void onLongItemClick(View view, int position) {
                      //  Toast.makeText(user.this, "asdf", Toast.LENGTH_SHORT).show();
                    }
                })
        );



        // message send action
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //retrieve the nickname and the message content and fire the event messagedetection
                Toast.makeText(user.this,"button clicked ", Toast.LENGTH_SHORT).show();



                    socket.emit("pingeachuser");


            }
        });



        //implementing socket listeners
        socket.on("userjoinedthechat", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        try {
                            //extract data from fired event
                            String nickname = data.getString("name");
                            String message = data.getString("from");
                            String list = data.getString("list");
                            Message m = new Message(nickname,message);


                            //add the message to the messageList

                            MessageList.add(m);

                            // add the new updated list to the dapter
                            chatBoxAdapter = new ChatBoxAdapter(MessageList);

                            // notify the adapter to update the recycler view

                            chatBoxAdapter.notifyDataSetChanged();

                            //set the adapter for the recycler view

                            myRecylerView.setAdapter(chatBoxAdapter);
                            String newlist[] = list.split(",");



                            Toast.makeText(user.this,"User Joined the chat : "+ nickname, Toast.LENGTH_SHORT).show();
                         //   Toast.makeText(user.this,"User list : "+ list[(1)%(list.length())], Toast.LENGTH_LONG).show();

                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        socket.on("userdisconnect", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String data = (String) args[0];
                        Toast.makeText(user.this,data,Toast.LENGTH_LONG).show();

                    }
                });
            }
        });
        socket.on("singleuser", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        try {
                            //extract data from fired event

                            String nickname = data.getString("senderNickname");
                            String message = data.getString("name");

                            // make instance of message

                            Message m = new Message(nickname,message);


                            //add the message to the messageList

                            MessageList.add(m);

                            // add the new updated list to the dapter
                            chatBoxAdapter = new ChatBoxAdapter(MessageList);

                            // notify the adapter to update the recycler view

                            chatBoxAdapter.notifyDataSetChanged();

                            //set the adapter for the recycler view

                            myRecylerView.setAdapter(chatBoxAdapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                });
            }
        });
        socket.on("messageping", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String data = (String) args[0];
                        Toast.makeText(user.this,data,Toast.LENGTH_LONG).show();

                    }
                });
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        socket.disconnect();
    }
}
