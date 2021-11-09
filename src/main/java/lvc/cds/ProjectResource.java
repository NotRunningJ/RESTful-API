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

import lvc.cds.briefobjects.BriefTodolist;
import lvc.cds.briefobjects.BriefUser;

// Written by Jake.

@Path("/projects")
public class ProjectResource {

    @Context
    UriInfo uriInfo;

    // Gets all the projects.
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProjects() {
        ArrayList<String> projectIDs = Database.projects.getMatchingKeys(".*");
        ArrayList<Project> projects = new ArrayList<>();
        for (String string : projectIDs) {
            if(!string.equals("count"))
            projects.add(Project.fullConversion(string));
        }

        return Response.ok(projects).build();
    }

    // Create a new project.
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNewProject(Project p) {
        UriBuilder uriBuilder = uriInfo.getAbsolutePathBuilder();
        int count = Database.projects.get("count").getInt("count");
        URI uri = uriBuilder.path("" + count).build();

        p.setId(count);
        p.setSelfLink();
        p.setProjectLinks();

        JSONObject projectJSON = new JSONObject();
        projectJSON.put("id", p.getId());
        projectJSON.put("name", p.getName());
        projectJSON.put("description", p.getDescription());
        projectJSON.put("selfLink", p.getSelfLink());


        Database.projects.put(Integer.toString(p.getId()), projectJSON);
        count++;
        Database.projects.updateJSONField("count", "count", count);
        Database.projects.write(Main.storageDir + "/projectsKVS.txt");

        return Response.created(uri).entity(p).build();
    }

    // Get the specified project
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProject(@PathParam("id") String id) {
        if (!Database.projects.containsKeyString(id))
            return Response.status(404).build();

        Project p = Project.fullConversion(id);
        return Response.ok(p).build();
    }

    // Update the name of the project
    @PUT
    @Path("{id}/name")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeName(@PathParam("id") String id, Project p) {
        if (!Database.projects.containsKeyString(id))
            return Response.status(404).build();

        Database.projects.updateJSONField(id, "name", p.getName());
        Database.projects.write(Main.storageDir + "/projectsKVS.txt");

        return Response.ok().build();
    }

    // Get the name of the project
    @GET
    @Path("{id}/name")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getName(@PathParam("id") String id) {
        if (!Database.projects.containsKeyString(id))
            return Response.status(404).build();

        String name = (String) Database.projects.getJSON(id, "name");
        JSONObject json = new JSONObject().put("name", name);
        return Response.ok(json.toString()).build();
    }

    // Update the description of the project
    @PUT
    @Path("{id}/description")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeDescription(@PathParam("id") String id, Project p) {
        if (!Database.projects.containsKeyString(id))
            return Response.status(404).build();

        Database.projects.updateJSONField(id, "description", p.getDescription());
        Database.projects.write(Main.storageDir + "/projectsKVS.txt");

        return Response.ok().build();
    }

    // Get the description of the project
    @GET
    @Path("{id}/description")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDescription(@PathParam("id") String id) {
        if (!Database.projects.containsKeyString(id))
            return Response.status(404).build();

        String description = (String) Database.projects.getJSON(id, "description");
        JSONObject json = new JSONObject().put("description", description);
        return Response.ok(json.toString()).build();
    }

    // Get the users associated with this project.
    @GET
    @Path("{id}/users")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers(@PathParam("id") String id) {
        if (!Database.projects.containsKeyString(id))
            return Response.status(404).build();
        ArrayList<String> userAssocs = Database.associations.getMatchingKeys("project=" + id + ":user=");
        ArrayList<BriefUser> users = new ArrayList<>();
        for (String string : userAssocs) {
            String uid = string.substring(string.lastIndexOf("=") + 1);
            String name = (String) Database.users.getJSON(uid, "name");
            String uri = Main.BASE_URI + "users/" + uid;
            users.add(new BriefUser(name, uri));
        }

        return Response.ok(users).build();
    }



    // Add a user to the project.
    // TODO: should this return the whole project showing the user in it or just the
    // condensed user object?
    @POST
    @Path("{id}/users")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUser(@PathParam("id") String id, User u) {
        String uid = Integer.toString(u.getId());
        if (!Database.users.containsKeyString(uid))
            return Response.status(404).header("reason", "The user being added does not exist.").build();
        
        // Create association string and add it to associations KVS
        String assoc = "project=" + id + ":user=" + uid;
        Database.associations.put(assoc, new JSONObject());
        Database.associations.write(Main.storageDir + "/associations.txt");

        // Construct a project represenation and show it in the response body.
        JSONObject userJSON = Database.users.get(uid);
        String name = userJSON.getString("name");
        String selfLink = userJSON.getString("selfLink");
        URI uri;
        try {
            uri = new URI(Main.BASE_URI + "projects/" + id + "/users/" + uid);
        } catch (URISyntaxException e) {
            return Response.status(400).build();
        }
        return Response.created(uri).entity(new BriefUser(name, selfLink)).build();
    }

    // Remove a user from a project
    @DELETE
    @Path("{id}/users/{userID}")
    public Response removeUser(@PathParam("id") String id, @PathParam("userID") String userID) {
        String assoc = "project=" + id + ":user=" + userID;
        if (!Database.associations.containsKeyString(assoc))
            return Response.status(404).build();
        Database.associations.remove(assoc);
        Database.associations.write(Main.storageDir + "/associations.txt");
        return Response.noContent().build();
    }

    // Add or update the todolist associated with the project
    @PUT
    @Path("{id}/todolist")
    public Response updateTodoList(@PathParam("id") String id, TodoList tl) {
        // Confirm that both the project and the todolist exist.
        String tlid = tl.getId();
        if (!Database.todolists.containsKeyString(tlid))
            return Response.status(404).header("reason", "The todolist does not exist.").build();
        if (!Database.projects.containsKeyString(id))
            return Response.status(404).header("reason", "The project does not exist.").build();

        String assoc = "project=" + id + ":todolist=" + tlid;
        boolean alreadyExists = !Database.associations.containsKeyString(assoc) ? false : true;
        
        Database.associations.put("project=" + id + ":todolist=" + tlid, new JSONObject());
        Database.associations.write(Main.storageDir + "/associations.txt");
        if (alreadyExists) {
            URI uri;
            try {
                uri = new URI(Database.projects.getJSON(id, "selfLink") + "/todolist/");
            } catch (URISyntaxException e) {
                return Response.status(404).build();
            }
            return Response.created(uri).build();
        } else {
            return Response.ok().build();
        }
    }

    // Get the project's todolist
    @GET
    @Path("{id}/todolist")
    public Response getTodoList(@PathParam("id") String id) {
        ArrayList<String> tlAssocs = Database.associations.getMatchingKeys("project=" + id + ":todolist=");
        if(tlAssocs.isEmpty()) {
            return Response.status(404).header("reason", "The project does not have a todolist").build();
        }
        String tlid = tlAssocs.get(0).substring(tlAssocs.get(0).lastIndexOf("=") + 1);
        return Response.ok(new BriefTodolist(Database.todolists.get(tlid))).build();
    }

    // Delete a todolist from a project
    // TODO: do we want this to delete the entire todolist or just the association?
    @DELETE
    @Path("{id}/todolist")
    public Response deleteTodolist(@PathParam("id") String id) {
        ArrayList<String> assocs = Database.associations.getMatchingKeys("project=" + id + ":todolist=");
        if (assocs.isEmpty())
            return Response.status(404).build();
        String assoc = assocs.get(0);
        Database.associations.remove(assoc);
        Database.associations.write(Main.storageDir + "/associations.txt");
        return Response.noContent().build();
    }

    // Delete the project with the associated ID.
    @DELETE
    @Path("{id}")
    public Response deleteProject(@PathParam("id") String id) {
        Database.projects.remove(id);
        Database.projects.write(Main.storageDir + "/projectsKVS.txt");
        ArrayList<String> projectAssociations = Database.associations.getMatchingKeys("project=" + id);

        for (String string : projectAssociations) {
            Database.associations.remove(string);
        }

        Database.associations.write(Main.storageDir + "/associations.txt");

        return Response.noContent().build();
    }

}
