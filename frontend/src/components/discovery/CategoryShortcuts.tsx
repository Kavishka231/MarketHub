import { Link } from "react-router-dom";
import { useCategories } from "../../hooks/useMarketplace";

export default function CategoryShortcuts() {
  const categories = useCategories();

  if (!categories.data?.length) {
    return null;
  }

  return (
    <nav aria-label="Browse categories" className="flex flex-wrap justify-center gap-2">
      {categories.data.slice(0, 6).map((category) => (
        <Link
          className="rounded-full border bg-white px-4 py-2 text-sm hover:border-brand-500"
          key={category.id}
          to={`/categories/${category.id}`}
        >
          {category.name}
        </Link>
      ))}
    </nav>
  );
}
