# Credit Agricole Test – Kotlin Multiplatform App

Cette application illustre une architecture Clean + MVI reposant sur Kotlin Multiplatform, Compose Multiplatform et une base réseau Ktor/Ktorfit injectée via Koin.  
L’écran principal présente les comptes d’une cliente répartis entre le Crédit Agricole et les autres banques conformément aux règles métier (RG00–RG04).

---

## **Overview**

## **TODO**

### 🚀 Fonctionnalités
- [ ] Implémenter l'architecture MVI / Clean Architecture
- [ ] Ajouter les libraries
- [ ] Implémenter les fonctionnalités principales
- [ ] Gérer les cas d’erreur

---

## **Features**

✔ **Architecture** – MVI / Clean Architecture.
✔ **Library** – Implémenter les librairies
✔ **Architecture** – Data / Domain / Presentation
✔ **Account Listing** – Displays a list of accounts.
✔ **Detail Account** – Display detail account.

## Sources / Bibliothèque

| Bibliothèque | Lien | Rôle principal |
|--------------|------|----------------|
| **Kotlin Multiplatform** | [kotlinlang.org](https://kotlinlang.org/docs/multiplatform.html) | Partage de code entre Android et iOS |
| **Compose Multiplatform** | [github.com/JetBrains/compose-multiplatform](https://github.com/JetBrains/compose-multiplatform) | UI déclarative partagée |
| **Koin 4** | [insert-koin.io](https://insert-koin.io/docs/reference/koin-mp/start) | Injection de dépendances multiplateforme |
| **Ktor 3** | [ktor.io/docs/http-client](https://ktor.io/docs/http-client.html) | Client HTTP multiplateforme |
| **Ktorfit 2** | [github.com/Foso/Ktorfit](https://github.com/Foso/Ktorfit) | DSL Retrofit-like au-dessus de Ktor |
| **kotlinx.serialization** | [kotlinlang.org/docs/serialization.html](https://kotlinlang.org/docs/serialization.html) | (De)serialisation JSON |
| **Coroutines** | [kotlinlang.org/docs/coroutines-overview.html](https://kotlinlang.org/docs/coroutines-overview.html) | Concurrence structurée |

---

## Architecture

### Vue d’ensemble

- **Clean Architecture** stricte : séparation Domain / Data / Presentation.
- **MVI** (Model–View–Intent) pour la couche présentation.
- **Kotlin Multiplatform** pour partager les use cases, repositories, modèles et logique de présentation.
- **Compose Multiplatform** pour l’UI partagée Android/iOS.

```
androidApp | iosApp
      \        /
      └── shared (composeApp)
            ├── data
            ├── domain
            ├── presentation
            └── di
```

### Couche Data

- Interfaces `BankingApi` déclarées avec Ktorfit.
- Implémentations runtime via `createBankingApi()` (pas de génération KSP requise).
- `BankRepositoryImpl` interroge l’API Firebase (`banks.json`) et retombe sur `BanksFixtures` (banks.json local) en cas d’erreur réseau.
- Mapping explicite DTO → Domain (`BankDto.toDomain()` etc.) pour préserver les invariants (MG/iso, signes, tri).

Exemple rapide :
```kotlin
override suspend fun getBanks(): List<Bank> = runCatching {
    api.getBanks().banks.map { it.toDomain() }
}.getOrElse { BanksFixtures.response.banks.map { it.toDomain() } }
```

### Couche Domain

- Value objects (`BankId`, `Money`, …) et entités (`Bank`, `Account`, `Operation`).
- Interfaces `BankRepository`, `AccountRepository`, `OperationRepository`.
- Use cases (`GetBanksUseCase`, `GetAccountsForBankUseCase`, …) appliquent les règles métier RG00–RG04 : séparation CA / autres banques, tri alphabétique, totalisation.
- `DispatcherProvider` injecté pour garder les suspending functions testables.

Exemple :
```kotlin
val (caBanks, otherBanks) = repository.getBanks().partition { it.isCreditAgricole }
val caAccounts = caBanks.flatMap { it.accounts }.sortedBy { it.name.lowercase() }
```

### Couche Presentation

- Compose Multiplatform + MVI maison (`AccountsState`, `AccountsIntent`, `AccountsEffect`).
- `AccountsRoute` observe le state, dispatch les intents et adapte l’affichage (groupes dépliants, totaux).
- Rendu sous forme de sections : « Mes Comptes » (CA) et « Autres Banques » (groupes collapsibles par banque), aligné avec RG01–RG04.

Extrait simplifié :
```kotlin
ExpandableGroup(
    headerTitle = group.title,
    headerValue = group.totalAmountFormatted,
    expanded = group.expanded,
    onToggle = { onIntent(AccountsIntent.ToggleGroup(group.groupId)) }
) { /* comptes de la banque */ }
```

### Injection (DI)

- Koin initialise les modules Core (dispatchers), Data (client HTTP + repositories), Domain (use cases) et Presentation.
- L’initialisation multiplateforme passe par `initKoin(baseUrl = …)`.

---

## Design System

- Dossier `designsystem` : palette, tailles, composants réutilisables.
- `ThemeDefaults` centralise les couleurs et espacements.
- Composants majeurs :
  - `TopTitle`, `SectionTitle` pour la hiérarchie typographique.
  - `ListRow` / `ListRowTrailings` pour la présentation homogène des comptes.
  - `ExpandableGroup` pour les sections dépliables (RG01).
  - `BottomBar` pour la navigation tabulaire.

Ces briques permettent de réutiliser la même charte sur Android et iOS en conservant un thème unique.

---

## Aller plus loin

- Ajouter un store MVI complet avec appels aux use cases.
- Étendre la data layer aux autres endpoints (accounts / operations) si l’API évolue.
- Écrire des tests unitaires multiplateformes sur les use cases et mappers.

---

`test_entretien_cats`  
[Consigne PDF](test_mobile_CA.pdf)
