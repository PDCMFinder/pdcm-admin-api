package org.cancermodels.suggestions.index;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class OboParser {

  public List<NcitTerm> parseOboFile(String oboFilePath) {
    BufferedReader reader;
    List<NcitTerm> terms = new ArrayList<>();
    try {
      reader = new BufferedReader(new FileReader(oboFilePath));
      String line = reader.readLine();

      String id = null;
      String name = null;
      String definition = null;
      Set<String> synonyms = new HashSet<>();
      boolean isObsolete = false;
      int obsolete=0;

      while (line != null) {
        if (line.startsWith("[Term]")) {
          if (isObsolete) {
            obsolete++;
          }
          if (id != null && name != null && !isObsolete) {
            NcitTerm term = new NcitTerm();
            term.setId(id);
            term.setName(name);
            term.setDefinition(definition == null ? "": definition);
            term.setSynonyms(synonyms);
            terms.add(term);

          }
          id = null;
          name = null;
          definition = null;
          isObsolete = false;
          synonyms = new HashSet<>();
        }
        else if (line.startsWith("id:")) {
          id = line.substring(line.indexOf("NCIT:"));
        }
        else if (line.startsWith("name:")) {
          name = line.substring(line.indexOf("name:") + "name:".length() + 1);
        }
        else if (line.startsWith("def:")) {
          String start = "def: ";
          int startIndex = start.length() + 1;
          int endIndex = line.indexOf("\"", startIndex + 1);
          definition = line.substring(startIndex, endIndex);
        }
        else if (line.startsWith("synonym:")) {
          String start = "synonym: ";
          int startIndex = start.length() + 1;
          int endIndex = line.indexOf("\"", startIndex + 1);

          String synonym = line.substring(startIndex, endIndex);
          synonyms.add(synonym);
        }
        else if (line.startsWith("is_obsolete:")) {
          String isObsoleteStr = line.substring(line.indexOf("is_obsolete:") + "is_obsolete:".length() + 1);
          isObsolete = Boolean.parseBoolean(isObsoleteStr);
        }
        // read next line
        line = reader.readLine();
      }
      reader.close();
      System.out.println("obsolete: " + obsolete);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return terms;
  }

}
