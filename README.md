# Credit Agricole Test – Kotlin Multiplatform App

Cette application multiplateforme (Android & iOS) illustre une architecture Clean + MVI en Kotlin 2.3.  
La liste des comptes est segmentée entre le Crédit Agricole et les autres banques en respectant les règles métier RG00–RG04.

---

## Aperçu rapide
- Kotlin Multiplatform + Compose Multiplatform pour partager la logique métier et l’UI.
- Flux unidirectionnel : use cases Domain, repositories Data, ViewModel MVI en Presentation.
- Couche réseau Ktor/Ktorfit avec implémentation manuelle et cache en mémoire du snapshot `banks.json` pour limiter les appels API.
- Règles métiers (séparation CA, sections collapsibles, tri alphabétique) implémentées dans la présentation.

---

## Prise en main

### Prérequis
- JDK 17 (AGP 8.11 l’exige).
- Android Studio Koala ou plus récent avec le plugin Kotlin Multiplatform.
- Xcode 15+ et CocoaPods 1.13+ pour lancer l’application iOS.
- `./gradlew --version` doit fonctionner (Gradle wrapper fourni).

### Installation
1. Cloner le dépôt :  
   `git clone https://github.com/<votre-org>/credit-agricole-test.git`
2. Se placer dans le projet :  
   `cd credit-agricole-test`
3. (Optionnel) Vérifier que la configuration Compose est OK :  
   `./gradlew doctor`

### Lancer l’application

#### Android
- Depuis Android Studio : ouvrir le projet et exécuter la configuration `composeApp`.
- En ligne de commande :  
  `./gradlew :composeApp:installDebug` puis lancer l’application sur l’émulateur/appareil.

#### iOS (Compose Multiplatform)
1. Synchroniser le framework partagé si nécessaire :  
   `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`
2. Ouvrir `iosApp/iosApp.xcodeproj` et sélectionner le schéma `iosApp`.
3. Choisir un simulateur (ARM64) puis `⌘R`.

### Tests
Exécuter l’ensemble des tests multiplateformes :  
`./gradlew :composeApp:check`

---

### Configuration
- **Base URL** : par défaut `https://cdf-test-mobile-default-rtdb.europe-west1.firebasedatabase.app/`.  
  Pour cibler un autre backend, injecter la propriété Koin `base_url` lors de l’appel à `initKoin { properties(mapOf(PROPERTY_BASE_URL to "<url>")) }`.
- **Jeu de données local** : le fichier `banks.json` à la racine reproduit la payload distante et est consommé par les tests / fixtures.
- **Cache** : le `BankingApi` conserve la dernière réponse `banks.json` en mémoire. Un futur `forceRefresh` pourra être propagé depuis la couche présentation (pull-to-refresh).

### Commandes utiles
- `./gradlew :composeApp:assembleDebug` : build Android sans installer.
- `./gradlew :composeApp:lint` : lint Kotlin et Compose.
- `./gradlew :composeApp:test` : tests JVM (unitaires) pour la partie commune.
- `./gradlew --stop` : arrêter les daemons Gradle en cas de souci de build.

---

## Règles métier (RG00–RG04)
- **RG00 – Séparation CA / autres banques**  
  Les données sont partitionnées dans `AccountListViewModel.toSections`.
- **RG01 – Cellules bancaires dépliantes**  
  `BankCollapsibleCell` gère un état `isExpanded` et déclenche un `ToggleExpanded`.
- **RG02 – Sections CA puis autres banques**  
  La construction des sections impose l’ordre « Banques Crédit Agricole » avant « Autres banques ».
- **RG03 – Tri alphabétique des comptes**  
  Les comptes sont triés via `accounts.sortedBy { it.name.lowercase() }` avant rendu.
- **RG04 – Comptes listés lors du dépliage**  
  Quand une banque est ouverte, ses comptes (`AccountItemUi.title`) sont affichés avec gestion des états (chargement, erreur, vide).

---

## Architecture

### Data
- `HttpClientFactory` construit un client Ktor commun et un builder Ktorfit.
- `Ktorfit.createBankingApi()` fournit une implémentation manuelle de `BankingApi`.  
  Le snapshot `banks.json` est mis en cache en mémoire (`Mutex` + `cachedBanks`) pour réduire les appels réseau.
- Repositories (`BankRepositoryImpl`, `AccountRepositoryImpl`, `OperationRepositoryImpl`) encapsulent les accès API et remontent les modèles Domain.

### Domain
- Modèles riches (`Bank`, `Account`, `Operation`, `Money`, `BankId`, …) garantissant les invariants.
- Use cases `GetBanksUseCase`, `GetAccountsForBankUseCase`, `GetOperationsForAccountUseCase` ordonnent et filtrent les données avant exposition.
- `DispatcherProvider` injecté pour garder la logique testable.

### Presentation
- `AccountListViewModel` implémente un MVI simple (`AccountListIntent`, `AccountListState`, `AccountListResult`).
- `AccountListReducer` applique les effets sur l’état (chargement, toggles, erreurs).
- `AccountListScreen` (Compose) rend les sections et les cellules dépliables avec gestion des retours ViewModel.

---

## Flux réseau & cache
- Base URL par défaut : `https://cdf-test-mobile-default-rtdb.europe-west1.firebasedatabase.app/`.
- `GET banks.json` est appelé une seule fois, puis servi depuis le cache mémoire tant que l’application reste active.
- Le cache peut être rafraîchi en appelant `fetchBanksSnapshot(forceRefresh = true)` (extension prévue pour les évolutions).

---

## Structure du projet
```
.
├── composeApp/
│   ├── src/commonMain/
│   │   ├── data/        // API, DTO, repositories, réseau
│   │   ├── domain/      // modèles, repositories, use cases
│   │   └── presentation // MVI, UI Compose partagée
│   ├── src/androidMain/ // Entrée Android + intégrations spécifiques
│   └── src/iosMain/     // Entrée iOS (ComposeUIViewController)
├── iosApp/              // Projet Xcode hôte
├── banks.json           // Snapshot de test local
├── gradle/              // Wrapper & versions
└── README.md
```

---

## Dépendances principales

| Bibliothèque | Version | Rôle |
|--------------|---------|------|
| Kotlin Multiplatform | 2.3.0 | Partage de code |
| Compose Multiplatform | 1.10.0 | UI déclarative |
| Android Gradle Plugin | 8.11.2 | Build Android |
| Coroutines | 1.10.2 | Concurrence |
| Ktor | 3.4.0 | Client HTTP |
| Ktorfit | 2.7.2 | DSL type-safe pour Ktor |
| Koin | 4.1.1 | Injection de dépendances |
| kotlinx.serialization | 1.7.x | JSON |

---

## Roadmap / TODO
- Implémenter `BankRepository.getMockBanks()` pour fournir un fallback hors-ligne.
- Ajouter des tests unitaires sur les use cases et le reducer.
- Gérer l’invalidation du cache `banks.json` (pull-to-refresh, TTL).
- Ajouter des aperçus Compose (Preview) supplémentaires et des tests UI.

---

`test_mobile_CA.pdf` contient l’énoncé initial.
