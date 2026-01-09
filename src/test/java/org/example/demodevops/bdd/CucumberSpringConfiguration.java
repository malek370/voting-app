package org.example.demodevops.bdd;

import com.voting.votingapp.VotingAppApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(classes = VotingAppApplication.class)
@ActiveProfiles("test")
public class CucumberSpringConfiguration {
}
