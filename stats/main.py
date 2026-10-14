import os

import polars as pl
import psycopg
from fastapi import FastAPI, HTTPException

DB_URL = os.getenv("DB_URL", "postgresql://sirene:sirene@localhost:5432/sirene")

app = FastAPI(title="API Statistiques Sirene",
              description="Statistiques sur les codes activité, calculées avec Polars")


def charger_entreprises() -> pl.DataFrame:
    """Lit la table brute (simple SELECT, aucun GROUP BY) : tout le calcul est fait par Polars."""
    with psycopg.connect(DB_URL) as connexion:
        return pl.read_database(
            """SELECT activite_principale_unite_legale AS activite,
                      etat_administratif_unite_legale  AS etat,
                      tranche_effectifs_unite_legale   AS tranche,
                      date_creation_unite_legale       AS date_creation
               FROM mytable""",
            connexion,
        )


def compter(df: pl.DataFrame, colonne: str) -> pl.DataFrame:
    """Nombre d'entreprises, d'actives et de cessées par valeur de la colonne."""
    total = df.height
    return (
        df.group_by(colonne)
        .agg(
            pl.len().alias("nombre"),
            (pl.col("etat") == "A").sum().alias("actives"),
            (pl.col("etat") == "C").sum().alias("cessees"),
        )
        .with_columns((pl.col("nombre") / total * 100).round(2).alias("pourcentage"))
        .sort("nombre", descending=True)
    )


# GET /stats/activites?top=20
@app.get("/stats/activites")
def stats_activites(top: int = 20):
    df = charger_entreprises().drop_nulls("activite")
    return compter(df, "activite").head(top).to_dicts()


# GET /stats/activites/10.71C
@app.get("/stats/activites/{code}")
def stats_activite(code: str):
    df = charger_entreprises().filter(pl.col("activite") == code)
    if df.is_empty():
        raise HTTPException(status_code=404, detail=f"Aucune entreprise avec le code activité {code}")

    tranches = df.group_by("tranche").agg(pl.len().alias("nombre")).sort("nombre", descending=True)
    return {
        "activite": code,
        "nombre": df.height,
        "actives": df.filter(pl.col("etat") == "A").height,
        "cessees": df.filter(pl.col("etat") == "C").height,
        "tranchesEffectifs": tranches.to_dicts(),
    }


# GET /stats/divisions : regroupement par les 2 premiers chiffres du code NAF (ex : 62.01Z -> 62)
@app.get("/stats/divisions")
def stats_divisions(top: int = 20):
    df = (
        charger_entreprises()
        .drop_nulls("activite")
        .with_columns(pl.col("activite").str.slice(0, 2).alias("division"))
    )
    return compter(df, "division").head(top).to_dicts()


# GET /stats/creations : nombre d'entreprises créées par année
@app.get("/stats/creations")
def stats_creations():
    df = charger_entreprises().drop_nulls("date_creation")
    return (
        df.group_by(pl.col("date_creation").dt.year().alias("annee"))
        .agg(pl.len().alias("nombre"))
        .sort("annee")
        .to_dicts()
    )
