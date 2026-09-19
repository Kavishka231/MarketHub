const storageKey = "markethub:recent-product-searches";
const maximumSearches = 6;

export function loadRecentSearches(): string[] {
  try {
    const parsed: unknown = JSON.parse(localStorage.getItem(storageKey) ?? "[]");
    if (!Array.isArray(parsed)) {
      return [];
    }
    return parsed
      .filter((value): value is string => typeof value === "string")
      .map((value) => value.trim())
      .filter(Boolean)
      .slice(0, maximumSearches);
  } catch {
    return [];
  }
}

export function saveRecentSearch(search: string): string[] {
  const normalized = search.trim().slice(0, 100);
  if (!normalized) {
    return loadRecentSearches();
  }

  const next = [
    normalized,
    ...loadRecentSearches().filter(
      (value) => value.toLowerCase() !== normalized.toLowerCase(),
    ),
  ].slice(0, maximumSearches);
  localStorage.setItem(storageKey, JSON.stringify(next));
  return next;
}

export function removeRecentSearch(search: string): string[] {
  const next = loadRecentSearches().filter((value) => value !== search);
  localStorage.setItem(storageKey, JSON.stringify(next));
  return next;
}

export function clearRecentSearches(): void {
  localStorage.removeItem(storageKey);
}
