package lvc.cds;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.Scanner;


public class Main {
    public static final String BASE_URI = "http://localhost:5000/";
    public static String storageDir;

    public static HttpServer startServer() {

        final ResourceConfig rc = new ResourceConfig().packages("lvc.cds");

        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }
    
    public static void main(String[] args) throws IOException {
        // Get KVS files from user.
        Scanner in = new Scanner(System.in);
        System.out.println("Enter a directory for KVS text files to build the KVSs from.\nIf you want to use the KVS files in this project, enter \"current\".");
        storageDir = in.nextLine();
        if (storageDir.equals("current")) {
            storageDir = System.getProperty("user.dir") + "/KVSTextFiles";
        }
        //Load KVSs from storage.
        Database.associations.read(storageDir+"/associations.txt");
        Database.projects.read(storageDir+"/projectsKVS.txt");
        Database.tasks.read(storageDir+"/tasksKVS.txt");
        Database.users.read(storageDir+"/usersKVS.txt");
        Database.todolists.read(storageDir+"/todolistsKVS.txt");

        final HttpServer server = startServer();
        System.out.println(String.format("Jersey app started with endpoints available at "
                + "%s%nHit Ctrl-C to stop it...", BASE_URI));
        System.in.read();
        server.shutdownNow();
        in.close();
    }
}