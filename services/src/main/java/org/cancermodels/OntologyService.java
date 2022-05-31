package org.cancermodels;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.transaction.Transactional;
import org.cancermodels.util.FileManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Class that gets the ontology terms and synonyms from OLS for Treatments, Regimens and Diagnosis
 */
@Service
public class OntologyService {

  private static final Logger LOG = LoggerFactory.getLogger(OntologyService.class);

  private static final String EMBEDDED = "_embedded";
  private static final String TERMS = "terms";
  private static final String OLS_BASE_URL = "https://www.ebi.ac.uk/ols/api/ontologies/ncit/terms/";

  private static final String CANCER_TERM_URL = "http://purl.obolibrary.org/obo/NCIT_C9305";
  private static final String CANCER_TERM_LABEL = "Cancer";

  // Treatment branches
  private static final String CHEMICAL_MODIFIER_BRANCH_URL = "http://purl.obolibrary.org/obo/NCIT_C1932";
  private static final String DIETARY_SUPPLEMENT_BRANCH_URL = "http://purl.obolibrary.org/obo/NCIT_C1505";
  private static final String DRUG_OR_CHEM_BY_STRUCT_BRANCH_URL = "http://purl.obolibrary.org/obo/NCIT_C1913";
  private static final String INDUSTRIAL_AID_BRANCH_URL = "http://purl.obolibrary.org/obo/NCIT_C45678";
  private static final String PHARMA_SUBSTANCE_BRANCH_URL = "http://purl.obolibrary.org/obo/NCIT_C1909";
  private static final String PHYSIOLOGY_BRANCH_URL = "http://purl.obolibrary.org/obo/NCIT_C1899";
  private static final String HEMATOPOIETIC_BRANCH_URL = "http://purl.obolibrary.org/obo/NCIT_C15431";
  private static final String THERAPEUTIC_PROCEDURES_BRANCH_URL = "http://purl.obolibrary.org/obo/NCIT_C49236";
  private static final String CLINICAL_STUDY_BRANCH_URL = "http://purl.obolibrary.org/obo/NCIT_C15206";
  private static final String GENE_PRODUCT_BRANCH_URL = "http://purl.obolibrary.org/obo/NCIT_C26548";

  // Regimen branches
  private static final String REGIMEN_BRANCH_URL = "http://purl.obolibrary.org/obo/NCIT_C12218";
  private static final String CUSTOM_REGIMEN_BRANCH_URL = "http://purl.obolibrary.org/obo/NCIT_C11197";

  // Ontology types
  private static final String DIAGNOSIS_ONTOLOGY_TYPE = "diagnosis";
  private static final String TREATMENT_ONTOLOGY_TYPE = "treatment";
  private static final String REGIMEN_ONTOLOGY_TYPE = "regimen";

  private Set<String> visitedTerms;
  private Set<OntologyTerm> toBeSavedTerms;
  private Set<OntologyTerm> discoveredTerms;
  private Set<String> failedUrls;


  private final OntologyTermRepository ontologyTermRepository;

  public OntologyService(OntologyTermRepository ontologyTermRepository) {
    this.ontologyTermRepository = ontologyTermRepository;
    resetCollections();
  }

  private void resetCollections() {
    // Reset collections and counters for each ontology type to load
    visitedTerms = new HashSet<>();
    toBeSavedTerms = new HashSet<>();
    discoveredTerms = new HashSet<>();
    failedUrls = new HashSet<>();
  }

  @Transactional
  public void reloadDiagnosisTerms() {
    resetCollections();
    ontologyTermRepository.deleteAllByType(DIAGNOSIS_ONTOLOGY_TYPE);
    fetchDiagnosisTerms();
    saveOntologyTerms();
    LOG.info("End reloadDiagnosisTerms");
  }

  @Transactional
  public void reloadTreatmentTerms() {
    resetCollections();
    ontologyTermRepository.deleteAllByType(TREATMENT_ONTOLOGY_TYPE);
    ontologyTermRepository.deleteAllByType(REGIMEN_ONTOLOGY_TYPE);
    fetchTreatmentsTerms();
    saveOntologyTerms();
    LOG.info("End reloadTreatmentTerms");
  }

  private void fetchTreatmentsTerms() {
    LOG.info("Querying treatment and regimen branches in OLS");
    fetchTreatmentTermsFromBranch(TREATMENT_ONTOLOGY_TYPE, CHEMICAL_MODIFIER_BRANCH_URL);
    fetchTreatmentTermsFromBranch(TREATMENT_ONTOLOGY_TYPE, DIETARY_SUPPLEMENT_BRANCH_URL);
    fetchTreatmentTermsFromBranch(TREATMENT_ONTOLOGY_TYPE, DRUG_OR_CHEM_BY_STRUCT_BRANCH_URL);
    fetchTreatmentTermsFromBranch(TREATMENT_ONTOLOGY_TYPE, INDUSTRIAL_AID_BRANCH_URL);
    fetchTreatmentTermsFromBranch(TREATMENT_ONTOLOGY_TYPE, PHARMA_SUBSTANCE_BRANCH_URL);
    fetchTreatmentTermsFromBranch(TREATMENT_ONTOLOGY_TYPE, PHYSIOLOGY_BRANCH_URL);
    fetchTreatmentTermsFromBranch(TREATMENT_ONTOLOGY_TYPE, HEMATOPOIETIC_BRANCH_URL);
    fetchTreatmentTermsFromBranch(TREATMENT_ONTOLOGY_TYPE, THERAPEUTIC_PROCEDURES_BRANCH_URL);
    fetchTreatmentTermsFromBranch(TREATMENT_ONTOLOGY_TYPE, CLINICAL_STUDY_BRANCH_URL);
    fetchTreatmentTermsFromBranch(TREATMENT_ONTOLOGY_TYPE, GENE_PRODUCT_BRANCH_URL);
    fetchTreatmentTermsFromBranch(REGIMEN_ONTOLOGY_TYPE, REGIMEN_BRANCH_URL);
    fetchTreatmentTermsFromBranch(REGIMEN_ONTOLOGY_TYPE, CUSTOM_REGIMEN_BRANCH_URL);
  }

  private void fetchDiagnosisTerms() {
    LOG.info("Getting diagnosis terms");

    // Root for all the diagnosis terms
    OntologyTerm cancerTerm = new OntologyTerm(
        CANCER_TERM_URL, CANCER_TERM_LABEL,DIAGNOSIS_ONTOLOGY_TYPE);

    discoveredTerms.add(cancerTerm);
    toBeSavedTerms.add(cancerTerm);

    while (!discoveredTerms.isEmpty()) {
      OntologyTerm notYetVisitedTerm = discoveredTerms.iterator().next();
      discoveredTerms.remove(notYetVisitedTerm);

      if (visitedTerms.contains(notYetVisitedTerm.getUrl())) {
        continue;
      }

      String parentUrlEncoded = "";
      //have to double encode the url to get the desired result
      parentUrlEncoded = URLEncoder.encode(notYetVisitedTerm.getUrl(),
          StandardCharsets.UTF_8);
      parentUrlEncoded = URLEncoder.encode(parentUrlEncoded, StandardCharsets.UTF_8);

      String url = OLS_BASE_URL + parentUrlEncoded + "/hierarchicalChildren?size=200";
      String jsonString;

      try {
        jsonString = FileManager.getStringFromUrl(url);
        JSONObject json = new JSONObject(jsonString);

        parseHierarchicalChildren(json, "diagnosis");

        visitedTerms.add(notYetVisitedTerm.getUrl());

      } catch (IOException e) {
        LOG.error("failed url " + url);
        LOG.error(e.getMessage());
        failedUrls.add(url);
      }
    }

  }


  public void fetchTreatmentTermsFromBranch(String type, String branchRootUrl) {

    int totalPages = 0;
    boolean totalPagesDetermined = false;

    for (int currentPage = 0; currentPage <= totalPages; currentPage++) {

      String encodedTermUrl = "";
      //have to double encode the url to get the desired result
      try {
        encodedTermUrl = URLEncoder.encode(branchRootUrl, "UTF-8");
        encodedTermUrl = URLEncoder.encode(encodedTermUrl, "UTF-8");

      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }

      String url = OLS_BASE_URL + encodedTermUrl + "/hierarchicalDescendants?size=200&page=" + currentPage;

      String jsonString = null;
      try {
        jsonString = FileManager.getStringFromUrl(url);
        JSONObject json = new JSONObject(jsonString);
        parseHierarchicalChildren(json, type);

        if (!totalPagesDetermined) {
          JSONObject pageObj = json.getJSONObject("page");
          totalPages = pageObj.getInt("totalPages") - 1;
          totalPagesDetermined = true;
        }
      } catch (IOException e) {
        LOG.error("failed url " + url);
        LOG.error(e.getMessage());
        failedUrls.add(url);
      }
    }
  }

  public void parseHierarchicalChildren(JSONObject job, String type) {

    if (!job.has(EMBEDDED)) {
      return;
    }
    JSONObject job2 = job.getJSONObject(EMBEDDED);
    JSONArray hierarchicalChildren = job2.getJSONArray(TERMS);

    for (int i = 0; i < hierarchicalChildren.length(); i++) {

      JSONObject term = hierarchicalChildren.getJSONObject(i);
      OntologyTerm newTerm = null;
      newTerm = createOntologyTerm(term, type);

      if (newTerm != null) {
        toBeSavedTerms.add(newTerm);
        discoveredTerms.add(newTerm);
        if (toBeSavedTerms.size() % 500 == 0) {
          LOG.info("Loaded {} terms", toBeSavedTerms.size());
        }
      }
    }
  }


  private OntologyTerm createOntologyTerm(JSONObject term, String type){
    String url = term.getString("iri");
    if (visitedTerms.contains(url)) {
      return null;
    }

    String termLabel = term.getString("label");

    String updatedTermLabel = null;
    if (DIAGNOSIS_ONTOLOGY_TYPE.equalsIgnoreCase(type)) {
      updatedTermLabel = updateTermLabel(termLabel);
    }

    termLabel = termLabel.replaceAll(",", "");

    OntologyTerm newTerm = new OntologyTerm(
        url, updatedTermLabel != null ? updatedTermLabel : termLabel, type);

    JSONArray synonyms = term.getJSONArray("synonyms");
    Set<String> synonymsSet = new HashSet<>();

    for (int j=0; j < synonyms.length(); j++) {
      synonymsSet.add(synonyms.getString(j).toLowerCase());
    }
    newTerm.setSynonyms(new ArrayList<>(synonymsSet));

    StringBuilder description = new StringBuilder();

    if(term.has("description")){
      try {
        JSONArray descriptions = term.getJSONArray("description");

        for (int j = 0; j < descriptions.length(); j++) {
          description.append(descriptions.getString(j));
        }
      }
      catch(Exception e){
        description = new StringBuilder(term.getString("description"));
      }
    }

    newTerm.setDescription(description.toString());
    return newTerm;
  }


  private String updateTermLabel(String termLabel){

    // Changes Malignant * Neoplasm to * Cancer
    String pattern = "(.*)Malignant(.*)Neoplasm(.*)";
    String updatedTermlabel = null;

    if (termLabel.matches(pattern)) {
      updatedTermlabel = (termLabel.replaceAll(pattern, "\t$1$2Cancer$3")).trim();
      LOG.trace("Replacing term label '{}' with '{}'", termLabel, updatedTermlabel);
    }
    return updatedTermlabel;
  }

  public void saveOntologyTerms(){
    LOG.info("Saving {} ontology terms to db", toBeSavedTerms.size());
    ontologyTermRepository.saveAll(toBeSavedTerms);
  }

}
