import os
import joblib
import pandas as pd

from sklearn.model_selection import train_test_split
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import Pipeline
from sklearn.metrics import accuracy_score, classification_report


DATA_PATH = "data/support_tickets_dataset.csv"
MODEL_DIR = "models"


def main():
    # Load dataset
    df = pd.read_csv(DATA_PATH)

    # Keep only rows with required values
    df = df.dropna(subset=["text", "category", "urgency"])

    print(f"Dataset size: {len(df)}")

    X = df["text"]
    y_category = df["category"]
    y_priority = df["urgency"]

    # Split once so both models use the same train/test records
    X_train, X_test, y_cat_train, y_cat_test, y_pri_train, y_pri_test = (
        train_test_split(
            X,
            y_category,
            y_priority,
            test_size=0.2,
            random_state=42,
            stratify=y_category
        )
    )

    # Category model
    category_model = Pipeline([
        ("tfidf", TfidfVectorizer(
            lowercase=True,
            stop_words="english",
            ngram_range=(1, 2)
        )),
        ("classifier", LogisticRegression(
            max_iter=1000
        ))
    ])

    # Priority model
    priority_model = Pipeline([
        ("tfidf", TfidfVectorizer(
            lowercase=True,
            stop_words="english",
            ngram_range=(1, 2)
        )),
        ("classifier", LogisticRegression(
            max_iter=1000
        ))
    ])

    # Train
    print("\nTraining category model...")
    category_model.fit(X_train, y_cat_train)

    print("Training priority model...")
    priority_model.fit(X_train, y_pri_train)

    # Evaluate
    category_predictions = category_model.predict(X_test)
    priority_predictions = priority_model.predict(X_test)

    category_accuracy = accuracy_score(
        y_cat_test,
        category_predictions
    )

    priority_accuracy = accuracy_score(
        y_pri_test,
        priority_predictions
    )

    print("\n===== CATEGORY MODEL =====")
    print(f"Accuracy: {category_accuracy:.4f}")
    print(classification_report(
        y_cat_test,
        category_predictions
    ))

    print("\n===== PRIORITY MODEL =====")
    print(f"Accuracy: {priority_accuracy:.4f}")
    print(classification_report(
        y_pri_test,
        priority_predictions
    ))

    # Save models
    os.makedirs(MODEL_DIR, exist_ok=True)

    joblib.dump(
        category_model,
        f"{MODEL_DIR}/category_model.joblib"
    )

    joblib.dump(
        priority_model,
        f"{MODEL_DIR}/priority_model.joblib"
    )

    print("\nModels saved successfully.")


if __name__ == "__main__":
    main()