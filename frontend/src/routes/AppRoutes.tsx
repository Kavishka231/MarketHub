import { lazy, Suspense } from "react";
import { Route, Routes } from "react-router-dom";
import AdminLayout from "../layouts/AdminLayout";
import CustomerLayout from "../layouts/CustomerLayout";
import PublicLayout from "../layouts/PublicLayout";
import VendorLayout from "../layouts/VendorLayout";
import HomePage from "../pages/HomePage";
import LoginPage from "../pages/LoginPage";
import RegisterPage from "../pages/RegisterPage";
import ProtectedRoute from "./ProtectedRoute";
import RoleRoute from "./RoleRoute";

const AccountDashboardPage = lazy(() => import("../pages/AccountDashboardPage"));
const AdminDashboardPage = lazy(() => import("../pages/AdminDashboardPage"));
const AdminAuditLogsPage = lazy(() => import("../pages/AdminAuditLogsPage"));
const AdminAuditLogDetailPage = lazy(() => import("../pages/AdminAuditLogDetailPage"));
const AdminOrderDetailPage = lazy(() => import("../pages/AdminOrderDetailPage"));
const AdminOrdersPage = lazy(() => import("../pages/AdminOrdersPage"));
const AdminProductDetailPage = lazy(() => import("../pages/AdminProductDetailPage"));
const AdminProductsPage = lazy(() => import("../pages/AdminProductsPage"));
const AdminUserDetailPage = lazy(() => import("../pages/AdminUserDetailPage"));
const AdminUsersPage = lazy(() => import("../pages/AdminUsersPage"));
const AdminVendorDetailPage = lazy(() => import("../pages/AdminVendorDetailPage"));
const AdminVendorsPage = lazy(() => import("../pages/AdminVendorsPage"));
const CartPage = lazy(() => import("../pages/CartPage"));
const CategoryPage = lazy(() => import("../pages/CategoryPage"));
const CheckoutPage = lazy(() => import("../pages/CheckoutPage"));
const NotFoundPage = lazy(() => import("../pages/NotFoundPage"));
const OrderDetailPage = lazy(() => import("../pages/OrderDetailPage"));
const OrdersPage = lazy(() => import("../pages/OrdersPage"));
const ProductDetailPage = lazy(() => import("../pages/ProductDetailPage"));
const ProductsPage = lazy(() => import("../pages/ProductsPage"));
const VendorDashboardPage = lazy(() => import("../pages/VendorDashboardPage"));
const VendorOrderDetailPage = lazy(() => import("../pages/VendorOrderDetailPage"));
const VendorOrdersPage = lazy(() => import("../pages/VendorOrdersPage"));
const VendorProductFormPage = lazy(() => import("../pages/VendorProductFormPage"));
const VendorProductsPage = lazy(() => import("../pages/VendorProductsPage"));
const VendorStorefrontPage = lazy(() => import("../pages/VendorStorefrontPage"));
const WishlistPage = lazy(() => import("../pages/WishlistPage"));

function PageLoader() {
  return <p className="mx-auto max-w-7xl px-6 py-12">Loading page…</p>;
}

export default function AppRoutes() {
  return (
    <Suspense fallback={<PageLoader />}>
      <Routes>
        <Route element={<PublicLayout />}>
          <Route index element={<HomePage />} />
          <Route path="products" element={<ProductsPage />} />
          <Route path="products/:productId" element={<ProductDetailPage />} />
          <Route path="categories/:categoryId" element={<CategoryPage />} />
          <Route path="vendors/:vendorId" element={<VendorStorefrontPage />} />
          <Route path="login" element={<LoginPage />} />
          <Route path="register" element={<RegisterPage />} />
        </Route>

        <Route element={<ProtectedRoute />}>
          <Route element={<RoleRoute roles={["CUSTOMER"]} />}>
            <Route path="account" element={<CustomerLayout />}>
              <Route index element={<AccountDashboardPage />} />
              <Route path="cart" element={<CartPage />} />
              <Route path="checkout" element={<CheckoutPage />} />
              <Route path="orders" element={<OrdersPage />} />
              <Route path="orders/:orderId" element={<OrderDetailPage />} />
              <Route path="wishlist" element={<WishlistPage />} />
            </Route>
          </Route>
          <Route element={<RoleRoute roles={["VENDOR"]} />}>
            <Route path="vendor" element={<VendorLayout />}>
              <Route index element={<VendorDashboardPage />} />
              <Route path="products" element={<VendorProductsPage />} />
              <Route path="products/new" element={<VendorProductFormPage />} />
              <Route path="products/:productId/edit" element={<VendorProductFormPage />} />
              <Route path="orders" element={<VendorOrdersPage />} />
              <Route path="orders/:orderId" element={<VendorOrderDetailPage />} />
            </Route>
          </Route>
          <Route element={<RoleRoute roles={["ADMIN"]} />}>
            <Route path="admin" element={<AdminLayout />}>
              <Route index element={<AdminDashboardPage />} />
              <Route path="users" element={<AdminUsersPage />} />
              <Route path="users/:userId" element={<AdminUserDetailPage />} />
              <Route path="vendors" element={<AdminVendorsPage />} />
              <Route path="vendors/:vendorId" element={<AdminVendorDetailPage />} />
              <Route path="products" element={<AdminProductsPage />} />
              <Route path="products/:productId" element={<AdminProductDetailPage />} />
              <Route path="orders" element={<AdminOrdersPage />} />
              <Route path="orders/:orderId" element={<AdminOrderDetailPage />} />
              <Route path="audit-logs" element={<AdminAuditLogsPage />} />
              <Route path="audit-logs/:auditId" element={<AdminAuditLogDetailPage />} />
            </Route>
          </Route>
        </Route>
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </Suspense>
  );
}