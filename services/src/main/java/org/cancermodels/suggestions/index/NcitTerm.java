package org.cancermodels.suggestions.index;

/**
 * A representation of a NCIt term as it comes from the ncit.obo file.
 */

import java.util.Set;
import lombok.Data;
import org.apache.lucene.document.Document;

@Data
public class NcitTerm {
  private String id;
  private String name;
  private String definition;
  private Set<String> synonyms;
  // This field is not in the file but is used internally in the system so we will calculate it
  private String url;

  public String getUrl() {
    if (id == null)
      return null;
    return "http://purl.obolibrary.org/obo/" + id.replace(":", "_");
  }

}
