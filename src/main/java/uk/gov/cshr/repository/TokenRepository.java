package uk.gov.cshr.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.domain.Token;
import uk.gov.cshr.domain.TokenStatus;

import java.util.Collection;

@Repository
public interface TokenRepository extends CrudRepository<Token, Long> {
    void deleteByUserName(String userName);
}
