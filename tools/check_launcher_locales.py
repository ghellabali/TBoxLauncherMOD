"""Check complete native launcher catalogs and their Android format arguments."""

from collections import Counter
import re
from pathlib import Path
import xml.etree.ElementTree as ET


RES = Path(__file__).resolve().parents[1] / "launcher/src/main/res"
FORMAT = re.compile(r"%(?:[0-9]+\$)?[-+#0,(]*[0-9]*(?:\.[0-9]+)?[aAbBcCdDeEfFgGhHoOsSxX%]")


def catalog(folder):
    result = {}
    for entry in ET.parse(RES / folder / "strings.xml").getroot().findall("string"):
        key = entry.attrib["name"]
        if key in result:
            raise ValueError(f"Repeated resource {folder}/{key}")
        result[key] = entry
    return result


def main():
    base = catalog("values")
    for folder in ("values-fr", "values-ar"):
        localized = catalog(folder)
        if base.keys() != localized.keys():
            raise ValueError(f"{folder} resource differences: {base.keys() ^ localized.keys()}")
        for key, entry in localized.items():
            text = entry.text or ""
            if not text.strip() or any(mark in text for mark in ("▁", "⁇", "�", "<unk>")):
                raise ValueError(f"Invalid translated text in {folder}/{key}")
            if entry.get("formatted") != base[key].get("formatted"):
                raise ValueError(f"Formatting flag mismatch in {folder}/{key}")
            if Counter(FORMAT.findall(text)) != Counter(FORMAT.findall(base[key].text or "")):
                raise ValueError(f"Format arguments differ in {folder}/{key}")
        print(f"{folder}: {len(localized)} strings verified")


if __name__ == "__main__":
    main()
