Vue d'ensemble
Cette application backend est construite avec Spring Boot et utilise MongoDB comme base de données. Elle fournit une API RESTful pour l'authentification des utilisateurs, avec des fonctionnalités de connexion, d'inscription, de déconnexion, de réinitialisation de mot de passe et de désinscription, ainsi que l'accès aux informations de profil.
Configuration de sécurité
Le fichier SecurityConfig configure la sécurité de l'application :

Désactive CSRF pour faciliter les tests avec Postman
Autorise l'accès public aux endpoints d'authentification (/auth/login, /auth/register, etc.)
Sécurise tous les autres endpoints en exigeant une authentification
Configure la gestion des sessions sans état (stateless) pour JWT
Configure CORS pour permettre les requêtes depuis le frontend Vue.js (http://localhost:8080)

Structure du code
Contrôleurs
AuthController : Gère toutes les requêtes liées à l'authentification

/auth/login : Connexion avec numéro étudiant et mot de passe
/auth/register : Inscription d'un nouvel utilisateur
/auth/reset-password/{studentNumber} : Réinitialisation du mot de passe
/auth/logout : Déconnexion (blackliste le token JWT)
/auth/unregister : Suppression de compte

UserController : Gère les requêtes liées aux informations utilisateur

/api/me : Récupère les informations personnelles de l'utilisateur connecté à partir de son token JWT

Modèles de données
User : Représente un utilisateur dans la base de données

Champs : id, username, passwordHash, studentNumber, fullName, dateOfBirth, address

TokenBlacklist : Stocke les tokens JWT révoqués

Champs : id, token, expiration

DTOs (Data Transfer Objects)
RegisterRequest : Structure les données pour l'inscription

Champs : username, password, fullName, dateOfBirth, address, student

Services
AuthService : Logique métier pour l'authentification

Vérification des identifiants lors de la connexion
Génération de numéros étudiants uniques
Hachage des mots de passe
Validation des tokens JWT

TokenBlacklistService : Gestion de la liste noire des tokens

Ajout de tokens à la liste noire
Vérification si un token est blacklisté
Nettoyage périodique des tokens expirés

Repositories
UserRepository : Accès aux données des utilisateurs

Recherche par nom d'utilisateur et numéro étudiant

TokenBlacklistRepository : Accès aux données des tokens blacklistés

Recherche de tokens
Suppression des tokens expirés

Sécurité
JwtUtil : Utilitaire pour la gestion des tokens JWT

Génération de nouveaux tokens
Validation des tokens existants
Extraction des informations (username, date d'expiration)

Flux d'authentification

Inscription :

L'utilisateur fournit ses informations
Un numéro étudiant unique est généré
Le mot de passe est haché avec BCrypt
Un token JWT est généré et retourné


Connexion :

L'utilisateur fournit son numéro étudiant et mot de passe
Les identifiants sont vérifiés
Un token JWT est généré et retourné en cas de succès


Déconnexion :

Le token JWT est ajouté à la liste noire
Le token reste valide jusqu'à son expiration mais est rejeté


Réinitialisation de mot de passe :

L'utilisateur fournit son numéro étudiant et nouveau mot de passe
Le mot de passe est haché et mis à jour


Désinscription :

L'utilisateur est authentifié via son token JWT
Le compte est supprimé de la base de données


Récupération des informations utilisateur :

Le token JWT est extrait de l'en-tête d'autorisation
Le nom d'utilisateur est extrait du token
Les informations personnelles de l'utilisateur sont récupérées et retournées



Configuration CORS
L'application est configurée pour accepter les requêtes depuis l'application frontend Vue.js exécutée sur http://localhost:8080.
Endpoints API
Authentification

POST /auth/login : Connexion utilisateur

Corps de la requête : { "studentNumber": "123456", "password": "motdepasse" }
Réponse : { "token": "jwt_token" }


POST /auth/register : Inscription utilisateur

Corps de la requête : { "username": "user1", "password": "motdepasse", "fullName": "Nom Complet", "dateOfBirth": "1990-01-01", "address": "Adresse" }
Réponse : { "message": "User registered successfully", "token": "jwt_token", "studentNumber": "123456" }


PUT /auth/reset-password/{studentNumber} : Réinitialisation du mot de passe

Corps de la requête : { "newPassword": "nouveaumotdepasse" }
Réponse : { "message": "Password reset successfully", "passwordHash": "hashed_password" }


DELETE /auth/logout : Déconnexion

En-tête : Authorization: Bearer jwt_token
Réponse : { "message": "Logged out successfully" }


DELETE /auth/unregister : Suppression de compte

En-tête : Authorization: Bearer jwt_token
Réponse : { "message": "User unregistered successfully" }



Utilisateur

GET /api/me : Récupérer les informations de l'utilisateur connecté

En-tête : Authorization: Bearer jwt_token
Réponse : { "id": "user_id", "username": "user1", "studentNumber": "123456", "fullName": "Nom Complet", "dateOfBirth": "1990-01-01", "address": "Adresse" }
