package com.khoros.batch.service;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.IdTokenCredentials;
import com.google.auth.oauth2.IdTokenProvider;
import com.google.auth.oauth2.IdTokenProvider.Option;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Arrays;


@Service
@Log4j2
public class CloudRunAuthenticator {


    // Use the Google Cloud metadata server to create an identity token and add it to the
    // HTTP request as part of an Authorization header.
    public String getIdTokenFromMetadataServer(String url) {
        // Construct the GoogleCredentials object which obtains the default configuration from your  working environment.

        try {
            GoogleCredentials googleCredentials = GoogleCredentials.getApplicationDefault();
            IdTokenCredentials idTokenCredentials =
                    IdTokenCredentials.newBuilder()
                            .setIdTokenProvider((IdTokenProvider) googleCredentials)
                            .setTargetAudience(url)
                            // Setting the ID token options.
                            .setOptions(Arrays.asList(Option.FORMAT_FULL, Option.LICENSES_TRUE))
                            .build();

            // Get the ID token. Once you've obtained the ID token, you can use it to make an authenticated call to the  target audience.
            String idToken = idTokenCredentials.refreshAccessToken().getTokenValue();
            log.info("Generated ID token for Cloud Run API call: " + idToken);

            return idToken;
        } catch (Exception e) {
            log.error(" Exception at getIdTokenFromMetadataServer : " + e.getMessage());
        }
        return null;
    }
}