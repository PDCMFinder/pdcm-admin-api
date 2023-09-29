package org.cancermodels.ontologies;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

@Data
@Component
public class OntologyLoaderConfReader {

  private static final String OLS_BASE_URL = "https://www.ebi.ac.uk/ols4/api/ontologies/ncit/terms/";
  private static final String OBO_LIBRARY_BASE_URL = "http://purl.obolibrary.org/obo/";

  public Map<String, List<String>> getBranchesUrlsToLoad() {
    Map<String, List<String>> urlsMap = new HashMap<>();
    Map<String, List<String>> branches = loadConf();
    for (String key : branches.keySet())
    {
      urlsMap.put(key, new ArrayList<>());
      for (String nctiTerm : branches.get(key)) {
        String url;
        String rawTermUrl = OBO_LIBRARY_BASE_URL + nctiTerm;
        url = buildUrl(rawTermUrl);
        urlsMap.get(key).add(url);
      }
    }
    return urlsMap;
  }

  private Map<String, List<String>> loadConf() {
    Yaml yaml = new Yaml();
    InputStream inputStream = this.getClass()
        .getClassLoader()
        .getResourceAsStream("ontology_loader.yaml");
    Map<String, Map<String, List<String>>> obj = yaml.load(inputStream);
    return obj.get("branches");

  }

  private String buildUrl(String ncitTermUrl) {
    return OLS_BASE_URL + encodeTerm(ncitTermUrl);
  }

  private static String encodeTerm(String termUrl) {
    String encodedUrlSection = URLEncoder.encode(termUrl, StandardCharsets.UTF_8);
    encodedUrlSection = URLEncoder.encode(encodedUrlSection, StandardCharsets.UTF_8);
    return encodedUrlSection;
  }

  public static void main(String[] args) {
    OntologyLoaderConfReader ontologyLoaderConfReader = new OntologyLoaderConfReader();
    ontologyLoaderConfReader.getBranchesUrlsToLoad();
  }
}
