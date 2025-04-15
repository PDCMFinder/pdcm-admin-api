package org.cancermodels.admin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.cancermodels.pdcm_admin.EntityTypeName;
import org.cancermodels.mapping_rules.MappingRulesService;
import org.springframework.web.bind.annotation.*;

/**
 * Class to manage all endpoints related to the JSON files containing the mapping rules
 */
@Tag(name = "Mapping Rules", description = "Operations related to the mapping rules")

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/mappings/rules/")
public class MappingRulesController {

  private final MappingRulesService mappingRulesService;

  public MappingRulesController(
      MappingRulesService mappingRulesService) {
    this.mappingRulesService = mappingRulesService;
  }

  /**
   * Generates and returns a ZIP file containing all mapping rules.
   *
   * @param response the HTTP response used to write the ZIP file
   * @throws IOException if an I/O error occurs during writing the ZIP file
   */
  @Operation(
      summary = "Download mapping rules as ZIP",
      description = "Returns a ZIP archive containing all available mapping rules.",
      tags = { "Mapping Rules" }
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "ZIP file successfully downloaded"),
      @ApiResponse(responseCode = "500", description = "Internal server error while generating ZIP")
  })
  @GetMapping(value="/rules-zip", produces="application/zip")
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
   * Deletes all current mapping entities and reloads only mapped data
   * from predefined JSON files. Any mappings in other statuses
   * (Review, Unmapped, Request) will be lost permanently.
   *
   * @throws IOException if an error occurs while reading the JSON files
   */
  @Operation(
      summary = "Restore mapping data from JSON files",
      description = "Deletes all mapping entities and restores only mapped data from JSON files. "
          + "Mappings in 'Review', 'Unmapped', or 'Request' states will be lost.",
      tags = { "Mapping Rules" }
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Mapping data successfully restored from JSON"),
      @ApiResponse(responseCode = "500", description = "Error while restoring data from JSON")
  })
  @PutMapping("/restore-from-jsons")
  public void restoreMappedMappingEntitiesFromJsons() throws IOException {
    mappingRulesService.restoreMappedMappingEntitiesFromJsons();
  }

}
