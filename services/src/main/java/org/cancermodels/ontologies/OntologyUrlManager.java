package org.cancermodels.ontologies;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.cancermodels.persistance.UnprocessedOntologyUrl;
import org.springframework.stereotype.Component;

/**
 * This class controls the urls to be used when reloading the ontologies
 */
@Component
public class OntologyUrlManager {

  public static final String EMBEDDED = "_embedded";
  public static final String TERMS = "terms";
  private static final String OLS_BASE_URL = "https://www.ebi.ac.uk/ols/api/ontologies/ncit/terms/";
  private static final String OBO_LIBRARY_BASE_URL = "http://purl.obolibrary.org/obo/";

  private static final String CANCER_TERM = "NCIT_C9305";
  private static final String CANCER_TERM_LABEL = "Cancer";

  // Treatment branches
  private static final String CHEMICAL_MODIFIER_BRANCH_TERM = "NCIT_C1932";
  private static final String DIETARY_SUPPLEMENT_BRANCH_TERM = "NCIT_C1505";
  private static final String DRUG_OR_CHEM_BY_STRUCT_BRANCH_TERM = "NCIT_C1913";
  private static final String INDUSTRIAL_AID_BRANCH_TERM = "NCIT_C45678";
  private static final String PHARMA_SUBSTANCE_BRANCH_TERM = "NCIT_C1909";
  private static final String PHYSIOLOGY_BRANCH_TERM = "NCIT_C1899";
  private static final String HEMATOPOIETIC_BRANCH_TERM = "NCIT_C15431";
  private static final String THERAPEUTIC_PROCEDURES_BRANCH_TERM = "NCIT_C49236";
  private static final String CLINICAL_STUDY_BRANCH_TERM = "NCIT_C15206";
  private static final String GENE_PRODUCT_BRANCH_TERM= "NCIT_C26548";

  // Regimen branches
  private static final String REGIMEN_BRANCH_TERM = "NCIT_C12218";
  private static final String CUSTOM_REGIMEN_BRANCH_TERM = "NCIT_C11197";

  public OntologyUrlManager() {
    ncitTerms = new ArrayList<>();
    initDiagnosisTerm();
    initTreatmentTerms();
    initRegimenTerms();
  }

  private void initDiagnosisTerm() {
    ncitTerms.add(new NcitTerm(CANCER_TERM, CANCER_TERM_LABEL, OntologyTermType.DIAGNOSIS));
  }

  private void initTreatmentTerms() {
    ncitTerms.add(new NcitTerm(
        CHEMICAL_MODIFIER_BRANCH_TERM, "Chemical Modifier Branch", OntologyTermType.TREATMENT));
    ncitTerms.add(new NcitTerm(
        DIETARY_SUPPLEMENT_BRANCH_TERM, "Dietary Supplement Branch", OntologyTermType.TREATMENT));
    ncitTerms.add(new NcitTerm(
        DRUG_OR_CHEM_BY_STRUCT_BRANCH_TERM, "Drug or Chem by Struct Branch", OntologyTermType.TREATMENT));
    ncitTerms.add(new NcitTerm(
        INDUSTRIAL_AID_BRANCH_TERM, "Industrial Aid Branch", OntologyTermType.TREATMENT));
    ncitTerms.add(new NcitTerm(
        PHARMA_SUBSTANCE_BRANCH_TERM, "Pharma Substance Branch", OntologyTermType.TREATMENT));
    ncitTerms.add(new NcitTerm(
        PHYSIOLOGY_BRANCH_TERM, "Physiology Branch", OntologyTermType.TREATMENT));
    ncitTerms.add(new NcitTerm(
        HEMATOPOIETIC_BRANCH_TERM, "Hematopoietic Branch", OntologyTermType.TREATMENT));
    ncitTerms.add(new NcitTerm(
        THERAPEUTIC_PROCEDURES_BRANCH_TERM, "Therapeutic Procedures Branch", OntologyTermType.TREATMENT));
    ncitTerms.add(new NcitTerm(
        CLINICAL_STUDY_BRANCH_TERM, "Clinical Study Branch", OntologyTermType.TREATMENT));
    ncitTerms.add(new NcitTerm(
        GENE_PRODUCT_BRANCH_TERM, "Gene Product Branch", OntologyTermType.TREATMENT));
  }

  private void initRegimenTerms() {
    ncitTerms.add(new NcitTerm(REGIMEN_BRANCH_TERM, "Regimen Branch", OntologyTermType.REGIMEN));
    ncitTerms.add(
        new NcitTerm(CUSTOM_REGIMEN_BRANCH_TERM, "Custom Regimen Branch", OntologyTermType.REGIMEN));
  }

  @Getter
  private static class NcitTerm {

    private final String term;
    private final String description;
    private final OntologyTermType ontologyTermType;

    NcitTerm(String term, String description, OntologyTermType ontologyTermType) {
      this.term = term;
      this.description = description;
      this.ontologyTermType = ontologyTermType;
    }
  }

  private final List<NcitTerm> ncitTerms;

  public List<UnprocessedOntologyUrl> getRootUrls() {

    List<UnprocessedOntologyUrl> unprocessedOntologyUrls = new ArrayList<>();

    for (NcitTerm ncitTerm : ncitTerms) {
      String url;
      String rawTermUrl = OBO_LIBRARY_BASE_URL + ncitTerm.term;
      url = buildUrl(rawTermUrl);

      UnprocessedOntologyUrl unprocessedOntologyUrl = new UnprocessedOntologyUrl();
      unprocessedOntologyUrl.setUrl(url);
      unprocessedOntologyUrl.setNote(ncitTerm.getDescription());
      unprocessedOntologyUrl.setRawTermUrl(rawTermUrl);
      unprocessedOntologyUrl.setType(ncitTerm.ontologyTermType.getDescription());

      unprocessedOntologyUrls.add(unprocessedOntologyUrl);
    }
    return unprocessedOntologyUrls;
  }

  public static String buildUrl(String ncitTermUrl) {
    return OLS_BASE_URL + encodeTerm(ncitTermUrl);
  }

  private static String encodeTerm(String termUrl) {
    String encodedUrlSection = URLEncoder.encode(termUrl, StandardCharsets.UTF_8);
    encodedUrlSection = URLEncoder.encode(encodedUrlSection, StandardCharsets.UTF_8);
    return encodedUrlSection;
  }

}
