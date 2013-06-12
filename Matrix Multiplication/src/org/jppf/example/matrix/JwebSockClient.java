package org.jppf.example.matrix;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.*;

import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.kit.WebSocketServerEvent;
import org.jwebsocket.listener.WebSocketServerTokenEvent;
import org.jwebsocket.listener.WebSocketServerTokenListener;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;


public class JwebSockClient
    implements WebSocketServerTokenListener
{
    private TokenServer tokenServer;

    public TokenServer getTokenServer()
    {
        return tokenServer;
    }

    public void init()
    {

        String[] aArgs = { "" };

        // the following line must not be removed due to GNU LGPL 3.0 license!
        JWebSocketFactory.printCopyrightToConsole();
        // check if home, config or bootstrap path are passed by command line
        JWebSocketConfig.initForConsoleApp( aArgs );
        try
        {
            JWebSocketFactory.start();
            tokenServer = (TokenServer) JWebSocketFactory.getServer( "ts0" );
            if ( tokenServer != null )
            {
                System.out.println( "Server was found!" );
                tokenServer.addListener( this );
            }
            else
            {
                System.out.println( "Server was NOT found!" );
            }
        }
        catch ( Exception IEx )
        {
            IEx.printStackTrace();
        }
    }

    @Override
    public void processClosed( WebSocketServerEvent arg0 )
    {
        // TODO Auto-generated method stub
        System.out.println( "Process closed!" );
    }

    @Override
    public void processOpened( WebSocketServerEvent arg0 )
    {
        // TODO Auto-generated method stub
        System.out.println( "Process opened!" );
    }

    @Override
    public void processPacket( WebSocketServerEvent arg0, WebSocketPacket arg1 )
    {
        // TODO Auto-generated method stub
        String[] packetData = new String[2];
        packetData = parseJSON( arg1.getString() );
        System.out.println( "Got data: " + packetData[1] +
            "\n" + "from: "+ packetData[0]);
        
        if(packetData[1].charAt( 0 ) == '[')
        {//its a matrix
            Object JSONString = JSONValue.parse( packetData[1] );
            JSONArray matrices = (JSONArray) JSONString;
            System.out.println("matrices size:  " + matrices.size());
            
            Object JSONStringA = matrices.get( 0 );
			System.out.println("011");
			/*JSONObject object1 = (JSONObject) JSONStringA;
			System.out.println("012");
			System.out.println(object1.toJSONString());
			*/
            JSONArray matrixA = (JSONArray) JSONStringA;
            System.out.println("matrixA size: "+matrixA.size());
			System.out.println("02");
            Object JSONStringB = matrices.get( 1 );
			System.out.println("03");
            JSONArray matrixB = (JSONArray) JSONStringB;
            System.out.println("04");
            doMatrixMultiplication(matrixA, matrixB, packetData[0]);
        }
        
        //sendProcessedMatrix(packetData);
        //sendPacket( 57 );
    }
    
    public void doMatrixMultiplication(JSONArray matrixA, JSONArray matrixB, String id)
    {
        String[] packetData = new String[3]; //[0] == ID, [1] == matrixStringA [2] =matrixB
        String matrixa = matrixA.toJSONString();
        String matrixb = matrixB.toJSONString();
		System.out.println("05");
        packetData[0] = id;
        packetData[1] = matrixa;
        packetData[2] = matrixb;
        String[] resultData = new String[2];
		System.out.println("01");
		MatrixRunner runner = new MatrixRunner();
		System.out.println("012a");
        resultData = runner.go(packetData);
        System.out.println("012b");
		if(resultData!=null)
		{
			System.out.println(resultData.length);
		}
		else System.out.println("array is null");
		
		if(resultData[0] == null)
		{
			System.out.println("result0: null");
		}
		
		
		//System.out.println("result0: "+resultData[0]);
		System.out.println("result1: "+resultData[1]);
        sendProcessedMatrix(resultData);
    }

    @Override
    public void processToken( WebSocketServerTokenEvent arg0, Token arg1 )
    {
        // TODO Auto-generated method stub
        //System.out.println( "###PROCESS TOKEN###" );
    }
    
    public void sendProcessedMatrix(String[] sourceData)
    {
        String sourceId = sourceData[0];
        String data = sourceData[1];
        Map lConnectorMap = getTokenServer().getAllConnectors();

        Collection<WebSocketConnector> lConnectors = lConnectorMap.values();
        for ( WebSocketConnector wsc : lConnectors )
        {
            String ids = wsc.getId();
            if(ids.equals( sourceId ))
            {//sending back to sender
                String json = "{\"type\":\"matrix\",\"action\":\"matrix\",\"uniqueId\":\""+ids+"\",\"matrix\":" + data + "}";
                WebSocketPacket wsPacket = new RawPacket( json );
                getTokenServer().sendPacket( wsc, wsPacket );
            }
        }
    }

    public void sendPacket( int slideNumber )
    {
        Map lConnectorMap = getTokenServer().getAllConnectors();

        Collection<WebSocketConnector> lConnectors = lConnectorMap.values();
        for ( WebSocketConnector wsc : lConnectors )
        {
            String json = "{\"action\":\"slide\",\"uniqueId\":123,\"slideNumber\":" + slideNumber + "}";
            WebSocketPacket wsPacket = new RawPacket( json );
            getTokenServer().sendPacket( wsc, wsPacket );
        }
    }

    public String[] parseJSON( String arg )
    {
        String jsonText = arg;
        JSONParser parser = new JSONParser();
        ContainerFactory containerFactory = new ContainerFactory()
        {
            public List creatArrayContainer()
            {
                return new LinkedList();
            }

            public Map createObjectContainer()
            {
                return new LinkedHashMap();
            }

        };
        String data = "";
        String sourceId = "";
        try
        {
            Map json = (Map) parser.parse( jsonText, containerFactory );
            Iterator iter = json.entrySet().iterator();
            // System.out.println("==iterate result==");
            while ( iter.hasNext() )
            {
                Map.Entry entry = (Map.Entry) iter.next();
                if ( entry.getKey().equals( "data" ) )
                {
                    data = "" + entry.getValue();
                }
                else if( entry.getKey().equals( "sourceId" ))
                {
                    sourceId = "" + entry.getValue();
                }
            }
        }
        catch ( ParseException pe )
        {
            System.out.println( pe );
        }

        if ( data.equals( "" ) )
            data = "EMPTY MESSAGE DATA";

        String[] packetData = new String[] { sourceId, data };
        return packetData;
    }
    
    public Map JSONtoMap(String json_string)
    {
        String jsonText = json_string;
        JSONParser parser = new JSONParser();
        ContainerFactory containerFactory = new ContainerFactory()
        {
            public List creatArrayContainer()
            {
                return new LinkedList();
            }

            public Map createObjectContainer()
            {
                return new LinkedHashMap();
            }

        };
        Map json = null;
        try
        {
            json = (Map) parser.parse( jsonText, containerFactory );
            Iterator iter = json.entrySet().iterator();
            // System.out.println("==iterate result==");
            while ( iter.hasNext() )
            {
                Map.Entry entry = (Map.Entry) iter.next();
                System.out.println(entry.getKey());
            }
        }
        catch ( ParseException pe )
        {
            System.out.println( pe );
        }

        return json;

    }

}
