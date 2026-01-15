# 📋 Explication des Types de Tests dans la Pipeline CI

## 🎯 Vue d'ensemble

Cette pipeline CI/CD exécute automatiquement **4 types de tests** sur le backend et vérifie le build du frontend.

---

## 🧪 **1. TESTS UNITAIRES** (`*Test.java`)

### 📍 **Fichiers concernés :**
- `AuthServiceTest.java` (5 tests)
- `ReservationServiceTest.java` (10 tests)

### 🔍 **Qu'est-ce que c'est ?**
Les tests unitaires vérifient le comportement **d'une seule classe/méthode isolée**, en mockant toutes les dépendances externes (base de données, autres services, etc.).

### 💡 **Exemple concret :**
```java
@Test
void LoginRetourneUtilisateurQuandIdentifiantsCorrects() {
    // On mock le repository (pas de vraie base de données)
    when(utilisateurRepository.findByEmail("test@test.com"))
        .thenReturn(Optional.of(utilisateur));
    
    // On teste uniquement la logique du service
    Utilisateur result = authService.login("test@test.com", "password");
    
    // Vérification
    assertEquals(utilisateur, result);
}
```

### ✅ **Avantages :**
- ⚡ **Rapides** (pas de base de données réelle)
- 🎯 **Ciblés** (testent une seule fonctionnalité)
- 🔄 **Isolés** (ne dépendent pas d'autres composants)

### 📊 **Dans la pipeline :**
```yaml
- name: 🧪 Exécution des tests unitaires
  run: mvn test -Dtest="*Test" -DfailIfNoTests=false
```

---

## 🔗 **2. TESTS D'INTÉGRATION** (`*IntegrationTest.java`)

### 📍 **Fichiers concernés :**
- `ReservationServiceIntegrationTest.java` (10 tests)

### 🔍 **Qu'est-ce que c'est ?**
Les tests d'intégration vérifient l'interaction entre **plusieurs composants réels** (service + repository + base de données H2 en mémoire).

### 💡 **Exemple concret :**
```java
@Test
@SpringBootTest
@Transactional
void CreerReservationRetourneReservationQuandDonneesValides() {
    // On utilise une VRAIE base de données H2 (en mémoire)
    Reservation result = reservationService.creerReservation(
        salle.getId(), utilisateur.getId(), dateDebut, dateFin
    );
    
    // On vérifie que la réservation est bien SAUVEGARDÉE en base
    Reservation saved = reservationRepository.findById(result.getId()).orElse(null);
    assertNotNull(saved);
}
```

### ✅ **Avantages :**
- 🗄️ **Base de données réelle** (H2 en mémoire)
- 🔄 **Vérifie les interactions** entre composants
- ✅ **Détecte les problèmes de mapping JPA**

### 📊 **Dans la pipeline :**
```yaml
- name: 🔗 Exécution des tests d'intégration
  run: mvn test -Dtest="*IntegrationTest" -DfailIfNoTests=false
```

---

## 🌐 **3. TESTS SYSTÈME** (`*SystemTest.java`)

### 📍 **Fichiers concernés :**
- `AuthControllerSystemTest.java` (9 tests)

### 🔍 **Qu'est-ce que c'est ?**
Les tests système vérifient le comportement **end-to-end** en simulant des requêtes HTTP réelles avec `MockMvc`. Ils testent les **endpoints REST** complets.

### 💡 **Exemple concret :**
```java
@Test
void RegisterRetourne201QuandDonneesValides() throws Exception {
    RegisterDTO registerDTO = new RegisterDTO("test@test.com", "password123");
    
    // On simule une requête HTTP POST réelle
    mockMvc.perform(post("/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerDTO)))
        .andExpect(status().isCreated())
        .andExpect(content().string("Compte créé avec succès"));
    
    // On vérifie que l'utilisateur est bien créé en base
    assertTrue(utilisateurRepository.findByEmail("test@test.com").isPresent());
}
```

### ✅ **Avantages :**
- 🌐 **Teste les endpoints HTTP** complets
- 🔐 **Vérifie la gestion des sessions**
- 📡 **Simule le comportement réel** d'un client

### 📊 **Dans la pipeline :**
```yaml
- name: 🌐 Exécution des tests système
  run: mvn test -Dtest="*SystemTest" -DfailIfNoTests=false
```

---

## ⚡ **4. TESTS DE PERFORMANCE** (`*PerformanceTest.java`)

### 📍 **Fichiers concernés :**
- `AuthServicePerformanceTest.java` (7 tests)

### 🔍 **Qu'est-ce que c'est ?**
Les tests de performance vérifient que les opérations s'exécutent **rapidement** et restent performantes même avec **plusieurs utilisateurs simultanés**.

### 💡 **Exemple concret :**
```java
@Test
void RegisterDoitEtreRapidePourUnUtilisateur() {
    long startTime = System.currentTimeMillis();
    
    authService.register("test@test.com", "password123");
    
    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;
    
    // On vérifie que l'inscription prend moins de 1 seconde
    assertTrue(duration < 1000, 
        "L'inscription doit prendre moins de 1 seconde");
}
```

### ✅ **Avantages :**
- ⏱️ **Détecte les ralentissements**
- 🔄 **Teste la charge** (100 utilisateurs, connexions concurrentes)
- 📈 **Assure une bonne expérience utilisateur**

### 📊 **Dans la pipeline :**
```yaml
- name: ⚡ Exécution des tests de performance
  run: mvn test -Dtest="*PerformanceTest" -DfailIfNoTests=false
```

---

## 🏗️ **5. BUILD FRONTEND** (Vérification de compilation)

### 📍 **Fichiers concernés :**
- Tous les fichiers React/Vite (`App.jsx`, `api.js`, etc.)

### 🔍 **Qu'est-ce que c'est ?**
On vérifie que le frontend **compile sans erreur** et génère les fichiers de production.

### 💡 **Ce qui est vérifié :**
- ✅ Syntaxe JavaScript/JSX correcte
- ✅ Imports valides
- ✅ Pas d'erreurs de compilation TypeScript/JSX
- ✅ Génération des fichiers optimisés (`dist/`)

### 📊 **Dans la pipeline :**
```yaml
- name: 🏗️ Build de production
  run: npm run build
```

---

## 📊 **Résumé des Tests Exécutés**

| Type de Test | Nombre | Fichiers | Temps estimé |
|-------------|--------|----------|--------------|
| **Tests unitaires** | 15 | `*Test.java` | ~5-10s |
| **Tests d'intégration** | 10 | `*IntegrationTest.java` | ~10-15s |
| **Tests système** | 9 | `*SystemTest.java` | ~15-20s |
| **Tests de performance** | 7 | `*PerformanceTest.java` | ~20-30s |
| **Build frontend** | - | React/Vite | ~10-15s |
| **TOTAL** | **41+ tests** | - | **~60-90s** |

---

## 🚀 **Déclenchement de la Pipeline**

La pipeline se déclenche automatiquement :
- ✅ À chaque **push** sur `main`, `master`, ou `develop`
- ✅ À chaque **pull request** vers ces branches
- ✅ Tu peux aussi la lancer manuellement depuis l'onglet "Actions" de GitHub

---

## 📈 **Résultats**

Après chaque exécution, tu peux voir :
- ✅ **Statut** : Succès ✅ ou Échec ❌
- 📊 **Rapports de tests** : Téléchargeables en artefacts
- 🔍 **Logs détaillés** : Pour comprendre les erreurs

---

## 🔧 **Configuration**

- **Java** : Version 21 (Temurin)
- **Node.js** : Version 20
- **Base de données de test** : H2 (en mémoire)
- **OS** : Ubuntu Latest

---

## 💡 **Pourquoi ces tests sont importants ?**

1. **Tests unitaires** → Détectent les bugs rapidement
2. **Tests d'intégration** → Vérifient que les composants fonctionnent ensemble
3. **Tests système** → Garantissent que l'API fonctionne pour les clients
4. **Tests de performance** → Assurent une bonne expérience utilisateur
5. **Build frontend** → Vérifient que le code compile sans erreur

**Résultat** : Tu peux déployer en confiance ! 🎉
