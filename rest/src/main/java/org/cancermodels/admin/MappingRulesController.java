package org.cancermodels.admin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.cancermodels.pdcm_admin.EntityTypeName;
import org.cancermodels.mapping_rules.MappingRulesService;
import org.springframework.web.bind.annotation.*;

/**
 * Class to manage all endpoints related to the JSON files containing the mapping rules
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/mappings/rules/")
public class MappingRulesController {

  private final MappingRulesService mappingRulesService;

  public MappingRulesController(
      MappingRulesService mappingRulesService) {
    this.mappingRulesService = mappingRulesService;
  }

  @GetMapping(value="/mappingRules", produces="application/zip")
  public void getZipOfMappingRules(HttpServletResponse response) throws IOException {

    //setting headers
    response.setStatus(HttpServletResponse.SC_OK);
    String fileName = "mappingRules_" + new SimpleDateFormat("yyyyMMddHHmm'.zip'").format(new Date());
    response.addHeader("Content-Disposition", "attachment; filename=\""+fileName+"\"");

    ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());

    for (EntityTypeName entityType : EntityTypeName.values()) {
      String entityTypeName = entityType.getLabel().toLowerCase();
      String mappingsFileName = entityTypeName + "_mappings.json";
      String json = mappingRulesService.buildMappingRulesJson(entityTypeName);
      zipOutputStream.putNextEntry(new ZipEntry(mappingsFileName));
      InputStream contentStream = new ByteArrayInputStream(json.getBytes());
      IOUtils.copy(contentStream, zipOutputStream);
      zipOutputStream.closeEntry();
    }

    zipOutputStream.close();
  }

  /**
   * Deletes all the mapping entities and reload the data from the json files with the mapping rules.
   * Because the json files contain only Mapped data, any mappings in other status
   * (Review, Unmapped, Request) will be lost.
   */
  @PutMapping("/restoreMappedMappingEntitiesFromJsons")
  public void getSimilar() throws IOException {
    mappingRulesService.restoreMappedMappingEntitiesFromJsons();
  }

}
