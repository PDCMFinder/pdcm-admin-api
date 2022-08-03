package org.cancermodels.ontologies;

import static org.cancermodels.ontologies.OntologyUrlManager.EMBEDDED;
import static org.cancermodels.ontologies.OntologyUrlManager.TERMS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.cancermodels.persistance.OntologyLoadReport;
import org.cancermodels.persistance.OntologyTerm;
import org.cancermodels.persistance.UnprocessedOntologyUrl;
import org.cancermodels.util.FileManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class OntologyLoader {
  private final OntologyUrlManager ontologyUrlManager;
  private final UnprocessedOntologyUrlService unprocessedOntologyUrlService;
  private final OntologyTermManager ontologyTermService;
  private final OntologyLoadReporter ontologyLoadReportService;

  private static final Logger LOG = LoggerFactory.getLogger(OntologyLoader.class);
  private final StringBuilder errors = new StringBuilder();

  private final Map<OntologyTermType, Set<OntologyTerm>> toBeSavedTermsByType;

  private final Set<UnprocessedOntologyUrl> unprocessedOntologyUrls = new HashSet<>();

  private static final int MAX_NUMBER_ATTEMPTS = 5;

  public OntologyLoader(
      OntologyUrlManager ontologyUrlManager,
      UnprocessedOntologyUrlService unprocessedOntologyUrlService,
      OntologyTermManager ontologyTermService,
      OntologyLoadReporter ontologyLoadReportService) {

    this.ontologyUrlManager = ontologyUrlManager;
    this.unprocessedOntologyUrlService = unprocessedOntologyUrlService;
    this.ontologyTermService = ontologyTermService;
    this.ontologyLoadReportService = ontologyLoadReportService;

    toBeSavedTermsByType = new HashMap<>();
    initMapOfSet(toBeSavedTermsByType);
  }

  private void initMapOfSet(Map<OntologyTermType, Set<OntologyTerm>> map) {
    map.put(OntologyTermType.DIAGNOSIS, new HashSet<>());
    map.put(OntologyTermType.TREATMENT, new HashSet<>());
    map.put(OntologyTermType.REGIMEN, new HashSet<>());
  }

  public OntologyLoadReport loadOntologies() {
    unprocessedOntologyUrls.addAll(unprocessedOntologyUrlService.findAll());
    long notProcessedRecordsCount = unprocessedOntologyUrls.size();

    if (notProcessedRecordsCount == 0) {
      LOG.info("Starting load from scratch");
      ontologyTermService.deleteAll();
      setInitialUrlsToLoad();
    } else {
      LOG.info(
          "The table with unprocessed urls already had data: "
              + notProcessedRecordsCount
              + "records");
    }
    processUnprocessedUrls();

    saveData();
    resetAttempts();
    return createReport(toBeSavedTermsByType);
  }

  private OntologyLoadReport createReport(Map<OntologyTermType, Set<OntologyTerm>> terms) {
    return ontologyLoadReportService.createReport(
        terms.get(OntologyTermType.DIAGNOSIS).size(),
        terms.get(OntologyTermType.TREATMENT).size(),
        terms.get(OntologyTermType.REGIMEN).size(),
        errors.toString());
  }

  // Set all attempts to zero so in a next execution the urls can be tried again
  private void resetAttempts() {
    List<UnprocessedOntologyUrl> unprocessedOntologyUrls = unprocessedOntologyUrlService.findAll();
    unprocessedOntologyUrls.forEach(x -> {
      x.setAttempts(0);
      unprocessedOntologyUrlService.update(x);
    });
  }

  private void setInitialUrlsToLoad() {
    List<UnprocessedOntologyUrl> urlsToLoad = ontologyUrlManager.getRootUrls();
    urlsToLoad.forEach(this::addUnprocessedUrl);
  }

  private void processUnprocessedUrls() {
    LOG.info("Init processing " + unprocessedOntologyUrls.size() + " urls");
    while (!unprocessedOntologyUrls.isEmpty() && notAllUrlsAreFailing()) {
      LOG.info("Current unprocessed: " + unprocessedOntologyUrls.size());
      UnprocessedOntologyUrl next = unprocessedOntologyUrls.stream().iterator().next();

      if (next.getAttempts() <= MAX_NUMBER_ATTEMPTS) {
        processParentUrl(next);
      } else {
        LOG.error("{} not processed because of max number of attempts: {}", next.getUrl(), next.getAttempts());
      }
    }
  }

  // Check if at least one url exists who hasn't failed more than the allowed number of times
  private boolean notAllUrlsAreFailing() {
    if (unprocessedOntologyUrls.isEmpty()) {
      return true;
    }
    return unprocessedOntologyUrls.stream().anyMatch(x -> x.getAttempts() < MAX_NUMBER_ATTEMPTS);
  }

  private OntologyTerm createOntologyTermFromJson(JSONObject term, OntologyTermType type) {
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
        url, termLabel, type.getDescription(), description.toString(), synonymsSet);
  }

  @Transactional
  void addUnprocessedUrl(UnprocessedOntologyUrl element) {
    unprocessedOntologyUrlService.saveIfNotExists(element);
    unprocessedOntologyUrls.add(element);
  }

  @Transactional
  void deleteUnprocessedUrl(UnprocessedOntologyUrl unprocessedOntologyUrl) {
    unprocessedOntologyUrlService.delete(unprocessedOntologyUrl);
    unprocessedOntologyUrls.remove(unprocessedOntologyUrl);
  }

  public List<OntologyTerm> parseJson(JSONObject job, OntologyTermType type) {
    List<OntologyTerm> ontologyTerms = new ArrayList<>();

    if (!job.has(EMBEDDED)) {
      return ontologyTerms;
    }
    JSONObject json = job.getJSONObject(EMBEDDED);
    JSONArray terms = json.getJSONArray(TERMS);

    for (int i = 0; i < terms.length(); i++) {

      JSONObject term = terms.getJSONObject(i);
      OntologyTerm newTerm = createOntologyTermFromJson(term, type);
      if (newTerm != null) {
        ontologyTerms.add(newTerm);
      }

    }
    return ontologyTerms;
  }

  private void processParentUrl(UnprocessedOntologyUrl unprocessedOntologyUrl) {
    try {

      String jsonString = FileManager.getStringFromUrl(unprocessedOntologyUrl.getUrl());
      JSONObject json = new JSONObject(jsonString);

      boolean hasChildren = json.getBoolean("has_children");
      if (hasChildren) {
        JSONObject links = json.getJSONObject("_links");
        JSONObject hierarchicalDescendants = links.getJSONObject("hierarchicalDescendants");

        String hierarchicalDescendantsUrl = hierarchicalDescendants.get("href") + "?size=200";
        UnprocessedOntologyUrl descendantsUnprocessedOntologyUrl = new UnprocessedOntologyUrl(
            hierarchicalDescendantsUrl, unprocessedOntologyUrl.getType()
        );
        processHierarchicalDescendants(descendantsUnprocessedOntologyUrl);
      }

      OntologyTermType type = OntologyTermType.getTypeByString(unprocessedOntologyUrl.getType());
      OntologyTerm newTerm = createOntologyTermFromJson(json, type);
      toBeSavedTermsByType.get(type).add(newTerm);

      deleteUnprocessedUrl(unprocessedOntologyUrl);

    } catch (IOException e) {
      // There was an error calling OLS, so we keep the url as not processed but store the error
      // message and increase the number of attempts
      String error = e.getClass().getCanonicalName() + ": " + e.getMessage();
      logApiCallError(unprocessedOntologyUrl, error);
    }
  }

  private void logApiCallError(UnprocessedOntologyUrl unprocessedOntologyUrl, String error) {
    LOG.error("failed url " + unprocessedOntologyUrl.getUrl());
    LOG.error(error);
    recordFailedAttempt(unprocessedOntologyUrl, error);
    errors.append(error).append("|");
  }

  @Transactional
  void recordFailedAttempt(UnprocessedOntologyUrl unprocessedOntologyUrl, String message) {
    unprocessedOntologyUrl.setAttempts(unprocessedOntologyUrl.getAttempts() + 1);
    unprocessedOntologyUrl.setErrorMessage(message);
    unprocessedOntologyUrlService.update(unprocessedOntologyUrl);
  }

  /**
   * A hierarchical descendants url gets *all* hierarchical children for a term. The results can be
   * in more than one page so we need to iterate to get all of them. Each term obtained as a result
   * of calling the url will be converted into an OntologyTerm and also stored in the database as an
   * not processed url, so it can be processed later.
   *
   * @param unprocessedOntologyUrl The unprocessed url
   */
  private void processHierarchicalDescendants(UnprocessedOntologyUrl unprocessedOntologyUrl) {
    OntologyTermType type = OntologyTermType.getTypeByString(unprocessedOntologyUrl.getType());

    List<OntologyTerm> ontologyTerms = new ArrayList<>();

    //Start calling it with initial. Then keep until it needs to stop
    String nextUrl = unprocessedOntologyUrl.getUrl();

    String lastUrl = null;

    while (true) {
      try {
        UnprocessedOntologyUrl newUnprocessedOntologyUrl = new UnprocessedOntologyUrl(
            nextUrl, unprocessedOntologyUrl.getType()
        );
        addUnprocessedUrl(newUnprocessedOntologyUrl);

        String jsonString = FileManager.getStringFromUrl(nextUrl);

        deleteUnprocessedUrl(newUnprocessedOntologyUrl);
        JSONObject json = new JSONObject(jsonString);

        ontologyTerms.addAll(parseJson(json, type));
        if (ontologyTerms.size() % 1000 == 0) {
          LOG.info("{} records for {}", ontologyTerms.size(), nextUrl);
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
        String error = e.getClass().getCanonicalName() + ": " + e.getMessage();
        logApiCallError(unprocessedOntologyUrl, error);
      }
    }
    LOG.info("Finished {} with {}", unprocessedOntologyUrl.getUrl(), ontologyTerms.size());
    toBeSavedTermsByType.get(type).addAll(ontologyTerms);
  }

  private void saveData() {
    Set<OntologyTerm> existingOntologyTerms = new HashSet<>(ontologyTermService.getAll());
    Set<OntologyTerm> termsToSave = new HashSet<>();

    for ( OntologyTermType type : toBeSavedTermsByType.keySet()) {
      Set<OntologyTerm> termsByType = toBeSavedTermsByType.get(type);
      LOG.info("{} terms: {}", type, termsByType.size());
      termsToSave.addAll(termsByType);
    }
    termsToSave.removeAll(existingOntologyTerms);
    ontologyTermService.saveOntologyTerms(termsToSave);
  }
}
