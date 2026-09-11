package com.evidlock;

import com.evidlock.persistence.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@SpringBootTest
public class SecurityProxyRbacTest {

    @Test
    @DisplayName("Verify UserAuditor Security Proxy blocks ALL mutation operations with 403 Forbidden")
    public void testAuditorProxyStrictlyReadOnly() {
        UserAuditor auditor = new UserAuditor();
        auditor.setEmail("auditor@evidlock.gov");
        auditor.setRole("AUDITOR");
        auditor.setAgencyCode("OIG-04");

        // Read operations allowed
        ResponseEntity<List<Case>> casesResp = auditor.getCases();
        Assertions.assertEquals(HttpStatus.OK, casesResp.getStatusCode());

        ResponseEntity<List<EvidenceItem>> evidenceResp = auditor.getEvidenceItems("MPD-01", null);
        Assertions.assertEquals(HttpStatus.OK, evidenceResp.getStatusCode());

        ResponseEntity<List<CustodyEvent>> eventsResp = auditor.getCustodyEvents(1L);
        Assertions.assertEquals(HttpStatus.OK, eventsResp.getStatusCode());

        // Mutation operations strictly denied
        Assertions.assertEquals(HttpStatus.FORBIDDEN, auditor.createCase(new Case()).getStatusCode());
        Assertions.assertEquals(HttpStatus.FORBIDDEN, auditor.updateCase(new Case()).getStatusCode());
        Assertions.assertEquals(HttpStatus.FORBIDDEN, auditor.uploadEvidence(null, 1L, "TAG", "desc", null).getStatusCode());
        Assertions.assertEquals(HttpStatus.FORBIDDEN, auditor.updateEvidence(new EvidenceItem()).getStatusCode());
        Assertions.assertEquals(HttpStatus.FORBIDDEN, auditor.deleteEvidence(1L).getStatusCode());
        Assertions.assertEquals(HttpStatus.FORBIDDEN, auditor.setLegalHold(1L, true).getStatusCode());
        Assertions.assertEquals(HttpStatus.FORBIDDEN, auditor.initiateTransfer(new TransferStepUpDto()).getStatusCode());
        Assertions.assertEquals(HttpStatus.FORBIDDEN, auditor.acceptTransfer(new TransferStepUpDto()).getStatusCode());
        Assertions.assertEquals(HttpStatus.FORBIDDEN, auditor.rejectTransfer(1L, "reason").getStatusCode());
    }

    @Test
    @DisplayName("Verify UserInvestigator Security Proxy cannot delete evidence or toggle legal hold")
    public void testInvestigatorProxyPermissions() {
        UserInvestigator investigator = new UserInvestigator();
        investigator.setEmail("investigator@evidlock.gov");
        investigator.setRole("INVESTIGATOR");
        investigator.setAgencyCode("MPD-01");

        // Allowed to view cases
        Assertions.assertEquals(HttpStatus.OK, investigator.getCases().getStatusCode());

        // Prohibited from deleting evidence
        Assertions.assertEquals(HttpStatus.FORBIDDEN, investigator.deleteEvidence(1L).getStatusCode());

        // Prohibited from modifying legal holds
        Assertions.assertEquals(HttpStatus.FORBIDDEN, investigator.setLegalHold(1L, true).getStatusCode());
    }

    @Test
    @DisplayName("Verify UserCustodian Security Proxy cannot create cases, delete evidence, or toggle legal hold")
    public void testCustodianProxyPermissions() {
        UserCustodian custodian = new UserCustodian();
        custodian.setEmail("custodian@evidlock.gov");
        custodian.setRole("CUSTODIAN");
        custodian.setAgencyCode("SFL-02");

        // Allowed to view cases
        Assertions.assertEquals(HttpStatus.OK, custodian.getCases().getStatusCode());

        // Prohibited from creating investigation cases
        Assertions.assertEquals(HttpStatus.FORBIDDEN, custodian.createCase(new Case()).getStatusCode());

        // Prohibited from deleting evidence
        Assertions.assertEquals(HttpStatus.FORBIDDEN, custodian.deleteEvidence(1L).getStatusCode());

        // Prohibited from modifying legal holds
        Assertions.assertEquals(HttpStatus.FORBIDDEN, custodian.setLegalHold(1L, true).getStatusCode());
    }

    @Test
    @DisplayName("Verify UserSupervisor Security Proxy can manage legal holds and view all metrics")
    public void testSupervisorProxyPermissions() {
        UserSupervisor supervisor = new UserSupervisor();
        supervisor.setEmail("supervisor@evidlock.gov");
        supervisor.setRole("SUPERVISOR");
        supervisor.setAgencyCode("MPD-01");

        // Allowed to view metrics
        Assertions.assertEquals(HttpStatus.OK, supervisor.getDashboardMetrics().getStatusCode());

        // Allowed to toggle legal hold
        ResponseEntity<EvidenceItem> holdResp = supervisor.setLegalHold(1L, true);
        Assertions.assertEquals(HttpStatus.OK, holdResp.getStatusCode());
        Assertions.assertTrue(holdResp.getBody().isLegalHold());
    }
}
