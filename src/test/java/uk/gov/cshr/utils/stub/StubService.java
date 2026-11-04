package uk.gov.cshr.utils.stub;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Getter
@RequiredArgsConstructor
public class StubService {

    private final CslServiceStubService cslServiceStubService;
    private final IdentityServiceStubService identityServiceStubService;

}
