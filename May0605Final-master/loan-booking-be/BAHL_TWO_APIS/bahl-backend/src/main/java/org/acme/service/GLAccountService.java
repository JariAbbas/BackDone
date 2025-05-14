// org.acme.service.GLAccountService.java
package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.model.GLAccountCustomerNumbers;
import org.acme.repository.GLAccountRepository;

@ApplicationScoped
public class GLAccountService {

    @Inject
    GLAccountRepository glAccountRepository;

    public GLAccountCustomerNumbers getCustomerNumbers() {
        return glAccountRepository.getCustomerNumbers();
    }
}