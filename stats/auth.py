"""Vérification des JWT délivrés par Keycloak (même principe que l'API Java)."""
import os

import jwt
from fastapi import Depends, HTTPException
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer

# Où télécharger les clés PUBLIQUES de Keycloak (adresse interne au réseau Docker)
JWK_SET_URI = os.getenv("JWK_SET_URI", "http://localhost:8180/realms/sirene/protocol/openid-connect/certs")
# Valeur attendue dans le champ "iss" du jeton (adresse publique de Keycloak)
ISSUER_URI = os.getenv("ISSUER_URI", "http://localhost:8180/realms/sirene")

# Télécharge les clés une fois, les garde en cache et choisit la bonne grâce au "kid" du jeton
jwks = jwt.PyJWKClient(JWK_SET_URI, cache_keys=True)

# Lit l'en-tête "Authorization: Bearer <JWT>" et ajoute le bouton "Authorize" dans /docs.
# auto_error=False : on renvoie nous-mêmes un 401 (FastAPI renverrait 403 par défaut).
bearer = HTTPBearer(auto_error=False, bearerFormat="JWT")


def non_authentifie(message: str) -> HTTPException:
    return HTTPException(status_code=401, detail=message, headers={"WWW-Authenticate": "Bearer"})


def utilisateur_courant(credentials: HTTPAuthorizationCredentials | None = Depends(bearer)) -> dict:
    """Renvoie le contenu du jeton s'il est valide, sinon 401."""
    if credentials is None:
        raise non_authentifie("Jeton manquant")
    try:
        cle = jwks.get_signing_key_from_jwt(credentials.credentials)
        return jwt.decode(
            credentials.credentials,
            cle.key,
            algorithms=["RS256"],          # n'accepte que la signature asymétrique RSA
            issuer=ISSUER_URI,             # le jeton doit venir de NOTRE Keycloak
            options={"verify_aud": False},  # comme côté Java : pas de contrôle d'audience
        )
    except jwt.ExpiredSignatureError:
        raise non_authentifie("Jeton expiré")
    except jwt.PyJWTError as erreur:
        raise non_authentifie(f"Jeton invalide : {erreur}")


def exiger_role(*roles: str):
    """Dépendance FastAPI : 403 si l'utilisateur n'a aucun des rôles demandés."""
    def verifier(payload: dict = Depends(utilisateur_courant)) -> dict:
        roles_utilisateur = payload.get("realm_access", {}).get("roles", [])
        if not set(roles) & set(roles_utilisateur):
            raise HTTPException(status_code=403, detail=f"Rôle requis : {' ou '.join(roles)}")
        return payload
    return verifier
