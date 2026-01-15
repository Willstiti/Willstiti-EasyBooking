# 🚀 Pipeline CI/CD GitHub Actions

## 📋 Vue d'ensemble

Cette pipeline CI/CD exécute automatiquement tous les tests du projet EasyBooking à chaque push ou pull request.

## 🎯 Ce qui est testé

### Backend (Java/Spring Boot)
- ✅ **Tests unitaires** (15 tests)
- ✅ **Tests d'intégration** (10 tests)
- ✅ **Tests système** (9 tests)
- ✅ **Tests de performance** (7 tests)
- ✅ **Build Maven**

### Frontend (React/Vite)
- ✅ **Compilation** (vérification de la syntaxe)
- ✅ **Build de production**

## 📁 Structure

```
.github/
├── workflows/
│   └── ci.yml              # Pipeline CI/CD principale
├── TESTS_EXPLICATION.md    # Explication détaillée des types de tests
└── README.md               # Ce fichier
```

## 🔄 Déclenchement automatique

La pipeline se déclenche automatiquement sur :
- ✅ Push sur `main`, `master`, ou `develop`
- ✅ Pull request vers ces branches
- ✅ Déclenchement manuel depuis l'onglet "Actions" de GitHub

## 📊 Résultats

Après chaque exécution :
1. **Statut** : ✅ Succès ou ❌ Échec
2. **Rapports** : Téléchargeables en artefacts
3. **Logs** : Disponibles dans l'onglet "Actions"

## 🔍 Pour plus de détails

Consulte le fichier [TESTS_EXPLICATION.md](./TESTS_EXPLICATION.md) pour comprendre chaque type de test.

## 🛠️ Configuration

- **Java** : 21 (Temurin)
- **Node.js** : 20
- **OS** : Ubuntu Latest
- **Base de données de test** : H2 (en mémoire)
