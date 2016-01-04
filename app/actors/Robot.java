package actors;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import hubs.ChatHub;
import hubs.ChatPage;
import signalJ.GlobalHost;
import signalJ.services.HubContext;

public class Robot extends AbstractActor {
    public Robot() {
    	//here the code that the Robot runs- disabled for us.
        receive(
                ReceiveBuilder.matchAny(x -> {
                    HubContext<ChatPage> hub = GlobalHost.getHub(ChatHub.class);
                    hub.clients().all.sendMessage("Robot", "I am alive!");
                }).build()
        );
    }
}