package org.cancermodels.ontologies;

import java.util.List;
import org.cancermodels.persistance.UnprocessedOntologyUrl;
import org.cancermodels.persistance.UnprocessedOntologyUrlRepository;
import org.springframework.stereotype.Service;

@Service
public class UnprocessedOntologyUrlService {
  private final UnprocessedOntologyUrlRepository unprocessedOntologyUrlRepository;

  public UnprocessedOntologyUrlService(
      UnprocessedOntologyUrlRepository unprocessedOntologyRepository) {
    this.unprocessedOntologyUrlRepository = unprocessedOntologyRepository;
  }

  public void update(UnprocessedOntologyUrl unprocessedOntologyUrl) {
    unprocessedOntologyUrlRepository.save(unprocessedOntologyUrl);
  }

  public void saveIfNotExists(UnprocessedOntologyUrl unprocessedOntologyUrl) {
    if (unprocessedOntologyUrlRepository.findByUrl(unprocessedOntologyUrl.getUrl()) == null) {
      unprocessedOntologyUrlRepository.save(unprocessedOntologyUrl);
    }
  }

  public void delete(UnprocessedOntologyUrl unprocessedOntologyUrl) {
    unprocessedOntologyUrlRepository.delete(unprocessedOntologyUrl);
  }

  public List<UnprocessedOntologyUrl> findAll() {
    return unprocessedOntologyUrlRepository.findAll();
  }
}
