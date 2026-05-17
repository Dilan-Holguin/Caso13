package com.eap08.domesticas.acceptance;

import io.cucumber.spring.ScenarioScope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@ScenarioScope
public class ScenarioContext {

    private ResponseEntity<String> lastResponse;
    private String currentEmail;
    private String currentRawPassword;
    private Long currentHogarId;
    private String currentJwt;
    private String externalJwt;

    public ResponseEntity<String> getLastResponse() {
        return lastResponse;
    }

    public void setLastResponse(ResponseEntity<String> response) {
        this.lastResponse = response;
    }

    public String getCurrentEmail() {
        return currentEmail;
    }

    public void setCurrentEmail(String currentEmail) {
        this.currentEmail = currentEmail;
    }

    public String getCurrentRawPassword() {
        return currentRawPassword;
    }

    public void setCurrentRawPassword(String currentRawPassword) {
        this.currentRawPassword = currentRawPassword;
    }

    public Long getCurrentHogarId() {
        return currentHogarId;
    }

    public void setCurrentHogarId(Long currentHogarId) {
        this.currentHogarId = currentHogarId;
    }

    public String getCurrentJwt() {
        return currentJwt;
    }

    public void setCurrentJwt(String currentJwt) {
        this.currentJwt = currentJwt;
    }

    public String getExternalJwt() {
        return externalJwt;
    }

    public void setExternalJwt(String externalJwt) {
        this.externalJwt = externalJwt;
    }
}
