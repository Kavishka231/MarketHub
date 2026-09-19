import { useEffect, useState } from "react";
import {
  clearRecentSearches,
  loadRecentSearches,
  removeRecentSearch,
} from "../../utils/recentSearches";

interface RecentSearchesProps {
  refreshKey?: string;
  onSelect: (search: string) => void;
}

export default function RecentSearches({
  refreshKey,
  onSelect,
}: RecentSearchesProps) {
  const [searches, setSearches] = useState(loadRecentSearches);

  useEffect(() => {
    setSearches(loadRecentSearches());
  }, [refreshKey]);

  if (searches.length === 0) {
    return null;
  }

  return (
    <section aria-label="Recent searches" className="flex flex-wrap items-center gap-2">
      <span className="text-sm font-medium text-slate-600">Recent:</span>
      {searches.map((search) => (
        <span className="inline-flex rounded-full border bg-white" key={search}>
          <button className="px-3 py-1 text-sm" onClick={() => onSelect(search)} type="button">
            {search}
          </button>
          <button
            aria-label={`Remove ${search} from recent searches`}
            className="border-l px-2 text-slate-500"
            onClick={() => setSearches(removeRecentSearch(search))}
            type="button"
          >
            ×
          </button>
        </span>
      ))}
      <button
        className="text-sm underline"
        onClick={() => {
          clearRecentSearches();
          setSearches([]);
        }}
        type="button"
      >
        Clear recent searches
      </button>
    </section>
  );
}
