package lvc.cds;

import java.net.URI;
import java.util.ArrayList;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.json.JSONObject;

import lvc.cds.briefobjects.*;

// Written by Zane.

@Path("todolists")
public class TodoListResource {
    @Context
    UriInfo uriInfo;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    // Gets all todolists
    public Response getTodoLists(@DefaultValue("-1.0")@QueryParam("mincomppercent") String percentage){
        var todolists = new ArrayList<TodoList>();

        Double percent = Double.parseDouble(percentage);
        
        for(var todolist : Database.todolists.entrySet()){
            if(todolist.getKey().equals("count")){
            }else{
                todolists.add(TodoList.fullConversion(todolist.getKey()));
            }
        }

        if(percent == -1.0){
            return Response.ok(todolists).build();
        }

        var filteredTodos = todolists.stream()
            .filter((TodoList t) -> t.getTasks().stream().map((BriefTask task) -> task.getStatus().equals("Completed")).count() / (double)t.getTasks().size() >= (percent/100.00))
            .collect(Collectors.toCollection(ArrayList::new));

        return Response.ok(filteredTodos).build();
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    // Adds new todolist, creates id for todolist
    public Response postTodoList(TodoList todo){
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();

        int count = Database.todolists.get("count").getInt("count");

        URI uri = uriBuilder.path("" + count).build();
        
        todo.setId(Integer.toString(count)).setSelfLink().setOptionLinks();
        Database.todolists.put(todo.getId(), todo.todolistToMiniJSON());
        Database.todolists.updateJSONField("count", "count", ++count);
        Database.todolists.write(Main.storageDir + "/todolistsKVS.txt");
        
        return Response.created(uri)
                    .entity(todo)
                    .build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    // Gets todolist with specific id
    public Response getTodoList(@PathParam("id") String id){
        return Response.ok(TodoList.fullConversion(id)).build();
    }

    @DELETE
    @Path("{id}")
    // Deletes todolist
    public Response deleteTodoList(@PathParam("id") String id){
        for(String key : Database.associations.getMatchingKeys("todolist=" + id)){
            Database.associations.remove(key);
        }
        Database.todolists.remove(id);
        Database.associations.write(Main.storageDir + "/associations.txt");
        Database.todolists.write(Main.storageDir + "/todolistsKVS.txt");

        return Response.noContent().build();
        
    }

    @GET
    @Path("{id}/description")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTodoListDescription(@PathParam("id") String id){
        String description = (String)Database.todolists.getJSON(id, "description");
        JSONObject descriptionJSON = new JSONObject().put("description", description);
        return Response.ok(descriptionJSON.toString()).build();
    }

    @PUT
    @Path("{id}/description")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putTodoListDescription(@PathParam("id") String id, TodoList todo){
        Database.todolists.updateJSONField(id, "description", todo.getDescription());
        Database.todolists.write(Main.storageDir + "/todolistsKVS.txt");
        return Response.noContent().build();
    }

    @GET
    @Path("{id}/name")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTodoListName(@PathParam("id") String id){
        String name = (String)Database.todolists.getJSON(id, "name");
        JSONObject nameJSON = new JSONObject().put("name", name);
        return Response.ok(nameJSON.toString()).build();
    }

    @PUT
    @Path("{id}/name")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putTodoListName(@PathParam("id") String id, TodoList todo) {
        Database.todolists.updateJSONField(id, "name", todo.getName());
        Database.todolists.write(Main.storageDir + "/todolistsKVS.txt");
        return Response.noContent().build();
    }


    @GET
    @Path("{id}/users")
    @Produces(MediaType.APPLICATION_JSON)
    // Gets users associated with that todolist
    public Response getTodoListUsers(@PathParam("id") String id){
        return Response.ok(Database.associations.getMatchingKeys("todolist=" + id + ":user=")
                .stream()
                .map((String s) -> new BriefUser(Database.users.get(s.substring(s.lastIndexOf("=") + 1))))
                .collect(Collectors.toCollection(ArrayList::new))).build();
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}/users")
    // Adds specified users to userlist in todolist
    public Response postTodoListUser(@PathParam("id") String id, User user){
        String userID = Integer.toString(user.getId());
        
        String assoc = "todolist=" + id + ":user=" + userID;
        if (!Database.users.containsKeyString(userID)){
            return Response.status(404, "The user you are trying to add to the todolist does not exist.").build();
        }
        
        Database.associations.put(assoc, new JSONObject());
        Database.associations.write(Main.storageDir + "/associations.txt");
        
        TodoList todo = TodoList.fullConversion(id);
        try{
            URI uri = new URI(todo.getSelfLink("/users/" + userID));
            return Response.created(uri).entity(todo).build();
        }catch(Exception e){
            return Response.status(500, "Error in uri creation").build();
        }
    }

    @DELETE
    @Path("{id}/users/{id2}")
    // Deletes specified users from todolist
    public Response deleteTodoListUser(@PathParam("id") String id, @PathParam("id2") String userID){
        String assoc = "todolist=" + id + ":user=" + userID;
        if (!Database.associations.containsKeyString(assoc))
            return Response.status(404).build();
        Database.associations.remove(assoc);
        Database.associations.write(Main.storageDir + "/associations.txt");
        return Response.noContent().build();
    }

    @GET
    @Path("{id}/project")
    @Produces(MediaType.APPLICATION_JSON)
    // Gets project from todolist
    public Response getTodoListProject(@PathParam("id") String id){
        String projectID = Database.associations.getMatchingKeys("project=\\d+:todolist=" + id).isEmpty() ? "" : Database.associations.getMatchingKeys("project=\\d+:todolist=" + id).get(0);
        if(projectID.equals("")){
            return Response.status(404, "No project was found that was associated with this todolist").build();
        }
        
        return Response.ok(new BriefProject(Database.projects.get(projectID.substring(projectID.indexOf("=") + 1, projectID.indexOf(":"))))).build();
    }

    @PUT
    @Path("{id}/project")
    @Consumes(MediaType.APPLICATION_JSON)
    // Allows for todolist to change projects
    public Response putTodoListProject(@PathParam("id") String id, Project p){
        String projectAssoc = Database.associations.getMatchingKeys("project=\\d+:todolist=" + id).isEmpty() ? "" : Database.associations.getMatchingKeys("project=\\d+:todolist=" + id).get(0);
        if (!projectAssoc.equals("")){
            Database.associations.remove(projectAssoc);
            String assoc = "project=" + p.getId() + ":todolist=" + id;

            Database.associations.put(assoc, new JSONObject());
            Database.associations.write(Main.storageDir + "/associations.txt");
            return Response.noContent().build();
        }
        
        String assoc = "project=" + p.getId() + ":todolist=" + id;

        Database.associations.put(assoc, new JSONObject());
        Database.associations.write(Main.storageDir + "/associations.txt");
        
        try{
            URI uri = new URI(Main.BASE_URI + "todolists/" + id + "/project/" + p.getId());
            return Response.created(uri).build();
        }catch(Exception e){
            return Response.status(500, "Error in uri creation").build();
        }
    }

    @GET
    @Path("{id}/tasks")
    @Produces(MediaType.APPLICATION_JSON)
    // Gets task list from todolist
    public Response getTodoListTasks(@PathParam("id") String id, @DefaultValue("false")@QueryParam("completed") String completed){
        return Response.ok(Database.associations.getMatchingKeys("todolist=" + id + ":task=")
                .stream()
                .map((String s) -> new BriefTask(Database.tasks.get(s.substring(s.lastIndexOf("=") + 1))))
                .filter((BriefTask t) -> completed.equalsIgnoreCase("true") ? t.getStatus().equals("Completed") : !t.getStatus().isEmpty())
                .collect(Collectors.toCollection(ArrayList::new))).build();
    }

    @POST
    @Path("{id}/tasks")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    // Adds specified tasks to todolist
    public Response postTodoListTask(@PathParam("id") String id, Task task){
        String taskID = Integer.toString(task.getId());
        
        String assoc = "todolist=" + id + ":task=" + taskID;
        if (!Database.tasks.containsKeyString(taskID)){
            return Response.status(404, "The task you are trying to add to the todolist does not exist.").build();
        }
        
        Database.associations.put(assoc, new JSONObject());
        Database.associations.write(Main.storageDir + "/associations.txt");
        
        TodoList todo = TodoList.fullConversion(id);
        try{
            URI uri = new URI(todo.getSelfLink("/tasks/" + taskID));
            return Response.created(uri).entity(todo).build();
        }catch(Exception e){
            return Response.status(500, "Error in uri creation").build();
        }
    }

    @DELETE
    @Path("{id}/tasks/{id2}")
    @Consumes(MediaType.APPLICATION_JSON)
    // Deletes specified tasks from todolist
    public Response deleteTodoListTask(@PathParam("id") String id, @PathParam("id2") String taskID){
        String assoc = "todolist=" + id + ":task=" + taskID;
        if (!Database.associations.containsKeyString(assoc))
            return Response.status(404).build();
        Database.associations.remove(assoc);
        Database.associations.write(Main.storageDir + "/associations.txt");
        return Response.noContent().build();
    }
}
