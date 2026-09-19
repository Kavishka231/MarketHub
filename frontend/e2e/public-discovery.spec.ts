import { expect, test } from "@playwright/test";

const emptyPage = {
  content: [],
  page: 0,
  size: 12,
  totalElements: 0,
  totalPages: 0,
};

test.beforeEach(async ({ page }) => {
  await page.route("**/api/categories", (route) =>
    route.fulfill({
      json: [{ id: 7, name: "Electronics", description: "Devices and accessories" }],
    }),
  );
  await page.route("**/api/products**", (route) => route.fulfill({ json: emptyPage }));
});

test("public discovery loads and preserves marketplace navigation", async ({ page }) => {
  await page.goto("/");

  await expect(page.getByRole("heading", { name: "Shop from trusted local vendors" })).toBeVisible();
  await expect(page.getByRole("link", { name: "Electronics" })).toBeVisible();

  await page.getByRole("link", { name: "Explore all products" }).click();
  await expect(page).toHaveURL(/\/products$/);
  await expect(page.getByRole("heading", { name: /products/i })).toBeVisible();
});