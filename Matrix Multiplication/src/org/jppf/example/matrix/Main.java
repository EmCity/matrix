package org.jppf.example.matrix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.server.TokenServer;


public class Main
{

    /**
     * @param args
     */
    
    JwebSockClient mClient;
    
    public static void main( String[] args )
    {
        // TODO Auto-generated method stub
        
        System.out.println("Starting jWebSocketServer...");
        
        new Main().run();
    }
    
    public void run()
    {
        mClient = new JwebSockClient();
        mClient.init();
    }
    
    public void getConnectedClients()
    {
        
        Map lConnectorMap = mClient.getTokenServer().getAllConnectors();
        List<Map> lResultList = new ArrayList<Map>();
        Collection<WebSocketConnector> lConnectors = lConnectorMap.values();
        for(WebSocketConnector lConnector : lConnectors)
        {
            Map lResultItem = new HashMap<String, Object>();
            lResultItem.put( "port", lConnector.getRemotePort() );
            lResultItem.put( "unid", lConnector.getNodeId() );
            lResultItem.put( "username", lConnector.getUsername() );
            lResultItem.put( "isToken", lConnector.getBoolean( TokenServer.VAR_IS_TOKENSERVER ) );
            lResultList.add( lResultItem );
        }
        for(Map m : lResultList)
        {
            System.out.println(m);
        }
        
    }

}
