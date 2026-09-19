import {
  parseProductSearchParams,
  updateProductSearchParams,
} from "./productSearchParams";

it("hydrates supported filters and safely ignores invalid URL values", () => {
  const query = parseProductSearchParams(
    new URLSearchParams(
      "q=tea&category=2&vendor=3&minPrice=5&maxPrice=30&sort=priceDesc&page=2",
    ),
  );

  expect(query).toMatchObject({
    search: "tea",
    categoryId: 2,
    vendorId: 3,
    minPrice: 5,
    maxPrice: 30,
    sort: "priceDesc",
    page: 2,
  });
  expect(
    parseProductSearchParams(new URLSearchParams("category=nope&sort=popular&page=-2")),
  ).toMatchObject({ categoryId: undefined, sort: "newest", page: 0 });
});

it("updates URL filters and resets pagination", () => {
  const next = updateProductSearchParams(
    new URLSearchParams("q=tea&page=4&category=2"),
    { q: "coffee", category: undefined },
  );

  expect(next.toString()).toBe("q=coffee");
});
