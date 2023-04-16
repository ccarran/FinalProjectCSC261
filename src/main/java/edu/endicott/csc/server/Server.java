package edu.endicott.csc.server;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author cesar
 */

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.endicott.csc.finalproject.GameInfo;
import edu.endicott.csc.finalproject.GameList;
import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
 

//Based on code from: https://www.geeksforgeeks.org/socket-programming-in-java/
public class Server
{
    private Socket socket               = null;
    private ServerSocket server         = null;
    private DataInputStream in          = null;
    private DataOutputStream out        = null;
    private GameInfo appDetails;
    
    public Server(int port)
    {
        try
        {
            server = new ServerSocket(port);
            
            // Check if changes have been made to the steam database
            checkSteamAPI();
            
            System.out.println("Server started");
 
            System.out.println("Waiting for a client ...");
 
            socket = server.accept();
            System.out.println("Client accepted");
 
            // takes input from the client socket
            in = new DataInputStream(
                new BufferedInputStream(socket.getInputStream()));
            
            this.out = new DataOutputStream(this.socket.getOutputStream());
 
            String line = "";
            
            // reads message from client until "Over" is sent
            while (!line.equals("Over"))
            {
                try
                {
                    line = in.readUTF();
                    
                    this.appDetails = this.getGameDataFromAPI(line);
                    
                    if (this.appDetails.success){
                        this.out.writeUTF(this.appDetails.data.name);
                        System.out.println(this.appDetails.data.name);
                    }
                }
                catch(IOException i)
                {
                    System.out.println(i);
                }
            }
            System.out.println("Closing connection");
 
            // close connection
            socket.close();
            in.close();
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
    }
 
    private GameInfo getGameDataFromAPI(String appid) {
        Gson gson = new Gson();
        GameInfo tmpGameEntry = null;
        
        try {
            URL steamGameURL = new URL("https://store.steampowered.com/api/appdetails?appids=" + appid);
            HttpURLConnection connectGamedata = (HttpURLConnection)steamGameURL.openConnection();
            BufferedReader readerGamedata = new BufferedReader(new InputStreamReader(connectGamedata.getInputStream()));
            
            JsonObject gamedataJson = JsonParser.parseReader(readerGamedata).getAsJsonObject().getAsJsonObject(appid);
        
            tmpGameEntry = gson.fromJson(gamedataJson, GameInfo.class);
        } catch (MalformedURLException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tmpGameEntry;
    }
    
    private void checkSteamAPI() {
        Gson gson = new Gson();
        GameList steamGameList = null;
        
        try {
            // First, we get the list of games from the Steam API
            URL steamGameURL = new URL("http://api.steampowered.com/ISteamApps/GetAppList/v0002/?key=STEAMKEY&format=json");
            HttpURLConnection connectListdata = (HttpURLConnection) steamGameURL.openConnection();
            BufferedReader readerListdata = new BufferedReader(new InputStreamReader(connectListdata.getInputStream()));
            
            JsonObject gameListJson = JsonParser.parseReader(readerListdata).getAsJsonObject();
        
            steamGameList = gson.fromJson(gameListJson, GameList.class);
   
            
            // Then, we check if we have that list already
            File steamGamesList = new File("allGamesFile.json");
            if(!steamGamesList.exists()) {
                FileWriter writer = new FileWriter(steamGamesList.getName());
                writer.write(gson.toJson(gameListJson));
            }
            else {
                
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String args[])
    {
        Server server = new Server(5000);
    }
}