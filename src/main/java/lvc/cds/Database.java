package lvc.cds;

import org.json.JSONObject;

public class Database {

    static KVS users = new KVS() {{
        put("count", new JSONObject().put("count", 0));
    }};

    static KVS projects = new KVS() {{
        put("count", new JSONObject().put("count", 0));
    }};

    static KVS todolists = new KVS() {{
        put("count", new JSONObject().put("count", 0));
    }};

    static KVS tasks = new KVS() {{
        put("count", new JSONObject().put("count", 0));
    }};

    static KVS associations = new KVS();
      
}
