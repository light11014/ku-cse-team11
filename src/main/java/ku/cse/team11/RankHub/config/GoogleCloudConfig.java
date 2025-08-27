package ku.cse.team11.RankHub.config;



import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.v3.TranslationServiceSettings;
import com.google.cloud.translate.v3.TranslationServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;

@Configuration
public class GoogleCloudConfig {

    @Value("${app.google.credentials-path}") // application.yml에서 주입
    private String credentialsPath;

    @Bean
    public TranslationServiceClient translationServiceClient() throws Exception {
        GoogleCredentials creds = GoogleCredentials
                .fromStream(new FileInputStream(credentialsPath));
        TranslationServiceSettings settings = TranslationServiceSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(creds))
                .build();
        return TranslationServiceClient.create(settings);
    }
}