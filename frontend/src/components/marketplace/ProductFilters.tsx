import type { Category } from "../../types/marketplace";

interface ProductFiltersProps {
  categories: Category[];
  categoryId?: number;
  minPrice?: number;
  maxPrice?: number;
  onCategory: (id?: number) => void;
  onPrice: (min?: number, max?: number) => void;
}

export default function ProductFilters({
  categories,
  categoryId,
  minPrice,
  maxPrice,
  onCategory,
  onPrice,
}: ProductFiltersProps) {
  return (
    <details className="rounded-xl border bg-white p-4" open>
      <summary className="cursor-pointer font-semibold md:hidden">
        Filters
      </summary>
      <div className="mt-3 grid gap-3 md:mt-0 md:grid-cols-3">
        <label className="text-sm">
          Category
          <select
            aria-label="Category"
            className="mt-1 w-full rounded border p-2"
            onChange={(event) =>
              onCategory(
                event.target.value ? Number(event.target.value) : undefined,
              )
            }
            value={categoryId ?? ""}
          >
            <option value="">All categories</option>
            {categories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </label>
        <label className="text-sm">
          Minimum price
          <input
            aria-label="Minimum price"
            className="mt-1 w-full rounded border p-2"
            min="0"
            onChange={(event) =>
              onPrice(
                event.target.value ? Number(event.target.value) : undefined,
                maxPrice,
              )
            }
            type="number"
            value={minPrice ?? ""}
          />
        </label>
        <label className="text-sm">
          Maximum price
          <input
            aria-label="Maximum price"
            className="mt-1 w-full rounded border p-2"
            min="0"
            onChange={(event) =>
              onPrice(
                minPrice,
                event.target.value ? Number(event.target.value) : undefined,
              )
            }
            type="number"
            value={maxPrice ?? ""}
          />
        </label>
      </div>
    </details>
  );
}
