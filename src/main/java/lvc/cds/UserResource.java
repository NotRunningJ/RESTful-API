package lvc.cds;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.json.JSONObject;

import lvc.cds.briefobjects.BriefProject;
import lvc.cds.briefobjects.BriefTask;


// Written by Jake, Jordan, and Zane.

@Path("users")
public class UserResource {

    @Context
    UriInfo uriInfo;

    // Get all users.
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers() {
        ArrayList<String> userIDs = Database.users.getMatchingKeys(".*");
        ArrayList<User> users = new ArrayList<>();
        for (String string : userIDs) {
            if(!string.equals("count"))
            users.add(User.fullConversion(string));
        }

        return Response.ok(users).build();
    }
    
    // Create new user.
    @POST
    @Consumes(MediaType.APPLICATION_JSON) 
    @Produces(MediaType.APPLICATION_JSON) 
    public Response createNewUser(User u) {
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        int count = Database.users.get("count").getInt("count");
        URI uri = uriBuilder.path("" + count).build();

        u.setId(count);
        u.setSelfLink();
        u.setUserLinks();
        JSONObject userJSON = new JSONObject();
        userJSON.put("id", u.getId());
        userJSON.put("name", u.getName());
        userJSON.put("selflink", u.getSelfLink());

        Database.users.put(Integer.toString(u.getId()), userJSON);
        count++;
        Database.users.updateJSONField("count", "count", count);
        Database.users.write(Main.storageDir + "/usersKVS.txt");
        
        return Response.created(uri)
                       .entity(u)
                       .build();
    }

    // Get a representation of a specific user.
    @GET
    @Path("{id}")
    public Response getUser(@PathParam("id") String id) {
        if (!Database.users.containsKeyString(id))
            return Response.status(404).build();

        User u = User.fullConversion(id);
        return Response.ok(u).build();
    }

    // Update the name of the user.
    @PUT
    @Path("{id}/name")
    @Consumes(MediaType.APPLICATION_JSON) 
        public Response changeName(@PathParam("id") String id, User u) {
        
            if (!Database.users.containsKeyString(id))
            return Response.status(404).build();

            Database.users.updateJSONField(id, "name", u.getName());
            Database.users.write(Main.storageDir + "/usersKVS.txt");
            return Response.ok().build();
        }
    
    // Get the name of the user.
    @GET
    @Path("{id}/name")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getName(@PathParam("id") String id) {
        if (!Database.users.containsKeyString(id))
            return Response.status(404).build();
        
        String name = (String)Database.users.getJSON(id, "name");
        JSONObject json = new JSONObject().put("name", name);
        return Response.ok(json.toString()).build();
    }

    //Get the tasks associated with the user
    @GET
    @Path("{id}/tasks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTasks(@PathParam("id") String id) {
        if (!Database.users.containsKeyString(id))
            return Response.status(404).build();
        ArrayList<String> taskAssocs = Database.associations.getMatchingKeys("task=\\d+:user=" + id);
        ArrayList<BriefTask> tasks = new ArrayList<>();
        for (String string : taskAssocs) {
            String tid = string.substring(string.indexOf("=")+1, string.indexOf(":"));
            String name = (String) Database.tasks.getJSON(tid, "name");
            String uri = Main.BASE_URI + "tasks/" + tid;
            tasks.add(new BriefTask(name, uri));
        }
        return Response.ok(tasks).build();
    }

    // Assign a task to this user.
    @POST
    @Path("{id}/tasks")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response assignTask(@PathParam("id") String id, Task t) {
        String tid = Integer.toString(t.getId());
        if (!Database.tasks.containsKeyString(tid))
            return Response.status(404).build();

        // Create association in KVS
        String assoc = "task=" + tid + ":user=" + id;
        Database.associations.put(assoc, new JSONObject());
        Database.associations.write(Main.storageDir + "/associations.txt");
        Database.tasks.updateJSONField(tid, "status", "Assigned");
        Database.tasks.write(Main.storageDir + "/tasksKVS.txt");

        // Construct user representation to include in response body.
        JSONObject taskJSON = Database.tasks.get(tid);
        String name = taskJSON.getString("name");
        String selfLink = taskJSON.getString("selflink");
        URI uri;
        try {
        uri = new URI(Main.BASE_URI + "users/" + id + "/tasks/" + tid);
        } catch (URISyntaxException e) {
            return Response.status(400).build();
        }
        return Response.created(uri).entity(new BriefTask(name, selfLink)).build();
    }

    // Unassign a task. Removes the task from the user's list of tasks.
    @DELETE
    @Path("{id}/tasks/{tid}")
    public Response unassignTask(@PathParam("id") String id, @PathParam("tid") String tid) {
        String assoc = "task=" + tid + ":user=" + id;
        if (!Database.associations.containsKeyString(assoc))
            return Response.status(404).build();
        Database.associations.remove(assoc);
        Database.associations.write(Main.storageDir + "/associations.txt");
        Database.tasks.updateJSONField(tid, "status", "unassigned");
        return Response.noContent().build();
    }

    // Get the projects associated with this user.
    @GET
    @Path("{id}/projects")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProjects(@PathParam("id") String id) {
        if (!Database.users.containsKeyString(id)) 
            return Response.status(404).build();
        
        ArrayList<String> projAssocs = Database.associations.getMatchingKeys("project=\\d+:user=" + id);
        ArrayList<BriefProject> projects = new ArrayList<>();
        for (String string : projAssocs) {
            String pid = string.substring(string.indexOf("=")+1,string.indexOf(":"));
            String name = (String) Database.projects.getJSON(pid, "name");
            String uri = Main.BASE_URI + "projects/" + pid;
            projects.add(new BriefProject(name, uri));
        }

        return Response.ok(projects).build();
    }

    // Join a project
    @POST
    @Path("{id}/projects")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON) 
    public Response joinProject(@PathParam("id") String id, Project p) {
        String pid = Integer.toString(p.getId());
        if (!Database.projects.containsKeyString(pid))
            return Response.status(404).build();
        
        // Create association and add to KVS
        String assoc = "project=" + pid + ":user=" + id;
        Database.associations.put(assoc, new JSONObject());
        Database.associations.write(Main.storageDir + "/associations.txt");

        
        JSONObject project = Database.projects.get(pid);
        String name = project.getString("name");
        String selfLink = project.getString("selflink");
        URI uri;
        try {
            uri = new URI(Main.BASE_URI +"users" + id + "/projects/" + pid);
        } catch (URISyntaxException e) {
            return Response.status(400).build();
        }
        return Response.created(uri).entity(new BriefProject(name, selfLink)).build();
    }

    // Leave a project.
    @DELETE
    @Path("{id}/projects/{pid}")
    public Response leaveProject(@PathParam("id") String id, @PathParam("pid") String pid) {
        String assoc = "project=" + pid + ":user=" + id;
        if (!Database.associations.containsKeyString(assoc))
            return Response.status(404).build();
        Database.associations.remove(assoc);
        Database.associations.write(Main.storageDir + "/associations.txt");
        return Response.noContent().build();
    }

    // Delete a user
    @DELETE
    @Path("{id}")
    public Response deleteProject(@PathParam("id") String id) {
        Database.users.remove(id);
        Database.users.write(Main.storageDir + "/usersKVS.txt");
        ArrayList<String> userAssociations = Database.associations.getMatchingKeys("user=" + id);

        for (String string : userAssociations) {
            Database.associations.remove(string);
        }

        Database.associations.write(Main.storageDir + "/associations.txt");

        return Response.ok().build();
    }



}
