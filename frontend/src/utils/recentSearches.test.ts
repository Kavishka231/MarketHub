import {
  clearRecentSearches,
  loadRecentSearches,
  removeRecentSearch,
  saveRecentSearch,
} from "./recentSearches";

beforeEach(() => localStorage.clear());

it("deduplicates, validates, and limits recent searches", () => {
  ["tea", "coffee", "books", "toys", "shoes", "phones", "TEA"].forEach(
    saveRecentSearch,
  );

  expect(loadRecentSearches()).toEqual([
    "TEA",
    "phones",
    "shoes",
    "toys",
    "books",
    "coffee",
  ]);
});

it("recovers malformed storage and supports remove and clear", () => {
  localStorage.setItem("markethub:recent-product-searches", "not-json");
  expect(loadRecentSearches()).toEqual([]);

  saveRecentSearch("tea");
  expect(removeRecentSearch("tea")).toEqual([]);
  saveRecentSearch("coffee");
  clearRecentSearches();
  expect(loadRecentSearches()).toEqual([]);
});
