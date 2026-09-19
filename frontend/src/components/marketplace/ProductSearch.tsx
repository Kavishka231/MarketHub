import { useEffect, useState } from "react";

interface ProductSearchProps {
  value: string;
  onChange: (value: string) => void;
}

export default function ProductSearch({
  value,
  onChange,
}: ProductSearchProps) {
  const [input, setInput] = useState(value);

  useEffect(() => {
    setInput(value);
  }, [value]);

  useEffect(() => {
    if (input === value) {
      return;
    }
    const timer = window.setTimeout(() => onChange(input.trim()), 350);
    return () => window.clearTimeout(timer);
  }, [input, onChange, value]);

  return (
    <div className="flex gap-2" role="search">
      <label className="sr-only" htmlFor="product-search">
        Search products
      </label>
      <input
        className="w-full rounded border px-3 py-2"
        id="product-search"
        maxLength={100}
        onChange={(event) => setInput(event.target.value)}
        placeholder="Search products, categories, or stores"
        value={input}
      />
      {input && (
        <button
          className="rounded border px-4 py-2"
          onClick={() => {
            setInput("");
            onChange("");
          }}
          type="button"
        >
          Clear search
        </button>
      )}
    </div>
  );
}
