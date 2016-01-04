package hubs;

import akka.actor.ActorRef;
import akka.actor.Props;
import play.Logger;
import play.libs.Akka;
import scala.concurrent.duration.Duration;
import signalJ.services.Hub;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ChatHub extends Hub<ChatPage> {
	//Users is a Map key-> rooms value-> users
    private final static Map<String, Set<String>> users = new HashMap<>();
    //ConnectionsToUseernames makes a relation between the socketui and the logged username 
    private final static Map<UUID, String> connectionsToUsernames = new HashMap<>();
    // auth will be true if the client socket is authenticated or not (invitado)
    private static boolean auth=false;

    public boolean login(String username) {
    	if(username.equals("invitado")){
    		username=context().connectionId.toString();
    		clients().callerState.put("username", username);
    		joinRoom("Room1");
    		joinRoom("Room2");
    		clients().caller.roomList(getRoomList());
    		connectionsToUsernames.putIfAbsent(context().connectionId, clients().callerState.get("username"));
    		return true;
    	}else{
    		if(connectionsToUsernames.containsValue(username) || username.equals("Robot")) return false;
    		auth=true;
    		clients().callerState.put("username", username);
    		joinRoom("Room1");
    		joinRoom("Room2");
    		clients().caller.roomList(getRoomList());
    		connectionsToUsernames.putIfAbsent(context().connectionId, clients().callerState.get("username"));
    		return true;
    	}
    }

    public void logout() {
        connectionsToUsernames.remove(context().connectionId);
        removeUserFromRoom(clients().callerState.get("username"));
        clients().callerState.put("username", "");
        auth=false;
    }

    public void joinRoom(String room) {
        if(users.containsKey(room)) joinRoom(room, false);
        else createRoom(room);
        clients().group(room).userList(getUserList(room));
    }

    private void createRoom(String room) {
        users.putIfAbsent(room, new HashSet<>());
        joinRoom(room, true);
    }

    public void sendMessage(String room, String message) {
    	if(!auth && room.equals("Room1")){
    		
    	}else{
    		clients().othersInGroup(room).sendMessage(clients().callerState.get("username"), message);
    	}
    }

    public void joinRoom(String room, boolean fromCreate) {
        boolean changed = removeUserFromRoom(clients().callerState.get("username"));
        addUserToRoom(clients().callerState.get("username"), room);
        if(fromCreate || changed) clients().all.roomList(getRoomList());
    }

    private void addUserToRoom(String username, String room) {
        users.get(room).add(username);
        groups().add(context().connectionId, room);
        clients().othersInGroup(room).userJoinedRoom(username);
    }

    private boolean removeUserFromRoom(String username) {
        String room = null;
        String removekey = null;
        for(String key : users.keySet()) {
            if(users.get(key).remove(username)) {
                room = key;
                clients().othersInGroup(key).userLeftRoom(username);
                clients().othersInGroup(key).userList(getUserList(key));
                if(users.get(key).size() == 0 && !key.equalsIgnoreCase("Lobby")) removekey = key;
                break;
            }
        }
        if (room != null) groups().remove(context().connectionId, room);
        if(removekey != null) {
            users.remove(removekey);
            return true;
        }
        return false;
    }

    private Set<String> getUserList(String room) {
        final Set<String> userlist = new HashSet<>(users.get(room));
        return userlist;
    }

    private Set<String> getRoomList() {
        return new HashSet<>(users.keySet());
    }

    @Override
    protected Class<ChatPage> getInterface() {
        return ChatPage.class;
    }

    @Override
    public void onDisconnected() {
        final String username = connectionsToUsernames.remove(context().connectionId);
        Logger.debug("Disconnect: " + username);
        removeUserFromRoom(username);
    }

    @Override
    public void onConnected() {
       // If it is needed to trigger something when a new socket is connected.
    }
}