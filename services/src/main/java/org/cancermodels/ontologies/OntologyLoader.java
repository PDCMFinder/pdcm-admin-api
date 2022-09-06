package org.cancermodels.ontologies;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.cancermodels.persistance.OntologyLoadReport;
import org.cancermodels.persistance.OntologyTerm;
import org.cancermodels.util.FileManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OntologyLoader {

  private final OntologyTermManager ontologyTermService;
  private final OntologyLoaderConfReader ontologyLoaderConfReader;
  private final OntologyLoadReporter ontologyLoadReportService;
  private final Map<String, OntologyTerm> processed = new HashMap<>();
  private final Set<OntologyTerm> termsToSave = new HashSet<>();
  private String error;

  public static final String EMBEDDED = "_embedded";
  public static final String TERMS = "terms";

  public OntologyLoader(
      OntologyTermManager ontologyTermService,
      OntologyLoaderConfReader ontologyLoaderConfReader,
      OntologyLoadReporter ontologyLoadReportService) {
    this.ontologyTermService = ontologyTermService;
    this.ontologyLoaderConfReader = ontologyLoaderConfReader;
    this.ontologyLoadReportService = ontologyLoadReportService;
  }

  /**
   * Loads ontology terms from OLS. It does it by going to specific branches and fetching all the
   * descendants.
   */
  public OntologyLoadReport loadOntologies() {
    Map<String, List<String>> branchUrls = ontologyLoaderConfReader.getBranchesUrlsToLoad();
    for (String ontologyType : branchUrls.keySet()) {
      List<String> urlsByType = branchUrls.get(ontologyType);
      for (String url : urlsByType) {
        processBranchUrl(url, ontologyType);
      }
    }
    ontologyTermService.deleteAll();
    System.out.println(processed.size());
    processed.forEach((k,v) -> termsToSave.add(v));
    ontologyTermService.saveOntologyTerms(termsToSave);
    return createReport();
  }

  private OntologyLoadReport createReport() {
    int diagnosisCount = 0;
    int treatmentCount = 0;
    int regimenCount = 0;
    for (OntologyTerm ontologyTerm : termsToSave) {
      if (ontologyTerm.getType().equalsIgnoreCase(OntologyTermType.DIAGNOSIS.getDescription())) {
        diagnosisCount++;
      } else if (ontologyTerm.getType().equalsIgnoreCase(OntologyTermType.TREATMENT.getDescription())) {
        treatmentCount++;
      } else if (ontologyTerm.getType().equalsIgnoreCase(OntologyTermType.REGIMEN.getDescription())) {
        regimenCount++;
      }
    }
    return ontologyLoadReportService.createReport(
        diagnosisCount,
        treatmentCount,
        regimenCount,
        error);
  }

  private void processBranchUrl(String url, String ontologyType) {
    log.info("Processing branch url " + url + " type: " + ontologyType);
    try {
      String jsonString = FileManager.getStringFromUrl(url);
      JSONObject json = new JSONObject(jsonString);
      OntologyTerm newTerm = createOntologyTermFromJson(json, ontologyType);
      processed.put(newTerm.getUrl(), newTerm);

      if (json.getBoolean("has_children")) {
        JSONObject links = json.getJSONObject("_links");
        JSONObject hierarchicalDescendants = links.getJSONObject("hierarchicalDescendants");

        String descendantsUrl = hierarchicalDescendants.get("href") + "?size=200";
        getHierarchicalDescendants(descendantsUrl, ontologyType);
      }
    } catch (IOException ioException) {
      ioException.printStackTrace();
    }
  }

  // Calls the url with to fetch the descendants. The call is paginated, so multiple calls might
  // be needed
  private void getHierarchicalDescendants(String descendantsUrl, String ontologyType) {

    List<OntologyTerm> ontologyTerms = new ArrayList<>();

    //Start calling it with initial. Then keep until it needs to stop
    String nextUrl = descendantsUrl;

    String lastUrl = null;

    while (true) {
      try {

        String jsonString = FileManager.getStringFromUrl(nextUrl);
        JSONObject json = new JSONObject(jsonString);

        ontologyTerms.addAll(parseDescendantsResponseJson(json, ontologyType));

        if (ontologyTerms.size() % 1000 == 0) {
          log.info("{} records for {}", ontologyTerms.size(), nextUrl);
        }
        JSONObject links = json.getJSONObject("_links");

        // Get last url if not yet defined
        if (lastUrl == null) {
          if (links.has("last")) {
            JSONObject lastUrlObject = links.getJSONObject("last");
            lastUrl = lastUrlObject.getString("href");
          }
          // If no last, there are no more pages so we can stop
          else {
            break;
          }
        }

        if (nextUrl.equals(lastUrl)) {
          break;
        }

        JSONObject nextUrlObject = links.getJSONObject("next");
        nextUrl = nextUrlObject.getString("href");
      } catch (IOException e) {
        error = e.getClass().getCanonicalName() + ": " + e.getMessage();
        log.error(error);
      }
    }
    ontologyTerms.forEach(x -> processed.put(x.getUrl(), x));
  }

  public List<OntologyTerm> parseDescendantsResponseJson(JSONObject job, String ontologyType) {
    List<OntologyTerm> ontologyTerms = new ArrayList<>();

    if (!job.has(EMBEDDED)) {
      return ontologyTerms;
    }
    JSONObject json = job.getJSONObject(EMBEDDED);
    JSONArray terms = json.getJSONArray(TERMS);

    for (int i = 0; i < terms.length(); i++) {

      JSONObject term = terms.getJSONObject(i);
      OntologyTerm newTerm = createOntologyTermFromJson(term, ontologyType);
      if (newTerm != null) {
        ontologyTerms.add(newTerm);
      }
    }
    return ontologyTerms;
  }

  private OntologyTerm createOntologyTermFromJson(JSONObject term, String ontologyType) {
    String url = term.getString("iri");

    String termLabel = term.getString("label");
    termLabel = termLabel.replaceAll(",", "");

    JSONArray synonyms = term.getJSONArray("synonyms");
    Set<String> synonymsSet = new HashSet<>();

    for (int j = 0; j < synonyms.length(); j++) {
      synonymsSet.add(synonyms.getString(j).toLowerCase());
    }

    StringBuilder description = new StringBuilder();

    if (term.has("description")) {
      try {
        JSONArray descriptions = term.getJSONArray("description");

        for (int j = 0; j < descriptions.length(); j++) {
          description.append(descriptions.getString(j));
        }
      } catch (Exception e) {
        description = new StringBuilder(term.getString("description"));
      }
    }

    return ontologyTermService.createOntologyTerm(
        url, termLabel, ontologyType, description.toString(), synonymsSet);
  }

}
