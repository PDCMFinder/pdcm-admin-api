package org.cancermodels.mappings;

import lombok.extern.slf4j.Slf4j;
import org.cancer_models.entity2ontology.index.model.IndexingResponse;
import org.cancer_models.entity2ontology.index.service.IndexingRequestService;
import org.cancermodels.pdcm_admin.types.ProcessReportModules;
import org.cancermodels.process_report.ProcessReportService;
import org.cancermodels.process_report.ProcessResponse;
import org.cancermodels.util.FileManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class IndexRequestHandler {

    @Value("${data-dir}")
    private String dataDir;

    @Value("${lucene_index_dir}")
    private String luceneIndexDir;

    @Value("${mapping_path}")
    private String mappingDirName;

    private static final String INDEX_REQUEST_CONFIG_TEMPLATE_FILE = "indexingRequestTemplate.json";

    private final IndexingRequestService indexingRequestService;
    private final ProcessReportService processReportService;

    public IndexRequestHandler(IndexingRequestService indexingRequestService, ProcessReportService processReportService) {
        this.indexingRequestService = indexingRequestService;
        this.processReportService = processReportService;
    }

    public ProcessResponse index() throws IOException {
        String indexRequestConfFilePath = writeIndexingRequestFile();
        IndexingResponse response = indexingRequestService.processRequest(indexRequestConfFilePath);
        ProcessResponse processResponse = formatProcessResponse(response);
        processReportService.register(ProcessReportModules.INDEXER, "Index created", response.end().toString());
        return processResponse;
    }

    private ProcessResponse formatProcessResponse(IndexingResponse response) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("Index created at", response.indexPath());
        result.put("Start", response.start().toString());
        result.put("End", response.end().toString());
        response.indexedElementsPerTarget().forEach((k, v) -> result.put(k, v.toString()));
        return new ProcessResponse(result);
    }

    private String writeIndexingRequestFile() {
        Path configFilePath;
        try {
            String templatePathStr = FileManager.getTmpPathForResource(INDEX_REQUEST_CONFIG_TEMPLATE_FILE);

            Path templatePath = Paths.get(templatePathStr);
            configFilePath = Files.createTempFile("indexRequest", "json");
            String mappingDirPath = dataDir + java.io.File.separator + mappingDirName;

            String content = new String(Files.readAllBytes(templatePath));
            // Set the folder where the index will be created
            content = content.replace("INDEX_PATH", luceneIndexDir);
            // Set the value of the folder where mappings are
            content = content.replace("MAPPING_PATH", mappingDirPath);

            if (configFilePath.toFile().exists())
            {
                boolean deleted = configFilePath.toFile().delete();
                if (deleted) {
                    log.info("Deleted previous config file: {}", configFilePath);
                }
            }
            Files.write(configFilePath, content.getBytes());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return configFilePath.toAbsolutePath().toString();
    }
}
