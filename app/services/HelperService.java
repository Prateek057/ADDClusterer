package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Configuration;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSAuthScheme;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import static play.mvc.Results.ok;
import static util.StaticFunctions.getStringValueFromSCObject;

public class HelperService {
    WSClient ws;

    static Configuration configuration = Configuration.root();
    public static String SC_BASE_URL = configuration.getString("sc.base.url");
    public static String USER_NAME = configuration.getString("sc.userName");
    public static String PASSWORD = configuration.getString("sc.password");

    public HelperService(WSClient ws) {
        this.ws = ws;
    }

    public CompletionStage<JsonNode> getWSResponseWithAuth(String url) {
        return ws.url(url).setAuth(USER_NAME, PASSWORD, WSAuthScheme.BASIC).get().thenApply(WSResponse::asJson);
    }

    public CompletionStage<JsonNode> getWSResponse(String url) {
        return ws.url(url).get().thenApply(WSResponse::asJson);
    }

    public CompletionStage<JsonNode> postWSRequest(String url, JsonNode json) {
        return ws.url(url).post(json).thenApply(WSResponse::asJson);
    }

    public CompletionStage<JsonNode> entitiesForPath(String path) {
        return getWSResponseWithAuth(path);
    }

    public CompletionStage<JsonNode> entitiesForTypeUid(String typeId) {
        return getWSResponseWithAuth(SC_BASE_URL + "entityTypes/" + typeId + "/entities");
    }

    public CompletionStage<JsonNode> entityForUid(String entityId) {
        return getWSResponseWithAuth(SC_BASE_URL + "entities/" + entityId);
    }

    public CompletionStage<JsonNode> executeMxl(String workspaceId, String expression) {
        String url = SC_BASE_URL + "workspaces/" + workspaceId + "/mxlQuery";
        JsonNode json = Json.newObject().put("expression", expression);
        return postWSRequest(url, json);
    }

    public ArrayNode getSCData(String type_url, List<String> miningAttributes) {
        Map map = new HashMap();
        ArrayNode entityArray = new ArrayNode(new JsonNodeFactory(false));
        miningAttributes.forEach(attribute -> map.put(attribute, new ArrayList<String>()));
        Logger.info("Begin getting data from SC");
        HelperService hs = new HelperService(ws);
        hs.entitiesForPath(type_url + "/entities").thenApply(entityObject -> {
            entityObject.forEach(entity -> {
                this.entityForUid(entity.get("id").asText()).thenApply(e -> {
                    JsonNode entityAttributes = e.get("attributes");
                    ObjectNode entityAttributePairs = new ObjectNode(new JsonNodeFactory(false));
                    for (String miningAttribute : miningAttributes) {
                        String text = "";
                        String textValue = getStringValueFromSCObject(entityAttributes, miningAttribute);
                        if (textValue != null) text = text.concat("," + textValue);
                        if (text != "") ((ArrayList) map.get(miningAttribute)).add(text.replaceAll("class", ""));
                        entityAttributePairs.set(miningAttribute, Json.toJson(text));
                    }
                    entityArray.add(entityAttributePairs);
                    return ok();
                }).toCompletableFuture().join();
            });
            return ok();
        }).toCompletableFuture().join();
        Logger.info("Completed getting data from SC");
        return entityArray;
    }
}
