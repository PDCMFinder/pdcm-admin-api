package org.cancermodels.releases;

import lombok.extern.slf4j.Slf4j;
import org.cancermodels.pdcm_admin.persistance.Release;
import org.cancermodels.pdcm_admin.persistance.ReleaseRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class ReleaseService {
    private final ReleaseRepository releaseRepository;

    public ReleaseService(ReleaseRepository releaseRepository) {
        this.releaseRepository = releaseRepository;
    }

    public Release getReleaseByIdOrFail(long id) {
        Optional<Release> releaseOpt = releaseRepository.findById(id);
        if (releaseOpt.isEmpty()) {
            throw new IllegalArgumentException("Release " + id + " does not exist.");
        }
        return releaseOpt.get();
    }

    public List<Release> getAllReleases() {
        return releaseRepository.findAll();
    }

    public Release save(Release release) {
        return releaseRepository.save(release);
    }

    public Optional<Release> findByNameAndDate(String name, LocalDateTime date) {
        return releaseRepository.findByNameAndDate(name, date);
    }
}