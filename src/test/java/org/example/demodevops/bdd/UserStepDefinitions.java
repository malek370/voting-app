package org.example.demodevops.bdd;

import com.voting.votingapp.model.User;
import com.voting.votingapp.repositories.UserRepository;
import io.cucumber.java.Before;
import io.cucumber.java.fr.*;
import io.cucumber.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

public class UserStepDefinitions {
    @Autowired
    private UserRepository userRepository;
    private List<User> userList;

    @Before
    public void setup() {
        userRepository.deleteAll();
    }

    @Etantdonné("une base de données vide")
    public void une_base_de_donnees_vide() {
        userRepository.deleteAll();
        assertThat(userRepository.count()).isZero();
    }

    @Quand("je crée un utilisateur {string} avec l'email {string}")
    public void je_cree_un_utilisateur(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        userRepository.save(user);
    }

    @Alors("l'utilisateur {string} existe dans la base")
    public void l_utilisateur_existe(String username) {
        List<User> users = userRepository.findAll();
        assertThat(users).anyMatch(u -> u.getUsername().equals(username));
    }

    @Etantdonné("les utilisateurs suivants existent:")
    public void les_utilisateurs_suivants_existent(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps();
        for (Map<String, String> row : rows) {
            User user = new User();
            user.setUsername(row.get("username"));
            user.setEmail(row.get("email"));
            userRepository.save(user);
        }
    }

    @Quand("je demande la liste des utilisateurs")
    public void je_demande_la_liste() {
        userList = userRepository.findAll();
    }

    @Alors("je reçois {int} utilisateurs")
    public void je_recois_utilisateurs(int count) {
        assertThat(userList).hasSize(count);
    }

    @Etantdonné("un utilisateur {string} existe avec l'ID {int}")
    public void un_utilisateur_existe_avec_id(String username, int id) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username.toLowerCase() + "@email.com");
        userRepository.save(user);
    }

    @Quand("je supprime l'utilisateur avec l'ID {int}")
    public void je_supprime_utilisateur(String id) {
        userRepository.deleteById(id);
    }

    @Alors("l'utilisateur avec l'ID {int} n'existe plus")
    public void utilisateur_n_existe_plus(String id) {
        assertThat(userRepository.existsById(id)).isFalse();
    }
}