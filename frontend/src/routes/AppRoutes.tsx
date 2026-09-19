import { Route, Routes } from "react-router-dom";
import AdminDashboardPage from "../pages/AdminDashboardPage";
import AdminOrderDetailPage from "../pages/AdminOrderDetailPage";
import AdminOrdersPage from "../pages/AdminOrdersPage";
import AdminProductDetailPage from "../pages/AdminProductDetailPage";
import AdminProductsPage from "../pages/AdminProductsPage";
import AdminUserDetailPage from "../pages/AdminUserDetailPage";
import AdminUsersPage from "../pages/AdminUsersPage";
import AdminVendorDetailPage from "../pages/AdminVendorDetailPage";
import AdminVendorsPage from "../pages/AdminVendorsPage";
import AccountDashboardPage from "../pages/AccountDashboardPage";
import CartPage from "../pages/CartPage";
import CategoryPage from "../pages/CategoryPage";
import CheckoutPage from "../pages/CheckoutPage";
import HomePage from "../pages/HomePage";
import LoginPage from "../pages/LoginPage";
import NotFoundPage from "../pages/NotFoundPage";
import OrderDetailPage from "../pages/OrderDetailPage";
import OrdersPage from "../pages/OrdersPage";
import ProductDetailPage from "../pages/ProductDetailPage";
import ProductsPage from "../pages/ProductsPage";
import RegisterPage from "../pages/RegisterPage";
import VendorDashboardPage from "../pages/VendorDashboardPage";
import VendorStorefrontPage from "../pages/VendorStorefrontPage";
import VendorOrderDetailPage from "../pages/VendorOrderDetailPage";
import VendorOrdersPage from "../pages/VendorOrdersPage";
import VendorProductFormPage from "../pages/VendorProductFormPage";
import VendorProductsPage from "../pages/VendorProductsPage";
import WishlistPage from "../pages/WishlistPage";
import AdminLayout from "../layouts/AdminLayout";
import CustomerLayout from "../layouts/CustomerLayout";
import PublicLayout from "../layouts/PublicLayout";
import VendorLayout from "../layouts/VendorLayout";
import ProtectedRoute from "./ProtectedRoute";
import RoleRoute from "./RoleRoute";

export default function AppRoutes() {
  return (
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
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
