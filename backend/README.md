## EasyBooking – Backend

### 1. Contexte et technologies

- **Langage** : Java (Spring Boot)
- **Framework backend** : Spring Boot (injection de dépendances, configuration auto, tests `@SpringBootTest`)
- **Web** : Spring Web MVC (`@RestController`, `@GetMapping`, `@PostMapping`, `@DeleteMapping`)
- **Accès aux données** : Spring Data JPA (`ReservationRepository`, `SalleRepository`, `UtilisateurRepository`)
- **Sécurité** : Spring Security (configurée dans `SecurityConfig`, filtres désactivés en profil `test` pour certains tests)
- **Structure principale** :
  - **Controller** : `AuthController`, `ReservationController`, `SalleController`, `UtilisateurController`
  - **Service** : `AuthService`, `ReservationService`, `SalleService`, `UtilisateurService`
  - **DTO** : `LoginDTO`, `RegisterDTO`, `ReservationDTO`
  - **Entités** : `Reservation`, `Salle`, `Utilisateur`

### 2. Typologie des tests

Les tests sont dans `src/test/java/com/efrei/easybooking` et couvrent plusieurs niveaux :

- **Tests unitaires (services isolés avec mocks)**  
  - `ReservationServiceTest`  
  - `AuthServiceTest`
- **Tests d’intégration (service + JPA + base de données de test)**  
  - `ReservationServiceIntegrationTest`
- **Tests de performance (temps d’exécution)**  
  - `AuthServicePerformanceTest`  
  - `ReservationServicePerformanceTest`
- **Tests système / API (MockMvc)**  
  - `AuthControllerSystemTest`
- **Test de démarrage global**  
  - `EasyBookingApplicationTests` (`contextLoads`)

Nombre total de tests : **47** méthodes annotées `@Test`.

---

### 3. Détail des tests unitaires

#### 3.1 `ReservationServiceTest`

| Nom du test | Comportement attendu |
|------------|----------------------|
| `CreerReservationRetourneReservationQuandDonneesValides` | Crée une réservation quand la salle et l’utilisateur existent et qu’il n’y a aucun chevauchement de créneau. |
| `CreerReservationLanceExceptionQuandSalleNonTrouvee` | Lance `"Salle non trouvée"` si l’ID de salle n’existe pas. |
| `CreerReservationLanceExceptionQuandUtilisateurNonTrouve` | Lance `"Utilisateur non trouvé"` si l’ID utilisateur n’existe pas. |
| `CreerReservationLanceExceptionQuandSalleDejaReservee` | Refuse la réservation avec `"La salle est déjà réservée sur ce créneau"` lorsqu’un créneau se chevauche totalement. |
| `CreerReservationLanceExceptionQuandSalleDejaReserveeChevauchementDebut` | Refuse la réservation si elle commence avant mais finit pendant une réservation existante. |
| `CreerReservationLanceExceptionQuandSalleDejaReserveeChevauchementFin` | Refuse la réservation si elle commence pendant mais finit après une réservation existante. |
| `CreerReservationLanceExceptionQuandDateDebutApresDateFin` | Lance `"La date de début doit être avant la date de fin"` si `dateDebut >= dateFin`. |
| `CreerReservationReussitQuandReservationAutreSalle` | Autorise une réservation si le conflit de créneau concerne une autre salle. |
| `GetReservationsByUserRetourneListeReservationsQuandUtilisateurExiste` | Retourne la liste des réservations pour un utilisateur ayant plusieurs réservations. |
| `GetReservationsByUserRetourneListeVideQuandAucuneReservation` | Retourne une liste vide si l’utilisateur n’a aucune réservation. |
| `GetReservationsByUserLanceExceptionQuandUtilisateurNonTrouve` | Lance `"Utilisateur non trouvé"` si l’ID utilisateur n’existe pas. |

#### 3.2 `AuthServiceTest`

| Nom du test | Comportement attendu |
|------------|----------------------|
| `LoginRetourneUtilisateurQuandIdentifiantsCorrects` | `login` retourne l’utilisateur quand l’email existe et le mot de passe est correct. |
| `loginLanceExceptionQuandEmailInconnu` | `login` lance `"Identifiants incorrects"` si l’email n’existe pas. |
| `loginLanceExceptionQuandMotDePasseIncorrect` | `login` lance `"Identifiants incorrects"` si le mot de passe est faux. |
| `RegisterSauvegardeEtRetourneUtilisateurQuandEmailInexistant` | `register` crée et retourne un utilisateur quand l’email n’est pas encore utilisé. |
| `registerLanceExceptionQuandEmailDejaExistant` | `register` lance `"Utilisateur déjà existant"` si l’email est déjà en base. |

---

### 4. Tests d’intégration – `ReservationServiceIntegrationTest`

Ces tests démarrent un **vrai contexte Spring Boot** avec profil `test` et une base de données de test. Ils valident l’intégration entre `ReservationService`, les repositories JPA et les entités.

| Nom du test | Comportement attendu |
|------------|----------------------|
| `CreerReservationRetourneReservationQuandDonneesValides` | Crée et persiste une réservation en base avec les bons liens (salle, utilisateur, dates). |
| `CreerReservationLanceExceptionQuandSalleNonTrouvee` | Lance `"Salle non trouvée"` si l’ID salle n’existe pas. |
| `CreerReservationLanceExceptionQuandUtilisateurNonTrouve` | Lance `"Utilisateur non trouvé"` si l’ID utilisateur n’existe pas. |
| `CreerReservationLanceExceptionQuandSalleDejaReservee` | Refuse une réservation qui chevauche totalement une réservation existante (même salle). |
| `CreerReservationLanceExceptionQuandSalleDejaReserveeChevauchementDebut` | Refuse une réservation dont le créneau chevauche le début d’une réservation existante. |
| `CreerReservationLanceExceptionQuandSalleDejaReserveeChevauchementFin` | Refuse une réservation dont le créneau chevauche la fin d’une réservation existante. |
| `CreerReservationReussitQuandReservationAutreSalle` | Accepte une réservation sur une autre salle même si le créneau est pris ailleurs. |
| `CreerReservationReussitQuandReservationMemeSalleMaisCreneauDifferent` | Accepte une réservation sur la même salle mais sur un créneau totalement différent. |
| `CreerReservationLanceExceptionQuandDateDebutApresDateFin` | Lance `"La date de début doit être avant la date de fin"` si `dateDebut > dateFin`. |
| `GetReservationsByUserRetourneListeReservationsQuandUtilisateurExiste` | Retourne 2 réservations persistées pour un utilisateur donné. |
| `GetReservationsByUserRetourneListeVideQuandAucuneReservation` | Retourne une liste vide pour un utilisateur sans réservations. |
| `GetReservationsByUserRetourneSeulementReservationsUtilisateur` | Ne retourne que les réservations de l’utilisateur demandé, même si d’autres utilisateurs ont des réservations similaires. |
| `GetReservationsByUserLanceExceptionQuandUtilisateurNonTrouve` | Lance `"Utilisateur non trouvé"` si l’ID n’existe pas en base de test. |

---

### 5. Tests de performance

Les tests de performance mesurent le temps d’exécution de scénarios critiques et imposent des seuils maximum.

#### 5.1 `AuthServicePerformanceTest`

| Nom du test | Comportement attendu |
|------------|----------------------|
| `RegisterDoitEtreRapidePourUnUtilisateur` | Une inscription unique doit prendre moins de 1 seconde et l’utilisateur doit être persisté. |
| `LoginDoitEtreRapidePourUnUtilisateur` | Une connexion unique doit prendre moins de 500 ms et retourner un utilisateur valide. |
| `RegisterDoitGererPlusieursUtilisateursRapidement` | 100 inscriptions successives : temps moyen par inscription < 50 ms, et 100 utilisateurs en base. |
| `LoginDoitGererPlusieursConnexionsRapidement` | 100 connexions successives : temps moyen par connexion < 30 ms. |
| `RegisterAvecEmailExistantDoitEtreRapide` | Deuxième inscription avec le même email : exception `"Utilisateur déjà existant"` levée en < 100 ms. |
| `LoginAvecIdentifiantsIncorrectsDoitEtreRapide` | `login` avec mauvais mot de passe : exception `"Identifiants incorrects"` levée en < 100 ms. |
| `RegisterEtLoginSequenceDoitEtreEfficace` | 50 séquences `register + login` : temps moyen par séquence < 100 ms et nombre d’utilisateurs en base cohérent. |

#### 5.2 `ReservationServicePerformanceTest`

| Nom du test | Comportement attendu |
|------------|----------------------|
| `CreerReservationDoitEtreRapidePourUnUtilisateur` | Création d’une réservation simple doit respecter un temps maximum défini dans le test. |
| `CreerReservationDoitGererPlusieursReservationsRapidement` | Série de créations de réservations (volume important) avec un temps moyen acceptable par réservation. |
| `GetReservationsByUserDoitEtreRapideAvecBeaucoupDeDonnees` | Récupération des réservations d’un utilisateur très chargé reste sous un seuil de temps défini. |

*(Les noms exacts peuvent varier, mais ces tests ont pour but de valider les performances sur la création et la lecture de réservations.)*

---

### 6. Tests système / API – `AuthControllerSystemTest`

Ces tests utilisent `MockMvc` avec un contexte Spring complet et le profil `test`. Ils valident les endpoints d’authentification.

| Endpoint / Test | Comportement attendu |
|-----------------|----------------------|
| `RegisterRetourne201QuandDonneesValides` | `POST /register` avec un `RegisterDTO` valide retourne 201 + `"Compte créé avec succès"`. |
| `RegisterCreeUtilisateurEnBase` | Après `POST /register`, l’utilisateur existe en base avec le bon email et mot de passe. |
| `RegisterRetourne400QuandEmailDejaExistant` | Deuxième `POST /register` avec le même email retourne 400 Bad Request. |
| `LoginRetourne200QuandIdentifiantsCorrects` | `POST /login` après inscription retourne 200 + `"Connexion réussie"`. |
| `LoginCreeSessionAvecUserIdEtEmail` | `POST /login` crée une session contenant `userId` et `email`. |
| `LoginRetourne400QuandEmailInconnu` | `POST /login` avec un email inexistant retourne 400 Bad Request. |
| `LoginRetourne400QuandMotDePasseIncorrect` | `POST /login` avec un mot de passe incorrect retourne 400 Bad Request. |
| `LogoutRetourne200EtInvalideSession` | `POST /logout` après connexion retourne 200 + `"Déconnexion réussie"` et invalide la session. |
| `LoginApresRegisterFonctionne` | Enchaînement `register` puis `login` fonctionne correctement. |
| `RegisterAvecEmailDifferentFonctionne` | Deux inscriptions avec des emails différents retournent 201 et créent bien deux utilisateurs en base. |

Des tests similaires pourraient être ajoutés pour les endpoints de réservation (`POST /reservations`, `GET /reservations`, `DELETE /reservations/{id}`) afin de couvrir complètement la partie réservation côté API.

---

### 7. Test de démarrage – `EasyBookingApplicationTests`

| Nom du test | Comportement attendu |
|------------|----------------------|
| `contextLoads` | Vérifie que le contexte Spring Boot démarre sans erreur (sanity check global). |

---

### 8. Synthèse qualité

- **Couverture fonctionnelle** : les tests couvrent les principaux cas métier de l’authentification et des réservations, y compris les cas d’erreur (identifiants invalides, email déjà utilisé, créneaux de réservation qui se chevauchent, utilisateurs inexistants).  
- **Multiniveau de tests** : unitaire, intégration, système et performance, ce qui permet de détecter aussi bien les erreurs de logique locale que les problèmes d’intégration ou de lenteur.  
- **Messages d’erreur robustes** : les messages fonctionnels (`"Salle non trouvée"`, `"Utilisateur non trouvé"`, `"La salle est déjà réservée sur ce créneau"`, `"La date de début doit être avant la date de fin"`, `"Identifiants incorrects"`, `"Utilisateur déjà existant"`, etc.) sont explicitement vérifiés par les tests.  
- **Base pour la suite** : la structure actuelle facilite l’ajout de nouveaux tests (par exemple sur les contrôleurs de réservation, la validation des emails, ou des règles métier supplémentaires) tout en conservant une bonne lisibilité et une bonne séparation des responsabilités.  

