package org.cancermodels.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.cancermodels.input_data.InputDataUpdaterService;
import org.cancermodels.process_report.ProcessResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller responsible for synchronizing data from the repository.
 * <p>
 * This controller provides endpoints to fetch mapping rules and metadata
 * from the GitLab data repository. The retrieved data is stored internally
 * and is used by the indexing process and the ontology mapping detection process.
 * </p>
 * <p>
 * The synchronized data ensures that:
 * <ul>
 *   <li>The indexer has up-to-date configuration and rules.</li>
 *   <li>The mapping detection process can identify new mappings.</li>
 * </ul>
 * </p>
 *
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/repository-sync")
@Tag(name = "Repository Sync", description = "Endpoints for syncing mapping rules and metadata from the data repository")
public class RepositorySyncController {

    private final InputDataUpdaterService inputDataUpdaterService;

    public RepositorySyncController(
        InputDataUpdaterService inputDataDownloaderService) {
        this.inputDataUpdaterService = inputDataDownloaderService;
    }


    /**
     * Synchronizes mapping rules and metadata from the repository.
     * <p>
     * This endpoint retrieves the latest mapping rules and metadata from the
     * GitLab repository and stores them internally. The data is not directly
     * exposed to users but is utilized by the indexing and mapping processes.
     * </p>
     *
     * @return {@link ProcessResponse} indicating the success or failure of the synchronization.
     */
    @Operation(
        summary = "Sync mapping rules and metadata",
        description = "Fetches the latest mapping rules and metadata from the GitLab repository. "
            + "This ensures that the indexing process and the mapping detection process "
            + "work with the most up-to-date data. The data is stored internally and "
            + "is not directly accessible to users."
    )
    @PostMapping
    public ProcessResponse updateInputData() {
        return inputDataUpdaterService.updateInputData();
    }
}
