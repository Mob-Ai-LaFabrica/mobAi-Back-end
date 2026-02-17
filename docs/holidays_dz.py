from datetime import date
from typing import Dict

HOLIDAY_FEATURE_NAMES = []


def get_holiday_features_for_date(target_date: date) -> Dict[str, float]:
    return {name: 0.0 for name in HOLIDAY_FEATURE_NAMES}
