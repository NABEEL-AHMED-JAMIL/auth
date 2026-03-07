package com.barco.auth.security;

import com.barco.common.security.session.EtlAccountSessionDetail;
import com.barco.common.security.session.OrganizationDetail;
import com.barco.common.utility.BarcoUtil;
import com.barco.model.lookup.APPLICATION_STATUS;
import com.barco.model.pojo.EtlAccount;
import com.barco.model.pojo.aoe.Organization;
import com.barco.model.repository.ETLAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Optional;

/**
 * @author Nabeel Ahmed
 */
@Service
public class EtlAccountDetailsService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EtlAccountDetailsService.class);

    private final ETLAccountRepository etlAccountRepository;

    public EtlAccountDetailsService(ETLAccountRepository etlAccountRepository) {
        this.etlAccountRepository = etlAccountRepository;
    }

    /**
     * Loads the user details for a given username. It first attempts to find an active EtlAccount
     * by the associated AppUser's username. If not found, it then attempts to find an active EtlAccount
     * by the associated AppUser's email. If neither is found, it throws a UsernameNotFoundException.
     *
     * @param username The username or email of the user to load.
     * @return UserDetails containing the user's information and authorities.
     * @throws UsernameNotFoundException if no active user is found with the given username or email.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LOGGER.info("Loading user details for username: {}", username);
        Optional<EtlAccount> etlAccount = this.etlAccountRepository.findByAppUserUsernameAndStatus(username, APPLICATION_STATUS.ACTIVE);
        if (etlAccount.isPresent()) {
            return buildUserSessionDetail(etlAccount.get());
        }
        throw new UsernameNotFoundException("User not found with username or email: " + username);
    }

    private EtlAccountSessionDetail buildUserSessionDetail(EtlAccount etlAccount) {
        EtlAccountSessionDetail sessionDetail = new EtlAccountSessionDetail();
        sessionDetail.setId(etlAccount.getId())
            .setUuid(etlAccount.getUuid())
            .setIpAddress(etlAccount.getIpAddress())
            .setOrgAccount(etlAccount.getOrgAccount())
            .setIsSystem(etlAccount.getIsSystem());
        // Set active profile
        sessionDetail.setAccountProfile(etlAccount.getAppProfile() != null && etlAccount.getAppProfile().getTokenDetail() != null ?
            etlAccount.getAppProfile().getTokenDetail().getToken() : null);
        // Set user details if the associated AppUser
        if (!BarcoUtil.isNull(etlAccount.getAppUser())) {
            sessionDetail
                .setFirstName(etlAccount.getAppUser().getFirstName())
                .setLastName(etlAccount.getAppUser().getLastName())
                .setUsername(etlAccount.getAppUser().getUsername())
                .setEmail(etlAccount.getAppUser().getEmail())
                .setPassword(etlAccount.getAppUser().getPassword());
        }
        // Set organization details if the EtlAccount is associated with an Organization
        if (!BarcoUtil.isNull(etlAccount.getOrganization())) {
            Organization organization = etlAccount.getOrganization();
            OrganizationDetail orgDetail = new OrganizationDetail()
                .setId(organization.getId())
                .setUuid(organization.getUuid())
                .setName(organization.getName());
            sessionDetail.setOrganization(orgDetail);
        }
        return sessionDetail;
    }
}
