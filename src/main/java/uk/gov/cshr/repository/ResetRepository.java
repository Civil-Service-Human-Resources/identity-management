package uk.gov.cshr.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Reset;

import java.util.Optional;

@Repository
public interface ResetRepository extends CrudRepository<Reset, Long> {
    void deleteByEmail(String email);
}
