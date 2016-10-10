package com.balinasoft.clever.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.balinasoft.clever.R;
import com.balinasoft.clever.model.Player;
import com.balinasoft.clever.model.Room;
import com.balinasoft.clever.ui.activities.RoomActivity_;
import com.balinasoft.clever.ui.adapters.RoomAdapter;
import com.balinasoft.clever.util.ConstantsManager;
import com.balinasoft.clever.util.RoomsClickListener;
import com.balinasoft.clever.util.SocketErrorListener;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.socket.client.Socket;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

import static com.balinasoft.clever.GameApplication.getAuthToken;
import static com.balinasoft.clever.GameApplication.getOnlineCoins;
import static com.balinasoft.clever.GameApplication.getOnlineName;
import static com.balinasoft.clever.GameApplication.getSocket;
import static com.balinasoft.clever.GameApplication.getUserId;
import static com.balinasoft.clever.GameApplication.getUserImage;

@EFragment(R.layout.fragment_online_config)
public class OnlineConfigFragment extends ConfigFragmentBase implements RoomsClickListener {

    @ViewById(R.id.rooms_list)
    RecyclerView mRoomsList;

    @ViewById(R.id.empty_view)
    FrameLayout mEmptyView;

    @ViewById(R.id.swipe)
    SwipeRefreshLayout mSwipe;

    @StringRes(R.string.socket_connect_error)
    String mSocketConnectErrorMessage;

    @StringRes(R.string.room_creation_error)
    String mErrorCreatingRoomMessage;

    @StringRes(R.string.room_connection_success)
    String mRoomConnectionSuccessfulMessage;

    @StringRes(R.string.room_creation_success)
    String mRoomCreationSuccessfulMessage;

    @StringRes(R.string.room_connection_fail)
    String mRoomConnectionFailedMessage;

    private SocketErrorListener mSocketErrorListener;

    private Socket mSocket = getSocket();

    private boolean mIsConnected = false;

    private boolean mHasEntered = false;

    private String mIdInRoom = "";

    private Subscription mSubscription;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FragmentActivity activity = getActivity();
        try {
            mSocketErrorListener = (SocketErrorListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SocketErrorListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        establishSocketConnection();
        Timber.e("starting socket connection");
    }

    @Override
    public void onResume() {
        super.onResume();
        mHasEntered = false;
        startSocketListening();
        Timber.e("listening........");
        hideList();
        requestForRooms();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopSocketListening();
        if(mSubscription != null && !mSubscription.isUnsubscribed()) mSubscription.unsubscribe();
        Timber.d("Stopping listening.........");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.e("disabling socket connection");
        disableSocketConnection();
    }

    @Override
    void setUpOtherViews() {
        setUpRooms();
        setUpRefresh();
    }

    @Override
    boolean areEnoughCoins() {
        return getOnlineCoins() >= mBet;
    }

    @Override
    void createGame() {
        createRoom();
    }

    private void setUpRooms() {
        mRoomsList.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void setUpRefresh() {
        mSwipe.setColorSchemeColors(ContextCompat.getColor(getContext(),R.color.colorPrimary));
        mSwipe.setOnRefreshListener(this::updateRooms);
    }

    private void hideSwipe() {
        if(mSwipe != null) mSwipe.setRefreshing(false);
    }

    private void requestForRooms() {
        mSocket.emit(ConstantsManager.ROOMS_LIST_GET_EVENT, "{}");
    }

    private void updateRooms() {
        requestForRooms();
        mSubscription = Observable.interval(ConstantsManager.CONNECTION_TIME_OUT, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterTerminate(this::hideSwipe)
                .subscribe(tick -> {},throwable -> {});
    }

    private void hideList() {
        mRoomsList.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.VISIBLE);
    }

    private void showList() {
        mRoomsList.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.GONE);
    }

    private void establishSocketConnection() {
        mSocket.connect();
    }

    private void disableSocketConnection() {
        mSocket.disconnect();
    }

    private void startSocketListening() {
        mSocket.on(Socket.EVENT_CONNECT, args -> onConnect());//
        mSocket.on(Socket.EVENT_DISCONNECT, args -> onDisconnect());//
        mSocket.on(Socket.EVENT_CONNECT_ERROR, args -> onConnectError());//
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, args -> onConnectError());//
        mSocket.on(ConstantsManager.AUTH_EVENT, this::onAuth);//
        mSocket.on(ConstantsManager.JOIN_ROOM_EVENT, this::onJoinRoom);//
        mSocket.on(ConstantsManager.ROOM_MESSAGE_EVENT, this::onRoomCreated);//
        mSocket.on(ConstantsManager.ROOMS_LIST_GET_EVENT, this::onRoomsListGot);//
        mSocket.on(ConstantsManager.ROOM_INFO_EVENT, this::onRoomInfoReceived);//
    }

    private void stopSocketListening() {
        mSocket.off(Socket.EVENT_CONNECT, args -> onConnect());
        mSocket.off(Socket.EVENT_DISCONNECT, args -> onDisconnect());
        mSocket.off(Socket.EVENT_CONNECT_ERROR, args -> onConnectError());
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, args -> onConnectError());
        mSocket.off(ConstantsManager.AUTH_EVENT, this::onAuth);
        mSocket.off(ConstantsManager.JOIN_ROOM_EVENT, this::onJoinRoom);
        mSocket.off(ConstantsManager.ROOM_MESSAGE_EVENT, this::onRoomCreated);
        mSocket.off(ConstantsManager.ROOMS_LIST_GET_EVENT, this::onRoomsListGot);
        mSocket.off(ConstantsManager.ROOM_INFO_EVENT, this::onRoomInfoReceived);
    }

    @UiThread
    void onConnect() {
        Timber.d("Connecting");
        if (!mIsConnected) {
            auth();
            mIsConnected = true;
        }
    }

    @UiThread
    void onDisconnect() {
        mIsConnected = false;
        Timber.e("Disconnecting");
    }

    @UiThread
    void onConnectError() {
        Timber.e("onConnectError");
        mSocketErrorListener.onSocketError(mSocketConnectErrorMessage);
    }

    @UiThread
    void onRoomsListGot(Object... args) {
        hideSwipe();
        JSONObject response = (JSONObject) args[0];
        Timber.e("onRoomsGot %s", response.toString());
        try {
            JSONArray roomsListJson = response.getJSONArray("rooms");
            List<Room> rooms = getRoomsList(roomsListJson);
            if (rooms != null && rooms.size() > 0 && mRoomsList != null) {
//                if (mRoomAdapter == null) {
//                    mRoomAdapter = new RoomAdapter(rooms, this);
//                    mRoomsList.setAdapter(mRoomAdapter);
//                } else {
//                    mRoomAdapter.refresh(rooms);
//                }
                mRoomsList.setAdapter(new RoomAdapter(rooms, this));
                showList();
            }
        } catch (JSONException ignored) {

        }
    }

    @UiThread
    void onAuth(Object... args) {
        Timber.e("onAuth");
        JSONObject data = (JSONObject) args[0];
        String message = "";
        int success = 0;
        try {
            message = data.getString("message");
            success = data.getInt("success");
        } catch (JSONException ignored) {

        }
        if (success == 0) {
            Timber.e("auth error %s", message);
            //Crashlytics.log(message);
            mSocketErrorListener.onSocketError(mSocketConnectErrorMessage);
            return;
        }
        Timber.e("socket auth successful");

    }

    @UiThread
    void onJoinRoom(Object... args) {
        JSONObject data = (JSONObject) args[0];
        String message = "";
        try {
            message = data.getString("message");
            mIdInRoom = data.getString("idInRoom");
            Timber.d("roomId is %s",mIdInRoom);
        } catch (JSONException ignored) {
        }
        Timber.e("onJoinRoom %s", message);
        Context context = getContext();
        if (context != null) Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    @UiThread
    void onRoomCreated(Object... args) {
        JSONObject data = (JSONObject) args[0];
        int success = 0;
        String message = "";
        try {
            success = data.getInt("success");
            message = data.getString("message");
        } catch (JSONException ignored) {
        }
        Timber.e("onRoomCreated %s", message);
        if (success == 0 && getActivity() != null) {
            notifyUserWith(mErrorCreatingRoomMessage);
        }
    }

    @UiThread
    void onRoomInfoReceived(Object... args) {
        JSONObject roomInfo = (JSONObject) args[0];
        Timber.e("onRoomInfoReceived %s",roomInfo.toString());
        try {
            int bet = roomInfo.getInt("bet");
            int roomNumber = roomInfo.getInt("numRoom");
            if (getOnlineCoins() >= bet) {

                JSONArray jsonPlayers = roomInfo.getJSONArray("players");
                List<Player> players = getPlayersList(jsonPlayers);
                Player[] pls = new Player[players.size()];
                pls = players.toArray(pls);
                moveToRoom(pls,roomNumber);
            }
            else if (getActivity() != null) notifyUserWith(mNotEnoughCoinsMessage);
        } catch (JSONException ignored) {

        }
    }

    @Override
    public void onRoomClicked(int roomNumber,int bet) {
        if(getOnlineCoins() >= bet) joinRoom(roomNumber);
        else if(getActivity() != null) notifyUserWith(mNotEnoughCoinsMessage);
    }

    private void joinRoom(int roomNumber) {
        JSONObject roomInfo = new JSONObject();
        try {
            roomInfo.put("room", roomNumber);
        } catch (JSONException ignored) {

        }
        Timber.d("onRoomClicked %s", roomInfo.toString());
        mSocket.emit(ConstantsManager.JOIN_ROOM_EVENT, roomInfo);
    }

    private List<Room> getRoomsList(JSONArray jsonArray) {
        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonRoom = (JSONObject) jsonArray.get(i);
                JSONArray jsonPlayers = jsonRoom.getJSONArray("players");
                if (jsonPlayers.length() == 0) continue;
                int bet = jsonRoom.getInt("bet");
                int number = jsonRoom.getInt("numRoom");
                Room room = new Room(getPlayersList(jsonPlayers), bet, number);
                rooms.add(room);

            } catch (JSONException ignored) {
                return null;
            }
        }
        return rooms;
    }

    private List<Player> getPlayersList(JSONArray jsonPlayers) throws JSONException {
        List<Player> players = new ArrayList<>();
        for (int j = 0; j < jsonPlayers.length(); j++) {
            JSONObject jsonPlayer = (JSONObject) jsonPlayers.get(j);
            Player player = new Player();
            Timber.d(jsonPlayer.getString("id"));
            Timber.d(getUserId());
            player.setId(jsonPlayer.getString("id"));
            player.setName(jsonPlayer.getString("name"));
            player.setImageResId(player.getId().equals(getUserId())
                    ? getUserImage() : Player.getRandomImage());
            players.add(player);
        }
        return players;
    }

    private void auth() {
        JSONObject authData = new JSONObject();
        try {
            authData.put("name", getOnlineName());
            authData.put("id", getUserId());
            authData.put("token", getAuthToken());
        } catch (JSONException ignored) {

        }
        mSocket.emit(ConstantsManager.AUTH_EVENT, authData);
    }

    private void createRoom() {
        Timber.e("Creating room...");
        JSONObject roomData = new JSONObject();
        try {
            roomData.put("bet", mBet);
        } catch (JSONException ignored) {

        }
        mSocket.emit(ConstantsManager.CREATE_ROOM_EVENT, roomData);
    }

    private void moveToRoom(Player[] players, int roomNumber) {
        if(!mHasEntered && getContext() != null) {
            RoomActivity_.intent(getContext())
                    .parcelablePlayers(players)
                    .roomNumber(roomNumber)
                    .bet(mBet)
                    .idInRoom(mIdInRoom)
                    .start();
            mHasEntered = true;
        }
    }
}
