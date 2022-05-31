package org.cancermodels.util;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

public class JSONHelper {

  public static Map<String, String> JSONObjectToStringMap(JSONObject jsonObject) {
    Map<String, String> map = new HashMap<>();
    Map<String, Object> keys = jsonObject.toMap();

    for (String key : keys.keySet()) {
      map.put(key, keys.get(key).toString());
    }
    return map;
  }
}
