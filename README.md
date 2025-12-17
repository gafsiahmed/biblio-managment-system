
# Système de Gestion de Bibliothèque (Spring Boot)

Application complète de gestion de bibliothèque développée avec Spring Boot 3, incluant la gestion des utilisateurs, des ressources (livres, médias), des prêts, et des réservations.

## Auteur 
Projet réalisé dans le cadre académique par **Ahmed Gafsi** -Développement Web Avancé (DWA)

## Fonctionnalités Principales
*   **Authentification & Sécurité** : Inscription, Connexion, Rôles (ADMIN, LIBRARIAN, USER).
*   **Gestion des Ressources** : Ajout, modification, recherche avancée, filtrage par bibliothèque.
*   **Prêts & Réservations** : Workflow complet de prêt (Emprunt, Retour, Renouvellement, Pénalités de retard) et file d'attente pour les réservations.
*   **Tableau de Bord** : Vues spécifiques pour Admin, Bibliothécaire et Utilisateur.
*   **Notifications** : Emails automatiques pour les rappels et confirmations.

## Prérequis
*   Java 17 ou supérieur
*   Maven 3.8+
*   MySQL 8.0
*   Docker & Docker Compose (optionnel pour le déploiement)

## Installation et Configuration

### 1. Cloner le projet
```bash
git clone https://github.com/gafsiahmed/biblio-managment-system.git
cd biblio-managment-system
```

### 2. Configuration Base de Données
Par défaut, l'application utilise une configuration de développement (H2 ou MySQL local).
Pour la production, modifiez `src/main/resources/application-prod.properties` ou utilisez les variables d'environnement.

### Exemple de configuration (application.properties)
Voici un exemple complet du fichier `src/main/resources/application.properties` pour une exécution locale ou via Docker :

```properties
spring.application.name=bibliotheque-management

# --- BASE DE DONNÉES ---
# Pour Docker (nom du service 'db' dans docker-compose)
# spring.datasource.url=jdbc:mysql://db:3306/library_db?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false
# Pour Local (localhost)
spring.datasource.url=jdbc:mysql://localhost:3306/library_db?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false

spring.datasource.username=root
spring.datasource.password=votre_mot_de_passe

# --- JPA / HIBERNATE ---
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.show-sql=true
spring.jpa.defer-datasource-initialization=true

# --- INITIALISATION DES DONNÉES ---
# Charger les données de test au démarrage
spring.sql.init.mode=always
spring.sql.init.data-locations=classpath:data-prod.sql

# --- EMAILS (Gmail SMTP) ---
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=votre_email@gmail.com
spring.mail.password=votre_mot_de_passe_app
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# --- FICHIERS ---
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# --- LOGGING ---
logging.level.root=INFO
logging.level.com.bibliotheque=INFO
spring.output.ansi.enabled=always
```

### 3. Lancer l'application
#### Via Maven
```bash
mvn spring-boot:run
```

#### Via Docker Compose (Recommandé pour test complet)
```bash
docker-compose up --build
```
Ceci lancera l'application sur le port 8080 et une base MySQL sur le port 3307.

## Accès à l'application
*   **URL** : `http://localhost:8080`

### Comptes de Test
Les comptes suivants sont automatiquement créés au démarrage (via `data-prod.sql` ou `data-dev.sql`) :

| Rôle | Nom d'utilisateur | Email | Mot de passe |
| :--- | :--- | :--- | :--- |
| **Administrateur** | `admin` | `admin@biblio.com` | `password` |
| **Bibliothécaire** | `librarian` | `librarian@biblio.com` | `password` |
| **Utilisateur** | `user` | `user@biblio.com` | `password` |

## Liste des Endpoints (Routes)

### Public / Authentification
*   `GET /` : Page d'accueil
*   `GET /login` : Page de connexion
*   `GET /register` : Page d'inscription
*   `POST /register` : Soumission de l'inscription
*   `GET /verify-email` : Validation de l'email
*   `GET /access-denied` : Page d'accès refusé

### Espace Utilisateur
*   `GET /user/dashboard` : Tableau de bord utilisateur (Prêts en cours, réservations, historique)
*   `GET /loans/my-loans` : Mes prêts détaillés
*   `GET /resources` : Catalogue et recherche simple
*   `GET /resources/search-advanced` : Recherche avancée

### Espace Bibliothécaire (`ROLE_LIBRARIAN`)
*   `GET /librarian` : Tableau de bord bibliothécaire (Retours attendus, validations)
*   `GET /loans/all` : Tous les prêts
*   `GET /loans/pending` : Prêts en attente de validation
*   `GET /loans/overdue` : Prêts en retard
*   `GET /loans/{id}/edit` : Édition d'un prêt

### Espace Administrateur (`ROLE_ADMIN`)
*   `GET /admin` : Tableau de bord administrateur (Statistiques, graphiques)
*   `GET /libraries` : Gestion des bibliothèques (Liste)
*   `GET /libraries/new` : Ajouter une bibliothèque
*   `GET /users` : Gestion des utilisateurs (Liste)
*   `GET /users/new` : Ajouter un utilisateur

## Documentation API (Swagger/OpenAPI)
L'API est documentée avec OpenAPI 3.
*   **Swagger UI** : `http://localhost:8080/swagger-ui.html`
*   **Spec JSON** : `http://localhost:8080/v3/api-docs`

## Tests
Lancer la suite de tests complète :
```bash
mvn test
```
Les tests couvrent :
*   Unitaires (Service Layer)
*   Intégration (Controller & Security)
*   Repository (JPA Queries)

## Architecture Technique

### Conception et Modèles
*   **Architecture en Couches** : Séparation stricte des responsabilités (Controller -> Service -> Repository).
*   **Domain-Driven Design (DDD)** : La logique métier réside dans les Services et les Entités du domaine, pas dans les contrôleurs.
*   **DTO Pattern** : Utilisation d'objets de transfert de données (DTO) pour découpler les entités de la base de données des contrats d'API.

### Sécurité
*   **Spring Security** : Protection par défaut de tous les endpoints.
*   **Contrôle d'Accès (RBAC)** : Gestion fine des permissions basée sur les rôles (ADMIN, LIBRARIAN, USER).
*   **Protection des Données** : Mots de passe hachés avec BCrypt, protection CSRF activée.

### Persistance
*   **JPA / Hibernate** : ORM pour la gestion des données relationnelles.
*   **Optimisation** : Utilisation de Lazy Loading et de requêtes JPQL optimisées pour éviter le problème N+1.
*   **Migrations** : Compatible avec Flyway/Liquibase (recommandé pour la production).

### Qualité du Code
*   **Tests** : Couverture complète avec JUnit 5 et Mockito (Unitaires) et @SpringBootTest (Intégration).
*   **Documentation** : Code documenté et API exposée via Swagger/OpenAPI.
*   **Gestion des Erreurs** : Gestion globale des exceptions avec @ControllerAdvice.

## Structure du Projet
*   `src/main/java/com/bibliotheque` : Code source
    *   `controller` : Endpoints REST/MVC
    *   `service` : Logique métier
    *   `repository` : Accès données
    *   `model` : Entités JPA
*   `src/test` : Tests unitaires et d'intégration
