package lvc.cds;

import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.json.JSONObject;

import lvc.cds.briefobjects.BriefUser;

import java.net.URI;
import java.util.ArrayList;

// Written by Jordan.

@Path("tasks")
public class TaskResource {

    @Context
    UriInfo uriInfo;

    // adds a new task
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postNewTask(Task t) {
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        int count = Database.tasks.get("count").getInt("count");
        URI uri = uriBuilder.path("" + count).build();

        t.setId(count);
        t.setSelfLink();
        t.setLinks();

        JSONObject taskJSON = new JSONObject();
        taskJSON.put("id", t.getId());
        taskJSON.put("name", t.getName());
        taskJSON.put("description", t.getDescription());
        taskJSON.put("status", t.getStatus());
        taskJSON.put("selflink", t.getSelfLink());
        Database.tasks.put(Integer.toString(t.getId()), taskJSON);
        count++;
        Database.tasks.updateJSONField("count", "count", count);
        Database.tasks.write(Main.storageDir + "/tasksKVS.txt");

        return Response.created(uri).entity(t).build();
    }


    // returns the entire list of tasks
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTaskList() {
        ArrayList<String> taskIDs = Database.tasks.getMatchingKeys(".*");
        ArrayList<Task> tasks = new ArrayList<>();
        for(String task : taskIDs){
            if(!task.equals("count"))
            tasks.add(Task.fullConversion(task));
        }

        return Response.ok(tasks).build();
    }


    // returns a specified task
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTask(@PathParam("id") String id) {
        if (!Database.tasks.containsKeyString(id)) {
            return Response.status(404).build();
        }
        // JSONObject taskJSON = Database.tasks.get(id);
        Task t = Task.fullConversion(id);

        return Response.ok(t).build();
    }


    // deletes a specified task
    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteTask(@PathParam("id") String id) {
        if(!Database.tasks.containsKeyString(id)) {
            return Response.status(404, "task does not exist").build();
        }
        Database.tasks.remove(id);

        ArrayList<String> taskAssociations = Database.associations.getMatchingKeys("task=" + id);
        for (String string : taskAssociations) {
            Database.associations.remove(string);
        }
        Database.tasks.write(Main.storageDir + "/tasksKVS.txt");
        Database.associations.write(Main.storageDir + "/associations.txt");

        return Response.noContent().build();
    }


    // return the status of a specified task
    @GET
    @Path("{id}/status")
    @Produces(MediaType.APPLICATION_JSON) 
    public Response getStatus(@PathParam("id") String id) {
        if (!Database.tasks.containsKeyString(id)) {
            return Response.status(404).build();
        }
        String status = (String) Database.tasks.getJSON(id, "status");
        JSONObject json = new JSONObject().put("status", status);
        return Response.ok(json.toString()).build();
    }


    // changes the status of a specified task
    @PUT
    @Path("{id}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateStatus(@PathParam("id") String id, Task t) {
        Database.tasks.updateJSONField(id, "status", t.getStatus());
        Database.tasks.write(Main.storageDir + "/tasksKVS.txt");
        return Response.ok().build();
    }


    // returns the description of the specified task
    @GET
    @Path("{id}/description")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDescription(@PathParam("id") String id) {
        if (!Database.tasks.containsKeyString(id)) {
            return Response.status(404).build();
        }

        String description = (String) Database.tasks.getJSON(id, "description");
        JSONObject json = new JSONObject().put("description", description);
        return Response.ok(json.toString()).build();
    }


    // changes the description of a specified task
    @PUT
    @Path("{id}/description")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateDescription(@PathParam("id") String id, Task t) {
        Database.tasks.updateJSONField(id, "description", t.getDescription());
        Database.tasks.write(Main.storageDir + "/tasksKVS.txt");
        return Response.ok().build();
    }


    // returns the name of a specified project
    @GET
    @Path("{id}/name")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getName(@PathParam("id") String id) {
        if (!Database.tasks.containsKeyString(id)) {
            return Response.status(404).build();
        }

        String name = (String) Database.tasks.getJSON(id, "name");
        JSONObject json = new JSONObject().put("name", name);
        return Response.ok(json.toString()).build();
    }


    // changes the name of a specified task
    @PUT
    @Path("{id}/name")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateName(@PathParam("id") String id, Task t) {
        Database.tasks.updateJSONField(id, "name", t.getName());
        Database.tasks.write(Main.storageDir + "/tasksKVS.txt");
        return Response.ok().build();
    }


    // returns the user associated with a specified task
    @GET
    @Path("{id}/user")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@PathParam("id") String id) {
        ArrayList<String> user = Database.associations.getMatchingKeys("task=" + id + ":user=");
        if (user.size() != 0) {
            String entireAssoc = user.get(0);
            String userId = entireAssoc.substring(entireAssoc.lastIndexOf("=") + 1);
            var temp = Database.users.get(userId);
            BriefUser u = new BriefUser();
            u.setName(temp.getString("name"));
            u.setUri(Main.BASE_URI+"users/"+userId);
            return Response.ok(u).build();
        }
        return Response.status(404).build();
    }


    // assigns a user to a specified task
    @PUT
    @Path("{id}/user")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response assignUser(@PathParam("id") String id, User user) {
        String assoc = "task=" + id + ":user=" + user.getId();
        // only one user can be assigned to a task, if there already is one throw an error code
        if (!Database.users.containsKeyString(Integer.toString(user.getId()))) {
            return Response.status(404, "The user you have requested to add to this task does not exist")
                    .build();
        }
        ArrayList<String> taskAssociations = Database.associations.getMatchingKeys("task=" + id + ":user=");
        if(!taskAssociations.isEmpty()) {
            return Response.status(409, "This task already has a user assigned to it").build();
        }
        Database.associations.put(assoc, new JSONObject());
        Database.associations.write(Main.storageDir + "/associations.txt");

        return Response.ok().build();
    }


    // deletes a user off a task
    @DELETE
    @Path("{id}/user")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeUserFromTask(@PathParam("id") String id) {
        ArrayList<String> taskAssociations = Database.associations.getMatchingKeys("task=" + id + ":user=");
        if(taskAssociations.isEmpty()) {
            return Response.status(404, "The user you have requested to delete from this task is not"
                    + " assigned to this task").build();
        }
        for (String string : taskAssociations) {
            Database.associations.remove(string);
        }

        Database.associations.write(Main.storageDir + "/associations.txt");
        return Response.noContent().build();
    }
    
}
