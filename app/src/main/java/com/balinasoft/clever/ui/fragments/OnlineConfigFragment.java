package com.balinasoft.clever.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.balinasoft.clever.R;
import com.balinasoft.clever.model.Player;
import com.balinasoft.clever.model.Room;
import com.balinasoft.clever.ui.activities.RoomActivity_;
import com.balinasoft.clever.ui.adapters.RoomAdapter;
import com.balinasoft.clever.util.AvatarMapper;
import com.balinasoft.clever.util.ConstantsManager;
import com.balinasoft.clever.util.NetworkStateChecker;
import com.balinasoft.clever.util.RoomsClickListener;
import com.balinasoft.clever.util.SocketErrorListener;
import com.jakewharton.rxbinding.widget.RxTextView;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
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
    TextView mEmptyView;

    @ViewById(R.id.swipe)
    SwipeRefreshLayout mSwipe;

    @StringRes(R.string.unstable_connection)
    String mSocketConnectErrorMessage;

    @StringRes(R.string.auth_failed)
    String mAuthFailedMessage;

    @StringRes(R.string.room_creation_error)
    String mErrorCreatingRoomMessage;

    @StringRes(R.string.room_connection_success)
    String mRoomConnectionSuccessfulMessage;

    @StringRes(R.string.room_creation_success)
    String mRoomCreationSuccessfulMessage;

    @StringRes(R.string.in_lobby)
    String mInLobbyMessage;

    @StringRes(R.string.room_connection_fail)
    String mRoomConnectionFailedMessage;

    @StringRes(R.string.unauthorized)
    String mUnAuthorizedMessage;

    @Bean
    NetworkStateChecker mNetworkStateChecker;

    @Bean
    AvatarMapper mAvatarMapper;

    private SocketErrorListener mSocketErrorListener;

    private Socket mSocket = getSocket();

    private boolean mIsConnected = false;

    private boolean mIsAuthorized = false;

    private boolean mHasEntered = false;

    private String mIdInRoom = "";

    private Subscription mSwipeSub;

    private Subscription mFilterSub;

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
        listenToFilter();
    }

    @Override
    public void onPause() {
        super.onPause();
        toggleSwipe(false);
        if (mSwipeSub != null && !mSwipeSub.isUnsubscribed()) mSwipeSub.unsubscribe();
        if (mFilterSub != null && !mFilterSub.isUnsubscribed()) mFilterSub.unsubscribe();
        stopSocketListening();
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
        if (mIsAuthorized) createRoom();
        else toastNotifyUserWith(mUnAuthorizedMessage);
    }

    private void listenToFilter() {
        mFilterSub = Observable.combineLatest(
                RxTextView.textChanges(mBetValueLabel),
                RxTextView.textChanges(mPlayersNumberValueLabel),
                (roomBet, roomPlayers) -> new AbstractMap.SimpleEntry<>(roomBet.toString(), roomPlayers.toString()))
                .debounce(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((roomInfo) -> {
                    updateRooms();
                }, throwable -> {
                });
    }

    private void setUpRooms() {
        mRoomsList.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void setUpRefresh() {
        mSwipe.setColorSchemeColors(ContextCompat.getColor(getContext(),R.color.colorPrimary));
        mSwipe.setOnRefreshListener(this::updateRooms);
    }

    private void toggleSwipe(boolean isRefreshing) {
        if (mSwipe != null) mSwipe.setRefreshing(isRefreshing);
    }

    private void requestForRooms() {
        mSocket.emit(ConstantsManager.ROOMS_LIST_GET_EVENT, "{}");
    }

    private void updateRooms() {
        if (mNetworkStateChecker.isNetworkAvailable() && mIsConnected) {
            if (!mSwipe.isRefreshing()) toggleSwipe(true);
            hideList();
            requestForRooms();
            mSwipeSub = Observable.interval(ConstantsManager.CONNECTION_TIME_OUT, TimeUnit.SECONDS)
                    .take(1)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doAfterTerminate(() -> toggleSwipe(false))
                    .subscribe(tick -> {
                    }, throwable -> {
                    });
        } else toggleSwipe(false);
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
        mSocket.on(Socket.EVENT_RECONNECT_ATTEMPT, this::onReconnectAttempt);//
        mSocket.on(ConstantsManager.AUTH_EVENT, this::onAuth);//
        mSocket.on(ConstantsManager.JOIN_ROOM_EVENT, this::onJoinRoom);//
        mSocket.on(ConstantsManager.ROOM_MESSAGE_EVENT, this::onRoomCreated);//
        mSocket.on(ConstantsManager.ROOMS_LIST_GET_EVENT, this::onRoomsListGot);//
        mSocket.on(ConstantsManager.ROOM_INFO_EVENT, this::onRoomInfoReceived);//
    }

    private void stopSocketListening() {
        mSocket.off(Socket.EVENT_CONNECT, args -> onConnect());
        mSocket.off(Socket.EVENT_DISCONNECT, args -> onDisconnect());
        mSocket.off(Socket.EVENT_RECONNECT_ATTEMPT,this::onReconnectAttempt);
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
        mIsAuthorized = false;
        Timber.e("Disconnecting");
    }

    @UiThread
    void onReconnectAttempt(Object... args) {
        Timber.e("onReconnectAttempt");
        Integer attempts = (Integer) args[0];
        if(attempts < 4) mSocketErrorListener.onSocketError(mSocketConnectErrorMessage);
    }

    @UiThread
    void onRoomsListGot(Object... args) {
        toggleSwipe(false);
        JSONObject response = (JSONObject) args[0];
        Timber.e("onRoomsGot %s", response.toString());
        try {
            JSONArray roomsListJson = response.getJSONArray("rooms");
            List<Room> rooms = getRoomsList(roomsListJson);
            if (rooms != null && rooms.size() > 0 && mRoomsList != null) {
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
            mSocketErrorListener.onSocketError(mAuthFailedMessage);
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
        if (message.equals(mInLobbyMessage)) {
            mIsAuthorized = true;
        } else toastNotifyUserWith(message);
    }

    @UiThread
    void onRoomCreated(Object... args) {
        JSONObject data = (JSONObject) args[0];
        int success = -1;
        String message = "";
        try {
            success = data.getInt("success");
            message = data.getString("message");
        } catch (JSONException ignored) {
        }
        Timber.e("onRoomCreated %s", message);
        if (success == 0) notifyUserWith(mErrorCreatingRoomMessage);
    }

    @UiThread
    void onRoomInfoReceived(Object... args) {
        JSONObject roomInfo = (JSONObject) args[0];
        Timber.e("onRoomInfoReceived %s",roomInfo.toString());
        try {
            int bet = roomInfo.getInt("bet");
            int roomNumber = roomInfo.getInt("numRoom");
            int maxPlayers = roomInfo.getInt("maxPlayers");
            if (getOnlineCoins() >= bet) {

                JSONArray jsonPlayers = roomInfo.getJSONArray("players");
                List<Player> players = getPlayersList(jsonPlayers);
                Player[] pls = new Player[players.size()];
                pls = players.toArray(pls);
                moveToRoom(pls,roomNumber,maxPlayers,bet);
            } else notifyUserWith(mNotEnoughCoinsMessage);
        } catch (JSONException ignored) {

        }
    }

    @Override
    public void onRoomClicked(int roomNumber,int bet) {
        if(getOnlineCoins() >= bet) joinRoom(roomNumber);
        else notifyUserWith(mNotEnoughCoinsMessage);
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
        Timber.d("RoomsList %s",jsonArray.toString());
        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonRoom = (JSONObject) jsonArray.get(i);
                JSONArray jsonPlayers = jsonRoom.getJSONArray("players");
                if (jsonPlayers.length() == 0) continue;
                int bet = jsonRoom.getInt("bet");
                int number = jsonRoom.getInt("numRoom");
                int maxPlayers = jsonRoom.getInt("maxPlayers");
                if (bet <= mBet && maxPlayers-1 <= mPlayersCount + 1) {// FIXME: 19.10.2016
                    Room room = new Room(getPlayersList(jsonPlayers), bet, number, maxPlayers);
                    rooms.add(room);
                }

            } catch (JSONException ignored) {
                return null;
            }
        }
        if(rooms.size() > 0) sortRooms(rooms);
        return rooms;
    }

    private void sortRooms(List<Room> rooms) {
        Collections.sort(rooms, (room1, room2) -> {
            int comp = room1.getBet() - room2.getBet();
            if(comp == 0) {
                comp = room1.getPlayers().size() - room2.getPlayers().size();
            }
            return comp;
        });
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
                    ? getUserImage() : mAvatarMapper.getAvatar(jsonPlayer.getInt("avatar")));
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
            authData.put("avatar", mAvatarMapper.getAvatarId(getUserImage()));
        } catch (JSONException ignored) {

        }
        mSocket.emit(ConstantsManager.AUTH_EVENT, authData);
    }

    private void createRoom() {
        Timber.e("Creating room...");
        JSONObject roomData = new JSONObject();
        try {
            roomData.put("bet", mBet);
            roomData.put("maxPlayers",mPlayersCount + 2);
        } catch (JSONException ignored) {

        }
        mSocket.emit(ConstantsManager.CREATE_ROOM_EVENT, roomData);
    }

    private void moveToRoom(Player[] players, int roomNumber,int maxPlayers,int bet) {
        Timber.d("move to room %d %d %d",roomNumber,maxPlayers,bet);
        if(!mHasEntered && getContext() != null) {
            RoomActivity_.intent(getContext())
                    .parcelablePlayers(players)
                    .roomNumber(roomNumber)
                    .bet(bet)
                    .maxPlayers(maxPlayers)
                    .idInRoom(mIdInRoom)
                    .start();
            mHasEntered = true;
        }
    }
}
